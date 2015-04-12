package aaron.sanfs.storageserver;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventListener;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.network.SocketListener;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.common.task.Task;
import aaron.sanfs.common.task.TaskMonitor;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.storageserver.event.*;
import aaron.sanfs.storageserver.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Storage server implementation
 * <p/>
 * It has two set of communication methods.
 * <p/>
 * It behaves as client for NameServer, send call to, receive call from NS
 * <p/>
 * It also behave as server for client, receive and handle calls, transport file
 * streams
 * <p/>
 * Finally, StorageServers inter-communicate for duplication and load balance
 *
 * @author dengshihong
 */
public class StorageServer implements TaskEventListener, CallListener,
        SocketListener {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(StorageServer.class);
    /**
     * Maximum number of task which can be running simultaneously
     */
    private static final int MAX_THREADS = 20;
    /**
     * Address of StorageServer as Server(ip:port)
     */
    private String address;
    /**
     * The Storage of the StorageServer
     */
    private final Storage storage;
    /**
     * connector for communication with NameServer
     */
    private ClientConnector connector = null;
    /**
     * connector for communication with StorageServer and Client
     */
    private XConnector xConnector = null;
    /**
     * Task list
     * <p/>
     * {taskId, task}
     */
    private Map<Long, Task> tasks = new HashMap<Long, Task>();
    /**
     * Files on migration
     * <p/>
     * {StorageAddress, filename list}
     */
    private Map<String, List<String>> onMigrateFile = new HashMap<String, List<String>>();
    /**
     * Files finished migration but have not reported to NameServer
     * <p/>
     * {StorageAddress, filename list}
     */
    private Map<String, List<String>> overMigrateFile = new HashMap<String, List<String>>();
    /**
     * Task Monitor, used to check task status.
     */
    private TaskMonitor taskMonitor = null;
    /**
     * Task thread executor
     */
    private ExecutorService taskExecutor = null;
    /**
     * Determine whether StorageServer has been initiated.
     */
    private boolean initialized = false;
    /**
     * Determine whether StorageServer has been register to NameServer
     */
    private Boolean registered = false;
    /**
     * For taskId generate
     */
    private Integer taskIDCount = 0;
    /**
     * NameServer taskId to handle this StorageServer's heartbeat
     */
    private long NStid = -1;

    /**
     * @param location Root directory for StorageServer
     * @throws java.io.IOException
     */
    public StorageServer(String location) throws IOException {
        storage = new Storage(location);
    }

    /**
     * @param port Monitor port for XConnector
     * @throws Exception
     * @see XConnector
     */
    public void initAndstart(int port) throws Exception {
        if (initialized) {
            logger.warn("StorgeServer has been initialized before, you can't do it twice.");
            return;
        } else {
            // check configuration
            if (null == Configuration.getInstance()) {
                throw new Exception(
                        "Initiation failed, couldn't load configuration file.");
            }
            // check connector
            if (null == ClientConnector.getInstance()) {
                throw new Exception(
                        "Initiation failed, couldn't create storage connector.");
            }

            taskExecutor = Executors.newFixedThreadPool(MAX_THREADS);

            taskMonitor = new TaskMonitor();
            taskMonitor.addListener(this);

            connector = ClientConnector.getInstance();
            connector.addListener(this);

            // xConnector = XConnector.getInstance();
            // xConnector.addListener(this);
            xConnector = new XConnector(port);
            xConnector.start();
            xConnector.addSocketListener(this);
            address = InetAddress.getLocalHost().getHostAddress() + ":" + port;

            startRegister();

            logger.info("StorageServer" + connector.getLocalAddress()
                    + " initialization finished.");

            initialized = true;
        }
    }

    @Override
    public void handleCall(Call call) {
        logger.info("StorageServer" + connector.getLocalAddress()
                + " recievced a call: " + call.getType());
        logger.info("dispatch to task: " + call.getToTaskId());

        // Dispatch calls to taskThread
        final Task task = tasks.get(call.getToTaskId());
        if (null == task)
            logger.error("StorageServer" + address + " couldn't find a task "
                    + call.getToTaskId() + " to handle the call.");
        else {
            logger.info("StorageServer" + address + " start handle the call.");
            task.handleCall(call);
        }
    }

    @Override
    public void handle(TaskEvent event) {
        final Task task = event.getTaskThread();

        if (event.getType() == TaskEvent.Type.TASK_FINISHED) {
            if (task instanceof RegisterTask) {
                registered = true;
                logger.info("RegisterTask: " + task.getTaskId() + " "
                        + event.getType());
                startHeartbeat();
                startSyncTask();

            } else if (task instanceof HeartbeatTask) {
                logger.info("HeartbeatTask: " + task.getTaskId() + " "
                        + event.getType());
                // 需要重新注册
                NStid = -1;
                registered = false;
                startRegister();
            }
        } else if (event.getType() == TaskEvent.Type.REG_FINISHED) {
            NStid = ((BeforeRegFinishEvent) event).getNStid();
        } else if (event.getType() == TaskEvent.Type.HEARTBEAT_RESPONSE) {
            if (task instanceof HeartbeatTask) {
                Map<String, List<String>> working = ((HeartbeatResponseEvent) event)
                        .getWorking();
                for (String key : working.keySet()) {
                    if (working.get(key).isEmpty() == false) {
                        startMigrateFileTask(key, working.get(key));
                    }
                }
            }
        } else if (event.getType() == TaskEvent.Type.MIGRATE_FINISHED) {
            if (task instanceof MigrateFileTask) {
                String key = ((MigrateFileFinishEvent) event).getAddress();
                List<String> filenames = ((MigrateFileFinishEvent) event)
                        .getFiles();
                synchronized (overMigrateFile) {
                    synchronized (onMigrateFile) {
                        onMigrateFile.get(key).removeAll(filenames);
                        if (overMigrateFile.get(key) == null)
                            overMigrateFile.put(key, new ArrayList<String>());
                        overMigrateFile.get(key).addAll(filenames);
                    }
                }
            }
        } else if (event.getType() == TaskEvent.Type.ADDFILE_DUPLICATE) {
            if (task instanceof AddFileTask) {

                for (String address : ((AddFileDuplicateEvent) event).getTodo()) {
                    startDuplicateFileTask(address,
                            ((AddFileDuplicateEvent) event).getFilename(),
                            task.getTaskId());
                }
            }
        } else if (event.getType() == TaskEvent.Type.DUPLICATE_FINISHED) {
            if (task instanceof DuplicateFileTask) {
                // TODO Need to check for duplication success
                tasks.get(((DuplicateFinishEvent) event).getParent())
                        .handleCall(null);
            }
        } else {

        }
    }

    @Override
    public void handleSocket(Socket s) {
        byte op;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(s.getInputStream());
            op = dis.readByte();
            // Setup task to handle the request according to opcode
            switch (op) {
                case XConnector.Type.OP_WRITE_BLOCK:
                    logger.info("Storage " + address + " start a addFileTask.");
                    startAddFileTask(s, dis);
                    break;
                case XConnector.Type.OP_READ_BLOCK:
                    logger.info("Storage " + address + " start a getFileTask.");
                    startGetFileTask(s, dis);
                    break;
                case XConnector.Type.OP_APPEND_BLOCK:
                    logger.info("Storage " + address + " start a appendFileTask.");
                    startAppendFileTask();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    /**
     * Start the Register task
     */
    public void startRegister() {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new RegisterTask(id, address);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * Start the Heartbeat task
     */
    public void startHeartbeat() {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new HeartbeatTask(id, overMigrateFile, onMigrateFile, NStid,
                taskMonitor);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * Start the Sync task
     */
    public void startSyncTask() {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new SyncTask(id, storage, address);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * @param socket Socket with the Client who send the request
     * @param dis    DataInputStream of the socket
     */
    public void startAddFileTask(Socket socket, DataInputStream dis) {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new AddFileTask(id, socket, dis, storage);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * @param socket Socket with the Client who send the request
     * @param dis    DataInputStream of the socket
     */
    public void startGetFileTask(Socket socket, DataInputStream dis) {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new GetFileTask(id, socket, dis, storage);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     *
     */
    public void startAppendFileTask() {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        task = new AppendFileTask(id);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * @param address  Address of the StorageServer to duplicate the file
     * @param filename Duplicate File name
     * @param parent   TaskId of the task to begin this duplication operation, need
     *                 to notify this task when finished
     */
    public void startDuplicateFileTask(String address, String filename,
                                       long parent) {
        Task task = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        logger.info("----------------->storage start a duplicateTask");
        task = new DuplicateFileTask(id, storage, address, filename, parent);
        tasks.put(task.getTaskId(), task);
        taskExecutor.execute(task);
        taskMonitor.addTask(task);
    }

    /**
     * @param address Address of the StorageServer from which to get files
     * @param files   List of filenames to get
     */
    public void startMigrateFileTask(String address, List<String> files) {
        Task workingTask = null;
        int id;
        synchronized (taskIDCount) {
            id = taskIDCount++;
        }
        workingTask = new MigrateFileTask(id, address, files, storage);
        tasks.put(workingTask.getTaskId(), workingTask);
        taskExecutor.execute(workingTask);
        taskMonitor.addTask(workingTask);
    }
}

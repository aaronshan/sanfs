package aaron.sanfs.nameserver;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.call.all.AbortCall;
import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventListener;
import aaron.sanfs.common.network.ServerConnector;
import aaron.sanfs.common.task.Task;
import aaron.sanfs.common.task.TaskMonitor;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.nameserver.meta.File;
import aaron.sanfs.nameserver.status.Status;
import aaron.sanfs.nameserver.status.Storage;
import aaron.sanfs.nameserver.task.HeartbeatTask;
import aaron.sanfs.nameserver.task.TaskFactory;
import aaron.sanfs.nameserver.ui.NameServerGUI;
import aaron.sanfs.nameserver.util.BackupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Name server implementation.
 * <p>
 * It is responsible for:
 * </p>
 * 1. Manage meta data, the directory structure.<br/>
 * 2. Manage status data, the storage server status.<br/>
 * <Strong>Warning:</Strong> name server will never send call to anyone
 * initially, it only reply other call.
 *
 * @author: aaronshan
 */
public class NameServer implements TaskEventListener, CallListener {
    private final static Logger logger = LoggerFactory.getLogger(NameServer.class);

    /**
     * Maximum number of task which can be running simultaneously.
     */
    private final static int MAX_THREADS = 20;

    /**
     * Server connector, used to send/receive call to/from client and storage server.
     */
    private ServerConnector connector = null;

    private BackupUtil backupUtil = null;

    /**
     * task list.
     * <p/>
     * {taskId, task}
     */
    private Map<Long, Task> taskMap = new HashMap<Long, Task>();

    private Map<Long, Task> heartbeatTasks = new HashMap<Long, Task>();

    /**
     * Task monitor, used to check task status.
     */
    private TaskMonitor taskMonitor = null;

    private TaskMonitor heartbeatMonitor = null;

    /**
     * task thread executor.
     */
    private ExecutorService taskExecutor = null;

    /**
     * snapshot maker thread executor.
     */
    private ScheduledExecutorService snapshotExecutor = null;

    /**
     * when name server is pausing, it won't response for any new call.
     */
    private Lock pauseLock = null;

    /**
     * determine whether name server has initiated.
     */
    private boolean initialized = false;

    public NameServer() {
    }

    public void initilize() throws Exception {
        if (initialized) {
            logger.warn("name server has been initialized before, you can't do it twice.");
            return;
        } else {
            // check configuration.
            if (null == Configuration.getInstance()) {
                throw new Exception("Initiation failed, couldn't load configuration file.");
            }

            // check backup util.
            backupUtil = BackupUtil.getInstance();
            if (null == backupUtil) {
                throw new Exception("Initiation failed, couldn't create backup directory.");
            }
            backupUtil.readBackupImage();
            backupUtil.readBackupLog();

            // check connector.
            connector = ServerConnector.getInstance();
            if (null == connector) {
                throw new Exception("Initiation failed, couldn't create server connector.");
            }
            connector.addListener(this);

            pauseLock = new ReentrantLock();

            taskExecutor = Executors.newFixedThreadPool(MAX_THREADS);

            snapshotExecutor = Executors.newSingleThreadScheduledExecutor();
            long backupInterval = Configuration.getInstance().getLong(Configuration.META_BACKUP_INTERVAL_KEY);
            snapshotExecutor.scheduleAtFixedRate(new SnapshotMaker(), backupInterval, backupInterval, TimeUnit.SECONDS);

            taskMonitor = new TaskMonitor();
            taskMonitor.addListener(this);

            heartbeatMonitor = new TaskMonitor();
            heartbeatMonitor.addListener(this);

            // GUI
            NameServerGUI gui = NameServerGUI.getInstance();
            Status.getInstance().addEventListener(gui);
            gui.init();

            logger.info("NameServer initialization finished.");

            initialized = true;
        }
    }

    @Override
    public void handleCall(Call call) {
        logger.info("NameServer received a call: " + call.getType()
                + " fromTaskId: " + call.getFromTaskId() + ", toTaskId: "
                + call.getToTaskId() + ", initiator: " + call.getInitiator());

        Task task = null;

        if (isNewCall(call)) {
            boolean permitted = pauseLock.tryLock();

            try {
                if (!permitted) {
                    sendAbortCall(call,
                            "Nameserver is maintaining, please try later.");
                } else {
                    task = TaskFactory.createTask(call);

                    if (task instanceof HeartbeatTask) {
                        heartbeatTasks.put(task.getTaskId(), task);
                        taskExecutor.execute(task);
                        heartbeatMonitor.addTask(task);
                    } else {
                        taskMap.put(task.getTaskId(), task);
                        taskExecutor.execute(task);
                        taskMonitor.addTask(task);
                    }
                }
            } finally {
                if (permitted) {
                    pauseLock.unlock();
                }
            }
        } else {
            task = getRelatedTask(call.getType(), call.getToTaskId());

            if (null != task) {
                task.handleCall(call);
            } else {
                sendAbortCall(call, "Failed to recognize " + call.getType()
                        + ". Homeless call.");
            }
        }
    }

    @Override
    public void handle(TaskEvent event) {
        final Task task = event.getTaskThread();

        if (event.getType() == TaskEvent.Type.TASK_DUE) {
            taskMap.remove(task);
            task.release();
            logger.info("Task: " + task.getTaskId() + " " + event.getType());
        } else if (event.getType() == TaskEvent.Type.TASK_FINISHED) {
            taskMap.remove(task);
            logger.info("Task: " + task.getTaskId() + " " + event.getType());
        } else if (event.getType() == TaskEvent.Type.HEARTBEAT_FATAL) {
            logger.info("Heartbeat fatal");
            heartbeatTasks.remove(task);
            handleHeartbeatFatal(event);
        }
    }

    /**
     * Handle heartbeat event, which means some storage server has dead.
     *
     * @param event
     */
    private synchronized void handleHeartbeatFatal(TaskEvent event) {
        Storage storage = ((HeartbeatTask) (event.getTaskThread())).getStorage();
        Status.getInstance().removeStorage(storage);

        // Remove files' location.
        List<File> files = storage.getFiles();
        for (File file : files) {
            file.removeLocation(storage);
        }

        List<Storage> storages = Status.getInstance().getStorages();
        if (0 == storages.size()) {
            logger.error("Failed to migrate data, no active storage server was found.");
            return;
        }

        // Allocate migration work.
        Iterator<Storage> iter = storages.iterator();
        for (File f : files) {
            // Refresh the iterator.
            if (!iter.hasNext())
                iter = storages.iterator();

            Storage active = iter.next();
            active.addMigrateFile(f.getLocations().get(0), f);
        }
    }

    /**
     * Test whether the coming call is new one.
     *
     * @param call
     * @return
     */
    private boolean isNewCall(Call call) {
        return call.getToTaskId() < 0;
    }

    /**
     * Get task with specified task id.
     *
     * @param tid
     * @return
     */
    private synchronized Task getRelatedTask(Call.Type type, long tid) {
        if (Call.Type.HEARTBEAT_S2N == type)
            return heartbeatTasks.get(tid);
        else
            return taskMap.get(tid);
    }

    private void sendAbortCall(Call call, String reason) {
        final long localTaskId = call.getToTaskId();
        final long remoteTaskId = call.getFromTaskId();

        Call back = new AbortCall(reason);

        back.setFromTaskId(localTaskId);
        back.setToTaskId(remoteTaskId);
        back.setInitiator(call.getInitiator());
        connector.sendCall(back);
    }

    private class SnapshotMaker implements Runnable {

        @Override
        public void run() {
            makeSnapshot();
        }

        private void makeSnapshot() {
            logger.info("Making snapshot.");
            pauseLock.lock();

            try {
                while (hasRunningTask()) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                backupUtil.writeBackupImage();
                backupUtil.deleteBackLog();
            } finally {
                pauseLock.unlock();
            }

            logger.info("Finish snapshot.");
        }

        private boolean hasRunningTask() {
            return 0 == taskMap.size();
        }
    }
}

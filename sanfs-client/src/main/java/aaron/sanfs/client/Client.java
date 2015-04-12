package aaron.sanfs.client;

import aaron.sanfs.client.task.*;
import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventDispatcher;
import aaron.sanfs.common.event.TaskEventListener;
import aaron.sanfs.common.task.Task;
import aaron.sanfs.common.task.TaskMonitor;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.common.util.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: aaronshan
 */
public class Client implements TaskEventListener, TaskEventDispatcher, CallListener {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private volatile static Client instance;

    private TaskMonitor taskMonitor;
    private Map<Long, Task> taskMap = new HashMap<Long, Task>();
    private Object taskWaitor = new Object();

    /**
     * List of <tt>TaskEventListener</tt>
     */
    private List<TaskEventListener> listeners = new ArrayList<TaskEventListener>();

    private Client() {
        taskMonitor = new TaskMonitor();
        taskMonitor.addListener(this);
    }

    /**
     * there's only one instance of Client globally
     *
     * @return
     */
    public static Client getInstance() {
        if (null == instance) {
            synchronized (Client.class) {
                instance = new Client();
            }
        }

        return instance;
    }

    /**
     * RPC call, must block.
     *
     * @param directory target directory
     * @return
     */
    public List<String> getDirectorySync(String directory) {
        logger.info("getDirectorySync - directory:{}.", directory);
        List<String> result = new ArrayList<String>();
        CGetDirectoryTask task = new CGetDirectoryTask(IdGenerator.getInstance().getLongId()
                , directory, result, taskWaitor);
        new Thread(task).start();
        taskMonitor.addTask(task);

        synchronized (taskWaitor) {
            try {
                taskWaitor.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * get path+name file to local file
     *
     * @param path
     * @param name
     * @param file
     */
    public void getFileAsync(String path, String name, File file) {
        logger.info("getFileASync - direct:" + path + name + " >>> " + file.getPath());
        CGetFileTask task = new CGetFileTask(IdGenerator.getInstance().getLongId()
                , path, name, file);
        new Thread(task).start();
        taskMonitor.addTask(task);
    }

    /**
     * RPC call, run in background and will not block
     *
     * @param dir
     * @param fileName
     */
    public void addFileAsync(String dir, String fileName, File file) {
        CAddFileTask task = new CAddFileTask(IdGenerator.getInstance().getLongId()
                , dir, fileName, file);
        new Thread(task).start();
        taskMonitor.addTask(task);
    }

    /**
     * create directory
     *
     * @param dir full path of new directory
     */
    public void createDirectoryASync(String dir) {
        CCreateDirTask task = new CCreateDirTask(IdGenerator.getInstance().getLongId()
                , dir);
        new Thread(task).start();
        taskMonitor.addTask(task);
    }

    /**
     * remome a file or directory
     *
     * @param dir
     * @param name if ends with "/", make dir+name a directory
     */
    public void removeFileDirectASync(String dir, String name) {
        Task task = null;
        if (!name.contains("/")) {    //file
            task = new CRemoveFileTask(IdGenerator.getInstance().getLongId()
                    , dir, name);
        } else {    //directory
            task = new CRemoveDirectoryTask(IdGenerator.getInstance().getLongId()
                    , dir + name);
        }
        new Thread(task).start();
        taskMonitor.addTask(task);
    }

    /**
     * move file
     *
     * @param oldDir  old directory
     * @param oldName old file name
     * @param newDir  new file directory
     * @param newName new file name
     */
    public void moveFileDirectASync(String oldDir, String oldName
            , String newDir, String newName) {
        Task task = null;
        if (!oldName.contains("/")) {    //file
            task = new CMoveFileTask(IdGenerator.getInstance().getLongId()
                    , oldDir, oldName, newDir, newName);
        } else {
            task = new CMoveDirectoryTask(IdGenerator.getInstance().getLongId()
                    , oldDir + oldName, newDir + newName);
        }
        new Thread(task).start();
        taskMonitor.addTask(task);
        logger.error("hahahahha");
    }

    @Override
    public void handleCall(Call call) {
        Task task = null;
        long remoteTaskId = call.getFromTaskId();
        long localTaskId = call.getToTaskId();
        Configuration configuration = Configuration.getInstance();

        if (localTaskId >= 0) {
            // should we send a abort call ? maybe not.
            if (taskMap.containsKey(localTaskId)) {
                taskMap.get(localTaskId).handleCall(call);
            }
        } else {
            localTaskId = IdGenerator.getInstance().getLongId();
            logger.debug("Client new task created {}:{}.", localTaskId, call.getType());
            if (Call.Type.ADD_FILE_N2C == call.getType()) {
                // update file to master storage server listed
            } else {
                logger.info("Wrong command type - {}!", call.getType());
            }
        }
    }

    @Override
    public synchronized void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireEvent(TaskEvent event) {

    }

    @Override
    public void handle(TaskEvent event) {

    }
}

package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.AddDirectoryCallC2N;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.Directory;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.util.BackupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task of adding directory.
 *
 * @author: aaronshan
 * @see aaron.sanfs.nameserver.task.NameServerTask
 */
public class AddDirectoryTask extends NameServerTask {
    private final static Logger logger = LoggerFactory.getLogger(AddDirectoryTask.class);

    /**
     * Name of the directory that client wants to add.
     */
    private String dirName;

    /**
     * Construction method.
     *
     * @param taskId    id of this task.
     * @param call      add directory call.
     * @param connector ServerConnector
     */
    public AddDirectoryTask(long taskId, Call call, Connector connector) {
        super(taskId, call, connector);
        AddDirectoryCallC2N callC2N = (AddDirectoryCallC2N) call;
        this.dirName = callC2N.getDirName();
    }

    @Override
    public void run() {
        final Meta meta = Meta.getInstance();
        final BackupUtil backupUtil = BackupUtil.getInstance();

        synchronized (meta) {
            if (directoryExists()) {
                sendAbortCall("Task aborted, there has been a directory with the same name.");
            } else {
                Directory dir = new Directory(dirName);

                logger.info("AddDirectoryTask {} started.", getTaskId());
                backupUtil.writeLogIssue(getTaskId(), Call.Type.ADD_DIRECTORY_C2N, dirName);

                sendFinishCall();
                meta.addDirectory(dir);
                meta.setDirectoryValid(dirName, true);
            }

            setFinish();
        }
    }

    private boolean directoryExists() {
        if (Meta.getInstance().containDirectory(dirName)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void release() {
        setDead();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCall(Call call) {

    }
}

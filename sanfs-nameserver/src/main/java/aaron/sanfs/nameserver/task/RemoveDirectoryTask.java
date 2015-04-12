package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.RemoveDirectoryCallC2N;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.util.BackupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task of removing directory.
 *
 * @author lishunyang
 * @see NameServerTask
 */
public class RemoveDirectoryTask extends NameServerTask {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory
            .getLogger(RemoveDirectoryTask.class);

    /**
     * File directory name.
     */
    private String dirName;

    /**
     * Construction method.
     *
     * @param tid
     * @param call
     * @param connector
     */
    public RemoveDirectoryTask(long tid, Call call, Connector connector) {
        super(tid, call, connector);
        RemoveDirectoryCallC2N c = (RemoveDirectoryCallC2N) call;
        this.dirName = c.getDirectoryName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final BackupUtil backup = BackupUtil.getInstance();

        synchronized (Meta.getInstance()) {
            if (!directoryExists()) {
                sendAbortCall("Task aborted, directory does not exist.");
            } else {
                logger.info("RemoveDirectoryTask " + getTaskId() + " started.");
                backup.writeLogIssue(getTaskId(),
                        Call.Type.REMOVE_DIRECTORY_C2N, dirName);

                logger.info("RemoveDirectoryTask " + getTaskId() + " commit.");
                backup.writeLogCommit(getTaskId());

                sendFinishCall();
                Meta.getInstance().removeDirectory(dirName);
            }

            setFinish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCall(Call call) {
        if (call.getToTaskId() != getTaskId())
            return;

        if (call.getType() == Call.Type.LEASE_C2N) {
            renewLease();
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        setDead();
    }

    /**
     * Test whether the directory that client wants to remove.
     *
     * @return
     */
    private boolean directoryExists() {
        if (Meta.getInstance().containDirectory(dirName))
            return true;
        else
            return false;
    }
}

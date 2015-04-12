package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.RemoveFileCallC2N;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.util.BackupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task of removing file.
 *
 * @author lishunyang
 * @see NameServerTask
 */
public class RemoveFileTask extends NameServerTask {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(RemoveFileTask.class);

    /**
     * File directory name.
     */
    private String dirName;

    /**
     * File name.
     */
    private String fileName;

    /**
     * Construction method.
     *
     * @param tid
     * @param call
     * @param connector
     */
    public RemoveFileTask(long tid, Call call, Connector connector) {
        super(tid, call, connector);
        RemoveFileCallC2N c = (RemoveFileCallC2N) call;
        this.dirName = c.getDirName();
        this.fileName = c.getFileName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final BackupUtil backup = BackupUtil.getInstance();

        synchronized (Meta.getInstance()) {
            if (!fileExists()) {
                sendAbortCall("Task aborted, file does not exist.");
            } else {
                logger.info("RemoveFileTask " + getTaskId() + " started.");
                backup.writeLogIssue(getTaskId(), Call.Type.REMOVE_FILE_C2N,
                        dirName + " " + fileName);

                logger.info("RemoveFileTask " + getTaskId() + " commit.");
                backup.writeLogCommit(getTaskId());

                sendFinishCall();
                Meta.getInstance().removeFile(dirName, fileName);
            }

            setFinish();
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
     * Test whether the file that client wants to remove exists.
     *
     * @return
     */
    private boolean fileExists() {
        return Meta.getInstance().containFile(dirName, fileName);
    }
}

package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.GetFileCallC2N;
import aaron.sanfs.common.call.n2c.GetFileCallN2C;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.File;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.status.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Task of getting file.
 *
 * @author lishunyang
 * @see NameServerTask
 */
public class GetFileTask extends NameServerTask {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(GetFileTask.class);

    /**
     * File directory name.
     */
    private String dirName;

    /**
     * File name.
     */
    private String fileName;

    /**
     * Sync object which is used for synchronizing.
     */
    private Object syncRoot = new Object();

    /**
     * The file that we focus on.
     */
    private File file = null;

    /**
     * Construction method.
     *
     * @param tid
     * @param call
     * @param connector
     */
    public GetFileTask(long tid, Call call, Connector connector) {
        super(tid, call, connector);
        GetFileCallC2N c = (GetFileCallC2N) call;
        this.dirName = c.getDirName();
        this.fileName = c.getFileName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final Meta meta = Meta.getInstance();

        synchronized (meta) {

            if (!fileExists()) {
                sendAbortCall("Task aborted, file does not exist.");
                setFinish();
                return;
            } else {
                logger.info("GetFileTask " + getTaskId() + " started.");

                file = Meta.getInstance().getFile(dirName, fileName);
                if (file.tryLockRead(1, TimeUnit.SECONDS)) {
                    sendResponseCall();
                } else {
                    sendAbortCall("Task aborted, someone is using the file.");
                    setFinish();
                    return;
                }
            }
        }

        waitUntilTaskFinish();

        if (isDead())
            return;

        synchronized (meta) {
            logger.info("GetFileTask " + getTaskId() + " commit.");

            file.updateVersion();
            file.unlockRead();
            setFinish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        setDead();
        synchronized (syncRoot) {
            syncRoot.notify();
        }
        file.unlockRead();
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

        if (call.getType() == Call.Type.FINISH) {
            synchronized (syncRoot) {
                syncRoot.notify();
            }
            return;
        }
    }

    /**
     * Test whether the file that client wants to get exists.
     *
     * @return
     */
    private boolean fileExists() {
        return Meta.getInstance().containFile(dirName, fileName);
    }

    /**
     * Send response call back to client.
     */
    private void sendResponseCall() {
        List<String> locations = new ArrayList<String>();
        for (Storage s : file.getLocations())
            locations.add(s.getId());

        String fileId = file.getId();

        Call back = new GetFileCallN2C(fileId, locations);
        sendCall(back);
    }

    /**
     * Wait until task is finished.
     */
    private void waitUntilTaskFinish() {
        try {
            synchronized (syncRoot) {
                syncRoot.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

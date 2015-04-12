package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.n2s.SyncCallN2S;
import aaron.sanfs.common.call.s2n.SyncCallS2N;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.File;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Task of synchronize file between storage server and name server.
 * <p/>
 * Storage server sends sync call to name server, name server checks it and
 * tells storage server which files are invalid and should be deleted.
 *
 * @author lishunyang
 * @see NameServerTask
 */
public class SyncTask extends NameServerTask {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(SyncTask.class);

    /**
     * The files that storage server has.
     */
    private List<String> files;

    /**
     * The duplicate number of file.
     */
    private int duplicate;

    private String address;

    /**
     * Construction method.
     *
     * @param tid
     * @param call
     * @param connector
     * @param duplicate
     */
    public SyncTask(long tid, Call call, Connector connector, int duplicate) {
        super(tid, call, connector);
        SyncCallS2N c = (SyncCallS2N) call;
        this.files = c.getFiles();
        this.duplicate = duplicate;
        this.address = c.getAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.info("SyncTask started.");

        synchronized (Meta.getInstance()) {
            if (!storageExists()) {
                sendAbortCall("Task aborted, unidentified storage server.");
            } else {
                List<String> removeList = new ArrayList<String>();
                for (String id : files) {
                    File file = Meta.getInstance().getFile(id);
                    if (null == file) {
                        removeList.add(id);
                    } else {
                        if (file.getLocationsCount() > duplicate)
                            removeList.add(id);
                        else
                            file.addLocation(Status.getInstance().getStorage(
                                    address));
                    }
                }
                sendResponseCall(removeList);
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
    }

    /**
     * Test whether the storage is existed.
     *
     * @return
     */
    private boolean storageExists() {
        return Status.getInstance().contains(address);
    }

    /**
     * Send response call back to storage server.
     *
     * @param removeList
     */
    private void sendResponseCall(List<String> removeList) {
        Call back = new SyncCallN2S(removeList);
        sendCall(back);
    }
}


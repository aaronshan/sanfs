package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.RemoveDirectoryCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * remove a directory
 *
 * @author gengyufeng
 */
public class CRemoveDirectoryTask extends Task {

    private static final Logger logger = LoggerFactory.getLogger(CRemoveDirectoryTask.class);

    private String dir;
    private Object netWaitor = new Object();
    private long toTaskId;

    /**
     * remove dir
     *
     * @param tid
     * @param dir
     */
    public CRemoveDirectoryTask(long tid, String dir) {
        super(tid);
        this.dir = dir;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleCall(Call call) {
        if (getTaskId() != call.getToTaskId()) {
            return;
        }
        if (call.getType() == Call.Type.FINISH
                || call.getType() == Call.Type.ABORT) {
            synchronized (netWaitor) {
                netWaitor.notify();
            }
        }
    }

    @Override
    public void run() {
        logger.debug("DeleteDirectory:" + dir);
        RemoveDirectoryCallC2N callC2N = new RemoveDirectoryCallC2N(dir);
        callC2N.setFromTaskId(getTaskId());
        ClientConnector.getInstance().sendCall(callC2N);
        ClientConnector.getInstance().addListener(this);
        synchronized (netWaitor) {
            try {
                netWaitor.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        setFinish();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}

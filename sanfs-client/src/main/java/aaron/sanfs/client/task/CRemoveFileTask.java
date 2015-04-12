package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.RemoveFileCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * remove a file(this operation only affect NSs, affection on SS with
 * happen after a sync operation between NS and SS)
 */
public class CRemoveFileTask extends Task {

    private static final Logger logger = LoggerFactory.getLogger(CRemoveFileTask.class);

    private String dir, name;
    private Object netWaitor = new Object();
    private long toTaskId;

    /**
     * remove the file:dir+name
     *
     * @param tid
     * @param dir
     * @param name
     */
    public CRemoveFileTask(long tid, String dir, String name) {
        super(tid);
        this.dir = dir;
        this.name = name;
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
        logger.debug("DeleteFile:" + dir + name);
        RemoveFileCallC2N callC2N = new RemoveFileCallC2N(dir, name);
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

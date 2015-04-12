package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.all.AbortCall;
import aaron.sanfs.common.call.c2n.GetDirectoryCallC2N;
import aaron.sanfs.common.call.n2c.GetDirectoryCallN2C;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * task to get contents of target directory
 *
 * @author gengyufeng
 */
public class CGetDirectoryTask extends Task {

    private static final Logger logger = LoggerFactory.getLogger(CGetDirectoryTask.class);

    /**
     * target directory
     */
    private String direct;
    /**
     * contents of the target directory, globally used
     */
    private List<String> ret;
    /**
     * getDirectory operation is Sync, the method blocks on this object
     */
    private Object taskWaitor;
    /**
     * wait on this object for NS to response
     */
    private Object netWaitor = new Object();
    private long toTaskId;
    private GetDirectoryCallN2C callN2C;

    /**
     * This task will BLOCK untill finished!
     *
     * @param tid    globally unique task id
     * @param direct target directory
     * @param ret    contents returned
     * @param waitor blocking object
     */
    public CGetDirectoryTask(long tid, String direct, List<String> ret, Object waitor) {
        super(tid);
        this.direct = direct;
        this.ret = ret;
        this.taskWaitor = waitor;
    }

    @Override
    public void handleCall(Call call) {
        if (getTaskId() != call.getToTaskId()) {
            return;
        }
        if (call.getType() == Call.Type.GET_DIRECTORY_N2C) {
            callN2C = (GetDirectoryCallN2C) call;
            this.toTaskId = call.getFromTaskId();
            synchronized (netWaitor) {
                netWaitor.notify();
            }
        } else if (call.getType() == Call.Type.ABORT) {
            synchronized (taskWaitor) {
                logger.info(((AbortCall) call).getReason());
                taskWaitor.notify();
            }
        } else {
            logger.error("Fatal error: call type cannot match.");
        }
    }

    @Override
    public void run() {
        GetDirectoryCallC2N callC2N = new GetDirectoryCallC2N(direct);
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

        //ret = callN2C.getDirectoryList();
        // make sure the same ret location
        for (String item : callN2C.getFilesAndDirectories()) {
            logger.debug(" - " + item);
            ret.add(item);
        }
        synchronized (taskWaitor) {
            taskWaitor.notify();
        }
        setFinish();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}


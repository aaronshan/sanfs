package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.AddDirectoryCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;

/**
 * Send Call to NS and finish
 *
 * @author gengyufeng
 */
public class CCreateDirTask extends Task {
    /**
     * directory to be created
     */
    private String direct;
    /**
     * wait on this object for NS to response
     */
    private Object netWaitor = new Object();

    /**
     * should call new Thread(task).start()
     *
     * @param tid    globally unique task id
     * @param direct target directory
     */
    public CCreateDirTask(long tid, String direct) {
        super(tid);
        this.direct = direct;
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
        // TODO Auto-generated method stub
        AddDirectoryCallC2N callC2N = new AddDirectoryCallC2N(direct);
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

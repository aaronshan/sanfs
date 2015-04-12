package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.MoveDirectoryCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;

/**
 * move a directory to new location
 *
 * @author gengyufeng
 */
public class CMoveDirectoryTask
        extends Task {

    private Object netWaitor = new Object();
    private String oldDir;
    private String newDir;

    /**
     * @param tid
     * @param oldDir old directory
     * @param newDir new directory
     */
    public CMoveDirectoryTask(long tid, String oldDir, String newDir) {
        super(tid);
        this.oldDir = oldDir;
        this.newDir = newDir;
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
        MoveDirectoryCallC2N callC2N = new MoveDirectoryCallC2N(oldDir, newDir);
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


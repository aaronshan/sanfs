package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.MoveFileCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;

/**
 * move is same with rename, just rename a file on NS
 *
 * @author gengyufeng
 */
public class CMoveFileTask extends Task {

    private String direct;
    private Object netWaitor = new Object();
    private String oldDir, oldName;
    private String newDir, newName;

    /**
     * rename a file
     *
     * @param tid
     * @param oldDir  old directory
     * @param oldName old name
     * @param newDir  new ..
     * @param newName new ..
     */
    public CMoveFileTask(long tid, String oldDir, String oldName
            , String newDir, String newName) {
        super(tid);
        this.oldDir = oldDir;
        this.oldName = oldName;
        this.newDir = newDir;
        this.newName = newName;
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
        MoveFileCallC2N callC2N = new MoveFileCallC2N(oldDir, oldName, newDir, newName);
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

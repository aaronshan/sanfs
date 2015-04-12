package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.all.AbortCall;
import aaron.sanfs.common.call.all.FinishCall;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.common.task.Task;

/**
 * Abstract name server task.
 *
 * @author: aaronshan
 * @see aaron.sanfs.common.task.Task
 */
public abstract class NameServerTask extends Task {

    /**
     * Instance of <tt>ServerConnector</tt>
     */
    private Connector connector;

    /**
     * Indicate who initiated this task.
     */
    private String initiator;

    /**
     * Indicate remote peer's task id.
     */
    private long remoteTaskId;

    /**
     * Indicate whether this task is dead.
     */
    private boolean dead = false;

    /**
     * Construction method.
     *
     * @param taskId
     * @param call
     * @param connector
     */
    public NameServerTask(long taskId, Call call, Connector connector) {
        super(taskId);
        this.connector = connector;
        this.initiator = call.getInitiator();
        this.remoteTaskId = call.getFromTaskId();
    }

    /**
     * Get initiator.
     *
     * @return
     */
    public String getInitiator() {
        return initiator;
    }

    /**
     * Send abort call back to remote peer.
     *
     * @param reason
     */
    protected void sendAbortCall(String reason) {
        sendCall(new AbortCall(reason));
    }

    /**
     * Send finish call back to remote peer.
     */
    protected void sendFinishCall() {
        sendCall(new FinishCall());
    }

    /**
     * Send call back to remote peer.
     *
     * @param call
     */
    protected void sendCall(Call call) {
        call.setFromTaskId(getTaskId());
        call.setToTaskId(remoteTaskId);
        call.setInitiator(initiator);

        connector.sendCall(call);
    }

    /**
     * Set task status to dead. If a task is dead, it shouldn't do anything
     * but exit.
     * <p/>
     * For those time-consuming tasks, they could be killed by
     * <tt>NameServer</tt>. After being killed, they will be woke up and do some
     * clean up jobs.
     */
    protected void setDead() {
        this.dead = true;
    }

    /**
     * Test whether the task is dead.
     *
     * @return
     */
    protected boolean isDead() {
        return dead;
    }
}

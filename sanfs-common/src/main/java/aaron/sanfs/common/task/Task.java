package aaron.sanfs.common.task;

import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventDispatcher;
import aaron.sanfs.common.event.TaskEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Task.
 * <p/>
 * Each task represents one process. Such as getting file, renaming directory or
 * sending heartbeat, etc.
 * <p/>
 * It implements <tt>Runnable</tt>, <tt>TaskEventDispatcher</tt> and
 * <tt>CallListener</tt>.
 *
 * @author: aaronshan
 * @see java.lang.Runnable
 * @see aaron.sanfs.common.event.TaskEventDispatcher
 * @see aaron.sanfs.common.call.CallListener
 */
public abstract class Task implements Runnable, TaskEventDispatcher, CallListener {

    /**
     * task id.
     */
    private long taskId;

    /**
     * Task lease.<p>
     * It is optional.
     */
    private Lease lease = null;

    /**
     * Indicate whether the task has lease.
     */
    private boolean hasLease = false;

    /**
     * List of <tt>TaskEventListener</tt>
     */
    private List<TaskEventListener> listeners = new ArrayList<TaskEventListener>();

    public Task(long taskId) {
        this.taskId = taskId;
    }

    /**
     * Get task id.
     *
     * @return
     */
    public long getTaskId() {
        return taskId;
    }

    /**
     * Set lease.
     *
     * @param lease
     */
    public void setLease(Lease lease) {
        this.lease = lease;
        hasLease = true;
    }

    /**
     * Renew task lease.
     */
    public void renewLease() {
        if (hasLease) {
            lease.renew();
        }
    }

    /**
     * Test whether task lease is valid.
     *
     * @return
     */
    public boolean isLeaseValid() {
        if (hasLease) {
            return lease.isValid();
        } else {
            return true;
        }
    }

    // TODO: Actually "finish" is not very suitable. When a task is aborted,
    // we also think it's finished. For instance, client send a rename call to
    // name server, but the original file didn't exist, so name server send
    // abort call back to client and let that task finish. So we should change
    // the option of this method and related stuff.

    /**
     * Notify event listens this task has finished.
     */
    public void setFinish() {
        fireEvent(new TaskEvent(TaskEvent.Type.TASK_FINISHED, this));
    }

    @Override
    public void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireEvent(TaskEvent event) {
        for (TaskEventListener listener : listeners) {
            listener.handle(event);
        }
    }

    /**
     * Task thread method.
     */
    @Override
    public void run() {
    }

    /**
     * Release the resources hold by this task.
     * <p/>
     * <strong>Warning:</strong> This method should only be called when task is
     * aborted.
     */
    public abstract void release();
}

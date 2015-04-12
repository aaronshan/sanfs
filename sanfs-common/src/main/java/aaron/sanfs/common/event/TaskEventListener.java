package aaron.sanfs.common.event;

/**
 * Listener of <tt>TaskEvent</tt>.
 *
 * @author: aaronshan
 * @see aaron.sanfs.common.event.TaskEvent
 */
public interface TaskEventListener {

    /**
     * Handle <tt>TaskEvent</tt>.
     *
     * @param event
     */
    public void handle(TaskEvent event);
}

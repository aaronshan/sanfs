package aaron.sanfs.common.event;

/**
 * The <tt>TaskEvent</tt> dispatcher.
 *
 * @author: aaronshan
 * @see aaron.sanfs.common.event.TaskEvent
 * @see aaron.sanfs.common.event.TaskEventListener
 */
public interface TaskEventDispatcher {

    /**
     * Add <tt>TaskEventListener</tt>
     *
     * @param listener
     */
    public void addListener(TaskEventListener listener);

    /**
     * Remove <tt>TaskEventListener</tt>
     *
     * @param listener
     */
    public void removeListener(TaskEventListener listener);

    /**
     * Fire <tt>TaskEvent</tt>
     *
     * @param event
     */
    public void fireEvent(TaskEvent event);
}

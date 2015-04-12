package aaron.sanfs.common.task;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventDispatcher;
import aaron.sanfs.common.event.TaskEventListener;
import aaron.sanfs.common.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <tt>TaskMonitor</tt> is used for checking tasks status, whether they are
 * aborted or finished. If something happens, it will notify
 * <tt>TaskEventListener</tt>
 *
 * @author: aaronshan
 * @see aaron.sanfs.common.event.TaskEventListener
 * @see aaron.sanfs.common.event.TaskEventDispatcher
 */
public class TaskMonitor implements TaskEventDispatcher, TaskEventListener {
    private static Logger logger = LoggerFactory.getLogger(TaskMonitor.class);

    /**
     * Tasks that would be checking.
     */
    private Map<Long, Task> tasks = new HashMap<Long, Task>();

    /**
     * Monitor thread executor.
     */
    private ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();

    /**
     * Task event listeners, if some task's lease is invalid, notify them.
     */
    private List<TaskEventListener> listeners = new ArrayList<TaskEventListener>();

    /**
     * Checking period. (seconds)
     */
    private long period;

    public TaskMonitor() {
        this.period = Configuration.getInstance().getLong(Configuration.LEASE_PERIOD_KEY) * 2;
        monitor.scheduleAtFixedRate(new Monitor(), 0, period, TimeUnit.SECONDS);

        logger.info("Task monitor started. Checking period: {}s.", period);
    }

    /**
     * Add task into monitoring list and start monitoring.
     * <p/>
     * Monitoring process won't start if <tt>TaskMonitor</tt> is stopped.
     *
     * @param thread
     */
    public synchronized void addTask(Task thread) {
        tasks.put(thread.getTaskId(), thread);
        thread.addListener(this);
    }

    @Override
    public synchronized void addListener(TaskEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(TaskEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireEvent(TaskEvent event) {
        for (TaskEventListener listener : listeners) {
            listener.handle(event);
        }
    }

    /**
     * Once a task has finished, it will notify the <tt>TaskMonitor</tt> and the
     * monitor will forward this note to registered <tt>TaskEventListener</tt>s.
     *
     * @param event
     */
    @Override
    public void handle(TaskEvent event) {
        fireEvent(event);

        if (TaskEvent.Type.TASK_FINISHED == event.getType()
                || TaskEvent.Type.TASK_DUE == event.getType()
                || TaskEvent.Type.MIGRATE_FINISHED == event.getType()
                || TaskEvent.Type.DUPLICATE_FINISHED == event.getType()) {
            synchronized (tasks) {
                Task task = tasks.remove(event.getTaskThread().getTaskId());
                logger.info("Task: {} has finished, {} removed from TaskMonitor.", task.getClass(), event.getType());
            }
        }
    }

    /**
     * Get sum of running tasks.
     *
     * @return
     */
    public int getTaskSum() {
        return tasks.size();
    }

    private class Monitor extends TimerTask {

        /**
         * check the lease validation. If someone has timeout, fire an abort event.
         */
        @Override
        public void run() {
            synchronized (tasks) {
                List<Long> abortedList = new ArrayList<Long>();
                for (Map.Entry<Long, Task> taskEntry : tasks.entrySet()) {
                    if (!taskEntry.getValue().isLeaseValid()) {
                        abortedList.add(taskEntry.getKey());
                    }
                }

                for (Long taskId : abortedList) {
                    fireEvent(new TaskEvent(TaskEvent.Type.TASK_DUE, tasks.get(taskId)));
                    listeners.remove(tasks.remove(taskId));
                    logger.info("Task {} is aborted because of invalid lease.", taskId);
                }
            }
        }
    }
}

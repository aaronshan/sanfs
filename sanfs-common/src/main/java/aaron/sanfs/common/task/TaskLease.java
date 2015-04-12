package aaron.sanfs.common.task;

/**
 * @author: aaronshan
 */
public class TaskLease implements Lease {
    /**
     * The timestamp when this lease has been created.
     */
    private long timestamp = 0;

    /**
     * How long this lease will be valid. (seconds)
     */
    private long period = 0;

    /**
     * Construction method.
     *
     * @param period
     */
    public TaskLease(long period) {
        timestamp = System.currentTimeMillis();
        this.period = period;
    }

    @Override
    public void renew() {
        timestamp = System.currentTimeMillis();
    }

    @Override
    public boolean isValid() {
        long current = System.currentTimeMillis();
        return (current - timestamp) < period;
    }
}

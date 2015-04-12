package aaron.sanfs.common.task;

/**
 * Task lease.
 *
 * @author: aaronshan
 */
public interface Lease {
    /**
     * Renew the lease.
     */
    public void renew();

    /**
     * Test whether the lease is valid.
     *
     * @return
     */
    public boolean isValid();
}

package aaron.sanfs.client.task;

import aaron.sanfs.common.call.c2n.LeaseCallC2N;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Send lease Call of toTaskId to name server periodically
 *
 * @author: aaronshan
 */
public class CLeaseTask extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CLeaseTask.class);
    /**
     * NS task id.
     */
    private long toTaskId;

    /**
     * client task id(not used)
     */
    private long fromTaskId;

    /**
     * every task start a LeaseTask to send lease.
     *
     * @param toTaskId
     * @param fromTaskId
     */
    public CLeaseTask(long toTaskId, long fromTaskId) {
        this.toTaskId = toTaskId;
        this.fromTaskId = fromTaskId;
    }

    @Override
    public void run() {
        LeaseCallC2N callC2N = new LeaseCallC2N(fromTaskId, toTaskId);
        while (!this.isInterrupted()) {
            ClientConnector.getInstance().sendCall(callC2N);
            try {
                TimeUnit.SECONDS.sleep(Configuration.getInstance().getLong(Configuration.LEASE_PERIOD_KEY));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

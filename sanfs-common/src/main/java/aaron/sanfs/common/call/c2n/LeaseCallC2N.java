package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of renewing lease for some specified task, sent from <tt>Client</tt> to
 * <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class LeaseCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -2940145842011756383L;

    /**
     * Construction method.
     *
     * @param fromTaskId
     * @param toTaskId
     */
    public LeaseCallC2N(long fromTaskId, long toTaskId) {
        super(Call.Type.LEASE_C2N);
        setFromTaskId(fromTaskId);
        setToTaskId(toTaskId);
    }
}

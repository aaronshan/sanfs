package aaron.sanfs.common.call.all;

import aaron.sanfs.common.call.Call;

/**
 * when a task finished successfully, a finish call
 * will be sent to the remote entity.
 *
 * @author: aaronshan
 */
public class FinishCall extends Call {
    private static final long serialVersionUID = 8622180358347032874L;

    public FinishCall() {
        super(Call.Type.FINISH);
    }
}

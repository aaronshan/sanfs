package aaron.sanfs.storageserver.event;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.task.Task;

/**
 * @author dengshihong
 */
public class BeforeRegFinishEvent extends TaskEvent {
    private long NStid;

    public BeforeRegFinishEvent(Task thread, long NStid) {
        super(Type.REG_FINISHED, thread);
        this.NStid = NStid;
    }

    public long getNStid() {
        return NStid;
    }
}

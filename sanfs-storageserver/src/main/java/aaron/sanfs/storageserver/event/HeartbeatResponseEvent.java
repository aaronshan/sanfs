package aaron.sanfs.storageserver.event;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.task.Task;

import java.util.List;
import java.util.Map;

/**
 * @author dengshihong
 */
public class HeartbeatResponseEvent extends TaskEvent {
    private Map<String, List<String>> working;

    public HeartbeatResponseEvent(Task thread, Map<String, List<String>> working) {
        super(Type.HEARTBEAT_RESPONSE, thread);
        this.working = working;
    }

    public Map<String, List<String>> getWorking() {
        return working;
    }
}


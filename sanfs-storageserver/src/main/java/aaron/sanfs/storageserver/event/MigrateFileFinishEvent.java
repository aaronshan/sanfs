package aaron.sanfs.storageserver.event;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.task.Task;

import java.util.List;

/**
 * @author dengshihong
 */
public class MigrateFileFinishEvent extends TaskEvent {
    private final String address;
    private final List<String> files;

    public MigrateFileFinishEvent(Task thread, String address,
                                  List<String> files) {
        super(TaskEvent.Type.MIGRATE_FINISHED, thread);
        this.address = address;
        this.files = files;
    }

    public String getAddress() {
        return address;
    }

    public List<String> getFiles() {
        return files;
    }

}


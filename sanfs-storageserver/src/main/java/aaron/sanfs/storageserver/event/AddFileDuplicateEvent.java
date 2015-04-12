package aaron.sanfs.storageserver.event;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.task.Task;

import java.util.List;

/**
 * Author: ruifengshan
 * Date: 09/04/2015
 */
public class AddFileDuplicateEvent extends TaskEvent {
    private final String filename;
    private final List<String> todo;

    public AddFileDuplicateEvent(Task thread, String filename, List<String> todo) {
        super(Type.ADDFILE_DUPLICATE, thread);
        this.filename = filename;
        this.todo = todo;
    }

    public String getFilename() {
        return filename;
    }

    public List<String> getTodo() {
        return todo;
    }

}


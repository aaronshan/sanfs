package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.task.Task;

/**
 * @author dengshihong
 */
public abstract class StorageServerTask extends Task {
    protected ClientConnector connector = ClientConnector.getInstance();

    public StorageServerTask(long tid) {
        super(tid);
    }
}

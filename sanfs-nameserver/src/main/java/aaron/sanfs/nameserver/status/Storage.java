package aaron.sanfs.nameserver.status;

import aaron.sanfs.nameserver.meta.File;

import java.util.*;

/**
 * storage server meta data, includes server id, heart-beat time stamp,
 * load, sum of running tasks.
 *
 * @author: aaronshan
 */
public class Storage {
    /**
     * server's id
     */
    private final String id;

    /**
     * heartbeat timestamp
     */
    private long heartbeatTime;

    /**
     * sum of running tasks.
     */
    private int taskSum;

    /**
     * storage server load status.
     * <p>
     * It represents the percentage of using capacity which ranges from o to 100.
     * </p>
     */
    private int storageLoad;

    /**
     * Files that are contained by this storage server
     */
    private List<File> files = new ArrayList<File>();

    /**
     * Files that need to be migrated from other storage server to local storage server
     */
    private Map<Storage, List<File>> migrateFiles = new HashMap<Storage, List<File>>();

    private List<StatusEventListener> listeners = new ArrayList<StatusEventListener>();

    /**
     * Construction method.
     *
     * @param id
     */
    public Storage(String id) {
        this.id = id;
        this.heartbeatTime = System.currentTimeMillis();
    }

    /**
     * Fire a <tt>StatusEvent</tt> event, notify those listeners.
     *
     * @param event
     */
    private void fireEvent(StatusEvent event) {
        for (StatusEventListener listener : listeners) {
            listener.handle(event);
        }
    }

    /**
     * Set heartbeat time.
     *
     * @param time
     */
    public synchronized void setHeartbeatTime(long time) {
        this.heartbeatTime = time;
        fireEvent(new StatusEvent(StatusEvent.Type.HEARTBEAT, this));
    }

    /**
     * Get heartbeat time.
     *
     * @return
     */
    public synchronized long getHearbeatTime() {
        return heartbeatTime;
    }

    /**
     * Set storage task num.
     *
     * @param taskSum
     */
    public void setTaskSum(int taskSum) {
        this.taskSum = taskSum;
        fireEvent(new StatusEvent(StatusEvent.Type.TASK_SUM_CHANGED, this));
    }

    /**
     * Get storage task num.
     *
     * @return
     */
    public int getTaskSum() {
        return taskSum;
    }

    /**
     * Update storage load status.
     *
     * @param load load of storage server
     */
    public void setStorageLoad(int load) {
        this.storageLoad = load;
        fireEvent(new StatusEvent(StatusEvent.Type.LOAD_CHANGED, this));
    }

    /**
     * Get storage load percentage.
     *
     * @return
     */
    public int getStorageLoad() {
        return storageLoad;
    }

    /**
     * Get storage id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Get all files that belongs to this storage server.
     *
     * @return
     */
    public synchronized List<File> getFiles() {
        return files;
    }

    /**
     * Add a file to this storage server.
     *
     * @param file
     */
    public synchronized void addFile(File file) {
        files.add(file);
    }

    /**
     * Remove a file from this storage server.
     *
     * @param file
     */
    public synchronized void removeFile(File file) {
        files.remove(file);
    }

    /**
     * Get all files that needs to be migrated.
     *
     * @return
     */
    public synchronized Map<Storage, List<File>> getMigrateFiles() {
        Map<Storage, List<File>> result = migrateFiles;
        return result;
    }

    /**
     * Add a file that needs to be migrated.
     *
     * @param storage where this file can be find.
     * @param file
     */
    public synchronized void addMigrateFile(Storage storage, File file) {
        List<File> list = migrateFiles.get(storage);

        if (null == list) {
            list = new ArrayList<File>();
            migrateFiles.put(storage, list);
        }

        list.add(file);
    }

    /**
     * Remove files that have already been modified.
     *
     * @param files {storage server address, {file id}}
     */
    public synchronized void removeMigrateFiles(Map<String, List<String>> files) {
        List<String> list = null;
        for (Storage storage : migrateFiles.keySet()) {
            list = files.get(storage.getId());
            Iterator<File> iterator = migrateFiles.get(storage).iterator();
            File file = null;
            while (iterator.hasNext()) {
                file = iterator.next();
                if (list.contains(file.getId())) {
                    file.addLocation(this);
                    addFile(file);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Add <tt>StatusEvent</tt> listener.
     *
     * @param listener
     */
    public void addEventListener(StatusEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Remove <tt>StatusEvent</tt> listener.
     *
     * @param listener
     */
    public void removeEventListener(StatusEventListener listener) {
        listeners.remove(listener);
    }
}

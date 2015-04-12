package aaron.sanfs.nameserver.meta;

import aaron.sanfs.nameserver.status.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * File meta structure, holds file information such as file id, version, name,
 * read&write lock.
 * <p/>
 * <strong>Warning:</strong> This structure is thread-unsafe.
 *
 * @author: aaronshan
 */
public class File {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * File's name
     */
    private String name;

    /**
     * File's id, in the global, this value must unique.
     */
    private final long globalId;

    /**
     * File's version.
     */
    private long version = 0;

    /**
     * used for build full id.
     * <p>
     * globalId + SEPARATOR + version
     * </p>
     */
    private final static String SEPARATOR = "_";

    /**
     * read and write lock.
     */
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * whether the file is valid. when a file has been successfully
     * created, this value should be set to true.
     */
    private boolean valid = false;

    /**
     * the file's locations.
     */
    private List<Storage> locations = new ArrayList<Storage>();

    /**
     * parse global id from full file id.
     * <p>
     * full file id: globalId_version
     * </p>
     *
     * @param fileId
     * @return
     */
    public static long getGlobalIdFromFileId(String fileId) {
        String[] tokens = fileId.split(SEPARATOR);

        if (tokens.length < 2) {
            return -1;
        }

        return Long.valueOf(tokens[0]);
    }

    /**
     * parse version from full file id.
     * <p>
     * full file id: globalId_version
     * </p>
     *
     * @param fileId
     * @return
     */
    public static long getVersionFromFielId(String fileId) {
        String[] tokens = fileId.split(SEPARATOR);

        if (tokens.length < 2) {
            return -1;
        }

        return Long.valueOf(tokens[1]);
    }

    /**
     * Construction method.
     *
     * @param name
     * @param globalId
     */
    public File(String name, long globalId) {
        this.name = name;
        this.globalId = globalId;
    }

    /**
     * get file name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * set file name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get file full id.
     *
     * @return
     */
    public String getId() {
        return globalId + SEPARATOR + version;
    }

    /**
     * set the file store locations.
     *
     * @param locations
     */
    public void setLocations(List<Storage> locations) {
        this.locations = locations;
    }

    /**
     * add a location to storage this file.
     *
     * @param storage
     */
    public void addLocation(Storage storage) {
        if (!locations.contains(storage)) {
            this.locations.add(storage);
        }
    }

    /**
     * get duplication number of this file.
     *
     * @return
     */
    public int getLocationsCount() {
        return locations.size();
    }

    /**
     * remove one duplication from its storage list.
     *
     * @param storage
     */
    public void removeLocation(Storage storage) {
        this.locations.remove(storage);
    }

    /**
     * get locations where this file is being stored at.
     *
     * @return
     */
    public List<Storage> getLocations() {
        return locations;
    }

    /**
     * whether this file is valid.
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * set valid of this file
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * update file version.
     */
    public void updateVersion() {
        version++;
    }

    /**
     * get file version.
     *
     * @return
     */
    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Try to get read lock. It will for a specified time to get lock.
     *
     * @param time
     * @param unit
     * @return if lock successfully, return true, else return false.
     */
    public boolean tryLockRead(long time, TimeUnit unit) {
        try {
            return readWriteLock.readLock().tryLock(time, unit);
        } catch (InterruptedException e) {
            logger.error("Try to get read lock error. error is {}.", e.getMessage());
            readWriteLock.readLock().unlock();
            return false;
        }
    }

    /**
     * Release read lock.
     */
    public void unlockRead() {
        readWriteLock.readLock().unlock();
    }

    /**
     * Try to get write lock. It will for a specified time to get lock.
     *
     * @param time
     * @param unit
     * @return if lock successfully, return true, else return false.
     */
    public boolean tryLockWrite(long time, TimeUnit unit) {
        try {
            return readWriteLock.writeLock().tryLock(time, unit);
        } catch (InterruptedException e) {
            logger.error("Try to get write lock error. error is {}.", e.getMessage());
            readWriteLock.writeLock().unlock();
            return false;
        }
    }

    /**
     * Release write lock.
     */
    public void unlockWrite() {
        readWriteLock.writeLock().unlock();
    }
}

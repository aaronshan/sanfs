package aaron.sanfs.nameserver.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Keep status of all storage servers.
 *
 * @author: aaronshan
 * @see Storage
 */
public class Status implements StatusEventListener {

    /**
     * Single pattern instance.
     */
    private static Status instance = new Status();

    /**
     * Storage status.
     */
    private List<Storage> status = new ArrayList<Storage>();

    /**
     * Status event listener, if any status of storage server changes, notify them.
     */
    private List<StatusEventListener> listeners = new ArrayList<StatusEventListener>();

    private Status() {
    }

    /**
     * Get single instance.
     *
     * @return
     */
    public static Status getInstance() {
        return instance;
    }

    /**
     * Fire an <tt>StatusEvent</tt>, notify the listeners.
     *
     * @param event
     */
    private void fireEvent(StatusEvent event) {
        for (StatusEventListener listener : listeners) {
            listener.handle(event);
        }
    }

    /**
     * Add new storage information. Allocate a new timestamp for it.
     *
     * @param storage
     */
    public synchronized void addStorage(Storage storage) {
        storage.addEventListener(this);
        status.add(storage);

        fireEvent(new StatusEvent(StatusEvent.Type.STORAGE_REGISTERED, storage));
    }

    /**
     * Allocate specified number of storage to something.
     *
     * @param count
     * @return
     */
    public synchronized List<Storage> allocateStorage(int count) {
        Collections.sort(status, new Comparator<Storage>() {
            @Override
            public int compare(Storage s1, Storage s2) {
                return s1.getTaskSum() - s2.getTaskSum();
            }
        });

        List<Storage> result = new ArrayList<Storage>();
        for (Storage storage : status) {
            if (count <= 0) {
                break;
            }
            count--;
            result.add(storage);
        }

        return result;
    }

    /**
     * Remove specified storage information.
     *
     * @param storage
     */
    public synchronized void removeStorage(Storage storage) {
        final boolean remove = status.remove(storage);

        if (remove) {
            fireEvent(new StatusEvent(StatusEvent.Type.STORAGE_DEAD, storage));
        }
    }

    /**
     * Test whether we have knowledge about specified storage server.
     *
     * @param id
     * @return
     */
    public synchronized boolean contains(String id) {
        for (Storage storage : status) {
            if (0 == id.compareTo(storage.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get storage server with specified address.
     *
     * @param id
     * @return
     */
    public synchronized Storage getStorage(String id) {
        for (Storage storage : status) {
            if (0 == id.compareTo(storage.getId())) {
                return storage;
            }
        }

        return null;
    }

    /**
     * Get all storage servers.
     *
     * @return
     */
    public synchronized List<Storage> getStorages() {
        List<Storage> result = new ArrayList<Storage>();
        for (Storage storage : status) {
            result.add(storage);
        }

        return result;
    }

    /**
     * Get how many storage server there is.
     *
     * @return
     */
    public synchronized int getStorageNum() {
        return status.size();
    }

    /**
     * Add <tt>StatusEventListener</tt>
     *
     * @param listener
     */
    public void addEventListener(StatusEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove <tt>StatusEventListener</tt>
     *
     * @param listener
     */
    public void removeEventListener(StatusEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void handle(StatusEvent event) {
        fireEvent(event);
    }
}

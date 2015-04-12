package aaron.sanfs.common.call.s2n;

import aaron.sanfs.common.call.Call;

import java.util.List;

/**
 * Synchronize call, sent from <tt>StorageServer</tt> to <tt>NameServer</tt>.
 * <p/>
 * <tt>StorageServer</tt> will attach all of his files (id) into this call, and
 * <tt>NameServer</tt> willd decide which files of them should be deleted.
 *
 * @author lishunyang
 */
public class SyncCallS2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 5000625749190241770L;

    /**
     * Address of this <tt>StorageServer</tt>.
     */
    private final String address;

    /**
     * Saving load information.
     */
    private final int load;

    /**
     * Files that this <tt>StorageServer</tt> has now.
     */
    private final List<String> files;

    /**
     * Construction method.
     *
     * @param address
     * @param files
     * @param load
     */
    public SyncCallS2N(String address, List<String> files, int load) {
        super(Call.Type.SYNC_S2N);
        this.files = files;
        this.address = address;
        this.load = load;
    }

    /**
     * Get files.
     *
     * @return
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Get address.
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get saving load.
     *
     * @return
     */
    public int getLoad() {
        return load;
    }
}

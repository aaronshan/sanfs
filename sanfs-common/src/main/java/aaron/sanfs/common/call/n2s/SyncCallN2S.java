package aaron.sanfs.common.call.n2s;

import aaron.sanfs.common.call.Call;

import java.util.List;

/**
 * Call of synchronizing files for storage server, sent from <tt>NameServer</tt>
 * to <tt>StorageServer</tt>.
 *
 * @author lishunyang
 */
public class SyncCallN2S extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 2018349521385214230L;

    /**
     * File list, indicates what files shoud be deleted.
     */
    private List<String> files;

    /**
     * Construction method.
     *
     * @param files
     */
    public SyncCallN2S(List<String> files) {
        super(Call.Type.SYNC_N2S);
        this.files = files;
    }

    /**
     * Get list of files which should be deleted.
     *
     * @return
     */
    public List<String> getFiles() {
        return files;
    }
}


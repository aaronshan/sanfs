package aaron.sanfs.common.call.s2n;

import aaron.sanfs.common.call.Call;

import java.util.List;
import java.util.Map;

/**
 * Call of heartbeat, sent from <tt>StorageServer</tt> to <tt>NameServer</tt>.
 * <p/>
 * In heartbeat call, <tt>StorageServer</tt> will append a list of files which
 * have been migrated successfully. Beside, <tt>StorageServer</tt> will attach
 * some other status information to notify <tt>NameServer</tt>.
 * <p/>
 * When <tt>NameServer</tt> receives this heartbeat call, it will know what
 * files has been migrated and update the unmigrated file list of that
 * <tt>StorageServer</tt>.
 *
 * @author lishunyang
 */
public class HeartbeatCallS2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 5334179702773690697L;

    /**
     * Files that have been migrated.
     * <p/>
     * {storage id, list of file id}
     */
    private final int load;

    /**
     * Get saving load.
     *
     * @return
     */
    public int getLoad() {
        return load;
    }

    /**
     * List of files that have been migrated successfully.
     */
    private final Map<String, List<String>> migratedFiles;

    /**
     * Construction method.
     *
     * @param migratedFiles
     * @param load
     */
    public HeartbeatCallS2N(Map<String, List<String>> migratedFiles, int load) {
        super(Call.Type.HEARTBEAT_S2N);
        this.migratedFiles = migratedFiles;
        this.load = load;
    }

    /**
     * Get migrated files.
     *
     * @return
     */
    public Map<String, List<String>> getMigratedFiles() {
        return migratedFiles;
    }
}


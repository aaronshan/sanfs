package aaron.sanfs.common.call.n2s;

import aaron.sanfs.common.call.Call;

import java.util.List;
import java.util.Map;

/**
 * File migration call sent from <tt>NameServer</tt> to <tt>StorageServer</tt>.
 *
 * @author lishunyang
 */
public class MigrateFileCallN2S extends Call {
    private static final long serialVersionUID = -5525795765372507337L;

    /**
     * Maps indicates what and where to migrate.
     */
    private Map<String, List<String>> files;

    /**
     * Construction method.
     *
     * @param files
     */
    public MigrateFileCallN2S(Map<String, List<String>> files) {
        super(Call.Type.MIGRATE_FILE_N2S);
        this.files = files;
    }

    /**
     * Get migration information.
     *
     * @return Files which is needed to be migrated from other storage server.
     * <p/>
     * Each entry contains two elements:
     * <p/>
     * {storage server address, list of file id}
     * <p/>
     * "storage server address" indicates where to get migrated files.
     * <p/>
     * "list of files" contains what file should be migrated.
     */
    public Map<String, List<String>> getFiles() {
        return files;
    }
}


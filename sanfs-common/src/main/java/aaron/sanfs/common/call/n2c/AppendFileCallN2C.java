package aaron.sanfs.common.call.n2c;

import aaron.sanfs.common.call.Call;

import java.util.List;

/**
 * Call of appending file, sent from <tt>NameServer</tt> to <tt>Client</tt>.
 *
 * @author lishunyang
 */
public class AppendFileCallN2C extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -3738634987359667308L;

    /**
     * Id of storage server where the file is.
     */
    private final List<String> locations;

    /**
     * File id.
     */
    private String fid;

    /**
     * Construction method.
     *
     * @param fid
     * @param locations
     */
    public AppendFileCallN2C(String fid, List<String> locations) {
        super(Call.Type.APPEND_FILE_N2C);
        this.fid = fid;
        this.locations = locations;
    }

    /**
     * Get locations.
     *
     * @return
     */
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Get file id.
     *
     * @return
     */
    public String getFileId() {
        return fid;
    }
}
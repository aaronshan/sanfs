package aaron.sanfs.common.call.n2c;

import aaron.sanfs.common.call.Call;

import java.util.List;

/**
 * Call of getting file, sent from <tt>NameServer</tt> to <tt>Client</tt>.
 *
 * @author lishunyang
 */
public class GetFileCallN2C extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -3738634987359667308L;

    /**
     * Id of storage servers where the file is.
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
    public GetFileCallN2C(String fid, List<String> locations) {
        super(Call.Type.GET_FILE_N2C);
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

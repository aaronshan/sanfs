package aaron.sanfs.common.call.n2c;

import aaron.sanfs.common.call.Call;

import java.util.List;

/**
 * Call of adding file, sent from <tt>NameServer</tt> to <tt>Client</tt>.
 *
 * @author lishunyang
 */
public class AddFileCallN2C extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 32014432346467370L;

    /**
     * File id.
     */
    private String fid;

    /**
     * Id of storage that the file belongs.
     */
    private final List<String> locations;

    /**
     * Construction method.
     *
     * @param fid
     * @param locations
     */
    public AddFileCallN2C(String fid, List<String> locations) {
        super(Type.ADD_FILE_N2C);
        this.fid = fid;
        this.locations = locations;
    }

    /**
     * Get file id.
     *
     * @return
     */
    public String getFileId() {
        return fid;
    }

    /**
     * Get location.
     *
     * @return
     */
    public List<String> getLocations() {
        return locations;
    }
}

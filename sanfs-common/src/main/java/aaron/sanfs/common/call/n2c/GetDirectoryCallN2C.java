package aaron.sanfs.common.call.n2c;

import aaron.sanfs.common.call.Call;

import java.util.ArrayList;
import java.util.List;

/**
 * Call of getting directory, sent from <tt>NameServer</tt> to <tt>Client</tt>.
 *
 * @author lishunyang
 */
public class GetDirectoryCallN2C extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -2876278623815280597L;

    /**
     * Files and directories in the specified directory.
     */
    private List<String> filesAndDirectories = new ArrayList<String>();

    /**
     * Construction method.
     *
     * @param filesAndDirectories
     */
    public GetDirectoryCallN2C(List<String> filesAndDirectories) {
        super(Call.Type.GET_DIRECTORY_N2C);
        this.filesAndDirectories = filesAndDirectories;
    }

    /**
     * Get files and directories in the specified directory.
     *
     * @return
     */
    public List<String> getFilesAndDirectories() {
        return filesAndDirectories;
    }
}

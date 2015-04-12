package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of getting things belongs a specified directory, sent from
 * <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class GetDirectoryCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -8496848383621955822L;

    /**
     * Directory name.
     */
    private String dirName;

    /**
     * Construction method.
     *
     * @param dirName
     */
    public GetDirectoryCallC2N(String dirName) {
        super(Call.Type.GET_DIRECTORY_C2N);
        this.dirName = dirName;
    }

    /**
     * Get directory name.
     *
     * @return
     */
    public String getDirName() {
        return dirName;
    }
}


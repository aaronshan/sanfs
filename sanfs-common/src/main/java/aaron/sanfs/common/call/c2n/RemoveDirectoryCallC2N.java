package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of removing directory, sent from <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class RemoveDirectoryCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -7582143207284111922L;

    /**
     * Directory name.
     */
    private String dirName;

    /**
     * Construction method.
     *
     * @param dirName
     */
    public RemoveDirectoryCallC2N(String dirName) {
        super(Call.Type.REMOVE_DIRECTORY_C2N);
        this.dirName = dirName;
    }

    /**
     * Get direcotry name.
     *
     * @return
     */
    public String getDirectoryName() {
        return dirName;
    }

}


package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of moving directory from one place to another, sent from <tt>Client</tt>
 * to <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class MoveDirectoryCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -3928327992229216635L;

    /**
     * Old directory name.
     */
    private String oldDirName;

    /**
     * New directory name.
     */
    private String newDirName;

    /**
     * Construction method.
     *
     * @param oldDirName
     * @param newDirName
     */
    public MoveDirectoryCallC2N(String oldDirName, String newDirName) {
        super(Call.Type.MOVE_DIRECTORY_C2N);
        this.oldDirName = oldDirName;
        this.newDirName = newDirName;
    }

    /**
     * Get old directory name.
     *
     * @return
     */
    public String getOldDirectoryName() {
        return oldDirName;
    }

    /**
     * Get new directory name.
     *
     * @return
     */
    public String getNewDirectoryName() {
        return newDirName;
    }
}

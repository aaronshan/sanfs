package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of moving file from one place to another, sent from <tt>Client</tt> to
 * <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class MoveFileCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 1336355298379688981L;

    /**
     * Old directory name.
     */
    private String oldDirName;

    /**
     * Old file name.
     */
    private String oldFileName;

    /**
     * New directory name.
     */
    private String newDirName;

    /**
     * New file name.
     */
    private String newFileName;

    /**
     * Construction method.
     *
     * @param oldDirName
     * @param oldFileName
     * @param newDirName
     * @param newFileName
     */
    public MoveFileCallC2N(String oldDirName, String oldFileName,
                           String newDirName, String newFileName) {
        super(Call.Type.MOVE_FILE_C2N);
        this.oldDirName = oldDirName;
        this.oldFileName = oldFileName;
        this.newDirName = newDirName;
        this.newFileName = newFileName;
    }

    /**
     * Get old directory name.
     *
     * @return
     */
    public String getOldDirName() {
        return oldDirName;
    }

    /**
     * Get old file name.
     *
     * @return
     */
    public String getOldFileName() {
        return oldFileName;
    }

    /**
     * Get new directory name.
     *
     * @return
     */
    public String getNewDirName() {
        return newDirName;
    }

    /**
     * Get new file name.
     *
     * @return
     */
    public String getNewFileName() {
        return newFileName;
    }
}


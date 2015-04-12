package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of removing file, sent from <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author lishunyang
 */
public class RemoveFileCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 7834245963090160026L;

    /**
     * Directory name.
     */
    private String dirName;

    /**
     * File name.
     */
    private String fileName;

    /**
     * Construction methdo.
     *
     * @param dirName
     * @param fileName
     */
    public RemoveFileCallC2N(String dirName, String fileName) {
        super(Call.Type.REMOVE_FILE_C2N);
        this.dirName = dirName;
        this.fileName = fileName;
    }

    /**
     * Get directory name.
     *
     * @return
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * Get file name.
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }
}

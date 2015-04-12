package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of adding file, sent from <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author: aaronshan
 */
public class AddFileCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -805598603013291328L;

    /**
     * Directory name.
     */
    private String dirName;

    /**
     * File name.
     */
    private String fileName;

    /**
     * Construction method.
     *
     * @param dirName
     * @param fileName
     */
    public AddFileCallC2N(String dirName, String fileName) {
        super(Call.Type.ADD_FILE_C2N);
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

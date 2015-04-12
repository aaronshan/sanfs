package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of appending file, sent from <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class AppendFileCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = 5944057489040298495L;

    /**
     * Directory name.
     */
    private final String dirName;

    /**
     * File name.
     */
    private final String fileName;

    /**
     * Construction method.
     *
     * @param dirName
     * @param fileName
     */
    public AppendFileCallC2N(String dirName, String fileName) {
        super(Call.Type.APPEND_FILE_C2N);
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

package aaron.sanfs.common.call.c2n;

import aaron.sanfs.common.call.Call;

/**
 * Call of getting file, sent from <tt>Client</tt> to <tt>NameServer</tt>.
 *
 * @author aaronshan
 */
public class GetFileCallC2N extends Call {
    /**
     * Serial id.
     */
    private static final long serialVersionUID = -6321724391912644392L;

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
    public GetFileCallC2N(String dirName, String fileName) {
        super(Call.Type.GET_FILE_C2N);
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


package aaron.sanfs.storageserver;

import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.common.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operate with file system for StorageServer
 *
 * @author dengshihong
 */
public class Storage {
    /**
     * Logger
     */
    public static final Logger logger = LoggerFactory.getLogger(Storage.class);
    /**
     * Max Storage Size, configurable
     */
    public static final long MAXSTORAGE = Configuration.getInstance().getLong(
            Configuration.SS_MAX_STORAGE);
    /**
     * StorageDir containing the current files
     */
    public static final String STORAGE_DIR_CURRENT = "current";
    /**
     * StorageDir containing the files on transport, moved to current when
     * transport finished
     */
    public static final String STORAGE_DIR_TRANS = "trans";
    /**
     * StorageDir containing the files removed, cleared when StorageServer reset
     */
    public static final String STORAGE_DIR_REMOVE = "removed";

    /**
     * Handle the Directory of the Storage
     */
    private StorageDirectory storageDir;

    /**
     * @param location Root directory
     * @throws java.io.IOException
     */
    public Storage(String location) throws IOException {
        File rootFile = new File(location);

        // Make sure location is exist and is a directory
        if (rootFile.exists() == false)
            rootFile.mkdirs();
        else {
            if (rootFile.isFile()) {
                rootFile.delete();
                rootFile.mkdir();
            }
        }
        storageDir = new StorageDirectory(rootFile);
    }

    /**
     * @return Percentage of the Storage Used
     */
    public int analyzeStorageLoad() {
        long load = 0;
        for (File file : storageDir.getCurrentDir().listFiles()) {
            if (file.isFile())
                load += file.length();
        }

        int t = (int) (load / MAXSTORAGE) * 100;
        if (t > 100)
            t = 100;
        return t;
    }

    /**
     * @return All existing files on the StorageServer
     */
    public List<String> analyzeCurrentFiles() {
        List<String> files = new ArrayList<String>();
        for (File file : storageDir.getCurrentDir().listFiles()) {
            if (file.isFile())
                files.add(file.getName());
        }
        return files;
    }

    /**
     * @param files
     */
    public void removefiles(List<String> files) {
        File curDir = storageDir.getCurrentDir();
        File removeDir = storageDir.getRemoveDir();
        for (String filename : files) {
            File src = new File(curDir.getAbsolutePath() + "//" + filename);
            File dest = new File(removeDir.getAbsolutePath() + "//" + filename);
            if (src.exists())
                src.renameTo(dest);
        }
    }

    /**
     * Get the file in transport directory, delete the origin file if exists
     *
     * @param filename
     * @return
     */
    public File getTransFile(String filename) {
        File file = new File(storageDir.getTransDir().getAbsolutePath() + "//"
                + filename);
        if (file.exists())
            file.delete();
        return file;
    }

    /**
     * Get the file in transport directory, not delete the origin file
     *
     * @param filename
     * @return
     */
    public File getTransFileNotDelete(String filename) {
        File file = new File(storageDir.getTransDir().getAbsolutePath() + "//"
                + filename);
        return file;
    }

    /**
     * Get the file in current directory
     *
     * @param name
     * @return
     * @throws IOException
     */
    public File getFile(String name) throws IOException {
        File file = new File(storageDir.getCurrentDir().getAbsoluteFile()
                + "//" + name);
        if (file.exists() == false || file.isFile() == false)
            throw (new IOException("GetFile: file not exist."));
        return file;
    }

    /**
     * Move the file from transport directory to current directory when
     * transport finished
     *
     * @param name
     * @throws IOException
     */
    public void transSuccess(String name) throws IOException {
        File curDir = storageDir.getCurrentDir();
        File transDir = storageDir.getTransDir();
        File dest = new File(curDir.getAbsolutePath() + "//" + name);
        File src = new File(transDir.getAbsolutePath() + "//" + name);
        if (dest.exists()) {
            dest.delete();
        }
        if (src.exists() && src.renameTo(dest)) {

        } else
            throw (new IOException("source file not exist."));
    }

    /**
     * Handle directory of the StorageServer
     *
     * @author dengshihong
     */
    public static class StorageDirectory {
        /**
         * Root directory
         */
        private File root;

        /**
         * @param dir
         * @throws IOException
         */
        public StorageDirectory(File dir) throws IOException {
            root = dir;
            File curDir = getCurrentDir();
            File transDir = getTransDir();
            File removeDir = getRemoveDir();
            if (curDir.exists() == false)
                curDir.mkdir();
            else if (curDir.isFile()) {
                curDir.delete();
                curDir.mkdir();
            }
            if (transDir.exists() == false)
                transDir.mkdir();
            else if (transDir.isFile()) {
                transDir.delete();
                transDir.mkdir();
            } else
                clearDirectory(transDir);
            if (removeDir.exists() == false)
                removeDir.mkdir();
            else if (removeDir.isFile()) {
                removeDir.delete();
                removeDir.mkdir();
            } else
                clearDirectory(removeDir);
        }

        /**
         * @return Root directory
         */
        public File getFile() {
            return root;
        }

        /**
         * @param dir
         * @throws IOException
         */
        public void clearDirectory(File dir) throws IOException {
            FileUtil.fullyDeleteContent(dir);
        }

        /**
         * @return Current directory
         */
        public File getCurrentDir() {
            return new File(root, STORAGE_DIR_CURRENT);
        }

        /**
         * @return Transport directory
         */
        public File getTransDir() {
            return new File(root, STORAGE_DIR_TRANS);
        }

        /**
         * @return Remove directory
         */
        public File getRemoveDir() {
            return new File(root, STORAGE_DIR_REMOVE);
        }
    }
}


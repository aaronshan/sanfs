package aaron.sanfs.nameserver.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * Directory structure, keep directory meta information such as name, files.
 * <p/>
 * <strong>Warning:</strong> This structure is thread-unsafe.
 *
 * @author: aaronshan
 * @see aaron.sanfs.nameserver.meta.File
 */
public class Directory {
    private String name;

    /**
     * Files belong to this directory. {fileName, file}
     */
    private Map<String, File> files = new HashMap<String, File>();

    /**
     * Indicate whether this directory is valid. After the directory has been
     * successfully created, this should be true.
     */
    private boolean valid = false;

    /**
     * Construction method.
     *
     * @param name
     */
    public Directory(String name) {
        this.name = name;
    }

    /**
     * Get directory name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get specified file that belongs to this directory.
     *
     * @param fileName
     * @return
     */
    public synchronized File getFile(String fileName) {
        return files.get(fileName);
    }

    /**
     * Add new file to this directory.
     * <p/>
     * <strong>Warning:</strong> If there is already a same file, it will be replaced.
     *
     * @param file
     */
    public synchronized void addFile(File file) {
        files.put(file.getName(), file);
    }

    /**
     * Remove a specified file from this directory.
     *
     * @param fileName
     * @return The removed file.
     */
    public synchronized File removeFile(String fileName) {
        return files.remove(fileName);
    }

    /**
     * Test whether a specified file belongs to this directory.
     *
     * @param fileName
     * @return
     */
    public synchronized boolean contains(String fileName) {
        return files.containsKey(fileName);
    }

    /**
     * Test whether the directory is valid.
     *
     * @return
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Set valid bit of this directory.
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Set the directory name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get all valid files of this directory.
     *
     * @return
     */
    public synchronized Map<String, String> getValidFileNameList() {
        Map<String, String> fileList = new HashMap<String, String>();

        for (File file : files.values()) {
            if (file.isValid()) {
                fileList.put(file.getName(), file.getId());
            }
        }

        return fileList;
    }
}

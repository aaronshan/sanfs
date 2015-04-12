package aaron.sanfs.nameserver.util;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.nameserver.meta.Directory;
import aaron.sanfs.nameserver.meta.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Backup tools, used for save/restore <tt>Meta</tt> image, save/restore log message.
 *
 * @author: aaronshan
 */
public class BackupUtil {
    private final static Logger logger = LoggerFactory.getLogger(BackupUtil.class);

    /**
     * Log file name.
     */
    private final static String logFileName = "LOG";

    /**
     * Image file name.
     */
    private final static String imageFileName = "IMAGE";

    /**
     * Time format suffix of image file name.
     */
    private final static DateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Separator.
     */
    private final static String SEPERATOR = " ";

    /**
     * Issue log flag.
     */
    private final static String ISSUE = "<ISSUE>";

    /**
     * Commit log flag.
     */
    private final static String COMMIT = "<COMMIT>";

    /**
     * Single instance pattern.
     */
    private static BackupUtil instance = null;

    /**
     * Log file directory
     */
    private static String logDirName;

    /**
     * Image file directory.
     */
    private static String imageDirName;

    private BackupUtil() throws Exception {
        imageDirName = Configuration.getInstance().getString(Configuration.META_BACKUP_DIR_KEY);
        logDirName = Configuration.getInstance().getString(Configuration.META_LOG_DIR_KEY);

        if (imageDirName.isEmpty() || logDirName.isEmpty()) {
            throw new Exception("Failed to initialize BackupUtil, image saving directory or log saving directory not found.");
        }

        File dir = null;
        dir = new File(imageDirName);
        if (!dir.exists()) {
            logger.info("Create image saving directory {}.", imageDirName);
            dir.mkdirs();
        }

        dir = new File(logDirName);
        if (!dir.exists()) {
            logger.info("Create log saving directory {}.", logDirName);
            dir.mkdirs();
        }
    }

    /**
     * Get <tt>BackupUtil</tt> instance.
     *
     * @return
     */
    public synchronized static BackupUtil getInstance() {
        try {
            if (null == instance) {
                instance = new BackupUtil();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return instance;
    }

    /**
     * Write backup image file.
     * <p/>
     * Other threads can't visit <tt>Meta</tt> until writing process is over.
     * <p/>
     * After image file finished, log file will be deleted.
     */
    public synchronized void writeBackupImage() {
        final Meta meta = Meta.getInstance();

        synchronized (meta) {
            logger.info("Write backup image file.");
            final String imageFilePath = imageDirName + imageFileName + "_" + timeFormat.format(new Date());
            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(imageFilePath));
                for (Map.Entry<String, Map<String, String>> entry : meta.getValidDirectoryList().entrySet()) {
                    writer.write(entry.getKey() + SEPERATOR + entry.getValue().size());
                    writer.newLine();
                    for (Map.Entry<String, String> stringEntry : entry.getValue().entrySet()) {
                        writer.write(SEPERATOR + stringEntry.getKey() + SEPERATOR + stringEntry.getValue());
                        writer.newLine();
                    }
                }

            } catch (IOException e) {
                e.fillInStackTrace();
            } finally {
                if (null != writer) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Restore <tt>Meta</tt> from image file.
     * <p/>
     * If there are many image files, it will only choose the most recently
     * created image file.
     */
    public synchronized void readBackupImage() {
        final Meta meta = Meta.getInstance();

        synchronized (meta) {
            logger.info("Read backup image file.");
            final File dir = new File(imageDirName);

            File[] backups = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(imageFileName);
                }
            });

            if (backups.length <= 0) {
                logger.error("Failed to read backup image file, file didn't exist.");
                return;
            }

            Arrays.sort(backups, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f2.getName().compareTo(f1.getName());
                }
            });

            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(imageFileName + backups[0].getName()));
                String line = null;

                while (true) {
                    line = reader.readLine();
                    if (null == line) {
                        break;
                    }

                    String[] dirInfo = line.split(SEPERATOR);
                    // +---------------+------------+
                    // |0              |1           |
                    // +---------------+------------+
                    // |directory name |file number |
                    // +---------------+------------+
                    if (dirInfo.length != 2) {
                        logger.error("Failed to read image file line, damaged line.");
                        break;
                    }

                    final String dirName = dirInfo[0];
                    final int numberFiles = Integer.valueOf(dirInfo[1]);

                    if (0 == numberFiles) {
                        Directory directory = new Directory(dirName);
                        meta.addDirectory(directory);
                        meta.setDirectoryValid(dirName, true);
                    }

                    for (int i = 0; i < numberFiles; i++) {
                        String fileLine = reader.readLine();
                        String[] fileInfo = fileLine.split(SEPERATOR);

                        // +--+----------+--------+
                        // |0 |1         |2       |
                        // +--+----------+--------+
                        // |  |file name |file id |
                        // +--+----------+--------+
                        if (fileInfo.length != 3) {
                            logger.error("Some files couldn't be restore, information was lost.");
                            continue;
                        }

                        final String fileName = fileInfo[1];
                        final String fileId = fileInfo[2];
                        final long globalFileId = aaron.sanfs.nameserver.meta.File.getGlobalIdFromFileId(fileId);
                        final long fileVersion = aaron.sanfs.nameserver.meta.File.getVersionFromFielId(fileId);
                        final aaron.sanfs.nameserver.meta.File file = new aaron.sanfs.nameserver.meta.File(fileName, globalFileId);
                        file.setVersion(fileVersion);
                        meta.addFile(dirName, file);
                        meta.setFileValid(dirName, fileName, true);
                    }
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Write operation issue log.
     * <p/>
     * <strong>Warning:</strong> Only those operations that modify <tt>Meta</tt>
     * need to be recorded, such as "add file/directory" and "rename file/directory",
     * other operation will just be ignored. Every operation log item contains of two
     * parts: issue and commit.
     * <p/>
     * Once an operation finished, commit log will be written to log file, and then update
     * <tt>Meta</tt> structure.
     *
     * @param tid         task id
     * @param type        call type
     * @param description
     */
    public synchronized void writeLogIssue(long tid, Call.Type type, String description) {
        final String logFilePath = logDirName + logFileName;
        Writer writer = null;

        try {
            writer = new FileWriter(new File(logFilePath), true);
            writer.write(ISSUE + SEPERATOR + tid + SEPERATOR + type + SEPERATOR + description + SEPERATOR + ISSUE + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Write operation commit log.
     * <p>
     * <strong>Warning:</strong> Only those operations that modify <tt>Meta</tt>
     * need to be recorded, such as "add file/directory" and "rename file/directory",
     * other operation will just be ignored. Every operation log item contains of two
     * parts: issue and commit.
     * <p>
     * Once an operation started, issue log will be written to log file.
     * </p>
     * After the operation finished, commit log will be written to log file, and then
     * update <tt>Meta</tt> structure.
     *
     * @param tid task id
     */
    public synchronized void writeLogCommit(long tid) {
        final String logFilePath = logDirName + logFileName;
        Writer writer = null;

        try {
            writer = new FileWriter(new File(logFilePath), true);
            writer.write(COMMIT + SEPERATOR + tid + SEPERATOR + COMMIT + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete log file.
     */
    public synchronized void deleteBackLog() {
        final String logFilePath = logDirName + logFileName;

        File file = new File(logFilePath);
        file.delete();
    }

    public synchronized void readBackupLog() {
        final Meta meta = Meta.getInstance();
        final Queue<String[]> suspendedTasks = new LinkedList<String[]>();
        final Set<String> committedTaskids = new HashSet<String>();
        final String logFilePath = logDirName + logFileName;
        BufferedReader reader = null;

        File file = new File(logFilePath);
        if (!file.exists()) {
            logger.error("Failed to read backup log, file didn't exist.");
            return;
        }

        synchronized (meta) {
            try {
                reader = new BufferedReader(new FileReader(logFilePath));
                String line = null;
                String[] tokens;

                while (true) {
                    line = reader.readLine();

                    //EOF
                    if (null == line) {
                        break;
                    }

                    tokens = line.split(SEPERATOR);
                    // +------+--------+----+----+
                    // |0     |1       |... |n-1 |
                    // +------+--------+----+----+
                    // |begin |task id |... |end |
                    // +------+--------+----+----+
                    if (tokens.length < 3) {
                        logger.info("BackupUtil: damaged log line, ignore it.");
                        continue;
                    }

                    final String begin = tokens[0];
                    final String end = tokens[tokens.length - 1];
                    final String taskId = tokens[1];

                    // Issue
                    if (0 == begin.compareTo(ISSUE) && 0 == end.compareTo(ISSUE)) {
                        suspendedTasks.add(tokens);
                    }

                    // Commit
                    else if (0 == begin.compareTo(COMMIT) && 0 == end.compareTo(COMMIT)) {
                        committedTaskids.add(taskId);
                    }
                }

                while (!suspendedTasks.isEmpty()) {
                    tokens = suspendedTasks.poll();

                    // +------+--------+----------+----+----+
                    // |0     |1       |2         |... |n-1 |
                    // +------+--------+----------+----+----+
                    // |begin |task id |call type |... |end |
                    // +------+--------+----------+----+----+
                    if (tokens.length < 4) {
                        logger.error("BackupUtil: damaged log line, ignore it.");
                        continue;
                    }

                    final String taskId = tokens[1];
                    final String callType = tokens[2];

                    // if the task is not committed, just ignore it.
                    if (!committedTaskids.contains(taskId)) {
                        continue;
                    }

                    if (callEqual(callType, Call.Type.ADD_FILE_C2N)) {
                        final String dirName = tokens[3];
                        final String fileName = tokens[4];
                        final String fileId = tokens[5];
                        final long globalFileId = aaron.sanfs.nameserver.meta.File.getGlobalIdFromFileId(fileId);
                        final aaron.sanfs.nameserver.meta.File f = new aaron.sanfs.nameserver.meta.File(fileName, globalFileId);
                        meta.setFileValid(dirName, fileName, true);

                        meta.addFile(dirName, f);
                        meta.setFileValid(dirName, fileName, true);
                    } else if (callEqual(callType, Call.Type.MOVE_FILE_C2N)) {
                        final String oldDirName = tokens[3];
                        final String oldFileName = tokens[4];
                        final String newDirName = tokens[5];
                        final String newFileName = tokens[6];

                        meta.renameFile(oldDirName, oldFileName, newDirName, newFileName);
                    } else if (callEqual(callType, Call.Type.MOVE_DIRECTORY_C2N)) {
                        final String oldDirName = tokens[3];
                        final String newDirName = tokens[4];

                        meta.renameDirectory(oldDirName, newDirName);
                    } else if (callEqual(callType, Call.Type.REMOVE_FILE_C2N)) {
                        final String dirName = tokens[3];

                        meta.removeDirectory(dirName);
                    } else if (callEqual(callType, Call.Type.REMOVE_DIRECTORY_C2N)) {
                        final String dirName = tokens[3];

                        meta.removeDirectory(dirName);
                    } else if (callEqual(callType, Call.Type.APPEND_FILE_C2N)) {
                        final String dirName = tokens[3];
                        final String fileName = tokens[4];

                        meta.getFile(dirName, fileName);
                    } else {
                        logger.info("BackupUtil: unknown operation: {}.", callType);
                    }

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test whether a log line is specified call type.
     *
     * @param logCall
     * @param callType
     * @return
     */
    private boolean callEqual(String logCall, Call.Type callType) {
        return logCall.compareTo(callType.toString()) == 0;
    }
}

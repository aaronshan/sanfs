package aaron.sanfs.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author dengshihong
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static boolean deleteFile(final File file) {
        final boolean wasDeleted = file.delete();
        if (wasDeleted) {
            return true;
        }
        final boolean exist = file.exists();
        if (exist) {
            logger.warn("Failed to delete file [{}]: it still exists.", file.getAbsolutePath());
        }
        return !exist;
    }

    public static boolean fullyDeleteContent(final File dir) {
        boolean deleteSucceeded = true;
        final File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (file.isFile()) {
                    if (!deleteFile(file))
                        deleteSucceeded = false;
                }
            }
        }
        return deleteSucceeded;
    }

    public static boolean copyFile(String src, String dst) {
        int byteSum = 0;
        int byteRead = 0;
        InputStream in = null;
        OutputStream out = null;
        try {
            File srcFile = new File(src);
            if (srcFile.isFile()) {
                in = new FileInputStream(src);
                out = new FileOutputStream(dst);
                byte[] buffer = new byte[2000];
                while ((byteRead = in.read(buffer)) != -1) {
                    byteSum += byteRead;
                    out.write(buffer, 0, byteRead);
                }
            }
            in.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

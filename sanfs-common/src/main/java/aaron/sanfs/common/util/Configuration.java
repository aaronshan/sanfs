package aaron.sanfs.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @uthor: aaronshan
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static Properties prop = new Properties();

    private volatile static Configuration instance = null;

    public static final String CONFIGURATION_PATH = System.getProperty("user.dir") + "/conf/conf.properties";

    public static final String CONF_DEFAULT_STRING = "NOT FOUND";

    public static final long CONF_DEFAULT_LONG = -1;

    public static final int CONF_DEFAULT_INTEGER = -1;

    public static final String HEARTBEAT_INTERVAL_KEY = "heartbeat_interval";

    public static final String LEASE_PERIOD_KEY = "lease_period";

    public static final String TASK_CHECK_INTERVAL_KEY = "task_check_interval";

    public static final String DUPLICATE_KEY = "duplicate_number";

    public static final String META_BACKUP_DIR_KEY = "meta_image_dir";

    public static final String META_LOG_DIR_KEY = "meta_log_dir";

    public static final String META_BACKUP_INTERVAL_KEY = "meta_backup_interval";

    public static final String SS_REG_INTERVAL = "ss_registeration_interval";
    public static final String SS_HB_INTERVAL = "ss_heartbeat_interval";
    public static final String SS_SYNC_INTERVAL = "ss_sync_interval";

    public static final String SS_MAX_STORAGE = "ss_max_storage";

    private Configuration() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(
                Configuration.CONFIGURATION_PATH));
        prop.load(in);
        in.close();
    }

    public static Configuration getInstance() {
        synchronized (Configuration.class) {
            if (null == instance) {
                try {
                    instance = new Configuration();
                } catch (IOException e) {
                    e.printStackTrace();
                    instance = null;
                }
            }
        }

        return instance;
    }

    private String getProperty(String key) {
        return prop.getProperty(key);
    }

    public Long getLong(String key) {
        if (prop.containsKey(key))
            return Long.valueOf(getProperty(key));
        else
            return Configuration.CONF_DEFAULT_LONG;
    }

    public Integer getInteger(String key) {
        if (prop.containsKey(key))
            return Integer.valueOf(getProperty(key));
        else
            return Configuration.CONF_DEFAULT_INTEGER;
    }

    public String getString(String key) {
        if (prop.containsKey(key))
            return getProperty(key);
        else
            return Configuration.CONF_DEFAULT_STRING;
    }
}

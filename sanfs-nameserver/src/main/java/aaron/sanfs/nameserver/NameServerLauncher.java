package aaron.sanfs.nameserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher of <tt>NameServer</tt>.
 *
 * @author lishunyang
 * @see NameServer
 */
public class NameServerLauncher {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(NameServerLauncher.class);

    /**
     * Launching entry.
     *
     * @param args
     */
    public static void main(String[] args) {
        NameServer nameServer = new NameServer();

        try {
            nameServer.initilize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("NameServer started.");
    }
}

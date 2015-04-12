package aaron.sanfs.storageserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dengshihong
 */
public class StorageServerLauncher {
    private final static Logger logger = LoggerFactory.getLogger(StorageServerLauncher.class);

    private static final String BASE_ARG_NAME = "--base=";

    private static final String PORT_ARG_NAME = "--port=";

    public static void main(String[] args) {
        String base = "";
        int port = -1;

        for (String arg : args) {
            if (arg.startsWith(BASE_ARG_NAME))
                base = arg.substring(BASE_ARG_NAME.length());
            else if (arg.startsWith(PORT_ARG_NAME))
                port = Integer.valueOf(arg.substring(PORT_ARG_NAME.length()));
        }

        if (base.isEmpty() || port < 0) {
            System.out.println("Failed to start StorageServer, bad parameter.");
            return;
        }

        StorageServer storageServer;
        try {
            storageServer = new StorageServer("baseDirectory");
            storageServer.initAndstart(port);
        } catch (Exception e) {
            logger.info("Failed to start StorageServer.");
            e.printStackTrace();
        }

        logger.info("StorageServer started.");
    }
}


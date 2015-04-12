package aaron.sanfs.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLauncher {
    private static final Logger logger = LoggerFactory.getLogger(ClientLauncher.class);

    public static void main(String[] args) {
        ClientGUI client = new ClientGUI();
        client.init();

        logger.info("Client initialize finished.");
    }
}
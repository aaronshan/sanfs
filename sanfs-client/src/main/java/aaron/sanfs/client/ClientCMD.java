package aaron.sanfs.client;

import aaron.sanfs.common.task.Task;
import aaron.sanfs.common.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * test part of functions of Client by cmdline
 *
 * @author gengyufeng
 */
public class ClientCMD extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ClientCMD.class);

    private Configuration configuration;
    private Client client;
    private String usage;

    public ClientCMD() {
        this.configuration = Configuration.getInstance();
        this.client = Client.getInstance();
        this.usage = configuration.getString("usage");
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            logger.info("waiting for command(any input for usage):");
            try {
                String inputLine = reader.readLine();
                String[] args = inputLine.split(" ");
                String cmdString = args[0];

                Task task = null;

                if (cmdString.toLowerCase().equals("addfile")) {
                    if (args.length != 3) {
                        logger.error("Wrong argument number.");
                        continue;
                    } else {
                        client.addFileAsync(args[1], args[2], new File(args[1] + args[2]));
                    }
                } else {
                    logger.info(usage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ClientCMD clientCMD = new ClientCMD();
        clientCMD.start();
    }
}

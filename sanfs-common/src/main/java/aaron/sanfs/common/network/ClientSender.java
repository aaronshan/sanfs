package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * a connection between client and server(including storage server???)
 *
 * @author geng yufeng
 */
public class ClientSender extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ClientSender.class);

    private ClientConnector connector;
    private OutputStream outputStream;

    public ClientSender(ClientConnector connector, OutputStream outputStream) {
        this.connector = connector;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {

        while (true) {
            try {
                Call cmd = connector.getCommandCall();
                logger.debug("Client sending command: " + cmd.getType());
                outputStream.write(ObjectUtil.ObjectToByte(cmd));
                logger.debug("Command sent: {}, size:{}", cmd.getType(), ObjectUtil.ObjectToByte(cmd).length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}

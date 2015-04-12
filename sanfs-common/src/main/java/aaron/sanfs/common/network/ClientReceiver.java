package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.common.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * holds socket with name server, wait for any data input. read data and translate
 * them into Call
 *
 * @author gengyufeng
 */
public class ClientReceiver extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ClientReceiver.class);

    private ClientConnector connector;
    private InputStream inputStream;

    public ClientReceiver(ClientConnector connector, InputStream inputStream) {
        this.connector = connector;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {

        while (true) {
            try {
                int buffer_size = Configuration.getInstance().getInteger("ByteBuffer_size");
                byte[] buffer = new byte[buffer_size];
                int received = inputStream.read(buffer);
                Call response = (Call) ObjectUtil.ByteToObject(buffer);
                connector.addResponseCall(response);
                logger.debug("Client received response: {}, size:{}.", response.getType(), received);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}

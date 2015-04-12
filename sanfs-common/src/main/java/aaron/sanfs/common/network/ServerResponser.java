package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.util.Constant;
import aaron.sanfs.common.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * watch over responseQueue of ServerConnector, and send responses(Call)
 *
 * @author gengyufeng
 */
public class ServerResponser extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ServerResponser.class);

    private ServerConnector connector;
    private ByteBuffer writeBuffer;

    public ServerResponser(ServerConnector _connector) {
        connector = _connector;
        writeBuffer = ByteBuffer.allocate(Constant.ByteBufferSize);
    }


    @Override
    public void run() {
        while (true) {
            Call resp = connector.getResponse();    //will block here
            SocketChannel sc = connector.getChannel(resp.getInitiator());
            writeBuffer.clear();
            try {
                writeBuffer.put(ObjectUtil.ObjectToByte(resp));
                writeBuffer.flip();
                int byteWritten = sc.write(writeBuffer);
                logger.debug("Server response sent: {}, size:{}.", resp.getType(), byteWritten);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}

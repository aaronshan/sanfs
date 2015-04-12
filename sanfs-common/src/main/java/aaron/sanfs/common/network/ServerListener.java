package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.common.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * listen on given port, accept connection or read data
 *
 * @author gengyufeng
 */
public class ServerListener extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ServerListener.class);

    private int port;
    private Configuration configuration;
    private ServerConnector connector;
    private Selector selector;

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    /**
     * setup ServerListener
     *
     * @param connector server connect it belongs to
     * @param port      port to listen
     */
    public ServerListener(ServerConnector connector, int port) {
        this.connector = connector;
        this.port = port;
        try {
            selector = Selector.open();
            configuration = Configuration.getInstance();
            readBuffer = ByteBuffer.allocate(configuration.getInteger("ByteBuffer_size"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            logger.debug("Server listening on port:{}.", port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        dealWithAccept(key);
                    }

                    if (key.isReadable()) {
                        dealWithRead(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * deal with new connection
     *
     * @param key
     */
    private void dealWithAccept(SelectionKey key) {
        try {
            logger.debug("New connection received...");
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = server.accept();
            socketChannel.configureBlocking(false);
            // register read event.
            socketChannel.register(selector, SelectionKey.OP_READ);
            connector.setAddressChannel(socketChannel.socket().getRemoteSocketAddress().toString(), socketChannel);

            logger.info("New connection accepted...({})", socketChannel.socket().getRemoteSocketAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * deal with new call
     *
     * @param key
     */
    private void dealWithRead(SelectionKey key) {
        SocketChannel socketChannel = null;
        try {
            socketChannel = (SocketChannel) key.channel();
            readBuffer.clear();
            //read into r_bBuf
            int byteRead = socketChannel.read(readBuffer);
            readBuffer.flip();
            try {
                //establish a Call object and bind the socket channel
                Call rc = (Call) ObjectUtil.ByteToObject(readBuffer.array());
                rc.setInitiator(socketChannel.socket().getRemoteSocketAddress().toString());
                connector.putCallQueue(rc);
                logger.debug("NameServer received call: {}, size:{}.", rc.getType(), byteRead);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            readBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socketChannel.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}

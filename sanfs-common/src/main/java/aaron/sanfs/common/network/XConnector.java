package aaron.sanfs.common.network;

import aaron.sanfs.common.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * XConnector has two functions:
 * 1. getSocket(): create socket on (ip, port)
 * 2. start up a thread to listen on (port), and feed SocketListeners
 * with socket accepted
 *
 * @author gengyufeng
 */
public class XConnector extends Thread implements SocketDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(XConnector.class);

    private int port = 0;
    private Configuration configuration;
    private ServerSocket serverSocket;

    private List<SocketListener> socketListeners = new ArrayList<SocketListener>();

    public XConnector(int port) {
        configuration = Configuration.getInstance();
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            logger.info("XConnector started listening on port:{}.", port);

            while (true) {
                Socket client = serverSocket.accept();
                logger.info("XConnector received connection:{}", client.getRemoteSocketAddress());
                for (SocketListener listener : socketListeners) {
                    listener.handleSocket(client);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket(String ip, int port) {
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
            logger.info("XConnector connected to {}.", socket.getRemoteSocketAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

    @Override
    public void addSocketListener(SocketListener listener) {
        synchronized (socketListeners) {
            socketListeners.add(listener);
        }
    }

    @Override
    public void removeSocketListener(SocketListener listener) {
        synchronized (socketListeners) {
            socketListeners.remove(listener);
        }
    }

    /*
     * defines operation code for xconnection
	 */
    public static class Type {
        /*
         *
         */
        public static final byte OP_READ_BLOCK = 0;
        /*
         *
         */
        public static final byte OP_WRITE_BLOCK = 1;
        /*
         * load balance
         */
        public static final byte OP_APPEND_BLOCK = 2;

        public static final byte OP_FINISH_SUC = 3;

        public static final byte OP_FINISH_FAIL = 4;
    }
}

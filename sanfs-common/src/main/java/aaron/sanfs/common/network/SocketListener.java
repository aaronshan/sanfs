package aaron.sanfs.common.network;

import java.net.Socket;

/**
 * a client or storage server will create an instance of XConnector to connect with
 * other entity and to listen on a port and accept connections.
 * </p>
 * when a connection is accepted, the socket will be feed to every SocketListener
 *
 * @author gengyufeng
 * @see XConnector
 */
public interface SocketListener {
    public void handleSocket(Socket s);
}


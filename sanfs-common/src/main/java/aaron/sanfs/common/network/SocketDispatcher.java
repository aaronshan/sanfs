package aaron.sanfs.common.network;

/**
 * interface that defines how Xconnector deal with sockets it created
 *
 * @author gengyufeng
 */
public interface SocketDispatcher {

    public void addSocketListener(SocketListener listener);

    public void removeSocketListener(SocketListener listener);
}

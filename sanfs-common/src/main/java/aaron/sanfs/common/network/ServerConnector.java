package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.CallDispatcher;
import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.util.Configuration;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * create an object and pass callback functions, then call start();
 *
 * @author: aaronshan
 */
public class ServerConnector implements CallDispatcher, Connector {
    private volatile static ServerConnector instance = null;

    private int port;
    private Configuration configuration;
    /**
     * Calls from client
     */
    private BlockingDeque<Call> callQueue;
    /**
     * responses to client
     */
    private BlockingDeque<Call> responseQueue;

    private Map<String, SocketChannel> channelMap;

    private List<CallListener> callListeners = new ArrayList<CallListener>();

    private ServerConnector() {
        configuration = Configuration.getInstance();
        port = configuration.getInteger("nameserver_port");
        callQueue = new LinkedBlockingDeque<Call>();
        responseQueue = new LinkedBlockingDeque<Call>();
        channelMap = new HashMap<String, SocketChannel>();
    }

    public static ServerConnector getInstance() {
        if (null == instance) {
            synchronized (ServerConnector.class) {
                instance = new ServerConnector();
                instance.start();
            }
        }
        return instance;
    }

    public void start() {
        ServerListener serverListener = new ServerListener(this, port);
        serverListener.start();
        ServerResponser serverResponser = new ServerResponser(this);
        serverResponser.start();
    }

    public void putCallQueue(Call call) {
        /**
         *  @TODO use callQueue and multithread Handler, or call
         *  listeners here?
         */
        for (CallListener listener : callListeners) {
            listener.handleCall(call);
        }
    }

    @Override
    public synchronized void addListener(CallListener listener) {
        callListeners.add(listener);
    }

    @Override
    public synchronized void removeListener(CallListener listener) {
        callListeners.remove(listener);
    }

    @Override
    /**
     * when completed with a client call, put the response here
     * @param response
     */
    public void sendCall(Call command) {
        try {
            responseQueue.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Call getResponse() {
        Call ret = null;
        try {
            ret = responseQueue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public synchronized void setAddressChannel(String address, SocketChannel channel) {
        channelMap.put(address, channel);
    }

    public synchronized SocketChannel getChannel(String address) {
        assert (channelMap.containsKey(address));
        return channelMap.get(address);
    }
}

package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.CallDispatcher;
import aaron.sanfs.common.call.CallListener;
import aaron.sanfs.common.call.all.AbortCall;
import aaron.sanfs.common.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * every client need an instance of ClientConnector to Name Server
 * </b>
 * ClientConnector sends calls to name server, and collects responses. calls and responses
 * are defined in all
 * </b>
 * use sendCall(Call) to send a Call
 * </b>
 * implement CallListener and add listener to ClientConnector to deal with responses
 *
 * @author geng yufeng
 */
public class ClientConnector implements Connector, CallDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnector.class);

    private volatile static ClientConnector instance;

    /**
     * socket created to connect with name server.
     */
    private Socket socket = null;

    /**
     * name server ip/port
     */
    private String remoteIP;
    private int remotePort;
    private Configuration configuration;

    /**
     * list of command to be sent to name server
     */
    private BlockingDeque<Call> commands;

    /**
     * list of responses received from name server
     */
    private BlockingDeque<Call> responses;

    /**
     * listeners for response add to clientConnector
     */
    private List<CallListener> responseListeners = new ArrayList<CallListener>();


    private ClientConnector() {
        configuration = Configuration.getInstance();
        remoteIP = configuration.getString("nameserver_ip");
        remotePort = configuration.getInteger("nameserver_port");
        commands = new LinkedBlockingDeque<Call>();
        responses = new LinkedBlockingDeque<Call>();
    }

    public static ClientConnector getInstance() {
        if (null == instance) {
            synchronized (ClientConnector.class) {
                instance = new ClientConnector();
                instance.setupSocket();
            }
        }

        return instance;
    }

    /**
     * setup connection with server, and create two threads to send
     * and receive respectively
     */
    public void setupSocket() {
        SocketAddress address = new InetSocketAddress(remoteIP, remotePort);
        while (true) {
            try {
                socket = new Socket();
                socket.connect(address, 1000);
            } catch (SocketTimeoutException e) {
                logger.info("Connection NameServer({}:{}) timeout, will reconnect in 5 seconds...", remoteIP, remotePort);
                try {
                    Thread.sleep(5000);
                    continue;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
            break;
        }
        logger.info("Connection established with:{}.", socket.getRemoteSocketAddress());
        try {
            ClientSender clientSender = new ClientSender(this, socket.getOutputStream());
            clientSender.start();
            ClientReceiver clientReceiver = new ClientReceiver(this, socket.getInputStream());
            clientReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public synchronized void addListener(CallListener listener) {
        responseListeners.add(listener);
    }

    @Override
    public synchronized void removeListener(CallListener listener) {
        responseListeners.remove(listener);
    }

    @Override
    /**
     * call this method to send a command to name server.
     */
    public void sendCall(Call command) {
        commands.push(command);
    }

    public Call getCommandCall() {
        Call retCall = null;
        try {
            retCall = commands.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return retCall;
    }

    /**
     * receive response from server
     *
     * @param responseCall
     */
    public void addResponseCall(Call responseCall) {
        try {
            responses.put(responseCall);
            if (responseCall.getType() == Call.Type.ABORT) {
                logger.error(((AbortCall) responseCall).getReason());
            }
            for (CallListener listener : responseListeners) {
                listener.handleCall(responseCall);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * return local address of socket used.
     * format: "/X.X.X.X:X"
     *
     * @return
     */
    public String getLocalAddress() {
        return socket.getLocalSocketAddress().toString();
    }
}

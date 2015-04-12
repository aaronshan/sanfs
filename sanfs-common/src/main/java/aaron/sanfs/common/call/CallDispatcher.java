package aaron.sanfs.common.call;

/**
 * ClientConnector and ServerConnector should implement this interface <p>
 * when a call is received by bottom level of network module, an event is fired
 * and every CallListener registered will be awaken
 *
 * @author: aaronshan
 */
public interface CallDispatcher {
    /**
     * add a listener that handle a potential Call event <p>
     * a new call will be feed to every listener.
     *
     * @param listener
     */
    public void addListener(CallListener listener);

    /**
     * remove a listener, stop listening on call event.
     *
     * @param listener
     */
    public void removeListener(CallListener listener);
}

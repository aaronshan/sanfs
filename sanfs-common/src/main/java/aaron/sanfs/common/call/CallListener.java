package aaron.sanfs.common.call;

/**
 * register on CallDispatcher, when a call is fired,
 * the listener will receive it so to handle it.
 *
 * @author: aaronshan
 */
public interface CallListener {
    public void handleCall(Call call);
}

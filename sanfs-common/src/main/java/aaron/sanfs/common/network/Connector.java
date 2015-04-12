package aaron.sanfs.common.network;

import aaron.sanfs.common.call.Call;

/**
 * @author: aaronshan
 */
public interface Connector {
    public void sendCall(Call command);
}

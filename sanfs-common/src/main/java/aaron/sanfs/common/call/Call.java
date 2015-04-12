package aaron.sanfs.common.call;

import java.io.Serializable;

/**
 * Base class of all kinds of other call used in the RPC system.
 * <p>
 * derived calls contains call sent name server to client(n2c), c2n,
 * name server to storage server(n2s), s2n <p>
 * every derived call links with a task, like AddFileCall, GetFileCall, etc.
 *
 * @author: aaronshan
 */
public class Call implements Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = -1157466700148810064L;

    /**
     * Type of call, defined below as enum Type.
     */
    private final Type type;

    /**
     * name server use this attribute to distinguish different clients,
     * it's derived by call the socket's getRemoteSocketAddress.
     */
    private String initiator;

    /**
     * Client, name server and storage server both have id for their tasks
     * respective, when a Call is passed between two entity, they assign fromTaskId as their own taskId.<p>
     * if call is initiated, toTaskId will be remained -1, if Call is a response, it will be the fromTaskId of the call it response to.
     */
    private long fromTaskId = -1;

    /**
     * Indicates the related task of remote peer.
     */
    private long toTaskId = -1;

    /**
     * Construction method.
     *
     * @param type
     */
    public Call(Type type) {
        this.type = type;
    }

    /**
     * Get Call type.
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Get "from task" id.
     *
     * @return
     */
    public long getFromTaskId() {
        return fromTaskId;
    }

    /**
     * Set "from task" id.
     *
     * @param fromTaskId
     */
    public void setFromTaskId(long fromTaskId) {
        this.fromTaskId = fromTaskId;
    }

    /**
     * Get "to task" id.
     *
     * @return
     */
    public long getToTaskId() {
        return toTaskId;
    }

    /**
     * Set "to task" id.
     *
     * @param toTaskId
     */
    public void setToTaskId(long toTaskId) {
        this.toTaskId = toTaskId;
    }

    /**
     * Get initiator's address.
     *
     * @return
     */
    public String getInitiator() {
        return initiator;
    }

    /**
     * Set initiator's address.
     *
     * @param initiator
     */
    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public static enum Type {
        /**
         * It's the default value when you create a Call
         */
        INVALID("INVALID"),

        /**
         * Abort the task
         */
        ABORT("ABORT"),

        /**
         * Heartbeat call from storage server to name server
         */
        HEARTBEAT_S2N("HEARTBEAT_S2N"),

        /**
         * Registration call from storage server to name server
         */
        REGISTRATION_S2N("REGISTRATION_S2N"),

        /**
         * Add file call from client to name server
         */
        ADD_FILE_C2N("ADD_FILE_C2N"),

        /**
         * Add file return call from name server to client
         */
        ADD_FILE_N2C("ADD_FILE_N2C"),

        /**
         * Add file from client to name server.
         */
        ADD_DIRECTORY_C2N("ADD_DIRECTORY_C2N"),

        /**
         * Get file from client to name server.
         */
        GET_FILE_C2N("GET_FILE_C2N"),

        /**
         * Get file return call from name server to client.
         */
        GET_FILE_N2C("GET_FILE_N2C"),

        /**
         * Get directory from client to name server.
         */
        GET_DIRECTORY_C2N("GET_DIRECTORY_C2N"),

        /**
         * Get directory return call from name server to client.
         */
        GET_DIRECTORY_N2C("GET_DIRECTORY_N2C"),

        /**
         * Append file from client to name server.
         */
        APPEND_FILE_C2N("APPEND_FILE_C2N"),

        /**
         * Append file return call from name server to client.
         */
        APPEND_FILE_N2C("APPEND_FILE_N2C"),

        /**
         * Remove file call from client to name server
         */
        REMOVE_FILE_C2N("REMOVE_FILE_C2N"),

        /**
         * Remove file return call from name server to client.
         */
        REMOVE_DIRECTORY_C2N("REMOVE_DIRECTORY_C2N"),

        /**
         * Move file call from client to name server
         */
        MOVE_FILE_C2N("MOVE_FILE_C2N"),

        /**
         * Move file call from client to name server.
         */
        MOVE_DIRECTORY_C2N("MOVE_DIRECTORY_C2N"),

        /**
         * Synchronize call from storage server to name server
         */
        SYNC_S2N("SYNC_S2N"),

        /**
         * Synchronize return call from name server to storage server.
         */
        SYNC_N2S("SYNC_N2S"),

        /**
         * Migrate data from one storage server to others when it's dead.
         */
        MIGRATE_FILE_N2S("MIGRATE_FILE_N2S"),

        /**
         * Notify task finish.
         */
        FINISH("FINISH"),

        /**
         * Renew lease call.
         */
        LEASE_C2N("LEASE_C2N");

        /**
         * Human-readable name.
         */
        private String name;

        /**
         * Construction method.
         *
         * @param name
         */
        private Type(String name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return name;
        }
    }
}

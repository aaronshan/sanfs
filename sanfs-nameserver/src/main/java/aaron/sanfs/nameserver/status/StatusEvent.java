package aaron.sanfs.nameserver.status;

/**
 * Status event. Indicate which kinds of change has happened to storage status.
 *
 * @author: aaronshan
 */
public class StatusEvent {
    /**
     * Event type.
     */
    private Type type;

    /**
     * which storage changes.
     */
    private Storage storage;

    /**
     * Construction method.
     *
     * @param type
     * @param storage
     */
    public StatusEvent(Type type, Storage storage) {
        this.type = type;
        this.storage = storage;
    }


    /**
     * Get <tt>StatusEvent</tt> type.
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Get changed storage.
     *
     * @return
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Indicate the type of <tt>StatusEvent</tt>.
     */
    public static enum Type {
        /**
         * A new storage server has registered.
         */
        STORAGE_REGISTERED("STORAGE_REGISTERED"),

        /**
         * A storage server has dead.
         */
        STORAGE_DEAD("STORAGE_DEAD"),

        /**
         * A storage server's load has changed.
         */
        LOAD_CHANGED("LOAD_CHANGED"),

        /**
         * A storage server's sum of running task has changed.
         */
        TASK_SUM_CHANGED("TASK_SUM_CHANGED"),

        /**
         * A storage server's heartbeat has come.
         */
        HEARTBEAT("HEARTBEAT");

        /**
         * Human-readable description of <tt>Type</tt>.
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

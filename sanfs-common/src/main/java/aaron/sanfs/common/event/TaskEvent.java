package aaron.sanfs.common.event;

import aaron.sanfs.common.task.Task;

/**
 * It represent some important thing has happened.
 *
 * @author: aaronshan
 */
public class TaskEvent {
    /**
     * Event type.
     */
    private Type type;

    /**
     * Indicate which task fired this event.
     */
    private Task thread;

    /**
     * Construction method.
     *
     * @param type
     * @param thread
     */
    public TaskEvent(Type type, Task thread) {
        this.type = type;
        this.thread = thread;
    }

    /**
     * Get event type.
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Get the event source.
     *
     * @return
     */
    public Task getTaskThread() {
        return thread;
    }

    public static enum Type {
        /**
         * Task has done.
         */
        TASK_FINISHED("TASK_FINISHED"),

        /**
         * Task lease is due.
         */
        TASK_DUE("TASK_DUE"),

        /**
         * No heartbeat for long time.
         */
        HEARTBEAT_FATAL("HEARTBEAT_FATAL"),

        /**
         * Task is invalid.
         */
        INVALID("INVALID"),

        /**
         * Heartbeat response.
         */
        HEARTBEAT_RESPONSE("HEARTBEAT_RESPONSE"),

        /**
         * Registration finished.
         */
        REG_FINISHED("REG_FINISHED"),

        /**
         * Files migration has finished.
         */
        MIGRATE_FINISHED("MIGRATE_FINISHED"),

        /**
         * Start to add duplicate file.
         */
        ADDFILE_DUPLICATE("ADDFILE_DUPLICATE"),

        /**
         * Duplicated files have been added successfully.
         */
        DUPLICATE_FINISHED("DUPLICATE_FINISHED");

        /**
         * Human-readable string.
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

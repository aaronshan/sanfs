package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.n2s.MigrateFileCallN2S;
import aaron.sanfs.common.call.s2n.HeartbeatCallS2N;
import aaron.sanfs.common.call.s2n.RegistrationCallS2N;
import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.nameserver.meta.File;
import aaron.sanfs.nameserver.status.Status;
import aaron.sanfs.nameserver.status.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Task of handling heatbeat.
 * <p/>
 * Heartbeat task also handles data migration.
 * <p/>
 * Everytime we get a heartbeat call, we send back a migration call.
 *
 * @author lishunyang
 * @see NameServerTask
 */
public class HeartbeatTask extends NameServerTask {
    /**
     * Logger.
     */
    private final static Logger logger = LoggerFactory.getLogger(HeartbeatTask.class);

    /**
     * The storage that we focus on.
     */
    private Storage storage;

    /**
     * Heartbeat check period.(second)
     */
    private final long period;

    private String address;

    /**
     * Construction method.
     *
     * @param tid
     * @param call
     * @param connector
     * @param period
     */
    public HeartbeatTask(long tid, Call call, Connector connector, long period) {
        super(tid, call, connector);
        RegistrationCallS2N c = (RegistrationCallS2N) call;
        this.address = c.getAddress();
        this.period = period;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        final Status status = Status.getInstance();

        synchronized (status) {
            this.storage = new Storage(address);
            Status.getInstance().addStorage(storage);
        }

        logger.info("New storage server registered.");
        // As for registration, send a finish call to notify storage server.
        sendFinishCall();

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(period);

                if (longTimeNoSee()) {
                    fireEvent(new TaskEvent(TaskEvent.Type.HEARTBEAT_FATAL,
                            this));
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCall(Call call) {
        logger.info("Heartbeat Task receive a call: " + call.getType());
        if (call.getToTaskId() != getTaskId())
            return;

        if (call.getType() == Call.Type.HEARTBEAT_S2N) {
            updateHeartbeatTimestamp();
            logger.info("Heartbeat update storage server " + storage.getId()
                    + " heartbeat timestamp: " + storage.getHearbeatTime());

            storage.setTaskSum(((HeartbeatCallS2N) call).getLoad());

            removeMigratedFilesFromMigrateList(((HeartbeatCallS2N) call)
                    .getMigratedFiles());

            sendMigrationCall();
            return;
        }
    }

    // FIXME: Why we need this method?

    /**
     * Get storage that we focus on.
     *
     * @return
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * Test whether the storage is dead.
     *
     * @return
     */
    private boolean longTimeNoSee() {
        final long currentTime = System.currentTimeMillis();
        if ((currentTime - storage.getHearbeatTime()) > (period * 2 * 1000)) {
            return true;
        }
        return false;
    }

    /**
     * Update storage heartbeat timestamp.
     */
    private void updateHeartbeatTimestamp() {
        storage.setHeartbeatTime(System.currentTimeMillis());
    }

    /**
     * Update migrate file list.
     * <p/>
     * When we get heatbeat call, it includes files of which storage server has
     * complete migration. So we just remove those files from un-migrate file
     * list.
     *
     * @param migratedFiles
     */
    private void removeMigratedFilesFromMigrateList(
            Map<String, List<String>> migratedFiles) {
        storage.removeMigrateFiles(migratedFiles);
    }

    /**
     * Send migration call back to storage server.
     */
    private void sendMigrationCall() {
        // As to heartbeaet call, name server always send the migration call
        // back to storage server. So, if storage server doesn't receive the
        // migration call, it will realize he is dead and should register
        // again.
        Map<Storage, List<File>> migrateFiles = storage.getMigrateFiles();
        Map<String, List<String>> rawMigrateFiles =
                new HashMap<String, List<String>>();

        for (Map.Entry<Storage, List<File>> e : migrateFiles.entrySet()) {
            List<String> fileList = new ArrayList<String>();

            for (File f : e.getValue()) {
                fileList.add(f.getId());
            }
            rawMigrateFiles.put(e.getKey().getId(), fileList);
        }

        Call back = new MigrateFileCallN2S(rawMigrateFiles);
        sendCall(back);
    }
}
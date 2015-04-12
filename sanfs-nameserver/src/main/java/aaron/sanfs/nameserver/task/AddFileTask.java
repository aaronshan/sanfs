package aaron.sanfs.nameserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.c2n.AddFileCallC2N;
import aaron.sanfs.common.call.n2c.AddFileCallN2C;
import aaron.sanfs.common.network.Connector;
import aaron.sanfs.common.util.IdGenerator;
import aaron.sanfs.nameserver.meta.Directory;
import aaron.sanfs.nameserver.meta.File;
import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.status.Status;
import aaron.sanfs.nameserver.status.Storage;
import aaron.sanfs.nameserver.util.BackupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Task of adding file.
 *
 * @author: ruifengshan
 * @see aaron.sanfs.nameserver.task.NameServerTask
 */
public class AddFileTask extends NameServerTask {

    private final static Logger logger = LoggerFactory.getLogger(AddFileTask.class);

    /**
     * Duplicate number of file.
     */
    private int duplicate;

    /**
     * File directory name.
     */
    private String dirName;

    /**
     * File name.
     */
    private String fileName;

    /**
     * Sync object which is used for synchronizing.
     */
    private Object syncRoot = new Object();

    /**
     * Indicates whether the directory is already existed before adding this
     * file. If the directory is already existed, we should not delete it when
     * task aborts, but if it isn't, we can consider this.
     */
    private boolean hasDir = false;

    /**
     * The file that we focus on.
     */
    private File file = null;

    /**
     * Construction method.
     *
     * @param taskId
     * @param call
     * @param connector
     * @param duplicate
     */
    public AddFileTask(long taskId, Call call, Connector connector, int duplicate) {
        super(taskId, call, connector);
        AddFileCallC2N callC2N = (AddFileCallC2N) call;
        this.dirName = callC2N.getDirName();
        this.fileName = callC2N.getFileName();
        this.duplicate = duplicate;
    }

    @Override
    public void run() {
        final Meta meta = Meta.getInstance();
        final BackupUtil backupUtil = BackupUtil.getInstance();

        synchronized (meta) {
            if (fileExists()) {
                sendAbortCall("Task aborted, there has been a directory/file with the same name.");
                setFinish();
                return;
            } else {
                file = new File(fileName, IdGenerator.getInstance().getLongId());
                // This must be success, because we create this file.
                file.tryLockWrite(1, TimeUnit.SECONDS);

                logger.info("AddFileTask {} started.", getTaskId());
                backupUtil.writeLogIssue(getTaskId(), Call.Type.ADD_FILE_C2N, dirName + " " + fileName + " " + file.getId());

                // This should be a problem: if here comes an exception, then it will never release the lock.
                addFileToMeta();
                sendResponseCall();
            }
        }

        waitUntilTaskFinish();

        if (isDead()) {
            return;
        }

        synchronized (meta) {
            logger.info("AddFileTask {} commit.", getTaskId());
            backupUtil.writeLogCommit(getTaskId());
            commit();
            file.unlockWrite();

            sendFinishCall();
            setFinish();
        }
    }

    /**
     * Test whether the file that client wants to add has existed.
     *
     * @return
     */
    private boolean fileExists() {
        if (Meta.getInstance().containDirectory(dirName)) {
            hasDir = true;
        } else {
            hasDir = false;
            return false;
        }

        if (Meta.getInstance().containFile(dirName, fileName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add file into meta structure.
     */
    private void addFileToMeta() {
        Meta.getInstance().addFile(dirName, file);

        List<Storage> storages = Status.getInstance().allocateStorage(duplicate);
        file.setLocations(storages);
        for (Storage storage : storages) {
            storage.addFile(file);
        }
    }

    /**
     * Remove file from meta structure.
     */
    private void removeFileFromMeta() {
        Meta.getInstance().removeFile(dirName, fileName);
        // The directory is invalid and doesn't exist before.
        if (!hasDir && !Meta.getInstance().isDrectoryValid(dirName)) {
            Meta.getInstance().removeDirectory(dirName);
        }

        if (null == file) {
            return;
        }

        for (Storage storage : file.getLocations()) {
            storage.removeFile(file);
        }
    }

    /**
     * wait until task has finished.
     */
    private void waitUntilTaskFinish() {
        try {
            synchronized (syncRoot) {
                syncRoot.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * send response call back to client.
     */
    private void sendResponseCall() {
        List<Storage> storages = file.getLocations();
        List<String> locations = new ArrayList<String>();

        for (Storage storage : storages) {
            locations.add(storage.getId());
        }

        String fileId = file.getId();
        Call back = new AddFileCallN2C(fileId, locations);
        sendCall(back);
    }

    /**
     * commit task.
     * <p/>
     * once a task is committed, all works it has done won't be lost.
     */
    private void commit() {
        Directory directory = Meta.getInstance().getDirectory(dirName);

        if (null == directory) {
            return;
        }
        Meta.getInstance().setFileValid(dirName, fileName, true);
    }

    @Override
    public void release() {
        setDead();

        synchronized (syncRoot) {
            syncRoot.notify();
        }

        synchronized (Meta.getInstance()) {
            removeFileFromMeta();
        }
    }

    @Override
    public void handleCall(Call call) {
        if (call.getToTaskId() != getTaskId()) {
            return;
        }

        if (call.getType() == Call.Type.LEASE_C2N) {
            renewLease();
            return;
        }

        if (call.getType() == Call.Type.FINISH) {
            synchronized (syncRoot) {
                syncRoot.notify();
            }
            return;
        }
    }
}

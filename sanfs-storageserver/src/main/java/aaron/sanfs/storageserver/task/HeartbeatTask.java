package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.n2s.MigrateFileCallN2S;
import aaron.sanfs.common.call.s2n.HeartbeatCallS2N;
import aaron.sanfs.common.task.TaskMonitor;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.storageserver.event.HeartbeatResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author dengshihong
 */
public class HeartbeatTask extends StorageServerTask {
    /**
     *
     */
    private final static Logger logger = LoggerFactory.getLogger(HeartbeatTask.class);
    /**
     *
     */
    private final long NStid;
    /**
     *
     */
    private final TaskMonitor taskMonitor;
    /**
     *
     */
    private Map<String, List<String>> overMigrateFile;
    /**
     *
     */
    private Map<String, List<String>> onMigrateFile;
    /**
     *
     */
    private Boolean alive = true;

    public HeartbeatTask(long tid, Map<String, List<String>> overMigrateFile,
                         Map<String, List<String>> onMigrateFile, long NStid,
                         TaskMonitor taskMonitor) {
        super(tid);
        this.overMigrateFile = overMigrateFile;
        this.onMigrateFile = onMigrateFile;
        this.NStid = NStid;
        this.taskMonitor = taskMonitor;
    }

    @Override
    public void handleCall(Call call) {
        if (call.getType() == Call.Type.MIGRATE_FILE_N2S) {
            MigrateFileCallN2S mycall = (MigrateFileCallN2S) call;
            Map<String, List<String>> recieve = mycall.getFiles();
            Map<String, List<String>> working = new HashMap<String, List<String>>();
            if (null == recieve || recieve.isEmpty())
                return;
            synchronized (overMigrateFile) {
                synchronized (onMigrateFile) {
                    for (String key : recieve.keySet()) {
                        working.put(key, new ArrayList<String>());
                        for (String filename : recieve.get(key)) {
                            if (null != overMigrateFile.get(key)
                                    && overMigrateFile.get(key).contains(
                                    filename)) {
                                // 迁移任务已经在完成队列
                            } else if (null != onMigrateFile.get(key)
                                    && onMigrateFile.get(key)
                                    .contains(filename)) {
                                // 迁移任务已经在进行队列
                            } else {
                                working.get(key).add(filename);
                                if (null == onMigrateFile.get(key))
                                    onMigrateFile.put(key,
                                            new ArrayList<String>());
                                onMigrateFile.get(key).add(filename);
                            }
                        }
                    }
                }
            }
            fireEvent(new HeartbeatResponseEvent(this, working));
        } else if (call.getType() == Call.Type.ABORT) {
            // TODO 可能需要加锁
            alive = false;
        }

    }

    @Override
    public void run() {
        Map<String, List<String>> migratefile = new HashMap<String, List<String>>();
        while (alive) {
            synchronized (overMigrateFile) {
                migratefile.clear();
                migratefile.putAll(overMigrateFile);
                overMigrateFile.clear();
            }
            HeartbeatCallS2N call = new HeartbeatCallS2N(migratefile,
                    taskMonitor.getTaskSum());
            call.setToTaskId(NStid);
            logger.info("---------->" + NStid);
            call.setFromTaskId(getTaskId());
            connector.sendCall(call);
            try {
                TimeUnit.SECONDS.sleep(Configuration.getInstance().getInteger(
                        Configuration.SS_HB_INTERVAL));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        setFinish();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}

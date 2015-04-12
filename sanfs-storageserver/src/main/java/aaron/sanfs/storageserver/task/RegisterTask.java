package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.s2n.RegistrationCallS2N;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.storageserver.event.BeforeRegFinishEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author dengshihong
 */
public class RegisterTask extends StorageServerTask {
    private final static Logger logger = LoggerFactory.getLogger(RegisterTask.class);
    private String address;
    private Boolean finished = false;

    public RegisterTask(long tid, String address) {
        super(tid);
        this.address = address;
    }

    @Override
    public void handleCall(Call call) {
        if (call.getType() == Call.Type.FINISH) {
            synchronized (finished) {
                if (false == finished) {
                    finished = true;
                    logger.info("StorageServer" + address
                            + " finish registeration.");
                    logger.info("------------------->" + call.getFromTaskId());
                    fireEvent(new BeforeRegFinishEvent(this,
                            call.getFromTaskId()));
                }
            }
        }
    }

    @Override
    public void run() {
        boolean isNSanswer = false;
        while (isNSanswer == false) {
            synchronized (finished) {
                if (false == finished) {
                    RegistrationCallS2N call = new RegistrationCallS2N(address);
                    call.setFromTaskId(getTaskId());
                    connector.sendCall(call);
                    logger.info("StorageServer" + address
                            + " send a registerationCall.");
                } else
                    isNSanswer = true;
            }
            try {
                TimeUnit.SECONDS.sleep(Configuration.getInstance().getInteger(
                        Configuration.SS_REG_INTERVAL));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
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

package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.all.AbortCall;
import aaron.sanfs.common.call.all.FinishCall;
import aaron.sanfs.common.call.c2n.AddFileCallC2N;
import aaron.sanfs.common.call.n2c.AddFileCallN2C;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Send ADD_FILE_CALL to NS, wait for response of SS locations
 * setup socket with SS and send data
 * Sent FINISH to NS when transmission finished
 *
 * @author: aaronshan
 */
public class CAddFileTask extends Task {

    private final static Logger logger = LoggerFactory.getLogger(CAddFileTask.class);

    private Socket storageSocket;

    /**
     * stream to Read/Write file with Storage Server.
     */
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    /**
     * wait for the name server to return the call
     */
    private AddFileCallN2C callN2C;

    /**
     * wait on this object for name server to response.
     */
    private Object waitor = new Object();

    /**
     * direction of file uploaded on name server
     */
    private String filePath;

    /**
     * file uploaded can have different name with local file
     */
    private String fileName;

    /**
     * handle of the local file to be uploaded.
     */
    private File file;

    /**
     * toTaskId and type of response call(N2C)
     */
    private long toTaskId;
    private Call.Type type;

    /**
     * create addFileTask, should call new Thread(task).start
     *
     * @param taskId
     * @param filePath
     * @param fileName
     * @param file
     */
    public CAddFileTask(long taskId, String filePath, String fileName, File file) {
        super(taskId);
        this.filePath = filePath;
        this.fileName = fileName;
        this.file = file;
    }

    @Override
    public void handleCall(Call call) {
        if (call.getToTaskId() != getTaskId()) {
            return;
        }

        if (call.getType() == Call.Type.ADD_FILE_N2C) {
            this.callN2C = (AddFileCallN2C) call;
            this.toTaskId = call.getFromTaskId();
            type = call.getType();
            synchronized (waitor) {
                waitor.notify();
            }
        }

        if (call.getType() == Call.Type.ABORT) {
            logger.error(((AbortCall) call).getReason());
            type = call.getType();
        } else {
            logger.error("Fatal error: call type can not match. callType is {}.", call.getType());
        }
    }

    @Override
    public void run() {
        AddFileCallC2N callC2N = new AddFileCallC2N(filePath, fileName);
        callC2N.setFromTaskId(getTaskId());
        ClientConnector.getInstance().sendCall(callC2N);
        ClientConnector.getInstance().addListener(this);
        try {
            synchronized (waitor) {
                waitor.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CLeaseTask leaseTask = new CLeaseTask(getTaskId(), toTaskId);
        leaseTask.start();
        if (type == Call.Type.ABORT) {
            return;
        }

        if (callN2C.getLocations().size() == 0) {
            logger.error("Fatal error! No storage server returned.");
            setFinish();
            return;
        }

        String location = callN2C.getLocations().get(0);
        String[] locationStrings = location.split(":");
        storageSocket = XConnector.getSocket(locationStrings[0], Integer.parseInt(locationStrings[1]));
        byte status = XConnector.Type.OP_FINISH_FAIL;
        try {
            long fileLength = file.length();
            FileInputStream fileInputStream = new FileInputStream(file);

            outputStream = new DataOutputStream(storageSocket.getOutputStream());
            inputStream = new DataInputStream(storageSocket.getInputStream());
            outputStream.writeByte(XConnector.Type.OP_WRITE_BLOCK);
            outputStream.writeInt(callN2C.getLocations().size() - 1);
            for (int i = 1; i < callN2C.getLocations().size(); i++) {
                outputStream.writeUTF(callN2C.getLocations().get(i));
            }
            outputStream.writeUTF(callN2C.getFileId());
            outputStream.writeLong(fileLength);

            byte[] sendBytes = new byte[1024];
            int length, sumLength = 0;
            while ((length = fileInputStream.read(sendBytes, 0, sendBytes.length)) > 0) {
                sumLength += length;
                logger.info("Data transfer: {}%", (((double) sumLength / fileLength) * 100));
                outputStream.write(sendBytes, 0, length);
                outputStream.flush();
            }

            status = inputStream.readByte();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (status == XConnector.Type.OP_FINISH_FAIL) {
            logger.error("CAddFileTask upload failed!");
            leaseTask.interrupt();
            return;
        }

        FinishCall finishCall = new FinishCall();
        finishCall.setToTaskId(toTaskId);
        finishCall.setFromTaskId(getTaskId());
        ClientConnector.getInstance().sendCall(finishCall);
        setFinish();
        leaseTask.interrupt();
    }

    @Override
    public void release() {
    }
}

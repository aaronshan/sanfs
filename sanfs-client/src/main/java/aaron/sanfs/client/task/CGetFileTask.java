package aaron.sanfs.client.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.call.all.FinishCall;
import aaron.sanfs.common.call.c2n.GetFileCallC2N;
import aaron.sanfs.common.call.n2c.GetFileCallN2C;
import aaron.sanfs.common.network.ClientConnector;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.common.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * target to download a file
 *
 * @author gengyufeng
 */
public class CGetFileTask extends Task {

    private static final Logger logger = LoggerFactory.getLogger(CGetFileTask.class);

    /**
     * socket used to connect with storage server(which is returned by NS)
     */
    private Socket storageSocket;
    /**
     * file/socket stream
     */
    private DataOutputStream out;
    private DataInputStream dis;
    private FileOutputStream fos;
    /**
     * local target file handler
     */
    private File file;

    //wait for the ns to return the call
    private GetFileCallN2C call;

    private Object waitor = new Object();
    /**
     * path and name of the file to be downloaded
     */
    private String filepath;
    private String filename;

    private long toTaskId;

    /**
     * ASync task to download a file
     *
     * @param tid   globally unique task id
     * @param _path path of file to be downloaded
     * @param _name name of file to be downloaded
     * @param file  local target file handler
     */
    public CGetFileTask(long tid, String _path, String _name, File file) {
        super(tid);
        filepath = _path;
        filename = _name;
        this.file = file;
    }

    @Override
    public void handleCall(Call call) {
        if (call.getToTaskId() != getTaskId()) {
            return;
        }
        if (call.getType() == Call.Type.GET_FILE_N2C) {
            this.call = (GetFileCallN2C) call;
            this.toTaskId = call.getFromTaskId();
            synchronized (waitor) {
                waitor.notify();
            }
        } else {
            logger.info("Fatal error: call type dismatch.");
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        GetFileCallC2N callC2N = new GetFileCallC2N(filepath, filename);
        callC2N.setFromTaskId(getTaskId());
        ClientConnector.getInstance().sendCall(callC2N);
        ClientConnector.getInstance().addListener(this);

        try {
            synchronized (waitor) {
                waitor.wait();
            }
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        CLeaseTask leaseTask = new CLeaseTask(getTaskId(), toTaskId);
        leaseTask.start();

        if (call.getLocations().size() == 0) {
            logger.error("Fatal error! No storage server returned");
            setFinish();
            return;
        }

        String location = call.getLocations().get(0);
        String[] locationStrings = location.split(":");
        storageSocket = XConnector.getSocket(locationStrings[0], Integer.parseInt(locationStrings[1]));
        byte status = XConnector.Type.OP_FINISH_FAIL;

        try {
            out = new DataOutputStream(storageSocket.getOutputStream());
            dis = new DataInputStream(storageSocket.getInputStream());
            fos = new FileOutputStream(file);

            out.writeByte(XConnector.Type.OP_READ_BLOCK);
            out.writeUTF(call.getFileId());
            long length = dis.readLong();
            long totalLen = length;
            byte[] inputByte = new byte[1024];
            int toreadlen = (length < inputByte.length) ? (int) length : inputByte.length;
            int readlen;
            while (length > 0 && (readlen = dis.read(inputByte, 0, toreadlen)) > 0) {
                fos.write(inputByte, 0, readlen);
                fos.flush();
                length -= readlen;
                logger.info("Data transfered：" + (((double) (totalLen - length) / totalLen) * 100) + "%");
                toreadlen = (length < inputByte.length) ? (int) length : inputByte.length;
            }
            out.writeByte(XConnector.Type.OP_FINISH_SUC);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                status = XConnector.Type.OP_FINISH_SUC;
                if (null != out) {
                    out.close();
                }
                if (null != dis)
                    dis.close();
                if (null != fos)
                    fos.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (status == XConnector.Type.OP_FINISH_FAIL) {
            logger.error("CGetFileTask Download failed");
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
        // TODO Auto-generated method stub

    }

}


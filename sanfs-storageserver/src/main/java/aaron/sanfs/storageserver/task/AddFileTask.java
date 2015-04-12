package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.storageserver.Storage;
import aaron.sanfs.storageserver.event.AddFileDuplicateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dengshihong
 */
public class AddFileTask extends StorageServerTask {
    private final static Logger logger = LoggerFactory.getLogger(AddFileTask.class);
    private final Socket socket;
    private final Storage storage;
    private final DataInputStream dis;

    private Object waitor = new Object();
    private int waitNum = 0;

    public AddFileTask(long tid, Socket socket, DataInputStream dis,
                       Storage storage) {
        super(tid);
        this.socket = socket;
        this.dis = dis;
        this.storage = storage;
    }

    @Override
    public void handleCall(Call call) {
        // TODO Auto-generated method stub
        synchronized (waitor) {
            waitNum--;
            waitor.notify();
        }
    }

    @Override
    public void run() {
        String filename;
        long length;
        int todo;
        List<String> todoAddress = new ArrayList<String>();
        int toreadlen;
        int readlen;
        byte[] inputByte = null;
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            try {
                todo = dis.readInt();
                if (todo != 0) {
                    for (int i = 0; i < todo; i++)
                        todoAddress.add(dis.readUTF());
                }
                filename = dis.readUTF();
                length = dis.readLong();
                inputByte = new byte[1024];
                fos = new FileOutputStream(storage.getTransFile(filename));
                dos = new DataOutputStream(socket.getOutputStream());
                toreadlen = (length < inputByte.length) ? (int) length
                        : inputByte.length;
                while (length > 0
                        && (readlen = dis.read(inputByte, 0, toreadlen)) > 0) {
                    fos.write(inputByte, 0, readlen);
                    fos.flush();
                    length -= readlen;
                    toreadlen = (length < inputByte.length) ? (int) length
                            : inputByte.length;
                }
                if (length < 0)
                    throw (new Exception("AddFileTask: filelength errors."));

                fos.close();
                logger.info("------------------------>need to duplicate" + todo);
                synchronized (waitor) {
                    waitNum = todo;
                    fireEvent(new AddFileDuplicateEvent(this, filename,
                            todoAddress));
                    while (waitNum > 0)
                        waitor.wait();
                }
                storage.transSuccess(filename);
                dos.writeByte(XConnector.Type.OP_FINISH_SUC);

            } catch (Exception e) {
                e.printStackTrace();
                logger.info("AddFileTask failed");
                if (dos != null)
                    dos.writeByte(XConnector.Type.OP_FINISH_FAIL);
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (dos != null)
                    dos.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFinish();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}


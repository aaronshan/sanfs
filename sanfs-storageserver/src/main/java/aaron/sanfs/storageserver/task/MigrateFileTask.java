package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.storageserver.Storage;
import aaron.sanfs.storageserver.event.MigrateFileFinishEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * @author dengshihong
 */
public class MigrateFileTask extends StorageServerTask {
    private final static Logger logger = LoggerFactory
            .getLogger(MigrateFileTask.class);
    private final Storage storage;
    private final String address;
    private List<String> filenames;

    public MigrateFileTask(long tid, String address, List<String> filenames,
                           Storage storage) {
        super(tid);
        this.address = address;
        this.filenames = filenames;
        this.storage = storage;
    }

    @Override
    public void handleCall(Call call) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        String[] string = address.split(":");
        long length;
        int readlen, toreadlen;
        byte[] inputByte = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        for (String filename : filenames) {
            try {
                Socket socket = XConnector.getSocket(string[0],
                        Integer.parseInt(string[1]));
                try {
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    fos = new FileOutputStream(storage.getTransFile(filename));
                    dos.writeByte(XConnector.Type.OP_READ_BLOCK);
                    dos.writeUTF(filename);
                    length = dis.readLong();
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
                        throw (new Exception(
                                "MigrateFileTask: filelength errors."));
                    fos.close();
                    storage.transSuccess(filename);
                    dos.writeByte(XConnector.Type.OP_FINISH_SUC);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("MigrateFileTask for " + filename + " failed");
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
        }
        fireEvent(new MigrateFileFinishEvent(this, address, filenames));
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}

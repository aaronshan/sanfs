package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.common.network.XConnector;
import aaron.sanfs.storageserver.Storage;
import aaron.sanfs.storageserver.event.DuplicateFinishEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * Duplicate file from the transDir of the Storage
 *
 * @author dengshihong
 */
public class DuplicateFileTask extends StorageServerTask {
    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory
            .getLogger(DuplicateFileTask.class);
    /**
     *
     */
    private final Storage storage;
    /**
     *
     */
    private final String address;
    /**
     *
     */
    private final String filename;
    /**
     *
     */
    private final long parent;

    public DuplicateFileTask(long tid, Storage storage, String address,
                             String filename, long parent) {
        super(tid);
        this.storage = storage;
        this.address = address;
        this.filename = filename;
        this.parent = parent;
    }

    @Override
    public void handleCall(Call call) {
        // TODO Auto-generated method stub
    }

    @Override
    public void run() {
        System.out.println("YYYYYYYYYYYYY " + address);
        String[] string = address.split(":");
        int readlen;
        byte status = XConnector.Type.OP_FINISH_FAIL;
        byte[] sendByte = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        FileInputStream fis = null;
        try {
            Socket socket = XConnector.getSocket(string[0],
                    Integer.parseInt(string[1]));
            try {
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                fis = new FileInputStream(
                        storage.getTransFileNotDelete(filename));
                dos.writeByte(XConnector.Type.OP_WRITE_BLOCK);
                dos.writeInt(0);
                dos.writeUTF(filename);
                dos.writeLong(storage.getTransFileNotDelete(filename).length());

                sendByte = new byte[1024];
                while ((readlen = fis.read(sendByte, 0, sendByte.length)) > 0) {
                    dos.write(sendByte, 0, readlen);
                    dos.flush();
                }
                // return finish status
                status = dis.readByte();
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("DuplicateFileTask failed");
            } finally {
                if (fis != null)
                    fis.close();
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
        fireEvent(new DuplicateFinishEvent(this, parent, status));
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}

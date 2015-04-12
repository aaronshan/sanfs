package aaron.sanfs.storageserver.task;

import aaron.sanfs.common.call.Call;
import aaron.sanfs.storageserver.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * @author dengshihong
 */
public class GetFileTask extends StorageServerTask {
    private final static Logger logger = LoggerFactory.getLogger(GetFileTask.class);
    private final Socket socket;
    private final Storage storage;
    private final DataInputStream dis;

    public GetFileTask(long tid, Socket socket, DataInputStream dis,
                       Storage storage) {
        super(tid);
        this.socket = socket;
        this.dis = dis;
        this.storage = storage;
    }

    @Override
    public void handleCall(Call call) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        String filename;
        File file;
        long length;
        int readlen;
        byte[] sendByte = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        try {
            try {
                filename = dis.readUTF();
                file = storage.getFile(filename);
                dos = new DataOutputStream(socket.getOutputStream());
                fis = new FileInputStream(file);

                length = file.length();
                dos.writeLong(length);

                sendByte = new byte[1024];
                while ((readlen = fis.read(sendByte, 0, sendByte.length)) > 0) {
                    dos.write(sendByte, 0, readlen);
                    dos.flush();
                }
                // client return finish status
                dis.readByte();
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("GetFileTask failed");
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
        setFinish();
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub

    }

}

package aaron.sanfs.common.util;

import java.io.*;

/**
 * @author: aaronshan
 */
public class ObjectUtil {

    /**
     * Convert object to bytes
     *
     * @param object
     * @return
     * @throws IOException
     */
    public static byte[] ObjectToByte(Object object) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
        objectOutputStream.writeObject(object);
        byte[] bytes = byteOutputStream.toByteArray();
        objectOutputStream.close();

        return bytes;
    }

    /**
     * Convert bytes to object
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object ByteToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteInputSteam = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteInputSteam);
        Object object = objectInputStream.readObject();
        objectInputStream.close();

        return object;
    }
}

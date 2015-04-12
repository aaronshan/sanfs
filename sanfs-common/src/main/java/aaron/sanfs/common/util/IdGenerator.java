package aaron.sanfs.common.util;

import java.util.Random;

/**
 * @author: ruifengshan
 */
public class IdGenerator {
    private Random random = new Random(System.currentTimeMillis());

    private static IdGenerator instance = new IdGenerator();

    private IdGenerator() {
    }

    public static IdGenerator getInstance() {
        return instance;
    }

    public synchronized Long getLongId() {
        return Math.abs(random.nextLong());
    }

    public synchronized Integer getIntegerId() {
        return Math.abs(random.nextInt());
    }
}

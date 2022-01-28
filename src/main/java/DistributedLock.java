import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DistributedLock {
    private final InterProcessMutex lock;
    private final String clientName;

    public DistributedLock(CuratorFramework client, String lockPath, String clientName) {
        this.clientName = clientName;
        this.lock = new InterProcessMutex(client, lockPath);
    }

    public void running(long time, TimeUnit unit) throws Exception {
        if (!lock.acquire(time, unit)) {
            System.out.println("the " + this.clientName + " could not recover the lock");
            return;
        }
        try {
            List<Integer> items = Database.findNext(Cache.lastProcessedId, 10);
            Thread.sleep(1000);
            System.out.println(this.clientName + " executing the task: " + items);
            if (items.isEmpty()) return;
            Cache.lastProcessedId = items.get(items.size() - 1);
        } finally {
            System.out.println(this.clientName + " releasing the lock");
            lock.release();
        }

    }
}

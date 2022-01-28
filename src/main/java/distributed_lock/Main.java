package distributed_lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String LOCK_PATH = "/system-a/lock";
    private static final int MAX_THREADS = 10;

    public static void main(String[] args) throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        final TestingServer server = new TestingServer();

        for (int i = 0; i < MAX_THREADS; i++) {
            executor.execute(() -> {
                CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 2));
                client.start();
                DistributedLock clientA = new DistributedLock(client, LOCK_PATH, "Client: "+ Thread.currentThread().getName());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            clientA.running(5, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                }, 1000, 2000);
            });
        }
        executor.shutdown();
    }
}

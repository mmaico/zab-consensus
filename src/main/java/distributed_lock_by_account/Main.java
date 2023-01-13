package distributed_lock_by_account;

import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int MAX_THREADS = 5;

    private static final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
            .handle(Exception.class)
            .withDelay(Duration.ofMillis(1000))
            .withMaxRetries(10);

    public static void main(String[] args) throws Exception {
        Long accountId = 2020l;
        final ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        final TestingServer server = new TestingServer();

        executor.execute(() -> {
            CuratorFramework client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 2));
            client.start();
            AccountReportService service = new AccountReportService(client, "Client: "+ Thread.currentThread().getName());
            try {
                service.getReport(accountId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Failsafe.with(retryPolicy).run(() -> {
            if (!Cache.accounts.containsKey(accountId)) {
                System.out.println("Retry");
                throw new RuntimeException("Retry");
            } else {
                System.out.println("the data was found");
            }
        });


        executor.shutdown();
    }

}

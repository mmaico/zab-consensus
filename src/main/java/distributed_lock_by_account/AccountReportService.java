package distributed_lock_by_account;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

public class AccountReportService {
    private final String clientName;
    private final CuratorFramework client;
    private final String PATH = "/lock/generate-report/for-account";

    public AccountReportService(CuratorFramework client, String clientName) {
        this.client = client;
        this.clientName = clientName;
    }

    public String getReport(Long accountId) throws Exception {
        if (Cache.accounts.containsKey(accountId)) {
            return Cache.accounts.get(accountId);
        }
        InterProcessMutex lock = new InterProcessMutex(client, PATH + "/" + accountId);
        if (lock.acquire(2000, TimeUnit.MILLISECONDS)) {
            try {
                System.out.println("the " + this.clientName + " new in process");
                Thread.sleep(5000);
                Cache.accounts.put(accountId, "The report is done!!!");
                return Cache.accounts.get(accountId);
            } finally {
                lock.release();
            }
        } else {
            throw new RuntimeException("Report yet in process");
        }
    }
}

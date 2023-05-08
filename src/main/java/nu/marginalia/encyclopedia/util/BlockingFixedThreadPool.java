package nu.marginalia.encyclopedia.util;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// It's been like a decade of people writing this exact class over and over
// and this is still not in the standard library...
public class BlockingFixedThreadPool {
    private final ThreadPoolExecutor executor;
    private final Semaphore sem;

    public BlockingFixedThreadPool(int n) {
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(n);
        this.sem = new Semaphore(n + n / 4);
    }

    public void execute(Runnable r) {
        sem.acquireUninterruptibly();
        executor.execute(() -> {
            try {
                r.run();
            } finally {
                sem.release();
            }
        });
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public void shutdown() {
        executor.shutdown();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
}

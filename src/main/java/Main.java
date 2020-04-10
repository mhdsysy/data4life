import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

// If you want more emails to be sent (Threads to be fired) try increasing CORE_POOL_SIZE.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        int[] input = IntStream.range(0, 1000000).toArray(); // Generate Array of emails

        int CAPACITY = 1000000; // Of Task Queue
        int CORE_POOL_SIZE = 1000;
        int MAXIMUM_POOL_SIZE = 100000;
        int KEEP_ALIVE_TIME = 0; // If pool has more than CORE_POOL_SIZE,
        // excess threads will be terminated if they have been idle for more than the KEEP_ALIVE_TIME.

        ArrayBlockingQueue<Runnable> arrayBlockingQueue = new ArrayBlockingQueue<Runnable>(CAPACITY);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, arrayBlockingQueue);

        MonitorThread monitor = new MonitorThread(executor);
        Thread monitorThread = new Thread(monitor);
        monitorThread.start();

        for (int email : input) {
            executor.execute(new SendMail());
        }
        executor.shutdown();
        monitor.shutDown();

    }

    // Monitors every 10 seconds
    static class MonitorThread implements Runnable {
        private ThreadPoolExecutor executor;
        private Boolean run = true;

        public MonitorThread(ThreadPoolExecutor executor) {
            super();
            this.executor = executor;
        }

        public void shutDown() {
            if (this.executor.getActiveCount() == 0)
                this.run = false;
        }

        @Override
        public void run() {
            while (run) {
                System.out.println(
                        String.format("[Space in Task Queue %d] [%d/%d] Active: %d, Completed: %d, Task: %d, isShutdown: %s, isTerminated: %s",
                                this.executor.getQueue().remainingCapacity(),
                                this.executor.getPoolSize(),
                                this.executor.getCorePoolSize(),
                                this.executor.getActiveCount(),
                                this.executor.getCompletedTaskCount(),
                                this.executor.getTaskCount(),
                                this.executor.isShutdown(),
                                this.executor.isTerminated()));
                try {
                    Thread.sleep(10 * 1000); // Change 10 to 60 if you want to monitor every minute.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

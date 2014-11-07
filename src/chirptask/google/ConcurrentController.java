//@author A0111840W
package chirptask.google;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentController {
    private final int DEFAULT_THREAD_POOL = 10;
    private final int WAIT_TIME = 10;
    
    private ExecutorService googleExecutor = null;
    
    ConcurrentController() {
        initComponents();
    }
    
    private void initComponents() {
        googleExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL);
    }
    
    void addToExecutor(Callable<Boolean> task) {
        if (task != null) {
            startExecutorIfNotRunning();
            googleExecutor.submit(task);
        }
    }
    
    private void startExecutorIfNotRunning() {
        if(googleExecutor.isShutdown()) {
            googleExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL);
        }
    }
    public void close() {
        googleExecutor.shutdown();
    }
    
    public boolean awaitTermination() throws InterruptedException {
        boolean isTerminated = 
                googleExecutor.awaitTermination(WAIT_TIME, TimeUnit.SECONDS);
        return isTerminated;
    }
}

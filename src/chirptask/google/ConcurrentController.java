package chirptask.google;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentController {
    private ExecutorService googleExecutor;
    
    ConcurrentController() {
        initComponents();
    }
    
    private void initComponents() {
        googleExecutor = Executors.newFixedThreadPool(10);
    }
    
    void addToExecutor(Callable<Boolean> task) {
        startExecutorIfNotRunning();
        googleExecutor.submit(task);
    }
    
    private void startExecutorIfNotRunning() {
        if(googleExecutor.isShutdown()) {
            googleExecutor = Executors.newFixedThreadPool(10);
        }
    }
    public void close() {
        googleExecutor.shutdown();
    }
    
    public boolean awaitTermination() throws InterruptedException {
        boolean isTerminated = googleExecutor.awaitTermination(10, TimeUnit.SECONDS);
        return isTerminated;
    }
}

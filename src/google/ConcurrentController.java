package chirptask.google;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.api.services.tasks.model.Task;

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
        Future<Boolean> futureTask = googleExecutor.submit(task);
    }
    
    private void startExecutorIfNotRunning() {
        if(googleExecutor.isShutdown()) {
            googleExecutor = Executors.newFixedThreadPool(10);
        }
    }
    public void close() {
        googleExecutor.shutdown();
    }
}

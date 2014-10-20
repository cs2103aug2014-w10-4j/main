package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class ConcurrentDelete implements Callable<Boolean> {

    private chirptask.storage.Task _taskToDelete;

    ConcurrentDelete(chirptask.storage.Task taskToDelete) {
        if (ConcurrentHandler.isNull(taskToDelete)) {
            _taskToDelete = null;
        } else {
            _taskToDelete = taskToDelete;
        }
    }

    public Boolean call() throws UnknownHostException, IOException {
        Boolean isDeleted = false;
        
        if (ConcurrentHandler.isNull(_taskToDelete)) {
            isDeleted = false;
            return isDeleted;
        }

        while (GoogleController.isGoogleLoaded() == false) {
            // wait until google is loaded in background
        }

        isDeleted = GoogleController.deleteTask(_taskToDelete);

        /*
         * Overwrites chirptask.storage.Task in the other storages
         */
        if (isDeleted) {
            _taskToDelete.setGoogleId(""); // Set Google ID to empty for deletion
            _taskToDelete.setDeleted(false); // Reset isDeleted flag to false
            ConcurrentHandler.modifyLocalStorage(_taskToDelete);
        }

        return isDeleted;
    }
}

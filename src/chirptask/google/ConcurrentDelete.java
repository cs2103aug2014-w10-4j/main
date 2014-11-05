package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class ConcurrentDelete implements Callable<Boolean> {

    private chirptask.storage.Task _taskToDelete;
    private static TasksController _tasksController;
    private static CalendarController _calendarController;

    ConcurrentDelete(chirptask.storage.Task taskToDelete, 
            TasksController tasksController, 
            CalendarController calController) {
        if (ConcurrentHandler.isNull(taskToDelete) ||
                ConcurrentHandler.isNull(tasksController) ||
                ConcurrentHandler.isNull(calController)) {
            _taskToDelete = null;
            _tasksController = null;
            _calendarController = null;
        } else {
            _taskToDelete = taskToDelete;
            _tasksController = tasksController;
            _calendarController = calController;
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

        isDeleted = deleteTask(_taskToDelete);

        /*
         * Overwrites chirptask.storage.Task in the other storages
         */
        if (isDeleted) {
            setDeleted(_taskToDelete);
            ConcurrentHandler.modifyLocalStorage(_taskToDelete);
        }

        return isDeleted;
    }
    
 // Called by ConcurrentDelete
    static boolean deleteTask(chirptask.storage.Task taskToDelete)
            throws UnknownHostException, IOException {
        boolean isDeleted = false;
        String googleId = taskToDelete.getGoogleId();
        String taskType = taskToDelete.getType();

        if (googleId == null || googleId == "") {
            isDeleted = false;
            return isDeleted;
        } else if (!GoogleController.isEntryExists(googleId, taskType)) {
            isDeleted = false;
            return isDeleted;
        }

        switch (taskType) {
        case "floating":
        case "deadline":
            isDeleted = deleteGoogleTask(googleId);
            break;
        case "timedtask":
            isDeleted = deleteGoogleEvent(googleId);
            break;
        default:
            break;
        }

        return isDeleted;
    }

    /**
     * deletes a specific task in Google Tasks by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private static boolean deleteGoogleTask(String taskId) {
        boolean isDeleted = false;

        if (GoogleController.isGoogleLoaded()) {
            isDeleted = _tasksController.deleteTask(taskId);
        }

        return isDeleted;
    }

    /**
     * deletes a specific task in Google Calendar by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private static boolean deleteGoogleEvent(String taskId) {
        boolean isDeleted = false;
        if (GoogleController.isGoogleLoaded()) {
            isDeleted = _calendarController.deleteEvent(taskId);
        }
        return isDeleted;
    }
    
    private void setDeleted(chirptask.storage.Task taskToModify) {
        taskToModify.setGoogleId(""); // Set Google ID to empty for deletion
        taskToModify.setDeleted(false); // Reset isDeleted flag to false
    }
}

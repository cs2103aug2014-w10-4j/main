//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import chirptask.common.Constants;
import chirptask.google.GoogleController.Status;
import chirptask.storage.EventLogger;

/**
 * ConcurrentDeleted is submitted to the ExecutorService to run Concurrently
 * It will determine which method to call based on the Task Type
 * Then, it will delete the correct task with the stored Google ID.
 */
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
            setAllNull();
        } else {
            setAllVars(taskToDelete, tasksController, calController);
        }
    }

    private void setAllNull() {
        ConcurrentHandler.setNull(_taskToDelete);
        ConcurrentHandler.setNull(_calendarController);
        ConcurrentHandler.setNull(_tasksController);
    }
    
    private void setAllVars(chirptask.storage.Task task, 
            TasksController tController,
            CalendarController cController) {
        _taskToDelete = task;
        _tasksController = tController;
        _calendarController = cController;
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
    
    static boolean deleteTask(chirptask.storage.Task taskToDelete)
            throws UnknownHostException, IOException {
        if (taskToDelete == null) {
            return false;
        }
        
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
        case chirptask.storage.Task.TASK_FLOATING :
        case chirptask.storage.Task.TASK_DEADLINE :
            isDeleted = deleteGoogleTask(googleId);
            break;
        case chirptask.storage.Task.TASK_TIMED :
            isDeleted = deleteGoogleEvent(googleId);
            break;
        default :
            EventLogger.getInstance().logError(Constants.LOG_MESSAGE_UNEXPECTED);
            assert false;
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
        if (taskId == null) {
            return false;
        }
        
        boolean isDeleted = false;

        if (GoogleController.isGoogleLoaded()) {
            isDeleted = _tasksController.deleteTask(taskId);
            if (!isDeleted) {
                GoogleController.setOnlineStatus(Status.SYNC_FAIL);
            }
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
        if (taskId == null) {
            return false;
        }
        
        boolean isDeleted = false;
        
        if (GoogleController.isGoogleLoaded()) {
            isDeleted = _calendarController.deleteEvent(taskId);
            if (!isDeleted) {
                GoogleController.setOnlineStatus(Status.SYNC_FAIL);
            }
        }
        return isDeleted;
    }
    
    /**
     * The instructions in this method is required as a form of clean-up 
     * to allow ChirpTask to recognise the task as an item to be deleted.
     * Setting GoogleID to empty is one of the criteria that ChirpTask checks
     * before actually deleting the Task from its LocalStorage
     * 
     * @param taskToModify The ChirpTask Task to be deleted
     */
    private void setDeleted(chirptask.storage.Task taskToModify) {
        taskToModify.setGoogleId(""); // Set Google ID to empty for deletion
        taskToModify.setDeleted(false); // Reset isDeleted flag to false
    }
}

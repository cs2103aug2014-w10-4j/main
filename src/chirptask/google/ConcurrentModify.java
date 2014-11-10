//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.common.Constants;
import chirptask.google.GoogleController.Status;
import chirptask.storage.EventLogger;
import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

/**
 * ConcurrentModify is submitted to the ExecutorService to run Concurrently
 * It will determine which method to call based on the Task Type
 * Furthermore, if the modification online is successfully, it will reset
 * the local ChirpTask Task isModified flag to false.
 *
 */
class ConcurrentModify implements Callable<Boolean> {

    private chirptask.storage.Task _taskToModify;
    private static TasksController _tasksController;

    ConcurrentModify(chirptask.storage.Task taskToModify,
            TasksController tasksController) {
        
        if (ConcurrentHandler.isNull(taskToModify) || 
                ConcurrentHandler.isNull(tasksController)){
            setAllNull();
        } else {
            setAllVars(taskToModify, tasksController);
        }
    }
    
    private void setAllNull() {
        ConcurrentHandler.setNull(_taskToModify);
        ConcurrentHandler.setNull(_tasksController);
    }

    private void setAllVars(chirptask.storage.Task task, 
            TasksController tController) {
        _taskToModify = task;
        _tasksController = tController;
    }

    public Boolean call() throws UnknownHostException, IOException {
        Boolean isModified = true;

        if (ConcurrentHandler.isNull(_taskToModify)) {
            isModified = false;
            return isModified;
        }

        while (GoogleController.isGoogleLoaded() == false) {
            // wait until google is loaded in background
        }

        String taskType = _taskToModify.getType(); // Will have implications,
                                                   // further discussions
                                                   // needed.

        switch (taskType) { // Currently, floating and deadline uses same API.
        case chirptask.storage.Task.TASK_FLOATING:
        case chirptask.storage.Task.TASK_DEADLINE:
            isModified = isModified && 
                            modifyGoogleTask(_taskToModify);
            break;
        case chirptask.storage.Task.TASK_TIMED:
             isModified = isModified &&
                            modifyGoogleEvent(_taskToModify);
            break;
        default:
            EventLogger.getInstance().logError(Constants.LOG_MESSAGE_UNEXPECTED);
            assert false;
            break;
        }

        /*
         * Overwrites chirptask.storage.Task in the other storages
         */
        if (isModified) {
            _taskToModify.setModified(false); // Reset the isModified Flag to false
            ConcurrentHandler.modifyLocalStorage(_taskToModify);
        }

        return isModified;
    }
    
    /**
     * Uses the GoogleId stored locally to check if the task is stored online
     * If it is, modify the online task. 
     * @param taskToModify The ChirpTask Task object
     * @return True if modified online task, false otherwise
     * @throws UnknownHostException If Google's hosts are unreachable
     * @throws IOException If bad response or transmission error
     */
    static boolean modifyGoogleTask(chirptask.storage.Task taskToModify)
            throws UnknownHostException, IOException {
        if (taskToModify == null) {
            return false;
        }
        
        boolean isModified = false;

        // First check if Google ID exists
        String googleId = taskToModify.getGoogleId();
        String taskType = taskToModify.getType();

        if (!GoogleController.isEntryExists(googleId, taskType)) {
            isModified = false;
            return isModified;
        }
        String taskListId = TasksController.getTaskListId();
        Task modifiedGoogleTask = TasksHandler.getTaskFromId(taskListId,
                googleId);

        modifiedGoogleTask = toggleTasksDone(
                modifiedGoogleTask, taskToModify);
        modifiedGoogleTask = updateTasksDescription(
                modifiedGoogleTask, taskToModify);
        modifiedGoogleTask = updateDueDate(modifiedGoogleTask,
                taskToModify);
        modifiedGoogleTask = TasksController.updateTask(modifiedGoogleTask);

        if (ConcurrentHandler.isNotNull(modifiedGoogleTask)) {
            isModified = true;
        } else {
            isModified = false;
            GoogleController.setOnlineStatus(Status.SYNC_FAIL);
        }

        return isModified;
    }
    
    /**
     * Uses the GoogleId stored locally to check if the task is stored online
     * If it is, modify the online task. 
     * @param taskToModify The ChirpTask Task object
     * @return True if modified online task, false otherwise
     * @throws UnknownHostException If Google's hosts are unreachable
     * @throws IOException If bad response or transmission error
     */
    static boolean modifyGoogleEvent(chirptask.storage.Task taskToModify)
            throws UnknownHostException, IOException {
        if (taskToModify == null || 
                taskToModify instanceof TimedTask == false) {
            return false;
        }
        
        boolean isModified = false;

        // First check if Google ID exists
        String googleId = taskToModify.getGoogleId();
        String taskType = taskToModify.getType();

        if (!GoogleController.isEntryExists(googleId, taskType)) {
            isModified = false;
            return isModified;
        }

        String calendarId = CalendarController.getCalendarId();
        Event modifiedGoogleEvent = CalendarHandler.getEventFromId(calendarId,
                googleId);
        modifiedGoogleEvent = createModifiedEvent(taskToModify, modifiedGoogleEvent);

        if (ConcurrentHandler.isNotNull(modifiedGoogleEvent)) {
            isModified = true;
        } else {
            isModified = false;
            GoogleController.setOnlineStatus(Status.SYNC_FAIL);
        }

        return isModified;
    }
    
    /**
     * This method enters details such as description from ChirpTask Task into 
     * Google Calendar Event object.
     * @param modifiedTask The ChirpTask Task object
     * @param modifiedEvent The Google Calendar Event object
     * @return The modified Google Calendar Event object
     * @throws IOException Can be thrown by CalendarHandler.updateEvent
     */
    static Event createModifiedEvent(chirptask.storage.Task modifiedTask, 
                                      Event modifiedEvent) throws IOException {
        if (modifiedTask == null || modifiedEvent == null || 
                modifiedTask instanceof TimedTask == false) {
            return null;
        }
        
        Event modifiedGoogleEvent = modifiedEvent;
        String calendarId = CalendarController.getCalendarId();
        String googleId = modifiedTask.getGoogleId();
        String newDesc = modifiedTask.getDescription();
        TimedTask modifyTimeTask = (TimedTask) modifiedTask;
        
        modifiedGoogleEvent = 
                CalendarHandler.setSummary(modifiedGoogleEvent, newDesc);
        modifiedGoogleEvent = 
                CalendarHandler.setStartAndEnd(modifyTimeTask, modifiedGoogleEvent);
        modifiedGoogleEvent = 
                CalendarHandler.setColorAndLook(modifiedGoogleEvent, 
                        modifiedTask.isDone());
        modifiedGoogleEvent = 
                CalendarHandler.updateEvent(calendarId, 
                        googleId, 
                        modifiedGoogleEvent);
        
        return modifiedGoogleEvent;
    }
    
    // Called by modifyGoogleTasks or modifyGoogleEvents
    static Task toggleTasksDone(Task googleTask,
            chirptask.storage.Task toggleTask) {
        if (googleTask == null || toggleTask == null) {
            return null;
        }
        
        boolean isDone = toggleTask.isDone();
        Task toggledTask = googleTask;
        
        if (isDone) {
            toggledTask = TasksHandler.setCompleted(googleTask);
        } else {
            toggledTask = TasksHandler.setNotCompleted(googleTask);
        }

        if (toggledTask != null) {
            return toggledTask;
        } else {
            return null;
        }
    }

    /**
     * Update the GoogleTasks Due Date value with the one stored in ChirpTask
     * @param taskToUpdate Google Tasks Task object
     * @param updatedTask ChirpTask Task object
     * @return The updated task if successful, null otherwise
     */
    static Task updateDueDate(Task taskToUpdate,
            chirptask.storage.Task updatedTask) {
        if (taskToUpdate == null || updatedTask == null) {
            return null;
        }
        
        String taskType = updatedTask.getType();

        switch (taskType) {
        case chirptask.storage.Task.TASK_DEADLINE :
            Date newDueDate = updatedTask.getDate().getTime();

            Task updatedGoogleTask = TasksHandler.setDueDate(
                taskToUpdate, newDueDate);

            if (updatedGoogleTask != null) {
                return updatedGoogleTask;
            } else {
                return taskToUpdate;
            }
        case chirptask.storage.Task.TASK_FLOATING :
            return taskToUpdate;
        case chirptask.storage.Task.TASK_TIMED :
            //Should not reach here in normal cases
            return null;
        default :
            //Should not reach here at all if covers all types of task
            EventLogger.getInstance()
                .logError(Constants.LOG_MESSAGE_INVALID_TASK_TYPE);
            assert false;
            return null;
        }
    }

    static Task updateTasksDescription(Task taskToUpdate,
            chirptask.storage.Task updatedTask) {
        if (taskToUpdate == null || updatedTask == null) {
            return null;
        }
        
        String updatedDescription = updatedTask.getDescription();
        Task updatedGoogleTask = 
                TasksHandler.setTitle(taskToUpdate, updatedDescription);

        if (updatedGoogleTask != null) {
            return updatedGoogleTask;
        } else {
            return null;
        }
    }
}
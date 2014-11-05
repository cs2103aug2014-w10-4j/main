package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.common.Messages;
import chirptask.storage.EventLogger;
import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;


class ConcurrentModify implements Callable<Boolean> {

    private chirptask.storage.Task _taskToModify;
    private static TasksController _tasksController;

    ConcurrentModify(chirptask.storage.Task taskToModify, 
            GoogleController gController
            , TasksController tasksController, 
            CalendarController calController) {
        
        if (ConcurrentHandler.isNull(taskToModify) || 
                ConcurrentHandler.isNull(tasksController)){
            taskToModify = null;
            _tasksController = null;
        } else {
            _taskToModify = taskToModify;
            _tasksController = tasksController;
        }
    }

    ConcurrentModify(chirptask.storage.Task taskToModify) {
        if (ConcurrentHandler.isNull(taskToModify)) {
            _taskToModify = null;
        } else {
            _taskToModify = taskToModify;
        }
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
        case "floating":
        case "deadline":
            isModified = isModified && 
                            modifyGoogleTask(_taskToModify);
            break;
        case "timedtask":
             isModified = isModified &&
                            modifyGoogleEvent(_taskToModify);
            break;
        default:
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
    
    static boolean modifyGoogleTask(chirptask.storage.Task taskToModify)
            throws UnknownHostException, IOException {
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
        }

        return isModified;
    }
    
    static boolean modifyGoogleEvent(chirptask.storage.Task taskToModify)
            throws UnknownHostException, IOException {
        boolean isModified = false;

        // First check if Google ID exists
        String googleId = taskToModify.getGoogleId();
        String taskType = taskToModify.getType();

        if (!GoogleController.isEntryExists(googleId, taskType)) {
            isModified = false;
            return isModified;
        }

        chirptask.storage.Task modifyTask = taskToModify;

        String newDescription = modifyTask.getDescription();
        String calendarId = CalendarController.getCalendarId();
        Event modifiedGoogleEvent = CalendarHandler.getEventFromId(calendarId,
                googleId);
        
        modifiedGoogleEvent = CalendarHandler.setSummary(modifiedGoogleEvent, newDescription);

        if (taskToModify instanceof TimedTask) { // Try type casting
            TimedTask modifyTimeTask = (TimedTask) modifyTask;
            Date newStartTime = modifyTimeTask.getStartTime().getTime();
            Date newEndTime = modifyTimeTask.getEndTime().getTime();
            modifiedGoogleEvent = CalendarHandler.setStart(modifiedGoogleEvent,
                    newStartTime);
            modifiedGoogleEvent = CalendarHandler.setEnd(modifiedGoogleEvent,
                    newEndTime);
        }
        
        modifiedGoogleEvent = CalendarHandler.setColorAndLook(modifiedGoogleEvent, taskToModify.isDone());

        modifiedGoogleEvent = CalendarHandler.updateEvent(calendarId, googleId,
                modifiedGoogleEvent);

        if (ConcurrentHandler.isNotNull(modifiedGoogleEvent)) {
            isModified = true;
        }

        return isModified;
    }
    
    // Called by modifyGoogleTasks or modifyGoogleEvents
    static Task toggleTasksDone(Task googleTask,
            chirptask.storage.Task toggleTask) {

        boolean isDone = toggleTask.isDone();
        Task toggledTask = _tasksController.toggleTaskDone(googleTask, isDone);

        if (toggledTask != null) {
            return toggledTask;
        } else {
            return null;
        }
    }

    static Task updateDueDate(Task taskToUpdate,
            chirptask.storage.Task updatedTask) {
        String taskType = updatedTask.getType();

        if (taskType.equals(chirptask.storage.Task.TASK_DEADLINE)) {
            Date newDueDate = updatedTask.getDate().getTime();

            Task updatedGoogleTask = _tasksController.updateDueDate(
                    taskToUpdate, newDueDate);

            if (updatedGoogleTask != null) {
                return updatedGoogleTask;
            } else {
                return taskToUpdate;
            }
        } else if (taskType.equals(chirptask.storage.Task.TASK_FLOATING)) { 
            return taskToUpdate;
        } else { //Should not reach here 
            EventLogger.getInstance().logError(Messages.LOG_MESSAGE_INVALID_TASK_TYPE);
            assert false;
            return null;
        }
    }

    static Task updateTasksDescription(Task taskToUpdate,
            chirptask.storage.Task updatedTask) {

        String updatedDescription = updatedTask.getDescription();
        Task updatedGoogleTask = _tasksController.updateDescription(
                taskToUpdate, updatedDescription);

        if (updatedGoogleTask != null) {
            return updatedGoogleTask;
        } else {
            return null;
        }
    }
}
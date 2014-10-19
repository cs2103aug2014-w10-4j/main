package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import chirptask.storage.GoogleStorage;
import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

class ConcurrentHandler {
    /**
     * General ChirpTask Task
     */
    static boolean isNull(chirptask.storage.Task task) {
        if (task == null) {
            return true;
        } else {
            return false;
        }
    }

    private static chirptask.storage.Task addGoogleIdToChirpTask(
            chirptask.storage.Task taskToModify, String googleId) {
        taskToModify.setGoogleId(googleId);
        return taskToModify;
    }

    /**
     * Google Tasks Task
     */
    static boolean isNotNull(Task googleTask) {
        if (googleTask != null) {
            return true;
        } else {
            return false;
        }
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
            Task modifiedGoogleTask = TasksHandler.getTaskFromId(taskListId, googleId);

            modifiedGoogleTask = GoogleController
                                    .toggleTasksDone(modifiedGoogleTask, taskToModify);
            modifiedGoogleTask = GoogleController
                                    .updateTasksDescription(modifiedGoogleTask, taskToModify);
            modifiedGoogleTask = GoogleController
                                    .updateDueDate(modifiedGoogleTask, taskToModify);
            modifiedGoogleTask = TasksController
                                    .updateTask(modifiedGoogleTask);
            
            if (isNotNull(modifiedGoogleTask)) {
                /*
                 * Possibly used to overwrite googleId in local storage, eg.
                 * change type from floating to timed. (GoogleTasks <->
                 * GoogleCalendar)
                 * ConcurrentHandler.addGoogleIdToStorage(modifiedGoogleTask,
                 * taskToModify);
                 */
                isModified = true;
            }

        return isModified;
    }

    static void addGoogleIdToStorage(Task googleTask,
            chirptask.storage.Task taskToModify) {
        String googleId = getGoogleId(googleTask);
        chirptask.storage.Task modifiedTask = addGoogleIdToChirpTask(
                taskToModify, googleId);
        GoogleStorage.addGoogleIdToStorage(modifiedTask);
    }

    private static String getGoogleId(Task googleTask) {
        String googleId = googleTask.getId();
        return googleId;
    }

    /**
     * Google Calendar Events
     */
    static boolean isNotNull(Event googleEvent) {
        if (googleEvent != null) {
            return true;
        } else {
            return false;
        }
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

            Event modifiedGoogleEvent = CalendarHandler.getEventFromId(calendarId, googleId);
            modifiedGoogleEvent = CalendarHandler.setSummary(modifiedGoogleEvent, newDescription);
            
            if (taskToModify instanceof TimedTask) { //Try type casting
                TimedTask modifyTimeTask = (TimedTask) modifyTask;
                Date newStartTime = modifyTimeTask.getStartTime().getTime();
                Date newEndTime = modifyTimeTask.getEndTime().getTime();
                modifiedGoogleEvent = CalendarHandler.setStart(modifiedGoogleEvent, newStartTime);
                modifiedGoogleEvent = CalendarHandler.setEnd(modifiedGoogleEvent, newEndTime);
            }
            
            modifiedGoogleEvent = CalendarHandler.updateEvent(calendarId, googleId, modifiedGoogleEvent);

            if (isNotNull(modifiedGoogleEvent)) {
                /*
                 * Possibly used to overwrite googleId in local storage, eg.
                 * change type from floating to timed. (GoogleTasks <->
                 * GoogleCalendar)
                 * ConcurrentHandler.addGoogleIdToStorage(modifiedGoogleTask,
                 * taskToModify);
                 */
                isModified = true;
            }

        return isModified;
    }

    /*
     * static Event getGoogleEventFromId(String googleId) { return null; }
     */

    static void addGoogleIdToStorage(Event googleTask,
            chirptask.storage.Task taskToModify) {
        String googleId = getGoogleId(googleTask);
        chirptask.storage.Task modifiedTask = addGoogleIdToChirpTask(
                taskToModify, googleId);
        GoogleStorage.addGoogleIdToStorage(modifiedTask);
    }

    private static String getGoogleId(Event googleTask) {
        String googleId = googleTask.getId();
        return googleId;
    }

}


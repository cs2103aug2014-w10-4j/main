package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import chirptask.storage.GoogleStorage;
import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

class ConcurrentHandler {
    private static final String STRING_DONE = "[Done]";
    private static final String STRING_EMPTY = "";
    
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
        Task modifiedGoogleTask = TasksHandler.getTaskFromId(taskListId,
                googleId);

        modifiedGoogleTask = GoogleController.toggleTasksDone(
                modifiedGoogleTask, taskToModify);
        modifiedGoogleTask = GoogleController.updateTasksDescription(
                modifiedGoogleTask, taskToModify);
        modifiedGoogleTask = GoogleController.updateDueDate(modifiedGoogleTask,
                taskToModify);
        modifiedGoogleTask = TasksController.updateTask(modifiedGoogleTask);

        if (isNotNull(modifiedGoogleTask)) {
            /*
             * Possibly used to overwrite googleId in local storage, eg. change
             * type from floating to timed. (GoogleTasks <-> GoogleCalendar)
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
        GoogleStorage.updateStorages(modifiedTask);
    }

    private static String getGoogleId(Task googleTask) {
        String googleId = googleTask.getId();
        return googleId;
    }

    static void addETagToStorage(Task googleTask,
            chirptask.storage.Task taskToModify) {
        String eTag = getETag(googleTask);

        chirptask.storage.Task modifiedTask = addETagToChirpTask(
                taskToModify,
                eTag);

        if (modifiedTask != null) {
            GoogleStorage.updateStorages(modifiedTask);
        }
    }

    static String getETag(Task googleTask) {
        String eTag = "";
        if (googleTask == null) {
            return eTag;
        }

        eTag = googleTask.getEtag();

        return eTag;
    }

    private static chirptask.storage.Task addETagToChirpTask(
            chirptask.storage.Task taskToUpdate, String eTag) {
        chirptask.storage.Task updatedTask = null;
        if (eTag != null && taskToUpdate != null) {
            taskToUpdate.setETag(eTag);
            updatedTask = taskToUpdate;
        }
        return updatedTask;
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

        boolean isDone = modifyTask.isDone();
        String newDescription = modifyTask.getDescription();
        String calendarId = CalendarController.getCalendarId();

        Event modifiedGoogleEvent = CalendarHandler.getEventFromId(calendarId,
                googleId);
        
        if (isDone) {
            newDescription = setDoneDescription(newDescription);
            modifiedGoogleEvent = CalendarHandler.setDescription(modifiedGoogleEvent, STRING_DONE);
        } else {
            if (newDescription.startsWith(STRING_DONE)) {
                newDescription.replaceFirst(STRING_DONE, STRING_EMPTY);
            }
            modifiedGoogleEvent = CalendarHandler.setDescription(modifiedGoogleEvent, STRING_EMPTY);
        }
        
        modifiedGoogleEvent = CalendarHandler.setSummary(modifiedGoogleEvent,
                newDescription);

        if (taskToModify instanceof TimedTask) { // Try type casting
            TimedTask modifyTimeTask = (TimedTask) modifyTask;
            Date newStartTime = modifyTimeTask.getStartTime().getTime();
            Date newEndTime = modifyTimeTask.getEndTime().getTime();
            modifiedGoogleEvent = CalendarHandler.setStart(modifiedGoogleEvent,
                    newStartTime);
            modifiedGoogleEvent = CalendarHandler.setEnd(modifiedGoogleEvent,
                    newEndTime);
        }

        modifiedGoogleEvent = CalendarHandler.updateEvent(calendarId, googleId,
                modifiedGoogleEvent);

        if (isNotNull(modifiedGoogleEvent)) {
            /*
             * Possibly used to overwrite googleId in local storage, eg. change
             * type from floating to timed. (GoogleTasks <-> GoogleCalendar)
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

    static void addGoogleIdToStorage(Event googleEvent,
            chirptask.storage.Task taskToModify) {
        String googleId = getGoogleId(googleEvent);
        chirptask.storage.Task modifiedTask = addGoogleIdToChirpTask(
                taskToModify, googleId);
        GoogleStorage.updateStorages(modifiedTask);
    }
    
    static void addETagToStorage(Event googleEvent,
            chirptask.storage.Task taskToModify) {
        String eTag = getETag(googleEvent);

        chirptask.storage.Task modifiedTask = addETagToChirpTask(
                taskToModify,
                eTag);

        if (modifiedTask != null) {
            GoogleStorage.updateStorages(modifiedTask);
        }
    }

    static void modifyLocalStorage(chirptask.storage.Task taskToModify) {
        if (taskToModify != null) {
            GoogleStorage.updateStorages(taskToModify);
        }
    }

    private static String getGoogleId(Event googleEvent) {
        String googleId = "";
        if (googleEvent == null) {
            return googleId;
        }
        
        googleId = googleEvent.getId();
        
        return googleId;
    }
    
    static String getETag(Event googleEvent) {
        String eTag = "";
        if (googleEvent == null) {
            return eTag;
        }

        eTag = googleEvent.getEtag();

        return eTag;
    }
    
    static String setDoneDescription(String description) {
        String newDescription = "";
        if (description != null) {
            newDescription = STRING_DONE + " " + description;
        }
        return newDescription;
    }

}

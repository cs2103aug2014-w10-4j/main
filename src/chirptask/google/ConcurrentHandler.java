//@author A0111840W
package chirptask.google;

import java.util.List;

import chirptask.storage.GoogleStorage;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

class ConcurrentHandler {
    
    /** 
     * General Google Component Preconditions
     */
    static boolean isNull(GoogleController gController) {
        if (gController == null) {
            return true;
        } else {
            return false;
        }
    }
    
    static boolean isNull(TasksController tController) {
        if (tController == null) {
            return true;
        } else {
            return false;
        }
    }
    
    static boolean isNull(CalendarController cController) {
        if (cController == null) {
            return true;
        } else {
            return false;
        }
    }
    
    static void setNull(chirptask.storage.Task task) {
        task = null;
    }
    
    static void setNull(List<chirptask.storage.Task> taskList) {
        taskList = null;
    }
    
    static void setNull(GoogleController gController) {
        gController = null;
    }
    
    static void setNull(TasksController tController) {
        tController = null;
    }
    
    static void setNull(CalendarController cController) {
        cController = null;
    }
    
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

    static boolean addGoogleIdToStorage(Task googleTask,
            chirptask.storage.Task taskToModify) {
        if (googleTask == null || taskToModify == null) {
            return false;
        }
        
        boolean isAdded = false;
        
        String googleId = getGoogleId(googleTask);
        chirptask.storage.Task modifiedTask = addGoogleIdToChirpTask(
                                                taskToModify, googleId);
        GoogleStorage.updateStorages(modifiedTask);
        isAdded = true;
        
        return isAdded;
    }

    private static String getGoogleId(Task googleTask) {
        String googleId = googleTask.getId();
        return googleId;
    }
    
    private static chirptask.storage.Task addGoogleIdToChirpTask(
            chirptask.storage.Task taskToModify, String googleId) {
        taskToModify.setGoogleId(googleId);
        return taskToModify;
    }

    static boolean addETagToStorage(Task googleTask,
            chirptask.storage.Task taskToModify) {
        if (googleTask == null || taskToModify == null) {
            return false;
        }
        
        boolean isAdded = false;
        
        String eTag = getETag(googleTask);

        chirptask.storage.Task modifiedTask = addETagToChirpTask(
                taskToModify,
                eTag);

        if (modifiedTask != null) {
            GoogleStorage.updateStorages(modifiedTask);
        }
        isAdded = true;
        
        return isAdded;
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

    static boolean addGoogleIdToStorage(Event googleEvent,
            chirptask.storage.Task taskToModify) {
        if (googleEvent == null || taskToModify == null) {
            return false;
        }
        
        boolean isAdded = false;
        
        String googleId = getGoogleId(googleEvent);
        chirptask.storage.Task modifiedTask = addGoogleIdToChirpTask(
                taskToModify, googleId);
        GoogleStorage.updateStorages(modifiedTask);
        isAdded = true;
        
        return isAdded;
    }
    
    static boolean addETagToStorage(Event googleEvent,
            chirptask.storage.Task taskToModify) {
        if (googleEvent == null || taskToModify == null) {
            return false;
        }
        
        boolean isAdded = false;
        
        String eTag = getETag(googleEvent);

        chirptask.storage.Task modifiedTask = addETagToChirpTask(
                taskToModify,
                eTag);

        if (modifiedTask != null) {
            GoogleStorage.updateStorages(modifiedTask);
        }
        isAdded = true;
        
        return isAdded;
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

}

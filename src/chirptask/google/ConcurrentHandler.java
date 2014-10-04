package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.storage.GoogleStorage;

import com.google.api.services.tasks.model.Task;

class ConcurrentHandler {
    protected static boolean isNull(chirptask.storage.Task task) {
        if (task == null) {
            return true;
        } else {
            return false;
        }
    }
    
    protected static boolean isNotNull(Task googleTask) {
        if (googleTask != null) {
            return true;
        } else {
            return false;
        }
    }
    
    protected static void addGoogleIdToStorage(Task googleTask,
                                        chirptask.storage.Task taskToModify) {
        String googleId = getGoogleId(googleTask);
        chirptask.storage.Task modifiedTask = 
                addGoogleIdToChirpTask(taskToModify, googleId);
        GoogleStorage.addGoogleIdToStorage(modifiedTask);
    }

    private static String getGoogleId(Task googleTask) {
        String googleId = googleTask.getId();
        return googleId;
    }
    
    private static chirptask.storage.Task addGoogleIdToChirpTask(
                        chirptask.storage.Task taskToModify, String googleId){
        taskToModify.setGoogleId(googleId);
        return taskToModify;
    }
}

class ConcurrentToggleDone implements Callable<Task> {
    private chirptask.storage.Task _taskToToggleDone;

    protected ConcurrentToggleDone(
            chirptask.storage.Task taskToToggleDone) {
        if (ConcurrentHandler.isNull(taskToToggleDone)) {
            _taskToToggleDone = null;
        } else {
            _taskToToggleDone = taskToToggleDone;
        }
    }

    public Task call() throws IOException, UnknownHostException {
        if (ConcurrentHandler.isNull(_taskToToggleDone)) {
            return null;
        }
        
        while (!GoogleController.isGoogleLoaded()) {
            // wait until google is loaded
        }

        String type = _taskToToggleDone.getType();
        Task modifiedGoogleTask = null;

        switch (type) {
        case "floating":
        case "deadline":
            modifiedGoogleTask = GoogleController.toggleFloatingDone(_taskToToggleDone);
            break;
        case "timed":
            break;
        default:
            break;
        }

        if (ConcurrentHandler.isNotNull(modifiedGoogleTask)) {
            /* 
             * Possibly used to overwrite googleId in local storage, 
             * eg. change type from floating to timed.
             * ConcurrentHandler.addGoogleIdToStorage(modifiedGoogleTask, 
             *                                          _taskToToggleDone);
             * 
             */
        }

        return modifiedGoogleTask;
    }
}

class ConcurrentAdd implements Callable<Task> {

    private chirptask.storage.Task _taskToAdd;

    protected ConcurrentAdd(chirptask.storage.Task taskToAdd) {
        if (ConcurrentHandler.isNull(taskToAdd)) {
            _taskToAdd = null;
        } else {
            _taskToAdd = taskToAdd;
        }
    }

    public Task call() throws IOException, UnknownHostException {
        if (ConcurrentHandler.isNull(_taskToAdd)) {
            return null;
        }
        
        while (!GoogleController.isGoogleLoaded()) {
            // wait until google is loaded
        }

        String type = _taskToAdd.getType();
        String task = _taskToAdd.getDescription();
        Date date = null;

        if (_taskToAdd.getDate() != null) {
            date = _taskToAdd.getDate();
        }
        Task addedGoogleTask = null;

        switch (type) {
        case "floating":
            addedGoogleTask = GoogleController.addFloatingTask(task);
            break;
        case "deadline":
            addedGoogleTask = GoogleController.addDeadlineTask(task, date);
            break;
        case "timed":
            break;
        default:
            break;
        }

        if (ConcurrentHandler.isNotNull(addedGoogleTask)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleTask, _taskToAdd);
        }

        return addedGoogleTask;
    }
}

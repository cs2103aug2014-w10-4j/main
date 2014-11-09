//@author A0111840W
package chirptask.storage;

import java.util.List;

import chirptask.google.GoogleController;
import chirptask.google.GoogleController.GoogleService;

public class GoogleStorage implements IStorage {
    private static GoogleController _gController;

    public GoogleStorage() {
        _gController = new GoogleController();
    }
    
    @Override
    public boolean storeNewTask(Task newTask) {
        boolean isAdded = false;
        _gController.addTask(newTask);
        isAdded = true;
        return isAdded;
    }

    @Override
    public Task removeTask(Task taskToRemove) {
        boolean isRemoved = false;
        _gController.removeTask(taskToRemove);
        isRemoved = true;
        
        if (isRemoved) {
            return taskToRemove;
        } else {
            return null;
        }
    }

    @Override
    public boolean modifyTask(Task modifiedTask) {
        boolean isModified = false;
        _gController.modifyTask(modifiedTask);
        isModified = true;
        return isModified;
    }

    @Override
    public Task getTask(int taskId) {
        return null;
    }

    @Override
    public List<Task> getAllTasks() {
        return null;
    }

    @Override
    public void close() {
        if (_gController != null) {
            _gController.close();
        }
    }
    
    /**
     * After login, GoogleController will use this to signal the 
     * StorageHandler that it is available and can add GoogleStorage
     * into the List of Storages.
     */
    public static void hasBeenInitialized() {
        StorageHandler.addGoogleStorageUponReady();
    }
    
    /**
     * This method is called by the background pool of threads
     * Keep updateStorages synchronized to only allow 1 thread to execute 
     * at a point of time
     * @param newTask The ChirpTask task to be updated
     */
    public synchronized static void updateStorages(Task newTask) {
        //Talk to storage handler to call add google id
        if (StorageHandler.isStorageInit()) {
            StorageHandler.updateStorages(newTask);
        } 
    }
    
    /**
     * This method is called by the background pool of threads
     * Keep deleteFromLocalStorage synchronized to only allow 1 thread to 
     * execute at a point of time
     * @param deleteTask The ChirpTask task to be updated
     */
    public synchronized static void deleteFromLocalStorage(Task deleteTask) {
        if (StorageHandler.isLocalChirpStorageInit()) {
            StorageHandler.deleteFromStorage(deleteTask);
        }
    }
    
    /**
     * This method is called by StorageHandler
     * Keep sync synchronized to only allow 1 thread to execute at a point of time
     * @param allTasks The List of Task to be sync-ed against/with
     * @return true if sync is runned, false otherwise
     */
    synchronized boolean sync(List<Task> allTasks) {
        boolean isSyncRunned = false;
        if (allTasks != null) {
            isSyncRunned = _gController.sync(allTasks);
        }
        return isSyncRunned;
    }
    
    /**
     * Calls the login function for Google Component
     * @return true if login function is called, false otherwise
     */
    boolean login() {
        boolean isLoginRun = false;
        if (_gController != null) {
            _gController.login();
            isLoginRun = true;
        }
        return isLoginRun;
    }
    
    /**
     * This is called by Google Component in the Event where the 
     * Google Calendar ID or Google Task ID got corrupted and ChirpTask 
     * cannot find the Calendar or Google Tasks list
     * Thus it creates a new Google Calendar or Google Tasks List.
     * 
     * This is called to allow ChirpTask to re-sync all the affected tasks to
     * the newly created Google Calendar / Google Tasks object
     * @param googleService The Google Service that got affected.
     */
    public static void resetGoogleIdAndEtag(GoogleService googleService) {
        StorageHandler.resetGoogleIdAndEtag(googleService);
    }
    
}

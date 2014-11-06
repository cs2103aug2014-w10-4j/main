package chirptask.storage;

import java.util.List;

import chirptask.google.GoogleController;

//For every action, return true, google service is up
//Return false when google service is down
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
    
    public static void hasBeenInitialized() {
        StorageHandler.addGoogleStorageUponReady();
    }
    
    public synchronized static void updateStorages(Task newTask) {
        //Talk to storage handler to call add google id
        if (StorageHandler.isStorageInit()) {
            StorageHandler.updateStorages(newTask);
        } 
    }
    
    public synchronized static void deleteFromLocalStorage(Task deleteTask) {
        if (StorageHandler.isLocalChirpStorageInit()) {
            StorageHandler.deleteFromStorage(deleteTask);
        }
    }
    
    synchronized boolean sync(List<Task> allTasks) {
        boolean isSyncRunned = false;
        if (allTasks != null) {
            isSyncRunned = _gController.sync(allTasks);
        }
        return isSyncRunned;
    }
    
    boolean login() {
        boolean isLoginRun = false;
        if (_gController != null) {
            _gController.login();
            isLoginRun = true;
        }
        return isLoginRun;
    }
    
    public static void resetGoogleIdAndEtag(String googleService) {
        StorageHandler.resetGoogleIdAndEtag(googleService);
    }
    
}

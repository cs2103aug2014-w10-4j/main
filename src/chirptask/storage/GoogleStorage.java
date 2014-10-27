package chirptask.storage;

import java.io.IOException;
import java.net.UnknownHostException;
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
        try {
            _gController.add(newTask);
            isAdded = true;
        } catch (UnknownHostException unknownHost) {
            //TODO for no access to Google services
            // retry add with sleep timer
        } catch (IOException ioError) {
            
        }
        return isAdded;
    }

    @Override
    public Task removeTask(Task taskToRemove) {
        boolean isRemoved = false;
        try {
            _gController.removeTask(taskToRemove);
            isRemoved = true;
        } catch (UnknownHostException unknownHostException) {
            //TODO for no access to Google services
            // retry remove with sleep timer
        } catch (IOException ioException) {
            
        }
        
        if (isRemoved) {
            return taskToRemove;
        } else {
            return null;
        }
    }

    @Override
    public boolean modifyTask(Task modifiedTask) {
        boolean isModified = false;
        try {
            _gController.modifyTask(modifiedTask);
            isModified = true;
        } catch (UnknownHostException unknownHostException) {
            //TODO for no access to Google services
            // retry modify with sleep timer
        } catch (IOException ioException) {
            
        }
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
    
    public static void updateStorages(Task newTask) {
        //Talk to storage handler to call add google id
        if (StorageHandler.isStorageInit()) {
            StorageHandler.updateStorages(newTask);
        } 
    }
    
    void sync(List<Task> allTasks) {
        if (allTasks != null) {
            try {
                _gController.sync(allTasks);
            } catch (UnknownHostException unknownHostException) {
                // retry sync with sleep timer
            } catch (IOException ioException) {
                
            }
        }
    }
    
    void login() {
        if (_gController != null) {
            _gController.login();
        }
    }
    
}

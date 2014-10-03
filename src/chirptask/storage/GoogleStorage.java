package chirptask.storage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Future;

import chirptask.google.GoogleController;

//For every action, return true, google service is up
//Return false when google service is down
public class GoogleStorage implements Storage {
    private static GoogleController _gController;

    public GoogleStorage() {
        _gController = new GoogleController();
        Thread initializeGoogleController = new Thread(_gController);
        initializeGoogleController.setDaemon(true);
        initializeGoogleController.start();
    }
    
    @Override
    public boolean storeNewTask(Task newTask) {
        boolean isAdded = false;
        try {
            _gController.add(newTask);
            isAdded = true;
        } catch (UnknownHostException unknownHost) {
            //TODO for no access to Google services
        } catch (IOException ioError) {
            
        }
        return isAdded;
    }

    @Override
    public Task removeTask(Task removeTask) {
        return null;
    }

    @Override
    public boolean modifyTask(Task modifiedTask) {
        return false;
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

    }
    
    public static void hasBeenInitialized() {
        StorageHandler.addGoogleStorageUponReady();
    }
    
    public static void addGoogleIdToStorage(Task newTask) {
        //Talk to storage handler to call add google id
        StorageHandler.updateGoogleId(newTask);
    }

}

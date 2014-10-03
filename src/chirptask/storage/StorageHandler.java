package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

public class StorageHandler {
    /** Global instance of ChirpTask's local copy. */
    private static List<Task> _allTasks;

    private static List<Storage> _listOfStorages = new ArrayList<Storage>();
    private static Storage localStorage;
    private static Storage googleStorage;
    private static Storage eventStorage;

    public StorageHandler() {
        initStorages();
    }

    private void initStorages() {
        //createStoragesList();
        addLocalList();
        addLocalStorage();
        addEventStorage();
    }

    public void initCloudStorage() {
        addGoogleStorage();
    }

    /*private void createStoragesList() {
        _listOfStorages = new ArrayList<Storage>();
    }*/

    private void addLocalList() {
        if (!isLocalListInit()) {
            _allTasks = new ArrayList<Task>();
        }
    }

    private void addLocalStorage() {
        if (!isLocalStorageInit()) {
            localStorage = new LocalStorage();
            _listOfStorages.add(localStorage);
        }
    }

    private void addEventStorage() {
        if (!isEventStorageInit()) {
            eventStorage = new EventLogger();
            _listOfStorages.add(eventStorage);
        }
    }

    private void addGoogleStorage() {
        if (!isGoogleStorageInit()) {
            googleStorage = new GoogleStorage();
        }
    }

    static void addGoogleStorageUponReady() {
        if (isStoragesListInit()) {
            _listOfStorages.add(googleStorage);
        }
    }

    public static List<Task> getAllTasks() {
        return _allTasks;
    }

    // @author A0111889W
    public void closeStorages() {
        for (Storage individualStorage : _listOfStorages) {
            individualStorage.close();
        }
    }

    // @author A0111889W
    public void modifyTask(Task modifiedTask) {
        if (_allTasks.contains(modifiedTask)) {
            int indexOfTask = _allTasks.indexOf(modifiedTask);
            _allTasks.add(indexOfTask, modifiedTask);
            _allTasks.remove(indexOfTask + 1);
        }

        for (Storage individualStorage : _listOfStorages) {
            individualStorage.modifyTask(modifiedTask);
        }
    }

    // @author A0111889W
    public void addTask(Task addedTask) {
        _allTasks.add(addedTask);
        for (Storage individualStorage : _listOfStorages) {
            individualStorage.storeNewTask(addedTask);
        }
    }

    // @author A0111889W
    public void deleteTask(Task deletedTask) {
        _allTasks.remove(deletedTask);
        for (Storage individualStorage : _listOfStorages) {
            individualStorage.removeTask(deletedTask);
        }
    }

    static void updateGoogleId(Task modifiedTask) {
        if (isStorageInit()) {
            if (_allTasks.contains(modifiedTask)) {
                int indexOfTask = _allTasks.indexOf(modifiedTask);
                _allTasks.add(indexOfTask, modifiedTask);
                _allTasks.remove(indexOfTask + 1);
            }
            localStorage.modifyTask(modifiedTask);
            eventStorage.modifyTask(modifiedTask);
        }
    }

    //@author A0111840W
    static boolean isStorageInit() {
        boolean init = true;
        init = init && isStoragesListInit();
        init = init && isLocalListInit();
        init = init && isLocalStorageInit();
        init = init && isEventStorageInit();
        init = init && isGoogleStorageInit();
        return init;
    }
    
    private static boolean isStoragesListInit() {
        if (_listOfStorages != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isLocalListInit() {
        if (_allTasks != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isLocalStorageInit() {
        if (localStorage != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isEventStorageInit() {
        if (eventStorage != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isGoogleStorageInit() {
        if (googleStorage != null) {
            return true;
        } else {
            return false;
        }
    }

}

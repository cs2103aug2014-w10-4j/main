package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

import chirptask.logic.Logic;

public class StorageHandler {
    /** Global instance of ChirpTask's local copy. */
    private static List<Task> _allTasks;

    private static List<IStorage> _listOfStorages = new ArrayList<IStorage>();
    private static IStorage localStorage;
    private static IStorage googleStorage;
    private static IStorage eventStorage;

    public StorageHandler() {
        initStorages();
    }

    private void initStorages() {
        //createStoragesList();
        addLocalList();
        addLocalStorage();
        addEventStorage();
        setAllTasks(localStorage.getAllTasks());
    }

    public boolean initCloudStorage() {
        boolean isInit = false;
        addGoogleStorage();
        isInit = true;
        return isInit;
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
            eventStorage = EventLogger.getInstance();
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
            sync();
        }
    }

    public static List<Task> getAllTasks() {
        return _allTasks;
    }
    
    public void setAllTasks(List<Task> allTasks) {
        _allTasks = allTasks;
    }

    //@author A0111889W
    public synchronized static void logError(String error){
        EventLogger.getInstance().logError(error);
    }
    
    //@author A0111889W
    public void closeStorages() {
        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.close();
        }
    }

    //@author A0111889W
    public synchronized boolean modifyTask(Task modifiedTask) {
        boolean isModified = false;
        if (_allTasks.contains(modifiedTask)) {
            int indexOfTask = _allTasks.indexOf(modifiedTask);
            _allTasks.add(indexOfTask, modifiedTask);
            _allTasks.remove(indexOfTask + 1);
        }

        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.modifyTask(modifiedTask);
        }
        isModified = true;
        return isModified;
    }

    //@author A0111889W
    public synchronized boolean addTask(Task addedTask) {
        boolean isAdded = false;
        
        addedTask.setDeleted(false);
        _allTasks.add(addedTask);
        
        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.storeNewTask(addedTask);
        }
        isAdded = true;
        return isAdded;
    }

    //@author A0111889W
    public synchronized Task deleteTask(Task deletedTask) {
        boolean isDeleted = false;
        
        if ("".equals(deletedTask.getGoogleId())){
            _allTasks.remove(deletedTask);
            for (IStorage individualStorage : _listOfStorages) {
                individualStorage.removeTask(deletedTask);
            }
        } else {
            if (!isStorageInit()) {
                modifyTask(deletedTask);
            } else {
                _allTasks.remove(deletedTask);
                for (IStorage individualStorage : _listOfStorages) {
                    individualStorage.removeTask(deletedTask);
                }
            }
        }
        
        isDeleted = true;
        
        if(isDeleted){
        	return deletedTask;
        } else{
        	return null;
        }
    }

    //@author A0111840W
    private static void sync() {
        if (isStorageInit()) {
            GoogleStorage gStorage = (GoogleStorage) googleStorage;
            List<Task> allTasks = getAllTasks();
            if (allTasks != null) {
                gStorage.sync(allTasks);
            }
        }
    }

    static synchronized void updateStorages(Task modifiedTask) {
        if (isStorageInit()) {
            if ("".equals(modifiedTask.getGoogleId())){
                _allTasks.remove(modifiedTask);
                for (IStorage individualStorage : _listOfStorages) {
                    individualStorage.removeTask(modifiedTask);
                }
            } else {
                if (_allTasks.contains(modifiedTask)) {
                    int indexOfTask = _allTasks.indexOf(modifiedTask);
                    _allTasks.add(indexOfTask, modifiedTask);
                    _allTasks.remove(indexOfTask + 1);
                    localStorage.modifyTask(modifiedTask);
                    eventStorage.modifyTask(modifiedTask);
                } else {
                    _allTasks.add(modifiedTask);
                    localStorage.storeNewTask(modifiedTask);
                    eventStorage.storeNewTask(modifiedTask);
                }
            }
            Logic.refresh(); // need to update GUI
        }
    }

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
        return (_listOfStorages != null); 
    }

    private static boolean isLocalListInit() {
        return (_allTasks != null); 
    }

    private static boolean isLocalStorageInit() {
        return (localStorage != null); 
    }

    private static boolean isEventStorageInit() {
        return (eventStorage != null); 
    }

    private static boolean isGoogleStorageInit() {
        return (googleStorage != null);
    }

}

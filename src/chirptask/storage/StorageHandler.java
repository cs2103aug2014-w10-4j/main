package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

import chirptask.common.Settings;
import chirptask.logic.Logic;

public class StorageHandler {

    private static List<IStorage> _listOfStorages = new ArrayList<IStorage>();
    private static IStorage eventStorage;
    private static IStorage googleStorage;
    private static IStorage localStorage;
    private static IStorage sessionStorage;

    private boolean isAutoLogin = false;

    public StorageHandler() {
        isAutoLogin = readAutoLoginSettings();
        initStorages();
    }

    //@author A0111840W
    private void initStorages() {
        addSessionStorage();
        addLocalStorage();
        addEventStorage();
        if (isAutoLogin) {
            initCloudStorage();
        }
        setAllTasks(localStorage.getAllTasks());
    }

    public boolean initCloudStorage() {
        boolean isInit = false;
        if (!isGoogleStorageInit()) {
            addGoogleStorage();
            if (isGoogleStorageInit()) {
                if (googleStorage instanceof GoogleStorage) {
                    GoogleStorage currentGStorage = (GoogleStorage) googleStorage;
                    if (currentGStorage != null) {
                        currentGStorage.login();
                    }
                }
            }
        }
        isInit = true;
        return isInit;
    }
    
    private static void addSessionStorage() {
        if (!isSessionStorageInit()) {
            sessionStorage = new SessionStorage();
            _listOfStorages.add(sessionStorage);
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
        if (isSessionStorageInit()) {
            return sessionStorage.getAllTasks();
        } else {
            addSessionStorage();
            setAllTasks(localStorage.getAllTasks());
            return getAllTasks();
        }
    }

    private static void setAllTasks(List<Task> allTasks) {
        try {
            SessionStorage sStorage = (SessionStorage) sessionStorage;
            sStorage.setTaskList(allTasks);
        } catch (ClassCastException exception) {
            logError(exception.getLocalizedMessage());
            assert false;
        }
    }

    //@author A0111889W
    public synchronized static void logError(String error) {
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

        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.modifyTask(modifiedTask);
        }
        isModified = true;
        return isModified;
    }

    //@author A0111889W
    public synchronized boolean addTask(Task addedTask) {
        boolean isAdded = false;

        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.storeNewTask(addedTask);
        }
        
        isAdded = true;
        return isAdded;
    }

    //@author A0111889W
    public synchronized Task deleteTask(Task deletedTask) {
        boolean isDeleted = false;

        if ("".equals(deletedTask.getGoogleId())) {
            for (IStorage individualStorage : _listOfStorages) {
                individualStorage.removeTask(deletedTask);
            }
        } else {
            if (isStorageInit()) { //All storages init including Google
                for (IStorage individualStorage : _listOfStorages) {
                    individualStorage.removeTask(deletedTask);
                }
            } else {
                deletedTask.setDeleted(true);
                modifyTask(deletedTask);
            }
        }

        isDeleted = true;

        if (isDeleted) {
            return deletedTask;
        } else {
            return null;
        }
    }

    //@author A0111840W
    private boolean readAutoLoginSettings() {
        boolean isAutoLogin = false;
        if (Settings.class != null) {
            isAutoLogin = Settings.LOGIN_AUTO;
        }
        return isAutoLogin;
    }
    
    static void resetGoogleIdAndEtag(String googleService) {
        if (googleService != null) {
            switch (googleService) {
            case "calendar" :
                resetCalendarItems();
                break;
            case "tasks" :
                resetTasksItems();
                break;
            default :
                break;
            }
        }
    }
    
    static void resetCalendarItems() {
        List<Task> allLocalTasks = getAllTasks();
        for (int i = 0; i < allLocalTasks.size(); i++) {
            Task currentTask = allLocalTasks.get(i);
            String taskType = currentTask.getType();
            
            if ("timedtask".equals(taskType)) {
                currentTask.setGoogleId("");
                currentTask.setETag("");
            }
        }
    }
    
    static void resetTasksItems() {
        List<Task> allLocalTasks = getAllTasks();
        for (int i = 0; i < allLocalTasks.size(); i++) {
            Task currentTask = allLocalTasks.get(i);
            String taskType = currentTask.getType();
            
            if ("deadline".equals(taskType) || "floating".equals(taskType)) {
                currentTask.setGoogleId("");
                currentTask.setETag("");
            }
        }
    }

    public boolean logout() {
        boolean isRanLogout = false;
        
        if (isGoogleStorageInit()) {
            GoogleStorage gStorage = (GoogleStorage) googleStorage;
            gStorage.close();
            _listOfStorages.remove(googleStorage);
            googleStorage = null;
            isRanLogout = true;
        }
        
        return isRanLogout;
    }

    public synchronized static boolean sync() {
        boolean isSyncRunned = false;

        if (isStorageInit()) {
            GoogleStorage gStorage = (GoogleStorage) googleStorage;
            List<Task> allTasks = getAllTasks();
            if (allTasks != null) {
                gStorage.sync(allTasks);
                isSyncRunned = true;
            }
        }

        return isSyncRunned;
    }

    static synchronized void updateStorages(Task modifiedTask) {
        if (isStorageInit()) {
            if ("".equals(modifiedTask.getGoogleId())) {
                for (IStorage individualStorage : _listOfStorages) {
                    individualStorage.removeTask(modifiedTask);
                }
            } else {
                List<Task> allTasks = sessionStorage.getAllTasks();
                
                if (allTasks.contains(modifiedTask)) {
                    sessionStorage.modifyTask(modifiedTask);
                    localStorage.modifyTask(modifiedTask);
                    eventStorage.modifyTask(modifiedTask);
                } else {
                    sessionStorage.storeNewTask(modifiedTask);
                    localStorage.storeNewTask(modifiedTask);
                    eventStorage.storeNewTask(modifiedTask);
                }
            }
            Logic.refresh(); // need to update GUI
        }
    }
    
    //@author A0111889W
    static synchronized void deleteFromStorage(Task deletedTask) {
        if (isLocalChirpStorageInit()) {
            if (deletedTask != null) {
                List<Task> allTasks = sessionStorage.getAllTasks();
                
                if (allTasks.contains(deletedTask)) {
                    sessionStorage.removeTask(deletedTask);
                    localStorage.removeTask(deletedTask);
                    eventStorage.removeTask(deletedTask);
                    Logic.refresh(); // need to update GUI
                }
            }
        }
    }

    static boolean isLocalChirpStorageInit() {
        boolean init = true;
        init = init && isStoragesListInit();
        init = init && isSessionStorageInit();
        init = init && isLocalStorageInit();
        init = init && isEventStorageInit();
        return init;
    }
    
    static boolean isStorageInit() {
        boolean init = true;
        init = init && isStoragesListInit();
        init = init && isSessionStorageInit();
        init = init && isLocalStorageInit();
        init = init && isEventStorageInit();
        init = init && isGoogleStorageInit();
        return init;
    }

    private static boolean isStoragesListInit() {
        return (_listOfStorages != null);
    }
    
    private static boolean isSessionStorageInit() {
        return (sessionStorage != null);
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

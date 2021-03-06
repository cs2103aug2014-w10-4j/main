package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

import chirptask.common.Settings;
import chirptask.google.GoogleController;
import chirptask.google.GoogleController.GoogleService;
import chirptask.google.GoogleController.Status;
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

    public static boolean initCloudStorage() {
        boolean isInit = false;
        if (!isGoogleStorageInit()) {
            addGoogleStorage();

            if (isGStorageValid()) {
                GoogleStorage gStore = (GoogleStorage) googleStorage;
                isInit = gStore.login();
            }
        } else if (googleStorage instanceof GoogleStorage) {
            isInit = sync();
        }
        return isInit;
    }

    private static boolean isGStorageValid() {
        boolean isValid = false;
        if (isGoogleStorageInit()) {
            if (googleStorage != null) {
                if (googleStorage instanceof GoogleStorage) {
                    isValid = true;
                }
            }
        }
        return isValid;
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

    private static void addGoogleStorage() {
        if (!isGoogleStorageInit()) {
            googleStorage = new GoogleStorage();
        }
    }

    static void addGoogleStorageUponReady() {
        if (isStoragesListInit()) {
            _listOfStorages.add(googleStorage);
            GoogleController.setOnlineStatus(Status.ONLINE);
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
        if (allTasks == null) {
            return;
        }

        try {
            SessionStorage sStorage = (SessionStorage) sessionStorage;
            sStorage.setTaskList(allTasks);
        } catch (ClassCastException exception) {
            logError(exception.getLocalizedMessage());
            assert false;
        }
    }

    //@author A0111889W
    /**
     * method for other components to log custom error messages. This is to
     * reduce coupling with EventLogger class
     * 
     * @param error error message to log in EventLogger
     */
    public synchronized static void logError(String error) {
        if (error == null) {
            return;
        }
        EventLogger.getInstance().logError(error);
    }

    //@author A0111889W
    /**
     * Close all storages. Run this before application closes.
     */
    public void closeStorages() {
        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.close();
        }
    }

    //@author A0111889W
    public synchronized boolean modifyTask(Task modifiedTask) {
        if (modifiedTask == null) {
            return false;
        }

        boolean isModified = false;
        
        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.modifyTask(modifiedTask);
        }

        isModified = true;
        return isModified;
    }

    //@author A0111889W
    public synchronized boolean addTask(Task addedTask) {
        if (addedTask == null) {
            return false;
        }

        boolean isAdded = false;

        for (IStorage individualStorage : _listOfStorages) {
            individualStorage.storeNewTask(addedTask);
        }

        isAdded = true;
        return isAdded;
    }

    //@author A0111889W
    public synchronized Task deleteTask(Task deletedTask) {
        if (deletedTask == null) {
            return null;
        }

        boolean isDeleted = false;

        if ("".equals(deletedTask.getGoogleId())) {
            for (IStorage individualStorage : _listOfStorages) {
                individualStorage.removeTask(deletedTask);
            }
        } else {
            if (isStorageInit()) { // All storages init including Google
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

    static void resetGoogleIdAndEtag(GoogleService googleService) {
        if (googleService != null) {
            switch (googleService) {
                case GOOGLE_CALENDAR :
                    resetCalendarItems();
                    break;
                case GOOGLE_TASKS :
                    resetTasksItems();
                    break;
                default:
                    break;
            }
        }
    }

    static void resetCalendarItems() {
        List<Task> allLocalTasks = getAllTasks();
        for (int i = 0; i < allLocalTasks.size(); i++) {
            Task currentTask = allLocalTasks.get(i);
            String taskType = currentTask.getType();

            if (Task.TASK_TIMED.equals(taskType)) {
                resetGoogleProps(currentTask);
            }
        }
    }

    static void resetGoogleProps(Task taskToReset) {
        if (taskToReset != null) {
            taskToReset.setGoogleId("");
            taskToReset.setETag("");
        }
    }

    static void resetTasksItems() {
        List<Task> allLocalTasks = getAllTasks();
        for (int i = 0; i < allLocalTasks.size(); i++) {
            Task currentTask = allLocalTasks.get(i);
            String taskType = currentTask.getType();

            if (Task.TASK_DEADLINE.equals(taskType)
                    || Task.TASK_FLOATING.equals(taskType)) {
                resetGoogleProps(currentTask);
            }
        }
    }

    public boolean logout() {
        boolean isRanLogout = false;

        if (isGoogleStorageInit()) {
            removeCloudStorage();
            GoogleController.setOnlineStatus(Status.OFFLINE);
            isRanLogout = true;
        }
        return isRanLogout;
    }

    public void removeCloudStorage() {
        if (isGStorageValid()) {
            GoogleStorage gStorage = (GoogleStorage) googleStorage;
            gStorage.close();
            _listOfStorages.remove(googleStorage);
            googleStorage = null;
        }
    }

    public synchronized static boolean sync() {
        boolean isSyncRunned = false;

        if (isStorageInit()) {
            if (isGStorageValid()) {
                GoogleStorage gStorage = (GoogleStorage) googleStorage;
                List<Task> allTasks = getAllTasks();
                if (allTasks != null) {
                    isSyncRunned = gStorage.sync(allTasks);
                }
            }
        } else if (isLocalChirpStorageInit()) {
            if (!isGoogleStorageInit()) {
                isSyncRunned = initCloudStorage();
            }
        }

        return isSyncRunned;
    }

    static synchronized void updateStorages(Task modifiedTask) {
        if (modifiedTask == null) {
            return;
        }

        if (isStorageInit()) {
            if ("".equals(modifiedTask.getGoogleId())) {
                for (IStorage individualStorage : _listOfStorages) {
                    individualStorage.removeTask(modifiedTask);
                }
            } else {
                updateFromAllExceptCloud(modifiedTask);
            }
            Logic.refresh(); // need to update GUI
        }
    }

    static void updateFromAllExceptCloud(Task modifiedTask) {
        if (modifiedTask == null) {
            return;
        }

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

    //@author A0111889W
    static synchronized void deleteFromStorage(Task deletedTask) {
        if (deletedTask == null) {
            return;
        }

        if (isLocalChirpStorageInit()) {
            if (deletedTask != null) {
                deleteFromAllExceptCloud(deletedTask);
            }
        }
    }

    //@author A0111840W
    static void deleteFromAllExceptCloud(Task deletedTask) {
        if (deletedTask == null) {
            return;
        }

        List<Task> allTasks = sessionStorage.getAllTasks();

        if (allTasks.contains(deletedTask)) {
            sessionStorage.removeTask(deletedTask);
            localStorage.removeTask(deletedTask);
            eventStorage.removeTask(deletedTask);
            Logic.refresh(); // need to update GUI
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

    static boolean isSessionStorageInit() {
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

    /**
     * This method is provided for Components to set up a local test XML store.
     * This is to aid testing, and it will ensure a fresh copy of storage
     * everytime it runs without affecting the usual "local.xml"
     * 
     * Method also updates the SessionStorage once JUnit Test XML is loaded
     */
    public void setUpJUnitTestXmlWriter() {
        if (isLocalStorageInit()) {
            if (localStorage instanceof LocalStorage) {
                LocalStorage lStorage = (LocalStorage) localStorage;
                lStorage.setUpJUnitTestXmlWriter();
                setAllTasks(localStorage.getAllTasks());
            }
        }
    }

}

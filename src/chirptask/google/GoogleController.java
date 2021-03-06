//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.List;

import chirptask.common.Constants;
import chirptask.logic.Logic;
import chirptask.storage.EventLogger;
import chirptask.storage.GoogleStorage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

/**
 * GoogleController is the main component that interacts with the Google
 * Services. The two main Google Services involved are: 1) Google Calendar 2)
 * Google Tasks The main controller of Google Calendar is the CalendarController
 * class. The main controller of Google Tasks is the TasksController class.
 * 
 * Therefore, GoogleController maintains a global instance of these two.
 * 
 * Aside from performing interactions with the two Google Services,
 * GoogleController is the main class that authenticates ChirpTask with Google
 * via OAuth2.0.
 * 
 * The interactions with the Google Services is possible only with a valid
 * Credential.
 */

public class GoogleController implements Runnable {
    public enum GoogleService {
        GOOGLE_CALENDAR, GOOGLE_TASKS;
    }
    
    public enum Status {
        ONLINE, OFFLINE, SYNC, SYNC_FAIL, LOGIN
    }
    
    /** Constant instance of the application name. */
    private static final String APPLICATION_NAME = "ChirpTask-GoogleIntegration/0.1";

    /** Constant instance of the directory to store the OAuth token. */
    private static final File DATA_STORE_DIR = new File(
            "credentials/google_oauth_credential");

    /** Constant instance of ConcurrentController. */
    private static final ConcurrentController CONCURRENT = new ConcurrentController();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the DataStoreFactory.
     * 
     * The best practice is to make it a single globally shared instance across
     * your application
     */
    static FileDataStoreFactory _dataStoreFactory = null;

    /** Global instance of the HTTP transport. */
    static HttpTransport _httpTransport = null;

    /** Global instance of the Credential. */
    private static Credential _credential = null;

    /** Global instance of the CalendarController. */
    private static CalendarController _calendarController = null;

    /** Global instance of the TasksController. */
    private static TasksController _tasksController = null;
    
    private final int timeToSleep = 5000;

    public GoogleController() {
        initializeLocalComponents();
    }

    private void initializeLocalComponents() {
        try {
            // initialize the transport
            _httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // initialize the data store factory
            _dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (NullPointerException nullPointerException) {
            assert false;
        } catch (GeneralSecurityException generalSecurityError) {
            // This error is thrown by
            // GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException ioError) {
            // This error can be thrown by both of newTrustedTransport(),
            // and new FileDataStoreFactory(File);
        }
    }

    public void login() {
        setOnlineStatus(Status.LOGIN);
        Thread initializeGoogleController = new Thread(this);
        initializeGoogleController.setDaemon(true);
        initializeGoogleController.start();
    }

    // Method below is for threading.
    /**
     * To make Google Login/Authentication run in the background. It allows the
     * program to continue running normally in the mean time.
     */
    public void run() {
        initializeRemoteComponents(); 
        while (isGoogleLoaded() == false) { //wait for google to load
            sleepThread();
        }
        GoogleStorage.hasBeenInitialized();
    }
    
    /**
     * Initialize the essential components to allow interaction with Google
     * Services - Google Calendar and Google Tasks.
     */
    private void initializeRemoteComponents() {
        initializeGoogleCredential();
        initializeCalendarController();
        initializeTasksController();
    }
    
    private void initializeGoogleCredential() {
        try {
            // initialize the credential component
            buildGoogleCredential();
        } catch (Exception allExceptions) {
            sleepThread();
            initializeGoogleCredential();
        }
    }
    
    private void buildGoogleCredential() throws IOException {
        _credential = GoogleAuthorizer.authorize();
        
        if (_credential == null) {
            throw new NullPointerException();
        }
    }
    
    private void sleepThread() {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException interruptedException) {
        }
    }
    
    private void initializeCalendarController() {
        try {
            // initialize the Calendar Controller
            buildCalendarController();
        } catch (Exception allExceptions) {
            sleepThread();
            initializeCalendarController();
        }
    }
    
    private void buildCalendarController() throws IOException {
        _calendarController = new CalendarController(_httpTransport,
                JSON_FACTORY, _credential, APPLICATION_NAME);
        
        if (_calendarController == null) {
            throw new NullPointerException();
        }
    }
    
    private void initializeTasksController() {
        try {
            // initialize the Tasks Controller
            buildTasksController();
        } catch (Exception allExceptions) {
            sleepThread();
            initializeTasksController();
        }
    }
    
    private void buildTasksController() throws IOException {
        _tasksController = new TasksController(_httpTransport,
                JSON_FACTORY, _credential, APPLICATION_NAME);
        
        if (_tasksController == null) {
            throw new NullPointerException();
        }
    }
    
    /**
     * Provides a checker if the required Google components loaded.
     * 
     * @return true if all required components has been loaded; false if at
     *         least one component has not been loaded.
     */
    public static boolean isGoogleLoaded() {
        boolean isLoaded = true;
        isLoaded = isLoaded && isHttpTransportLoaded();
        isLoaded = isLoaded && isDataStoreFactoryLoaded();
        isLoaded = isLoaded && isCredentialLoaded();
        isLoaded = isLoaded && isHttpTransportLoaded();
        isLoaded = isLoaded && isCalendarLoaded();
        isLoaded = isLoaded && isTasksLoaded();
        return isLoaded;
    }

    private static boolean isHttpTransportLoaded() {
        if (_httpTransport != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isDataStoreFactoryLoaded() {
        if (_dataStoreFactory != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isCredentialLoaded() {
        if (_credential != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isCalendarLoaded() {
        if (_calendarController != null) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isTasksLoaded() {
        if (_tasksController != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Methods below are made to be called by the GoogleStorage for the
     * GoogleIntegration component of ChirpTask.
     */
    // Called by GoogleStorage
    /**
     * add(Task) will perform the relevant addTask method depending on the
     * content of the chirptask.storage.Task object passed in. After the task
     * has been added to the relevant Google Service, it will return the Google
     * ID of the newly created task to update the entry in the local storage
     * (xml file).
     * 
     * @param taskToAdd The ChirpTask Task object to add
     */
    public void addTask(chirptask.storage.Task taskToAdd) {
        if (taskToAdd == null) {
            return;
        }
        
        if (isGoogleLoaded()) {
            ConcurrentAdd addTask = new ConcurrentAdd(taskToAdd, 
                    this, 
                    _tasksController, 
                    _calendarController);
            CONCURRENT.addToExecutor(addTask);
        }
    }

    public void modifyTask(chirptask.storage.Task taskToModify) {
        if (taskToModify == null) {
            return;
        }
        
        if (isGoogleLoaded()) {
            ConcurrentModify modifyTask = new ConcurrentModify(taskToModify,
                    _tasksController);
            CONCURRENT.addToExecutor(modifyTask);
        }
    }

    public void removeTask(chirptask.storage.Task taskToRemove) {
        if (taskToRemove == null) {
            return;
        }
        
        if (isGoogleLoaded()) {
            ConcurrentDelete deleteTask = new ConcurrentDelete(taskToRemove, 
                    _tasksController, 
                    _calendarController);
            CONCURRENT.addToExecutor(deleteTask);
        }
    }

    /**
     * We must ensure that the ExecutorService shutdown signal is sent
     * to prevent any memory leakage or wild process/thread in background
     */
    public void close() {
        CONCURRENT.close();
        try {
            CONCURRENT.awaitTermination();
        } catch (InterruptedException e) {
        }
    }

    // Methods below are general methods to perform other actions
    public boolean sync(List<chirptask.storage.Task> allTasks) {
        boolean isSyncRunned = false;
        if (isGoogleLoaded() && allTasks != null) {
            ConcurrentSync concurrentSync = new ConcurrentSync(allTasks, this, 
                    _calendarController, _tasksController);
            CONCURRENT.addToExecutor(concurrentSync);
            isSyncRunned = true;
        }
        return isSyncRunned;
    }
    
    static void resetGoogleIdAndEtag(GoogleService googleService) {
        if (googleService == null) {
            return;
        }
        
        GoogleStorage.resetGoogleIdAndEtag(googleService);
    }
    
    // Method(s) to aid checking for add/modify.
    /**
     * Checks if the specified task's Google ID exists in the client's Google
     * account.
     * 
     * Should be called before performing modification or deletion of the task.
     * 
     * @param googleId
     *            The Google ID of the task from Tasks or Calendar
     * @param taskType
     *            The type of ChirpTask (floating/timed/deadline)
     * @return true if it exists; false if it does not.
     * @throws UnknownHostException
     *             If the host machine cannot reach Google.
     * @throws IOException
     *             If there are other errors when sending the request.
     */
    static boolean isEntryExists(String googleId, String taskType)
            throws UnknownHostException, IOException {
        if (googleId == null || taskType == null) {
            return false;
        }
        
        boolean isExist = false;
        String googleListId = "";

        switch (taskType) {
        case chirptask.storage.Task.TASK_FLOATING :
        case chirptask.storage.Task.TASK_DEADLINE :
            googleListId = TasksController.getTaskListId();
            Task foundTask = TasksHandler.getTaskFromId(googleListId, googleId);
            
            if (foundTask != null) {
                isExist = true;
            }
            break;
        case chirptask.storage.Task.TASK_TIMED :
            googleListId = CalendarController.getCalendarId();
            Event foundEvent = CalendarHandler.getEventFromId(googleListId,
                    googleId);
            
            if (foundEvent != null) {
                isExist = true;
            }
            break;
        default :
            EventLogger.getInstance().logError(Constants.LOG_MESSAGE_UNEXPECTED);
            assert false;
            break;
        }

        return isExist;
    }
    
    /**
     * An interface for Google (or Other) Components to call,
     * to change and reflect the updated MainGui Online Status.
     * @param newStatus One of the statuses in the Status enum
     */
    public static void setOnlineStatus(Status newStatus) {
        if (newStatus != null) {
            switch (newStatus) {
            case ONLINE :
                Logic.setOnlineStatus(Constants.TITLE_ONLINE);
                break;
            case OFFLINE :
            Logic.setOnlineStatus(Constants.TITLE_OFFLINE);
            break;
            case SYNC :
                Logic.setOnlineStatus(Constants.TITLE_SYNCING);
                break;
            case SYNC_FAIL :
                Logic.setOnlineStatus(Constants.TITLE_SYNC_FAIL);
                break;
            case LOGIN :
                Logic.setOnlineStatus(Constants.TITLE_LOGGING_IN);
                break;
            default :
                EventLogger.getInstance().logError(Constants.LOG_MESSAGE_UNEXPECTED);
                assert false;
                break;
            }
        }
    }

}

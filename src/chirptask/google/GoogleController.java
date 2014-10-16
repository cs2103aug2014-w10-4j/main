//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Date;

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
    /** Constant instance of the application name. */
    private static final String APPLICATION_NAME = 
                                             "ChirpTask-GoogleIntegration/0.1";
    
    /** Constant instance of the directory to store the OAuth token. */
    private static final File DATA_STORE_DIR = new File(
                                        "credentials/google_oauth_credential");
    
    /** Constant instance of ConcurrentController. */
    private static final ConcurrentController CONCURRENT = 
                                                    new ConcurrentController();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the DataStoreFactory. 
     * 
     * The best practice is to make it a single globally shared instance
     *  across your application
     */
    static FileDataStoreFactory _dataStoreFactory;

    /** Global instance of the HTTP transport. */
    static HttpTransport _httpTransport;

    /** Global instance of the Credential. */
    private static Credential _credential;

    /** Global instance of the CalendarController. */
    private static CalendarController _calendarController;

    /** Global instance of the TasksController. */
    private static TasksController _tasksController;

    public GoogleController() {
        initializeLocalComponents();
    }
    
    private void initializeLocalComponents() {
        try {
            // initialize the transport
            _httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // initialize the data store factory
            _dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR); 
        } catch (GeneralSecurityException generalSecurityError) {
            // This error is thrown by
            // GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException ioError) {
            // This error can be thrown by both of newTrustedTransport(),
            // and new FileDataStoreFactory(File);
        }
    }

    /**
     * Initialize the essential components to allow interaction with Google
     * Services - Google Calendar and Google Tasks.
     */
    private void initializeRemoteComponents() {
        try {
            // initialize the credential component
            _credential = GoogleAuthorizer.authorize();
            // initialize the Calendar Controller
            _calendarController = new CalendarController(_httpTransport,
                    JSON_FACTORY, _credential, APPLICATION_NAME);
            // initialize the Tasks Controller
            _tasksController = new TasksController(_httpTransport,
                    JSON_FACTORY, _credential, APPLICATION_NAME);
        } catch (IOException ioError) {
            // This error can be thrown by authorize();
        } 
    }

    /**
     * Methods below are made to be called by the StorageHandler for the
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
    * @param taskToAdd
    * @return
    * @throws IOException
    */
    public void add(chirptask.storage.Task taskToAdd) throws 
                                                   UnknownHostException, 
                                                   IOException {
       if (isGoogleLoaded()) {
           ConcurrentAdd addTask = new ConcurrentAdd(taskToAdd);
           CONCURRENT.addToExecutor(addTask);
           
           CONCURRENT.close(); //Should be called when application exits to prevent leakage
       }
    }
   
    public void modifyTask(chirptask.storage.Task taskToModify) 
                                throws UnknownHostException, IOException {
        if (isGoogleLoaded()) {
            ConcurrentModify modifyTask = 
                    new ConcurrentModify(taskToModify);
            CONCURRENT.addToExecutor(modifyTask);

            CONCURRENT.close(); //Should be called when application exits to prevent leakage
        }
    }
    
    public void removeTask(chirptask.storage.Task taskToRemove) 
                                throws UnknownHostException, IOException {
        if (isGoogleLoaded()) {
            ConcurrentDelete deleteTask = 
                    new ConcurrentDelete(taskToRemove);
            CONCURRENT.addToExecutor(deleteTask);

            CONCURRENT.close(); //Should be called when application exits to prevent leakage
        }
    }
 

    

    /**
     * deletes a specific task in Google Tasks by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private static boolean deleteGoogleTask(String taskId) {
        boolean isDeleted = false;
        
        if (isGoogleLoaded()) {
            isDeleted = _tasksController.deleteTask(taskId);
        }
        
        return isDeleted;
    }
    
    /**
     * deletes a specific task in Google Calendar by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private static boolean deleteGoogleEvent(String taskId) {
        boolean isDeleted = false;
        if (isGoogleLoaded()) {
            isDeleted = _calendarController.deleteEvent(taskId);
        }
        return isDeleted;
    }

    /**
     * shows the specific task in Google Tasks retrieved by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private void showTask(String taskId) {
        if (isGoogleLoaded()) {
            try {
                _tasksController.showTask(taskId);
            } catch (IOException ioError) {

            }
        }
    }
    
    /**
     * shows all the available calendars in the authenticated account
     * 
     * @throws IOException
     */
    private void showCalendars() throws IOException {
        if (isGoogleLoaded()) {
            _calendarController.showCalendars();
        }
    }
    
    /**
     * shows all the tasks in the ChirpTask task list. All tasks include undone
     * tasks as well as completed tasks which are not cleared yet.
     * 
     * @throws IOException
     */
    private void showTasks() throws IOException {
        if (isGoogleLoaded()) {
            _tasksController.showTasks();
        }
    }

    /**
     * shows all the cleared tasks in the ChirpTask hidden task list. cleared
     * tasks are completed tasks that went through the clear operation.
     * 
     * @throws IOException
     */
    private void showHiddenTasks() throws IOException {
        if (isGoogleLoaded()) {
            _tasksController.showHiddenTasks();
        }
    }

    /**
     * shows all the tasks which are not done yet. tasks can have a due date
     * where applicable.
     * 
     * @throws IOException
     */
    private void showUndoneTasks() throws IOException {
        if (isGoogleLoaded()) {
            _tasksController.showUndoneTasks();
        }
    }

    // Called by ConcurrentAdd
    /**
     * adds a floating task with the specified task title.
     * 
     * @param taskTitle The floating task description
     * @return The reference to the created Google Task object
     * @throws UnknownHostException If the host machine cannot reach Google.
     * @throws IOException If there are other errors when sending the request.
     */
    static Task addFloatingTask(String taskTitle) throws 
                                                        UnknownHostException, 
                                                        IOException {
        Task addedTask = _tasksController.addTask(taskTitle);
        return addedTask;
    }

    /**
     * adds a deadline task with the specified task title and due date.
     * 
     * @param taskTitle The deadline task description
     * @param date The due date
     * @return The reference to the created Google Task object
     * @throws UnknownHostException If the host machine cannot reach Google.
     * @throws IOException If there are other errors when sending the request.
     */
    static Task addDeadlineTask(String taskTitle, Date date)
                                    throws UnknownHostException, IOException {
        Task addedTask = _tasksController.addTask(taskTitle, date);
        return addedTask;
    }
    
    static Event addTimedTask(String taskTitle, Date startTime, Date endTime) 
                                    throws UnknownHostException, IOException {
        Event addedEvent = _calendarController.addTimedTask(taskTitle, startTime, endTime);
        return addedEvent;
    }
    
    // Called by ConcurrentDelete
    static boolean deleteTask(chirptask.storage.Task taskToDelete) 
                                    throws UnknownHostException, IOException {
        boolean isDeleted = false;
        String googleId = taskToDelete.getGoogleId();
        String taskType = taskToDelete.getType();
        
        if (googleId == null || googleId == "") {
            isDeleted = false;
            return isDeleted;
        } else if (!isEntryExists(googleId, taskType)) {
            isDeleted = false;
            return isDeleted;
        }
        
        switch (taskType) {
        case "floating" :
        case "deadline" :
            isDeleted = deleteGoogleTask(googleId);
            break;
        case "timedtask" :
            isDeleted = deleteGoogleEvent(googleId);
            break;
        default :
            break;
        }
        
        return isDeleted;
    }
    
    // Called by ConcurrentModify 
    static Task toggleTasksDone(Task googleTask, chirptask.storage.Task toggleTask) {
        
        boolean isDone = toggleTask.isDone();
        Task toggledTask = _tasksController.toggleTaskDone(googleTask, isDone);
        
        if (toggledTask != null) {
            return toggledTask;
        } else {
            return null;
        }
    }
    
    static Task updateDueDate(Task taskToUpdate, 
                                chirptask.storage.Task updatedTask) {
        String taskType = updatedTask.getType();
        
        if (taskType.equals("deadline")) { //magic string, must solve this.
            Date newDueDate = updatedTask.getDate();
        
            Task updatedGoogleTask = 
                    _tasksController.updateDueDate(taskToUpdate, newDueDate);

            if (updatedGoogleTask != null) {
                return updatedGoogleTask;
            } else {
                return taskToUpdate;
            }
        } else { //Is a floating task
            return taskToUpdate;
        }
    }
    
    static Task updateTasksDescription(Task taskToUpdate, chirptask.storage.Task updatedTask) {
        
        String updatedDescription = updatedTask.getDescription();
        Task updatedGoogleTask = _tasksController.updateDescription(taskToUpdate, updatedDescription);
        
        if (updatedGoogleTask != null) {
            return updatedGoogleTask;
        } else {
            return null;
        }
    }
    
    //Code from here onwards are methods to aid checking.
    /**
     * Checks if the specified task's Google ID exists 
     * in the client's Google account.
     * 
     * Should be called before performing modification or deletion 
     * of the task.
     * 
     * @param googleId The Google ID of the task from Tasks or Calendar
     * @param taskType The type of ChirpTask (floating/timed/deadline)
     * @return true if it exists; false if it does not.
     * @throws UnknownHostException If the host machine cannot reach Google.
     * @throws IOException If there are other errors when sending the request.
     */
    static boolean isEntryExists(String googleId, String taskType) 
                                    throws UnknownHostException, IOException {
        boolean isExist = false;
        String googleListId = "";
        
        switch (taskType) {
        case "floating" :
        case "deadline" :
            googleListId = TasksController.getTaskListId();
            Task foundTask = TasksHandler.getTaskFromId(googleListId, googleId);
            if (foundTask != null) {
                isExist = true;
            }
            break;
        case "timedtask" :
            googleListId = CalendarController.getCalendarId();
            Event foundEvent = CalendarHandler.getEventFromId(googleListId, googleId);
            if (foundEvent != null) {
                isExist = true;
            }
            break;
        default :
            break;
        }
        
        return isExist;
    }
    

    /**
     * Provides a checker if the required Google components loaded.
     * @return true if all required components has been loaded;
     *          false if at least one component has not been loaded.
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

    //Method below is for threading.
    /**
     * To make Google Login/Authentication run in the background.
     * It allows the program to continue running normally in the mean time.
     */
    public void run() {
        initializeRemoteComponents();
        if (isGoogleLoaded()) {
            GoogleStorage.hasBeenInitialized();
        }
    }

}


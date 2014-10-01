//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Date;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Calendar;
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

public class GoogleController {
    private static final String APPLICATION_NAME = "ChirpTask-GoogleIntegration/0.1";

    private static final File DATA_STORE_DIR = new File(
            "credentials/google_oauth_credential");

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the DataStoreFactory. The best practice is to make it
     * a single globally shared instance across your application
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
        initializeComponents();
    }

    /**
     * Initialize the essential components to allow interaction with Google
     * Services - Google Calendar and Google Tasks.
     */
    private void initializeComponents() {
        try {
            // initialize the transport
            _httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // initialize the data store factory
            _dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // initialize the credential component
            _credential = GoogleAuthorizer.authorize();
            // initialize the Calendar Controller
            _calendarController = new CalendarController(_httpTransport,
                    JSON_FACTORY, _credential, APPLICATION_NAME);
            // initialize the Tasks Controller
            _tasksController = new TasksController(_httpTransport,
                    JSON_FACTORY, _credential, APPLICATION_NAME);
        } catch (GeneralSecurityException generalSecurityError) {
            // This error is thrown by GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException ioError) {
            // This error can be thrown by 
        } catch (Exception anyOtherErrors) {
            anyOtherErrors.printStackTrace();
        }
    }

    // test if the service is available and connected
    public static void main(String[] args) {
        GoogleController gController = new GoogleController();
        if (gController.isGoogleLoaded()) {
            try {
                /**
                 * Google Tasks
                 */
                // Test creation of task
                Task tempTask = gController.addFloatingTask("Hello World!");
                gController.showTask(tempTask.getId());

                // Test adding due date
                DateTime dueDate = DateTimeHandler.getDateTime("2014-09-29");
                tempTask = TasksHandler.addDueDate(tempTask, dueDate);
                tempTask = _tasksController.updateTask(tempTask);
                gController.showTask(tempTask.getId());

                // Test setting complete
                tempTask = TasksHandler.setCompleted(tempTask);
                tempTask = _tasksController.updateTask(tempTask);
                gController.showTask(tempTask.getId());

                // Show all tasks in list
                // gController.showTasks();

                // Show all hidden tasks in list
                // gController.showHiddenTasks();

                // Show all undone tasks in list
                gController.showUndoneTasks();

                // Clean up
                gController.deleteTask(tempTask.getId());

                /**
                 * Google Calendar
                 */
                gController.showCalendars();
            } catch (IOException ioError) {

            }
        } else { // TODO for Google not loaded

        }
    }

    /**
     * Methods below are made to be called by the StorageHandler for the
     * GoogleIntegration component of ChirpTask.
     */

    /**
     * shows all the available calendars in the authenticated account
     * 
     * @throws IOException
     */
    private void showCalendars() throws IOException {
        _calendarController.showCalendars();
    }

    /**
     * deletes a specific task in Google Tasks by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private void deleteTask(String taskId) {
        try {
            _tasksController.deleteTask(taskId);
        } catch (IOException ioError) {

        }
    }

    /**
     * shows the specific task in Google Tasks retrieved by its ID
     * 
     * @param taskId
     *            to be passed in, should read in from localStorage
     */
    private void showTask(String taskId) {
        try {
            _tasksController.showTask(taskId);
        } catch (IOException ioError) {

        }
    }

    /**
     * shows all the tasks in the ChirpTask task list. All tasks include undone
     * tasks as well as completed tasks which are not cleared yet.
     * 
     * @throws IOException
     */
    private void showTasks() throws IOException {
        _tasksController.showTasks();
    }

    /**
     * shows all the cleared tasks in the ChirpTask hidden task list. cleared
     * tasks are completed tasks that went through the clear operation.
     * 
     * @throws IOException
     */
    private void showHiddenTasks() throws IOException {
        _tasksController.showHiddenTasks();
    }

    /**
     * shows all the tasks which are not done yet. tasks can have a due date
     * where applicable.
     * 
     * @throws IOException
     */
    private void showUndoneTasks() throws IOException {
        _tasksController.showUndoneTasks();
    }

    /**
     * adds a floating task with the specified task title.
     * 
     * @param taskTitle
     *            is the floating task
     * @return the reference to the created Task object
     * @throws IOException
     */
    private Task addFloatingTask(String taskTitle) throws IOException,
            UnknownHostException {
        Task addedTask = _tasksController.addTask(taskTitle);
        return addedTask;
    }

    private Task addDeadlineTask(String taskTitle, Date date)
            throws IOException {
        Task addedTask = _tasksController.addTask(taskTitle, date);
        return addedTask;
    }

    // Task type will be changed to an enum, eg. TaskType.FLOATING
    // From the storage.Task object, we can retrieve the task,
    // due date, time range, etc. (if exists)
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
     public String add(chirptask.storage.Task taskToAdd) 
             throws IOException, UnknownHostException {
        // String type = _taskToAdd.getDescription(); //Should have
        // taskToAdd.getType();
        String type = "floating";
        String task = taskToAdd.getDescription();
        Date date = null;
        if (taskToAdd.getDate() != null) {
            date = taskToAdd.getDate();
        }
        Task addedTask = null;
        String googleId = null;

        switch (type) {
        case "floating":
            addedTask = addFloatingTask(taskToAdd.getDescription());
            googleId = addedTask.getId();
            break;
        case "deadline":
            addedTask = addDeadlineTask(taskToAdd.getDescription(),
                    taskToAdd.getDate());
            googleId = addedTask.getId();
            break;
        case "timed":
            break;
        default:
            break;
        }

        return googleId;
    }

    private boolean isGoogleLoaded() {
        boolean isLoaded = true;
        isLoaded = isLoaded && isHttpTransportLoaded();
        isLoaded = isLoaded && isDataStoreFactoryLoaded();
        isLoaded = isLoaded && isCredentialLoaded();
        isLoaded = isLoaded && isHttpTransportLoaded();
        isLoaded = isLoaded && isCalendarLoaded();
        isLoaded = isLoaded && isTasksLoaded();
        return isLoaded;
    }

    private boolean isHttpTransportLoaded() {
        if (_httpTransport != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isDataStoreFactoryLoaded() {
        if (_dataStoreFactory != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCredentialLoaded() {
        if (_credential != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isCalendarLoaded() {
        if (_calendarController != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isTasksLoaded() {
        if (_tasksController != null) {
            return true;
        } else {
            return false;
        }
    }

}

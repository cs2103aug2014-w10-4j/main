//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
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

    /**
     * Global instance of the DataStoreFactory. The best practice is to make it
     * a single globally shared instance across your application
     */
    static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the HTTP transport. */
    static HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the Credential. */
    private static Credential credential;

    /** Global instance of the CalendarController. */
    static CalendarController calendarController;

    /** Global instance of the TasksController. */
    static TasksController tasksController;

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
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // initialize the credential component
            credential = GoogleAuthorizer.authorize();
            // initialize the Calendar Controller
            calendarController = new CalendarController(httpTransport,
                    JSON_FACTORY, credential, APPLICATION_NAME);
            // initialize the Tasks Controller
            tasksController = new TasksController(httpTransport, JSON_FACTORY,
                    credential, APPLICATION_NAME);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // test if the service is available and connected
    public static void main(String[] args) {
        GoogleController _gController = new GoogleController();
        try {
            /**
             * Google Tasks
             */
            // Test creation of task
            Task tempTask = _gController.addFloatingTask("Hello World!");
            _gController.showTask(tempTask.getId());

            // Test adding due date
            DateTime _dueDate = DateTimeHandler.getDateTime("2014-09-29");
            tempTask = TasksHandler.addDueDate(tempTask, _dueDate);
            tempTask = tasksController.updateTask(tempTask);
            _gController.showTask(tempTask.getId());

            // Test setting complete
            tempTask = TasksHandler.setCompleted(tempTask);
            tempTask = tasksController.updateTask(tempTask);
            _gController.showTask(tempTask.getId());

            // Show all tasks in list
            // _gController.showTasks();

            // Show all hidden tasks in list
            // _gController.showHiddenTasks();

            // Show all undone tasks in list
            _gController.showUndoneTasks();

            // Clean up
            _gController.deleteTask(tempTask.getId());

            /**
             * Google Calendar
             */
            // _gController.showCalendars();
        } catch (IOException ioE) {

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
        calendarController.showCalendars();
    }

    /**
     * deletes a specific task in Google Tasks by its ID
     * 
     * @param _taskId
     *            to be passed in, should read in from localStorage
     */
    private void deleteTask(String _taskId) {
        try {
            tasksController.deleteTask(_taskId);
        } catch (IOException e) {

        }
    }

    /**
     * shows the specific task in Google Tasks retrieved by its ID
     * 
     * @param _taskId
     *            to be passed in, should read in from localStorage
     */
    private void showTask(String _taskId) {
        try {
            tasksController.showTask(_taskId);
        } catch (IOException e) {

        }
    }

    /**
     * shows all the tasks in the ChirpTask task list. All tasks include undone
     * tasks as well as completed tasks which are not cleared yet.
     * 
     * @throws IOException
     */
    private void showTasks() throws IOException {
        tasksController.showTasks();
    }

    /**
     * shows all the cleared tasks in the ChirpTask hidden task list. cleared
     * tasks are completed tasks that went through the clear operation.
     * 
     * @throws IOException
     */
    private void showHiddenTasks() throws IOException {
        tasksController.showHiddenTasks();
    }

    /**
     * shows all the tasks which are not done yet. tasks can have a due date
     * where applicable.
     * 
     * @throws IOException
     */
    private void showUndoneTasks() throws IOException {
        tasksController.showUndoneTasks();
    }

    /**
     * adds a floating task with the specified task title.
     * 
     * @param _taskTitle
     *            is the floating task
     * @return the reference to the created Task object
     * @throws IOException
     */
    private Task addFloatingTask(String _taskTitle) throws IOException {
        Task _addedTask = tasksController.addTask(_taskTitle);
        return _addedTask;
    }
    
    private Task addDeadlineTask(String _taskTitle, Date _date) 
            throws IOException {
        Task _addedTask = tasksController.addTask(_taskTitle, _date);
        return _addedTask;
    }
    
    //Task type will be changed to an enum, eg. TaskType.FLOATING
    //From the storage.Task object, we can retrieve the task,
    //due date, time range, etc. (if exists)
    /**
     * add(Task) will perform the relevant addTask method depending on the 
     * content of the chirptask.storage.Task object passed in.
     * After the task has been added to the relevant Google Service, it will
     * return the Google ID of the newly created task to update the entry 
     * in the local storage (xml file).
     * @param _taskToAdd
     * @return
     * @throws IOException
     */
    private String add(chirptask.storage.Task _taskToAdd)
            throws IOException {
        //String _type = _taskToAdd.getDescription(); //Should have _taskToAdd.getType();
        String _type = "floating";
        String _task = _taskToAdd.getDescription();
        Date _date = null;
        if (_taskToAdd.getDate() != null) {
            _date = _taskToAdd.getDate();
        }
        Task _addedTask = null;
        String _googleId = null;
        
        switch (_type) {
        case "floating" :
            _addedTask = addFloatingTask(_taskToAdd.getDescription());
            _googleId = _addedTask.getId();
            break;
        case "deadline" :
            _addedTask = addDeadlineTask(_taskToAdd.getDescription(), _taskToAdd.getDate());
            _googleId = _addedTask.getId();
            break;
        case "timed" :
            break;
        default :
            break;
        }
        
        return _googleId;
    }

}

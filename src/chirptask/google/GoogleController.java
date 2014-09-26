package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.tasks.model.Task;

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

    private void initializeComponents() {
        try {
            // initialize the transport
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // initialize the credential component
            credential = GoogleAuthorizer.authorize();
            // initialize the Calendar Controller
            calendarController = new CalendarController(
                    httpTransport, JSON_FACTORY, credential, APPLICATION_NAME);
            // initialize the Tasks Controller
            tasksController = new TasksController(
                    httpTransport, JSON_FACTORY, credential, APPLICATION_NAME);
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
        try {
            GoogleController _gController = new GoogleController();
            /**
             * Google Tasks
             */
            //_gController.addTask("Hello World!");
            _gController.showTasks();
            //_gController.showTask("MDAyMjI2NjE3NTcxMjkxMDA0ODY6MTkxMTc4NDkzMjoyNTYzNjk0MDk");
            
            /**
             * Google Calendar
             */
            //_gController.showCalendars();
        } catch (IOException ioE) {
            
        }
    }
    
    private void showCalendars() throws IOException {
        calendarController.showCalendars();
    }
    
    private void showTask(String _taskId) throws IOException {
        tasksController.showTask(_taskId);
    }
    
    private void showTasks() throws IOException {
        tasksController.showTasks();
    }
    
    private void addTask(String taskTitle) throws IOException {
        tasksController.addTask(taskTitle);
    }

}

package chirptask.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.Tasks.Tasklists;

import com.google.api.services.tasks.model.*;

public class TasksController {
    /** Global instance of the Google Tasks Service Client. */
    private static com.google.api.services.tasks.Tasks tasksClient;
    
    private final String DEFAULT_TASKLIST = "@default";

    TasksController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeTasksController(httpTransport, jsonFactory, credential,
                applicationName);
    }

    private void initializeTasksController(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }
    
    
    
    private TaskLists retrieveAllTaskLists() throws IOException {
        TaskLists allLists = tasksClient.tasklists().list().execute();
        return allLists;
    }
   
    public void showTasks() throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = tasksClient.tasks()
                .list(DEFAULT_TASKLIST).setFields("items/title").execute();
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }
    
    public void addTask(String taskTitle) throws IOException {
        Task _newTask = createTask(taskTitle);
        insertTask(_newTask);
    }
    
    private void insertTask(Task task) throws IOException {
        tasksClient.tasks().insert(DEFAULT_TASKLIST, task).execute();
    }
    
    //private void updateTask(Task)
    
    private Task createTask(String taskTitle) {
        Task _newTask = new Task();
        _newTask.setTitle(taskTitle);
        return _newTask;
    }
    
    //public void deleteTask()
    
}

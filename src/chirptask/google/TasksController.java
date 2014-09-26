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
   
    public void showTasks() throws IOException {
        com.google.api.services.tasks.model.Tasks tasks = tasksClient.tasks()
                .list("@default").setFields("items/title").execute();
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }
    
}

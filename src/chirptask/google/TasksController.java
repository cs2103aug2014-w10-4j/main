//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;


/**
 * TasksController is the main controller that interacts with Google Tasks. It
 * uses the Google Tasks v1 API to do such operations. TasksController has 2
 * helper classes, TasksViewer and TasksHandler.
 * 
 * TasksViewer is a helper class that is often called to help perform retrieval
 * of the Task's statuses/information etc.
 * 
 * TasksHandler is a helper class that is often called to help perform the API
 * calls such as insertTask, clear, delete, update.
 */
public class TasksController {
    /** Constant name of the default task list. */
    private static final String DEFAULT_TASKLIST = "@default";

    /**
     * Global instance of the Google Tasks Service Client. 
     * Tasks tasksClient; is the main object connected to the Google Tasks API.
     */
    static com.google.api.services.tasks.Tasks _tasksClient;

    /** Constructor */
    TasksController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeTasksClient(httpTransport, jsonFactory, credential,
                applicationName);
    }

    private void initializeTasksClient(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        _tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }

    static String getTaskListId() {
        return DEFAULT_TASKLIST;
    }


    Task getTask(String id) throws UnknownHostException, IOException {
        Task result = TasksHandler.getTaskFromId(DEFAULT_TASKLIST, id);
        return result;
    }

    Task addTask(String taskTitle) throws UnknownHostException, IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    Task addTask(String taskTitle, Date dueDate)
            throws UnknownHostException, IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        newTask = TasksHandler.setDueDate(newTask, dueDate);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    Task addTask(String taskTitle, String notes, Date dueDate)
            throws UnknownHostException, IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        newTask = TasksHandler.setNotes(newTask, notes);
        newTask = TasksHandler.setDueDate(newTask, dueDate);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    private Task insertTask(Task task) 
            throws UnknownHostException, IOException {
        Task result = TasksHandler.insertTaskToList(DEFAULT_TASKLIST, task);
        return result;
    }
    
    Task toggleTaskDone(Task taskToToggle, boolean isDone) {
        Task toggledTask = taskToToggle;
        if (isDone) {
            toggledTask = TasksHandler.setCompleted(taskToToggle);
        } else {
            toggledTask = TasksHandler.setNotCompleted(taskToToggle);
        }
        return toggledTask;
    }

    boolean deleteTask(String taskId) {
        boolean isDeleted = false;
        isDeleted = TasksHandler.deleteTaskWithId(DEFAULT_TASKLIST, taskId);
        return isDeleted;
    }

    Tasks getTasks() throws UnknownHostException, IOException {
        Tasks tasks = TasksHandler.getTasksFromId(DEFAULT_TASKLIST);
        return tasks;
    }

    Task updateDescription(Task taskToUpdate, String description) {
        Task updatedTask = TasksHandler.setTitle(taskToUpdate, description);
        return updatedTask;
    }
    
    Task updateDueDate(Task taskToUpdate, Date dueDate) {
        Task updatedTask = TasksHandler.setDueDate(taskToUpdate, dueDate);
        return updatedTask;
    }
    
    static Task updateTask(Task updatedTask) 
                            throws UnknownHostException, IOException {
        updatedTask = TasksHandler.updateTask(DEFAULT_TASKLIST,
                updatedTask.getId(), updatedTask);
        return updatedTask;
    }

}

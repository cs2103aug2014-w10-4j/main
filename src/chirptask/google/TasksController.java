//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.tasks.Tasks.Tasklists;
import com.google.api.services.tasks.model.*;

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
    /** Constant name of the task list. */
    private final String DEFAULT_TASKLIST = "ChirpTaskv0.1";

    /** Global instance of the TasksId file. */
    private static final File TASKS_ID_STORE_FILE = new File(
            "credentials/googletasks/tasklistid.txt");

    /**
     * Global instance of the Google Tasks Service Client. 
     * Tasks tasksClient; is the main object connected to the Google Tasks API.
     */
    static com.google.api.services.tasks.Tasks _tasksClient;

    /** Global instance of the working Google TaskList */
    private static TaskList _workingTaskList;

    /** Global instance of the working Google TaskList ID */
    private static String _taskListId;

    /** Constructor */
    TasksController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeHostFiles();
        initializeTasksClient(httpTransport, jsonFactory, credential,
                applicationName);
        initializeWorkingTaskList();
        TasksViewer.displayTitle(_workingTaskList); // For testing
    }

    private void initializeHostFiles() {
        try {
            TASKS_ID_STORE_FILE.getParentFile().mkdirs();
            TASKS_ID_STORE_FILE.createNewFile();
        } catch (IOException e) {

        }
    }

    private void initializeTasksClient(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        _tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }

    private void initializeWorkingTaskList() {
        String taskListId = retrieveId();
        TaskList currentTaskList = retrieveTaskList(taskListId);
        setWorkingTaskList(currentTaskList);
    }

    private String retrieveId() {
        String workingListId = retrieveIdFromFile();
        setTaskListId(workingListId);
        return workingListId;
    }

    String getTaskListId() {
        return _taskListId;
    }

    private String retrieveIdFromFile() {
        String retrievedId = IdHandler.getIdFromFile(TASKS_ID_STORE_FILE);
        return retrievedId;
    }

    private void setTaskListId(String newId) {
        _taskListId = newId;
    }

    private TaskList retrieveTaskList(String taskListId) {
        if (taskListId == null) { // If null ID, assume fresh install/run
            TaskList newTaskList = null;
            try {
                newTaskList = createTaskList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newTaskList;
        } else {
            try {
                TaskList foundTaskList = getTaskListById(taskListId);
                if (TasksHandler.isNull(foundTaskList)) { // TaskList not found
                    foundTaskList = createTaskList();
                }
                return foundTaskList;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void setWorkingTaskList(TaskList taskList) {
        _workingTaskList = taskList;
    }

    private TaskList createTaskList() throws IOException {
        TaskList newTaskList = newTaskList(DEFAULT_TASKLIST);
        String id = newTaskList.getId();
        setTaskListId(id);
        IdHandler.saveIdToFile(TASKS_ID_STORE_FILE, id);
        return newTaskList;
    }

    private TaskList newTaskList(String listName) throws IOException {
        TaskList newTaskList = TasksHandler.createTaskList(listName);
        TaskList insertList = TasksHandler.insertTaskList(newTaskList);
        return insertList;
    }

    private TaskList getTaskListById(String taskListId) throws IOException {
        TaskList foundTaskList = null;

        try {
            foundTaskList = TasksHandler.getTaskListFromId(taskListId);
        } catch (GoogleJsonResponseException e) {
            foundTaskList = createTaskList();
        }

        return foundTaskList;
    }

    void showTask(String id) throws IOException {
        Task result = TasksHandler.getTaskFromId(_taskListId, id);
        TasksViewer.display(result);
    }

    public Task addTask(String taskTitle) throws IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    public Task addTask(String taskTitle, Date dueDate)
            throws IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        DateTime dueDateTime = DateTimeHandler.getDateTime(dueDate);
        newTask = TasksHandler.addDueDate(newTask, dueDateTime);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    public Task addTask(String taskTitle, String notes, Date dueDate)
            throws IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        newTask = TasksHandler.addNotes(newTask, notes);
        DateTime dueDateTime = DateTimeHandler.getDateTime(dueDate);
        newTask = TasksHandler.addDueDate(newTask, dueDateTime);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    private Task insertTask(Task task) throws IOException {
        Task result = TasksHandler.insertTaskToList(_taskListId, task);
        return result;
    }

    public void deleteTask(String taskId) throws IOException {
        TasksHandler.deleteTaskWithId(_taskListId, taskId);
    }

    public void showTasks() throws IOException {
        Tasks tasks = TasksHandler.getTasksFromId(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }

    public void showHiddenTasks() throws IOException {
        Tasks tasks = TasksHandler.getHiddenTasks(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }

    public void showUndoneTasks() throws IOException {
        Tasks tasks = TasksHandler.getUndoneTasks(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }

    public Task updateTask(Task updatedTask) throws IOException {
        updatedTask = TasksHandler.updateTask(_taskListId,
                updatedTask.getId(), updatedTask);
        return updatedTask;
    }

}

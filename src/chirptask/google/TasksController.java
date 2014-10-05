//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;
import com.google.api.services.tasks.model.TaskList;


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
            "credentials/googletasks/TaskListID.txt");

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
        setTaskListId(workingListId); //May have to handle if workingListId is null
        return workingListId;
    }

    static String getTaskListId() {
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
            } catch (UnknownHostException unknownHost) {
                // No internet
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * To avoid null pointers, if a null taskList is given, 
     * set working TaskList to be null.
     * @param taskList
     */
    private void setWorkingTaskList(TaskList taskList) {
        if (taskList == null) {
            _workingTaskList = null;
        } else {
            _workingTaskList = taskList;
        }
    }

    private TaskList createTaskList() throws IOException {
        TaskList newTaskList = newTaskList(DEFAULT_TASKLIST);
        String id = newTaskList.getId();
        setTaskListId(id);
        IdHandler.saveIdToFile(TASKS_ID_STORE_FILE, id);
        return newTaskList;
    }

    private TaskList newTaskList(String listName) 
            throws IOException, UnknownHostException {
        TaskList newTaskList = TasksHandler.createTaskList(listName);
        TaskList insertList = TasksHandler.insertTaskList(newTaskList);
        return insertList;
    }

    private TaskList getTaskListById(String taskListId) throws UnknownHostException, IOException {
        TaskList foundTaskList = null;

        try {
            foundTaskList = TasksHandler.getTaskListFromId(taskListId);
        } catch (GoogleJsonResponseException gJsonResponseError) {
            foundTaskList = createTaskList();
        } catch (UnknownHostException unknownHost) {
            foundTaskList = null;
        }

        return foundTaskList;
    }

    void showTask(String id) throws UnknownHostException, IOException {
        Task result = TasksHandler.getTaskFromId(_taskListId, id);
        TasksViewer.display(result);
    }

    Task addTask(String taskTitle) throws UnknownHostException, IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    Task addTask(String taskTitle, Date dueDate)
            throws IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        newTask = TasksHandler.setDueDate(newTask, dueDate);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    Task addTask(String taskTitle, String notes, Date dueDate)
            throws IOException {
        Task newTask = TasksHandler.createTask(taskTitle);
        newTask = TasksHandler.setNotes(newTask, notes);
        newTask = TasksHandler.setDueDate(newTask, dueDate);
        Task addedTask = insertTask(newTask);
        return addedTask;
    }

    private Task insertTask(Task task) 
            throws IOException, UnknownHostException {
        Task result = TasksHandler.insertTaskToList(_taskListId, task);
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

    void deleteTask(String taskId) {
        TasksHandler.deleteTaskWithId(_taskListId, taskId);
    }

    void showTasks() throws UnknownHostException, IOException {
        Tasks tasks = TasksHandler.getTasksFromId(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }

    void showHiddenTasks() throws UnknownHostException, IOException {
        Tasks tasks = TasksHandler.getHiddenTasks(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
    }

    void showUndoneTasks() throws UnknownHostException, IOException {
        Tasks tasks = TasksHandler.getUndoneTasks(_taskListId);
        TasksViewer.header("Show All Tasks");
        TasksViewer.display(tasks);
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
        updatedTask = TasksHandler.updateTask(_taskListId,
                updatedTask.getId(), updatedTask);
        return updatedTask;
    }

}

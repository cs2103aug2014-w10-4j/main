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
    private static final File TASKSID_STORE_FILE = new File(
            "credentials/googletasks/tasklistid.txt");

    /**
     * Global instance of the Google Tasks Service Client. 
     * Tasks tasksClient; is the main object connected to the Google Tasks API.
     */
    static com.google.api.services.tasks.Tasks tasksClient;

    /** Global instance of the working Google TaskList */
    private static TaskList workingTaskList;

    /** Global instance of the working Google TaskList ID */
    private static String _taskListId;

    /** Constructor */
    TasksController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeHostFiles();
        initializeTasksController(httpTransport, jsonFactory, credential,
                applicationName);
        initializeWorkingTaskList();
        TasksViewer.displayTitle(workingTaskList); // For testing
    }

    private void initializeHostFiles() {
        try {
            TASKSID_STORE_FILE.getParentFile().mkdirs();
            TASKSID_STORE_FILE.createNewFile();
        } catch (IOException e) {

        }
    }

    private void initializeTasksController(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }

    private void initializeWorkingTaskList() {
        String _taskListId = retrieveId();
        TaskList _currentTaskList = retrieveTaskList(_taskListId);
        setWorkingTaskList(_currentTaskList);
    }

    private String retrieveId() {
        String _workingListId = retrieveIdFromFile();
        setTaskListId(_workingListId);
        return _workingListId;
    }

    String getTaskListId() {
        return _taskListId;
    }

    private String retrieveIdFromFile() {
        String _retrievedId = IdHandler.getIdFromFile(TASKSID_STORE_FILE);
        return _retrievedId;
    }

    private void setTaskListId(String _newId) {
        _taskListId = _newId;
    }

    private TaskList retrieveTaskList(String _taskListId) {
        if (_taskListId == null) { // If null ID, assume fresh install/run
            TaskList _newTaskList = null;
            try {
                _newTaskList = createTaskList();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return _newTaskList;
        } else {
            try {
                TaskList _foundTaskList = getTaskListById(_taskListId);
                if (TasksHandler.isNull(_foundTaskList)) { // TaskList not found
                    _foundTaskList = createTaskList();
                }
                return _foundTaskList;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void setWorkingTaskList(TaskList _taskList) {
        workingTaskList = _taskList;
    }

    private TaskList createTaskList() throws IOException {
        TaskList _newTaskList = newTaskList(DEFAULT_TASKLIST);
        String _id = _newTaskList.getId();
        setTaskListId(_id);
        IdHandler.saveIdToFile(TASKSID_STORE_FILE, _id);
        return _newTaskList;
    }

    private TaskList newTaskList(String _listName) throws IOException {
        TaskList _newTaskList = TasksHandler.createTaskList(_listName);
        TaskList _insertList = TasksHandler.insertTaskList(_newTaskList);
        return _insertList;
    }

    private TaskList getTaskListById(String _taskListId) throws IOException {
        TaskList _foundTaskList = null;

        try {
            _foundTaskList = TasksHandler.getTaskListFromId(_taskListId);
        } catch (GoogleJsonResponseException e) {
            _foundTaskList = createTaskList();
        }

        return _foundTaskList;
    }

    void showTask(String _id) throws IOException {
        Task _result = TasksHandler.getTaskFromId(_taskListId, _id);
        TasksViewer.display(_result);
    }

    public Task addTask(String _taskTitle) throws IOException {
        Task _newTask = TasksHandler.createTask(_taskTitle);
        Task _addedTask = insertTask(_newTask);
        return _addedTask;
    }

    public Task addTask(String _taskTitle, Date _dueDate)
            throws IOException {
        Task _newTask = TasksHandler.createTask(_taskTitle);
        DateTime _dueDateTime = DateTimeHandler.getDateTime(_dueDate);
        _newTask = TasksHandler.addDueDate(_newTask, _dueDateTime);
        Task _addedTask = insertTask(_newTask);
        return _addedTask;
    }

    public Task addTask(String _taskTitle, String _notes, Date _dueDate)
            throws IOException {
        Task _newTask = TasksHandler.createTask(_taskTitle);
        _newTask = TasksHandler.addNotes(_newTask, _notes);
        DateTime _dueDateTime = DateTimeHandler.getDateTime(_dueDate);
        _newTask = TasksHandler.addDueDate(_newTask, _dueDateTime);
        Task _addedTask = insertTask(_newTask);
        return _addedTask;
    }

    private Task insertTask(Task _task) throws IOException {
        Task _result = TasksHandler.insertTaskToList(_taskListId, _task);
        return _result;
    }

    public void deleteTask(String _taskId) throws IOException {
        TasksHandler.deleteTaskWithId(_taskListId, _taskId);
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

    public Task updateTask(Task _updatedTask) throws IOException {
        _updatedTask = TasksHandler.updateTask(_taskListId,
                _updatedTask.getId(), _updatedTask);
        return _updatedTask;
    }

}

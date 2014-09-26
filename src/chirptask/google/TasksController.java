package chirptask.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.Tasks.Tasklists;
import com.google.api.services.tasks.model.*;

public class TasksController {
    /** Constant name of the task list. */
    private final String DEFAULT_TASKLIST = "ChirpTaskv0.1";
    
    private final String TASKLIST_ID = "MDAyMjI2NjE3NTcxMjkxMDA0ODY6MTQ0NDgxNjA6MA";
    
    /** Global instance of the Google Tasks Service Client. */
    private static com.google.api.services.tasks.Tasks tasksClient;
    
    /** Global instance of the working Google TaskList */
    private static TaskList workingTaskList;
    
    /** Constructor */
    TasksController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeTasksController(httpTransport, jsonFactory, credential,
                applicationName);
        initializeWorkingTaskList();
        showTaskListTitle();
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
        String workingListId = TASKLIST_ID; // Should be getting this ID from the host.
        return workingListId;
    }
    
    private TaskList retrieveTaskList(String taskListId) {
        if (taskListId == null) {   // If null ID, assume fresh install of ChirpTask
            TaskList _newTaskList = null;
            try { 
                _newTaskList = createTaskList(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
            return _newTaskList;
        } else {
            try {
                TaskList _foundTaskList = getTaskListById(taskListId);
                if (_foundTaskList == null) { //TaskList not found in account
                    _foundTaskList = createTaskList();
                } 
                return _foundTaskList;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private void setWorkingTaskList(TaskList taskList) {
        workingTaskList = taskList;
    }
    
    private TaskList createTaskList() throws IOException {
        TaskList _newTaskList = newTaskList(DEFAULT_TASKLIST);
        /** 
         * Future implementation to store this TaskList ID on host.
         * storeTaskListIdToHost(_newTaskList.getId());
         */
        return _newTaskList;
    }
    
    private TaskList newTaskList(String listName) throws IOException {
        TaskList _newTaskList = new TaskList();
        _newTaskList.setTitle(listName);
        return tasksClient.tasklists().insert(_newTaskList).execute();
    }
    
    private TaskList getTaskListById(String taskListId) throws IOException {
        TaskList _foundTaskList = tasksClient.tasklists().get(taskListId).execute();
        return _foundTaskList;
    }

    public void showTaskListTitle() {
        System.out.println(workingTaskList.getTitle());
    }
    
    public void showTaskListDetails(TaskList taskList) {
        TasksViewer.display(taskList);
    }
    
    
    private TaskLists retrieveAllTaskLists() throws IOException {
        TaskLists allLists = tasksClient.tasklists().list().execute();
        return allLists;
    }
    
    private void iterateTaskLists() throws IOException {
        TaskLists allLists = retrieveAllTaskLists();
        
        for (TaskList currentList : allLists.getItems()) {
              
        }
    }
    
    private String retrieveTaskListId() {
        String _taskId = workingTaskList.getId();
        return _taskId;
    }
    
    private String retrieveTaskListTitle() {
        String _taskTitle = workingTaskList.getTitle();
        return _taskTitle;
    }
   
    private DateTime retrieveLastModifiedTime() {
        DateTime _lastModifiedTimestamp = workingTaskList.getUpdated();
        return _lastModifiedTimestamp;
    }
    
    public void showTasks() throws IOException {
        iterateTaskLists();
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

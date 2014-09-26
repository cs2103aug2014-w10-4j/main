package chirptask.google;

import java.io.IOException;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.Tasks;

class TasksHandler {
    static boolean isNull(TaskList _taskList) {
        if (_taskList == null) {
            return true;
        } else {
            return false;
        }
    }

    static Task createTask(String floatingTask) {
        Task _newTask = new Task();
        _newTask.setTitle(floatingTask);
        return _newTask;
    }
    
    static TaskList createTaskList(String listName) {
        TaskList _newTaskList = new TaskList();
        _newTaskList.setTitle(listName);
        return _newTaskList;
    }
    
    static void deleteTaskWithId(String _taskListId, String _taskId)
            throws IOException {
        TasksController.tasksClient.tasks()
        .delete(_taskListId, _taskId).execute();
    }
    
    static Task getTaskFromId(String _taskListId, String _id) throws IOException {
        Task _retrieveTask = 
                TasksController.tasksClient.tasks()
                .get(_taskListId, _id).execute();
        return _retrieveTask;
    }
    
    static Tasks getTasksFromId(String _taskListId) throws IOException {
        Tasks _retrieveTask = 
                TasksController.tasksClient.tasks()
                .list(_taskListId).execute();
        return _retrieveTask;
    }
    
    static TaskList getTaskListFromId(String _taskListId) throws IOException {
        TaskList _retrieveTaskList = 
                TasksController.tasksClient.tasklists()
                .get(_taskListId).execute();
        return _retrieveTaskList;
    }
    
    static TaskList insertTaskList(TaskList _newTaskList) throws IOException {
        TaskList _insertList = 
                TasksController.tasksClient.tasklists()
                .insert(_newTaskList).execute();
        return _insertList;
    }
    
    static Task insertTaskToList(String _taskListId, Task _taskToInsert) throws IOException {
        Task _insertTask = 
                TasksController.tasksClient.tasks()
                .insert(_taskListId, _taskToInsert).execute();
        return _insertTask;
    }
}

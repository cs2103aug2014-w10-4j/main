//@author A0111840W
package chirptask.google;

import java.io.IOException;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.Tasks;

/**
 * TasksHandler provides static methods to separate methods, that perform
 * GoogleTasks operations, from the TasksController class.
 * 
 * This allows a global instance of the mentioned functions as well as provide 
 * easier maintenance of the TasksController class.
 */
class TasksHandler {
	static boolean isNull(TaskList _taskList) {
		if (_taskList == null) {
			return true;
		} else {
			return false;
		}
	}
	
	static boolean isNull(Task _task) {
        if (_task == null) {
            return true;
        } else {
            return false;
        }
    }

	static Task createTask(String _floatingTask) {
		Task _newTask = new Task();
		_newTask.setTitle(_floatingTask);
		return _newTask;
	}

	static Task addNotes(Task _taskToEdit, String _notes) {
		Task _editedTask = _taskToEdit.setNotes(_notes);
		return _editedTask;
	}

	static Task addDueDate(Task _taskToEdit, DateTime _dueDate) {
		Task _editedTask = _taskToEdit.setDue(_dueDate);
		return _editedTask;
	}

	static Task setCompleted(Task _taskToEdit) {
		Task _editedTask = _taskToEdit.setStatus("completed");
		return _editedTask;
	}

	static Task setNotCompleted(Task _taskToEdit) {
		Task _editedTask = _taskToEdit.setStatus("needsAction");
		_editedTask = _editedTask.setCompleted(null);
		return _editedTask;
	}

	static void clearCompletedTasks(String _taskListId) throws IOException {
		TasksController._tasksClient.tasks().clear(_taskListId).execute();
	}

	static TaskList createTaskList(String listName) {
		TaskList _newTaskList = new TaskList();
		_newTaskList.setTitle(listName);
		return _newTaskList;
	}

	static void deleteTaskWithId(String _taskListId, String _taskId)
			throws IOException {
		TasksController._tasksClient.tasks().delete(_taskListId, _taskId)
				.execute();
	}

	static Task getTaskFromId(String _taskListId, String _id)
			throws IOException {
		Task _retrieveTask = TasksController._tasksClient.tasks()
				.get(_taskListId, _id).execute();
		return _retrieveTask;
	}

	static Tasks getTasksFromId(String _taskListId) throws IOException {
		Tasks _retrieveTasks = TasksController._tasksClient.tasks()
				.list(_taskListId).execute();
		return _retrieveTasks;
	}

	static Tasks getHiddenTasks(String _taskListId) throws IOException {
		Tasks _retrieveTasks = TasksController._tasksClient.tasks()
				.list(_taskListId).set("showHidden", true).execute();
		return _retrieveTasks;
	}

	static Tasks getUndoneTasks(String _taskListId) throws IOException {
		Tasks _retrieveTasks = TasksController._tasksClient.tasks()
				.list(_taskListId).set("showCompleted", false).execute();
		return _retrieveTasks;
	}

	static TaskList getTaskListFromId(String _taskListId) throws IOException {
		TaskList _retrieveTaskList = TasksController._tasksClient.tasklists()
				.get(_taskListId).execute();
		return _retrieveTaskList;
	}

	static TaskList insertTaskList(TaskList _newTaskList) throws IOException {
		TaskList _insertList = TasksController._tasksClient.tasklists()
				.insert(_newTaskList).execute();
		return _insertList;
	}

	static Task insertTaskToList(String _taskListId, Task _taskToInsert)
			throws IOException {
		Task _insertTask = TasksController._tasksClient.tasks()
				.insert(_taskListId, _taskToInsert).execute();
		return _insertTask;
	}

	static Task updateTask(String _taskListId, String _taskId, Task _updatedTask)
			throws IOException {
		_updatedTask = TasksController._tasksClient.tasks()
				.update(_taskListId, _taskId, _updatedTask).execute();
		return _updatedTask;
	}
}

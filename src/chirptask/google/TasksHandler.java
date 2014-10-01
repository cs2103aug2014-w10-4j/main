//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;

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
	static boolean isNull(TaskList taskList) {
		if (taskList == null) {
			return true;
		} else {
			return false;
		}
	}
	
	static boolean isNull(Task task) {
        if (task == null) {
            return true;
        } else {
            return false;
        }
    }

	static Task createTask(String floatingTask) {
		Task newTask = new Task();
		newTask.setTitle(floatingTask);
		return newTask;
	}

	static Task addNotes(Task taskToEdit, String notes) {
		Task editedTask = taskToEdit.setNotes(notes);
		return editedTask;
	}

	static Task addDueDate(Task taskToEdit, DateTime dueDate) {
		Task editedTask = taskToEdit.setDue(dueDate);
		return editedTask;
	}

	static Task setCompleted(Task taskToEdit) {
		Task editedTask = taskToEdit.setStatus("completed");
		return editedTask;
	}

	static Task setNotCompleted(Task taskToEdit) {
		Task editedTask = taskToEdit.setStatus("needsAction");
		editedTask = editedTask.setCompleted(null);
		return editedTask;
	}

	static void clearCompletedTasks(String taskListId) throws IOException, UnknownHostException {
		TasksController._tasksClient.tasks().clear(taskListId).execute();
	}

	static TaskList createTaskList(String listName) {
		TaskList newTaskList = new TaskList();
		newTaskList.setTitle(listName);
		return newTaskList;
	}

	static void deleteTaskWithId(String taskListId, String taskId)
			throws IOException, UnknownHostException {
		TasksController._tasksClient.tasks().delete(taskListId, taskId)
				.execute();
	}

	static Task getTaskFromId(String taskListId, String id)
			throws IOException, UnknownHostException {
		Task retrieveTask = TasksController._tasksClient.tasks()
				.get(taskListId, id).execute();
		return retrieveTask;
	}

	static Tasks getTasksFromId(String taskListId) throws IOException, UnknownHostException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).execute();
		return retrieveTasks;
	}

	static Tasks getHiddenTasks(String taskListId) throws IOException, UnknownHostException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).set("showHidden", true).execute();
		return retrieveTasks;
	}

	static Tasks getUndoneTasks(String taskListId) throws IOException, UnknownHostException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).set("showCompleted", false).execute();
		return retrieveTasks;
	}

	static TaskList getTaskListFromId(String taskListId) throws IOException, UnknownHostException {
		TaskList retrieveTaskList = TasksController._tasksClient.tasklists()
				.get(taskListId).execute();
		return retrieveTaskList;
	}

	static TaskList insertTaskList(TaskList newTaskList) throws IOException, UnknownHostException {
		TaskList insertList = TasksController._tasksClient.tasklists()
				.insert(newTaskList).execute();
		return insertList;
	}

	static Task insertTaskToList(String taskListId, Task taskToInsert)
			throws IOException, UnknownHostException {
		Task insertTask = TasksController._tasksClient.tasks()
				.insert(taskListId, taskToInsert).execute();
		return insertTask;
	}

	static Task updateTask(String taskListId, String taskId, Task updatedTask)
			throws IOException, UnknownHostException {
		updatedTask = TasksController._tasksClient.tasks()
				.update(taskListId, taskId, updatedTask).execute();
		return updatedTask;
	}
}

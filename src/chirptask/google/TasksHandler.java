//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

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

	static Task setNotes(Task taskToEdit, String notes) {
		Task editedTask = taskToEdit.setNotes(notes);
		return editedTask;
	}

	static Task setDueDate(Task taskToEdit, Date dueDate) {
	    DateTime googleDateTime = DateTimeHandler.getDateTime(dueDate);
		Task editedTask = taskToEdit.setDue(googleDateTime);
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
	
	static Task setTitle(Task taskToEdit, String description) {
	    Task editedTask = taskToEdit.setTitle(description);
	    return editedTask;
	}

	static void clearCompletedTasks(String taskListId) 
	        throws UnknownHostException, IOException {
		TasksController._tasksClient.tasks().clear(taskListId).execute();
	}

	static TaskList createTaskList(String listName) {
		TaskList newTaskList = new TaskList();
		newTaskList.setTitle(listName);
		return newTaskList;
	}

	static boolean deleteTaskWithId(String taskListId, String taskId) {
	    boolean isDeleted = false;
	    
		try {
            TasksController._tasksClient.tasks().delete(taskListId, taskId)
            		.execute();
            isDeleted = true;
        } catch (UnknownHostException unknownHostException) {
        } catch (IOException e) {
        }
		
		return isDeleted;
	}

	static Task getTaskFromId(String taskListId, String id)
			throws UnknownHostException, IOException {
		Task retrieveTask = TasksController._tasksClient.tasks()
				.get(taskListId, id).execute();
		return retrieveTask;
	}

	static Tasks getTasksFromId(String taskListId) 
	        throws UnknownHostException, IOException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).execute();
		return retrieveTasks;
	}

	static Tasks getHiddenTasks(String taskListId) 
	        throws UnknownHostException, IOException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).set("showHidden", true).execute();
		return retrieveTasks;
	}

	static Tasks getUndoneTasks(String taskListId) 
	        throws UnknownHostException, IOException {
		Tasks retrieveTasks = TasksController._tasksClient.tasks()
				.list(taskListId).set("showCompleted", false).execute();
		return retrieveTasks;
	}

	// Method provided to insert custom TaskList name
	/*static TaskList insertTaskList(TaskList newTaskList) 
	        throws UnknownHostException, IOException {
		TaskList insertList = TasksController._tasksClient.tasklists()
				.insert(newTaskList).execute();
		return insertList;
	}*/

	static Task insertTaskToList(String taskListId, Task taskToInsert)
			throws UnknownHostException, IOException {
        Task insertTask = TasksController._tasksClient.tasks()
                .insert(taskListId, taskToInsert).execute();
		return insertTask;
	}

	static Task updateTask(String taskListId, String taskId, Task updatedTask)
			throws UnknownHostException, IOException {
		updatedTask = TasksController._tasksClient.tasks()
				.update(taskListId, taskId, updatedTask).execute();
		return updatedTask;
	}
}

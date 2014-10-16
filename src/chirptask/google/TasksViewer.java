//@author A0111840W
package chirptask.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Tasks;

/**
 * TasksViewer provides static methods to separate methods, that perform
 * format/display/retrieve functions, from the TasksController class.
 * 
 * This allows a global instance of the mentioned functions as well as provide 
 * easier maintenance of the TasksController class.
 */
public class TasksViewer {

	static void header(String name) {
		System.out.println();
		System.out.println("============== " + name + " ==============");
		System.out.println();
	}

	static void display(TaskLists feed) {
		if (feed.getItems() != null) {
			for (TaskList entry : feed.getItems()) {
				System.out.println();
				System.out
						.println("------------------------------------------");
				display(entry);
			}
		}
	}

	static void display(TaskList entry) {
		System.out.println("ID: " + entry.getId());
		System.out.println("Task Title: " + entry.getTitle());
	}

	static void display(Tasks event) {
		if (event.getItems() != null) {
			for (Task entry : event.getItems()) {
				System.out.println();
				System.out
						.println("------------------------------------------");
				display(entry);
			}
		}
	}

	static void display(Task entry) {
		System.out.println("ID: " + entry.getId());
		System.out.println("Title: " + entry.getTitle());
		if (entry.getNotes() != null) {
			System.out.println("Notes: " + entry.getNotes());
		}
		if (entry.getDue() != null) {
			System.out.println("Due Date: " + entry.getDue());
		}
		System.out.println("Status: " + entry.getStatus());
		if (entry.getCompleted() != null) {
			System.out.println("Completed Time: " + entry.getCompleted());
		}
	}

	static void displayTitle(TaskList taskList) {
		System.out.println("Task Title: " + taskList.getTitle());
	}

	static String retrieveTaskListId(TaskList taskList) {
		String taskId = taskList.getId();
		return taskId;
	}

	static String retrieveTaskListTitle(TaskList taskList) {
		String taskTitle = taskList.getTitle();
		return taskTitle;
	}

	static DateTime retrieveLastModifiedTime(TaskList taskList) {
		DateTime lastModifiedTimestamp = taskList.getUpdated();
		return lastModifiedTimestamp;
	}

}

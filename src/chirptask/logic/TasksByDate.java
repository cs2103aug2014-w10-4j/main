//@author A0111930W-unused
package chirptask.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chirptask.storage.Task;

/**
 * Did not used it as it was not appropriate for our search function.
 * However this can be used for future developement if dev are interested to do a search by date range.
 * 
 * This class keep track of each task belonging to which dates.
 *
 *
 */

public class TasksByDate {
	private Calendar taskDate;
	private List<Task> taskList;

	public TasksByDate() {
		taskList = new ArrayList<Task>();
	}

	public Calendar getTaskDate() {
		return taskDate;
	}

	public List<Task> getTaskList() {
		return taskList;
	}

	public void setTaskDate(Calendar taskDate) {
		this.taskDate = taskDate;
	}

	public void addToTaskList(Task T) {
		taskList.add(T);
	}
}

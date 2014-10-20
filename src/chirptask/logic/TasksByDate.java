package chirptask.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chirptask.storage.Task;
//@author A0111930W
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

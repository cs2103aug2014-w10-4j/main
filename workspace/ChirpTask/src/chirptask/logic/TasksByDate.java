package chirptask.logic;

import chirptask.storage.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TasksByDate {
	private Date taskDate;
	private List<Task> taskList;

	public TasksByDate() {
		taskList = new ArrayList<Task>();
	}

	public Date getTaskDate() {
		return taskDate;
	}

	public List<Task> getTaskList() {
		return taskList;
	}

	public void setTaskDate(Date taskDate) {
		this.taskDate = taskDate;
	}

	public void addToTaskList(Task T) {
		taskList.add(T);
	}
}

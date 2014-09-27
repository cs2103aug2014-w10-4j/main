package chirptask.storage;

import java.util.ArrayList;
import java.util.Iterator;

public class TaskList {

	ArrayList<Task> _taskList;

	public TaskList() {
		_taskList = new ArrayList<Task>();
	}

	public TaskList(ArrayList<Task> list) {
		_taskList = list;
	}

	public ArrayList<Task> getTaskList() {
		return _taskList;
	}

	public void setTaskList(ArrayList<Task> list) {
		_taskList = list;
	}

}

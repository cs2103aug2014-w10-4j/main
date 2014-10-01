package chirptask.storage;

import java.util.ArrayList;
<<<<<<< HEAD
=======
import java.util.List;
import java.util.Iterator;
>>>>>>> FETCH_HEAD

public class TaskList {

	List<Task> _taskList;

	public TaskList() {
		_taskList = new ArrayList<Task>();
	}

	public TaskList(List<Task> list) {
		_taskList = list;
	}

	public List<Task> getTaskList() {
		return _taskList;
	}

	public void setTaskList(List<Task> list) {
		_taskList = list;
	}

}

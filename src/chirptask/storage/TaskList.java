package chirptask.storage;

import java.util.ArrayList;

public class TaskList {
	
	ArrayList<Task> taskList;
	
	public TaskList() {
		taskList = new ArrayList<Task>();
	}
	
	public ArrayList<Task> getTaskList() {	
		return taskList;
	}

	public void setTaskList(ArrayList<Task> list) {
		taskList = list;
	}

}

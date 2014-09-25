package chirptask.storage;

import java.util.ArrayList;
import java.util.Date;

//Abstract class for the various tasks: Timed Task and Deadline Task.

public abstract class Task {
	private ArrayList<String> _contexts;
	private ArrayList<String> _categories;
	private int _taskId;
	private String _description;

	public Task() {
		_contexts = new ArrayList<String>();
		_categories = new ArrayList<String>();
	}

	public Task(int taskId, String description) {
		super();
		_taskId = taskId;
		_description = description;
	}

	public int compareTo(Task b) {

		return 0;
	}

	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}
}

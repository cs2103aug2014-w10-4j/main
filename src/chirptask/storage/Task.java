package chirptask.storage;

import java.util.ArrayList;
import java.util.Date;

//Super class for the various tasks: Timed Task and Deadline Task.


public class Task implements Comparable<Task> {
	private ArrayList<String> _contexts;
	private ArrayList<String> _categories;
	private int _taskId;
	private String _description;
	private Date _date;

	public Task() {
		_contexts = new ArrayList<String>();
		_categories = new ArrayList<String>();
	}
	
	public Task(int taskId, String description) {
		super();
		_taskId = taskId;
		_description = description;
	}

	/*
	 * Compare first by Date object then description
	 */
	public int compareTo(Task b) {
		boolean isSameDateAndTime = this.getDate().compareTo(b.getDate()) == 0;

		if (isSameDateAndTime) {
			// compare description
			return this.getDescription().compareTo(b.getDescription());
		} else {
			return this.getDate().compareTo(b.getDate());
		}
	}

	public boolean equals(Object o) {
		if (o instanceof Task) {
			Task b = (Task) o;
			if (this.getTaskId() == b.getTaskId()) {
				return true;
			}
		}
		return false;
	}

	public int getTaskId() {
		return _taskId;
	}

	public void setTaskId(int taskId) {
		_taskId = taskId;
	}

	public String getDescription() {
		return _description;
	}
	
	public void setDescription (String description) {
		_description = description;
	}

	public Date getDate() {
		Date today = new Date();
		return today;
	}
	
	public void setDate(Date date) {
		_date = date;
	}

	public ArrayList<String> getContexts() {
		return _contexts;
	}

	public void setContexts(ArrayList<String> _contexts) {
		this._contexts = _contexts;
	}

	public ArrayList<String> getCategories() {
		return _categories;
	}

	public void setCategories(ArrayList<String> _categories) {
		this._categories = _categories;
	}
}

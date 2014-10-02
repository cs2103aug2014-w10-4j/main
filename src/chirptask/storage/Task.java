package chirptask.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Super class for the various tasks: Timed Task and Deadline Task.
 */

public class Task implements Comparable<Task> {
    //@author A0111889W
	private List<String> _contexts;
	private List<String> _categories;
	private int _taskId;
	private String _description;
	private String _type;
	private boolean _isDone = false;

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

	public boolean isDone() {
		return _isDone;
	}

	public void setDone(boolean isDone) {
		_isDone = isDone;
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

	public void setDescription(String description) {
		_description = description;
	}
	
	public String getType() {
	    return _type;
	}
	
	public void setType(String type) {
	    _type = type;
	}

	public Date getDate() {
		Date today = new Date();
		return today;
	}

	public List<String> getContexts() {
		return _contexts;
	}

	public void setContexts(ArrayList<String> _contexts) {
		this._contexts = _contexts;
	}

	public List<String> getCategories() {
		return _categories;
	}

	public void setCategories(ArrayList<String> _categories) {
		this._categories = _categories;
	}
}

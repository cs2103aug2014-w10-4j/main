package chirptask.storage;

import java.util.ArrayList;
import java.util.Date;

//Super class for the various tasks: Timed Task and Deadline Task.

public class Task implements Comparable<Task> {
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

	/*
	 * Compare first by Date then time then description
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

	public String getDescription() {
		return _description;
	}

	public Date getDate() {
		Date today = new Date();
		return today;
	}
}

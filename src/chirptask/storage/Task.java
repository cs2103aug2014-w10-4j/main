package chirptask.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Super class for the various tasks: Timed Task and Deadline Task.
 */

public class Task implements Comparable<Task> {
    private static final String TASK_FLOATING = "floating";
    
    
    //@author A0111889W
	private List<String> _contexts;
	private List<String> _categories;
	private int _taskId;
	private String _description;
    private String _googleId;
	private String _type;
	private boolean _isDone = false;
	private Calendar _cal = Calendar.getInstance();
	
	public Task() {
		_contexts = new ArrayList<String>();
		_categories = new ArrayList<String>();
		_googleId = "";
        _type = TASK_FLOATING;
	}

	public Task(int taskId, String description) {
		this();
		_taskId = taskId;
		_description = description;
	}
	
    public Task(int taskId, String description, String taskType) {
        this();
        _taskId = taskId;
        _description = description;
        _type = taskType;
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
	
	public String getGoogleId() {
	    return _googleId;
	}
	
	public void setGoogleId(String googleId) {
	    _googleId = googleId;
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
	//@author A0111930W
	public Calendar getCalendar(){
		return _cal;
	}
	//@author A0111930W
	public void setCalendar(int month, int date){
		_cal.set(_cal.get(Calendar.YEAR), month, date);
	}
	
	public List<String> getContexts() {
		return _contexts;
	}

	public void setContexts(List<String> _contexts) {
		this._contexts = _contexts;
	}

	public List<String> getCategories() {
		return _categories;
	}

	public void setCategories(List<String> _categories) {
		this._categories = _categories;
	}
}

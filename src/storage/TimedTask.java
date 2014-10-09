package chirptask.storage;

import java.util.Date;

public class TimedTask extends Task {
    private static final String TASK_TIMED = "timedtask";
    
    //@author A0111889W
	Date _startTime;
	Date _endTime;

	public TimedTask() {
		super();
	}

	public TimedTask(int taskId, String description, Date startTime,
			Date endTime) {
		super(taskId, description, TASK_TIMED);
		_startTime = startTime;
		_endTime = endTime;
	}
	
	public Date getDate() {
	    return getStartTime();
	}

	public Date getStartTime() {
		return _startTime;
	}

	public void setStartTime(Date startTime) {
		_startTime = startTime;
	}

	public Date getEndTime() {
		return _endTime;
	}

	public void setEndTime(Date endTime) {
		_endTime = endTime;
	}

}

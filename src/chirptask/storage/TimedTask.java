package chirptask.storage;

import java.util.Calendar;
import java.util.Date;

public class TimedTask extends Task {
    private static final String TASK_TIMED = "timedtask";
    
    //@author A0111889W
	Calendar _startTime;
	Calendar _endTime;

	public TimedTask() {
		super();
	}

	public TimedTask(int taskId, String description, Calendar startTime,
			Calendar endTime) {
		super(taskId, description, TASK_TIMED);
		_startTime = startTime;
		_endTime = endTime;
	}
	
	public Calendar getDate() {
	    return getStartTime();
	}

	public Calendar getStartTime() {
		return _startTime;
	}

	public void setStartTime(Calendar startTime) {
		_startTime = startTime;
	}

	public Calendar getEndTime() {
		return _endTime;
	}

	public void setEndTime(Calendar endTime) {
		_endTime = endTime;
	}

}

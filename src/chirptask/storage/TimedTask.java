package chirptask.storage;

import java.util.Date;

/**
 *
 * @author Yeo Quan Yang
 * @MatricNo A0111889W
 * 
 */

public class TimedTask extends Task {
	Date _startTime;
	Date _endTime;

	public TimedTask() {
		super();
	}

	public TimedTask(int taskId, String description, Date startTime,
			Date endTime) {
		super(taskId, description);
		_startTime = startTime;
		_endTime = endTime;
	}

	public Date getDate() {
		return _startTime;
	}

	public void setDate(Date startTime) {
		_startTime = startTime;
	}

	public Date getEndTime() {
		return _endTime;
	}

	public void setEndTime(Date endTime) {
		_endTime = endTime;
	}

}

package chirptask.storage;

import java.util.Date;

public class DeadlineTask extends Task {
    //@author A0111889W
	Date _deadline;

	public DeadlineTask() {
		super();
	}

	public DeadlineTask(int taskId, String description, Date deadline) {
		super(taskId, description);
		_deadline = deadline;
	}

	public Date getDate() {
		return _deadline;
	}

	public void setDate(Date deadline) {
		_deadline = deadline;
	}

}
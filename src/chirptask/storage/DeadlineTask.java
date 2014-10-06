package chirptask.storage;

import java.util.Date;

public class DeadlineTask extends Task {
    private static final String TASK_DEADLINE = "deadline";
    
    //@author A0111889W
	Date _deadline;

	public DeadlineTask(int taskId, String description, Date deadline) {
		super(taskId, description, TASK_DEADLINE);
		_deadline = deadline;
	}

	public Date getDate() {
		return _deadline;
	}

	public void setDate(Date deadline) {
		_deadline = deadline;
	}

}
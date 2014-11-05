//@author A0111889W
package chirptask.storage;

import java.util.Calendar;

public class DeadlineTask extends Task {
    private static final String TASK_DEADLINE = "deadline";
    
	Calendar _deadline;

	public DeadlineTask(int taskId, String description, Calendar deadline) {
		super(taskId, description, TASK_DEADLINE);
		_deadline = deadline;
	}

	public Calendar getDate() {
		return _deadline;
	}

	public void setDate(Calendar deadline) {
		_deadline = deadline;
	}

}
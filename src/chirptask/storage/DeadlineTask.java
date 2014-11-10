//@author A0111889W
package chirptask.storage;

import java.util.Calendar;

public class DeadlineTask extends Task {
    private static final String TASK_DEADLINE = "deadline";

    Calendar _deadline;

    /**
     * Creates a deadline task with specified description and deadline on the
     * input date.
     *
     * @param taskId taskId of task to create
     * @param description description of task to create
     * @param deadline deadline of task to create
     */
    public DeadlineTask(int taskId, String description, Calendar deadline) {
        super(taskId, description, TASK_DEADLINE);
        if (deadline == null) {
            throw new NullPointerException();
        }
        _deadline = deadline;
    }

    /*
     * (non-Javadoc)
     *
     * @see chirptask.storage.Task#getDate()
     */
    @Override
    public Calendar getDate() {
        return _deadline;
    }

    /*
     * (non-Javadoc)
     *
     * @see chirptask.storage.Task#setDate(java.util.Calendar)
     */
    @Override
    public void setDate(Calendar deadline) {
        if (deadline == null) {
            throw new NullPointerException();
        }
        _deadline = deadline;
    }
}
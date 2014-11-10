//@author A0111889W
package chirptask.storage;

import java.util.Calendar;

public class TimedTask extends Task {
    private static final String TASK_TIMED = "timedtask";

    Calendar _startTime;
    Calendar _endTime;

    /**
     * Creates a timed task or schedule object.
     * 
     * @param taskId taskId of task to create
     * @param description description of task to create
     * @param startTime starting time of task to create
     * @param endTime ending time of task to create
     */
    public TimedTask(int taskId, String description, Calendar startTime,
            Calendar endTime) {
        super(taskId, description, TASK_TIMED);
        if (startTime == null || endTime == null) {
            throw new NullPointerException();
        }
        _startTime = startTime;
        _endTime = endTime;
    }

    public Calendar getDate() {
        return getStartTime();
    }

    public Calendar getStartTime() {
        return _startTime;
    }

    /**
     * Sets the starting time of schedule or timedtask.
     * 
     * @param startTime calendar object of the starting time
     */
    public void setStartTime(Calendar startTime) {
        if (startTime == null) {
            throw new NullPointerException();
        }
        _startTime = startTime;
    }

    public Calendar getEndTime() {
        return _endTime;
    }

    /**
     * Sets the ending time of schedule or timedtask.
     * 
     * @param endTime calendar object of the ending time
     */
    public void setEndTime(Calendar endTime) {
        if (endTime == null) {
            throw new NullPointerException();
        }
        _endTime = endTime;
    }

    /* (non-Javadoc)
     * @see chirptask.storage.Task#hashCode()
     */
    public int hashCode() {
        String uniqueString = "";
        if (this.getGoogleId() == null || this.getGoogleId().isEmpty()) {
            uniqueString = this.getTaskId() + "," + this.getDescription() + ","
                    + this.getStartTime() + "," + this.getEndTime() + ","
                    + this.getType();
        } else {
            uniqueString = this.getGoogleId() + "," + this.getDescription()
                    + "," + this.getStartTime() + "," + this.getEndTime() + ","
                    + this.getType();
        }

        return uniqueString.hashCode();
    }

}

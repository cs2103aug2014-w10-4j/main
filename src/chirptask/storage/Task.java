//@author A0111889W
package chirptask.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Super class for the various tasks: Timed Task and Deadline Task.
 */

public class Task implements Comparable<Task> {
    public static final String TASK_FLOATING = "floating";
    public static final String TASK_DEADLINE = "deadline";
    public static final String TASK_TIMED = "timedtask";

    private List<String> _hashtags;
    private List<String> _categories;

    private int _taskId;

    private String _description;
    private String _eTag;
    private String _googleId;
    private String _type;

    private boolean _isDone = false;
    private boolean _isDeleted;
    private boolean _isModified;

    private Calendar _cal;

    public Task() {
        _hashtags = new ArrayList<String>();
        _categories = new ArrayList<String>();
        _eTag = "";
        _googleId = "";
        _type = TASK_FLOATING;
        _isDeleted = false;
        _isModified = false;
        _cal = null;
    }

    public Task(int taskId, String description) {
        this();
        _taskId = taskId;
        _description = description;
    }

    Task(int taskId, String description, String taskType) {
        this();
        if (taskType == null) {
            throw new NullPointerException();
        }
        if (taskType.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        _taskId = taskId;
        _description = description;
        _type = taskType;
    }

    /*
     * Compare by Time then Type then Description (Lexicographically)
     */
    public int compareTo(Task b) {
        if (b == null) {
            throw new NullPointerException();
        }

        boolean isSameDateAndTime = this.getDate().compareTo(b.getDate()) == 0;
        boolean isSameType = this.getType().compareTo(b.getType()) == 0;
        if (isSameDateAndTime) {
            if (isSameType) {
                // compare description
                return this.getDescription().compareTo(b.getDescription());
            } else {

                /*
                 * Floating tasks shown at the top before the rest.
                 * Floating < Deadline < TimedTasks
                 * You only enter this section of the code if the two Task are
                 * of different type. With that, if either is a floating type,
                 * the floating type must be shown first.
                 */
                if (this.getType().equals("floating")) {
                    return -1;
                } else if (b.getType().equals("floating")) {
                    return 1;
                } else {
                    return this.getType().compareTo(b.getType());
                }
            }
        } else {
            return this.getDate().compareTo(b.getDate());
        }
    }

    public boolean equals(Object o) {
        try {
            if (o instanceof Task) {
                Task b = (Task) o;
                if (this.getTaskId() == b.getTaskId()) {
                    return true;
                }
            }
        } catch (Exception e) {
            // returns false if null value is provided.
        }
        return false;
    }

    public boolean isDone() {
        return _isDone;
    }

    public void setDone(boolean isDone) {
        _isDone = isDone;
    }

    public boolean isDeleted() {
        return _isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        _isDeleted = isDeleted;
    }

    public boolean isModified() {
        return _isModified;
    }

    public void setModified(boolean isModified) {
        _isModified = isModified;
    }

    public String getETag() {
        return _eTag;
    }

    public void setETag(String eTag) {
        _eTag = eTag;
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
        if (type == null) {
            throw new NullPointerException();
        }
        if (type.trim().isEmpty()) {
            throw new IllegalArgumentException("type cannot be empty");
        }
        _type = type;
    }

    public Calendar getDate() {
        if (_cal == null) {
            _cal = Calendar.getInstance();
            _cal.set(Calendar.HOUR_OF_DAY, 0);
            _cal.set(Calendar.MINUTE, 0);
            _cal.set(Calendar.SECOND, 0);
            _cal.set(Calendar.MILLISECOND, 0);
        }
        return _cal;
    }

    public void removeDate() {
        _cal = null;
    }

    public List<String> getHashtags() {
        return _hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        if (hashtags == null) {
            throw new NullPointerException();
        }
        this._hashtags = hashtags;
    }

    public List<String> getCategories() {
        return _categories;
    }

    public void setCategories(List<String> categories) {
        if (_categories == null) {
            throw new NullPointerException();
        }
        this._categories = categories;
    }

    public int hashCode(){
        String uniqueString = this.getGoogleId()+","+this.getDescription()+","+this.getDate()+","+this.getType();
        return uniqueString.hashCode();
    }

    // @author A0111930W
    public void setDate(Calendar doneDate) {
        if (doneDate == null) {
            throw new NullPointerException();
        }
        _cal = doneDate;
        _cal.set(Calendar.HOUR_OF_DAY, 0);
        _cal.set(Calendar.MINUTE, 0);
        _cal.set(Calendar.SECOND, 0);
        _cal.set(Calendar.MILLISECOND, 0);
    }

}

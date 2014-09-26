package chirptask.google;

import com.google.api.services.tasks.model.TaskList;

public class TasksHandler {
    static boolean isNull(TaskList _taskList) {
        if (_taskList == null) {
            return true;
        } else {
            return false;
        }
    }
}

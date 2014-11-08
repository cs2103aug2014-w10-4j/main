//@author A0111930W-unused
package chirptask.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Did not think it would be appropriate for our search function as this class allows
 * to retrieve a tasksbydate object.
 * 
 * @author User
 *
 */

public class TaskView {

	private List<TasksByDate> listByDate;

	public TaskView() {
		listByDate = new ArrayList<TasksByDate>();
	}

	public List<TasksByDate> getListByDates() {
		return listByDate;
	}

	public void addToTaskView(TasksByDate task) {
		listByDate.add(task);
	}
}

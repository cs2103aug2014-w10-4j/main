package chirptask.logic;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class DisplayView {
	/**
	 * This will take in a filtered list and update the taskview, sort to
	 * date/time, store
	 * into Arraylist of TasksByDates of arraylist of tasks
	 * */
	static TaskView updateTaskView(List<Task> tasks) {

		// Should change .getAllTasks() to arraylist?
		// List<Task> allTasks = _storageHandler.getAllTasks();
		Collections.sort(tasks);
		TreeMap<Date, TasksByDate> map = new TreeMap<Date, TasksByDate>();

		for (Task task : tasks) {
			Date currDate = task.getDate();
			if (map.containsKey(currDate)) {
				map.get(currDate).addToTaskList(task);
			} else {
				TasksByDate dateTask = new TasksByDate();
				dateTask.setTaskDate(currDate);
				dateTask.addToTaskList(task);
				map.put(dateTask.getTaskDate(), dateTask);
			}
		}

		Iterator<Map.Entry<Date, TasksByDate>> it = map.entrySet().iterator();
		TaskView view = new TaskView();
		while (it.hasNext()) {
			view.addToTaskView(it.next().getValue());
		}
		return view;

	}
	//Call this at init to show all tasks.
	static TaskView updateTaskView() {

		List<Task> allTasks = StorageHandler.getAllTasks();
		Collections.sort(allTasks);
		//call filter
		return updateTaskView(allTasks);

	}

	// Take in type, action
	static void showStatusToUser(StatusType type, Action action) {
		if (type == StatusType.ERROR) {
			// message processing and call GUI api
			action.getCommandType();
			action.getTask().getDescription();
			action.getTask().getDate().toString();

		} else {
			// message processing and call GUI api
			action.getCommandType();
			action.getTask().getDescription();
			action.getTask().getDate().toString();
		}
	}
}

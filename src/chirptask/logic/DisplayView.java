package chirptask.logic;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class DisplayView {
	/**
	 * This will take in a filtered list and update the taskview, sort to
	 * date/time, store into List of tasks
	 * 
	 * @param _gui
	 * */
	public static void updateTaskView(List<Task> tasks, MainGui gui) {

		Collections.sort(tasks);
		TreeMap<String, TasksByDate> map = new TreeMap<String, TasksByDate>();

		processUpdateTaskView(tasks, gui, map);
		
		processUpdateContextAndCategoryView(gui);
		// Iterator<Map.Entry<Date, TasksByDate>> it =
		// map.entrySet().iterator();
		// TaskView view = new TaskView();
		// while (it.hasNext()) {
		// view.addToTaskView(it.next().getValue());
		// }

	}

	private static void processUpdateContextAndCategoryView(MainGui gui) {
		updateCategoryView(gui);
		updateContextView(gui);
	}

	private static void processUpdateTaskView(List<Task> tasks, MainGui gui,
			TreeMap<String, TasksByDate> map) {
		for (Task task : tasks) {
			String currDate = MainGui.convertDateToString(task.getDate());

			if (map.containsKey(currDate)) {
				map.get(currDate).addToTaskList(task);
			} else {
				TasksByDate dateTask = new TasksByDate();
				// dateTask.setTaskDate(task.getDate());
				// dateTask.addToTaskList(task);
				gui.addNewTaskViewDate(task.getDate());
				map.put(currDate, dateTask);
			}
			gui.addNewTaskViewToDate(task.getDate(), task.getTaskId(),
					task.getDescription(), task.getDate().toString(),
					task.isDone());

		}
	}

	// Call this at init to show all tasks.
	public static void updateTaskView(MainGui gui) {

		List<Task> allTasks = StorageHandler.getAllTasks();
		if (allTasks != null) {
			Collections.sort(allTasks);
			updateTaskView(allTasks, gui);
		}

	}

	public static void updateCategoryView(MainGui gui) {
		List<String> categories = FilterTasks.getCategoryList();
		for (String category : categories) {
			gui.addCategoryIntoList(category);
		}
	}

	public static void updateContextView(MainGui gui) {
		List<String> contexts = FilterTasks.getContextList();
		for (String context : contexts) {
			gui.addContextIntoList(context);
		}
	}

	// Take in type, action
	public static void showStatusToUser(StatusType type, Action action,
			MainGui gui) {
		if (type == StatusType.ERROR) {

			gui.setError("Error in " + action.getCommandType() + " task");

		} else {

			gui.setStatus("Success in " + action.getCommandType() + " task");
		}
	}

}

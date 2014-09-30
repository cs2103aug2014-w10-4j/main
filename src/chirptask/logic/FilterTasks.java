package chirptask.logic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class FilterTasks {
	private StorageHandler _storageHandler;
	private static List<Task> filteredTask;
	private static String currentFilter;

	static void filter(Task T) {
		filteredTask = new ArrayList<Task>();
		currentFilter = T.getDescription();
		List<Task> allTask = StorageHandler.getAllTasks();

		for (Task a : allTask) {
			if (a.getDescription().equalsIgnoreCase(currentFilter)) {
				filteredTask.add(a);
			}
		}

	}

	// Add in filter time, date, task, done, undone
	// add in checkTaskType
	static void filter() {
		if (currentFilter.isEmpty() || currentFilter == "") {
			filteredTask = StorageHandler.getAllTasks();
		} else {
			filteredTask = new ArrayList<Task>();
			List<Task> allTask = StorageHandler.getAllTasks();

			for (Task a : allTask) {
				if (a.getDescription().equalsIgnoreCase(currentFilter)) {
					filteredTask.add(a);
				}
			}
		}
	}

	static List<Task> getFilteredList() {
		return filteredTask;
	}
}

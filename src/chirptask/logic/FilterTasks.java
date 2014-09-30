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
	
	private List<Task> filterParser(Task task) {
		// process the task into type of filter then filter accordingly
		task.getDescription();
		List<Task> allTask = StorageHandler.getAllTasks();
		return null;
	}

	// Filtering according to the UI tag
	public List<Task> filter(String tag, List<Task> taskList) {
		List<Task> filteredTask = new ArrayList<Task>();
		// get storage
		// filter storage
		// Use iterator
		for (Task task : taskList) {
			if (task.getDescription().equalsIgnoreCase(tag)) {
				filteredTask.add(task);
			}
		}
		return filteredTask;
	}

	static void filter(Task T) {
		filteredTask = new ArrayList<Task>();
		
		
	}

	public void filter(Date date) {

	}

	public void filter(Date fromDate, Date toDate) {

	}

	public void filter(Time time) {

	}

	public void filter(Time fromTime, Time toTime) {

	}
	static List<Task> getFilteredList(){
		return filteredTask;
	}
}

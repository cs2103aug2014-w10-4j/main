package chirptask.logic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class FilterTasks {

	private static List<Task> filteredTask;
	private static List<String> categoriesList;
	private static List<String> contextsList;
	private static String currentFilter = "";

	static void filter(Task T) {
		filteredTask = new ArrayList<Task>();
		currentFilter = T.getDescription();
		List<Task> allTask = StorageHandler.getAllTasks();
		// check 1st String to determine the type of filter
		for (Task a : allTask) {
			if (a.getDescription().contains(currentFilter)) {
				filteredTask.add(a);
			}
		}
		for (Task task : filteredTask) {
			populateContext(task);
		}

	}

	// Add in filter time, date, task, done, undone

	static void filter() {
		categoriesList = new ArrayList<String>();
		contextsList = new ArrayList<String>();
		if (currentFilter.isEmpty()) {
			filteredTask = StorageHandler.getAllTasks();

			populateCategoryAndContext();

		} else {
			filteredTask = new ArrayList<Task>();
			List<Task> allTask = StorageHandler.getAllTasks();

			for (Task a : allTask) {
				if (a.getDescription().equalsIgnoreCase(currentFilter)) {
					filteredTask.add(a);
				}
			}
			categoriesList.clear();
			contextsList.clear();
			populateCategoryAndContext();
		}
	}

	private static void populateCategoryAndContext() {
		for (Task task : StorageHandler.getAllTasks()) {
			populateContext(task);
			populateCategory(task);
		}
	}

	private static void populateCategory(Task task) {
		
			for (String category : task.getCategories()) {
				if (!categoriesList.contains(category.toLowerCase())) {
					categoriesList.add(category.toLowerCase());
					
				}
			}
		
	}

	private static void populateContext(Task task) {
		
			for (String context : task.getContexts()) {
				if (!contextsList.contains(context.toLowerCase())) {
					contextsList.add(context.toLowerCase());
				}
			}
		
	}

	public static List<Task> getFilteredList() {
		return filteredTask;
	}

	public static List<String> getContextList() {
		return contextsList;
	}
	
	public static List<String> getCategoryList(){
		return categoriesList;
	}
}

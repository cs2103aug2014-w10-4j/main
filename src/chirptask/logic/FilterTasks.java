package chirptask.logic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

//@A0111930W
public class FilterTasks {

	private static List<Task> filteredTask;
	private static List<String> categoriesList;
	private static List<String> contextsList;
	private static String currentFilter = "";

	static void filter(Task T) {
		currentFilter = T.getDescription();
		
		List<Task> allTask = StorageHandler.getAllTasks();
		// check 1st String to determine the type of filter

		if (currentFilter.isEmpty()) {
			filteredTask = StorageHandler.getAllTasks();
		} else {
			
			processFilter(currentFilter);			

		}
		contextsList.clear();
		for (Task task : filteredTask) {
			populateContext(task);
		}
	}

	

	// Add in filter time, date, task, done, undone

	/**
	 * Assuming that we use keywords like -TIME -DONE -UNDONE -DATE -TIME
	 * Assuming if no flag indication means filter by that keyword
	 * **/
	private static void processFilter(String filter) {
		String[] param = filter.split("\\s+");
		List<Task> templist = new ArrayList<Task>();
		for (int i = 0; i < param.length; i++) {
			
			if (param[i].equalsIgnoreCase("/TASK")) {
				// search task type
				if (param[i + 1].equalsIgnoreCase("timed")) {
					
					populateTaskList(templist, "timed");
				} else if (param[i + 1].equalsIgnoreCase("floating")) {
					populateTaskList(templist, "floating");
				} else if (param[i + 1].equalsIgnoreCase("deadline")) {
					populateTaskList(templist, "deadline");
				}
				i++;
			} else if (param[i].equalsIgnoreCase("/DONE")) {
				// search done task
				populateDoneList(templist, true);
			} else if (param[i].equalsIgnoreCase("/UNDONE")) {
				// search undone task
				populateDoneList(templist, false);
			} else {
				// String search, assume only 1 keyword
				populateStringList(templist, param[i]);
			}
			filteredTask = templist;
			
		}

	}
	private static void populateStringList(List<Task> templist, String filter) {
		for (Task T : filteredTask) {
			if (T.getDescription().contains(filter)) {
				templist.add(T);
			}
		}
	}
	private static void populateDoneList(List<Task> templist, boolean done) {
		for (Task T : filteredTask) {
			if (done) {
				if (T.isDone()) {
					templist.add(T);
				}
			} else {
				if (!T.isDone()) {
					templist.add(T);
				}
			}
		}
	}

	private static void populateTaskList(List<Task> templist, String type) {
		for (Task T : filteredTask) {
			
			if (T.getType().equalsIgnoreCase(type)) {
				templist.add(T);
				
			}
		}
	}

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

	public static List<String> getCategoryList() {
		return categoriesList;
	}
}

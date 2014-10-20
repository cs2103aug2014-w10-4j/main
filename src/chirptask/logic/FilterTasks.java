package chirptask.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.storage.EventLogger;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

import java.util.Calendar;

//@author A0111930W
public class FilterTasks {

	private static List<Task> filteredTask;
	private static List<String> categoriesList;
	private static List<String> contextsList;
	private static String currentFilter = "";
	private static final int PARAM_FILTER = 1;
	private static EventLogger log = new EventLogger();
	static void filter(Task T) {
		currentFilter = T.getDescription();

		List<Task> allTask = StorageHandler.getAllTasks();
		// check 1st String to determine the type of filter

		if (currentFilter.isEmpty()) {
			filteredTask = StorageHandler.getAllTasks();
			filteredTask = hideDeleted(filteredTask);
		} else {

			processFilter(currentFilter);

		}
		contextsList.clear();
		for (Task task : filteredTask) {
			populateContext(task);
		}
	}
	
	static List<Task> hideDeleted(List<Task> taskList) {
	    List<Task> newList = new ArrayList<Task>();
	    Iterator<Task> tasks = taskList.iterator();
	    
	    while (tasks.hasNext()) {
	        Task currTask = tasks.next();
	        if (!currTask.isDeleted()) {
	            newList.add(currTask);
	        }
	    }
	    
	    return newList;
	}

	
	private static void processFilter(String filters) {
		String[] param = filters.split("\\s+");
		List<Task> templist = new CopyOnWriteArrayList<Task>();
		;
		for (int i = 0; i < param.length; i++) {
			String filter = param[i];

			switch (filter) {
			case "/done":
				// search done task
				filterDone(templist, true);
				break;
			case "/undone":
				// search undone task
				filterDone(templist, false);
				break;
			case "/floating":
				filterTaskType(templist, "floating");
				break;
			case "/timed":
				filterTaskType(templist, "timedtask");
				break;
			case "/deadline":
				filterTaskType(templist, "deadline");
				break;
			case "/date":
				// Assuming input is 23/10
				try {
					Calendar filterdate = processFilterDateParam(param[i+1]);
					filterTaskByDate(templist, filterdate);
				}catch (ArrayIndexOutOfBoundsException e){
					//log down invalid input
					log.logError(Messages.LOG_MESSAGE_INVALID_COMMAND);
					
				}
				i++;
				break;
			default:
				// Entire string keyword search
				filterKeyword(templist, filter);
				break;
			}

			filteredTask = new ArrayList<Task>(templist);
			templist.clear();
		}

	}

	private static Calendar processFilterDateParam(String filter) {
		String[] temp = filter.split("/");
		Calendar filterdate = Calendar.getInstance();
		filterdate.set(filterdate.get(Calendar.YEAR),
				Integer.parseInt(temp[0])-1, Integer.parseInt(temp[1]));
		
		return filterdate;
	}

	private static void filterTaskByDate(List<Task> tempList,
			Calendar filterdate) {
		populateDateList(tempList, filterdate);
		if (tempList.isEmpty()) {
			resetFilteredTask();
			populateDateList(tempList, filterdate);
		}
	}
	/**
	 * @author A0111930W
	 * @param tempList
	 * @param filterdate
	 * 
	 * This method will check the filter date with the list of fliteredtask if the filter date is after and equals to the Task date
	 * add to the list.
	 * 
	 */
	
	private static void populateDateList(List<Task> tempList,
			Calendar filterdate) {
		for (Task T : filteredTask) {
			//System.out.println(T.getDate().get(Calendar.DATE)+"/"+T.getDate().get(Calendar.MONTH));
			//>= 0 means the current calendar is after or equals to the Task calendar
			if (filterdate.compareTo(T.getDate()) >= 0) {
				//System.out.println(filterdate.get(Calendar.DATE));
				tempList.add(T);		
			}
		}

	}

	private static void filterKeyword(List<Task> tempList, String keyword) {
		populateStringList(tempList, keyword);
		if (tempList.isEmpty()) {
			resetFilteredTask();
			populateStringList(tempList, keyword);
		}
	}

	private static void populateStringList(List<Task> templist, String keywords) {
		for (Task T : filteredTask) {
			if (T.getDescription().contains(keywords)) {
				templist.add(T);
			}
		}
	}

	private static void filterDone(List<Task> tempList, boolean done) {
		populateDoneList(tempList, done);
		if (tempList.isEmpty()) {
			resetFilteredTask();
			populateDoneList(tempList, done);
		}
	}

	private static void populateDoneList(List<Task> tempList, boolean done) {
		for (Task T : filteredTask) {
			if (done) {
				if (T.isDone()) {
					tempList.add(T);
				}
			} else {
				if (!T.isDone()) {
					tempList.add(T);
				}
			}
		}
	}

	private static void resetFilteredTask() {
		filteredTask = StorageHandler.getAllTasks();
		filteredTask = hideDeleted(filteredTask);
	}

	private static void filterTaskType(List<Task> tempList, String taskType) {
		populateTaskList(tempList, taskType);
		if (filteredTask.isEmpty()) {
			resetFilteredTask();
			populateTaskList(tempList, taskType);
		}
	}

	private static void populateTaskList(List<Task> tempList, String taskType) {

		for (Task T : filteredTask) {
			if (T.getType().equalsIgnoreCase(taskType)) {
				tempList.add(T);

			}
		}

	}

	static void filter() {
		categoriesList = new ArrayList<String>();
		contextsList = new ArrayList<String>();
		if (currentFilter.isEmpty()) {
			filteredTask = StorageHandler.getAllTasks();
			filteredTask = hideDeleted(filteredTask);
			populateCategoryAndContext();
		} else {
			filteredTask = new ArrayList<Task>();
			List<Task> allTask = StorageHandler.getAllTasks();

			for (Task a : allTask) {
				if (a.getDescription().equalsIgnoreCase(currentFilter)) {
				    if (!a.isDeleted()) {
				        filteredTask.add(a);
				    }
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

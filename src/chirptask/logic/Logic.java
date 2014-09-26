package chirptask.logic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import chirptask.storage.Storage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;
import chirptask.ui.*;

enum CommandType {
	ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, LOGIN, INVALID, EXIT
}

enum StatusType {
	ERROR, MESSAGE
}

public class Logic {
	private Action _lastAction;
	private InputParser _parser;
	private StorageHandler _storageHandler;
	private GUI _gui;

	public Logic() {
		_storageHandler = new StorageHandler();
		_gui = new GUI();
		// lastAction = new Action();
		_parser = new InputParser();
	}

	private CommandType determineCommandType(String commandTypeString) {
		if (commandTypeString.equalsIgnoreCase("add")) {
			return CommandType.ADD;
		} else if (commandTypeString.equalsIgnoreCase("display")) {
			return CommandType.DISPLAY;
		} else if (commandTypeString.equalsIgnoreCase("delete")) {
			return CommandType.DELETE;
		} else if (commandTypeString.equalsIgnoreCase("edit")) {
			return CommandType.EDIT;
		} else if (commandTypeString.equalsIgnoreCase("undo")) {
			return CommandType.UNDO;
		} else if (commandTypeString.equalsIgnoreCase("done")) {
			return CommandType.DONE;
		} else if (commandTypeString.equalsIgnoreCase("login")) {
			return CommandType.LOGIN;
		} else if (commandTypeString.equalsIgnoreCase("exit")) {
			return CommandType.EXIT;
		} else {
			return CommandType.INVALID;
		}
	}

	// Will take in Action object
	public void executeAction(Action command) {
		String action = command.getCommandType();
		CommandType actionType = determineCommandType(action);
		Task task = command.getTask();
		switch (actionType) {
		case ADD:
			_storageHandler.addTask(task);
			this.setLastAction(command);
			break;
		case DELETE:
			_storageHandler.deleteTask(task);
			this.setLastAction(command);
			break;
		case DISPLAY:
			updateTaskView(filterParser(task));
			break;
		case EDIT:
			this.setLastAction(command);
			break;
		case UNDO:
			// negate action and run excecuteAction again
			executeAction(command.undo(this.getLastAction()));
			break;
		case DONE:
			this.setLastAction(command);
			break;
		case LOGIN:
			break;
		case EXIT:
			System.exit(0);
			break;
		case INVALID:
			// call print some invalid message
			break;
		default:
			// throw error
		}
	}

	private List<Task> filterParser(Task task) {
		// process the task into type of filter then filter accordingly
		task.getDescription();
		List<Task> allTask = this._storageHandler.getAllTasks();
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

	public void filter(Task T) {
		List<Task> filteredTask = new ArrayList<Task>();

	}

	public void filter(Date date) {

	}

	public void filter(Date fromDate, Date toDate) {

	}

	public void filter(Time time) {

	}

	public void filter(Time fromTime, Time toTime) {

	}

	/**
	 * This will update the taskview Retrieve alltasks, sort to date/time, store
	 * into Arraylist of dates of arraylist of tasks
	 * */
	public TaskView updateTaskView(List<Task> tasks) {

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

	// Take in type, action
	public void showStatusToUser(StatusType type, Action action) {
		if (type == StatusType.ERROR) {
			// message processing and call GUI api
		} else {
			// message processing and call GUI api
		}
	}

	public Action getLastAction() {
		return _lastAction;
	}

	public void setLastAction(Action lastAction) {
		this._lastAction = lastAction;
	}

}

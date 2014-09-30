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
import chirptask.google.GoogleController;
import chirptask.gui.*;

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
	private MainGui _gui;

	public Logic() {
		_storageHandler = new StorageHandler();
		_gui = new MainGui();
		// lastAction = new Action();

	}

	public void retrieveInputFromUI(String input) {
		_parser = new InputParser(input);
		processGroupAction(_parser.getActions().getActionList());
	}

	public void processGroupAction(List<Action> list) {
		for (Action a : list) {
			executeAction(a);
		}
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
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case DELETE:
			_storageHandler.deleteTask(task);
			this.setLastAction(command);
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case DISPLAY:
			FilterTasks.filter(task);
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case EDIT:
			_storageHandler.modifyTask(task);
			this.setLastAction(command);
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case UNDO:
			// negate action and run excecuteAction again
			executeAction(command.undo(this.getLastAction()));
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case DONE:
			task.setDone(true);
			_storageHandler.modifyTask(task);
			this.setLastAction(command);
			DisplayView.updateTaskView(FilterTasks.getFilteredList());
			break;
		case LOGIN:
			_storageHandler.initCloudStorage();
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

	public Action getLastAction() {
		return _lastAction;
	}

	public void setLastAction(Action lastAction) {
		this._lastAction = lastAction;
	}

}

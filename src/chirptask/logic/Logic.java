package chirptask.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private static final String MESSAGE_NEW_COMMAND = "command: ";
	private static final int ERROR_OPENING_STREAM = 57;

	private Action _lastAction;
	private InputParser _parser;
	private StorageHandler _storageHandler;
	private MainGui _gui;

	// For working ChirpTask
	private BufferedReader commandBufferReader = new BufferedReader(
			new InputStreamReader(System.in));

	public Logic(MainGui gui) {
		_storageHandler = new StorageHandler();
		_parser = new InputParser();
		_gui = gui;
		FilterTasks.filter();
		DisplayView.updateTaskView(_gui);
	}

	public Logic() {

		_storageHandler = new StorageHandler();
		_parser = new InputParser();
		FilterTasks.filter();
		
		// runUntilExitCommand(); //Temporary CLI code before full integration
		// with GUI
		// lastAction = new Action();

	}

	/**
	 * Temporary CLI code before full integration with GUI
	 */
	private void runUntilExitCommand() {
		while (true) {
			issueNewCommandStatement();
			String userInput = waitForUserCommand();
			retrieveInputFromUI(userInput);
		}
	}

	private void issueNewCommandStatement() {
		displayToUser(MESSAGE_NEW_COMMAND);
	}

	private String waitForUserCommand() {
		String userCommand = getUserCommand();
		return userCommand;
	}

	private void displayToUser(String messageToDisplay) {
		System.out.print(messageToDisplay);
	}

	private String getUserCommand() {
		try {
			String userInputCommand = commandBufferReader.readLine();
			return userInputCommand;
		} catch (IOException ioAccessError) {
			exitChirpTask(ERROR_OPENING_STREAM);
		}
		return "INVALID";
	}

	/**
	 * End temporary code
	 */

	private void exitChirpTask(int typeOfExit) {
		System.exit(typeOfExit);
	}
	
	private void clearUI() {
	    _gui.clearTrendingList();
	    _gui.clearTaskView();
	}

	public void retrieveInputFromUI(String input) {
		_parser.receiveInput(input);
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
			processAdd(command, task);
			break;
		case DELETE:
			processDelete(command, task);
			break;
		case DISPLAY:
			// now can only filter string
			System.out.println("Display");
			processDisplay(task);
			break;
		case EDIT:
			processEdit(command, task);
			break;
		case UNDO:
			// negate action and run excecuteAction again
			processUndo(command);
			break;
		case DONE:
			processDone(command, task);
			break;
		case LOGIN:
			processLogin(command);
			break;
		case EXIT:
			processExit();
			break;
		case INVALID:
			// call print some invalid message
			break;
		default:
			// throw error
		}
	}

	private void processExit() {
		System.exit(0);
	}

	private void processLogin(Action command) {
		boolean isSuccess;
		isSuccess = _storageHandler.initCloudStorage();
		this.showStatusToUser(command, isSuccess);
	}

	private void processDone(Action command, Task task) {
		task.setDone(true);
		processEdit(command, task);
	}

	private void processUndo(Action command) {
		executeAction(command.undo(this.getLastAction()));
        clearUI();
		FilterTasks.filter();
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void processEdit(Action command, Task task) {
		boolean isSuccess;
		isSuccess = _storageHandler.modifyTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void processDisplay(Task task) {
		clearUI();
		FilterTasks.filter(task);
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void processDelete(Action command, Task task) {
		boolean isSuccess;
		isSuccess = _storageHandler.deleteTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void processAdd(Action command, Task task) {
		boolean isSuccess;
		isSuccess = _storageHandler.addTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void filterAndDisplay(Action command, boolean isSuccess) {
		// set lastAction
		this.setLastAction(command);
        clearUI();
		FilterTasks.filter();
		showStatusToUser(command, isSuccess);
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void showStatusToUser(Action command, boolean isSuccess) {
		if (isSuccess == true) {
			DisplayView.showStatusToUser(StatusType.MESSAGE, command, _gui);
		} else {
			DisplayView.showStatusToUser(StatusType.ERROR, command, _gui);
		}
	}

	public Action getLastAction() {
		return _lastAction;
	}

	public void setLastAction(Action lastAction) {
		this._lastAction = lastAction;
	}

}

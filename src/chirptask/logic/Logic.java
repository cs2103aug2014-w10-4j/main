package chirptask.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;




import chirptask.storage.EventLogger;
import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.gui.MainGui;
//import chirptask.storage.Storage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;



//@author A0111930W
public class Logic {
	private static final String MESSAGE_NEW_COMMAND = "command: ";
	private static final int ERROR_OPENING_STREAM = 57;

	private Action _lastAction;
	private InputParser _parser;
	private StorageHandler _storageHandler;
	private MainGui _gui;
	private EventLogger _logger;
	// For working ChirpTask
	private BufferedReader commandBufferReader = new BufferedReader(
			new InputStreamReader(System.in));

	public Logic(MainGui gui) {
		_storageHandler = new StorageHandler();
		_parser = new InputParser();
		_gui = gui;
		_logger = new EventLogger();
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

	private void clearUi() {
		_gui.clearTrendingList();
		_gui.clearTaskView();
	}

	public void retrieveInputFromUI(String input) {
		_parser.receiveInput(input);
		//Assuming there will always be GroupActions parse by InputParser every user input.
		assert _parser.getActions()!=null;
		processGroupAction(_parser.getActions().getActionList());
	}

	public void processGroupAction(List<Action> list) {
		for (Action a : list) {
			executeAction(a);
		}
	}


	// Will take in Action object
	public void executeAction(Action command) {
		Settings.CommandType actionType = command.getCommandType();
		assert actionType!=null;
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
			processDisplay(task);
			break;
		case EDIT:
			processEdit(command, task);
			break;
		case UNDO:
			// negate action and run excecuteAction again
			processUndo();
			break;
		case DONE:
			processDone(command, task);
			break;
		case UNDONE:
			processUndone(command, task);
			break;
		case LOGIN:
			processLogin(command);
			break;
		case EXIT:
			processExit();
			break;
		case INVALID:
			processInvalid(command);
			break;
		default:
			//Assuming InputParser will always pass a Action object
			//code will never reach here.
			assert false;
			
		}
	}

	private void processInvalid(Action command) {
		//Check whether Action is a command, if is command call GUI to display on textbox

		//showStatus to user
		showStatusToUser(command, false);
		//log down invalid input to log file
		logErrorCommand();
		
	}

	private void logErrorCommand() {
		_logger.logError(String.format(Messages.LOG_MESSAGE_INVALID_COMMAND, Messages.LOG_MESSAGE_ERROR));
	}

	private void processExit() {
		//Add in GUI code to close, storage close	
		System.exit(Settings.EXIT_APPLICATION_NO);
	}

	private void processLogin(Action command) {
		assert command != null;
		boolean isSuccess;
		isSuccess = _storageHandler.initCloudStorage();
		this.showStatusToUser(command, isSuccess);
	}

	private void processDone(Action command, Task task) {
		assert command!=null && task != null;
		task.setDone(true);
		processEdit(command, task);
	}

	private void processUndone(Action command, Task task) {
		assert command!=null && task != null;
		task.setDone(false);
		processEdit(command, task);
	}

	private void processUndo() {
		Action lastAction = getLastAction();
		Action undoAction = lastAction.undo();
		undoAction.setUndo(lastAction);
		executeAction(undoAction);
	}

	private void processEdit(Action command, Task task) {
		assert command!=null && task != null;
		boolean isSuccess;
		isSuccess = _storageHandler.modifyTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void processDisplay(Task task) {
		assert task!=null;
		clearUi();
		FilterTasks.filter(task);
		_gui.setFilterText(task.getDescription());
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void processDelete(Action command, Task task) {
		assert command!=null && task != null;
		Task deletedTask;
		boolean isSuccess;
		deletedTask = _storageHandler.deleteTask(task);
		if (deletedTask == null) {
			isSuccess = false;
		} else {
			isSuccess = true;

		}
		filterAndDisplay(command, isSuccess);
	}

	private void processAdd(Action command, Task task) {
		assert command!=null && task != null;
		boolean isSuccess;
		isSuccess = _storageHandler.addTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void filterAndDisplay(Action command, boolean isSuccess) {
		assert command!=null;
		// set lastAction
		this.setLastAction(command);
		clearUi();
		FilterTasks.filter();
		showStatusToUser(command, isSuccess);
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void showStatusToUser(Action command, boolean isSuccess) {
		assert command!=null;
		if (isSuccess == true) {
			DisplayView.showStatusToUser(Settings.StatusType.MESSAGE, command, _gui);
		} else {
			DisplayView.showStatusToUser(Settings.StatusType.ERROR, command, _gui);
		}
	}

	public Action getLastAction() {
		return _lastAction;
	}

	public void setLastAction(Action lastAction) {
		this._lastAction = lastAction;
	}

}

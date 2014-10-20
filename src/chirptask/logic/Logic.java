package chirptask.logic;

import java.util.List;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

//@author A0111930W
public class Logic {

	private GroupAction _lastAction;
	private InputParser _parser;
	private StorageHandler _storageHandler;
	private static MainGui _gui;

	public Logic(MainGui gui) {
		_storageHandler = new StorageHandler();
		// This will enable auto login uncomment this to allow auto login
		// _storageHandler.initCloudStorage();
		_parser = new InputParser();
		_gui = gui;
		FilterTasks.filter();
		DisplayView.updateTaskView(_gui);
	}

	private static void clearUi() {
		_gui.clearTrendingList();
		_gui.clearTaskView();
	}

	public void retrieveInputFromUI(String input) {
		_parser.receiveInput(input);
		// Assuming there will always be GroupActions parse by InputParser every
		// user input.
		assert _parser.getActions() != null;
		
		if(_parser.getActions().getActionList().get(0).getCommandType()!=Settings.CommandType.UNDO){
			setLastGroupAction(_parser.getActions());
		}
		
		processGroupAction(_parser.getActions().getActionList());
	}

	public void processGroupAction(List<Action> list) {

		for (Action a : list) {
			//System.out.println("Hello");
			executeAction(a);

		}
	}

	// Will take in Action object
	public void executeAction(Action command) {
		Settings.CommandType actionType = command.getCommandType();
		assert actionType != null;
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
			processDisplay(command, task);
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
			// Assuming InputParser will always pass a Action object
			// code will never reach here.
			assert false;

		}
	}

	private void processInvalid(Action command) {
		// Check whether Action is a command, if is command call GUI to display
		// on textbox
		// showStatus to user
		showStatusToUser(command, false);
		// log down invalid input to log file
		logErrorCommand();

	}

	private void logErrorCommand() {
		StorageHandler.logError(String.format(
				Messages.LOG_MESSAGE_INVALID_COMMAND,
				Messages.LOG_MESSAGE_ERROR));
	}

	private void processExit() {
		// Add in GUI code to close, storage close
		System.exit(Settings.EXIT_APPLICATION_NO);
	}

	private void processLogin(Action command) {
		assert command != null;
		boolean isSuccess;
		isSuccess = _storageHandler.initCloudStorage();
		this.showStatusToUser(command, isSuccess);
	}

	private void processDone(Action command, Task task) {
		assert command != null && task != null;
		task.setDone(true);
		processEdit(command, task);
	}

	private void processUndone(Action command, Task task) {
		assert command != null && task != null;
		task.setDone(false);
		processEdit(command, task);
	}

	private void processUndo() {
		GroupAction lastAction = getLastGroupAction();
		GroupAction tempGroupAction = new GroupAction();

		for (Action action : lastAction.getActionList()) {
			Action undoAction = action.undo();
			undoAction.setUndo(action);
			tempGroupAction.addAction(undoAction);

		}
		setLastGroupAction(tempGroupAction);
		lastAction = getLastGroupAction();
		for (Action action : lastAction.getActionList()) {
			executeAction(action);
		}
	}

	private void processEdit(Action command, Task task) {
		assert command != null && task != null;
		boolean isSuccess;
		task.setModified(true);
		isSuccess = _storageHandler.modifyTask(task);
		filterAndDisplay(command, isSuccess);
	}

	private void processDisplay(Action command, Task task) {
		assert task != null;
		clearUi();
		FilterTasks.filter(task, _gui);
		_gui.setFilterText(task.getDescription());
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void processDelete(Action command, Task task) {
		assert command != null && task != null;
		Task deletedTask;
		boolean isSuccess;
		task.setDeleted(true);
		deletedTask = _storageHandler.deleteTask(task);
		if (deletedTask == null) {
			isSuccess = false;
		} else {
			isSuccess = true;

		}
		filterAndDisplay(command, isSuccess);
	}

	private void processAdd(Action command, Task task) {
		assert command != null && task != null;
		boolean isSuccess;
		isSuccess = _storageHandler.addTask(task);
		filterAndDisplay(command, isSuccess);
	}

	public static void refresh() {
		clearUi();
		FilterTasks.filter();
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void filterAndDisplay(Action command, boolean isSuccess) {
		assert command != null;
		// set lastAction

		clearUi();
		FilterTasks.filter();
		showStatusToUser(command, isSuccess);
		DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
	}

	private void showStatusToUser(Action command, boolean isSuccess) {
		assert command != null;
		if (isSuccess == true) {
			DisplayView.showStatusToUser(Settings.StatusType.MESSAGE, command,
					_gui);
		} else {
			DisplayView.showStatusToUser(Settings.StatusType.ERROR, command,
					_gui);
		}
	}

	public GroupAction getLastGroupAction() {
		return _lastAction;
	}

	public void setLastGroupAction(GroupAction lastAction) {
		this._lastAction = lastAction;
	}

}

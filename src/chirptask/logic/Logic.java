package chirptask.logic;

import java.util.Calendar;
import java.util.List;

import org.jnativehook.GlobalScreen;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

//@author A0111930W
public class Logic {

    private static final String FLOATING = "floating";
    private GroupAction _lastAction;
    private InputParser _parser = new InputParser();
    private StorageHandler _storageHandler = new StorageHandler();
    private static MainGui _gui;

    // For testing purpose - commend out when product is done testing
    public Logic() {

    }

    public Logic(MainGui gui) {
        // This will enable auto login uncomment this to allow auto login
        // _storageHandler.initCloudStorage();
        _gui = gui;

        FilterTasks.filter(_gui);
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

        if (_parser.getActions().getActionList().get(0).getCommandType() != Settings.CommandType.UNDO) {
            setLastGroupAction(_parser.getActions());
        }

        processGroupAction(_parser.getActions().getActionList());
    }

    public void processGroupAction(List<Action> list) {

        for (Action a : list) {
            // System.out.println("Hello");
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
        case CLEAR:
            processClear(StorageHandler.getAllTasks());
            break;
        case SYNC:
            processSync(command);
            break;
        case LOGOUT:
            processLogout(command);
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

    private void processLogout(Action command) {
        assert command != null;
        boolean isSuccess;
        // should return a boolean variable to state whether sync is successful
        isSuccess = _storageHandler.logout();
        this.showStatusToUser(command, isSuccess);

    }

    private void processSync(Action command) {
        assert command != null;
        boolean isSuccess;
        // should return a boolean variable to state whether sync is successful
        isSuccess = StorageHandler.sync();
        this.showStatusToUser(command, isSuccess);
    }

    public void processClear(List<Task> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isDone()) {
                processDelete(Settings.CommandType.DELETE, list.get(i));
            }
        }

    }

    private void processDelete(CommandType delete, Task t) {
        Task deletedTask;
        boolean isSuccess;
        t.setDeleted(true);
        deletedTask = _storageHandler.deleteTask(t);
        if (deletedTask == null) {
            isSuccess = false;
        } else {
            isSuccess = true;

        }
        filterAndDisplay(delete, isSuccess);

    }

    private void filterAndDisplay(CommandType delete, boolean isSuccess) {
        clearUi();
        FilterTasks.filter(_gui);
        showStatusToUser(delete, isSuccess);
        DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);

    }

    private void showStatusToUser(CommandType delete, boolean isSuccess) {
        if (isSuccess == true) {
            DisplayView.showStatusToUser(Settings.StatusType.MESSAGE, delete,
                    _gui);
        } else {
            DisplayView.showStatusToUser(Settings.StatusType.ERROR, delete,
                    _gui);
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
        GlobalScreen.unregisterNativeHook();
        System.runFinalization();
        System.exit(Settings.SYSTEM_EXIT_NORMAL);
    }

    private void processLogin(Action command) {
        assert command != null;
        boolean isSuccess;
        isSuccess = _storageHandler.initCloudStorage();
        this.showStatusToUser(command, isSuccess);
    }

    private void processDone(Action command, Task task) {
        assert command != null && task != null;
        if (task.getType().equalsIgnoreCase(FLOATING)) {
            task.setDate();
        }
        task.setDone(true);
        processEdit(command, task);
    }

    private void processUndone(Action command, Task task) {
        assert command != null && task != null;
        if (task.getType().equalsIgnoreCase(FLOATING)) {
            task.removeDate();
        }
        task.setDone(false);
        processEdit(command, task);
    }

    private void processUndo() {
        GroupAction lastAction = getLastGroupAction();
        GroupAction tempGroupAction = new GroupAction();
        if (lastAction != null) {
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

        } else {
            // showstatus
            DisplayView.showStatusToUser(Messages.LOG_MESSAGE_UNDO_NOTHING,
                    _gui);
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

    public synchronized void refreshUi() {
        clearUi();
        FilterTasks.filter(_gui);
        DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
    }

    public static void refresh() {
        _gui.refreshUI();
    }

    private void filterAndDisplay(Action command, boolean isSuccess) {
        assert command != null;

        clearUi();
        FilterTasks.filter(_gui);
        DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
        showStatusToUser(command, isSuccess);
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

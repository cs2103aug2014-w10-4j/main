package chirptask.logic;

import java.util.ArrayList;
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
    private static final String STRING_DONE = "[Done]";

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
        DisplayView
                .showStatusToUser(Messages.LOG_MESSAGE_INVALID_COMMAND, _gui);
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
            case ADD :
                processAdd(command, task);
                break;
            case DELETE :
                processDelete(command, task);
                break;
            case DISPLAY :
                // now can only filter string
                processDisplay(command, task);
                break;
            case EDIT :
                processEdit(command, task);
                break;
            case UNDO :
                // negate action and run excecuteAction again
                processUndo();
                break;
            case DONE :
                processDone(command, task);
                break;
            case UNDONE :
                processUndone(command, task);
                break;
            case LOGIN :
                processLogin(command);
                break;
            case EXIT :
                processExit();
                break;
            case CLEAR :
                processClear(StorageHandler.getAllTasks());
                break;
            case SYNC :
                processSync(command);
                break;
            case LOGOUT :
                processLogout(command);
                break;
            case INVALID :
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
        
        if (isSuccess) {
            setOnlineStatus(Messages.TITLE_OFFLINE);
        }
    }

    private void processSync(Action command) {
        assert command != null;
        boolean isSuccess;
        // should return a boolean variable to state whether sync is successful
        isSuccess = StorageHandler.sync();
        this.showStatusToUser(command, isSuccess);
    }

    private void processClear(List<Task> list) {
        List<Task> clearList = new ArrayList<Task>();
        for (int i = 0; i < list.size(); i++) {
            Task currentTask = list.get(i);
            if (currentTask.isDone()) {
                clearList.add(currentTask);
            }
        }

        for (int i = 0; i < clearList.size(); i++) {
            processDelete(Settings.CommandType.DELETE, clearList.get(i));
        }
    }

    private void processDelete(CommandType delete, Task t) {
        Task deletedTask;
        boolean isSuccess = false;
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
        Settings.CommandType type = command.getInvalidCommandType();

        processInvalidTypes(type);
        // Show proper useage of command
        // showStatusToUser()
        if (type != Settings.CommandType.INVALID) {
            _gui.setUserInputText(command.getUserInput());
        }
        // log down invalid input to log file
        logErrorCommand();

    }

    private void processInvalidTypes(Settings.CommandType type) {
        switch (type) {
            case ADD :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_ADD_USAGE,
                        _gui, false);
                break;
            case DELETE :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_DELETE_USAGE,
                        _gui, false);
                break;
            case DISPLAY :
                DisplayView.showStatusToUser(
                        Messages.LOG_MESSAGE_DISPLAY_USAGE, _gui, false);

                break;
            case EDIT :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_EDIT_USAGE,
                        _gui, false);

                break;
            case UNDO :
                // negate action and run excecuteAction again
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_UNDO_USAGE,
                        _gui, false);

                break;
            case DONE :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_DONE_USAGE,
                        _gui, false);

                break;
            case UNDONE :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_UNDONE_USAGE,
                        _gui, false);

                break;
            case LOGIN :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_LOGIN_USAGE,
                        _gui, false);
                break;
            case CLEAR :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_CLEAR_USAGE,
                        _gui, false);

                break;
            case SYNC :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_SYNC_USAGE,
                        _gui, false);
                break;
            case LOGOUT :
                DisplayView.showStatusToUser(Messages.LOG_MESSAGE_LOGOUT_USAGE,
                        _gui, false);
                break;
            case INVALID :
                DisplayView.showStatusToUser(
                        Messages.LOG_MESSAGE_INVALID_COMMAND, _gui, false);
                break;
            default:
                assert false;
                break;
        }
    }

    private void logErrorCommand() {
        StorageHandler.logError(String.format(
                Messages.LOG_MESSAGE_INVALID_COMMAND,
                Messages.LOG_MESSAGE_ERROR));
    }

    private void processExit() {
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook();
        }
        _storageHandler.closeStorages();
        System.runFinalization();
        System.exit(Settings.SYSTEM_EXIT_NORMAL);
    }

    private void processLogin(Action command) {
        assert command != null;
        boolean isSuccess;
        isSuccess = _storageHandler.initCloudStorage();
        
        if (isSuccess) {
            setOnlineStatus(Messages.TITLE_LOGGING_IN);
        }
        
        this.showStatusToUser(command, isSuccess);
    }

    private void processDone(Action command, Task task) {
        assert command != null && task != null;
        if (Task.TASK_FLOATING.equalsIgnoreCase(task.getType())) {
            Calendar doneDate = Calendar.getInstance();
            task.setDate(doneDate);
        }
        task.setDone(true);
        processEdit(command, task);
    }

    private void processUndone(Action command, Task task) {
        assert command != null && task != null;
        if (Task.TASK_FLOATING.equalsIgnoreCase(task.getType())) {
            task.removeDate();
        }

        task.setDone(false);
        processEdit(command, task);
    }

    private void processUndo() {
        GroupAction lastAction = getLastGroupAction();
        GroupAction tempGroupAction = new GroupAction();

        if (lastAction != null) {
            for (int i = 0; i < lastAction.getActionList().size(); i++) {
                Action action = lastAction.getActionList().get(i);

                if (action.undo() != null) {
                    Action undoAction = action.undo();
                    undoAction.setUndo(action);
                    tempGroupAction.addAction(undoAction);
                } else {
                    DisplayView.showStatusToUser(
                            Messages.LOG_MESSAGE_UNDO_NOTHING, _gui);
                }
            }
            setLastGroupAction(tempGroupAction);
            lastAction = getLastGroupAction();
            for (int i = 0; i < lastAction.getActionList().size(); i++) {
                Action action = lastAction.getActionList().get(i);
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

        if (!task.getGoogleId().isEmpty()) {
            if (Task.TASK_TIMED.equalsIgnoreCase(task.getType()) && task.isDone()) {
                processEditDone(task);

            } else if (Task.TASK_TIMED.equalsIgnoreCase(task.getType())
                    && !task.isDone()) {
                processEditUndone(task);
            }
        }
        boolean isSuccess;
        task.setModified(true);
        isSuccess = _storageHandler.modifyTask(task);
        filterAndDisplay(command, isSuccess);
    }

    private void processEditUndone(Task task) {
        if (task.getDescription().length() > 6) {
            // [Done]
            if (task.getDescription().substring(0, 6)
                    .equalsIgnoreCase(STRING_DONE)) {
                task.setDescription(task.getDescription().substring(7,
                        task.getDescription().length()));
            }
        }
    }

    private void processEditDone(Task task) {
        if (task.getDescription().length() > 6) {
            // [Done]
            if (!task.getDescription().substring(0, 6)
                    .equalsIgnoreCase(STRING_DONE)) {
                task.setDescription(STRING_DONE + " " + task.getDescription());
            }
        }
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
        boolean isSuccess = true;

        if ("".equals(task.getGoogleId())) {
            isSuccess = _storageHandler.addTask(task);
        } else {
            task.setDeleted(false);
            _storageHandler.modifyTask(task);
        }

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
    
    public static void setOnlineStatus(String status) {
        if (status != null) {
            _gui.setOnlineStatus(status);
        }
    }

    public GroupAction getLastGroupAction() {
        return _lastAction;
    }

    public void setLastGroupAction(GroupAction lastAction) {
        this._lastAction = lastAction;
    }

}

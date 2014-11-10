//@author A0111930W
package chirptask.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.jnativehook.GlobalScreen;

import chirptask.common.Constants;
import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class Logic {
    private static final String STRING_DONE = "[Done]";

    private GroupAction _lastAction;
    private InputParser _parser = new InputParser();
    private StorageHandler _storageHandler = new StorageHandler();
    private static MainGui _gui;
    private static final int FIRST_ACTION = 0;
    private static final int ZERO = 0;
    private static final int DESCRIPTION_LENGTH = 6;
    private static final int SUB_DESCRIPTION_LEN = 7;

    /**
     * Init of logic which will display all tasks and show user what are the
     * available commands for the program.
     * 
     * @param gui
     */
    public Logic(MainGui gui) {
        _gui = gui;
        FilterTasks.filter(_gui);
        DisplayView.updateTaskView(_gui);
        DisplayView
                .showStatusToUser(Constants.LOG_MESSAGE_INVALID_COMMAND, _gui);
    }

    /**
     * Refresh the task view and trending list
     */
    private static void clearUi() {
        _gui.clearTrendingList();
        _gui.clearTaskView();
    }

    /**
     * Method for GUI to call to interpret user input and display the command
     * results.
     * 
     * @param input
     */
    public void retrieveInputFromUI(String input) {
        _parser.receiveInput(input);
        assert _parser.getActions() != null;

        if (isNotUndoCommand()) {
            setLastGroupAction(_parser.getActions());
        }

        processGroupAction(_parser.getActions().getActionList());
    }

    /**
     * Check whether action receive by inputParser contains undo command.
     * 
     * @return
     */
    private boolean isNotUndoCommand() {
        return _parser.getActions().getActionList().get(FIRST_ACTION)
                .getCommandType() != Settings.CommandType.UNDO;
    }

    /**
     * Execute the list of actions
     * 
     * @param list
     */
    public void processGroupAction(List<Action> list) {
        for (Action a : list) {
            executeAction(a);
        }
    }

    /**
     * Execute the command given.
     * 
     * @param command
     */
    public void executeAction(Action command) {
        Settings.CommandType actionType = command.getCommandType();
        assert actionType != null;
        Task task = command.getTask();
        interpretCommandAndExecute(command, actionType, task);
    }

    /**
     * Interpret and execute the command.
     * 
     * @param command
     * @param actionType
     * @param task
     */
    private void interpretCommandAndExecute(Action command,
            Settings.CommandType actionType, Task task) {
        switch (actionType) {
        case ADD:
            processAdd(command, task);
            break;
        case DELETE:
            processDelete(command, task);
            break;
        case DISPLAY:
            processDisplay(command, task);
            break;
        case EDIT:
            processEdit(command, task);
            break;
        case UNDO:
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
            assert false;
        }
    }

    private void processLogout(Action command) {
        assert command != null;
        boolean isSuccess;
        isSuccess = _storageHandler.logout();
        this.showStatusToUser(command, isSuccess);
    }

    private void processSync(Action command) {
        assert command != null;
        boolean isSuccess;
        isSuccess = StorageHandler.sync();
        this.showStatusToUser(command, isSuccess);
    }

    /**
     * Clear out all the done task from the list.
     * 
     * @param list
     */
    private void processClear(List<Task> list) {
        List<Task> clearList = new ArrayList<Task>();
        populateDoneTasks(list, clearList);
        clearDoneTasks(clearList);
    }

    /**
     * Delete the all done tasks from the list
     * 
     * @param clearList
     */
    private void clearDoneTasks(List<Task> clearList) {
        for (int i = ZERO; i < clearList.size(); i++) {
            processDelete(Settings.CommandType.DELETE, clearList.get(i));
        }
    }

    /**
     * 
     * @param list
     * @param clearList
     */
    private void populateDoneTasks(List<Task> list, List<Task> clearList) {
        for (int i = ZERO; i < list.size(); i++) {
            Task currentTask = list.get(i);
            if (currentTask.isDone()) {
                clearList.add(currentTask);
            }
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
            displaySuccess(delete);
        } else {
            displayError(delete);
        }

    }

    private void displayError(CommandType delete) {
        DisplayView.showStatusToUser(Settings.StatusType.ERROR, delete, _gui);
    }

    private void displaySuccess(CommandType delete) {
        DisplayView.showStatusToUser(Settings.StatusType.MESSAGE, delete, _gui);
    }

    /**
     * Process the invalid actions by user, usage will be display accordingly by
     * the type of command. The wrong command will also be display in the input
     * box for easy editing.
     * 
     * @param command
     */
    private void processInvalid(Action command) {

        Settings.CommandType type = interpreteInvalidCommand(command);
        if (type != Settings.CommandType.INVALID) {
            _gui.setUserInputText(command.getUserInput());
        }
        logErrorCommand();

    }

    private Settings.CommandType interpreteInvalidCommand(Action command) {
        Settings.CommandType type = command.getInvalidCommandType();
        processInvalidTypes(type);
        return type;
    }

    private void processInvalidTypes(Settings.CommandType type) {
        switch (type) {
        case ADD:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_ADD_USAGE, _gui,
                    false);
            break;
        case DELETE:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_DELETE_USAGE,
                    _gui, false);
            break;
        case DISPLAY:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_DISPLAY_USAGE,
                    _gui, false);

            break;
        case EDIT:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_EDIT_USAGE, _gui,
                    false);

            break;
        case UNDO:
            // negate action and run excecuteAction again
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_UNDO_USAGE, _gui,
                    false);

            break;
        case DONE:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_DONE_USAGE, _gui,
                    false);

            break;
        case UNDONE:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_UNDONE_USAGE,
                    _gui, false);

            break;
        case LOGIN:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_LOGIN_USAGE,
                    _gui, false);
            break;
        case CLEAR:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_CLEAR_USAGE,
                    _gui, false);

            break;
        case SYNC:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_SYNC_USAGE, _gui,
                    false);
            break;
        case LOGOUT:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_LOGOUT_USAGE,
                    _gui, false);
            break;
        case INVALID:
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_INVALID_COMMAND,
                    _gui, false);
            break;
        default:
            assert false;
            break;
        }
    }

    private void logErrorCommand() {
        StorageHandler.logError(String.format(
                Constants.LOG_MESSAGE_INVALID_COMMAND,
                Constants.LOG_MESSAGE_ERROR));
    }

    /**
     * Close all storages/utils and exit the program.
     */
    private void processExit() {
        nativeHookCleanUp();
        _storageHandler.closeStorages();
        systemExit();
    }

    private void systemExit() {
        System.runFinalization();
        System.exit(Settings.SYSTEM_EXIT_NORMAL);
    }

    private void nativeHookCleanUp() {
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook();
        }
    }

    private void processLogin(Action command) {
        assert command != null;
        boolean isSuccess;
        isSuccess = StorageHandler.initCloudStorage();
        this.showStatusToUser(command, isSuccess);
    }

    /**
     * Method will mark task as done, and set the done date for task that are
     * floating.
     * 
     * @param command
     * @param task
     */
    private void processDone(Action command, Task task) {
        assert command != null && task != null;
        if (isFloatingTask(task)) {
            Calendar doneDate = Calendar.getInstance();
            task.setDate(doneDate);
        }
        task.setDone(true);
        processEdit(command, task);
    }

    private boolean isFloatingTask(Task task) {
        return Task.TASK_FLOATING.equalsIgnoreCase(task.getType());
    }

    /**
     * Method will mark task as undone, and remove the done date for task that
     * are floating.
     * 
     * @param command
     * @param task
     */
    private void processUndone(Action command, Task task) {
        assert command != null && task != null;
        if (isFloatingTask(task)) {
            task.removeDate();
        }
        task.setDone(false);
        processEdit(command, task);
    }

    /**
     * Process the undo function populate the undo action list, method will the
     * set the list as an lastgroupaction once that is set the method will
     * execute each action as if enter by user.
     * 
     */
    private void processUndo() {
        GroupAction lastAction = getLastGroupAction();
        GroupAction tempGroupAction = new GroupAction();

        if (isLastActionNotNull(lastAction)) {
            populateUndoAction(lastAction, tempGroupAction);
            setLastGroupAction(tempGroupAction);
            lastAction = getLastGroupAction();
            undoGroupAction(lastAction);

        } else {
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_UNDO_NOTHING,
                    _gui);
        }
    }

    private void populateUndoAction(GroupAction lastAction,
            GroupAction tempGroupAction) {
        for (int i = ZERO; i < lastAction.getActionList().size(); i++) {
            Action action = lastAction.getActionList().get(i);
            populateUndoActionList(tempGroupAction, action);
        }
    }

    private void populateUndoActionList(GroupAction tempGroupAction,
            Action action) {
        if (isUndoActionNotNull(action)) {
            setUndoAndAddToGroupAction(tempGroupAction, action);
        } else {
            DisplayView.showStatusToUser(Constants.LOG_MESSAGE_UNDO_NOTHING,
                    _gui);
        }
    }

    /**
     * Execute the undo actions from the given list.
     * 
     * @param lastAction
     */
    private void undoGroupAction(GroupAction lastAction) {
        for (int i = ZERO; i < lastAction.getActionList().size(); i++) {
            Action action = lastAction.getActionList().get(i);
            executeAction(action);
        }
    }

    /**
     * Set the undo action and set the undo undo action. Meaning user is able to
     * undo only the last action. Eg, add -> undo -> <delete> -> undo -> add
     * 
     * @param tempGroupAction
     * @param action
     */
    private void setUndoAndAddToGroupAction(GroupAction tempGroupAction,
            Action action) {
        Action undoAction = action.undo();
        undoAction.setUndo(action);
        tempGroupAction.addAction(undoAction);
    }

    private boolean isUndoActionNotNull(Action action) {
        return action.undo() != null;
    }

    private boolean isLastActionNotNull(GroupAction lastAction) {
        return lastAction != null;
    }

    /**
     * Method for processing the task for edit. Method will further process
     * googleID task.
     * 
     * @param command
     * @param task
     */
    private void processEdit(Action command, Task task) {
        assert command != null && task != null;
        processGoogleIdTasks(task);
        boolean isSuccess;
        task.setModified(true);
        isSuccess = _storageHandler.modifyTask(task);
        filterAndDisplay(command, isSuccess);
    }

    /**
     * Edit a done timed google task will auto include a [Done] infront of the
     * task description. Eg, edit 1 abc -> [Done] abc for undone, edit 1 abc ->
     * abc
     * 
     * @param task
     */
    private void processGoogleIdTasks(Task task) {
        if (!isGoogleIdEmpty(task)) {
            if (isDoneTimedTasked(task)) {
                processEditDone(task);
            } else if (isUndoneTimedTask(task)) {
                processEditUndone(task);
            }
        }
    }

    private boolean isUndoneTimedTask(Task task) {
        return Task.TASK_TIMED.equalsIgnoreCase(task.getType())
                && !task.isDone();
    }

    private boolean isDoneTimedTasked(Task task) {
        return Task.TASK_TIMED.equalsIgnoreCase(task.getType())
                && task.isDone();
    }

    private boolean isGoogleIdEmpty(Task task) {
        return task.getGoogleId().isEmpty();
    }

    /**
     * Remove the [Done] from description if task is undone.
     * 
     * @param task
     */
    private void processEditUndone(Task task) {
        if (isValidDescriptionLength(task)) {
            if (containsDoneInDescription(task)) {
                removeDoneDescription(task);
            }
        }
    }

    private void removeDoneDescription(Task task) {
        task.setDescription(task.getDescription().substring(
                SUB_DESCRIPTION_LEN, task.getDescription().length()));
    }

    private boolean containsDoneInDescription(Task task) {
        return task.getDescription().substring(0, DESCRIPTION_LENGTH)
                .equalsIgnoreCase(STRING_DONE);
    }

    private boolean isValidDescriptionLength(Task task) {
        return task.getDescription().length() > DESCRIPTION_LENGTH;
    }

    /**
     * Include done description for task
     * 
     * @param task
     */
    private void processEditDone(Task task) {
        if (isValidDescriptionLength(task)) {
            if (!containsDoneInDescription(task)) {
                includeDoneDescription(task);
            }
        }
    }

    private void includeDoneDescription(Task task) {
        task.setDescription(STRING_DONE + " " + task.getDescription());
    }

    /**
     * Method will refresh UI, refresh tasklist and display filter string on
     * filter bar task list will also be updated according to the filter user
     * enters.
     * 
     * @param command
     * @param task
     */
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

    /**
     * Method will add the task to all storages, this method also process
     * googleId Task and set the deleted flag to false by default.
     * 
     * @param command
     * @param task
     */
    private void processAdd(Action command, Task task) {
        assert command != null && task != null;
        boolean isSuccess = true;

        if (isGoogleIdEmpty(task)) {
            isSuccess = _storageHandler.addTask(task);
        } else {
            task.setDeleted(false);
            _storageHandler.modifyTask(task);
        }

        filterAndDisplay(command, isSuccess);
    }

    /**
     * Method for google syncing to refresh the tasklist when it has
     * successfully sync the tasks.
     */
    public synchronized void refreshUi() {
        if (_gui != null) {
            clearUi();
            FilterTasks.filter(_gui);
            DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
        }
    }

    public synchronized static void refresh() {
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

    /**
     * Method for Google component to call when sync is successful
     * 
     * @param status
     */
    public static void setOnlineStatus(String status) {
        if (status != null && _gui != null) {
            _gui.setOnlineStatus(status);
        }
    }

    public GroupAction getLastGroupAction() {
        return _lastAction;
    }

    public void setLastGroupAction(GroupAction lastAction) {
        this._lastAction = lastAction;
    }
    
    
    /**
     * Call this method for JUnit Tests to get a fresh local storage each run
     * This method also ensure that the FilteredList and DisplayView 
     * are updated with the tasks from JUnit Test XML.
     */
    public void useTestLocalStorage() {
        _storageHandler.setUpJUnitTestXmlWriter();
        FilterTasks.filter(_gui);
        DisplayView.updateTaskView(_gui);
    }

}

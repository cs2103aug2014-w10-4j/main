package chirptask.logic;

import chirptask.common.Settings;
import chirptask.storage.Task;

public class Action {
    private Settings.CommandType _commandType;
    // private String _commandType;
    private Task _task;
    private Action _negateAction;
    private String _userInput;
    private boolean isCommandType;

    public Action() {
        _commandType = Settings.CommandType.INVALID;
        _task = null;
        _negateAction = null;
    }

    public Action(Settings.CommandType commandType) {
        _commandType = commandType;
        _task = null;
        _negateAction = null;
    }

    public Action(Settings.CommandType commandType, Task task) {
        _commandType = commandType;
        _task = task;
        _negateAction = null;
    }

    public Action(Settings.CommandType commandType, Task task,
            Action negateAction) {
        _commandType = commandType;
        _task = task;
        _negateAction = negateAction;
    }

    public Settings.CommandType getCommandType() {
        return _commandType;
    }

    public void setCommandType(Settings.CommandType commandType) {
        _commandType = commandType;
    }

    public Task getTask() {
        return this._task;
    }

    public void setTask(Task task) {
        _task = task;
    }

    public Action undo() {
        // do some negation logic
        return _negateAction;
    }

    public void setUndo(Action negateAction) {
        _negateAction = negateAction;
    }

    //@author A0111930W
    public void setIsCommandType(boolean isCommand) {
        this.isCommandType = isCommand;
    }

    //@author A0111930W
    public boolean getIsCommandType() {
        return this.isCommandType;
    }

    public String toString() {
        String stringToReturn = _commandType.toString();

        if (_task != null) {
            stringToReturn += " " + String.valueOf(_task.getTaskId()) + " "
                    + _task.getDescription();
        }
        if (_negateAction != null) {
            stringToReturn += " [negation " + _negateAction.getCommandType()
                    + " " + String.valueOf(_negateAction.getTask().getTaskId())
                    + "]";
        }
        return stringToReturn;
    }

    public boolean equals(Object o) {
        if (o instanceof Action) {
            Action a = (Action) o;
            return this.getCommandType().equals(a.getCommandType())
                    && this.getTask() == null
                    && a.getTask() == null
                    || (this.getTask() != null && a.getTask() != null
                            && this.getTask().equals(a.getTask()) && this
                            .getTask().getDescription()
                            .equals(a.getTask().getDescription()));
        } else {
            return false;
        }
    }

    public String getUserInput() {
        return _userInput;
    }

    public void setUserInput(String userInput) {
        _userInput = userInput;
    }
}

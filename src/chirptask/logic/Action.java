package chirptask.logic;

import chirptask.storage.Task;

public class Action {
	private String _commandType;
	private Task _task;
	private Action _negateAction;

	public Action() {
		_commandType = new String();
		_task = null;
		_negateAction = null;
	}

	public Action(String commandType) {
		_commandType = commandType;
		_task = null;
		_negateAction = null;
	}

	public Action(String commandType, Task task) {
		_commandType = commandType;
		_task = task;
		_negateAction = null;
	}

	public Action(String commandType, Task task, Action negateAction) {
		_commandType = commandType;
		_task = task;
		_negateAction = negateAction;
	}

	public String getCommandType() {
		return this._commandType;
	}

	public void setCommandType(String commandType) {
		_commandType = commandType;
	}

	public Task getTask() {
		return this._task;
	}

	public void setTask(Task task) {
		_task = task;
	}

	public Action undo(Action undo) {
		// do some negation logic
		return _negateAction;
	}

	public void setUndo(Action negateAction) {
		_negateAction = negateAction;
	}

	public String toString() {
		String stringToReturn = _commandType;

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
}

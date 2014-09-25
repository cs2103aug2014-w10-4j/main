package chirptask.logic;

import chirptask.storage.Task;

public class Action {
	private String _commandType;
	private Task _task;
	private Action _negateAction;

	public String getCommandType() {
		return this._commandType;
	}

	public Task getTask() {
		return this._task;
	}

	public Action undo(Action undo) {
		// do some negation logic
		return _negateAction;
	}
	
}

package chirptask.logic;

import chirptask.storage.Task;

public class InputParser {
	private String _userInput;
	private Action _action;
	private static int _idGenerator = 0;
	
	public InputParser() {
		
	}
	
	public InputParser(String userInput) {
		_userInput = userInput;
		setAction();
	}
	
	private void setAction() {
		_action = new Action();
		_action.setCommandType(getCommandTypeString());
		
		Task taskToDo = null;
		
		if (!getParameter().equals("")) {
			taskToDo = new Task();
			_idGenerator++;
			taskToDo.setTaskId(_idGenerator);
			taskToDo.setDescription(getParameter());
		}
		_action.setTask(taskToDo);
		
		undoler();
	}
	
	public Action getAction() {
		return _action;
	}

	private String getCommandTypeString() {
		return _userInput.trim().split("\\s+")[0];
	}
	
	private String getParameter() {
		return _userInput.replace(getCommandTypeString(), "").trim();
	}
	
	
	public void undoler() {
		switch (_action.getCommandType()) {
		case "add":
			_action.setUndo(new Action("delete", _action.getTask()));
			break;
		case "delete":
			_action.setUndo(new Action("add", _action.getTask()));
			break;
		default:
			_action.setUndo(null);
		}
	}
}

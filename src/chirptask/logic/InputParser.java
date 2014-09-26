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
		String command = getCommandTypeString();
		_action = new Action();
		_action.setCommandType(command);
		
		switch (command) {
		case "add":
			_action.setTask(new Task(_idGenerator, getParameter()));
			_idGenerator++;
			break;
		case "edit":
			int taskId = getId(getParameter());
			_action.setTask(new Task(taskId, getTaskDescription(getParameter())));
			break;
		case "delete":
			_action.setTask(new Task(getId(getParameter()), ""));
		case "done":
			
		default:
			
		}
		
		
		
		
		undoler();
	}

	private String getTaskDescription(String parameter) {
		String description = parameter.replace(parameter.trim().split("\\s+")[0], "").trim();
		return description;
	}

	private int getId(String parameter) {
		String id = parameter.trim().split("\\s+")[0];
		return Integer.parseInt(id);
	}

	/**
	 * 
	 */
	
	public Action getAction() {
		return _action;
	}

	private String getCommandTypeString() {
		return _userInput.trim().split("\\s+")[0].toLowerCase();
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
			_action.setUndo(new Action("add", new Task(getId(getParameter()), "")));
			break;
		case "edit":
			_action.setUndo(new Action("edit", new Task(getId(getParameter()), "")));
		default:
			_action.setUndo(null);
		}
	}
}

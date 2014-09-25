package chirptask.logic;

import chirptask.storage.Task;

public class Action {
	private String commandType;
	private Task task;
	private Action negateAction;
	
	public String getCommandType(){
		return this.commandType;
	}
	
	public Task getTask(){
		return this.task;
	}
	
	public Action undo(Action undo){
		//do some negation logic
		return negateAction;
	}
}

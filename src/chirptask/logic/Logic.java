package chirptask.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;




//import chirptask.storage.Storage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

enum COMMAND_TYPE{
	ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, LOGIN, INVALID, EXIT	
}

public class Logic {
	private Action lastAction;
	private InputParser parser;
	private StorageHandler storageHandler;
	
	public Logic(){
		storageHandler = new StorageHandler();
		//lastAction = new Action();
		parser = new InputParser();
	}
	
	private COMMAND_TYPE determineCommandType(String commandTypeString){
		if (commandTypeString.equalsIgnoreCase("add")) {
			return COMMAND_TYPE.ADD;
		} else if (commandTypeString.equalsIgnoreCase("display")) {
			return COMMAND_TYPE.DISPLAY;
		} else if (commandTypeString.equalsIgnoreCase("delete")) {
		 	return COMMAND_TYPE.DELETE;
		} else if (commandTypeString.equalsIgnoreCase("edit")) {
		 	return COMMAND_TYPE.EDIT;
		} else if (commandTypeString.equalsIgnoreCase("undo")) {
		 	return COMMAND_TYPE.UNDO;
		} else if (commandTypeString.equalsIgnoreCase("done")) {
		 	return COMMAND_TYPE.DONE;
		} else if (commandTypeString.equalsIgnoreCase("login")) {
		 	return COMMAND_TYPE.LOGIN;
		} else if (commandTypeString.equalsIgnoreCase("exit")) {
			return COMMAND_TYPE.EXIT;
		} else {
			return COMMAND_TYPE.INVALID;
		} 
	}
	//Will take in Action object
	public void executeAction(Action command){
		String action = command.getCommandType();
		COMMAND_TYPE actionType = determineCommandType(action);
		Task task = command.getTask();
		switch (actionType){ 
			case ADD:
				storageHandler.addTask(task);
				this.setLastAction(command);
				break;
			case DELETE:
				storageHandler.deleteTask(task);
				this.setLastAction(command);
				break;
			case DISPLAY:
				updateTaskView();
				break;
			case EDIT:
				this.setLastAction(command);
				break;
			case UNDO:
				//negate action and run excecuteAction again
				executeAction(command.undo(this.getLastAction()));
				break;
			case DONE:
				this.setLastAction(command);
				break;
			case LOGIN:
				break;
			case EXIT:
				System.exit(0);
				break;
			case INVALID:
				//call print some invalid message
				break;
			default:
				//throw error
		}
	}
	
	//Filtering according to the UI tag
	public void filter(String tag){
		
	}
	
	/**
	 * This will update the taskview
	 * Retrieve alltasks, sort to date/time, 
	 * store into Arraylist of dates of arraylist of tasks
	 * */	
	public TaskView updateTaskView(){
		
		//Should change .getAllTasks() to arraylist?
		List<Task> allTasks = storageHandler.getAllTasks();
 		Collections.sort(allTasks);
 		TreeMap<Date, TasksByDate> map = new TreeMap<Date, TasksByDate>();
 		
		for (Task task : allTasks) {
			Date currDate = task.getDate();
			if (map.containsKey(currDate)) {
					map.get(currDate).addToTaskList(task);
			}
			else{
				TasksByDate dateTask = new TasksByDate();
				dateTask.setTaskDate(currDate);
				dateTask.addToTaskList(task);
				map.put(dateTask.getTaskDate(), dateTask);
			}
		}
		
		Iterator<Map.Entry<Date, TasksByDate>> it = map.entrySet().iterator();
		TaskView view = new TaskView();
		while(it.hasNext()){
			view.addToTaskView(it.next().getValue());
		}
		return view;
		
	}
	
	//Take in type, action
	public void showStatusToUser(Type type, Action action ){
		
	}

	public Action getLastAction() {
		return lastAction;
	}

	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}

}

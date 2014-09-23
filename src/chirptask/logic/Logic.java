package chirptask.logic;

import java.util.ArrayList;
import java.util.Collections;

import chirptask.storage.Storage;
import chirptask.storage.Task;

public class Logic {
	private static Action lastAction;
	private static InputParser parser;
	
	//Will take in Action object
	public void executeAction(Action command){
		String action = command.type();
		switch (action){ 
			case "add":
				break;
			case "delete":
				break;
			default:
				break;
		}
	}
	
	//Filtering according to the UI tag
	public void filter(UI tag){
		
	}
	
	/**
	 * This will update the taskview
	 * Retrieve alltasks, sort to date/time, 
	 * store into Arraylist of dates of arraylist of tasks
	 * */	
	public void updateTaskView(){
		Storage s = new localStorage();
		//Should change .getAllTasks() to arraylist?
		Task[] alltasks = s.getAllTasks();
		ArrayList<Task> sortedTasks = Collections.sort(alltasks);
		
	}
	
	//Take in type, action
	public void showStatusToUser(Type type, Action action ){
		
	}
	
	
}

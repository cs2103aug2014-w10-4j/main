package chirptask.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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
	public void filter(String tag){
		
	}
	
	/**
	 * This will update the taskview
	 * Retrieve alltasks, sort to date/time, 
	 * store into Arraylist of dates of arraylist of tasks
	 * */	
	public TaskView updateTaskView(){
		Storage s = new Storage();
		//Should change .getAllTasks() to arraylist?
		ArrayList<Task> allTasks = s.getAllTasks();
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
	}
	
	//Take in type, action
	public void showStatusToUser(Type type, Action action ){
		
	}
	
	
}

package chirptask.storage;

import java.util.List;

public class StorageHandler {
	private List<Task> allTasks;
	
	public List<Task> getAllTasks(){
		return this.allTasks;
	}
	
	public void addTask(Task addedTask){
		allTasks.add(addedTask);
		//add in code to do communicate with storage
	}
	
	public void deleteTask(Task deletedTask){
		allTasks.remove(deletedTask);
		//add in code to do communication with storage
	}
}

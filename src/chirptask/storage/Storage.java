package chirptask.storage;

import java.util.ArrayList;

public interface Storage {

	boolean storeNewTask(Task T);

	Task removeTask(Task T);

	boolean modifyTask(Task T);
	
	Task getTask(int taskId);
	
	// Task array or other data structure(?)
	ArrayList<Task> getAllTasks();

}

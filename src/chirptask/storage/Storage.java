package chirptask.storage;

public interface Storage {

	boolean addTask(Task T);

	Task deleteTask(Task T);

	boolean updateTask(Task T);
	
	Task getTask(int taskId);
	
	// Task array or other data structure(?)
	Task[] getAllTasks();

}

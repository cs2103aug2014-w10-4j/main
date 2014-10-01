package chirptask.storage;

import java.util.List;

/**
 *
 * @author Yeo Quan Yang
 * @MatricNo A0111889W
 * 
 */

public interface Storage {

	boolean storeNewTask(Task T);

	Task removeTask(Task T);

	boolean modifyTask(Task T);

	Task getTask(int taskId);

	// Task array or other data structure(?)
	List<Task> getAllTasks();

	void close();

}

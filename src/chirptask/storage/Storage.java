package chirptask.storage;

import java.util.ArrayList;

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
	ArrayList<Task> getAllTasks();

	void close();

}

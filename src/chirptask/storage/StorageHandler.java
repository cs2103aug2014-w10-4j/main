package chirptask.storage;

import java.util.List;

public class StorageHandler {
	private List<Storage> _listOfStorages;
	private List<Task> _allTasks;

	public List<Task> getAllTasks() {
		return this._allTasks;
	}

	public void addTask(Task addedTask) {
		_allTasks.add(addedTask);
		// add in code to do communicate with storage
	}

	public void deleteTask(Task deletedTask) {
		_allTasks.remove(deletedTask);
		// add in code to do communication with storage
	}
}

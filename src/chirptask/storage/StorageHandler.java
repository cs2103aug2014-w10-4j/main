package chirptask.storage;

import java.util.List;

public class StorageHandler {
	private List<Storage> _listOfStorages;
	private List<Task> _allTasks;

	public List<Task> getAllTasks() {
		return this._allTasks;
	}

	public void closeStorages() {
		for (Storage individualStorage : _listOfStorages) {
			individualStorage.close();
		}
	}

	public void modifyTask(Task modifiedTask) {
		if (_allTasks.contains(modifiedTask)) {
			int indexOfTask = _allTasks.indexOf(modifiedTask);
			_allTasks.add(indexOfTask, modifiedTask);
			_allTasks.remove(indexOfTask + 1);
		}

		for (Storage individualStorage : _listOfStorages) {
			individualStorage.modifyTask(modifiedTask);
		}
	}

	public void addTask(Task addedTask) {
		_allTasks.add(addedTask);
		// add in code to do communicate with storage
		for (Storage individualStorage : _listOfStorages) {
			individualStorage.storeNewTask(addedTask);
		}
	}

	public void deleteTask(Task deletedTask) {
		_allTasks.remove(deletedTask);
		// add in code to do communication with storage
		for (Storage individualStorage : _listOfStorages) {
			individualStorage.removeTask(deletedTask);
		}
	}
}

package chirptask.storage;

import java.util.List;

import chirptask.google.GoogleController;

public class StorageHandler {
    /** Global instance of ChirpTask's local copy. */
    private static List<Task> _allTasks;
    
	private List<Storage> _listOfStorages;
	private GoogleController _googleController;
	
	public static List<Task> getAllTasks() {
		return _allTasks;
	}
	
	public void initCloudStorage(){
		this._googleController = new GoogleController();
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
		for (Storage individualStorage : _listOfStorages) {
			individualStorage.storeNewTask(addedTask);
		}
	}

	public void deleteTask(Task deletedTask) {
		_allTasks.remove(deletedTask);
		for (Storage individualStorage : _listOfStorages) {
			individualStorage.removeTask(deletedTask);
		}
	}

}

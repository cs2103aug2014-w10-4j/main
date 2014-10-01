package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

import chirptask.google.GoogleController;

public class StorageHandler {
    /** Global instance of ChirpTask's local copy. */
    private static List<Task> _allTasks;
    
	private static List<Storage> _listOfStorages;
	private static Storage localStorage;
	private static Storage googleStorage;
	private static Storage eventStorage;
	
	public StorageHandler() {
	    initStorages();
	}
	
	private void initStorages() {
	    createStoragesList();
	    addLocalStorage();
	    addEventStorage();
	}
	
	public void initCloudStorage() {
        addGoogleStorage();
	}
	
	private void createStoragesList() {
        _listOfStorages = new ArrayList<Storage>();
	}
	
	private void addLocalStorage() {
        localStorage = new LocalStorage();
        _listOfStorages.add(localStorage);
	}
	
	private void addEventStorage() {
        eventStorage = new EventLogger();
        _listOfStorages.add(eventStorage);
	}
	
	private void addGoogleStorage() {
        googleStorage = new GoogleStorage();
        _listOfStorages.add(googleStorage);
	}
	
	public static List<Task> getAllTasks() {
		return _allTasks;
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
	
	static void updateGoogleId(Task modifiedTask) {
	    if (_allTasks.contains(modifiedTask)) {
            int indexOfTask = _allTasks.indexOf(modifiedTask);
            _allTasks.add(indexOfTask, modifiedTask);
            _allTasks.remove(indexOfTask + 1);
        }
        localStorage.modifyTask(modifiedTask);
        eventStorage.modifyTask(modifiedTask);
	}
	

}

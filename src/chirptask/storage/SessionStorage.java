//@author A0111840W
package chirptask.storage;

import java.util.ArrayList;
import java.util.List;

public class SessionStorage implements IStorage {

	private List<Task> _taskList = null;

	public SessionStorage() {
		_taskList = new ArrayList<Task>();
	}
	
	public void setTaskList(List<Task> list) {
		_taskList = list;
	}

    @Override
    public boolean storeNewTask(Task addTask) {
        addTask.setDeleted(false);
        _taskList.add(addTask);
        return true;
    }

    @Override
    public Task removeTask(Task removeTask) {
        boolean isRemoved = _taskList.remove(removeTask);
        if (isRemoved) {
            return removeTask;
        } else {
            return null;
        }
    }

    @Override
    public boolean modifyTask(Task modifyTask) {
        boolean isModified = false;
        
        if (_taskList.contains(modifyTask)) {
            int indexOfTask = _taskList.indexOf(modifyTask);
            _taskList.add(indexOfTask, modifyTask);
            Task removedTask = _taskList.remove(indexOfTask + 1);

            if (removedTask != null) {
                isModified = true;
            } else {
                isModified = false;
            }
        }
        
        return isModified;
    }

    @Override
    public Task getTask(int taskId) {
        if (taskId >= 0 || taskId < _taskList.size()) {
            Task foundTask = _taskList.get(taskId);
            return foundTask;
        } else {
            return null;
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return _taskList;
    }

    @Override
    public void close() {
        _taskList = null;
    }

}

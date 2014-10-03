package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.storage.GoogleStorage;

import com.google.api.services.tasks.model.Task;

public class ConcurrentHandler {

}


class ConcurrentAdd implements Callable<Task> {
    
    private chirptask.storage.Task _taskToAdd;
    
    protected ConcurrentAdd(chirptask.storage.Task taskToAdd) {
        if (taskToAdd == null) {
            _taskToAdd = null;
        } else {
            _taskToAdd = taskToAdd;
        }
    }
    
    public Task call() throws IOException, UnknownHostException {
        // String type = _taskToAdd.getDescription(); //Should have
        // taskToAdd.getType();
        String type = "floating";
        String task = _taskToAdd.getDescription();
        Date date = null;
        if (_taskToAdd.getDate() != null) {
            date = _taskToAdd.getDate();
        }
        Task addedGoogleTask = null;

        if (GoogleController.isGoogleLoaded()) {
            switch (type) {
            case "floating":
                addedGoogleTask = GoogleController.addFloatingTask(task);
                break;
            case "deadline":
                addedGoogleTask = GoogleController.addDeadlineTask(task, date);
                break;
            case "timed":
                break;
            default:
                break;
            }
            addGoogleIdToStorage(addedGoogleTask);
        }
        return addedGoogleTask;
    }
    
    private void addGoogleIdToStorage(Task googleTask) {
        addGoogleIdToCurrentTask(googleTask);
        GoogleStorage.addGoogleIdToStorage(_taskToAdd);
    }
    
    private void addGoogleIdToCurrentTask(Task googleTask) {
        String googleId = googleTask.getId();
        _taskToAdd.setGoogleId(googleId);
    }
}

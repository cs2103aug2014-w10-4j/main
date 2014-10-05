package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import com.google.api.services.tasks.model.Task;

class ConcurrentAdd implements Callable<Boolean> {

    private chirptask.storage.Task _taskToAdd;

    ConcurrentAdd(chirptask.storage.Task taskToAdd) {
        if (ConcurrentHandler.isNull(taskToAdd)) {
            _taskToAdd = null;
        } else {
            _taskToAdd = taskToAdd;
        }
    }

    public Boolean call() throws IOException, UnknownHostException {
        Boolean isAdded = false;
        if (ConcurrentHandler.isNull(_taskToAdd)) {
            return isAdded;
        }

        while (GoogleController.isGoogleLoaded() == false) {
            // wait until google is loaded in background
        }

        String type = _taskToAdd.getType();
        String task = _taskToAdd.getDescription();
        Date date = null;

        if (_taskToAdd.getDate() != null) {
            date = _taskToAdd.getDate();
        }
        Task addedGoogleTask = null;

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

        if (ConcurrentHandler.isNotNull(addedGoogleTask)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleTask, _taskToAdd);
            isAdded = true;
        }

        return isAdded;
    }
}

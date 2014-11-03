package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

class ConcurrentAdd implements Callable<Boolean> {

    private chirptask.storage.Task _taskToAdd;
    private GoogleController _gController;

    ConcurrentAdd(chirptask.storage.Task taskToAdd, GoogleController gController) {
        if (ConcurrentHandler.isNull(taskToAdd) || ConcurrentHandler.isNull(gController)) {
            _taskToAdd = null;
        } else {
            _taskToAdd = taskToAdd;
            _gController = gController;
        }
    }

    public Boolean call() throws UnknownHostException, IOException  {
        Boolean isAdded = false;
        if (ConcurrentHandler.isNull(_taskToAdd)) {
            return isAdded;
        }

        while (GoogleController.isGoogleLoaded() == false) {
            // wait until google is loaded in background
        }

        String type = _taskToAdd.getType();
        String task = _taskToAdd.getDescription();

        Task addedGoogleTask = null;
        Event addedGoogleEvent = null;

        switch (type) {
        case "floating":
            addedGoogleTask = GoogleController.addFloatingTask(task);
            break;
        case "deadline":
            Date dueDate = _taskToAdd.getDate().getTime();
            addedGoogleTask = GoogleController.addDeadlineTask(task, dueDate);
            break;
        case "timedtask":
            TimedTask timedTask = (TimedTask) _taskToAdd;
            Date startTime = timedTask.getStartTime().getTime();
            Date endTime = timedTask.getEndTime().getTime();
            addedGoogleEvent = GoogleController.addTimedTask(task, startTime, endTime);
            break;
        default:
            break;
        }
        
        if (ConcurrentHandler.isNotNull(addedGoogleTask)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleTask, _taskToAdd);
            ConcurrentHandler.addETagToStorage(addedGoogleTask, _taskToAdd);
            isAdded = true;
        } else if (ConcurrentHandler.isNotNull(addedGoogleEvent)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleEvent, _taskToAdd);
            ConcurrentHandler.addETagToStorage(addedGoogleEvent, _taskToAdd);
            isAdded = true;
        }

        boolean isDone = _taskToAdd.isDone();
        if (isDone) {
            _gController.modifyTask(_taskToAdd);
        }

        return isAdded;
    }
}


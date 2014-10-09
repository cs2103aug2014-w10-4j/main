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

    ConcurrentAdd(chirptask.storage.Task taskToAdd) {
        if (ConcurrentHandler.isNull(taskToAdd)) {
            _taskToAdd = null;
        } else {
            _taskToAdd = taskToAdd;
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
            Date dueDate = _taskToAdd.getDate();
            addedGoogleTask = GoogleController.addDeadlineTask(task, dueDate);
            break;
        case "timedtask":
            TimedTask timedTask = (TimedTask) _taskToAdd;
            Date startTime = timedTask.getStartTime();
            Date endTime = timedTask.getEndTime();
            addedGoogleEvent = GoogleController.addTimedTask(task, startTime, endTime);
            break;
        default:
            break;
        }

        if (ConcurrentHandler.isNotNull(addedGoogleTask)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleTask, _taskToAdd);
            isAdded = true;
        } else if (ConcurrentHandler.isNotNull(addedGoogleEvent)) {
            ConcurrentHandler.addGoogleIdToStorage(addedGoogleEvent, _taskToAdd);
            isAdded = true;
        }

        return isAdded;
    }
}


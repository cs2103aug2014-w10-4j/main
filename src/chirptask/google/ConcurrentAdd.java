package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;

import chirptask.google.GoogleController.Status;
import chirptask.storage.TimedTask;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;

class ConcurrentAdd implements Callable<Boolean> {

    private chirptask.storage.Task _taskToAdd;
    private static GoogleController _gController;
    private static TasksController _tasksController;
    private static CalendarController _calendarController;

    ConcurrentAdd(chirptask.storage.Task taskToAdd, 
            GoogleController gController, 
            TasksController tasksController, 
            CalendarController calController) {
        
        if (ConcurrentHandler.isNull(taskToAdd) || 
                ConcurrentHandler.isNull(gController) ||
                ConcurrentHandler.isNull(tasksController) ||
                ConcurrentHandler.isNull(calController)) {
            setAllNull();
        } else {
            setAllVars(taskToAdd, gController, tasksController, calController);
        }
    }
    
    private void setAllNull() {
        ConcurrentHandler.setNull(_taskToAdd);
        ConcurrentHandler.setNull(_gController);
        ConcurrentHandler.setNull(_calendarController);
        ConcurrentHandler.setNull(_tasksController);
    }
    
    private void setAllVars(chirptask.storage.Task task, 
            GoogleController gController,
            TasksController tController,
            CalendarController cController) {
        _taskToAdd = task;
        _gController = gController;
        _tasksController = tController;
        _calendarController = cController;
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
        case chirptask.storage.Task.TASK_FLOATING:
            addedGoogleTask = addFloatingTask(task);
            break;
        case chirptask.storage.Task.TASK_DEADLINE:
            Date dueDate = _taskToAdd.getDate().getTime();
            addedGoogleTask = addDeadlineTask(task, dueDate);
            break;
        case chirptask.storage.Task.TASK_TIMED:
            TimedTask timedTask = (TimedTask) _taskToAdd;
            Date startTime = timedTask.getStartTime().getTime();
            Date endTime = timedTask.getEndTime().getTime();
            addedGoogleEvent = addTimedTask(task, startTime, endTime);
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
        } else {
            GoogleController.setOnlineStatus(Status.SYNC_FAIL);
        }

        boolean isDone = _taskToAdd.isDone();
        if (isDone) {
            _gController.modifyTask(_taskToAdd);
        }

        return isAdded;
    }
    
 // Called by ConcurrentAdd
    /**
     * adds a floating task with the specified task title.
     * 
     * @param taskTitle
     *            The floating task description
     * @return The reference to the created Google Task object
     * @throws UnknownHostException
     *             If the host machine cannot reach Google.
     * @throws IOException
     *             If there are other errors when sending the request.
     */
    static Task addFloatingTask(String taskTitle) throws UnknownHostException,
            IOException {
        Task addedTask = _tasksController.addTask(taskTitle);
        return addedTask;
    }

    /**
     * adds a deadline task with the specified task title and due date.
     * 
     * @param taskTitle
     *            The deadline task description
     * @param date
     *            The due date
     * @return The reference to the created Google Task object
     * @throws UnknownHostException
     *             If the host machine cannot reach Google.
     * @throws IOException
     *             If there are other errors when sending the request.
     */
    static Task addDeadlineTask(String taskTitle, Date date)
            throws UnknownHostException, IOException {
        Task addedTask = _tasksController.addTask(taskTitle, date);
        return addedTask;
    }

    static Event addTimedTask(String taskTitle, Date startTime, Date endTime)
            throws UnknownHostException, IOException {
        Event addedEvent = _calendarController.addTimedTask(taskTitle,
                startTime, endTime);
        return addedEvent;
    }
}


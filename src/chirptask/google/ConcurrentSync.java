package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import chirptask.logic.InputParser;
import chirptask.storage.DeadlineTask;
import chirptask.storage.GoogleStorage;
import chirptask.storage.LocalStorage;
import chirptask.storage.TimedTask;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.Tasks;

class ConcurrentSync implements Callable<Boolean> {

    private static final String STRING_DONE_TASK = "completed";
    private static final String STRING_DONE_EVENT = "[Done]";

    private List<chirptask.storage.Task> _taskList = null;
    private GoogleController _gController = null;
    private CalendarController _calendarController = null;
    private TasksController _tasksController = null;

    
    ConcurrentSync(List<chirptask.storage.Task> allTasks, 
            GoogleController gController, 
            CalendarController calController, 
            TasksController tasksController) {
        if (allTasks != null && gController != null && 
                tasksController != null &&
                calController != null) {
            _taskList = allTasks;
            _gController = gController;
            _calendarController = calController;
            _tasksController = tasksController;
        } else {
            _taskList = null;
            _gController = null;
            _calendarController = null;
            _tasksController = null;
        }
    }

    public Boolean call() throws UnknownHostException, IOException  {
        boolean isSyncing = false;
        
        if (_taskList == null) {
            isSyncing = false;
            return isSyncing;
        }
        
        try {
            sync(_taskList);
        } catch (Exception allException) {
            
        }
        
        return isSyncing;
    }
    
    private void sync(List<chirptask.storage.Task> allTasks) throws Exception {
        if (allTasks != null) {
            syncPhaseOne(_taskList);
            syncPhaseTwo(_taskList);
            //weird bug happens here at phase three, isModified becomes false
            syncPhaseThree(_taskList); 
            syncPhaseFour(_taskList);
        }
    }
    
    /**
     * Phase one is a Google One-way synchronisation method.
     * 
     * Phase one adds local tasks without Google ID to Google; If the task has
     * been deleted, it does not perform the add operation. Phase one also
     * deletes tasks with Google ID that are flagged as deleted from the Google
     * account. Phase one also modify tasks with Google ID that are flagged as
     * modified
     * 
     * @param allTasks
     *            The local list of all tasks
     * @throws UnknownHostException
     *             If Google's servers cannot be reachable
     * @throws IOException
     *             If transmission is interrupted
     */
    private void syncPhaseOne(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {
        
        if (allTasks != null) {
            for (int i = 0; i < allTasks.size(); i++) {
                chirptask.storage.Task currTask = allTasks.get(i);
                String currGoogleId = currTask.getGoogleId();
                boolean isDeleted = currTask.isDeleted();
                
                if (currGoogleId == null || "".equals(currGoogleId)) {
                    if (!isDeleted) {
                        currTask.setModified(false);
                        _gController.add(currTask);
                    }
                } else {
                    if (isDeleted) {
                        _gController.removeTask(currTask);
                    } else {
                        boolean isModified = currTask.isModified();
                        if (isModified) {
                            _gController.modifyTask(currTask);
                        }
                    }
                }
            }
        }
    }

    private void syncPhaseTwo(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            Map<String, chirptask.storage.Task> googleIdMap = new TreeMap<String, chirptask.storage.Task>();
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();
            
            for (int i = 0; i < allTasks.size(); i++) {
                chirptask.storage.Task currTask = allTasks.get(i);
                String googleId = currTask.getGoogleId();

                if (googleId != null || "".equals(googleId)) {
                    googleIdMap.put(googleId, currTask);
                }
            }

            for (Event currEvent : events) {
                String gId = currEvent.getId();
                if (googleIdMap.containsKey(gId)) {
                    chirptask.storage.Task currTask = googleIdMap.get(gId);
                    String googleETag = currEvent.getEtag();
                    String localETag = currTask.getETag();
                    if (googleETag != null && localETag != null) {
                        if (!localETag.equals(googleETag)) {
                            if (!currTask.isModified()) { // push from remote to
                                                          // local
                                String eventDescription = currEvent
                                        .getSummary();
                                EventDateTime start = currEvent.getStart();
                                EventDateTime end = currEvent.getEnd();
                                Calendar startDate = DateTimeHandler
                                        .getCalendar(start);
                                Calendar endDate = DateTimeHandler
                                        .getCalendar(end);

                                boolean isDone = false;
                                if (eventDescription != null) {
                                    if (eventDescription.startsWith(STRING_DONE_EVENT)) {
                                        isDone = true;
                                    }
                                }

                                if (currTask instanceof chirptask.storage.TimedTask) {
                                    TimedTask timedTask = (TimedTask) currTask;
                                    timedTask.setDescription(eventDescription);
                                    timedTask.setStartTime(startDate);
                                    timedTask.setEndTime(endDate);
                                    timedTask.setDone(isDone);
                                    GoogleStorage.updateStorages(timedTask);
                                }
                            }
                        }
                    }
                }
            }

            for (Task currTask : taskList) {
                String gId = currTask.getId();
                if (googleIdMap.containsKey(gId)) {
                    chirptask.storage.Task chirpTask = googleIdMap.get(gId);
                    String googleETag = currTask.getEtag();
                    String localETag = chirpTask.getETag();
                    if (googleETag != null && localETag != null) {
                        if (!localETag.equals(googleETag)) {
                            if (!chirpTask.isModified()) { // push from remote
                                                           // to local
                                int taskId = chirpTask.getTaskId();
                                List<String> contextList = chirpTask
                                        .getContexts();
                                List<String> categoryList = chirpTask
                                        .getCategories();
                                String doneString = currTask.getStatus();
                                String eTag = googleETag;
                                String googleId = gId;
                                String taskDescription = currTask.getTitle();
                                DateTime dueDate = currTask.getDue();
                                
                                boolean isDone = false;
                                if (STRING_DONE_TASK.equalsIgnoreCase(doneString)) {
                                    isDone = true;
                                }

                                if (dueDate != null) {
                                    Calendar dueCalendar = DateTimeHandler
                                            .getDateFromDateTime(dueDate);
                                    DeadlineTask newDeadline = new DeadlineTask(
                                            taskId, taskDescription,
                                            dueCalendar);
                                    newDeadline.setCategories(categoryList);
                                    newDeadline.setContexts(contextList);
                                    newDeadline.setDone(isDone);
                                    newDeadline.setETag(eTag);
                                    newDeadline.setGoogleId(googleId);
                                    GoogleStorage.updateStorages(newDeadline);
                                } else {
                                    chirptask.storage.Task newFloating = new chirptask.storage.Task(
                                            taskId, taskDescription);
                                    newFloating.setCategories(categoryList);
                                    newFloating.setContexts(contextList);
                                    newFloating.setDone(isDone);
                                    newFloating.setETag(eTag);
                                    newFloating.setGoogleId(googleId);
                                    GoogleStorage.updateStorages(newFloating);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void syncPhaseThree(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            Map<String, chirptask.storage.Task> googleIdMap = new TreeMap<String, chirptask.storage.Task>();
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();

            for (int i = 0; i < allTasks.size(); i++) {
                chirptask.storage.Task currTask = allTasks.get(i);
                String googleId = currTask.getGoogleId();

                if (googleId != null || "".equals(googleId)) {
                    googleIdMap.put(googleId, currTask);
                }
            }

            for (Event currEvent : events) {
                String gId = currEvent.getId();
                if (!googleIdMap.containsKey(gId)) {
                    int taskId = LocalStorage.generateId();
                    String description = currEvent.getSummary();
                    chirptask.storage.Task newTask = InputParser
                            .getTaskFromString(description);

                    String googleId = gId;
                    String googleETag = currEvent.getEtag();
                    List<String> contextList = newTask.getContexts();
                    List<String> categoryList = newTask.getCategories();
                    EventDateTime start = currEvent.getStart();
                    EventDateTime end = currEvent.getEnd();
                    Calendar startDate = DateTimeHandler.getCalendar(start);
                    Calendar endDate = DateTimeHandler.getCalendar(end);

                    boolean isDone = false;
                    if (description != null) {
                        if (description.startsWith(STRING_DONE_EVENT)) {
                            isDone = true;
                        }
                    }

                    TimedTask newTimed = new TimedTask(taskId, description,
                            startDate, endDate);
                    newTimed.setContexts(contextList);
                    newTimed.setCategories(categoryList);
                    newTimed.setGoogleId(googleId);
                    newTimed.setETag(googleETag);
                    newTimed.setDone(isDone);
                    GoogleStorage.updateStorages(newTimed);
                }
            }

            for (Task currTask : taskList) {
                String gId = currTask.getId();
                if (!googleIdMap.containsKey(gId)) {
                    int taskId = LocalStorage.generateId();
                    String description = currTask.getTitle();
                    
                    if (description.trim().isEmpty()) {
                        continue;
                    }
                    
                    chirptask.storage.Task newTask = InputParser
                            .getTaskFromString(description);
                    List<String> contextList = newTask.getContexts();
                    List<String> categoryList = newTask.getCategories();
                    String doneStatus = currTask.getStatus();
                    String googleId = currTask.getId();
                    String googleETag = currTask.getEtag();

                    boolean isDone = false;
                    if (STRING_DONE_TASK.equals(doneStatus)) {
                        isDone = true;
                    }

                    DateTime dueDate = currTask.getDue();
                    if (dueDate != null) {
                        Calendar dueCalendar = DateTimeHandler
                                .getDateFromDateTime(dueDate);
                        dueCalendar.set(Calendar.HOUR_OF_DAY, 23);
                        dueCalendar.set(Calendar.MINUTE, 59);
                        
                        DeadlineTask newDeadline = new DeadlineTask(taskId,
                                description, dueCalendar);
                        newDeadline.setCategories(categoryList);
                        newDeadline.setContexts(contextList);
                        newDeadline.setDone(isDone);
                        newDeadline.setETag(googleETag);
                        newDeadline.setGoogleId(googleId);
                        GoogleStorage.updateStorages(newDeadline);
                    } else {
                        chirptask.storage.Task newFloating = new chirptask.storage.Task(
                                taskId, description);
                        newFloating.setCategories(categoryList);
                        newFloating.setContexts(contextList);
                        newFloating.setDone(isDone);
                        newFloating.setETag(googleETag);
                        newFloating.setGoogleId(googleId);
                        GoogleStorage.updateStorages(newFloating);
                    }
                }
            }
        }
    }
    
    private void syncPhaseFour(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();
            List<chirptask.storage.Task> localList = new ArrayList<chirptask.storage.Task>();
            
            for (int i = 0; i < allTasks.size(); i++) {
                chirptask.storage.Task currTask = allTasks.get(i);
                localList.add(currTask);
            }
            
            for (int i = 0; i < localList.size(); i++) {
                chirptask.storage.Task currTask = localList.get(i);
                String googleId = currTask.getGoogleId();
                if (currTask == null || googleId == null || 
                        googleId.equals("")) {
                    continue;
                }
                
                if (events == null) {
                    break;
                }
                
                boolean isStoredOnline = false;
                
                for (Event event : events) {
                    String eventId = event.getId();
                    if (eventId != null) {
                        if (googleId.equals(eventId)) {
                            isStoredOnline = true;
                        }
                    }
                }
                
                if (taskList == null) {
                    break;
                }
                
                for (Task task : taskList) {
                    String taskId = task.getId();
                    if (taskId != null) {
                        if (googleId.equals(taskId)) {
                            isStoredOnline = true;
                        }
                    }
                }
                
                if (isStoredOnline == false) { //Deleted online
                    chirptask.storage.Task toDeleteLocally = currTask;
                    if (toDeleteLocally != null) {
                        // Local Task always takes precedence
                        if (toDeleteLocally.isModified()) {
                            // Set Google ID empty to remove in storages
                            //toDeleteLocally.setGoogleId(""); 
                            //GoogleStorage.updateStorages(toDeleteLocally);
                            // Push from local to Google
                        } else {
                            GoogleStorage.deleteFromLocalStorage(toDeleteLocally);
                        }
                    }
                }

            }
        }
    }
}

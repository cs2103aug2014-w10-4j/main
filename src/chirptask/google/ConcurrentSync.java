package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import chirptask.google.GoogleController.Status;
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
        if (allTasks == null && gController == null && 
                tasksController == null &&
                calController == null) {
            setAllNull();
        } else {
            setAllVars(allTasks, gController, tasksController, calController);
        }
    }
    
    private void setAllNull() {
        ConcurrentHandler.setNull(_taskList);
        ConcurrentHandler.setNull(_gController);
        ConcurrentHandler.setNull(_calendarController);
        ConcurrentHandler.setNull(_tasksController);
    }
    
    private void setAllVars(List<chirptask.storage.Task> taskList, 
            GoogleController gController,
            TasksController tController,
            CalendarController cController) {
        _taskList = taskList;
        _gController = gController;
        _tasksController = tController;
        _calendarController = cController;
    }

    public Boolean call() throws UnknownHostException, IOException  {
        boolean isSync = false;
        
        if (_taskList == null || _gController == null || 
                _calendarController == null || _tasksController == null) {
            isSync = false;
            return isSync;
        }
        
        try {
            sync(_taskList);
            isSync = true;
        } catch (Exception allException) {
            GoogleController.setOnlineStatus(Status.SYNC_FAIL);
        }
        
        return isSync;
    }
    
    private void sync(List<chirptask.storage.Task> allTasks) throws Exception {
        if (allTasks != null) {
            GoogleController.setOnlineStatus(Status.SYNC);
            syncPhaseOne(_taskList);
            syncPhaseTwo(_taskList);
            syncPhaseThree(_taskList);
            syncPhaseFour(_taskList); 
            GoogleController.setOnlineStatus(Status.ONLINE);
        }
    }
    
    /**
     * Phase One is to check if user deleted task from Google
     * @param allTasks 
     *              ChirpTask's local task list
     * @throws UnknownHostException 
     *              If Google's servers cannot be reached
     * @throws IOException
     *              If wrong response or transmission is interrupted
     */
    private void syncPhaseOne(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();
            
            List<chirptask.storage.Task> localList = cloneTaskList(allTasks);
            
            for (int i = 0; i < localList.size(); i++) {
                chirptask.storage.Task currTask = localList.get(i);
                String googleId = currTask.getGoogleId();
                
                if (currTask == null || googleId == null || 
                        googleId.equals("")) {
                    continue;
                }
                
                boolean isStoredOnline = false;
                
                if (events == null) {
                    break;
                }
                isStoredOnline = checkEventsOnline(googleId, events);
                
                if (taskList == null) {
                    break;
                }
                isStoredOnline = checkTasksOnline(googleId, taskList);
                
                if (isStoredOnline == false) { //Deleted online
                    deleteTaskLocally(currTask);
                }
            }
        }
    }
    
    /**
     * To preserve the list of tasks to be processed
     * @param toClone List to be preserved
     * @return The preserved list
     */
    private List<chirptask.storage.Task> cloneTaskList(
                        List<chirptask.storage.Task> toClone) {
        List<chirptask.storage.Task> clonedList = 
                new ArrayList<chirptask.storage.Task>();
            for (int i = 0; i < toClone.size(); i++) {
                chirptask.storage.Task currTask = toClone.get(i);
                clonedList.add(currTask);
            }
            return clonedList;
        }
    
    /**
     * Checks if the timed task can be found online
     * @param googleId The stored ID Locally
     * @param events The Google Tasks' List
     * @return True if found, false otherwise.
     */
    private boolean checkEventsOnline(String googleId, List<Event> events) {
        boolean isStoredOnline = false;
        
        for (Event event : events) {
            String eventId = event.getId();
            if (eventId != null) {
                if (googleId.equals(eventId)) {
                    isStoredOnline = true;
                }
            }
        }
        
        return isStoredOnline;
    }
    
    /**
     * Checks if the floating or deadline task can be found online
     * @param googleId The stored ID locally
     * @param taskList The Google Tasks' List
     * @return True if found, false otherwise.
     */
    private boolean checkTasksOnline(String googleId, List<Task> taskList) {
        boolean isStoredOnline = false;

        for (Task task : taskList) {
            String taskId = task.getId();
            if (taskId != null) {
                if (googleId.equals(taskId)) {
                    isStoredOnline = true;
                }
            }
        }
        
        return isStoredOnline;
    }
    
    /**
     * Local Task always takes precedence. If the task was modified locally,
     * and the task is deleted remotely, it will push from Local to Remote.
     * @param taskToDelete The task to be deleted from ChirpTask
     */
    private void deleteTaskLocally(chirptask.storage.Task taskToDelete) {
        if (taskToDelete != null) {
            boolean isModified = taskToDelete.isModified();
            boolean isNotDeleted = !taskToDelete.isDeleted();
            
            // Local Task always takes precedence
            if (isModified && isNotDeleted) { 
                // Set Google ID empty to remove in storages
                taskToDelete.setGoogleId(""); 
                taskToDelete.setModified(false);
                GoogleStorage.updateStorages(taskToDelete);
                // Push from local to Google
                _gController.addTask(taskToDelete);
            } else {
                GoogleStorage.deleteFromLocalStorage(taskToDelete);
            }
        }
    }
    
    /**
     * Phase two is a Google One-way synchronisation method.
     * 
     * Phase two adds local tasks without Google ID to Google; If the task has
     * been deleted, it does not perform the add operation. 
     * 
     * Phase two also deletes tasks with Google ID that are flagged as deleted 
     * from the Google account. 
     * 
     * Phase two also modify tasks with Google ID that are flagged as
     * modified
     * 
     * @param allTasks
     *            The local list of all tasks
     * @throws UnknownHostException
     *             If Google's servers cannot be reached
     * @throws IOException
     *             If transmission is interrupted
     */
    private void syncPhaseTwo(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {
        
        if (allTasks != null) {
            for (int i = 0; i < allTasks.size(); i++) {
                chirptask.storage.Task currTask = allTasks.get(i);
                String currGoogleId = currTask.getGoogleId();
                boolean isDeleted = currTask.isDeleted();
                
                if (currGoogleId == null || "".equals(currGoogleId)) {
                    if (!isDeleted) {
                        currTask.setModified(false);
                        _gController.addTask(currTask);
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

    /**
     * Phase Three checks for modifications from Google
     * Local Tasks will always take precedence.
     * 
     * If Local Task and Google's task are both modified, 
     *  do nothing in this phase.
     * @param allTasks 
     *              ChirpTask's local task list
     * @throws UnknownHostException 
     *              If Google's servers cannot be reached
     * @throws IOException
     *              If wrong response or transmission is interrupted
     */
    private void syncPhaseThree(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            Map<String, chirptask.storage.Task> googleIdMap = 
                                        createMap(allTasks);
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();

            checkAllEventsEdit(events, googleIdMap);
            checkAllTasksEdit(taskList, googleIdMap);
        }
    }
    
    /**
     * This method will create a Map that provides easy lookup if a Google ID
     * is known and stored locally. The value of the Map is a ChirpTask object
     * @param allTasks All local ChirpTask used to populate the Google ID list
     * @return A Map that contains Google ID as key, ChirpTask as value
     */
    private Map<String, chirptask.storage.Task> createMap(
                                       List<chirptask.storage.Task> allTasks) {
        Map<String, chirptask.storage.Task> googleIdMap = 
                new TreeMap<String, chirptask.storage.Task>();
        
        for (int i = 0; i < allTasks.size(); i++) {
            chirptask.storage.Task currTask = allTasks.get(i);
            String googleId = currTask.getGoogleId();

            if (googleId != null || "".equals(googleId)) {
                googleIdMap.put(googleId, currTask);
            }
        }
        
        return googleIdMap;
    }

    /**
     * This method checks if there are any events in Google Calendar
     * that are modified and if we require to update our local task.
     * @param events A list of Google Calendar Events 
     * @param googleIdMap ChirpTask list of known Google ID
     */
    private void checkAllEventsEdit(List<Event> events,
                        Map<String, chirptask.storage.Task> googleIdMap ) {
        for (Event currEvent : events) {
            String gId = currEvent.getId();
            if (googleIdMap.containsKey(gId)) {
                chirptask.storage.Task currTask = googleIdMap.get(gId);
                String googleETag = currEvent.getEtag();
                String localETag = currTask.getETag();
                
                if(checkIfRequireEdit(googleETag, localETag, currTask)) {
                    // Pull from remote to local
                    updateLocalEvents(currTask, currEvent);
                }
            }
        }
    }
    
    private boolean checkIfRequireEdit(String googleETag, 
                                        String localETag, 
                                        chirptask.storage.Task currTask) {
        boolean isRequired = false;
        
        if (googleETag != null && localETag != null) {
            if (!localETag.equals(googleETag)) {
                if (!currTask.isModified()) { 
                    isRequired = true;
                }
            }
        }
        return isRequired;
    }
    private void updateLocalEvents(chirptask.storage.Task currTask, 
                                                    Event currEvent) {
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
    
    /**
     * This method checks if there are any tasks in Google Tasks
     * that are modified and if we require to update our local task.
     * @param taskList A list of Google Tasks 
     * @param googleIdMap ChirpTask list of known Google ID
     */
    private void checkAllTasksEdit(List<Task> taskList, 
                        Map<String, chirptask.storage.Task> googleIdMap) {
        for (Task currTask : taskList) {
            String gId = currTask.getId();
            if (googleIdMap.containsKey(gId)) {
                chirptask.storage.Task chirpTask = googleIdMap.get(gId);
                String googleETag = currTask.getEtag();
                String localETag = chirpTask.getETag();
                
                if (checkIfRequireEdit(googleETag, localETag, chirpTask)) { 
                    updateLocalGTasks(chirpTask, currTask);
                }
            }
        }
    }
    
    /**
     * Update Local deadline or floating task from Google.
     * This method is more complex than updateLocalEvents because 
     * we allow the user to edit from deadline to floating and vice versa
     * from Google Tasks.
     * @param chirpTask The local task
     * @param currTask The Google Task
     */
    private void updateLocalGTasks(chirptask.storage.Task chirpTask, Task currTask) {
        // push from remote to local
        int taskId = chirpTask.getTaskId();
        List<String> hashtagList = chirpTask.getContexts();
        List<String> categoryList = chirpTask.getCategories();
        String doneString = currTask.getStatus();
        String eTag = chirpTask.getETag();
        String googleId = currTask.getId();
        String taskDesc = currTask.getTitle();
        DateTime dueDate = currTask.getDue();
                
        boolean isDone = false;
        if (STRING_DONE_TASK.equalsIgnoreCase(doneString)) {
            isDone = true;
        }
                
        chirptask.storage.Task newTask = null;
                
        if (dueDate != null) {
            Calendar dueCalendar = DateTimeHandler.getDateFromDateTime(dueDate);
            
            Calendar chirpDate = chirpTask.getDate();
            dueCalendar.set(Calendar.HOUR_OF_DAY, chirpDate.get(Calendar.HOUR_OF_DAY));
            dueCalendar.set(Calendar.MINUTE, chirpDate.get(Calendar.MINUTE));
            
            DeadlineTask newDeadline = 
                            new DeadlineTask(taskId, taskDesc, dueCalendar);
            setMiscTaskDetails(newDeadline, categoryList, hashtagList,
                            isDone, eTag, googleId);
            newTask = newDeadline;
        } else {
            chirptask.storage.Task newFloating = 
                            new chirptask.storage.Task(taskId, taskDesc);
            setMiscTaskDetails(newFloating, 
                               categoryList, 
                               hashtagList,
                               isDone, 
                               eTag, 
                               googleId);
            newTask = newFloating;
        }
                
        if (newTask != null) {
            GoogleStorage.updateStorages(newTask);
        }
    }
    
    /**
     * This method adds the miscellaneous properties to the ChirpTask Task
     * @param taskToSet The ChirpTask Task
     * @param categoryList The list of categories
     * @param hashtagList The list of hashtags
     * @param isDone The flag if the task is done
     * @param eTag The locally stored ETag
     * @param googleId The locally stored Google ID
     */
    private void setMiscTaskDetails(chirptask.storage.Task taskToSet, 
            List<String> categoryList, 
            List<String> hashtagList, 
            boolean isDone, 
            String eTag, 
            String googleId) {
        taskToSet.setCategories(categoryList);
        taskToSet.setContexts(hashtagList);
        taskToSet.setDone(isDone);
        taskToSet.setETag(eTag);
        taskToSet.setGoogleId(googleId);
    }

    /**
     * Phase four adds new tasks from Google
     * @param allTasks 
     *              ChirpTask's local task list
     * @throws UnknownHostException 
     *              If Google's servers cannot be reached
     * @throws IOException
     *              If wrong response or transmission is interrupted
     */
    private void syncPhaseFour(List<chirptask.storage.Task> allTasks)
            throws UnknownHostException, IOException {

        if (allTasks != null) {
            Map<String, chirptask.storage.Task> googleIdMap = createMap(allTasks);
            List<Event> events = _calendarController.getEvents();
            Tasks tasks = _tasksController.getTasks();
            List<Task> taskList = tasks.getItems();

            addFromNewGoogleEvent(events, googleIdMap);
            addFromNewGoogleTask(taskList, googleIdMap);
        }
    }

    /**
     * This method adds from a new, unknown, Google Calendar Event,
     * only if the Google ID from the Google Calendar Event is new and unknown
     * @param Events from Google Calendar
     * @param googleIdMap ChirpTask List of known Google ID
     */
    private void addFromNewGoogleEvent(List<Event> events, 
            Map<String, chirptask.storage.Task> googleIdMap) {
        for (Event currEvent : events) {
            String gId = currEvent.getId();
            if (!googleIdMap.containsKey(gId)) {
                int taskId = LocalStorage.generateId();
                String description = currEvent.getSummary();
                
                chirptask.storage.Task newTask = 
                        InputParser.getTaskFromString(description);

                String googleId = gId;
                String googleETag = currEvent.getEtag();
                List<String> hashtagList = newTask.getContexts();
                List<String> categoryList = newTask.getCategories();
                Calendar startDate = getCalendarFromEvent(currEvent.getStart());
                Calendar endDate = getCalendarFromEvent(currEvent.getEnd());

                boolean isDone = false;
                if (description != null) {
                    if (description.startsWith(STRING_DONE_EVENT)) {
                        isDone = true;
                    }
                }

                TimedTask newTimed = 
                        new TimedTask(taskId, description,startDate, endDate);
                setMiscTaskDetails(newTimed, 
                        categoryList, 
                        hashtagList, 
                        isDone, 
                        googleETag, 
                        googleId);
                GoogleStorage.updateStorages(newTimed);
            }
        }
    }
    
    /**
     * This method retrieves a Calendar object from the given 
     * EventDateTime object which is found in a Google Calendar Event object.
     * @param eventDateTime The Google Calendar Event DateTime object 
     * @return A Calendar object of the same interpretation
     */
    private Calendar getCalendarFromEvent(EventDateTime eventDateTime) {
        if (eventDateTime != null) {
            Calendar newCalendar = DateTimeHandler.getCalendar(eventDateTime);
            return newCalendar;
        } else {
            return null;
        }
    }
    
    /**
     * This method adds from a new, unknown, Google Task task,
     * only if the Google ID from the Google Task task is new and unknown
     * @param taskList Google Tasks' List
     * @param googleIdMap ChirpTask List of known Google ID
     */
    private void addFromNewGoogleTask(List<Task> taskList, 
            Map<String, chirptask.storage.Task> googleIdMap) {
        
        for (Task currTask : taskList) {
            String gId = currTask.getId();
            
            if (!googleIdMap.containsKey(gId)) {
                int taskId = LocalStorage.generateId();
                String taskDesc = currTask.getTitle();
                
                if (taskDesc.trim().isEmpty()) {
                    continue;
                }
                
                chirptask.storage.Task newTask = InputParser
                        .getTaskFromString(taskDesc);
                newTask.setTaskId(taskId);
                //Set ETag allows reuse of updateLocalGTask(Task,Task)
                newTask.setETag(currTask.getEtag()); 
                
                updateLocalGTasks(newTask, currTask);
            }
        }
    }
    
    
}

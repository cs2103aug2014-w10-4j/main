//@author A0111840W
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

/**
 * ConcurrentSync is submitted to the ExecutorService to run Concurrently
 * It will perform a series of sync phases to ensure that the tasks between
 * ChirpTask and Google are in sync.
 * 
 * One thing to note is that we have prioritised ChirpTask to take precedence
 * in the event of having unsynced modifications on both sides at the same time
 * The ChirpTask Task will take over the modification remotely in this case.
 */
class ConcurrentSync implements Callable<Boolean> {
    private static final int sleepTime = 10000; // 10 Second cooldown per sync
    private static final int sleepAfterSync = 2000; // 2 Second cooldown
    
    private static final String STRING_DONE_TASK = "completed";
    private static final String STRING_DONE_EVENT = "[Done]";
    private static final String STRING_DONE_EVENT_REGEX = "^\\[Done\\]";
    private static final String STRING_EMPTY = "";
    
    private static boolean isSyncing = false; // Ensure only 1 thread do sync

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
            if (ConcurrentSync.isSyncing == false) { // Unlocked state
                ConcurrentSync.isSyncing = true; // Keep a lock
                sync(_taskList);
                sleepThread(sleepTime);
                ConcurrentSync.isSyncing = false; // Unlock the state
                isSync = true;
            }
        } catch (Exception allException) {
            GoogleController.setOnlineStatus(Status.SYNC_FAIL);
        } finally {
            ConcurrentSync.isSyncing = false;
        }
        
        return isSync;
    }
    
    /**
     * The general sync method that will call all phases of sync
     * @param allTasks A List of ChirpTask Task
     * @throws Exception If any sync phase throws any exception
     */
    private void sync(List<chirptask.storage.Task> allTasks) throws Exception {
        if (allTasks != null) {
            GoogleController.setOnlineStatus(Status.SYNC);
            syncPhaseOne(_taskList);
            sleepThread(sleepAfterSync);
            syncPhaseTwo(_taskList);
            sleepThread(sleepAfterSync);
            syncPhaseThree(_taskList);
            sleepThread(sleepAfterSync);
            syncPhaseFour(_taskList); 
            GoogleController.setOnlineStatus(Status.ONLINE);
        }
    }
    
    /**
     * Common method to be called for sleeping threads
     * @param timeToSleep Amount of time to sleep in milliseconds
     */
    private void sleepThread(int timeToSleep) {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
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
                isStoredOnline = 
                        isStoredOnline || checkTasksOnline(googleId, taskList);
                
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
        if (toClone == null) {
            return new ArrayList<chirptask.storage.Task>();
        }
        
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
        if (googleId == null || events == null) {
            return false;
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
        
        return isStoredOnline;
    }
    
    /**
     * Checks if the floating or deadline task can be found online
     * @param googleId The stored ID locally
     * @param taskList The Google Tasks' List
     * @return True if found, false otherwise.
     */
    private boolean checkTasksOnline(String googleId, List<Task> taskList) {
        if (googleId == null || taskList == null) {
            return false;
        }
        
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
        if (allTasks == null) {
            return new TreeMap<String, chirptask.storage.Task>();
        }
        
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
                     Map<String, 
                     chirptask.storage.Task> googleIdMap ) throws IOException {
        if (events == null || googleIdMap == null) {
            return;
        }
        
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
        if (googleETag == null || localETag == null || currTask == null) {
            return false;
        }
        
        boolean isRequired = false;
        
        if (!localETag.equals(googleETag)) {
            if (!currTask.isModified()) { 
                isRequired = true;
            }
        }
        return isRequired;
    }
    private void updateLocalEvents(chirptask.storage.Task currTask, 
                                         Event currEvent) throws IOException {
        if (currTask == null || currEvent == null) {
            return;
        }
        
        String eventDescription = currEvent
                .getSummary();
        EventDateTime start = currEvent.getStart();
        EventDateTime end = currEvent.getEnd();
        Calendar startDate = DateTimeHandler
                .getCalendar(start);
        Calendar endDate = DateTimeHandler
                .getCalendar(end);
        boolean isDone = setDoneEvent(eventDescription);
        
        removeDoneAndUpdateEvent(currEvent, isDone);
        
        if (currTask instanceof chirptask.storage.TimedTask) {
            TimedTask timedTask = (TimedTask) currTask;
            timedTask.setDescription(eventDescription);
            timedTask.setStartTime(startDate);
            timedTask.setEndTime(endDate);
            timedTask.setDone(isDone);
            GoogleStorage.updateStorages(timedTask);
        }
    }
    
    private void removeDoneAndUpdateEvent(Event event, 
                                boolean isDone) throws IOException {
        String description = event.getSummary();
        description = removeDoneTag(description, isDone);
        updateEventColor(event, isDone);
    }
    
    private String removeDoneTag(String description, boolean isDone) 
                                            throws NullPointerException {
        if (description == null) {
            throw new NullPointerException();
        }
        String newDesc = description.trim();
        if (isDone) {
            newDesc = description.replaceFirst(
                                STRING_DONE_EVENT_REGEX, STRING_EMPTY);
            newDesc = newDesc.trim();
        }
        return newDesc;
    }
    
    private void updateEventColor(Event event, 
                                        boolean isDone) throws IOException {
        event = CalendarHandler.setColorAndLook(event, isDone);
        CalendarHandler.updateEvent(CalendarController.getCalendarId(), 
                                    event.getId(), event);
    }
    
    /**
     * This method checks if there are any tasks in Google Tasks
     * that are modified and if we require to update our local task.
     * @param taskList A list of Google Tasks 
     * @param googleIdMap ChirpTask list of known Google ID
     */
    private void checkAllTasksEdit(List<Task> taskList, 
                        Map<String, chirptask.storage.Task> googleIdMap) {
        if (taskList == null || googleIdMap == null) {
            return;
        }
        
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
    private void updateLocalGTasks(chirptask.storage.Task chirpTask, 
                                                    Task currTask) {
        if (chirpTask == null || currTask == null) {
            return;
        }
        
        // push from remote to local
        int taskId = chirpTask.getTaskId();
        List<String> hashtagList = chirpTask.getHashtags();
        List<String> categoryList = chirpTask.getCategories();
        String doneString = currTask.getStatus();
        String eTag = chirpTask.getETag();
        String googleId = currTask.getId();
        String taskDesc = currTask.getTitle();
        boolean isDone = setDoneTasks(doneString);
                
        chirptask.storage.Task newTask = 
                makeDeadlineOrFloating(currTask, taskDesc);
                
        if (newTask != null) {
            newTask.setTaskId(taskId); 
            setMiscTaskDetails(newTask, categoryList, hashtagList,
                    isDone, eTag, googleId);
            GoogleStorage.updateStorages(newTask);
        }
    }
    
    /**
     * Set done if the Google Tasks Task status is completed
     * @param doneStatus The Google Tasks Task status
     * @return true if doneStatus is completed, false otherwise
     */
    private boolean setDoneTasks(String doneStatus) {
        boolean isDone = false;
        
        if (STRING_DONE_TASK.equalsIgnoreCase(doneStatus)) {
            isDone = true;
        }
        return isDone;
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
        if (taskToSet == null || categoryList == null || hashtagList == null ||
                eTag == null || googleId == null) {
            return;
        }
        
        taskToSet.setCategories(categoryList);
        taskToSet.setHashtags(hashtagList);
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
            Map<String, chirptask.storage.Task> googleIdMap = 
                                                    createMap(allTasks);
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
          Map<String, chirptask.storage.Task> googleIdMap) throws IOException {
        if (events == null || googleIdMap == null) {
            return;
        }
        
        for (Event currEvent : events) {
            String gId = currEvent.getId();
            if (!googleIdMap.containsKey(gId)) {
                int taskId = LocalStorage.generateId();
                String description = currEvent.getSummary();
                
                chirptask.storage.Task newTask = 
                        InputParser.getTaskFromString(description);

                String googleId = gId;
                String googleETag = currEvent.getEtag();
                List<String> hashtagList = newTask.getHashtags();
                List<String> categoryList = newTask.getCategories();
                Calendar startDate = getCalendarFromEvent(currEvent.getStart());
                Calendar endDate = getCalendarFromEvent(currEvent.getEnd());
                boolean isDone = setDoneEvent(description);
                
                removeDoneAndUpdateEvent(currEvent, isDone);
                
                TimedTask newTimed = 
                        new TimedTask(taskId, description,startDate, endDate);
                
                if (newTimed != null) {
                    setMiscTaskDetails(newTimed, categoryList, hashtagList, 
                            isDone, googleETag, googleId);
                    GoogleStorage.updateStorages(newTimed);
                }
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
     * Set done if the Google Calendar Event Title begins with [Done]
     * @param description The Google Calendar Event Title
     * @return true if description starts with [Done], false otherwise
     */
    private boolean setDoneEvent(String description) {
        boolean isDone = false;
        
        if (description != null) {
            if (description.startsWith(STRING_DONE_EVENT)) {
                isDone = true;
            } else {
                isDone = false;
            }
        }
        return isDone;
    }
    
    /**
     * This method adds from a new, unknown, Google Task task,
     * only if the Google ID from the Google Task task is new and unknown
     * @param taskList Google Tasks' List
     * @param googleIdMap ChirpTask List of known Google ID
     */
    private void addFromNewGoogleTask(List<Task> taskList, 
            Map<String, chirptask.storage.Task> googleIdMap) {
        if (taskList == null || googleIdMap == null) {
            return;
        }
        
        for (Task currTask : taskList) {
            String gId = currTask.getId();
            
            if (!googleIdMap.containsKey(gId)) {
                String taskDesc = currTask.getTitle();
                
                if (taskDesc.trim().isEmpty()) {
                    continue; //skip, don't consider empty desc
                }
                chirptask.storage.Task newTask = 
                        makeDeadlineOrFloating(currTask, taskDesc);
                
                if (newTask != null) {
                    //Set ETag allows reuse of updateLocalGTask(Task,Task)
                    newTask.setETag(currTask.getEtag()); 
                    updateLocalGTasks(newTask, currTask);
                }
            }
        }
    }
    
    /**
     * Decide to make Deadline or Floating by checking dueDate of Task
     * @param currTask The Google Tasks Task object
     * @param desc The description to set for the Chirptask Task
     * @return The newly created ChirpTask Task object
     */
    private chirptask.storage.Task makeDeadlineOrFloating(
                    Task currTask, String desc) {
        chirptask.storage.Task newTask = null;
        DateTime dueDate = currTask.getDue(); 
        
        if (dueDate != null) {
            newTask = InputParser.getTaskFromString( 
                    chirptask.storage.Task.TASK_DEADLINE, desc);
            if (newTask == null) { // Task did not contain Time
                newTask = InputParser.getTaskFromString(
                        chirptask.storage.Task.TASK_FLOATING, desc);
                newTask = makeDeadlineWithNoTime(newTask, dueDate);
            } else { // Copy date from Google
                setDateFromGoogle(newTask, dueDate);
            }
        } else { 
            newTask = InputParser.getTaskFromString(
                    chirptask.storage.Task.TASK_FLOATING, desc);
        }
        return newTask;
    }
    
    /**
     * Makes a Deadline Task without a specified Time if not given
     * @param task The ChirpTask Task
     * @param dueDate The DateTime object from Google Tasks Task
     * @return A DeadlineTask object with a due date without time
     */
    private chirptask.storage.Task makeDeadlineWithNoTime(
            chirptask.storage.Task task, DateTime dueDate) {
        int taskId = task.getTaskId();
        String taskDesc = task.getDescription();
        Calendar dueCalendar = 
                DateTimeHandler.getDateFromDateTime(dueDate);
        DeadlineTask newDeadline = new DeadlineTask(taskId, taskDesc, dueCalendar);
        
        return newDeadline;
    }
    
    /**
     * Sets the date from Google Tasks Task to ChirpTask Task
     * @param task The ChirpTask Task
     * @param dueDate The Google Tasks Task DateTime object
     */
    private void setDateFromGoogle(
            chirptask.storage.Task task, DateTime dueDate) {
        Calendar dueCalendar = DateTimeHandler.getDateFromDateTime(dueDate);
        Calendar chirpDate = task.getDate();
        chirpDate.set(Calendar.DATE, dueCalendar.get(Calendar.DATE));
        chirpDate.set(Calendar.MONTH, dueCalendar.get(Calendar.MONTH));
        chirpDate.set(Calendar.YEAR, dueCalendar.get(Calendar.YEAR));
    }
}

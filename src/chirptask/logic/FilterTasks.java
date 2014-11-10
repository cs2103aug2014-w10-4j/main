//@author A0111930W

package chirptask.logic;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import chirptask.common.Constants;
import chirptask.common.Settings;
import chirptask.common.Settings.StatusType;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

/**
 * This class is use to populate the task/hashtag/category list and will be use to display
 * by DisplayView class.
 * 
 * 
 *
 */
public class FilterTasks {

    private static List<Task> filteredTask;
    private static List<String> categoriesList;
    private static List<String> hashtagList;
    private static String currentFilter = Settings.DEFAULT_FILTER;
    private static final int INIT_FILTER = 1;
    private static final int INIT_TASKINDEX = -1;
    private static final int ZERO_POS = 0;
    private static final int FIRST_POS = 1;
    private static final int START_INDEX = 0;
    private static final int INT_ONE = 1;
    private static final String SPACE = "\\s+";
    private static final String SLASH = "/";
    private static final String EDIT = "edit ";
    /**
     * This method is use to process the current filter entered by user
     * 
     * @param T The Task containing the filter string
     * @param gui The MainGui object to manipulate
     */
    public static void filter(Task T, MainGui gui) {
        filteredTask = StorageHandler.getAllTasks();
        filteredTask = hideDeleted(filteredTask);
        currentFilter = T.getDescription();
        if (currentFilter.isEmpty()) {
            showStatusToUser(gui);
        } else {
            processFilter(currentFilter, gui);
        }

    }

    /**
     * Show appropriate message to user
     * 
     * @param gui The MainGui object to manipulate
     */
    private static void showStatusToUser(MainGui gui) {
        DisplayView.showStatusToUser(StatusType.MESSAGE, gui, "");
    }

    /**
     * Enables gui component to call this method when user presses tab to show
     * the edited description
     * 
     * @param editInput The user input to search for index number
     * @param gui The MainGui object to manipulate
     */
    public static void editCli(String editInput, MainGui gui) {
        assert gui != null && !editInput.trim().isEmpty();
        int taskIndex = convertInputToIndex(editInput);
        showEditTaskToUser(gui, taskIndex);
    }

    /**
     * Check if edited task is in the task list range and show the selected task
     * to cli.
     * 
     * @param gui The MainGui object to manipulate
     * @param taskIndex The task index to grab information from
     */
    private static void showEditTaskToUser(MainGui gui, int taskIndex) {
        int oldtaskIndex = taskIndex;
        taskIndex--;
        if (isTaskIndexInRange(taskIndex)) {
            showEditTask(gui, taskIndex, oldtaskIndex);
        }
    }

    private static void showEditTask(MainGui gui, int taskIndex,
            int oldtaskIndex) {
        gui.setUserInputText(EDIT + oldtaskIndex + " "
                + filteredTask.get(taskIndex).getDescription());
    }

    /**
     * Method returns true if task key in by user is within the range of the
     * displayed task.
     * 
     * @param taskIndex The task index to check if in range
     * @return true if in range, false otherwise
     */
    private static boolean isTaskIndexInRange(int taskIndex) {
        return taskIndex > INIT_TASKINDEX && taskIndex < filteredTask.size();
    }

    private static int convertInputToIndex(String editInput) {
        int taskIndex = INIT_TASKINDEX;
        try {
            taskIndex = Integer.parseInt(editInput.split(" ")[FIRST_POS]);
        } catch (NumberFormatException e) {
            // update status bar if required
        } catch (ArrayIndexOutOfBoundsException e) {
            // update status bar if required
        }
        return taskIndex;
    }

    /**
     * Return a list of tasks that is not deleted.
     * 
     * @param taskList The List of Task objects to hide
     * @return The List of Task without all the isDeleted=true Task
     */
    public static List<Task> hideDeleted(List<Task> taskList) {
        List<Task> unhiddenList = new ArrayList<Task>();

        for (int i = START_INDEX; i < taskList.size(); i++) {
            Task currTask = taskList.get(i);
            if (!currTask.isDeleted()) {
                unhiddenList.add(currTask);
            }
        }

        return unhiddenList;
    }

    /**
     * Process the user input for filter and populate the list of tasks
     * accordingly
     * 
     * @param filters The String containing all filters
     * @param gui The MainGui object to manipulate
     */
    private static void processFilter(String filters, MainGui gui) {
        String[] param = processFilterInput(filters);

        List<Task> processList = new CopyOnWriteArrayList<Task>();
        processList.addAll(filteredTask);

        for (int paramPos = START_INDEX; paramPos < param.length; paramPos++) {
            String filter = param[paramPos];
            paramPos = determineFilterAndExecute(gui, param, processList, paramPos, filter);
            filteredTask = new ArrayList<Task>(processList);
        }

    }

    private static int determineFilterAndExecute(MainGui gui, String[] param,
            List<Task> templist, int pos, String filter) {
        switch (filter) {
        case Constants.FILTER_DONE:
            filterStatus(templist, true);
            break;
        case Constants.FILTER_UNDONE:
            filterStatus(templist, false);
            break;
        case Constants.FILTER_FLOATING:
            filterTaskType(templist, Task.TASK_FLOATING);
            break;
        case Constants.FILTER_TIMED:
            filterTaskType(templist, Task.TASK_TIMED);
            break;
        case Constants.FILTER_DEADLINE:
            filterTaskType(templist, Task.TASK_DEADLINE);
            break;
        case Constants.FILTER_DATE:
            filterTaskDate(gui, param, templist, pos);
            pos++;
            break;
        default:
            filterKeyword(templist, filter);
            break;
        }
        return pos;
    }

    /**
     * Filter the task list by date
     * 
     * @param gui The MainGui object to manipulate
     * @param param The filter date parameters 
     * @param templist The filtered List to manipulate
     * @param index The index of filter in param
     */
    private static void filterTaskDate(MainGui gui, String[] param,
            List<Task> templist, int index) {
        try {
            Calendar filterdate = processFilterDateParam(param[index + INIT_FILTER]);
            if (filterdate != null) {
                processFilterDate(gui, param, templist, index, filterdate);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            templist = processExceptionLogging(gui);

        } catch (InvalidParameterException invalidParameterException) {
            DisplayView.showStatusToUser(StatusType.ERROR, gui, "");
        }
    }

    /**
     * Process the date filter and display status to user
     * 
     * @param gui The MainGui object to manipulate
     * @param param The filter data parameters
     * @param templist The filtered List to manipulate
     * @param i The index of filter in param
     * @param filterdate The Calendar Date object to filter by and on
     */
    private static void processFilterDate(MainGui gui, String[] param,
            List<Task> templist, int i, Calendar filterdate) {
        // add 1 so that the filter includes tasks of the
        // same date.
        filterdate.add(Calendar.DAY_OF_MONTH, 1);
        filterTaskByDate(templist, filterdate);
        DisplayView.showStatusToUser(StatusType.MESSAGE, gui, param[i
                + INIT_FILTER]);
    }

    /**
     * Log down invalid action by user and set error status.
     * 
     * @param gui The MainGui object to manipulate
     * @return The List of Task containing all Session tasks
     */
    private static List<Task> processExceptionLogging(MainGui gui) {
        List<Task> templist;
        StorageHandler.logError(Constants.LOG_MESSAGE_INVALID_COMMAND);
        DisplayView.showStatusToUser(StatusType.ERROR, gui, "");

        templist = new ArrayList<Task>(StorageHandler.getAllTasks());
        return templist;
    }

    /**
     * Method that will split the filter string by space
     * 
     * @param filters
     * @return
     */
    private static String[] processFilterInput(String filters) {
        return filters.split(SPACE);
    }

    /**
     * Process the user date input and return an calendar object
     * 
     * @param filter The filter date String 
     * @return The Calendar object that was parsed from filter string
     * @throws InvalidParameterException If user did not enter ##/##
     */
    public static Calendar processFilterDateParam(String filter)
            throws InvalidParameterException {
        String[] temp = filter.split(SLASH);
        Calendar filterdate = Calendar.getInstance();
        if (isValidDateLength(temp)) {
            try {
                setCalendar(temp, filterdate);
            } catch (NumberFormatException e) {

            }
        } else {
            // Exception should handle here show status to user
            throw new InvalidParameterException();
        }

        return filterdate;
    }

    /**
     * 
     * @param temp The temp split date array [dd/mm]
     * @param filterdate The Calendar object to manipulate
     */
    private static void setCalendar(String[] temp, Calendar filterdate) {
        filterdate.set(filterdate.get(Calendar.YEAR),
                convertStringDateToInt(temp, FIRST_POS) - INT_ONE,
                convertStringDateToInt(temp, ZERO_POS));
    }

    private static int convertStringDateToInt(String[] temp, int pos) {
        return Integer.parseInt(temp[pos]);
    }

    private static boolean isValidDateLength(String[] temp) {
        return temp.length > INT_ONE;
    }

    private static void filterTaskByDate(List<Task> tempList,
            Calendar filterdate) {
        populateDateList(tempList, filterdate);
        if (tempList.isEmpty()) {
            resetFilteredTask();
            populateDateList(tempList, filterdate);
        }
    }

    /**
     * This method will check the filter date with the list of
     * fliteredtask if the filter date is after and equals to the
     * Task date add to the list.
     * 
     * @param tempList The List of current Task
     * @param filterdate The date to filter by and on.
     */

    private static void populateDateList(List<Task> tempList,
            Calendar filterdate) {
        for (Task T : filteredTask) {
            if (filterdate.compareTo(T.getDate()) < INT_ONE) {
                tempList.remove(T);
            }
        }

    }

    private static void filterKeyword(List<Task> tempList, String keyword) {
        populateStringList(tempList, keyword);
        if (tempList.isEmpty()) {
            resetFilteredTask();
            populateStringList(tempList, keyword);
        }
    }
    /**
     * This method will remove from all the tasks that does not match the user input,
     * final filteredTask list will contains all tasks that matches. 
     * 
     * @param templist The current working List to filter
     * @param keywords The keywords to search for
     */
    private static void populateStringList(List<Task> templist, String keywords) {
        for (Task T : filteredTask) {
            if (!T.getDescription().toLowerCase()
                    .contains(keywords.toLowerCase())) {
                templist.remove(T);
            }
        }
    }

    private static void filterStatus(List<Task> tempList, boolean done) {
        populateStatusList(tempList, done);
        if (tempList.isEmpty()) {
            resetFilteredTask();
            populateStatusList(tempList, done);
        }
    }
    /**
     * 
     * @param tempList The current List of Task
     * @param done If true filter all done task, else filter all undone task
     */
    private static void populateStatusList(List<Task> tempList, boolean done) {
        for (Task T : filteredTask) {
            if (T.isDone() != done) {
                tempList.remove(T);
            }
        }
    }
    /**
     * Repopulate the filtered task which simulate a refresh.
     */
    private static void resetFilteredTask() {
        filteredTask = StorageHandler.getAllTasks();
        filteredTask = hideDeleted(filteredTask);
    }

    private static void filterTaskType(List<Task> tempList, String taskType) {
        populateTaskList(tempList, taskType);
        if (filteredTask.isEmpty()) {
            resetFilteredTask();
            populateTaskList(tempList, taskType);
        }
    }

    private static void populateTaskList(List<Task> tempList, String taskType) {
        for (Task T : filteredTask) {
            if (!T.getType().equalsIgnoreCase(taskType)) {
                tempList.remove(T);
            }
        }
    }
    /**
     * This will populate all task and category/hastags into the respective list for display.
     * 
     * @param gui The MainGui object to manipulate
     */
    static void filter(MainGui gui) {
        categoriesList = new ArrayList<String>();
        hashtagList = new ArrayList<String>();
        filteredTask = StorageHandler.getAllTasks();
        filteredTask = hideDeleted(filteredTask);
        populateCategoryAndHashtag();

        if (!currentFilter.isEmpty()) {
            processFilter(currentFilter, gui);
        }

    }

    private static void populateCategoryAndHashtag() {
        for (Task task : hideDeleted(StorageHandler.getAllTasks())) {
            populateHashtag(task);
            populateCategory(task);
        }
    }

    private static void populateCategory(Task task) {

        for (String category : task.getCategories()) {
            if (!categoriesList.contains(category.toLowerCase())) {
                categoriesList.add(category.toLowerCase());
            }
        }

    }

    private static void populateHashtag(Task task) {

        for (String hashtag : task.getHashtags()) {
            if (!hashtagList.contains(hashtag.toLowerCase())) {
                hashtagList.add(hashtag.toLowerCase());
            }
        }

    }

    public static List<Task> getFilteredList() {
        return filteredTask;
    }

    public static List<String> getContextList() {
        return hashtagList;
    }

    public static List<String> getCategoryList() {
        return categoriesList;
    }
}

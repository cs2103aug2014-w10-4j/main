//@author A0111930W

package chirptask.logic;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.common.Settings.StatusType;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

/**
 * This class is use to populate the task/hashtag/category list and will be use to display
 * by DisplayView class.
 * 
 * @author A0111930W
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

    /**
     * This method is use to process the current filter entered by user
     * 
     * @param T
     * @param gui
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
     * @param gui
     */
    private static void showStatusToUser(MainGui gui) {
        DisplayView.showStatusToUser(StatusType.MESSAGE, gui, "");
    }

    /**
     * Enables gui component to call this method when user presses tab to show
     * the edited description
     * 
     * @param editInput
     * @param gui
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
     * @param gui
     * @param taskIndex
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
        gui.setUserInputText("edit " + oldtaskIndex + " "
                + filteredTask.get(taskIndex).getDescription());
    }

    /**
     * Method returns true if task key in by user is within the range of the
     * displayed task.
     * 
     * @param taskIndex
     * @return
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
     * @param taskList
     * @return
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
     * @param filters
     * @param gui
     */
    private static void processFilter(String filters, MainGui gui) {
        String[] param = processFilterParam(filters);

        List<Task> templist = new CopyOnWriteArrayList<Task>();
        templist.addAll(filteredTask);

        for (int i = START_INDEX; i < param.length; i++) {
            String filter = param[i];
            switch (filter) {
            case "/done":
                // search done task
                filterStatus(templist, true);
                break;
            case "/undone":
                // search undone task
                filterStatus(templist, false);
                break;
            case "/floating":
                filterTaskType(templist, Task.TASK_FLOATING);
                break;
            case "/timed":
                filterTaskType(templist, Task.TASK_TIMED);
                break;
            case "/deadline":
                filterTaskType(templist, Task.TASK_DEADLINE);
                break;
            case "/date":
                filterTaskDate(gui, param, templist, i);
                i++;
                break;
            default:
                filterKeyword(templist, filter);
                break;
            }
            filteredTask = new ArrayList<Task>(templist);
        }

    }

    /**
     * Filter the task list by date
     * 
     * @param gui
     * @param param
     * @param templist
     * @param i
     */
    private static void filterTaskDate(MainGui gui, String[] param,
            List<Task> templist, int i) {
        try {
            Calendar filterdate = processFilterDateParam(param[i + INIT_FILTER]);
            if (filterdate != null) {
                processFilterDate(gui, param, templist, i, filterdate);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // log down invalid input
            templist = processExceptionLogging(gui);

        } catch (InvalidParameterException invalidParameterException) {
            DisplayView.showStatusToUser(StatusType.ERROR, gui, "");
        }
    }

    /**
     * Process the date filter and display status to user
     * 
     * @param gui
     * @param param
     * @param templist
     * @param i
     * @param filterdate
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
     * @param gui
     * @return
     */
    private static List<Task> processExceptionLogging(MainGui gui) {
        List<Task> templist;
        StorageHandler.logError(Messages.LOG_MESSAGE_INVALID_COMMAND);
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
    private static String[] processFilterParam(String filters) {
        return filters.split("\\s+");
    }

    /**
     * Process the user date input and return an calendar object
     * 
     * @param filter
     * @return
     * @throws InvalidParameterException
     */
    public static Calendar processFilterDateParam(String filter)
            throws InvalidParameterException {
        String[] temp = filter.split("/");
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
     * @param temp
     * @param filterdate
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
     * @author A0111930W
     * @param tempList
     * @param filterdate
     * 
     *            This method will check the filter date with the list of
     *            fliteredtask if the filter date is after and equals to the
     *            Task date add to the list.
     * 
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
     * @param templist
     * @param keywords
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
     * @param tempList
     * @param done
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
     * @param gui
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

        for (String hashtag : task.getContexts()) {
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

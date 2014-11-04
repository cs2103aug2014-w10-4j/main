package chirptask.logic;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.common.Settings.StatusType;
import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

//@author A0111930W
public class FilterTasks {

    private static List<Task> filteredTask;
    private static List<String> categoriesList;
    private static List<String> hashtagList;
    private static String currentFilter = Settings.DEFAULT_FILTER;
    private static final int PARAM_FILTER = 1;

    static void filter(Task T, MainGui gui) {

        currentFilter = T.getDescription();

        // check 1st String to determine the type of filter
        filteredTask = StorageHandler.getAllTasks();
        filteredTask = hideDeleted(filteredTask);
        if (currentFilter.isEmpty()) {
            DisplayView.showStatusToUser(StatusType.MESSAGE, gui, "");
        } else {
            processFilter(currentFilter, gui);
        }

    }

    public static void editCli(String editInput, MainGui gui) {
        assert gui != null && !editInput.trim().isEmpty();
        int taskIndex = -1;
        try {
            taskIndex = Integer.parseInt(editInput.split(" ")[1]);
        } catch (NumberFormatException e) {
            //update status bar if required
        } catch (ArrayIndexOutOfBoundsException e){
            //update status bar if required
        }

        int oldtaskIndex = taskIndex;
        taskIndex = taskIndex - 1;
        if (taskIndex > -1 && taskIndex < filteredTask.size()) {

            gui.setUserInputText("edit " + oldtaskIndex + " "
                    + filteredTask.get(taskIndex).getDescription());

        }
    }

    public static List<Task> hideDeleted(List<Task> taskList) {
        List<Task> unhiddenList = new ArrayList<Task>();

        for (int i = 0; i < taskList.size(); i++) {
            Task currTask = taskList.get(i);
            if (!currTask.isDeleted()) {
                unhiddenList.add(currTask);
            }
        }

        return unhiddenList;
    }

    private static void processFilter(String filters, MainGui gui) {
        String[] param = filters.split("\\s+");

        List<Task> templist = new CopyOnWriteArrayList<Task>();
        templist.addAll(filteredTask);

        for (int i = 0; i < param.length; i++) {
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
                filterTaskType(templist, "floating");
                break;
            case "/timed":
                filterTaskType(templist, "timedtask");
                break;
            case "/deadline":
                filterTaskType(templist, "deadline");
                break;
            case "/date":
                // Assuming input is 23/10
                try {
                    Calendar filterdate = processFilterDateParam(param[i
                            + PARAM_FILTER]);
                    if (filterdate != null) {
                        // add 1 so that the filter includes tasks of the
                        // same date.
                        filterdate.add(Calendar.DAY_OF_MONTH, 1);
                        filterTaskByDate(templist, filterdate);
                        DisplayView.showStatusToUser(StatusType.MESSAGE, gui,
                                param[i + PARAM_FILTER]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // log down invalid input

                    StorageHandler
                            .logError(Messages.LOG_MESSAGE_INVALID_COMMAND);
                    DisplayView.showStatusToUser(StatusType.ERROR, gui, "");

                    templist = new ArrayList<Task>(StorageHandler.getAllTasks());

                    break;

                } catch (InvalidParameterException invalidParameterException) {
                    DisplayView.showStatusToUser(StatusType.ERROR, gui, "");
                } finally {
                    i++;
                }
                break;
            default:
                // Entire string keyword search
                filterKeyword(templist, filter);
                break;
            }
            filteredTask = new ArrayList<Task>(templist);
        }

    }

    public static Calendar processFilterDateParam(String filter)
            throws InvalidParameterException {
        String[] temp = filter.split("/");
        Calendar filterdate = Calendar.getInstance();
        if (temp.length > 1) {
            try{
            filterdate.set(filterdate.get(Calendar.YEAR),
                    Integer.parseInt(temp[1]) - 1, Integer.parseInt(temp[0]));
            }catch(NumberFormatException e){
                
            }
        } else {
            // Exception should handle here show status to user
            throw new InvalidParameterException();
        }

        return filterdate;
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
            // System.out.println(T.getDate().get(Calendar.DATE)+"/"+T.getDate().get(Calendar.MONTH));
            // >= 0 means the current calendar is after or equals to the Task
            // calendar
            if (filterdate.compareTo(T.getDate()) < 1) {
                // System.out.println(filterdate.get(Calendar.DATE));
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

    private static void populateStatusList(List<Task> tempList, boolean done) {
        for (Task T : filteredTask) {
            if (T.isDone() != done) {
                tempList.remove(T);
            }
        }
    }

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
        for (Task task : StorageHandler.getAllTasks()) {
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

        for (String context : task.getContexts()) {
            if (!hashtagList.contains(context.toLowerCase())) {
                hashtagList.add(context.toLowerCase());
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

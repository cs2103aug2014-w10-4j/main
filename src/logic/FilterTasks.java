package chirptask.logic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

//@A0111930W
public class FilterTasks {

    private static List<Task> filteredTask;
    private static List<String> categoriesList;
    private static List<String> contextsList;
    private static String currentFilter = "";
    private static final int PARAM_FILTER = 1;

    static void filter(Task T) {
        currentFilter = T.getDescription();

        List<Task> allTask = StorageHandler.getAllTasks();
        // check 1st String to determine the type of filter

        if (currentFilter.isEmpty()) {
            filteredTask = StorageHandler.getAllTasks();
        } else {

            processFilter(currentFilter);

        }
        contextsList.clear();
        for (Task task : filteredTask) {
            populateContext(task);
        }
    }

    // Add in filter time, date, task, done, undone

    /**
     * Assuming that we use keywords like -TIME -DONE -UNDONE -DATE -TIME
     * Assuming if no flag indication means filter by that keyword
     * **/
    private static void processFilter(String filters) {
        String[] param = filters.split("\\s+");
        List<Task> templist = new ArrayList<Task>();
        for (int i = 0; i < param.length; i++) {
            String filter = param[i];

            switch (filter) {
                case "/done" :
                    // search done task
                    filterDone(templist, true);
                    break;
                case "/undone" :
                    // search undone task
                    filterDone(templist, false);
                    break;
                case "/floating" :
                    filterTaskType(templist, "floating");
                    break;
                case "/timed" :
                    filterTaskType(templist, "timedtask");
                    break;
                case "/deadline" :
                    filterTaskType(templist, "deadline");
                    break;
                default:
                    // Entire string keyword search
                    filterKeyword(templist, filter);
                    break;
            }

            filteredTask = templist;
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
            if (T.getDescription().contains(keywords)) {
                templist.add(T);
            }
        }
    }

    private static void filterDone(List<Task> tempList, boolean done) {
        populateDoneList(tempList, done);
        if (tempList.isEmpty()) {
            resetFilteredTask();
            populateDoneList(tempList, done);
        }
    }

    private static void populateDoneList(List<Task> tempList, boolean done) {
        for (Task T : filteredTask) {
            if (done) {
                if (T.isDone()) {
                    tempList.add(T);
                }
            } else {
                if (!T.isDone()) {
                    tempList.add(T);
                }
            }
        }
    }

    private static void resetFilteredTask() {
        filteredTask = StorageHandler.getAllTasks();
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
            if (T.getType().equalsIgnoreCase(taskType)) {
                tempList.add(T);
            }
        }
    }

    static void filter() {
        categoriesList = new ArrayList<String>();
        contextsList = new ArrayList<String>();
        if (currentFilter.isEmpty()) {
            filteredTask = StorageHandler.getAllTasks();
            populateCategoryAndContext();
        } else {
            filteredTask = new ArrayList<Task>();
            List<Task> allTask = StorageHandler.getAllTasks();

            for (Task a : allTask) {
                if (a.getDescription().equalsIgnoreCase(currentFilter)) {
                    filteredTask.add(a);
                }
            }
            
            categoriesList.clear();
            contextsList.clear();
            populateCategoryAndContext();
        }
    }

    private static void populateCategoryAndContext() {
        for (Task task : StorageHandler.getAllTasks()) {
            populateContext(task);
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

    private static void populateContext(Task task) {

        for (String context : task.getContexts()) {
            if (!contextsList.contains(context.toLowerCase())) {
                contextsList.add(context.toLowerCase());
            }
        }

    }

    public static List<Task> getFilteredList() {
        return filteredTask;
    }

    public static List<String> getContextList() {
        return contextsList;
    }

    public static List<String> getCategoryList() {
        return categoriesList;
    }
}

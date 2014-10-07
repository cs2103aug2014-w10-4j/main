package chirptask.logic;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import chirptask.gui.MainGui;
import chirptask.settings.Messages;
import chirptask.storage.DeadlineTask;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

//@A0111930W
public class DisplayView {
    /**
     * This will take in a filtered list and update the taskview, sort to
     * date/time, store into List of tasks
     * 
     * @param _gui
     * */
    public static void updateTaskView(List<Task> tasks, MainGui gui) {

        Collections.sort(tasks);
        TreeMap<String, TasksByDate> map = new TreeMap<String, TasksByDate>();

        processUpdateTaskView(tasks, gui, map);

        processUpdateContextAndCategoryView(gui);
        // Iterator<Map.Entry<Date, TasksByDate>> it =
        // map.entrySet().iterator();
        // TaskView view = new TaskView();
        // while (it.hasNext()) {
        // view.addToTaskView(it.next().getValue());
        // }

    }

    private static void processUpdateContextAndCategoryView(MainGui gui) {
        updateCategoryView(gui);
        updateContextView(gui);
    }

    private static void processUpdateTaskView(List<Task> tasks, MainGui gui,
            TreeMap<String, TasksByDate> map) {
        for (Task task : tasks) {
            String dateToString = "";
            Date taskViewDate = null;
            
            if (task.getType() == "floating") {
                dateToString = "all-day";
                taskViewDate = task.getDate();
            } else if (task instanceof DeadlineTask) {
                DeadlineTask dTask = (DeadlineTask) task;
                dateToString = "due by " + dTask.getDate().getHours() + ":"
                        + dTask.getDate().getMinutes();
                taskViewDate = task.getDate();
            } else if (task instanceof TimedTask) {
                TimedTask tTask = (TimedTask) task;
                dateToString = tTask.getStartTime().getHours() + ":"
                        + tTask.getStartTime().getMinutes() + " to "
                        + tTask.getEndTime().getHours() + ":"
                        + tTask.getEndTime().getMinutes();
                taskViewDate = tTask.getStartTime();
            }
            
            String currDate = MainGui.convertDateToString(taskViewDate);
            
            if (map.containsKey(currDate)) {
                map.get(currDate).addToTaskList(task);
            } else {
                TasksByDate dateTask = new TasksByDate();
                // dateTask.setTaskDate(task.getDate());
                // dateTask.addToTaskList(task);
                gui.addNewTaskViewDate(taskViewDate);
                map.put(currDate, dateTask);
            }

            gui.addNewTaskViewToDate(taskViewDate, task.getTaskId(),
                    task.getDescription(), dateToString, task.isDone());
        }
    }

    // Call this at init to show all tasks.
    public static void updateTaskView(MainGui gui) {

        List<Task> allTasks = StorageHandler.getAllTasks();
        if (allTasks != null) {
            Collections.sort(allTasks);
            updateTaskView(allTasks, gui);
        }

    }

    public static void updateCategoryView(MainGui gui) {
        List<String> categories = FilterTasks.getCategoryList();
        for (String category : categories) {
            gui.addCategoryIntoList(category);
        }
    }

    public static void updateContextView(MainGui gui) {
        List<String> contexts = FilterTasks.getContextList();
        for (String context : contexts) {
            gui.addContextIntoList(context);
        }
    }

    // Take in type, action
    public static void showStatusToUser(StatusType type, Action action,
            MainGui gui) {
        CommandType command = Logic.determineCommandType(action
                .getCommandType());
        if (type == StatusType.ERROR) {
            switch (command) {
                case ADD :
                    processGUI(action, gui, Messages.LOG_MESSAGE_ADD_TASK,
                            Messages.LOG_MESSAGE_ERROR);
                    break;
                case DELETE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_REMOVE_TASK,
                            Messages.LOG_MESSAGE_ERROR);
                    break;

                case EDIT :
                    processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                            Messages.LOG_MESSAGE_ERROR);

                    break;
                case DONE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_DONE,
                            Messages.LOG_MESSAGE_ERROR);
                    break;
                case UNDONE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                            Messages.LOG_MESSAGE_ERROR);
                    break;
                case LOGIN :
                    processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN,
                            Messages.LOG_MESSAGE_ERROR);
                    break;
            }
        } else {
            switch (command) {
                case ADD :
                    processGUI(action, gui, Messages.LOG_MESSAGE_ADD_TASK,
                            Messages.LOG_MESSAGE_SUCCESS);

                    break;
                case DELETE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_REMOVE_TASK,
                            Messages.LOG_MESSAGE_SUCCESS);
                    break;

                case EDIT :
                    processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                            Messages.LOG_MESSAGE_SUCCESS);

                    break;
                case DONE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_DONE,
                            Messages.LOG_MESSAGE_SUCCESS);

                    break;
                case UNDONE :
                    processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                            Messages.LOG_MESSAGE_SUCCESS);
                    break;
                case LOGIN :
                    processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN,
                            Messages.LOG_MESSAGE_SUCCESS);
                    break;
            }
        }
    }

    private static void processGuiLogin(MainGui gui, String message,
            String result) {
        if (result.equalsIgnoreCase(Messages.LOG_MESSAGE_SUCCESS)) {
            gui.setStatus(String.format(message, result));
        } else {
            gui.setError(String.format(message, result));
        }
    }

    private static void processGUI(Action action, MainGui gui, String message,
            String result) {
        if (result.equalsIgnoreCase(Messages.LOG_MESSAGE_SUCCESS)) {
            gui.setStatus(String.format(message, result, action.getTask()
                    .getDescription()));
        } else {
            gui.setError(String.format(message, result, action.getTask()
                    .getDescription()));
        }
    }
}

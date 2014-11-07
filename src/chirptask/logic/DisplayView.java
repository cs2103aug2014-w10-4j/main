//@author A0111930W
package chirptask.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.common.Settings.StatusType;
import chirptask.gui.MainGui;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;


public class DisplayView {
    private static final int START_LIST = 0;
    private static final String TASK_FLOATING = "floating";
    private static final String TASK_ALL_DAY = "all-day";

    /**
     * This will take in a filtered list and update the taskview, sort to
     * date/time, store into List of tasks
     * 
     * @author a0111930w
     * @param _gui
     * @param List
     *            <Task>
     * 
     * */
    public static void updateTaskView(List<Task> tasks, MainGui gui) {

        sortTask(tasks);
        processUpdateTaskView(tasks, gui);
        processUpdateContextAndCategoryView(gui);
        // Iterator<Map.Entry<Date, TasksByDate>> it =
        // map.entrySet().iterator();
        // TaskView view = new TaskView();
        // while (it.hasNext()) {
        // view.addToTaskView(it.next().getValue());
        // }

    }
    /**
     * Method will sort the task
     * @param tasks
     */
    private static void sortTask(List<Task> tasks) {
        Collections.sort(tasks);
    }

    
    /**
     * This method will update the Context and category on the GUI
     * 
     * @author A0111930W
     * @param gui
     */
    private static void processUpdateContextAndCategoryView(MainGui gui) {
        updateCategoryView(gui);
        updateContextView(gui);
    }

    
    /**
     * This method will update the user GUI view. The GUI view will be sorted to
     * all tasks under a date.
     * 
     * @author A0111930W
     * @param tasks
     * @param gui
     * @param map
     */
    private synchronized static void processUpdateTaskView(List<Task> tasks,
            MainGui gui) {

        for (int i = START_LIST; i < tasks.size(); i++) {
            Task T = tasks.get(i);
            updateTaskViewDate(gui, T);
            String dateToString = convertTaskDateToDurationString(T);
            updateTaskToDate(gui, T, dateToString);
        }
        //Unused
        // Iterator<Task> itr = tasks.iterator();
        // while (itr.hasNext()) {
        // Task T = itr.next();
        // gui.addNewTaskViewDate(T.getDate());
        // String dateToString = convertTaskDateToDurationString(T);
        // gui.addNewTaskViewToDate(T.getDate(), T.getTaskId(),
        // T.getDescription(), dateToString, T.isDone());
        // }

        // for (Task task : tasks) {
        // gui.addNewTaskViewDate(task.getDate());
        // String dateToString = convertTaskDateToDurationString(task);
        // gui.addNewTaskViewToDate(task.getDate(), task.getTaskId(),
        // task.getDescription(), dateToString, task.isDone());
        // }
    }
    
    /**
     * Method will call GUI method to update the task under the respective date
     * @param gui
     * @param T
     * @param dateToString
     */
    private static void updateTaskToDate(MainGui gui, Task T,
            String dateToString) {
        gui.addNewTaskViewToDate(T.getDate(), T.getTaskId(),
                T.getDescription(), dateToString, T.isDone());
    }
    
    /**
     * Method will call GUI method to create a date view
     * @param gui
     * @param T
     */
    private static void updateTaskViewDate(MainGui gui, Task T) {
        gui.addNewTaskViewDate(T.getDate());
    }

    //@author A0111889W
    /**
     * Assuming there are only 3 type of task we need to handle
     * 
     * @param task
     * @return String
     */
    public static String convertTaskDateToDurationString(Task task) {
        assert task != null && task.getDate() != null;
        String dateToString = "";
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");

        if (task.getType() == TASK_FLOATING) {
            dateToString = TASK_ALL_DAY;
        } else if (task.getType() == "deadline") {
            DeadlineTask dTask = (DeadlineTask) task;
            dateToString = "due by " + sdf.format(dTask.getDate().getTime());
        } else if (task.getType() == "timedtask") {
            TimedTask tTask = (TimedTask) task;
            dateToString = sdf.format(tTask.getStartTime().getTime()) + " to "
                    + sdf.format(tTask.getEndTime().getTime());
        } else {
            assert false;
        }
        return dateToString;
    }

    //@author A0111930W
    // Call this at init to show all tasks.
    public static void updateTaskView(MainGui gui) {
        List<Task> allTasks = FilterTasks.getFilteredList();// StorageHandler.getAllTasks();
        if (allTasks != null) {
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

    public static void showStatusToUser(String Message, MainGui gui) {
        gui.setStatus(Message);
    }
    
    public static void showStatusToUser(String Message, MainGui gui, boolean success){
           if(success){
               gui.setStatus(Message);
           }else{
               gui.setError(Message);
           }
    }

    public static void showStatusToUser(Settings.StatusType type, MainGui gui,
            String filter) {
        if (type == Settings.StatusType.ERROR) {
            processGUIError(gui, Messages.LOG_MESSAGE_INVALID_COMMAND,
                    Messages.LOG_MESSAGE_ERROR, "");
        } else {
            processGUIError(gui, Messages.LOG_MESSAGE_DISPLAY,
                    Messages.LOG_MESSAGE_SUCCESS, filter);
        }
    }

    /**
     * This method will show the status result to user after each action user
     * input.
     * 
     * @author A0111930W
     * @param type
     * @param action
     * @param gui
     * 
     * 
     */
    public static void showStatusToUser(Settings.StatusType type,
            Action action, MainGui gui) {
        CommandType command = action.getCommandType();
        if (type == Settings.StatusType.ERROR) {
            switch (command) {
            case ADD:
                processGUI(action, gui, Messages.LOG_MESSAGE_ADD_TASK,
                        Messages.LOG_MESSAGE_ERROR);
                break;
            case DELETE:
                processGUI(action, gui, Messages.LOG_MESSAGE_REMOVE_TASK,
                        Messages.LOG_MESSAGE_ERROR);
                break;

            case EDIT:
                processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                        Messages.LOG_MESSAGE_ERROR);

                break;
            case DONE:
                processGUI(action, gui, Messages.LOG_MESSAGE_DONE,
                        Messages.LOG_MESSAGE_ERROR);
                break;
            case UNDONE:
                processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                        Messages.LOG_MESSAGE_ERROR);
                break;
            case LOGIN:
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN, false,
                        Messages.LOG_MESSAGE_ERROR);
                break;
            case SYNC:
                processGuiLogin(gui, Messages.LOG_MESSAGE_SYNC_FAIL, false,
                        Messages.LOG_MESSAGE_FAIL);
                break;
            case LOGOUT:
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGOUT_FAIL, false,
                        "");
                break;
            default:
                processGUIError(gui, Messages.LOG_MESSAGE_INVALID_COMMAND,
                        Messages.LOG_MESSAGE_ERROR, "");
                break;
            }
        } else {
            switch (command) {
            case ADD:
                processGUI(action, gui, Messages.LOG_MESSAGE_ADD_TASK,
                        Messages.LOG_MESSAGE_SUCCESS);

                break;
            case DELETE:
                processGUI(action, gui, Messages.LOG_MESSAGE_REMOVE_TASK,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;

            case EDIT:
                processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                        Messages.LOG_MESSAGE_SUCCESS);

                break;
            case DONE:
                processGUI(action, gui, Messages.LOG_MESSAGE_DONE,
                        Messages.LOG_MESSAGE_SUCCESS);

                break;
            case UNDONE:
                processGUI(action, gui, Messages.LOG_MESSAGE_MODIFY_TASK,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;
            case LOGIN:
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN, true,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;
            case LOGOUT:
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGOUT_SUCCESS, true,
                        "");
                break;
            case DISPLAY:
                processGUI(action, gui, Messages.LOG_MESSAGE_DISPLAY,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;
            case SYNC:
                processGuiLogin(gui, Messages.LOG_MESSAGE_SYNC, true,
                        Messages.LOG_MESSAGE_SYN_INIT);
                break;
            default:

                break;
            }
        }
    }

    /**
     * This method will process the show the user error when a wrong command is
     * input.
     * 
     * @author A0111930W
     * @param action
     * @param gui
     * @param logMessageInvalidCommand
     * @param logMessageError
     */
    private static void processGUIError(MainGui gui,
            String logMessageInvalidCommand, String logMessageError,
            String filter) {
        if (logMessageError == Messages.LOG_MESSAGE_ERROR) {
            gui.setError(String.format(logMessageInvalidCommand));
        } else {
            gui.setStatus(String.format(logMessageInvalidCommand,
                    logMessageError, filter));
        }
    }

    /**
     * This method will show the failure or success of login to user.
     * 
     * @param gui
     * @param message
     * @param result
     */
    private static void processGuiLogin(MainGui gui, String message,
            Boolean isSuccess, String result) {
        if (isSuccess) {
            gui.setStatus(String.format(message, result));
        } else {
            gui.setError(String.format(message, result));
        }
    }

    /**
     * This method will show the failure or success for simple
     * add/delete/done/undone/login/display
     * 
     * @param action
     * @param gui
     * @param message
     * @param result
     */
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

    // @author A0111889W
    public static TextFlow parseDescriptionToTextFlow(String description,
            boolean done, MainGui _gui) {
        TextFlow parsedDesc = new TextFlow();
        StringBuilder descSb = new StringBuilder(description);
        Text bufferText = new Text();

        while (descSb.length() > 0) {
            int index = descSb.length();

            boolean hasSpaceInDesc = descSb.indexOf(" ") > 0;

            if (hasSpaceInDesc) {
                index = descSb.indexOf(" ");
            } else if (descSb.indexOf(" ") == 0) {
                index = 1;
            }

            // obtain description till first space
            bufferText = new Text(descSb.substring(0, index));

            if (descSb.charAt(0) == Settings.CONTEXT_CHAR) {
                // Context
                bufferText.getStyleClass().add("context-text");
                bufferText.setOnMouseClicked(_gui.clickOnContext());
            } else if (descSb.charAt(0) == Settings.CATEGORY_CHAR) {
                // Category
                bufferText.getStyleClass().add("category-text");
                bufferText.setOnMouseClicked(_gui.clickOnCategory());
            }

            descSb.delete(0, index);
            bufferText.setStrikethrough(done);
            parsedDesc.getChildren().add(bufferText);
        }

        return parsedDesc;
    }

    // @author A0111889W
    public static String convertDateToString(Calendar date) {
        assert date != null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String parseDateToString = sdf.format(date.getTime());
        return parseDateToString;
    }

    public static void showStatusToUser(StatusType message, CommandType type,
            MainGui _gui) {
        assert _gui != null;
        processGUI(message, type, _gui);
    }

    private static void processGUI(StatusType message, CommandType type,
            MainGui _gui) {
        assert _gui != null;
        if (message == StatusType.ERROR) {

            _gui.setError(String.format(
                    Messages.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                    Messages.LOG_MESSAGE_FAIL, type.toString()));
        } else {

            _gui.setStatus(String.format(
                    Messages.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                    Messages.LOG_MESSAGE_SUCCESS, type.toString()));
        }

    }
}

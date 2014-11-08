//@author A0111930W
package chirptask.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

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
        processUpdateHashtagAndCategoryView(gui);
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
    private static void processUpdateHashtagAndCategoryView(MainGui gui) {
        updateCategoryView(gui);
        updateHashtagView(gui);
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
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");


        if (Task.TASK_FLOATING.equals(task.getType())) {
            dateToString = "";
        } else if (Task.TASK_DEADLINE.equals(task.getType())) {
            DeadlineTask dTask = (DeadlineTask) task;
            dateToString = "due by " + sdf.format(dTask.getDate().getTime());
        } else if (Task.TASK_TIMED.equals(task.getType())) {
            TimedTask tTask = (TimedTask) task;
            dateToString = sdf.format(tTask.getStartTime().getTime()) + " to "
                    + sdf.format(tTask.getEndTime().getTime());
        } else {
            assert false;
        }
        return dateToString;
    }

    //@author A0111930W
    /**
     * This method will be call during init to display all task
     * @param gui
     */
    public static void updateTaskView(MainGui gui) {
        List<Task> allTasks = FilterTasks.getFilteredList();
        if (allTasks != null) {
            updateTaskView(allTasks, gui);
        }

    }
    /**
     * This method will call gui to update the category view.
     * @param gui
     */
    public static void updateCategoryView(MainGui gui) {
        List<String> categories = FilterTasks.getCategoryList();
        for (String category : categories) {
            gui.addCategoryIntoList(category);
        }
    }
    
    /**
     * This method will call gui to update the Hashtag view.
     * @param gui
     */
    public static void updateHashtagView(MainGui gui) {
        List<String> contexts = FilterTasks.getContextList();
        for (String context : contexts) {
            gui.addHashtagIntoList(context);
        }
    }
    
    /**
     * Show status to user with the respective message
     * @param Message
     * @param gui
     */
    public static void showStatusToUser(String Message, MainGui gui) {
        gui.setStatus(Message);
    }
    
    /**
     * Show status to user depending on the success
     * @param Message
     * @param gui
     * @param success
     */
    public static void showStatusToUser(String Message, MainGui gui,
            boolean success) {
        if (success) {
            gui.setStatus(Message);
        } else {
            gui.setError(Message);
        }
    }
    
    /**
     * Show status to user depending on the execution of display command
     * @param type
     * @param gui
     * @param filter
     */
    public static void showStatusToUser(Settings.StatusType type, MainGui gui,
            String filter) {
        if (isStatusError(type)) {
            processGUIError(gui, Messages.LOG_MESSAGE_DISPLAY_USAGE,
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
        if (isStatusError(type)) {
            processErrorGui(action, gui, command);
        } else {
            processSuccessGui(action, gui, command);
        }
    }
    /**
     * Method will display success message to user
     * @param action
     * @param gui
     * @param command
     */
    private static void processSuccessGui(Action action, MainGui gui,
            CommandType command) {
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
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN, true,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;
            case LOGOUT :
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGOUT_SUCCESS,
                        true, "");
                break;
            case DISPLAY :
                processGUI(action, gui, Messages.LOG_MESSAGE_DISPLAY,
                        Messages.LOG_MESSAGE_SUCCESS);
                break;
            case SYNC :
                processGuiLogin(gui, Messages.LOG_MESSAGE_SYNC, true,
                        Messages.LOG_MESSAGE_SYN_INIT);
                break;
            default:
                assert false;
                break;
        }
    }
    /**
     * Method will display error message to user
     * @param action
     * @param gui
     * @param command
     */
    private static void processErrorGui(Action action, MainGui gui,
            CommandType command) {
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
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGIN, false,
                        Messages.LOG_MESSAGE_ERROR);
                break;
            case SYNC :
                processGuiLogin(gui, Messages.LOG_MESSAGE_SYNC_FAIL, false,
                        Messages.LOG_MESSAGE_FAIL);
                break;
            case LOGOUT :
                processGuiLogin(gui, Messages.LOG_MESSAGE_LOGOUT_FAIL,
                        false, "");
                break;
            default:
                processGUIError(gui, Messages.LOG_MESSAGE_INVALID_COMMAND,
                        Messages.LOG_MESSAGE_ERROR, "");
                break;
        }
    }
    
    /**
     * Return true if statustype is error, else return true
     * @param type
     * @return
     */
    private static boolean isStatusError(Settings.StatusType type) {
        return type == Settings.StatusType.ERROR;
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
        if (isLogMessageError(logMessageError)) {
            gui.setError(String.format(logMessageInvalidCommand));
        } else {
            gui.setStatus(String.format(logMessageInvalidCommand,
                    logMessageError, filter));
        }
    }
    /**
     * Method will return true is is a error message, else false
     * @param logMessageError
     * @return
     */
    private static boolean isLogMessageError(String logMessageError) {
        return logMessageError == Messages.LOG_MESSAGE_ERROR;
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

    //@author A0111889W
    public static TextFlow parseDescriptionToTextFlow(String description,
            boolean done, MainGui _gui) {
        TextFlow parsedDesc = new TextFlow();
        StringBuilder descStringBuilder = new StringBuilder(description);
        Text bufferText = new Text();

        while (descStringBuilder.length() > 0) {
            int index = descStringBuilder.length();

            boolean hasSpaceCharInDesc = descStringBuilder.indexOf(" ") > 0;

            if (hasSpaceCharInDesc) {
                index = descStringBuilder.indexOf(" ");
            } else if (descStringBuilder.indexOf(" ") == 0) {
                index = 1;
            }

            // obtain description until the first space
            bufferText = new Text(descStringBuilder.substring(0, index));

            if (descStringBuilder.charAt(0) == Settings.HASHTAG_CHAR) {
                // Context
                bufferText.getStyleClass().add("hashtag-text");
                bufferText.setOnMouseClicked(_gui.clickOnHashtag());
            } else if (descStringBuilder.charAt(0) == Settings.CATEGORY_CHAR) {
                // Category
                bufferText.getStyleClass().add("category-text");
                bufferText.setOnMouseClicked(_gui.clickOnCategory());
            }
            
            // delete parsed text
            descStringBuilder.delete(0, index);
            bufferText.setStrikethrough(done);
            parsedDesc.getChildren().add(bufferText);
        }

        return parsedDesc;
    }

    //@author A0111889W
    public static String convertDateToString(Calendar date) {
        assert date != null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/YYYY");
        String parseDateToString = sdf.format(date.getTime());
        return parseDateToString;
    }
    
    //@author A0111930W
    /**
     * Show message and command type to user.
     * @param message
     * @param type
     * @param _gui
     */
    public static void showStatusToUser(StatusType message, CommandType type,
            MainGui _gui) {
        assert _gui != null;
        processGUI(message, type, _gui);
    }
    
    /**
     * Method will call gui and show status to user
     * @param message
     * @param type
     * @param _gui
     */

    private static void processGUI(StatusType message, CommandType type,
            MainGui _gui) {
        assert _gui != null;
        if (isStatusError(message)) {

            _gui.setError(formatStringError(type));
        } else {

            _gui.setStatus(formatStringSuccess(type));
        }

    }
    /**
     * Format the success message
     * @param type
     * @return
     */
    private static String formatStringSuccess(CommandType type) {
        return String.format(
                Messages.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                Messages.LOG_MESSAGE_SUCCESS, type.toString());
    }
    /**
     * Format the error message
     * @param type
     * @return
     */
    private static String formatStringError(CommandType type) {
        return String.format(
                Messages.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                Messages.LOG_MESSAGE_FAIL, type.toString());
    }
}

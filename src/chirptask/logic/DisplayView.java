//@author A0111930W
package chirptask.logic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import chirptask.common.Constants;
import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.common.Settings.StatusType;
import chirptask.gui.MainGui;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

/**
 * This class handles the processing of filtertask before displaying the results
 * to user
 * using MainGui methods.
 * 
 *
 */

public class DisplayView {
    private static final int START_LIST = 0;

    /**
     * This will take in a filtered list and update the taskview, sort to
     * date/time, store into List of tasks
     * 
     * @param tasks
     *            The list of filtered tasks
     * @param gui
     *            The MainGui object to manipulate
     */
    public static void updateTaskView(List<Task> tasks, MainGui gui) {

        sortTask(tasks);
        processUpdateTaskView(tasks, gui);
        processUpdateHashtagAndCategoryView(gui);

    }

    /**
     * Method will sort the task
     * 
     * @param tasks
     *            The list of tasks to sort
     */
    private static void sortTask(List<Task> tasks) {
        Collections.sort(tasks);
    }

    /**
     * This method will update the Context and category on the GUI
     * 
     * 
     * @param gui
     *            The MainGui object to manipulate
     */
    private static void processUpdateHashtagAndCategoryView(MainGui gui) {
        updateCategoryView(gui);
        updateHashtagView(gui);
    }

    /**
     * This method will update the user GUI view. The GUI view will be sorted to
     * all tasks under a date.
     * 
     * 
     * @param tasks
     *            The list of tasks to loop through
     * @param gui
     *            The MainGui object to manipulate
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
     * 
     * @param gui
     *            The MainGui object to manipulate
     * @param T
     *            The Task to update in GUI
     * @param dateToString
     *            The date String to update in GUI
     */
    private static void updateTaskToDate(MainGui gui, Task T,
            String dateToString) {
        gui.addNewTaskViewToDate(T.getDate(), T.getTaskId(),
                T.getDescription(), dateToString, T.isDone());
    }

    /**
     * Method will call GUI method to create a date view
     * 
     * @param gui
     *            The MainGui object to manipulate
     * @param T
     *            The Task to add the date to GUI
     */
    private static void updateTaskViewDate(MainGui gui, Task T) {
        gui.addNewTaskViewDate(T.getDate());
    }

    //@author A0111889W
    /**
     * Returns the string representation of the time for a task.
     * Floating: ""
     * Timed: start to end
     * Deadline: due by time
     * 
     * @param task
     *            The task to process
     * @return String The string to display on the MainGui
     */
    public static String convertTaskDateToDurationString(Task task) {
        assert task != null && task.getDate() != null;
        String dateToString = "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_HH_MM);

        if (Task.TASK_FLOATING.equals(task.getType())) {
            dateToString = "";
        } else if (Task.TASK_DEADLINE.equals(task.getType())) {
            DeadlineTask dTask = (DeadlineTask) task;
            dateToString = "due by" + " "
                    + sdf.format(dTask.getDate().getTime());
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
     * 
     * @param gui
     *            The MainGui object to manipulate
     */
    public static void updateTaskView(MainGui gui) {
        List<Task> allTasks = FilterTasks.getFilteredList();
        if (allTasks != null) {
            updateTaskView(allTasks, gui);
        }

    }

    /**
     * This method will call gui to update the category view.
     * 
     * @param gui
     *            The MainGui object to manipulate
     */
    public static void updateCategoryView(MainGui gui) {
        List<String> categories = FilterTasks.getCategoryList();
        for (String category : categories) {
            gui.addCategoryIntoList(category);
        }
    }

    /**
     * This method will call gui to update the Hashtag view.
     * 
     * @param gui
     *            The MainGui object to manipulate
     */
    public static void updateHashtagView(MainGui gui) {
        List<String> contexts = FilterTasks.getContextList();
        for (String context : contexts) {
            gui.addHashtagIntoList(context);
        }
    }

    /**
     * Show status to user with the respective message
     * 
     * @param Message
     *            The status message to update
     * @param gui
     *            The MainGui object to manipulate
     */
    public static void showStatusToUser(String Message, MainGui gui) {
        gui.setStatus(Message);
    }

    /**
     * Show status to user depending on the success
     * 
     * @param Message
     *            The status message to update
     * @param gui
     *            The MainGui object to manipulate
     * @param success
     *            The flag of whether it is successful or not
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
     * 
     * @param type
     *            The StatusType to update on GUI
     * @param gui
     *            The MainGui object to manipulate
     * @param filter
     *            The filters applied
     */
    public static void showStatusToUser(Settings.StatusType type, MainGui gui,
            String filter) {
        if (isStatusError(type)) {
            processGUIError(gui, Constants.LOG_MESSAGE_DISPLAY_USAGE,
                    Constants.LOG_MESSAGE_ERROR, "");
        } else {
            processGUIError(gui, Constants.LOG_MESSAGE_DISPLAY,
                    Constants.LOG_MESSAGE_SUCCESS, filter);
        }
    }

    /**
     * This method will show the status result to user after each action user
     * input.
     * 
     * 
     * @param type
     *            The StatusType to update on GUI
     * @param action
     *            The Action that was being processed
     * @param gui
     *            The MainGui object to manipulate
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
     * 
     * @param action
     *            The Action that was being processed
     * @param gui
     *            The MainGui object to manipulate
     * @param command
     *            The CommandType to process
     */
    private static void processSuccessGui(Action action, MainGui gui,
            CommandType command) {
        switch (command) {
            case ADD :
                processGUI(action, gui, Constants.LOG_MESSAGE_ADD_TASK,
                        Constants.LOG_MESSAGE_SUCCESS);

                break;
            case DELETE :
                processGUI(action, gui, Constants.LOG_MESSAGE_REMOVE_TASK,
                        Constants.LOG_MESSAGE_SUCCESS);
                break;

            case EDIT :
                processGUI(action, gui, Constants.LOG_MESSAGE_MODIFY_TASK,
                        Constants.LOG_MESSAGE_SUCCESS);

                break;
            case DONE :
                processGUI(action, gui, Constants.LOG_MESSAGE_DONE,
                        Constants.LOG_MESSAGE_SUCCESS);

                break;
            case UNDONE :
                processGUI(action, gui, Constants.LOG_MESSAGE_MODIFY_TASK,
                        Constants.LOG_MESSAGE_SUCCESS);
                break;
            case LOGIN :
                processGuiLogin(gui, Constants.LOG_MESSAGE_LOGIN, true,
                        Constants.LOG_MESSAGE_SUCCESS);
                break;
            case LOGOUT :
                processGuiLogin(gui, Constants.LOG_MESSAGE_LOGOUT_SUCCESS,
                        true, "");
                break;
            case DISPLAY :
                processGUI(action, gui, Constants.LOG_MESSAGE_DISPLAY,
                        Constants.LOG_MESSAGE_SUCCESS);
                break;
            case SYNC :
                processGuiLogin(gui, Constants.LOG_MESSAGE_SYNC, true,
                        Constants.LOG_MESSAGE_SYN_INIT);
                break;
            default:
                assert false;
                break;
        }
    }

    /**
     * Method will display error message to user
     * 
     * @param action
     *            The Action to process
     * @param gui
     *            The MainGui object to manipulate
     * @param command
     *            The CommandType to process
     */
    private static void processErrorGui(Action action, MainGui gui,
            CommandType command) {
        switch (command) {
            case ADD :
                processGUI(action, gui, Constants.LOG_MESSAGE_ADD_TASK,
                        Constants.LOG_MESSAGE_ERROR);
                break;
            case DELETE :
                processGUI(action, gui, Constants.LOG_MESSAGE_REMOVE_TASK,
                        Constants.LOG_MESSAGE_ERROR);
                break;

            case EDIT :
                processGUI(action, gui, Constants.LOG_MESSAGE_MODIFY_TASK,
                        Constants.LOG_MESSAGE_ERROR);

                break;
            case DONE :
                processGUI(action, gui, Constants.LOG_MESSAGE_DONE,
                        Constants.LOG_MESSAGE_ERROR);
                break;
            case UNDONE :
                processGUI(action, gui, Constants.LOG_MESSAGE_MODIFY_TASK,
                        Constants.LOG_MESSAGE_ERROR);
                break;
            case LOGIN :
                processGuiLogin(gui, Constants.LOG_MESSAGE_LOGIN, false,
                        Constants.LOG_MESSAGE_ERROR);
                break;
            case SYNC :
                processGuiLogin(gui, Constants.LOG_MESSAGE_SYNC_FAIL, false,
                        Constants.LOG_MESSAGE_FAIL);
                break;
            case LOGOUT :
                processGuiLogin(gui, Constants.LOG_MESSAGE_LOGOUT_FAIL, false,
                        "");
                break;
            default:
                processGUIError(gui, Constants.LOG_MESSAGE_INVALID_COMMAND,
                        Constants.LOG_MESSAGE_ERROR, "");
                break;
        }
    }

    /**
     * Return true if statustype is error, else return true
     * 
     * @param type
     *            The StatusType to compare
     * @return True if error, false otherwise
     */
    private static boolean isStatusError(Settings.StatusType type) {
        return type == Settings.StatusType.ERROR;
    }

    /**
     * This method will process the show the user error when a wrong command is
     * input.
     * 
     * 
     * @param action
     *            The Action to process
     * @param gui
     *            The MainGui object to manipulate
     * @param logMessageInvalidCommand
     *            The message to log
     * @param logMessageError
     *            The message to log
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
     * 
     * @param logMessageError
     *            The message to log
     * @return True if error, false otherwise
     */
    private static boolean isLogMessageError(String logMessageError) {
        return logMessageError == Constants.LOG_MESSAGE_ERROR;
    }

    /**
     * This method will show the failure or success of login to user.
     * 
     * @param gui
     *            The MainGui object to manipulate
     * @param message
     *            The message to display
     * @param result
     *            The result to display
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
     *            The Action to process
     * @param gui
     *            The MainGui object to manipulate
     * @param message
     *            The message to display
     * @param result
     *            The result to display
     */
    private static void processGUI(Action action, MainGui gui, String message,
            String result) {
        if (result.equalsIgnoreCase(Constants.LOG_MESSAGE_SUCCESS)) {
            gui.setStatus(String.format(message, result, action.getTask()
                    .getDescription()));
        } else {
            gui.setError(String.format(message, result, action.getTask()
                    .getDescription()));
        }
    }

    //@author A0111889W
    /**
     * This method converts a description string value into the corresponding
     * textflow object, with the hashtags and categories coloured accordingly
     * and their eventhandlers set.
     * 
     * @param description
     *            string value to turn into text flow object.
     * @param done
     *            status of task. If true, text will be strikethroughed
     * @param _gui
     *            MainGui Object with event for mouse click on category/hashtag
     * @return TextFlow object of description
     * @throws NullPointerException
     *             description and gui cannot be null
     */
    public static TextFlow parseDescriptionToTextFlow(String description,
            boolean done, MainGui _gui) throws NullPointerException {
        if (description == null || _gui == null) {
            throw new NullPointerException();
        }
        TextFlow parsedDesc = new TextFlow();
        StringBuilder descStringBuilder = new StringBuilder(description);
        Text bufferText = new Text();
        String singleSpace = " ";
        int zero = 0;

        while (descStringBuilder.length() > zero) {
            int index = descStringBuilder.length();

            boolean hasSpaceCharInDesc = descStringBuilder.indexOf(singleSpace) > zero;

            if (hasSpaceCharInDesc) {
                index = descStringBuilder.indexOf(singleSpace);
            } else if (descStringBuilder.indexOf(singleSpace) == zero) {
                index = 1;
            }

            // obtain description until the first space
            bufferText = new Text(descStringBuilder.substring(zero, index));

            if (descStringBuilder.charAt(zero) == Constants.HASHTAG_CHAR) {
                // Context
                bufferText.getStyleClass().add("hashtag-text");
                bufferText.setOnMouseClicked(_gui.clickOnHashtag());
            } else if (descStringBuilder.charAt(zero) == Constants.CATEGORY_CHAR) {
                // Category
                bufferText.getStyleClass().add("category-text");
                bufferText.setOnMouseClicked(_gui.clickOnCategory());
            }

            // delete parsed text
            descStringBuilder.delete(zero, index);
            bufferText.setStrikethrough(done);
            parsedDesc.getChildren().add(bufferText);
        }

        return parsedDesc;
    }

    //@author A0111889W
    /**
     * Converts date object into the corresponding string value with DD/MM/YYYY
     * format.
     * 
     * @param date
     *            date object to parse into string.
     * @return String string representation of date object
     * @throws NullPointerException
     *             date object cannot be null
     */
    public static String convertDateToString(Calendar date)
            throws NullPointerException {
        if (date == null) {
            throw new NullPointerException();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(
                Constants.DATE_FORMAT_DD_MM_YYYY);
        String parseDateToString = sdf.format(date.getTime());
        return parseDateToString;
    }

    //@author A0111889W
    /**
     * Shell method for GUI to invoke logic class to autocomplete user input
     * field with task description.
     * 
     * @param input
     *            Current user input value. Should be of the form
     *            "edit [number]"
     * @param _gui
     *            MainGui object that contains the user input field to
     *            autocomplete.
     */
    public static void autocompleteEditWithTaskDescription(String input,
            MainGui _gui) {
        assert !input.trim().isEmpty() && _gui != null;
        FilterTasks.editCli(input, _gui);
    }

    //@author A0111930W
    /**
     * Show message and command type to user.
     * 
     * @param message
     *            The message to display
     * @param type
     *            The CommandType to display
     * @param _gui
     *            The MainGui object to manipulate
     */
    public static void showStatusToUser(StatusType message, CommandType type,
            MainGui _gui) {
        assert _gui != null;
        processGUI(message, type, _gui);
    }

    /**
     * Method will call gui and show status to user
     * 
     * @param message
     *            The StatusType to process
     * @param type
     *            The CommandType to process
     * @param _gui
     *            The MainGui object to manipulate
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
     * 
     * @param type
     *            The CommandType to process
     * @return The formatted success string
     */
    private static String formatStringSuccess(CommandType type) {
        return String.format(Constants.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                Constants.LOG_MESSAGE_SUCCESS, type.toString());
    }

    /**
     * Format the error message
     * 
     * @param type
     *            The CommandType to process
     * @return The formatted error string
     */
    private static String formatStringError(CommandType type) {
        return String.format(Constants.LOG_MESSAGE_SUCCESS_OR_FAILURE,
                Constants.LOG_MESSAGE_FAIL, type.toString());
    }
}

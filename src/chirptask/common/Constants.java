//@author A0111889W
package chirptask.common;

public class Constants {
    public static final String LOG_MESSAGE_ADD_TASK = "%1$s Added new task %2$s.";
    public static final String LOG_MESSAGE_REMOVE_TASK = "%1$s Removed task %2$s.";
    public static final String LOG_MESSAGE_MODIFY_TASK = "%1$s Modified task %2$s.";
    public static final String LOG_MESSAGE_GET_TASK = "%1$s Retrieved task with Id: %2$s.";
    public static final String LOG_MESSAGE_GET_ALL_TASKS = "%1$s Retrieved all tasks.";
    public static final String LOG_MESSAGE_INVALID_COMMAND = "Valid commands: add addd addt edit delete done undone display filter clear undo login logout sync";
    public static final String LOG_MESSAGE_INVALID_TASK_TYPE = "Invalid Task Type.";
    public static final String LOG_MESSAGE_LOGIN = "%1$s Login";
    public static final String LOG_MESSAGE_SYNC = "%1$s Sync to google";
    public static final String LOG_MESSAGE_SYNC_FAIL = "%1$s Sync to google, Please login to enable sync";
    public static final String LOG_MESSAGE_DONE = "%1$s: Done %2$s";
    public static final String LOG_MESSAGE_UNDONE = "%1$s: Undone %2$s";
    public static final String LOG_MESSAGE_DISPLAY = "%1$s Displayed %2$s.";
    public static final String LOG_MESSAGE_SUCCESS = "Successfully";
    public static final String LOG_MESSAGE_ERROR = "Error";
    public static final String LOG_MESSAGE_FAIL = "Fail to ";
    public static final String LOG_MESSAGE_UNDO_NOTHING = "Nothing to undo";
    public static final String LOG_MESSAGE_SUCCESS_OR_FAILURE = "%1$s execute %2$s";
    public static final String LOG_MESSAGE_LOGOUT_FAIL = "You are not logged in.";
    public static final String LOG_MESSAGE_LOGOUT_SUCCESS = "Successfully logout.";
    public static final String LOG_MESSAGE_SYN_INIT = "Initiated";
    public static final String LOG_MESSAGE_UNEXPECTED = "Unexpected behaviour encountered.";

    public static final String LOG_MESSAGE_ADD_USAGE = "Usage: Add <Task> | Addd <Task> by MM/DD | Addt <Task> from HH to HH DD/MM";
    public static final String LOG_MESSAGE_DELETE_USAGE = "Usage: delete <Task no> | delete <Task no>-<Task no>,<Task no>";
    public static final String LOG_MESSAGE_DISPLAY_USAGE = "Usage: display <keyword> | display /<category>";
    public static final String LOG_MESSAGE_EDIT_USAGE = "Usage: edit <Task no> <desc>";
    public static final String LOG_MESSAGE_UNDO_USAGE = "Usage: undo";
    public static final String LOG_MESSAGE_UNDONE_USAGE = "Usage: undone <Task no> | undone <Task no>-<Task no>";
    public static final String LOG_MESSAGE_DONE_USAGE = "Usage: done <Task no> | done <Task no>-<Task no>";
    public static final String LOG_MESSAGE_LOGIN_USAGE = "Usage: login";
    public static final String LOG_MESSAGE_LOGOUT_USAGE = "Usage: logout";
    public static final String LOG_MESSAGE_SYNC_USAGE = "Usage: sync";
    public static final String LOG_MESSAGE_CLEAR_USAGE = "Usage: clear";

    public static char CATEGORY_CHAR = '@';
    public static char HASHTAG_CHAR = '#';

    public static String CATEGORY = CATEGORY_CHAR + "";
    public static String CONTEXT = HASHTAG_CHAR + "";

    public static final String LABEL_CATEGORIES = "Categories ("
            + CATEGORY_CHAR + ")";
    public static final String LABEL_HASHTAGS = "Hashtags (" + HASHTAG_CHAR
            + ")";
    public static final String STATUS_ERROR = "Error: %1$s";
    public static final String STATUS_NORMAL = "Status: %1$s";

    public static final String TITLE_SETTINGS = "Settings";
    public static final String TITLE_SOFTWARE = "ChirpTask";
    public static final String TITLE_LOGGING_IN = "Connecting";
    public static final String TITLE_OFFLINE = "Offline";
    public static final String TITLE_ONLINE = "Online";
    public static final String TITLE_SYNCING = "Syncing";
    public static final String TITLE_SYNC_FAIL = "Sync failed";

    public static final String DEFAULT_STATUS = "Nothing is happening.";

    public static final String ERROR = "%1$s: Error %2$s";
    public static final String ERROR_LOCAL = "Local storage %2$s.";
    public static final String INVALID_INPUT = "Invalid input: %1$s.";

}

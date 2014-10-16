package chirptask.common;

/**
 *
 * @author Yeo Quan Yang
 * @MatricNo A0111889W
 * 
 */

public class Settings {

    public static final String EVENT_LOG_FILENAME = "eventlogs.txt";
    public static final String DEFAULT_FILTER = "";
    public static final char CATEGORY_STRING = '@';
    public static final char CONTEXT_STRING = '#';
    public static final int EXIT_APPLICATION_NO = 0;
    public static final String CATEGORY = "@";
    public static final String CONTEXT = "#";
    public enum CommandType {
    	ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, UNDONE, LOGIN, INVALID, EXIT
    }
    public enum StatusType {
    	ERROR, MESSAGE
    }

}

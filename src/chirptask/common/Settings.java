package chirptask.common;

import org.jnativehook.keyboard.NativeKeyEvent;

/**
 *
 * @author Yeo Quan Yang
 * @MatricNo A0111889W
 * 
 */

public class Settings {

    public static final String EVENT_LOG_FILENAME = "eventlogs.txt";
    public static final String DEFAULT_FILTER = "";
    public static final char CATEGORY_CHAR = '@';
    public static final char CONTEXT_CHAR = '#';
    public static final int EXIT_APPLICATION_NO = 0;
    public static final String CATEGORY = "@";
    public static final String CONTEXT = "#";

    public static final int HOTKEY_TOGGLE_HIDE = NativeKeyEvent.VC_ESCAPE;
    public static final int HOTKEY_TOGGLE_SHOW = NativeKeyEvent.VC_G;

    public enum CommandType {
        ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, UNDONE, LOGIN, INVALID, EXIT, CLEAR
    }

    public enum StatusType {
        ERROR, MESSAGE
    }

}

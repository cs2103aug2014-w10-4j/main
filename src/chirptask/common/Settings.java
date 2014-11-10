//@author A0111889W
package chirptask.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.jnativehook.keyboard.NativeKeyEvent;

import chirptask.storage.StorageHandler;

public class Settings {

    /*
     * These will act as default values in the event that settings does not get
     * initialized properly.
     */
    public static String EVENT_LOG_FILENAME = "eventlogs.txt";
    public static String DEFAULT_FILTER = "";
    public static String GOOGLE_CALENDAR_ID = "";

    public static boolean LOGIN_AUTO = false;
    public static int SYSTEM_EXIT_NORMAL = 0;

    public static int HOTKEY_TOGGLE_HIDE = NativeKeyEvent.VC_ESCAPE;
    public static int HOTKEY_TOGGLE_SHOW = NativeKeyEvent.VC_G;

    public enum CommandType {
        ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, UNDONE, LOGIN, INVALID, EXIT, CLEAR, SYNC, LOGOUT
    }

    public enum StatusType {
        ERROR, MESSAGE
    }

    private static final String propertiesFile = "config.properties";
    private static final File configFile = new File(propertiesFile);
    private static final Properties props = new Properties();
    public static boolean hasRead = false;

    // Initialized once by MainGui Class
    public Settings() {
        if (configFile.exists()) {
            openFileForReading();
        } else {
            openFileForWriting();
        }
    }

    private void openFileForWriting() {
        try {
            FileWriter writer = new FileWriter(configFile);
            setDefaultValuesIntoProperty();
            // this writes the settings into the file.
            props.store(writer, "Default Settings");
            writer.close();
        } catch (IOException e) {
            StorageHandler.logError(String.format(Constants.ERROR, "Settings",
                    "while writing to file.\n" + e.getMessage()));
        }
    }

    private void setDefaultValuesIntoProperty() {
        // write default values
        props.setProperty("EVENT_LOG_FILENAME", "eventlogs.txt");
        props.setProperty("DEFAULT_FILTER", "");
        props.setProperty("LOGIN_AUTO", "false");
        props.setProperty("SYSTEM_EXIT_NORMAL", "0");
        props.setProperty("HOTKEY_TOGGLE_HIDE", "" + NativeKeyEvent.VC_ESCAPE);
        props.setProperty("HOTKEY_TOGGLE_SHOW", "" + NativeKeyEvent.VC_G);
        props.setProperty("GOOGLE_CALENDAR_ID", "");
    }

    private void openFileForReading() {
        FileReader reader;
        try {
            reader = new FileReader(configFile);
            // loads property from file
            props.load(reader);
            readSettingsFromProperty();
            hasRead = true;

            reader.close();
        } catch (FileNotFoundException ex) {
            openFileForWriting();
        } catch (NullPointerException NPE) {
            openFileForWriting();
        } catch (IndexOutOfBoundsException OOB) {
            // corrupted settings
            openFileForWriting();
        } catch (NumberFormatException NFE) {
            // corrupted settings
            openFileForWriting();
        } catch (IOException ex) {
            StorageHandler.logError(String.format(Constants.ERROR, "Settings",
                    "while reading from file.\n" + ex.getMessage()));
        }
    }

    private void readSettingsFromProperty() {
        if (props.getProperty("EVENT_LOG_FILENAME") == null
                || props.getProperty("DEFAULT_FILTER") == null
                || props.getProperty("LOGIN_AUTO") == null
                || props.getProperty("SYSTEM_EXIT_NORMAL") == null
                || props.getProperty("HOTKEY_TOGGLE_HIDE") == null
                || props.getProperty("HOTKEY_TOGGLE_SHOW") == null
                || props.getProperty("GOOGLE_CALENDAR_ID") == null) {
            openFileForWriting();
        }
        
        EVENT_LOG_FILENAME = props.getProperty("EVENT_LOG_FILENAME");
        DEFAULT_FILTER = props.getProperty("DEFAULT_FILTER");
        LOGIN_AUTO = Boolean.parseBoolean(props.getProperty("LOGIN_AUTO"));
        SYSTEM_EXIT_NORMAL = Integer.parseInt(props
                .getProperty("SYSTEM_EXIT_NORMAL"));
        HOTKEY_TOGGLE_HIDE = Integer.parseInt(props
                .getProperty("HOTKEY_TOGGLE_HIDE"));
        HOTKEY_TOGGLE_SHOW = Integer.parseInt(props
                .getProperty("HOTKEY_TOGGLE_SHOW"));
        GOOGLE_CALENDAR_ID = props.getProperty("GOOGLE_CALENDAR_ID");
    }

    public static void writeGoogleCalendarId(String googleId) {
        try {
            if (props != null && googleId != null) {
                FileWriter writer = new FileWriter(configFile);
                props.setProperty("GOOGLE_CALENDAR_ID", googleId);
                props.store(writer, "Default Settings");
                writer.close();
            }
        } catch (IOException e) {
            StorageHandler.logError(String.format(Constants.ERROR, "Settings",
                    "while writing to file.\n" + e.getMessage()));
        }
    }
}

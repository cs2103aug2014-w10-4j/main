package chirptask.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.jnativehook.keyboard.NativeKeyEvent;

import chirptask.storage.StorageHandler;

//@author A0111889W
public class Settings {

    public static String EVENT_LOG_FILENAME;
    public static String DEFAULT_FILTER;
    public static char CATEGORY_CHAR;
    public static char CONTEXT_CHAR;
    public static int SYSTEM_EXIT_NORMAL;

    public static String CATEGORY = CATEGORY_CHAR + "";
    public static String CONTEXT = CONTEXT_CHAR + "";

    public static int HOTKEY_TOGGLE_HIDE;
    public static int HOTKEY_TOGGLE_SHOW;

    public enum CommandType {
        ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, UNDONE, LOGIN, INVALID, EXIT, CLEAR
    }

    public enum StatusType {
        ERROR, MESSAGE
    }

    private static final String propertiesFile = "config.properties";
    private static final File configFile = new File(propertiesFile);
    private static final Properties props = new Properties();

    // Initialized at the start by logic
    public Settings() {
        if (configFile.exists()) {
            readPropertiesFromFile();
        } else {
            writeDefaultPropertiesToFile();
        }
    }

    private void writeDefaultPropertiesToFile() {
        try {
            FileWriter writer = new FileWriter(configFile);

            // write default values
            props.setProperty("EVENT_LOG_FILENAME", "eventlogs.txt");
            props.setProperty("DEFAULT_FILTER", "");
            props.setProperty("CATEGORY_CHAR", "@");
            props.setProperty("CONTEXT_CHAR", "#");
            props.setProperty("SYSTEM_EXIT_NORMAL", "0");
            props.setProperty("HOTKEY_TOGGLE_HIDE", ""
                    + NativeKeyEvent.VC_ESCAPE);
            props.setProperty("HOTKEY_TOGGLE_SHOW", "" + NativeKeyEvent.VC_G);

            props.store(writer, "Default Settings");
            writer.close();
        } catch (IOException e) {
            StorageHandler.logError(String.format(Messages.ERROR, "Settings",
                    "while writing to file.\n" + e.getMessage()));
        }
    }

    public void readPropertiesFromFile() {

        try {
            FileReader reader = new FileReader(configFile);
            props.load(reader);

            // read settings into variables
            EVENT_LOG_FILENAME = props.getProperty("EVENT_LOG_FILENAME",
                    "eventlogs.txt");
            DEFAULT_FILTER = props.getProperty("DEFAULT_FILTER", "");
            CATEGORY_CHAR = props.getProperty("CATEGORY_CHAR", "@").charAt(0);
            CONTEXT_CHAR = props.getProperty("CONTEXT_CHAR", "#").charAt(0);
            SYSTEM_EXIT_NORMAL = Integer.parseInt(props.getProperty(
                    "SYSTEM_EXIT_NORMAL", "0"));
            HOTKEY_TOGGLE_HIDE = Integer.parseInt(props.getProperty(
                    "HOTKEY_TOGGLE_HIDE", "" + NativeKeyEvent.VC_ESCAPE));
            HOTKEY_TOGGLE_SHOW = Integer.parseInt(props.getProperty(
                    "HOTKEY_TOGGLE_SHOW", "" + NativeKeyEvent.VC_G));

            reader.close();
        } catch (FileNotFoundException ex) {
            writeDefaultPropertiesToFile();
        } catch (IOException ex) {
            StorageHandler.logError(String.format(Messages.ERROR, "Settings",
                    "while reading from file.\n" + ex.getMessage()));
        }
    }
}

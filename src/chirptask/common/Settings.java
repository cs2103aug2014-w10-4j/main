package chirptask.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.jnativehook.keyboard.NativeKeyEvent;

import chirptask.storage.StorageHandler;

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

    private static final String propertiesFile = "config.properties";
    private static final File configFile = new File(propertiesFile);
    private static final Properties props = new Properties();

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
            reader.close();
        } catch (FileNotFoundException ex) {
            writeDefaultPropertiesToFile();
        } catch (IOException ex) {
            StorageHandler.logError(String.format(Messages.ERROR, "Settings",
                    "while reading from file.\n" + ex.getMessage()));
        }
    }
}

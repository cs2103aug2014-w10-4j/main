package chirptask.storage;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import chirptask.common.Messages;
import chirptask.common.Settings;

//@author A0111889W
public class EventLogger implements IStorage {

    private static PrintStream fileWriter;
    private static EventLogger instance = null;

    private EventLogger() {
        try {
            fileWriter = new PrintStream(new BufferedOutputStream(
                    new FileOutputStream(Settings.EVENT_LOG_FILENAME, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a printstream to the fileWriter variable, allowing printing of logs
     * to other output streams like system.out
     * 
     * @param output
     */
    public static void setStream(PrintStream output) {
        fileWriter = output;
    }

    public static EventLogger getInstance() {
        if (instance == null) {
            instance = new EventLogger();
        }
        return instance;
    }

    @Override
    public void close() {
        fileWriter.close();
    }

    @Override
    public boolean storeNewTask(Task T) {
        try {
            fileWriter.println(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                    new Date(), T.getDate().getTime(), T.getTaskId(),
                    T.getDescription()));
            fileWriter.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Task removeTask(Task T) {
        try {
            fileWriter.println(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                    new Date(), T.getDate().getTime(), T.getTaskId(),
                    T.getDescription()));
            fileWriter.flush();
            return T;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean modifyTask(Task T) {
        try {
            fileWriter.println(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                    new Date(), T.getDate().getTime(), T.getTaskId(),
                    T.getDescription()));
            fileWriter.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Task getTask(int taskId) {
        fileWriter.println(String.format(Messages.LOG_MESSAGE_GET_TASK,
                new Date(), taskId));
        fileWriter.flush();
        return null;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        fileWriter.println(String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                new Date()));
        fileWriter.flush();
        return null;
    }

    public void logError(String error) {
        fileWriter.println(String.format(Messages.ERROR, new Date(), error));
        fileWriter.flush();
    }

}
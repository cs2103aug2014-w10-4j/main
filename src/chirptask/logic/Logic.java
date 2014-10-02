package chirptask.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import chirptask.storage.Storage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;
import chirptask.google.GoogleController;
import chirptask.gui.*;

enum CommandType {
    ADD, DISPLAY, DELETE, EDIT, UNDO, DONE, LOGIN, INVALID, EXIT
}

enum StatusType {
    ERROR, MESSAGE
}

public class Logic {
    private static final String MESSAGE_NEW_COMMAND = "command: ";
    private static final int ERROR_OPENING_STREAM = 57;

    private Action _lastAction;
    private InputParser _parser;
    private StorageHandler _storageHandler;
    private MainGui _gui;

    // For working ChirpTask
    private BufferedReader commandBufferReader = new BufferedReader(
            new InputStreamReader(System.in));

    public Logic(MainGui gui) {

        _storageHandler = new StorageHandler();
        _parser = new InputParser();
        _gui = gui;

        DisplayView.updateTaskView(_gui);
    }

    public Logic() {

        _storageHandler = new StorageHandler();
        _parser = new InputParser();
        // runUntilExitCommand(); //Temporary CLI code before full integration
        // with GUI
        // lastAction = new Action();

    }

    /**
     * Temporary CLI code before full integration with GUI
     */
    private void runUntilExitCommand() {
        while (true) {
            issueNewCommandStatement();
            String userInput = waitForUserCommand();
            retrieveInputFromUI(userInput);
        }
    }

    private void issueNewCommandStatement() {
        displayToUser(MESSAGE_NEW_COMMAND);
    }

    private String waitForUserCommand() {
        String userCommand = getUserCommand();
        return userCommand;
    }

    private void displayToUser(String messageToDisplay) {
        System.out.print(messageToDisplay);
    }

    private String getUserCommand() {
        try {
            String userInputCommand = commandBufferReader.readLine();
            return userInputCommand;
        } catch (IOException ioAccessError) {
            exitChirpTask(ERROR_OPENING_STREAM);
        }
        return "INVALID";
    }

    /**
     * End temporary code
     */

    private void exitChirpTask(int typeOfExit) {
        System.exit(typeOfExit);
    }

    public void retrieveInputFromUI(String input) {
        _parser.receiveInput(input);
        processGroupAction(_parser.getActions().getActionList());
    }

    public void processGroupAction(List<Action> list) {
        for (Action a : list) {
            executeAction(a);
        }
    }

    private CommandType determineCommandType(String commandTypeString) {
        if (commandTypeString.equalsIgnoreCase("add")) {
            return CommandType.ADD;
        } else if (commandTypeString.equalsIgnoreCase("display")) {
            return CommandType.DISPLAY;
        } else if (commandTypeString.equalsIgnoreCase("delete")) {
            return CommandType.DELETE;
        } else if (commandTypeString.equalsIgnoreCase("edit")) {
            return CommandType.EDIT;
        } else if (commandTypeString.equalsIgnoreCase("undo")) {
            return CommandType.UNDO;
        } else if (commandTypeString.equalsIgnoreCase("done")) {
            return CommandType.DONE;
        } else if (commandTypeString.equalsIgnoreCase("login")) {
            return CommandType.LOGIN;
        } else if (commandTypeString.equalsIgnoreCase("exit")) {
            return CommandType.EXIT;
        } else {
            return CommandType.INVALID;
        }
    }

    // Will take in Action object
    public void executeAction(Action command) {
        String action = command.getCommandType();
        CommandType actionType = determineCommandType(action);
        Task task = command.getTask();
        switch (actionType) {
            case ADD :
                _storageHandler.addTask(task);
                this.setLastAction(command);
                FilterTasks.filter();
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case DELETE :
                _storageHandler.deleteTask(task);
                this.setLastAction(command);
                FilterTasks.filter();
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case DISPLAY :
                // now can only filter string
                FilterTasks.filter(task);
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case EDIT :
                _storageHandler.modifyTask(task);
                this.setLastAction(command);
                FilterTasks.filter();
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case UNDO :
                // negate action and run excecuteAction again
                executeAction(command.undo(this.getLastAction()));
                FilterTasks.filter();
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case DONE :
                task.setDone(true);
                _storageHandler.modifyTask(task);
                this.setLastAction(command);
                FilterTasks.filter();
                DisplayView.updateTaskView(FilterTasks.getFilteredList(), _gui);
                break;
            case LOGIN :
                _storageHandler.initCloudStorage();
                break;
            case EXIT :
                System.exit(0);
                break;
            case INVALID :
                // call print some invalid message
                break;
            default:
                // throw error
        }
    }

    public Action getLastAction() {
        return _lastAction;
    }

    public void setLastAction(Action lastAction) {
        this._lastAction = lastAction;
    }

}

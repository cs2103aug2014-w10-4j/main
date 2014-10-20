package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;


class ConcurrentModify implements Callable<Boolean> {

    private chirptask.storage.Task _taskToModify;

    ConcurrentModify(chirptask.storage.Task taskToModify) {
        if (ConcurrentHandler.isNull(taskToModify)) {
            _taskToModify = null;
        } else {
            _taskToModify = taskToModify;
        }
    }

    public Boolean call() throws UnknownHostException, IOException {
        Boolean isModified = true;

        if (ConcurrentHandler.isNull(_taskToModify)) {
            isModified = false;
            return isModified;
        }

        while (GoogleController.isGoogleLoaded() == false) {
            // wait until google is loaded in background
        }

        String taskType = _taskToModify.getType(); // Will have implications,
                                                   // further discussions
                                                   // needed.

        switch (taskType) { // Currently, floating and deadline uses same API.
        case "floating":
        case "deadline":
            isModified = isModified && 
                            ConcurrentHandler.modifyGoogleTask(_taskToModify);
            break;
        case "timedtask":
             isModified = isModified &&
                            ConcurrentHandler.modifyGoogleEvent(_taskToModify);
            break;
        default:
            break;
        }

        /*
         * Overwrites chirptask.storage.Task in the other storages
         */
        if (isModified) {
            _taskToModify.setModified(false); // Reset the isModified Flag to false
            ConcurrentHandler.modifyLocalStorage(_taskToModify);
        }

        return isModified;
    }
}
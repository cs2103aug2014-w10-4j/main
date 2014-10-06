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
            isModified = isModified
                    && ConcurrentHandler.modifyGoogleTasks(_taskToModify);
            break;
        case "timed":
            // isModified = isModified &&
            // ConcurrentHandler.modifyGoogleEvent(_taskToModify);
            break;
        default:
            break;
        }

        /*
         * Code below is possibly used to overwrite googleId in local storage,
         * eg. change type from floating to timed.
         */
        if (isModified) {
            // TODO if implement modification of task type.
        }

        return isModified;
    }
}
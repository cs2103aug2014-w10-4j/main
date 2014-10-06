package chirptask.logic;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import chirptask.gui.MainGui;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;

public class DisplayView {
    /**
     * This will take in a filtered list and update the taskview, sort to
     * date/time, store
     * into Arraylist of TasksByDates of arraylist of tasks
     * 
     * @param _gui
     * */
    public static void updateTaskView(List<Task> tasks, MainGui gui) {
        // Should change .getAllTasks() to arraylist?
        // List<Task> allTasks = _storageHandler.getAllTasks();
        Collections.sort(tasks);
        TreeMap<String, TasksByDate> map = new TreeMap<String, TasksByDate>();

        for (Task task : tasks) {
            String currDate = MainGui.convertDateToString(task.getDate());

            if (map.containsKey(currDate)) {
                map.get(currDate).addToTaskList(task);
            } else {
                  TasksByDate dateTask = new TasksByDate();
              //  dateTask.setTaskDate(task.getDate());
              //  dateTask.addToTaskList(task);
                gui.addNewTaskViewDate(task.getDate());
                map.put(currDate, dateTask);
            }
            gui.addNewTaskViewToDate(task.getDate(), task.getTaskId(),
                    task.getDescription(), task.getDate().toString(),
                    task.isDone());

        }

        // Iterator<Map.Entry<Date, TasksByDate>> it =
        // map.entrySet().iterator();
        // TaskView view = new TaskView();
        // while (it.hasNext()) {
        // view.addToTaskView(it.next().getValue());
        // }

    }

    // Call this at init to show all tasks.
    public static void updateTaskView(MainGui gui) {

        List<Task> allTasks = StorageHandler.getAllTasks();
        if (allTasks != null) {
            //allTasks is emptyt
            Collections.sort(allTasks);
            //System.out.println(allTasks);
            // call filter
            updateTaskView(allTasks, gui);
        }

    }

    // Take in type, action
    public static void showStatusToUser(StatusType type, Action action, MainGui gui) {
        if (type == StatusType.ERROR) {
            // message processing and call GUI api
        	gui.setError("Error in " + action.getCommandType());
            //action.getCommandType();
            //action.getTask().getDescription();
            //action.getTask().getDate().toString();

        } else {
            // message processing and call GUI api
        	gui.setStatus("Success in "+ action.getCommandType());
        }
    }
    // Add in checkTaskType
}

package chirptask.google;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.Tasks;
import com.google.api.services.tasks.model.TaskLists;

public class TasksViewer {

    static void header(String name) {
        System.out.println();
        System.out.println("============== " + name + " ==============");
        System.out.println();
    }
    
    static void display(TaskLists feed) {
        if (feed.getItems() != null) {
            for (TaskList entry : feed.getItems()) {
                System.out.println();
                System.out
                        .println("------------------------------------------");
                display(entry);
            }
        }
    }
    
    static void display(TaskList entry) {
        System.out.println("ID: " + entry.getId());
        System.out.println("Task Title: " + entry.getTitle());
    }

    static void display(Tasks event) {
        if (event.getItems() != null) {
            for (Task entry : event.getItems()) {
                System.out.println();
                System.out
                        .println("------------------------------------------");
                display(entry);
            }
        }
    }

    static void display(Task entry) {
        System.out.println("ID: " + entry.getId());
        System.out.println("Title: " + entry.getTitle());
        if (entry.getNotes() != null) {
            System.out.println("Notes: " + entry.getNotes());
        }
    }
    
    static void displayTitle(TaskList taskList) {
        System.out.println("Task Title: " + taskList.getTitle());
    }
    

    static String retrieveTaskListId(TaskList taskList) {
        String _taskId = taskList.getId();
        return _taskId;
    }

    static String retrieveTaskListTitle(TaskList taskList) {
        String _taskTitle = taskList.getTitle();
        return _taskTitle;
    }

    static DateTime retrieveLastModifiedTime(TaskList taskList) {
        DateTime _lastModifiedTimestamp = taskList.getUpdated();
        return _lastModifiedTimestamp;
    }

}

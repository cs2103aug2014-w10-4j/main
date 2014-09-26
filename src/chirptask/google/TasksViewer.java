package chirptask.google;

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

}

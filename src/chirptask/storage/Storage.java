package chirptask.storage;

import java.util.List;

public interface Storage {
    //@author A0111889W
    boolean storeNewTask(Task T);
    
    Task removeTask(Task T);

    boolean modifyTask(Task T);

    Task getTask(int taskId);

    // Task array or other data structure(?)
    List<Task> getAllTasks();

    void close();

}

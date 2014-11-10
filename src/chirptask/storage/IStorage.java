//@author A0111889W
package chirptask.storage;

import java.util.List;

/**
 * Storage interface provides default methods for storage type classes.
 * Methods: storeNewTask, removeTask, modifyTask, getTask, getAllTasks, close.
 * 
 * 
 *
 */
public interface IStorage {

    /**
     * Stores a new task into the storage.
     * 
     * @param T Task to store into storage
     * @return boolean Status of operation
     */
    boolean storeNewTask(Task T);

    /**
     * Removes existing task from storage. Returns the task removed, null if
     * don't exist.
     * 
     * @param T Task to remove from storage
     * @return Task Task that was removed. Null if doesn't exist.
     */
    Task removeTask(Task T);

    /**
     * Modifies an existing task in storage.
     * 
     * @param T Task to modify in storage
     * @return boolean status of operation.
     */
    boolean modifyTask(Task T);

    /**
     * Gets an existing task from storage based on taskId.
     * 
     * @param taskId taskId of task to get from storage
     * @return Task task with the given taskId
     */
    Task getTask(int taskId);

    /**
     * Gets all task from storage.
     * 
     * @return a list containing all tasks
     */
    List<Task> getAllTasks();

    /**
     * Close and flush storage.
     */
    void close();

}

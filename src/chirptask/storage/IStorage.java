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
     * @param T
     * @return
     */
    boolean storeNewTask(Task T);

    /**
     * Removes existing task from storage. Returns the task removed, null if
     * don't exist.
     * 
     * @param T
     * @return Task
     */
    Task removeTask(Task T);

    /**
     * Modifies an existing task in storage.
     * 
     * @param T
     * @return boolean status of operation.
     */
    boolean modifyTask(Task T);

    /**
     * Gets an existing task from storage based on taskId.
     * 
     * @param taskId
     * @return Task
     */
    Task getTask(int taskId);

    /**
     * Gets all task from storage.
     * 
     * @return List%3CTask%3E
     */
    List<Task> getAllTasks();

    /**
     * Close and flush storage.
     */
    void close();

}

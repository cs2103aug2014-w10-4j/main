//@author A0111840W
package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chirptask.common.Settings;
import chirptask.google.GoogleController;
import chirptask.logic.Logic;
import chirptask.storage.LocalStorage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;
import chirptask.storage.DeadlineTask;
import chirptask.storage.TimedTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Warning: JUnitGoogleAtd takes a while to load completely
 * There may be times where it will fail totally as it depends on 
 * your internet connection stability.
 * 
 * Do take the factor of how slow/unstable the internet may be if
 * the JUnit Test failed.
 * 
 * The Thread.sleep(10000) were added in their positions because
 * there is no easy way to tell when the Google requests are completed.
 * This is because they are running in the background threads.
 *
 */
public class JUnitGoogleAtd {
    private Settings chirptaskSettings;
    private StorageHandler storageHandler;
    private List<Task> allTasks;
    private MainGui2 gui;
    private Logic logic;

    @Before
    public void setupGoogle() {
        gui = new MainGui2();
        logic = new Logic(gui);
        chirptaskSettings = new Settings();
        storageHandler = new StorageHandler();
        allTasks = new ArrayList<Task>();

        logic.useTestLocalStorage();
        StorageHandler.initCloudStorage();

        while (GoogleController.isGoogleLoaded() == false) {
            sleep();
        }
        //At this point, the JUnitTest XML file should have all your synced
        // items from Google
        
        //Setup floating task
        Task floatingTask = new Task(LocalStorage.generateId(), "Task Floating - Google Test");
        allTasks.add(floatingTask);
        
        //Setup deadline task
        Long millisA = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        Calendar dueDate = Calendar.getInstance(); //Assume local time is SGT TimeZone
        dueDate.setTimeInMillis(millisA);
        Task deadlineTask = new DeadlineTask(LocalStorage.generateId(), "Task Deadline - Google Test", dueDate);
        allTasks.add(deadlineTask);
        
        //Setup timed task
        Long millisB = 1412763010000L; //Wed Oct 08 18:10:10 SGT 2014 in Epoch Millis
        Long millisB2 = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        Calendar startTime = Calendar.getInstance(); //Assume local time is SGT TimeZone
        startTime.setTimeInMillis(millisB);
        Calendar endTime = Calendar.getInstance(); //Assume local time is SGT TimeZone
        endTime.setTimeInMillis(millisB2);
        Task timedTask = new TimedTask(LocalStorage.generateId(), "Task Timed - Google Test", startTime, endTime);
        allTasks.add(timedTask);
        
        //Restart the JUnitTest XML file
        storageHandler.setUpJUnitTestXmlWriter();
    }
    
    @Test
    public void testGoogleAddAndDelete() {
        //Adding all tasks
        for (int i = 0; i < allTasks.size(); i++) {
            Task currentTask = allTasks.get(i);
            storageHandler.addTask(currentTask);
        }
        
        sleep();
        
        List<Task> localList = StorageHandler.getAllTasks();
        
        //All tasks got added
        assertEquals("same size", allTasks.size(), localList.size());
        
        for (int i = 0; i < localList.size(); i++) {
            Task currentTask = localList.get(i);
            assertNotNull(currentTask);
            assertNotNull(currentTask.getGoogleId());
            assertNotNull(currentTask.getETag());
            assertNotEquals("empty", "", currentTask.getGoogleId());
            assertNotEquals("empty", "", currentTask.getETag());
        }
        
        // Deletion of all tasks
        localList = StorageHandler.getAllTasks();
        List<Task> allTasks = new ArrayList<Task>();
        
        for (Task task : localList) {
            allTasks.add(task);
        }
        
        for (int i = 0; i < allTasks.size(); i++) {
            Task currentTask = allTasks.get(i);
            storageHandler.deleteTask(currentTask);
        }

        sleep();
        
        //All tasks got deleted
        assertEquals("empty list", 0, localList.size());
    }
    
    @After
    public void closeGoogle() {
        storageHandler.closeStorages();
    }
    
    public void sleep() {
        try {
            Thread.sleep(10000); //Wait for request to be done
        } catch (InterruptedException e) {
        }
    }
}

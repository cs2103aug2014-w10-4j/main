package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.LocalStorage;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitStorage {

    //@author A0111840W
    /**
     * This tests the local storage for the unforeseen circumstances 
     * if the program allows values out of boundary.
     * It also tests the correctness of the values when storing and
     * reading the LocalStorage.
     */
	@Test
	public void testLocalStorage() {
        LocalStorage local = new LocalStorage();
        local.setUpJUnitTestXmlWriter(); //Start test storage from fresh
        
	    int taskIdA = 1; //1
        int taskIdB = LocalStorage.generateId(); //2
        int taskIdC = LocalStorage.generateId(); //3
        Long millisB1 = 1412763010000L; //Wed Oct 08 18:10:10 SGT 2014 in Epoch Millis
        Long millisB2 = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        Long millisC = 1413108610000L; //Sun Oct 12 18:10:10 SGT 2014 in Epoch Millis
        String taskA = "Task A";
        String taskB = "Task B";
        String taskC = "Task C";

        Calendar calB1 = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB1.setTimeInMillis(millisB1);
        Calendar calB2 = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB2.setTimeInMillis(millisB2);
        Calendar calC = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calC.setTimeInMillis(millisC);
        
        Task floatingTask = new Task(taskIdA, taskA);
        Task timedTask = new TimedTask(taskIdB, taskB, calB1, calB2);
        Task deadlineTask = new DeadlineTask(taskIdC, taskC, calC);
	    
		
		//true if the task has been successfully stored
		assertTrue(local.storeNewTask(floatingTask));
		assertTrue(local.storeNewTask(timedTask));
		assertTrue(local.storeNewTask(deadlineTask));


        /* This JUnit Test presents 3 partitions. */
		//There is only Task ID 1-3 in storage
		//4 and -1 should fail and return null.
        assertEquals(floatingTask, local.getTask(taskIdA)); //Test value within range 1-3
        assertEquals(null, local.getTask(9999));  //Over the current limit value partition
        assertEquals(null, local.getTask(-1)); //Negative value partition
        
        assertEquals(timedTask, local.removeTask(timedTask));
        assertNull(local.getTask(timedTask.getTaskId()));

        Task task4 = new Task(-1, "");
		assertEquals(null, local.removeTask(task4)); //Task 4 not in storage, should return null
        assertFalse(local.modifyTask(task4)); //Task 4 not in storage, should be false

        /* This tests the correctness of the values being stored and read
         * from the persistent xml storage.
         */
		assertEquals(taskC, deadlineTask.getDescription());
		deadlineTask.setDescription("");
		assertTrue(local.modifyTask(deadlineTask));
		deadlineTask = local.getTask(deadlineTask.getTaskId());
        assertNotEquals(taskC, deadlineTask.getDescription());
        assertEquals("", deadlineTask.getDescription());
        
	}

}

package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitTask {

	//@author A0111840W
	@Test
	public void testSortTasks() {
	    int taskIdA = 1;
        int taskIdB = 2;
        int taskIdC = 3;
        int taskIdD = 4;
        Long millisB1 = 1412763010000L; //Wed Oct 08 18:10:10 SGT 2014 in Epoch Millis
        Long millisB2 = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        Long millisC = 1413108610000L; //Sun Oct 12 18:10:10 SGT 2014 in Epoch Millis
        String taskA = "Task A";
        String taskB = "Task B";
        String taskC = "Task C";
        String taskD = "Task D";

        Calendar calB1 = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB1.setTimeInMillis(millisB1);
        Calendar calB2 = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB2.setTimeInMillis(millisB2);
        Calendar calC = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calC.setTimeInMillis(millisC);
		
		List<Task> taskList = new ArrayList<Task>();
		Task floatingTask = new Task(taskIdA, taskA);
		Task timedTask = new TimedTask(taskIdB, taskB, calB1, calB2);
		Task deadlineTask = new DeadlineTask(taskIdC, taskC, calC);
		Task nextFloatingTask = new Task(taskIdD, taskD);

        taskList.add(nextFloatingTask);   //Task D
		taskList.add(floatingTask);       //Task A
        taskList.add(deadlineTask);       //Task C
		taskList.add(timedTask);          //Task B

		Collections.sort(taskList); //Deadline -> Timed -> Floating -> Alphabetical order 

		assertEquals("Task B", taskList.get(0).getDescription());
		assertEquals("Task C", taskList.get(1).getDescription());
		assertEquals("Task A", taskList.get(2).getDescription());
		assertEquals("Task D", taskList.get(3).getDescription());
	}

	@Test
	public void testDeadlineTasks() {
	    int taskIdA = 1;
	    int taskIdB = 2;
	    int taskIdC = 3;
	    Long millisA = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
	    Long millisB = 1412763010000L; //Wed Oct 08 18:10:10 SGT 2014 in Epoch Millis
	    Long millisC = 1413108610000L; //Sun Oct 12 18:10:10 SGT 2014 in Epoch Millis
	    String taskA = "Task A";
	    String taskB = "Task B";
	    String taskC = "Task C";
	    
	    Calendar calA = Calendar.getInstance(); //Assume local time is SGT TimeZone
	    calA.setTimeInMillis(millisA);
	    Calendar calB = Calendar.getInstance(); //Assume local time is SGT TimeZone
	    calB.setTimeInMillis(millisB);
        Calendar calC = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calC.setTimeInMillis(millisC);
        
        assertEquals("test calendar values", "Fri Oct 10 18:10:10 SGT 2014", calA.getTime().toString());
        assertEquals("test calendar values", "Wed Oct 08 18:10:10 SGT 2014", calB.getTime().toString());
        assertEquals("test calendar values", "Sun Oct 12 18:10:10 SGT 2014", calC.getTime().toString());
	    
	    DeadlineTask deadlineA = new DeadlineTask(taskIdA, taskA, calA);
	    DeadlineTask deadlineB = new DeadlineTask(taskIdB, taskB, calB);
        DeadlineTask deadlineC = new DeadlineTask(taskIdC, taskC, calC);
        
        assertNotNull("deadlineA is not null", deadlineA);
        assertNotNull("deadlineB is not null", deadlineB);
        assertNotNull("deadlineC is not null", deadlineC);
        
        //These are the Calendar objects that was entered as the parameter
        calA = deadlineA.getDate(); 
        calB = deadlineB.getDate();
        calC = deadlineC.getDate();
        
        assertEquals("test calendar values", "Fri Oct 10 18:10:10 SGT 2014", calA.getTime().toString());
        assertEquals("test calendar values", "Wed Oct 08 18:10:10 SGT 2014", calB.getTime().toString());
        assertEquals("test calendar values", "Sun Oct 12 18:10:10 SGT 2014", calC.getTime().toString());
        
        //These are the Calendar objects will be created by Task object (superclass)
        //These calendars should not be equal to the ones entered above
        //These calendars should be of the current instance time of the host machine
        calA = deadlineA.getDate();
        calB = deadlineB.getDate(); 
        calC = deadlineC.getDate(); 
        
        assertNotEquals("should not equal", "Fri Oct 10 18:10:10 SGT 2014", calA.getTime().toString());
        assertNotEquals("should not equal", "Wed Oct 08 18:10:10 SGT 2014", calB.getTime().toString());
        assertNotEquals("should not equal", "Sun Oct 12 18:10:10 SGT 2014", calC.getTime().toString());
	}
}

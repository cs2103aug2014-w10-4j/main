//@author A0111840W
package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitTaskAtd {
    
    private int taskIdA;
    private int taskIdB;
    private int taskIdC;
    private int taskIdD;
    private Calendar calA;
    private Calendar calB;
    private Calendar calB2;
    private Calendar calC;
    private List<Task> taskList;
    private Long millisA;
    private Long millisB;
    private Long millisB2;
    private Long millisC;
    private String taskA;
    private String taskB;
    private String taskC;
    private String taskD;
    private Task floatingTask;
    private Task timedTask;
    private Task deadlineTask;
    private Task nextFloatingTask;
    
    @Before
    public void setupTest() {
        taskIdA = 1;
        taskIdB = 2;
        taskIdC = 3;
        taskIdD = 4;
        millisA = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        millisB = 1412763010000L; //Wed Oct 08 18:10:10 SGT 2014 in Epoch Millis
        millisB2 = 1412935810000L; //Fri Oct 10 18:10:10 SGT 2014 in Epoch Millis
        millisC = 1413108610000L; //Sun Oct 12 18:10:10 SGT 2014 in Epoch Millis
        taskA = "Task A";
        taskB = "Task B";
        taskC = "Task C";
        taskD = "Task D";
        calA = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calA.setTimeInMillis(millisA);
        calB = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB.setTimeInMillis(millisB);
        calB2 = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calB2.setTimeInMillis(millisB2);
        calC = Calendar.getInstance(); //Assume local time is SGT TimeZone
        calC.setTimeInMillis(millisC);
        taskList = new ArrayList<Task>();
        floatingTask = new Task(taskIdA, taskA);
        timedTask = new TimedTask(taskIdB, taskB, calB, calB2);
        deadlineTask = new DeadlineTask(taskIdC, taskC, calC);
        nextFloatingTask = new Task(taskIdD, taskD);
    }

	@Test
	public void testSortTasks() {
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
	}
}

package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.common.Settings;
import chirptask.gui.MainGui;
import chirptask.logic.Action;
import chirptask.logic.DisplayView;
import chirptask.logic.FilterTasks;
import chirptask.logic.Logic;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;

import chirptask.storage.TimedTask;

import javafx.application.Application;
import javafx.stage.Stage;


public class JUnitLogic {
	
	@Test
	public void Displaytest() {
		// Testing display logic with tag /undone /floating
		//commend out the GUI portion for this to run.
		Logic a = new Logic(null);

		Action act = new Action();
		Task task = new Task();

		task.setTaskId(-1);
		task.setDescription("/undone /floating");
		act.setCommandType(Settings.CommandType.DISPLAY);
		act.setTask(task);
		act.setUndo(null);

		List<Task> list = FilterTasks.getFilteredList();

		a.executeAction(act);

		assertEquals(list, FilterTasks.getFilteredList());

	}

	@Test
	public void DisplayViewTest() {
		
		//Test method convertTaskDateToString
		
		//test floating task
		Task test1 = new Task();
		test1.setType("floating");
		assertEquals("all-day",DisplayView.convertTaskDateToString(test1));
		
		//test Deadline task
		Calendar date = Calendar.getInstance();
		date.set(2014, 9, 22);
		date.set(Calendar.HOUR_OF_DAY, 23);
		date.set(Calendar.MINUTE, 59);
		Task test2 = new DeadlineTask(1, "test", date);
		assertEquals("due by 23:59",DisplayView.convertTaskDateToString(test2));
		
		//test timedtask
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		startTime.set(2014, 9, 22, 12, 0);
		endTime.set(2014, 9, 22, 14, 0);
		Task timed = new TimedTask(2, "test2", startTime,
				endTime);
		assertEquals("12:00 to 14:00", DisplayView.convertTaskDateToString(timed));
		
		//Boundary -- test a type of task that is not the 3 type 
		//this should crash the program as this will never happen
		Task troll = new Task();
		troll.setType("troll");
		//DisplayView.convertTaskDateToString(troll);
		
		
	}
	@Test
	public void testFilterTasks(){
		//test Method hideDeleted
		
		//Create a list of all deleted task
		//hideDeleted should return a emptylist
		List<Task> list = new ArrayList<Task>();
		List<Task> expected = new ArrayList<Task>();
		Task A = new Task();
		Task B = new Task();
		Task C = new Task();
		A.setDeleted(true);
		B.setDeleted(true);
		C.setDeleted(true);
		
		list.add(A);
		list.add(B);
		list.add(C);
		
		assertEquals(expected, FilterTasks.hideDeleted(list));
		
		//Set B to be deleted
		//hideDeleted should return a list with Task B
		B.setDeleted(false);
		expected.add(B);
		assertEquals(expected, FilterTasks.hideDeleted(list));
		
		//setDeleted false for all Tasks
		//should return a list of all task
		A.setDeleted(false);
		C.setDeleted(false);
		expected.add(A);
		expected.add(B);
		assertEquals(expected, FilterTasks.hideDeleted(list));
		//End of test for hideDeleted
		
		//test for method processFilterDateParam
		
		//proper date format MM/DD
		String date1 = "10/22";
		Calendar testParam = Calendar.getInstance();
		testParam.set(testParam.get(Calendar.YEAR), 9, 22);
		Calendar expected1 = FilterTasks.processFilterDateParam(date1);
		
		assertEquals(testParam.get(Calendar.YEAR), expected1.get(Calendar.YEAR));
		assertEquals(testParam.get(Calendar.MONTH), expected1.get(Calendar.MONTH));
		assertEquals(testParam.get(Calendar.DAY_OF_MONTH), expected1.get(Calendar.DAY_OF_MONTH));
		
		//wrong date format MM-DD
		//Return a current Calendar object
		String date2 = "10-22";
		Calendar testParam1 = Calendar.getInstance();
		Calendar expected2 = FilterTasks.processFilterDateParam(date2);
		
		assertEquals(testParam1.get(Calendar.YEAR), expected2.get(Calendar.YEAR));
		assertEquals(testParam1.get(Calendar.MONTH), expected2.get(Calendar.MONTH));
		assertEquals(testParam1.get(Calendar.DAY_OF_MONTH), expected2.get(Calendar.DAY_OF_MONTH));
		//End of test for processFilterDateParam
	}

}

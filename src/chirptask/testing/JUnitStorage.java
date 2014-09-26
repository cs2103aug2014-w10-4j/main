package chirptask.testing;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import chirptask.storage.LocalStorage;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitStorage {

	@Test
	public void test() throws ParseException {
		LocalStorage local = new LocalStorage();
		Task task1 = new Task(1, "task 1");
		Task task2 = new Task(2, "task 2");
		task2.setContexts(new ArrayList<String>(Arrays.asList("context1", "context2")));
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		Task task3 = new TimedTask(3, "task3", df.parse("9/26/14 11:00"), df.parse("9/26/14 12:00"));
		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(task1);
		tasks.add(task2);
		tasks.add(task3);
		
		assertTrue(local.storeNewTask(task1));
		assertTrue(local.storeNewTask(task2));
		assertTrue(local.storeNewTask(task3));
		assertEquals(tasks, local.getAllTasks());

	}

}

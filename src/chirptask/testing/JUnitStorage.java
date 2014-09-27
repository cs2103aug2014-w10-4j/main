package chirptask.testing;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.LocalStorage;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitStorage {

	@Test
	public void test() throws ParseException {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		LocalStorage local = new LocalStorage();
		Task task1 = new Task(1, "task 1");
		Task task2 = new DeadlineTask(2, "task 2", df.parse("9/27/14"));
		task2.setContexts(new ArrayList<String>(Arrays.asList("context1", "context2")));
		Task task3 = new TimedTask(3, "task3", df.parse("9/26/14 11:00"),
				df.parse("9/26/14 12:00"));
		Task task4 = new Task(4, "task 4");
		Task task5 = new Task(3, "task 5");

		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(task1);
		tasks.add(task2);
		tasks.add(task3);
		
		assertTrue(local.storeNewTask(task1));
		assertTrue(local.storeNewTask(task2));
		assertTrue(local.storeNewTask(task3));
		assertEquals(tasks, local.getAllTasks());

		assertEquals(null, local.getTask(4));
		assertEquals(task2, local.getTask(2));

		assertEquals(null, local.removeTask(task4));
		assertEquals(task2, local.removeTask(task2));

		assertTrue(local.modifyTask(task5));
		local.addGoogleId(3, "6");

	}

}

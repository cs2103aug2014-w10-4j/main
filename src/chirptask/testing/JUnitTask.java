package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitTask {

	// @author A0111889W
	@Test
	public void test() throws ParseException {

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		ArrayList<Task> a = new ArrayList<Task>();

		a.add(new Task(1, "B"));
		a.add(new TimedTask(2, "B", df.parse("9/24/14"), new Date()));
		a.add(new Task(4, "A"));
		a.add(new DeadlineTask(3, "C", df.parse("9/25/14 13:00")));

		Collections.sort(a);

		assertEquals("B", a.get(0).getDescription());
		assertEquals("C", a.get(1).getDescription());
		assertEquals("A", a.get(2).getDescription());
		assertEquals("B", a.get(3).getDescription());

	}

}

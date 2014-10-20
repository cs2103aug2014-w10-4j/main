package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import chirptask.logic.Action;
import chirptask.logic.GroupAction;
import chirptask.logic.InputParser;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;

//@author A0113022
public class InputParserTest {
	InputParser parser = new InputParser();

	@Test
	public void testAdd() {
		parser.receiveInput("add task 1 @2103 @2101 #homework");
	}

	public boolean compareTask(Task task1, Task task2) {
		boolean isSameType = (task1.getType().equals(task2.getType()));
		boolean isSameDate = false;
		if (isSameType) {
			switch (task1.getType()) {
			case "deadline":
				isSameDate = (((DeadlineTask) task1).getDate().compareTo(
						((DeadlineTask) task2).getDate()) == 0);
			}
		}
		return false;
	}

	@Test
	public void testDelte() {
		assertTrue(false);
	}

	@Test
	public void test3() {
		assertTrue(true);
	}

}

package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.common.Settings;
import chirptask.logic.Action;
import chirptask.logic.GroupAction;
import chirptask.logic.InputParser;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;
import com.joestelmach.natty.CalendarSource;

//@author A0113022
public class JUnitInputParserAtd {

	InputParser parser = new InputParser();

	@Test
	// Partition: floating task with categories and contexts
	public void testAddFloating() {
		List<String> contexts = new ArrayList<String>();
		List<String> categories = new ArrayList<String>();
		List<String> empty = new ArrayList<String>();
		Task task;
		GroupAction group;

		parser.receiveInput("add task 1 @2103 @2101 #homework");
		contexts.add("homework");
		categories.add("2103");
		categories.add("2101");
		task = templateTaskFloating("task 1 @2103 @2101 #homework", categories,
				contexts);

		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("add @2103 @2101 #homework");
		task = templateTaskFloating("@2103 @2101 #homework", categories,
				contexts);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("add @2103 @2101");
		task = templateTaskFloating("@2103 @2101", categories, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("add watch edge of tomorrow");
		task = templateTaskFloating("watch edge of tomorrow", empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

	}

	@Test
	public void testAddInvalid() {
		parser.receiveInput("add");
		GroupAction group = templateGroupInvalid("add",
				Settings.CommandType.ADD);

		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: deadline task, no categories/contexts, relative date
	public void testAddDeadline() {
		List<String> contexts = new ArrayList<String>();
		List<String> categories = new ArrayList<String>();
		List<String> empty = new ArrayList<String>();
		DeadlineTask task;
		GroupAction group;
		Calendar deadline = Calendar.getInstance();
		deadline.set(2014, 10, 4, 10, 0);
		CalendarSource.setBaseDate(deadline.getTime());

		parser.receiveInput("addd finish this by today");
		deadline.set(Calendar.HOUR_OF_DAY, 23);
		deadline.set(Calendar.MINUTE, 59);
		task = templateTaskDeadline("finish this by 23:59 04/11", deadline,
				empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addd finish this on today");
		task = templateTaskDeadline("finish this by 23:59 04/11", deadline,
				empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addd finish this at today 11pm");
		deadline.set(Calendar.HOUR_OF_DAY, 23);
		deadline.set(Calendar.MINUTE, 00);
		task = templateTaskDeadline("finish this by 23:00 04/11", deadline,
				empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addd v0.2 by next week @2103 #homework");
		deadline.set(2014, 10, 11, 23, 59);
		categories.add("2103");
		contexts.add("homework");
		task = templateTaskDeadline("v0.2 @2103 #homework by 23:59 11/11", deadline,
				categories, contexts);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addd watch goodbye tomorrow on 23 oct");
		deadline.set(2014, 9, 23, 23, 59);
		task = templateTaskDeadline("watch goodbye tomorrow by 23:59 23/10",
				deadline, empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addd finish this at school tomorrow by today");
		deadline.set(2014, 10, 4, 23, 59);
		task = templateTaskDeadline(
				"finish this at school tomorrow by 23:59 04/11", deadline,
				empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: timed task, no categories/contexts, absolute date mm/dd
	// representation
	public void testAddTimed() {
		List<String> contexts = new ArrayList<String>();
		List<String> categories = new ArrayList<String>();
		List<String> empty = new ArrayList<String>();
		TimedTask task;
		GroupAction group;
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.set(2014, 10, 4, 10, 0);
		CalendarSource.setBaseDate(start.getTime());

		parser.receiveInput("addt from 2pm to 4pm 23/10");
		start.set(2014, 9, 23, 14, 00);
		end.set(2014, 9, 23, 16, 00);

		task = templateTaskTimed("from 2pm to 4pm 23/10", start, end, empty,
				empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());

		parser.receiveInput("addt attend talk from code to product "
				+ "from 3pm til 5pm @2103 @3204 01 nov");
		start.set(2014, 10, 1, 15, 00);
		end.set(2014, 10, 1, 17, 00);
		categories.add("2103");
		categories.add("3204");
		task = templateTaskTimed(
				"attend talk from code to product @2103 @3204", start, end,
				categories, contexts);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());
		
		parser.receiveInput("addt attend talk from code to product "
				+ "from 3pm -> 5pm today");
		start.set(2014, 10, 4, 15, 00);
		end.set(2014, 10, 4, 17, 00);
		task = templateTaskTimed(
				"attend talk from code to product", start, end,
				empty, empty);
		group = templateGroupAdd(task);
		compareGroup(group, parser.getActions());
	}


	/**
	 * @param task
	 * @return
	 */
	private GroupAction templateGroupAdd(Task task) {
		Action toAdd = new Action(Settings.CommandType.ADD, task);
		Action negate = new Action(Settings.CommandType.DELETE, task);
		toAdd.setUndo(negate);

		GroupAction group = new GroupAction();
		group.addAction(toAdd);
		return group;
	}

	private GroupAction templateGroupInvalid(String input,
			Settings.CommandType command) {
		Action invalid = new Action(Settings.CommandType.INVALID);
		invalid.setUndo(null);
		invalid.setInvalidCommandType(command);
		invalid.setUserInput(input);

		GroupAction group = new GroupAction();
		group.addAction(invalid);
		return group;
	}

	private DeadlineTask templateTaskDeadline(String desc, Calendar deadline,
			List<String> categories, List<String> contexts) {
		DeadlineTask task = new DeadlineTask(0, desc, deadline);

		task.setCategories(categories);
		task.setContexts(contexts);

		return task;
	}

	/**
	 * @return
	 */
	private Task templateTaskFloating(String desc, List<String> categories,
			List<String> contexts) {
		Task task = new Task(0, desc);

		task.setCategories(categories);
		task.setContexts(contexts);

		return task;
	}

	private TimedTask templateTaskTimed(String desc, Calendar start,
			Calendar end, List<String> categories, List<String> contexts) {
		TimedTask task = new TimedTask(0, desc, start, end);

		task.setCategories(categories);
		task.setContexts(contexts);

		return task;
	}

	private void compareGroup(GroupAction result, GroupAction expected) {
		assertEquals(result.getActionList().size(), expected.getActionList()
				.size());

		int size = result.getActionList().size();
		for (int i = 0; i < size; i++) {
			compareAction(result.getActionList().get(i), expected
					.getActionList().get(i));
		}
	}

	private void compareAction(Action result, Action expected) {
		if (expected == null) {
			assertNull(result);
		} else {
			assertEquals(result.getCommandType(), expected.getCommandType());
			compareTask(result.getTask(), expected.getTask());
			if (result.undo() == null) {
				assertEquals(expected.undo(), null);
			} else if (expected.undo() == null) {
				assertTrue(false);
			} else {
				assertEquals(result.undo().getCommandType(), expected.undo()
						.getCommandType());
				compareTask(result.undo().getTask(), expected.undo().getTask());
			}
		}
	}

	// ignore task Id
	private void compareTask(Task result, Task expected) {
		if (expected == null) {
			assertNull(result);
		} else {
			assertEquals(result.getType(), expected.getType());
			switch (result.getType()) {
			case Task.TASK_DEADLINE:
				compareTime(result.getDate(), expected.getDate());
				break;
			case Task.TASK_TIMED:
				compareTime(result.getDate(), expected.getDate());
				compareTime(((TimedTask) result).getEndTime(),
						((TimedTask) expected).getEndTime());
			default:
			}
			assertEquals(result.getDescription(), expected.getDescription());
			assertEquals(result.getContexts(), expected.getContexts());
			assertEquals(result.getCategories(), expected.getCategories());

		}

	}

	// @author A0113022
	private void compareTime(Calendar result, Calendar expected) {
		if (expected == null) {
			assertNull(result);
		} else {
			assertEquals(result.get(Calendar.YEAR), expected.get(Calendar.YEAR));
			assertEquals(result.get(Calendar.MONTH),
					expected.get(Calendar.MONTH));
			assertEquals(result.get(Calendar.DAY_OF_MONTH),
					expected.get(Calendar.DAY_OF_MONTH));
			assertEquals(result.get(Calendar.HOUR_OF_DAY),
					expected.get(Calendar.HOUR_OF_DAY));
			assertEquals(result.get(Calendar.MINUTE),
					expected.get(Calendar.MINUTE));
		}
	}

}

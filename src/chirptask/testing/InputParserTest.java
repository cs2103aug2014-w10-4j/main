package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import chirptask.common.Settings;
import chirptask.logic.Action;
import chirptask.logic.GroupAction;
import chirptask.logic.InputParser;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

//@author A0113022
public class InputParserTest {
	InputParser parser = new InputParser();

	@Test
	// Partition: floating task with categories and contexts
	public void testAdd() {
		parser.receiveInput("add task 1 @2103 @2101 #homework");

		Task toCompare = taskToCompareF();

		GroupAction group = groupActionAdd(toCompare);

		compareGroup(group, parser.getActions());
	}

	@Test
	// Boundary: floating task with only categories and contexts
	public void testAdd2() {
		parser.receiveInput("add @2103 @2101 #homework");
		Task toCompare = taskToCompareF();
		toCompare.setDescription("@2103 @2101 #homework");

		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: floating task with no categories and contexts
	public void testAdd3() {
		parser.receiveInput("add task 1");
		List<String> empty = new ArrayList<String>();
		Task toCompare = taskToCompareF();
		toCompare.setCategories(empty);
		toCompare.setContexts(empty);
		toCompare.setDescription("task 1");

		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());

	}

	@Test
	// Partition: deadline task, no categories/contexts, relative date
	public void testAddd() {
		parser.receiveInput("addd finish this by today");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		DeadlineTask toCompare = taskToCompareD("finish this by today", cal);

		GroupAction group = groupActionAdd(toCompare);

		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: deadline task, has categories/contexts, relative date
	public void testAddd2() {
		parser.receiveInput("addd v0.2 by next week @2103");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.add(Calendar.DAY_OF_MONTH, 7);
		DeadlineTask toCompare = taskToCompareD("v0.2 by next week @2103", cal);
		List<String> categories = new ArrayList<String>();
		categories.add("2103");
		toCompare.setCategories(categories);
		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: deadline task, no categories/contexts, absolute date
	public void testAddd3() {
		parser.receiveInput("addd watch goodbye tomorrow by 23 oct");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 9, 23, 23, 59);
		DeadlineTask toCompare = taskToCompareD(
				"watch goodbye tomorrow by 23 oct", cal);
		GroupAction group = groupActionAdd(toCompare);

		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: deadline task, no categories/contexts, absolute date mm/dd
	// representation
	// (plan to change to dd/mm representation)
	public void testAddd4() {
		parser.receiveInput("addd finish this by 23/10");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 9, 23, 23, 59);
		DeadlineTask toCompare = taskToCompareD("finish this by 23/10", cal);
		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());
	}

	@Test
	public void TestAddd5() {
		parser.receiveInput("addd finish this by 10/23");
		GroupAction group = groupActionInvalid("addd finish this by 10/23",
				Settings.CommandType.ADD);
		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: timed task, no categories/contexts, absolute date mm/dd
	// representation
	public void testAddt() {
		parser.receiveInput("addt from 2pm to 4pm 23/10");
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2014, 9, 23, 14, 00);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(2014, 9, 23, 16, 00);

		TimedTask toCompare = taskToCompareT("from 2pm to 4pm 23/10", cal1,
				cal2);
		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());
	}

	@Test
	// Partition: timed task, has categories/contexts, absolute date mm/dd
	// representation
	// has another pair from to with no date
	public void testAddt2() {
		parser.receiveInput("addt attend talk from code to product "
				+ "from 3pm to 5pm @2103 @3204 01 nov");
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2014, 10, 1, 15, 00);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(2014, 10, 1, 17, 00);
		List<String> categories = new ArrayList<String>();
		categories.add("2103");
		categories.add("3204");

		TimedTask toCompare = taskToCompareT(
				"attend talk from code to product "
						+ "from 3pm to 5pm @2103 @3204 01 nov", cal1, cal2);
		toCompare.setCategories(categories);
		GroupAction group = groupActionAdd(toCompare);
		compareGroup(group, parser.getActions());
	}
	@Test
	public void testAddt3() {
		parser.receiveInput("addt do this from 2pm to 4pm 10/23");
		GroupAction group = groupActionInvalid("addt do this from 2pm to 4pm 10/23",
				Settings.CommandType.ADD);
		compareGroup(group, parser.getActions());	
	}

	// ignore task Id for now
	private void compareTask(Task task1, Task task2) {
		if (task1 == null) {
			assertEquals(task2, null);
		} else if (task2 == null) {
			assertTrue(false);
		} else {
			assertEquals(task1.getType(), task2.getType());
			switch (task1.getType()) {
			case "deadline":
				compareTime(task1.getDate(), task2.getDate());
				break;
			case "timed task":
				compareTime(task1.getDate(), task2.getDate());
				compareTime(((TimedTask) task1).getEndTime(),
						((TimedTask) task2).getEndTime());
			default:
			}
			assertEquals(task1.getDescription(), task2.getDescription());
			assertEquals(task1.getContexts(), task2.getContexts());
			assertEquals(task1.getCategories(), task2.getCategories());
			// boolean isSameId = (task1.getTaskId() == task2.getTaskId());
		}

	}

	private void compareAction(Action act1, Action act2) {
		if (act1 == null) {
			assertEquals(act2, null);
		} else if (act2 == null) {
			assertTrue(false);
		} else {
			assertEquals(act1.getCommandType(), act2.getCommandType());
			compareTask(act1.getTask(), act2.getTask());
			if (act1.undo() == null) {
				assertEquals(act2.undo(), null);
			} else if (act2.undo() == null) {
				assertTrue(false);
			} else {
				assertEquals(act1.undo().getCommandType(), act2.undo()
						.getCommandType());
				compareTask(act1.undo().getTask(), act2
						.undo().getTask());
			}
		}
	}

	private void compareGroup(GroupAction gr1, GroupAction gr2) {
		assertEquals(gr1.getActionList().size(), gr2.getActionList().size());
		int size = gr1.getActionList().size();
		for (int i = 0; i < size; i++) {
			compareAction(gr1.getActionList().get(i),
					gr2.getActionList().get(i));

		}
	}

	/**
	 * @return
	 */
	private Task taskToCompareF() {
		Task toCompare = new Task();
		toCompare.setDescription("task 1 @2103 @2101 #homework");
		List<String> categories = new ArrayList<String>();
		categories.add("2103");
		categories.add("2101");
		List<String> contexts = new ArrayList<String>();
		contexts.add("homework");
		toCompare.setCategories(categories);
		toCompare.setContexts(contexts);
		return toCompare;
	}

	/**
	 * @param toCompare
	 * @return
	 */
	private GroupAction groupActionAdd(Task toCompare) {
		Action toAdd = new Action(Settings.CommandType.ADD, toCompare);
		Action negate = new Action(Settings.CommandType.DELETE, toCompare);
		toAdd.setUndo(negate);

		GroupAction group = new GroupAction();
		group.addAction(toAdd);
		return group;
	}

	private GroupAction groupActionInvalid(String input,
			Settings.CommandType command) {
		Action invalid = new Action(Settings.CommandType.INVALID);
		invalid.setUndo(null);
		invalid.setInvalidCommandType(command);
		invalid.setUserInput(input);
		GroupAction group = new GroupAction();
		group.addAction(invalid);
		return group;
	}

	private DeadlineTask taskToCompareD(String desc, Calendar cal) {
		DeadlineTask toCompare = new DeadlineTask(0, desc, cal);
		List<String> empty = new ArrayList<String>();
		toCompare.setCategories(empty);
		toCompare.setContexts(empty);
		return toCompare;
	}

	private TimedTask taskToCompareT(String desc, Calendar cal1, Calendar cal2) {
		TimedTask toCompare = new TimedTask(0, desc, cal1, cal2);
		List<String> empty = new ArrayList<String>();
		toCompare.setCategories(empty);
		toCompare.setContexts(empty);
		return toCompare;
	}

	// code reused from natty
	private void validateDateTime(Calendar cal, int year, int month, int date,
			int hour, int minute) {
		assertEquals(cal.get(Calendar.YEAR), year);
		assertEquals(cal.get(Calendar.MONTH), month);
		assertEquals(cal.get(Calendar.DAY_OF_MONTH), date);
		assertEquals(cal.get(Calendar.HOUR), hour);
		assertEquals(cal.get(Calendar.MINUTE), minute);
	}

	// @author A0113022
	private void compareTime(Calendar cal1, Calendar cal2) {
		if (cal1 == null) {
			assertEquals(cal2, null);
		} else if (cal2 == null) {
			assertTrue(false);
		} else {
			assertEquals(cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
			assertEquals(cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
			assertEquals(cal1.get(Calendar.DAY_OF_MONTH),
					cal2.get(Calendar.DAY_OF_MONTH));
			assertEquals(cal1.get(Calendar.HOUR_OF_DAY),
					cal2.get(Calendar.HOUR_OF_DAY));
			assertEquals(cal1.get(Calendar.MINUTE), cal2.get(Calendar.MINUTE));
		}
	}

}

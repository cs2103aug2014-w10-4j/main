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
	public void testAdd() {
		parser.receiveInput("add task 1 @2103 @2101 #homework");

		Task toCompare = taskToCompareF();

		GroupAction group = groupActionAdd(toCompare);

		assertTrue(compareGroup(group, parser.getActions()));
	}
	
	@Test 
	public void testAdd2() {
		parser.receiveInput("add @2103 @2101 #homework");
		Task toCompare = taskToCompareF();
		toCompare.setDescription("@2103 @2101 #homework");
		
		GroupAction group = groupActionAdd(toCompare);
		assertTrue(compareGroup(group, parser.getActions()));
	}

	@Test 
	public void testAdd3() {
		parser.receiveInput("add task 1");
		List<String> empty = new ArrayList<String>();
		Task toCompare = taskToCompareF();
		toCompare.setCategories(empty);
		toCompare.setContexts(empty);
		toCompare.setDescription("task 1");
		
		GroupAction group = groupActionAdd(toCompare);
		assertTrue(compareGroup(group, parser.getActions()));
		
	}
	
	@Test 
	public void testAddd() {
		parser.receiveInput("addd finish this by today");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		DeadlineTask toCompare = taskToCompareD("finish this by today", cal);
		
		GroupAction group = groupActionAdd(toCompare);

		assertTrue(compareGroup(group, parser.getActions()));
	}
	
	@Test 
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
		assertTrue(compareGroup(group, parser.getActions()));
	}
	
	@Test 
	public void testAddd3() {
		parser.receiveInput("addd watch goodbye tomorrow by 23 oct");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 9, 23, 23, 59);
		DeadlineTask toCompare = taskToCompareD("watch goodbye tomorrow by 23 oct", cal);
		GroupAction group = groupActionAdd(toCompare);
		
		assertTrue(compareGroup(group, parser.getActions()));
	}
	
	@Test
	public void testAddd4() {
		parser.receiveInput("addd finish this by 10/23");
		Calendar cal = Calendar.getInstance();
		cal.set(2014, 9, 23, 23, 59);
		DeadlineTask toCompare = taskToCompareD("finish this by 10/23", cal);
		GroupAction group = groupActionAdd(toCompare);
		assertTrue(compareGroup(group, parser.getActions()));
	}
	
	@Test
	public void testAddt() {
		parser.receiveInput("addt from 2pm to 4pm 10/23");
		Calendar cal1 = Calendar.getInstance();
		cal1.set(2014, 9, 23, 14, 00);
		Calendar cal2 = Calendar.getInstance();
		cal2.set(2014, 9, 23, 16, 00);
		
		TimedTask toCompare = taskToCompareT("from 2pm to 4pm 10/23", cal1, cal2);
		GroupAction group = groupActionAdd(toCompare);
		assertTrue(compareGroup(group, parser.getActions()));
	}

	@Test
	public void testDelete() {
//		assertTrue(false);
	}

	@Test
	public void test3() {
		assertTrue(true);
	}

	// ignore task Id for now
	private boolean compareTask(Task task1, Task task2) {
		boolean isSameType = (task1.getType().equals(task2.getType()));
		boolean isSameDate = false;
		if (isSameType) {
			switch (task1.getType()) {
			case "deadline":
				isSameDate = Math.abs((((DeadlineTask) task1).getDate().getTimeInMillis() - 
						((DeadlineTask) task2).getDate().getTimeInMillis())) < 60000;
				break;
			case "timed task":
				boolean isSameStart = Math.abs((((TimedTask) task1).getDate().getTimeInMillis() - 
						((TimedTask) task2).getDate().getTimeInMillis())) < 60000;
				boolean isSameEnd = Math.abs((((TimedTask) task1).getEndTime().getTimeInMillis() - 
						((TimedTask) task2).getEndTime().getTimeInMillis())) < 60000;
				isSameDate = isSameStart && isSameEnd;
			default:
				isSameDate = true;
			}
		}
		boolean isSameDesc = task1.getDescription().equals(
				task2.getDescription());
		boolean isSameContexts = (task1.getContexts() == null && task2
				.getContexts() == null)
				|| (task1.getContexts().equals(task2.getContexts()));
		boolean isSameCategories = (task1.getCategories() == null && task2
				.getCategories() == null) ||
				task1.getCategories().equals(task2.getCategories());
		// boolean isSameId = (task1.getTaskId() == task2.getTaskId());
		return isSameType && isSameDate && isSameDesc && isSameContexts
				&& isSameCategories;
	}

	private boolean compareAction(Action act1, Action act2) {
		boolean isSameType = (act1.getCommandType().equals(act2
				.getCommandType()));
		boolean isSameTask = compareTask(act1.getTask(), act2.getTask());
		boolean isSameNegateCommand = (act1.undo().getCommandType().equals(act2
				.undo().getCommandType()));
		boolean isSameNegateTask = compareTask(act1.undo().getTask(), act2
				.undo().getTask());
		return isSameType && isSameTask && isSameNegateCommand
				&& isSameNegateTask;
	}

	private boolean compareGroup(GroupAction gr1, GroupAction gr2) {
		boolean isSameSize = gr1.getActionList().size() == gr2.getActionList()
				.size();
		boolean isSame = isSameSize;
		if (isSameSize) {
			int size = gr1.getActionList().size();
			for (int i = 0; i < size; i++) {
				boolean isSameAction = compareAction(gr1.getActionList()
						.get(i), gr2.getActionList().get(i));
				isSame = isSame && isSameAction;
			}
		}
		return isSame;
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

}

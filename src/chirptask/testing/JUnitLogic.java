package chirptask.testing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import chirptask.logic.Action;
import chirptask.logic.FilterTasks;
import chirptask.logic.Logic;
import chirptask.storage.Task;

public class JUnitLogic {

	@Test
	public void test() {
		Logic a = new Logic();
		Action act = new Action();
		Task task = new Task();
		
		task.setTaskId(-1);
		task.setDescription("tml");
		act.setCommandType("display");
		act.setTask(task);
		act.setUndo(null);
		
		List<Task> list = FilterTasks.getFilteredList();
		
		
		a.executeAction(act);
		assertEquals(list, FilterTasks.getFilteredList());
		
		
	}

}

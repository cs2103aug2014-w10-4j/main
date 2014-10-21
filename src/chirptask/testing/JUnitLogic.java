package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import chirptask.common.Settings;
import chirptask.gui.MainGui;
import chirptask.logic.Action;
import chirptask.logic.FilterTasks;
import chirptask.logic.Logic;
import chirptask.storage.Task;
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
	public void testAdd() {
		Stage primaryStage = new Stage();
		MainGui gui = new MainGui();
		gui.start(primaryStage);
		
		Logic logic = new Logic(gui);
		
		logic.retrieveInputFromUI("delete all");
		logic.retrieveInputFromUI("add hom nay em den truong");
	}

}

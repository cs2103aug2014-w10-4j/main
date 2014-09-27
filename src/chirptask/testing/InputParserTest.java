package chirptask.testing;

import static org.junit.Assert.*;

import org.junit.Test;

import chirptask.logic.Action;
import chirptask.logic.GroupAction;
import chirptask.logic.InputParser;
import chirptask.storage.Task;

public class InputParserTest {

	@Test
	public void test() {
		InputParser parser1 = new InputParser("display -done -float");
		GroupAction action1 = new GroupAction();
		action1.addAction(new Action("display", new Task(-1, "done"), null));
		action1.addAction(new Action("display", new Task(-1, "float"), null));
		assertEquals(action1, parser1.getActions());

		InputParser parser4 = new InputParser("delete 0-1, 2");
		GroupAction action4 = new GroupAction();
		action4.addAction(new Action("delete", new Task(0, ""), new Action(
				"add", new Task(0, ""))));
		action4.addAction(new Action("delete", new Task(1, ""), new Action(
				"add", new Task(1, ""))));
		action4.addAction(new Action("delete", new Task(2, ""), new Action(
				"add", new Task(2, ""))));
		assertEquals(action4, parser4.getActions());

		InputParser parser3 = new InputParser("edit 0 def");
		GroupAction action3 = new GroupAction();
		action3.addAction(new Action("edit", new Task(0, "def"), new Action(
				"edit", new Task(0, ""))));
		assertEquals(action3, parser3.getActions());

		InputParser parser2 = new InputParser("add abc@a#b");
		GroupAction action2 = new GroupAction();
		action2.addAction(new Action("add", new Task(0, "abc"), new Action(
				"delete", new Task(0, "abc"))));
		assertEquals(action2, parser2.getActions());
	}

}

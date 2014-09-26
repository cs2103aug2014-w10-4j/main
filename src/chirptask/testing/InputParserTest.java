package chirptask.testing;

import static org.junit.Assert.*;

import org.junit.Test;

import chirptask.logic.Action;
import chirptask.logic.InputParser;
import chirptask.storage.Task;

public class InputParserTest {

	@Test
	public void test() {
		InputParser parser1 = new InputParser("display");
		Action action1 = new Action("display");
		assertEquals(action1, parser1.getAction());
		
		InputParser parser2 = new InputParser("add abc");
		Action action2 = new Action("add", new Task(0, "abc"), new Action("delete", new Task(0, "abc")));
		assertEquals(action2, parser2.getAction());
		
		InputParser parser3 = new InputParser("edit 0 def");
		Action action3 = new Action("edit", new Task(0, "def"), new Action("edit", new Task(0, "")));
		assertEquals(action3, parser3.getAction());
		
		InputParser parser4 = new InputParser("delete 0");
		Action action4 = new Action("delete", new Task(0, ""), new Action("add", new Task(0, "def")));
		assertEquals(action4, parser4.getAction());
	}

}

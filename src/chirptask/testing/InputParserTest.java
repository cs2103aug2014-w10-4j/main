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
		Action action2 = new Action("add", new Task(1, "abc"), new Action("delete", new Task(1, "abc")));
		assertEquals(action2, parser2.getAction());
	}

}

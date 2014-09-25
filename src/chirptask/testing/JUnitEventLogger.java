package chirptask.testing;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;

import org.junit.Test;

import chirptask.storage.*;

public class JUnitEventLogger {

	@Test
	public void test() throws ParseException {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		EventLogger logger = new EventLogger();
		
		logger.storeNewTask(new DeadlineTask(1, "C", df.parse("9/25/14 1:00pm")));
	}

}

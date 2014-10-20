package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.EventLogger;

public class JUnitEventLogger {
	// @author A0111889W
	@Test
	public void test() throws ParseException {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse("9/25/14 1:00pm"));
		EventLogger logger = new EventLogger();
		DeadlineTask dt = new DeadlineTask(1, "C", cal);

		assertEquals(true, logger.storeNewTask(dt));
		assertEquals(dt, logger.removeTask(dt));
		assertEquals(true, logger.modifyTask(dt));
		assertEquals(null, logger.getTask(999));
		assertEquals(null, logger.getAllTasks());

		logger.close();
	}
}

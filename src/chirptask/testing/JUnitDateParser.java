package chirptask.testing;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.logic.DateParser;

public class JUnitDateParser {
	DateParser parser = new DateParser();
	
	@Test
	public void test() {
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		cals = parser.parseDate("23/10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("10/23");
		assertEquals(cals.size(), 0);
		
		cals = parser.parseDate("from today 5pm to 6pm");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 17, 0);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 0);
		
		
	}

	// code reused from natty
	private void validateDateTime(Calendar cal, int year, int month, int date,
			int hour, int minute) {
		assertEquals(cal.get(Calendar.YEAR), year);
		assertEquals(cal.get(Calendar.MONTH), month);
		assertEquals(cal.get(Calendar.DAY_OF_MONTH), date);
		assertEquals(cal.get(Calendar.HOUR_OF_DAY), hour);
		assertEquals(cal.get(Calendar.MINUTE), minute);
	}

}

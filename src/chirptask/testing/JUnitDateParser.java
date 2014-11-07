package chirptask.testing;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.logic.DateParser;
//@author A0113022
public class JUnitDateParser {
	DateParser parser = new DateParser();
	
	@Test
	public void testDate() {
		/*
		 * recognized format:
		 * dd/mm dd-mm dd.mm mm/dd mm-dd mm.dd (dd/mm format takes precedence over mm/dd)
		 * relaxed month dd-MMM, MMM
		 * specific time HH:mm, HHmm, HHmm'h', 
		 * HHmm'hr', hh:mm+am/pm, h+am/pm, hh'a', hh'p'
		 * relaxed day of week
		 * relative date now, today, tomorrow, (next/this/last/from) week, month, day,
		 * hour, hrs, minute, min 
		 */
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		cals = parser.parseDate("23/10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("23-10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("23.10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("10.23");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("10-23");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("10/23");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		
		cals = parser.parseDate("11.3");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 2, 11, 23, 59);
		
		cals = parser.parseDate("23-nov");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 10, 23, 23, 59);
		
		cals = parser.parseDate("from 23/10 to 25/11");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), 10, 25, 23, 59);
		
		cals = parser.parseDate("from 23.10 to 11.25");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), 10, 25, 23, 59);
		
		cals = parser.parseDate("from 23 oct to 11.25");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), 10, 25, 23, 59);
		
		cals = parser.parseDate("from 23 oct to 25 nov");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), 9, 23, 23, 59);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), 10, 25, 23, 59);
	}
	
	@Test
	public void testDateTimeRelative() {
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		cals = parser.parseDate("from today 5pm to 6pm");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 17, 0);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 0);
		
		cals = parser.parseDate("by 12p tomorrow");
		assertEquals(cals.size(), 1);
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_MONTH, 1);
		validateDateTime(cals.get(0), tomorrow.get(Calendar.YEAR), tomorrow.get(Calendar.MONTH),
				tomorrow.get(Calendar.DAY_OF_MONTH), 12, 0);
	}
	
	@Test
	public void testTime() {
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		
		cals = parser.parseDate("by 8a");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 8, 0);
		
		cals = parser.parseDate("by 12p");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 12, 0);
		
		cals = parser.parseDate("by 06:30");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 0630hr");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 0630h");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 0630");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 0630pm");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 30);
		
		cals = parser.parseDate("by 0630am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 0630 am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("by 6 pm");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 0);
		
		cals = parser.parseDate("by 6am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 0);
		
		cals = parser.parseDate("from 6p to 8p");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 0);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 20, 0);
		
		cals = parser.parseDate("from 6p to 0630");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 18, 0);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 30);
		
		cals = parser.parseDate("from 0600 to 0800");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 6, 0);
		validateDateTime(cals.get(1), today.get(Calendar.YEAR), today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH), 8, 0);
	}
	
	@Test
	public void testInvalidDate() {
		List<Calendar> cals;
		cals = parser.parseDate("32/11");
		assertEquals(cals.size(), 0);
				
	}

	// code reused from natty
	private void validateDateTime(Calendar cal, int year, int month, int date,
			int hour, int minute) {
		assertEquals(year, cal.get(Calendar.YEAR));
		assertEquals(month, cal.get(Calendar.MONTH));
		assertEquals(date, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(hour, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(minute, cal.get(Calendar.MINUTE));
	}

}

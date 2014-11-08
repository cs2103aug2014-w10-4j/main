package chirptask.testing;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.logic.DateParser;
import com.joestelmach.natty.CalendarSource;

//@author A0113022
public class JUnitDateParserAtd {
	/*
	 * recognized format: 
	 * Formal date: dd/mm dd-mm dd.mm mm/dd mm-dd mm.dd
	 * (dd/mm format takes precedence over mm/dd) 
	 * Relaxed month: dd-MMM, MMM
	 * Specific time: HH:mm, HHmm, HHmm'h', HHmm'hr', hh:mm am/pm, h am/pm,
	 * hh'a', hh'p' 
	 * Relaxed day of week: Mon, Tue, Wed, Thurs, Thur, Fri, Sat, Sun 
	 * Relative date: now, today, tomorrow, 
	 * (next/this/last) week, month, day, hour, hrs, minute, min
	 */
	DateParser parser = new DateParser();

	@Test
	public void testFormalDate() {

		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		int year = today.get(Calendar.YEAR);

		cals = parser.parseDate("23/10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);

		cals = parser.parseDate("by 23-10-15");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2015, 9, 23, 23, 59);

		cals = parser.parseDate("by 23.10");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);

		cals = parser.parseDate("by 10.23.13");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2013, 9, 23, 23, 59);

		cals = parser.parseDate("by 10-23");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);

		cals = parser.parseDate("by 10/23");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);

		cals = parser.parseDate("by 11.3");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 2, 11, 23, 59);

		cals = parser.parseDate("from 23/10 to 25/11");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), year, 10, 25, 23, 59);

		cals = parser.parseDate("from 23.10 to 11.25");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), year, 10, 25, 23, 59);

		cals = parser.parseDate("from 23.10 to 25.01.15");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), 2015, 0, 25, 23, 59);

		cals = parser.parseDate("from 10-23 to 01.25.15");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), 2015, 0, 25, 23, 59);

		cals = parser.parseDate("from 25.01.15 to 10/23/15");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2015, 0, 25, 23, 59);
		validateDateTime(cals.get(1), 2015, 9, 23, 23, 59);

	}

	@Test
	public void testRelaxedMonth() {

		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		int year = today.get(Calendar.YEAR);

		cals = parser.parseDate("by dec");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 11, 1, 23, 59);

		cals = parser.parseDate("23-nov");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 10, 23, 23, 59);

		cals = parser.parseDate("by 1-dec-2015");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2015, 11, 1, 23, 59);

		cals = parser.parseDate("by jan 1st");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 0, 1, 23, 59);

		cals = parser.parseDate("from 23 oct to 11.25");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), year, 10, 25, 23, 59);

		cals = parser.parseDate("from 23 oct to 25 nov");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 9, 23, 23, 59);
		validateDateTime(cals.get(1), year, 10, 25, 23, 59);

		cals = parser.parseDate("from nov 23rd to nov 25");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 10, 23, 23, 59);
		validateDateTime(cals.get(1), year, 10, 25, 23, 59);

	}

	@Test
	public void testTime() {
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH);
		int date = today.get(Calendar.DAY_OF_MONTH);
	
		cals = parser.parseDate("by 8a");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 8, 0);
	
		cals = parser.parseDate("by 12p");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 12, 0);
	
		cals = parser.parseDate("by 06:30");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 0630hr");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 0630h");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 0630");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 0630pm");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 18, 30);
	
		cals = parser.parseDate("by 0630am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 0630 am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 30);
	
		cals = parser.parseDate("by 6 pm");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 18, 0);
	
		cals = parser.parseDate("by 6am");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, month, date, 6, 0);
	
		cals = parser.parseDate("from 6p to 8p");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, month, date, 18, 0);
		validateDateTime(cals.get(1), year, month, date, 20, 0);
	
		cals = parser.parseDate("from 6p to 0630");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, month, date, 18, 0);
		validateDateTime(cals.get(1), year, month, date, 6, 30);
	
		cals = parser.parseDate("from 0600 to 0800");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, month, date, 6, 0);
		validateDateTime(cals.get(1), year, month, date, 8, 0);
	}
	
	public void testDateTime() {
		Calendar today = Calendar.getInstance();
		int year = today.get(Calendar.YEAR);
		List<Calendar> cals;
		
		cals = parser.parseDate("by 5pm 09.11");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 10, 9, 17, 0);
		
		cals = parser.parseDate("by 23/10 0600");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 9, 23, 6, 0);
		
		cals = parser.parseDate("by 01.01.15 12:00");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2015, 0, 1, 12, 0);
		
		cals = parser.parseDate("at 2300hr 01 nov ");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), year, 10, 1, 23, 0);
		
		cals = parser.parseDate("on 1530 23.12.15");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2015, 11, 23, 15, 30);
		
		cals = parser.parseDate("from 1530 to 1730 01 nov");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 10, 1, 15, 30);
		validateDateTime(cals.get(1), year, 10, 1, 17, 30);
		
		cals = parser.parseDate("from 01 nov 1530 to 1730 03 nov");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 10, 1, 15, 30);
		validateDateTime(cals.get(1), year, 10, 3, 17, 30);
		
		cals = parser.parseDate("from 1530 12/26 to 1730 01 nov");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), year, 11, 26, 15, 30);
		validateDateTime(cals.get(1), year, 10, 1, 17, 30);
	}
	
	//test relative date from this point onward. 
	//Base date 4/11/2014, 10am.
	@Test
	public void testDayOfWeek() {
		Calendar today = Calendar.getInstance();
		List<Calendar> cals;
		today.set(2014, 10, 4, 10, 0, 0);
		CalendarSource.setBaseDate(today.getTime());
		
		cals = parser.parseDate("by Mon");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 10, 23, 59);
		
		cals = parser.parseDate("by next Tues");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 11, 23, 59);
		
		cals = parser.parseDate("by this Thur");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 6, 23, 59);
		
		cals = parser.parseDate("from last wed to next fri");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 9, 29, 23, 59);
		validateDateTime(cals.get(1), 2014, 10, 14, 23, 59);
		
		cals = parser.parseDate("from tomorrow to wed");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 10, 5, 23, 59);
		validateDateTime(cals.get(1), 2014, 10, 5, 23, 59);
	}

	@Test
	public void testDateRelative() {
		List<Calendar> cals;
		Calendar today = Calendar.getInstance();
		today.set(2014, 10, 4, 10, 0, 0);
		CalendarSource.setBaseDate(today.getTime());
		
		cals = parser.parseDate("by today");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 4, 23, 59);
		
		cals = parser.parseDate("from today to tomorrow");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 10, 4, 23, 59);
		validateDateTime(cals.get(1), 2014, 10, 5, 23, 59);
		
		cals = parser.parseDate("by two days from now");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 6, 23, 59);
		
		cals = parser.parseDate("by one week from now");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 11, 23, 59);
		
		cals = parser.parseDate("by one month from now");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 11, 4, 23, 59);
		
		cals = parser.parseDate("by three months from 30/11");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2015, 1, 28, 23, 59);
		
		cals = parser.parseDate("on the next hour");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 4, 11, 0);
		
		cals = parser.parseDate("at thirty mins from now");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 4, 10, 30);
		
		cals = parser.parseDate("from thirty mins to 6 hrs from now");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 10, 4, 10, 30);
		validateDateTime(cals.get(1), 2014, 10, 4, 16, 30);
	}
	
	@Test
	public void testDateTimeRelative() {
		List<Calendar> cals;

		cals = parser.parseDate("from today 5pm to 6pm");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 10, 4, 17, 0);
		validateDateTime(cals.get(1), 2014, 10, 4, 18, 0);

		cals = parser.parseDate("by 12p tomorrow");
		assertEquals(cals.size(), 1);
		validateDateTime(cals.get(0), 2014, 10, 5, 12, 0);
		
		cals = parser.parseDate("from 2p to 5p next month");
		assertEquals(cals.size(), 2);
		validateDateTime(cals.get(0), 2014, 11, 4, 14, 0);
		validateDateTime(cals.get(1), 2014, 11, 4, 17, 0);
	}

	@Test
	public void testInvalidDate() {
		List<Calendar> cals;
		cals = parser.parseDate("32/11");
		assertEquals(cals.size(), 0);

		cals = parser.parseDate("2.29.15");
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

package chirptask.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * Acknowledgment: this code uses natty 0.9 (author joestelmach) 
 * note: the date format is mm/dd
 */
//@author A0113022
public class DateParser {
	private List<Calendar> list;
	private Parser parse;
	
	private final long millisecondADay = 24*60*60*1000;
	
	public DateParser() {
		parse = new Parser();
//		parse.parse("21 oct"); start up natty (the first passing takes >3000 milliseconds
	}

	public List<Calendar> parseDate(String toParse) {
		list = new ArrayList<Calendar>();
		Date today = new Date();
		List<DateGroup> dateGroup = parse.parse(toParse);
		for (int i = 0; i < dateGroup.size(); i++) {
			List<Date> dates = dateGroup.get(i).getDates();
			for (int j = 0; j < dates.size(); j++) {
				Calendar cal = convertToCalendar(dates.get(j), today);
				list.add(cal);
			}
		}

		return list;
	}

	private Calendar convertToCalendar(Date date, Date today) {
		boolean isNotSet = false;
		//assume the chance of user inputting deadline exactly multiple of 
		//millisecondADay/50 after the current time negligible
		if (((date.getTime() - today.getTime())/50) % (millisecondADay/50) == 0) {
			isNotSet = true;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (isNotSet) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		}
		return cal;
	}
}

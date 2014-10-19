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

	public DateParser() {

	}

	public List<Calendar> parseDate(String toParse) {
		Parser parse = new Parser();
		list = new ArrayList<Calendar>();
		List<DateGroup> dateGroup = parse.parse(toParse);
		for (int i = 0; i < dateGroup.size(); i++) {
			List<Date> dates = dateGroup.get(i).getDates();
			for (int j = 0; j < dates.size(); j++) {
				Calendar cal = convertToCalendar(dates.get(j));
				list.add(cal);
			}
		}

		return list;
	}

	private Calendar convertToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
}

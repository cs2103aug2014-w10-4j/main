package chirptask.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

/**
 * Acknowledgment: this code uses natty 0.9 (author joestelmach)
 */
// @author A0113022
public class DateParser {
	private List<Calendar> list;
	private Parser parse;

	private final static String[] patternsDate = { "dd/MM", "dd-MM", "dd.MM",
			"MM/dd","MM-dd","MM.dd","MMM", "dd-MMM", "EEE" };
	private final static String[] patternsTime = { "HH:mm", "HHmm", "HHmm'h'",
			"HHmm'hr'", "hha", "hhmma", "hh:mma", "ha", "hh'a'", "hh'p'" };
	private final static String relativeKey = "next|from|last|this";
	private final static String relativeKeyDate = "now|today|tomorrow|week|month|day|yesterday|weeks|months|days";
	private final static String relativeKeyTime = "am|pm|hour|hours|hrs|min|minute|mins|minutes";

	public DateParser() {
		parse = new Parser();
	}

	public List<Calendar> parseDate(String toParse) {
		boolean success = false;
		boolean mayHas = false;
		boolean isTimeSet = false;
		boolean isDateSet = false;
		int dateMatched = -1; 
		int pos = -1;
		Date date = null;
		Date time = null;

		if (toParse == null) {
			return null;
		}
		
		list = new ArrayList<Calendar>();
		toParse = toParse.replaceAll("\\s+(?=-/.)", "").replaceAll(
				"(?<=-/.)\\s+", "");
		String splitSpace[] = toParse.split("\\s+");
		for (int i = 0; i < patternsDate.length; i++) {
			SimpleDateFormat dateParse = new SimpleDateFormat(patternsDate[i]);
			dateParse.setLenient(false);
			for (int j=0; j < splitSpace.length; j++) {
				try {
					date = dateParse.parse(splitSpace[j]);
				} catch (ParseException e) {
					date = null;
				}
				if (date != null) {
					isDateSet = true;
					success = true;
					dateMatched = i;
					pos = j;
					break;
				}
			}

			if (isDateSet) {
				break;
			}
		}
		if (dateMatched <= 5 && dateMatched >=3 && pos >= 0) {
			String[] flip = splitSpace[pos].split("[/.-]");
			String newDate;
			if (flip.length == 2) {
				newDate = flip[1] + "/" + flip[0];
				toParse = toParse.replaceAll(splitSpace[pos], newDate);
			} 
		}
		
		if (dateMatched == 2 && pos >= 0) {
			String newForm = splitSpace[pos].replace(".", "/");
			toParse = toParse.replaceAll(splitSpace[pos], newForm);
		}

		for (int i = 0; i < patternsTime.length; i++) {
			SimpleDateFormat timeParse = new SimpleDateFormat(patternsTime[i]);
			timeParse.setLenient(false);
			for (String s : splitSpace) {
				try {
					time = timeParse.parse(s);
				} catch (ParseException e) {
					time = null;
				}
				if (time != null) {
					success = true;
					isTimeSet = true;
					break;
				}
			}
			if (isTimeSet) {
				break;
			}

		}

		if (!success) {
			for (String s : splitSpace) {
				if (s.matches(relativeKey)) {
					mayHas = true;
				}
				if (s.matches(relativeKeyDate)) {
					mayHas = true;
					isDateSet = true;
				}
				if (s.matches(relativeKeyTime)) {
					mayHas = true;
					isTimeSet = true;
				}
			}
		}
//		System.out.printf("toParse: %s, success: %s, mayhas: %s\n", toParse,
//				success, mayHas);
		if (success || mayHas) {
			Calendar today = Calendar.getInstance();
			today.setTime(new Date());
			List<DateGroup> dateGroup = parse.parse(toParse);
			for (int i = 0; i < dateGroup.size(); i++) {
				List<Date> dates = dateGroup.get(i).getDates();
				for (int j = 0; j < dates.size(); j++) {
					Calendar cal = convertToCalendar(dates.get(j));
					if (!isTimeSet) {
						cal.set(Calendar.HOUR_OF_DAY, 23);
						cal.set(Calendar.MINUTE, 59);
					}
					list.add(cal);
				}
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
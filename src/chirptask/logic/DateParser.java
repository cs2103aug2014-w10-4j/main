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
//@author A0113022
public class DateParser {
	private List<Calendar> list;
	private Parser parse;

	private final static String[] patternsDate = { "dd/MM", "dd-MM", "dd.MM",
			"MM/dd", "MM-dd", "MM.dd", "MMM", "dd-MMM", "EEE" };
	private final static String[] patternsTime = { "HH:mm", "HHmm", "HHmm'h'",
			"HHmm'hr'", "hha", "hhmma", "hh:mma", "ha", "hh'a'", "hh'p'" };
	private final static String relativeKey = "next|last|this";
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
		String seekDate;
		String seekTime;

		if (toParse == null) {
			return null;
		}

		list = new ArrayList<Calendar>();
		toParse = toParse.replaceAll("\\s+(?=-/.)", "").replaceAll(
				"(?<=-/.)\\s+", "");
		String splitSpace[] = toParse.split("\\s+");
		toParse = "";
		for (int i = 0; i < splitSpace.length; i++) {
			seekDate = findDate(splitSpace[i]);
			if (seekDate != null) {
				isDateSet = true;
				splitSpace[i] = seekDate;
			}

			seekTime = findTime(splitSpace[i]);
			if (seekTime != null) {
				isTimeSet = true;
				splitSpace[i] = seekTime;
			}

			if (!success) {
				success = (isDateSet || isTimeSet);
				if (splitSpace[i].matches(relativeKey)) {
					mayHas = true;
				}
				if (splitSpace[i].matches(relativeKeyDate)) {
					mayHas = true;
					isDateSet = true;
				}
				if (splitSpace[i].matches(relativeKeyTime)) {
					mayHas = true;
					isTimeSet = true;
				}
			}
			
			toParse = toParse.concat(splitSpace[i]).concat(" ");
		}

		if (success || mayHas) {
			parseDateTime(toParse, isTimeSet);
		}
		return list;
	}

	/**
	 * @param toParse
	 * @param isTimeSet
	 */
	private void parseDateTime(String toParse, boolean isTimeSet) {
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

	private String findTime(String seek) {
		Date time = null;
		int pattern = -1;
		for (int i = 0; i < patternsTime.length; i++) {
			SimpleDateFormat timeParse = new SimpleDateFormat(patternsTime[i]);
			timeParse.setLenient(false);
			try {
				time = timeParse.parse(seek);
			} catch (ParseException e) {
				time = null;
			}
			if (time != null) {
				pattern = i;
				break;
			}
		}
		//pattern that may be interpreted wrongly by natty
		if (pattern == 1) {
			seek += 'h';
		} else if (pattern == -1) {
			seek = null;
		}
		return seek;
	}

	private String findDate(String seek) {
		Date date = null;
		int pattern = -1;
		for (int i = 0; i < patternsDate.length; i++) {
			SimpleDateFormat dateParse = new SimpleDateFormat(patternsDate[i]);
			dateParse.setLenient(false);
			try {
				date = dateParse.parse(seek);
			} catch (ParseException e) {
				date = null;
			}
			if (date != null) {
				pattern = i;
				break;
			}
		}
		//pattern natty does not recognize
		if (pattern <= 5 && pattern >= 3) {
			String[] flip = seek.split("[/.-]");
			if (flip.length == 2) {
				seek = flip[1] + "/" + flip[0];
			}
		} else if (pattern == 2) {
			seek = seek.replace(".", "/");
		} else if (pattern == -1) {
			seek = null;
		}
		return seek;
	}

	private Calendar convertToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
}
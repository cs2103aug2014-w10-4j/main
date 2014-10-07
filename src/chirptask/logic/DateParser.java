package chirptask.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * assumption: add abc by dd/mm. Deadline task add abc from 2 to 4. Timed task
 * 
 * @author Linh
 *
 */
public class DateParser {
	private List<Date> list;

	public DateParser() {

	}

	public List<Date> parseDate(String toParse) {
		list = new ArrayList<Date>();
		if (toParse.contains("by")) {
			getDeadline(toParse);
		} else if (toParse.contains("from") || toParse.contains("to")) {
			getStartEnd(toParse);
		}
		return list;
	}

	/**
	 * get start date and end date of timed task. only works for today.
	 * 
	 * @param toParse
	 */
	private void getStartEnd(String toParse) {
		if (!toParse.contains("/")) {
			List<Date> newList = getHours(toParse);
			for (Date d : newList) {
				list.add(d);
			}
		} else {
			timedTaskAnotherDay(toParse);
		}

	}

	/**
	 * assume the form from ... to ... [date]
	 * 
	 * @param toParse
	 */
	private void timedTaskAnotherDay(String toParse) {
		String[] dates = toParse.trim().split("\\s+");
		Date day = getSimpleDateFromString(dates[dates.length - 1]);
		System.out.println(day);
		String hours = new String();
		for (int i = 0; i < dates.length - 1; i++) {
			hours += dates[i];
			hours += " ";
		}
		List<Date> eventTime = getHours(hours.trim());
		for (Date d : eventTime) {
			d.setDate(day.getDate());
			d.setMonth(day.getMonth());
			System.out.println(d);
			list.add(d);
		}
	}

	/**
	 * @param toParse
	 */
	private List<Date> getHours(String toParse) {
		List<Date> hoursToAdd = new ArrayList<Date>();
		String[] dates = toParse.trim().split("from", 2);
		if (dates.length > 1) {
			Date eventStart = new Date();
			Date eventEnd = new Date();
			String[] hours = dates[1].trim().split("\\s+");
			if (dates[1].contains(" to ") && hours.length == 3) {
				if (!(hours[0].contains("am") || hours[0].contains("pm")
						|| hours[2].contains("am") || hours[2].contains("pm"))) {

					eventStart.setHours(Integer.parseInt(hours[0]));
					eventEnd.setHours(Integer.parseInt(hours[2]));
				} else {
					if (hours[0].contains("am")) {
						eventStart.setHours(Integer.parseInt(hours[0]
								.substring(0, 1)));
					} else if (hours[0].contains("pm")) {
						int time = Integer.parseInt(hours[0].substring(0, 1)) + 12;
						eventStart.setHours(time);
					}
					if (hours[2].contains("am")) {
						eventEnd.setHours(Integer.parseInt(hours[2].substring(
								0, 1)));
					} else if (hours[2].contains("pm")) {
						int time = Integer.parseInt(hours[2].substring(0, 1)) + 12;
						eventEnd.setHours(time);
					}
				}
				hoursToAdd.add(eventStart);
				hoursToAdd.add(eventEnd);
			}
		}
		return hoursToAdd;
	}

	/**
	 * @param toParse
	 */
	private void getDeadline(String toParse) {
		String[] tokens = toParse.trim().split("by", 2);
		if (tokens.length > 1) {
			String day = tokens[1].trim();
			Date deadline = getSimpleDateFromString(day);
			list.add(deadline);
		}
	}

	/**
	 * @param tokens
	 */
	private Date getSimpleDateFromString(String day) {
		String[] days = day.split("/");
		switch (days.length) {
		case 1:
			try {
				int time = Integer.parseInt(days[0]);
				Date today = new Date();
				Date deadline = new Date();
				if (time > 0 && time <= 31 && time >= today.getDate()) {
					deadline.setDate(time);

				} else if (time < today.getDate() && time <= 24) {
					deadline.setHours(time);

				}
				return deadline;
			} catch (Exception e) {
				return new Date();
			}

		case 2:
			try {
				int dayOfMonth = Integer.parseInt(days[0]);
				int month = Integer.parseInt(days[1]);
				Date deadline = new Date();
				deadline.setDate(dayOfMonth);
				deadline.setMonth(month - 1);
				return deadline;
			} catch (Exception e) {
				System.out.println("Sth wr here");
				return new Date();
			}

		default:
			return new Date();
		}
	}

	public List<Date> getDate() {
		return list;
	}
}

package chirptask.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * assumption: add abc by dd/mm. Deadline task
 * add abc from 2 to 4. Timed task
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

	private void getStartEnd(String toParse) {
		if (!toParse.contains("/")) {
			String[] dates = toParse.trim().split("from", 2);
			if (dates.length > 1) {
				Date eventStart = new Date();
				Date eventEnd = new Date();
				String[] hours = dates[1].trim().split("\\s+");
				if (dates[1].contains(" to ") && hours.length == 3) {
					eventStart.setHours(Integer.parseInt(hours[0]));
					eventEnd.setHours(Integer.parseInt(hours[2]));
					list.add(eventStart);
					list.add(eventEnd);
				}
			}
		}
		
	}

	/**
	 * @param toParse
	 */
	private void getDeadline(String toParse) {
		String[] tokens = toParse.trim().split("by", 2);
		if (tokens.length > 1) {
			String day = tokens[1].trim();
			String[] days = day.split("/");
			
			switch (days.length) {
			case 1:
				try {
					int time = Integer.parseInt(days[0]);
					Date today = new Date();
					Date deadline = new Date();
					if (time > 0 && time <= 31 && time >= today.getDate()) {
						deadline.setDate(time);
						list.add(deadline);
					} else if (time < today.getDate() && time <= 24) {
						deadline.setHours(time);
						list.add(deadline);
					}
				} catch (Exception e) {
					
				}
				break;
			case 2:
				try {
					 int dayOfMonth = Integer.parseInt(days[0]);
					 int month = Integer.parseInt(days[1]);
					 Date deadline = new Date();
					 deadline.setDate(dayOfMonth);
					 deadline.setMonth(month - 1);
					 list.add(deadline);
				} catch (Exception e) {
					System.out.println("Sth wr here");
				}
				break;
			default: 
				list.add(new Date());	
			}
		}
	}
	
	public List<Date> getDate() {
		return list;
	}
}


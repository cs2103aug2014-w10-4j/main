package chirptask.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * assumption: add abc by dd/mm. Deadline task
 * problem: GUI displays the task first then the date. 
 * And there must be a floating task added before we can 
 * add a deadline task
 * @author Linh
 *
 */
public class DateParser {
	private List<Date> list;

	public DateParser(String toParse) {
		list = new ArrayList<Date>();
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
					System.out.println("Sth wrong");
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

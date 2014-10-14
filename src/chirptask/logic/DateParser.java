package chirptask.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.joestelmach.natty.*;

/**
 * Acknowledgment: this code uses methods found in natty (author joestelmach) 
 * @author Linh
 * note: the date format is mm/dd
 */
public class DateParser {
	private List<Date> list;

	public DateParser() {

	}

	public List<Date> parseDate(String toParse) {
		Parser parse = new Parser();
		list = new ArrayList<Date>();
		List<DateGroup> dateGroup = parse.parse(toParse);
		for (int i = 0; i < dateGroup.size(); i++) {
			List<Date> dates = dateGroup.get(i).getDates();
			for (int j = 0; j < dates.size(); j++) {
				list.add(dates.get(j));
			}
		}

		return list;
	}
}

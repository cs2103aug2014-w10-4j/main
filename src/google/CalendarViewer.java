//@author A0111840W
package chirptask.google;

import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * CalendarViewer provides static methods to separate methods, that perform
 * format/display/retrieve functions, from the CalendarController class.
 * 
 * This allows a global instance of the mentioned functions as well as provide 
 * easier maintenance of the CalendarController class.
 */

public class CalendarViewer {

	static void header(String name) {
		System.out.println();
		System.out.println("============== " + name + " ==============");
		System.out.println();
	}

	static void display(CalendarList feed) {
		if (feed.getItems() != null) {
			for (CalendarListEntry entry : feed.getItems()) {
				System.out.println();
				System.out
						.println("------------------------------------------");
				display(entry);
			}
		}
	}

	static void display(Events feed) {
		if (feed.getItems() != null) {
			for (Event entry : feed.getItems()) {
				System.out.println();
				System.out
						.println("------------------------------------------");
				display(entry);
			}
		}
	}

	static void display(CalendarListEntry entry) {
		System.out.println("ID: " + entry.getId());
		System.out.println("Summary: " + entry.getSummary());
		if (entry.getDescription() != null) {
			System.out.println("Description: " + entry.getDescription());
		}
	}

	static void display(Calendar entry) {
		System.out.println("ID: " + entry.getId());
		System.out.println("Summary: " + entry.getSummary());
		if (entry.getDescription() != null) {
			System.out.println("Description: " + entry.getDescription());
		}
	}

	static void display(Event event) {
		if (event.getStart() != null) {
			System.out.println("Start Time: " + event.getStart());
		}
		if (event.getEnd() != null) {
			System.out.println("End Time: " + event.getEnd());
		}
	}

}

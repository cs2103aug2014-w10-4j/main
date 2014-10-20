//@author A0111840W
package chirptask.google;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;

/**
 * DateTimeHandler is a class that contains static methods to help
 * parse dates from input (assumed to be formatted before reaching here)
 * into a Google DateTime object used by the relevant Google API.
 */
public class DateTimeHandler {
    private static final String DEFAULT_TIME_ZONE = "Asia/Singapore";
	static final String DATE_FORMAT = "yyyy-MM-dd";
	static final String DEFAULT_DATE = "2015-12-31";

	static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat(
			DATE_FORMAT);

	static Date getDateFromInput(String input) {
		Date newDate = null;
		if (input == null) {
			input = DEFAULT_DATE;
		}
		try {
			newDate = FORMAT_DATE.parse(input);
		} catch (ParseException parseError) {
			newDate = getDateFromToday();
		}
		return newDate;
	}

	static DateTime getDateTime() {
		Date currentDate = getDateFromToday();
		TimeZone hostTimeZone = getTimeZoneFromDefault();
		DateTime newDateTime = newDateTime(currentDate, hostTimeZone);
		return newDateTime;
	}

	static Date getDateFromToday() {
		Date newDate = new Date();
		return newDate;
	}

	static TimeZone getTimeZoneFromDefault() {
	    TimeZone defaultTimeZone = TimeZone.getTimeZone(DEFAULT_TIME_ZONE);
	    return defaultTimeZone;
	}
	
	static TimeZone getTimeZoneFromHost() {
		TimeZone hostTimeZone = TimeZone.getDefault();
		return hostTimeZone;
	}

	static DateTime newDateTime(Date date, TimeZone timeZone) {
		DateTime newDateTime = new DateTime(date, timeZone);
		return newDateTime;
	}

	static DateTime getDateTime(String inputDate) {
		Date dateFromInput = getDateFromInput(inputDate);
		TimeZone hostTimeZone = getTimeZoneFromDefault();
		DateTime newDateTime = newDateTime(dateFromInput, hostTimeZone);
		return newDateTime;
	}
	
	static DateTime getDateTime(Date inputDate) {
        TimeZone hostTimeZone = getTimeZoneFromDefault();
        DateTime newDateTime = newDateTime(inputDate, hostTimeZone);
        return newDateTime;
    }
	
	//For Google Calendar Events
	static EventDateTime getEventDateTime(Date inputDate) {
	    DateTime googleDateTime = getDateTime(inputDate);
	    EventDateTime eventDateTime = new EventDateTime();
	    eventDateTime.setDate(googleDateTime);
	    return eventDateTime;
        
	}
	
	static Calendar getCalendar(EventDateTime eventDateTime) {
	    Long eventLong = eventDateTime.getDateTime().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventLong);
	    return calendar;
	}
	
	static Calendar getDateFromDateTime(DateTime dateTime) {
	    Long dateLong = dateTime.getValue();
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(dateLong);
	    return calendar;
	}

}

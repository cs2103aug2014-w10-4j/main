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
	    if (date == null || timeZone == null) {
	        return null;
	    }
	    
		DateTime newDateTime = new DateTime(date, timeZone);
		return newDateTime;
	}

	static DateTime getDateTime(String inputDate) {
	    if (inputDate == null) {
	        return null;
	    }
	    
		Date dateFromInput = getDateFromInput(inputDate);
		TimeZone hostTimeZone = getTimeZoneFromDefault();
		DateTime newDateTime = newDateTime(dateFromInput, hostTimeZone);
		return newDateTime;
	}
	
	static DateTime getDateTime(Date inputDate) {
	    if (inputDate == null) {
	        return null;
	    }
	    
        TimeZone hostTimeZone = getTimeZoneFromDefault();
        DateTime newDateTime = newDateTime(inputDate, hostTimeZone);
        return newDateTime;
    }
	
	//For Google Calendar Events
	static EventDateTime getEventDateTime(Date inputDate) {
	    if (inputDate == null) {
	        return null;
	    }
	    
	    DateTime googleDateTime = getDateTime(inputDate);
	    EventDateTime eventDateTime = new EventDateTime();
	    eventDateTime.setDate(googleDateTime);
	    return eventDateTime;
        
	}
	/**
	 * For Google Calendar's Events, we will parse EventDateTime
	 * to Calendar (ChirpTask's "native" date object)
	 * @param eventDateTime From a Google Calendar Event object
	 * @return The converted Calendar object
	 */
	static Calendar getCalendar(EventDateTime eventDateTime) {
	    if (eventDateTime == null) {
	        return null;
	    }
	    
	    Long eventLong = eventDateTime.getDateTime().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventLong);
	    return calendar;
	}
	
	/**
	 * For Google Tasks, DateTime is passed in,
	 * we will convert it to 23:59 by default, after setting the day.
	 * @param dateTime From a Google Task object
	 * @return The converted Calendar object
	 */
	static Calendar getDateFromDateTime(DateTime dateTime) {
	    if (dateTime == null) {
	        return null;
	    }
	    
	    Long dateLong = dateTime.getValue();
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(dateLong);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
	    return calendar;
	}

}

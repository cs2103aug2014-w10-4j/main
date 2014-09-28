package chirptask.google;

import java.io.IOException;

import com.google.api.services.calendar.model.Calendar;

public class CalendarHandler {
    static boolean isNull(Calendar calendar) {
        if (calendar == null) {
            return true;
        } else {
            return false;
        }
    }
    
    static Calendar addCalendar(String calendarName) throws IOException {
        Calendar createdCalendar = createCalendar(calendarName);
        Calendar addedCalendar = insertCalendarIntoGCal(createdCalendar);
        return addedCalendar;
    }
    
    private static Calendar createCalendar(String calendarName) {
        Calendar createdCalendar = new Calendar();
        setCalendarName(createdCalendar, calendarName);
        setTimeZone(createdCalendar, "Asia/Singapore");
        return createdCalendar;
    }
    
    static void setCalendarName(Calendar editCalendar, String newName) {
        editCalendar.setSummary(newName);
    }
    
    static void setTimeZone(Calendar editCalendar, String timeZone) {
        editCalendar.setTimeZone(timeZone);
    }
    
    private static Calendar insertCalendarIntoGCal(Calendar calendarToInsert) throws IOException {
        Calendar insertedCalendar = CalendarController._calendarClient.calendars().insert(calendarToInsert).execute();
        return insertedCalendar;
    }
    
    static void updateCalendar(String calendarId) throws IOException {
        Calendar calendarToUpdate = retrieveCalendarById(calendarId);
        updateCalendar(calendarId, calendarToUpdate);
    }
    
    private static void updateCalendar(String calendarId, Calendar toUpdate) throws IOException {
        CalendarController._calendarClient.calendars().update(calendarId, toUpdate).execute();
    }
    
    static Calendar retrieveCalendarById(String calendarId) throws IOException {
        Calendar retrievedCalendar = CalendarController._calendarClient.calendars().get(calendarId).execute();
        return retrievedCalendar;
    }

}

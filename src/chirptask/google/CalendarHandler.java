package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class CalendarHandler {
    private static final String DEFAULT_TIME_ZONE = "Asia/Singapore";
    
    static boolean isNull(Calendar calendar) {
        if (calendar == null) {
            return true;
        } else {
            return false;
        }
    }
    
    //Methods related to Calendars
    static Calendar addCalendar(String calendarName) 
            throws UnknownHostException, IOException {
        Calendar createdCalendar = createCalendar(calendarName);
        Calendar addedCalendar = insertCalendarIntoGCal(createdCalendar);
        return addedCalendar;
    }
    
    private static Calendar createCalendar(String calendarName) {
        Calendar createdCalendar = new Calendar();
        setCalendarName(createdCalendar, calendarName);
        setTimeZone(createdCalendar, DEFAULT_TIME_ZONE);
        return createdCalendar;
    }
    
    private static void setCalendarName(Calendar editCalendar, String newName) {
        editCalendar.setSummary(newName);
    }
    
    private static void setTimeZone(Calendar editCalendar, String timeZone) {
        editCalendar.setTimeZone(timeZone);
    }
    
    private static Calendar insertCalendarIntoGCal(Calendar calendarToInsert) 
            throws UnknownHostException, IOException {
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Calendar insertedCalendar = calendarClient
                                    .calendars()
                                    .insert(calendarToInsert)
                                    .execute();
        return insertedCalendar;
    }
    
    static Calendar retrieveCalendarById(String calendarId) 
            throws UnknownHostException, IOException {
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Calendar retrievedCalendar = calendarClient
                                        .calendars()
                                        .get(calendarId)
                                        .execute();
        return retrievedCalendar;
    }
    
    //Methods related to Events
    static Event createEvent(String eventName) {
        Event newEvent = new Event();
        
        newEvent.setSummary(eventName);
        
        return newEvent;
    }
    
    static Event setStart(Event eventToSet, Date startTime) {
        EventDateTime eventStartTime = new EventDateTime();
        DateTime googleDateTime = DateTimeHandler.getDateTime(startTime);
        eventStartTime.setDateTime(googleDateTime);
        
        Event updatedEvent = eventToSet.setStart(eventStartTime);
        
        return updatedEvent;
    }
    
    static Event setEnd(Event eventToSet, Date endTime) {
        EventDateTime eventEndTime = new EventDateTime();
        DateTime googleDateTime = DateTimeHandler.getDateTime(endTime);
        eventEndTime.setDateTime(googleDateTime);
        
        Event updatedEvent = eventToSet.setEnd(eventEndTime);
        
        return updatedEvent;
    }
    
    static Event insertToCalendar(String calendarId, Event eventToInsert) 
            throws UnknownHostException, IOException {
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event insertedEvent = calendarClient
                                .events()
                                .insert(calendarId, eventToInsert)
                                .execute();
        return insertedEvent;
    }
    
    static Event getEventFromId(String calendarId, String eventId) 
            throws UnknownHostException, IOException {
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event foundEvent = calendarClient
                                    .events()
                                    .get(calendarId, eventId)
                                    .execute();
        return foundEvent;
    }
    
    static boolean deleteEvent(String calendarId, String eventId) {
        boolean isDeleted = false;
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        try {
            calendarClient.events().delete(calendarId, eventId).execute();
            isDeleted = true;
        } catch (UnknownHostException unknownHostException) {
            
        } catch (IOException ioException) {
            
        }
        return isDeleted;
    }

}

package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class CalendarHandler {

    private static final String COLOR_ID_DONE = "8";
    private static final String COLOR_ID_UNDONE = "2";
    private static final String DEFAULT_TIME_ZONE = "Asia/Singapore";
    private static final String EVENT_DONE = "transparent";
    private static final String EVENT_UNDONE = "opaque";
    
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
    
    private static void setCalendarName(Calendar editCalendar, String desc) {
        editCalendar.setSummary(desc);
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
        
        if (calendarClient == null) {
            return null;
        }
        
        Calendar retrievedCalendar = calendarClient
                                        .calendars()
                                        .get(calendarId)
                                        .execute();
        return retrievedCalendar;
    }
    
    static List<Event> retrieveEventsById(String calendarId) throws 
                                            UnknownHostException, IOException {
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        String pageToken = null;
        List<Event> allEvents = new ArrayList<Event>();
        
        do {
            Events retrievedEvents = calendarClient.events()
                    .list(calendarId)
                    .setPageToken(pageToken)
                    .execute();
            List<Event> currentPageEvents = retrievedEvents.getItems();
            
            for (Event currentEvent : currentPageEvents) {
                allEvents.add(currentEvent);
            }
            
            pageToken = retrievedEvents.getNextPageToken();
        } while (pageToken != null);
        
        return allEvents;
    }
    
    
    
    //Methods related to Events
    static Event createEvent(String eventName) {
        Event newEvent = new Event();
        newEvent = setSummary(newEvent, eventName);
        return newEvent;
    }
    
    static Event setSummary(Event eventToSet, String eventDescription) {
        Event updatedEvent = eventToSet.setSummary(eventDescription);
        return updatedEvent;
    }
    
    static Event setDescription(Event eventToSet, String eventDescription) {
        Event updatedEvent = eventToSet.setDescription(eventDescription);
        return updatedEvent;
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
    
    static Event setColorAndLook(Event eventToSet, boolean isDone) {
        Event updatedEvent = eventToSet;
        
        if (updatedEvent == null) {
            return null;
        }
        
        if (isDone) {
            updatedEvent.setColorId(COLOR_ID_DONE);
            updatedEvent.setTransparency(EVENT_DONE);
        } else {
            updatedEvent.setColorId(COLOR_ID_UNDONE);
            updatedEvent.setTransparency(EVENT_UNDONE);
        }
        
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
    
    static Event updateEvent(String calendarId, String eventId, Event newEvent) 
            throws UnknownHostException, IOException {
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event updatedEvent = calendarClient.events()
                .update(calendarId, eventId, newEvent)
                .execute();
        
        return updatedEvent;
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

//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.TimedTask;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class CalendarHandler {

    private static final String COLOR_ID_DONE = "8"; //grey
    private static final String COLOR_ID_UNDONE = "2"; //light blue-green
    private static final String DEFAULT_TIME_ZONE = "Asia/Singapore";
    private static final String EVENT_DONE = "transparent";
    private static final String EVENT_UNDONE = "opaque";
    public static final String CALENDAR_DONE = "[Done] ";
    
    static boolean isNull(Calendar calendar) {
        if (calendar == null) {
            return true;
        } else {
            return false;
        }
    }
    
    //Methods related to Calendars
    /**
     * Adds a new Calendar into the account with the specified calendar name
     * @param calendarName The new calendar name
     * @return The newly added Google Calendar
     * @throws UnknownHostException If Google host is unreachable
     * @throws IOException If bad response or transmission error
     */
    static Calendar addCalendar(String calendarName) 
            throws UnknownHostException, IOException {
        if (calendarName == null) {
            return null;
        }
        
        Calendar createdCalendar = createCalendar(calendarName);
        Calendar addedCalendar = insertCalendarIntoGCal(createdCalendar);
        return addedCalendar;
    }
    
    private static Calendar createCalendar(
                            String calendarName) throws NullPointerException {
        if (calendarName == null) {
            return null;
        }
        
        Calendar createdCalendar = new Calendar();
        setCalendarName(createdCalendar, calendarName);
        setTimeZone(createdCalendar, DEFAULT_TIME_ZONE);
        return createdCalendar;
    }
    
    private static void setCalendarName(Calendar editCalendar, 
                                    String desc) throws NullPointerException {
        if (editCalendar == null || desc == null) {
            throw new NullPointerException();
        }
        editCalendar.setSummary(desc);
    }
    
    private static void setTimeZone(Calendar editCalendar, 
                                String timeZone) throws NullPointerException {
        if (editCalendar == null || timeZone == null) {
            throw new NullPointerException();
        }
        editCalendar.setTimeZone(timeZone);
    }
    
    private static Calendar insertCalendarIntoGCal(Calendar calendarToInsert) 
            throws UnknownHostException, IOException {
        if (calendarToInsert == null) {
            return null;
        }
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Calendar insertedCalendar = calendarClient
                                    .calendars()
                                    .insert(calendarToInsert)
                                    .execute();
        return insertedCalendar;
    }
    
    /**
     * Gets the Google Calendar by its ID
     * @param calendarId The Google Calendar ID
     * @return The retrieved Google Calendar 
     * @throws UnknownHostException If Google host is unreachable
     * @throws IOException If bad response or transmission error
     */
    static Calendar retrieveCalendarById(String calendarId) 
            throws UnknownHostException, IOException {
        if (calendarId == null) {
            return null;
        }
        
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
        if (calendarId == null) {
            return new ArrayList<Event>();
        }
        
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
        if (eventName == null) {
            return null;
        }
        
        Event newEvent = new Event();
        newEvent = setSummary(newEvent, eventName);
        return newEvent;
    }
    
    /**
     * Google Component will auto add suffix [Done] to the Calendar
     * so that we do not dirty ChirpTask with [Done] tags all over.
     * @param eventToSet The Google Calendar Event object
     * @param desc The current description
     * @param isDone The boolean flag to indicate whether task is done
     * @return The modified Google Calendar Event object
     */
    static Event setDoneTag(Event eventToSet, String desc, boolean isDone) {
        if (eventToSet == null || desc == null) {
            return null;
        }
        
        if (isDone) {
            String descWithDone = CALENDAR_DONE + desc;
            eventToSet.setSummary(descWithDone);
        } 
        return eventToSet;
    }
    
    /**
     * Sets the Google Calendar Event's title
     * @param eventToSet The Google Calendar Event object
     * @param eventSummary The new title to set
     * @return The updated Google Calendar Event object
     */
    static Event setSummary(Event eventToSet, String eventSummary) {
        if (eventToSet == null || eventSummary == null) {
            return null;
        }
        
        Event updatedEvent = eventToSet.setSummary(eventSummary);
        return updatedEvent;
    }
    
    /**
     * Sets the Google Calendar Event's description
     * @param eventToSet The Google Calendar Event object
     * @param eventDescription The new description to set
     * @return The updated Google Calendar Event object
     */
    static Event setDescription(Event eventToSet, String eventDescription) {
        if (eventToSet == null || eventDescription == null) {
            return null;
        }
        
        Event updatedEvent = eventToSet.setDescription(eventDescription);
        return updatedEvent;
    }
    
    /**
     * Sets the Google Calendar Event's start and end times
     * @param modifiedTask The ChirpTask Task object
     * @param modifiedEvent The Google Calendar Event object
     * @return The updated Google Calendar Event object
     */
    static Event setStartAndEnd(TimedTask modifiedTask, Event modifiedEvent) {
        if (modifiedTask == null || modifiedEvent == null || 
                modifiedTask instanceof TimedTask == false) {
            return null;
        }
        
        Date newStartTime = modifiedTask.getStartTime().getTime();
        Date newEndTime = modifiedTask.getEndTime().getTime();
        
        Event modifiedGoogleEvent = modifiedEvent;
        modifiedGoogleEvent = 
                setStart(modifiedEvent, newStartTime);
        modifiedGoogleEvent = 
                setEnd(modifiedEvent, newEndTime);
        return modifiedGoogleEvent;
    }
    
    /**
     * Sets the Google Calendar Event's start time
     * @param eventToSet The Google Calendar Event object
     * @param startTime The Start Time's Date object
     * @return The updated Google Calendar Event object
     */
    static Event setStart(Event eventToSet, Date startTime) {
        if (eventToSet == null || startTime == null) {
            return null;
        }
        
        EventDateTime eventStartTime = new EventDateTime();
        DateTime googleDateTime = DateTimeHandler.getDateTime(startTime);
        eventStartTime.setDateTime(googleDateTime);
        
        Event updatedEvent = eventToSet.setStart(eventStartTime);
        
        return updatedEvent;
    }
    
    /**
     * Sets the Google Calendar Event's end time
     * @param eventToSet The Google Calendar Event object
     * @param endTime The End Time's Date object
     * @return The updated Google Calendar Event object
     */
    static Event setEnd(Event eventToSet, Date endTime) {
        if (eventToSet == null || endTime == null) {
            return null;
        }
        
        EventDateTime eventEndTime = new EventDateTime();
        DateTime googleDateTime = DateTimeHandler.getDateTime(endTime);
        eventEndTime.setDateTime(googleDateTime);
        
        Event updatedEvent = eventToSet.setEnd(eventEndTime);
        
        return updatedEvent;
    }
    
    /**
     * Sets the Google Calendar Event's Color and Opaqueness
     * @param eventToSet The Google Calendar Event object to set
     * @param isDone The boolean flag to indicate if is done
     * @return The updated Google Calendar Event
     */
    static Event setColorAndLook(Event eventToSet, boolean isDone) {
        if (eventToSet == null) {
            return null;
        }
        
        Event updatedEvent = eventToSet;
        
        if (isDone) {
            updatedEvent.setColorId(COLOR_ID_DONE);
            updatedEvent.setTransparency(EVENT_DONE);
        } else {
            updatedEvent.setColorId(COLOR_ID_UNDONE);
            updatedEvent.setTransparency(EVENT_UNDONE);
        }
        
        return updatedEvent;
    }
    
    /**
     * Inserts the Google Calendar Event to the specified Calendar ID
     * @param calendarId The specified Google Calendar ID
     * @param eventToInsert The Google Calendar Event object
     * @return The inserted Google Calendar Event object
     * @throws UnknownHostException If Google host is unreachable
     * @throws IOException If bad response or transmission error
     */
    static Event insertToCalendar(String calendarId, Event eventToInsert) 
            throws UnknownHostException, IOException {
        if (calendarId == null || eventToInsert == null) {
            return null;
        }
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event insertedEvent = calendarClient
                                .events()
                                .insert(calendarId, eventToInsert)
                                .execute();
        return insertedEvent;
    }
    
    /**
     * Updates the Google Calendar Event online from its ID
     * @param calendarId The specified Google Calendar ID
     * @param eventId The Google Calendar Event ID
     * @param newEvent The updated Google Calendar Event
     * @return The updated Google Calendar Event upon success
     * @throws UnknownHostException If Google host is unreachable
     * @throws IOException If bad repsonse or transmission error
     */
    static Event updateEvent(String calendarId, String eventId, Event newEvent) 
            throws UnknownHostException, IOException {
        if (calendarId == null || eventId == null || newEvent == null) {
            return null;
        }
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event updatedEvent = calendarClient.events()
                .update(calendarId, eventId, newEvent)
                .execute();
        
        return updatedEvent;
    }
    
    /**
     * Gets a specific Google Calendar Event ID with the specified ID
     * @param calendarId The Google Calendar ID
     * @param eventId The Google Calendar Event ID
     * @return The Google Calendar Event that was found
     * @throws UnknownHostException If Google host is unreachable
     * @throws IOException If bad response or transmission error
     */
    static Event getEventFromId(String calendarId, String eventId) 
            throws UnknownHostException, IOException {
        if (calendarId == null || eventId == null) {
            return null;
        }
        
        com.google.api.services.calendar.Calendar calendarClient = 
                CalendarController.getCalendarClient();
        
        Event foundEvent = calendarClient
                                    .events()
                                    .get(calendarId, eventId)
                                    .execute();
        return foundEvent;
    }
    
    /**
     * Deletes the specified Google Calendar Event with its specified ID
     * @param calendarId The Google Calendar ID
     * @param eventId The Google Calendar Event ID
     * @return true if deleted, false if otherwise
     */
    static boolean deleteEvent(String calendarId, String eventId) {
        if (calendarId == null || eventId == null) {
            return false;
        }
        
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

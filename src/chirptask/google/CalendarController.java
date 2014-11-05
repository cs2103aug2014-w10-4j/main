//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;

/**
 * CalendarController is the main controller that interacts with Google 
 * Calendar. It uses the Google Calendar v3 API to do such operations. 
 */
public class CalendarController {
    private final int RESOURCE_NOT_FOUND = 404;
    
    private final boolean DEFAULT_DONE_STATUS = false;
    
    private final String DEFAULT_CALENDAR = "ChirpTaskv0.5";
    private final String GOOGLE_SERVICE_NAME = "calendar";
    private final String JSON_NOT_FOUND = "Not Found";
    
    /**
     * Global instance of the Google Calendar Service Client. Calendar
     * calendarClient; is the main object connected to the Google Calendar API.
     */
    private static com.google.api.services.calendar.Calendar _calendarClient;
    
    /** Global instance of the working Google Calendar ID */
    private static String _calendarId;


    CalendarController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) throws IOException {
        initializeCalendarClient(httpTransport, jsonFactory, credential,
                applicationName);
        initializeCalendarId();
        initializeCalendar(getCalendarId());
    }
    
    //@author A0111840W-unused 
    // Code is unused because we remove the need for this additional file
    // Now we store Google Calendar ID in the Settings, config.properties file
    /*private void initializeHostFiles() throws IOException {
        try {
            TIMEDTASK_CALENDAR_ID_STORE_FILE.getParentFile().mkdirs();
            TIMEDTASK_CALENDAR_ID_STORE_FILE.createNewFile();
        } catch (IOException ioError) {
            String event = "failed to initialize Google Calendar ID File on Host";
            StorageHandler.logError(event);
            throw new IOException();
        }
    }*/

    //@author A0111840W
    private void initializeCalendarClient(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        // initialize the Google Calendar Service Client
        _calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }
    
    private void initializeCalendarId() {
        String timedTaskCalendarId = retrieveId();
        setCalendarId(timedTaskCalendarId);
    }
    
    private String retrieveId() {
        String workingListId = IdHandler.getIdFromSettings();
        return workingListId;
    }

    private void setCalendarId(String newId) {
        _calendarId = newId;
    }

    private Calendar initializeCalendar(String calendarId) {
        if (calendarId == null) { // If null ID, assume fresh install/run
            Calendar newCalendar = null;
            try {
                newCalendar = createCalendar();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newCalendar;
        } else {
            try {
                Calendar foundCalendar = CalendarHandler.retrieveCalendarById(calendarId);
                
                if (CalendarHandler.isNull(foundCalendar)) { // TaskList not found
                    foundCalendar = createCalendar();
                }
                return foundCalendar;
                
            } catch (UnknownHostException unknownHost) {
                // No internet
                initializeCalendar(calendarId);
                
            } catch (GoogleJsonResponseException jsonResponseEx) {
                int responseCode = jsonResponseEx.getStatusCode();
                String responseMessage = jsonResponseEx.getStatusMessage();
                
                if (responseCode == RESOURCE_NOT_FOUND && 
                        JSON_NOT_FOUND.equals(responseMessage)) {
                    GoogleController.resetGoogleIdAndEtag(GOOGLE_SERVICE_NAME);
                    initializeCalendar(null);
                }
                
            } catch (IOException ioError) { // Error during transmission
                initializeCalendar(calendarId);
            } 
        }
        return null;
    }

    private Calendar createCalendar() throws UnknownHostException, IOException {
        Calendar newCalendar = CalendarHandler.addCalendar(DEFAULT_CALENDAR);
        setCalendarId(newCalendar);
        String calendarId = getCalendarId();
        IdHandler.saveIdToSettings(calendarId);
        return newCalendar;
    }
    
    private void setCalendarId(Calendar calendar) {
        String calendarId = calendar.getId();
        setCalendarId(calendarId);
    }
    
    static String getCalendarId() {
        return _calendarId;
    }
    
    List<Event> getEvents() throws UnknownHostException, IOException {
        List<Event> events = CalendarHandler.retrieveEventsById(_calendarId);
        return events;
    }
    
    Event addTimedTask(String taskTitle, Date startTime, Date endTime) 
                                throws UnknownHostException, IOException {
        Event newTimedTask = CalendarHandler.createEvent(taskTitle);
        newTimedTask = CalendarHandler.setStart(newTimedTask, startTime);
        newTimedTask = CalendarHandler.setEnd(newTimedTask, endTime);
        newTimedTask = CalendarHandler.setColorAndLook(newTimedTask, 
                DEFAULT_DONE_STATUS);
        Event addedEvent = insertEvent(newTimedTask);
        return addedEvent;
    }
    
    private Event insertEvent(Event timedTask)
                                throws UnknownHostException, IOException {
        String calendarId = getCalendarId();
        Event insertedEvent = CalendarHandler.insertToCalendar(calendarId, 
                timedTask);
        return insertedEvent;
    }
    
    boolean deleteEvent(String eventId) {
        boolean isDeleted = false;
        isDeleted = CalendarHandler.deleteEvent(_calendarId, eventId);
        return isDeleted;
    }

    static com.google.api.services.calendar.Calendar getCalendarClient() {
        return _calendarClient;
    }
    
}

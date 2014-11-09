//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.storage.StorageHandler.GoogleService;

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
    private final String JSON_NOT_FOUND = "Not Found";
    
    /**
     * Global instance of the Google Calendar Service Client. Calendar
     * calendarClient; is the main object connected to the Google Calendar API.
     */
    private static com.google.api.services.calendar.Calendar _calendarClient = null;
    
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
    /**
     * Creates the Calendar Client object to be used for Google Calendar
     * @param httpTransport The provided HttpTransport object
     * @param jsonFactory The provided JsonFactory object
     * @param credential The provided OAuth Credential
     * @param applicationName The provided application name for OAuth
     * @throws NullPointerException If any parameter is null
     */
    private void initializeCalendarClient(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) throws NullPointerException {
        if (httpTransport == null || 
                jsonFactory == null || 
                credential == null || 
                applicationName == null) {
            throw new NullPointerException();
        }
        
        // initialize the Google Calendar Service Client
        _calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }
    
    /**
     * Reads and sets the Calendar ID from Settings
     * @throws NullPointerException If timedTaskCalendarId is null
     */
    private void initializeCalendarId() throws NullPointerException {
        String timedTaskCalendarId = retrieveId();
        setCalendarId(timedTaskCalendarId);
    }
    
    private String retrieveId() {
        String workingListId = IdHandler.getIdFromSettings();
        return workingListId;
    }

    private void setCalendarId(String newId) throws NullPointerException {
        if (newId == null) {
            throw new NullPointerException();
        }
        _calendarId = newId;
    }

    /**
     * Creates the Calendar object for Google Calendar if doesn't exist
     * Else it will retrieve the Calendar from Google Calendar
     * @param calendarId The Google Calendar ID to retrieve
     * @return A Calendar object that was successful in initialization
     * @throws UnknownHostException If the host cannot be contacted
     * @throws IOException If it got an invalid response or transmission error
     */
    private Calendar initializeCalendar(String calendarId) throws 
                                        UnknownHostException, IOException {
        if (calendarId == null || "".equals(calendarId)) { 
            // Assume fresh install/run
            Calendar newCalendar = null;
                newCalendar = createCalendar();
            return newCalendar;
        } else {
            try {
                Calendar foundCalendar = 
                        CalendarHandler.retrieveCalendarById(calendarId);
                
                // TaskList not found
                if (CalendarHandler.isNull(foundCalendar)) { 
                    foundCalendar = createCalendar();
                }
                return foundCalendar;
                
            } catch (GoogleJsonResponseException jsonResponseEx) {
                int responseCode = jsonResponseEx.getStatusCode();
                String responseMessage = jsonResponseEx.getStatusMessage();
                if (responseCode == RESOURCE_NOT_FOUND && 
                        JSON_NOT_FOUND.equalsIgnoreCase(responseMessage)) {
                    GoogleController.resetGoogleIdAndEtag(GoogleService.GOOGLE_CALENDAR);
                    setCalendarId("");
                    initializeCalendar(getCalendarId());
                }
            } 
        }
        return null;
    }

    /**
     * CreateCalendar will invoke methods in CalendarHandler to perform the 
     * actual creation of Calendar object as well as to send the request to 
     * Google. When Google returns the Calendar object, the unique Calendar ID
     * will be stored into ChirpTask settings, config.properties.
     * @return 
     *      The created Calendar object from Google
     * @throws UnknownHostException If Google host is not reachable
     * @throws IOException If bad response or transmission error
     */
    private Calendar createCalendar() throws UnknownHostException, IOException {
        Calendar newCalendar = CalendarHandler.addCalendar(DEFAULT_CALENDAR);
        setCalendarId(newCalendar);
        String calendarId = getCalendarId();
        IdHandler.saveIdToSettings(calendarId);
        return newCalendar;
    }
    
    private void setCalendarId(Calendar calendar) throws NullPointerException {
        if (calendar == null) {
            throw new NullPointerException();
        }
        
        String calendarId = calendar.getId();
        setCalendarId(calendarId);
    }
    
    static String getCalendarId() {
        return _calendarId;
    }
    
    /**
     * Retrieves an instance of all Events from the given Google Calendar ID
     * @return A List<Event> object if found. 
     *          If calendarID is null, returns an empty List<Event>
     * @throws UnknownHostException If cannot reach the host
     * @throws IOException If invalid response or transmission error
     */
    List<Event> getEvents() throws UnknownHostException, IOException {
        if (_calendarId == null) {
            return new ArrayList<Event>();
        }
        
        List<Event> events = CalendarHandler.retrieveEventsById(_calendarId);
        return events;
    }
    
    /**
     * Adds an Event into Google Calendar
     * @param taskTitle The task description itself
     * @param startTime The start time in a Date object
     * @param endTime The end time in a Date object
     * @return The added Google Event if successful, null if null parameter
     * @throws UnknownHostException If cannot reach the host
     * @throws IOException If invalid repsonse or transmission error
     */
    Event addTimedTask(String taskTitle, Date startTime, Date endTime) 
                                throws UnknownHostException, IOException {
        if (taskTitle == null || startTime == null || endTime == null) {
            return null;
        }
        
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
        if (timedTask == null) {
            return null;
        }
        
        String calendarId = getCalendarId();
        Event insertedEvent = CalendarHandler.insertToCalendar(calendarId, 
                timedTask);
        return insertedEvent;
    }
    
    /**
     * Deletes the Event from Google Calendar with the Event ID
     * @param eventId The Google Calendar's Event ID
     * @return true if delete is successful, false otherwise
     */
    boolean deleteEvent(String eventId) {
        if (eventId == null) {
            return false;
        }
        
        boolean isDeleted = false;
        isDeleted = CalendarHandler.deleteEvent(_calendarId, eventId);
        return isDeleted;
    }

    static com.google.api.services.calendar.Calendar getCalendarClient() {
        return _calendarClient;
    }
    
}

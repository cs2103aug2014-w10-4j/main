//@author A0111840W
package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;

/**
 * CalendarController is the main controller that interacts with Google 
 * Calendar. It uses the Google Calendar v3 API to do such operations. 
 */
public class CalendarController {
    private final String DEFAULT_CALENDAR = "ChirpTaskv0.1";
    
    /** Global instance of the TasksId file. */
    private static final File TIMEDTASK_CALENDAR_ID_STORE_FILE = new File(
            "credentials/googlecalendar/ChirpTaskTimedTaskCalendarID.txt");
    
    /**
     * Global instance of the Google Calendar Service Client. Calendar
     * calendarClient; is the main object connected to the Google Calendar API.
     */
    private static com.google.api.services.calendar.Calendar _calendarClient;
    
    /** Global instance of the working Google Calendar */
    private static Calendar _workingCalendar;
    
    /** Global instance of the working Google Calendar ID */
    private static String _calendarId;


    // Constructor
    CalendarController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeHostFiles();
        initializeCalendarClient(httpTransport, jsonFactory, credential,
                applicationName);
        initializeWorkingCalendar();
    }
    
    private void initializeHostFiles() {
        try {
            TIMEDTASK_CALENDAR_ID_STORE_FILE.getParentFile().mkdirs();
            TIMEDTASK_CALENDAR_ID_STORE_FILE.createNewFile();
        } catch (IOException ioError) {

        }
    }

    private void initializeCalendarClient(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        // initialize the Google Calendar Service Client
        _calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }
    
    private void initializeWorkingCalendar() {
        String timedTaskCalendarId = retrieveId();
        Calendar retrievedCalendar = retrieveCalendar(timedTaskCalendarId);
        setWorkingCalendar(retrievedCalendar);
    }
    
    private String retrieveId() {
        String workingListId = retrieveIdFromFile();
        setCalendarId(workingListId);
        return workingListId;
    }

    private String retrieveIdFromFile() {
        String retrievedId = IdHandler.getIdFromFile(TIMEDTASK_CALENDAR_ID_STORE_FILE);
        return retrievedId;
    }

    private void setCalendarId(String newId) {
        _calendarId = newId;
    }

    private Calendar retrieveCalendar(String calendarId) {
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
                return null;
            } catch (IOException ioError) {
                ioError.printStackTrace();
            } 
        }
        return null;
    }

    private void setWorkingCalendar(Calendar calendar) {
        _workingCalendar = calendar;
    }

    private Calendar createCalendar() throws UnknownHostException, IOException {
        Calendar newCalendar = CalendarHandler.addCalendar(DEFAULT_CALENDAR);
        setCalendarId(newCalendar);
        String calendarId = getCalendarId();
        IdHandler.saveIdToFile(TIMEDTASK_CALENDAR_ID_STORE_FILE, calendarId);
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
        Event addedEvent = insertEvent(newTimedTask);
        return addedEvent;
    }
    
    private Event insertEvent(Event timedTask)
                                throws UnknownHostException, IOException {
        String calendarId = getCalendarId();
        Event insertedEvent = CalendarHandler.insertToCalendar(calendarId, timedTask);
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

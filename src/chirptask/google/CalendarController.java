//@author A0111840W
package chirptask.google;

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.model.CalendarList;

/**
 * CalendarController is the main controller that interacts with Google 
 * Calendar. It uses the Google Calendar v3 API to do such operations. 
 * CalendarController has a helper class, CalendarViewer.
 * 
 * CalendarViewer is a helper class that is often called to help perform 
 * retrieval of the Calendar's statuses/events/information etc.
 */
public class CalendarController {
    /**
     * Global instance of the Google Calendar Service Client. Calendar
     * calendarClient; is the main object connected to the Google Calendar API.
     */
    private static com.google.api.services.calendar.Calendar calendarClient;

    private final String DEFAULT_CALENDAR = "@ChirpyChirpy";

    // Constructor
    CalendarController(HttpTransport httpTransport, JsonFactory jsonFactory,
            Credential credential, String applicationName) {
        initializeCalendar(httpTransport, jsonFactory, credential,
                applicationName);
    }

    private void initializeCalendar(HttpTransport httpTransport,
            JsonFactory jsonFactory, Credential credential,
            String applicationName) {
        // initialize the Google Calendar Service Client
        calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, jsonFactory, credential).setApplicationName(
                applicationName).build();
    }

    public void showCalendars() throws IOException {
        CalendarList calendarList = calendarClient.calendarList().list()
                .execute();
        CalendarViewer.header("Show All Calendars");
        CalendarViewer.display(calendarList);
    }

}

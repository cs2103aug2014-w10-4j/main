package chirptask.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;


public class CalendarController {
    /** Global instance of the Google Calendar Service Client. */
    private static com.google.api.services.calendar.Calendar calendarClient;

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

}

//@author A0111840W
package chirptask.google;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.TasksScopes;

/**
 * GoogleAuthorizer provides a static method to authorize ChirpTask to perform
 * the Google Service calls on behalf of the user by authenticating via
 * OAuth2.0.
 * 
 * It also sets the specific scopes that ChirpTask requires. The user will be
 * directed to their browser to grant ChirpTask access to the specified scopes.
 * 
 * The current scopes to be granted are: 1) Google Calendar 2) Google Tasks
 */
public class GoogleAuthorizer {

    /** Authorizes the installed application to access user's protected data. */
    static Credential authorize() throws Exception {
        List<String> googleScopes = new ArrayList<String>();

        String chirpUser = "ChirpUser";

        FileDataStoreFactory dataStoreFactory = GoogleController._dataStoreFactory;
        HttpTransport httpTransport = GoogleController._httpTransport;
        JsonFactory jsonFactory = GoogleController.JSON_FACTORY;

        // set up FileInputStream for GoogleClientSecrets.load(.., ..) method
        FileInputStream inputStream = new FileInputStream(
                "resources/client_secrets.json");

        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory, new InputStreamReader(inputStream));

        // exit if client secrets is default / error
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret()
                        .startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from "
                    + "https://code.google.com/apis/console/?api=calendar "
                    + "into " + "resources/client_secrets.json");
            System.exit(1);
        }

        // set up scopes for Google Calendar and Google Tasks
        googleScopes.add(CalendarScopes.CALENDAR);
        googleScopes.add(TasksScopes.TASKS);

        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, googleScopes)
                .setDataStoreFactory(dataStoreFactory).build();

        // authorize
        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize(chirpUser);
    }
}

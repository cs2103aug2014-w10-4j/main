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

public class GoogleAuthorizer {

	/** Authorizes the installed application to access user's protected data. */
	static Credential authorize() throws Exception {
		List<String> _googleScopes = new ArrayList<String>();

		String _chirpUser = "ChirpUser";

		FileDataStoreFactory _dataStoreFactory = GoogleController.dataStoreFactory;
		HttpTransport _httpTransport = GoogleController.httpTransport;
		JsonFactory _jsonFactory = GoogleController.JSON_FACTORY;

		// set up FileInputStream for GoogleClientSecrets.load(.., ..) method
		FileInputStream _inputStream = new FileInputStream(
				"resources/client_secrets.json");

		// load client secrets
		GoogleClientSecrets _clientSecrets = GoogleClientSecrets.load(
				_jsonFactory, new InputStreamReader(_inputStream));

		// exit if client secrets is default / error
		if (_clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| _clientSecrets.getDetails().getClientSecret()
						.startsWith("Enter ")) {
			System.out.println("Enter Client ID and Secret from "
					+ "https://code.google.com/apis/console/?api=calendar "
					+ "into " + "resources/client_secrets.json");
			System.exit(1);
		}

		// set up scopes for Google Calendar and Google Tasks
		_googleScopes.add(CalendarScopes.CALENDAR);
		_googleScopes.add(TasksScopes.TASKS);

		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				_httpTransport, _jsonFactory, _clientSecrets, _googleScopes)
				.setDataStoreFactory(_dataStoreFactory).build();

		// authorize
		return new AuthorizationCodeInstalledApp(flow,
				new LocalServerReceiver()).authorize(_chirpUser);
	}
}

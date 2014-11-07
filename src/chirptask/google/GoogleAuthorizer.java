//@author A0111840W
package chirptask.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
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

    /** Authorizes ChirpTask to access user's Google services. */
    static Credential authorize() throws IOException {
        String credentialUser = getCredentialUser();
        String oAuthClientId = getOAuthClientId();
        String oAuthClientSecret = getOAuthClientSecret();
        List<String> googleScopes = getGoogleScopesList();

        // Set up Google authorization code flow
        GoogleAuthorizationCodeFlow codeFlow = getAuthorizationCodeFlow(
                                            oAuthClientId,
                                            oAuthClientSecret,
                                            googleScopes);
        // Authorize with Google using OAuth
        Credential accessToken = authorizeUsingOAuth(codeFlow, credentialUser);
        
        return accessToken;
    }
    
    /**
     * Set up and return a list of scopes that contains scopes for
     * Google Tasks and Google Calendar.
     * @return List<String> googleScopes
     */
    private static List<String> getGoogleScopesList() {
        List<String> googleScopes = new ArrayList<String>();
        googleScopes.add(CalendarScopes.CALENDAR);
        googleScopes.add(TasksScopes.TASKS);
        return googleScopes;
    }
    
    private static String getOAuthClientId() {
        String oAuthClientId = 
            "157073781842-d8dlmu4d07othjlqegcv7d1pdajso5gv.apps.googleusercontent.com";
        return oAuthClientId;
    }
    
    private static String getOAuthClientSecret() {
        String oAuthClientSecret = "Pjd5SdWv-RSE5xKK7TOUQYzK";
        return oAuthClientSecret;
    }
    
    private static String getCredentialUser() {
        String credentialUser = "ChirpUser";
        return credentialUser;
    }
    
    private static GoogleAuthorizationCodeFlow getAuthorizationCodeFlow(
                                                    String oAuthClientId, 
                                                    String oAuthClientSecret,
                                                    List<String> googleScopes)
                                                    throws IOException {
        if (oAuthClientId == null || oAuthClientSecret == null || 
                googleScopes == null) {
            return null;
        }
        
        FileDataStoreFactory dataStoreFactory = GoogleController._dataStoreFactory;
        HttpTransport httpTransport = GoogleController._httpTransport;
        JsonFactory jsonFactory = GoogleController.JSON_FACTORY;
        
        GoogleAuthorizationCodeFlow codeFlow = 
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, 
                                                        jsonFactory, 
                                                        oAuthClientId, 
                                                        oAuthClientSecret, 
                                                        googleScopes)
                                                .setDataStoreFactory(
                                                        dataStoreFactory)
                                                .build();
        return codeFlow;
    }
    
    private static Credential authorizeUsingOAuth(
                                        GoogleAuthorizationCodeFlow codeFlow,
                                        String credentialUser) 
                                                throws IOException{
        if (codeFlow == null) { // if credentialUser null, non-persistent mode.
            return null;
        }
        
        VerificationCodeReceiver codeReceiver = new LocalServerReceiver();
        AuthorizationCodeInstalledApp authorizer = 
                                            new AuthorizationCodeInstalledApp(
                                                codeFlow,
                                                codeReceiver);

        Credential accessToken = authorizer.authorize(credentialUser);
        
        return accessToken;
    }
}

package chirptask.google;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.tasks.model.Task;

public class GoogleController {
	private static final String APPLICATION_NAME = "ChirpTask-GoogleIntegration/0.1";

	private static final File DATA_STORE_DIR = new File(
			"credentials/google_oauth_credential");

	/**
	 * Global instance of the DataStoreFactory. The best practice is to make it
	 * a single globally shared instance across your application
	 */
	static FileDataStoreFactory dataStoreFactory;

	/** Global instance of the HTTP transport. */
	static HttpTransport httpTransport;

	/** Global instance of the JSON factory. */
	static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the Credential. */
	private static Credential credential;

	/** Global instance of the CalendarController. */
	static CalendarController calendarController;

	/** Global instance of the TasksController. */
	static TasksController tasksController;

	public GoogleController() {
		initializeComponents();
	}

	private void initializeComponents() {
		try {
			// initialize the transport
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			// initialize the data store factory
			dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
			// initialize the credential component
			credential = GoogleAuthorizer.authorize();
			// initialize the Calendar Controller
			calendarController = new CalendarController(httpTransport,
					JSON_FACTORY, credential, APPLICATION_NAME);
			// initialize the Tasks Controller
			tasksController = new TasksController(httpTransport, JSON_FACTORY,
					credential, APPLICATION_NAME);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// test if the service is available and connected
	public static void main(String[] args) {
		GoogleController _gController = new GoogleController();
		try {
			/**
			 * Google Tasks
			 */
			// Test creation of task
			Task tempTask = _gController.addTask("Hello World!");
			_gController.showTask(tempTask.getId());

			// Test adding due date
			DateTime _dueDate = DateTimeHandler.getDateTime("2014-09-29");
			tempTask = TasksHandler.addDueDate(tempTask, _dueDate);
			tempTask = tasksController.updateTask(tempTask);
			_gController.showTask(tempTask.getId());

			// Test setting complete
			tempTask = TasksHandler.setCompleted(tempTask);
			tempTask = tasksController.updateTask(tempTask);
			_gController.showTask(tempTask.getId());

			// Show all tasks in list
			// _gController.showTasks();

			// Show all hidden tasks in list
			// _gController.showHiddenTasks();

			// Show all undone tasks in list
			_gController.showUndoneTasks();

			// Clean up
			_gController.deleteTask(tempTask.getId());

			/**
			 * Google Calendar
			 */
			// _gController.showCalendars();
		} catch (IOException ioE) {

		}
	}

	private void showCalendars() throws IOException {
		calendarController.showCalendars();
	}

	private void deleteTask(String _taskId) {
		try {
			tasksController.deleteTask(_taskId);
		} catch (IOException e) {

		}
	}

	private void showTask(String _taskId) {
		try {
			tasksController.showTask(_taskId);
		} catch (IOException e) {

		}
	}

	private void showTasks() throws IOException {
		tasksController.showTasks();
	}

	private void showHiddenTasks() throws IOException {
		tasksController.showHiddenTasks();
	}

	private void showUndoneTasks() throws IOException {
		tasksController.showUndoneTasks();
	}

	private Task addTask(String taskTitle) throws IOException {
		Task _addedTask = tasksController.addTask(taskTitle);
		return _addedTask;
	}

}

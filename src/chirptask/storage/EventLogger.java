package chirptask.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import chirptask.common.Messages;
import chirptask.common.Settings;

public class EventLogger implements Storage {
	// @author A0111889W
	PrintWriter fileWriter;

	// Suggestion: Convert to output stream instead
	public EventLogger() {
		try {
			fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(Settings.EVENT_LOG_FILENAME), true)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		fileWriter.close();
	}

	@Override
	public boolean storeNewTask(Task T) {
		try {
			fileWriter
					.println(String.format(Messages.LOG_MESSAGE_ADD_TASK,
							new Date(), T.getDate(), T.getTaskId(),
							T.getDescription()));
			fileWriter.flush();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Task removeTask(Task T) {
		try {
			fileWriter
					.println(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
							new Date(), T.getDate(), T.getTaskId(),
							T.getDescription()));
			fileWriter.flush();
			return T;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean modifyTask(Task T) {
		try {
			fileWriter
					.println(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
							new Date(), T.getDate(), T.getTaskId(),
							T.getDescription()));
			fileWriter.flush();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Task getTask(int taskId) {
		fileWriter.println(String.format(Messages.LOG_MESSAGE_GET_TASK,
				new Date(), taskId));
		fileWriter.flush();
		return null;
	}

	@Override
	public ArrayList<Task> getAllTasks() {
		fileWriter.println(String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
				new Date()));
		fileWriter.flush();
		return null;
	}
	
	public void logError(String error) {
		fileWriter.println(String.format(Messages.ERROR, new Date(),
				error));
		fileWriter.flush();
	}

}
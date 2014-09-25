package chirptask.storage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import chirptask.settings.Settings;
import chirptask.settings.Messages;

public class EventLogger implements Storage {
	PrintWriter fileWriter;

	public EventLogger() {
		try {
			fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(Settings.eventLogFileName),true)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close(){
		fileWriter.close();
	}

	@Override
	public boolean storeNewTask(Task T) {
		fileWriter.println(String.format(Messages.MESSAGE_ADD_TASK_TO_LOG,new Date(),T.getTaskId(),T.getDate(),T.getDescription()));
		fileWriter.flush();
		return true;
	}

	@Override
	public Task removeTask(Task T) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean modifyTask(Task T) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Task getTask(int taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Task> getAllTasks() {
		// TODO Auto-generated method stub
		return null;
	}

}

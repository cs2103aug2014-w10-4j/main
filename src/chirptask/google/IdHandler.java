//@author A0111840W
package chirptask.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import chirptask.storage.StorageHandler;

/**
 * IdHandler class provides static methods to read the relevant ID from file.
 * 
 * It is to be used by TasksController to retrieve the TaskList's ID which is
 * maintained by ChirpTask and stored on the host's machine.
 * 
 * It is also to be used by CalendarController to retrieve the Calendars' ID 
 * which is maintained by ChirpTask and stored on the host's machine. 
 */
class IdHandler {

	static String getIdFromFile(File idFile) {
		String id = null;

		try (BufferedReader idFileReader = new BufferedReader(new FileReader(
				idFile))) {
			id = idFileReader.readLine();
		} catch (FileNotFoundException fileNotFoundError) {
		    return null;
		} catch (IOException accessFileError) {
		    return null;
		}

		return id;
	}

	static void saveIdToFile(File idFile, String id) {
		try (BufferedWriter idFileWriter = new BufferedWriter(new FileWriter(
				idFile))) {
			idFileWriter.write(id);
		} catch (FileNotFoundException fileNotFoundError) {
            StorageHandler.logError("Failed to save ID to File");
		} catch (IOException accessFileError) {
		    StorageHandler.logError("Failed to save ID to File");
		}
	}

}

//@author A0111840W
package chirptask.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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

	static String getIdFromFile(File _idFile) {
		String _id = null;

		try (BufferedReader idFileReader = new BufferedReader(new FileReader(
				_idFile))) {
			_id = idFileReader.readLine();
		} catch (FileNotFoundException fileNotFoundError) {

		} catch (IOException accessFileError) {

		}

		return _id;
	}

	static void saveIdToFile(File _idFile, String _id) {
		try (BufferedWriter idFileWriter = new BufferedWriter(new FileWriter(
				_idFile))) {
			idFileWriter.write(_id);
		} catch (FileNotFoundException fileNotFoundError) {

		} catch (IOException accessFileError) {

		}
	}

}

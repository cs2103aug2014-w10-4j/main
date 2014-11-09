//@author A0111889W
package chirptask.testing;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.storage.DeadlineTask;
import chirptask.storage.EventLogger;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitEventLoggerAtd {

    Settings settings = new Settings();
    Calendar today = Calendar.getInstance();
    Calendar tomorrow = Calendar.getInstance();

    EventLogger logger = EventLogger.getInstance();
    PipedInputStream pin = new PipedInputStream();
    BufferedReader in = new BufferedReader(new InputStreamReader(pin));
    PipedOutputStream pout;
    PrintStream c;

    @Before
    public void setupTest() throws IOException {
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        pout = new PipedOutputStream(pin);
        c = new PrintStream(pout);

        EventLogger.setStream(c);
    }

    /*
     * Tests the EventLogger Component
     * 
     * Not much heuristics to be applied to eventlog testing, code is pretty
     * straightforward, no boundary cases, no equivalence partition either.
     */

    @Test
    public void deadlineTaskTesting() throws ParseException, IOException {
        DeadlineTask dt = new DeadlineTask(111889,
                "Deadline Task JUnit Testing", tomorrow);

        // Tests if logging is accurate for deadline tasks operations
        // More importantly, tests if time and descriptions are accurately
        // logged.
        assertTrue(logger.storeNewTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertEquals(dt, logger.removeTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertTrue(logger.modifyTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertNull(logger.getTask(dt.getTaskId()));
        assertEquals(String.format(Messages.LOG_MESSAGE_GET_TASK,
                today.getTime(), dt.getTaskId()), in.readLine());

        assertNull(logger.getAllTasks());
        assertEquals(
                String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                        today.getTime(), today.getTime()), in.readLine());

        logger.logError(dt.getDescription());
        assertEquals(
                String.format(Messages.ERROR, today.getTime(),
                        dt.getDescription()), in.readLine());

        logger.close();
    }

    @Test
    public void taskTesting() throws ParseException, IOException {
        Task dt = new Task(111840, "Floating Task JUnit Testing");

        // Tests if logging is accurate for floating tasks operations
        assertTrue(logger.storeNewTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertEquals(dt, logger.removeTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertTrue(logger.modifyTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertNull(logger.getTask(dt.getTaskId()));
        assertEquals(String.format(Messages.LOG_MESSAGE_GET_TASK,
                today.getTime(), dt.getTaskId()), in.readLine());

        assertNull(logger.getAllTasks());
        assertEquals(
                String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                        today.getTime(), today.getTime()), in.readLine());

        logger.logError(dt.getDescription());

        assertEquals(
                String.format(Messages.ERROR, today.getTime(),
                        dt.getDescription()), in.readLine());

        logger.close();
    }

    @Test
    public void timedTaskTesting() throws ParseException, IOException {
        // Tests if logging is accurate for timed tasks operations
        TimedTask dt = new TimedTask(1337, "Timed Task JUnit Testing",
                tomorrow, tomorrow);

        assertTrue(logger.storeNewTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertEquals(dt, logger.removeTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertTrue(logger.modifyTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertNull(logger.getTask(dt.getTaskId()));
        assertEquals(String.format(Messages.LOG_MESSAGE_GET_TASK,
                today.getTime(), dt.getTaskId()), in.readLine());

        assertNull(logger.getAllTasks());
        assertEquals(
                String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                        today.getTime(), today.getTime()), in.readLine());

        logger.logError(dt.getDescription());

        assertEquals(
                String.format(Messages.ERROR, today.getTime(),
                        dt.getDescription()), in.readLine());

        logger.close();
    }

    @Test
    public void timedTaskAsTaskTesting() throws ParseException, IOException {
        Task dt = new TimedTask(1337, "Timed Task as Task JUnit Testing",
                tomorrow, today);
        // Tests if logging is accurate for timed tasks stored in task
        // operations
        assertTrue(logger.storeNewTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertEquals(dt, logger.removeTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertTrue(logger.modifyTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertNull(logger.getTask(dt.getTaskId()));
        assertEquals(String.format(Messages.LOG_MESSAGE_GET_TASK,
                today.getTime(), dt.getTaskId()), in.readLine());

        assertNull(logger.getAllTasks());
        assertEquals(
                String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                        today.getTime(), today.getTime()), in.readLine());

        logger.logError(dt.getDescription());

        assertEquals(
                String.format(Messages.ERROR, today.getTime(),
                        dt.getDescription()), in.readLine());

        logger.close();
    }

    @Test
    public void deadlineTaskAsTaskTesting() throws ParseException, IOException {

        Task dt = new DeadlineTask(1337, "Deadline Task As Task JUnit Testing",
                tomorrow);
        // Tests if logging is accurate for deadline tasks stored as floating
        // task operations
        assertTrue(logger.storeNewTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_ADD_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertEquals(dt, logger.removeTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_REMOVE_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertTrue(logger.modifyTask(dt));
        assertEquals(String.format(Messages.LOG_MESSAGE_MODIFY_TASK,
                today.getTime(), dt.getDate().getTime()), in.readLine());

        assertNull(logger.getTask(dt.getTaskId()));
        assertEquals(String.format(Messages.LOG_MESSAGE_GET_TASK,
                today.getTime(), dt.getTaskId()), in.readLine());

        assertNull(logger.getAllTasks());
        assertEquals(
                String.format(Messages.LOG_MESSAGE_GET_ALL_TASKS,
                        today.getTime(), today.getTime()), in.readLine());

        logger.logError(dt.getDescription());

        assertEquals(
                String.format(Messages.ERROR, today.getTime(),
                        dt.getDescription()), in.readLine());

        logger.close();
    }

}

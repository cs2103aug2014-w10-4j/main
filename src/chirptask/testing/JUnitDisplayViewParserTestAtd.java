//@author A0111889W
package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import chirptask.logic.DisplayView;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitDisplayViewParserTestAtd {

    /*
     * Tests only two methods under DisplayView.
     * These two methods are used by GUI to parse certain object into strings.
     *
     * ConvertDateToString method and ConvertTaskDateToDuration method
     */
    @Test
    public void testConvertDateToString() {

        Calendar date = Calendar.getInstance();

        // Valid inputs
        // There are no invalid inputs as defensive coding has handled that
        date.set(1991, 7, 27);
        assertEquals("27/08/1991", DisplayView.convertDateToString(date));

        date.set(1991, 0, 1);
        assertEquals("01/01/1991", DisplayView.convertDateToString(date));

        date.set(2000, 0, 1);
        assertEquals("01/01/2000", DisplayView.convertDateToString(date));

    }

    @Test
    public void testConvertTaskDateToDurationString() {

        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        // test for valid input
        date.set(2014, 9, 22, 21, 30, 00);
        Task floating = new Task(0, "Floating Task");
        assertEquals("",
                DisplayView.convertTaskDateToDurationString(floating));

        // checks if method verifies type of task as deadline task
        floating = new DeadlineTask(1, "Deadline Task as Floating Task", date);
        assertEquals("due by 21:30",
                DisplayView.convertTaskDateToDurationString(floating));

        DeadlineTask deadline = new DeadlineTask(2, "Deadline Task", date);
        assertEquals("due by 21:30",
                DisplayView.convertTaskDateToDurationString(deadline));

        date2.set(2014, 9, 22, 23, 30, 00);

        // checks if method verifies type of task as timed task
        floating = new TimedTask(3, "Timed Task as Floating Task", date, date2);
        assertEquals("21:30 to 23:30",
                DisplayView.convertTaskDateToDurationString(floating));

        TimedTask timed = new TimedTask(4, "Floating Task", date, date2);
        assertEquals("21:30 to 23:30",
                DisplayView.convertTaskDateToDurationString(timed));
    }

}

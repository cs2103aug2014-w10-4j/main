//@author A0111889W
package chirptask.testing;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class JUnitTaskSortingAtd {

    @Test
    public void testSortingOfTask() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        List<Task> taskList = new ArrayList<Task>();

        Task task = new Task(0, "Floating Task JUnit Testing");
        DeadlineTask deadlineTask = new DeadlineTask(1,
                "Deadline Task JUnit Testing", today);
        TimedTask timedTask = new TimedTask(2, "Timed Task JUnit Testing",
                today, today);

        taskList.add(timedTask);
        taskList.add(deadlineTask);
        taskList.add(task);

        // before sorting
        assertEquals(timedTask, taskList.get(0));
        assertEquals(deadlineTask, taskList.get(1));
        assertEquals(task, taskList.get(2));

        Collections.sort(taskList);

        // This tests that floating task < deadline < timed
        // after sorting
        assertEquals(task, taskList.get(0));
        assertEquals(deadlineTask, taskList.get(1));
        assertEquals(timedTask, taskList.get(2));

        // Now tests that they are sorted by time accurately
        // Currently deadline is at index 1 and timedtask at 2
        // Set deadline to 13:30hours and it should be at index 2 instead
        // This tests that timed task is sorted by start time
        today.set(Calendar.HOUR_OF_DAY, 12);
        today.set(Calendar.MINUTE, 30);
        timedTask.setStartTime(today);

        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 13);
        today.set(Calendar.MINUTE, 30);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        timedTask.setEndTime(today);
        deadlineTask.setDate(today);

        Collections.sort(taskList);

        assertEquals(task, taskList.get(0));
        assertEquals(timedTask, taskList.get(1));
        assertEquals(deadlineTask, taskList.get(2));

        // Now test that if they are have the same time and type they are sorted
        // by description
        Task floatingTask2 = new Task(3, "A Floating Task JUnit Testing");
        TimedTask timedTask2 = new TimedTask(4, "A Timed Task JUnit Testing",
                today, today);
        DeadlineTask deadlineTask2 = new DeadlineTask(5,
                "A Deadline Task JUnit Testing", today);

        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 12);
        today.set(Calendar.MINUTE, 30);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        timedTask2.setStartTime(today);

        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 13);
        today.set(Calendar.MINUTE, 30);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        timedTask2.setEndTime(today);

        taskList.add(timedTask2);
        taskList.add(floatingTask2);
        taskList.add(deadlineTask2);

        // before sort
        assertEquals(task, taskList.get(0));
        assertEquals(timedTask, taskList.get(1));
        assertEquals(deadlineTask, taskList.get(2));
        assertEquals(timedTask2, taskList.get(3));
        assertEquals(floatingTask2, taskList.get(4));
        assertEquals(deadlineTask2, taskList.get(5));

        Collections.sort(taskList);

        // after sort
        assertEquals(floatingTask2, taskList.get(0));
        assertEquals(task, taskList.get(1));
        assertEquals(timedTask2, taskList.get(2));
        assertEquals(timedTask, taskList.get(3));
        assertEquals(deadlineTask2, taskList.get(4));
        assertEquals(deadlineTask, taskList.get(5));
    }
}

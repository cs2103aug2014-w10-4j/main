package chirptask.testing;

import static org.junit.Assert.*;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import chirptask.logic.DisplayView;
import chirptask.logic.FilterTasks;
import chirptask.logic.Logic;
import chirptask.storage.DeadlineTask;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

//@author A0111930W
public class JUnitLogicAtd {

    @Test
    public void commandtest() {
        // Integration testing with UI and storage
        MainGui2 _mainGui = new MainGui2();
        Logic _logic = new Logic(_mainGui);
        _logic.useTestLocalStorage();

        // Adding a normal floating task
        _logic.retrieveInputFromUI("add normal floating task");
        // Compare task list if task is there means successfully added to local
        // storage
        assertEquals(FilterTasks.getFilteredList().get(0).getDescription(),
                "normal floating task");

        // Delete that task
        _logic.retrieveInputFromUI("delete 1");
        assertEquals(FilterTasks.getFilteredList().size(), 0);

        // adding a timed task
        _logic.retrieveInputFromUI("addt eating with mum from 12a to 12p 08/11");
        assertEquals(FilterTasks.getFilteredList().get(0).getDescription(),
                "eating with mum");

        // add deadline task
        _logic.retrieveInputFromUI("addd go out with parents on 17/11");
        assertEquals(FilterTasks.getFilteredList().get(1).getDescription(),
                "go out with parents by 23:59 17/11");
        
        // Use a invalid command 
        // Expected task to be in the list 2, since this is a wrong type
        _logic.retrieveInputFromUI("adddd go out on 17/11");
        assertEquals(FilterTasks.getFilteredList().size(), 2);

        // delete all task
        _logic.retrieveInputFromUI("delete 1-2");
        assertEquals(FilterTasks.getFilteredList().size(), 0);

        // Undo command
        _logic.retrieveInputFromUI("undo");
        assertEquals(FilterTasks.getFilteredList().size(), 2);

        // Undo again
        _logic.retrieveInputFromUI("undo");
        assertEquals(FilterTasks.getFilteredList().size(), 0);

        // Undo the tasks again this time task will be added back
        _logic.retrieveInputFromUI("undo");
        assertEquals(FilterTasks.getFilteredList().size(), 2);

        // edit task 1 change desc to dad
        _logic.retrieveInputFromUI("edit 1 eating with dad from 12a to 12p 08/11");
        assertEquals(FilterTasks.getFilteredList().get(0).getDescription(),
                "eating with dad");
        
        // Delete out of range tasks
        // This action will not be execute as it delete tasks that are out of range
        _logic.retrieveInputFromUI("delete 1-3");
        assertEquals(FilterTasks.getFilteredList().size(), 2);
        
        //Done a task
        //Task 1 is done, expected true
        _logic.retrieveInputFromUI("done 1");
        assertEquals(FilterTasks.getFilteredList().get(0).isDone(), true);
        //Undone a task
        //Task 1 is undone, expected false
        _logic.retrieveInputFromUI("undone 1");
        assertEquals(FilterTasks.getFilteredList().get(0).isDone(), false);
        //Done a task out of range
        //Expect none of the tasks to be done since its out of range.
        _logic.retrieveInputFromUI("done 1-3");
        assertEquals(FilterTasks.getFilteredList().get(0).isDone(), false);
        assertEquals(FilterTasks.getFilteredList().get(1).isDone(), false);
        //Undone a task out of range
        //Expect all task to be remain as done none of the task will be undone.
        _logic.retrieveInputFromUI("done 1-2");
        _logic.retrieveInputFromUI("undone 1-3");
        assertEquals(FilterTasks.getFilteredList().get(0).isDone(), true);
        assertEquals(FilterTasks.getFilteredList().get(1).isDone(), true);
        //Delete all task
        _logic.retrieveInputFromUI("delete 1-2");
        //Clear all task, task will be deleted from local storage
        _logic.retrieveInputFromUI("clear");
        assertEquals(FilterTasks.getFilteredList().size(), 0);
    }

    @Test
    public void DisplayViewTest() {

        // Test method convertTaskDateToString

        // test floating task
        Task test1 = new Task();
        test1.setType("floating");
        assertEquals("", DisplayView.convertTaskDateToDurationString(test1));

        // test Deadline task
        Calendar date = Calendar.getInstance();
        date.set(2014, 9, 22);
        date.set(Calendar.HOUR_OF_DAY, 23);
        date.set(Calendar.MINUTE, 59);
        Task test2 = new DeadlineTask(1, "test", date);
        assertEquals("due by 23:59",
                DisplayView.convertTaskDateToDurationString(test2));

        // test timedtask
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        startTime.set(2014, 9, 22, 12, 0);
        endTime.set(2014, 9, 22, 14, 0);
        Task timed = new TimedTask(2, "test2", startTime, endTime);
        assertEquals("12:00 to 14:00",
                DisplayView.convertTaskDateToDurationString(timed));

        // Boundary -- test a type of task that is not the 3 type
        // this should crash the program as this will never happen
        Task troll = new Task();
        troll.setType("troll");
        // DisplayView.convertTaskDateToString(troll);

    }

    @Test
    public void testFilterTasks() {
        // test Method hideDeleted

        // Create a list of all deleted task
        // hideDeleted should return an empty list
        List<Task> list = new ArrayList<Task>();
        List<Task> expected = new ArrayList<Task>();
        Task A = new Task();
        Task B = new Task();
        Task C = new Task();
        A.setDeleted(true);
        B.setDeleted(true);
        C.setDeleted(true);

        list.add(A);
        list.add(B);
        list.add(C);

        list = FilterTasks.hideDeleted(list);
        assertEquals(expected, list);

        // Set B to be deleted
        // hideDeleted should return a list with Task B
        B.setDeleted(false);
        list.add(A);
        list.add(B);
        list.add(C);
        expected.add(B);

        list = FilterTasks.hideDeleted(list);
        assertEquals(expected, list);

        // setDeleted false for all Tasks
        // should return a list of all task
        A.setDeleted(false);
        C.setDeleted(false);
        list.add(A);
        list.add(C);
        expected.add(A);
        expected.add(C);

        list = FilterTasks.hideDeleted(list);
        assertEquals(expected, list);
        // End of test for hideDeleted

        // test for method processFilterDateParam

        // proper date format DD/MM
        String date1 = "22/10";
        Calendar testParam = Calendar.getInstance();
        testParam.set(testParam.get(Calendar.YEAR), 9, 22);

        Calendar expected1 = null;
        try {
            expected1 = FilterTasks.processFilterDateParam(date1);
        } catch (InvalidParameterException invalidParameterException) {

        }

        assertNotNull(expected1);
        assertEquals(testParam.get(Calendar.YEAR), expected1.get(Calendar.YEAR));
        assertEquals(testParam.get(Calendar.MONTH),
                expected1.get(Calendar.MONTH));
        assertEquals(testParam.get(Calendar.DAY_OF_MONTH),
                expected1.get(Calendar.DAY_OF_MONTH));

        // wrong date format MM-DD
        // Return a current Calendar object
        String date2 = "10-22";
        Calendar expected2 = null;
        try {
            expected2 = FilterTasks.processFilterDateParam(date2);
        } catch (InvalidParameterException invalidParameterException) {
            assertNotNull(invalidParameterException);
        }

        assertNull(expected2);
        // End of test for processFilterDateParam
    }

}

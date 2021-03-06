//@author A0111889W
package chirptask.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import chirptask.logic.DisplayView;
import chirptask.logic.Logic;

public class JUnitSystemTestingAtd {
    MainGui2 _mainGui;
    Logic _logic;

    @Before
    public void setupTest() {
        _mainGui = new MainGui2();
        _logic = new Logic(_mainGui);
        _logic.useTestLocalStorage();
    }

    @Test
    public void invalidCommands() {

        _logic.retrieveInputFromUI("delete 1-");
        assertEquals("delete 1-", _mainGui._userInput);
        assertEquals(
                "Usage: delete <Task no> | delete <Task no>-<Task no>,<Task no>",
                _mainGui._status);

        _logic.retrieveInputFromUI("logout");
        assertEquals("You are not logged in.", _mainGui._status);

        _logic.retrieveInputFromUI("Invalid");
        assertEquals(
                "Valid commands: add addd addt edit delete done undone display filter clear undo login logout sync",
                _mainGui._status);
    }

    @Test
    public void validCommands() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM");
        int currentTaskListSize = MainGui2._taskIndexToId.size();

        _logic.retrieveInputFromUI("delete 1-" + currentTaskListSize);

        _logic.retrieveInputFromUI("add normal floating task");

        assertEquals("Successfully Added new task normal floating task.",
                _mainGui._status);
        assertEquals(0, _mainGui._categoryList.size());
        assertEquals(0, _mainGui._hashtagList.size());
        assertEquals(
                1,
                _mainGui._taskViewDateMap
                .get(DisplayView.convertDateToString(Calendar
                        .getInstance())).size());

        _logic.retrieveInputFromUI("delete 1");

        assertEquals("Successfully Removed task normal floating task.",
                _mainGui._status);
        assertNull(_mainGui._taskViewDateMap.get(DisplayView
                .convertDateToString(Calendar.getInstance())));

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        _logic.retrieveInputFromUI("addt #Junit @Testing from 10am to 10pm tomorrow");

        assertEquals("Successfully Added new task #Junit @Testing.",
                _mainGui._status);
        assertEquals(1, _mainGui._categoryList.size());
        assertEquals(1, _mainGui._hashtagList.size());
        assertEquals(
                1,
                _mainGui._taskViewDateMap.get(
                        DisplayView.convertDateToString(tomorrow)).size());

        _logic.retrieveInputFromUI("addd #Junit @Testing2 by 12pm today");

        assertEquals("Successfully Added new task #Junit @Testing2 by 12:00.", _mainGui._status);
        assertEquals(2, _mainGui._categoryList.size());
        assertEquals(1, _mainGui._hashtagList.size());
        assertEquals(
                1,
                _mainGui._taskViewDateMap
                .get(DisplayView.convertDateToString(Calendar
                        .getInstance())).size());

        _logic.retrieveInputFromUI("done 1");
        assertEquals("Successfully: Done #Junit @Testing2 by 12:00", _mainGui._status);

        _logic.retrieveInputFromUI("undone 1");
        assertEquals("Successfully Modified task #Junit @Testing2 by 12:00.", _mainGui._status);

        _logic.retrieveInputFromUI("Edit 1 nothing by 1pm today");
        assertEquals(
                "Successfully Modified task nothing by 13:00 "
                        + formatter.format(today.getTime()) + ".",
                        _mainGui._status);

        _logic.retrieveInputFromUI("undo");
        assertEquals("Successfully Modified task #Junit @Testing2 by 12:00.", _mainGui._status);

        _logic.retrieveInputFromUI("display #Junit");
        assertEquals(2, MainGui2._taskIndexToId.size());

        _logic.retrieveInputFromUI("display #Junit @Testing2");
        assertEquals(1, MainGui2._taskIndexToId.size());

        _logic.retrieveInputFromUI("display");
        assertEquals(2, MainGui2._taskIndexToId.size());

        // cleaning up
        _logic.retrieveInputFromUI("delete 1-2");

        assertEquals("Successfully Removed task #Junit @Testing.",
                _mainGui._status);
        assertNull(_mainGui._taskViewDateMap.get(DisplayView
                .convertDateToString(Calendar.getInstance())));

    }
}

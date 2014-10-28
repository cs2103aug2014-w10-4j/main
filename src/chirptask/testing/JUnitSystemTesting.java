package chirptask.testing;

import static org.junit.Assert.*;

import org.junit.Test;

import chirptask.logic.Logic;

public class JUnitSystemTesting {

    @Test
    public void test() {
        MainGui2 _mainGuiStub = new MainGui2();
        Logic _logic = new Logic(_mainGuiStub);

        _logic.retrieveInputFromUI("add 123");

        System.out.println(_mainGuiStub._status);
    }

}

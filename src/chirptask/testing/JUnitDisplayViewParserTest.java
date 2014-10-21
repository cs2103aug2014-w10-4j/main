package chirptask.testing;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Test;

import chirptask.logic.DisplayView;

public class JUnitDisplayViewParserTest {

    @Test
    public void test() {

        Calendar date = Calendar.getInstance();
        date.set(1991, 7, 27);
        assertEquals("27/08/91",DisplayView.convertDateToString(date));
        
        
    }

}

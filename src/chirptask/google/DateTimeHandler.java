package chirptask.google;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;

public class DateTimeHandler {
    static final String DATE_FORMAT = "yyyy-MM-dd";
    static final String DEFAULT_DATE = "2015-12-31";
    
    static final SimpleDateFormat FORMAT_DATE = 
            new SimpleDateFormat(DATE_FORMAT);
    
    static Date getDateFromInput(String _input) {
        Date _newDate = null;
        if (_input == null) {
            _input = DEFAULT_DATE;
        }
        try {
            _newDate = FORMAT_DATE.parse(_input);
        } catch (ParseException parseError) {
            _newDate = getDateFromToday();
        }
        return _newDate;
    }
    
    static DateTime getDateTime() {
        Date _currentDate = getDateFromToday();
        TimeZone _hostTimeZone = getTimeZoneFromHost();
        DateTime _newDateTime = newDateTime(_currentDate, _hostTimeZone);
        return _newDateTime;
    }

    static Date getDateFromToday() {
        Date _newDate = new Date();
        return _newDate;
    }

    static TimeZone getTimeZoneFromHost() {
        TimeZone _hostTimeZone = TimeZone.getDefault();
        return _hostTimeZone;
    }
    
    static DateTime newDateTime(Date _date, TimeZone _timeZone) {
        DateTime _newDateTime = new DateTime(_date, _timeZone);
        return _newDateTime;
    }
    
    static DateTime getDateTime(String _inputDate) {
        Date _dateFromInput = getDateFromInput(_inputDate);
        TimeZone _hostTimeZone = getTimeZoneFromHost();
        DateTime _newDateTime = newDateTime(_dateFromInput, _hostTimeZone);
        return _newDateTime;
    }
    
}

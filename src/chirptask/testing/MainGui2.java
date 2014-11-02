package chirptask.testing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import chirptask.gui.MainGui;
import chirptask.logic.DisplayView;

/*
 * Class for System Testing. Dependency Injection.
 */
//@author A0111889W
public class MainGui2 extends MainGui {

    public String _filter = "";
    public String _userInput = "";
    public String _status = "";
    public List<String> _categoryList = new ArrayList<String>();
    public List<String> _contextList = new ArrayList<String>();
    public SortedMap<String, ArrayList<Integer>> _taskViewDateMap = new TreeMap<>();
    public static List<Integer> _taskIndexToId = new ArrayList<>();

    public String getFilter() {
        return _filter;
    }

    public void setFilterText(String text) {
        _filter = text;
    }

    public void setUserInputText(String text) {
        _userInput = text;
    }

    public String getUserInput() {
        return _userInput;
    }

    public void clearTrendingList() {
        _contextList.clear();
        _categoryList.clear();
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String message) {
        _status = message;
    }

    public void setError(String errorMessage) {
        _status = errorMessage;
    }

    public void addHashtagIntoList(String Context) {
        _contextList.add(Context);
    }

    public void addCategoryIntoList(String Category) {
        _categoryList.add(Category);
    }

    public boolean addNewTaskViewDate(Calendar date) {
        assert date != null;
        String parseDateToString = DisplayView.convertDateToString(date);

        if (_taskViewDateMap.containsKey(parseDateToString)) {
            return false;
        }

        _taskViewDateMap.put(parseDateToString, new ArrayList<Integer>());

        return true;
    }

    public boolean addNewTaskViewToDate(Calendar date, int taskId,
            String description, String time, boolean done) {
        assert date != null && !time.isEmpty() && taskId > -1;

        if (_taskIndexToId.contains(taskId)) {
            return false;
        }

        _taskIndexToId.add(taskId);
        _taskViewDateMap.get(DisplayView.convertDateToString(date)).add(taskId);

        return true;
    }

    public void clearTaskView() {
        _taskViewDateMap.clear();
        _taskIndexToId.clear();
    }

    public static List<Integer> getTaskIndexToId() {
        return _taskIndexToId;
    }

    public void refreshUI() {
        // nothing
    }
}

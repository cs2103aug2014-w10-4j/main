//@author A0111889W
package chirptask.testing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import chirptask.gui.MainGui;
import chirptask.logic.DisplayView;

/**
 * Class for System Testing. Dependency Injection.
 */
public class MainGui2 extends MainGui {
    public String _filter = "";
    public String _onlineStatus = "Offline";
    public String _status = "";
    public String _userInput = "";
    public List<String> _categoryList = new ArrayList<String>();
    public List<String> _hashtagList = new ArrayList<String>();
    public SortedMap<String, ArrayList<Integer>> _taskViewDateMap = new TreeMap<>();
    public static List<Integer> _taskIndexToId = new ArrayList<>();

    @Override
    public String getFilter() {
        return _filter;
    }

    @Override
    public void setFilterText(String text) {
        _filter = text;
    }

    @Override
    public void setUserInputText(String text) {
        _userInput = text;
    }

    @Override
    public String getUserInput() {
        return _userInput;
    }

    @Override
    public void clearTrendingList() {
        _hashtagList.clear();
        _categoryList.clear();
    }

    @Override
    public void setOnlineStatus(String status) {
        _onlineStatus = status;
    }

    public String getStatus() {
        return _status;
    }

    @Override
    public void setStatus(String message) {
        _status = message;
    }

    @Override
    public void setError(String errorMessage) {
        _status = errorMessage;
    }

    @Override
    public void addHashtagIntoList(String hashtag) {
        assert hashtag != null;
        _hashtagList.add(hashtag);
    }

    @Override
    public void addCategoryIntoList(String category) {
        assert category != null;
        _categoryList.add(category);
    }

    @Override
    public boolean addNewTaskViewDate(Calendar date) {
        assert date != null;
        String parseDateToString = DisplayView.convertDateToString(date);

        if (_taskViewDateMap.containsKey(parseDateToString)) {
            return false;
        }

        _taskViewDateMap.put(parseDateToString, new ArrayList<Integer>());

        return true;
    }

    @Override
    public boolean addNewTaskViewToDate(Calendar date, int taskId,
            String description, String time, boolean done) {
        assert (date != null) && (taskId > -1);

        if (_taskIndexToId.contains(taskId)) {
            return false;
        }

        _taskIndexToId.add(taskId);
        _taskViewDateMap.get(DisplayView.convertDateToString(date)).add(taskId);

        return true;
    }

    @Override
    public void clearTaskView() {
        _taskViewDateMap.clear();
        _taskIndexToId.clear();
    }

    public static List<Integer> getTaskIndexToId() {
        return _taskIndexToId;
    }

    @Override
    public void refreshUI() {
        // nothing
    }
}

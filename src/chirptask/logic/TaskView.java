package chirptask.logic;

import java.util.ArrayList;
import java.util.List;

//Contains list of TaskByDates Objects.
public class TaskView {
	
	private List<TasksByDate> listByDate;
	
	public TaskView(){
		listByDate = new ArrayList<TasksByDate>();
	}
	
	public List<TasksByDate> getListByDates(){
		return listByDate;
	}
	
	public void addToTaskView(TasksByDate task){
		listByDate.add(task);
	}
}

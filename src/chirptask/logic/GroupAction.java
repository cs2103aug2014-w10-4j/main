package chirptask.logic;

import java.util.ArrayList;

public class GroupAction {
	private ArrayList<Action> _actions;
	
	public GroupAction() {
		_actions = new ArrayList<Action>();
	}
	
	public GroupAction(ArrayList<Action> list){
		_actions = list;
	}
	
	public ArrayList<Action> getActionList() {	
		return _actions;
	}
	
	public void addAction(Action action) {
		_actions.add(action);
	}

	public void setActionList(ArrayList<Action> list) {
		_actions = list;
	}
	
	public boolean equals(Object o) {
		if (o instanceof GroupAction) {
			GroupAction g = (GroupAction) o;
			return this.getActionList().equals(g.getActionList());
		}
		else 
			return false;
	}
}

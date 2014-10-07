package chirptask.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chirptask.gui.MainGui;
import chirptask.storage.DeadlineTask;
import chirptask.storage.LocalStorage;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

public class InputParser {
	private static final int USER_INPUT_TO_ARRAYLIST = 1;
	private static final String COMMAND_LOGIN = "login";

	private String _userInput;
	private GroupAction _actions;

	public InputParser() {
		_actions = new GroupAction();
	}

	public InputParser(String userInput) {
		_userInput = userInput;
		_actions = processCommand();
	}

	public void receiveInput(String userInput) {
		_userInput = userInput;
		_actions = processCommand();
	}

	private GroupAction processCommand() {
		String commandType = getCommandTypeString();
		String parameter = getParameter();
		switch (commandType) {
		case "add": case "addt": case "addd":
			return processForAdd(commandType, parameter);
		case "edit":
			return processForEdit(parameter);
		case "delete":
			return processForDelete(parameter);
		case "done":
			return processForDone(parameter);
		case "undone":
            return processForUndone(parameter);
		case "undo":
			return processNoTask(commandType);
		case "display":
			return processDisplay(parameter);
		case "login":
			return processLogin();
		default:
			return new GroupAction();
		}
	}

	private GroupAction processDisplay(String parameter) {
		GroupAction actions = new GroupAction();
		if (parameter != null) {
			String[] filters = parameter.trim().split("\\s+|-");
			for (int i = 0; i < filters.length; i++) {
				if (!filters[i].equals("")) {
					Action action = new Action();
					Task task = new Task();
					task.setTaskId(-1);
					task.setDescription(filters[i]);
					action.setCommandType("display");
					action.setTask(task);
					action.setUndo(null);
					actions.addAction(action);
				}
			}
		}
		return actions;
	}


	private GroupAction processNoTask(String command) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		action.setCommandType(command);
		actions.addAction(action);
		return actions;
	}

	private GroupAction processForAdd(String command, String parameter) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Action negate = new Action();
		Task toDo;
		DateParser dp = new DateParser(parameter);
		switch (command) {
		case "addd":
			toDo = new DeadlineTask();
			toDo.setType("deadline task");
			if (dp.getDate().size() >= 1) {
				((DeadlineTask) toDo).setDate(dp.getDate().get(0));
			}
			break;
		case "addt":
			toDo = new TimedTask();
			toDo.setType("timed task");
			if (dp.getDate().size() == 2) {
				((TimedTask) toDo).setStartTime(dp.getDate().get(0));
				((TimedTask) toDo).setEndTime(dp.getDate().get(1));
			}
			break;
		default:
			toDo = new Task();
			toDo.setType("floating");
		}
		getTaskFromString(parameter, toDo);
		toDo.setTaskId(LocalStorage.generateId());
		action.setCommandType("add");
		action.setTask(toDo);
		negate.setCommandType("delete");
		negate.setTask(toDo);
		action.setUndo(negate);

		actions.addAction(action);
		return actions;
	}

	private void getTaskFromString(String parameter, Task task) {
		parameter = parameter.trim();
		String[] taskDesc = parameter.split("@|#", 2);
	//	task.setDescription(taskDesc[0]);
		task.setDescription(parameter);

		if (taskDesc.length > 1 && !taskDesc[1].equals("")) {
			String[] conCat = parameter.split("(?=@|#)");
			ArrayList<String> contexts = new ArrayList<String>();
			ArrayList<String> categories = new ArrayList<String>();
			for (int i = 1; i < conCat.length; i++) {
				if (conCat[i].contains("@") && conCat[i].length() > 1) {
					contexts.add(conCat[i].substring(1));
				}
				if (conCat[i].contains("#") && conCat[i].length() > 1) {
					categories.add(conCat[i].substring(1));
				}
			}
			task.setCategories(categories);
			task.setContexts(contexts);
		}
	}



    private GroupAction processForDone(String parameter) {
        GroupAction actions = null;
        List<Integer> list = getTaskIndexFromString(parameter);
        if (list != null) {
            // convertFromIndexToId(list);
            actions = new GroupAction();
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("done");
                int normalizedIndex = normalizeIndexToListId(i);
                if (isIndexInRange(normalizedIndex)) {
                    Task task = allTasks.get(normalizedIndex);
                    action.setTask(task);

                    Action negate = new Action();
                    negate.setCommandType("undone");
                    negate.setTask(task);

                    action.setUndo(negate);
                    actions.addAction(action);
                }
            }
        }
        return actions;
    }

    private GroupAction processForUndone(String parameter) {
        GroupAction actions = null;
        List<Integer> list = getTaskIndexFromString(parameter);
        if (list != null) {
            actions = new GroupAction();
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("undone");
                int normalizedIndex = normalizeIndexToListId(i);
                if (isIndexInRange(normalizedIndex)) {
                    Task task = allTasks.get(normalizedIndex);
                    action.setTask(task);

                    Action negate = new Action();
                    negate.setCommandType("done");
                    negate.setTask(task);

                    action.setUndo(negate);
                    actions.addAction(action);
                }
            }
        }
        return actions;
    }

    private GroupAction processUndo(String command) {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        action.setCommandType(command);
        actions.addAction(action);
        return actions;
    }

    private GroupAction processForDelete(String parameter) {
        GroupAction actions = null;
        List<Integer> list = getTaskIndexFromString(parameter);

        if (list != null) {
            // convertFromIndexToId(list);
            actions = new GroupAction();
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("delete");
                int normalizedIndex = normalizeIndexToListId(i);
                if (isIndexInRange(normalizedIndex)) {
                    Task task = allTasks.get(normalizedIndex);
                    action.setTask(task);

                    Action negate = new Action();
                    negate.setCommandType("add");
                    negate.setTask(task);

                    action.setUndo(negate);
                    actions.addAction(action);
                }
            }
        }
        return actions;
    }

    private void convertFromIndexToId(List<Integer> list) {
        List<Task> allTasks = FilterTasks.getFilteredList();
        for (int i = 0; i < list.size(); i++) {
            int index = list.get(i);
            int normalizedIndex = normalizeIndexToListId(index);
            if (normalizedIndex < allTasks.size() && normalizedIndex >= 0) {
                Integer listId = allTasks.get(normalizedIndex).getTaskId();
                list.set(i, listId);
            } else {
                list.remove(i);
            }
        }
    }

    private int normalizeIndexToListId(int index) {
        int listId = index - 1;
        return listId;
    }

    private List<Integer> getTaskIndexFromString(String parameter) {
        List<Integer> taskIds = new ArrayList<Integer>(); 
        String[] split = parameter.trim().split("\\s+|,");
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals("") && split[i].contains("-")) {
                String[] sequence = split[i].split("-");
                int start = Integer.parseInt(sequence[0]);
                int end = Integer.parseInt(sequence[1]);
                for (int j = start; j <= end; j++) {
                    taskIds.add(j); 
                }
            } else if (!split[i].equals("")) {
                taskIds.add(Integer.parseInt(split[i]));
            }
        }

        return taskIds;
    }

    private GroupAction processForEdit(String parameter) {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        Action negate = new Action();

        int taskId = getId(parameter);
        if (taskId >= 1) {
        	List<Task> taskList = FilterTasks.getFilteredList();
            int normalizedIndex = normalizeId(taskId);
            Task oldTask = taskList.get(normalizedIndex);
            String[] parameters = parameter.trim().split("\\s+", 2);
            if (parameters.length > 1) {
                parameter = parameters[1];

                Task editedTask = getTaskFromString(parameter); //we should handle edit better.
                //edit description? or edit due date? or edit start/end time?
                editedTask.setTaskId(oldTask.getTaskId());

                action.setCommandType("edit");
                action.setTask(editedTask);
                negate.setCommandType("edit");
                negate.setTask(oldTask);
                action.setUndo(negate);
            }
            actions.addAction(action);
        }
        return actions;
    }



    private Task getTaskFromString(String parameter) {
        Task newTask = new Task();
        parameter = parameter.trim();
        String[] taskDesc = parameter.split("@|#", 2);
        // task.setDescription(taskDesc[0]);
        newTask.setDescription(parameter);

        if (taskDesc.length > 1 && !taskDesc[1].equals("")) {
            String[] conCat = parameter.split("(?=@|#)");
            List<String> contexts = new ArrayList<String>();
            List<String> categories = new ArrayList<String>();
            for (int i = 0; i < conCat.length; i++) {
                if (conCat[i].contains("#") && conCat[i].length() > 1) {
                    contexts.add(conCat[i].substring(1));
                }
                if (conCat[i].contains("@") && conCat[i].length() > 1) {
                    categories.add(conCat[i].substring(1));
                }
            }
            newTask.setCategories(categories);
            newTask.setContexts(contexts);
        }
        return newTask;
    }

    private int getId(String parameter) {
        String id = parameter.trim().split("\\s+")[0];
        int listId = Integer.parseInt(id);
//        int taskId = getIdFromList(listId);
        return listId;
    }

    private int getIdFromList(int id) {
        List<Integer> list = MainGui.getTaskIndexToId();
        id = normalizeId(id);
        if (id < list.size() && id >= 0) {
            return list.get(id);
        }
        // List<Task> taskList = FilterTasks.getFilteredList();
        // Task task = taskList.get(normalizeId(id));
        // int taskId = task.getTaskId();
        return -1;
    }

    private int normalizeId(int id) {
        int normalizedId = id - USER_INPUT_TO_ARRAYLIST;
        return normalizedId;
    }

    private String getCommandTypeString() {
        return _userInput.trim().split("\\s+")[0].toLowerCase();
    }

    private String getParameter() {
        String[] commands = _userInput.trim().split("\\s+", 2);
        if (commands.length == 2) {
            return commands[1];
        } else {
            return null;
        }
    }

    private GroupAction processLogin() {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        action.setCommandType(COMMAND_LOGIN);
        action.setTask(null);
        action.setUndo(null);
        actions.addAction(action);
        return actions;
    }

    public GroupAction getActions() {
        return _actions;
    }

    public void setActions(GroupAction actions) {
        _actions = actions;
    }

    private boolean isIndexInRange(int index) {
        boolean isInRange = false;
        List<Task> allTasks = FilterTasks.getFilteredList();
        if (index >= 0 && index < allTasks.size()) {
            isInRange = true;
        }
        return isInRange;
    }
}

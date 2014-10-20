package chirptask.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.storage.DeadlineTask;
import chirptask.storage.LocalStorage;
import chirptask.storage.StorageHandler;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

//@author A0113022

public class InputParser {
	private static final int USER_INPUT_TO_ARRAYLIST = 1;
	private static final int TASK_ID_DISPLAY = -1;
	private static final int TASK_ID_INVALID = -2;
	private final DateParser _dateParser = new DateParser();

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
		case "add":
		case "addt":
		case "addd":
			return processAdd(commandType, parameter);
		case "edit":
			return processEdit(parameter);
		case "delete":
			return processDelete(parameter);
		case "done":
			return processDone(parameter);
		case "undone":
			return processUndone(parameter);
		case "undo":
			return processUndo();
		case "display":
			return processDisplay(parameter);
		case "login":
			return processLogin();
		case "exit":
			return processExit();
		default:
			return processInvalid();
		}
	}

	private GroupAction processExit() {
		return processWithNoTask(CommandType.EXIT);
	}

	private GroupAction processLogin() {
		return processWithNoTask(CommandType.LOGIN);
	}

	private GroupAction processUndo() {
		return processWithNoTask(CommandType.UNDO);
	}

	private GroupAction processUndone(String parameter) {
		return processByTaskIndex(CommandType.UNDONE, parameter);
	}

	private GroupAction processDone(String parameter) {
		return processByTaskIndex(CommandType.DONE, parameter);
	}

	private GroupAction processDelete(String parameter) {
		return processByTaskIndex(CommandType.DELETE, parameter);
	}

	private GroupAction processWithNoTask(CommandType command) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		action.setCommandType(command);
		actions.addAction(action);
		return actions;
	}

	private GroupAction processDisplay(String parameter) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Task task = new Task();
		task.setTaskId(TASK_ID_DISPLAY);
		action.setCommandType(Settings.CommandType.DISPLAY);

		if (parameter != null) {
			task.setDescription(parameter);
		} else {
			task.setDescription("");
		}

		action.setTask(task);
		action.setUndo(null);
		actions.addAction(action);

		return actions;
	}

	private GroupAction processAdd(String command, String parameter) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Action negate = new Action();

		if (parameter == null) {
			return processInvalid();
		}

		Task toDo = getTaskFromString(parameter);
		String toParse;
		List<Calendar> dateList;
		int taskIndex = LocalStorage.generateId();
		String description = toDo.getDescription();
		List<String> categoryList = toDo.getCategories();
		List<String> contextList = toDo.getContexts();

		switch (command) {
		case "add":
			Task floating = new Task(taskIndex, description);
			toDo = floating;
			break;
		case "addd":
			toParse = getStringToParseDate(parameter);
			dateList = _dateParser.parseDate(toParse);
			if (dateList.size() == 1) {
				Calendar dueDate = dateList.get(0);
				Task deadline = new DeadlineTask(taskIndex, description, dueDate);
				toDo = deadline;
			} else {
				return processInvalid();
			}
			break;
		case "addt":
			toParse = getStringToParseDate(parameter);
			dateList = _dateParser.parseDate(toParse);
			if (dateList.size() == 2) {
				Calendar startTime = dateList.get(0);
				Calendar endTime = dateList.get(1);
				Task timed = new TimedTask(taskIndex, description, startTime,
					endTime);
				toDo = timed;
			} else {
				return processInvalid();
			}
			break;
		default:
			actions = processInvalid();
			return actions;
		}

		toDo.setCategories(categoryList);
		toDo.setContexts(contextList);

		action.setCommandType(Settings.CommandType.ADD);
		action.setTask(toDo);
		negate.setCommandType(Settings.CommandType.DELETE);
		negate.setTask(toDo);
		action.setUndo(negate);

		actions.addAction(action);
		return actions;
	}

	private String getStringToParseDate(String parameter) {
		String toReturn = new String();
		if (parameter.contains(Settings.CATEGORY)
				|| parameter.contains(Settings.CONTEXT)) {
			String[] split = parameter.split("(?=@|#|\\s+)");

			for (String s : split) {
				if (!(s.contains(Settings.CATEGORY) || s
						.contains(Settings.CONTEXT))) {
					toReturn = toReturn.concat(s);
				}
			}
			
		} else {
			toReturn = parameter;
		}
		if (toReturn.contains("by")) {
			String[] deadline = toReturn.split("by");
			toReturn = deadline[deadline.length - 1];
		} else if (toReturn.contains("from")) {
			String[] timed = toReturn.split("from");
			toReturn = timed[timed.length - 1];
		} 
		
		return toReturn;
	}

	private GroupAction processByTaskIndex(CommandType command, String parameter) {
		GroupAction actions = new GroupAction();
		Settings.CommandType reverse;
		if (parameter == null) {
			return processInvalid();
		}
		
		switch (command) {
		case DONE:
			reverse = CommandType.UNDONE;
			break;
		case UNDONE:
			reverse = CommandType.DONE;
			break;
		case DELETE:
			reverse = CommandType.ADD;
			break;
		default:
			command = CommandType.INVALID;
			reverse = CommandType.INVALID;
		}

		List<Integer> list = getTaskIndexFromString(parameter);
		if (list != null && list.size() != 0) {
			List<Task> allTasks = FilterTasks.getFilteredList();
			for (Integer i : list) {
				Action action = new Action();
				action.setCommandType(command);
				int normalizedIndex = normalizeId(i);
				if (isIndexInRange(normalizedIndex)) {
					Task task = allTasks.get(normalizedIndex);
					action.setTask(task);

					Action negate = new Action();
					negate.setCommandType(reverse);
					negate.setTask(task);

					action.setUndo(negate);
					actions.addAction(action);
				}
			}
		}
		return actions;
	}

	private List<Integer> getTaskIndexFromString(String parameter) {
		List<Integer> taskIndex = new ArrayList<Integer>();

		if (parameter.equals("all")) {
			int size = FilterTasks.getFilteredList().size();
			for (int i = 1; i <= size; i++) {
				taskIndex.add(i);
			}
		}
		parameter = parameter.replaceAll("\\s+(?=-)", "").replaceAll(
				"(?<=-)\\s+", "");
		String[] split = parameter.trim().split("\\s+|,");
		for (int i = 0; i < split.length; i++) {
			if (!split[i].equals("") && split[i].contains("-")) {
				String[] sequence = split[i].split("-");
				try {
					int size = sequence.length;
					if (size == 2) {
						int start = Integer.parseInt(sequence[0]);
						int end = Integer.parseInt(sequence[1]);
						for (int j = start; j <= end; j++) {
							taskIndex.add(j);
						}
					}
				} catch (NumberFormatException e) {
					// TODO handle invalid input
				}
			} else if (!split[i].equals("")) {
				try {
					taskIndex.add(Integer.parseInt(split[i]));

				} catch (NumberFormatException e) {
					// TODO Handle invalid input
				}
			}
		}
		return taskIndex;

	}

	private GroupAction processEdit(String parameter) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Action negate = new Action();

		if (parameter == null) {
			actions = processInvalid();
			return actions;
		}

		int taskIndex = getId(parameter);
		if (taskIndex >= 1) {
			List<Task> taskList = FilterTasks.getFilteredList();
			int normalizedIndex = normalizeId(taskIndex);

			if (isIndexInRange(normalizedIndex)) {
				Task oldTask = taskList.get(normalizedIndex);
				String[] parameters = parameter.trim().split("\\s+", 2);
				if (parameters.length > 1) {
					parameter = parameters[1];
					String toParse = getStringToParseDate(parameter);
					List<Calendar> dateList = _dateParser.parseDate(toParse);

					Task editedTask = getTaskFromString(parameter);
					editedTask = getEditedTask(oldTask, editedTask, dateList);

					action.setCommandType(Settings.CommandType.EDIT);
					action.setTask(editedTask);
					negate.setCommandType(Settings.CommandType.EDIT);
					negate.setTask(oldTask);
					action.setUndo(negate);
				}
			} else {
				return processInvalid();
			}
			actions.addAction(action);
		}
		return actions;
	}

	private Task getEditedTask(Task oldTask, Task editedTask,
			List<Calendar> dateList) {
		int taskId = oldTask.getTaskId();
		String taskType = oldTask.getType(); // Assumes cannot change task type

		String googleId = oldTask.getGoogleId();

		String editedDescription = editedTask.getDescription();
		List<String> editedCategoryList = editedTask.getCategories();
		List<String> editedContextList = editedTask.getContexts();
		List<Calendar> editedDateList = dateList;

		Task newTask = null;
		switch (taskType) {
		case "deadline":
			Calendar dueDate = oldTask.getDate();
			if (editedDateList.size() > 0) {
				dueDate = editedDateList.get(0);
			} else {
				// should append due date to description eg. " by 8/10"
				// String appendDueDate = regex the old description?
				// editedDescription = editedDescription + appendDueDate;
			}
			newTask = new DeadlineTask(taskId, editedDescription, dueDate);
			break;
		case "timedtask":
			TimedTask timedTask = (TimedTask) oldTask;
			Calendar startDate = timedTask.getStartTime();
			Calendar endDate = timedTask.getEndTime();

			if (editedDateList.size() > 1) {
				startDate = editedDateList.get(0);
				endDate = editedDateList.get(1);
			} else {
				// should append start/end to description eg. " from 8 to 10"
				// String appendInfo = regex the old description?
				// editedDescription = editedDescription + appendInfo;
			}

			newTask = new TimedTask(taskId, editedDescription, startDate,
					endDate);
			break;
		case "floating":
			newTask = new Task(taskId, editedDescription);
			break;
		default:
			break;
		}

		if (newTask != null) {
			newTask.setCategories(editedCategoryList);
			newTask.setContexts(editedContextList);
			newTask.setGoogleId(googleId);
		}

		return newTask;
	}

	private Task getTaskFromString(String parameter) {
		Task newTask = new Task();

		parameter = parameter.trim();
		String[] taskDesc = parameter.split("@|#", 2);
		newTask.setDescription(parameter);

		if (taskDesc.length > 0) {
			List<String> contexts = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();

			String[] word = parameter.split("\\s+");

			for (int i = 0; i < word.length; i++) {
				char firstChar = word[i].charAt(0);

				if (firstChar == Settings.CONTEXT_STRING
						&& word[i].length() > 1) {
					contexts.add(word[i].substring(1));
				}
				if (firstChar == Settings.CATEGORY_STRING
						&& word[i].length() > 1) {
					categories.add(word[i].substring(1));
				}
			}
			newTask.setCategories(categories);
			newTask.setContexts(contexts);
		}
		return newTask;
	}

	private int getId(String parameter) {
		String id = parameter.trim().split("\\s+")[0];
		int listId = TASK_ID_INVALID;
		try {
			listId = Integer.parseInt(id);
		} catch (Exception e) {
		}
		return listId;
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

	private GroupAction processInvalid() {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Task invalidInput = new Task(TASK_ID_INVALID, _userInput);
		action.setCommandType(Settings.CommandType.INVALID);
		action.setTask(invalidInput);
		actions.addAction(action);

		return actions;
	}
}

package chirptask.logic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import chirptask.common.Settings;
import chirptask.common.Settings.CommandType;
import chirptask.storage.DeadlineTask;
import chirptask.storage.LocalStorage;
import chirptask.storage.Task;
import chirptask.storage.TimedTask;

//@author A0113022

public class InputParser {
	private static final int USER_INPUT_TO_ARRAYLIST = 1;
	private static final int TASK_ID_DISPLAY = -1;
	private static final int TASK_ID_INVALID = -2;
	private static final int TASK_ID_PARSE_EXCEPTION = -3;
	
	private static final String[] deadlineKeyword = new String[] { "by", "on", "at" };
	private static final String[] timedKeyword = new String[] { "to", "til", "->" };
	private static final int INVALID_POSITION = -1;
	
	private final DateParser _dateParser = new DateParser();
	private String _userInput;
	private GroupAction _actions;

	public InputParser() {
	}

	public InputParser(String userInput) {
		_userInput = userInput;
		_actions = processCommand();
	}

	public void receiveInput(String userInput) {

		_userInput = userInput;
		_actions = processCommand();
	}

	public GroupAction getActions() {
		return _actions;
	}

	public void setActions(GroupAction actions) {
		_actions = actions;
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
		case "filter":
			return processDisplay(parameter);
		case "login":
			return processLogin();
		case "exit":
			return processExit();
		case "clear":
			return processClear();
		case "sync":
			return processSync();
		case "logout":
			return processLogout();
		default:
			return processInvalid(CommandType.INVALID);
		}
	}

	private GroupAction processLogout() {
		return processWithNoTask(CommandType.LOGOUT);
	}

	private GroupAction processSync() {
		return processWithNoTask(CommandType.SYNC);
	}

	private GroupAction processClear() {
		return processWithNoTask(CommandType.CLEAR);
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

	private GroupAction processInvalid(CommandType command) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		action.setCommandType(Settings.CommandType.INVALID);
		action.setInvalidCommandType(command);
		action.setUserInput(_userInput);
		action.setUndo(null);
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
			return processInvalid(CommandType.ADD);
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
			String[] parameters = getStringToParseDate(parameter, Task.TASK_DEADLINE);
			toParse = parameters[0];

			dateList = _dateParser.parseDate(toParse);
			if (dateList != null && dateList.size() == 1) {
				Calendar dueDate = dateList.get(0);
				if (parameters[1] != null && !parameters[1].equals("")) {
					String deadline = new SimpleDateFormat("HH:mm dd/MM")
						.format(dueDate.getTime());
					description = parameters[1] + " by " + deadline;
				}
				Task deadline = new DeadlineTask(taskIndex, description,
						dueDate);
				toDo = deadline;
			} else {
				return processInvalid(CommandType.ADD);
			}
			break;
		case "addt":
			String[] details = getStringToParseDate(parameter, Task.TASK_TIMED);
			toParse = details[0];

			dateList = _dateParser.parseDate(toParse);
			if (dateList != null && dateList.size() == 2) {
				Calendar startTime = dateList.get(0);
				Calendar endTime = dateList.get(1);
				if(details[1] != null && !details[1].equals("")) {
					description = details[1];
				}
				Task timed = new TimedTask(taskIndex, description, startTime,
						endTime);
				toDo = timed;
			} else {
				return processInvalid(CommandType.ADD);
			}
			break;
		default:
			actions = processInvalid(CommandType.ADD);
			return actions;
		}

		toDo.setCategories(categoryList);
		toDo.setContexts(contextList);

		action.setCommandType(Settings.CommandType.ADD);
		action.setTask(toDo);
		action.setUserInput(_userInput);
		negate.setCommandType(Settings.CommandType.DELETE);
		negate.setTask(toDo);
		action.setUndo(negate);

		actions.addAction(action);
		return actions;
	}

	private GroupAction processEdit(String parameter) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		Action negate = new Action();
	
		if (parameter == null) {
			actions = processInvalid(CommandType.EDIT);
			return actions;
		}
	
		int taskIndex = getId(parameter);
	
		List<Task> taskList = FilterTasks.getFilteredList();
		int normalizedIndex = normalizeId(taskIndex);
	
		if (!isIndexInRange(normalizedIndex)) {
			actions = processInvalid(CommandType.EDIT);
		} else {
			Task oldTask = taskList.get(normalizedIndex);
			String[] parameters = parameter.trim().split("\\s+", 2);
			if (parameters.length > 1) {
				parameter = parameters[1];
				Task editedTask = getTaskFromString(parameter);
				editedTask = getEditedTask(oldTask, editedTask);
				if (editedTask == null) {
					return processInvalid(CommandType.EDIT);
				}
				action.setCommandType(Settings.CommandType.EDIT);
				action.setTask(editedTask);
				negate.setCommandType(Settings.CommandType.EDIT);
				negate.setTask(oldTask);
				action.setUndo(negate);
				actions.addAction(action);
			} else {
				actions = processInvalid(CommandType.EDIT);
			}
		}
		return actions;
	}

	private GroupAction processByTaskIndex(CommandType command, String parameter) {
		GroupAction actions = new GroupAction();
		Settings.CommandType reverse;
		if (parameter == null) {
			return processInvalid(command);
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

		try {
			List<Integer> list = getTaskIndexFromString(parameter);
			List<Task> allTasks = FilterTasks.getFilteredList();
			if (allTasks != null && allTasks.size() != 0) {
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
					} else {
						return processInvalid(command);
					}
				}
			} else {
				return processInvalid(command);
			}
		} catch (NumberFormatException e) {
			return processInvalid(command);
		}
		return actions;
	}

	private GroupAction processWithNoTask(CommandType command) {
		GroupAction actions = new GroupAction();
		Action action = new Action();
		action.setCommandType(command);
		action.setUndo(null);
		actions.addAction(action);
		return actions;
	}

	private Task getEditedTask(Task oldTask, Task editedTask) {
		int taskId = oldTask.getTaskId();
		String taskType = oldTask.getType(); // Assumes cannot change task type

		String googleId = oldTask.getGoogleId();
		String eTag = oldTask.getETag();
		boolean isDeleted = oldTask.isDeleted();
		boolean isModified = oldTask.isModified();
		boolean isDone = oldTask.isDone();
		String editedDescription = editedTask.getDescription();
		List<String> editedCategoryList = editedTask.getCategories();
		List<String> editedContextList = editedTask.getContexts();
		// List<Calendar> editedDateList = dateList;

		String toParse = getStringToParseDate(editedDescription,
				oldTask.getType())[0];
		String newDesc = getStringToParseDate(editedDescription,
				oldTask.getType())[1];
		List<Calendar> editedDateList = _dateParser.parseDate(toParse);
		boolean emptyParse = ((toParse == null) || toParse.equals(""));
		boolean emptyDesc = ((newDesc == null) || newDesc.equals(""));

		Task newTask = null;
		switch (taskType) {
		case Task.TASK_DEADLINE:
			Calendar dueDate = oldTask.getDate();
			if (emptyParse && emptyDesc) {
				return newTask;
			} else if (emptyParse && !emptyDesc) {
				String wrongType = getStringToParseDate(newDesc, Task.TASK_TIMED)[0];
				List<Calendar> testWrongType = _dateParser.parseDate(wrongType);
				if (testWrongType != null && testWrongType.size() != 0) {
					return newTask;
				}
				String deadline = new SimpleDateFormat("HH:mm dd/MM")
						.format(dueDate.getTime());
				editedDescription += " by " + deadline;
			} else if (!emptyParse && emptyDesc) {
				if (editedDateList != null && editedDateList.size() == 1) {
					dueDate = editedDateList.get(0);
					editedDescription = getStringToParseDate(
							oldTask.getDescription(), oldTask.getType())[1];
					String deadline = new SimpleDateFormat("HH:mm dd/MM")
							.format(dueDate.getTime());
					editedDescription += " by " + deadline;
				} else {
					return newTask;
				}
			} else {
				if (editedDateList != null && editedDateList.size() == 1) {
					dueDate = editedDateList.get(0);
				} else {
					return newTask;
				}
			}
			newTask = new DeadlineTask(taskId, editedDescription, dueDate);
			break;
		case Task.TASK_TIMED:
			TimedTask timedTask = (TimedTask) oldTask;
			Calendar startDate = timedTask.getStartTime();
			Calendar endDate = timedTask.getEndTime();

			if (emptyParse && emptyDesc) {
				return newTask;
			} else if (emptyParse && !emptyDesc) {
				String wrongType = getStringToParseDate(newDesc, Task.TASK_DEADLINE)[0];
				List<Calendar> testWrongType = _dateParser.parseDate(wrongType);
				if (testWrongType != null && testWrongType.size() != 0) {
					return newTask;
				}
			} else if (!emptyParse && emptyDesc) {
				if (editedDateList != null && editedDateList.size() == 2) {
					startDate = editedDateList.get(0);
					endDate = editedDateList.get(1);
					editedDescription = getStringToParseDate(
							oldTask.getDescription(), oldTask.getType())[1];
				} else {
					return newTask;
				}
			} else {
				if (editedDateList != null && editedDateList.size() == 2) {
					startDate = editedDateList.get(0);
					endDate = editedDateList.get(1);
				} else {
					return newTask;
				}
			}

			newTask = new TimedTask(taskId, editedDescription, startDate,
					endDate);
			break;
		case Task.TASK_FLOATING:
			newTask = new Task(taskId, editedDescription);
			break;
		default:
			break;
		}

		if (newTask != null) {
			newTask.setCategories(editedCategoryList);
			newTask.setContexts(editedContextList);
			newTask.setGoogleId(googleId);
			newTask.setETag(eTag);
			newTask.setDeleted(isDeleted);
			newTask.setModified(isModified);
			newTask.setDone(isDone);
		}

		return newTask;
	}

	public static Task getTaskFromString(String parameter) {
		Task newTask = new Task();
		newTask.setDescription(parameter);

		parameter = parameter.trim();
	
		List<String> contexts = new ArrayList<String>();
		List<String> categories = new ArrayList<String>();

		String[] word = parameter.split("\\s+");

		for (int i = 0; i < word.length; i++) {
			char firstChar = ' ';
			if (word[i].length() > 0) {
				firstChar = word[i].charAt(0);
			}

			if (firstChar == Settings.HASHTAG_CHAR && word[i].length() > 1) {
				contexts.add(word[i].substring(1));
			}
			if (firstChar == Settings.CATEGORY_CHAR && word[i].length() > 1) {
				categories.add(word[i].substring(1));
			}
		}
		newTask.setCategories(categories);
		newTask.setContexts(contexts);

		return newTask;
	}

	private int getId(String parameter) {
		String id = parameter.trim().split("\\s+")[0];
		int listId = TASK_ID_INVALID;
		try {
			listId = Integer.parseInt(id);
		} catch (Exception e) {
			return TASK_ID_PARSE_EXCEPTION;
		}
		return listId;
	}

	private int normalizeId(int id) {
		int normalizedId = id - USER_INPUT_TO_ARRAYLIST;
		return normalizedId;
	}

	//@author A0113022
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

	private boolean isIndexInRange(int index) {
		boolean isInRange = false;
		List<Task> allTasks = FilterTasks.getFilteredList();
		if (index >= 0 && index < allTasks.size()) {
			isInRange = true;
		}
		return isInRange;
	}

	private List<Integer> getTaskIndexFromString(String parameter)
			throws NumberFormatException {
		List<Integer> taskIndex = new ArrayList<Integer>();
	
		parameter = parameter.replaceAll("\\s+(?=-)", "").replaceAll(
				"(?<=-)\\s+", "");
		String[] split = parameter.trim().split("\\s+|,");
		for (int i = 0; i < split.length; i++) {
			if (!split[i].equals("") && split[i].contains("-")) {
				String[] sequence = split[i].split("-");
	
				int size = sequence.length;
				if (size == 2) {
					int start = Integer.parseInt(sequence[0]);
					int end = Integer.parseInt(sequence[1]);
					for (int j = start; j <= end; j++) {
						taskIndex.add(j);
					}
				} else {
					Integer.parseInt(split[i]);
				}
			} else if (!split[i].equals("")) {
				taskIndex.add(Integer.parseInt(split[i]));
			}
	
		}
		return taskIndex;
	}

	private String[] getStringToParseDate(String parameter, String type) {
		String[] timeAndDesc = new String[2];
	
		switch (type) {
		case Task.TASK_DEADLINE:
			timeAndDesc = extractDeadline(parameter);
			break;
			
		case Task.TASK_TIMED:
			timeAndDesc = extractTimed(parameter);
			break;
			
		default:
			timeAndDesc[0] = "";
			timeAndDesc[1] = parameter;
		}
	
		timeAndDesc = extractCategoryAndContext(timeAndDesc);
	
		return timeAndDesc;
	}

	/**
	 * @param timeAndDesc
	 * @param splitSpaces
	 */
	private String[] extractDeadline(String parameter) {
		String[] timeAndDesc = new String[2];
		timeAndDesc[0] = "";
		timeAndDesc[1] = parameter;
		StringBuilder description = new StringBuilder(parameter.length());
		
		String[] splitSpaces = parameter.split("\\s+");
		String deadline[];
		
		int keywordPos[] = new int[deadlineKeyword.length];
		Arrays.fill(keywordPos, INVALID_POSITION);
		int lastKey = INVALID_POSITION;
	
		for (int i = 0; i < splitSpaces.length; i++) {
			for (int j = 0; j < deadlineKeyword.length; j++) {
				if (splitSpaces[i].equals(deadlineKeyword[j])) {
					keywordPos[j] = i;
				}
			}
		}
		
		for (int i = 0; i < deadlineKeyword.length; i++) {
			if (keywordPos[i] > lastKey) {
				lastKey = keywordPos[i];
			}
		}
		
		if (lastKey != INVALID_POSITION) {
			String key = splitSpaces[lastKey] + " ";
			deadline = parameter.split(key);
	
			timeAndDesc[0] = deadline[deadline.length - 1];
			for (int i = 0; i < deadline.length - 1; i++) {
				description.append(deadline[i]);
				if (i < deadline.length - 2) {
					description.append(key);
				}
			}
			timeAndDesc[1] = description.toString();
		} 
		return timeAndDesc;
	}

	/**
	 * @param timeAndDesc
	 * @param splitSpaces
	 */
	private String[] extractTimed(String parameter) {
		String[] timeAndDesc = new String[2];
		timeAndDesc[0] = "";
		timeAndDesc[1] = parameter;
		String[] splitSpaces = parameter.split("\\s+");
		StringBuilder time = new StringBuilder(parameter.length());
		StringBuilder description = new StringBuilder(parameter.length());
		
		int fromPos = INVALID_POSITION;
		int toPos[] = new int[timedKeyword.length];
		int lastTo = INVALID_POSITION;
		Arrays.fill(toPos, INVALID_POSITION);
		
		for (int i = 0; i < splitSpaces.length; i++) {
			for (int j = 0; j < timedKeyword.length; j++) {
				if (splitSpaces[i].equals(timedKeyword[j])) {
					toPos[j] = i;
				}
			}
		}
	
		for (int i = 0; i < timedKeyword.length; i++) {
			if (toPos[i] > lastTo) {
				lastTo = toPos[i];
			}
		}
	
		for (int i = 0; i < lastTo; i++) {
			if (splitSpaces[i].equals("from")) {
					fromPos = i;
			}
		}
		
		if (fromPos != INVALID_POSITION) {
			timeAndDesc[1] = "";
			for (int i = 0; i < fromPos; i++) {
				description.append(splitSpaces[i] + " ");
			}
			
			for (int i = fromPos; i < splitSpaces.length; i++) {
				if (i != lastTo) {
					time.append(splitSpaces[i] + " ");
				} else {
					time.append(timedKeyword[0] + " ");
				}
			}
			timeAndDesc[0] = time.toString();
			timeAndDesc[1] = description.toString();
		} 
		return timeAndDesc;
	}

	/**
	 * @param timeAndDesc
	 */
	private String[] extractCategoryAndContext(String[] timeAndDesc) {
		StringBuilder time = new StringBuilder();
		StringBuilder description = new StringBuilder(timeAndDesc[1].trim());
		String[] hashAndAt = new String[0];
		if (!timeAndDesc[0].equals("")) {
			hashAndAt = timeAndDesc[0].split("\\s+");
		}

		for (int i = 0; i < hashAndAt.length; i++) {
			if (!(hashAndAt[i].contains(Settings.CATEGORY)
					|| hashAndAt[i].contains(Settings.CONTEXT))) {
				time.append(hashAndAt[i] + " ");
			} else {
				description.append(" " + hashAndAt[i]);
			}	
		}
		timeAndDesc[0] = time.toString();
		timeAndDesc[1] = description.toString();
		
		return timeAndDesc;
	}
}

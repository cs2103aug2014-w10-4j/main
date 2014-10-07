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
            return processUndo(commandType);
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

    private GroupAction processForAdd(String command, String parameter) {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        Action negate = new Action();

        if (parameter == null) {
            actions = returnInvalidAction();
            return actions;
        }

        Task toDo = getTaskFromString(parameter);
        int taskIndex = LocalStorage.generateId();
        String description = toDo.getDescription();
        List<String> categoryList = toDo.getCategories();
        List<String> contextList = toDo.getContexts();
        List<Date> dateList = _dateParser.parseDate(parameter);

        switch (command) {
        case "addd":
            if (dateList.size() < 1) {
                actions = returnInvalidAction();
                return actions;
            }
            Date dueDate = dateList.get(0);
            Task deadline = new DeadlineTask(taskIndex, description, dueDate);
            toDo = deadline;
            break;
        case "addt":
            if (dateList.size() < 2) {
                actions = returnInvalidAction();
                return actions;
            }
            Date startTime = dateList.get(0);
            Date endTime = dateList.get(1);
            Task timed = new TimedTask(taskIndex, description, startTime,
                    endTime);
            toDo = timed;
            break;
        default:
            Task floating = new Task(taskIndex, description);
            toDo = floating;
            break;
        }

        toDo.setCategories(categoryList);
        toDo.setContexts(contextList);

        action.setCommandType("add");
        action.setTask(toDo);
        negate.setCommandType("delete");
        negate.setTask(toDo);
        action.setUndo(negate);

        actions.addAction(action);
        return actions;
    }

    private GroupAction processForDone(String parameter) {
        GroupAction actions = new GroupAction();

        if (parameter == null) {
            actions = returnInvalidAction();
            return actions;
        }

        List<Integer> list = getTaskIndexFromString(parameter);
        if (list != null) {
            // convertFromIndexToId(list);
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("done");
                int normalizedIndex = normalizeId(i);
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
        GroupAction actions = new GroupAction();

        if (parameter == null) {
            actions = returnInvalidAction();
            return actions;
        }

        List<Integer> list = getTaskIndexFromString(parameter);
        if (list != null) {
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("undone");
                int normalizedIndex = normalizeId(i);
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
        GroupAction actions = new GroupAction();

        if (parameter == null) {
            actions = returnInvalidAction();
            return actions;
        }

        List<Integer> list = getTaskIndexFromString(parameter);

        if (list != null) {
            List<Task> allTasks = FilterTasks.getFilteredList();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("delete");
                int normalizedIndex = normalizeId(i);
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

        if (parameter == null) {
            actions = returnInvalidAction();
            return actions;
        }

        int taskIndex = getId(parameter);
        if (taskIndex >= 1) {
            List<Task> taskList = FilterTasks.getFilteredList();
            int normalizedIndex = normalizeId(taskIndex);

            Task oldTask = taskList.get(normalizedIndex);
            String[] parameters = parameter.trim().split("\\s+", 2);
            if (parameters.length > 1) {
                parameter = parameters[1];

                Task editedTask = getTaskFromString(parameter);
                copyAttributesFromOldTask(oldTask, editedTask);

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

    private Task copyAttributesFromOldTask(Task oldTask, Task editedTask) {
        int taskId = oldTask.getTaskId();
        String taskType = oldTask.getType();
        String googleId = oldTask.getGoogleId();

        editedTask.setTaskId(taskId);
        editedTask.setType(taskType);
        editedTask.setGoogleId(googleId);

        switch (taskType) {
        case "deadline":
            // setDueDate
            break;
        case "timed":
            // setStartTime
            // setEndTime
            break;
        default:
            break;
        }

        return editedTask;
    }

    private Task getTaskFromString(String parameter) {
        Task newTask = new Task();

        parameter = parameter.trim();
        String[] taskDesc = parameter.split("@|#", 2);
        newTask.setDescription(parameter);

        if (taskDesc.length > 0 && !taskDesc[0].equals("")) {
            List<String> contexts = new ArrayList<String>();
            List<String> categories = new ArrayList<String>();

            String[] word = parameter.split("\\s+");

            for (int i = 0; i < word.length; i++) {
                char firstChar = word[i].charAt(0);

                if (firstChar == '#' && word[i].length() > 1) {
                    contexts.add(word[i].substring(1));
                }
                if (firstChar == '@' && word[i].length() > 1) {
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
        int listId = Integer.parseInt(id);
        // int taskId = getIdFromList(listId);
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

    private GroupAction returnInvalidAction() {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        action.setCommandType("invalid");
        actions.addAction(action);
        return actions;
    }
}

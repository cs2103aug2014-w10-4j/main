package chirptask.logic;

import java.util.ArrayList;
import java.util.List;

import chirptask.gui.MainGui;
import chirptask.storage.LocalStorage;
import chirptask.storage.Task;

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
        case "add":
            return processForAdd(parameter);
        case "edit":
            return processForEdit(parameter);
        case "delete":
            return processForDelete(parameter);
        case "done":
            return processForDone(parameter);
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
        } else {
            Action action = new Action();
            Task task = new Task();
            task.setTaskId(-1);
            task.setDescription("");
            action.setCommandType("display");
            action.setTask(task);
            action.setUndo(null);
            actions.addAction(action);
        }
        return actions;
    }

    private GroupAction processForDone(String parameter) {
        GroupAction actions = null;
        List<Integer> list = getTaskIndexFromString(parameter);
        if (list != null) {
            convertFromIndexToId(list);
            actions = new GroupAction();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("done");
                Task task = new Task();
                task.setTaskId(i);
                action.setTask(task);

                Action negate = new Action();
                negate.setCommandType("undone");
                negate.setTask(task);

                action.setUndo(negate);
                actions.addAction(action);
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

    private GroupAction processForDelete(String parameter) {
        GroupAction actions = null;
        List<Integer> list = getTaskIndexFromString(parameter);

        if (list != null) {
            convertFromIndexToId(list);
            actions = new GroupAction();
            for (Integer i : list) {
                Action action = new Action();
                action.setCommandType("delete");
                Task task = new Task();
                task.setTaskId(i);
                task.setDescription("");
                action.setTask(task);

                Action negate = new Action();
                negate.setCommandType("add");
                negate.setTask(task);

                action.setUndo(negate);
                actions.addAction(action);
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
        List<Integer> taskIds = new ArrayList<Integer>(); // Maybe use set, See
                                                          // below.
        String[] split = parameter.trim().split("\\s+|,");
        for (int i = 0; i < split.length; i++) {
            if (!split[i].equals("") && split[i].contains("-")) {
                String[] sequence = split[i].split("-");
                int start = Integer.parseInt(sequence[0]);
                int end = Integer.parseInt(sequence[1]);
                for (int j = start; j <= end; j++) {
                    taskIds.add(j); // what if delete 1-2 1-2 1-2? Maybe use
                                    // Set.
                }
            } else if (!split[i].equals("")) {
                taskIds.add(Integer.parseInt(split[i])); // what if delete 1 1 1
            }
        }

        return taskIds;
    }

    private GroupAction processForEdit(String parameter) {
        GroupAction actions = new GroupAction();
        Action action = new Action();
        Action negate = new Action();
        Task toDo = new Task();

        int taskId = getId(parameter);
        if (taskId >= 1) {
            String[] parameters = parameter.trim().split("\\s+", 2);
            if (parameters.length > 1) {
                parameter = parameters[1];
                getTaskFromString(parameter, toDo);
                toDo.setTaskId(taskId);

                action.setCommandType("edit");
                action.setTask(toDo);
                negate.setCommandType("edit");
                negate.setTask(new Task(taskId, ""));
                action.setUndo(negate);
            }
            actions.addAction(action);
        }
        return actions;
    }

    private GroupAction processForAdd(String parameter) {
        GroupAction actions = new GroupAction();
        if (parameter != null) {
            Action action = new Action();
            Action negate = new Action();
            Task toDo = new Task();

            getTaskFromString(parameter, toDo);
            toDo.setType("floating"); // Needs attention. Input Parser please
                                      // handle
            toDo.setTaskId(LocalStorage.generateId());
            action.setCommandType("add");
            action.setTask(toDo);
            negate.setCommandType("delete");
            negate.setTask(toDo);
            action.setUndo(negate);

            actions.addAction(action);
        }
        return actions;
    }

    private void getTaskFromString(String parameter, Task task) {
        parameter = parameter.trim();
        String[] taskDesc = parameter.split("@|#", 2);
        // task.setDescription(taskDesc[0]);
        task.setDescription(parameter);

        if (taskDesc.length > 1 && !taskDesc[1].equals("")) {
            String[] conCat = parameter.split("(?=@|#)");
            ArrayList<String> contexts = new ArrayList<String>();
            ArrayList<String> categories = new ArrayList<String>();
            for (int i = 0; i < conCat.length; i++) {
                if (conCat[i].contains("#") && conCat[i].length() > 1) {
                    contexts.add(conCat[i].substring(1));
                }
                if (conCat[i].contains("@") && conCat[i].length() > 1) {
                    categories.add(conCat[i].substring(1));
                }
            }
            task.setCategories(categories);
            task.setContexts(contexts);
        }
    }

    private int getId(String parameter) {
        String id = parameter.trim().split("\\s+")[0];
        int listId = Integer.parseInt(id);
        int taskId = getIdFromList(listId);
        return taskId;
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
}

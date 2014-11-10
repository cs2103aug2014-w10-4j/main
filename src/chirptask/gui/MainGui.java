//@author A0111889W
package chirptask.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import chirptask.common.Constants;
import chirptask.common.Settings;
import chirptask.logic.DisplayView;
import chirptask.logic.Logic;

public class MainGui extends Application implements NativeKeyListener {

    private static final String COMMAND_DISPLAY = "display";
    private static final String COMMAND_UNDONE = "undone";
    private static final String COMMAND_DONE = "done";
    private static final String COMMAND_EXIT = "exit";

    private static final String CSS_SCENE_NAME = "layoutStyle.css";
    private static final String OS_NAME_MAC_OS_X = "Mac OS X";
    private static final String IMAGES_CHIRPTASK_CLEAR_PNG = "images/chirptask_clear.png";
    private static final String COLOR_TASKVIEW_HEADER_NORMAL = "#777";
    private static final String COLOR_TASKVIEW_HEADER_TODAY = "#CC6C6B";
    private static final String BUTTON_CLEAR_LABEL = "Clear";
    private static final String FONT_FAMILY = "Lucida Grande";
    private static final String LABEL_FILTER = "Filter: ";
    private static final String LABEL_USERINPUT = "Input: ";

    private static List<Integer> _taskIndexToId = new ArrayList<>();
    private static final String[] DAY_OF_WEEK = new String[] { "Sunday",
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };

    private static final int MIN_HEIGHT = 300;
    private static final int MIN_WIDTH = 500;

    private static final String[] MONTH = new String[] { "January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };
    private static final double SCROLL_VALUE = 50;

    private static final int STARTING_HEIGHT = 600;
    private static final int STARTING_WIDTH = 800;

    private BorderPane _headerBar;
    private VBox _categoryList = new VBox();
    private TextField _commandLineInterface;
    private TextField _filterField;
    private MainGui _gui = this;
    private VBox _hashtagList = new VBox();
    private Logic _logic;
    private Stage _primaryStage;
    private Label _statusText;

    private VBox _taskViewByDate = new VBox();

    private SortedMap<String, VBox> _taskViewDateMap = new TreeMap<>();

    private ScrollPane _taskViewScrollPane;

    public static List<Integer> getTaskIndexToId() {
        return _taskIndexToId;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        // ensures that settings has been initialized before continuing.
        while (!Settings.hasRead) {
            new Settings();
        }
        launch(args);
    }

    /**
     * Sets the current status of Google services.
     * 
     * @param Status
     *            Current status of Google services.
     */
    public void setOnlineStatus(final String Status) {
        assert Status != null && !Status.isEmpty();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Text onlineStatus = new Text(Status);
                onlineStatus.getStyleClass().add("header-title");
                BorderPane.setAlignment(onlineStatus, Pos.BOTTOM_RIGHT);
                _headerBar.setRight(onlineStatus);
            }
        });
    }

    /**
     * Inserts a category onto the current list of categories showing on the
     * GUI.
     * 
     * @param Category
     *            category to be appended into this list
     */
    public void addCategoryIntoList(String Category) {
        assert Category != null && !Category.isEmpty();
        Text categoryText = new Text(Constants.CATEGORY_CHAR + Category);
        categoryText.getStyleClass().add("category-text");
        categoryText.setOnMouseClicked(clickOnCategory());
        _categoryList.getChildren().add(categoryText);
    }

    /**
     * Inserts a hashtag onto the current list of hashtags showing on the GUI.
     * 
     * @param hashtag
     *            hashtag to be appended to this list
     */
    public void addHashtagIntoList(String hashtag) {
        assert hashtag != null && !hashtag.isEmpty();
        Text hashtagText = new Text(Constants.HASHTAG_CHAR + hashtag);
        hashtagText.getStyleClass().add("hashtag-text");
        hashtagText.setOnMouseClicked(clickOnHashtag());
        _hashtagList.getChildren().add(hashtagText);
    }

    /**
     * Adds a new Task View Date to the GUI. Task View Date will display all
     * tasks of a date in it.
     * 
     * @param date
     *            date of task view date to generate and add into GUI.
     * @return boolean status of operation
     */
    public boolean addNewTaskViewDate(Calendar date) {
        assert date != null;
        String parseDateToString = DisplayView.convertDateToString(date);

        // checks if GUI already has a TaskViewDate for the same date
        if (_taskViewDateMap.containsKey(parseDateToString)) {
            return false;
        }

        // Task View Date will contain all tasks of a date
        VBox taskViewDateBox = generateTaskViewDate(date);

        _taskViewDateMap.put(parseDateToString, taskViewDateBox);
        _taskViewByDate.getChildren().add(taskViewDateBox);
        return true;
    }

    private VBox generateTaskViewDate(Calendar date) {
        assert date != null;

        Insets taskViewDateBoxPadding = new Insets(0, 5, 10, 5);

        // Box to contain all tasks of that date
        VBox taskViewDateBox = new VBox();
        taskViewDateBox.setPadding(taskViewDateBoxPadding);

        // header that shows day of the week and date
        BorderPane taskViewHeader = generateTaskViewHeader(date);
        taskViewDateBox.getChildren().add(taskViewHeader);
        return taskViewDateBox;
    }

    /**
     * Adds a TaskView to a TaskViewDate.
     * <p>
     * Task View is a View that displays all information of a single task in it.
     * pre-cond: date cannot be null, description cannot be empty, taskId cannot
     * be negative
     * </p>
     * 
     * @param date
     *            date of new task to add
     * @param taskId
     *            taskid of new task to add
     * @param description
     *            description of new task to add
     * @param time
     *            time of new task to add
     * @param done
     *            status of new task to add
     * @return boolean status of operation
     */
    public boolean addNewTaskViewToDate(Calendar date, int taskId,
            String description, String time, boolean done) {
        assert date != null && description != null 
                && taskId > -1;

        // Checks for duplicate taskId
        if (_taskIndexToId.contains(taskId)) {
            return false;
        }

        _taskIndexToId.add(taskId);

        // Concatenate the index of the task to the description
        String descriptionWithIndex = _taskIndexToId.size() + ". "
                + description;

        // Generates a Task View
        BorderPane taskView = generateTaskView(time, done, descriptionWithIndex);

        _taskViewDateMap.get(DisplayView.convertDateToString(date))
                .getChildren().add(taskView);
        return true;
    }

    private BorderPane generateTaskView(String time, boolean done,
            String descriptionWithIndex) {
        assert descriptionWithIndex != null && !descriptionWithIndex.isEmpty();

        // pane that makes up task view
        BorderPane taskPane = new BorderPane();

        Pane checkBoxPane = generateTaskCheckBox(done, taskPane);
        HBox descriptionBox = generateTaskDescription(descriptionWithIndex,
                done);
        Text taskTime = generateTaskTimeText(time, done);

        // format the task view
        formatTaskView(taskPane, checkBoxPane, descriptionBox, taskTime);
        return taskPane;
    }

    private void formatTaskView(BorderPane taskPane, Pane checkBoxPane,
            HBox descriptionBox, Text taskTime) {
        assert taskPane != null && checkBoxPane != null
                && descriptionBox != null && taskTime != null;

        Insets taskPanePadding = new Insets(10, 5, 8, 10);
        taskPane.setPadding(taskPanePadding);
        taskPane.getStyleClass().add("task-pane");
        taskPane.setLeft(checkBoxPane);
        taskPane.setCenter(descriptionBox);
        taskPane.setRight(taskTime);
    }

    /**
     * Clears all tasks showing in GUI.
     */
    public void clearTaskView() {
        _taskViewByDate.getChildren().clear();
        _taskViewDateMap.clear();
        _taskIndexToId.clear();
    }

    /**
     * Clears all hashtags and categories showing in GUI.
     */
    public void clearTrendingList() {
        _hashtagList.getChildren().clear();
        _categoryList.getChildren().clear();
        generateTrendingList();
    }

    /**
     * EventHandler for clicking on categories
     * 
     * @return method to call when a category is clicked.
     */
    public EventHandler<MouseEvent> clickOnCategory() {
        return onClickTrendingListText();
    }

    /**
     * EventHandler for clicking on hashtags
     * 
     * @return method to call when a hashtag is clicked.
     */
    public EventHandler<MouseEvent> clickOnHashtag() {
        return onClickTrendingListText();
    }

    /**
     * Gets the current filter showing on the GUI.
     * 
     * @return String current filter
     */
    public String getFilter() {
        return _filterField.getText();
    }

    /**
     * Gets the current user input showing on the CLI of the GUI.
     * 
     * @return String current user input
     */
    public String getUserInput() {
        return _commandLineInterface.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jnativehook.keyboard.NativeKeyListener#nativeKeyTyped(org.jnativehook
     * .keyboard.NativeKeyEvent)
     */
    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jnativehook.keyboard.NativeKeyListener#nativeKeyPressed(org.jnativehook
     * .keyboard.NativeKeyEvent)
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        hotKeyToFocusCLI(e);
        hotKeyToScrollTaskView(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jnativehook.keyboard.NativeKeyListener#nativeKeyReleased(org.jnativehook
     * .keyboard.NativeKeyEvent)
     */
    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        hotKeyToScrollToToday(e);
        hotKeyToShowStage(e);
        hotKeyToHideStage(e);
    }

    /**
     * Refreshes the UI after the UI is done with what it is currently busy
     * with.
     */
    public void refreshUI() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (_logic != null) {
                    _logic.refreshUi();
                }
            }
        });
    }

    /**
     * Sets the status of the GUI with the given error message.
     * Message will be coloured red as it's an error.
     * 
     * @param errorMessage
     *            error message to display on GUI.
     */
    public void setError(String errorMessage) {
        assert errorMessage != null && !errorMessage.trim().isEmpty();

        String Status = String.format(Constants.STATUS_ERROR, errorMessage);
        _statusText.setText(Status);
        _statusText.getStyleClass().clear();
        _statusText.getStyleClass().add("error-message");
    }

    /**
     * Sets the current filter input to the given text. Empty strings are
     * allowed.
     * 
     * @param text
     *            string to set in filter's text field.
     */
    public void setFilterText(String text) {
        int caretPosition = _filterField.getCaretPosition();
        _filterField.setText(text);
        _filterField.positionCaret(caretPosition);
    }

    /**
     * Sets the status of the GUI with the given status message.
     * 
     * @param message
     *            status message to display on GUI.
     */
    public void setStatus(String message) {
        assert message != null && !message.trim().isEmpty();

        String Status = String.format(Constants.STATUS_NORMAL, message);
        _statusText.setText(Status);
        _statusText.getStyleClass().clear();
        _statusText.getStyleClass().add("status-message");
    }

    /**
     * Sets the user input text of the CLI. Empty strings are accepted.
     * 
     * @param text
     *            string to show in input text field.
     */
    public void setUserInputText(String text) {
        _commandLineInterface.setText(text);
        _commandLineInterface.positionCaret(text.length());
    }

    @Override
    /*
     * Actual starting point of application
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    public void start(Stage primaryStage) {
        macOsXInitialization();
        prepareScene(primaryStage);

        /*
         * Try to have lesser stuff loading before .show().
         * Too much processing will cause the GUI to take awhile to load.
         */
        primaryStage.show();

        initJNativeHook();
        _logic = new Logic(this);
        scrollToToday();
    }

    /**
     * EventHandler for the clear all button. clears the filter input and sends
     * an enter action, invoking the filter action.
     * 
     * @return
     */
    private EventHandler<? super MouseEvent> clearAllAction() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean isLeftClick = event.getButton() == MouseButton.PRIMARY;
                if (isLeftClick) {
                    setFilterText("");
                    sendEnterKeyToFilterBar();
                }
            }

            private void sendEnterKeyToFilterBar() {
                KeyEvent enterEvent = new KeyEvent(null, null, null,
                        KeyCode.ENTER, false, false, false, false);
                filterModified().handle(enterEvent);
            }
        };
    }

    /**
     * EventHandler for CLI input text box.
     * 
     * @return
     */
    private EventHandler<KeyEvent> cliKeyPressHandler() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode keyPressed = event.getCode();
                cliKeyTab(event, keyPressed);
                cliKeyEnter(keyPressed);
            }

            private void cliKeyEnter(KeyCode keyPressed) {
                boolean pressedEnter = keyPressed == KeyCode.ENTER;

                if (pressedEnter) {
                    String input = _commandLineInterface.getText();
                    boolean isInputIsNotEmpty = !input.trim().isEmpty();

                    if (isInputIsNotEmpty) {
                        _commandLineInterface.setText("");
                        sendCommandToLogic(input);
                    }
                }
            }

            private void cliKeyTab(KeyEvent event, KeyCode keyPressed) {
                boolean pressedTab = keyPressed == KeyCode.TAB;
                if (pressedTab) {
                    String input = _commandLineInterface.getText()
                            .toLowerCase().trim();
                    boolean isDisplayCommand = input
                            .startsWith(COMMAND_DISPLAY);
                    boolean isFilterCommand = input.startsWith("filter");
                    boolean isEditCommand = input.startsWith("edit ");

                    if (isDisplayCommand) {
                        event.consume();
                        setUserInputText("display " + getFilter());

                    } else if (isFilterCommand) {
                        event.consume();
                        setUserInputText("filter " + getFilter());

                    } else if (isEditCommand) {
                        event.consume();
                        DisplayView.autocompleteEditWithTaskDescription(input,
                                _gui);
                    }
                }
            }
        };
    }

    /**
     * EventHandler for filter input text box.
     * 
     * @return
     */
    private EventHandler<KeyEvent> filterModified() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                boolean pressedEnter = event.getCode() == KeyCode.ENTER;
                if (pressedEnter) {
                    sendCommandToLogic(COMMAND_DISPLAY + " "
                            + _filterField.getText());
                }
            }
        };
    }

    /**
     * Sends command to logic for processing and execution.
     * 
     * @param command
     */
    private void sendCommandToLogic(String command) {
        assert command != null && !command.trim().isEmpty();

        _logic.retrieveInputFromUI(command);
    }

    /**
     * Format a label with specified color.
     * 
     * @param Label
     * @param color
     */
    private void formatTextLabel(Text Label, String color) {
        assert Label != null && color != null && !color.trim().isEmpty();
        Label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        Label.setFill(Color.web(color));
    }

    private ScrollPane generateCategoryList() {
        Text categoryTitle = generateCategoryLabel();
        generateCategoryListBox(categoryTitle);
        ScrollPane categoryScrollPane = generateCategoryScrollPane();
        VBox.setVgrow(categoryScrollPane, Priority.ALWAYS);
        categoryScrollPane.setMaxSize(Region.USE_COMPUTED_SIZE,
                Region.USE_COMPUTED_SIZE);
        return categoryScrollPane;
    }

    private ScrollPane generateCategoryScrollPane() {
        ScrollPane categoryScrollPane = new ScrollPane();
        categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        categoryScrollPane.setContent(_categoryList);
        categoryScrollPane.getStyleClass().add("category-scroll");
        return categoryScrollPane;
    }

    private void generateCategoryListBox(Text categoryTitle) {
        assert categoryTitle != null;
        _categoryList.setPadding(new Insets(8));
        _categoryList.setSpacing(5);
        _categoryList.getChildren().add(categoryTitle);
    }

    private Text generateCategoryLabel() {
        Text categoryTitle = new Text(Constants.LABEL_CATEGORIES);
        setTrendingListTitleFont(categoryTitle);
        return categoryTitle;
    }

    private HBox generateFilterBox() {

        generateFilterField();
        Text filterLabel = generateFilterLabel();
        Button clearFilter = generateClearAllButton();

        HBox filterBox = new HBox();
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(5));

        filterBox.getChildren().addAll(filterLabel, _filterField, clearFilter);

        return filterBox;
    }

    private Text generateFilterLabel() {
        Text filterLabel = new Text(LABEL_FILTER);
        return filterLabel;
    }

    private Button generateClearAllButton() {
        Button clearFilter = new Button();
        clearFilter.setText(BUTTON_CLEAR_LABEL);
        clearFilter.getStyleClass().add("clear-button");
        clearFilter.setOnMouseClicked(clearAllAction());
        return clearFilter;
    }

    private void generateFilterField() {
        _filterField = new TextField();
        _filterField.setText(Settings.DEFAULT_FILTER);
        _filterField.setOnKeyReleased(filterModified());
        HBox.setHgrow(_filterField, Priority.ALWAYS);
    }

    private ScrollPane generateHashtagList() {
        Text hashtagTitle = generateHashtagLabel();
        generateHashtagListBox(hashtagTitle);
        ScrollPane hashtagScrollPane = generateHashtagScrollPane();
        VBox.setVgrow(hashtagScrollPane, Priority.ALWAYS);
        hashtagScrollPane.setMaxSize(Region.USE_COMPUTED_SIZE,
                Region.USE_COMPUTED_SIZE);
        return hashtagScrollPane;
    }

    private ScrollPane generateHashtagScrollPane() {
        ScrollPane hashtagScrollPane = new ScrollPane();
        hashtagScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hashtagScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        hashtagScrollPane.setContent(_hashtagList);
        hashtagScrollPane.getStyleClass().add("context-scroll");
        return hashtagScrollPane;
    }

    private void generateHashtagListBox(Text hashtagTitle) {
        assert hashtagTitle != null;
        _hashtagList.setPadding(new Insets(8));
        _hashtagList.setSpacing(5);
        _hashtagList.getChildren().add(hashtagTitle);
    }

    private Text generateHashtagLabel() {
        Text hashtagTitle = new Text(Constants.LABEL_HASHTAGS);
        setTrendingListTitleFont(hashtagTitle);
        return hashtagTitle;
    }

    private BorderPane generateHeaderBar() {
        BorderPane headerBar = new BorderPane();
        headerBar.setPadding(new Insets(13, 10, 8, 10));
        headerBar.getStyleClass().add("header-bar");

        Text onlineStatus = generateOnlineStatusText();
        HBox titleBox = generateTitleBox();

        headerBar.setLeft(titleBox);
        headerBar.setRight(onlineStatus);
        return headerBar;
    }

    private BorderPane generateMainDisplay() {
        BorderPane mainDisplay = new BorderPane();
        mainDisplay.setPadding(new Insets(0));
        mainDisplay.getStyleClass().add("address");

        HBox filterBox = generateFilterBox();
        mainDisplay.setTop(filterBox);

        _taskViewScrollPane = generateTasksView();
        mainDisplay.setCenter(_taskViewScrollPane);

        VBox mainDisplayBottom = generateUserInputAndStatusBar();
        mainDisplay.setBottom(mainDisplayBottom);

        return mainDisplay;
    }

    private Text generateOnlineStatusText() {
        // default is offline.
        Text onlineStatus = new Text(Constants.TITLE_OFFLINE);
        onlineStatus.getStyleClass().add("header-title");
        BorderPane.setAlignment(onlineStatus, Pos.BOTTOM_RIGHT);
        return onlineStatus;
    }

    private BorderPane generateRootPane() {
        BorderPane rootPane = new BorderPane();

        _headerBar = generateHeaderBar();
        rootPane.setTop(_headerBar);

        BorderPane mainDisplay = generateMainDisplay();
        rootPane.setCenter(mainDisplay);

        VBox trendingList = generateTrendingList();
        rootPane.setRight(trendingList);

        beautifyScrollBar(mainDisplay, trendingList);

        return rootPane;
    }

    private void beautifyScrollBar(BorderPane mainDisplay, VBox trendingList) {
        assert mainDisplay != null && trendingList != null;
        // scroll bar hack to beautify scroll bar
        makeScrollFadeable(mainDisplay.lookup(".address > .scroll-pane"));
        makeScrollFadeable(trendingList.getChildren().get(0));
        makeScrollFadeable(trendingList.getChildren().get(1));
    }

    private VBox generateStatusBarInterface() {
        _statusText = new Label();
        _statusText.setTextOverrun(OverrunStyle.ELLIPSIS);
        setStatus(Constants.DEFAULT_STATUS);

        VBox statusBar = new VBox();
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));
        statusBar.getChildren().add(_statusText);
        return statusBar;
    }

    private Pane generateTaskCheckBox(boolean Done, final BorderPane taskPane) {
        assert taskPane != null;

        CheckBox markTaskAsDone = new CheckBox();
        markTaskAsDone.setSelected(Done);

        markTaskAsDone.selectedProperty().addListener(
                listenerForTaskStatusChange(taskPane));

        Pane checkBoxPane = new Pane();
        checkBoxPane.setMaxWidth(20);
        checkBoxPane.getChildren().add(markTaskAsDone);

        return checkBoxPane;
    }

    private HBox generateTaskDescription(String description, boolean done) {
        assert description != null && !description.trim().isEmpty();

        HBox descriptionBox = new HBox();
        descriptionBox.setPadding(new Insets(0, 8, 0, 8));
        descriptionBox.setAlignment(Pos.CENTER_LEFT);
        TextFlow taskDescription = DisplayView.parseDescriptionToTextFlow(
                description, done, this);
        descriptionBox.getChildren().add(taskDescription);
        return descriptionBox;
    }

    private ScrollPane generateTasksView() {

        ScrollPane taskViewScrollPane = new ScrollPane();
        taskViewScrollPane.setPadding(new Insets(5));
        taskViewScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        taskViewScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        taskViewScrollPane.setContent(_taskViewByDate);
        taskViewScrollPane.setFitToWidth(true);

        return taskViewScrollPane;
    }

    private Text generateTaskTimeText(String time, boolean done) {
        // time can be empty string.
        Text taskTime = new Text(time);
        taskTime.getStyleClass().add("task-time");
        taskTime.setStrikethrough(done);
        return taskTime;
    }

    private BorderPane generateTaskViewHeader(Calendar date) {
        assert date != null;

        String dayOfTheWeek = DAY_OF_WEEK[date.get(Calendar.DAY_OF_WEEK) - 1];
        Text dayLabel = new Text(dayOfTheWeek);

        String dateString = date.get(Calendar.DAY_OF_MONTH) + " "
                + MONTH[date.get(Calendar.MONTH)] + ", "
                + (date.get(Calendar.YEAR));
        Text dateLabel = new Text(dateString);

        BorderPane taskViewHeader = new BorderPane();
        taskViewHeader.setPadding(new Insets(5, 5, 3, 5));
        taskViewHeader.setLeft(dayLabel);
        taskViewHeader.setRight(dateLabel);
        colourDateAndDayIfDateIsToday(date, dayLabel, dateLabel, taskViewHeader);

        return taskViewHeader;
    }

    private void colourDateAndDayIfDateIsToday(Calendar date, Text dayLabel,
            Text dateLabel, BorderPane taskViewHeader) {
        assert date != null && dayLabel != null && dateLabel != null
                && taskViewHeader != null;

        boolean isToday = DisplayView.convertDateToString(date).equals(
                DisplayView.convertDateToString(Calendar.getInstance()));
        if (isToday) {
            taskViewHeader.getStyleClass().add("taskView-header-today");
            formatTextLabel(dayLabel, COLOR_TASKVIEW_HEADER_TODAY);
            formatTextLabel(dateLabel, COLOR_TASKVIEW_HEADER_TODAY);
        } else {
            taskViewHeader.getStyleClass().add("taskView-header");
            formatTextLabel(dayLabel, COLOR_TASKVIEW_HEADER_NORMAL);
            formatTextLabel(dateLabel, COLOR_TASKVIEW_HEADER_NORMAL);
        }
    }

    private HBox generateTitleBox() {
        Text sceneTitle = new Text(Constants.TITLE_SOFTWARE);
        sceneTitle.getStyleClass().add("header-title");
        sceneTitle.setTextAlignment(TextAlignment.CENTER);

        ImageView imgView = new ImageView(new Image(this.getClass()
                .getResourceAsStream(IMAGES_CHIRPTASK_CLEAR_PNG)));
        imgView.setFitHeight(30);
        imgView.setPreserveRatio(true);
        imgView.setSmooth(true);
        imgView.setCache(true);

        HBox titleBox = new HBox();
        titleBox.setSpacing(10);
        titleBox.setAlignment(Pos.BOTTOM_LEFT);
        titleBox.getChildren().addAll(imgView, sceneTitle);
        return titleBox;
    }

    private VBox generateTrendingList() {
        VBox trendingList = new VBox();
        trendingList.setPadding(new Insets(0));
        trendingList.setSpacing(0);
        trendingList.setMinWidth(180);
        trendingList.getStyleClass().addAll("trending-list", "address");

        ScrollPane hashtagPane = generateHashtagList();
        ScrollPane categoryPane = generateCategoryList();

        trendingList.getChildren().addAll(categoryPane, hashtagPane);
        return trendingList;
    }

    private VBox generateUserInputAndStatusBar() {
        VBox mainDisplayBottom = new VBox();

        HBox userInputBox = generateUserInputInterface();
        VBox statusBar = generateStatusBarInterface();

        mainDisplayBottom.getChildren().addAll(userInputBox, statusBar);

        return mainDisplayBottom;
    }

    private void generateUserInputField() {
        _commandLineInterface = new TextField();
        HBox.setHgrow(_commandLineInterface, Priority.ALWAYS);
        _commandLineInterface.setOnKeyPressed(cliKeyPressHandler());
    }

    private HBox generateUserInputInterface() {
        generateUserInputField();
        Text userInputLabel = new Text(LABEL_USERINPUT);

        HBox userInputBox = new HBox();
        userInputBox.setPadding(new Insets(5));
        userInputBox.setAlignment(Pos.CENTER);
        userInputBox.getChildren().add(userInputLabel);
        userInputBox.getChildren().add(_commandLineInterface);
        return userInputBox;
    }

    private void guiClosing(WindowEvent we) {
        assert we != null;
        // consume the closing request, let logic handle
        we.consume();
        sendCommandToLogic(COMMAND_EXIT);
    }

    /**
     * When user is typing on application, but not focused on CLI and
     * FilterField, application automatically focuses on CLI for disruption-free
     * experience.
     * 
     * @param e
     */
    private void hotKeyToFocusCLI(NativeKeyEvent e) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                boolean applicationIsFocusedButNotOnFilterField = _primaryStage
                        .isFocused() && !_filterField.isFocused();

                if (applicationIsFocusedButNotOnFilterField) {
                    _commandLineInterface.requestFocus();
                }
            }
        });
    }

    /**
     * hotkey to hide the application.
     * 
     * @param e
     */
    private void hotKeyToHideStage(NativeKeyEvent e) {
        assert e != null;
        boolean applicationIsFocusedAndPressedHotKeyForHide = _primaryStage
                .isFocused() && e.getKeyCode() == Settings.HOTKEY_TOGGLE_HIDE;

        if (applicationIsFocusedAndPressedHotKeyForHide) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    _primaryStage.setIconified(true);
                }
            });
        }
    }

    /**
     * Easily scrolls tasks view up and down using keys.
     * 
     * @param e
     */
    private void hotKeyToScrollTaskView(NativeKeyEvent e) {
        assert e != null;

        boolean applicationIsFocused = _primaryStage.isFocused();
        boolean pressedUpKey = e.getKeyCode() == NativeKeyEvent.VC_UP;
        boolean pressedDownKey = e.getKeyCode() == NativeKeyEvent.VC_DOWN;
        final double amountToScroll = SCROLL_VALUE
                / (_taskViewByDate.getHeight() - _taskViewScrollPane
                        .getHeight());

        if (applicationIsFocused) {
            if (pressedUpKey) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        _taskViewScrollPane.setVvalue(_taskViewScrollPane
                                .getVvalue() - amountToScroll);
                    }
                });
            } else if (pressedDownKey) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        _taskViewScrollPane.setVvalue(_taskViewScrollPane
                                .getVvalue() + amountToScroll);
                    }
                });
            }
        }
    }

    /**
     * Checks whether key is pressed for hotkey to scroll to Task View Date of
     * current day's date.
     * 
     * @param e
     */
    private void hotKeyToScrollToToday(NativeKeyEvent e) {
        assert e != null;
        int mod = e.getModifiers();
        boolean holdingCtrlOrCommandKey = mod == NativeInputEvent.CTRL_L_MASK
                || mod == NativeInputEvent.CTRL_R_MASK
                || mod == NativeInputEvent.CTRL_MASK
                || mod == NativeInputEvent.META_L_MASK
                || mod == NativeInputEvent.META_R_MASK
                || mod == NativeInputEvent.META_MASK;

        boolean applicationIsFocusedAndPressedTAndCtrlOrCommand = _primaryStage
                .isFocused()
                && e.getKeyCode() == NativeKeyEvent.VC_T
                && holdingCtrlOrCommandKey;

        if (applicationIsFocusedAndPressedTAndCtrlOrCommand) {
            scrollToToday();
        }
    }

    /**
     * Toggles out application from minimized state.
     * 
     * @param e
     */
    private void hotKeyToShowStage(NativeKeyEvent e) {
        assert e != null;
        int mod = e.getModifiers();
        boolean holdingCtrlOrCommandKey = mod == NativeInputEvent.CTRL_L_MASK
                || mod == NativeInputEvent.CTRL_R_MASK
                || mod == NativeInputEvent.CTRL_MASK
                || mod == NativeInputEvent.META_L_MASK
                || mod == NativeInputEvent.META_R_MASK
                || mod == NativeInputEvent.META_MASK;
        boolean pressedHotKeyForTogglingApplication = e.getKeyCode() == Settings.HOTKEY_TOGGLE_SHOW
                && holdingCtrlOrCommandKey;

        if (pressedHotKeyForTogglingApplication) {
            // focus on CLI
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    toggleOutApplication();
                    _commandLineInterface.requestFocus();
                }

                private void toggleOutApplication() {
                    if (!_primaryStage.isFocused()) {
                        _primaryStage.toFront();
                        _primaryStage.setIconified(false);
                        _primaryStage.requestFocus();
                        _primaryStage.getScene().getRoot().requestFocus();
                    }
                }
            });
        }
    }

    private void initJNativeHook() {
        try {
            // Gets the JNativeHook logger
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage()
                    .getName());
            LogManager.getLogManager().reset();
            logger.setLevel(Level.WARNING);

            GlobalScreen.registerNativeHook();

        } catch (NativeHookException ex) {
            // Occasionally the hook will fail, this issue is with the library,
            // we are unable to solve it.
            System.err
                    .println("There was a problem registering the native hook.");
        }
        GlobalScreen.getInstance().addNativeKeyListener(this);
    }

    /**
     * Listener to check if a task status is changed from done to undone, vice
     * versa.
     * 
     * @param taskPane
     * @return
     */
    private ChangeListener<Boolean> listenerForTaskStatusChange(
            final BorderPane taskPane) {
        return new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean oldValue, Boolean newValue) {
                assert ov != null;

                HBox descriptionBox = (HBox) taskPane.getCenter();
                TextFlow desc = (TextFlow) descriptionBox.getChildren().get(0);
                String taskIndex = ""
                        + ((Text) desc.getChildren().get(0)).getText().split(
                                "\\.")[0];

                if (newValue) {
                    sendCommandToLogic(COMMAND_DONE + " " + taskIndex);
                } else {
                    sendCommandToLogic(COMMAND_UNDONE + " " + taskIndex);
                }

                setStrikethroughOfDescription(taskPane, newValue, desc);
            }

            private void setStrikethroughOfDescription(
                    final BorderPane taskPane, Boolean newValue, TextFlow desc) {
                assert taskPane != null && desc != null;

                Iterator<Node> descChildIterator = desc.getChildren()
                        .iterator();
                Text taskTime = (Text) taskPane.getRight();
                while (descChildIterator.hasNext()) {
                    Text descChild = (Text) descChildIterator.next();
                    descChild.setStrikethrough(newValue);
                }
                taskTime.setStrikethrough(newValue);
            }
        };
    }

    private void macOsXInitialization() {
        // Sets the icon of application for Mac OS X
        if (System.getProperty("os.name").equals(OS_NAME_MAC_OS_X)) {
            // com.apple.eawt.Application application =
            // com.apple.eawt.Application
            // .getApplication();
            // java.awt.Image image = Toolkit.getDefaultToolkit().getImage(
            // getClass().getResource("images/chirptask_clear.png"));
            // System.setProperty("apple.laf.useScreenMenuBar", "true");
            // application.setDockIconImage(image);
        }
    }

    //@author A0111889W-reused
    /*
     * Code re-use from https://gist.github.com/jewelsea/
     */
    private void makeScrollFadeable(final Node scroll) {
        assert scroll != null;
        final Node scrollbar = scroll.lookup(".scroll-bar:vertical");
        final FadeTransition fader = new FadeTransition(Duration.seconds(0.5),
                scrollbar);
        fader.setFromValue(1);
        fader.setToValue(0);
        fader.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!scroll.getStyleClass().contains("hide-thumb")) {
                    scroll.getStyleClass().add("hide-thumb");
                }
            }
        });
        if (!scroll.isHover()) {
            scroll.getStyleClass().add("hide-thumb");
        }
        scroll.hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(
                    ObservableValue<? extends Boolean> observableValue,
                    Boolean wasHover, Boolean isHover) {
                if (!isHover) {
                    fader.playFromStart();
                } else {
                    fader.stop();
                    if (scrollbar != null) {
                        scrollbar.setOpacity(1);
                    }
                    scroll.getStyleClass().remove("hide-thumb");
                }
            }
        });
    }

    //@author A0111889W
    /**
     * Handler for mouse clicks on hashtags/categories.
     * Sets filter to hashtag or category.
     * 
     * @return
     */
    private EventHandler<MouseEvent> onClickTrendingListText() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String hashtagOrCategoryValue = ((Text) event.getSource())
                        .getText();
                setFilterText(hashtagOrCategoryValue);
                sendCommandToLogic(COMMAND_DISPLAY + " "
                        + hashtagOrCategoryValue);
            }
        };
    }

    private void prepareScene(Stage primaryStage) {
        assert primaryStage != null;
        _primaryStage = primaryStage;

        // generates the entire gui
        BorderPane rootPane = generateRootPane();

        Scene scene = sceneSetUp(rootPane);
        primaryStageSetUp(primaryStage, scene);

        // focus on CLI
        _commandLineInterface.requestFocus();

    }

    private void primaryStageSetUp(Stage primaryStage, Scene scene) {
        assert primaryStage != null && scene != null;

        primaryStage.setScene(scene);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setTitle(Constants.TITLE_SOFTWARE);
        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream(
                        IMAGES_CHIRPTASK_CLEAR_PNG)));
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                guiClosing(we);
            }
        });
    }

    private Scene sceneSetUp(BorderPane rootPane) {
        assert rootPane != null;

        Scene scene = new Scene(rootPane, STARTING_WIDTH, STARTING_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource(CSS_SCENE_NAME).toExternalForm());
        return scene;
    }

    /**
     * Scrolls to task view date of today's date if exist.
     */
    private void scrollToToday() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                VBox Today = _taskViewDateMap.get(DisplayView
                        .convertDateToString(Calendar.getInstance()));
                boolean taskViewDateOfTodayExist = Today != null;

                if (taskViewDateOfTodayExist) {
                    double scrollPositionOfTaskViewDate = (Today.getLayoutY())
                            / (_taskViewByDate.getHeight() - _taskViewScrollPane
                                    .getHeight());

                    _taskViewScrollPane.setVvalue(scrollPositionOfTaskViewDate);
                }
            }
        });

    }

    private void setTrendingListTitleFont(Text titleText) {
        assert titleText != null;
        titleText.setFont(Font.font(FONT_FAMILY, FontWeight.BLACK, 14));
    }

}

//@author A0111889W
/*
 * JavaFx CSS files. Inserted here so that collate catches it.
 * 
 * .root{
 * -fx-font-family: "Lucida Grande";
 * -fx-font-size:11.0px;
 * }
 * 
 * .header-title {
 * -fx-font-size:20.0px;
 * }
 * 
 * .status-bar, .header-bar {
 * -fx-background-color: rgb(241.0,241.0,241.0);
 * }
 * 
 * .status-message {
 * 
 * }
 * 
 * .error-message {
 * -fx-text-fill:red;
 * }
 * 
 * .clear-button {
 * -fx-background-radius: 0.0;
 * -fx-background-insets: 0.0;
 * -fx-padding:3;
 * -fx-border-color: -fx-text-box-border;
 * -fx-border-width: 1.0 1.0 1.0 0.0;
 * -fx-focus-color: transparent;
 * }
 * .clear-button:hover {
 * -fx-cursor:hand;
 * }
 * 
 * .text-field {
 * -fx-background-radius: 0.0;
 * }
 * 
 * .text-field:focused {
 * -fx-background-color:-fx-shadow-highlight-color, -fx-text-box-border,
 * -fx-control-inner-background;
 * -fx-background-radius: 0.0;
 * -fx-background-insets: -1.4, 0.0, 1.0;
 * }
 * 
 * 
 * .trending-list {
 * -fx-border-width: 0.0px 0.0px 0.0px 1.0px;
 * -fx-border-style: solid;
 * -fx-border-color: transparent transparent transparent #ddd;
 * }
 * 
 * .category-text {
 * -fx-fill:rgba(68.0,167.0,3.0);
 * }
 * 
 * .category-scroll {
 * -fx-border-width: 0.0px 0.0px 1.0px 0.0px;
 * -fx-border-style: solid;
 * -fx-border-color: transparent transparent #ddd transparent;
 * }
 * 
 * .hashtag-text {
 * -fx-fill:rgba(14.0,97.0,185.0);
 * }
 * 
 * .hashtag-scroll {
 * -fx-border-width: 0.0px 0.0px 1.0px 0.0px;
 * -fx-border-style: solid;
 * -fx-border-color: transparent transparent #ddd transparent;
 * }
 * 
 * .hashtag-text:hover, .category-text:hover {
 * -fx-cursor:hand;
 * -fx-underline:true;
 * }
 * 
 * .task-time {
 * -fx-fill:#999;
 * }
 * 
 * .taskView-header-today {
 * -fx-border-width: 0.0px 0.0px 3.0px 0.0px;
 * -fx-border-style: solid;
 * -fx-border-color: transparent transparent #CC6C6B transparent;
 * -fx-border-insets:3.0px;
 * }
 * 
 * 
 * .taskView-header {
 * -fx-border-width: 0.0px 0.0px 3.0px 0.0px;
 * -fx-border-style: solid;
 * -fx-border-color: transparent transparent #ddd transparent;
 * -fx-border-insets:3.0px;
 * }
 * 
 * .task-pane {
 * -fx-border-width: 1.0px 0.0px 1.0px 0.0px;
 * -fx-border-style: dashed;
 * -fx-border-color: transparent transparent #ccc transparent;
 * }
 * 
 * .task-pane:hover {
 * -fx-background-color: rgb(229.0,234.0,238.0);
 * -fx-background-radius:5.0px;
 * -fx-border-width:0.0px;
 * -fx-background-insets:-2.0px -3.0px -2.0px -3.0px;
 * }
 * 
 * .address {
 * -fx-background-color:white;
 * }
 * 
 * .address .scroll-pane {
 * -fx-background: transparent;
 * -fx-background-color: transparent;
 * }
 * 
 * .address .scroll-bar .increment-button {
 * -fx-opacity: 0.0;
 * }
 * 
 * .address .scroll-bar .decrement-button {
 * -fx-opacity: 0.0;
 * }
 * 
 * .address .scroll-bar:vertical {
 * -fx-background-color: transparent;
 * }
 * 
 * .address .scroll-bar:vertical .track-background {
 * -fx-opacity: 0.0;
 * }
 * 
 * .address .scroll-bar:vertical .track {
 * -fx-opacity: 0.0;
 * }
 * .address .scroll-bar:vertical .thumb {
 * -fx-background-color: #999;
 * -fx-opacity: 1.0;
 * }
 * 
 * .address .hide-thumb .scroll-bar:vertical .thumb {
 * -fx-background-color: transparent;
 * }
 */
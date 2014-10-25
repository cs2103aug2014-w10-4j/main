package chirptask.gui;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import chirptask.common.Messages;
import chirptask.common.Settings;
import chirptask.logic.DisplayView;
import chirptask.logic.Logic;

//@author A0111889W
public class MainGui extends Application implements NativeKeyListener {

    private static final int STARTING_HEIGHT = 600;
    private static final int STARTING_WIDTH = 800;
    private static final int MIN_WIDTH = 500;
    private static final int MIN_HEIGHT = 300;

    private static final String[] DAY_OF_WEEK = new String[] { "Sunday",
            "Monday", "Tuesday", "Wednesday", "Thusday", "Friday", "Saturday" };
    private static final String[] MONTH = new String[] { "January", "February",
            "March", "April", "May", "June", "July", "August", "September",
            "October", "November", "December" };

    private final TextField _commandLineInterface = new TextField();
    private final TextField _filterField = new TextField();
    private final Label _statusText = new Label();

    private final VBox _categoryList = new VBox();
    private final VBox _contextList = new VBox();
    private final VBox _taskViewByDate = new VBox();

    private ScrollPane _taskViewScrollPane = new ScrollPane();

    private final SortedMap<String, VBox> _taskViewDateMap = new TreeMap<>();
    private static final List<Integer> _taskIndexToId = new ArrayList<>();

    private Stage _primaryStage;

    private Logic _logic;

    /*
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) {
        macOsXInitialization();
        prepareScene(primaryStage);
        primaryStage.show();

        initJNativeHook();
        _logic = new Logic(this);
        this.scrollToToday();
    }

    private void initJNativeHook() {
        try {
            GlobalScreen.registerNativeHook();
            // Gets the JNativeHook logger
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage()
                    .getName());
            LogManager.getLogManager().reset();
            logger.setLevel(Level.WARNING);

        } catch (NativeHookException ex) {
            System.err
                    .println("There was a problem registering the native hook.");
            // System.err.println(ex.getMessage());
            // ex.printStackTrace();
            // System.exit(1);
        }
        GlobalScreen.getInstance().addNativeKeyListener(this);
    }

    private void guiClosing() {
        System.out.println("Stage is closing");
        _logic.retrieveInputFromUI("exit");
    }

    private BorderPane generateRootPane() {
        BorderPane rootPane = new BorderPane();

        BorderPane headerBar = generateHeaderBar();
        rootPane.setTop(headerBar);

        BorderPane mainDisplay = generateMainDisplay();
        rootPane.setCenter(mainDisplay);

        VBox trendingList = generateTrendingList();
        rootPane.setRight(trendingList);

        // scroll bar hack to beautify scroll bar
        makeScrollFadeable(mainDisplay.lookup(".address > .scroll-pane"));
        makeScrollFadeable(trendingList.lookup(".address > .scroll-pane"));

        return rootPane;
    }

    private void prepareScene(Stage primaryStage) {

        _primaryStage = primaryStage;

        BorderPane rootPane = generateRootPane();
        Scene scene = sceneSetUp(rootPane);
        primaryStageSetUp(primaryStage, scene);

        // focus on CLI
        _commandLineInterface.requestFocus();

    }

    private Scene sceneSetUp(BorderPane rootPane) {
        Scene scene = new Scene(rootPane, STARTING_WIDTH, STARTING_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("layoutStyle.css").toExternalForm());
        return scene;
    }

    private void primaryStageSetUp(Stage primaryStage, Scene scene) {
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setTitle(Messages.TITLE_SOFTWARE);
        primaryStage.getIcons().add(
                new Image(getClass().getResourceAsStream(
                        "images/chirptask_clear.png")));
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                guiClosing();
            }
        });
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
        launch(args);
    }

    private void macOsXInitialization() {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            // com.apple.eawt.Application application =
            // com.apple.eawt.Application
            // .getApplication();
            java.awt.Image image = Toolkit.getDefaultToolkit().getImage(
                    getClass().getResource("images/chirptask_clear.png"));
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // application.setDockIconImage(image);
        }
    }

    private BorderPane generateHeaderBar() {
        BorderPane headerBar = new BorderPane();

        headerBar.setPadding(new Insets(13, 10, 8, 10));
        headerBar.getStyleClass().add("header-bar");

        Text sceneTitle = new Text(Messages.TITLE_SOFTWARE);
        sceneTitle.getStyleClass().add("header-title");

        Text settingsButton = new Text(Messages.TITLE_SETTINGS);
        settingsButton.getStyleClass().add("header-title");
        // ImageView imgView = new ImageView(new
        // Image("file:chirptask_clear.png"));

        headerBar.setLeft(sceneTitle);
        // headerBar.setRight(settingsButton);

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

    private VBox generateTrendingList() {
        VBox trendingList = new VBox();
        trendingList.setPadding(new Insets(0));
        trendingList.setSpacing(0);
        trendingList.setMinWidth(180);
        trendingList.getStyleClass().addAll("trending-list", "address");

        ScrollPane contextPane = generateContextList();
        ScrollPane categoryPane = generateCategoryList();

        VBox.setVgrow(contextPane, Priority.ALWAYS);
        VBox.setVgrow(categoryPane, Priority.ALWAYS);
        contextPane.setMaxSize(Region.USE_COMPUTED_SIZE,
                Region.USE_COMPUTED_SIZE);
        categoryPane.setMaxSize(Region.USE_COMPUTED_SIZE,
                Region.USE_COMPUTED_SIZE);

        trendingList.getChildren().addAll(categoryPane, contextPane);
        return trendingList;
    }

    private ScrollPane generateContextList() {
        Text contextTitle = new Text("Context");
        setTrendingListTitleFont(contextTitle);

        _contextList.setPadding(new Insets(8));
        _contextList.setSpacing(5);
        _contextList.getChildren().add(contextTitle);
        ScrollPane contextScrollPane = new ScrollPane();
        contextScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        contextScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contextScrollPane.setContent(_contextList);

        return contextScrollPane;
    }

    private void setTrendingListTitleFont(Text titleText) {
        titleText.setFont(Font.font("Lucida Grande", FontWeight.BLACK, 14));
    }

    private ScrollPane generateCategoryList() {
        Text categoryTitle = new Text("Category");
        setTrendingListTitleFont(categoryTitle);

        _categoryList.setPadding(new Insets(8));
        _categoryList.setSpacing(5);
        _categoryList.getChildren().add(categoryTitle);

        ScrollPane categoryScrollPane = new ScrollPane();
        categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        categoryScrollPane.setContent(_categoryList);
        categoryScrollPane.getStyleClass().add("category-scroll");

        return categoryScrollPane;
    }

    private HBox generateFilterBox() {
        _filterField.setText(Settings.DEFAULT_FILTER);
        _filterField.setOnKeyReleased(filterModified());

        HBox.setHgrow(_filterField, Priority.ALWAYS);

        Text filterLabel = new Text(Messages.LABEL_FILTER);

        HBox filterBox = new HBox();
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(5));

        filterBox.getChildren().add(filterLabel);
        filterBox.getChildren().add(_filterField);

        return filterBox;
    }

    private EventHandler<KeyEvent> filterModified() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                // check difference
                if (event.getCode() == KeyCode.ENTER) {
                    _logic.retrieveInputFromUI("display "
                            + _filterField.getText());
                }
            }
        };
    }

    private VBox generateUserInputAndStatusBar() {
        HBox.setHgrow(_commandLineInterface, Priority.ALWAYS);
        _commandLineInterface.setOnKeyPressed(cliKeyPressHandler());

        VBox mainDisplayBottom = new VBox();

        HBox userInputBox = generateUserInputInterface();
        VBox statusBar = generateStatusBarInterface();

        mainDisplayBottom.getChildren().addAll(userInputBox, statusBar);

        return mainDisplayBottom;
    }

    private VBox generateStatusBarInterface() {
        _statusText.setTextOverrun(OverrunStyle.ELLIPSIS);
        setStatus(Messages.DEFAULT_STATUS);

        VBox statusBar = new VBox();
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));
        statusBar.getChildren().add(_statusText);
        return statusBar;
    }

    private HBox generateUserInputInterface() {
        Text userInputLabel = new Text("Input: ");

        HBox userInputBox = new HBox();
        userInputBox.setPadding(new Insets(5));
        userInputBox.setAlignment(Pos.CENTER);
        userInputBox.getChildren().add(userInputLabel);
        userInputBox.getChildren().add(_commandLineInterface);
        return userInputBox;
    }

    private EventHandler<KeyEvent> cliKeyPressHandler() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode keyPressed = ((KeyEvent) event).getCode();
                cliKeyEnter(keyPressed);
            }

            private void cliKeyEnter(KeyCode keyPressed) {
                if (keyPressed == KeyCode.ENTER) {
                    _logic.retrieveInputFromUI(_commandLineInterface.getText());
                    _commandLineInterface.setText("");
                }
            }
        };
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
        Text taskTime = new Text(time);
        taskTime.getStyleClass().add("task-time");
        taskTime.setStrikethrough(done);
        return taskTime;
    }

    private Pane generateTaskCheckBox(boolean Done, final BorderPane taskPane) {
        CheckBox markTaskAsDone = new CheckBox();
        markTaskAsDone.setSelected(Done);

        markTaskAsDone.selectedProperty().addListener(
                listenerForTaskStatusChange(taskPane));

        Pane checkBoxPane = new Pane();
        checkBoxPane.setMaxWidth(20);
        checkBoxPane.getChildren().add(markTaskAsDone);

        return checkBoxPane;
    }

    private ChangeListener<Boolean> listenerForTaskStatusChange(
            final BorderPane taskPane) {
        return new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                    Boolean old_val, Boolean new_val) {

                HBox descriptionBox = (HBox) taskPane.getCenter();
                TextFlow desc = (TextFlow) descriptionBox.getChildren().get(0);
                String taskIndex = ""
                        + ((Text) desc.getChildren().get(0)).getText().split(
                                "\\.")[0];

                if (new_val) {
                    _logic.retrieveInputFromUI("done " + taskIndex);
                } else {
                    _logic.retrieveInputFromUI("undone " + taskIndex);
                }

                Iterator<Node> descChildIterator = desc.getChildren()
                        .iterator();
                Text taskTime = (Text) taskPane.getRight();
                while (descChildIterator.hasNext()) {
                    Text descChild = (Text) descChildIterator.next();
                    descChild.setStrikethrough(new_val);
                }
                taskTime.setStrikethrough(new_val);

            }
        };
    }

    private BorderPane generateTaskViewHeader(Calendar date) {

        Text dayLabel = new Text();
        dayLabel.setText(DAY_OF_WEEK[date.get(Calendar.DAY_OF_WEEK) - 1]);

        Text dateLabel = new Text();
        dateLabel.setText(date.get(Calendar.DAY_OF_MONTH) + " "
                + MONTH[date.get(Calendar.MONTH)] + ", "
                + (date.get(Calendar.YEAR)));

        BorderPane taskViewHeader = new BorderPane();
        taskViewHeader.setPadding(new Insets(5, 5, 3, 5));
        taskViewHeader.setLeft(dayLabel);
        taskViewHeader.setRight(dateLabel);

        boolean isToday = DisplayView.convertDateToString(date).equals(
                DisplayView.convertDateToString(Calendar.getInstance()));
        if (isToday) {
            taskViewHeader.getStyleClass().add("taskView-header-today");
            formatTextLabel(dayLabel, "#CC6C6B");
            formatTextLabel(dateLabel, "#CC6C6B");
        } else {
            taskViewHeader.getStyleClass().add("taskView-header");
            formatTextLabel(dayLabel, "#777");
            formatTextLabel(dateLabel, "#777");
        }
        return taskViewHeader;
    }

    private void formatTextLabel(Text Label, String color) {
        Label.setFont(Font.font("Lucida Grande", FontWeight.BOLD, 12));
        Label.setFill(Color.web(color));
    }

    private HBox generateTaskDescription(String description, boolean done) {
        HBox descriptionBox = new HBox();
        descriptionBox.setPadding(new Insets(0, 8, 0, 8));
        TextFlow taskDescription = DisplayView.parseDescriptionToTextFlow(
                description, done, this);

        descriptionBox.setAlignment(Pos.CENTER_LEFT);
        descriptionBox.getChildren().add(taskDescription);
        return descriptionBox;
    }

    public EventHandler<MouseEvent> clickOnContext() {
        return onClickTrendingListText();
    }

    public EventHandler<MouseEvent> clickOnCategory() {
        return onClickTrendingListText();
    }

    private EventHandler<MouseEvent> onClickTrendingListText() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setFilterText(((Text) event.getSource()).getText());
                _logic.retrieveInputFromUI("display " + _filterField.getText());
            }
        };
    }

    /*
     * Code re-use from https://gist.github.com/jewelsea/
     */
    // @author A0111889W-reused
    private void makeScrollFadeable(final Node scroll) {
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

    public String getFilter() {
        return _filterField.getText();
    }

    public void setFilterText(String text) {
        int caretPosition = _filterField.getCaretPosition();
        _filterField.setText(text);
        _filterField.positionCaret(caretPosition);
    }

    public void setUserInputText(String text) {
        _commandLineInterface.setText(text);
        _commandLineInterface.positionCaret(text.length());
    }

    public String getUserInput() {
        return _commandLineInterface.getText();
    }

    public void clearTrendingList() {
        _contextList.getChildren().clear();
        _categoryList.getChildren().clear();
        generateTrendingList();
    }

    public void setStatus(String message) {
        assert !message.isEmpty();
        _statusText.setText("Status: " + message);
        _statusText.getStyleClass().clear();
        _statusText.getStyleClass().add("status-message");
    }

    public void setError(String errorMessage) {
        assert !errorMessage.isEmpty();
        _statusText.setText("Error: " + errorMessage);
        _statusText.getStyleClass().clear();
        _statusText.getStyleClass().add("error-message");
    }

    public void addContextIntoList(String Context) {
        assert !Context.isEmpty();
        Text contextText = new Text(Settings.CONTEXT_CHAR + Context);
        contextText.getStyleClass().add("context-text");
        contextText.setOnMouseClicked(clickOnContext());
        _contextList.getChildren().add(contextText);
    }

    public void addCategoryIntoList(String Category) {
        assert !Category.isEmpty();
        Text categoryText = new Text(Settings.CATEGORY_CHAR + Category);
        categoryText.getStyleClass().add("category-text");
        categoryText.setOnMouseClicked(clickOnCategory());
        _categoryList.getChildren().add(categoryText);
    }

    private void scrollToToday() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                VBox Today = (VBox) _taskViewDateMap.get(DisplayView
                        .convertDateToString(Calendar.getInstance()));
                if (Today != null) {
                    _taskViewScrollPane.setVvalue((Today.getLayoutY())
                            / (_taskViewByDate.getHeight() - _taskViewScrollPane
                                    .getHeight()));
                }
            }
        });

    }

    public boolean addNewTaskViewDate(Calendar date) {
        assert date != null;
        String parseDateToString = DisplayView.convertDateToString(date);

        if (_taskViewDateMap.containsKey(parseDateToString)) {
            return false;
        }

        // Box to contain all tasks of that date
        VBox taskViewDateBox = new VBox();
        taskViewDateBox.setPadding(new Insets(0, 5, 10, 5));

        // header that shows day of the week and date
        BorderPane taskViewHeader = generateTaskViewHeader(date);
        taskViewDateBox.getChildren().add(taskViewHeader);

        _taskViewDateMap.put(parseDateToString, taskViewDateBox);
        _taskViewByDate.getChildren().add(taskViewDateBox);

        return true;
    }

    public boolean addNewTaskViewToDate(Calendar date, int taskId,
            String description, String time, boolean done) {
        assert date != null && !time.isEmpty() && taskId > -1;

        if (_taskIndexToId.contains(taskId)) {
            return false;
        }

        _taskIndexToId.add(taskId);
        String descriptionWithIndex = _taskIndexToId.size() + ". "
                + description;

        // pane that makes up task view
        BorderPane taskPane = new BorderPane();

        Pane checkBoxPane = generateTaskCheckBox(done, taskPane);
        HBox descriptionBox = generateTaskDescription(descriptionWithIndex,
                done);
        Text taskTime = generateTaskTimeText(time, done);

        // formatting task view pane
        taskPane.setPadding(new Insets(10, 5, 8, 10));
        taskPane.getStyleClass().add("task-pane");
        taskPane.setLeft(checkBoxPane);
        taskPane.setCenter(descriptionBox);
        taskPane.setRight(taskTime);

        _taskViewDateMap.get(DisplayView.convertDateToString(date))
                .getChildren().add(taskPane);
        return true;
    }

    public void clearTaskView() {
        _taskViewByDate.getChildren().clear();
        _taskViewDateMap.clear();
        _taskIndexToId.clear();
    }

    public static List<Integer> getTaskIndexToId() {
        return _taskIndexToId;
    }

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

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        hotKeyToScrollTaskView(e);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // System.out.println("Key Text "
        // + NativeKeyEvent.getKeyText(e.getKeyCode()));
        // System.out.println("Mod Text "
        // + NativeKeyEvent.getModifiersText(e.getKeyCode()));
        // System.out.println("Key Char " + e.getKeyChar());
        // System.out.println("Key Code " + e.getKeyCode());
        // System.out.println("Key Loc " + e.getKeyLocation());
        // System.out.println("Raw Code " + e.getRawCode());
        // System.out.println("Modifiers : " + e.getModifiers() + " : "
        // + NativeInputEvent.getModifiersText(e.getModifiers()));
        hotKeyToScrollToToday(e);
        hotKeyToShowStage(e);
        hotKeyToHideStage(e);
    }

    private void hotKeyToScrollToToday(NativeKeyEvent e) {
        int mod = e.getModifiers();
        if (e.getKeyCode() == NativeKeyEvent.VC_T
                && (mod == NativeInputEvent.CTRL_L_MASK
                        || mod == NativeInputEvent.CTRL_R_MASK
                        || mod == NativeInputEvent.CTRL_MASK
                        || mod == NativeInputEvent.META_L_MASK
                        || mod == NativeInputEvent.META_R_MASK || mod == NativeInputEvent.META_MASK)) {
            scrollToToday();
        }
    }

    private void hotKeyToScrollTaskView(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_UP) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    _taskViewScrollPane.setVvalue(_taskViewScrollPane
                            .getVvalue() - 0.03);
                }
            });
        }
        if (e.getKeyCode() == NativeKeyEvent.VC_DOWN) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    _taskViewScrollPane.setVvalue(_taskViewScrollPane
                            .getVvalue() + 0.03);
                }
            });
        }

    }

    private void hotKeyToHideStage(NativeKeyEvent e) {
        if (e.getKeyCode() == Settings.HOTKEY_TOGGLE_HIDE) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (_primaryStage.isFocused()) {
                        _primaryStage.setIconified(true);
                    }
                }
            });
        }
    }

    private void hotKeyToShowStage(NativeKeyEvent e) {
        int mod = e.getModifiers();
        if (e.getKeyCode() == Settings.HOTKEY_TOGGLE_SHOW
                && (mod == NativeInputEvent.CTRL_L_MASK
                        || mod == NativeInputEvent.CTRL_R_MASK
                        || mod == NativeInputEvent.CTRL_MASK
                        || mod == NativeInputEvent.META_L_MASK
                        || mod == NativeInputEvent.META_R_MASK || mod == NativeInputEvent.META_MASK)) {
            // focus on CLI
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (!_primaryStage.isFocused()) {
                        _primaryStage.toFront();
                        _primaryStage.setIconified(false);
                        _primaryStage.requestFocus();
                        _primaryStage.getScene().getRoot().requestFocus();
                    }
                    _commandLineInterface.requestFocus();
                }
            });
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }

}

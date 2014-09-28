package chirptask.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.animation.FadeTransition;
import javafx.application.Application;
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
import javafx.util.Duration;

/**
 *
 * @author Yeo Quan Yang
 * @MatricNo A0111889W
 */
public class MainGui extends Application {

	private static final String STATUS_DEFAULT = "Nothing is happening.";
	private static final char CATEGORY_STRING = '@';
	private static final char CONTEXT_STRING = '#';
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

	private final SortedMap<String, VBox> _taskViewDateMap = new TreeMap<>();
	private final ArrayList<Integer> _taskIndexToId = new ArrayList<>();

	@Override
	public void start(Stage primaryStage) {
		BorderPane border = new BorderPane();

		BorderPane headerBar = generateHeaderBar();
		border.setTop(headerBar);

		BorderPane mainDisplay = generateMainDisplay();
		border.setCenter(mainDisplay);

		VBox trendingList = generateTrendingList();
		border.setRight(trendingList);

		Scene scene = new Scene(border, STARTING_WIDTH, STARTING_HEIGHT);
		scene.getStylesheets().add(
				getClass().getResource("layoutStyle.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(MIN_HEIGHT);
		primaryStage.setMinWidth(MIN_WIDTH);
		primaryStage.setTitle("ChirpTask");
		primaryStage.show();

		// scroll bar hack to beautify scroll bar
		makeScrollFadeable(mainDisplay.lookup(".address > .scroll-pane"));
		makeScrollFadeable(trendingList.lookup(".address > .scroll-pane"));
		// focus on CLI
		_commandLineInterface.requestFocus();

		addCategoryIntoList("123");
		addContextIntoList("TEST");

		addNewTaskViewDate(new Date());
		addNewTaskViewToDate(new Date(), 0, "#123 @123 TEST", "all-day", true);
		addNewTaskViewToDate(new Date(), 4, "#TEST @123", "all-day", true);
		addNewTaskViewToDate(new Date(), 1, "TEST @123 #123", "8:00 to 10:00",
				true);
		addNewTaskViewToDate(new Date(), 2, "#TEST", "noon to 16:00", true);
		addNewTaskViewToDate(new Date(), 3, "@TEST", "due by 16:00", true);
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

	private BorderPane generateHeaderBar() {
		BorderPane headerBar = new BorderPane();

		headerBar.setPadding(new Insets(13, 10, 13, 10));
		headerBar.getStyleClass().add("header-bar");

		Text sceneTitle = new Text("ChirpTask");
		sceneTitle.getStyleClass().add("header-title");

		Text settingsButton = new Text("Settings");
		settingsButton.getStyleClass().add("header-title");

		headerBar.setLeft(sceneTitle);
		headerBar.setRight(settingsButton);

		return headerBar;
	}

	private BorderPane generateMainDisplay() {
		BorderPane mainDisplay = new BorderPane();
		mainDisplay.setPadding(new Insets(0));

		HBox filterBox = generateFilterBox();
		mainDisplay.setTop(filterBox);

		ScrollPane taskViewScrollPane = generateTaskView();
		mainDisplay.setCenter(taskViewScrollPane);

		VBox mainDisplayBottom = generateUserInputAndStatusBar();
		mainDisplay.setBottom(mainDisplayBottom);
		mainDisplay.getStyleClass().add("address");
		return mainDisplay;
	}

	private VBox generateTrendingList() {
		VBox trendingList = new VBox();
		trendingList.setPadding(new Insets(0));
		trendingList.setSpacing(0);
		trendingList.setMinWidth(180);
		trendingList.getStyleClass().add("trending-list");

		ScrollPane contextPane = generateContextList();
		ScrollPane categoryPane = generateCategoryList();

		VBox.setVgrow(categoryPane, Priority.ALWAYS);
		VBox.setVgrow(contextPane, Priority.ALWAYS);
		trendingList.getChildren().addAll(contextPane, categoryPane);
		trendingList.getStyleClass().add("address");
		return trendingList;
	}

	private ScrollPane generateContextList() {
		Text contextTitle = new Text("Context");
		contextTitle.setFont(Font.font("Lucida Grande", FontWeight.BLACK, 14));

		_contextList.setPadding(new Insets(8));
		_contextList.setSpacing(5);
		_contextList.getChildren().add(contextTitle);

		ScrollPane contextScrollPane = new ScrollPane();
		contextScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		contextScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		contextScrollPane.setContent(_contextList);
		contextScrollPane.getStyleClass().add("context-scroll");

		return contextScrollPane;
	}

	private ScrollPane generateCategoryList() {
		Text categoryTitle = new Text("Category");
		categoryTitle.setFont(Font.font("Lucida Grande", FontWeight.BLACK, 14));

		_categoryList.setPadding(new Insets(8));
		_categoryList.setSpacing(5);
		_categoryList.getChildren().add(categoryTitle);

		ScrollPane categoryScrollPane = new ScrollPane();
		categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		categoryScrollPane.setContent(_categoryList);

		return categoryScrollPane;
	}

	private HBox generateFilterBox() {
		_filterField.setText("is:NotDone");
		HBox.setHgrow(_filterField, Priority.ALWAYS);

		Text filterLabel = new Text("Filter: ");

		HBox filterBox = new HBox();
		filterBox.setAlignment(Pos.CENTER);
		filterBox.setPadding(new Insets(5));

		filterBox.getChildren().add(filterLabel);
		filterBox.getChildren().add(_filterField);

		return filterBox;
	}

	private VBox generateUserInputAndStatusBar() {
		HBox.setHgrow(_commandLineInterface, Priority.ALWAYS);

		VBox mainDisplayBottom = new VBox();

		Text userInputLabel = new Text("Input: ");

		HBox userInputBox = new HBox();
		userInputBox.setPadding(new Insets(5));
		userInputBox.setAlignment(Pos.CENTER);
		userInputBox.getChildren().add(userInputLabel);
		userInputBox.getChildren().add(_commandLineInterface);
		_commandLineInterface.setOnKeyPressed(cliKeyPressHandler());

		_statusText.setTextOverrun(OverrunStyle.ELLIPSIS);
		setStatus(STATUS_DEFAULT);
		VBox statusBar = new VBox();
		statusBar.getStyleClass().add("status-bar");
		statusBar.setPadding(new Insets(5));
		statusBar.getChildren().add(_statusText);

		mainDisplayBottom.getChildren().addAll(userInputBox, statusBar);

		return mainDisplayBottom;
	}

	private EventHandler<KeyEvent> cliKeyPressHandler() {
		return new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				System.out.println(((KeyEvent) event).getCode());
			}
		};
	}

	private ScrollPane generateTaskView() {
		ScrollPane taskViewScrollPane = new ScrollPane();
		taskViewScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		taskViewScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		taskViewScrollPane.setContent(_taskViewByDate);
		taskViewScrollPane.setFitToWidth(true);

		return taskViewScrollPane;
	}

	public boolean addNewTaskViewDate(Date date) {
		String parseDateToString = convertDateToString(date);

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

	private boolean addNewTaskViewToDate(Date date, int taskId,
			String description, String time, boolean done) {

		if (_taskIndexToId.contains(taskId)) {
			return false;
		}

		_taskIndexToId.add(taskId);

		Pane checkBoxPane = generateTaskCheckBox(done);
		HBox descriptionBox = generateTaskDescription(_taskIndexToId.size()
				+ ". " + description);

		Text taskTime = generateTaskTimeText(time);

		BorderPane taskPane = new BorderPane();
		taskPane.setPadding(new Insets(10, 5, 8, 10));

		taskPane.getStyleClass().add("task-pane");
		taskPane.setLeft(checkBoxPane);
		taskPane.setCenter(descriptionBox);
		taskPane.setRight(taskTime);

		_taskViewDateMap.get(convertDateToString(date)).getChildren()
				.add(taskPane);

		return true;
	}

	private Text generateTaskTimeText(String time) {
		Text taskTime = new Text(time);
		taskTime.getStyleClass().add("task-time");
		return taskTime;
	}

	private Pane generateTaskCheckBox(boolean Done) {
		CheckBox markTaskAsDone = new CheckBox();
		markTaskAsDone.setSelected(Done);

		Pane checkBoxPane = new Pane();
		checkBoxPane.setMaxWidth(20);
		checkBoxPane.getChildren().add(markTaskAsDone);

		return checkBoxPane;
	}

	private BorderPane generateTaskViewHeader(Date date) {

		Text dayLabel = new Text();
		dayLabel.setText(DAY_OF_WEEK[date.getDay()]);
		formatTextLabel(dayLabel);

		Text dateLabel = new Text();
		dateLabel.setText(date.getDate() + " " + MONTH[date.getMonth()] + ", "
				+ (1900 + date.getYear()));
		formatTextLabel(dateLabel);

		BorderPane taskViewHeader = new BorderPane();
		taskViewHeader.setPadding(new Insets(5, 5, 3, 5));
		taskViewHeader.setLeft(dayLabel);
		taskViewHeader.setRight(dateLabel);
		taskViewHeader.getStyleClass().add("taskView-header");
		return taskViewHeader;
	}

	private void formatTextLabel(Text Label) {
		Label.setFont(Font.font("Lucida Grande", FontWeight.BOLD, 12));
		Label.setFill(Color.web("#777"));
	}

	private HBox generateTaskDescription(String description) {
		HBox descriptionBox = new HBox();
		descriptionBox.setPadding(new Insets(0, 8, 0, 8));
		TextFlow taskDescription = parseDescriptionToTextFlow(description);

		descriptionBox.setAlignment(Pos.CENTER_LEFT);
		descriptionBox.getChildren().add(taskDescription);
		return descriptionBox;
	}

	/*
	 * Move this to logic (?)
	 */
	public String convertDateToString(Date date) {
		String parseDateToString = date.getDate() + "/" + date.getMonth() + "/"
				+ (1900 + date.getYear());

		return parseDateToString;
	}

	/*
	 * Move to logic(?)
	 */
	private TextFlow parseDescriptionToTextFlow(String description) {
		TextFlow parsedDesc = new TextFlow();
		StringBuilder descSb = new StringBuilder(description);
		Text bufferText = new Text();

		while (descSb.length() > 0) {
			int index = descSb.length();
			if (descSb.indexOf(" ") > 0) {
				index = descSb.indexOf(" ");
			} else if (descSb.indexOf(" ") == 0) {
				index = 1;
			}

			bufferText = new Text(descSb.substring(0, index));
			if (descSb.charAt(0) == CONTEXT_STRING) {
				// Context
				bufferText.getStyleClass().add("context-text");
				bufferText.setOnMouseClicked(clickOnContext());
			} else if (descSb.charAt(0) == CATEGORY_STRING) {
				// Category
				bufferText.getStyleClass().add("category-text");
				bufferText.setOnMouseClicked(clickOnCategory());
			}

			descSb.delete(0, index);
			parsedDesc.getChildren().add(bufferText);
		}

		return parsedDesc;
	}

	public String getFilter() {
		return _filterField.getText();
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
		_statusText.setText("Status: " + message);
		_statusText.getStyleClass().clear();
		_statusText.getStyleClass().add("status-message");
	}

	public void setError(String errorMessage) {
		_statusText.setText("Error: " + errorMessage);
		_statusText.getStyleClass().clear();
		_statusText.getStyleClass().add("error-message");
	}

	public void addCategoryIntoList(String Category) {
		Text categoryText = new Text(CATEGORY_STRING + Category);
		categoryText.getStyleClass().add("category-text");
		categoryText.setOnMouseClicked(clickOnCategory());
		_categoryList.getChildren().add(categoryText);
	}

	private EventHandler<MouseEvent> clickOnContext() {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println(((Text) event.getSource()).getText());
			}
		};
	}

	private EventHandler<MouseEvent> clickOnCategory() {
		return new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				System.out.println(((Text) event.getSource()).getText());
			}
		};
	}

	public void addContextIntoList(String Context) {
		Text contextText = new Text(CONTEXT_STRING + Context);
		contextText.getStyleClass().add("context-text");
		contextText.setOnMouseClicked(clickOnContext());
		_contextList.getChildren().add(contextText);
	}

	/*
	 * Code re-use from https://gist.github.com/jewelsea/
	 */
	private void makeScrollFadeable(final Node scroll) {
		final Node scrollbar = scroll.lookup(".scroll-bar:vertical");
		System.out.println(scroll);
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
					scrollbar.setOpacity(1);
					scroll.getStyleClass().remove("hide-thumb");
				}
			}
		});
	}
}

package chirptask.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.geometry.Insets;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 *
 * @author QuanYang
 */
public class MainGui extends Application {

	private final TextField _commandLineInterface = new TextField();
	private final TextField _filterField = new TextField();

	private final VBox _categoryList = new VBox();
	private final VBox _contextList = new VBox();
	private final VBox _taskViewByDate = new VBox();

	private final SortedMap<String, VBox> _taskViewDateMap = new TreeMap<>();
	private final ArrayList<Integer> _taskIndexToId = new ArrayList<>();

	private final String[] dayOfWeekString = new String[] { "Sunday", "Monday",
			"Tuesday", "Wednesday", "Thusday", "Friday", "Saturday" };
	private final String[] monthString = new String[] { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };

	@Override
	public void start(Stage primaryStage) {
		BorderPane border = new BorderPane();
		border.setStyle("-fx-background-color:white;");

		BorderPane headerBar = generateHeaderBar();
		border.setTop(headerBar);

		BorderPane mainDisplay = generateMainDisplay();
		border.setCenter(mainDisplay);

		VBox _trendingList = generateTrendingList();
		border.setRight(_trendingList);

		Scene scene = new Scene(border, 800, 600);
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(300);
		primaryStage.setMinWidth(500);
		// primaryStage.setResizable(false);
		primaryStage.setTitle("ChirpTask");
		primaryStage.show();

		// focus on CLI
		_commandLineInterface.requestFocus();

		addNewTaskViewDate(new Date());
		addNewTaskViewToDate(new Date(), 0, "TEST", "all-day", true);
		addNewTaskViewToDate(new Date(), 1, "TEST", "all-day", true);
		addNewTaskViewToDate(new Date(), 2, "TEST", "all-day", true);
		addNewTaskViewToDate(new Date(), 3, "TEST", "all-day", true);
		addNewTaskViewToDate(new Date(), 4, "TEST", "all-day", true);
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
		headerBar.setStyle("-fx-background-color:gainsboro;");

		Text sceneTitle = new Text("ChirpTask");
		sceneTitle.setFont(Font.font("Lucida Grande", FontWeight.NORMAL, 20));

		Text settingsButton = new Text("Settings");
		settingsButton.setFont(Font
				.font("Lucida Grande", FontWeight.NORMAL, 20));

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
		mainDisplay.setMinWidth(200);
		return mainDisplay;
	}

	private VBox generateTrendingList() {
		VBox trendingList = new VBox();
		trendingList.setPadding(new Insets(0));
		trendingList.setSpacing(0);
		trendingList.setMinWidth(180);
		trendingList
				.setStyle(""
						+ "-fx-border-width: 0px 0px 0px 1px;"
						+ "-fx-border-style: solid;"
						+ "-fx-border-color: transparent transparent transparent #ddd;");
		ScrollPane contextPane = generateContextList();
		ScrollPane categoryPane = generateCategoryList();

		VBox.setVgrow(categoryPane, Priority.ALWAYS);
		VBox.setVgrow(contextPane, Priority.ALWAYS);
		trendingList.getChildren().addAll(contextPane, categoryPane);
		return trendingList;
	}

	private ScrollPane generateContextList() {
		Text contextTitle = new Text("#Context");
		contextTitle.setFont(Font.font("Lucida Grande", FontWeight.BLACK, 14));

		_contextList.setPadding(new Insets(8));
		_contextList.setSpacing(5);
		_contextList.getChildren().add(contextTitle);

		ScrollPane contextScrollPane = new ScrollPane();
		contextScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		contextScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		contextScrollPane.setFocusTraversable(false);
		contextScrollPane.setContent(_contextList);
		contextScrollPane
				.setStyle(""
						+ "-fx-background-insets:0;"
						+ "-fx-border-width: 0px 0px 1px 0px;"
						+ "-fx-border-style: solid;"
						+ "-fx-border-color: transparent transparent #ddd transparent;");

		return contextScrollPane;
	}

	private ScrollPane generateCategoryList() {

		ScrollPane categoryScrollPane = new ScrollPane();
		categoryScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		categoryScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		categoryScrollPane.setContent(_categoryList);
		categoryScrollPane.setFocusTraversable(false);
		categoryScrollPane.setStyle("-fx-background-insets:0; ");

		_categoryList.setPadding(new Insets(8));
		_categoryList.setSpacing(5);

		Text categoryTitle = new Text("@Category");
		categoryTitle.setFont(Font.font("Lucida Grande", FontWeight.BLACK, 14));

		_categoryList.getChildren().add(categoryTitle);

		return categoryScrollPane;
	}

	private HBox generateFilterBox() {
		HBox filterBox = new HBox();
		filterBox.setAlignment(Pos.CENTER);
		filterBox.setPadding(new Insets(5));

		Text filterLabel = new Text("Filter: ");
		filterLabel.setFont(Font.font("Lucida Grande", FontWeight.NORMAL, 14));
		filterBox.getChildren().add(filterLabel);

		_filterField.setText("is:NotDone");
		HBox.setHgrow(_filterField, Priority.ALWAYS);

		filterBox.getChildren().add(_filterField);
		return filterBox;
	}

	private VBox generateUserInputAndStatusBar() {
		VBox mainDisplayBottom = new VBox();

		HBox userInputBox = new HBox();
		userInputBox.setPadding(new Insets(5));
		Text userInputLabel = new Text("Input: ");
		userInputLabel.setFont(Font
				.font("Lucida Grande", FontWeight.NORMAL, 14));

		HBox.setHgrow(_commandLineInterface, Priority.ALWAYS);

		userInputBox.setAlignment(Pos.CENTER);
		userInputBox.getChildren().add(userInputLabel);
		userInputBox.getChildren().add(_commandLineInterface);

		VBox statusBar = new VBox();
		statusBar.setStyle("-fx-background-color: gainsboro");
		Text statusText = new Text("Status: ");
		statusBar.setPadding(new Insets(5));
		statusBar.getChildren().add(statusText);

		mainDisplayBottom.getChildren().addAll(userInputBox, statusBar);

		return mainDisplayBottom;
	}

	private ScrollPane generateTaskView() {
		ScrollPane taskViewScrollPane = new ScrollPane();
		taskViewScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		taskViewScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		taskViewScrollPane.setStyle("" + "-fx-background-insets:0;"
				+ "-fx-background: white;");
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

	public String convertDateToString(Date date) {
		String parseDateToString = date.getDate() + "/" + date.getMonth() + "/"
				+ (1900 + date.getYear());
		return parseDateToString;
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

		taskPane.setStyle("" + "-fx-border-width: 0px 0px 1px 0px;"
				+ "-fx-border-style: dashed;"
				+ "-fx-border-color: transparent transparent #ccc transparent;");
		taskPane.setLeft(checkBoxPane);
		taskPane.setCenter(descriptionBox);
		taskPane.setRight(taskTime);

		_taskViewDateMap.get(convertDateToString(date)).getChildren()
				.add(taskPane);

		return true;
	}

	private Text generateTaskTimeText(String time) {
		Text taskTime = new Text(time);
		taskTime.setFont(Font.font("Lucida Grande", 14));
		taskTime.setFill(Color.web("#999"));
		return taskTime;
	}

	private Pane generateTaskCheckBox(boolean Done) {
		Pane checkBoxPane = new Pane();
		checkBoxPane.setMaxWidth(20);
		CheckBox markTaskAsDone = new CheckBox();
		markTaskAsDone.setSelected(Done);
		checkBoxPane.getChildren().add(markTaskAsDone);
		return checkBoxPane;
	}

	private BorderPane generateTaskViewHeader(Date date) {

		Text dayLabel = new Text();
		dayLabel.setText(dayOfWeekString[date.getDay()]);
		dayLabel.setFont(Font.font("Lucida Grande", FontWeight.BOLD, 14));
		dayLabel.setFill(Color.web("#777"));
		Text dateLabel = new Text();
		dateLabel.setText(date.getDate() + " " + monthString[date.getMonth()]
				+ ", " + (1900 + date.getYear()));
		dateLabel.setFont(Font.font("Lucida Grande", FontWeight.BOLD, 14));
		dateLabel.setFill(Color.web("#777"));

		BorderPane taskViewHeader = new BorderPane();
		taskViewHeader.setPadding(new Insets(5, 5, 3, 5));
		taskViewHeader.setLeft(dayLabel);
		taskViewHeader.setRight(dateLabel);

		taskViewHeader
				.setStyle(""
						+ "-fx-background-color:white;"
						+ "-fx-border-width: 0px 0px 3px 0px;"
						+ "-fx-border-style: solid;"
						+ "-fx-border-color: transparent transparent #ddd transparent;");
		return taskViewHeader;
	}

	private HBox generateTaskDescription(String description) {
		HBox descriptionBox = new HBox();
		descriptionBox.setPadding(new Insets(0, 8, 0, 8));
		TextFlow taskDescription = new TextFlow();

		Text bufferText = new Text("@CS2103T ");
		bufferText.setFill(Color.web("#09F"));
		taskDescription.getChildren().add(bufferText);

		bufferText = new Text("#Milestone1");
		bufferText.setFill(Color.web("#F33"));
		taskDescription.getChildren().add(bufferText);

		taskDescription.getChildren().add(new Text(description));

		descriptionBox.setAlignment(Pos.CENTER_LEFT);
		descriptionBox.getChildren().add(taskDescription);
		return descriptionBox;
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

	public void addContextIntoList(String Context) {
		_contextList.getChildren().add(new Text("#" + Context));
	}

	public void addCategoryIntoList(String Category) {
		_categoryList.getChildren().add(new Text("@" + Category));
	}
}

package nicholasBenson;

import java.io.File;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;


public class GenAssist extends Application {
	
	// Whether or not the application is running as a standalone JavaFX
	// application or in an embedded TheGenesisSystem JFXPanel.
	private boolean standalone = false;

	private Scene primaryScene;
	private final String titlePrefix = "GenAssist";
	private final int primaryStageDefaultWidth = 800;
	private final int primaryStageDefaultHeight = 600;

	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem openMenuItem, saveMenuItem, saveAsMenuItem;
	private Menu storyMenu;
	private MenuItem analyzeMenuItem;
	private StoryEditor storyEditor;
	
	/** (Standalone only) Application entry point. */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		standalone = true;
		primaryStage.setScene(getPrimaryScene());
		
		primaryStage.setTitle(titlePrefix + " - " + storyEditor.getTitle());
		primaryStage.setWidth(primaryStageDefaultWidth);
		primaryStage.setHeight(primaryStageDefaultHeight);
		primaryStage.show();
		

		// (Standalone only) Title bar contains name of the current file.
		this.storyEditor.titleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observed,
					String oldString, String newString) {
				primaryStage.setTitle(titlePrefix + " - " + storyEditor.getTitle());
			}
		});
		
	}
	
	/** buildPrimaryScene Returns a JavaFX Scene graph for the primary editing window.*/
	public Scene buildPrimaryScene() {

		// Root of the scene graph.
		StackPane uiRoot = new StackPane();

		// Menu bar.
		this.menuBar = new MenuBar();
		this.menuBar.setUseSystemMenuBar(standalone);
		this.fileMenu = new Menu("File");
		this.openMenuItem = new MenuItem("Open...");
		this.openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
		this.openMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				openStoryFile();
			}
		});
		this.saveMenuItem = new MenuItem("Save...");
		this.saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
		this.saveMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				saveStoryFile();
			}
		});
		this.saveAsMenuItem = new MenuItem("Save As...");
		this.saveAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
		this.saveAsMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				saveStoryFileAs();
			}
		});
		this.fileMenu.getItems().addAll(openMenuItem, saveMenuItem, saveAsMenuItem);
		this.storyMenu = new Menu("Story");
		this.analyzeMenuItem = new MenuItem("Analyze Story...");
		this.analyzeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
		this.analyzeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				analyzeStory();
			}
		});
		this.storyMenu.getItems().addAll(analyzeMenuItem);
		this.menuBar.getMenus().addAll(fileMenu, storyMenu);

		// Story editing node as an extended HBox.
		this.storyEditor = new StoryEditor();
		
		if (!standalone) {
			VBox vbox = new VBox();
			vbox.getChildren().addAll(menuBar, storyEditor);
			VBox.setVgrow(storyEditor, Priority.ALWAYS);
			uiRoot.getChildren().add(vbox);
		}
		else {
			uiRoot.getChildren().addAll(menuBar, storyEditor);
		}
		
		return new Scene(uiRoot);
	}

	/** openStoryFile Opens a text file in the StoryEditor for editing. */
	private void openStoryFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Story File");
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("Text Files", "*.txt"));
		File fileToOpen = fileChooser.showOpenDialog(primaryScene.getWindow());

		if (fileToOpen != null) {
			storyEditor.open(fileToOpen);
		} else {
			//Mark.err("Chosen file is null.");
		}
	}

	/** saveStoryFile Saves the text in the StoryEditor to a file. */
	private void saveStoryFile() {
		File saveFile = storyEditor.getFile();
		
		if (saveFile == null) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Story File");
			fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
			saveFile = fileChooser.showSaveDialog(primaryScene.getWindow());
		}
		
		storyEditor.save(saveFile);
	}

	/** saveStoryFileAs Saves the text in the StoryEditor to a new file. */
	private void saveStoryFileAs() {
		File saveFile;
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Story File");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));
		saveFile = fileChooser.showSaveDialog(primaryScene.getWindow());
		
		storyEditor.save(saveFile);
	}
	
	/** analyzeStory Launches an analysis window targeting the currently open story. */
	private void analyzeStory() {
		String storyToAnalyze = storyEditor.getStoryText();
		
		AnalysisPane analysisPane = new AnalysisPane();
		analysisPane.setStoryText(storyToAnalyze);
		
		Stage analysisStage = new Stage();
		analysisStage.setScene(new Scene(analysisPane));
		analysisStage.setWidth(800);
		analysisStage.setTitle("GenAssist Analysis - " + storyEditor.getTitle());
		analysisStage.show();
		
		analysisPane.analyzeStoryWithThread(); // Go!
	}

	/** getPrimaryScene returns the Scene object for the primary editing window.*/
	public Scene getPrimaryScene() {
		if (primaryScene == null) {
			primaryScene = buildPrimaryScene();
		}
		return primaryScene;
	}
	

	/** JavaFX entry point. **/
	public static void main(String[] args) {
		launch(args);
	}

}
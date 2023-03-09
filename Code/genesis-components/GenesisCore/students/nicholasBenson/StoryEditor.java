package nicholasBenson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import utils.Mark;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


/** An extended JavaFX StackPane that contains a text editor and
 * convenience methods for doing I/O with the its textual contents. */
public class StoryEditor extends StackPane {
	
	private StringProperty title = new SimpleStringProperty(null);
	public StringProperty titleProperty() { return title; }
	
	private boolean saved = false;
	private File storyFile = null;
	
	private Font font;
	private Text lineNumbers;
	private GeneseseArea geneseseArea;
	
	public StoryEditor() {
		super();
		
		// Font to use for both line numbers and the writing area.
		font = Font.font("Monospaced", 14);
		
		
		////////////////////////
		// Node construction. //
		
		// Line numbers as a Text node.
		lineNumbers = new Text();
		lineNumbers.setFont(this.font);
		lineNumbers.setFill(Color.BLACK);
		//lineNumbers.setTranslateY(6);
		lineNumbers.setTextAlignment(TextAlignment.LEFT);
		lineNumbers.setText(" 1 ");
		
		ScrollPane lineScrollPane = new ScrollPane();
		lineScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		lineScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		lineScrollPane.setFitToHeight(true);
		lineScrollPane.setFitToWidth(true);
		lineScrollPane.setContent(lineNumbers);
		lineScrollPane.setSnapToPixel(true);
		
		// Typing area as a TextArea node.
		geneseseArea = new GeneseseArea();
		geneseseArea.setMaxHeight(Double.MAX_VALUE);
		geneseseArea.setFont(this.font);

		
		// Bidirectionally bind the vertical scroll state of the line numbers and
		// the story editing area. This isn't perfect, but with padding, and no
		// horizontal scrollbar on, it's at least mostly correct.
		Bindings.bindBidirectional(
				lineScrollPane.vvalueProperty(),
				((GeneseseAreaSkin)geneseseArea.getSkin()).getScrollPaneVValueProperty());
	    lineScrollPane.setPadding(new Insets(5, 0, 5, 0));
		geneseseArea.getGASkin().getScrollPane().setHbarPolicy(ScrollBarPolicy.NEVER);

		HBox hbox = new HBox();
		hbox.getChildren().addAll(lineScrollPane, geneseseArea);
		HBox.setHgrow(geneseseArea, Priority.ALWAYS);
		
		// Add to scene graph.
		this.getChildren().addAll(hbox);
		
		
		////////////////////
		// Data bindings. //
		
		// Bind the number of rows in the TextArea to the line numbers displayed.
		geneseseArea.lineCountProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(
						ObservableValue<? extends Number> observed,
						Number oldValue, Number newValue) {
					updateLineNumberDisplay(newValue.intValue());
				}
		});
		
		/*
		// Bind the height of the GeneseseArea to the height of the window.
		this.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observed,
					Number oldHeight, Number newHeight) {
				// geneseseArea.setPrefHeight(newHeight.doubleValue());
			}
		});*/
		
	}

	protected void updateLineNumberDisplay(int numLines) {
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i <= numLines; i++) {
			builder.append(String.format(" %d ", i));
			if (i < numLines) builder.append("\n");
		}
		lineNumbers.setText(builder.toString());
	}
	
	
	////////////////////////////////////////////////////////
	// Public Methods
	
	public String getTitle() {
		if (this.title.getValue() == null) {
			return "(unsaved story)";
		}
		return this.title.getValue();
	}
	
	public boolean getSaved() {
		return saved;
	}
	
	public File getFile() {
		return storyFile;
	}

	public String getStoryText() {
		return this.geneseseArea.getText();
	}

	public void save(File file) {
		if (file != null) {
			FileWriter writer;
			try {
				writer = new FileWriter(file);
				writer.write(geneseseArea.getText());
				writer.close();
				// Save success, story has a file now.
				title.set(file.getName());
				storyFile = file;
				saved = true;
			} catch (IOException e) {
				Mark.err("IOException trying to save the story.");
			}
		}
		else {
			Mark.say("Can't save a null file.");
		}
	}
	
	public void open(File file) {
		if (file != null) {
			try {
				String fileContent = readFile(file.getAbsolutePath(), Charset.defaultCharset());
				// Open success, story has a file now.
				title.set(file.getName());
				storyFile = file;
				geneseseArea.replaceAllText(fileContent);
				saved = true;
			} catch (IOException e) {
				Mark.err("IOException trying to open the story.");
			}
		}
		else {
			Mark.err("Can't open a null file.");
		}
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}

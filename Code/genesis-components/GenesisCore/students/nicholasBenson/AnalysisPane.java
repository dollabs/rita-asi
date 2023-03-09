package nicholasBenson;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Mark;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

public class AnalysisPane extends StackPane {
	
	// State
	private boolean busy = false;
	
	// Model
	private String storyText = null;
	private ArrayList<String> rawSentences = new ArrayList<String>();
	private HashMap<Integer, Integer> sentenceIndexToLineNumberMap = null;
	
	// View
	private ProgressBar progressBar;
	private Text progressText;
	
	public ListProperty<Sentence> getSentencesProperty() { return sentencesProperty; }
	private ListProperty<Sentence> sentencesProperty;
	
	private ListView<Sentence> sentenceListView;
	
	private TextArea storyInfoTextArea;
	private TextArea sentenceInfoTextArea;
	
	// Sentence Tests
	private ArrayList<SentenceTest> sentenceTests = new ArrayList<SentenceTest>();
	
	//private DoubleProperty analysisProgress = new SimpleDoubleProperty();
	//public DoubleProperty analysisProgressProperty() { return analysisProgress; }
	
	public AnalysisPane() {
		super();
		initUI();
		initTests();
	}
	
	private void initUI() {
		
		this.progressBar = new ProgressBar(0);
		progressBar.setPrefHeight(25);
		progressBar.setMinHeight(25);
		progressBar.setMaxHeight(25);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		
		this.progressText = new Text(" Initializing...");
		StackPane.setAlignment(progressText, Pos.CENTER_LEFT);
		progressText.setTranslateX(4);
		
		StackPane progressStack = new StackPane();
		progressStack.getChildren().addAll(progressBar, progressText);
		
		storyInfoTextArea = new TextArea();
		storyInfoTextArea.setEditable(false);
		storyInfoTextArea.setMinHeight(100);
		storyInfoTextArea.setMaxHeight(100);
		storyInfoTextArea.setText("Story analysis initializing.");
		
		sentencesProperty = new SimpleListProperty<Sentence>(FXCollections.observableList(new ArrayList<Sentence>()));
		this.sentenceListView = new ListView<Sentence>(sentencesProperty);
		sentenceListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observed,
					Number oldValue, Number newValue) {
				if (newValue.intValue() != -1) {
					sentenceInfoTextArea.setText(sentencesProperty.get(newValue.intValue()).getBreakdownAsText());
				}
				else {
					Platform.runLater(new Runnable() {
						@Override public void run() {
							sentenceListView.getSelectionModel().select(oldValue.intValue());
						}
					});
				}
			}
		});
		sentenceListView.setCellFactory(new Callback<ListView<Sentence>, ListCell<Sentence>>() {
			@Override public ListCell<Sentence> call(ListView<Sentence> list) {
				return new SentenceCell();
			}
		});
		 
		ScrollPane sentenceListScrollPane = new ScrollPane();
		sentenceListScrollPane.setFitToHeight(true);
		sentenceListScrollPane.setFitToWidth(true);
		sentenceListScrollPane.setContent(sentenceListView);
		
		TextFlow sentenceInfoTextFlow = new TextFlow();
		//sentenceInfoTextFlow
		
		sentenceInfoTextArea = new TextArea();
		sentenceInfoTextArea.setEditable(false);
		sentenceInfoTextArea.setText("Select a sentence from the left panel.");
		HBox.setHgrow(sentenceInfoTextArea, Priority.ALWAYS);

		HBox hbox = new HBox();
		hbox.getChildren().addAll(sentenceListScrollPane, sentenceInfoTextArea);
		VBox.setVgrow(hbox, Priority.ALWAYS);
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(progressStack, storyInfoTextArea, hbox);
		
		this.getChildren().addAll(vbox);
		
	}
	
	private void initTests() {
		sentenceTests = GeneseseSpec.getAllTests();
	}
	
	
	public void analyzeStoryWithThread() {
		if (busy) {
			Mark.err("This AnalysisPane is already busy analyzing a story!");
			return;
		}
		else {
			busy = true;
		}
		if (storyText == null) {
			Mark.err("Can't analyze a null story!");
			return;
		}
		new Thread() {
			public void run() {
				
				String commentScrubbedStory = scrubComments(storyText);
				
				if (commentScrubbedStory == null) {
					Mark.err("An error occurred in the story while processing out its comments.");
					reportAnalysisComplete(false, "Malformed comment."); // failure + failure text
				}
				
				// Split the text of the story into sentences.
				rawSentences = splitIntoSentences(commentScrubbedStory);
				
				reportNumSentences(rawSentences.size());
				
				for (int i = 0; i < rawSentences.size(); i++) {
					String curRawSentence = rawSentences.get(i);
					
					Sentence newSentence = new Sentence(curRawSentence);
					newSentence.setIndex(i);
					newSentence.setLine(sentenceIndexToLineNumberMap.get(i));
					sentencesProperty.add(newSentence);
					newSentence.addListener(new InvalidationListener() {
						// Listener to allow the ListView to update its display of the sentence
						// when something in the sentence changes -- e.g. Test Results added.
						@Override
						public void invalidated(Observable observableSentence) {
							sentenceListView.fireEvent(new ListView.EditEvent<Sentence>(sentenceListView, ListView.editCommitEvent(), newSentence, newSentence.getIndex()));
						}
					});
				}
				
				// Sentence splitting tests:
				// Mark.say("Hi from the worker! Here are the sentences:");
				// for (String s : sentences) {
				//    Mark.say(s); }
				// Mark.say("There were " + sentences.length + " of them.");
				// Mark.say("And here are the indices and their line numbers:");
				// for (int i = 0; i < sentences.length; i++) {
				//    Mark.say("Sentence " + (i+1) + " is on line " + sentenceIndexToLineNumberMap.get(i)); }
				
				// Analyze each individual sentence in turn.
				for (Sentence sentence : sentencesProperty.getValue()) {
					analyzeSentence(sentence);
				}
				
				// Report the analysis as finished.
				reportAnalysisComplete(true /* successful */); 
				
			}
		}.start();
	}
	
	/** Removes any comments from the input text without affecting line numbers.
	 *  Mostly copied from FileSourceReader.removeComments().
	 */
	private String scrubComments(String string) {
		if (string == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer(string);
		int index1;
		int index2;
		while ((index1 = sb.indexOf("/*")) >= 0) {
			index2 = sb.indexOf("*/", index1+2);
			StringBuilder newlines = new StringBuilder();
			for (int i = index1; i < index2+2; i++) {
				if (sb.charAt(i) == '\n') {
					newlines.append('\n');
				}
			}
			if (index1 > index2 + 2) {
				Mark.err("Malformed multiline comment found in story! Aborting.");
				return null;
			}
			sb.replace(index1, index2 + 2, newlines.toString());
		}
		while ((index1 = sb.indexOf("//")) >= 0) {
			index2 = sb.indexOf("\n", index1);
			if (index2 < 0) {
				sb.delete(index1, sb.length() + 1);
			}
			else {
				// Mark.say("Removing:", sb.substring(index1, index2+1));
				sb.replace(index1, index2+1, "\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Calls Genesis-source-copied methods to split the story into sentences
	 * to ensure accurate splitting.
	 */
	private ArrayList<String> splitIntoSentences(String story) {
		return splitText(new String(story));
	}
	
	/**
	 * Copied from FileSourceReader, 12/1/2015, to match Genesis.
	 * "Search for first period, not followed by a number, and first question mark. Split on which comes first."
	 * 
	 * Modified 12/1/2015 to remember the line number index of the sentence, needed for analysis.
	 * 
	 * Copying code and modifying it in this way isn't good for code maintainability, so it would
	 * be nice to have a public method that can be used straight from Genesis to get the desired
	 * data on sentences separated from input stories.
	 */
	private ArrayList<String> splitText(String string) {
		
		int lineNumber = 1;			// These lines added for modification
		int curSentenceIdx = 0;		//
		// and this:
		HashMap<Integer, Integer> sentenceIndexToLineNumberMap = new HashMap<Integer, Integer>();
		
		// Also this -- must take the leading whitespace into account for line tracking.
		for (int i = 0; i < string.length(); i++) {
			char iChar = string.charAt(i);
			if (Character.isWhitespace(iChar)) {
				if (iChar == '\n') {
					lineNumber++;
				}
			}
			else {
				break;
			}
		}
		
		ArrayList<String> result = new ArrayList<String>();
		while (true) {
			int terminator = findSentenceTerminator(string);
			if (terminator >= 0) {
				// Must be something there
				// Mark.say("String", string, string.length());
				String sentence = string.substring(0, terminator + 1).trim();
				string = string.substring(terminator + 1);
				int numNewLinesToNextPotentialSentence = 0;						// These
				for (int i = 0; i < string.length(); i++) {						// lines
					char iChar = string.charAt(i);								// added
					if (Character.isWhitespace(iChar)) {						// for
						if (iChar == '\n') {									// modification
							numNewLinesToNextPotentialSentence++;				//
						}														//
					}															//
					else {														//
						break;													//
					}															//
				}																//
				// Sentences aren't supposed to contain newlines, but if I don't//
				// check for them here, it'll screw up GenAssist's line number  //
				// references.													//
				for (int i = 0; i < sentence.length(); i++) {					//
					if (sentence.charAt(i) == '\n') {							//
						lineNumber++;											//
					}															//
				}																//
				sentenceIndexToLineNumberMap.put(curSentenceIdx++, lineNumber); //
				lineNumber += numNewLinesToNextPotentialSentence;				//
				result.add(sentence);
			}
			else {
				// Nothing there, quit
				break;
			}
		}
		// result.stream().forEach(f -> Mark.say("Sentence", f));
		this.sentenceIndexToLineNumberMap = sentenceIndexToLineNumberMap;		// added for modification
		return result;
	}
	
	/**
	 * Copied from FileSourceReader, 12/1/2015, to match Genesis, as above. No changes made.
	 */
	private int findSentenceTerminator(String string) {
		int questionMarkIndex = string.indexOf('?');

		int start = 0;
		int periodIndex = -1;

		while (true) {
			periodIndex = string.indexOf('.', start);
			if (periodIndex < string.length() - 1) {
				// Ok, there must be another character
				if (Character.isLetterOrDigit(string.charAt(periodIndex + 1))) {
					// Cannot be a sentence terminator, so keep going.
					start = periodIndex + 1;
					continue;
				}
				else {
					// Evidently, a sentence terminator, break
					break;
				}
			}
			// Period is final character, break
			break;

		}
		// Now, see which is both found and least
		if (questionMarkIndex >= 0 && periodIndex >= 0) {
			return Math.min(questionMarkIndex, periodIndex);
		}
		else if (periodIndex >= 0) {
			return periodIndex;
		}
		else if (questionMarkIndex >= 0) {
			return questionMarkIndex;
		}
		// Nothing found
		return -1;

	}
	
	private void reportProgressFromThread(double progress) {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				progressBar.setProgress(progress);
			}
		});
	}
	
	private void reportNumSentences(int numSentences) {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				storyInfoTextArea.setText("Number of sentences: " + numSentences);
			}
		});
	}
	
	private void analyzeSentence(Sentence sentence) {
		
		// Report the ensuing analysis of the current sentence.
		reportBeginningOfSentenceAnalysis(sentence);

		// Run all sentence tests on the current sentence.
		for (SentenceTest sentenceTest : sentenceTests) {
			TestResult testResult = sentenceTest.test(sentence);
			
			// Report the results of each test as they are completed.
			reportTestResultForSentence(sentence, testResult);
			
		}
		
		// Report the finishing of the analysis of the current sentence.
		reportEndOfSentenceAnalysis(sentence);
		
	}
	
	private void reportBeginningOfSentenceAnalysis(Sentence sentence) {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				progressText.setText(String.format("Analyzing sentence %d (line %d, %s)",
						sentence.getIndex()+1, sentence.getLine(), sentence));
			}
		});
	}
	
	private void reportTestResultForSentence(Sentence sentence, TestResult testResult) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				sentence.addTestResult(testResult);
				
				// Update the text breakdown display for the sentence if this one is the currently selected sentence.
				if (sentenceListView.selectionModelProperty().getValue().getSelectedItem() == sentence) {
					sentenceInfoTextArea.setText(sentenceListView.getSelectionModel().getSelectedItem().getBreakdownAsText());
				}
			}
		});
	}
	
	private void reportEndOfSentenceAnalysis(Sentence sentence) {
		this.reportProgressFromThread((sentence.getIndex()+1) / (double)sentencesProperty.size());
	}
	
	private void reportAnalysisComplete(boolean successful) {
		reportAnalysisComplete(successful, null);
	}
	private void reportAnalysisComplete(boolean successful, String errorMessage) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				busy = false;
				if (!successful) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText("Couldn't analyze the story.");
					alert.setContentText(
							"The analysis failed with the following error message: \n"
					      + errorMessage);
					alert.show();
					progressText.setText("Analysis failed: " + errorMessage);
				}
				else {
					progressText.setText("Analysis completed successfully.");
				}
			}
		});
	}
	

	public String getStoryText() {
		return storyText;
	}
	public void setStoryText(String storyText) {
		this.storyText = storyText;
	}
	
	/** 
	 * A custom cell factory for JavaFX's ListView, specialized to
	 * color the text of the Sentence in the list based on the Sentence's
	 * known test results.
	 * 
	 * @author nickbenson
	 */
	public class SentenceCell extends ListCell<Sentence> {
		
		/** See https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Cell.html **/
		
		public SentenceCell() { }
	       
	     @Override protected void updateItem(Sentence sentence, boolean empty) {
	         // calling super here is very important - don't skip this!
	         super.updateItem(sentence, empty);
	         
	         setText(sentence == null ? "" : sentence.toString());
	         
	         if (sentence != null) {
	        	 
	        	 /* For some reason, isSelected() is always returning false,
	        	  * even when a particular cell is obviously in a colored-in,
	        	  * selected state. This is contrary to what all of the JavaFX
	        	  * API documentation says about the use of isSelected() in
	        	  * this context, so I'm not sure what's going on.
	        	  * nicholasBenson, 12/3/2015
	        	  * 
	        	  * Update 12/4/2015:
	        	  * isSelected() appears to work correctly when a Sentence is
	        	  * invalidated, causing the cell to be updated, but doesn't
	        	  * work just when a new item is selected. This results in
	        	  * strange visual behavior, so I'm removing the alternate
	        	  * colors for selection for now.
	        	  */
	        	 
	        	 if (!sentence.hasTestResults()) {
	        		 //setTextFill(isSelected() ? Color.WHITE : Color.DARKGRAY);
	        		 setTextFill(Color.DARKGRAY);
	        	 }
	        	 else if (sentence.failsAnyCriticalTests()) {
	        		 //setTextFill(isSelected() ? Color.LIGHTPINK : Color.RED);
	        		 setTextFill(Color.RED);
	        	 }
	        	 else if (sentence.failsAnyMaybeCriticalTests()) {
	        		 //setTextFill(isSelected() ? Color.LIGHTGOLDENRODYELLOW : Color.GOLDENROD);
	        		 setTextFill(Color.GOLDENROD);
	        	 }
	        	 else {
	        		 //setTextFill(isSelected() ? Color.WHITE : Color.DARKGREEN);
	        		 setTextFill(Color.DARKGREEN);
	        	 }
	         }
	     }
		
	}

}

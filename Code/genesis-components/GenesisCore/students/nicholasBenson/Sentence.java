package nicholasBenson;

import java.util.ArrayList;

import frames.entities.Entity;
import nicholasBenson.SentenceTest.FailureMeaning;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import start.Start.Triple;
import utils.Mark;

public class Sentence implements Observable {
	
	private String originalSentence;
	private int indexInStory = -1;
	private int lineNumberInStory = -1;
	
	private boolean hasTestResults = false;
	private boolean failsAnyCriticalTests = false;
	private boolean failsAnyMaybeCriticalTests = false;
	
	private ArrayList<InvalidationListener> invalidationListeners = new ArrayList<InvalidationListener>();
	
	public Sentence(String text) {
		originalSentence = text;
	}
	
	private ArrayList<TestResult> testResults = new ArrayList<TestResult>();
	private ListProperty<TestResult> testResultsProperty = new SimpleListProperty<TestResult>(FXCollections.observableArrayList(testResults));
	public ListProperty<TestResult> getTestResultsProperty() { return testResultsProperty; }
	
	public void addTestResult(TestResult testResult) {
		if (!testResult.getSentenceText().equals(this.originalSentence)) {
			Mark.err("The given test result is not from a test for this sentence.");
			return;
		}
		
		if (!testResult.succeeded) {
			switch (testResult.getTest().getFailureMeaning()) {
			case WILL_CAUSE_PROBLEMS:
				failsAnyCriticalTests = true;
				break;
			case MIGHT_BE_FINE:
				failsAnyMaybeCriticalTests = true;
				break;
			default:
				break;
			}
		}

		hasTestResults = true;
		testResults.add(testResult);
		
		notifyInvalidationListeners(); // The Sentence object has been modified.
	}
	
	public String getBreakdownAsText() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format("Sentence %d, line %d: %s", indexInStory, lineNumberInStory, originalSentence));
		builder.append("\n");
		
		builder.append("\n\n");
		
		builder.append("Test Results");
		if (testResults.isEmpty()) {
			builder.append("\n\n");
			builder.append("No tests have been reported for this sentence.");
		}
		else {
			for (TestResult testResult : testResults) {
				builder.append("\n\n");
				builder.append(String.format("%s: %s\n", testResult.getTest().getName(), (testResult.succeeded? "Succeeded" : "Failed")));
				if (!testResult.succeeded) {
					if (testResult.getTest().getFailureMeaning() == FailureMeaning.WILL_CAUSE_PROBLEMS) {
						builder.append("Warning! This failure means Genesis won't read your story!\n");
					}
				}
				builder.append(String.format("\tReason: %s\n", testResult.reason));
				builder.append(String.format("\tRecommendation: %s\n", testResult.recommendation));
				if (testResult.hasData()) {
					if (testResult.getTest() instanceof GeneseseSpec.StartParsableTest) {
						Object[] triplesAndGenerated = (Object[])testResult.getData();
						ArrayList<Triple> triples = (ArrayList<Triple>) triplesAndGenerated[0];
						String startGeneratedSentence = (String) triplesAndGenerated[1];
						builder.append("\tStart-generated sentence: \n\t\t");
						builder.append(startGeneratedSentence.trim());
						builder.append("\n\tStart triples generated: \n");
						for (String tripleString : triplesToStrings(triples)) {
							builder.append(String.format("\t\t%s\n", tripleString));
						}
					}
					else if (testResult.getTest() instanceof GeneseseSpec.GeneseseTest) {
						Object[] sequenceAndGenerated = (Object[])testResult.getData();
						Entity sequence = (Entity)sequenceAndGenerated[0];
						ArrayList<String> innereseGeneratedSentences = (ArrayList<String>)sequenceAndGenerated[1];
						builder.append("\tInnerese:\n");
						builder.append(String.format("\t\t%s\n", sequence.toString()));
						builder.append("\tGenerated sentence from Innerese:\n");
						for (String sentence : innereseGeneratedSentences) {
							builder.append(String.format("\t\t%s\n", sentence.trim()));
						}
					}
				}
			}
		}
		
		builder.append("\n");
		
		return builder.toString();
	}
	
	private ArrayList<String> triplesToStrings(ArrayList<Triple> triples) {
		ArrayList<String> strings = new ArrayList<String>();
		for (Triple triple : triples) {
			strings.add(triple.toString());
		}
		return strings;
	}
	
	public String getText() {
		return originalSentence;
	}
	public int getIndex() {
		return indexInStory;
	}
	public void setIndex(int indexInStory) {
		this.indexInStory = indexInStory;
		notifyInvalidationListeners();
	}
	public int getLine() {
		return lineNumberInStory;
	}
	public void setLine(int lineNumberInStory) {
		this.lineNumberInStory = lineNumberInStory;
		notifyInvalidationListeners();
	}
	@Override
	public String toString() {
		return originalSentence;
	}
	public boolean hasTestResults() {
		return hasTestResults;
	}
	public boolean failsAnyCriticalTests() {
		return failsAnyCriticalTests;
	}
	public boolean failsAnyMaybeCriticalTests() {
		return failsAnyMaybeCriticalTests;
	}
	
	
	// Invalidation methods
	private void notifyInvalidationListeners() {
		for (InvalidationListener listener : invalidationListeners) {
			listener.invalidated(this);
		}
	}
	@Override
	public void addListener(InvalidationListener invalidationListener) {
		this.invalidationListeners.add(invalidationListener);
	}
	@Override
	public void removeListener(InvalidationListener invalidationListener) {
		this.invalidationListeners.remove(invalidationListener);
	}

}
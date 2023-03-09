package nicholasBenson;

import generator.Generator;
import generator.RoleFrame;

import java.util.ArrayList;

import frames.entities.Entity;
import start.Start;
import start.Start.Triple;
import translator.BasicTranslator;
import utils.Mark;

/**
 * The Genesese spec in code used by GenAssist to
 * test various properties of an input string to understand
 * it as an English sentence, a Genesese sentence, incorrectly
 * formatted, etc.
 * 
 * @author nickbenson
 *
 */
public class GeneseseSpec {
	
	// All tests
	private static FormattingTest formattingTest;
	private static StartParsableTest startParsableTest;
	private static GeneseseTest geneseseTest;
	private static ConceptPatternDeclarationTest conceptPatternDeclarationTest;
	
	public static ArrayList<SentenceTest> getAllTests() {
		ArrayList<SentenceTest> allTests = new ArrayList<SentenceTest>();
		allTests.add(getFormattingTest());
		allTests.add(getStartParsableTest());
		allTests.add(getGeneseseTest());
		allTests.add(getConceptPatternDeclarationTest());
		return allTests;
	}
	
	
	/* 
	 * Formatting
	 */
	public static boolean doesNotContainNewlines(String sentence) {
		return !sentence.contains("\n");
	}
	public static boolean doesNotContainExclamationPoints(String sentence) {
		return !sentence.contains("!");
	}
	public static int getFirstIndexOfNewline(String sentence) {
		return sentence.indexOf("\n");
	}
	public static FormattingTest getFormattingTest() {
		if (formattingTest == null) {
			formattingTest = new FormattingTest();
		}
		return formattingTest;
	}
	public static class FormattingTest extends SentenceTest {
		@Override
		public String getName() { return "Formatting Test"; }
		@Override
		public TestResult test(String sentence) {
			if (!doesNotContainNewlines(sentence)) {
				return new TestResult(sentence, this, false,
						"The sentence contains one or more newlines.",
						"Remove all newlines from the sentence.",
						null);
			}
			if (!doesNotContainExclamationPoints(sentence)) {
				return new TestResult(sentence, this, false,
						"The sentence contains one or more exclamation points.",
						"Genesis can't understand exclamation points! Replace them with periods.",
						null);
			}
			return new TestResult(sentence, this, true,
					"The sentence is formatted correctly.",
					null,
					null);
		}
		@Override
		public FailureMeaning getFailureMeaning() {
			return FailureMeaning.WILL_CAUSE_PROBLEMS;
		}
	}
	
	
	/*
	 * Start
	 */
	public static boolean isStartParsable(String sentence) {
		return Start.getStart().getTriples(Start.getStart().processWithStart(sentence)).isEmpty();
	}
	public static String getStartTriplesHTML(String sentence) {
		return Start.getStart().processWithStart(sentence);
	}
	public static ArrayList<Triple> getStartTriples(String sentence) {
		return Start.getStart().getTriples(Start.getStart().processWithStart(sentence));
	}
	public static String getStartGeneratedSentence(String sentence) {
		try {
			return Start.getStart().generate(Start.getStart().processWithStart(sentence));
		}
		catch (NullPointerException e) {
			Mark.err("Could not generate sentence via Start from " + sentence);
			return "";
		}
	}
	public static StartParsableTest getStartParsableTest() {
		if (startParsableTest == null) {
			startParsableTest = new StartParsableTest();
		}
		return startParsableTest;
	}
	public static class StartParsableTest extends SentenceTest {
		@Override public String getName() { return "Start-Parsable Test"; }
		@Override public TestResult test(String sentence) {
			ArrayList<Triple> startTriples = getStartTriples(sentence);
			String generatedSentence = getStartGeneratedSentence(sentence);
			if (startTriples.isEmpty()) {
				return new TestResult(sentence, this, false,
						"Start is unable to create triples from this sentence.",
						"There could be any number of reasons why this is the case...",
						null);
						//TODO: I'd like to implement a better recommendation engine...
						// Could easily be done right here in the test class! Do some logic to the sentence!
			}
			Object[] triplesAndSentence = new Object[] { startTriples, generatedSentence };
			return new TestResult(sentence, this, true,
					"Start successfully parsed the sentence into triples.",
					null,
					triplesAndSentence);
		}
		@Override public FailureMeaning getFailureMeaning() {
			return FailureMeaning.WILL_CAUSE_PROBLEMS;
		}
	}

	
	/*
	 * Genesese
	 */
	public static boolean isGenesese(String sentence) {
		return isStartParsable(sentence);
	}
	public static GeneseseTest getGeneseseTest() {
		if (geneseseTest == null) {
			geneseseTest = new GeneseseTest();
		}
		return geneseseTest;
	}
	public static class GeneseseTest extends SentenceTest {
		@Override public String getName() { return "Genesese Test"; }
		@Override public TestResult test(String sentence) {

			Entity sequence = null;
			ArrayList<String> innereseGeneratedSentences = new ArrayList<String>();
			boolean translated = false;
			
			try { // try to translate the sentence to Innerese
				sequence = BasicTranslator.getTranslator().translate(sentence);
				translated = true;
			} catch (Exception e) {
				Mark.err("Couldn't translate the sentence!");
			}
			
			if (translated) { // try to generate Proper Genesese from the Innerese
				// Note: The result might well be multiple sentences!
				if (sequence != null) {
					for (Entity t : sequence.getElements()) {
						// Mark.say("Received entity", t.asString());
						RoleFrame roleFrame = Generator.getGenerator().generateFromEntity(t);
						if (roleFrame != null) {
							innereseGeneratedSentences.add(Generator.getGenerator().generate(roleFrame));
						}
						else {
							innereseGeneratedSentences.add("Unable to generate text.");
						}
					}
				}
				else {
					return new TestResult(sentence, this, false,
							"The Translator could not translate the sentence into Innerese.",
							"There are a number of reasons why this could be the case...",
							null);
				}
				return new TestResult(sentence, this, true,
						"The Translator was able to convert the sentence into Innerese.",
						null,
						new Object[] { sequence, innereseGeneratedSentences } );
			}
			else {
				return new TestResult(sentence, this, false,
						"The Translator could not translate the sentence into Innerese.",
						"There are a number of reasons why this could be the case...",
						null);
			}
		}
		@Override public FailureMeaning getFailureMeaning() {
			return FailureMeaning.WILL_CAUSE_PROBLEMS;
		}
	}
	
	public static ConceptPatternDeclarationTest getConceptPatternDeclarationTest() {
		if (conceptPatternDeclarationTest == null) {
			conceptPatternDeclarationTest = new ConceptPatternDeclarationTest();
		}
		return conceptPatternDeclarationTest;
	}
	public static class ConceptPatternDeclarationTest extends SentenceTest {
		@Override public String getName() { return "Concept Pattern Declaration Test"; }
		@Override public TestResult test(String sentence) {
			if (sentence.matches("Start description of \".+\"\\.")) {
				return new TestResult(sentence, this, true,
						"The sentence matches the idiom pattern for declaring a concept pattern.",
						null,
						null);
			}
			else {
				return new TestResult(sentence, this, false,
						"The sentence doesn't match the idiom pattern for declaring a concept pattern.",
						"Concept pattern declarations look like this: Start description of \"name here\".",
						null);
			}
					
		}
		@Override public FailureMeaning getFailureMeaning() {
			return FailureMeaning.INFORMATIONAL_ONLY;
		}
	}

}

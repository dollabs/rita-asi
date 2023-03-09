package nicholasBenson;

public abstract class SentenceTest {
	
	protected SentenceTest() {}

	public abstract String getName();
	public abstract TestResult test(String sentence);
	public TestResult test(Sentence sentence) {
		return test(sentence.getText());
	}
	public abstract FailureMeaning getFailureMeaning();
	
	public enum FailureMeaning {
		WILL_CAUSE_PROBLEMS,
		MIGHT_BE_FINE,
		INFORMATIONAL_ONLY
	}
	
	public String explainFailureImportance(FailureMeaning failureMeaning) {
		switch(failureMeaning) {
			case WILL_CAUSE_PROBLEMS:
				return "Failing this test will prevent the story from being understood properly.";
			case MIGHT_BE_FINE:
				return "Failing this test might be fine, or it could prevent the story from being understood properly.";
			case INFORMATIONAL_ONLY: default:
				return "This test answers a question. Failing this test will not impact Genesis's ability to read the story.";
		}
	}
}
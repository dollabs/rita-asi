package nicholasBenson;

public class TestResult {
	
	public final String sentenceText;
	public final SentenceTest sentenceTest;
	public final boolean succeeded;
	public final String reason;
	public final String recommendation;
	public final Object testResultData; // optional, can be null
	
	public TestResult(
			String forSentence,
			SentenceTest sentenceTest,
			boolean succeeded,
			String reason,
			String recommendation,
			Object testResultData) {
		this.sentenceText = forSentence;
		this.sentenceTest = sentenceTest;
		this.succeeded = succeeded;
		this.reason = reason;
		this.recommendation = recommendation == null? "No recommendation." : recommendation;
		this.testResultData = testResultData;
	}
	
	public SentenceTest getTest() {
		return sentenceTest;
	}
	
	public String getSentenceText() {
		return sentenceText;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(sentenceTest.getName());
		builder.append(" for sentence '");
		boolean shorten = sentenceText.length() > 24;
		String sentenceTextDigest = (shorten? sentenceText.substring(0, 24) + "..." : sentenceText);
		builder.append(sentenceTextDigest);
		builder.append("' result: ");
		builder.append(succeeded? "Succeeded]" : "Failed]");
		return builder.toString();
	}
	
	public boolean hasData() {
		return testResultData != null;
	}
	
	public Object getData() {
		return testResultData;
	}

}

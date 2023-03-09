package silaSayan.Evaluators;

import matchers.StandardMatcher;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;

public class FullStateComparator extends AbstractWiredBox {

	// PORTS
	public static final String NARRATOR_COMPLETE_STORY_PORT = "narrator elaboration";

	public static final String AUDIENCE_COMPLETE_STORY_PORT = "audience elaboration";

	public static final String COMPARATOR_OUTPUT_PORT = "comparison result";

	// FIELDS
	private Sequence narratorElaboration = new Sequence();

	private Sequence audienceElaboration = new Sequence();

	private Sequence aberrationsFromGoalState = new Sequence();

	public FullStateComparator() {
		super("Full State Comparator");
		Connections.getPorts(this).addSignalProcessor(NARRATOR_COMPLETE_STORY_PORT, "processNarratorElaboration");
		Connections.getPorts(this).addSignalProcessor(AUDIENCE_COMPLETE_STORY_PORT, "processAudienceElaboration");
	}

	public void processNarratorElaboration(Object o) {
		if (o instanceof Sequence) {
			narratorElaboration = (Sequence) o;
		}
	}

	public void processAudienceElaboration(Object o) {
		if (o instanceof Sequence) {
			audienceElaboration = (Sequence) o;
		}

		if (!narratorElaboration.getElements().isEmpty() && !audienceElaboration.getElements().isEmpty()) {
			compareFullState(narratorElaboration, audienceElaboration);
		}
	}

	public void compareFullState(Sequence narratorElaboration, Sequence audienceElaboration) {
		if (narratorElaboration.equals(audienceElaboration)) {
			return;
		}
		else {
			StandardMatcher matcher = StandardMatcher.getBasicMatcher();

			for (Entity e : audienceElaboration.getElements()) {
				int matchCount = 0;
				for (Entity f : narratorElaboration.getElements()) {
					if (matcher.match(e, f) != null) {
						matchCount++;
						break;
					}
				}
				if (matchCount == 0) {
					if (aberrationsFromGoalState.contains(e)) {
						continue;
					}
					else {
						aberrationsFromGoalState.addElement(e);
					}
				}
			}
		}
		BetterSignal comparisonResult = new BetterSignal(aberrationsFromGoalState);
		Connections.getPorts(this).transmit(COMPARATOR_OUTPUT_PORT, comparisonResult);
	}
}

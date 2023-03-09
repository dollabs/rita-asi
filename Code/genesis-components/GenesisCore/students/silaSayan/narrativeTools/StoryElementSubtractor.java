package silaSayan.narrativeTools;

import java.util.HashSet;
import java.util.Set;

import matchers.StandardMatcher;
import subsystems.summarizer.Summarizer;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;

public class StoryElementSubtractor extends AbstractWiredBox {

	// PORTS
	public static final String COMPARISON_RESULT_INPUT_PORT = "comparison results";

	public static final String ORIGINAL_STORY_PORT = "original story";

	public static final String AUDIENCE_COMPLETE_STORY_PORT = "audience elaboration";

	public static final String EDITED_STORY_PORT = "edited story";

	// FIELDS
	private Sequence storyToEdit = new Sequence();

	private Sequence audienceElaboration = new Sequence();

	private Sequence undesirableAudienceInferences = new Sequence();

	private Sequence elementsToSubtract = new Sequence();

	private Sequence editedStory = new Sequence();

	// TIMING FIELDS
	private boolean gotOriginalStory = false;

	private boolean gotAudienceElaboration = false;

	private boolean gotComparisonResults = false;

	public StoryElementSubtractor() {
		super("Story Element Subtractor");
		Connections.getPorts(this).addSignalProcessor(ORIGINAL_STORY_PORT, "processOriginalStory");
		Connections.getPorts(this).addSignalProcessor(AUDIENCE_COMPLETE_STORY_PORT, "processAudienceElaboration");
		Connections.getPorts(this).addSignalProcessor(COMPARISON_RESULT_INPUT_PORT, "processComparisonResults");
	}

	public void processOriginalStory(Object o) {
		if (o instanceof Sequence) {
			storyToEdit = (Sequence) o;
			gotOriginalStory = true;
		}
	}

	public void processAudienceElaboration(Object o) {
		if (o instanceof Sequence) {
			audienceElaboration = (Sequence) o;
			gotAudienceElaboration = true;
		}
	}

	// Diked by phw because Summarizer no longer available via static getter. Doesn't seem used at the moment 21 Sep
	// 2014

	public void processComparisonResults(Object o) {
		if (o instanceof Sequence) {
			undesirableAudienceInferences = (Sequence) o;
			gotComparisonResults = true;
		}
		if (gotComparisonResults && gotOriginalStory && gotAudienceElaboration) {
			traceUndesirableStoryElements(undesirableAudienceInferences, audienceElaboration);
		}
	}

	public void traceUndesirableStoryElements(Sequence undesirableInferences, Sequence audienceElaboration) {
		// Set<Entity> undesirableRootsSet = new HashSet<Entity>();
		// Summarizer summarizer = getSummarizer();
		for (Entity unwanted : undesirableInferences.getElements()) {
			StandardMatcher matcher = StandardMatcher.getBasicMatcher();
			for (Entity f : audienceElaboration.getElements()) {
				if (matcher.match(unwanted, f) != null) {

				}
			}
		}
	}

}

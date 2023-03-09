package genesis;

import storyProcessor.ConceptAnalysis;
import connections.signals.BetterSignal;
import frames.entities.Sequence;

/*
 * Created on May 1, 2015
 * @author phw
 */

public class StoryAnalysis extends BetterSignal {

	public StoryAnalysis(Object... arguments) {
		super(arguments);
	}

	public String getTitle() {
		return this.get(0, Sequence.class).getType();
	}

	public Sequence getStory() {
		return this.get(0, Sequence.class);
	}

	public Sequence getExplicitElements() {
		return this.get(1, Sequence.class);
	}

	public Sequence getInferences() {
		return this.get(2, Sequence.class);
	}

	public Sequence getInstantiatedConceptPatterns() {
		return this.get(3, Sequence.class);
	}

	public ConceptAnalysis getConceptAnalysis() {
		return this.get(4, ConceptAnalysis.class);
	}

}

package storyProcessor;

import java.util.ArrayList;

import frames.entities.Sequence;
import utils.Punctuator;
import utils.Mark;

/*
 * A wrapper for all the concept descriptions found by the reflection expert in a story. Created on Oct 16, 2010
 * @author phw
 */

public class ConceptAnalysis {

	ArrayList<ConceptDescription> concepts;

	Sequence story;

	String storyName;

	public ConceptAnalysis(ArrayList<ConceptDescription> completions, Sequence story) {
		super();
		// Intervene here to weed out subsequences
		concepts = completions;
		if (story == null && completions != null && !completions.isEmpty()) {
			story = completions.get(0).getStory();
		}
		this.storyName = Punctuator.conditionName(StoryProcessor.getTitle(story));
		this.story = story;
	}

	/*
	 * Returns all the reflection descriptions.
	 */
	public ArrayList<ConceptDescription> getConceptDescriptions() {
		return concepts;
	}

	/*
	 * Returns the story involved.
	 */
	public Sequence getStory() {
		return story;
	}

	/*
	 * Convenience method. Returns the name of the story involved.
	 */
	public String getStoryName() {
		return storyName;
	}

}

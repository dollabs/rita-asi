package subsystems.recall;

import java.util.*;

import storyProcessor.*;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Sequence;

public class StoryRecallExpert extends AbstractWiredBox {

	public static final String MEMORY_PORT = "memory port";

	public static final String RECALL_PORT = "recall port";

	public static final String CLEAR_PORT = "clear port";

	public static final String CONCEPTS = "concepts";

	public static final String ENTITIES = "entities";

	private ArrayList<StoryVectorWrapper> memory;

	private ArrayList<Sequence> precedents = new ArrayList<>();

	public StoryRecallExpert() {
		super("Story recall expert");
		Connections.getPorts(this).addSignalProcessor(MEMORY_PORT, this::processMemory);
		Connections.getPorts(this).addSignalProcessor(RECALL_PORT, this::processRecall);
		Connections.getPorts(this).addSignalProcessor(CLEAR_PORT, this::clearMemory);
	}

	public void processStory(Sequence story) {
		if (!(story instanceof Sequence)) {
			return;
		}
	}

	public void clearMemory(Object signal) {
		clearMemory();
	}

	public void clearMemory() {
		getMemory().clear();
	}

	public void processMemory(Object signal) {
		// Mark.say("Zero");
		if (!(signal instanceof BetterSignal)) {
			return;
		}
		BetterSignal bs = (BetterSignal) signal;
		// Whole story
		// Sequence story = bs.get(0, Sequence.class);
		// Explicit elements
		Sequence story = bs.get(1, Sequence.class);
		ConceptAnalysis analysis = bs.get(4, ConceptAnalysis.class);
		// Mark.say("Mark store", story.getType());
		StoryVectorWrapper storyVectorWrapper = new StoryVectorWrapper(story, analysis);
		getMemory().add(storyVectorWrapper);

	}

	public void processRecall(Object signal) {
		// Mark.say("Zero");
		if (!(signal instanceof BetterSignal)) {
			return;
		}
		BetterSignal bs = (BetterSignal) signal;
		Sequence story = bs.get(1, Sequence.class);
		ConceptAnalysis analysis = bs.get(4, ConceptAnalysis.class);
		// Mark.say("Mark recall", story.getType());
		processRecall(story, analysis);
	}

	public void processRecall(Sequence story, ConceptAnalysis analysis) {
		precedents.clear();
		StoryVectorWrapper currentStoryVectorWrapper = new StoryVectorWrapper(story, analysis);
		// Mark.say("Practicing recall with story memory count", getMemory().size());
		if (!getMemory().isEmpty()) {
			TreeSet<MatchWrapper> bestPrecedents = StoryVectorMatcher
			        .findBestMatches(currentStoryVectorWrapper, getMemory(), StoryVectorMatcher.CONCEPT);
			if (!bestPrecedents.isEmpty()) {

				// Sila:
				// Note that bestPrecedents is ordered by quality of match with most recent story
				// To get story out of a MatchWrapper instance in bestPrecedents, use getStory()
				// To get bestPrecedents into another box, go to GenesisPlugBoardUpper and insert wire:
				// Connections.wire(getStoryRecallExpert1(), <your port> <your box>);
				// Once you have the ordered precedent stories in your box, you can inspect the storyies
				// using StandardMatcher to see if any of them have an event you want
				Connections.getPorts(this).transmit(CONCEPTS, bestPrecedents);
			}

			TreeSet<MatchWrapper> bestPrecedents2 = StoryVectorMatcher
			        .findBestMatches(currentStoryVectorWrapper, getMemory(), StoryVectorMatcher.ENTITY);
			if (!bestPrecedents2.isEmpty()) {

				// Sila:
				// Note that bestPrecedents is ordered by quality of match with most recent story
				// To get story out of a MatchWrapper instance in bestPrecedents, use getStory()
				// To get bestPrecedents into another box, go to GenesisPlugBoardUpper and insert wire:
				// Connections.wire(getStoryRecallExpert1(), <your port> <your box>);
				// Once you have the ordered precedent stories in your box, you can inspect the storyies
				// using StandardMatcher to see if any of them have an event you want
				Connections.getPorts(this).transmit(ENTITIES, bestPrecedents2);
			}

		}
		else {
			Mark.say("No precedents yet");
		}
	}

	public ArrayList<Sequence> getPrecedents() {
		return precedents;
	}

	public ArrayList<StoryVectorWrapper> getMemory() {
		if (memory == null) {
			memory = new ArrayList<StoryVectorWrapper>();
		}
		return memory;
	}

}

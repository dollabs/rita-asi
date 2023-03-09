package subsystems.recall;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Sequence;
import genesis.StoryAnalysis;
import storyProcessor.*;
import utils.Mark;

/*
 * Created on Apr 18, 2015
 * @author phw
 */

public class SessionStoryMemory extends AbstractWiredBox {

	public final static String ADD_STORY = "Add story";

	private static SessionStoryMemory sessionStoryMemory1;

	private static SessionStoryMemory sessionStoryMemory2;

	private HashMap<String, StoryAnalysis> memory;

	public HashMap<String, StoryAnalysis> getMemory() {
		if (memory == null) {
			memory = new HashMap<>();
		}
		return memory;
	}

	public SessionStoryMemory() {
		super("Session story memory");
		Connections.getPorts(this).addSignalProcessor(ADD_STORY, this::addStory);
	}

	public void addStory(Object o) {
		if (o instanceof StoryAnalysis) {
			StoryAnalysis bs = (StoryAnalysis) o;
			// Mark.say("Adding story", bs.get(0, Sequence.class).getType());
			String key = bs.get(0, Sequence.class).getType();
			if (getMemory().get(key) == null) {
				// Mark.say("Recording", key);
				getMemory().put(key, bs);
			}
		}
	}

	public int size() {
		return getMemory().size();
	}

	public Set<ConceptDescription> getInstantiatedConceptPatterns(List<String> conceptNames) {
		Set<ConceptDescription> result = new HashSet<>();
		for (BetterSignal betterSignal : getMemory().values()) {
			ConceptAnalysis conceptAnalysis = betterSignal.get(4, ConceptAnalysis.class);
			// StoryProcessor.describeConceptAnalysis(conceptAnalysis);
			for (ConceptDescription conceptDescription : conceptAnalysis.getConceptDescriptions()) {
				String rememberedConceptName = conceptDescription.getName();
				for (String conceptName : conceptNames) {
					if (rememberedConceptName.equalsIgnoreCase(conceptName)) {
						// Mark.say("Bingo!", rememberedConceptName);
						result.add(conceptDescription);
					}
				}
			}
		}
		return result;
	}

}

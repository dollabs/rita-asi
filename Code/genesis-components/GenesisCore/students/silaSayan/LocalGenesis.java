package silaSayan;

import genesis.Genesis;
import gui.TabbedTextViewer;
import storyProcessor.*;
import utils.Mark;
import connections.Connections;
import constants.Markers;

/*
 * Created on Mar 25, 2010
 * @author phw
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {

	LocalProcessor localProcessor;

	public LocalGenesis() {
		super();
		Mark.say("Sila's LocalGenesis's constructor");
		// Local wiring goes here; example shown

		/* Use following connection to get story at once. in combination with line labeled COMPLETE in LocalProcessor. */
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getMentalModel1(), getLocalProcessor());

		/* Use the following two lines to get the story incrementally */
		/** July 19: following connection doesn't achieve desired result. namely, incremental access to story. **/
		Connections.wire(StoryProcessor.NEW_ELEMENT_PORT, getMentalModel1(), LocalProcessor.STORY1, getLocalProcessor());

		Connections.wire(StoryProcessor.NEW_ELEMENT_PORT, getMentalModel2(), LocalProcessor.STORY2, getLocalProcessor());

		Connections.wire(Markers.NEXT, getAnaphoraExpert(), LocalProcessor.PLOT, getLocalProcessor());

		Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getMentalModel1(), LocalProcessor.REFLECTION_PORT1, getLocalProcessor());
		Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getMentalModel2(), LocalProcessor.REFLECTION_PORT2, getLocalProcessor());
		// Removed for Compile Errors
		// Connections.wire(ConceptExpert.REFLECTION_ANALYSIS, getReflectionExpert1(), LocalProcessor.REFLECTION_PORT1,
		// getLocalProcessor());
		// Connections.wire(ConceptExpert.REFLECTION_ANALYSIS, getReflectionExpert2(), LocalProcessor.REFLECTION_PORT2,
		// getLocalProcessor());

		Connections.wire(StoryProcessor.INCREMENT_PORT, getMentalModel1(), LocalProcessor.QUIESCENCE_PORT1, getLocalProcessor());
		Connections.wire(StoryProcessor.INCREMENT_PORT, getMentalModel2(), LocalProcessor.QUIESCENCE_PORT2, getLocalProcessor());

		Connections.wire(StoryProcessor.INFERENCES, getMentalModel1(), LocalProcessor.INFERENCES, getLocalProcessor());
		Connections.wire(StoryProcessor.NEW_INFERENCE_PORT, getMentalModel1(), LocalProcessor.INCREMENT, getLocalProcessor());

		Connections.wire(StoryProcessor.RULE_PORT, getMentalModel1(), LocalProcessor.RULE_PORT, getLocalProcessor());

		Connections.wire(StoryProcessor.START_STORY_INFO_PORT, getMentalModel1(), LocalProcessor.START_STORY_INFO1, getLocalProcessor());
		Connections.wire(StoryProcessor.START_STORY_INFO_PORT, getMentalModel2(), LocalProcessor.START_STORY_INFO2, getLocalProcessor());

		Connections.wire(LocalProcessor.TEACH_RULE_PORT, getLocalProcessor(), StoryProcessor.LEARNED_RULE_PORT, getMentalModel2());
		Connections.wire(LocalProcessor.NEW_RULE_MESSENGER_PORT, getLocalProcessor(), StoryProcessor.NEW_RULE_MESSENGER_PORT, getMentalModel2());

		// The following is for Story Viewer tab
		Connections.wire(TabbedTextViewer.TAB, getLocalProcessor(), TabbedTextViewer.TAB, getResultContainer());
		Connections.wire(getLocalProcessor(), getResultContainer());

	}

	public LocalProcessor getLocalProcessor() {
		if (localProcessor == null) {
			localProcessor = new LocalProcessor();
		}
		return localProcessor;
	}

	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
	}

}

package jessicaNoss;

import genesis.Genesis;
import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.Connections;

/**
 * This is a personal copy of Genesis I can play with without endangering the code of others. I will also want to look
 * at the main methods in Entity, for examples of how the representational substrate works, and Generator, for examples
 * of how to go from English to Genesis's inner language and back.
 * 
 * @author phw
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {

	LocalProcessor localProcessor;

	public LocalGenesis() {
		super();
		Mark.say("Local constructor");
		// Local wiring goes here; example shown connects my LocalProcessor box to the output of the main mental model
		// that reads stories; getMentalModel2() is a second mental model that also reads stories if the source file
		// calls for that to happen.
		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getLocalProcessor());
		// Example of disconnection, commented out, in case I want to eliminate a connection in Genesis without screwing
		// up other people.
		// Connections.disconnect(getStoryProcessor1(), getLocalProcessor());
	}

	/*
	 * Get an instance of LocalProcessor to do something with the output of a complete story object from a story
	 * processor.
	 */
	public LocalProcessor getLocalProcessor() {
		if (localProcessor == null) {
			localProcessor = new LocalProcessor();
		}
		return localProcessor;
	}

	/*
	 * Fires up my copy of Genesis in a simple Java frame. It can also be started up in other ways; that is the reason
	 * for the startInFrame call.
	 */
	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
	}
}

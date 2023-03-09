// Updated 10 June 2015

package suriBandler;

import connections.Connections;
import expert.QuestionExpert;
import genesis.Genesis;
import storyProcessor.StoryProcessor;
import utils.Mark;

/**
 * This is a personal copy of Genesis I can play with without endangering the code of others. I will also want to look
 * at the main methods in Entity, for examples of how the representational substrate works, and Generator, for examples
 * of how to go from English to Genesis's inner language and back.
 * 
 * @author phw
 */

@SuppressWarnings("serial")
public class DemoLocalGenesis extends Genesis {

	DemoLocalProcessor localProcessor;

	public DemoLocalGenesis() {
		super();
		Mark.say("Local constructor");

		// This one connects a new port (as of 13 Jan 2015) from the story processor to your processor. Delivers the
		// story processor itself.

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1()
		        .getStoryProcessor(), StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getLocalProcessor());

		Connections.wire(QuestionExpert.TO_SB, getQuestionExpert(), DemoLocalProcessor.TRIGGER_PORT, getLocalProcessor());
	}

	/*
	 * Get an instance of LocalProcessor to do something with the output of a complete story object from a story
	 * processor.
	 */
	public DemoLocalProcessor getLocalProcessor() {
		if (localProcessor == null) {
			localProcessor = new DemoLocalProcessor();
		}
		return localProcessor;
	}

	/*
	 * Fires up my copy of Genesis in a simple Java frame. It can also be started up in other ways; that is the reason
	 * for the startInFrame call.
	 */
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		DemoLocalGenesis myGenesis = new DemoLocalGenesis();
		myGenesis.startInFrame();
	}
}

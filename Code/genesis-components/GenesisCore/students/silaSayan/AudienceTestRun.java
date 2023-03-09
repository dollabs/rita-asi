package silaSayan;

import connections.AbstractWiredBox;
import connections.Connections;
import frames.entities.Sequence;

/**
 * This class allows the narrator to simulate, using its own mental model of the listener, telling the story to the
 * listener.
 **/

public class AudienceTestRun extends AbstractWiredBox {

	// FIELDS
	public static Sequence audienceReaction;

	// INPUT PORTS
	public static String UPDATED_STORY_INPUT_PORT = "story from StoryTeller"; // from StoryTeller. can be the original
																			  // or updated story.

	// OUTPUT PORTS
	public static String AUDIENCE_REACTION_OUTPUT_PORT = "simulated audience reaction"; // to StoryTeller

	public AudienceTestRun() {
		super("AudienceSimulator");
		Connections.getPorts(this).addSignalProcessor(UPDATED_STORY_INPUT_PORT, "testRun");
	}

	public void testRun() {
		// @TODO: actually generate audience reaction!!
		Connections.getPorts(this).transmit(audienceReaction);
		return;
	}

}

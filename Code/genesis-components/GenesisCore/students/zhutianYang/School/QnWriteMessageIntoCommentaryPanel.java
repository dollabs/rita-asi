package zhutianYang.School;

import connections.Connections;
import constants.*;
import genesis.Genesis;
import storyProcessor.StoryProcessor;
import utils.Mark;

public class QnWriteMessageIntoCommentaryPanel extends Genesis {

	public static String MY_PORT = "my port";

	// For your box, create a commentary port
	public static String COMMENTARY = "commentary port";

	public QnWriteMessageIntoCommentaryPanel() {
		super();
		Mark.say("Local constructor");
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1().getStoryProcessor(), MY_PORT, this);
		Connections.getPorts(this).addSignalProcessor(MY_PORT, this::processStoryProcessor);

		// Wire your box's commentary port to the commentary container. You probably will do this in your copy of
		// Genesis or in one of the Genesis plug boards.
		Connections.wire(COMMENTARY, this, getCommentaryContainer());

	}

	/**
	 * Define a helper method that hides some details
	 */
	private void say(Object... objects) {
		// First argument is the box that wants to write a message
		// Second argument is commentary port wired to the commentary panel
		// Third argument is location on screen: LEFT, RIGHT, BOTTOM
		// Fourth argument is tab title
		// Final arguments are message content
		Mark.comment(this, COMMENTARY, GenesisConstants.BOTTOM, "Hello world", objects);
	}

	public void processStoryProcessor(Object signal) {
		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;
			Mark.say("Story processor received information");
			// Ok, after receiving a story, write to the commentary panel.
			say("There are", processor.getStory().getElements().size(), "story elements");
		}
	}



	public static void main(String[] args) {
		QnWriteMessageIntoCommentaryPanel myGenesis = new QnWriteMessageIntoCommentaryPanel();
		myGenesis.startInFrame();
	}

	
}

// Updated 10 June 2015

package suriBandler;

import connections.*;
import constants.Switch;
import genesis.Genesis;
import storyProcessor.StoryProcessor;
import utils.Mark;


@SuppressWarnings("serial")
public class Demo extends Genesis {

	DemoReprocessing demoReprocessing;

	public Demo() {
		super();
		Mark.say("Local constructor");

		// Connect both mental model story processors to DemoReprocessing box
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1()
		        .getStoryProcessor(), DemoReprocessing.MM1_PORT, getDemoReprocessing());

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel2()
		        .getStoryProcessor(), DemoReprocessing.MM2_PORT, getDemoReprocessing());
		
		// Now wire DemoReprocessing box back to first mental model.  
		// As soon as DemoReprocessing box gets input, it makes new story and
		// returns it to the first mental model via this connection.
		
		Connections.wire(DemoReprocessing.REVISION_PORT, getDemoReprocessing(),
		        Port.INPUT, getMentalModel1().getStoryProcessor());
		        

	}

	public DemoReprocessing getDemoReprocessing() {
		if (demoReprocessing == null) {
			demoReprocessing = new DemoReprocessing("Reprocessing box");
			// Switch determines whether box is active. Not currently visible.
			// Set by instruction in demonstration file.
			demoReprocessing.setGateKeeper(Switch.Reprocess);
		}
		return demoReprocessing;
	}


	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		Demo myGenesis = new Demo();
		myGenesis.startInFrame();
	}
}

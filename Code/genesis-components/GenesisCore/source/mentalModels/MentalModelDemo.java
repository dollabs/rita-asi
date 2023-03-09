// Updated 10 June 2015

package mentalModels;

import newStudent.*;
import genesis.*;
import start.*;
import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.Connections;
import constants.Markers;

/**
 * This is a demo copy of Genesis. It is mean to read Who knows what.txt in stories > MentalModelDemos
 */

@SuppressWarnings("serial")
public class MentalModelDemo extends Genesis {

	MentalModelProcessor mentalModelProcessor;

	public MentalModelDemo() {
		super();
		Connections.wire(StoryProcessor.INCREMENT_PORT_COMPLETE, getMentalModel1().getStoryProcessor(), getLocalProcessor());
	}

	/*
	 * Get an instance of LocalProcessor.
	 */
	public MentalModelProcessor getLocalProcessor() {
		if (mentalModelProcessor == null) {
			mentalModelProcessor = new MentalModelProcessor();
		}
		return mentalModelProcessor;
	}

	/*
	 * Fires up Genesis.
	 */
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		MentalModelDemo myGenesis = new MentalModelDemo();
		myGenesis.startInFrame();
	}
}

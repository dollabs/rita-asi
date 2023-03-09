// Updated 10 June 2015

package mentalModels;

import java.util.Map;

import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.RoleFrames;
import mentalModels.MentalModel;

/**
 * A local processor class for demonstrating mental model manipulations
 */
public class MentalModelProcessor extends AbstractWiredBox {

	/**
	 * Connect method to default port
	 */
	public MentalModelProcessor() {
		super("Local story processor");
		// Receives story incrementally, element by element
		Connections.getPorts(this).addSignalProcessor(this::processSignal);
	}

	/**
	 * Processes story, element by element
	 * 
	 * @param signal
	 */
	public void processSignal(Object signal) {
		boolean debug = true;
		if (signal instanceof BetterSignal) {
			BetterSignal b = (BetterSignal) signal;
			Entity increment = b.get(0, Entity.class);
			Sequence story = b.get(1, Sequence.class);
			StoryProcessor storyProcessor = b.get(2, StoryProcessor.class);
			MentalModel mentalModel = storyProcessor.getMentalModel();
			// Mark.say(debug, "Next entity:", Generator.getGenerator().generate(increment));

			// See if there are any mental models yet
			Map<String, MentalModel> mentalModels = mentalModel.getLocalMentalModels();
			if (!mentalModels.isEmpty()) {
				// Mark.say("Now have mental model(s)");
				// Idea here will be to let the mental model know about the element
				// if the mental model models the actor (that is, the subject of the relation)
				Entity subject = increment.getSubject();
				for (MentalModel model : mentalModels.values()) {
					// Mark.say("Test\n", subject, "\n", model.getModeledEntity());
					if (subject == model.getModeledEntity()) {
						model.getStoryProcessor().injectElementWithDereference(increment);
					}
				}
			}

		}
	}

	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		MentalModelDemo.main(args);
	}
}
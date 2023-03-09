/**
 * 
 */
package suriBandler;


import connections.*;
import frames.entities.Entity;
import frames.entities.Sequence;
import storyProcessor.StoryProcessor;
import translator.Translator;
import utils.Mark;

/**
 * @author phw
 *
 */
public class DemoReprocessing extends AbstractWiredBox {

	public static String MM1_PORT = "Mental model 1 port";

	public static String MM2_PORT = "Mental model 2 port";

	public static String REVISION_PORT = "Mental model 2 port";

	StoryProcessor SPOne = null;

	StoryProcessor SPTwo = null;


	Sequence explicitElementsInOne = null;

	Sequence newElements = null;

	public DemoReprocessing(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(MM1_PORT, this::MMOneReports);
		Connections.getPorts(this).addSignalProcessor(MM2_PORT, this::MMTwoReports);
	}

	public void MMOneReports(Object object) {
		if (object instanceof StoryProcessor) {
			Mark.say("Got signal on MM1_PORT");
			SPOne = (StoryProcessor) object;
			explicitElementsInOne = SPOne.getExplicitElements();
		}
	}

	public void MMTwoReports(Object object) {
		if (object instanceof StoryProcessor) {
			Mark.say("Got signal on MM2_PORT");
			SPTwo = (StoryProcessor) object;
			// Replace with actual new elements desired.
			// This just appends entire second story to first story.
			newElements = SPTwo.getExplicitElements();
			// Construct new story to be fed back to First perspective.
			Sequence combination = new Sequence();
			Entity starter = Translator.getTranslator().translateToEntity("Start story titled \"Reprocessed story\".");
			combination.getElements().add(starter);
			combination.getElements().addAll(explicitElementsInOne.getElements());
			combination.getElements().addAll(newElements.getElements());
			combination.getElements().stream().forEachOrdered(e -> {
				Mark.say("Element:", e);
			});
			// Wake up first story processor
			SPOne.setAwake(true);
			// Now transmit back to first story processor
			combination.stream().forEachOrdered(e -> {
				Mark.say("Transmitting", e);
				Connections.getPorts(this).transmit(REVISION_PORT, e);
			});
			// Now, stop the story as if The end. were encountered.
			SPOne.stopStory();

		}
	}

}


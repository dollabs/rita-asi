// Updated 10 June 2015

package nicholasBenson;

import generator.Generator;
import mentalModels.MentalModelDemo;
import start.Start;
import storyProcessor.StoryProcessor;
import subsystems.summarizer.Summarizer;
import utils.Mark;
import connections.*;
import constants.Markers;
import frames.entities.Sequence;

/**
 * A local processor class that just receives a complete story description, takes apart the wrapper object to fetch
 * various parts of the complete story description, and prints them so you can see what is in there.
 */
public class LocalProcessor extends AbstractWiredBox {

	// EXamples of how ports are named, not used here
	public final String MY_INPUT_PORT = "my input port";

	public final String MY_OUTPUT_PORT = "my output port";

	/**
	 */
	public LocalProcessor() {
		super("Local story processor");
		// Receives story processor when story has been processed
		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::reset);

		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processStoryProcessor);
	}

	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 * <p>
	 * Writes information extracted from the story processor received on the port.
	 */
	public void processStoryProcessor(Object signal) {
		boolean debug = true;
		// Make sure it is what was expected
		// Make sure it is what was expected
		Mark.say("Entering processStoryProcessor");

		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;

			Sequence story = processor.getStory();
			Sequence explicitElements = processor.getExplicitElements();
			Sequence inferences = processor.getInferredElements();
			Sequence concepts = processor.getInstantiatedConceptPatterns();
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			Mark.say(debug, "\n\n\nExplicit story elements");
			explicitElements.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated commonsense rules");
			inferences.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated concept patterns");
			concepts.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nAll story elements, in English");
			Generator generator = Generator.getGenerator();
			story.stream().forEach(e -> Mark.say(debug, generator.generate(e)));

			processor.getRuleMemory().getRuleSequence().getElements().stream().filter(r -> r.getProbability() == null ? true : false)
			        .forEach(r -> Mark.say(debug, "Rule:", r.getProbability(), r));

		}
	}

	public void reset(Object signal) {
		// Does nothing right now
	}

	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		MentalModelDemo.main(args);
	}
}
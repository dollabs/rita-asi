// Updated 10 June 2015

package suriBandler;

import generator.Generator;
import genesis.GenesisGetters;
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
public class DemoLocalProcessor extends AbstractWiredBox {

	public static String TRIGGER_PORT = "trigger port";

	// EXamples of how ports are named, not used here
	public final String MY_INPUT_PORT = "my input port";
	public final String MY_OUTPUT_PORT = "my output port";

	/**
	 */
	public DemoLocalProcessor() {
		super("Local story processor");
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processStoryProcessor);
		Connections.getPorts(this).addSignalProcessor(DemoLocalProcessor.TRIGGER_PORT, this::triggerProcessor);
	}

	public void triggerProcessor(Object signal) {
		Mark.say("Trigger processor received", signal);
	}

	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 * <p>
	 * This one writes information extracted from the story processor received on the STORY_PROCESSOR_SNAPSHOT port.
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

			Mark.say("Recorded stories", GenesisGetters.getMentalModel1().getStoryMemory().size());

			Mark.say("Map size", GenesisGetters.getMentalModel1().getStoryMemory().getMemory().size());

			GenesisGetters.getMentalModel1().getStoryMemory().getMemory().values().stream().forEach(m -> Mark.say("Title", m.getTitle()));

		}
	}

	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		MentalModelDemo.main(args);
	}
}
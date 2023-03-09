// too slow or failed?

package zhutianYang.School;

import java.util.List;
import java.util.stream.Collectors;

import connections.Connections;
import frames.entities.Entity;
import genesis.Genesis;
import storyProcessor.StoryProcessor;
import utils.Mark;
import utils.tools.Predicates;

public class QnGetListOfRulesUsedByStoryProcessor extends Genesis {

	public static String MY_PORT = "my port";

	// For your box, create a commentary port
	public static String COMMENTARY = "commentary port";

	public QnGetListOfRulesUsedByStoryProcessor() {
		super();
		Mark.say("Local constructor");
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1().getStoryProcessor(), MY_PORT, this);
		Connections.getPorts(this).addSignalProcessor(MY_PORT, this::processStoryProcessor);
	}


	public void processStoryProcessor(Object signal) {
		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;
			Mark.say("Story processor received input");
			// Ok, you get at the rules via the story processor that comes in via a port connected to the story
			// processor of a mental model
			Mark.say("The rules are:");
			processor.getRuleMemory().getRuleList().stream().forEachOrdered(r -> Mark.say("Rule:", r));
			Mark.say("The abduction rules are:");
			processor.getRuleMemory().getRuleList().stream().filter(r -> Predicates.isExplanation(r))
			        .forEachOrdered(w -> Mark.say("Explanation rule:", w));
			// Ok, make a list of them
			List<Entity> abductionRules = processor.getRuleMemory().getRuleList().stream().filter(r -> Predicates.isExplanation(r))
			        .collect(Collectors.toList());
			Mark.say("Looks like there are", abductionRules.size(), "explanation rules");
		}
	}



	public static void main(String[] args) {
		QnGetListOfRulesUsedByStoryProcessor myGenesis = new QnGetListOfRulesUsedByStoryProcessor();
		myGenesis.startInFrame();
//		processStoryProcessor(myGenesis);
	}

	
}

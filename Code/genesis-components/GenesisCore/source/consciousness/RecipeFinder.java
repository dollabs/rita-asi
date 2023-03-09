package consciousness;

import connections.*;
import connections.signals.BetterSignal;
import constants.Radio;
import constants.Switch;
import storyProcessor.StoryProcessor;
import utils.Mark;
import utils.Z;
import zhutianYang.StoryAligner;

/**
 * @author phw
 */
public class RecipeFinder extends AbstractWiredBox {

	public static String FROM_FIRST_PERSPECTIVE = "from first perspective";

	public static String FROM_SECOND_PERSPECTIVE = "from second perspective";
	
	public static String TO_COMMENTARY = "to commentary";

	StoryProcessor leftStoryProcessor;

	StoryProcessor rightStoryProcessor;

	public RecipeFinder(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(FROM_FIRST_PERSPECTIVE, this::processFirstPerspective);
		Connections.getPorts(this).addSignalProcessor(FROM_SECOND_PERSPECTIVE, this::processSecondPerspective);
	}



	/*
	 * This one just makes a note of what has come in over first perspective wire:
	 */
	public void processFirstPerspective(Object signal) {
		Mark.say("processFirstPerspective running");
		if (signal instanceof StoryProcessor) {
			leftStoryProcessor = (StoryProcessor) signal;
		}
	}

	/*
	 * This one makes a note of what has come in over second perspective wire and initiates action:
	 */
	public void processSecondPerspective(Object signal) {
		Mark.say("processSecondPerspective running");
		if (signal instanceof StoryProcessor) {
			rightStoryProcessor = (StoryProcessor) signal;
			Mark.say("Computing recipes!");
			leftStoryProcessor.getStory().stream().forEachOrdered(e -> {
				if (e.getType().equals("want")) {
					Mark.say("Wanting element in first perspective:", e);
				}

			});
			rightStoryProcessor.getStory().stream().forEachOrdered(e -> {
				if (e.getType().equals("want")) {
					Mark.say("Wanting element in second perspective:", e);
				}

			});
			// At this point, call Z's code, either by direct call or by transmitting over a wire defined in
			// GenesisPlugBoardUpper (see Note to Z in that file).
			if(Switch.storyAlignerCheckBox.isSelected()) {
				String toPrint = StoryAligner.learnFromStoryEnsembles(leftStoryProcessor, rightStoryProcessor);
				Connections.getPorts(this).transmit(TO_COMMENTARY, new BetterSignal("Similarity & Difference", toPrint));
//				if(Radio.learnProcedure.isSelected()) {
//					Connections.getPorts(this).transmit(TO_COMMENTARY, new BetterSignal("Created Recipes", toPrint));
//				} else if(Radio.learnConcept.isSelected()) {
//					Connections.getPorts(this).transmit(TO_COMMENTARY, new BetterSignal("Discovered Pattern", toPrint));
//				} else if(Radio.learnDifference.isSelected()) {
//					Connections.getPorts(this).transmit(TO_COMMENTARY, new BetterSignal("Discovered Similarity & Difference", toPrint));
//				}
			}
		}
	}

}

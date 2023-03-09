package dylanHolmes;

import genesis.Genesis;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.Connections;
import expert.QuestionExpert;
import expert.WhatIfExpert;

/**
 * This is a personal copy of Genesis I can play with without endangering the code of others. I will also want to look
 * at the main methods in Entity, for examples of how the representational substrate works, and Generator, for examples
 * of how to go from English to Genesis's inner language and back.
 * 
 * @author dxh
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {

	MeansEndsProcessor meProcessor;

	GoalTraitProcessor gtProcessor;

	LacunaProcessor lcProcessor;
	
	public LocalGenesis() {
		super();
		Mark.say("Dylan's local constructor");
		// Local wiring goes here; example shown connects my LocalProcessor box to the output of the main mental model
		// that reads stories; getMentalModel2() is a second mental model that also reads stories if the source file
		// calls for that to happen.
		Connections
		        .wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMeansProcessor());

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), LacunaProcessor.INPUT_COMPLETE_STORY, getLacunaProcessor());

		// connect means-ends to goal-trait
		
		
		
		Connections.wire(MeansEndsProcessor.OUTPUT_GOALS, getMeansProcessor(), GoalTraitProcessor.INPUT_GOALS, getGoalTraitProcessor());

		Connections.wire(GoalTraitProcessor.HTML, getGoalTraitProcessor(), getResultContainer());

		Connections.wire(LacunaProcessor.OUTPUT_REDUCED_STORY, getGoalTraitProcessor(), getMentalModel2());
		
		
		Connections.wire(QuestionExpert.TO_DXH, getQuestionExpert(), MeansEndsProcessor.FROM_QUESTION_EXPERT, meProcessor);
		Connections.wire(WhatIfExpert.TO_MEANS_ENDS_EXPERT, getWhatIfExpert(), MeansEndsProcessor.FROM_QUESTION_EXPERT, meProcessor);
		Connections.wire(MeansEndsProcessor.COMMENTARY, meProcessor, getCommentaryContainer());


		// Inserted by phw
		Connections.wire(LacunaProcessor.OUTPUT_REDUCED_STORY, getLacunaProcessor(), MentalModel.INJECT_STORY, getMentalModel2());


		
	}

	/* Get the relevant processors, creating them if need be. */
	public MeansEndsProcessor getMeansProcessor() {
		return meProcessor = (meProcessor == null) ? new MeansEndsProcessor() : meProcessor;
	}

	public LacunaProcessor getLacunaProcessor() {
		return lcProcessor = (lcProcessor == null) ? new LacunaProcessor() : lcProcessor;
	}

	public GoalTraitProcessor getGoalTraitProcessor() {
		return gtProcessor = (gtProcessor == null) ? new GoalTraitProcessor() : gtProcessor;
	}

	/*
	 * Fires up my copy of Genesis in a simple Java frame. It can also be started up in other ways; that is the reason
	 * for the startInFrame call.
	 */
	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();

		myGenesis.startInFrame();
	}
}

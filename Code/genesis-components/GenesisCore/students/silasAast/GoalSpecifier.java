package silasAast;

import connections.AbstractWiredBox;
import generator.Generator;
import genesis.*;
import gui.TabbedTextViewer;

import java.util.ArrayList;

import matchers.StandardMatcher;
import storyProcessor.ConceptAnalysis;
import storyProcessor.ConceptDescription;
import storyProcessor.StoryProcessor;
import utils.Html;
import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

public class GoalSpecifier extends AbstractWiredBox {

	// FIELDS
	private String goal = "SUMMARY"; //options: SUMMARY, SPOONFEED, PRIME, PREPERSUADE, PERSUADE, NEGOTIATE
									// note: goal needs extra components once more complex than prepersuade
	
	private Sequence narratorView = new Sequence();
	private Sequence reflections = new Sequence();
	private Sequence desiredReaction = new Sequence();
	
	// INPUT PORTS
	public static String NARRATOR_UNDERSTANDING;// from InternalNarrator. 
	
	// OUTPUT PORTS
		
	public static String DESIRED_AUDIENCE_REACTION; 
	
	public GoalSpecifier(){
		Connections.getPorts(this).addSignalProcessor(NARRATOR_UNDERSTANDING, "setGoal");
	}
	
	public void setGoal(BetterSignal signal){		
		if (signal instanceof BetterSignal){
			narratorView = (Sequence) signal.get(0, Sequence.class);
			reflections = (Sequence) signal.get(1, Sequence.class);
			
			if (goal == "SUMMARY"){
				desiredReaction = SummarySpecifier.setGoal(narratorView, reflections);
				Connections.getPorts(this).transmit(DESIRED_AUDIENCE_REACTION, desiredReaction);
			}
			else if (goal=="SPOONFEED"){
				desiredReaction = SpoonfeedSpecifier.setGoal(narratorView);
				Connections.getPorts(this).transmit(DESIRED_AUDIENCE_REACTION, desiredReaction);
			}
			else if (goal == "PRIME"){
				
			}
		}
		
	}
	
	
	
}

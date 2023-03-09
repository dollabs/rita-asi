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


public class StorySimulator extends AbstractWiredBox{

	//INPUT PORTS
	public static String SIMULATION_SPECS_IN; //from StoryPreSimulator
	public static String SIMULATION_RESULTS_IN; //from MentalModel
	
	//OUTPUT PORTS
	public static String EXPORT_COMMONSENSE;
	public static String EXPORT_REFLECTIVE;
	public static String EXPORT_TEXT;
	public static String SIMULATION_RESULTS_OUT;
	
	//FIELDS
	private BetterSignal bundle;
	private Sequence commonsense;
	private Sequence reflective;
	private Sequence modifiedStory;
	private Sequence simulatedAudienceReaction; //to GoalTracker
	
	public StorySimulator(){
		Connections.getPorts(this).addSignalProcessor(SIMULATION_SPECS_IN, "unwrapSimulationInfo");
		Connections.getPorts(this).addSignalProcessor(SIMULATION_RESULTS_IN, "processSimulationResults");
	}

	public void unwrapSimulationInfo(Object o){
		if (o instanceof BetterSignal){
			bundle = (BetterSignal) o;
			commonsense = bundle.get(0, Sequence.class);
			reflective = bundle.get(1, Sequence.class);
			modifiedStory = bundle.get(2, Sequence.class);
			
			Connections.getPorts(this).transmit(EXPORT_COMMONSENSE, commonsense);
			Connections.getPorts(this).transmit(EXPORT_REFLECTIVE, reflective);
			Connections.getPorts(this).transmit(EXPORT_TEXT, modifiedStory);
		}
	}
	
	public void processSimulationResults(Object o){
		if (o instanceof Sequence){
			simulatedAudienceReaction = (Sequence) o;
			Connections.getPorts(this).transmit(SIMULATION_RESULTS_OUT, simulatedAudienceReaction);
		}
	}
	
}

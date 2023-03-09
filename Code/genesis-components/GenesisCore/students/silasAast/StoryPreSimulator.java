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


public class StoryPreSimulator extends AbstractWiredBox{
	
	// INPUT PORTS
	 
	 public static String AUDIENCE_COMMONSENSE_IN; // from StaticAudienceModeler. Sequence of commonsense rules.
	 public static String AUDIENCE_REFLECTIVE_IN; // from StaticAudienceModeler. Sequence of reflective rules.
	 public static String TEXT_IN; //from StoryModifier.  inner representation of story to be told.
	 	
	 
	 // OUTPUT PORTS
	 public static String AUDIENCE_COMMONSENSE_OUT;
	 public static String AUDIENCE_REFLECTIVE_OUT;
	 public static String SIMULATION_INFO_OUT; // to StorySimulator.  betterSignal wraps audience and story information. 

	 
	 // FIELDS
	 private Sequence commonsenseRules = new Sequence();
	 private Sequence conceptRules = new Sequence();
	 private Sequence modifiedStory = new Sequence();
	 
	 public StoryPreSimulator(){
		 Connections.getPorts(this).addSignalProcessor(AUDIENCE_COMMONSENSE_IN, "storeCommonsense");
		 Connections.getPorts(this).addSignalProcessor(AUDIENCE_REFLECTIVE_IN, "storeReflective");
		 Connections.getPorts(this).addSignalProcessor(TEXT_IN, "storeModifiedStory");
	 }
	 	 
	 public void storeCommonsense(Object o){
		 if (o instanceof Sequence){
			 commonsenseRules = (Sequence) o;
//			 Connections.getPorts(this).transmit(AUDIENCE_COMMONSENSE_OUT, commonsenseRules);
		 }
	 }
	 
	 public void storeReflective(Object o){
		 if (o instanceof Sequence){
			 conceptRules = (Sequence) o;
//			 Connections.getPorts(this).transmit(AUDIENCE_REFLECTIVE_OUT, reflectiveRules);
		 }
	 }
	 
	 public void storeModifiedStory(Object o){
		 if (o instanceof Sequence){
			 modifiedStory = (Sequence) o;
			 BetterSignal simulatorWrap = new BetterSignal(commonsenseRules, conceptRules, modifiedStory);
			 Connections.getPorts(this).transmit(SIMULATION_INFO_OUT,simulatorWrap);
		 }
	 }

}

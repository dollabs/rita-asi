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



public class StoryEnvironment extends AbstractWiredBox {

	//INPUT PORTS
	public static String BEGIN;
	
	//OUTPUT PORTS
	
	public static String GOAL; // Can be SUMMARY, SPOONFEED, PRIME, PREPERSUADE, PERSUADE, DECEIVE
	  						  // In the case of persuade and deceive, "goal" will have extra layers providing direction.
	public static String DECLARED_AUDIENCE;
	public static String ACTUAL_AUDIENCE;
	
	public StoryEnvironment(){
		
	}
	
	public void setEnvironment(String goal, String declared, String actual){
		Connections.getPorts(this).transmit(GOAL, goal);
		Connections.getPorts(this).transmit(DECLARED_AUDIENCE, declared);
		Connections.getPorts(this).transmit(ACTUAL_AUDIENCE, actual);
	}
	
}

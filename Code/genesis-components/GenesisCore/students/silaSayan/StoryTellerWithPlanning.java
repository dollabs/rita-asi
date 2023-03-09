package silaSayan;

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

@SuppressWarnings("unused")
public class StoryTellerWithPlanning extends AbstractWiredBox {

	;
	
	// OPTIONS FIELDS
	
	
	private int SPOONFEED = 0, PRIME= 1, PREPERSUADE = 2, PERSUADE = 3, DECEIVE = 4, SUMMARIZE = 5; // modes of story telling
	private int goal = SPOONFEED; // story telling goal [e.g. could be SUMMARIZE]
	private int GENERIC = 0, STUDENT = 1;
	private int audience = GENERIC;
	
	// FIELDS
	
	private Sequence proposedStory; //what story the narrator plans to tell the listener
	private Sequence finalStory; //story that narrator finds will successfully inform the audience according to goal.
	
    //INPUT PORTS
	public static String RAW_STORY_INPUT_PORT = "unprocessed story coming in as representations"; // from StoryProcessor.
	public static String RAW_STORY_READABLE_INPUT_PORT = "unprocessed story coming in as string"; // from StoryProcessor. broadcasts plot of story (e.g. "Macbeth and Duncan are persons")
	public static String UPDATED_STORY_INPUT_PORT = "updated version of story"; // from UpdateStory. new versions of story to better fit audience and goal.  
 	public static String AUDIENCE_REACTION_INPUT_PORT = "simulated audience reaction"; //from AudienceTestRun. simulated audience's response to story.
	public static String RELEVANT_STORY_INPUT_PORT = "story relevant to goal"; //from ExtractRelevance. 
 	
	//OUTPUT PORTS
	public static String DECLARED_AUDIENCE; //TEMP. to AudienceTestRun. broadcasts declared audience
	public static String GOAL; // TEMP. to ExtractRelevance. port on which to broadcast goal
	public static String UPDATED_STORY_OUTPUT_PORT = "unprocessed new story going out"; //to AudienceTestRun.
	public static String AUDIENCE_REACTION_OUTPUT_PORT = "simulated audience reaction"; //to ExtractComprehensionGaps.
	public static String RELEVANT_STORY_OUTPUT_PORT = "story relevant to goal"; //to ExtractComprehensionGaps.
	
	public StoryTellerWithPlanning(){
		this.setName("My story teller");
//		Connections.getPorts(this).addSignalProcessor(DECLARED_AUDIENCE, "meetAudience");
		Connections.getPorts(this).addSignalProcessor(RAW_STORY_INPUT_PORT, "storeStory");
		Connections.getPorts(this).addSignalProcessor(UPDATED_STORY_INPUT_PORT, "storeStory");
		Connections.getPorts(this).transmit(GOAL, goal);  //POSSIBLE PROBLEM HERE. CAN YOU TRANSMIT AN INTEGER???
		Connections.getPorts(this).transmit(DECLARED_AUDIENCE, audience);
	}
	
	public void storeStory(Object o){
		if (o instanceof Sequence){
			proposedStory = (Sequence) o;
			Connections.getPorts(this).transmit(UPDATED_STORY_OUTPUT_PORT, proposedStory);
		}
	}
	

	
//	//
//	public void meetAudience(Object o){
//		
//		if (o instanceof Integer){
//			audience = (Integer) o;
//		}
//		ModelAudience.loadAudienceModel(audience);		
//	}
	
	
	
	
	
}

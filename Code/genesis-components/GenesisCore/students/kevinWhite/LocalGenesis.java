package kevinWhite;

import connections.Connections;
import genesis.Genesis;
import gui.*;
import storyProcessor.StoryProcessor;
import utils.Mark;

/*
 * Created on Mar 25, 2010
 * @author phw
 */

@SuppressWarnings("serial")
public class LocalGenesis extends Genesis {   

	LocalProcessor localProcessor;

	public LocalGenesis() {
		super();
		Mark.say("Kevin's constructor");
		// Local wiring goes here; example shown
		// Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, getStoryProcessor1(), getLocalProcessor());
		// Example of disconnection
		// Connections.disconnect(getStoryProcessor1(), getLocalProcessor());
		Connections.wire(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, this.getLocalProcessor(), getJobPanel());
	}

	public LocalProcessor getLocalProcessor() {
		if (localProcessor == null) {
			localProcessor = new LocalProcessor();
		}
		return localProcessor;
	}
	
	WiredBlinkingBox kevinsBlinker;
	
	public WiredBlinkingBox getKevinsBlinker() {
		Mark.say("Constructing Kevin's blinker");
		if (kevinsBlinker == null) {
			kevinsBlinker = new WiredBlinkingBox("Goal2", getGoalExpert(), new GoalPanel(), getExpertsPanel());
		}
		return kevinsBlinker;
	}
	
//	WiredBlinkingBox partPanel;
//	
//	public WiredBlinkingBox getPartPanel(){
//	    Mark.say("Constructing Part Panel");
//	    if (partPanel == null){
//	        partPanel = new WiredBlinkingBox("Part2", getPartExpert(), new PartPanel(), getExpertsPanel());
//	    }
//	    return partPanel;
//	}
//	
//	WiredBlinkingBox possessionPanel;
//	public WiredBlinkingBox getPossessionPanel(){
//        Mark.say("Constructing Possession Panel");
//        if (possessionPanel == null){
//            possessionPanel = new WiredBlinkingBox("Possession2", getPossessionExpert(), new PossessionPanel(), getExpertsPanel());
//        }
//        return possessionPanel;
//    }
	
	WiredBlinkingBox jobPanel;
	public WiredBlinkingBox getJobPanel(){
	    Mark.say("Constructing Job Panel");
	    if (jobPanel == null){
	        jobPanel = new WiredBlinkingBox("Job2",getJobExpert(),new JobPanel(), getExpertsPanel());
	    }
	    return jobPanel;
	}

	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
	}
}


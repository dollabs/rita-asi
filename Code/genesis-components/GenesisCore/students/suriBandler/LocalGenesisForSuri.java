// Updated 10 June 2015

package suriBandler;

import connections.Connections;
import genesis.Genesis;
import mentalModels.MentalModel;
//import howTo.WriteMessageIntoCommentaryPanel;
import start.*;
import storyProcessor.StoryProcessor;
import utils.Mark;

/**
 * This is a personal copy of Genesis I can play with without endangering the code of others. I will also want to look
 * at the main methods in Entity, for examples of how the representational substrate works, and Generator, for examples
 * of how to go from English to Genesis's inner language and back.
 * 
 * @author phw
 */

@SuppressWarnings("serial")
public class LocalGenesisForSuri extends Genesis {
	
	

	//public static String OUTPUT_AUTHOR_STORY = "my output port";
	
	public static String MY_PORT = "my port";
	// ADDING GUI: For your box, create a commentary port
	public static String COMMENTARY = "commentary port";
	
	public LocalGenesisForSuri() {
		super();
		Mark.say("Local constructor for Suri");

		// Dead? PHW this is a mystery processor that I think does nothing because not wired to anything
		Connections.getPorts(this).addSignalProcessor(MY_PORT, getRashiExperts()::processStoryProcessor);

		// Wire your box's commentary port to the commentary container. You probably will do this in your copy of
		// Genesis or in one of the Genesis plug boards.

	}
	

	
	
	/*
	 * Fires up my copy of Genesis in a simple Java frame. It can also be started up in other ways; that is the reason
	 * for the startInFrame call.
	 */
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		LocalGenesisForSuri myGenesis = new LocalGenesisForSuri();
		myGenesis.startInFrame();
		
	}
}

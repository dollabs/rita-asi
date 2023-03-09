package cagriZaman;

import connections.Connections;
import constants.Markers;
import frames.entities.Entity;
import genesis.Genesis;
import genesis.GenesisGetters;
import start.Start;
import start.StartPreprocessor;
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
public class LocalGenesis extends Genesis {

	LocalProcessor localProcessor;

	SocketTester socketTester;



	public LocalGenesis() {
		super();
		Mark.say("Cagri's local constructor");

		// For the moment, no final processing; story never completes
		// Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(),
		// StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getLocalProcessor());

		// Wire socket tester to story processor
		Connections.wire(SocketTester.WORLD, getSocketTester(), StoryProcessor.INJECT_ELEMENT_WITHOUT_DEREFERENCE, getMentalModel1()
		        .getStoryProcessor());
		
		Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), getSocketTester());

		Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, getLocalProcessor());
		Connections.wire(StoryProcessor.INCREMENT_PORT,getMentalModel1(),StoryProcessor.INCREMENT_PORT,getSocketTester());
		
		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(),StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT,getLocalProcessor());
		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getMentalModel1().getStoryProcessor(),StoryProcessor.STORY_PROCESSOR_SNAPSHOT,getLocalProcessor());
		// Not sure if this works; needs testing
		//StartPreprocessor.getStartPreprocessor().process(Markers.START_EXPERIMENT);

//		Entity xx = new Entity("thing");
//		Entity yy = new Entity("thing");
//		Entity zz = new Entity("thing");
//
//		Entity rf1 = RoleFrames.makeRoleFrame(xx, "appear");
//		Entity rf2 = RoleFrames.makeRoleFrame(yy, "appear");
//
//		RoleFrames.addRole(rf1, "on", yy);
//		RoleFrames.addRole(rf2, "on", zz);
//
//		Entity rfc = RoleFrames.makeRoleFrame(xx, "appear");
//		RoleFrames.addRole(rfc, "above", zz);
//
//		Entity rule = Rules.makePredictionRule(rfc, rf1, rf2);

	

		// This hack differs from all other usage! Never set to true anywhere else
		StoryProcessor.allowMultipleEntriesInSameScene = true;
		//getMentalModel1().getStoryProcessor().addToRuleList(new Entity());
		// Start up story; needed to show story elements in dashboard
		//getMentalModel1().getStoryProcessor().startStory(ISpeak.makeRoleFrame(new Entity(Markers.YOU), "start", "The World"));

	}	

	/**
	 * Get an instance of LocalProcessor to do something with the output of a complete story object from a story
	 * processor.
	 */
	 public LocalProcessor getLocalProcessor() {
	 if (localProcessor == null) {
	 localProcessor = new LocalProcessor();
	 }
	 return localProcessor;
	 }

	public SocketTester getSocketTester() {
		if (socketTester == null) {
			socketTester = new SocketTester();
		}
		return socketTester;
	}

	/*
	 * Fires up my copy of Genesis in a simple Java frame. It can also be started up in other ways; that is the reason
	 * for the startInFrame call.s
	 */
	public static void main(String[] args) {

		LocalGenesis myGenesis = new LocalGenesis();
		myGenesis.startInFrame();
		myGenesis.getSocketTester().run();
	}
}

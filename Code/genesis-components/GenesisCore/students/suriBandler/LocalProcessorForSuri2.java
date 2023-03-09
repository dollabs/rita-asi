// Updated 10 June 2015

package suriBandler;

import java.util.*;


import connections.*;
import constants.GenesisConstants;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import genesis.GenesisGetters;
import matchers.Substitutor;
import mentalModels.MentalModelDemo;
import start.Start;
import storyProcessor.StoryProcessor;
import subsystems.rashi.RashiHelpers;
import subsystems.rashi.RashisExperts;
import utils.Html;
import utils.Mark;

/**
 * A local processor class that just receives a complete story description, takes apart the wrapper object to fetch
 * various parts of the complete story description, and prints them so you can see what is in there.
 */
public class LocalProcessorForSuri2 extends AbstractWiredBox {

	// EXamples of how ports are named, not used here
	public final String MY_INPUT_PORT = "my input port";
	public final String MY_OUTPUT_PORT = "my output port";
	public final RashisExperts oldProcessor = new RashisExperts();
	
	/**
	 */
	public LocalProcessorForSuri2() {
		super("Local story processor");
		// Receives story processor when story has been processed
		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::reset);

		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::processStoryProcessor);
	}

	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 * <p>
	 * This one writes information extracted from the story processor received on the STORY_PROCESSOR_SNAPSHOT port.
	 */
	public void processStoryProcessor(Object signal) {
		boolean debug = true;
		// Make sure it is what was expected
		// Make sure it is what was expected
		Mark.say("Entering processStoryProcessor");

		if (signal instanceof StoryProcessor) {
			StoryProcessor processor = (StoryProcessor) signal;

			Sequence story = processor.getStory();
			Sequence explicitElements = processor.getExplicitElements();
			Sequence inferences = processor.getInferredElements();
			Sequence concepts = processor.getInstantiatedConceptPatterns();
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			Mark.say(debug, "\n\n\nExplicit story elements");
			explicitElements.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated commonsense rules");
			inferences.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nInstantiated concept patterns");
			concepts.stream().forEach(e -> Mark.say(debug, e));
			Mark.say(debug, "\n\n\nAll story elements, in English");
			Generator generator = Generator.getGenerator();
			story.stream().forEach(e -> Mark.say(debug, generator.generate(e)));

			processor.getRuleMemory().getRuleSequence().getElements().stream().filter(r -> r.getProbability() == null ? true : false)
			        .forEach(r -> Mark.say(debug, "Rule:", r.getProbability(), r));

			Mark.say("Recorded stories", GenesisGetters.getMentalModel1().getStoryMemory().size());

			Mark.say("Map size", GenesisGetters.getMentalModel1().getStoryMemory().getMemory().size());

			GenesisGetters.getMentalModel1().getStoryMemory().getMemory().values().stream().forEach(m -> Mark.say("Title", m.getTitle()));
			/// ----- Testing Out  ---- //
			List<Entity> children = new ArrayList<Entity>();
			story.getElements().stream().forEach(f -> children.add(f));//.getChildren()));
			String result = processElements(children);
			
			
		}
	}
	//Added for Commentary panel stuff
			/**
			 * Define a helper method that hides some details
			 */
			private void say(Object... objects) {
				// First argument is the box that wants to write a message
				// Second argument is commentary port wired to the commentary panel
				// Third argument is location on screen: LEFT, RIGHT, BOTTOM
				// Fourth argument is tab title
				// Final arguments are message content
				Mark.comment(this, LocalGenesisForSuri.COMMENTARY, GenesisConstants.BOTTOM, "Metaphor Output", objects);
			}
			
	public String processElements(List<Entity> elements){
		Mark.say("Begin testing, here");
		boolean forwardThinking = false;
		boolean think = false;
		boolean like = false;
		Entity michael = null;
		Entity john = null;
		Entity johnMom = null;
		Entity michaelMom = null;
		Entity steven = null;
		Entity sammy = null;
		Entity sandwich = null; 
		Entity candy = null;
		
		String processResults = "";//Html.large("The Original Stories\n");
		
		List<Entity> thoughts = new ArrayList<Entity>();
		for(Entity e : elements){
			//Mark.say(e.toEnglish(), e);
			//processResults += (e.toEnglish() + " ");
			List<String> actions = (RashiHelpers.getActionThreadString(e));
			Mark.say(actions, e.toEnglish());
			Map<String, Entity> subjectObject = RashiHelpers.getSeqSubjectAndObject(e);
			if(subjectObject.containsKey("subject")){
				if(subjectObject.get("subject").toEnglish().contains("Michael")){
					michael = subjectObject.get("subject");
					
					if(subjectObject.get("subject").toEnglish().contains("mom") && michaelMom==null){
						michaelMom = subjectObject.get("subject");
					}
				}
				
			}
			
			if(subjectObject.containsKey("object")){
				if(subjectObject.get("object").toEnglish().contains("Candy") && candy==null){
					candy = subjectObject.get("object");
				}
				else if(subjectObject.get("object").toEnglish().contains("sandwich") && sandwich==null){
					sandwich = subjectObject.get("object");
				}
			}
			if(subjectObject.containsKey("subject")){
				if(subjectObject.get("subject").toEnglish().contains("Steven") && steven==null){
					steven = subjectObject.get("subject");
					
				}
				else if(subjectObject.get("subject").toEnglish().contains("Sammy") && sammy==null){
					sammy = subjectObject.get("subject");
					
				}
				
				
				else if(subjectObject.get("subject").toEnglish().contains("John")){
					john = subjectObject.get("subject");
					
					if(subjectObject.get("subject").toEnglish().contains("mom")){
						johnMom = subjectObject.get("subject");
					}
				}
			}
			if(actions.contains("like")){
				like = true;
			}
			if(actions.contains("notice")){
				forwardThinking = !forwardThinking;
			}
			
			//if(actions.contains("disappear")){
				//forwardThinking = !forwardThinking;
			//}
			if(actions.contains("think")){
				think = true;
			}
			
			if(forwardThinking && think){ 
				thoughts.add(e);
			}
			//TODO: When processing, slice the list from "thinks", rest is actions
			// that can be applied to John.
			
		}
		Mark.say("THOUGHTS:", thoughts);
		
		
		List<Entity> results = new ArrayList<Entity>();
		Mark.say("JOHN, MICHAEL", john, michael);
		Mark.say("SAMMY, STEVEN", sammy, steven);
		Mark.say("CANDY sandwhich", candy, sandwich);
		Mark.say("Johnmom michaelmom", johnMom, michaelMom);
		if(like){
		for(Entity thought : thoughts){
			
			Entity newRoleFrame = Substitutor.substitute(john, michael, thought);
			newRoleFrame = Substitutor.substitute(candy, sandwich, newRoleFrame);
			newRoleFrame = Substitutor.substitute(sammy, steven, newRoleFrame);
			newRoleFrame = Substitutor.substitute(johnMom, michaelMom, newRoleFrame);
			Mark.say("FROM", thought.toEnglish(), "GENERATED:", newRoleFrame.toEnglish());
			results.add(newRoleFrame);
			}
		}
		
		Mark.say("RESULTS", results);
		processResults += Html.ital("\n  Because John returns the food like michael\n");
		for (Entity result : results){
			if(result.toEnglish().contains("selfish")){
				processResults += (Html.red(result.toEnglish()) + "\n");
			}
			else{
				processResults += (result.toEnglish() + "\n");
			}
			if(result.toEnglish().equals("John is selfish.")){
				break;
			}
		}
		say(processResults);
		return "Success";
		
	}
	
	public void reset(Object signal) {
		// Does nothing right now
	}

	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		MentalModelDemo.main(args);
	}
}
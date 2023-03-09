package cagriZaman;

import generator.Generator;
import generator.RoleFrames;
import storyProcessor.StoryProcessor;
import translator.Translator;
import utils.Mark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conceptNet.conceptNetModel.ConceptNetFeature;
import conceptNet.conceptNetModel.ConceptNetFeature.FeatureType;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import conceptNet.conceptNetNetwork.ConceptNetScoredAssertion;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/**
 * A local processor class that just receives a complete story description, takes apart the wrapper object to fetch
 * various parts of the complete story description, and prints them so you can see what is in there.
 */
public class LocalProcessor extends AbstractWiredBox {

	// EXamples of how ports are named, not used here
	public final String MY_INPUT_PORT = "my input port";

	public final String MY_OUTPUT_PORT = "my output port";

	public final Set<String> DIRECTIONS =  Stream.of("up", "down", "left", "right","forward").collect(Collectors.toCollection(HashSet::new));
	/**
	 * The constructor places the writeoutStoryAnalysis signal processor on two input ports for illustration. Only the
	 * StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT is wired to in LocalGenesis.
	 */
	public LocalProcessor() {
		this.setName("Cagri's local story processor");
		// Example of default port connection, old style using string to identify signal processor
		// Connections.getPorts(this).addSignalProcessor("writeoutStoryAnalysis");
		// New style, Java 8, uses functional argument
		// Connections.getPorts(this).addSignalProcessor(this::writeoutStoryAnalysis);
		// Example of named port connection, old style using string to identify signal processor
		// Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT,
		// "writeoutStoryAnalysis");
		// New style, Java 8, uses functional argument
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, this::writeoutStoryAnalysis);
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT,this::processStoryProcessor);
		// Another example using output of sentence translation, which is the input to the mental model, note that the
		// default input port is used (see LocalGenesis).
		Connections.getPorts(this).addSignalProcessor(this::writeoutGeneseseAnalysis);
	}

	/**
	 * Writes the signal, which will be a human readable Inerese translation of an English input sentence.
	 */
	public void writeoutGeneseseAnalysis(Object signal) {
		boolean debug = true;
		// Now proceed to print what has come into the box over the default input port; this Gensis print convention
		// produces console output that
		// a click traces back to where the producing Mark.say(...) lies. Not also that if the first argument is a
		// boolean, the statement prints or does not according to value. A useful convention is to turn on all
		// print statements in a method with a controlling boolean.

		// Load the system, set debug to true, and click on "Run test sentences"
		// Mark.say(true, "Innerese:", signal);
	}

	/**
	 * You have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 */
	public void writeoutStoryAnalysis(Object signal) {
		// Load the system, set debug to true, and click on "Demonstrations > Shakespeare basic > Macbeth"
		boolean debug = true;
		// Should always check to be sure my input is in the expected form and ignore it if not. A BetterSignal is just
		// a convenient wrapper for multiple objects that allows easy extraction of objects without further casting.
		if (signal instanceof BetterSignal) {
			// Shows how to take BetterSignal instance apart, the one coming in on COMPLETE_STORY_ANALYSIS_PORT port.
			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);
			
			 
			
			Mark.say(debug, "\n\n\nStory elements");
			story.getElements().stream().forEach(f -> Mark.say(debug, f));
			// for (Entity e : story.getElements()) {
			// // Print story element in human-readable, parenthesized form
			// Mark.say(debug, e);
			// }
			Mark.say(debug, "\n\n\nExplicit story elements");
			for (Entity e : explicitElements.getElements()) {
				Mark.say(debug, e);
			}
			Mark.say(debug, "\n\n\nInstantiated commonsense rules");
			for (Entity e : inferences.getElements()) {
				Mark.say(debug, e);
			}
			Mark.say(debug, "\n\n\nInstantiated concept patterns");
			concepts.getElements().stream().forEach(e -> Mark.say(e));

			Mark.say(debug, "\n\n\nAll story elements, in English");
			Generator generator = Generator.getGenerator();

			story.getElements().stream().forEach(f -> Mark.say(generator.generate(f)));
		}
	}
	
	public String whereAmI(String[] objects ){
		
		HashMap<String,Integer> placeFrequency = new HashMap<String,Integer>();
		
		

		for (String s:objects){
			ConceptNetFeature places = new ConceptNetFeature(s, "AtLocation", FeatureType.LEFT);
	        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
	                ConceptNetClient.featureToAssertions(places);	
	        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
	        Set<ConceptNetQueryResult<Double>> placeResults = flattenedResults.stream()    
	                .collect(Collectors.toSet());
	        
	        for (ConceptNetQueryResult< Double> place:placeResults){
	        	String placeResultString=place.getQuery().getComponentConcepts().get(1).getConceptString().split(" ")[0];
	        	if(placeFrequency.containsKey(placeResultString))
					placeFrequency.put(placeResultString,placeFrequency.get(placeResultString)+1);
	        	else
	        		placeFrequency.put(placeResultString,1);
	        }
		}
		
		List<String> sortedPlaces =placeFrequency.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
		.map(f->f.getKey()).collect(Collectors.toList()); // or any other terminal method
		return sortedPlaces.get(0);
	}
	
	
	public boolean getLocationRelation(String s, String o ){
		
		ConceptNetFeature causeGoalFeature = new ConceptNetFeature(s, "AtLocation", FeatureType.LEFT);
        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
                ConceptNetClient.featureToAssertions(causeGoalFeature);	
        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
        Set<ConceptNetQueryResult<Double>> locationResults = flattenedResults.stream()
                
                .collect(Collectors.toSet());
        
        for (ConceptNetQueryResult< Double> location:locationResults){
        	if(o.toLowerCase().equals(location.getQuery().getComponentConcepts().get(1).getConceptString())){
        		Mark.say("Concept net AtLocation match!");
        		return true;
        	}
        }
		
		
		return false;
	}
	
	public String observeThing(String s){
		ConceptNetFeature causeGoalFeature = new ConceptNetFeature(s, "UsedFor", FeatureType.LEFT);
        ConceptNetQueryResult<List<ConceptNetScoredAssertion>> unflattenedResult = 
                ConceptNetClient.featureToAssertions(causeGoalFeature);	
        List<ConceptNetQueryResult<Double>> flattenedResults = ConceptNetQueryResult.flattenResult(unflattenedResult);
        
        return flattenedResults.get(0).getQuery().getComponentConcepts().get(1).getConceptString();   
        
	}
	
	

	public void processStoryProcessor(Object signal)
	{
		Mark.say("PROCESS STORY PROCESSOR HAS BEEN CALLED");
		if (signal instanceof StoryProcessor) {
			Generator generator =Generator.getGenerator();
			StoryProcessor processor = (StoryProcessor) signal;   
			Sequence explicitElements = processor.getExplicitElements();
			Translator translator = new Translator();
			
			//Begin the description by telling what type of space is it.. Use ConceptNet to infer the most probable location from the observed objects.
			Set<String> setOfObjects=explicitElements.stream()
									.filter(entity-> entity.getObject().getType().equals("roles"))
									.map(f->f.getObject().getElement(0).getSubject().toEnglish())
									.filter(f -> !DIRECTIONS.contains(f))
									.collect(Collectors.toSet());
			
			String place = whereAmI(setOfObjects.toArray(new String[0]));
			Mark.say(place);
			

			
			
			// REFER TO CONCEPT NET TO FIGURE OUT THE COMPOSITIONAL STRUCTURE (WHAT IS WHERE)
			String prevEntity = "";
			String direction="forward";
			HashMap<String, String> locationPairs  = new HashMap<String,String>();
			HashMap<String,String>	directionPairs = new HashMap<String,String>();
			for(Entity e:explicitElements.getElements()){
				if(e.getObject().getType().equals("roles")){
					String entityName=e.getObject().getElement(0).getSubject().getPrimedThread().lastElement();
					if(!(DIRECTIONS.contains(entityName))){
						directionPairs.put(entityName, direction);

						if(getLocationRelation(entityName,prevEntity)){
							locationPairs.put(entityName, prevEntity);
							Mark.say(entityName,prevEntity);
						}
						else if(getLocationRelation(prevEntity,entityName)){
							locationPairs.put(prevEntity, entityName);
							Mark.say(entityName,prevEntity);

						}
						prevEntity=entityName;
					}
					else{
						direction = entityName;
						prevEntity="";
					}
								
				}
			}
			
	
			
			//ADD DETAILED OBSERVATIONS FROM CONCEPTNET
			HashMap<String,String> detailedObservations = new HashMap<String,String>();
			explicitElements.stream().forEach(f-> {
				if(f.getPrimedThread().lastElement().equals("observe")){
					
					String thing = f.getObject().getElement(0).getSubject().getPrimedThread().lastElement();
					String usedFor=observeThing(thing);
					detailedObservations.put(thing, usedFor);

				}			
			});
			
			// COMPOSE FINAL DESCRIPTION
			// FIRST TALK ABOUT WHERE WE ARE AND PROVIDE US AN ORIENTATION.
			// THEN GO THROUGH EACH DIRECTION AND DESCRIBE WHAT IS THERE.
			// TODO : FIND OUT HOW TO ACCESS INJECTED STORY ELEMENTS. 
			ArrayList<Entity> completeDescription = new ArrayList<Entity>();
			
			Entity whereIsJohn = RoleFrames.makeRoleFrame("John", "is", "in", place);
			whereIsJohn.addProperty(Markers.GOAL_ANALYSIS, true,true);	
			processor.injectElementWithDereference(whereIsJohn);
			completeDescription.add(whereIsJohn);
			directionPairs.entrySet().stream().sorted(Map.Entry.comparingByValue())
					.forEach(entry->{
						String k = entry.getKey();
						String v = entry.getValue();
						Mark.say(entry.getKey() + " " + entry.getValue());
						Entity directionRelation;
						if(!locationPairs.containsKey(k)){ // ONLY TALK ABOUT THINGS THAT APPEAR INDEPENDENT. THEN GO INTO EACH DEPENDENT OBJECT.
							if(v.equals("forward")){
						        directionRelation = RoleFrames.makeRoleFrame(k, "appears", "in", "front");
							}
							else if(v.equals("left") || v.equals("right")){
						        directionRelation = RoleFrames.makeRoleFrame(k, "appears", "on", v);
							}
							else{
						        if (v.equals("up"))
							        directionRelation = RoleFrames.makeRoleFrame(k, "appears", "above", "room");
						        else
							        directionRelation = RoleFrames.makeRoleFrame(k, "appears", "below", "room");
							}
							
							directionRelation.addProperty(Markers.GOAL_ANALYSIS,true,true);
							processor.injectElementWithDereference(directionRelation);
							completeDescription.add(directionRelation);
							Mark.say("Direction relation was injected into the story for " + k + " " + v);
							
							if(detailedObservations.containsKey(k)){
								Entity observation = translator.internalize(k+" allows you to "+detailedObservations.get(k));
								observation.addProperty(Markers.GOAL_ANALYSIS, true,true);
								processor.injectElementWithDereference(observation);
								completeDescription.add(observation);
							}
							if(locationPairs.containsValue(k)){
								locationPairs.entrySet().stream().filter(e2 ->e2.getValue().equals(k)).forEach(e3->{
									
							        Entity objectAt = RoleFrames.makeRoleFrame(e3.getKey(), "appears", "at", k);
									objectAt.addProperty(Markers.GOAL_ANALYSIS,true,true);
									processor.injectElementWithDereference(objectAt);
									completeDescription.add(objectAt);
									if(detailedObservations.containsKey(e3.getKey())){
										Entity observation = translator.internalize(e3.getKey()+" allows you to "+detailedObservations.get(e3.getKey()));
										observation.addProperty(Markers.GOAL_ANALYSIS, true,true);
										processor.injectElementWithDereference(observation);
										completeDescription.add(observation);
									}
									});
							}
						}
						
			});
			
			
			//OUTPUT THE FULL DESCRIPTION IN ENGLISH
			Mark.say("BEGIN DESCRIBING THE SPACE");

			completeDescription.stream().forEach(element->{
				Mark.say(element.toEnglish());
			});
			
		}
	}
	/**
	 * Merely calls main method in LocalGenesis, a shortcut
	 */
	public static void main(String[] args) {
		LocalGenesis.main(args);
	}
}
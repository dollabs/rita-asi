package hibaAwad;

import java.util.*;

import matchers.StandardMatcher;
import storyProcessor.*;
import utils.Html;
import utils.minilisp.LList;
import utils.*;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

public class EastWestExpert extends AbstractWiredBox {

	Entity test;
	Entity cause;
	Entity cause2;
	Entity result;
	ConceptAnalysis analysis;
	public ArrayList<Entity> causes = new ArrayList<Entity>();
	public ArrayList<String> untranslatedCauses = new ArrayList<String>();
	public Vector<Sequence> stories = new Vector<Sequence>();
	
	public static final String FINAL_INFERENCES = "final inferances port";
	
	public static final String FINAL_STORY= "final story port";
	
	public static final String COHERENCE_DATA = "coherence data port";
	public static final String COHERENCE_TEXT = "coherence text port";
	public static final String COHERENCE_LABEL = "coherence label port";
	public static final String COHERENCE_AXIS = "coherence axis label port";
	public static final String REFLECTION = "reflection port";
	public static final String CAUSAL_ANALYSIS= "causal analysis";
	//public static final String COHERENCE_INPUT_PORT = "coherence port";
	public static final String FULL_STORY_INPUT_PORT = "complete story input port";

	public static final String MY_OUTPUT_PORT = "my output port";

	public EastWestExpert() {
		this.setName("My story processor");
		Connections.getPorts(this).addSignalProcessor(REFLECTION,"processReflections" );
		Connections.getPorts(this).addSignalProcessor(FULL_STORY_INPUT_PORT, "processStory");
	
		/*try {
			test = Translator.getTranslator().translate("WW may kill YY because WW is angry at YY.").getElements().get(0);
			test.addType("inference");
			test.addType("explanation");
			untranslatedCauses.add("Lu is insane");
			untranslatedCauses.add("America is individualistic");
			untranslatedCauses.add("America's media glorifies violence");
			untranslatedCauses.add("Goertz fails to help Lu");
			cause =  Translator.getTranslator().translate("Lu is insane").getElements().get(0);
//			cause.addType("presonality-type");
			cause2 = Translator.getTranslator().translate("America is individualistic").getElements().get(0);
			Thing cause3 = Translator.getTranslator().translate("America's media glorifies violence").getElements().get(0);
			Thing cause4 = Translator.getTranslator().translate("Goertz fails to help Lu").getElements().get(0);
			causes.add(cause);
			causes.add(cause2);
			causes.add(cause3);
			causes.add(cause4);
			for (Thing factor:causes){
				Mark.say(factor.asString());
			}
			result = Translator.getTranslator().translate("Lu kills Shan").getElements().get(0);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Mark.say("cause:", cause.asString());
		Mark.say("Test:", test.asString());
		*/
	}
	
	/**
	 * 
	 * @param signal
	 * processes both coherence and causal modules. 
	 */
	@SuppressWarnings("unused")
	public void processStory(Object signal ){
		
		Mark.say("Receiving complete story analysis!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		BetterSignal s = (BetterSignal) signal;
		Sequence story = s.get(0, Sequence.class);
		Sequence explicitElements = s.get(1, Sequence.class);
		Sequence inferences = s.get(2, Sequence.class);
		Sequence concepts = s.get(3, Sequence.class);
		//ArrayList<boolean> results = new ArrayList<boolean>();
		//get Coherence data)
		Mark.say("Calling processCoherence");
		processCoherence(story);
		
	//	for (Thing cause: causes){
	//		boolean answer = evaluateCause(story, analysis.getReflectionDescriptions(), cause, result);
	//		results.add(answer);
	//	}
		Connections.getPorts(this).transmit(COHERENCE_LABEL, StoryProcessor.getTitle(story));
		Connections.getPorts(this).transmit(CAUSAL_ANALYSIS , Html.bold(StoryProcessor.getTitle(story)) + "\n");
		
	//	for (int i = 0; i<causes.size(); i++){
	//		Connections.getPorts(this).transmit(CAUSAL_ANALYSIS, Html.red(untranslatedCauses.get(i)) + ": " + results.get(i));				
	//	}
		
	//	for (int result:results){
	//		Mark.say(result);
	//	}
		
	//	for (Thing element: story.getElements()){
	//		Mark.say(element.asString()); 
	//		for (Thing factor:causes){
	//			Mark.say("matching:" + factor.asString());
	//			Mark.say(BasicMatcher.getBasicMatcher().match(cause,element ));
	//		}
	//	}
		
	}
	
	/**
	 * 
	 * @param story
	 * @param cause
	 * @param result
	 * causal evaluation module
	 * @return 1 if the cause is important to the result. 0 otherwise. 
	 */
	public static boolean evaluateCause(Sequence story, ArrayList<ConceptDescription> reflections, Entity cause, Entity result ) {
		// check if the cause exists in the story:
		int answer = 0;
		// found a match.
		// get distance to the main results.
		int distance = -1;
		int distance2 = -1;
		
		//Updated this to fix bugs - mpfay
		Vector<Entity> dist;
		dist = StoryMethods.findPath(cause, result, story);
		if(dist != null)
			distance = dist.size();
		dist = StoryMethods.findPath(result, cause, story);
		if(dist != null)
			distance2 = dist.size();
		
		Mark.say(distance);
		if (distance != -1 || distance2!=-1) {
			answer=+1;
		}
		//otherwise check if it matches any of concept patterns. 
		else {
			Mark.say("concepts");
			for (ConceptDescription rd : reflections){
			
				for (Entity t : rd.getStoryElementsInvolved().getElements()) {
					//Mark.say(t);
					
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(cause, t);

					if (bindings != null) {
							answer =+1;
					}
				}
			}
		}
		if (answer>0)
			return true;
		else return false;
	}
	
	public static boolean existStory(Entity element, Sequence story ){
		boolean found = false;
		for (Entity storyElement : story.getElements()){
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(storyElement, element);
			if (bindings!=null){
				found = true;
			}
		}
		return found;
		
	}
	public void processReflections(Object signal){
		analysis = (ConceptAnalysis) signal;
//		Mark.say("here");
		
	//	Connections.getPorts(this).transmit(COHERENCE_TEXT, CoherenceViewer.CLEAR);
		
		/*Mark.say("reflection analysis received");
		for (ReflectionDescription rd : analysis.getReflectionDescriptions()){
			Mark.say("reflection description:");
			Mark.say(rd.getName());
			for (Thing t : rd.getStoryElementsInvolved().getElements()) {
				Mark.say(t.asString());
			}
		} */
	}
	/*//checks to see if the rule was inferred. 
	public void processFinalInferences(Object signal) {
		if (signal instanceof Thing) {
			Thing t = (Thing) signal;
			if (t.sequenceP()) {
				Sequence s = (Sequence) t;
				Mark.say("Inferences received:");
				for (Thing e : s.getElements()) {
					Mark.say(e.asString());
					LList<PairOfEntities> bindings = BasicMatcher.getBasicMatcher().match(test, e);

					if (bindings != null) {
						Mark.say("Match!\n", test.asString(), "\n", e.asString());
						
						// Mark.say("Bindings when matching entire rule:",
						// bindings);
						// Mark.say(test.getBundle());
						// Mark.say(e.getBundle());

					}
					else {
						// Mark.say("No Match!\n", test.asString(), "\n",
						// e.asString());
					}
				}
			}
		}
	}*/

	//gets coherence data and sends it across the wire;
	/**
	 * 
	 * @param signal
	 * processes coherence metric and sends them down the wire to the coherence module. 
	 */
	@SuppressWarnings("unused")
	public void processCoherence(Object signal) {
		Mark.say("Entering process coherence");
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			if (t.sequenceP()) {
				Sequence s = (Sequence) t;
				Mark.say("Story received:");
				int size = this.getTotalElements(s);
				
				Vector<Entity> uncausedEvents = getUncausedEvents(s);
				Vector<Vector<Entity>> chains = getCausalChains(uncausedEvents, s);
				
				double longestChain = 0.0;
			
				for (int i = 0; i< chains.size();i++){
					Vector<Entity> chain = chains.get(i); 
					if (chain.size()>longestChain){
						longestChain = chain.size();
					}
		
					for (Entity element:chain){

					}
					
				}
				double weightedLongestChain = longestChain/size;
				
				double weightedCausedEvent = (size - uncausedEvents.size())/(double)size;
				
				double weightedChains = chains.size()/(double)size;
				
				
			//	Connections.getPorts(this).transmit(e);
				double [] data = {longestChain, size- uncausedEvents.size(), chains.size()};
				
				
				Connections.getPorts(this).transmit(COHERENCE_DATA, data);

				//Connections.getPorts(this).transmit(COHERENCE_TEXT, CoherenceViewer.CLEAR);
				
				Connections.getPorts(this).transmit(COHERENCE_TEXT, "Story Title: " + StoryProcessor.getTitle(s));
				Connections.getPorts(this).transmit(COHERENCE_TEXT, "Number of chains: " + chains.size());
				Connections.getPorts(this).transmit(COHERENCE_TEXT, "Length of longest chain: " + longestChain);
				Connections.getPorts(this).transmit(COHERENCE_TEXT, "Number of caused event: " + (size- uncausedEvents.size()));
				
				
				/*for (Thing e : s.getElements()) {
					LList<PairOfEntities> bindings = BasicMatcher.getBasicMatcher().match(cause, e);

					if (bindings != null) {
						Mark.say("Match!\n", cause.asString(), "\n", e.asString());
						
						boolean result = StoryMethods.isInferred(e, s);
						Mark.say("Cause is inferred: " + result);
						Mark.say("Distance test for", e.asString());
							for (Thing u : s.getElements()) {
								Mark.say("Distance", StoryMethods.distance(e, u, s), u.asString());
							}
					}
					else {
						 Mark.say("No Match!\n", cause.asString(), "\n");
						// e.asString());
					}
					Mark.say(e.asString());

				}
			}*/
			}
		}
	}
	
	
	/**
	 * 
	 * @param uncausedEvents
	 * @param story
	 * @return A vector where each element is a vector representing causal chain of elements in the story
	 */
	
	@SuppressWarnings("unused")
	public Vector<Vector<Entity>> getCausalChains(Vector<Entity> uncausedEvents,
			Sequence story) {
		Vector<Vector<Entity>> chains = new Vector<Vector<Entity>>();
		for (Entity event : uncausedEvents) {
			Vector<Vector<Entity>> queue = new Vector<Vector<Entity>>();
			// Vector<Thing> extendedList = new Vector<Thing>();
			// Get started
			Vector<Entity> initialPath = new Vector<Entity>();
			initialPath.add(event);
			queue.add(initialPath);

			while (!queue.isEmpty()) {
				//print queue 
				for (Vector<Entity> pathprint: queue){
					
						
				}
				Vector<Entity> path = queue.firstElement();
				queue.remove(0);
				Entity lastElement = path.lastElement();

				// Find extensions of path and put them at the end for breadth
				// first
				// search

				// boolean to check if we find continuation of path in story.
				boolean seperate = true;
				for (Entity element : story.getElements()) {
					if (element.relationP(Markers.CAUSE_MARKER)) {
						Vector<Entity> antecedents = element.getSubject()
								.getElements();
						if (antecedents.contains(lastElement)) {
							if (element.getObject() != null) {
								Entity object = element.getObject();

								if (!path.contains(object)) {
									//Mark.say("addding object to path and then to queue");
									//Mark.say("object is: " + object.asString());
									seperate = false;
									Vector<Entity> newPath = new Vector<Entity>();
									newPath.addAll(path);
									newPath.add(element.getObject());
									queue.add(newPath);
								}
							}
						}
					}
				}

				// if no contiuation is added (seperate is true)
				if (seperate) {
					if (!chains.contains(path)){
					//Mark.say("adding path to chains");
					chains.add(path);
					}
				}

			}
		}

		return chains;
	}

	/**
	 * 
	 * @param story
	 * @return the uncaused events in the story.
	 */
	
	public Vector<Entity> getUncausedEvents(Sequence story){
		Vector<Entity> causedEvents = new Vector<Entity>();
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.CAUSE_MARKER)) {
				Entity antecedents = element.getObject();
				causedEvents.add(antecedents);
				}
			}
		
		Vector<Entity> uncausedEvents = new Vector<Entity>();
		
		for (Entity element : story.getElements()) {
			if (!element.relationP(Markers.CAUSE_MARKER)) {
				if (!causedEvents.contains(element))
					if (!uncausedEvents.contains(element)){
						uncausedEvents.add(element);
					}
				}
			}
		
		return uncausedEvents;
	}
	
	public int getTotalElements (Sequence story){
		int count = 0;
		for (Entity element : story.getElements()) {
			if (!element.relationP(Markers.CAUSE_MARKER)) {
				count+=1;
			}
		}
		return count;
	}
	
	
}
package subsystems.summarizer;

import generator.Generator;
import genesis.GenesisGetters;

import java.util.*;

import matchers.StandardMatcher;
import storyProcessor.*;
import utils.Html;
import utils.Mark;
import utils.tools.Predicates;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Apr 19, 2014
 * @author phw
 */

public class Persuader extends AbstractWiredBox {

	public static String COMMAND = "command";

	public static String TO_SECOND_PERSPECTIVE = "to second perspective";

	private static Persuader persuader;

	BetterSignal previousSignal;

	/*
	 * // SELECTION FIELDS. // TODO make these into buttons in GUI. private String granularity = "HIGH"; // can be
	 * "HIGH" or "LOW". private boolean subtractionMethod = true; private boolean additionMethod = false; private String
	 * selectionMode = "CHARACTER SPECIFIC"; // can be "CHARACTER SPECIFIC" or "RELATIVISTIC". private Map<String,
	 * ArrayList<String>> superConceptsMap = new HashMap<>(); // to be used when granularity is HIGH. private
	 * Map<String, ArrayList<String>> conceptualOpposites = new HashMap<>(); private ArrayList<String> evilList = new
	 * ArrayList<String>( Arrays.asList("Cruel", "Dishonest 1", "Dishonest 2", "Hateful", "Selfish", "Sly", "Violent",
	 * "Vicious")); private ArrayList<String> goodList = new ArrayList<String>( Arrays.asList("Caring", "Goodparent",
	 * "Nice", "Honest 1", "Generous", "Repentant", "Friendly")); // private ArrayList<String> unsympatheticList = new
	 * ArrayList<String>(Arrays.asList("Weak", "Harsh", "Cruel 1", // "Violent")); private ArrayList<String>
	 * unsympatheticList = new ArrayList<String>( Arrays.asList("Bad parent", "Badparent 3", "Bad husband",
	 * "Dishonest 1", "Dishonest 2", "Dishonest 3", "Dishonest 4", "Weak 1", "Weak 2", "Harsh", "Cruel 1", "Cruel 2",
	 * "Hateful", "Violent", "Vicious", "Scary", "Sly", "Unfamiliar")); // private ArrayList<String> sympatheticList =
	 * new ArrayList<String>(Arrays.asList("Resourceful", "Nice", "Caring", // "In a dilemma", "Sad")); private
	 * ArrayList<String> sympatheticList = new ArrayList<String>( Arrays.asList("Goodparent", "Caring", "Honest 1",
	 * "Honest 2", "Resourceful", "Nice", "Sad", "Friendly", "Repentant", "Generous", "Hardworking", "Unlucky"));
	 */

	private int counter = 0; // private Entity purpose = null;

	private String agent = null;

	private String quality = null;

	private Map<String, String> opposites = new HashMap<>();

	/*
	 * Getter for singleton class.
	 */

	public static Persuader getPersuader() {
		if (persuader == null) {
			persuader = new Persuader();
			// persuader.setGateKeeper(Switch.persuaderCheckBox);
		}
		return persuader;
	}

	private Persuader() {
		super("Persuader");
		/*
		 * superConceptsMap.put("evil", evilList); superConceptsMap.put("good", goodList);
		 * superConceptsMap.put("sympathetic", sympatheticList); superConceptsMap.put("unsympathetic",
		 * unsympatheticList); conceptualOpposites.put("good", new ArrayList<String>(Arrays.asList("evil")));
		 * conceptualOpposites.put("evil", new ArrayList<String>(Arrays.asList("good")));
		 * conceptualOpposites.put("sympathetic", new ArrayList<String>(Arrays.asList("unsympathetic")));
		 * conceptualOpposites.put("unsympathetic", new ArrayList<String>(Arrays.asList("sympathetic")));
		 */
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, "processSignal");
		Connections.getPorts(this).addSignalProcessor(COMMAND, "processInCommandThread");
	}

	/**
	 * Use this so that question can appear in text box while processing takes place.
	 */
	public void processInCommandThread(Object signal) {
		new ProcessCommandThread(signal).start();
	}

	private class ProcessCommandThread extends java.lang.Thread {
		Object signal;

		public ProcessCommandThread(Object signal) {
			this.signal = signal;
		}

		public void run() {
			process(signal);
		}

	}

	public void process(Object signal) {
		if (signal instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) signal;
			if (bs.get(0, String.class) == Markers.OPPOSITES_COMMAND) {
				String x = bs.get(1, String.class);
				String y = bs.get(2, String.class);
				Mark.say("Got opposites", x, y);
				opposites.put(x, y);
				opposites.put(y, x);
			}
			else if (bs.get(0, String.class) == Markers.EMPHASIZE_COMMAND && previousSignal != null) {
				String p = bs.get(1, String.class);
				String q = bs.get(2, String.class);
				Mark.say("Hello, I better do something with previous story, specifically show", p);
				Sequence complete = previousSignal.get(0, Sequence.class);
				Sequence explicit = previousSignal.get(1, Sequence.class);
				List<ConceptDescription> conceptDescriptions = previousSignal.get(4, ConceptAnalysis.class).getConceptDescriptions();
				agent = p;
				quality = q;
				String opposite = opposites.get(q);
				Mark.say("Noted opposite of", quality, "is", opposite);
				composeSummaryForPersuation(p, q, opposite, explicit, complete, conceptDescriptions);
			}
			if (bs.get(0, String.class) == Markers.EMPHASIZE_COMMAND && previousSignal != null) {
				// Obsolete
				// else if (bs.get(0, String.class) == CommandExpert.PERSUADE && previousSignal != null) {
				// Entity e = bs.get(1, Entity.class);
				// Mark.say("Bingo, I better do something with previous story, specifically show", e);
				//
				// purpose = e;
				//
				// String mode = previousSignal.get(0, String.class);
				// Sequence explicit = previousSignal.get(1, Sequence.class);
				// Sequence complete = previousSignal.get(2, Sequence.class);
				// List<ReflectionDescription> conceptDescriptions = previousSignal.get(3, List.class);
				// composeSummaryForPersuation(e, explicit, complete, conceptDescriptions);
				// }
			}
		}
	}

	public void processSignal(Object signal) {
		if (signal instanceof BetterSignal) {

			// Mark.say("Recording signal in persuader");

			BetterSignal bs = (BetterSignal) signal;
			if (bs.get(0, Object.class) instanceof Sequence) {
				previousSignal = bs;
			}
		}
	}

	/*
	 * Main event goes here.
	 */
	public void composeSummaryForPersuation(String actor, String quality, String opposite, Sequence explicitStorySequence, Sequence completeStorySequence, List<ConceptDescription> conceptDescriptions) {

		boolean debug = true;

		boolean debug2 = false;

		Mark.say(debug, "Quality:", quality, "and opposite is", opposite);

		ArrayList<String> conceptsToAvoid = new ArrayList<>();

		ArrayList<String> conceptsToKeep = new ArrayList<>();

		// Identifying concepts that need filtering out according to expressed goal in command

		// Print names of concepts in the second story processor
		// I'm not doing anything with this yet!
		Mark.say(debug, "Printing CONCEPTS KNOWN TO SECOND PERSPECTIVE:");
		for (Entity pattern : GenesisGetters.getMentalModel2().getStoryProcessor().getConceptPatterns().getElements()) {
			if (pattern.sequenceP()) {
				Mark.say(debug, "Concept", pattern.getType());
			}
		}

		StandardMatcher idMatcher = StandardMatcher.getIdentityMatcher();
		HashSet<Entity> feedersOfBadConcepts = new HashSet<Entity>(); // will hold story elements involved in
		                                                              // UNdesirable concepts
		                                                              // triggered by the main actor
		HashSet<Entity> feedersOfGoodConcepts = new HashSet<Entity>(); // will hold story elements involved in
		                                                               // desirable concepts
		                                                               // triggered by the main actor

		// The following loop identifies the story elements involved in
		// the concepts triggered by our main actor from the command, both the concepts that
		// we want to keep, AND the ones we want to eliminate from the telling.
		// the observation is that certain story elements might be involved in both a desirable and an undesirable
		// concept for the main actor.
		// e.g. If we want to make Macbeth be evil, we don't want to take out those elements that would show he's good
		// in the mean time.

		ArrayList<ConceptDescription> goodConcepts = new ArrayList<>();
		ArrayList<ConceptDescription> badConcepts = new ArrayList<>();

		for (ConceptDescription rd : conceptDescriptions) {
			String name = rd.getName().trim().toLowerCase();
			Bundle b = BundleGenerator.getBundle(name);
			Mark.say(debug, "Encountered", name, "with bundle", b);
			if (contains(b, quality)) {
				Mark.say(debug, name, "is a kind of", quality);
				goodConcepts.add(rd);
			}
			else if (opposite != null && contains(b, opposite)) {
				badConcepts.add(rd);
			}
		}

		Mark.say(debug, "Good", goodConcepts);
		Mark.say(debug, "Bad", badConcepts);

		ArrayList<ConceptDescription> tempGood = new ArrayList<>();
		ArrayList<ConceptDescription> tempBad = new ArrayList<>();

		for (ConceptDescription r : goodConcepts) {
			for (Entity e : r.getInstantiations().getElements()) {
				// if (e.relationP("trigger") && StandardMatcher.getIdentityMatcher().match(e.getSubject().getType(),
				// actor) != null) {
				if (e.relationP("trigger")) {
					if (e.getSubject().getType().equals(actor)) {
						tempGood.add(r);
					}
					else if (Switch.showContrastInPersuasion.isSelected()) {
						tempBad.add(r);
					}
				}
			}
		}

		for (ConceptDescription r : badConcepts) {
			for (Entity e : r.getInstantiations().getElements()) {
				if (e.relationP("trigger")) {

					if (e.getSubject().getType().equals(actor)) {
						tempBad.add(r);
					}
					else if (Switch.showContrastInPersuasion.isSelected()) {
						tempGood.add(r);
					}
				}
			}
		}

		goodConcepts = tempGood;
		badConcepts = tempBad;

		for (ConceptDescription r : goodConcepts) {
			Set<Entity> good = FeederFinder.getFeederFinder().findFeeders(r, completeStorySequence.getElements());
			Mark.say(debug, "Found", good.size(), "feeders for good concept", r);
			feedersOfGoodConcepts.addAll(good);
		}

		Set<Entity> removed = new HashSet<>();

		for (ConceptDescription r : badConcepts) {
			Set<Entity> bad = FeederFinder.getFeederFinder().findFeeders(r, completeStorySequence.getElements());
			Mark.say(debug, "Found", bad.size(), "feeders for bad concept", r);
			// At this point, want to knockout as much as possible, without knocking out a good feeder
			Set<Entity> badKeepers = new HashSet<>();
			badKeepers.addAll(bad);
			boolean done = false;
			// If any feeder already removed, quit
			for (Entity x : removed) {
				Mark.say(debug2, "Checking", x);
				if (badKeepers.contains(x)) {
					Mark.say(debug2, "No need to get rid of any of");
					for (Entity e : badKeepers) {
						Mark.say(debug2, ">>>  ", e);
					}
					Mark.say(debug2, "...in", r.getName(), "because already knocked out", x);
					done = true;
					break;
				}
			}
			if (done) {
				continue;
			}

			for (Entity x : bad) {
				// Revised so only get rid of one feeder
				if (badKeepers.size() > 1 && !feedersOfGoodConcepts.contains(x)) {
					badKeepers.remove(x);
					Mark.say(debug2, "Removed", x, " a feeder of", r.getName());
					break;
				}
				else if (badKeepers.size() > 1 && feedersOfGoodConcepts.contains(x)) {
					Mark.say(debug2, "Could not remove", x, " a feeder of", r.getName(), "because involved in good concept");
				}
				else {
					Mark.say(debug2, "Cannot remove", x, " a feeder of", r.getName(), "because it is last feeder standing");
				}
			}
			removed.addAll(badKeepers);
			feedersOfBadConcepts.addAll(badKeepers);
		}

		// Summarizer may have done stuff that needs to be cleared here

		for (Entity e : explicitStorySequence.getElements()) {
			e.removeProperty(Markers.MARKUP);
		}

		Mark.say(debug, "Good concepts", goodConcepts);
		for (Entity g : feedersOfGoodConcepts) {
			Mark.say(debug, ">>>", g);
			g.addProperty(Markers.MARKUP, Markers.HIGHLIGHT);
		}
		Mark.say(debug, "Bad concepts", badConcepts);
		for (Entity g : feedersOfBadConcepts) {
			Mark.say(debug, ">>>  Feeder", g);
			g.addProperty(Markers.MARKUP, Markers.STRIKE);
		}

		// Get back on track with Sila here

		HashSet<Entity> elementsToRetain = new HashSet<>(feedersOfGoodConcepts);
		HashSet<Entity> elementsToFilterOut = new HashSet<>(feedersOfBadConcepts);

		// This new sequence will be the edited story to send over to second perspective.
		Sequence editedForElaborationGraph = new Sequence();

		Sequence editedForRetelling = new Sequence(Markers.STORY_MARKER);

		Mark.say(debug, "EXPLICIT ELEMENTS");
		// Don't want to include inferences
		// Sequence expliciteStorySequenceMinusInferences = removeInferences(explicitStorySequence);

		Sequence expliciteStorySequenceMinusInferences = explicitStorySequence;

		for (Entity x : elementsToFilterOut) {
			Mark.say(debug, "Get rid of", x);
			if (Predicates.isCause(x)) {
				Mark.say(debug, "Get rid of CAUSE", x);
			}
		}

		for (Entity e : expliciteStorySequenceMinusInferences.getElements()) {
			// Mark.say(debug, "Explicit story element:", Predicates.isCause(e), e.asString());
			if (elementsToRetain.contains(e)) {
				editedForElaborationGraph.addElement(e);
			}
			else if (Predicates.isExplictCauseOrLeadsTo(e) && causeHasElementInSet(e, elementsToRetain)) {
				e.addProperty(Markers.MARKUP, Markers.HIGHLIGHT);
				Mark.say(debug, "THIS IS ALSO A GO BECAUSE IN CAUSAL RELATION: ", e.asString());
				editedForElaborationGraph.addElement(e);
			}

			else if (elementsToFilterOut.contains(e)) {
				// this explicit story element is among those we decided we don't want to include so as to achieve
				// persuasion goal
				e.addProperty(Markers.MARKUP, Markers.STRIKE);
				Mark.say(debug, "THIS IS A NO GO: ", e.asString());

			}
			// Don't want to include explicit causes that involve stuff to be taken out
			else if (Predicates.isExplictCauseOrLeadsTo(e) && causeHasElementInSet(e, elementsToFilterOut)) {
				e.addProperty(Markers.MARKUP, Markers.STRIKE);
				Mark.say(debug, "THIS IS ALSO NO GO BECAUSE IN CAUSAL RELATION: ", e.asString());
			}
			// Not sure if this really works right, so diked by phw
			else if (false && Predicates.isMeans(e)) {
				e = e.getObject();
				e.addProperty(Markers.MARKUP, Markers.GRAY);
				editedForElaborationGraph.addElement(e);
			}
			else {
				editedForElaborationGraph.addElement(e);
			}
			editedForRetelling.addElement(e);
		}

		// ///Below was an attempt to use matching instead of "contains" but this didn't match correctly, either.

		// StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		// for (Entity e: explicitStorySequence.getAllComponents()){
		// Mark.say(debug, "Explicit story element:", e.asString());
		// int matchCount = 0;
		// for (Entity f: elementsToFilterOut){
		// if (matcher.match(e,f)!=null){
		// Mark.say ("Found a match! :");
		// Mark.say(debug, e.asString());
		// Mark.say(debug, f.asString());
		// matchCount++;
		// Mark.say(debug, "Incrementing matchCount: ", matchCount);
		//
		// }
		// } if (matchCount==0){
		// Mark.say(debug, "NO MATCHES WITH UNWANTED.Added to edit: ", e.asString());
		// editedStory.addElement(e);
		//
		// }
		// }
		// Mark.say(debug, "FINAL EDITED VERSION!!!!!!!!!!!!!!!!!");
		// for (Entity n: editedStory.getAllComponents()){
		// Mark.say(debug, n.asString());
		// }

		// TRANSMITTING EDITED STORY TO THE SECOND PERSPECTIVE
		Mark.say(debug, "Transmitting story with", editedForElaborationGraph.getElements().size(), "elements", "vs", explicitStorySequence
		        .getElements().size());
		// Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, elementsRetained);

		Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, editedForElaborationGraph);
		// Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, explicitStorySequence);

		tell(editedForRetelling);

	}

	/**
	 * Quick hack to see if a bundle contains a class
	 */
	private boolean contains(Bundle b, String strippedGoalCommand) {
		if (strippedGoalCommand == null || strippedGoalCommand.isEmpty()) {
			return false;
		}
		for (Vector<String> t : b) {
			for (String s : t) {
				if (strippedGoalCommand.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	private HashSet<Entity> entityIntersection(HashSet<Entity> setA, HashSet<Entity> setB) {
		HashSet<Entity> intersection = new HashSet<Entity>();

		for (Entity x : setB) {
			if (setA.contains(x)) {
				intersection.add(x);
			}
		}
		return intersection;
	}

	private boolean causeHasElementInSet(Entity e, HashSet<Entity> set) {
		if (!Predicates.isCause(e)) {
			return false;
		}
		else if (set.contains(e.getObject())) {
			return true;
		}
		for (Entity a : e.getSubject().getElements()) {
			if (set.contains(a)) {
				return true;
			}
		}
		return false;
	}

	private Sequence removeInferences(Sequence explicitStorySequence) {
		Sequence result = new Sequence();
		for (Entity e : explicitStorySequence.getElements()) {
			if (!Predicates.isInference(e)) {
				result.addElement(e);
			}
			else {
				Mark.say("Not including inference", e, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
		return result;
	}

	/**
	 * This method identifies the concepts to filter out when the persuasion goal involves "Super concepts". e.g.
	 * "Sympathetic" is a super concept, which is a wrapper around regular concepts like "generous", or "honest".
	 * 
	 * @param conceptCommand
	 * @author Sila Sayan
	 */
	private ArrayList<String> identifySuperConceptsToFilterOut(String goalSuperConcept, Map<String, ArrayList<String>> superConceptsMap, Map<String, ArrayList<String>> conceptualOpposites) {
		Mark.say("The goal concept is : ", goalSuperConcept);
		ArrayList<String> superConceptsToFilterOut = conceptualOpposites.get(goalSuperConcept); // identify the opposite
		                                                                                        // super concepts. e.g.
		                                                                                        // sympathetic vs
		                                                                                        // unsympathetic.
		ArrayList<String> associatedConceptsToFilterOut = new ArrayList<String>();
		for (String sc : superConceptsToFilterOut) {
			// identify regular concepts that the super concepts includes
			for (String c : superConceptsMap.get(sc)) {
				Mark.say("Super concept ", sc, "has as part: ", c);
				associatedConceptsToFilterOut.add(c);
			}
		}
		return associatedConceptsToFilterOut;
	}

	/**
	 * This method does a simple look up to identify the regular concepts associated with a super concept. e.g.
	 * Unsypmathetic --> weak, dishonest, harsh, sly...
	 * 
	 * @author sila
	 * @return list of regular concepts that must be retained
	 */
	private ArrayList<String> identifySuperConceptsToRetain(String goalSuperConcept, Map<String, ArrayList<String>> superConceptsMap) {
		return superConceptsMap.get(goalSuperConcept);
	}

	/**
	 * Undesirable and desirable elements may share some story elements. e.g. A character may be a bad parent AND a good
	 * parent in the same story. In both cases, we need the story element that declares the character is a parent. We
	 * identify these to ensure they're not filtered out of the story.
	 * 
	 * @author Sila
	 * @return
	 */
	private HashSet<Entity> identifyStoryElementsToRetain(HashSet<Entity> elementsInDesirableConcepts, HashSet<Entity> elementsInUndesirableConcepts) {
		Mark.say("TIME TO CHECK ELEMENTS TO RETAIN");
		for (Entity e : elementsInDesirableConcepts) {
			Mark.say("Desirable: ", e.asString());
		}
		HashSet<Entity> elementsToRetain = elementsInDesirableConcepts;
		elementsToRetain.retainAll(elementsInUndesirableConcepts); // takes the difference of two sets: desirable and
		                                                           // undesirable element sets.
		                                                           // leaves only those elements that belong *uniquely*
		                                                           // to the set of undesirable elements.
		for (Entity e : elementsToRetain) {
			Mark.say("These must be kept eventho in BAD concept:", e.asString());
		}

		return elementsToRetain;
	}

	/**
	 * This method identifies the concepts to filter out when the persuasion goal involves regular concepts. e.g
	 * Sympathetic is a super concept versus generous, which is a regular concept.
	 * 
	 * @param conceptCommand
	 * @return
	 * @author Sila Sayan
	 */
	// private ArrayList<String> identifyConceptsToFilterOut(Entity conceptCommand){
	//
	// }

	private String formatConcept(String rawConcept) {
		String[] parts = rawConcept.split("-");
		String justConcept = parts[0];
		// Mark.say("string before replace: ", justConcept);
		String strippedConcept = justConcept.replaceAll("_", " ");
		// Mark.say("replace result: ", strippedConcept);
		return strippedConcept;
	}

	/**
	 * This is an example that just picks out the biggest concepts.
	 */
	protected ArrayList<ConceptDescription> limitToRelevantConcepts(List<ConceptDescription> conceptDescriptions) {
		Mark.say("IN WINSTON'S FUNCTION!!!!!!!!");
		ArrayList<ConceptDescription> relevantConcepts = new ArrayList<>();
		int maxSize = 0;
		for (ConceptDescription candidate : conceptDescriptions) {
			Mark.say("Reflection CANDIDATE: ", candidate.getName());
			for (Entity elt : candidate.getStoryElementsInvolved().getElements()) {
				Mark.say("Story elt involved: ", elt.asString());
			}
			// int size = numberOfElements(candidate);
			int size = 0;
			// Biggest by number of story elements involved
			size = candidate.getStoryElementsInvolved().getElements().size();
			// If same as biggest so far, keep it
			if (size == maxSize) {
				relevantConcepts.add(candidate);
			}
			// If bigger than biggest so far, keep only it
			else if (size > maxSize) {
				maxSize = size;
				relevantConcepts.clear();
				relevantConcepts.add(candidate);
			}
			// Else ignore
			else {
			}
		}
		return relevantConcepts;
	}

	private void tell(Sequence explicitStorySequence) {

		boolean debug = false;

		Mark.say(debug, "IN TELL!!!!!!!!");
		// // for (Entity elt : relevantElements) {
		// // Mark.say("Relevant element: ", elt.asString());
		// // }
		//
		// // Extract entities
		// Set<Entity> entities = getEntities(relevantElements);
		// Set<Entity> roleElements = new HashSet<Entity>();
		// if (Switch.includeAgentRolesInSummary.isSelected()) {
		// roleElements = extractRoleElements(completeStorySequence, entities);
		// }
		//
		// // Gets rid of detail
		// if (Switch.meansProcessing.isSelected()) {
		// relevantElements = filterUsingMeansSuppression(relevantElements);
		// }
		//
		// if (Switch.abductionProcessing.isSelected()) {
		// relevantElements = filterUsingAbductionSuppression(relevantElements);
		// }
		//
		// // Gets rid of elements embedded in causes
		// relevantElements = removeIfInCause(relevantElements);
		//
		// Sequence elementsRetained = filterUsingRelevantElements(completeStorySequence, relevantElements,
		// roleElements);
		//
		// // Smooths langauge
		// if (Switch.postHocProcessing.isSelected()) {
		// elementsRetained = filterUsingPostHocErgoPropterHoc(elementsRetained);
		// }
		//
		// String name = completeStorySequence.getType();
		//
		// int originalSize = storySize(explicitStorySequence);
		//
		// int summarySize = storySize(elementsRetained);
		//
		// // ATTENTION HERE TO TRANSMIT OVER!!!!
		// // Mark.say("Transmitting story with", elementsRetained.getElements().size(), "elements");
		// // // Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, elementsRetained);
		// // Connections.getPorts(this).transmit(TO_SECOND_PERSPECTIVE, explicitStorySequence);

		// Want to deal with purpose.

		String title = Html.h2("A story about " + quality);

		// revisedStory += Html.normal(composeStoryEnglishFromEntities(elementsRetained));

		String text = Html.bold(Html.blue("This is a story that demonstrates that " + agent + " is " + quality + ".  "));

		text += composeStoryEnglishFromEntities(explicitStorySequence);

		String revisedStory = title + Html.normal(text);

		// Mark.say("Sizes", originalSize, summarySize, revisedStory.split("\\.").length);

		// Do something else here; broadcast to gui, for example.

		// Selects summary based on all observed concepts
		Mark.say(debug, "Story produced by persuader:");
		for (String s : revisedStory.split("\\.")) {
			Mark.say(debug, "\n", s.trim());
		}

		Connections.getPorts(this).transmit(Summarizer.REPORT_OUTPUT, new BetterSignal("Version " + ++counter, revisedStory));

	}

	/**
	 * Differes from method in Summarizer because do not want to automatically strike means
	 */

	protected String composeStoryEnglishFromEntities(Sequence story) {
		Mark.say("Composing");
		String english = "";
		for (Entity entity : story.getElements()) {
			String sentence = Generator.getGenerator().generate(entity);
			if (sentence != null) {
				if (Switch.showMarkup.isSelected()) {
					if (entity.getProperty(Markers.MARKUP) == Markers.STRIKE) {
						// Mark.say("Noted markup", sentence);
						sentence = Html.strike(Html.red(sentence));
					}
					else if (entity.getProperty(Markers.MARKUP) == Markers.HIGHLIGHT) {
						// Mark.say("Noted markup", sentence);
						sentence = Html.bold(Html.green(sentence));
					}
					else if (entity.getProperty(Markers.MARKUP) == Markers.GRAY) {
						sentence = Html.gray(sentence);
					}
				}
				english += sentence.trim() + "  ";
			}
			else {
				Mark.err("Unexpected null sentence in Summarizer");
			}
		}
		return english;
	}
}

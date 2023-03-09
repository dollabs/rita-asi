package translator;

import java.util.*;
import javax.swing.JFrame;


import connections.AbstractWiredBox;
import constants.Markers;
import constants.Switch;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.Generator;
import generator.ISpeak;
import generator.RoleFrames;
import generator.TransitionFrames;
import start.Start;
import storyProcessor.StoryProcessor;

import utils.Mark;
import utils.tools.JFactory;
import utils.tools.Predicates;

/*
 * Created on Nov 24, 2007 @author phw
 */

/*
 * Edited on 15 August 2013 by ahd
 */

public class NewRuleSet extends AbstractWiredBox {

	private static List<String> getTimeWords() {
		return timeWords;
	}

	protected ArrayList<Rule> ruleSet;

	public static List<String> assertionWords = Arrays.asList("know", "see", "show", "demonstrate");

	public static List<String> frequencyWords = Arrays.asList("sometimes", "often", "occasionally");

	public static List<String> pathPrepositions = Arrays
	        .asList("to_the_left_of", "to_the_right_of", "across", "from", "to", "into", "under", "toward", "towards", "via", "behind", "between", "past", "by", "over", "above", "down", "up", "below", "on", "in", "off");

	public static List<String> locationPrepositions = Arrays
	        .asList("to_the_left_of", "to_the_right_of",

	                "next_to", "close to", "far away from",

	                "around", "over", "under", "behind", "between", "by", "above", "down", "up", "below", "on", "in", "near");

	public static List<String> placePrepositions = Arrays
	        .asList("at", "side", "top", "bottom", "left", "right", "inside", "front", "back", "on", "next_to");

	public static List<String> directionWords = Arrays.asList("left", "right");

	public static List<String> trajectoryWords = Arrays
	        .asList("trajectory", "travel", "go", "come", "leave", "arrive", "move", "roll", "enter", "exit");

	protected static List<String> causeToMoveWords = Arrays.asList("propel", "push", "roll", "move");

	protected static List<String> pathWords = Arrays.asList("path", "location");

	public static List<String> transitionWords = Arrays.asList("occur", "happen", "appear", "disappear", "change", "increase", "decrease");

	protected static List<String> timeWords = Arrays.asList("before", "after", "while");

	protected static List<String> newTimeWords = Arrays.asList("before_connector", "after_connector", "while_connector");

	protected static List<String> timeMarkerWords = Arrays.asList("then", "later", "afterward", "next");

	protected static List<String> requireWords = Arrays.asList("force", "ask");

	protected static List<String> goalWords = Arrays.asList("want", "try");

	protected static List<String> transferWords = Arrays.asList("give", "take", "throw", "pass", "receive");

	protected static List<String> persuadeWords = Arrays.asList("persuade", "ask");

	protected static List<String> forceWords = Arrays.asList("force", "coerce");

	protected static List<String> thinkWords = Arrays.asList("think", "believe", "conclude", "know");

	protected static List<String> roleWords = Arrays
	        .asList("of", "by", "as", "with", "for", "at", "about", "in", "against", "whether", Markers.MANNER_MARKER);

	protected static List<String> positiveWords = Arrays.asList("happy", "calm", "pp", "mental-state");

	protected static List<String> negativeWords = Arrays.asList("sad", "unhappy", "distraught", "nn");

	/**
	 * Amusement<br>
	 * Anger<br>
	 * Contempt<br>
	 * Contentment<br>
	 * Disgust<br>
	 * Embarrassment<br>
	 * Excitement<br>
	 * Fear<br>
	 * Guilt<br>
	 * Happiness<br>
	 * Pleasure<br>
	 * Pride<br>
	 * Relief<br>
	 * Sadness<br>
	 * Satisfaction<br>
	 * Shame<br>
	 * Surprise
	 */

	protected static List<String> mentalStateWords = new ArrayList<String>();

	public static List<String> jobWords = Arrays.asList("ruler", "legislator", "administrator", "leader", "professional", "judge");

	protected static List<String> repetitionAllowedWords = Arrays.asList("add");

	protected Entity root = new Entity("root");

	public NewRuleSet() {
		super("New rule set");
		mentalStateWords.addAll(positiveWords);
		mentalStateWords.addAll(negativeWords);
		makeRuleSet();
	}

	private void addRule(BasicRule rule) {
		getRuleSet().add(new Rule(rule));
	}

	protected ArrayList<Rule> getRuleSet() {
		if (ruleSet == null) {
			ruleSet = new ArrayList<Rule>();
		}
		return ruleSet;
	}

	/*
	 * Processes "Did contact appear after the bird flew"
	 */
	class ProcessDoTimeQuestion extends BasicRule {
		ArrayList firstWords = new ArrayList();

		ArrayList secondWords = new ArrayList();

		public ProcessDoTimeQuestion() {
			firstWords.addAll(NewRuleSet.getTimeWords());
			firstWords.add("because");
			secondWords.add("do");
			secondWords.add("have");
		}

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed(firstWords) && this.firstLinkObject.isAPrimed(secondWords)) {
				Function question = new Function("question", firstLinkSubject);
				question.addType("do");
				replace(firstLink, new Relation("link", root, question));
				succeeded();
			}
			else if (this.firstLinkObject.isAPrimed(firstWords) && this.firstLinkSubject.isAPrimed(secondWords)) {
				Function question = new Function("question", firstLinkObject);
				question.addType("do");
				replace(firstLink, new Relation("link", root, question));
				succeeded();
			}
		}
	}

	/*
	 * Processes "Is the bird above the tree"
	 */
	// class ProcessLocationQuestion extends BasicRule2 {
	// public void run() {
	// super.run();
	// if (this.firstLinkSubject.isAPrimed("entity") && this.firstLinkObject.isAPrimed("be")) {
	// if (!firstLinkObject.isAPrimed(pathPrepositions)) {
	// if (secondLinkSubject == firstLinkSubject && this.secondLinkObject.isAPrimed(pathPrepositions)) {
	// Sequence locationCollection = JFactory.createLocation();
	// Relation location = new Relation(firstLinkSubject, locationCollection);
	// Function question = new Function("Location question", location);
	// location.addType("question");
	// location.addType("state");
	// replace(firstLinkSubject, location);
	// replace(this.firstLink, new Relation("link", root, question));
	// succeeded();
	// }
	// }
	// }
	// }
	// }

	private Function getNewDerivative(String type, Entity t) {
		Thread thread = new Thread();
		thread.add(type);
		Function result = new Function(t);
		result.replacePrimedThread(thread);
		return result;
	}

	public static List getPathPrepositions() {
		return pathPrepositions;
	}

	// /////////////////////////// New versions below

	private boolean isA(Entity t) {
		return "is-a".equals(t.getType()) || "is".equals(t.getType());
	}

	/*
	 * Processes A causes B, leaving only one semantic structure
	 */
	class KillRedundantRoots extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLinkSubject == root) {
				if (this.secondLinkSubject == root) {
					if (firstInsideSecond(this.firstLinkObject, this.secondLinkObject)) {
						remove(firstLink);
						succeeded();
					}
					else if (firstInsideSecond(this.secondLinkObject, this.firstLinkObject)) {
						remove(secondLink);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "a bird"
	 */
	// class AbsorbDeterminer extends BasicRule {
	// public void run() {
	// super.run();
	// if (getFirstLink().getObject().isA("part-of-speech-dt")) {
	// if (firstLinkObject.isAPrimed("the")) {
	// firstLinkSubject.addFeature("definite");
	// }
	// else if (firstLinkObject.isAPrimed("a") ||
	// firstLinkObject.isAPrimed("an")) {
	// firstLinkSubject.addFeature("indefinite");
	// }
	// remove(getFirstLink());
	// succeeded();
	// }
	// }
	// }

	/*
	 * This is the end of the story.
	 */
	class EndOfStoryIdiom extends BasicRule {
		public void run() {
			super.run();
			if (firstLinkSubject.isA("end")) {
				if (firstLinkObject.isAPrimed("is")) {
					if (firstLinkSubject.functionP()) {
						Function derivative = (Function) firstLinkSubject;
						if (derivative.getSubject().isAPrimed("story")) {
							replace(firstLink, new Relation("link", root, firstLinkSubject));
							succeeded();
						}
					}
				}
			}
		}
	}

	/*
	 * Processes "red bird"
	 */
	class AbsorbeAdjective extends BasicRule {

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("entity") && this.firstLink.isA("adjectival-modifier")) {
				this.firstLinkSubject.addFeature(this.firstLinkObject.getType());
				remove(this.firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "a bird can fly"
	 */
	class AbsorbAuxiliary extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("action") && this.firstLink.isA("auxiliary")) {
				if (firstLinkObject.isAPrimed("do")) {
					return;
				}
				if (firstLinkObject.isAPrimed("to")) {
					return;
				}
				this.firstLinkSubject.addFeature(this.firstLinkObject.getType());
				remove(this.firstLink);
				succeeded();
				// System.out.println("Rule 1 succeded in absorbing
				// determiner for " +
				// getFirstLink().getSubject().getType());
			}
		}
	}

	/*
	 * Processes "a bird can fly"
	 */
	class AbsorbNegation extends BasicRule {

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("action") && this.firstLink.isA("negation-modifier")) {
				this.firstLinkSubject.addFeature(this.firstLinkObject.getType());
				remove(this.firstLink);
				succeeded();
				// System.out.println("Rule 1 succeded in absorbing
				// determiner for " +
				// getFirstLink().getSubject().getType());
			}
		}
	}

	/*
	 * Proecesses "the top of the tree"
	 */
	class ProcessRegion extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLinkSubject.isA("location") && firstLinkObject.isAPrimed("of")) {
				if (secondLinkSubject == firstLinkObject && secondLinkObject.isAPrimed("entity")) {
					Function place = new Function("place", secondLinkObject);
					remove(firstLink);
					remove(secondLink);
					transferTypes(this.firstLinkSubject, place);
					replace(firstLinkSubject, place);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "bird is under the table"
	 */
	// class ProcessLocation extends BasicRule {
	// public void run() {
	// super.run();
	// if (this.firstLinkSubject.isAPrimed("be") && firstLinkObject.isAPrimed("entity") &&
	// !firstLinkSubject.isAPrimed(transitionWords)) {
	// Sequence locationCollection = JFactory.createLocation();
	// Relation location = new Relation("state", firstLinkObject, locationCollection);
	// replace(firstLinkSubject, location);
	// replace(firstLink, new Relation("link", root, location));
	// succeeded();
	//
	// }
	// }
	// }

	/*
	 * Processes "A bouvier is a dog"
	 */
	// class ProcessClassification extends BasicRule2 {
	// public void run() {
	// super.run();
	// if (this.firstLinkSubject.isAPrimed("entity")) {
	// if (this.secondLinkSubject == this.firstLinkSubject && secondLinkObject.isAPrimed("is")) {
	// Thread thread = firstLinkObject.getThread(Entity.MARKER_FEATURE);
	// Relation classification;
	// if (thread == null) {
	// classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
	// }
	// else if (thread.contains("indefinite")) {
	// if (firstLinkObject.isAPrimed("unknownWord")) {
	// classification = new Relation(Markers.THREAD_TYPE, firstLinkSubject, firstLinkObject);
	// }
	// else {
	// classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
	// }
	// }
	// else if (thread.contains("definite")) {
	// classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
	// }
	// else {
	// classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
	// }
	// String type = firstLinkObject.getType();
	// // transferTypes(this.firstLinkSubject,
	// // this.firstLinkObject);
	// // firstLinkObject.addThread(firstLinkSubject.getPrimedThread().copyThread());
	// // firstLinkObject.addType(type);
	// replace(this.firstLink, new Relation("link", root, classification));
	// remove(this.secondLink);
	// succeeded();
	//
	// }
	// }
	// }
	// }

	// **********************************************************************************
	// New Rules for Start
	// **********************************************************************************

	private boolean inclusion(Entity secondLink, Entity firstLink) {
		if (firstLink.isA("is_pp") || secondLink.isA("is_pp")) {
			return false;
		}
		else if (secondLink == firstLink) {
			return true;
		}
		// else if (BasicMatcher.getBasicMatcher().match(secondLink, firstLink) != null) {
		// return true;
		// }
		else if (firstLink.entityP()) {
			return false;
		}
		else if (firstLink.functionP()) {
			return inclusion(secondLink, ((Function) firstLink).getSubject());
		}
		else if (firstLink.relationP()) {
			Relation r = (Relation) firstLink;
			return inclusion(secondLink, r.getSubject()) || inclusion(secondLink, r.getObject());
		}
		else if (firstLink.sequenceP()) {
			Sequence s = (Sequence) firstLink;
			for (Entity element : s.getElements()) {
				if (inclusion(secondLink, element)) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Processes "A BIRD FLEW"
	 */

	// class PathExpert extends BasicRule {
	// public void run() {
	// super.run();
	// if (firstLink.isA(trajectoryWords) && firstLinkSubject.isA("thing")) {
	// Sequence path = JFactory.createPath();
	// if (firstLinkObject == Markers.NULL) {
	// Relation move = new Relation("action", firstLinkSubject, path);
	// Bundle b = firstLink.getBundle();
	// b = b.filterFor("action");
	// // b = b.filterFor(trajectoryWords);
	// b = b.filterForNot("entity");
	// if (b.isEmpty()) {
	// return;
	// }
	// move.setBundle(b);
	// // Bad idea, screwed up disambiguator
	// // addTypeAfterReference("action", "trajectory", move);
	// replace(firstLink, move);
	// succeeded();
	// }
	// }
	// }
	// }

	/*
	 * Processes "A BIRD WALKED LEFT"
	 */
	class DirectionExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isA(trajectoryWords) && firstLinkSubject.isA("thing")) {
				if (firstLinkObject.isA(directionWords)) {
					Sequence path = JFactory.createPath();
					Relation move = new Relation("action", firstLinkSubject, path);
					Bundle b = firstLink.getBundle();
					b = b.filterFor("action");
					b = b.filterFor(trajectoryWords);
					b = b.filterForNot("entity");
					move.setBundle(b);
					addTypeAfterReference("action", "trajectory", move);
					Function at = new Function("at", firstLinkObject);
					Function toward = new Function(Markers.PATH_ELEMENT_MARKER, at);
					toward.addType("toward");
					path.addElement(toward);
					replace(firstLink, move);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "A BIRD FLEW"
	 */
	class LocationExpert extends BasicRule {

		public void run() {
			super.run();
			if (firstLink.isA("be") && firstLinkSubject.isA("thing") && !firstLink.isA(transitionWords)) {
				if (firstLink.isNotA("enter")) {
					Sequence path = new Sequence("location");
					if (firstLinkObject == Markers.NULL) {
						Relation place = new Relation("place", firstLinkSubject, path);
						replace(firstLink, place);
						succeeded();
					}
				}
			}
		}
	}

	class AtExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.AT)) {
				Relation place = (Relation) firstLinkSubject;
				Function at = new Function(Markers.AT, firstLinkObject);
				place.getObject().addElement(at);
				replace(firstLink, place);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A bird flew FROM A HOUSE TO A TREE"
	 */
	class PathElementExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(pathPrepositions) && firstLinkObject.isA("thing")) {
				if (firstLinkSubject.isAPrimed(trajectoryWords) && firstLinkSubject.relationP()) {
					Relation r = (Relation) firstLinkSubject;
					if (r.getObject().isAPrimed(pathWords) && r.getObject().sequenceP()) {
						Sequence path = (Sequence) (r.getObject());
						Entity place;
						if (firstLinkObject.isAPrimed(placePrepositions) && !firstLinkObject.isAPrimed(directionWords)) {
							place = firstLinkObject;
						}
						else {
							place = new Function("at", firstLinkObject);
						}
						Function pathFunction = new Function(Markers.PATH_ELEMENT_MARKER, place);
						pathFunction.addType(firstLink.getType());
						path.addElement(pathFunction);
						remove(firstLink);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flew FROM A HOUSE TO A TREE"
	 */
	class LocationElementExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(locationPrepositions) && firstLinkObject.isA("thing")) {
				if (firstLinkSubject.relationP("place")) {
					Relation r = (Relation) firstLinkSubject;
					if (r.getObject().isAPrimed(pathWords) && r.getObject().sequenceP()) {
						Sequence path = (Sequence) (r.getObject());
						Entity place;
						if (firstLinkObject.isAPrimed(placePrepositions)) {
							place = firstLinkObject;
						}
						else {
							place = new Function("at", firstLinkObject);
						}
						Function pathFunction = new Function(Markers.PATH_ELEMENT_MARKER, place);
						pathFunction.addType(firstLink.getType());
						path.addElement(pathFunction);
						remove(firstLink);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flew from a house to THE TOP OF A TREE"
	 */
	class RelatedToExpert extends BasicRule {

		public RelatedToExpert() {
		}

		public void run() {
			super.run();
			if (firstLink.isAPrimed("related-to")) {
				if (firstLinkObject.isAPrimed("entity") && firstLinkSubject.isAPrimed("region")) {
					Function place = new Function("dummy", firstLinkObject);
					Bundle b = firstLinkSubject.getBundle();
					b = b.filterFor("region");
					place.setBundle(b);
					addTypeAfterReference("region", Markers.PLACE_MARKER, place);
					replace(firstLinkSubject, place);
					remove(firstLink);
					succeeded();
				}
				// else {
				// firstLinkSubject.addProperty(Markers.RELATED_TO, firstLinkObject);
				// remove(firstLink);
				// succeeded();
				// }
			}
		}
	}

	/*
	 * Processes "A bird appeared"
	 */
	class TransitionExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && transitionWords.contains(firstLink.getType()) && firstLinkSubject.isAPrimed("entity")
			        && firstLinkObject == Markers.NULL) {
				// Create derivative
				Function transition = new Function(Markers.ACTION_MARKER, firstLinkSubject);
				transition.addType(Markers.TRANSITION_MARKER);
				transition.addType(firstLink.getType());
				if (firstLink.getTypes().contains("occur") || firstLink.getTypes().contains("happen")) {
					transition.addType(Markers.APPEAR_MARKER);
				}
				replace(firstLink, transition);
				succeeded();
			}
		}
	}

	/*
	 * Processes "An event occurred"
	 */
	class EventExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.functionP() && transitionWords.contains(firstLink.getType())) {
				if (firstLinkSubject.isAPrimed("action")) {
					replace(firstLink, firstLinkSubject);
					succeeded();
				}
			}
		}
	}

	private boolean isMentalState(Entity e) {
		// Mark.say("Checking", e);
		if (e.isA("mental_state")) {
			// Mark.say("Mental state noted!", e);
			return true;
		}
		return e.isAPrimed(mentalStateWords);
	}

	/*
	 * Processes "Macbeth became king"
	 */
	class BecomeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed(Markers.BECOME)) {
				Relation relation;
				if (isMentalState(firstLinkObject) || firstLinkObject.isAPrimed("emotion")) {

					Entity quality = new Entity("mental-state");
					if (isMentalState(firstLinkObject)) {
						quality.addType(firstLinkObject.getType());
					}
					// else if (firstLinkObject.isAPrimed(positiveWords)) {
					// quality.addType(firstLinkObject.getType());
					// // quality.addType(Markers.POSITIVE);
					// }
					// else if (firstLinkObject.isAPrimed(negativeWords)) {
					// // quality.addType(Markers.NEGATIVE);
					// quality.addType(firstLinkObject.getType());
					// }
					relation = new Relation(Markers.MENTAL_STATE_MARKER, firstLinkSubject, quality);
				}
				else {
					relation = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkObject, firstLinkSubject);
				}
				// Create derivative
				Function transition = TransitionFrames.makeAppear(relation);
				replace(firstLink, transition);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A bird is NOT happy"
	 */
	class NegationExpert extends BasicRule {
		public void run() {
			super.run();
			// New
			if (firstLink.relationP() && firstLink.isAPrimed("is_negative") && firstLinkObject.isAPrimed(Markers.YES)) {
				// Mark.say("Inserting not A", firstLink.asString());
				// Mark.say("Inserting not B", firstLinkSubject.asString());
				firstLinkSubject.addFeature(Markers.NOT);
				// Mark.say("Inserting not C", firstLinkSubject.asString());
				remove(firstLink);
				succeeded();
			}
		}
	}

	class SometimesTrap extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed(Markers.HAS_MODIFIER)) {
				if (firstLinkObject.isA(Markers.SOMETIMES)) {
					firstLinkSubject.addProperty(Markers.IDIOM, Markers.SOMETIMES);
					Relation property = new Relation(Markers.PROPERTY_TYPE, firstLinkSubject, firstLinkObject);
					replace(firstLink, property);
					succeeded();
				}
			}
		}
	}

	class IntensifierExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP(Markers.HAS_INTENSIFIER)) {
				// Mark.say("Got intensifier on", firstLink);
				firstLinkSubject.addFeature(firstLinkObject.getType());
				remove(firstLink);
				succeeded();
			}
		}
	}

	class WouldLikeExpert extends BasicRule2 {
		public void run() {
			super.run();
			Entity firstLinkRole = StoryProcessor.extractObjectRole(firstLink);
			if (secondLink == firstLinkRole && firstLinkRole != null && firstLink.isA(Markers.LIKE_WORD)) {
				Relation relation = new Relation(Markers.ACTION_WORD, firstLinkSubject, firstLinkObject);
				relation.addType(Markers.GOAL_MARKER);
				relation.addType(Markers.DESIRE_MARKER);
				relation.addType(Markers.WANT_MARKER);
				relation.addProperty(Markers.PROCESSED, true);

				remove(secondLink);
				replace(firstLink, relation);

				succeeded();
			}

		}
	}

	/*
	 * Deletes mode and modifier relation
	 */
	class ModeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed("has_modal")) {
				firstLinkSubject.addProperty(Markers.MODAL, firstLinkObject.getType());
				if (firstLinkSubject.isA(Markers.LIKE_WORD)) {
					remove(firstLink);
					succeeded();
				}
//				else if (firstLinkObject.isAPrimed(Markers.WILL_WORD)){
//					succeeded();
//				}
				else {
					if (firstLinkObject.isAPrimed(Markers.MAY_WORD)) {
						// Handled by modal marker
						firstLinkSubject.addProperty(Markers.CERTAINTY, Markers.TENTATIVE);
					}
					else if (firstLinkObject.isAPrimed(Markers.PRESUMPTION_WORD)) {
						firstLinkSubject.addProperty(Markers.CERTAINTY, Markers.PRESUMPTION_RULE);
					}
					else if (firstLinkObject.isAPrimed(Markers.MUST_WORD)) {
						firstLinkSubject.addProperty(Markers.IMPERATIVE, true);
					}
//					// "happen" is a special case used in questions: What would happen if...
					else if (firstLinkObject.isAPrimed(Markers.WILL_WORD) && !firstLinkSubject.isAPrimed("happen")) {
						Function function = new Function(Markers.EXPECTATION, firstLinkSubject);
						firstLinkSubject.addProperty(Markers.TENSE, Markers.FUTURE);
						// addLink(function);
					}
					remove(firstLink);
					succeeded();
				}
			}
			// Special case for then; then changes the scene
			else if (firstLink.relationP() && firstLink.isAPrimed(Markers.HAS_MODIFIER) && firstLinkObject.isAPrimed(Markers.THEN)) {
				Entity sceneMarker = Start.makeThing("scene");
				sceneMarker.addProperty(Markers.SCENE, true);
				addLink(sceneMarker);
				remove(firstLink);
				succeeded();
			}
			else if (firstLink.relationP() && firstLink.isAPrimed(Markers.HAS_MODIFIER)) {
				// Do it both ways
				firstLinkSubject.addFeature(firstLinkObject.getType());
				firstLinkSubject.addProperty(Markers.PROPERTY_TYPE, firstLinkObject.getType());
				if (firstLinkObject.getType().equalsIgnoreCase(Markers.ALSO)) {
					firstLinkSubject.addProperty(Markers.HAS_MODIFIER, Markers.ALSO);
				}
				Relation property = new Relation(Markers.MANNER_MARKER, firstLinkSubject, firstLinkObject);
				replace(firstLink, property);
				succeeded();
			}
		}
	}

	/*
	 * Adds tense as atrribute
	 */
	class TenseExpert extends BasicRule {
		public void run() {
			super.run();
			if (false && firstLink.relationP() && firstLink.isAPrimed("has_tense")) {
				firstLinkSubject.addProperty(Markers.TENSE, firstLinkObject.getType());
				// PHW 5 July 2013; don't think we need to keep relation after this
				if (links.containsDeprecated(firstLinkSubject)) {
					remove(firstLink);
				}
				else {
					replace(firstLink, firstLinkSubject);
				}
				succeeded();
			}
		}
	}

	/*
	 * Checks whether progressive or not
	 */
	class ProgressiveExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed("is_progressive")) {
				firstLinkSubject.addProperty(Markers.PROGRESSIVE, true);
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	/*
	 * Makes quantity property if present
	 */
	class QuantityExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed("has_quantity")) {
				firstLinkSubject.addProperty(Markers.QUANTITY, firstLinkObject.getType());
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	/*
	 * Identifies the main sentence (i.e. most basic subj-act-obj relation)
	 */
	class MainClauseExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed(Markers.MAIN_MARKER)) {
				firstLinkSubject.addProperty(Markers.MAIN_MARKER, true);
				firstLinkSubject.addProperty(Markers.CLAUSE_HOLDERS, new Vector<Entity>());
				// PHW 5 July 2013; don't think we need to keep relation after this
				// remove(firstLink);
				if (links.containsDeprecated(firstLinkSubject)) {
					remove(firstLink);
				}
				else if (firstLinkSubject.functionP(Markers.QUESTION_MARKER)) {
					remove(firstLink);
				}
				else {
					replace(firstLink, firstLinkSubject);
				}
				succeeded();
			}
		}
	}

	/*
	 * Identifies relative clauses
	 */
	class RelativeClauseExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed(Markers.RELATIVE_MARKER)) {
				firstLinkObject.addProperty(Markers.RELATIVE_MARKER, firstLinkSubject);
				firstLinkSubject.addProperty(Markers.CLAUSES, new Vector<Entity>());
				Vector<Entity> clauses = (Vector<Entity>) firstLinkSubject.getProperty(Markers.CLAUSES);
				clauses.add(firstLinkObject);
				replace(firstLink, firstLinkObject);
				succeeded();
			}
		}
	}

	/*
	 * Checks cause structures, marks with SOMETIMES if any antecedent or the consequent is marked with sometimes
	 */
	class SometimesExpert extends BasicRule {
		public void run() {
			super.run();
			if (Predicates.isCause(firstLink) && !firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
				if (firstLinkObject.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
					firstLink.addProperty(Markers.IDIOM, Markers.SOMETIMES);
					succeeded();
				}
				else {
					for (Entity e : firstLinkSubject.getElements()) {
						if (e.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
							firstLink.addProperty(Markers.IDIOM, Markers.SOMETIMES);
							succeeded();
							break;
						}
					}
				}
			}
		}
	}

	class EachOtherExpert extends BasicRule2 {
		public void run() {
			super.run();
			Entity firstObject = RoleFrames.getObject(firstLink);
			Entity secondObject = RoleFrames.getObject(secondLink);
			if (firstObject != null && secondObject != null) {
				if (firstObject.isA(Markers.EACH_OTHER)) {
					if (RoleFrames.getObject(firstLink) == RoleFrames.getObject(secondLink)) {
						ISpeak.getSlot(Markers.OBJECT_MARKER, firstLink).setSubject(secondLink.getSubject());
						ISpeak.getSlot(Markers.OBJECT_MARKER, secondLink).setSubject(firstLink.getSubject());
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Fixed. No longer calls getConnected().
	 */
	class ClauseExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.hasProperty(Markers.MAIN_MARKER, true) && secondLink.hasProperty(Markers.RELATIVE_MARKER)) {
				Vector<Entity> holders = (Vector<Entity>) firstLink.getProperty(Markers.CLAUSE_HOLDERS);
				Entity holder = (Entity) secondLink.getProperty(Markers.RELATIVE_MARKER);
				if (!holders.contains(holder)) {
					holders.add((Entity) secondLink.getProperty(Markers.RELATIVE_MARKER));
				}
				Object clauseObject = holder.getProperty(Markers.CLAUSES);
				Vector<Entity> clauses;
				if (clauseObject == null) {
					clauses = new Vector<Entity>();
					holder.addProperty(Markers.CLAUSES, clauses);
				}
				else {
					clauses = (Vector) clauseObject;
				}
				// Mark.say("New holder/clause", holder, secondLink);
				if (!clauses.contains(secondLink)) {
					clauses.add(secondLink);
				}
				remove(secondLink);
				succeeded();
			}
		}
	}

	/*
	 * Removes any duplicate link
	 */
	// class DuplicateExpert extends BasicRule2 {
	// public void run() {
	// super.run();
	// if (firstLink == secondLink) {
	// remove(secondLink);
	// succeeded();
	// }
	// }
	// }

	/*
	 * Deletes attribute relation
	 */
	// class PossibilityExpert extends BasicRule {
	// public void run() {
	// super.run();
	// if (firstLink.isA(Markers.PROPERTY_TYPE) && firstLinkObject.isAPrimed(Markers.POSSIBLY)) {
	// firstLinkSubject.addFeature(Markers.POSSIBLY);
	// remove(firstLink);
	// succeeded();
	// }
	// }
	// }

	/*
	 * Deletes attribute relation
	 */
	class AttributeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP() && firstLink.isAPrimed("has_attribute")) {
				remove(firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A bird flew BEFORE a dog ran"
	 */
	class TimeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(getTimeWords()) && !firstLink.isAPrimed(Markers.TIME_MARKER)) {
				Thread thread = new Thread("thing");
				thread.addType("action");
				thread.addType(Markers.TIME_MARKER);
				thread.addType(firstLink.getType());
				firstLink.setBundle(new Bundle(thread));
				// Relation relation = new Relation(firstLinkSubject,
				// firstLinkObject);
				// addTypeAfterReference("thing", "action", firstLink);
				// addTypeAfterReference("action", Markers.TIME_MARKER,
				// firstLink);
				// relation.addType(firstLink.getType());
				// replace(firstLink, relation);
				// remove(firstLinkSubject);
				// remove(firstLinkObject);
				succeeded();
			}
		}
	}

	class EntailExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP(Markers.ENTAIL_RULE) && !firstLink.isAPrimed(Markers.CAUSE_MARKER)) {

				Relation relation = new Relation(Markers.CAUSE_MARKER, firstLinkSubject, firstLinkObject);

				relation.addType(Markers.ENTAIL_RULE);

				firstLink.setBundle(relation.getBundle());

				if (Markers.MYSTERIOUSLY.equals(firstLink.getProperty(Markers.PROPERTY_TYPE))) {
					firstLink.addType(Markers.UNKNOWABLE_ENTAIL_RULE);
				}
				succeeded();
			}
		}
	}

	class EntailPropertyExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLinkSubject.relationP(Markers.ENTAIL_RULE) && firstLink.isAPrimed(Markers.PROPERTY_TYPE)) {
				firstLinkSubject.addFeature(Markers.SOMETIMES);
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	class FrequencyExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.PROPERTY_TYPE)) {
				if (firstLinkObject.isAPrimed(frequencyWords)) {
					firstLinkSubject.addFeature(firstLinkObject.getType());
					// replace(firstLink, firstLinkSubject);
					remove(firstLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Converts has_effect to cause.
	 */
	class NominalizationExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_effect")) {
				if (firstLinkSubject.isAPrimed("make")) {
					Relation relation = new Relation("cause", firstLinkSubject.getSubject(), firstLinkObject);
					replace(firstLink, relation);
					succeeded();
				}
			}
		}
	}

	/*
	 * Deal with agent causes, converting them to <agent> does something that causes ...
	 */

	class AgentCauseExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.getType().equals(Markers.CAUSE_MARKER)) {
				if (firstLinkSubject.entityP()) {
					Entity action = ISpeak.makeRoleFrame(firstLinkSubject, Markers.PERFORM, new Entity(Markers.ACTION_MARKER));
					firstLink.setSubject(action);
					succeeded();
				}
			}
		}
	}

	/*
	 * Converts make to cause.
	 */
	class MakeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.getType().equals("make")) {
				Entity object = RoleFrames.getObject(firstLink);
				if (object != null && object.isA(Markers.ACTION_MARKER)) {
					// if (firstLink.isAPrimed(Markers.ACTION_MARKER)) {
					if (firstLink.hasProperty(Markers.PROCESSED)) {
						Relation relation = new Relation("cause", firstLinkSubject, firstLinkObject);
						replace(firstLink, relation);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Converts because_of to cause.
	 */

	class BecauseOfExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("because_of")) {
				Relation relation = new Relation(Markers.CAUSE_MARKER, firstLinkObject, firstLinkSubject);
				replace(firstLink, relation);
				succeeded();
			}
		}
	}

	/*
	 * Converts if-then to cause relations
	 */

	class CauseExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("because") && !firstLink.isAPrimed("action")) {
				// Special case; subject is itself a cause relation
				Relation relation = new Relation(firstLinkObject, firstLinkSubject);
				addTypeAfterReference("thing", "action", relation);
				addTypeAfterReference("action", "cause", relation);
				if (firstLinkSubject.hasProperty(Markers.PROBABILITY)) {
					relation.addProbability(firstLinkSubject.getProbability());
				}

				// This should really happen downstream in cause expert, rather
				// than here in the translator
				// The boy was sad because the cat killed the bird.
				if (firstLinkSubject.relationP() && firstLinkSubject.isAPrimed(Markers.MENTAL_STATE_MARKER)) {
					Relation floRelation = (Relation) firstLinkSubject;
					Entity agent = floRelation.getSubject();
					String state = floRelation.getObject().getType();
					if (state == Markers.POSITIVE) {
						Relation view = new Relation(Markers.POSITIVE_VIEW, agent, firstLinkObject);
						// addLinkAtEnd(view);
					}
					else if (state == Markers.NEGATIVE) {
						Relation view = new Relation(Markers.NEGATIVE_VIEW, agent, firstLinkObject);
						// addLinkAtEnd(view);
					}
				}
				replace(firstLink, relation);
				remove(firstLinkSubject);
				// remove(firstLinkObject);
				succeeded();
			}
		}
	}

	class CauseModifier extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.CAUSE_MARKER) && firstLinkObject.hasProperty(Markers.PROPERTY_TYPE, Markers.EVIDENTLY)) {
				if (!firstLink.isAPrimed(Markers.EXPLANATION_RULE)) {
					firstLink.addType(Markers.EXPLANATION_RULE);
				}
				if (!firstLink.isAPrimed(Markers.PROXIMITY_RULE)) {
					firstLink.addType(Markers.PROXIMITY_RULE);
					succeeded();
				}
			}
		}
	}

	class DetectPostHocErgoPropterHoc extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.ASSUME_MARKER) || firstLink.isAPrimed(Markers.INFER_MARKER)) {
				Entity e = RoleFrames.getObject(firstLink);
				if (e.isAPrimed(Markers.CAUSE_MARKER) && e.getObject().isA(Markers.IMPLICATION)) {
					Vector<Entity> elements = e.getSubject().getElements();
					Entity c = elements.lastElement();
					if (c.hasFeature(Markers.IMMEDIATELY)) {
						elements.remove(c);
						e.setObject(c);
						e.addType(Markers.EXPLANATION_RULE);
						e.addType(Markers.PROXIMITY_RULE);
						replace(firstLink, e);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flies if a dog ran"
	 */
	class IfExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.IF_MARKER) && firstLink.isNotAPrimed(Markers.CAUSE_MARKER)) {
				Relation relation = new Relation(Markers.IF_MARKER, firstLinkObject, firstLinkSubject);
				relation.addType(Markers.CAUSE_MARKER);
				replace(firstLink, relation);
				succeeded();
			}
		}
	}

	/**
	 * Processes "If xx wants to do vv, then xx may do vv.", where vv is a variable. Needed for creating rules that
	 * match actions.
	 */
	class DoExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (secondLink.isAPrimed(Markers.DO_WORD)) {
				if (RoleFrames.isRoleFrame(secondLink)) {
					// This part is convoluted because do not want to kill off
					// "John does a good deed"
					if (firstInsideSecond(secondLink, firstLink)) {
						if (!firstLink.isA("is_main")) {
							Entity object = RoleFrames.getObject(secondLink);
							if (secondLink.hasProperty(Markers.CERTAINTY)) {
								object.addProperty(Markers.CERTAINTY, secondLink.getProperty(Markers.CERTAINTY));
							}
							else if (secondLink.hasProperty(Markers.IMPERATIVE)) {
								object.addProperty(Markers.IMPERATIVE, secondLink.getProperty(Markers.IMPERATIVE));
							}
							replace(secondLink, object);
							succeeded();
						}
					}
				}
			}
		}
	}

	class WhatIfExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.IF_MARKER) && firstLinkSubject.isAPrimed("happen")) {
				if (secondLink == firstLinkSubject) {
					remove(secondLink);
					succeeded();
				}
				else if (secondLink.isAPrimed(Markers.IS_QUESTION_MARKER) && firstLinkSubject == secondLinkSubject) {
					Function question = new Function(Markers.QUESTION_MARKER, firstLinkObject);
					question.addType(Markers.WHAT_IF_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "x because y and z.
	 */

	class CauseAntecedantsStarter extends BasicRule {
		public void run() {
			super.run();
			// Mark.say("A");
			if ((Predicates.isCauseWord(firstLink) || firstLink.isAPrimed(Markers.ENABLE_WORD)) && !firstLinkSubject.isA(Markers.CONJUNCTION)) {
				// Deals with corner case: It happens for many reasons
				if (!firstLink.entityP() && firstLink.isNotA(Markers.DO_WORD)) {
					// Mark.say("B");
					if (firstLink.isAPrimed(Markers.ENABLE_WORD)) {

						// Special case because of lack of cause in wordnet enable
						addTypeBeforeLast(Markers.CAUSE_MARKER, firstLink);
					}
					Sequence antecedants = new Sequence(Markers.CONJUNCTION);
					antecedants.addElement(firstLinkSubject);
					firstLink.setSubject(antecedants);
					// Incredible hack. If sentence actually uses word "cause" want result to look the same as if it had
					// used "because".
					if (firstLink.isA("induce")) {
						// Mark.say("C");
						Relation relation = new Relation(antecedants, firstLinkObject);
						addTypeAfterReference("thing", "action", relation);
						addTypeAfterReference("action", "cause", relation);
						firstLink.setBundle(relation.getBundle());
					}
					succeeded();
				}
			}
		}
	}

	class CheckOnExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.WHETHER_QUESTION) && secondLink.isAPrimed(Markers.CHECK_WORD)) {
				if (firstLinkSubject.isAPrimed(Markers.CHECK_WORD)) {
					firstLinkObject.addProperty(Markers.IDIOM, Markers.CHECK);
					// Mark.say("Replacing", firstLink, "with", firstLinkObject);
					// Relation relation = new Relation("love", new Entity("dog"), new Entity("cat"));
					remove(secondLink);
					replace(firstLink, firstLinkObject);
					succeeded();
				}
			}
		}
	}

	class AbsorbAdviceExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isA(Markers.ON)) {
				if (firstLink.relationP() && RoleFrames.isRoleFrame(firstLinkSubject) && RoleFrames.isRoleFrame(firstLinkObject)) {
					if (secondLink.isA("has_comp") && secondLinkObject.isA(Markers.HOW_QUESTION) && secondLinkSubject == firstLinkObject) {
						Entity question = new Function(Markers.QUESTION_MARKER, firstLinkObject);
						question.addType(Markers.HOW_QUESTION);
						RoleFrames.addRole(firstLinkSubject, Markers.ON, question);
						replace(firstLink, firstLinkSubject);
						succeeded();
					}
				}
			}
		}
	}


	class CauseAntecedantsExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.CAUSE_MARKER) && secondLink.isAPrimed(Markers.CAUSE_MARKER)) {
				if (firstLinkObject == secondLinkObject) {
					if (firstLinkSubject.isA(Markers.CONJUNCTION) && secondLinkSubject.isA(Markers.CONJUNCTION)) {
						mergeElements(firstLinkSubject, secondLinkSubject);
						// A complete hack, installed to deal with
						// Regina gossips about Janice because Janice walks and
						// Regina runs.
						// Problem was that multiple indexes on about led to
						// lack of about in first gossip expression

						// Later found out solution screwed Macbeth by damaging
						// concept patters, so reverted to previous form, by
						// diking this out.
						// firstLink.setObject(secondLinkObject);

						remove(secondLink);
						succeeded();
					}
				}
			}
		}

		private void mergeElements(Entity firstLinkSubject, Entity secondLinkSubject) {
			if (!firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES) && secondLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
				firstLink.addFeature(Markers.SOMETIMES);
			}
			if (firstLinkSubject == secondLinkSubject) {
				// Mark.say("Identical, no addition");
				return;
			}
			try {
				for (Entity e : secondLinkSubject.getElements()) {
					if (firstLinkSubject.getElements().contains(e)) {
						// Mark.say("Already included, no addition");
					}
					else {
						firstLinkSubject.addElement(e);
					}
				}
			}
			catch (Exception e) {
				Mark.err("Error in NewRuleSet.CauseAntecedentExpert.mergeElements");
				Mark.err(firstLinkSubject.asString());
				Mark.err(secondLinkSubject.asString());
			}
		}
	}


	// fixes things like xx angers yy because yy is furious AT xx (the AT screws
	// things up and doesn't get nested)
	class CauseAntecedantsFixer extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.CAUSE_MARKER) && secondLink.isAPrimed("at") && firstLinkSubject.sequenceP(Markers.CONJUNCTION)
			        && firstLinkSubject.getElements().contains(secondLinkSubject)) {
				firstLinkSubject.getElements().remove(secondLinkSubject);
				firstLinkSubject.addElement(secondLink);
				succeeded();
			}
		}
	}

	// class BodyPartExpert extends BasicRule2 {
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed(Markers.BODY_PART_WORD)) {
	// if (firstLinkObject.isA("body-part") || firstLinkObject.isA("body-covering")) {
	// if (secondLink.isA("related-to") && secondLinkSubject == firstLinkObject && secondLinkSubject == firstLinkObject)
	// {
	// Relation relation = new Relation(Markers.BODY_PART_MARKER, firstLinkObject, firstLinkSubject);
	// replace(firstLink, relation);
	// remove(secondLink);
	// succeeded();
	// }
	// }
	// }
	// }
	// }

	class PartOfExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (isA(firstLink) && secondLink.isAPrimed("of")) {
				if (firstLinkObject.isA("part") && firstLinkObject == secondLinkSubject) {
					Relation relation = new Relation(Markers.PART_OF, secondLinkObject, firstLinkSubject);
					replace(firstLink, relation);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	private boolean sameThing(Entity thingA, Entity thingB) {
		if (thingA == thingB) {
			return true;
		}
		else if (thingA.isA(Markers.NAME) && thingB.isA(Markers.NAME) && thingA.getType().equals(thingB.getType())) {
			return true;
		}
		return false;
	}

	/*
	 * Processes "A BIRD IS HAPPY"
	 */
	class MoodExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("is") && firstLinkObject != Markers.NULL) {
				if (secondLink.isAPrimed("has_property") && isMentalState(secondLinkObject)) {
					if (sameThing(firstLinkSubject, secondLinkSubject) && firstLinkObject.getType().equals(secondLinkObject.getType())) {
						// if (thirdLinkSubject == firstLink &&
						// thirdLinkObject.isAPrimed("be")) {
						// Thing quality = new Thing("mental-state");

						Entity mood = secondLinkObject;
						Mark.say("Mood1", mood.asString());

						addTypeAfterLast(Markers.MENTAL_STATE, mood);

						if (mood.isAPrimed(positiveWords)) {
							addTypeAfterLast(Markers.POSITIVE, mood);
						}
						else if (mood.isAPrimed(negativeWords)) {
							addTypeAfterLast(Markers.NEGATIVE, mood);
						}
						else {
							addTypeAfterLast(mood.getType(), mood);
						}
						Mark.say("Mood2", mood.asString());
						Relation mentalState = new Relation(Markers.MENTAL_STATE_MARKER, firstLinkSubject, mood);
						// Relation mentalState = RoleFrames.makeRoleFrame(firstLinkSubject,
						// Markers.MENTAL_STATE_MARKER, mood);
						replace(firstLink, mentalState);
						remove(secondLink);
						remove(thirdLink);
						succeeded();
					}
				}
			}
		}
	}

	class TransferExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(transferWords) && firstLinkObject.sequenceP(Markers.ROLE_MARKER)
			        && !firstLink.isAPrimed(Markers.TRANSFER_MARKER)) {
				// firstLink.addType(Markers.TRANSFER_MARKER);
				addTypeAfterReference("action", Markers.TRANSFER_MARKER, firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "THE MAN GAVE A BONE TO A DOG"
	 */
	// class TransferExpert extends BasicRule2 {
	// public void run() {
	// super.run();
	// if (firstLink.isA(transferWords) &&
	// this.firstLinkSubject.isAPrimed("entity") &&
	// firstLinkObject.isAPrimed("entity")) {
	// if (this.secondLink.isAPrimed("to") && this.secondLinkSubject ==
	// firstLink && secondLinkObject.isAPrimed("entity")) {
	// Sequence path = JFactory.createPath();
	// Derivative fromAt = new Derivative("at", firstLinkSubject);
	// Derivative fromPathFunction = new Derivative(Markers.PATH_ELEMENT_MARKER,
	// fromAt);
	// fromPathFunction.addType("from");
	// Derivative toAt = new Derivative("at", secondLinkObject);
	// Derivative toPathFunction = new Derivative(Markers.PATH_ELEMENT_MARKER,
	// toAt);
	// toPathFunction.addType("to");
	// path.addElement(fromPathFunction);
	// path.addElement(toPathFunction);
	// Relation go = new Relation("action", firstLinkObject, path);
	// addTypeAfterReference("action", "move", go);
	// addTypeAfterReference("action", "trajectory", go);
	// Relation transfer = new Relation("action", firstLinkSubject, go);
	// addTypeAfterReference("action", Markers.TRANSFER_MARKER, transfer);
	// replace(firstLink, transfer);
	// remove(this.secondLink);
	// succeeded();
	// }
	// }
	// if (firstLink.isA("take") && this.firstLinkSubject.isAPrimed("entity") &&
	// firstLinkObject.isAPrimed("entity")) {
	// if (this.secondLink.isAPrimed("from") && this.secondLinkSubject ==
	// firstLink && secondLinkObject.isAPrimed("entity")) {
	// Sequence path = JFactory.createPath();
	// Derivative fromAt = new Derivative("at", secondLinkObject);
	// Derivative fromPathFunction = new Derivative("from", fromAt);
	// Derivative toAt = new Derivative("at", firstLinkSubject);
	// Derivative toPathFunction = new Derivative("to", toAt);
	// path.addElement(fromPathFunction);
	// path.addElement(toPathFunction);
	// Relation go = new Relation("action", firstLinkObject, path);
	// addTypeAfterReference("action", "move", go);
	// addTypeAfterReference("action", "trajectory", go);
	// Relation transfer = new Relation("action", firstLinkSubject, go);
	// addTypeAfterReference("action", Markers.TRANSFER_MARKER, transfer);
	// replace(firstLink, transfer);
	// remove(this.secondLink);
	// succeeded();
	// }
	// }
	// }
	// }

	// /*
	// * Processes "A DOG FORCED A CAT TO RUN TO A TREE."
	// */
	// class ForceExpert extends BasicRule3 {
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed(NewRuleSet.toConnector()) &&
	// firstLinkSubject.isAPrimed(requireWords) &&
	// firstLinkObject.isAPrimed("action")) {
	// if (secondLink == firstLinkSubject) {
	// if (thirdLink == firstLinkObject) {
	// // Relation relation = new Relation("dummy",
	// // secondLinkSubject, thirdLink);
	// Bundle b = firstLinkSubject.getBundle();
	// b = b.filterFor("coerce");
	// if (!b.isEmpty()) {
	// firstLinkSubject.setBundle(b);
	// }
	// // replace(this.firstLink, relation);
	// firstLinkSubject.setObject(thirdLink);
	// replace(firstLink, firstLinkSubject);
	// remove(this.secondLink);
	// remove(this.thirdLink);
	// succeeded();
	// }
	// }
	// }
	// }
	// }

	/*
	 * Processes "A DOG WANTED a cat to run to a tree.."
	 * 		goalWords includes "want", "try"
	 */
	class GoalExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(goalWords)) {
				if (firstLink.isNotA("goal")) {
					Bundle b = firstLink.getBundle();
					b = b.filterFor("desire");
					b = b.filterFor("action");
					if (b.isEmpty()) {
						b = firstLink.getBundle();
						b.filterFor("act");
						b = b.filterFor("action");
					}
					// Mark.say("Bundle is", b);
					Bundle b2 = firstLink.getBundle();
					b2 = b2.filterFor(Entity.MARKER_FEATURE);
					b.addAll(b2);
					firstLink.setBundle(b);
					addTypeAfterReference("action", "goal", firstLink);

					succeeded();
				}
			}
		}
	}

	// Shortened, let purge step take care of embeddings
	class ForceAndPersuadeExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("to")) {
				if (firstLinkSubject.isAPrimed(forceWords) || firstLinkSubject.isAPrimed(persuadeWords)) {
					if (RoleFrames.getObject(firstLinkSubject) == firstLinkObject.getSubject()) {
						firstLinkSubject.limitToRoot("action");
						if (firstLinkSubject.isAPrimed(forceWords)) {
							// Mark.say("Z1");
							addTypeAfterReference("action", Markers.FORCE_MARKER, firstLinkSubject);
						}
						else if (firstLinkSubject.isAPrimed(persuadeWords)) {
							// Mark.say("Z2");
							if (!firstLinkSubject.isAPrimed("persuade")) {
								addTypeAfterReference("action", Markers.PERSUATION_MARKER, firstLinkSubject);
							}
						}
						firstLinkSubject.setObject(firstLinkObject);
						replace(firstLink, firstLinkSubject);
						succeeded();
						// }
					}
				}
			}
		}
	}

	/*
	 * Processes "A MAN PERSUADED AN ELEPHANT TO PUSH A LOG" Processes "A MAN PERSUADED AN ELEPHANT TO LAUGH"
	 */
	// class PersuadeExpert extends BasicRule3 {
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed("to") &&
	// firstLinkSubject.isAPrimed(persuadeWords)) {
	// if (firstLinkSubject == secondLink && firstLinkObject == thirdLink) {
	// if (firstLinkSubject.getObject() == firstLinkObject.getSubject()) {
	// Thing man = firstLinkSubject.getSubject();
	// Thing elephant = firstLinkSubject.getObject();
	// Thing log = firstLinkObject.getObject();
	//
	// Sequence path = JFactory.createPath();
	// Relation move = new Relation("move", log, path);
	// Relation push = new Relation(firstLinkObject.getType(), elephant, move);
	// Relation force = new Relation(Markers.PERSUATION_MARKER, man, push);
	// force.addType(firstLinkSubject.getType());
	// replace(firstLink, force);
	// replace(firstLinkObject, move);
	// remove(secondLink);
	// remove(thirdLink);
	// succeeded();
	// }
	// }
	// }
	// }
	// }
	/*
	 * Processes "The dog persuaded the cat to want to run to a tree."
	 */
	// class PersuadeExpert extends BasicRule {
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed("to")) {
	// if (firstLinkSubject.isAPrimed(persuadeWords)) {
	// if (firstLinkObject.isAPrimed("action")) {
	// if (firstLinkSubject.relationP()) {
	// Relation subjectRelation = (Relation) firstLinkSubject;
	// if (subjectRelation.getObject() == firstLinkObject.getSubject()) {
	// // want.addType("action");
	// // want.addType("desire");
	// // want.addType("want");
	// firstLinkSubject.setObject(firstLinkObject);
	// // Do not want to handle cause here. Do it in
	// // commonsense rules
	// // Relation relation = new
	// // Relation(Markers.CAUSE_MARKER, antecedants,
	// // firstLinkObject);
	// replace(firstLink, firstLinkSubject);
	// succeeded();
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	// class PersuadeExpert extends BasicRule {
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed("to")) {
	// if (firstLinkSubject.isAPrimed(persuadeWords)) {
	// if (firstLinkObject.isAPrimed("action")) {
	// if (firstLinkSubject.relationP() && firstLinkObject.relationP()) {
	// Relation subjectRelation = (Relation) firstLinkSubject;
	// Relation objectRelation = (Relation) firstLinkObject;
	// if (subjectRelation.getObject() == objectRelation.getSubject()) {
	// Relation want = (Relation)firstLinkObject;
	// want.addType("action");
	// want.addType("desire");
	// want.addType("want");
	// firstLinkSubject.setObject(want);
	// // Do not want to handle cause here. Do it in
	// // commonsense rules
	// // Relation relation = new
	// // Relation(Markers.CAUSE_MARKER, antecedants,
	// // firstLinkObject);
	// replace(firstLink, firstLinkSubject);
	// succeeded();
	// }
	// }
	// }
	// }
	// }
	// }
	// }
	class ImperativesAndQuestionsExpert extends BasicRule2 {
		public void run() {
			super.run();
			if ((secondLink.isAPrimed("has_attitude") && secondLinkObject.isAPrimed(Markers.IMPERATIVE))
			        || (secondLink.isAPrimed(Markers.IS_IMPERATIVE_MARKER)) && secondLinkObject.isAPrimed("yes")) {
				if (firstLink == secondLinkSubject) {
					firstLink.addProperty(Markers.IMPERATIVE, true);

					// Mark.say("Repetition allowed?", firstLink.isA(repetitionAllowedWords));

					if (firstLink.isA(repetitionAllowedWords)) {
						firstLink.addProperty(Markers.REPETITION_ALLOWED, true);
					}

					remove(secondLink);
					succeeded();
				}
			}
			else if ((secondLink.isAPrimed("has_attitude") && secondLinkObject.isAPrimed("interrogative"))
			        || (secondLink.isAPrimed("is_question") && secondLinkObject.isAPrimed("yes"))) {
				if (firstLinkSubject == secondLinkSubject) {
					if (firstLink.isAPrimed("has_location") && firstLinkObject.isAPrimed("where")) {
						Function question = new Function(Markers.QUESTION_MARKER, firstLinkSubject);
						question.addType(Markers.WHERE_QUESTION);
						replace(firstLink, question);
						remove(secondLink);
						succeeded();
					}
					else if (firstLink.isAPrimed("has_purpose") && firstLinkObject.isAPrimed("why")) {
						Function question = new Function(Markers.QUESTION_MARKER, secondLinkSubject);
						question.addType(Markers.WHY_QUESTION);
						replace(firstLink, question);
						remove(secondLink);
						succeeded();
					}
					else if (firstLink.isAPrimed("has_time") && firstLinkObject.isAPrimed("when")) {
						Function question = new Function(Markers.QUESTION_MARKER, firstLinkSubject);
						question.addType(Markers.WHEN_QUESTION);
						replace(firstLink, question);
						remove(secondLink);
						succeeded();
					}
					else if (firstLink.isAPrimed("has_method") && firstLinkObject.isAPrimed("how")) {
						Function question = new Function(Markers.QUESTION_MARKER, firstLinkSubject);
						question.addType(Markers.HOW_QUESTION);
						replace(firstLink, question);
						remove(secondLink);
						succeeded();
					}
				}
			}
			else if (secondLink.isAPrimed(Markers.DID_QUESTION) && firstLink.isAPrimed(Markers.CAUSE_MARKER)) {
				if (secondLink.getSubject() == firstLink.getObject()) {
					Function question = new Function(Markers.QUESTION_MARKER, firstLink);
					question.addType(Markers.DID_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class QuestionDisjunctionExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isA(Markers.QUESTION_MARKER) && secondLink.isA(Markers.QUESTION_MARKER)) {
				if (firstLink.isA(Markers.QUESTION_MARKER) && secondLink.isA(Markers.QUESTION_MARKER)) {
					if (firstLink.getSubject().isNotA(Markers.DISJUNCTION) && secondLink.getSubject().isNotA(Markers.DISJUNCTION)) {
					Sequence elements = new Sequence(Markers.DISJUNCTION);
					elements.addElement(firstLink.getSubject());
					elements.addElement(secondLink.getSubject());
					firstLink.setSubject(elements);
					remove(secondLink);
					succeeded();
					}
				}
			}
		}
	}

	class ImperativesAndQuestionsExpert1 extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_attitude") && firstLinkObject.isAPrimed("interrogative")
			        || firstLink.isAPrimed("question") && firstLinkObject.isAPrimed("yes")) {
				Function question = new Function(Markers.QUESTION_MARKER, firstLinkSubject);
				question.addType(Markers.WHETHER_QUESTION);
				replace(firstLink, question);
				succeeded();
			}
			else if (firstLink.isAPrimed("imagine") && firstLinkSubject.isAPrimed(Markers.YOU) && firstLinkObject.isAPrimed(Markers.ROLE_MARKER)) {
				Entity object = RoleFrames.getObject(firstLink);
				if (object != null) {
					replace(firstLink, new Function(Markers.IMAGINE, object));
					succeeded();
				}
			}
		}
	}

	class DidQuestionExpert extends BasicRule {
		public void run() {
			super.run();
			boolean debug = false;
			// Mark.say(debug, "X");
			if (firstLink.isAPrimed("is_question") && "yes".equals(firstLinkObject.getType())) {
				// Mark.say(debug, "Y");
				Function question = new Function(Markers.QUESTION_MARKER, firstLinkSubject);
				if (RoleFrames.getObject(firstLinkSubject) == null) {
					// Mark.say(debug, "Q");
					question.addType(Markers.DID_QUESTION);
					replace(firstLink, question);
					succeeded();
				}
				else if (RoleFrames.getObject(firstLinkSubject) != null) {
					if (RoleFrames.getObject(firstLinkSubject).isA("what")) {
						question.addType(Markers.WHAT_QUESTION);
					}
					else if (RoleFrames.getObject(firstLinkSubject).isA("who")) {
						question.addType(Markers.WHO_QUESTION);
					}
					else if (RoleFrames.getObject(firstLinkSubject).isA("whom")) {
						question.addType(Markers.WHOM_QUESTION);
					}
					else {
						// Mark.say(debug, "Z");
						question.addType(Markers.DID_QUESTION);
					}
					replace(firstLink, question);
					succeeded();
				}
				else if (RoleFrames.hasRole("about", "what", firstLinkSubject)) {
					question.addType(Markers.WHAT_QUESTION);
					replace(firstLink, question);
					succeeded();
				}
			}
		}
	}

	class DidReductionExpert extends BasicRule {
		public void run() {
			Mark.say("Entering");
			if (firstLink.isA(Markers.DID_QUESTION)) {
				if (firstLinkSubject.isA(Markers.BE_MARKER)) {
					if (RoleFrames.getRole(Markers.ABOUT_MARKER, firstLinkSubject).isA(Markers.WHAT_QUESTION)) {
						Thread thread = firstLinkSubject.getPrimedThread();
						thread.add(1, Markers.QUESTION_MARKER);
						remove(firstLink);
						succeeded();
					}
				}
			}
		}
	}

	class DidReductionExpert2 extends BasicRule2 {
		public void run() {
			if (firstLink.isA(Markers.BE_MARKER) && secondLink.isA(Markers.MAIN_MARKER) && firstLink == secondLink) {
				remove(secondLink);
				succeeded();
			}
		}
	}


	private static Entity makeObject(Entity element, Entity object) {
		Sequence s = new Sequence(Markers.ROLE_MARKER);
		Function f = new Function(Markers.OBJECT_MARKER, object);
		s.addElement(f);
		element.setObject(s);
		return element;
	}

	/*
	 * Processes "Did the ball touch the block?"
	 */
	class WhatHappensWhenExpert extends BasicRule3 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_attitude") && firstLinkSubject.isAPrimed("when") && firstLinkObject.isAPrimed("interrogative")) {
				if (secondLink.isAPrimed("when")) {
					if (secondLinkSubject.isAPrimed("occur") || secondLinkSubject.isAPrimed("happen")) {
						if (thirdLink == secondLinkSubject) {
							Function question = new Function(Markers.QUESTION_MARKER, secondLinkObject);
							question.addType(Markers.WHAT_HAPPENS_WHEN_QUESTION);
							replace(firstLink, question);
							remove(secondLink);
							remove(thirdLink);
							succeeded();
						}
					}
				}
			}
		}
	}

	/*
	 * Processes "Describe a ball"
	 */
	class DescribeExpert extends BasicRule3 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_attitude") && firstLinkSubject == secondLink && firstLinkObject.isAPrimed("interrogative")) {
				if (secondLink.isAPrimed("tell") && secondLinkSubject.isAPrimed("you") && secondLinkObject.isAPrimed("i")) {
					if (thirdLink.isAPrimed("about") && thirdLinkSubject == secondLink) {
						Function command = new Function(Markers.COMMAND_MARKER, thirdLinkObject);
						command.addType(Markers.DESCRIBE_MARKER);
						replace(secondLink, command);
						remove(thirdLink);
						remove(firstLink);
						succeeded();
					}
				}
			}
		}
	}

	class RoleInverterExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isA(Markers.ACTION_MARKER) && firstLink.relationP()) {
				if (secondLink.isAPrimed(roleWords) && secondLink.getSubject() == firstLink) {
					if (firstLink.getObject().sequenceP()) {
						Sequence roles = (Sequence) (firstLink.getObject());
						Function role = new Function(secondLink.getType(), secondLink.getObject());
						roles.addElement(role);
						remove(secondLink);
						succeeded();
					}
					else {
						// remove(secondLink);
						// succeeded();
					}
				}
			}
		}
	}

	private void purgeThingThreads(Entity t) {
		Bundle oldB = t.getBundle();
		Bundle newB = new Bundle();
		for (Thread thread : oldB) {
			if (!thread.get(0).equals("thing")) {
				newB.add(thread);
			}
		}
		// Mark.say("Bundles", oldB, newB);
		t.setBundle(newB);
	}

	public static boolean USE_ROLES = true;

	/*
	 * Proceses "With probability of 0.8, John loves Mary Does not seem to work as of 8 Mar 2017
	 */
	class ProbabilityExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (false && firstLink.isA("with") && secondLink.isA(Markers.RELATED_TO) && firstLinkObject == secondLinkSubject) {
				remove(firstLink);
				remove(secondLink);
				firstLinkSubject.addProbability(secondLinkObject.getType());
				succeeded();
			}
		}
	}

	/*
	 * Processes "A MAN WALKED WITH A CANE."
	 */
	class RoleExpert extends BasicRule2 {
		private ArrayList<String> testWords = new ArrayList<String>();

		public RoleExpert() {
			testWords.addAll(roleWords);
			testWords.addAll(timeWords);
			testWords.addAll(placePrepositions);
			testWords.addAll(pathPrepositions);
			testWords.addAll(locationPrepositions);
		}

		public void run() {
			super.run();
			// Mark.say("Testing:", secondLink.isA(testWords) || firstLink.isA(testWords));
			if (secondLink.isA(testWords)) {
				// Mark.say("A");
				if (firstLink == secondLinkSubject) {
					// Mark.say("B");
					// If not a role frame, probably a causal connection, as in Strangely, ...
					if (RoleFrames.isRoleFrame(firstLink)) {
						// Mark.say("C");
						Entity e = RoleFrames.getRole(secondLink.getType(), firstLink);
						if (e != secondLinkObject) {
							ISpeak.addRole((Relation) firstLink, secondLink.getType(), secondLinkObject);
						}
					}
					else if (RoleFrames.isRoleFrame(firstLinkSubject)) {
						Mark.say("C");
						Entity e = RoleFrames.getRole(secondLink.getType(), firstLink);
						if (e != secondLinkObject) {
							ISpeak.addRole((Relation) firstLinkSubject, secondLink.getType(), secondLinkObject);
						}
					}
					// Handles appears with embedded role frame

					remove(secondLink);
					succeeded();
				}
			}
			// This must have been useful for something, but don't know what, and screws up
			// xx's harming yy leads to yy's persuading zz to attack xx.
			if (false && firstLink.isA(testWords)) {
				// Mark.say("aaa", firstLink.getType(), testWords);
				if (secondLink == firstLinkSubject) {
					// Mark.say("bbb", secondLink, firstLinkSubject);
					// If not a role frame, probably a causal connection, as in Strangely, ...
					if (RoleFrames.isRoleFrame(secondLink)) {

						// Mark.say("ccc", firstLink.getType(), "\n", firstLink, "\n", secondLink);

						// Mark.say("ddd", secondLink, "\n", firstLink.getType(), "\n", firstLinkObject);

						// This one handles xx's harming yy leads to yy's persuading zz to attack xx.
						// RoleFrames.addRole(secondLink, firstLink.getType(), firstLinkObject);

						// Not sure what this does, if anything

						Entity e = RoleFrames.getRole(firstLink.getType(), secondLink);
						if (e != firstLinkObject) {
							Mark.say("CCC", e);
							ISpeak.addRole((Relation) secondLink, firstLink.getType(), firstLinkObject);
						}
					}

					Mark.say("First link", firstLink);
					Mark.say("Second link", secondLink);

					remove(firstLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "A MAN KICKED A ROCK."
	 */
	class ActionExpert extends BasicRule {

		// "enable, entail, time-relation, become", "travel, move",
		String[] inclusions = { Markers.ENABLE_WORD, Markers.ENTAIL_RULE, Markers.TIME_MARKER, Markers.BECOME };

		String[] exclusions = { "travel", "move" };

		public void run() {
			super.run();

			// Mark.say("Testing", firstLink,
			// "\n", firstLink.isAnyOf(inclusions), firstLink.isNoneOf(exclusions),
			// "\n", Predicates.isCauseWord(firstLink) && firstLink.isNotA(Markers.DO_WORD) &&
			// firstLink.isNotA("establish"),
			// "\n", RoleFrames.isRoleFrame(firstLink)
			// );
			//
			// Mark.say("Features:\n", firstLink, firstLink.isA(Markers.ACTION_MARKER), firstLink .relationP(),
			// !firstLink.hasProperty(Markers.PROCESSED));


			// the relation is not CAUSE: "cause" "move" "help" "prepare" "trigger"; "do"; "establish"; and it doesn't have "roles" sequence frame
			if ((firstLink.isAnyOf(inclusions) && firstLink.isNoneOf(exclusions))
			        || (Predicates.isCauseWord(firstLink) && firstLink.isNotA(Markers.DO_WORD) && firstLink.isNotA("establish"))
			        || RoleFrames.isRoleFrame(firstLink)) {

			}


			// if the relation is an action
			else if (firstLink.isA(Markers.ACTION_MARKER) && !firstLink.isA(locationPrepositions) && firstLink.relationP()
			        && !firstLink.hasProperty(Markers.PROCESSED)) {

				// Mark.say("Roles noted for", firstLink.asString());
				Thread thread = firstLink.getThreadWith(Markers.ACTION_MARKER, firstLink.getType());
				if (thread != null) {
					firstLink.setPrimedThread(thread);
				}
				// firstLink.addProperty(Markers.PROCESSED, true);


				// Input: (rel get (ent bob-92) (ent mad-144))
				// Output: (rel temp (ent bob-92) (seq roles (fun object (ent mad-144))))
				Entity roleFrame;
				if (!firstLinkObject.isA("null")) {
					roleFrame = ISpeak.makeRoleFrame(firstLinkSubject, "temp", firstLinkObject);
				}
				else {
					roleFrame = ISpeak.makeRoleFrame(firstLinkSubject, "temp");
				}


				// assign the primary thread of the action to the new sequence frame
				Bundle bundle = new Bundle(thread);
				roleFrame.setBundle(bundle);
				if (firstLink.hasProperty(Markers.CERTAINTY)) {
					roleFrame.addProperty(Markers.CERTAINTY, firstLink.getProperty(Markers.CERTAINTY));
				}


				replace(firstLink, roleFrame);
				succeeded();
			}
		}
	}

	public static Entity getMentalModelStarter() {
		Sequence roles = new Sequence(Markers.ROLE_MARKER);
		roles.addElement(new Function(Markers.OBJECT_MARKER, new Entity(Markers.STORY_MARKER)));
		Relation relation = new Relation("start", new Entity("you"), roles);
		return relation;
	}

	/*
	 * Processes "A DOG IS IN THE HOUSE"
	 */
	// class LocationExpert extends BasicRule3 {
	// public void run() {
	// super.run();
	// if (firstLink.isA("be") && firstLinkObject == Markers.NULL) {
	// if (pathPrepositions.contains(secondLink.getType())) {
	// // if (thirdLink.isAPrimed("has_mode") &&
	// // thirdLinkObject.isA("be")) {
	// if (firstLink == secondLinkSubject) { // && firstLink ==
	// // thirdLinkSubject) {
	// Sequence location = new Sequence("location");
	// Thing connector = secondLink;
	// if (connector.isAPrimed("to_the_left_of")) {
	// connector.addType("left");
	// }
	// if (connector.isAPrimed("to_the_right_of")) {
	// connector.addType("right");
	// }
	// Relation relation = new Relation(Markers.STATE_TYPE, firstLinkSubject,
	// location);
	// replace(firstLink, relation);
	// remove(thirdLink);
	// succeeded();
	// }
	// }
	// }
	// }
	// }

	/*
	 * Proecesses "THE BALL TOUCHED THE BLOCK"
	 */
	class TouchExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("touch") && firstLinkSubject.isAPrimed("entity") && firstLinkObject.isAPrimed("entity")) {
				Relation contact = new Relation("contact", firstLinkSubject, firstLinkObject);
				Function transition = TransitionFrames.makeAppear(contact);
				replace(firstLink, transition);
				succeeded();
			}
		}

	}

	// public class WithExpert extends BasicRule2 {
	//
	// public WithExpert() {
	// sample = "Estonia fought with Russia";
	// }
	//
	// public void run() {
	// super.run();
	// if (firstLink.isAPrimed("action")) {
	// if (secondLink.isAPrimed("with")) {
	// if (firstLink == secondLinkSubject) {
	// firstLink.setObject(secondLinkObject);
	// Relation fight = new Relation("dummy", secondLinkObject,
	// firstLinkSubject);
	// Bundle b = firstLink.getBundle().getThingClones();
	// fight.setBundle(b);
	// replace(secondLink, fight);
	// succeeded();
	// }
	// }
	// }
	// }
	// }

	/*
	 * Processes "A BOUVIER IS A DOG"
	 */
	// class ThreadExpert extends BasicRule {
	// public void run() {
	// super.run();
	// // Member relation
	// if (firstLink.isA(Markers.IS_A) && firstLinkObject != Markers.NULL &&
	// firstLinkObject.isNotA(Markers.PROPERTY_TYPE)) {
	// Relation classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkObject, firstLinkSubject);
	//
	// if (firstLinkObject.isA(Markers.NAME)) {
	// // Remove "name"; has one because of indefinite article
	// removeName(firstLinkObject);
	// }
	// // Need this to prevent sometimes expressions from altering thread.
	// if (!firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
	// Thread thread = (Thread) (firstLinkObject.getPrimedThread().clone());
	// String type = firstLinkSubject.getType();
	// thread.add(type);
	// // Bundle bundle = firstLinkSubject.getBundle();
	// Bundle bundle = new Bundle(thread);
	//
	// if (firstLinkSubject.isA(Markers.PROPER)) {
	// firstLinkSubject.setBundle(bundle);
	// addProperName(firstLinkSubject);
	// }
	// else if (firstLinkSubject.isA(Markers.NAME)) {
	// // Remove "name"; has one because of indefinite article
	// firstLinkSubject.setBundle(bundle);
	// }
	// else {
	// // Add it if not there, because must not have an indefinite
	// // article;
	// firstLinkSubject.setBundle(bundle);
	// addName(firstLinkSubject);
	// }
	// BundleGenerator.setBundle(type, bundle);
	// }
	// replace(this.firstLink, classification);
	// succeeded();
	// }
	// else if (isA(firstLink) && firstLinkObject != Markers.NULL && firstLinkObject.isA(Markers.PERSONALITY_TRAIT)) {
	// Mark.say("Personality classification");
	// Relation r = new Relation(Markers.PERSONALITY_TRAIT, firstLinkSubject, firstLinkObject);
	// replace(firstLink, r);
	// remove(firstLink);
	// succeeded();
	// }
	// else if (isA(firstLink) && firstLinkObject != Markers.NULL && firstLinkObject.isA(Markers.PROPERTY_TYPE)) {
	//
	// Relation r = new Relation(Markers.PROPERTY_TYPE, firstLinkSubject, firstLinkObject);
	// replace(firstLink, r);
	// remove(firstLink);
	// succeeded();
	// }
	// // Subclass relation
	// else if (firstLink.isAPrimed("kind") && !firstLink.isAPrimed("like")) {
	// Relation classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
	// Thread thread = (Thread) (firstLinkSubject.getPrimedThread().clone());
	// String type = firstLinkObject.getType();
	// thread.add(type);
	// Bundle bundle = new Bundle(thread);
	// firstLinkObject.setBundle(bundle);
	// BundleGenerator.setBundle(type, bundle);
	// replace(this.firstLink, classification);
	// succeeded();
	// }
	// }
	// }

	/**
	 * In "Patrick is a pig", the pig part is not to get naming treatment, as would happen if determiner part left
	 * intact.
	 */

	class ClassificationExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.relationP(Markers.CLASSIFICATION_MARKER) && secondLink.relationP(Markers.HAS_DETERMINER)) {
				if (firstLinkSubject == secondLinkSubject) {
					remove(secondLink);
				}
			}
		}
	}

	class IfClassExpert extends BasicRule2 {

		public void run() {
			super.run();
			if ((firstLink.isA(Markers.IF_MARKER) && secondLink.isA(Markers.IS_A))
			        || (firstLink.isA(Markers.BECAUSE) && secondLink.isA(Markers.IS_A))) {
				if (firstLinkSubject == secondLink || firstLinkObject == secondLink) {
					Entity classification = translateIsAToRoleFrame(secondLink, false);
					// Careful, the if-then may have multiple antecedents, so need to do something that will handle
					// translation everywhere...doing it this way to avoid potential for unintended consequences

					Entity x = secondLink.getSubject();

					secondLink.setSubject(secondLink.getObject());
					secondLink.setObject(x);
					secondLink.removeType(Markers.IS_A);
					secondLink.addType(Markers.CLASSIFICATION_MARKER);

					// if (firstLinkSubject == secondLink) {
					// firstLink.setSubject(classification);
					// }
					// else {
					// firstLink.setObject(classification);
					// }
					// remove(secondLink);
					succeeded();
				}
			}
			// Handles classifications embedded in beliefs and prevents thread change
			else if (firstLink.isA(Markers.BELIEVE_WORD) && secondLink.isA(Markers.IS_A) && RoleFrames.getObject(firstLink) == secondLink) {
				Entity classification = translateIsAToRoleFrame(secondLink, false);
				firstLink.setObject(classification);
				remove(secondLink);
				succeeded();
			}
		}
	}

	private Entity translateIsAToRoleFrame(Entity link, boolean reviseClassification) {
		boolean debug = false;
		// At this point, know it is an is-a relation
		Mark.say(debug, "Debugging translateIsAToRoleFrame");

		Entity subject = link.getSubject();
		Entity object = link.getObject();

		if (link.hasProperty(Markers.CHARACTERIZED, true)) {
			Mark.err("Already handled", link);
			return null;
		}

		link.addProperty(Markers.CHARACTERIZED, true);

		String relation = null;
		Entity roleFrame;

		if (object.isA(Markers.PERSONALITY_TRAIT)) {
			Mark.say(debug, Markers.PERSONALITY_TRAIT);
			relation = Markers.PROPERTY_TYPE;
		}
		// Things that are structures are not, in fact, getting a property
		else if (object.isA(Markers.PROPERTY_TYPE) && !object.isA("structure") && !object.isA("currency")) {
			Mark.say(debug, Markers.PROPERTY_TYPE);
			relation = Markers.PROPERTY_TYPE;
		}
		else if (object.isA(jobWords)) {
			Mark.say(debug, Markers.JOB_TYPE_MARKER);
			relation = Markers.JOB_TYPE_MARKER;
		}

		if (relation != null) {
			roleFrame = RoleFrames.makeRoleFrame(link.getSubject(), relation, link.getObject());
			transferProperties(link, roleFrame);
			return roleFrame;
		}

		Mark.say(debug, Markers.CLASSIFICATION_MARKER);

		Relation classification = new Relation(Markers.CLASSIFICATION_MARKER, object, subject);

		transferProperties(link, classification);

		if (reviseClassification) {
			// Mark.say("Clearing bundle");
			String type = subject.getType();
			Thread thread = (Thread) (object.getPrimedThread().clone());
			if (thread.contains(Markers.NAME)) {
				thread.remove(thread.size() - 1);
			}
			else {
				thread.add(Markers.NAME);

			}
			thread.add(type);

			Bundle bundle = subject.getBundle();
			if (!(link.getProperty(Markers.HAS_MODIFIER) == Markers.ALSO)) {
				bundle.clear();
			}
			bundle.add(thread);
			BundleGenerator.setBundle(type, bundle);

		}

		return classification;
	}

	private void transferProperties(Entity link, Entity roleFrame) {
		link.getPropertyList().stream().forEach(e -> roleFrame.addProperty(e.getLabel(), e.getValue()));
	}

	// class AlsoExpert extends BasicRule {
	// public void run() {
	// if (firstLink.isA(Markers.HAS_MODIFIER) && firstLinkObject.isA(Markers.ALSO)) {
	// firstLinkSubject.addProperty(Markers.HAS_MODIFIER, Markers.ALSO);
	// remove(firstLink);
	// succeeded();
	// }
	// }
	// }

	/**
	 * //// Marks tentative adjustment, 14 June 2015. Problem is that classifications accumulate...
	 */
	class ThreadExpert extends BasicRule {
		public void run() {
			super.run();
			boolean debug = false;
			// Look for John is a dog.

			if (firstLinkSubject.hasProperty(Markers.CHARACTERIZED, true)) {
				Mark.say(debug, "If exit");
			}

			else if (firstLink.isA(Markers.IS_A) && firstLinkObject != Markers.NULL) {
				Mark.say(debug, "Else exit");
				Entity replacement = translateIsAToRoleFrame(firstLink, true);
				if (replacement != null) {

					replace(firstLink, replacement);
					succeeded();
					return;
				}

				Mark.err("Unable to handle", firstLink);

			}
		}
	}

	class ThreadExpert2 extends BasicRule2 {
		public void run() {
			super.run();
			boolean debug = false;
			if (firstLink.isAPrimed("kind") && !firstLink.isAPrimed("like") && !firstLink.isAPrimed("make")) {
				if (secondLink.isA("has_det") && secondLinkSubject == firstLinkObject) {
					Mark.say(debug, "EE", firstLink);
					Relation classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
					Thread thread = (Thread) (firstLinkSubject.getPrimedThread().clone());
					String type = firstLinkObject.getType();
					thread.add(type);
					Bundle bundle = new Bundle(thread);
					firstLinkObject.setBundle(bundle);
					BundleGenerator.setBundle(type, bundle);
					Mark.say(debug, "EE Result", classification);
					remove(secondLink);
					replace(firstLink, classification);
					succeeded();
				}
			}
		}

		private Bundle cull(String s, Bundle b) {
			Bundle result = new Bundle();
			for (Thread t : b) {
				if (t.contains(s)) {
					result.add(t);
				}
			}
			return result;
		}
	}






	/*
	 * Processes proper names using has_det and is_proper relations
	 */
	class NameExpert extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed(Markers.IS_PROPER) && firstLinkObject.isA(Markers.YES)) {
				addProperName(firstLinkSubject);
				addName(firstLinkSubject);
				remove(firstLink);
				succeeded();
			}
			else if (this.firstLink.isAPrimed("has_det")) {
				if (this.firstLinkObject.isAPrimed(Markers.WHAT_QUESTION)) {
					// No special action other than remove determiner
				}
				else if (this.firstLinkObject.isA(Markers.INDEFINITE)) {
					addName(firstLinkSubject);
					firstLinkSubject.addProperty(Markers.DETERMINER, Markers.INDEFINITE);
				}
				else if (this.firstLinkObject.isA(Markers.DEFINITE)) {
					// if (firstLinkSubject.isNotA(Markers.POSITION_TYPE)) {
					addName(firstLinkSubject);
					// }
					firstLinkSubject.addProperty(Markers.DETERMINER, Markers.DEFINITE);
				}
				remove(firstLink);
				succeeded();
			}
			else if (this.firstLink.isAPrimed("has_property")) {
				if (this.firstLinkObject.isA(Markers.ANOTHER)) {
					addName(firstLinkSubject);
					// Not needed; START does it right after all
					// replaceEverywhere(firstLinkSubject);
					firstLinkSubject.addDeterminer(Markers.ANOTHER);
					remove(firstLink);
					succeeded();
				}
				else if (this.firstLinkObject.isA(Markers.FIRST)) {
//					Mark.err("Develop response 2!  Why did this mechanism trigger?");
					addName(firstLinkSubject);
					firstLinkSubject.addFeature(Markers.FIRST);
					remove(firstLink);
					succeeded();
				}
				else if (this.firstLinkObject.isA(Markers.SECOND)) {
//					Mark.err("Develop response 3!  Why did this mechanism trigger?");
					addName(firstLinkSubject);
					firstLinkSubject.addFeature(Markers.SECOND);
					remove(firstLink);
					succeeded();
				}
				else if (this.firstLinkObject.isA(Markers.THIRD)) {
//					Mark.err("Develop response 4!  Why did this mechanism trigger?");
					addName(firstLinkSubject);
					firstLinkSubject.addFeature(Markers.THIRD);
					remove(firstLink);
					succeeded();
				}
				else if (this.firstLinkObject.isA(Markers.FOURTH)) {
//					Mark.err("Develop response 5!  Why did this mechanism trigger?");
					addName(firstLinkSubject);
					firstLinkSubject.addFeature(Markers.FOURTH);
					remove(firstLink);
					succeeded();
				}
				else if (this.firstLinkObject.isA(Markers.FIFTH)) {
//					Mark.err("Develop response 6!  Why did this mechanism trigger?");

					addName(firstLinkSubject);
					firstLinkSubject.addFeature(Markers.FIFTH);
					remove(firstLink);
					succeeded();

				}
			}
		}

	}







	class ConceptExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("start") && firstLinkSubject.isAPrimed("you") && firstLinkObject.isAPrimed(Markers.DESCRIBE_MARKER)) {
				if (secondLinkSubject == firstLinkObject) {
					Function d = new Function(Markers.CONCEPT_MARKER, secondLinkObject);
					firstLink.setObject(d);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class StoryExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("start") && firstLinkSubject.isAPrimed("you") && secondLinkSubject.isAPrimed(Markers.STORY_MARKER)) {
				if (secondLinkSubject == firstLinkObject) {
					Function d = new Function(Markers.STORY_MARKER, secondLinkObject);
					firstLink.setObject(d);
					// Mark.say("Removing", secondLink.asString());
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class PossessionExpert2 extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed("related-to")) {
				boolean legacy = true;
				boolean talk = false;
				if (legacy) {
					Mark.say(talk, "Adding", firstLinkObject, "as owner of", firstLinkSubject);
					Mark.say(talk, "Processing legacy analysis");
					firstLinkSubject.addProperty(Entity.OWNER, firstLinkObject);
					// Entity ownership = RoleFrames.makeRoleFrame(firstLinkObject, Markers.POSSESSION_MARKER,
					// firstLinkSubject);

					// Entity ownership = new Relation("owned-by", firstLinkSubject, firstLinkObject);

					// replace(firstLink, ownership);
					remove(firstLink);
				}
				else {
					Mark.say(talk, "Processing new analysis");
					// Alternate formulation, 2 following:
					Entity ownership = RoleFrames.makeRoleFrame(firstLinkObject, Markers.POSSESSION_MARKER, firstLinkSubject);
					firstLinkSubject.addProperty("owned", true);
					replace(firstLink, ownership);
				}
				succeeded();
			}
		}
	}


	class AngryExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("property") && firstLinkObject.isAPrimed("angry")) {
				if (secondLink.isAPrimed("at") && secondLinkSubject == firstLink) {
					Relation relation = Start.makeRelation("anger", secondLinkObject, firstLinkSubject);
					replace(firstLink, relation);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	public static boolean containsRelation(String type, String subject, String object, Entity sequence) {
		if (!sequence.sequenceP()) {
			return false;
		}
		for (Entity t : sequence.getElements()) {
			if (t.relationP(type)) {
				if (t.getSubject().isA(subject) && t.getObject().isA(object)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRelation(String type, String subject, String object, Entity t) {
		if (t.relationP(type)) {
			if (t.getSubject().isA(subject) && t.getObject().isA(object)) {
				return true;
			}
		}
		return false;
	}

	class StopExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("stop")) {
				Function transition = getNewDerivative("action", firstLink.getObject());
				transition.addType("transition");
				transition.addType("disappear");
				replace(firstLink, transition);
			}
			if (firstLink.isAPrimed("begin")) {
				Function transition = getNewDerivative("action", firstLink.getObject());
				transition.addType("transition");
				transition.addType("appear");
				replace(firstLink, transition);
			}
		}
	}

	// public static boolean storyStopper(Thing x) {
	// if (!x.relationP() || x.isNotA("stop")) {
	// return false;
	// }
	// Relation r = (Relation) x;
	// if (r.isA("stop") && r.getSubject().isA("you") &&
	// r.getObject().isA("story")) {
	// return true;
	// }
	// return false;
	// }

	/*
	 * Processes "A tree is GREEN"
	 */
	class PropertyExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (secondLink.isAPrimed("has_property")) {
				if (firstLink.isAPrimed(Markers.IS) || firstLink.isAPrimed("become")) {
					if (secondLinkSubject.getType().equals(firstLinkSubject.getType())) {
						if (secondLinkObject.getType().equals(firstLinkObject.getType())) {
							String relationType = Markers.PROPERTY_TYPE;
							Entity relation;
							if (firstLinkObject.isAPrimed(Markers.PERSONALITY_TRAIT) || firstLinkObject.isAPrimed(Markers.PERSONALITY_TYPE)) {
								relation = RoleFrames.makeRoleFrame(firstLinkSubject, relationType, firstLinkObject);
								relation.addType(Markers.PERSONALITY_TRAIT);
							}
							else if (isMentalState(firstLinkObject)) {
								Entity quality = firstLinkObject;
								addTypeBeforeLast(Markers.MENTAL_STATE, quality);
								// if (quality.isAPrimed(positiveWords)) {
								// quality.addType(Markers.POSITIVE);
								// }
								// else if (quality.isAPrimed(negativeWords)) {
								// quality.addType(Markers.NEGATIVE);
								// }
								relation = RoleFrames.makeRoleFrame(firstLinkSubject, Markers.MENTAL_STATE_MARKER, quality);
							}
							else {
								relation = RoleFrames.makeRoleFrame(firstLinkSubject, relationType, firstLinkObject);
							}
							if (firstLink.isAPrimed("become")) {
								Function transition = getNewDerivative("action", relation);
								transition.addType("transition");
								transition.addType("appear");
								if (firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
									transition.addProperty(Markers.IDIOM, Markers.SOMETIMES);
								}
								replace(firstLink, transition);
							}
							else {
								if (firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
									relation.addProperty(Markers.IDIOM, Markers.SOMETIMES);
								}
								replace(firstLink, relation);
							}
							remove(secondLink);
							succeeded();
						}
					}
				}
			}
		}
	}

	class InvertProperty extends BasicRule {
		public void run() {
			super.run();
			if (firstLinkSubject.isA(Markers.PROPERTY_TYPE) && firstLink.isA(roleWords)) {
				if (firstLinkSubject.getObject().entityP()) {
					Mark.err("Waiting for InvertProperty to be brought up to date.");
					// Entity roles = Constructors.makeRoles(firstLinkSubject.getObject(), Markers.PROPERTY_TYPE);
					//
					// roles.addElement(new Function(firstLink.getType(), firstLinkObject));
					//
					// Mark.say("Role frame is", firstLink);
					//
					// firstLinkSubject.setObject(roles);
					//
					// remove(firstLink);
					//
					// if (links.containsDeprecated(firstLinkSubject)) {
					// // remove(firstLinkSubject);
					// }
					//
					// succeeded();

				}
			}
		}
	}

	// certain things are treated as classifications when they should be
	// properties
	class PropertySubstitutor extends BasicRule {
		private List<String> propertySubstitutions = Arrays.asList(new String[] { "chinese", "" });

		public void run() {
			super.run();
			if (!firstLink.hasProperty(Markers.PROCESSED) && (firstLink.isAPrimed(Markers.IS) || firstLink.isAPrimed("become"))
			        && firstLinkObject.isAPrimed(propertySubstitutions)) {
				firstLink.addProperty(Markers.PROCESSED, true);
				Relation property = new Relation("has_property", firstLinkSubject, firstLinkObject);
				addLinkAtEnd(property);
				succeeded();
			}
		}
	}

	/**
	 * Could not use is a because property ako relation
	 */
	class PropertyAbsorber extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isA(Markers.HAS_MODIFIER) && firstLinkObject.isA(Markers.PRESUMABLY_WORD)) {
				firstLinkSubject.addProperty(Markers.CERTAINTY, Markers.PRESUMPTION_RULE);
				remove(firstLink);
				succeeded();
			}
			// Probably broken by changes in START
			else if (firstLink.getType().equals(Markers.PROPERTY_TYPE) && !firstLinkSubject.entityP()) {

					firstLinkSubject.addProperty(Markers.PROPERTY_TYPE, firstLinkObject.getType());

				if (firstLinkObject.isAPrimed(Markers.PRESUMABLY_WORD)) {
					firstLinkSubject.addProperty(Markers.CERTAINTY, Markers.PRESUMPTION_RULE);
				}
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	class DeterminerExpert extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("entity") && this.firstLink.isA("has_det")) {
				if (this.firstLinkObject == Markers.NULL) {
					this.firstLinkSubject.addProperty(Markers.DETERMINER, Markers.NONE);
				}
				else {

					this.firstLinkSubject.addProperty(Markers.DETERMINER, firstLinkObject.getType());

					// Mark.say(firstLinkSubject);
				}
				remove(this.firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A LITTLE GIRL is happy"
	 */
	class AdjectiveExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_property")) {
				Relation property = new Relation(Markers.PROPERTY_TYPE, firstLinkSubject, firstLinkObject);
				firstLinkSubject.addFeature(firstLinkObject.getType());

				// replace(firstLink, property);
				remove(firstLink);
				succeeded();
			}
		}
	}

	class QuantifierExpert extends BasicRule {
		public QuantifierExpert() {
			sample = "many trees";
		}

		public void run() {
			super.run();
			if (firstLink.isAPrimed("has_quantifier")) {
				firstLinkSubject.addProperty(Markers.QUANTIFIER, firstLinkObject.getType());
				remove(firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "Paul is the king."
	 */
	class JobExpert extends BasicRule {
		public void run() {
			super.run();
			if (isA(firstLink) || firstLink.isAPrimed("become")) {
				if (firstLinkObject.isA(jobWords)) {
					Entity classification = RoleFrames.makeRoleFrame(firstLinkSubject, Markers.JOB_TYPE_MARKER, firstLinkObject);
					// firstLinkSubject.addFeature(firstLinkObject.getType());

					if (firstLink.isAPrimed("become")) {
						Function transition = getNewDerivative("action", classification);
						transition.addType("transition");
						transition.addType("appear");
						replace(firstLink, transition);
					}
					else {
						replace(firstLink, classification);
					}

					replace(this.firstLink, classification);
					succeeded();
				}
			}
		}
	}

	class JobExpert2 extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("become")) {
				if (firstLinkObject.isAPrimed(jobWords)) {
					Relation job = new Relation(Markers.JOB_TYPE_MARKER, firstLinkObject, firstLinkSubject);
					// firstLinkSubject.addFeature(firstLinkObject.getType());
					Function transition = getNewDerivative("action", job);
					transition.addType("transition");
					transition.addType("appear");
					replace(this.firstLink, transition);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "Run to the store"
	 */
	class CommandExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed("you") && firstLinkObject.isA("action")) {
				if (secondLink == firstLinkObject) {
					Function command = new Function(Markers.COMMAND_MARKER, firstLinkObject);
					command.addType(firstLink.getType());
					replace(firstLink, command);
					remove(secondLink);
					succeeded();
				}
			}
			else if (firstLinkSubject.isAPrimed("you") && firstLinkObject.isA("event")) {
				if (firstLinkObject == secondLinkSubject) {
					Function command = new Function(Markers.COMMAND_MARKER, secondLinkObject);
					command.addType(firstLink.getType());
					replace(firstLink, command);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class WhatExpert extends BasicRule2 {
		public void run() {
			super.run();
			if ((firstLink.isAPrimed("after") || firstLink.isAPrimed("when")) && secondLink.isA("did")) {
				if (firstLinkSubject == secondLinkSubject && firstLinkSubject.isA("happen")) {
					Function question = new Function(Markers.QUESTION_MARKER, new Function(Markers.AFTER, firstLinkObject));
					question.addType(Markers.WHAT_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
			else if (firstLink.isAPrimed("before") && secondLink.isA("did")) {
				if (firstLinkSubject == secondLinkSubject && firstLinkSubject.isA("happen")) {
					Function question = new Function(Markers.QUESTION_MARKER, new Function(Markers.BEFORE, firstLinkObject));
					question.addType(Markers.WHAT_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class TimeRelationExpert extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed("after") && secondLink.isA("did")) {
				if (firstLinkSubject == secondLinkSubject) {
					Function question = new Function(Markers.QUESTION_MARKER, firstLink);
					question.addType(Markers.DID_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
			else if (firstLink.isAPrimed("before") && secondLink.isA("did")) {
				if (firstLinkSubject == secondLinkSubject) {
					Function question = new Function(Markers.QUESTION_MARKER, firstLink);
					question.addType(Markers.DID_QUESTION);
					replace(firstLink, question);
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	class ingExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.GERUND_OF)) {
				// Mark.say("Replacing", firstLinkSubject.getName(), "with", firstLinkObject.getName());
				replace(firstLinkSubject, firstLinkObject);
				remove(firstLink);
				succeeded();
			}
		}
	}

	/*
	 * Peoria is bigger than Concord.
	 */
	class ComparisonExpert extends BasicRule2 {
		// d.stimulate("The man believed the bird flew.");
		// d.stimulate("The man believed the bird wanted to fly.");
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.PROPERTY_TYPE) && secondLink.isAPrimed("than") && firstLink == secondLinkSubject) {
				Relation relation = new Relation(Markers.COMPARISON_MARKER, firstLinkSubject, secondLinkObject);
				relation.addType(firstLinkObject.getType());
				replace(firstLink, relation);
				remove(secondLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A boy is a friend of a girl"
	 */
	class SocialExpert extends BasicRule2 {

		public void run() {
			super.run();
			if (isA(firstLink)) {

				if (secondLink.isAPrimed("related-to")) {
					if (firstLinkObject == secondLinkSubject) {
						String name = firstLinkObject.getType();
						Relation relation = new Relation(name, secondLinkObject, firstLinkSubject);

						if (firstLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
							relation.addProperty(Markers.IDIOM, Markers.SOMETIMES);
						}

						this.addTypeAfterReference("thing", Markers.SOCIAL_MARKER, relation);
						replace(firstLink, relation);
						remove(secondLink);
						succeeded();
					}
				}
			}
			else if (isA(secondLink)) {

				if (firstLink.isAPrimed("related-to")) {
					if (secondLinkObject == firstLinkSubject) {
						String name = secondLinkObject.getType();
						Relation relation = new Relation(name, firstLinkObject, secondLinkSubject);

						if (secondLink.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
							relation.addProperty(Markers.IDIOM, Markers.SOMETIMES);
						}

						this.addTypeAfterReference("thing", Markers.SOCIAL_MARKER, relation);
						replace(secondLink, relation);
						remove(firstLink);
						succeeded();
					}
				}
			}
		}
	}

	class BeliefExpert extends BasicRule {
		// d.stimulate("The man believed the bird flew.");
		// d.stimulate("The man believed the bird wanted to fly.");
		public void run() {
			super.run();
			if (firstLink.isAPrimed("evaluate") && !firstLink.isAPrimed(Markers.BELIEF_MARKER)) {
				if (firstLinkSubject.isA("living-thing") || firstLinkSubject.isA("group") || firstLinkSubject.isA(Markers.NAME)) {
					addTypeAfterReference("evaluate", Markers.BELIEF_MARKER, firstLink);
					succeeded();
				}
			}
		}
	}

	class HowToExpertStarter extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.HAS_PURPOSE)) {
				Sequence recipe = new Sequence(Markers.CONJUNCTION);
				recipe.addType(Markers.RECIPE);
				recipe.addElement(firstLinkSubject);
				Relation instructions = new Relation(Markers.CAUSE_MARKER, recipe, firstLinkObject);
				instructions.addType(Markers.MEANS);
				replace(firstLink, instructions);
				// Does nothing; eliminated by mechanism that clears links embedded in other links
				// addLink(firstLinkSubject);
				succeeded();
			}
		}
	}

	class HowToExpertAugmenter extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink.isAPrimed(Markers.MEANS)) {
				if (secondLink.isAPrimed(Markers.HAS_PURPOSE)) {
					if (firstLinkObject == secondLinkObject) {
						firstLinkSubject.addElement(secondLinkSubject);
						remove(secondLink);
						succeeded();
					}
				}
			}
		}
	}

	class YouCommandExpert extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed("for") && this.firstLink.getSubject().isA("check") && firstLink.getSubject().getSubject().isA("you")) {
				Mark.say(firstLinkSubject.getObject(), firstLinkObject);
				firstLinkSubject.getObject().addElement(new Function("for", firstLinkObject));
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	class NoteGrammarFeatures extends BasicRule {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed("passive_aux") && this.firstLinkObject.isA("be")) {
				firstLinkSubject.addFeature(Markers.PASSIVE);
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
			else if (this.firstLink.isAPrimed("has_tense")) {
				firstLinkSubject.addFeature(firstLinkObject.getType());
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}

	}

	class InputJunkExpert extends BasicRule {
		public void run() {
			super.run();
			if (isInputJunk(firstLink)) {
				remove(firstLink);
				succeeded();
			}
		}
	}

	class OutputJunkExpert extends BasicRule {
		public void run() {
			super.run();
			if (isOutputJunk(firstLink)) {
				remove(firstLink);
				succeeded();
			}
		}
	}

	private boolean isOutputJunk(Entity t) {
		if (t.isAPrimed("has_attribute")) {
			return true;
		}
		else if (t.isAPrimed("has_root")) {
			return true;
		}
		else if (t.isAPrimed("has_det")) {
			return true;
		}
		else if (t.isAPrimed("is_pp")) {
			return true;
		}
		else if (t.isAPrimed("has_comp")) {
			return true;
		}
		else if (t.isAPrimed("related-to")) {
			return true;
		}
		else if (t.isAPrimed("happen")) {
			return true;
		}
		return false;
	}

	// remove all the syntactic relations from the sequence of relations
	// remove "verb_root", "has_person", "has_quality", "has_sign", "has_number", "is_clausal"
	// "has_surface_form", "has_position", "has_category", "has_voice", "has_argument"
	// "has_clause_type", "has_conjunction", "has_counter", "is_wh", "has_nominalization",
	// "is_topic", "has_surface_subject", "has_quoting"
	private boolean isInputJunk(Entity t) {
		if (t.isAPrimed("verb_root")) {
			return true;
		}
		else if (t.isAPrimed("has_person")) {
			return true;
		}
		else if (t.isAPrimed("has_quality")) {
			return true;
		}
		else if (t.isAPrimed("has_sign")) {
			return true;
		}
		else if (t.isAPrimed("has_number")) {
			return true;
		}
		else if (t.isAPrimed("is_clausal")) {
			return true;
		}
		else if (t.isAPrimed("has_surface_form")) {
			return true;
		}
		else if (t.isAPrimed("has_position")) {
			return true;
		}
		else if (t.isAPrimed("has_category")) {
			return true;
		}
		else if (t.isAPrimed("has_voice")) {
			return true;
		}
		else if (t.isAPrimed("has_argument")) {
			return true;
		}
		else if (t.isAPrimed("has_clause_type")) {
			return true;
		}
		else if (t.isAPrimed("has_conjunction")) {
			return true;
		}
		else if (t.isAPrimed("has_counter")) {
			return true;
		}
		else if (t.isAPrimed("is_wh")) {
			return true;
		}
		else if (t.isAPrimed("has_nominalization")) {
			return true;
		}
		else if (t.isAPrimed("is_topic")) {
			return true;
		}
		else if (t.isAPrimed("has_surface_subject")) {
			return true;
		}

		else if (t.isAPrimed("has_quoting")) {
			return true;
		}

		// Oops, need this
		// else if (t.isAPrimed("has_modal")) {
		// return true;
		// }
		return false;
	}

	class PurgeEmbeddingsExpert extends BasicRule2 {
		public void run() {
			super.run();
			// Mark.say("Running");
			if (secondLink.equals(RoleFrames.getObject(firstLink)) && firstLink.isA(assertionWords)) {
			}
			else if (firstLink.equals(RoleFrames.getObject(secondLink)) && secondLink.isA(assertionWords)) {
			}
			else if (inclusion(secondLink, firstLink)) {
				// Mark.say("Removing", secondLink);
				remove(secondLink);
				succeeded();
			}
			else if (inclusion(firstLink, secondLink)) {
				// Mark.say("Removing", firstLink);
				remove(firstLink);
				succeeded();
			}
		}
	}

	class TimeMarkerExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP(Markers.PROPERTY_TYPE) && firstLinkObject.isAPrimed(timeMarkerWords)) {
				addLink(new Function(Markers.MILESTONE, firstLinkObject));
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
			else if (firstLink.relationP("pass") && firstLinkSubject.isAPrimed("time")) {
				replace(firstLink, new Function(Markers.MILESTONE, firstLinkObject));
				succeeded();
			}
		}
	}

	class AdvanceExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.relationP("has_purpose")) {
				if (firstLinkSubject.relationP("advance") && firstLinkObject.relationP("frame")) {
					if (true || secondLinkObject.entityP("imperative")) {
						Entity video = new Entity("video");
						video.addType(Markers.NAME);
						video.addType(firstLinkSubject.getObject().getType());
						Entity frame = new Entity("frame");
						frame.addType(Markers.NAME);
						frame.addType("number" + firstLinkObject.getObject().getType());
						Relation r = new Relation("imperative", video, frame);
						r.addType("advance");
						replace(firstLink, r);
						// remove(secondLink);
						succeeded();
					}
				}
			}
		}
	}

	class ContactExpert extends BasicRule3 {
		public void run() {
			super.run();
			if (firstLink.functionP(Markers.APPEAR_MARKER) && firstLinkSubject.isA("contact")) {
				if (secondLink.relationP("between") && secondLinkSubject == firstLinkSubject) {
					if (thirdLink.relationP("between") && thirdLinkSubject == firstLinkSubject) {
						Relation relation = new Relation("contact", secondLinkObject, thirdLinkObject);
						firstLink.setSubject(relation);
						remove(secondLink);
						remove(thirdLink);
						succeeded();
					}
				}
			}
		}
	}

	class PerfectiveExpert extends BasicRule { // dxh
		public void run() {
			super.run();
			if (this.firstLink.relationP() && this.firstLink.isAPrimed(Markers.IS_PERFECTIVE)) {
				firstLinkSubject.addProperty(Markers.PERFECTIVE, true);
				replace(firstLink, firstLinkSubject);
				succeeded();
			}

		}
	}

	class FixThinkExpert extends BasicRule {
		public void run() {
			super.run();
			// First predicate a trap for Lu murder.
			if (!firstLinkObject.relationP("glorify") && firstLink.relationP(Markers.CAUSE_MARKER) && firstLinkObject.relationP(thinkWords)) {
				Entity thinker = firstLinkObject.getSubject();

				if (!Predicates.embedded(thinker, firstLinkSubject)) {

					Entity consequent = RoleFrames.getObject(firstLinkObject);
					Sequence antecedents = (Sequence) firstLinkSubject;
					Relation cause = new Relation(Markers.CAUSE_MARKER, antecedents, consequent);
					firstLinkObject.setObject(cause);
					replace(firstLink, firstLinkObject);
					succeeded();
				}
			}
		}

	}

	class MannerExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isA("has_method")) {
				RoleFrames.addRole(firstLinkSubject, Markers.BY, firstLinkObject);
				replace(firstLink, firstLinkSubject);
				succeeded();
			}
		}
	}

	class WhetherExpert extends BasicRule {
		public void run() {
			super.run();
			if (firstLink.isA("whether") && Predicates.isCause(firstLinkSubject)) {
				if (firstLinkSubject.getSubject().isA(Markers.CONJUNCTION) && firstLinkSubject.getObject().isA("null")) {
					Function function = new Function(Markers.DID_QUESTION, firstLinkObject);
					replace(firstLink, function);
					succeeded();
				}
			}
			else if (firstLink.isA("whether") && firstLinkSubject.isA("establish")) {
				if (firstLinkSubject.getSubject().isA(Markers.YOU) && RoleFrames.isRoleFrame(firstLinkSubject)) {
					Function function = new Function(Markers.DID_QUESTION, firstLinkObject);
					replace(firstLink, function);
					succeeded();
				}
			}

		}
	}

	class HappenExpert extends BasicRule {
		public void run() {
			super.run();
			Entity e = RoleFrames.getRole("after", firstLink);
			if (firstLink.isA("happen") && e != null) {
				Relation r = new Relation("after", firstLinkSubject, e);
				replace(firstLink, r);
				succeeded();

			}
			e = RoleFrames.getRole("before", firstLink);
			if (firstLink.isA("happen") && e != null) {
				Relation r = new Relation("before", firstLinkSubject, e);
				replace(firstLink, r);
				succeeded();

			}
		}
	}


	// class WorkAroundImperativeQuestionBug extends BasicRule2 {
	// public void run() {
	// super.run();
	// // Mark.say("F/X", firstLink.getType(), secondLink.getType());
	// if (secondLink.isA("is_imperative")) {
	// if (firstLink.isA("is_question")) {
	// remove(firstLink);
	// succeeded();
	// }
	// }
	// }
	// }


	// ------------------------ see BasicTranslator.java transform() for all the functions of the rules, marked bt ///////// ----------
	private void makeRuleSet() {

		boolean verbose = false;

		// addRule(new WorkAroundImperativeQuestionBug());

		addRule(new InputJunkExpert()); ///////// ----------

		addRule(new ingExpert());

		// Needs to be early, else exposes elements already processed
		addRule(new TenseExpert());

		// addRule(new NamingExpert());
		// addRule(new DirectionExpert());
		// addRule(new EventExpert());


		addRule(new SometimesTrap());

		// Creates disappear structures before PropertyExpert makes role frame.
		addRule(new StopExpert());

		addRule(new PropertyExpert()); ///////// ----------
		addRule(new PropertySubstitutor());
		addRule(new PropertyAbsorber());

		addRule(new StoryExpert());
		addRule(new ConceptExpert()); ///////// ----------

		addRule(new ActionExpert()); ///////// ----------

		addRule(new ProbabilityExpert());
		addRule(new RoleExpert()); ///////// ----------

		// Special case for "check on" idiom in concepts; must be before path, because check can mean stop
		addRule(new CheckOnExpert());

		// addRule(new PathExpert());
		addRule(new LocationExpert());
		// addRule(new PathElementExpert());
		addRule(new LocationElementExpert());
		// Need this to get top of to work, etc.
		addRule(new RelatedToExpert());
		addRule(new TransitionExpert()); ///////// ----------

		addRule(new AngryExpert());
		addRule(new TransferExpert()); ///////// ----------
		addRule(new SocialExpert()); ///////// ----------
		// addRule(new WithExpert());

		// ************************************

		// Is and properties
		addRule(new ComparisonExpert());
		addRule(new JobExpert()); ///////// ----------
		addRule(new JobExpert2());

		addRule(new BecomeExpert());

		addRule(new QuantifierExpert());

		// addRule(new PropertyExpert2());
		// Need body part expert before possession expert
		// addRule(new BodyPartExpert());
		addRule(new PartOfExpert());

		addRule(new PossessionExpert2()); ///////// ----------

		addRule(new IntensifierExpert());
		addRule(new ModeExpert());
		addRule(new IfClassExpert());
		// Cannot be before here, screws up something
		// addRule(new AlsoExpert());
		addRule(new ThreadExpert());
		addRule(new ThreadExpert2());
		addRule(new ClassificationExpert());
		addRule(new NameExpert()); ///////// ----------

		// Idiom
		addRule(new TouchExpert());

		// Combinations
		addRule(new TimeExpert());

		// What is this?
		// addRule(new RoleInverterExpert());

		addRule(new GoalExpert()); ///////// ----------
		addRule(new ForceAndPersuadeExpert()); ///////// ----------
		// addRule(new PersuadeExpert());

		// ************************************

		addRule(new DescribeExpert());
		addRule(new NegationExpert()); ///////// ----------
		// addRule(new BeliefExpert());

		// addRule(new PossibilityExpert());

		// addRule(new AttributeExpert());

		addRule(new DeterminerExpert());
		addRule(new AdjectiveExpert());

		addRule(new ContactExpert());

		addRule(new CommandExpert());

		addRule(new WhatExpert());

		addRule(new TimeRelationExpert());

		addRule(new DoExpert());

		addRule(new EntailExpert());
		addRule(new WhatIfExpert());
		addRule(new IfExpert()); ///////// ----------
		addRule(new NominalizationExpert());

		// addRule(new AgentCauseExpert());
		addRule(new MakeExpert());

		addRule(new WouldLikeExpert());

		addRule(new BecauseOfExpert());
		addRule(new CauseExpert()); ///////// ----------
		addRule(new CauseAntecedantsExpert()); ///////// ----------
		addRule(new CauseAntecedantsStarter()); ///////// ----------
		addRule(new CauseAntecedantsFixer());

		addRule(new CauseModifier());

		addRule(new DetectPostHocErgoPropterHoc());

		// addRule(new FrequencyExpert());

		// addRule(new JunkExpert());

		// Translates xx happen after yy to xx after yy.
		addRule(new HappenExpert());

		// Questions
		addRule(new ImperativesAndQuestionsExpert());
		addRule(new DidQuestionExpert());
		// Following could not be made to work in Japan, 2017
		// addRule(new DidReductionExpert());
		// addRule(new DidReductionExpert2());
		addRule(new ImperativesAndQuestionsExpert1()); ///////// ----------
		addRule(new WhatHappensWhenExpert());
		addRule(new QuestionDisjunctionExpert());

		addRule(new ProgressiveExpert());
		addRule(new QuantityExpert());
		addRule(new AtExpert());
		// addRule(new DuplicateExpert());
		addRule(new MainClauseExpert()); ///////// ----------
		addRule(new RelativeClauseExpert()); ///////// ----------
		addRule(new ClauseExpert());

		addRule(new InvertProperty());

		addRule(new AdvanceExpert());
		addRule(new PurgeEmbeddingsExpert()); ///////// ----------
		// Following experts must be after PurgeEmbeddingsExpert

		// addRule(new EntailPropertyExpert());

		addRule(new FrequencyExpert());

		addRule(new TimeMarkerExpert());

		addRule(new PerfectiveExpert());

		addRule(new HowToExpertAugmenter());

		addRule(new HowToExpertStarter());

		addRule(new EachOtherExpert());

		addRule(new SometimesExpert());

		addRule(new FixThinkExpert());

		addRule(new MannerExpert());

		addRule(new WhetherExpert());

		addRule(new NoteGrammarFeatures()); ///////// ----------

		addRule(new YouCommandExpert());

		addRule(new AbsorbAdviceExpert());

		addRule(new OutputJunkExpert()); ///////// ----------



		Mark.say(verbose, "Rule set includes", getRuleSet().size(), "rules");
	}

	public static void main(String[] ignore) throws Exception {
		// true means use experimental start
		final TranslatorGeneratorTestApplication d = new TranslatorGeneratorTestApplication();
		 d.stimulate("The man believed the bird flew.");
		// d.stimulate("The man believed the bird wanted to fly.");
		// d.stimulate("Duncan is king because Macbeth defeated the rebels.");

		// d.stimulate("The boy disappeared because a dog barked and a bear appeared and a cat ran to a lake.");
		// d.stimulate("Henry is happy because James defeated the rebel.");
		// d.stimulate("England's power became weaker than Poland's power");
		// d.stimulate("XX becomes happy because XX wanted an event to occur and the event occurred.");
		// d.stimulate("XX wanted an event to occur.");
		// d.stimulate("XX wanted an event to occur.");
		// d.stimulate("The little girl is happy.");
		// d.stimulate("John is angry at Paul.");
		// d.stimulate("Did the girl take the ball?");
		// d.stimulate("John did not want an action to occur.");
		// d.stimulate("John did not want to murder George and John did not murder George.");
		// d.stimulate("John did not want to run and John did not run.");
		// d.stimulate("John wanted to murder George and John murdered George.");
		// d.stimulate("John was happy because he did not want an action to occur and the action did not occur");
		// d.stimulate("John was happy because John did not want to run and john did not run");
		// d.stimulate("If James harms Henry and Henry does not become dead, then James angers Henry.");
		// d.stimulate("XX may seize RR from YY");
		// d.stimulate("a man walked to a hole");
		// d.stimulate("a man puts a package into a hole");
		// d.stimulate("Why did the man run to a hole");
		// d.stimulate("Sonja is a dog");
		// d.stimulate("XX is an entity");
		// d.stimulate("the bird flew to the top of a tree");
		// d.stimulate("what occurs when a dog appears");
		// d.stimulate("the king persuaded the knight to love the princess");
		// d.stimulate("the king persuaded the people to kill macbeth");
		// d.stimulate("Anthony persuaded the people to attack Brutus because Brutus murdered Caesar");
		// d.stimulate("James persuades George to murder Henry because James wants George to Murder Henry");
		// d.stimulate("A bird flew to the top of a tree");
		// d.stimulate("The dog is the man's friend");
		// d.stimulate("Start description of \"Revenge\".");
		// d.stimulate("Duncan, who is Macduff's friend, ran");


		// d.stimulate("Duncan is Macduff's friend");
		// d.stimulate("Anthony persuaded the bird to fly because Anthony wanted the cat to appear");
		// d.stimulate("Anthony persuaded the bird to fly.");
		// d.stimulate("Start story titled \"Macbeth plot\".");
		// d.stimulate("Then, the boy ran.");
		// d.stimulate("The boy's dog ran.");
		// d.stimulate("Then, George killed Sally.");
		// d.stimulate("Time passes.");
		// d.stimulate("Lady Macbeth, who is Macbeth's wife, persuades Macbeth to want to become the king.");
		// d.stimulate("The man loved his cow.");
		// d.stimulate("A man gave a bone to a boy.");
		// d.stimulate("Describe a ball.");
		// d.stimulate("Lady Macbeth persuades Macbeth to want to become the king.");
		// d.stimulate("Lady Macbeth persuades Macbeth to want to become the king.");
		// d.stimulate("The dog forced the cat to want to run.");
		// d.stimulate("James becomes happy because James laughed and James wants to become the king.");
		// d.stimulate("Anthony persuades the people to attack Brutus.");
		// d.stimulate("The dog moved the bone.");
		// d.stimulate("The dog became happy by moving the bone.");
		// d.stimulate("The man gave the ball to the girl.");
		// d.stimulate("The cat ran.");
		// d.stimulate("Start story.");
		// d.stimulate("A mouse persuaded a bull to disappear..");
		// d.stimulate("Macbeth murdered Duncan by stabbing him with a knife.");
		// d.stimulate("xx is a Dog.");
		// d.stimulate("Macbeth is king.");
		// d.stimulate("James may kill Henry because James is angry at Henry.");
		// d.stimulate("Boris loves beer because Boris is nice.");
		// d.stimulate("Macbeth believes Macduff is nice.");
		// d.stimulate("If xxx murder yyy then yyy is dead.");

		// d.stimulate("A dog forced a bird to fly to a tree.");
		// d.stimulate("Someone damaged the valuable computer networks of Estonia after Estonia harmed Russia.");
		// d.stimulate("A cat disappeared after a dog appeared");
		// d.stimulate("why did macduff kill macbeth");
		// d.stimulate("Start description of \"Revenge\"");
		// d.stimulate("Claudius murders King Hamlet");
		// d.stimulate("Jack murdered Jill with a cane");
		// d.stimulate("Jack gave a book to Jill");
		// d.stimulate("Jack gave Jill a book");
		// d.stimulate("Jack wanted to murder Jill");
		// d.stimulate("Start story");
		// d.stimulate("Duncan is the king");
		// d.stimulate("Duncan, who is Macduff's friend, is the king, and Macbeth is Duncan's successor.");
		// d.stimulate("Macbeth is a person.");
		// d.stimulate("Macbeth talked to the witches.");
		// d.stimulate("If Henry is George's successor, then Henry becomes king.");
		// d.stimulate("Jack murdered Jill with a knife."); // Belief
		// d.stimulate("xx's being not sane leads to xx's killing yy");
		// d.stimulate("A bird flew to the top of a tree");
		// d.stimulate("Jack murdered Jill");
		// d.stimulate("A robin flew because a cat ran.");
		// d.stimulate("A girl forced a cat to disappear.");
		// d.stimulate("The man persuaded the lion to want to bite the cat.");
		// d.stimulate("The man believed the lion wanted to bite the cat.");
		// d.stimulate("The man persuaded the elephant to push the log to a lake");
		// d.stimulate("The man ate an apple with a fork");
		// d.stimulate("The man persuades the woman to push the log.");
		// d.stimulate("The king does not think that John does not love Mary");
		// // Action
		// d.stimulate("The king thinks that John loves Mary"); // Action
		// d.stimulate("Lady Macbeth, who is Macbeth's wife, persuades Macbeth to want to become the king");
		// d.stimulate("Macbeth wants to become king because Lady Macbeth persuaded him to want to become king");
		// d.stimulate("Macbeth appeared because Lady Macbeth persuaded Macbeth to want to become king");
		// d.stimulate("John may steal money from Henry because Henry trusts John");
		// d.stimulate("xx's wanting an action leads to the action.");
		// d.stimulate("Hamlet and Polonius are persons");
		// d.stimulate("Macbeth wants to become king because Lady Macbeth persuaded Macbeth to want to become the
		// king.");
		// d.stimulate("Start story titled \"Hello world\".");
		// d.stimulate("Macbeth killed Duncan with a knife");
		// d.stimulate("Estonia moved a war memorial");
		// d.stimulate("Patrick is a person");
		// d.stimulate("xx ran to a try and xx flew to a lake");
		// d.stimulate("Imagine a jumping event");
		// d.stimulate("Load persona named \"Matthew\"");
		// d.stimulate("A bird loves the Kremlin");
		// d.stimulate("Imagine a killing event");
		// d.stimulate("Imagine that a bird flew");
		// d.stimulate("If someone kills you, then you become dead.");
		// d.stimulate("Someone murdered Boris.");
		// d.stimulate("Why did Macbeth murder Duncan");
		// d.stimulate("Imagine a jumping action");
		// d.stimulate("China's troops fought India's troops");
		// d.stimulate("John loves Mary");
		// d.stimulate("Look at video named \"approach\"");
		// d.stimulate("\"Approach_27\" is a video");
		// d.stimulate("Look at \"Approach_27\"");
		// d.stimulate("Is the first person moving toward the second person");
		// d.stimulate("Start story titled \"Macbeth/Insanity\"");
		// d.stimulate(")If James becomes dead, then James cannot become emotion");
		// d.stimulate("Is the first marker larger than the second marker");
		// d.stimulate("Look at video called \"George\"");
		// d.stimulate("Advance video to frame 27");
		// d.stimulate("Is the first object a person");
		// d.stimulate("Is the first object approaching the second object");
		// d.stimulate("The boy gave the ball to the girl");
		// d.stimulate("The boy gave the girl the ball");
		// d.stimulate("The bird flew to the top of a tree");
		// d.stimulate("Start description of \"Revenge\"");
		// d.stimulate("The cat ran and the bird flew");
		// d.stimulate("web site is part of computer network");
		// d.stimulate("birds have wings");
		// d.stimulate("Macbeth wants to become king");
		// d.stimulate("Why did Macbeth murder Duncan");
		// d.stimulate("A dog believes Duncan is king.");
		// d.stimulate("Macbeth has a dog.");
		// d.stimulate("If James harmed George and George is Henry's friend, then James harmed Henry");
		// d.stimulate("Georgia's telecommunication websites are part of Georgia's computer networks.");
		// d.stimulate("John loves his dog.");
		// d.stimulate("James may kill Henry because James is not sane.");
		// d.stimulate("James is not sane.");
		// d.stimulate("James gave a ball to George.");
		// d.stimulate("Start story titled \"macbeth\".");
		// d.stimulate("Start description of \"macbeth\".");
		// d.stimulate("The man gave the dog a bone.");
		// d.stimulate("She persuaded Macbeth to murder Duncan.");
		// d.stimulate("xx's harming yy leads to yy's harming xx.");
		// d.stimulate("England believed artifact is valuable because England built artifact.");
		// d.stimulate("England may want to harm France because France did not respect England.");
		// d.stimulate("Russia attacked Georgia's army units because Russia wanted to harm Georgia.");
		// d.stimulate("Patrick is happy.");

		// d.stimulate("Patrick gave the cup to Boris.");
		// d.stimulate("Patrick took the cup from Boris.");
		// d.stimulate("Patrick threw the cup towards Boris.");
		// d.stimulate("Patrick is near the tree.");
		// d.stimulate("Patrick persuaded Boris to want to become king.");
		// d.stimulate("The Israelis knew the Egyptians were preparing to attack.");
		// d.stimulate("Patrick tried to control Boris.");

		// d.stimulate("If a room is a toolshed, then it may contain a spade");
		// d.stimulate("A dog appeared");
		// d.stimulate("The president asked Iraq to move toward democracy.");
		// d.stimulate("A room is a toolshed");
		// d.stimulate("Bravo gives a ball to Alpha.");
		// d.stimulate("Start story titled \"Take\".");
		// d.stimulate("George walked toward Paul.");

		// d.stimulate("The Alpha killed Delta");
		// d.stimulate("Duncan becomes dead.");
		// d.stimulate("If Mary persuades someone to attack James, then someone attacks James.");
		// d.stimulate("If Mary persuades Henry to murder James, then Henry murders James.");
		// d.stimulate("Macbeth fought with Macduff with Duncan's sword.");
		// d.stimulate("Macbeth's victory cause Duncan to become happy.");
		// d.stimulate("Mary persuades Henry to murder James.");
		// d.stimulate("Macbeth killed Duncan with a knife");
		// d.stimulate("Imagine that a man gave a ball to a man.");
		// d.stimulate("Imagine that a man flew.");
		// d.stimulate("Start story titled \"hello world\".");
		// d.stimulate("If contact between xxx and yyy appears, then xxx holds yyy");
		// d.stimulate("xxx holds yyy");
		// d.stimulate("Did the other girl take the ball");
		// d.stimulate("Boris wanted Karen to help Patrick");
		// d.stimulate("Boris wanted to help Patrick");
		// d.stimulate("Boris forced Karen to help Patrick");

		// d.stimulate("Karen's helping Patrick leads to Patrick's helping Karen");
		// d.stimulate("Karen helped Patrick because Patrick helped Karen");
		// d.stimulate("Karen knew Patrick was preparing to help Sarah");
		// d.stimulate("Karen came before Patrick left");
		// d.stimulate("Anthony persuades the people to attack Cassius.");
		// d.stimulate("Anthony persuades the people to attack Cassius.");
		// d.stimulate("Imagine that a student gave a ball to a girl.");
		// d.stimulate("John gave a ball to his honey for a rock");
		// d.stimulate("The robin (flew fly travel) to a (tree organism).");

		// d.stimulate("The hawk (flew fly travel) to a conference");
		// d.stimulate("A student gave a ball to a girl.");
		// d.stimulate("John gave a ball to a girl.");
		// d.stimulate("Mary upset the applecart");
		// d.stimulate("if a cat appears a bird may possibly fly");
		//
		// d.stimulate("if a cat appears and a ball bounces then a bird may possibly fly");
		//
		// d.stimulate("Macbeth became happy.");

		// d.stimulate("Macbeth rules Scotland.");
		// d.stimulate("A bird flew.");
		// d.stimulate("Start description of \"revenge\".");
		// d.stimulate("The cat flew.");
		// d.stimulate("The stranger stutters at Anne.");
		// d.stimulate("A bird flew because Macbeth became unhappy.");
		// d.stimulate("Macbeth's success made Duncan become happy.");
		// d.stimulate("Duncan rewarded Macbeth because Duncan became happy.");
		// d.stimulate("Hitler wants to become feared by Petain because Hitler wants to win the war.");
		// d.stimulate("xx's wanting yy leads to xx's becoming unhappy.");
		// d.stimulate("Hitler wants to become feared by Petain");
		// d.stimulate("Luke allows Nick to play with the bike because Luke wants Nick to become happy.");
		// d.stimulate("Dorian thinks that Dorian will become happy if Dorian becomes immortal.");
		// d.stimulate("xx thinks that xx will become happy if xx becomes immortal.");
		// d.stimulate("xx thinks that xx will become happy if xx performs aa.");
		// d.stimulate("A bouvier is a dog.");
		// d.stimulate("Hector thinks that Hector will become harmed if he smokes.");
		// d.stimulate("Hector will become harmed if Hector fights Achilles.");
		// d.stimulate("Hector thinks that Hector will become harmed if Hector fights Achilles.");
		// d.stimulate("Regina gossips about Janice because Janice walks and Regina runs.");
		// d.stimulate("Regina gossips about Janice because Janice does not wear clothes.");
		// d.stimulate("Regina gossips about Janice.");
		// d.stimulate("Cordelia guides Billy to home because Cordelia wants Billy to become happy.");
		// d.stimulate("The man climbed to the top of a tree with a knife.");
		// d.stimulate("Patrick is shy.");
		// d.stimulate("Patrick campaigned for mayor.");

		// d.stimulate("Human campaigned.");
		// d.stimulate("The first human takes a ball from a second human");
		// d.stimulate("Did John kill James because America is individualistic");
		// d.stimulate("If patrick kills sam then patrick must be insane.");
		// d.stimulate("Sally married Patrick because patrick is tall and patrick is short.");

		// d.stimulate("Sometimes, Patrick's killing of Macbeth leads to Macbeth's hating of Patrick.");

		// d.stimulate("Mary sees that John killed Josh.");

		// d.stimulate("John and Mary love each other.");
//		d.stimulate("check whether john loves mary.");
//
//		d.stimulate("check whether john loves mary.");

		Mark.say("Experimental?", Start.isExperimentalStart());
		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(d.getView());
		frame.setSize(800, 600);
		frame.setVisible(true);
		Switch.showTranslationDetails.setSelected(true);

	}
}

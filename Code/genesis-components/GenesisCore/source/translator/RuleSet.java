package translator;

import java.util.*;

import connections.AbstractWiredBox;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import utils.tools.JFactory;

/*
 * Created on Nov 24, 2007 @author phw
 */

public class RuleSet extends AbstractWiredBox {

	public static boolean reportSuccess = false;

	protected ArrayList<Rule> ruleSet;

	protected static List<String> pathPrepositions = Arrays
	        .asList("from", "to", "into", "over", "under", "toward", "via", "behind", "between", "past", "by", "over", "above", "down", "up", "under", "below", "on", "in", "near", "off");

	public static List<String> placePrepositions = Arrays.asList("at", "side", "top", "bottom", "left", "right", "inside", "front", "back");

	protected static List<String> transitionWords = Arrays.asList("appear", "disappear", "change", "increase", "decrease");

	protected static List<String> travelWords = Arrays.asList("travel", "leave", "move", "roll", "decrease");

	protected static List<String> timeWords = Arrays.asList("before", "after", "while");

	protected static List<String> requireWords = Arrays.asList("force", "desire", "induce", "ask", "necessitate", "express");

	protected static List<String> transferWords = Arrays.asList("give", "propel", "push", "roll");

	protected static List<String> roleWords = Arrays.asList("by", "with", "for");

	protected static List<String> mentalStateWords = Arrays
	        .asList("angry", "calm", "happy", "sad", "unhappy", "excited", "tired", "awake", "asleep", "alive", "dead");

	protected Entity root = new Entity("root");

	public RuleSet() {
		super("Rule set");
		makeRuleSet();
	}

	private void makeRuleSet() {

		// Redundancy killer
		addRule(new KillRedundantRoots());
		addRule(new MergeComplementaryRoles());

		// Determiners, adjectives, and regions
		addRule(new AbsorbDeterminer());
		addRule(new AbsorbeAdjective());
		addRule(new ProcessRegion());

		// Auxiliaries and negation
		addRule(new AbsorbAuxiliary());
		addRule(new AbsorbNegation());

		// Question (must be here, to prevent ProcessLocation from screwing up)
		addRule(new ProcessWhat());

		// Location
		// addRule(new ProcessLocation());
		// addRule(new ProcessLocationQuestion());

		// Classification
		addRule(new ProcessClassification());
		addRule(new ProcessClassificationQuestion());

		// Characterization/quality/state
		addRule(new ProcessMentalState());

		// Idioms
		addRule(new EndOfStoryIdiom());

		// Path functions
		addRule(new ProcessOfDeletionA());
		addRule(new ProcessOfDeletionB());
		addRule(new ProcessPath());
		addRule(new ProcessPathFunction());

		// Transfer (has object, must appear before processTrajectory)
		addRule(new ProcessTransfer());

		// Trajectories and transitions
		addRule(new ProcessTrajectory());
		addRule(new ProcessTouch());

		// Transitions
		addRule(new ProcessTransition());

		// Roles
		addRule(new ProcessAction());
		addRule(new ProcessRoles());

		// Force and ask
		addRule(new Force());

		// Cause and time
		// addRule(new ProcessCauseAndTime());

		// Imagine and describe
		addRule(new ProcessImagine());
		addRule(new ProcessDescribe());

		// Do questions
		addRule(new ProcessDoQuestion());
		addRule(new ProcessDoTimeQuestion());
		addRule(new ProcessQuestion());

		// Other Questions
		addRule(new ProcessWhy());
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

	// protected void makeRule4B() {
	// SequencePattern pathPattern = new SequencePattern();
	// RelationPattern subjectPattern = new RelationPattern(new Star(),
	// pathPattern);
	// subjectPattern.mustContain("travel");
	// subjectPattern.mustContain("leave");
	// subjectPattern.mustContain("move");
	// subjectPattern.mustContain("roll");
	// subjectPattern.mustContain("state");
	// ThingPattern objectPattern = new ThingPattern();
	// objectPattern.mustContain(pathPrepositions);
	// RelationPattern relationPattern = new RelationPattern(subjectPattern,
	// objectPattern);
	//
	// DerivativePattern objectPattern2 = new DerivativePattern(new Star());
	// objectPattern2.mustContain("region");
	// RelationPattern relationPattern2 = new RelationPattern(objectPattern,
	// objectPattern2);
	//
	// Derivative pathFunction = new Derivative("pathFunction", objectPattern2);
	// Sequence path = new Sequence("path");
	// path.addElement(pathFunction);
	//
	// Rule rule = new Rule("Path function 2");
	// rule.addPattern(relationPattern);
	// rule.addPattern(relationPattern2);
	// rule.setAncestor(pathPattern);
	// rule.setDescendant(path);
	// rule.addTypeTransferPair(objectPattern, pathFunction);
	// getRuleSet().add(rule);
	//
	// }

	/*
	 * Processes "Did contact appear after the bird flew"
	 */
	class ProcessDoTimeQuestion extends BasicRule {
		ArrayList firstWords = new ArrayList();

		ArrayList secondWords = new ArrayList();

		public ProcessDoTimeQuestion() {
			firstWords.addAll(timeWords);
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
	class AbsorbDeterminer extends BasicRule {
		public void run() {
			super.run();
			if (getFirstLink().getObject().isA("part-of-speech-dt")) {
				if (firstLinkObject.isAPrimed("the")) {
					firstLinkSubject.addType("definite", "feature");
				}
				else if (firstLinkObject.isAPrimed("a") || firstLinkObject.isAPrimed("an")) {
					firstLinkSubject.addType("indefinite", "feature");
				}
				remove(getFirstLink());
				succeeded();
			}
		}
	}

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
				this.firstLinkSubject.addType(this.firstLinkObject.getType(), "feature");
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
				this.firstLinkSubject.addType(this.firstLinkObject.getType(), "feature");
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
				this.firstLinkSubject.addType(this.firstLinkObject.getType(), "feature");
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
	class ProcessClassification extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("entity")) {
				if (this.secondLinkSubject == this.firstLinkSubject && secondLinkObject.isAPrimed("is")) {
					Thread thread = firstLinkObject.getThread("feature");
					Relation classification;
					if (thread == null) {
						classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
					}
					else if (thread.contains("indefinite")) {
						if (firstLinkObject.isAPrimed("unknownWord")) {
							classification = new Relation(Markers.THREAD_TYPE, firstLinkSubject, firstLinkObject);
						}
						else {
							classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
						}
					}
					else if (thread.contains("definite")) {
						classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
					}
					else {
						classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, firstLinkObject);
					}
					String type = firstLinkObject.getType();
					// transferTypes(this.firstLinkSubject,
					// this.firstLinkObject);
					firstLinkObject.addThread(firstLinkSubject.getPrimedThread().copyThread());
					firstLinkObject.addType(type);
					replace(this.firstLink, new Relation("link", root, classification));
					remove(this.secondLink);
					succeeded();

				}
			}
		}
	}

	/*
	 * Processes "A bouvier is unhappy"
	 */
	class ProcessMentalState extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed(mentalStateWords) && firstLinkObject.isAPrimed("entity")) {
				if (this.secondLinkSubject == this.firstLinkSubject && secondLinkObject.isAPrimed("is")) {

					Relation mentalState;
					Entity quality = new Entity("mental-state");
					quality.addType(firstLinkSubject.getThread("feature").lastElement());
					mentalState = new Relation(Markers.MENTAL_STATE_MARKER, firstLinkObject, quality);
					replace(this.firstLink, new Relation("link", root, mentalState));
					remove(this.secondLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "Is bouvier a dog"
	 */
	class ProcessClassificationQuestion extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("thing") && firstLinkObject.isAPrimed("is")) {
				if (this.secondLinkSubject == this.firstLinkSubject && secondLinkObject.isAPrimed("entity")) {
					Relation classification = new Relation(Markers.CLASSIFICATION_MARKER, firstLinkSubject, secondLinkObject);
					classification.addType("question");
					replace(this.firstLink, new Relation("link", root, classification));
					remove(this.secondLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "The ball fell off OF the block"
	 */
	class ProcessOfDeletionA extends BasicRule2 {

		ArrayList words = new ArrayList();

		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed(travelWords) && firstLinkObject.isAPrimed("off")) {
				if (secondLinkSubject.isAPrimed(travelWords) && secondLinkObject.isAPrimed("of")) {
					remove(secondLink);
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "The ball fell off OF the block"
	 */
	class ProcessOfDeletionB extends BasicRule2 {

		ArrayList words = new ArrayList();

		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed(travelWords) && firstLinkObject.isAPrimed("off")) {
				if (secondLinkSubject.isAPrimed("of") && secondLinkObject.isAPrimed("entity")) {
					Function at = new Function("at", secondLinkObject);
					Function pathFunction = new Function("pathFunction", at);
					transferTypes(firstLinkObject, pathFunction);
					if (firstLinkSubject.relationP()) {
						Entity t = firstLinkSubject.getObject();
						if (t.sequenceP()) {
							Sequence path = (Sequence) t;
							path.addElement(pathFunction);
							remove(firstLink);
							remove(secondLink);
							succeeded();
						}
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flew to a tree"
	 */
	class ProcessPath extends BasicRule2 {
		ArrayList placeClasses = new ArrayList();

		ArrayList trajectoryClasses = new ArrayList();

		public ProcessPath() {
			placeClasses.add("entity");
			placeClasses.add("location");
			trajectoryClasses.add("trajectory");
			trajectoryClasses.add("state");
		}

		public void run() {
			super.run();
			// First item is a link, same verb, with path preposition
			if (getFirstLink().getSubject().isAPrimed(trajectoryClasses) && getFirstLink().getObject().isAPrimed(pathPrepositions)) {
				// Second item is a link, same path preposition, with entity
				// or region
				if (getSecondLink().getSubject() == getFirstLink().getObject() && getSecondLink().getObject().isAPrimed(placeClasses)) {
					// Dig out path:
					Relation trajectory = (Relation) (getFirstLink().getSubject());
					Sequence path = (Sequence) (trajectory.getObject());
					// Create path element
					Function place;
					if (secondLinkObject.functionP() && placePrepositions.contains(secondLinkObject.getType())) {
						place = (Function) secondLinkObject;
					}
					else {
						place = new Function("at", getSecondLink().getObject());
					}
					Function pathFunction = new Function("pathFunction", place);
					pathFunction.addType(getSecondLink().getSubject().getType());
					path.addElement(pathFunction);
					remove(getFirstLink());
					remove(getSecondLink());
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "The ball rolled OFF the block"
	 */
	class ProcessPathFunction extends BasicRule2 {

		ArrayList words = new ArrayList();

		public ProcessPathFunction() {
			words.addAll(travelWords);
			words.add("leave");
			words.add("move");
			words.add("roll");
		}

		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed(words) && firstLinkObject.isAPrimed(pathPrepositions)) {
				if (secondLinkSubject == firstLinkSubject && secondLinkObject.isAPrimed("entity")) {

					Function at = new Function("at", secondLinkObject);
					Function pathFunction = new Function("pathFunction", at);
					transferTypes(firstLinkObject, pathFunction);
					if (firstLinkSubject.relationP()) {
						Entity t = firstLinkSubject.getObject();
						if (t.sequenceP()) {
							Sequence path = (Sequence) t;
							path.addElement(pathFunction);
							remove(firstLink);
							remove(secondLink);
							succeeded();
						}
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flew"
	 */
	class ProcessTrajectory extends BasicRule {
		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed(travelWords) && firstLinkObject.isAPrimed("entity")) {
				Sequence path = JFactory.createPath();
				Relation go = new Relation(getFirstLink().getObject(), path);
				transferTypes(getFirstLink().getSubject(), go);
				addTypeAfterReference("action", "trajectory", go);
				replace(getFirstLink(), new Relation("link", root, go));
				replace(getFirstLink().getSubject(), go);
				succeeded();
			}
			else if (firstLinkObject.isAPrimed(travelWords) && firstLinkSubject.isAPrimed("entity")) {
				Sequence path = JFactory.createPath();
				Relation go = new Relation(getFirstLink().getSubject(), path);
				transferTypes(getFirstLink().getObject(), go);
				addTypeAfterReference("action", "trajectory", go);
				replace(getFirstLink(), new Relation("link", root, go));
				replace(getFirstLink().getObject(), go);
				succeeded();
			}
		}
	}

	/*
	 * Proecesses "the ball touched the block"
	 */
	class ProcessTouch extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed("touch") && firstLinkObject.isAPrimed("entity")) {
				if (secondLinkSubject == firstLinkSubject && secondLinkObject.isAPrimed("entity")) {
					if (firstLink.isAPrimed("nominal-subject") && !secondLink.isAPrimed("nominal-subject")) {
						Relation contact = new Relation("contact", firstLinkObject, secondLinkObject);
						Function transition = getNewDerivative("action", contact);
						transition.addType("transition");
						transition.addType("appear");
						remove(secondLink);
						replace(firstLinkSubject, transition);
						replace(firstLink, new Relation("link", root, transition));
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird appeared"
	 */
	class ProcessTransition extends BasicRule {
		public void run() {
			super.run();
			if (getFirstLink().getSubject().isAPrimed(transitionWords) && getFirstLink().getObject().isAPrimed("entity")) {
				// Create derivative
				Function transition = new Function(getFirstLink().getObject());
				transferTypes(getFirstLink().getSubject(), transition);
				replace(getFirstLink(), new Relation("link", root, transition));
				replace(getFirstLink().getSubject(), transition);
				addTypeAfterReference("action", "transition", transition);
				succeeded();
			}
			else if (getFirstLink().getObject().isAPrimed(transitionWords) && getFirstLink().getSubject().isAPrimed("entity")) {
				// Create derivative
				Function transition = new Function(getFirstLink().getSubject());
				transferTypes(getFirstLink().getObject(), transition);
				replace(getFirstLink(), new Relation("link", root, transition));
				replace(getFirstLink().getObject(), transition);
				addTypeAfterReference("action", "transition", transition);
				succeeded();
			}
		}
	}

	/*
	 * A man ate.
	 */
	class ProcessAction extends BasicRule {
		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed("action") && !firstLinkSubject.isAPrimed("imagine") && !firstLinkSubject.isAPrimed(requireWords)
			        && !firstLinkSubject.isAPrimed(transferWords) && firstLinkSubject.functionP() && firstLinkObject.isAPrimed("entity")) {
				System.out.println("Transfer words: " + transferWords);
				System.out.println("Test: " + firstLinkSubject.isAPrimed(pathPrepositions));
				// Create relation
				Sequence bag = new Sequence("bag");
				Relation roleCoupler = new Relation("roles", firstLinkSubject, bag);
				Function slot = new Function("object", firstLinkObject);
				bag.addElement(slot);
				replace(firstLink, new Relation("link", root, roleCoupler));
				succeeded();
			}
			else if (firstLinkSubject.isAPrimed("action") && !firstLinkSubject.isAPrimed("imagine") && !firstLinkSubject.isAPrimed(requireWords)
			        && !firstLinkSubject.isAPrimed(transferWords) && firstLinkObject.isAPrimed("entity")) {
				// Create derivative
				Function action = new Function(firstLinkObject);
				transferTypes(firstLinkSubject, action);
				replace(firstLink, new Relation("link", root, action));
				replace(getFirstLink().getSubject(), action);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A man went to a lake by car"
	 */
	class ProcessRoles extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLinkObject.isAPrimed(roleWords) && firstLinkObject == secondLinkSubject) {
				// Create relation
				Sequence bag = new Sequence("bag");
				Relation roleCoupler = new Relation("roles", firstLinkSubject, bag);
				String preposition = firstLinkObject.getType();
				String role = preposition;
				// if (preposition.equals("with")) {
				// if (secondLinkObject.isA("person")) {
				// role = "coagent";
				// }
				// else if (secondLinkObject.isA("physical-entity")) {
				// role = "instrument";
				// }
				// }
				// if (preposition.equals("for")) {
				// if (secondLinkObject.isA("person")) {
				// role = "beneficiary";
				// }
				// }
				Function slot = new Function(role, secondLinkObject);
				bag.addElement(slot);
				replace(firstLink, new Relation("link", root, roleCoupler));
				remove(secondLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes multiple roles
	 */
	class MergeComplementaryRoles extends BasicRule2 {
		public void run() {
			super.run();
			if (firstLink != secondLink) {
				if (this.firstLinkSubject == root) {
					if (this.secondLinkSubject == root) {
						if (this.firstLinkObject.isAPrimed("roles") && this.secondLinkObject.isAPrimed("roles")) {
							if (firstLinkObject.relationP() && secondLinkObject.relationP()) {
								if (firstLinkObject.getSubject() == secondLinkObject.getSubject()) {
									mergeBags(firstLinkObject.getObject(), secondLinkObject.getObject());
									remove(secondLink);
									succeeded();
								}
							}
						}
					}
				}
			}
		}
	}

	private void mergeBags(Entity object, Entity object2) {
		if (!object.sequenceP() || !object2.sequenceP()) {
			return;
		}
		Sequence s1 = (Sequence) object;
		Sequence s2 = (Sequence) object2;
		for (Iterator i = s2.getElements().iterator(); i.hasNext();) {
			s1.addElement((Entity) (i.next()));
		}
	}

	/*
	 * Processes "A dog forced a bird to fly to a tree"
	 */
	class Force extends BasicRule3 {
		public void run() {
			super.run();
			// System.out.println("Mark 0");
			if (firstLinkSubject.isAPrimed(requireWords) && firstLinkObject.isAPrimed("entity")) {
				// System.out.println("Mark 1");
				if (thirdLinkSubject.isAPrimed("action") && thirdLinkObject.isAPrimed("to")) {
					// System.out.println("Mark 2");
					if (firstLinkSubject == secondLinkSubject && thirdLinkSubject.relationP() && secondLinkObject == thirdLinkSubject.getSubject()) {
						// System.out.println("Mark 3");
						Relation relation = new Relation("force", this.firstLinkObject, this.thirdLinkSubject);
						transferTypes(this.firstLinkSubject, relation);
						replace(this.firstLink, new Relation("link", root, relation));
						remove(this.secondLink);
						remove(this.thirdLink);
						succeeded();
					}
				}
			}
		}
	}

	/*
	 * Processes "A bird flew because/after a dog appeared.
	 */
	// class ProcessCauseAndTime extends BasicRule2 {
	//
	// ArrayList words = new ArrayList();
	//
	// public ProcessCauseAndTime() {
	// words.addAll(timeWords);
	// words.add("because");
	// }
	//
	// public void run() {
	// super.run();
	// if (firstLinkSubject.isAPrimed("action") &&
	// firstLinkObject.isAPrimed("action")) {
	// if (firstLinkObject == secondLinkSubject &&
	// secondLinkObject.isAPrimed(words)) {
	// Relation relation = new Relation(firstLinkObject, firstLinkSubject);
	// // transferTypes(this.secondLinkObject, relation);
	// System.out.println("Relation thread: " +
	// relation.getBundle().getPrimedThread());
	// relation.addType("action");
	// System.out.println("Relation thread: " +
	// relation.getBundle().getPrimedThread());
	// if (secondLinkObject.isAPrimed(timeWords)) {
	// relation.addType(secondLinkObject.getType());
	// }
	// else {
	// relation.addType("cause");
	// }
	// System.out.println("Relation thread: " +
	// relation.getBundle().getPrimedThread());
	// remove(firstLink);
	// remove(secondLink);
	// replace(firstLinkSubject, relation);
	// succeeded();
	// }
	// }
	// }
	// }
	/*
	 * Processes "Did contact appear"
	 */
	class ProcessDoQuestion extends BasicRule {
		ArrayList words = new ArrayList();

		public ProcessDoQuestion() {
			words.add("do");
			words.add("have");
		}

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("action") && this.firstLinkObject.isAPrimed(words)) {
				Function question = new Function("question", firstLinkSubject);
				question.addType("do");
				replace(firstLink, new Relation("link", root, question));
				succeeded();
			}
			else if (this.firstLinkObject.isAPrimed("action") && this.firstLinkSubject.isAPrimed(words)) {
				Function question = new Function("question", firstLinkObject);
				question.addType("do");
				replace(firstLink, new Relation("link", root, question));
				succeeded();
			}
		}
	}

	/*
	 * Processes Did the bird run?
	 */
	class ProcessQuestion extends BasicRule {

		ArrayList subjectWords = new ArrayList();

		ArrayList objectWords = new ArrayList();

		public ProcessQuestion() {
			objectWords.add("do");
			objectWords.add("have");
			subjectWords.add("because");
			subjectWords.addAll(timeWords);
		}

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed(subjectWords) && this.firstLinkObject.isAPrimed(objectWords)) {
				Function question = new Function("question", this.firstLinkSubject);
				replace(this.firstLink, new Relation("link", root, question));
				succeeded();
			}
		}
	}

	class ProcessImagine extends BasicRule2 {

		ArrayList localWords = new ArrayList();

		public ProcessImagine() {
			localWords.add("action");
			localWords.add("event");
			localWords.add("state");
			localWords.addAll(timeWords);
		}

		public void run() {
			super.run();
			if (firstLinkSubject.isAPrimed("imagine")) {
				if (secondLinkSubject.isA("root")) {
					Function command = new Function("imagine", this.secondLinkObject);
					replace(this.firstLink, new Relation("link", root, command));
					succeeded();
				}
			}
			if (secondLinkSubject.isAPrimed("imagine")) {
				if (firstLinkSubject.isA("root")) {
					Function command = new Function("imagine", this.firstLinkObject);
					replace(this.secondLink, new Relation("link", root, command));
					succeeded();
				}
			}
		}
	}

	/*
	 * Processes "Describe a bird"
	 */
	class ProcessDescribe extends BasicRule {

		ArrayList localWords = new ArrayList();

		public ProcessDescribe() {
			localWords.add("action");
			localWords.add("event");
			localWords.add("state");
			localWords.add(timeWords);
		}

		public void run() {
			super.run();
			if (this.firstLinkSubject.isAPrimed("describe")) {
				Function command = new Function("describe", this.firstLinkObject);
				replace(this.firstLink, new Relation("link", root, command));
				succeeded();

			}
		}
	}

	/*
	 * Processes "Why did the bird fly"
	 */
	class ProcessWhat extends BasicRule2 {

		public void run() {
			super.run();
			if (firstLinkObject.isAPrimed("what") && secondLinkObject.isAPrimed("is")) {
				Entity concept = firstLinkSubject;
				Function question = new Function("question", concept);
				question.addType("what");
				replace(firstLink, new Relation("link", root, question));
				remove(secondLink);
				succeeded();
			}
			else if (firstLinkSubject.isAPrimed("is") && firstLinkObject.isAPrimed("what") && secondLinkSubject.isAPrimed("is")
			        && secondLinkObject.isAPrimed("entity")) {
				Entity concept = secondLink.getObject();
				Function question = new Function("question", concept);
				question.addType("what");
				replace(firstLink, new Relation("link", root, question));
				remove(secondLink);
				succeeded();
			}
		}
	}

	/*
	 * Processes "Why did the bird fly"
	 */
	class ProcessWhy extends BasicRule2 {
		ArrayList words = new ArrayList();

		public ProcessWhy() {
			words.add("question");
			words.add("do");
			words.add("have");
		}

		public void run() {
			super.run();
			if (firstLinkSubject.functionP() && firstLinkSubject.isAPrimed(words) && firstLinkObject.isAPrimed("why")) {
				Entity event = ((Function) firstLinkSubject).getSubject();
				Function question = new Function("question", event);
				question.addType("why");
				remove(firstLink);
				replace(firstLinkSubject, question);
				succeeded();
			}
		}
	}

	/*
	 * Processes "A man gave a ball to a woman."
	 */
	class ProcessTransferX extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed("nominal-subject") && this.firstLinkSubject.isAPrimed(transferWords) && firstLinkObject.isAPrimed("entity")) {
				if (this.secondLink.isAPrimed("direct-object") && this.secondLinkSubject.isAPrimed(transferWords)
				        && this.secondLinkObject.isAPrimed("entity")) {
					Sequence path = JFactory.createPath();
					Function at = new Function("at", getFirstLink().getObject());
					Function pathFunction = new Function("from", at);
					path.addElement(pathFunction);
					Relation go = new Relation("action", getSecondLink().getObject(), path);
					addTypeAfterReference("action", "move", go);
					addTypeAfterReference("action", "trajectory", go);
					Relation transfer = new Relation("action", firstLinkObject, go);
					transferTypes(firstLinkSubject, transfer);
					addTypeAfterReference("action", "transfer", transfer);
					replace(getFirstLink(), new Relation("link", root, transfer));
					replace(getFirstLink().getSubject(), go);
					remove(this.secondLink);
					succeeded();
				}
			}
		}
	}

	class ProcessTransfer extends BasicRule2 {
		public void run() {
			super.run();
			if (this.firstLink.isAPrimed("nominal-subject") && this.firstLinkSubject.isAPrimed(transferWords) && firstLinkObject.isAPrimed("entity")) {
				if (this.secondLink.isAPrimed("direct-object") && this.secondLinkSubject.isAPrimed(transferWords)
				        && this.secondLinkObject.isAPrimed("entity")) {
					Sequence path = JFactory.createPath();
					Function at = new Function("at", firstLinkObject);
					Function pathFunction = new Function("from", at);
					path.addElement(pathFunction);
					Relation go = new Relation("action", secondLinkObject, path);
					addTypeAfterReference("action", "move", go);
					addTypeAfterReference("action", "trajectory", go);

					Relation transfer = new Relation("action", firstLinkObject, go);

					transferTypes(firstLinkSubject, transfer);
					addTypeAfterReference("action", "transfer", transfer);
					replace(firstLink, new Relation("link", root, transfer));
					remove(secondLink);
					replace(firstLinkSubject, go);
					succeeded();
				}
			}
		}
	}

	// public static void main(String[] ignore) {
	// new LocalDemo().main(ignore);
	// LocalDemo.textField.setText("a story is a tragedy");
	// }

}
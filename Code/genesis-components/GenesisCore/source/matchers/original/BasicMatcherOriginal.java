package matchers.original;

import java.util.*;

import matchers.Substitutor;
import translator.*;
import utils.*;
import utils.minilisp.LList;
import constants.Markers;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Matcher;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.RoleFrames;

/*
 * Created on May 2, 2009
 * @author phw
 */

public class BasicMatcherOriginal {

	private boolean debugSpecial = false;

	private boolean debugReturn = false;

	public static boolean debug = false;

	public static boolean debugTypeMatch = false;

	private boolean debugAlreadyDone = false;

	private boolean specialMatch = false;

	private static BasicMatcherOriginal matcher;

	public LList<PairOfEntities> matchAnyPart(Entity pattern, Entity datum) {
		// If pattern or datum is null, no match possible
		if (datum == null || pattern == null) {
			return null;
		}
		// See if they match at this level
		LList<PairOfEntities> match = match(pattern, datum);
		if (match != null) {
			return match;
		}
		// If either is a thing, and did not match, then no recursion is
		// possible.
		if (pattern.entityP() || datum.entityP()) {
			return null;
		}
		// Maybe it is a sequence; recurse into each element if so
		if (datum.sequenceP()) {
			for (Entity t : datum.getElements()) {
				match = matchAnyPart(pattern, t);
				if (match != null) {
					return match;
				}
			}
			return null;
		}
		// Recurse into datum subject
		match = matchAnyPart(pattern, datum.getSubject());
		if (match != null) {
			return match;
		}
		// Recurse into datum object
		match = matchAnyPart(pattern, datum.getObject());
		if (match != null) {
			return match;
		}
		return null;
	}

	public boolean matchStructures(Entity pattern, Entity datum) {
		if (pattern.entityP() && datum.entityP()) {
			return true;
		}
		else if (pattern.functionP() && datum.functionP()) {
			return matchStructures(pattern.getSubject(), datum.getSubject());
		}
		else if (pattern.relationP() && datum.relationP()) {
			return matchStructures(pattern.getSubject(), datum.getSubject()) && matchStructures(pattern.getObject(), datum.getObject());
		}
		else if (pattern.sequenceP() && datum.sequenceP()) {
			for (Entity p : pattern.getElements()) {
				boolean test = false;
				for (Entity d : datum.getElements()) {
					boolean result = matchStructures(p, d);
					if (result == true) {
						// Found a match, so withdraw negativism, and stop
						// looking for this one
						test = true;
						continue;
					}
				}
				// Must not have won on any attempted match
				if (test == false) {
					return false;
				}
			}
			// If got to here, everything matched, so ok
			return true;
		}
		return false;
	}

	// public LList<PairOfEntities> matchRuleToInstantiation(Thing rule, Thing
	// instantiation) {
	// BasicMatcher matcher = BasicMatcher.getBasicMatcher();
	// LList<PairOfEntities> bindings = matcher.match(rule.getObject(),
	// instantiation.getObject(), matcher.match(rule.getSubject(), instantiation
	// .getSubject()));
	// return bindings;
	// }

	public LList<PairOfEntities> matchRuleToInstantiation(Entity rule, Entity instantiation) {
		BasicMatcherOriginal matcher = BasicMatcherOriginal.getBasicMatcher();
		LList<PairOfEntities> bindings = matcher.match(rule.getObject(), instantiation.getObject());
		if (bindings == null) {
			return null;
		}
		bindings = matcher.match(rule.getSubject(), instantiation.getSubject(), bindings);
		return bindings;
	}

	public static BasicMatcherOriginal getBasicMatcher() {
		if (matcher == null) {
			matcher = new BasicMatcherOriginal();
		}
		return matcher;
	}

	public LList<PairOfEntities> specialMatch(Entity current, Entity remembered) {
		try {
			specialMatch = true;
			return match(current, remembered);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			specialMatch = false;
		}
		return null;
	}

	public LList<PairOfEntities> match(Entity pattern, Entity current) {
		// ArrayList<Pair> matches = new ArrayList<Pair>();
		LList<PairOfEntities> matches = new LList<PairOfEntities>();
		LList<PairOfEntities> result = match(pattern, current, matches);
		return result;
	}

	public LList<PairOfEntities> matchAll(Entity pattern, Entity current) {
		// ArrayList<Pair> matches = new ArrayList<Pair>();
		LList<PairOfEntities> matches = new LList<PairOfEntities>();
		LList<PairOfEntities> result = matchAll(pattern, current, matches);
		return result;
	}

	public LList<PairOfEntities> matchNegation(Entity pattern, Entity remembered, LList<PairOfEntities> matches) {
		return matchAux(remembered, pattern, matches, true, false);
	}

	public LList<PairOfEntities> match(Entity pattern, Entity datum, LList<PairOfEntities> bindings) {
		return matchAux(pattern, datum, bindings, false, false);
	}

	public LList<PairOfEntities> matchAll(Entity pattern, Entity datum, LList<PairOfEntities> bindings) {
		return matchAux(pattern, datum, bindings, false, true);
	}

	// public LList<PairOfEntities> match(Thing pattern, Thing datum,
	// LList<PairOfEntities> bindings, boolean sign) {
	// Mark.say(debugSequence, "Trying to match " + datum.asString() + " with "
	// + pattern.asString());
	// LList<PairOfEntities> result = matchAux(pattern, datum, bindings, sign);
	// if (result != null) {
	// Mark.say(debug, "Cause matcher matched " + datum.asString() + " with " +
	// pattern.asString() + " yielding " + result);
	// }
	// return result;
	// }

	/*
	 * Only work with sign inverstion at the top level
	 */
	public LList<PairOfEntities> matchAux(Entity pattern, Entity current, LList<PairOfEntities> matches) {
		return matchAux(pattern, current, matches, false, false);
	}

	// private boolean xor(boolean x, boolean y) {
	// return (x && y) || (!x && !y);
	// }

	// Used for inherited classes to disallow matches
	// based on criteria
	public boolean allowMatch(LList<PairOfEntities> matches, Entity pattern, Entity datum) {
		// [phw 24 Jun 2011] Should not return true; that would mean anything
		// matches anything
		// return true;
		return false;
	}

	/*
	 * On the top level, be willing to invert sign; includeAll means include all matching elements on the binding list,
	 * not just variable values
	 */
	public LList<PairOfEntities> matchAux(Entity pattern, Entity datum, LList<PairOfEntities> matches, boolean invertSign, boolean includeAll) {
		// Mark.say("Entering matchAux with\n", pattern.asString(), "\n", datum.asString());
		boolean debugAuxMatch = false;
		boolean debugAction = false;
		boolean debugSequence = false;
		if (matches == null || pattern == null || datum == null) {
			Mark.say(debugAuxMatch, "Something is null, cannot match");
			return matches;
		}

		try {
			Mark.say(debugReturn, "Attempting to match with sign " + invertSign, "\n", pattern.asString(), "\n", datum.asString(), "\n", matches);
		}
		catch (Exception e) {
			Mark.say("Blew out in BasicMatcher.matchAux"); // ,
			                                               // pattern.asString(),
			                                               // datum.asString(),
			                                               // matches,
			                                               // invertSign);
		}

		// Super special case for actions that appear in leads-to relations. May
		// be just specified as Thing actions, such as yy, or as of 27 July
		// 2012, may be embedded in a perform phrase.

		// Mark.say("A/B/C", pattern.entityP(), pattern.isA(Markers.ACTION_MARKER
		// ), datum.isA(Markers.ACTION_MARKER));

		if (pattern.relationP(Markers.PERFORM) && RoleFrames.getObject(pattern) != null && datum.getSubject().isNotA("you")) {

			Entity pActor = pattern.getSubject();
			Entity pAction = RoleFrames.getObject(pattern);
			if (pAction.isA(Markers.ACTION_MARKER)) {
				// Ok, it is a special case.
				Mark.say(debugAction, "Perform trap, working on\n", pattern.asString(), "\n", datum.asStringWithIndexes(), "\n", matches);
				Entity dAction = getBinding(pAction, matches);
				Entity dActor = getBinding(pActor, matches);
				if (dAction != null) {
					Mark.say(debugAction, "Triggered on A");
					// Evidently there is a bound action, use it
					if (dAction == datum) {
						Mark.say(debugAction, "Action succeeded at P1", matches);
						return matches;
					}
					return null;
				}
				else if (dActor != null) {
					Mark.say(debugAction, "Triggered on B, pattern/datum\n", pattern.asString(), "\n", datum.asString());
					// Evidently there is a bound actor, but no bound action
					if (datum.relationP(Markers.ACTION_MARKER) && datum.getSubject().entityP()) {
						if (dActor == datum.getSubject()) {
							// Bingo
							Mark.say(debugAction, "Action succeeded at P2 matching", pAction.asString(), "to", datum.asString());
							matches = matches.cons(new PairOfEntities(pAction, datum));
							return matches;
						}
					}
					else if (datum.functionP(Markers.ACTION_MARKER) && datum.functionP(Markers.TRANSITION_MARKER)) {
						// This is to catch transitions with actor as subject of transition element
						if (dActor == datum.getSubject().getSubject()) {
							matches = matches.cons(new PairOfEntities(pAction, datum));
							return matches;
						}
					}
				}
				// Evidently neither a bound action or actor. Match if possible. First, relation case
				else if (datum.relationP(Markers.ACTION_MARKER) && datum.getSubject().entityP()) {
					Mark.say(debugAction, "Triggered on C");
					matches = matches.cons(new PairOfEntities(pAction, datum));
					matches = matches.cons(new PairOfEntities(pActor, datum.getSubject()));
					Mark.say(debugAction, "Action succeeded at P3 matching", pAction.asString(), "to", datum.asString());
					return matches;
				}
				// Now, transition case, a derivative
				else if (datum.functionP(Markers.ACTION_MARKER) && datum.functionP(Markers.TRANSITION_MARKER)) {
					Mark.say("Bingo, found case never tested!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					Mark.say(debugAction, "Triggered on D");
					matches = matches.cons(new PairOfEntities(pAction, datum));
					matches = matches.cons(new PairOfEntities(pActor, datum.getSubject()));
					Mark.say(debugAction, "Action succeeded at P4 matching", pAction.asString(), "to", datum.asString());
					return matches;
				}
				Mark.say(debugAction, "No luck at all on pattern/datum\n", pattern.asString(), "\n", datum.asString());
				// No luck
				return null;
			}
			return null;
		}
		else if (pattern.entityP() && pattern.isA(Markers.ACTION_MARKER) && datum.isA(Markers.ACTION_MARKER) && pattern.isNotA(Markers.POSITION_TYPE)
		        && datum.isNotA(Markers.POSITION_TYPE)) {

			Mark.say(debugAction, "Special case trap for actions");
			Mark.say(debugAction, "Pattern:", pattern.asStringWithIndexes());
			Mark.say(debugAction, "Datum:  ", datum.asStringWithIndexes());
			Entity match = getBinding(pattern, matches);
			if (match == null) {
				if (allowMatch(matches, pattern, datum)) {
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debugAction, "Action succeeded at A1", matches);
					return matches;
				}
				else {
					// At this point, it is known that the pattern is any action
					// so go ahead and match
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debugAction, "Action succeded at A2, " + matches);
					return matches;
				}
			}
			else if (match == datum) {
				Mark.say(debugAction, "Action succeeded at A3", matches);
				return matches;
			}
			Mark.say(debugAction, "Action failed at A4", "null");
			return null;
		}
		// Yet another leads-to special case
		else if (pattern.entityP("anything")) {
			Mark.say(debug, "Special case trap #1 for elements embedded in appear");
			Entity match = getBinding(pattern, matches);
			if (match == null) {
				if (allowMatch(matches, datum, pattern)) {
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debugReturn, "D", matches);
					return matches;
				}
				else {
					// At this point, it is known that the pattern is "anything"
					// so go ahead and match
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debug, "Succeded at E.1, " + matches);
					return matches;
				}
			}
			else if (match == datum) {
				Mark.say(debugReturn, "E", matches);
				return matches;
			}
			Mark.say(debugReturn, "F", "null");
			return null;
			// if (invertSign && hasNotFeature) {
			// // Evidently, this was a top-level antecedent to a rule,
			// // and at this point we are looking for a match-stopping
			// // positive occurrence
			// return matchAux(storyPart, rulePart, matches, false);
			// }
			// else if (hasNotFeature) {
			// // Not a top-level antecedent to a rule, but embedded in a
			// // negative, so look for element of opposite sign
			// return matchAux(storyPart, rulePart, matches, true);
			// }
			// else if (true) {
			// // Merely embedded, no use of opposite sign
			// return matchAux(storyPart, rulePart, matches, false);
			// }
		}
		// Special case for actions that appear
		else if (pattern.functionP() && pattern.isA(Markers.APPEAR_MARKER) && pattern.getSubject().getType().equals("action")
		        && datum.isAPrimed("action")) {
			Mark.say(debug, "Special case trap #2 for actions embedded in appear");
			boolean hasNotFeature = pattern.hasFeature(Markers.NOT);
			Entity subject = pattern.getSubject();
			if (invertSign && hasNotFeature) {
				// Evidently, this was a top-level antecedent to a rule,
				// and at this point we are looking for a match-stopping
				// positive occurrence
				matches = matchAux(subject, datum, matches, false, includeAll);
				Mark.say(debugReturn, "G", matches);
				return matches;
			}
			else if (hasNotFeature) {
				// Not a top-level antecedent to a rule, but embedded in a
				// negative, so look for element of opposite sign
				matches = matchAux(subject, datum, matches, true, includeAll);
				Mark.say(debugReturn, "H", matches);
				return matches;
			}
			else if (true) {
				// Merely embedded, no use of opposite sign
				matches = matchAux(subject, datum, matches, false, includeAll);
				Mark.say(debugReturn, "I", matches);
				return matches;
			}
		}
		else if (!matchTypes(pattern, datum, invertSign)) {
			Mark.say(debug, "Failed at D", pattern.asString(), datum.asString());
			Mark.say(debugReturn, "K", "null");
			return null;
		}
		else if (datum.isAPrimed("action") && pattern.entityP() && pattern.isAPrimed("action")) {
			Mark.say(debug, "X.0: Looking for match of " + pattern.asString());
			Entity match = getBinding(pattern, matches);
			if (match == null) {
				if (allowMatch(matches, datum, pattern)) {
					matches = matches.cons(new PairOfEntities(pattern, datum));
					// matches.add(new Pair(remembered, current));
					Mark.say(debug, "X.1: Remembering match of " + datum.asString() + " with " + pattern.asString());
					Mark.say(debug, "New bindings: " + matches);
					Mark.say(debugReturn, "L", matches);
					return matches;
				}
				else {
					return null;
				}
			}
			else if (match == datum) {
				Mark.say(debug, "X.2: Old match of " + datum.asString() + " with " + pattern.asString());
				Mark.say(debugReturn, "M", matches);
				return matches;
			}
			Mark.say(debug, "Failed at B to match " + match.getName() + " " + datum.getName());
			Mark.say(debugReturn, "N", "null");
			return null;
		}
		// This change made so that MentalModel, a subclass of thing, can match a thing.
		else if (false && datum.getClass() != pattern.getClass()) {
			Mark.say(debug, "Failed at C to match " + datum.getClass() + ", " + pattern.getClass());
			Mark.say(debugReturn, "O", "null");
			return null;
		}
		else if (datum.entityP() && pattern.entityP()) {
			Mark.say(debug, "Working in part C");
			Entity match = getBinding(pattern, matches);
			if (match == null) {
				if (allowMatch(matches, datum, pattern)) {
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debug, "Succeded at C.1, " + matches);
					return matches;
				}
				else {
					// At this point, it is known that types match, so there is
					// a match.
					matches = matches.cons(new PairOfEntities(pattern, datum));
					Mark.say(debug, "Succeded at C.1, " + matches);
					return matches;
				}
			}
			// Found remembered binding; must match datum
			else if (match == datum) {
				Mark.say(debug, "Succeded at C.2, " + matches);
				Mark.say(debugReturn, "Q", matches);
				return matches;
			}
			else {
				// No match, evidently
				return null;
			}
			// Mark.say(debug, "Failed at C.4 to match " + datum.getName() +
			// " " + pattern.getName());
			// Mark.say(debugReturn, "S", "null");
			// return null;
		}
		else if (datum.functionP() && pattern.functionP()) {
			Function p = (Function) pattern;
			Function d = (Function) datum;
			matches = matchAux(p.getSubject(), d.getSubject(), matches);
			if (matches == null) {
				Mark.say(debug, "Failed at E");
				Mark.say(debugReturn, "T1", "null");
				return matches;
			}

			Mark.say(debugReturn, "T2", matches);
			if (includeAll) {
				matches = matches.cons(new PairOfEntities(pattern, datum));
			}
			// Mark.say("Succeded on\n", datum, "\n", pattern, "\n", matches);
			return matches;
		}
		else if (datum.relationP() && pattern.relationP()) {
			Mark.say(debug, "Matching relations\n", pattern.asString(), "\n", datum.asString());
			Relation p = (Relation) pattern;
			Relation d = (Relation) datum;
			Mark.say(debug, "Matching relation subjects\n", p.getSubject().asString(), "\n", d.getSubject().asString());
			LList<PairOfEntities> subjectList = matchAux(p.getSubject(), d.getSubject(), matches);
			if (subjectList == null) {
				Mark.say(debug, "Failed at F");
				Mark.say(debugReturn, "U", "null");
				return null;
			}
			else {
				matches = matchAux(p.getObject(), d.getObject(), subjectList);
				if (matches == null) {
					Mark.say(debug, "Failed at G");
					Mark.say(debugReturn, "V", "null");
					return null;
				}
				else {
					Mark.say(debugReturn, "W", matches);
					if (includeAll) {
						matches = matches.cons(new PairOfEntities(pattern, datum));
					}
					return matches;
				}
			}
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (datum.sequenceP() && pattern.sequenceP()) {
			Mark.say(debugSequence, "Starting sequence match\n", pattern.asString(), "\n", datum.asString());
			for (Entity patternElement : pattern.getElements()) {
				Mark.say(debugSequence, "Working on pattern element", patternElement.asString());
				// Look for element of matching type
				String patternType = patternElement.getType();
				boolean found = false;
				for (Entity datumElement : datum.getElements()) {
					Mark.say(debugSequence, "Looking ad datum element", datumElement.asString());
					// if (matchTypes(datumElement, patternElement, false)) {
					Mark.say(debugSequence, "Now matching pattern element", patternElement.asString(), "to", datumElement.asString());
					LList<PairOfEntities> matchList = matchAux(patternElement, datumElement, matches);
					if (matchList == null) {
						Mark.say(debugSequence, "Failed at H in sequence matching--matching sequence types do not match for", patternType);
						// Have to keep going, may be multiple elements of
						// same type
						// return null;
						continue;
					}
					else {
						found = true;
						matches = matchList;
						if (includeAll) {
							matches = matches.cons(new PairOfEntities(patternElement, datumElement));
						}
						break;
					}
					// }
				}
				if (!found) {
					Mark.say(debugSequence, "Failed at I in sequence matching--no matching type for", patternType);
					Mark.say(debugReturn, "X", "null");
					return null;
				}
			}
			// Alls is well
			return matches;
		}
		// Not same types, cannot match! Noted only on 24 September 2013
		return null;
		// Mark.say(debugReturn, "Y", matches);
		// return matches;
	}

	private Entity getBinding(Entity key, LList<PairOfEntities> pairs) {
		for (PairOfEntities pair : pairs) {
			if (key == pair.getPattern()) {
				return pair.getDatum();
			}
		}
		return null;
	}

	/*
	 * Complicated, because may have to match a thread from the past.
	 */
	private boolean matchTypes(Entity patternThing, Entity currentThing, boolean invertSign) {
		Mark.say(debug, "Matching types of", patternThing, currentThing);
		boolean t1Not = currentThing.hasFeature(Markers.NOT);
		boolean t2Not = patternThing.hasFeature(Markers.NOT);
		// Can't match unless signs match; that is not die will not match die
		// unless looking for inverted match
		if (t1Not != t2Not && !invertSign) {
			Mark.say(debug, "Incompatible signs", "" + t1Not, "" + t2Not, invertSign);
			return false;
		}
		else if (t1Not == t2Not && invertSign) {
			Mark.say(debug, "Incompatible signs", "" + t1Not, "" + t2Not, invertSign);
			return false;
		}
		// Special case for handling matching of abstract notion of action to
		// any action description; note that the use of getType()
		// requires the action in a rule to be non-specific, not just any old
		// action.
		if (currentThing.isAPrimed("action") && patternThing.getType().equals("action")) {
			return true;
		}
		// General case, make sure threads are compatible
		// mpfay 3/11/2014 problem: flipped pattern & current here; working to resolve
		if (matchTypes(getRegularThread(currentThing), getRegularThread(patternThing))) {
			// if (matchTypes(getRegularThread(patternThing), getRegularThread(currentThing))) {
			return true;
		}
		// preposition case
		else if (isPreposition(currentThing.getType()) && currentThing.getType().equals(patternThing.getType())) {
			return true;
		}
		else {
			for (Thread currentThread : currentThing.getBundle()) {
				if (currentThread.contains(Entity.MARKER_FEATURE) || currentThread.contains(Markers.DETERMINER)) {
					continue;
				}
				for (Thread memoryThread : patternThing.getBundle()) {
					if (memoryThread.contains(Entity.MARKER_FEATURE)) {
						continue;
					}
					// if (matchTypes(currentThread, memoryThread)) {
					if (matchTypes(memoryThread, currentThread)) {
						Mark.say(debugTypeMatch, "Matched " + currentThread + ", " + memoryThread);
						Mark.say(debugTypeMatch, "... but could not match " + getRegularThread(currentThing) + ", " + getRegularThread(patternThing));
						return true;
					}
					else {
						Mark.say(debugTypeMatch, "No match " + currentThread + ", " + memoryThread);
					}
				}
			}
		}
		return false;
	}

	private Thread getRegularThread(Entity t) {
		for (Thread currentThread : t.getBundle()) {
			if (currentThread.contains(Entity.MARKER_FEATURE)) {
				Mark.say("Necesary Check?");
				continue;
			}
			return currentThread;
		}
		return null;
	}

	private boolean isPreposition(String s) {
		return NewRuleSet.placePrepositions.contains(s) || NewRuleSet.pathPrepositions.contains(s);
	}

	/*
	 * Work down the thread. As long as classes are the same, continue. As soon as NAME is encountered, win, unless name
	 * is actually I, in which case, require that too. If memory thread becomes longer, lose. If they don't match, lose.
	 * If you get all the way to the end of both, win.
	 */
	private boolean matchTypes(Thread currentThread, Thread memoryThread) {
		// Mark.say("Matching", currentThread, memoryThread);
		if (specialMatch) {
			return specialMatchTypes(currentThread, memoryThread);
		}
		else if (currentThread == null || memoryThread == null) {
			return false;
		}
		for (int i = 0; i < memoryThread.size(); ++i) {
			if (memoryThread.get(i).equals(Markers.NAME)) {
				// Special case. Only match I with I
				if (currentThread.lastElement().equalsIgnoreCase(Markers.I)) {
					if (memoryThread.lastElement().equalsIgnoreCase(Markers.I)) {
						return true;
					}
					else {
						return false;
					}
				}
				else {
					return true;
				}
			}
			else if (i >= currentThread.size()) {
				return false;
			}
			else if (memoryThread.get(i).equalsIgnoreCase(currentThread.get(i))) {
				continue;
			}
			// Mark.say(debug, "Failed to match thread", currentThread,
			// memoryThread);
			// System.out.println("Failed to match " + currentThread.get(i) +
			// " with " + memoryThread.get(i));
			return false;
		}
		return true;
	}

	/*
	 * This one is used in the vision processor. Threads match if the types in the thread in the memory matches types in
	 * the current thread unless either contains "name", in which case the match is on the elements before name.
	 */
	private boolean specialMatchTypes(Thread currentThread, Thread memoryThread) {
		if (currentThread == null || memoryThread == null) {
			return false;
		}
		int cLocation = currentThread.indexOf("name");
		int cLimit = currentThread.size();

		int mLocation = memoryThread.indexOf("name");
		int mLimit = memoryThread.size();

		if (mLocation >= 0) {
			mLimit = mLocation;
		}
		if (cLocation >= 0) {
			cLimit = cLocation;
		}
		// Mark.say(debugSpecial, "Matching ", currentThread,
		// memoryThread, cLimit, mLimit);
		for (int i = 0; i < cLimit; ++i) {
			String aClass = currentThread.get(i);
			if (!memoryThread.contains(aClass)) {
				Mark.say(debugSpecial, "Failed to match     ", aClass, currentThread, memoryThread, cLimit, mLimit);
				return false;
			}
		}
		Mark.say(debugSpecial, "Successfully matched", currentThread, memoryThread, "ok");
		return true;
	}

	public synchronized Entity dereference(Entity thing, int start, Sequence story) {
		// No dereferences for cause? Yes, otherwise fouls up reflections with
		// actors across elements
		// if (thing.isA(Markers.CAUSE_MARKER)) {return null;}
		// if (thing.isA(Markers.CAUSE_MARKER)) {return thing;}
		Sequence shortStory = new Sequence();
		Sequence completeStory = new Sequence();
		Vector<Entity> elements = story.getElements();
		int storySize = elements.size();
		for (int i = 0; i < storySize; ++i) {
			completeStory.addElement((Entity) (elements.get(i)));
			if (i >= start) {
				shortStory.addElement((Entity) (elements.get(i)));
			}
		}
		boolean old = true;
		if (old) {
			return Substitutor.dereferenceElement(thing, shortStory, completeStory);
		}
		else {
			Entity dereference = quickDereference(thing, shortStory);
			// if (dereference == thing) {
			// Mark.say("Dereferenced", thing.asString(), "--> same");
			// }
			// else {
			// Mark.say("Dereferenced", thing.asString(), "-->",
			// dereference.asString());
			// }
			return dereference;
		}
	}

	public Entity quickDereference(Entity thing, Sequence shortStory) {
		for (Entity element : shortStory.getElements()) {
			if (quickMatch(thing, element)) {
				return element;
			}
		}
		return thing;
	}

	public boolean quickMatch(Entity newThing, Entity oldThing) {
		if (newThing.entityP() && oldThing.entityP()) {
			return newThing == oldThing;
		}
		else if (newThing.functionP() && oldThing.functionP() && Substitutor.matchTypesAndSign(newThing, oldThing)) {
			return quickMatch(newThing.getSubject(), oldThing.getSubject());
		}
		else if (newThing.relationP() && oldThing.relationP() && Substitutor.matchTypesAndSign(newThing, oldThing)) {
			return quickMatch(newThing.getSubject(), oldThing.getSubject()) && quickMatch(newThing.getObject(), oldThing.getObject());
		}
		else if (newThing.sequenceP() && oldThing.sequenceP() && Substitutor.matchTypesAndSign(newThing, oldThing)) {
			Collection<Entity> newElements = newThing.getElements();
			Collection<Entity> oldElements = newThing.getElements();
			if (newElements.size() != oldElements.size()) {
				return false;
			}
			for (Entity newElement : newElements) {
				boolean result = false;
				for (Entity oldElement : oldElements) {
					if (quickMatch(newElement, oldElement)) {
						result = true;
						break;
					}
				}
				if (!result) {
					return false;
				}

			}
			return true;
		}
		return false;
	}

	public boolean alreadyExploited(Entity rule, Sequence story) {
		for (Entity t : story.getElements()) {
			if (specialRuleMatch(rule, t)) {
				Mark.say(debugAlreadyDone, "Story already contains", t.asString());
				Mark.say(debugAlreadyDone, "a varient of          ", rule.asString());
				return true;
			}
			if (t.isA("inference")) {
				Mark.say(debugAlreadyDone, "No match of", rule.asString());
				Mark.say(debugAlreadyDone, "to         ", t.asString());
			}
		}

		Mark.say(debugAlreadyDone, "No such rule already used", rule.asString());
		return false;
	}

	public synchronized boolean identicalElementIsPresent(Entity target, Sequence story) {
		return identicalElementIsPresent(target, 0, story);
	}

	public synchronized boolean identicalElementIsPresent(Entity target, int start, Sequence story) {
		Vector<Entity> elements = story.getElements();
		int index = elements.indexOf(target);
		if (index >= 0) {
			return true;
		}
		return false;
	}

	/*
	 * This matcher is used only to determine if a rule has been used already
	 */
	private boolean specialRuleMatch(Entity rule, Entity element) {
		if (!matchTypes(element, rule, false)) {
			return false;
		}
		else if (rule.entityP() && element.entityP()) {
			// This is the special place. Cannot just match. Must be same.
			return rule == element;
		}
		else if (rule.functionP() && element.functionP()) {
			Function p = (Function) rule;
			Function d = (Function) element;
			return specialRuleMatch(p.getSubject(), d.getSubject());
		}
		else if (rule.relationP() && element.relationP()) {
			Relation p = (Relation) rule;
			Relation d = (Relation) element;
			return specialRuleMatch(p.getSubject(), d.getSubject()) && specialRuleMatch(p.getObject(), d.getObject());
		}
		else if (rule.sequenceP() && element.sequenceP()) {
			// Now the hard part, sequences
			// Assumes only one element in sequence of each type
			Vector<Entity> ruleElements = ((Sequence) rule).getElements();
			Vector<Entity> elementElements = ((Sequence) element).getElements();
			if (ruleElements.size() != elementElements.size()) {
				return false;
			}
			for (int i = 0; i < ruleElements.size(); ++i) {
				if (!specialRuleMatch(ruleElements.get(i), elementElements.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public double distance(Entity imaginedDescription, Entity rememberedDescription) {
		LList<PairOfEntities> bindings = matchAll(imaginedDescription, rememberedDescription);
		if (bindings == null) {
			Mark.say("No match at all between", imaginedDescription.asString(), rememberedDescription.asString());
			return 0.0;
		}
		double result = 0;
		for (PairOfEntities pair : bindings) {
			result += intersectTypes(pair.getPattern().getBundle(), pair.getDatum().getBundle());
		}
		// Mark.say("Bindings:", bindings, result);
		return result;
	}

	public double intersectTypes(Bundle b1, Bundle b2) {
		double result = 0.0;
		for (Thread t1 : b1) {
			for (Thread t2 : b2) {
				result = Math.max(result, intersectTypes(t1, t2));
			}
		}
		return result;
	}

	public double intersectTypes(Thread t1, Thread t2) {
		int l1 = t1.size();
		int l2 = t2.size();
		int count;
		for (count = 0; count < Math.min(l1, l2); ++count) {
			if (t1.get(count).equalsIgnoreCase(t2.get(count))) {
				continue;
			}
			break;
		}
		return Math.pow(count, 2) / (l1 * l2);
	}

	public Entity instantiate(Entity t, LList<PairOfEntities> bindings) {
		// Uses legacy instantiator from another class
		return Matcher.instantiate(t, bindings);
	}

	public static void main(String[] ignore) throws Exception {

		// Set up match demonstration using START. Compound sentences used
		// because otherwise START would not know that xx in xx is a person is
		// the same xx as in xx loves yy. Also, xx is used, not x, because x has
		// special meaning to START.

		BasicMatcherOriginal matcher = BasicMatcherOriginal.getBasicMatcher();
		// // Create pattern and sample sentences. Translation produces a
		// sequence
		// // of elements; desired relation is final element, #2.
		// Thing R1 =
		// Translator.getTranslator().translate("xx is a person and yy is a person and xx loves
		// yy").getElements().get(2);
		// Thing R2 =
		// Translator.getTranslator().translate("John is a person and Mary is a person and John loves
		// Mary").getElements().get(2);
		// Thing R3 =
		// Translator.getTranslator().translate("Patrick is a person and Karen is a person and Patrick helps
		// Karen").getElements().get(2);
		// Thing R4 =
		// Translator.getTranslator().translate("Peter is a man and Sally is a woman and Peter loves
		// Sally").getElements().get(2);
		//
		// // Look to be sure you know what is what. asString used to print out
		// // objects n compact form.
		// Mark.say("R1", R1.asString());
		// Mark.say("R2", R2.asString());
		// Mark.say("R3", R3.asString());
		// Mark.say("R4", R4.asString());
		//
		// // Straightforward. John and XX and Mary and YY are same type.
		// Mark.say("Match pattern against John loves Mary ",
		// matcher.match(R1, R2));
		// // No match, wrong relation.
		// Mark.say("Match pattern against Patrick helps Karen ",
		// matcher.match(R1, R3));
		// // Works fine. A man is a kind of person and only a person is
		// required
		// // in the match. Ditto woman.
		// Mark.say("Match pattern against Peter, a man, loves Sally, a woman",
		// matcher.match(R1, R4));
		// // Prepare bindings to be used to instantiate a pattern, as in the
		// // consequent of a rule.
		// LList<PairOfEntities> bindings = matcher.match(R1, R2);
		// Mark.say("Bindings: ",
		// bindings);
		// // Have to manufacture pattern to instantiate, as it will not have
		// the
		// // same xx and yy if goes through start
		// Relation R5 = new Relation("adore", R1.getSubject(), R1.getObject());
		// Mark.say("R5 ",
		// R5.asString());
		// Mark.say("Instantiate xx adores yy with previous bindings ",
		// matcher.instantiate(R5, bindings).asString());
		Translator basicTranslator = Translator.getTranslator();
		Entity t1 = basicTranslator.translate("John is glad that Mary is happy");
		Entity t2 = basicTranslator.translate("Mary is happy").getElements().get(0);
		Entity t3 = basicTranslator.translate("A dog is angry that a cat is miserable");

		Mark.say("T1:", t1.asString());
		Mark.say("T2:", t2.asString());
		Mark.say("T3:", t3.asString());

		t1 = basicTranslator.translate("A human entered from the left");
		t2 = basicTranslator.translate("A human entered from the right");

		Mark.say("Match top level:", matcher.match(t2, t1));
		Mark.say("Match any part:", matcher.matchAnyPart(t2, t1));
		// Mark.say("Match:", matcher.match(t1, t3));
		// Mark.say("Match structure:", matcher.matchStructures(t1, t3));
		Mark.say("Match structure:", matcher.matchStructures(t1, t2));

	}

}

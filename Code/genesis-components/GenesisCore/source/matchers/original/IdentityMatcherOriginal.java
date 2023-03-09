package matchers.original;

import translator.NewRuleSet;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * Created on May 2, 2009
 * @author phw
 */

public class IdentityMatcherOriginal {

	private static boolean debug = false;

	private static IdentityMatcherOriginal identityMatcher;

	private boolean requireSameNames = false;

	public boolean isRequireSameNames() {
		return requireSameNames;
	}

	public void setRequireSameNames(boolean requireSameNames) {
		this.requireSameNames = requireSameNames;
	}

	public LList<PairOfEntities> match(Entity current, Entity remembered) {
		// ArrayList<Pair> matches = new ArrayList<Pair>();
		LList<PairOfEntities> matches = new LList<PairOfEntities>();
		LList<PairOfEntities> result = match(current, remembered, matches);
		return result;
	}

	public LList<PairOfEntities> matchNegation(Entity current, Entity remembered, LList<PairOfEntities> matches) {
		return match(current, remembered, matches, true);
	}

	public LList<PairOfEntities> match(Entity current, Entity remembered, LList<PairOfEntities> bindings) {
		return match(current, remembered, bindings, false);
	}

	public LList<PairOfEntities> match(Entity current, Entity remembered, LList<PairOfEntities> bindings, boolean invertSign) {
		Mark.say(debug, "Trying to match " + current.asString() + " with " + remembered.asString());
		LList<PairOfEntities> result = matchAux(current, remembered, bindings, invertSign);
		if (result != null) {
			Mark.say(debug, "Cause matcher matched " + current.asString() + " with " + remembered.asString() + " yielding " + result);
		}
		return result;
	}

	/*
	 * Only work with sign inverstion at the top level
	 */
	public LList<PairOfEntities> matchAux(Entity current, Entity remembered, LList<PairOfEntities> matches) {
		return matchAux(current, remembered, matches, false);
	}

	// private boolean xor(boolean x, boolean y) {
	// return (x && y) || (!x && !y);
	// }

	/*
	 * On the top level, be willing to invert sign
	 */
	public LList<PairOfEntities> matchAux(Entity datum, Entity pattern, LList<PairOfEntities> matches, boolean invertSign) {
		Mark.say(debug, "Attempting to match with sign " + invertSign, "\n", datum.asString(), pattern.asString());
		if (matches == null) {
			Mark.say(debug, "Failed at A");
			return null;
		}
		// Special case for actions that appear
		else if (pattern.functionP() && pattern.isA(Markers.APPEAR_MARKER) && pattern.getSubject().getType().equals("action")
		        && datum.isAPrimed("action")) {
			Mark.say(debug, "Special case trap for actions embedded in appear");
			boolean hasNotFeature = pattern.hasFeature(Markers.NOT);
			Entity subject = pattern.getSubject();
			if (invertSign && hasNotFeature) {
				// Evidently, this was a top-level antecedent to a rule,
				// and at this point we are looking for a match-stopping
				// positive occurrence
				return matchAux(datum, subject, matches, false);
			}
			else if (hasNotFeature) {
				// Not a top-level antecedent to a rule, but embedded in a
				// negative, so look for element of opposite sign
				return matchAux(datum, subject, matches, true);
			}
			else if (true) {
				// Merely embedded, no use of opposite sign
				return matchAux(datum, subject, matches, false);
			}
		}
		else if (!matchTypes(datum, pattern, invertSign)) {
			Mark.say(debug, "Failed at D");
			return null;
		}
		else if (datum.isAPrimed("action") && pattern.entityP() && pattern.isAPrimed("action")) {
			Mark.say(debug, "X.0: Looking for match of " + pattern.asString());
			Entity match = getMatchForRemembered(pattern, matches);
			if (match == null) {
				matches = matches.cons(new PairOfEntities(pattern, datum));
				// matches.add(new Pair(remembered, current));
				Mark.say(debug, "X.1: Remembering match of " + datum.asString() + " with " + pattern.asString());
				Mark.say(debug, "New bindings: " + matches);
				return matches;
			}
			else if (match == datum) {
				Mark.say(debug, "X.2: Old match of " + datum.asString() + " with " + pattern.asString());
				return matches;
			}
			Mark.say(debug, "Failed at B to match " + match.getName() + " " + datum.getName());
			return null;

		}
		else if (datum.getClass() != pattern.getClass()) {
			Mark.say(debug, "Failed at C to match " + datum.getClass() + ", " + pattern.getClass());
			return null;
		}
		else if (datum.entityP() && pattern.entityP()) {
			Entity match = getMatchForRemembered(pattern, matches);
			if (match == null) {
				matches = matches.cons(new PairOfEntities(pattern, datum));
				Mark.say(debug, "Succeded at C.1, " + matches);
				return matches;
			}
			else if (match == datum) {
				Mark.say(debug, "Succeded at C.2, " + matches);
				return matches;
			}
			// Not at all clear what this is for. Possibly bug causer.
			else if (match.getType().equals(datum.getType())) {
				Mark.say(debug, "Succeded at C.3, " + matches);
				return matches;
			}
			Mark.say(debug, "Failed at C.4  to match " + match.getName() + " " + datum.getName());
			return null;
		}
		else if (datum.functionP() && pattern.functionP()) {
			Function p = (Function) datum;
			Function d = (Function) pattern;
			LList<PairOfEntities> result = matchAux(p.getSubject(), d.getSubject(), matches);
			if (result == null) {
				Mark.say(debug, "Failed at E");
			}
			return result;
		}
		else if (datum.relationP() && pattern.relationP()) {
			Relation p = (Relation) datum;
			Relation d = (Relation) pattern;
			LList<PairOfEntities> subjectList = matchAux(p.getSubject(), d.getSubject(), matches);
			if (subjectList == null) {
				Mark.say(debug, "Failed at F");
				return null;
			}
			else {
				LList<PairOfEntities> objectList = matchAux(p.getObject(), d.getObject(), subjectList);
				if (objectList == null) {
					Mark.say(debug, "Failed at G");
					return null;
				}
				else {
					return objectList;
				}
			}
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (datum.sequenceP() && pattern.sequenceP()) {
			Mark.say(debug, "Starting sequence match", datum.asString(), pattern.asString());
			for (Entity patternElement : ((Sequence) datum).getElements()) {
				// Look for element of matching type
				String patternType = patternElement.getType();
				boolean found = false;
				for (Entity datumElement : ((Sequence) pattern).getElements()) {
					Mark.say(debug, "Looking for match between", patternType, "and", datumElement.getType());
					if (matchTypes(patternElement, datumElement, false)) {
						LList<PairOfEntities> matchList = matchAux(patternElement, datumElement, matches);
						if (matchList == null) {
							Mark.say(debug, "Failed at H in sequence matching--matching sequence types do not match for", patternType);
							// Have to keep going, may be multiple elements of
							// same type
							// return null;
							continue;
						}
						else {
							found = true;
							matches = matchList;
							break;
						}
					}
				}
				if (!found) {
					Mark.say(debug, "Failed at I in sequence matching--no matching type for", patternType);
					return null;
				}
			}
		}
		return matches;
	}

	private Entity getMatchForRemembered(Entity thing, LList<PairOfEntities> matches) {
		Mark.say(debug, "Looking for remembered match for " + thing.getName());

		for (Object o : matches) {
			PairOfEntities pairOfThings = (PairOfEntities) o;
			if (pairOfThings.getPattern() == thing) {
				Mark.say(debug, "Found " + pairOfThings.getDatum().getName() + "\n\n");
				return pairOfThings.getDatum();
			}
		}
		Mark.say(debug, "None found");
		return null;

	}

	/*
	 * Complicated, because may have to match a thread from the past.
	 */
	private boolean matchTypes(Entity currentThing, Entity memoryThing, boolean invertSign) {
		boolean t1Not = currentThing.hasFeature(Markers.NOT);
		boolean t2Not = memoryThing.hasFeature(Markers.NOT);
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
		if (currentThing.isAPrimed("action") && memoryThing.getType().equals("action")) {
			return true;
		}
		// General case, make sure threads are compatible
		if (matchTypes(getRegularThread(currentThing), getRegularThread(memoryThing))) {
			return true;
		}
		// Special case, I hope temporary
		else if (getRegularThread(currentThing).contains("killing") && getRegularThread(memoryThing).contains("killing")) {
			return true;
		}
		// preposition case
		else if (isPreposition(currentThing.getType()) && currentThing.getType().equals(memoryThing.getType())) {
			return true;
		}
		else {
			for (Thread currentThread : currentThing.getBundle()) {
				if (currentThread.contains(Entity.MARKER_FEATURE)) {
					continue;
				}
				for (Thread memoryThread : memoryThing.getBundle()) {
					if (memoryThread.contains(Entity.MARKER_FEATURE)) {
						continue;
					}
					if (matchTypes(currentThread, memoryThread)) {
						Mark.say(debug, "Matched " + currentThread + ", " + memoryThread);
						Mark.say(debug, "... but could not match " + getRegularThread(currentThing) + ", " + getRegularThread(memoryThing));
						return false;
					}
					else {
						// Mark.say(debugTypeMatch, "No match " + currentThread
						// + ", " + memoryThread);
					}
				}
			}
		}
		return false;
	}

	private Thread getRegularThread(Entity t) {
		for (Thread currentThread : t.getBundle()) {
			if (currentThread.contains(Entity.MARKER_FEATURE)) {
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
	 * Work down the thread. As long as classes are the same, continue. If NAME
	 * is encountered, threads must be identical. If memory thread becomes
	 * longer, lose. If they don't match, lose. If you get all the way to the
	 * end of both, win.
	 */
	private boolean matchTypes(Thread currentThread, Thread memoryThread) {
		if (currentThread == null || memoryThread == null) {
			return false;
		}
		if (isRequireSameNames() && (currentThread.contains(Markers.NAME) || memoryThread.contains(Markers.NAME))) {
			Mark.say("Testing", currentThread.getType(), memoryThread.getThreadType());
			if (currentThread.getType().equalsIgnoreCase(memoryThread.getType())) {
				Mark.say("Matched", currentThread.getType(), memoryThread.getThreadType());
				return true;
			}
			return false;
		}
		for (int i = 0; i < currentThread.size(); ++i) {
			if (i >= memoryThread.size()) {
				return false;
			}
			else if (memoryThread.get(i).equalsIgnoreCase(currentThread.get(i))) {
				continue;
			}
			// System.out.println("Failed to match " + currentThread.get(i) +
			// " with " + memoryThread.get(i));
			return false;
		}
		return true;
	}

	public static void main(String[] ignore) {
		Entity t1 = new Entity("foo");
		t1.addType("name");
		t1.addType("john");
		Entity t2 = new Entity("foo");
		t2.addType("name");
		t2.addType("paul");
		Function d1 = new Function("d", t1);
		Function d2 = new Function("d", t2);
		IdentityMatcherOriginal identityMatcher = new IdentityMatcherOriginal();
		identityMatcher.setRequireSameNames(true);
		System.out.println("Match: " + identityMatcher.match(d1, d2));
		System.out.println("Match: " + identityMatcher.match(t1, t2));
	}
	
	private IdentityMatcherOriginal() {
		
	}

	public static IdentityMatcherOriginal getIdentityMatcher() {
	    if (identityMatcher == null) {
	    	identityMatcher = new IdentityMatcherOriginal();
	    }
	    return identityMatcher;
    }

}

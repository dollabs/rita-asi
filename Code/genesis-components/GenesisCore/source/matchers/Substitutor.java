package matchers;

import java.util.*;

import start.Start;
import translator.BasicTranslator;
import utils.*;
import utils.NewTimer;
import utils.minilisp.LList;
import constants.Markers;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/**
 * See DemoMatcherAndSubstitutor for examples.
 * <p>
 * Created on Mar 3, 2009
 * 
 * @author phw
 */

public class Substitutor {

	private static boolean debug = false;

	public static Entity substitute(Entity current, Entity remembered, Entity consequence) {
		Mark.say(debug, "Substituting!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Mark.say(debug, "Current\n" + current);
		Mark.say(debug, "History\n" + remembered);
		Mark.say(debug, "Consequence\n" + consequence);
		LList<PairOfEntities> matches = new LList<PairOfEntities>();
		matches = findMatches(current, remembered, matches);
		if (matches == null) {
			return null;
		}
		Mark.say(debug, "Matches\n" + matches);
		Entity result = substitute(consequence, matches);
		Mark.say(debug, "Result\n" + result);
		Mark.say(debug, "Now consequence is\n" + consequence);
		return result;
	}

	private static LList<PairOfEntities> findMatches(Entity current, Entity remembered) {
		LList<PairOfEntities> matches = new LList<PairOfEntities>();
		return findMatches(current, remembered, matches);
	}

	private static LList<PairOfEntities> findMatches(Entity datum, Entity pattern, LList<PairOfEntities> matches) {
		if (datum.entityP() && pattern.entityP()) {
			Entity match = getMatch(datum, matches);
			if (match == null) {
				matches = matches.cons(new PairOfEntities(pattern, datum));
				return matches;
			}
			else if (match == pattern) {
				return matches;
			}
			return null;
		}
		else if (datum.functionP() && pattern.functionP()) {
			if (!matchTypes(datum, pattern)) {
				return null;
			}
			Function p = (Function) datum;
			Function d = (Function) pattern;
			if (findMatches(p.getSubject(), d.getSubject(), matches) == null) {
				return null;
			}
		}
		else if (datum.relationP() && pattern.relationP()) {
			if (!matchTypes(datum, pattern)) {
				return null;
			}
			Relation p = (Relation) datum;
			Relation d = (Relation) pattern;
			if ((findMatches(p.getSubject(), d.getSubject(), matches) == null || findMatches(p.getObject(), d.getObject(), matches) == null)) {
				return null;
			}
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (datum.sequenceP() && pattern.sequenceP()) {
			Mark.say("Trying to match", datum.asStringWithIndexes(), "\n", pattern.asStringWithIndexes());
			for (Entity patternElement : ((Sequence) datum).getElements()) {
				// Look for element of matching type
				for (Entity datumElement : ((Sequence) pattern).getElements()) {
					if (matchTypes(patternElement, datumElement)) {
						if (findMatches(patternElement, datumElement, matches) == null) {
							Mark.say("Failed");
							return null;
						}
						Mark.say("Won");
						break;
					}

				}
			}
		}
		return matches;
	}

	public static Entity substitute(Entity pattern, LList<PairOfEntities> bindings) {
		Entity result = null;

		boolean debug = false;

		Mark.say(false, "Substitutor bindings", bindings);

		if (pattern.entityP()) {

			Entity match = getReverseMatch(pattern, bindings);
			if (match != null) {
				Mark.say(debug, "Substituting", match, "for", pattern);
				result = match;

			}
			else {
				Mark.say(debug, "No substitution for", pattern);

				// Modified 17 Nov 2017 to handle owner bindings.
				// Revised 20 Jun 2018 to ensure there is a new entity
				// so as not to reuse original one over and over

				Object owner = pattern.getProperty(Markers.OWNER_MARKER);
				if (owner != null) {
					Mark.say(debug, "Encountered owner of", pattern, "namely", owner);
					Entity ownerMatch = getReverseMatch((Entity) owner, bindings);
					if (ownerMatch != null) {
						Mark.say(debug, "\n>>> Which matches", ownerMatch);
						// So put it in
						Mark.say(debug, "So adding", ownerMatch);
						Entity newPattern = Start.makeThing(pattern.getType());
						newPattern.addProperty(Markers.OWNER_MARKER, ownerMatch);
						pattern = newPattern;
					}
				}
				result = pattern;
			}

		}
		else if (pattern.functionP()) {
			result = new Function(pattern.getBundle().copy(), substitute(((Function) pattern).getSubject(), bindings));
		}
		else if (pattern.relationP()) {
			result = new Relation(pattern.getBundle().copy(), substitute(((Relation) pattern).getSubject(), bindings),
			        substitute(((Relation) pattern).getObject(), bindings));
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (pattern.sequenceP()) {
			Sequence sequence = new Sequence(pattern.getBundle().copy());
			for (Entity element : ((Sequence) pattern).getElements()) {
				sequence.addElement(substitute(element, bindings));
			}
			result = sequence;
		}
		if (pattern.hasFeature(Markers.NOT)) {
			result.addFeature(Markers.NOT);
		}
		return result;
	}

	public static Entity replaceWithDereference(Entity pattern, LList<PairOfEntities> bindings, Sequence story) {
		if (pattern.entityP()) {
			Entity match = getReverseMatch(pattern, bindings);
			if (match != null) {
				return match;
			}
			// makes an attempt to dereference the actor
			match = dereferenceActor(pattern, story);
			if (match != null) {
				return match;
			}
			return pattern;
		}
		else if (pattern.functionP()) {
			return new Function(pattern.getBundle().copy(), replaceWithDereference(((Function) pattern).getSubject(), bindings, story));
		}
		else if (pattern.relationP()) {
			return new Relation(pattern.getBundle().copy(), replaceWithDereference(((Relation) pattern).getSubject(), bindings, story),
			        replaceWithDereference(((Relation) pattern).getObject(), bindings, story));
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (pattern.sequenceP()) {
			Mark.say(debug, "Sequence to dereference:", pattern.asString());
			Sequence result = new Sequence(pattern.getBundle().copy());
			for (Entity element : ((Sequence) pattern).getElements()) {
				result.addElement(replaceWithDereference(element, bindings, story));
			}
			Mark.say(debug, "Bindngs:                ", bindings);
			Mark.say(debug, "Dereferenced sequence:  ", result.asString());
			return result;
		}
		return null;
	}

	public static Entity dereferenceActor(Entity actor, Sequence story) {
		Collection<Entity> deconstructedStory = deconstruct(story);
		if (!actor.entityP("name")) {
			return null;
		}
		for (Entity t : deconstructedStory) {
			if (!t.entityP("name")) {
				continue;
			}

			if (t.getType().equalsIgnoreCase(actor.getType())) {
				return t;
			}
		}
		return null;
	}

	private static boolean matchTypes(Entity thing, Entity base) {
		return base.isA(thing.getType());
	}

	private static Entity getMatch(Entity thing, LList<PairOfEntities> matches) {
		for (Object object : matches) {
			PairOfEntities pairOfThings = (PairOfEntities) object;
			if (pairOfThings.getDatum() == thing) {
				return pairOfThings.getPattern();
			}
		}
		return null;
	}

	public static LList<PairOfEntities> invert(LList<PairOfEntities> input) {
		LList<PairOfEntities> output = new LList<>();
		for (PairOfEntities p : input) {
			Mark.say("Inverting", p);
			output = output.cons(p.reverse());
		}
		return output;
	}

	/*
	 * Should be identity match, rather than type match, but memory does not use same objects!
	 */
	private static Entity getReverseMatch(Entity thing, LList<PairOfEntities> matches) {
		for (Object object : matches) {
			PairOfEntities pairOfThings = (PairOfEntities) object;
			// if (pairOfThings.getPattern().getType().equals(thing.getType())) {
			// return pairOfThings.getDatum();
			// }
			if (pairOfThings.getPattern() == thing) {
				return pairOfThings.getDatum();
			}
		}
		return null;
	}

	// public static Thing dereferenceLastElement(Sequence sequence) {
	// Vector<Thing> elements = sequence.getElements();
	// Thing last = elements.lastElement();
	// elements.removeElementAt(elements.size() - 1);
	// ArrayList<Thing> things = deconstruct(sequence);
	// Thing result = dereference(last, things);
	// elements.add(result);
	// return sequence;
	// }

	// public static Thing dereferenceElement(Thing element, Sequence story) {
	// return dereferenceElement(element, story, story);
	// }

	public static Entity dereferenceElement(Entity element, Sequence scene, Sequence story) {
		long time = System.currentTimeMillis();
		Collection<Entity> deconstructedScene = deconstruct(scene);
		Collection<Entity> deconstructedStory = deconstruct(story);
		// Timer.time(true, "Deconstruction time", "Deconstruction " +
		// deconstructedStory.size(), time);
		Entity result = dereference(element, deconstructedScene, deconstructedStory);
		// Timer.time(true, "Dereference timeasdf", "Deconstruction " +
		// deconstructedStory.size() + ", " + element.asString(), time);
		return result;
	}

	public static Collection<Entity> deconstruct(Entity current) {
		Collection<Entity> things = new HashSet<Entity>();
		deconstruct(current, things);
		return things;
	}

	private static synchronized void addElementToMap(Entity thing, Collection<Entity> things) {
		things.add(thing);
	}

	public static void deconstruct(Entity current, Collection<Entity> things) {
		Mark.say(debug, "Deconstructing", current.asStringWithIndexes());
		if (current.entityP()) {
			addElementToMap(current, things);
		}
		else if (current.functionP()) {
			deconstruct(current.getSubject(), things);
			addElementToMap(current, things);
		}
		else if (current.relationP()) {
			deconstruct(current.getSubject(), things);
			deconstruct(current.getObject(), things);
			addElementToMap(current, things);
		}
		else if (current.sequenceP()) {
			// Special case for roles
			if (false && current.isA(Markers.ROLE_MARKER)) {
				for (Entity t : current.getElements()) {
					deconstruct(t.getSubject(), things);
				}
			}
			else {
				for (Entity t : current.getElements()) {
					deconstruct(t, things);
				}
			}
			addElementToMap(current, things);
		}
	}

	public static Entity dereference(Entity current) {
		return dereference(current, new ArrayList<Entity>(), new ArrayList<Entity>());
	}

	private static Bundle getBundleClone(Entity t) {
		return t.getBundle().getAllClones();
	}

	public static synchronized Entity dereference(Entity current, Collection<Entity> deconstructedScene, Collection<Entity> deconstructedStory) {
		Mark.say(debug, "\n\nLooking for dereference of " + current.asStringWithIndexes());
		if (current.relationP()) {
			Mark.say(debug, "Matching relation", current.asString());
			dereference(current.getSubject(), deconstructedScene, deconstructedStory);
			dereference(current.getObject(), deconstructedScene, deconstructedStory);
			Relation result = (Relation) match(0, current, deconstructedScene);
			if (result != null) {
				Mark.say(debug, "Found " + result.asStringWithIndexes());
				return result;
			}
			result = new Relation(getBundleClone(current), dereference(current.getSubject(), deconstructedScene, deconstructedStory),
			        dereference(current.getObject(), deconstructedScene, deconstructedStory));
			addElementToMap(result, deconstructedScene);
			addElementToMap(result, deconstructedStory);
			Mark.say(debug, "Found " + result.asStringWithIndexes());
			return result;
		}
		else if (current.functionP()) {
			Mark.say(debug, "Matching derivative", current.asString());
			dereference(current.getSubject(), deconstructedScene, deconstructedStory);
			Function result = (Function) match(0, current, deconstructedScene);
			if (result != null) {
				Mark.say(debug, "Found " + result.asStringWithIndexes());
				return result;
			}
			result = new Function(getBundleClone(current), dereference(current.getSubject(), deconstructedScene, deconstructedStory));
			addElementToMap(result, deconstructedScene);
			addElementToMap(result, deconstructedStory);
			Mark.say(debug, "Found " + result.asStringWithIndexes());
			return result;
		}
		else if (current.entityP()) {
			if (current == Markers.NULL) {
				Mark.say(debug, "Found NULL " + current.asStringWithIndexes());
				return current;
			}
			// Note here deconstruction is on story!
			Mark.say(debug, "Matching thing", current.asString());
			Entity result = match(0, current, deconstructedStory);
			if (result != null) {
				Mark.say(debug, "Found thing " + result.asStringWithIndexes());
				return result;
			}
			result = new Entity(getBundleClone(current));
			Mark.say(debug, "Created thing " + result.asStringWithIndexes());
			addElementToMap(result, deconstructedScene);
			addElementToMap(result, deconstructedStory);
			return result;
		}
		else if (current.sequenceP()) {
			Mark.say(debug, "Matching sequence", current.asStringWithIndexes());
			for (Entity t : current.getElements()) {
				dereference(t, deconstructedScene, deconstructedStory);
			}
			Sequence result = (Sequence) match(0, current, deconstructedStory);
			if (result != null) {
				Mark.say(debug, "Found " + result.asStringWithIndexes());
				return result;
			}
			else {
				result = new Sequence(getBundleClone(current));
				for (Entity t : current.getElements()) {
					result.addElement(dereference(t, deconstructedScene, deconstructedStory));
				}
				Mark.say(debug, "Constructed " + result.asStringWithIndexes());

				addElementToMap(result, deconstructedScene);
				addElementToMap(result, deconstructedStory);

				return result;
			}
		}
		return current;
	}

	public static boolean matchTypesAndSign(Entity t1, Entity t2) {
		try {
			if (!t1.getType().equalsIgnoreCase(t2.getType())) {
				return false;
			}
			boolean t1Not = t1.hasFeature(Markers.NOT);
			boolean t2Not = t2.hasFeature(Markers.NOT);
			if (t1Not != t2Not) {
				return false;
			}
		}
		catch (Exception e) {
			System.err.println("Blew out in matchTypesAndSign with " + t1 + ", " + t2);
			e.printStackTrace();
		}
		return true;
	}

	// private static Thing match(int level, Thing thing, Collection<Thing>
	// candidates) {
	// if (thing == null) {
	// // Drip pan
	// return null;
	// }
	// else if (level > 5) {
	// Mark.say("Recursing through", level, thing.asString());
	// }
	// if (level > 15) {
	// Mark.say("Appear to have recursing through", level, thing.asString());
	// // for (Thing t : candidates) {
	// // Mark.say("Thing:", t.asString());
	// // }
	// throw new RuntimeException("Ugh!");
	// }
	// else if (level > 20) {
	// Mark.say("Quiting", thing.asString());
	// return null;
	// }
	// for (Thing candidate : candidates) {
	// if (candidate == null) {
	// // Another drip pan.
	// continue;
	// }
	// Mark.say("Working to match", thing.asString(), "|",
	// candidate.asString());
	// if (thing.relationP() && candidate.relationP()) {
	// if (!matchTypesAndSign(thing, candidate)) {
	// continue;
	// }
	// Thing subjectT = match(level + 1, thing.getSubject(), candidates);
	// Thing objectT = match(level + 1, thing.getObject(), candidates);
	// Thing subjectC = match(level + 1, candidate.getSubject(), candidates);
	// Thing objectE = match(level + 1, candidate.getObject(), candidates);
	// if (subjectT != null && subjectT == subjectC && objectT != null &&
	// objectT == objectE) {
	// return candidate;
	// }
	// }
	// else if (thing.functionP() && candidate.functionP()) {
	// if (!matchTypesAndSign(thing, candidate)) {
	// continue;
	// }
	// Thing subjectT = match(level + 1, thing.getSubject(), candidates);
	// Thing subjectC = match(level + 1, candidate.getSubject(), candidates);
	// if (subjectT != null && subjectT == subjectC) {
	// return candidate;
	// }
	// }
	// else if (thing.entityP() && candidate.entityP()) {
	// if (!matchTypesAndSign(thing, candidate)) {
	// continue;
	// }
	// if (thing == candidate) {
	// return candidate;
	// }
	// // Don't merge if owners different; don't merge if features
	// // inconsistent
	// else if (sameOwners(thing, candidate) && consistenFeatures(thing,
	// candidate)) {
	// return candidate;
	// }
	// }
	//
	// // Match if all elements in first sequence match an element in
	// // second sequence;
	// // I suppose this allows for short sequences to match longer, which
	// // is natural in English.
	// // Ugh, but creates a problem because one element of an antecedant
	// // list can cause the whole antecedant list to match,
	// // so don't do it!
	// else if (thing.sequenceP() && candidate.sequenceP()) {
	// if (!matchTypesAndSign(thing, candidate)) {
	// continue;
	// }
	// // Mark.say(debug, "Looking for match of sequence " +
	// // thing.asString() + " against " + element.asString());
	// Vector<Thing> thingElements = ((Sequence) thing).getElements();
	// Vector<Thing> candidateElements = ((Sequence) candidate).getElements();
	//
	// // Bad news if not same size
	// if (thingElements.size() != candidateElements.size()) {
	// continue;
	// }
	//
	// boolean ok = true;
	//
	// for (Thing t : thingElements) {
	// Thing match = match(level + 1, t, candidates);
	// if (match == null || !candidateElements.contains(match)) {
	// ok = false;
	// break;
	// }
	// }
	// if (ok) {
	// return candidate;
	// }
	// }
	// }
	// return null;
	// }

	private static Entity match(int level, Entity thing, Collection<Entity> candidates) {
		if (thing == null) {
			// Drip pan
			return null;
		}
		else if (level > 10) {
			Mark.say("Recursing through", level, thing.asString());
		}
		if (level > 15) {
			Mark.say("Appear to have recursed through", level, thing.asString());
			throw new RuntimeException("Ugh!");
		}
		if (level > 40) {
			Mark.say("Quiting", thing.asString());
			return null;
		}
		for (Entity candidate : candidates) {
			if (candidate == null) {
				// Another drip pan.
				continue;
			}
			Mark.say(debug, "Working to match", thing.asString(), "|", candidate.asString());
			if (thing.relationP() && candidate.relationP()) {
				if (!matchTypesAndSign(thing, candidate)) {
					continue;
				}
				Entity subjectT = match(level + 1, thing.getSubject(), candidates);
				Entity subjectC = candidate.getSubject();
				if (subjectT != subjectC) {
					continue;
				}
				Entity objectT = match(level + 1, thing.getObject(), candidates);
				Entity objectC = candidate.getObject();
				if (objectT == objectC) {
					return candidate;
				}
			}
			else if (thing.functionP() && candidate.functionP()) {
				if (!matchTypesAndSign(thing, candidate)) {
					continue;
				}
				Entity subjectT = match(level + 1, thing.getSubject(), candidates);
				Entity subjectC = candidate.getSubject();
				if (subjectT != null && subjectT == subjectC) {
					return candidate;
				}
			}
			else if (thing.entityP() && candidate.entityP()) {
				if (!matchTypesAndSign(thing, candidate)) {
					continue;
				}
				if (thing == candidate) {
					return candidate;
				}
				// Don't merge if owners different; don't merge if features
				// inconsistent
				else if (sameOwners(thing, candidate) && consistenFeatures(thing, candidate)) {
					return candidate;
				}
			}

			// Match if all elements in first sequence match an element in
			// second sequence;
			// I suppose this allows for short sequences to match longer, which
			// is natural in English.
			// Ugh, but creates a problem because one element of an antecedant
			// list can cause the whole antecedant list to match,
			// so don't do it!
			else if (thing.sequenceP() && candidate.sequenceP()) {
				NewTimer.matcherTimer.reset();
				if (!matchTypesAndSign(thing, candidate)) {
					continue;
				}
				// Mark.say(debug, "Looking for match of sequence " +
				// thing.asString() + " against " + element.asString());
				Vector<Entity> thingElements = ((Sequence) thing).getElements();
				Vector<Entity> candidateElements = ((Sequence) candidate).getElements();

				// Bad news if not same size
				if (thingElements.size() != candidateElements.size()) {
					continue;
				}

				// Special speed-up for role sequences
				// Do not use this. Breaks everything
				// if (thing.isA(Markers.ROLE_MARKER) && candidate.isA(Markers.ROLE_MARKER)) {
				// return matchRoles(level, candidate, thingElements, candidateElements, candidates);
				// }
				// else if (thing.isA(Markers.ROLE_MARKER) || candidate.isA(Markers.ROLE_MARKER)) {
				// continue;
				// }

				boolean ok = true;

				for (Entity t : thingElements) {
					Entity match = match(level + 1, t, candidates);
					if (match == null || !candidateElements.contains(match)) {
						ok = false;
						break;
					}
				}
				NewTimer.matcherTimer.report(false);

				if (ok) {
					return candidate;
				}
			}
		}
		return null;
	}

	private static Entity matchRoles(int level, Entity result, Vector<Entity> thingElements, Vector<Entity> candidateElements, Collection<Entity> candidates) {
		for (Entity element : thingElements) {
			boolean match = false;
			for (Entity candidate : candidateElements) {
				// Assume only one with given role marker for now
				if (element.getType().equals(candidate.getType())) {
					if (match(level + 1, element, candidates) == candidate) {
						// Ok, this one matches;
						match = true;
						continue;
					}
				}
			}
			if (!match) {
				return null;
			}
		}
		return result;
	}

	private static boolean consistenFeatures(Entity thing, Entity element) {
		Vector<String> thingFeatures = thing.getThread(Entity.MARKER_FEATURE);
		Vector<String> elementFeatures = element.getThread(Entity.MARKER_FEATURE);
		if (thingFeatures == null && elementFeatures == null) {
			return true;
		}
		else if (thingFeatures != null && elementFeatures == null) {
			return false;
		}
		else if (thingFeatures == null && elementFeatures != null) {
			return false;
		}
		Vector<String> larger = thingFeatures;
		Vector<String> smaller = elementFeatures;
		if (larger.size() < smaller.size()) {
			larger = elementFeatures;
			smaller = thingFeatures;
		}
		for (String x : smaller) {
			if (!larger.contains(x)) {
				return false;
			}
		}
		return true;
	}

	private static boolean sameOwners(Entity thing, Entity element) {
		// Tempporary, for debugging.
		// if (true) {return true;}
		Vector<String> thingOwners = thing.getThread(Entity.MARKER_OWNERS);
		Vector<String> elementOwners = element.getThread(Entity.MARKER_OWNERS);
		if (thingOwners == null && elementOwners == null) {
			return true;
		}
		else if (thingOwners != null && elementOwners == null) {
			return false;
		}
		else if (thingOwners == null && elementOwners != null) {
			return false;
		}
		else if (thingOwners.size() != elementOwners.size()) {
			return false;
		}
		for (String owner : thingOwners) {
			if (!elementOwners.contains(owner)) {
				return false;
			}
		}
		return true;
	}

	private static ArrayList<Entity> extractThings(Entity current, ArrayList<Entity> things) {
		if (current.entityP()) {
			if (!things.contains(current)) {
				things.add(current);
			}
			return things;
		}
		else if (current.functionP()) {
			Function p = (Function) current;
			return extractThings(p.getSubject(), things);
		}
		else if (current.relationP()) {
			Relation p = (Relation) current;
			extractThings(p.getSubject(), things);
			extractThings(p.getObject(), things);
			return things;
		}
		else if (current.sequenceP()) {
			for (Entity patternElement : ((Sequence) current).getElements()) {
				extractThings(patternElement, things);
			}
			return things;
		}
		return things;
	}

	private static LList<PairOfEntities> extractPairs(Entity pattern, ArrayList<Entity> things, LList<PairOfEntities> pairOfThings) {
		if (pattern.entityP()) {
			if (getReverseMatch(pattern, pairOfThings) == null) {
				Entity datum = findMatchingThing(pattern, things);
				if (datum != null) {
					things.remove(datum);
					pairOfThings = pairOfThings.cons(new PairOfEntities(pattern, datum), pairOfThings);
				}
			}
			return pairOfThings;
		}
		else if (pattern.functionP()) {
			Function p = (Function) pattern;
			return extractPairs(p.getSubject(), things, pairOfThings);
		}
		else if (pattern.relationP()) {
			Relation p = (Relation) pattern;
			extractPairs(p.getSubject(), things, pairOfThings);
			extractPairs(p.getObject(), things, pairOfThings);
			return pairOfThings;
		}
		else if (pattern.sequenceP()) {
			for (Entity patternElement : ((Sequence) pattern).getElements()) {
				extractPairs(patternElement, things, pairOfThings);
			}
			return pairOfThings;
		}
		return pairOfThings;
	}

	private static Entity findMatchingThing(Entity current, ArrayList<Entity> things) {
		for (Entity match : things) {
			if (match.isAPrimed(current.getType())) {
				return match;
			}
		}
		return null;
	}

	public static Entity reconcile(Entity cause, Entity result) {
		ArrayList<Entity> things = extractThings(cause, new ArrayList<Entity>());
		LList<PairOfEntities> pairOfThings = extractPairs(result, things, new LList<PairOfEntities>());
		return substitute(result, pairOfThings);
	}

}

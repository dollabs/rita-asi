package frames.entities;

import java.util.*;


import utils.*;
import utils.logging.Logger;
import utils.minilisp.LList;
import utils.tools.JFactory;

public class Matcher {

	/**
	 * Matches Thing instances. Returns null or vector of bindings. Pattern element is indicated if primed thread has a
	 * question mark, ?.
	 */
	public static BindingSet match(Entity x, Entity y) {
		return match(x, y, new BindingSet(), false);
	}

	/**
	 * Matches Thing instances. Returns null or a BindingSet instance. Pattern element is indicated if Thing or
	 * Description threads has a question mark, ?.
	 */
	public static BindingSet forceMatch(Entity x, Entity y) {
		return match(x, y, new BindingSet(), true);
	}

	/**
	 * Matches Thing instances. Returns null or a BindingSet instance. Pattern element is indicated if Thing or
	 * Description thread has a question mark, ?.
	 */
	public static BindingSet match(Entity pattern, Entity datum, BindingSet bindings, boolean force) {
		fine("Trying to match " + pattern.getTypes() + " with " + datum.getTypes());

		// Fail if no bindings
		if (bindings == null) {
			return null;
		}

		// Fail if objects are not of the same class
		if (!pattern.getClass().equals(datum.getClass()) && !pattern.isA("?") && !datum.isA("?")) {
			return null;
		}

		// If pattern element has a binding, substitute, and then check against
		// datum.
		Entity substitution = fetchValue(pattern, bindings);
		if (substitution != null) {
			return match(substitution, datum, bindings, force);
		}

		// Find excess types in pattern, except for ?
		Vector<String> patternTypes = pattern.getMatcherTypes();
		Vector<String> datumTypes = datum.getMatcherTypes();
		Vector<String> excess = vectorDifference(patternTypes, datumTypes);
		excess.remove("?");
		excess.remove("description"); // because "?" is an adjective...what???

		// If no excess types or forcing match, succeed with thread matching
		if (excess.isEmpty() || force) {
			if (patternTypes.contains("?")) {
				// If the pattern contains a ?, add binding and return result
				bindings.add(new Binding(pattern, datum));
				return bindings;
			}
			// If forcing, add excess types to datum
			if (force && !(excess.isEmpty())) {
				System.out.println("Forcing match by adding " + excess);
				bindings.incrementForcedTypeCount(excess.size());
				datum.addTypes(excess);
			}
		}
		// Excess types, no forcing, so fail
		else {
			fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
			return null;
		}

		// Now, check to see if match is between derivatives, and match subjects
		// if so
		if (pattern.functionP() && datum.functionP()) {
			return match(pattern.getSubject(), datum.getSubject(), bindings, force);
		}

		// Now, check to see if match is between relations, and match both
		// subjects and objects if so
		else if (pattern.relationP() && datum.relationP()) {
			// Match subjects
			bindings = match(((Relation) pattern).getSubject(), ((Relation) datum).getSubject(), bindings, force);
			if (bindings == null) {
				return null;
			}
			// If successful, match objects with new bindings:
			else {
				return match(pattern.getObject(), datum.getObject(), bindings, force);
			}
		}

		// Now, check to see if match is between sequences, and match elements
		// if so
		// Each element in pattern must match an element in datum, but not vice
		// versa
		// First element matched is removed from datum
		else if (pattern instanceof Sequence && datum instanceof Sequence) {
			Vector patternElements = ((Sequence) pattern).getElements();
			Vector datumElements = ((Sequence) datum).getElements();
			// Ok to match if different sizes!
			if (patternElements.size() > datumElements.size()) {
				return null;
			}
			int successes = -1;
			for (int i = 0; i < patternElements.size(); ++i) {
				Entity patternElement = (Entity) (patternElements.elementAt(i));
				for (int j = 0; j < datumElements.size(); ++j) {
					Entity datumElement = (Entity) (datumElements.elementAt(j));
					fine("Trying to match " + patternElement.getTypes() + " with " + datumElement.getTypes());
					if (bindings == null) {
						break;
					}
					BindingSet trialMatch = match(patternElement, datumElement, bindings, force);
					if (trialMatch != null) {
						bindings = trialMatch;
						++successes;
						break;
					}
				}
				if (successes < i) {
					return null;
				}
			}
			return bindings;
		}
		if (bindings == null) {
			fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
		}
		return bindings;
	}

	/**
	 * Fetches a value from a vector of bindings.
	 */
	private static Entity fetchValue(Entity x, Vector bindingVector) {
		for (int i = 0; i < bindingVector.size(); ++i) {
			Binding binding = (Binding) (bindingVector.elementAt(i));
			Entity variable = (Entity) (binding.getVariable());
			if (x.equals(variable)) {
				// System.out.println("Match found on binding list!");
				return (Entity) (binding.getValue());
			}
		}
		return null;
	}

	private static Entity fetchByValue(Entity x, LList<PairOfEntities> bindingVector) {
		for (Object object : bindingVector) {
			PairOfEntities pairOfThings = (PairOfEntities) object;
			if (pairOfThings.getPattern() == x) {
				return pairOfThings.getDatum();
			}
		}
		return null;
	}

	/**
	 * Fetches a variable from a vector of bindings.
	 */
	private static Entity fetchVariable(Entity x, Vector bindingVector) {
		for (int i = 0; i < bindingVector.size(); ++i) {
			Binding binding = (Binding) (bindingVector.elementAt(i));
			Entity value = (Entity) (binding.getValue());
			if (x.equals(value)) {
				// System.out.println("Match found on binding list!");
				return (Entity) (binding.getVariable());
			}
		}
		return null;
	}

	/**
	 * Computes the difference of two vectors.
	 */
	private static <T> Vector<T> vectorDifference(Vector<T> v1, Vector<T> v2) {
		Vector<T> result = new Vector<T>();
		for (int i = 0; i < v1.size(); ++i) {
			T object = v1.elementAt(i);
			if (v2.contains(object)) {
			}
			else {
				result.add(object);
			}
		}
		return result;
	}

	private static <T> Vector<T> vectorIntersection(Vector<T> v1, Vector<T> v2) {
		Vector<T> result = new Vector<T>();
		for (int i = 0; i < v1.size(); ++i) {
			T object = v1.elementAt(i);
			if (v2.contains(object)) {
				result.add(object);
			}
		}
		return result;
	}

	/**
	 * Matches Thing instances. Returns null or vector of bindings. No pattern elements.
	 */
	public static BindingSet exactMatch(Entity pattern, Entity datum) {
		return exactMatch(pattern, datum, false);
	}

	public static BindingSet exactMatch(Entity x, Entity y, boolean matchModifiers) {
		BindingSet bs = null;
		if (x == null && y == null) {
			return new BindingSet();
		}
		if (x == null || y == null) {
			return null;
		}
		try {
			bs = exactMatch(x, y, new BindingSet(), matchModifiers);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bs;
	}

	/**
	 * Matches Thing instances, but only if they match exactly. Returns null or a BindingSet instance. No pattern
	 * elements.
	 */
	public static BindingSet exactMatch(Entity pattern, Entity datum, BindingSet bindings) {
		return exactMatch(pattern, datum, bindings, false);
	}

	public static BindingSet exactMatch(Entity pattern, Entity datum, BindingSet bindings, boolean matchModifiers) {
		fine("Trying to match " + pattern.getTypes() + " with " + datum.getTypes());

		// Fail if no bindings
		if (bindings == null) {
			return null;
		}

		// Fail if objects are not of the same class
		if (!pattern.getClass().equals(datum.getClass())) {
			return null;
		}

		// Find excess types

		Vector<String> patternTypes = pattern.getMatcherTypes();
		Vector<String> datumTypes = datum.getMatcherTypes();
		Vector<String> excessP = vectorDifference(patternTypes, datumTypes);
		Vector<String> excessD = vectorDifference(datumTypes, patternTypes);

		// If no excess types or forcing match, succeed with thread matching
		if (excessP.isEmpty() && excessD.isEmpty()) {
		}
		// Excess types, so fail
		else {
			fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
			return null;
		}

		if (matchModifiers) {
			Vector patternElements = pattern.getModifiers();
			Vector datumElements = datum.getModifiers();
			// Forces match in same order of all elements
			if (patternElements.size() != datumElements.size()) {
				return null;
			}
			int successes = 0;
			for (int i = 0; i < patternElements.size(); ++i) {
				Entity patternElement = (Entity) (patternElements.elementAt(i));
				Entity datumElement = (Entity) (datumElements.elementAt(i));
				BindingSet trialMatch = exactMatch(patternElement, datumElement, bindings, matchModifiers);
				if (trialMatch != null) {
					bindings = trialMatch;
				}
				else {
					return null;
				}
			}
		}

		// Now, check to see if match is between things, and if so, add binding
		if (pattern.entityP() && datum.entityP()) {
			fine("Binding " + pattern.getTypes() + " to " + datum.getTypes());
			bindings.add(new Binding(pattern, datum));
			return bindings;
		}

		// Now, check to see if match is between derivatives, and match subjects
		// if so
		if (pattern.functionP() && datum.functionP()) {
			return exactMatch(pattern.getSubject(), datum.getSubject(), bindings, matchModifiers);
		}

		// Now, check to see if match is between relations, and match both
		// subjects and objects if so
		else if (pattern.relationP() && datum.relationP()) {
			// Match subjects
			bindings = exactMatch(((Relation) pattern).getSubject(), ((Relation) datum).getSubject(), bindings, matchModifiers);
			if (bindings == null) {
				return null;
			}
			// If successful, match objects with new bindings:
			else {
				return exactMatch(pattern.getObject(), datum.getObject(), bindings, matchModifiers);
			}
		}

		// Now, check to see if match is between sequences, and match elements
		// if so
		// Each element in pattern must match an element in datum, and vice
		// versa
		// Matching elements must be incorresponding positions
		else if (pattern.sequenceP() && datum.sequenceP()) {
			Vector patternElements = ((Sequence) pattern).getElements();
			Vector datumElements = ((Sequence) datum).getElements();
			// Forces match in same order of all elements
			if (patternElements.size() != datumElements.size()) {
				return null;
			}
			int successes = 0;
			for (int i = 0; i < patternElements.size(); ++i) {
				Entity patternElement = (Entity) (patternElements.elementAt(i));
				Entity datumElement = (Entity) (datumElements.elementAt(i));
				BindingSet trialMatch = exactMatch(patternElement, datumElement, bindings, matchModifiers);
				if (trialMatch != null) {
					bindings = trialMatch;
				}
				else {
					return null;
				}
			}
			return bindings;
		}
		return bindings;
	}

	/**
	 * Matches Thing instances. Returns null or a BindingSet instance. All pattern Thing instances are considered
	 * pattern elements. Derivatives, Relations, and sequences always match. Intended to match precident (pattern side)
	 * against current situation (datum side). Thus, the following example: Precident: X threw Y at Z. Current
	 * situation: M threw N at O. Matching precident against current situation produces these bindings: Variable/Value:
	 * X/M, Y/N, Z/O. There are two arbitrary parameters, minimumTypeMatch and minimumSequenceMatch. minimumTypeMatch is
	 * the minimum number of corresponding objects on pattern and datum threads to have a match; and
	 * minimumSequenceMatch is the minimum number of elements of corresponding sequences that must match to have a
	 * match.
	 */
	public static BindingSet measureMatch(Entity x, Entity y) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < Entity.pathList.length; ++i) {
			list.add(Entity.pathList[i]);
		}
		for (int i = 0; i < Entity.placeList.length; ++i) {
			list.add(Entity.placeList[i]);
		}
		for (int i = 0; i < Entity.pathList.length; ++i) {
			list.add(Entity.changeList[i]);
		}
		return measureMatch(x, y, new BindingSet(), list);
	}

	private static final int minimumTypeMatch = 1;

	private static final int minimumSequenceMatch = 1;

	/**
	 * Helper for measureMatch
	 */
	public static BindingSet measureMatch(Entity pattern, Entity datum, BindingSet bindings, ArrayList matchList) {
		fine("Trying to match " + pattern.getTypes() + " with " + datum.getTypes());

		// Fail if no bindings
		if (bindings == null) {
			return null;
		}

		// Fail if objects are not of the same class
		if (!pattern.getClass().equals(datum.getClass()) && !pattern.isA("?") && !datum.isA("?")) {
			return null;
		}

		// If pattern element has a binding, substitute, and then check against
		// datum.
		// Note that this must be an exact match, lest same variable get bound
		// to multiple datums
		Entity substitution = fetchValue(pattern, bindings);
		if (substitution != null) {
			return exactMatch(substitution, datum, bindings);
		}

		// Also, if datum is already bound to a variable, but not matched in the
		// previous step, lose.
		if (fetchVariable(datum, bindings) != null) {
			return null;
		}

		// Find excess types in pattern
		Vector<String> patternTypes = pattern.getMatcherTypes();
		Vector<String> datumTypes = datum.getMatcherTypes();
		Vector<String> excessPatternTypes = vectorDifference(patternTypes, datumTypes);
		Vector<String> excessDatumTypes = vectorDifference(datumTypes, patternTypes);
		Vector<String> intersection = vectorIntersection(datumTypes, patternTypes);

		// Note number of mismatches; not used at the moment
		int mismatches = excessPatternTypes.size() + excessDatumTypes.size();

		// For the moment, better be something other than Thing that matches
		if (intersection.size() < minimumTypeMatch) {
			fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
			fine("Intersection size = " + intersection.size());
			fine("Pattern types " + pattern.getMatcherTypes());
			fine("Datum types " + datum.getMatcherTypes());
			return null;
		}

		// Now, if pattern belongs to a class specified on the matchList, then
		// make sure the datum belongs to that class as well, and vice versa
		for (int i = 0; i < matchList.size(); ++i) {
			String x = (String) (matchList.get(i));
			if (pattern.isA(x) && !datum.isA(x)) {
				fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
				fine("Mismatch is on " + x);
				return null;
			}
			if (datum.isA(x) && !pattern.isA(x)) {
				fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
				fine("Mismatch is on " + x);
				return null;
			}
		}

		// Now, check to see if match is between things, and if so, add binding
		if (pattern.entityP() && datum.entityP()) {
			fine("Binding " + pattern.getTypes() + " to " + datum.getTypes());
			bindings.add(new Binding(pattern, datum));
			return bindings;
		}

		// Now, check to see if match is between derivatives, and match subjects
		// if so
		else if (pattern.functionP() && datum.functionP()) {
			fine("Recursing into subjects");
			return measureMatch(pattern.getSubject(), datum.getSubject(), bindings, matchList);
		}

		// Now, check to see if match is between relations, and match both
		// subjects and objects if so
		else if (pattern.relationP() && datum.relationP()) {
			// Match subjects
			fine("Recursing into subjects");
			bindings = measureMatch(((Relation) pattern).getSubject(), ((Relation) datum).getSubject(), bindings, matchList);
			if (bindings == null) {
				return null;
			}
			// If successful, match objects with new bindings:
			else {
				fine("Recursing into objects");
				return measureMatch(pattern.getObject(), datum.getObject(), bindings, matchList);
			}
		}

		// Now, check to see if match is between sequences, and match elements
		// if so,
		// matching as many elements as possible.
		else if (pattern.sequenceP() && datum.sequenceP()) {
			Vector patternElements = ((Sequence) pattern).getElements();
			Vector datumElements = ((Sequence) datum).getElements();
			int successes = 0;
			for (int i = 0; i < patternElements.size(); ++i) {
				Entity patternElement = (Entity) (patternElements.elementAt(i));
				for (int j = 0; j < datumElements.size(); ++j) {
					Entity datumElement = (Entity) (datumElements.elementAt(j));
					fine("Trying to match " + patternElement.getTypes() + " with " + datumElement.getTypes());
					if (bindings == null) {
						break;
					}
					BindingSet trialMatch = measureMatch(patternElement, datumElement, bindings, matchList);
					if (trialMatch != null) {
						bindings = trialMatch;
						++successes;
						break;
					}
				}
			}
			// Let's insist on at least something matching, unless both paths
			// are empty!
			if ((patternElements.size() > 0 || datumElements.size() > 0) && successes < minimumSequenceMatch) {
				return null;
			}
			return bindings;
		}
		if (bindings == null) {
			fine("Failed to match " + pattern.getTypes() + " with " + datum.getTypes());
		}
		return bindings;
	}

	/**
	 * Replaces variables with bindings, recursively. All other structure is cloned. Intended uses is as follows: First,
	 * measureMatch is used to match part of a precident with part of current situation. Then, the binding set is then
	 * used to instantiate and clone some other part of the precident. Here is an example: Precident: X walked to Y, and
	 * then X threw Y at Z. Current situation: M threw threw N at O. Matching precident against current
	 * situationproduces these bindings: Variable/Value: X/M, Y/N, Z/O. Instantiate of the first part of the precident
	 * produces this: M walked to N.
	 */
	public static Entity instantiate(Entity precident, BindingSet bindings) {
		fine("Trying to instantiate " + precident.getTypes());

		// Stop if no bindings
		if (bindings == null) {
			return precident;
		}

		// If precident element has a binding, substitute and return.
		Entity substitution = fetchValue(precident, bindings);
		if (substitution != null) {
			return substitution;
		}

		// Now, if predicent is a Thing instance, just return it, cloned.
		if (precident.entityP()) {
			Entity t = (Entity) (precident.clone());
			return t;
		}

		// Now, check to see if precident is a derivative; if so, clone and work
		// on subject
		else if (precident.functionP() && precident.functionP()) {
			Function d = (Function) (precident.clone());
			d.setSubject(instantiate(precident.getSubject(), bindings));
			return d;
		}

		// Now, check to see if precident is a relation; if so, clone and work
		// on subject and object
		else if (precident.relationP()) {
			Relation r = (Relation) (precident.clone());
			r.setSubject(instantiate(precident.getSubject(), bindings));
			r.setObject(instantiate(precident.getObject(), bindings));
			return r;
		}

		// Now, check to see if precident is a sequence; if so, clone and work
		// on elements
		else if (precident.sequenceP()) {
			Sequence s = (Sequence) (precident.clone());
			Vector v = (Vector) (s.getElements().clone());
			s.clearElements();
			for (int i = 0; i < v.size(); ++i) {
				Entity t = (Entity) (v.elementAt(i));
				Entity u = instantiate(t, bindings);
				s.addElement(u);
			}
			return s;
		}
		// Should never be called
		return precident;
	}

	/**
	 * To do: does not yet handle owners!
	 * 
	 * @param precident
	 * @param bindings
	 * @return
	 */

	public static Entity instantiate(Entity precident, LList<PairOfEntities> bindings) {
		// Mark.say("Entering instantiate\n", precident, "\n", bindings);
		if (precident == null) {
			Mark.err("Null precident");
		}
		// Stop if no bindings
		if (bindings == null) {
			return precident;
		}

		// If precident element has a binding, substitute and return.
		Entity substitution = fetchByValue(precident, bindings);
		if (substitution != null) {
			return substitution;
		}

		// Now, if predicent is a Thing instance, just return it, cloned.
		// As of 7 February 2016, deleted. Not clear what it was for; all seems to work without cloning.
		if (false && precident.entityP()) {
			Entity t = (Entity) (precident.clone());
			return t;
		}

		// Now, check to see if precident is a derivative; if so, clone and work
		// on subject
		else if (precident.functionP() && precident.functionP()) {
			Function d = (Function) (precident.clone());
			d.setSubject(instantiate(precident.getSubject(), bindings));
			return d;
		}

		// Now, check to see if precident is a relation; if so, clone and work
		// on subject and object
		else if (precident.relationP()) {
			Relation r = (Relation) (precident.clone());
			r.setSubject(instantiate(precident.getSubject(), bindings));
			r.setObject(instantiate(precident.getObject(), bindings));
			return r;
		}

		// Now, check to see if precident is a sequence; if so, clone and work
		// on elements
		else if (precident.sequenceP()) {
			Sequence s = (Sequence) (precident.clone());
			Vector v = (Vector) (s.getElements().clone());
			s.clearElements();
			for (int i = 0; i < v.size(); ++i) {
				Entity t = (Entity) (v.elementAt(i));
				Entity u = instantiate(t, bindings);
				s.addElement(u);
			}
			return s;
		}
		// Should never be called
		return precident;
	}

	/*
	 * public static boolean arianMeasureMatchTest() { BridgeSpeak bs = new BridgeSpeak(); bs.readFile("wordk.txt");
	 * bs.readFile("classk.txt"); bs.setInput("the girl ran to the top of the pole."); Thing t1 =
	 * (Thing)(bs.getBuffer().getElements().elementAt(0)); bs.setInput("the bird flew to the top of the tree."); Thing
	 * t2 = (Thing)(bs.getBuffer().getElements().elementAt(0)); BindingSet result = measureMatch(t1, t2);
	 * fine(result.toString()); System.out.println(result); if (result == null) {return false;} return true; }
	 */

	public static boolean basicMeasureMatchTest() {

		// Set up a situation testing two paths, one with an element
		// that matches the sole element on the other path.

		// From next to the table to on the pole.
		Entity t1 = new Entity("table");
		Entity t2 = new Entity("pole");
		Sequence pPathB = JFactory.createPath();
		pPathB.addElement(JFactory.createPathElement("from", JFactory.createPlace("nextTo", t1)));
		pPathB.addElement(JFactory.createPathElement("to", JFactory.createPlace("on", t2)));

		// via next to the pole.
		Sequence pPathA = JFactory.createPath();
		pPathA.addElement(JFactory.createPathElement("via", JFactory.createPlace("nextTo", t2)));

		// to on the pole.
		Entity t4 = new Entity("pole");
		Sequence cPath = JFactory.createPath();
		cPath.addElement(JFactory.createPathElement("to", JFactory.createPlace("on", t4)));

		// Enrich types a bit
		t2.addType("big");
		t4.addType("small");
		t2.addType("firePole");
		t4.addType("flagPole");

		// Match second part of previous situation against current situation,
		// picking up bindings.
		BindingSet bindings = Matcher.measureMatch(pPathB, cPath);

		// If no bindings, alreadly lost
		if (bindings == null) {
			return false;
		}

		// Should bind t4 to t2, I think!
		boolean test1 = t4 == bindings.getValue(t2);

		// Now instantiate first part of previous situation using the binding
		// set
		// Should produce To next to the table.
		Entity instantiation = instantiate(pPathA, bindings);

		// Create a reference for comparison
		Sequence reference = JFactory.createPath();
		reference.addElement(JFactory.createPathElement("via", JFactory.createPlace("nextTo", t4)));

		// Test for identity with reference
		// Reference should not match the instantiated path
		boolean test2 = exactMatch(pPathA, reference) != null;
		// ... but it should match the instantiation of the path
		boolean test3 = exactMatch(instantiation, reference) != null;

		// Overall, should match if following combination is true
		return test1 && !test2 && test3;
	}

	// public static boolean anotherMeasureMatchTest() {
	//
	// // Set up a situation testing two paths, one with an element
	// // that matches the sole element on the other path.
	// // From next to the table to on the pole.
	// Entity boy = new Entity("person");
	// Entity rock = new Entity("projectile");
	// Entity girl = new Entity("person");
	// Entity ball = new Entity("projectile");
	//
	// boy.addType("boy");
	// girl.addType("girl");
	// rock.addType("rock");
	// ball.addType("ball");
	//
	// Sequence boyPath = JFactory.createPath();
	// Sequence girlPath = JFactory.createPath();
	//
	// Sequence rockPath = JFactory.createPath();
	// Sequence ballPath = JFactory.createPath();
	//
	// Relation goBoy = new Relation("go", boy, boyPath);
	// Relation goGirl = new Relation("go", girl, girlPath);
	// Relation goRock = new Relation("go", rock, rockPath);
	// Relation goBall = new Relation("go", ball, ballPath);
	//
	// Sequence boyAndRock = JFactory.createTrajectoryLadder();
	// Sequence girlAndBall = JFactory.createTrajectoryLadder();
	//
	// JFactory.extendTrajectoryLadder(boyAndRock, goBoy);
	// JFactory.extendTrajectoryLadder(boyAndRock, goRock);
	//
	// JFactory.extendTrajectoryLadder(girlAndBall, goGirl);
	// JFactory.extendTrajectoryLadder(girlAndBall, goBall);
	//
	// Sequence boySpace = JFactory.createEventSpace();
	// Sequence girlSpace = JFactory.createEventSpace();
	//
	// JFactory.extendEventSpace(boySpace, boyAndRock);
	// JFactory.extendEventSpace(girlSpace, girlAndBall);
	//
	// // Match second part of previous situation against current situation,
	// // picking up bindings.
	// BindingSet bindings = Matcher.measureMatch(boySpace, girlSpace);
	//
	// System.out.println(bindings);
	//
	// // If no bindings, alreadly lost
	// if (bindings == null) {
	// return false;
	// }
	// return true;
	//
	// }

	/**
	 * Matches Change instances. Returns true or false. Changes match if objects match and change matches.
	 */
	public static boolean matchChanges(Entity currentChange, Entity previousChange) {
		// First verify that currentChange and previousChange are changes
		if (!currentChange.functionP() || !previousChange.functionP() || !currentChange.isA("transitionElement")
		        || !previousChange.isA("transitionElement")) {
			fine("At least one of the arguments is not an event derivative");
			return false;
		}

		// Verify that events match, without reference to changing object
		if (!matchSurfaces(currentChange, previousChange)) {
			fine("Changes do not pass basic matching test");
			return false;
		}

		// Now verify that changing objects match
		Entity eventMover = currentChange.getSubject();
		Entity precedentMover = previousChange.getSubject();
		if (!matchChangingObjects(eventMover, precedentMover)) {
			return false;
		}

		fine("Matched corresponding changes");
		return true;
	}

	/**
	 * Matches movers. Movers match if they are matching thing instances.
	 */
	private static boolean matchChangingObjects(Entity currentMover, Entity previousMover) {
		if (!matchStructures(currentMover, previousMover)) {
			fine("Movers do not match");
			return false;
		}
		fine("Changing objects match");
		return true;
	}

	/**
	 * Matches structures recursively.
	 */
	private static boolean matchStructures(Entity currentStructure, Entity previousStructure) {
		if (currentStructure.entityP() && previousStructure.entityP()) {
			return matchThings(currentStructure, previousStructure);
		}
		if (currentStructure.functionP() && previousStructure.functionP()) {
			return matchDerivatives(currentStructure, previousStructure);
		}
		if (currentStructure.relationP() && previousStructure.relationP()) {
			return matchRelations(currentStructure, previousStructure);
		}
		if (currentStructure.sequenceP() && previousStructure.sequenceP()) {
			return matchSequences(currentStructure, previousStructure);
		}
		fine("Structures not same type");
		return false;
	}

	/**
	 * Matches Derivative instances. Returns true or false.
	 */
	private static boolean matchDerivatives(Entity currentDerivative, Entity previousDerivative) {
		if (!currentDerivative.functionP() || !previousDerivative.functionP()) {
			fine("At least one of the arguments is not a derivative");
			return false;
		}
		if (!matchSurfaces(currentDerivative, previousDerivative)) {
			fine("Derivatives do not pass basic matching test");
			return false;
		}
		Entity currentThing = currentDerivative.getSubject();
		Entity previousThing = previousDerivative.getSubject();
		if (!matchStructures(currentThing, previousThing)) {
			fine("Subject arguments do not match");
			return false;
		}
		fine("Derivatives match");
		return true;
	}

	/**
	 * Matches Relation instances. Returns true or false.
	 */
	public static boolean matchRelations(Entity currentRelation, Entity previousRelation) {
		// First verify that currentRelation and previousRelation are events
		if (!currentRelation.relationP() || !previousRelation.relationP()) {
			fine("At least one of the arguments is not a relation");
			return false;
		}

		// Verify that events match, without reference to slots
		if (!matchSurfaces(currentRelation, previousRelation)) {
			fine("Relations do not pass basic matching test");
			return false;
		}

		// Now verify that subject objects match
		Entity relationSubject = currentRelation.getSubject();
		Entity precedentSubject = previousRelation.getSubject();
		if (!matchStructures(relationSubject, precedentSubject)) {
			return false;
		}

		// Now verify that object objects match
		Entity relationObject = currentRelation.getObject();
		Entity precedentObject = previousRelation.getObject();
		if (!matchStructures(relationObject, precedentObject)) {
			return false;
		}

		fine("Relations match");
		return true;
	}

	/**
	 * Matches sequences. Sequences match if all sequence elements in the current sequence are found in the previous
	 * sequence.
	 */
	private static boolean matchSequences(Entity currentSequence, Entity previousSequence) {
		if (!currentSequence.sequenceP() || !previousSequence.sequenceP()) {
			fine("At least one of the arguments is not a sequence");
		}
		if (!matchSurfaces(currentSequence, previousSequence)) {
			fine("Sequences do not pass basic matching test");
			return false;
		}
		// At this juncture, every sequence element in the current sequence must
		// have a corresponding sequence element in the previous sequence
		Vector cVector = currentSequence.getElements();
		Vector pVector = previousSequence.getElements();
		boolean testResult = false;
		for (int i = 0; i < cVector.size(); ++i) {
			Entity currentSequenceElement = (Entity) (cVector.elementAt(i));
			if (!checkForSequenceInclusion(currentSequenceElement, pVector)) {
				fine("Sequence element missing from previous sequence");
				return false;
			}
		}
		fine("Sequences match");
		return true;
	}

	/**
	 * Checks to see if first argument, a path element, matches a path element in the second argument, a vector of path
	 * elements.
	 */
	private static boolean checkForSequenceInclusion(Entity currentSequenceElement, Vector previousSequenceElements) {
		for (int i = 0; i < previousSequenceElements.size(); ++i) {
			if (matchStructures(currentSequenceElement, (Entity) (previousSequenceElements.elementAt(i)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Matches Event instances. Returns true or false. Events match if movers match and paths match. Match of paths,
	 * path elements, and place elements occurs only if same most-specific class and elements of the description thread
	 * of current item are a subset of the elements of the description thread of the previous item. Of course, path
	 * elements and place elements must have matching subjects. Paths match if all path elements of current path are
	 * present in the previous path. Thing instances match only if they are identical, because reference has already
	 * been worked out for Thing instances in the parser.
	 */
	public static boolean matchEvents(Entity currentEvent, Entity previousEvent) {
		// First verify that currentEvent and previousEvent are events
		if (!currentEvent.relationP() || !previousEvent.relationP() || !currentEvent.isA("event") || !previousEvent.isA("event")) {
			fine("At least one of the arguments is not an event relation");
			return false;
		}

		// Verify that events match, without reference to slots
		if (!matchSurfaces(currentEvent, previousEvent)) {
			fine("Events do not pass basic matching test");
			return false;
		}

		// Now verify that moving objects match
		Entity eventMover = currentEvent.getSubject();
		Entity precedentMover = previousEvent.getSubject();
		if (!matchMovers(eventMover, precedentMover)) {
			return false;
		}

		// Now verify that moving objects are moving along a path
		Entity eventPath = currentEvent.getObject();
		Entity precedentPath = previousEvent.getObject();
		if (!matchPaths(eventPath, precedentPath)) {
			return false;
		}
		fine("Matched corresponding events");
		return true;
	}

	/**
	 * Matches movers. See documentation for matchEvents.
	 */
	private static boolean matchMovers(Entity currentMover, Entity previousMover) {
		if (!matchThings(currentMover, previousMover)) {
			fine("Movers do not match");
			return false;
		}
		fine("Movers match");
		return true;
	}

	/**
	 * Matches things for event matcher. Things match only if identical, because references already worked out in the
	 * parser.
	 */
	private static boolean matchThings(Entity currentThing, Entity previousThing) {
		if (!currentThing.entityP() || !previousThing.entityP()) {
			fine("At least one of the arguments is not a thing");
			fine(currentThing);
			fine(previousThing);
			return false;
		}
		if (currentThing != previousThing) {
			fine("Things do not match");
			return false;
		}
		fine("Things match");
		return true;
	}

	/**
	 * Matches paths. Paths match if all path elements in the current path are found in the previous path.
	 */
	private static boolean matchPaths(Entity currentPath, Entity previousPath) {
		if (!currentPath.sequenceP() || !previousPath.sequenceP() || !currentPath.isA("path") || !previousPath.isA("path")) {
			fine("At least one of the paths is not a path sequence");
		}
		if (!matchSurfaces(currentPath, previousPath)) {
			fine("Paths do not pass basic matching test");
			return false;
		}
		// At this juncture, every path element in the current path must have a
		// corresponding path element in the previous path
		Vector cVector = currentPath.getElements();
		Vector pVector = previousPath.getElements();
		boolean testResult = false;
		for (int i = 0; i < cVector.size(); ++i) {
			Entity currentPathElement = (Entity) (cVector.elementAt(i));
			if (!checkForPathInclusion(currentPathElement, pVector)) {
				fine("Path element missing from previous path");
				return false;
			}
		}
		fine("Paths match");
		return true;
	}

	/**
	 * Checks to see if first argument, a path element, matches a path element in the second argument, a vector of path
	 * elements.
	 */
	private static boolean checkForPathInclusion(Entity currentPathElement, Vector previousPathElements) {
		for (int i = 0; i < previousPathElements.size(); ++i) {
			if (matchPathElements(currentPathElement, (Entity) (previousPathElements.elementAt(i)))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Matches path elements. See documentation for matchEvents.
	 */
	private static boolean matchPathElements(Entity currentPathElement, Entity previousPathElement) {
		if (!currentPathElement.functionP() || !previousPathElement.functionP() || !currentPathElement.isA("pathElement")
		        || !previousPathElement.isA("pathElement")) {
			fine("At least one of the arguments is not a pathElement derivative");
			return false;
		}
		if (!matchSurfaces(currentPathElement, previousPathElement)) {
			fine("Path elements do not pass basic matching test");
			return false;
		}
		Entity currentThing = currentPathElement.getSubject();
		Entity previousThing = previousPathElement.getSubject();
		if (!matchPlaces(currentThing, previousThing)) {
			fine("Path element arguments do not match");
			return false;
		}
		fine("Path Elements match");
		return true;
	}

	/**
	 * Matches places. See documentation for matchEvents.
	 */
	private static boolean matchPlaces(Entity currentPlace, Entity previousPlace) {
		if (!currentPlace.functionP() || !previousPlace.functionP() || !currentPlace.isA("place") || !previousPlace.isA("place")) {
			fine("At least one of the arguments is not a place derivative");
			return false;
		}
		if (!matchSurfaces(currentPlace, previousPlace)) {
			fine("Places do not pass basic matching test");
			return false;
		}
		Entity currentThing = currentPlace.getSubject();
		Entity previousThing = previousPlace.getSubject();
		if (!matchThings(currentThing, previousThing)) {
			fine("Place arguments do not match");
			return false;
		}
		fine("Places match");
		return true;
	}

	/**
	 * Matches thing classes and descriptions. Things match if they are identical or if they have the same most specific
	 * class and the elements on the description thread of the current thing are all in the desription thread of the
	 * previous thing.
	 */
	private static boolean matchSurfaces(Entity currentThing, Entity previousThing) {

		// First, if they are the same, they match, of course
		if (currentThing == previousThing) {
			fine("Matched identical things: " + currentThing.getName() + " matches " + previousThing.getName());
			return true;
		}

		// If not identical, they better be referenced by the same class
		if (!currentThing.getType().equals(previousThing.getType())) {
			return false;
		}

		// Then, if there are descriptors, the descriptors of the current thing
		// better be a subset of the descriptors of the previous thing
		Vector currentDescriptors = currentThing.getThread("description");
		Vector previousDescriptors = previousThing.getThread("description");
		if (currentDescriptors != null) {
			if (previousDescriptors == null) {
				return false;
			}
			for (int i = 0; i < currentDescriptors.size(); ++i) {
				Object descriptor = currentDescriptors.elementAt(i);
				fine("Checking " + descriptor);
				// Check particular descriptor
				if (!previousDescriptors.contains(descriptor)) {
					return false;
				}
			}
		}

		// Looks ok
		fine("Matched corresponding things: " + currentThing.getName() + " matches " + previousThing.getName());
		return true;
	}

	/**
	 * Replaces one Thing instance with another whereever it is found (and of course, all subclasses of Thing work too).
	 */
	public static void replace(Entity oldThing, Entity newThing) {
		fine("Replacing new event with antecedant");
		Vector subjectOfVector = (Vector) (oldThing.getSubjectOf().clone());
		Vector objectOfVector = (Vector) (oldThing.getObjectOf().clone());
		Vector elementOfVector = (Vector) (oldThing.getElementOf().clone());
		for (int i = 0; i < subjectOfVector.size(); ++i) {
			Function d = (Function) (subjectOfVector.elementAt(i));
			d.setSubject(newThing);
		}
		for (int i = 0; i < objectOfVector.size(); ++i) {
			Relation r = (Relation) (objectOfVector.elementAt(i));
			r.setObject(newThing);
		}
		for (int i = 0; i < elementOfVector.size(); ++i) {
			Sequence s = (Sequence) (elementOfVector.elementAt(i));
			Vector<Entity> v = s.getElements();
			int index = v.indexOf(oldThing);
			if (index < 0) {
				warning("Unable to replace sequence element for some reason");
			}
			else {
				v.set(index, newThing);
			}
		}
		// get.forget(oldThing);

	}

	// public static boolean testMatchers() {
	// // Create basic stuff to test with
	// Entity eMover = new Entity();
	// Entity pMover = new Entity();
	// pMover.addType("animal");
	// pMover.addType("bird");
	// // pMover.addType("cardinal");
	// pMover.addType("red", "description");
	// pMover.addType("animal");
	// pMover.addType("bird");
	// pMover.addType("red", "description");
	// Sequence pPath = JFactory.createPath();
	// Sequence ePath = JFactory.createPath();
	// // Relation pEvent = JFactory.createGo(pMover, pPath);
	// // Relation eEvent = JFactory.createGo(pMover, ePath);
	// // pEvent.addType("walk");
	// // eEvent.addType("walk");
	//
	// // Enrich paths
	// Entity pTarget = new Entity("table");
	// Entity eTarget = new Entity("table");
	// Function pElement2 = JFactory.createPathElement("from", JFactory.createPlace("under", pTarget));
	// Function pElement = JFactory.createPathElement("to", JFactory.createPlace("above", pTarget));
	//
	// Function eElement = JFactory.createPathElement("from", JFactory.createPlace("under", pTarget));
	// pPath.addElement(pElement);
	// pPath.addElement(pElement2);
	// ePath.addElement(eElement);
	// boolean result1 = matchEvents(eEvent, pEvent);
	//
	// if (!result1) {
	// info("Events do not match");
	// }
	// else {
	// info("Events match");
	// }
	//
	// Function pChange = BFactory.createTransitionElement("increase", pEvent);
	// Function eChange = BFactory.createTransitionElement("increase", eEvent);
	//
	// boolean result2 = matchChanges(eChange, pChange);
	//
	// if (!result2) {
	// info("Changes do not match");
	// }
	// else {
	// info("Changes match");
	// }
	// return result1 && result2;
	//
	// }

	private static void fine(Object s) {
		Logger.getLogger("frames.Matcher").fine(s);
	}

	private static void info(Object s) {
		Logger.getLogger("frames.Matcher").info(s);
	}

	private static void warning(Object s) {
		Logger.getLogger("frames.Matcher").warning(s);
	}

	// public static void main(String[] ignore) {
	//
	// testMatchers();
	//
	// // System.out.println(basicMeasureMatchTest());
	//
	// // setLevel(Level.FINE);
	//
	// // System.out.println(anotherMeasureMatchTest());
	//
	// // System.out.println(arianMeasureMatchTest());
	//
	// }

}

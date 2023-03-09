package storyProcessor;

import java.util.*;


import constants.Markers;
import frames.entities.Binding;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.RoleFrames;
import matchers.StandardMatcher;
import translator.*;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Aug 10, 2015
 * @author phw
 */

public class RoleFrameMatcher {

	private static RoleFrameMatcher roleFrameMatcher;

	private RoleFrameMatcher() {

	}

	public static RoleFrameMatcher getRoleFrameMatcher() {
		if (roleFrameMatcher == null) {
			roleFrameMatcher = new RoleFrameMatcher();
		}
		return roleFrameMatcher;
	}

	public class MatchDescription {

		private Entity copy = new Entity(Markers.UNKNOWN);

		private double penalty = 0;

		public double getPenalty() {
			return penalty;
		}

		List<Binding> bindings = new ArrayList<>();

		public MatchDescription() {
			this(0);
		}

		public MatchDescription(double x) {
			penalty = x;
		}

		public void penalize(double x) {
			penalty += x;
		}

		public boolean isMatch() {
			if (penalty < Double.POSITIVE_INFINITY) {
				return true;
			}
			return false;
		}

		public List<Binding> getBindings() {
			return bindings;
		}

		public void addBinding(Binding binding) {
			bindings.add(binding);
		}

		public Entity getCopy() {
			return copy;
		}

		public void setCopy(Entity copy) {
			this.copy = copy;
		}

		public String toString() {
			if (copy != null) {
				return copy.toString();
			}
			return "No copy";
		}
	}

	public MatchDescription match(Entity pattern, Entity datum) {
		return match(pattern, datum, new ArrayList<Binding>());
	}

	public MatchDescription match(Entity pattern, Entity datum, List<Binding> bindings) {
		boolean debug = false;
		Mark.say(debug, "Trying to match\n ", pattern, "\n ", datum);

		MatchDescription matchDescription = new MatchDescription();

		// Better be sure both are role frames
		if (!RoleFrames.isRoleFrame(pattern) || !RoleFrames.isRoleFrame(datum)) {
			matchDescription.penalize(Double.POSITIVE_INFINITY);
			return matchDescription;
		}
		MatchDescription subjectMatch = matchEntities(pattern.getSubject(), datum.getSubject(), bindings);
		MatchDescription objectMatch = matchRoles(pattern.getObject(), datum.getObject(), subjectMatch.getBindings());

		// Mark.say("Score", objectMatch.getPenalty());

		// pattern.setObject(objectMatch.getCopy());

		return objectMatch;
	}

	private MatchDescription combineWithFunction(Function datum, MatchDescription subjectMatch) {
		MatchDescription description = new MatchDescription();
		Function f = new Function(Markers.UNKNOWN, subjectMatch.getCopy());
		transferThreadsAndProperties(datum, f);
		description.setCopy(f);
		return description;
	}

	private MatchDescription combineWithRelation(Relation datum, MatchDescription subjectMatch, MatchDescription objectMatch) {
		MatchDescription description = new MatchDescription();
		Relation r = new Relation(Markers.UNKNOWN, subjectMatch.getCopy(), objectMatch.getCopy());
		transferThreadsAndProperties(datum, r);
		description.setCopy(r);
		return description;
	}

	private MatchDescription combineWithSequence(Sequence datum, MatchDescription... elements) {
		MatchDescription description = new MatchDescription();
		Sequence s = new Sequence(Markers.UNKNOWN);
		transferThreadsAndProperties(datum, s);
		Arrays.asList(elements).stream().forEachOrdered(e -> s.addElement(e.getCopy()));
		description.setCopy(s);
		return description;
	}

	private MatchDescription matchEntities(Entity pattern, Entity datum, List<Binding> bindings) {
		MatchDescription outMatch = new MatchDescription();
		// Better be sure they are entities
		if (!pattern.entityP() || !datum.entityP()) {
			outMatch.penalize(Double.POSITIVE_INFINITY);
			return outMatch;
		}
		else {
			if (pattern.isA(Markers.NAME) && datum.isA(Markers.NAME)) {
				// Make sure pattern thread above name is a subthread of datum thread above name
				Vector<String> patternThread = pattern.getPrimedThread();
				Vector<String> datumThread = datum.getPrimedThread();

				int effectiveDatumSize = datumThread.size() - 3;
				int effectivePatternSize = datumThread.size() - 3;
				if (effectiveDatumSize > effectivePatternSize) {
					// Bad news, datum is more specific
					outMatch.penalize(Double.POSITIVE_INFINITY);
					return outMatch;
				}
				for (int i = 0; i < effectiveDatumSize; ++i) {
					String patternType = patternThread.get(i);
					String datumType = datumThread.get(i);
					if (!patternType.equals(datumType)) {
						// Bad news, they don't match
						outMatch.penalize(Double.POSITIVE_INFINITY);
						return outMatch;
					}
				}
				// Good news, tests indicate match
				outMatch.addBinding(new Binding(pattern, datum));
				outMatch.setCopy(pattern);
				return outMatch;
			}
			else if (pattern.isA(Markers.NAME)) {
				// Asymmetric
				outMatch.penalize(Double.POSITIVE_INFINITY);
				return outMatch;
			}
			else if (datum.isA(Markers.NAME)) {
				// Asymmetric
				outMatch.penalize(Double.POSITIVE_INFINITY);
				return outMatch;
			}
			else {
				// Now, just look for thread starts with condition on any pair of threads
				Bundle patternBundle = pattern.getBundle();
				Bundle datumBundle = datum.getBundle();
				for (Thread patternThread : patternBundle) {
					for (Thread datumThread : patternBundle) {
						patternThread.startsWith(datumThread);
						// All is well, no penalty
						outMatch.addBinding(new Binding(pattern, datum));
						outMatch.setCopy(pattern);
						return outMatch;
					}
				}
			}
			// Evidently no thread match
			outMatch.penalize(Double.POSITIVE_INFINITY);
			return outMatch;
		}
	}

	/**
	 * The entree
	 */
	private MatchDescription matchRoles(Entity pattern, Entity datum, List<Binding> list) {
		boolean debug = false;
		// Mark.say(debug, "Matching roles", pattern);
		// Mark.say(debug, "Matching roles", datum);

		Sequence mirror = new Sequence();

		transferThreadsAndProperties(datum, mirror);

		Vector datumElements = (Vector<Entity>) datum.getElements().clone();
		Vector patternElements = (Vector<Entity>) pattern.getElements().clone();

		// Check out the elements, need clone to prevent concurrent modification exception
		MatchDescription matchDescription = new MatchDescription();

		for (Entity d : (Vector<Entity>) datumElements.clone()) {

			// See if it is a path element

			if (isPathElement(d)) {

				Function dPathElement = (Function) d;
				Function dPlaceElement;
				Entity dTarget;

				if (isPlaceElement(dPathElement.getSubject())) {
					dPlaceElement = (Function) dPathElement.getSubject();
					dTarget = dPlaceElement.getSubject();
				}
				else {
					dTarget = dPathElement.getSubject();
					dPlaceElement = new Function(Markers.AT, dTarget);
					dPathElement.setSubject(dPlaceElement);
				}

				Function pPathElement = null;
				Function pPlaceElement = null;
				Entity pSubject = null;

				Entity eSubject = new Entity(Markers.UNKNOWN);
				Function ePlaceElement = new Function(Markers.UNKNOWN, eSubject);
				Function ePathElement = new Function(Markers.UNKNOWN, ePlaceElement);

				transferThreadsAndProperties(dPathElement, ePathElement);

				// At this point, have a complete path element with embedded place element with embedded subject
				// So, see if there is a match in the pattern

				boolean match = false;

				for (Entity p : (Vector<Entity>) patternElements.clone()) {

					// See if p is a matching path element

					if (isPathElement(p) && d.getType().equals(p.getType())) {

						patternElements.remove(p);
						match = true;

						pPathElement = (Function) p;

						// At this point, I know that the roles both have the same pathPreposition;
						// but there are 4 combinations (not nine, both preposition and target missing
						// would not be reasonable English, and if there are both a path preposition
						// and a target, of any kind, then it is a complete command)

						// Place preposition matches

						// Place preposition doesn't match

						// Place preposition missing

						// Target matches

						// Target doesn't match

						// Target missing

						// First see if there is a place preposition

						if (isPlaceType(pPathElement.getSubject())) {
							// Place type present, see if there is an object
							if (pPathElement.getSubject().functionP()) {
								Mark.err("Oops, these clauses should never be seen, as command is complete");
								Mark.err("Path elements\n", pPathElement, "\n", dPathElement);
								// Target present
								if (pPathElement.getSubject().getType().equals(dPlaceElement.getType())) {
									// Place types the same
									if (null != StandardMatcher.getBasicMatcher().match(pPathElement.getSubject().getSubject(), dTarget)) {
										// Targets are compatible; all is cool
									}
									else {
										// Targets are not compatible
										matchDescription.penalize(10);
									}
								}
								else {
									// Place types not the same
									matchDescription.penalize(10);
									if (null != StandardMatcher.getBasicMatcher().match(pPathElement.getSubject().getSubject(), dTarget)) {
										// At least the targets are compatible

									}
									else {
										// Targets are not compatible either
										matchDescription.penalize(10);
									}
								}
								ePathElement = pPathElement;
							}
							else {
								// Target not present
								ePlaceElement.setSubject(dTarget);
								if (pPathElement.getSubject().getType().equals(dPlaceElement.getType())) {
									// Place types the same
									transferThreadsAndProperties(dPlaceElement, ePlaceElement);
									matchDescription.penalize(5);
									Mark.say(debug, "Case 1", ePathElement);
								}
								else {
									// Place types not the same
									transferThreadsAndProperties(pPathElement.getSubject(), ePlaceElement);
									matchDescription.penalize(10);
									Mark.say(debug, "Case 2", ePathElement);
								}
							}
						}
						else {
							// Looks like subject of path preposition is not a place element; assume it is the target

							if (null != StandardMatcher.getBasicMatcher().match(pPathElement.getSubject().getSubject(), dTarget)) {
								// At least the targets are compatible
								Mark.say(debug, "Case 3", ePathElement);
							}
							else {
								// Targets are not compatible either
								matchDescription.penalize(15);
								Mark.say(debug, "Case 4", ePathElement);
							}
							ePlaceElement.setSubject(pPathElement.getSubject());
							transferThreadsAndProperties(dPlaceElement, ePlaceElement);
						}
					}
				}
				if (!match) {
					// No match, have to synthesize entire path element
					ePlaceElement.setSubject(dTarget);
					transferThreadsAndProperties(dPlaceElement, ePlaceElement);
					transferThreadsAndProperties(dPathElement, ePathElement);
					Mark.say(debug, "Case 5", ePathElement);
				}
				mirror.addElement(ePathElement);
				// Mark.say("debug", Adding to mirror", ePathElement);
				datumElements.remove(d);
			}
		}

		for (Entity d : (Vector<Entity>) datumElements.clone()) {
			// It is not a path element, do ordinary match
			for (Entity p : (Vector<Entity>) pattern.getElements().clone()) {
				LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(p, d);
				if (match != null) {
					mirror.addElement(p);

					// Mark.say("Adding to mirror", p, "replacing", d);
					patternElements.remove(p);
					datumElements.remove(d);
					break;
				}
			}
		}

		for (Entity d : (Vector<Entity>) datumElements.clone()) {
			mirror.addElement(d);
		}

		Mark.say(debug, "Mirror size is", mirror.getElements().size(), mirror);

		matchDescription.setCopy(mirror);

		Mark.say(debug, "Sequence match produces", matchDescription);

		Mark.say("Mirror is", mirror);

		return matchDescription;

	}

	public static void transferThreadsAndProperties(Entity source, Entity target) {
		source.transferThreadsFeaturesAndProperties(target);
	}

	private boolean isPathElement(Entity entity) {
		return entity.functionP() && isPathType(entity);
	}

	private boolean isPathType(Entity entity) {
		return NewRuleSet.pathPrepositions.stream().anyMatch(p -> entity.getType().equals(p));
	}

	private boolean isPlaceElement(Entity entity) {
		return entity.functionP() && isPlaceType(entity);
	}

	private boolean isPlaceType(Entity entity) {
		return NewRuleSet.placePrepositions.stream().anyMatch(p -> entity.getType().equals(p));
	}

	public static void main(String[] ignore) throws Exception {
		Mark.say("Starting...");
		String p1 = "Add another block to the top of the tower";
		String p2 = "Add another block to the top of the tower";
		String p3 = "Add another block to the tower";
		String p4 = "Add another block to the top";
		String p5 = "Add another block";
		Entity precedent1 = BasicTranslator.getTranslator().translate(p1).get(0).getSubject();
		Entity precedent2 = BasicTranslator.getTranslator().translate(p2).get(0).getSubject();
		Entity precedent3 = BasicTranslator.getTranslator().translate(p3).get(0).getSubject();
		Entity precedent4 = BasicTranslator.getTranslator().translate(p4).get(0).getSubject();
		Entity precedent5 = BasicTranslator.getTranslator().translate(p5).get(0).getSubject();

		Entity nogood = BasicTranslator.getTranslator().translate("Add another block to the side of the tower").get(0);
		Entity statement = BasicTranslator.getTranslator().translate("Add another block to the bottom").get(0);
		Entity sansRole = BasicTranslator.getTranslator().translate("Add another block").get(0);

		Mark.say("P2", p2);
		Mark.say("P3", p3);
		Mark.say("P4", p4);
		Mark.say("P5", p5);

		Mark.say("\n>>>  P1", precedent1);

		Mark.say("P2 to P1", RoleFrameMatcher.getRoleFrameMatcher().match(precedent2, precedent1));

		Mark.say("P3 to P1", RoleFrameMatcher.getRoleFrameMatcher().match(precedent3, precedent1));

		Mark.say("P4 to P1", RoleFrameMatcher.getRoleFrameMatcher().match(precedent4, precedent1));

		Mark.say("P5 to P1", RoleFrameMatcher.getRoleFrameMatcher().match(precedent5, precedent1));

		// Mark.say(StandardMatcher.getBasicMatcher().match(precedent, statement));
		// Mark.say(StandardMatcher.getBasicMatcher().match(statement, precedent));
		//
		// Mark.say(StandardMatcher.getBasicMatcher().match(precedent, sansRole));
		// Mark.say(StandardMatcher.getBasicMatcher().match(sansRole, precedent));

		// Mark.say(RoleMatcher.getRoleMatcher().match(precedent, statement));

		// MatchDescription em1 = RoleFrameMatcher.getRoleFrameMatcher().match(nogood, precedent1);
		// MatchDescription em2 = RoleFrameMatcher.getRoleFrameMatcher().match(statement, precedent1);
		//
		// Mark.say("Side to top", em1.isMatch(), em1.getPenalty(), em1.getBindings());
		// Mark.say("Missing referent", em2.isMatch(), em2.getPenalty(), em2.getBindings());

		// Mark.say(RoleMatcher.getRoleMatcher().match(precedent, sansRole));
		// Mark.say(RoleMatcher.getRoleMatcher().match(sansRole, precedent).toLList());

	}

}

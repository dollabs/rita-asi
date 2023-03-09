package utils.tools;

import java.util.*;

import matchers.StandardMatcher;
import utils.Mark;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Dec 27, 2013
 * @author phw
 */

public class Predicates {

	/**
	 * Don't just test for equality, see if they match!
	 */
	public static boolean equals(Entity element1, Entity element2) {
		try {
			if (element1 == element2) {
				return true;
			}
			else if (StandardMatcher.getIdentityMatcher().match(element1, element2) != null) {
				// Mark.say("Match by matcher but not by equals:\n...", element1, "\n...", element2);
				return true;
			}
		}
		catch (Exception e) {
			Mark.say("Blew out of Predicates.equals", element1, element2);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Needed because wordnet considers help to be a cause.
	 */
	public static boolean isCause(Entity e) {
		if (e.relationP(Markers.CAUSE_MARKER) && e.isNotA(Markers.PREPARE_MARKER) && e.isNotA(Markers.HELP_MARKER)
		        && e.isNotA(Markers.MEANS)
		        && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isExplictCauseOrLeadsTo(Entity e) {
		return (isCause(e) && e.getType() == Markers.CAUSE_MARKER && !isGoalInference(e)) || isEntail(e);
	}

	public static boolean isInference(Entity e) {
		if (isCause(e)) {
			if (isPrediction(e)) {
				return true;
			}
			else if (isExplanation(e)) {
				return true;
			}
			else if (isPresumption(e)) {
				return true;
			}
			else if (isAbduction(e)) {
				return true;
			}
			else if (isEnabler(e)) {
				return true;
			}
			else if (isGoalInference(e)) {
			    return true;
			}
		}
		return false;
	}

	public static boolean isExplanation(Entity e) {
		if (isCause(e) && e.isA(Markers.EXPLANATION_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isPresumption(Entity e) {
		if (isCause(e) && e.isA(Markers.PRESUMPTION_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isEnabler(Entity e) {
		if (isCause(e) && e.isA(Markers.ENABLER_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	/**
	 * Entail is just a relation indicating first and last part of leads-to; no conjunction
	 */
	// public static boolean isEntail(Entity e) {
	// if (isCause(e) && e.isA(Markers.ENTAIL_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
	// return true;
	// }
	// return false;
	// }

	public static boolean isEntail(Entity e) {
		if (e.isA(Markers.ENTAIL_RULE)) {
			return true;
		}
		return false;
	}

	public static boolean isPrediction(Entity e) {
		if (isCause(e) && e.isA(Markers.PREDICTION_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isAbduction(Entity e) {
		if (isCause(e) && e.isA(Markers.ABDUCTION_RULE) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isMeans(Entity e) {
		if (e.isA(Markers.MEANS) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isLeadsTo(Entity e) {
		if (isCause(e) && e.isA(Markers.LEADS_TO) && e.getSubject().isA(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	public static boolean isCauseWord(Entity e) {
		if (e.isA(Markers.CAUSE_MARKER) && e.isNotA("move") && e.isNotA("help") && e.isNotA("prepare") && e.isNotA("trigger")) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if this is a story event or causal connection that has been inferred using ASPIRE
	 */
	public static boolean isGoalInference(Entity e) {
	    return e.hasProperty(Markers.GOAL_ANALYSIS, true);
	}

	public static boolean isAntecedentOfAbduction(Entity e) {
		Vector<Entity> sequences = e.getElementOf();
		if (!sequences.isEmpty()) {
			for (Entity x : sequences) {
				if (x.isA(Markers.CONJUNCTION)) {
					Vector<Function> candidates = x.getSubjectOf(Markers.ABDUCTION_RULE);
					if (!candidates.isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isAction(Entity e) {
		return e.relationP(Markers.ACTION_MARKER);
	}

	public static boolean thingRelationP(Entity e, String type) {
		if (!e.relationP()) {
			return false;
		}
		if (
		e.getBundle().stream().anyMatch(x -> {
			if (x.get(0).equals(Markers.THING_WORD) && x.contains(type)) {
				return true;
			}
			return false;
		})) {
			return true;
		}

		return false;
	}

	public static boolean actionRelationP(Entity e, String type) {
		if (!e.relationP()) {
			return false;
		}
		e.getBundle().stream().anyMatch(x -> {
			if (x.get(0).equals(Markers.ACTION_MARKER) && x.contains(type)) {
				return true;
			}
			return false;
		});
		return false;
	}

	/*
	 * Don't just test for equality, see if they match!
	 */
	public static boolean contained(Entity element, Collection<Entity> antecedents) {
		for (Entity antecedent : antecedents) {
			if (equals(element, antecedent)) {
				// if (StandardMatcher.getIdentityMatcher().match(element, antecedent) != null) {
				return true;
			}
		}
		return false;
	}

	/*
	 * Needed because sometimes tag may be on object of causal connection
	 */
	public static boolean isSometimes(Entity e) {
		if (e.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
			return true;
		}
		else if (e.getObject() != null && e.getObject().hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
			return true;
		}
		return false;
	}

	public static String isTimeConstraint(Entity e) {
		if (e.isA(Markers.BEFORE)) {
			return Markers.BEFORE;
		}
		else if (e.isA(Markers.AFTER)) {
			return Markers.AFTER;
		}
		return null;
	}

	/*
	 * Included because isSometimes is defined
	 */
	public static boolean isCheck(Entity e) {
		if (e.hasProperty(Markers.IDIOM, Markers.CHECK)) {
			return true;
		}
		return false;
	}

	public static boolean embedded(Entity x, Entity container) {
		if (x == container) {
			return true;
		}
		else if (container.entityP()) {
			return false;
		}
		else if (container.functionP()) {
			return embedded(x, container.getSubject());
		}
		else if (container.relationP()) {
			return embedded(x, container.getSubject()) || embedded(x, container.getObject());
		}
		// Must be sequence
		else {
			return container.stream().anyMatch(e -> embedded(x, e));
		}
	}

}

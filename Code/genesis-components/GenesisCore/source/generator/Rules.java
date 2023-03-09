package generator;

import java.util.List;


import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import utils.Mark;

/*
 * Created on Mar 14, 2015
 * @author phw
 */

public class Rules extends TransitionFrames {

	// Cause and rule constructors

	public static Relation makeCause(Entity consequent, List<Entity> antecedents) {
		int length = antecedents.size();
		Entity[] antecedentArray = new Entity[length];
		for (int i = 0; i < length; ++i) {
			antecedentArray[i] = antecedents.get(i);
		}
		return makeCause(consequent, antecedentArray);
	}

	public static Relation makeCause(Entity consequent, Entity... args) {
		Relation causalRelation = new Relation(Markers.CAUSE_MARKER, makeConjunction(args), consequent);
		return causalRelation;
	}

	public static Relation makeCause(Entity consequent, Sequence antecedents) {
		Relation causalRelation = new Relation(Markers.CAUSE_MARKER, antecedents, consequent);
		return causalRelation;
	}

	public static Relation makePredictionRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.PREDICTION_RULE);
		return cause;
	}

	public static Relation makeExpectationRule(double probability, Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.EXPECTATION_RULE);
		cause.addProperty(Markers.PROBABILITY, probability);
		return cause;
	}

	public static Relation makeExplanationRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.EXPLANATION_RULE);
		return cause;
	}

	public static Relation makeProximityRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.PROXIMITY_RULE);
		return cause;
	}

	public static Relation makeCensorRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.CENSOR);
		return cause;
	}

	public static Relation makeLeadsToRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.ENTAIL_RULE);
		return cause;
	}

	// public static Relation makeLeadsToRelation(Entity consequent, Entity... antecedents) {
	// Relation cause = makeCause(consequent, makeConjunction(antecedents));
	// cause.addType(Markers.LEADS_TO);
	// return cause;
	// }

	public static Relation makeUnknowableLeadsToRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.ENTAIL_RULE);
		cause.addType(Markers.UNKNOWABLE_ENTAIL_RULE);
		return cause;
	}

	public static Relation makeEnablerRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.INFERENCE_RULE);
		cause.addType(Markers.ENABLER_RULE);
		return cause;
	}

	public static Relation makePresumptionRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.EXPLANATION_RULE);
		cause.addType(Markers.PRESUMPTION_RULE);
		return cause;
	}

	public static Relation makeAbductionRule(Entity consequent, Entity... antecedents) {
		Relation cause = makeRule(consequent, makeConjunction(antecedents));
		cause.addType(Markers.ABDUCTION_RULE);
		return cause;
	}

	// Auxiliaries

	private static Relation makeRule(Entity consequent, Sequence antecedents) {
		Relation cause = new Relation(Markers.CAUSE_MARKER, antecedents, consequent);
		Thread thread = cause.getPrimedThread();
		thread.add(0, Markers.RULE);
		return cause;
	}

	private static Sequence makeConjunction(Entity[] entities) {
		if (false && entities.length == 1 && entities[0].sequenceP()) {
			return (Sequence) entities[0];
		}
		Sequence s = new Sequence(Markers.CONJUNCTION);
		for (Entity e : entities) {
			s.addElement(e);
		}
		return s;
	}

	private static Sequence makeRecipe(Entity[] entities) {
		Sequence s = makeConjunction(entities);
		s.addType(Markers.RECIPE);
		return s;
	}

	// Plain connections, not rules

	public static Relation makeMeans(Entity consequent, Entity... args) {
		Relation meansRelation = new Relation(Markers.CAUSE_MARKER, makeRecipe(args), consequent);
		meansRelation.addType(Markers.MEANS);
		return meansRelation;
	}
}

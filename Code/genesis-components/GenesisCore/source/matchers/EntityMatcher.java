package matchers;

import java.util.*;

import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.representations.*;
import start.Start;
import translator.Translator;
import utils.Mark;

/**
 * The goal of this EntityMatcher is to provide a standardized, simple way of doing matching between semantic entities
 * within Genesis Upon completion this matcher will be able to replace all the existing&conflicting matchers currently
 * within genesis Matching Modes which should be supported: - StructureMatch, returns a boolean indicating whether the
 * elements being compared are structurally identical - BindingMatch, returns a list of bindings between the entities of
 * the elements, if possible - ScoreMatch, returns a score between 0 and 1 representing how good of match the two
 * elements are Known missing features: prepositions, etc.
 * 
 * @author Matthew Fay
 * @date 2013
 */
public class EntityMatcher {
	
	public enum MatchMode {
		BASIC, SCORE
	}

	public MatchMode patternMatchMode = MatchMode.BASIC;

	public float score_cutoff = 0.1f;

	protected ThreadMatcher threadMatcher = new ThreadMatcher();

	public ThreadMatcher getThreadMatcher() {
		return threadMatcher;
	}

	// Use first element as a pattern instead of looking for exact match
	protected boolean sequencePatternMatch = true;

	protected boolean strictTypeMatching = true;

	protected boolean includeAllBindings = false;

	// Deals with adjectives and noun modifiers.


	public void includeAll() {
		includeAllBindings = true;
	}

	public void includeOnlyEnitites() {
		includeAllBindings = false;
	}

	public void useScoreMatching() {
		threadMatcher.useScoreMatching();
		patternMatchMode = MatchMode.SCORE;
	}

	public void useIdentityMatching() {
		threadMatcher.useIdentityMatching();
	}

	/**
	 * The match function does the vast majority of the grunt work for comparing two elements.
	 * 
	 * @param pattern
	 * @param datum
	 * @return
	 */
	public synchronized EntityMatchResult match(Entity pattern, Entity datum) {
		boolean debug = false;
		Mark.say(debug, "Trying to match\n ", pattern, "\n ", datum);


		// Mark.say("Note that", pattern.entityP(Markers.SEQUENCE_MARKER), datum.sequenceP());

		// Mark.say("Working on pattern", pattern.isA(Markers.ENTITY_MARKER), pattern.isA(Markers.FUNCTION_MARKER),
		// pattern.isA(Markers.RELATION_MARKER));

		// Mark.say("Working on datum ", datum.entityP(), datum.functionP(), datum.relationP());

		// If either element is null, the MatchResult is undefined
		if (pattern == null || datum == null) return new EntityMatchResult();

		boolean hasNotFeature1 = pattern.hasFeature(Markers.NOT);
		boolean hasNotFeature2 = datum.hasFeature(Markers.NOT);
		boolean inversion = (hasNotFeature1 != hasNotFeature2);


		ThreadMatchResult threadMatch = threadMatcher.match(pattern, datum);

		EntityMatchResult result = new EntityMatchResult();

		// Special case for when names must match
		if (pattern.getBooleanProperty(Markers.REQUIRE_NAME_MATCH)) {
			if (pattern.isA(Markers.NAME) && datum.isA(Markers.NAME)) {
				if (pattern.getType().equalsIgnoreCase(datum.getType())) {
					List<BindingPair> bindings = new ArrayList<>();
					bindings.add(new BindingPair(pattern, datum, threadMatch.score));
					result = new EntityMatchResult(1, inversion, true, bindings);
					Mark.say(debug, "Named entities match", pattern, datum);
				}
				else {
					result = new EntityMatchResult();
					Mark.say(debug, "Named entities do not match", pattern, datum);
				}
			}
			else {
				result = new EntityMatchResult();
				Mark.say(debug, "Named entities do not match", pattern, datum);
			}
		}

		// Special cases for catching variables
		else if (pattern.entityP(Markers.VARIABLE_MARKER)) {
			Mark.say(debug, "Matched variable", pattern.getType());
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, 1.0));
			result = new EntityMatchResult(1, inversion, true, bindings);
		}
		else if (pattern.entityP(Markers.ENTITY_MARKER) && datum.entityP()) {
			Mark.say(debug, "Matched entity", pattern.getType());
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, 1.0));
			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		else if (pattern.entityP(Markers.FUNCTION_MARKER) && datum.functionP()) {
			Mark.say(debug, "Matched function", pattern.getType());
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, 1.0));
			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		else if (pattern.entityP(Markers.RELATION_MARKER) && datum.relationP()) {
			Mark.say(debug, "Matched relation", pattern.getType());
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, 1.0));
			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		else if (pattern.entityP(Markers.SEQUENCE_MARKER) && datum.sequenceP()) {
			Mark.say(debug, "Matched sequence", pattern.getType());
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, 1.0));
			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		// Special Case for catching abstract actions
		else if ((pattern.entityP() || datum.entityP()) && pattern.isAPrimed(Markers.ACTION_MARKER) && datum.isAPrimed(Markers.ACTION_MARKER)) {
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, threadMatch.score));

			result = new EntityMatchResult(1, inversion, true, bindings);
		}
		// Special Case for matching the appearance of an action
		else if ((pattern.functionP("appear") && pattern.getSubject().entityP() && pattern.getSubject().isA(Markers.ACTION_MARKER))
		        && (datum.isA(Markers.ACTION_MARKER))) {
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern.getSubject(), datum, threadMatch.score));

			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		// Experimental case for matching generics
		else if ((pattern.entityP("something") || datum.entityP("something"))
		        && (pattern.relationP() || datum.relationP() || pattern.entityP() || datum.entityP() || pattern.functionP() || datum.functionP())) {
			// Generic Relation match
			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, threadMatch.score));

			result = new EntityMatchResult(1, inversion, true, bindings);
		}

		// Special Case for catching entity's performing actions
		else if ((pattern.relationP(Markers.PERFORM)) && datum.getSubject() != null && datum.getSubject().isNotA("you")) {
			Entity performElt = pattern;
			Entity actionElt = datum;

			Entity pActor = performElt.getSubject();
			Entity pAction = RoleFrames.getObject(performElt);
			// Verify can get the action Object from perform frame

			Mark.say(debug, "pActor/pAction", pActor, pAction);

			Mark.say(debug, pAction, "is action marker", pAction.isA(Markers.ACTION_MARKER));

			if (pAction != null && pAction.isA(Markers.ACTION_MARKER)) {
				// Verify action is an action!
				Mark.say(debug, "Y");

				if (actionElt.isA(Markers.ACTION_MARKER) && (actionElt.relationP() || actionElt.functionP())) {

					Entity aActor = actionElt.getSubject();
					Entity aAction = actionElt;

					Mark.say(debug, "Z", aActor, aAction);

					List<BindingPair> bindings = new ArrayList<>();
					bindings.add(new BindingPair(pActor, aActor, 1));
					bindings.add(new BindingPair(pAction, aAction, 1));

					int score = 1;
					if (!mustCheck(pattern, datum)) {
						score = -1;
					}
					result = new EntityMatchResult(score, false, true, bindings);
				}
			}
		}

		// If both elements are Entities then we can do entity level
		// comparisons
		else if (pattern.entityP() && datum.entityP()) {
			// Mark.say(debug, "Comparing", pattern.toXML(), datum.toXML());

			List<BindingPair> bindings = new ArrayList<>();
			bindings.add(new BindingPair(pattern, datum, threadMatch.score));

			// ***

			// In this piece, I think I am binding and making use of owner property attached to entities
			// 17 Nov 2017 phw, modifying Matthew Fay's code

			Object ownerP = pattern.getProperty(Markers.OWNER_MARKER);

			Object ownerD = datum.getProperty(Markers.OWNER_MARKER);

			if (ownerP != null && ownerD != null && threadMatch.score > 0) {
				Mark.say(debug, "\n>>>  Pattern", pattern, "has owner", ownerP);
				Mark.say(debug, "Datum", datum, "has owner", ownerD);
				// // If no binding, make one
				ThreadMatchResult ownerMatch = threadMatcher.match(pattern, datum);
				//
				// Mark.say(debug, "Added binding:", ownerP, ownerD, threadMatch.score, ownerMatch.score);
				bindings.add(new BindingPair((Entity) ownerP, (Entity) ownerD, ownerMatch.score));

				Entity ownerPE = (Entity) ownerP;
				Entity ownerDE = (Entity) ownerD;

				EntityMatchResult ownerResult = match(ownerPE, ownerDE);

				Mark.say(debug, "Thread match score, match score", ownerMatch.score, ownerResult.score);

				threadMatch.score = Math.min(threadMatch.score, ownerMatch.score);

				Mark.say(debug, "Comparing owners", ownerPE, ownerDE, "yields", result.score);

			}

			// Note special case code for dealing with features added 21 Apr 2018 by phw

			ArrayList<Object> patternFeatures = pattern.getFeatures();
			ArrayList<Object> datumFeatures = datum.getFeatures();

			if (Switch.useFeaturesWhenMatching.isSelected() && datum.entityP() && pattern.entityP()) {
				// For now, all must match
				if (patternFeatures.size() != datumFeatures.size()) {
					threadMatch.score = -1;
				}
				else {
					for (Object feature : datumFeatures) {

						if (!patternFeatures.contains(feature)) {
							// Should not match in this case
							// Mark.say("Feature", feature, "not contained in", patternFeatures);
							threadMatch.score = -1;
							break;
						}

					}
				}
			}

			// Mark.say("Thread match score", threadMatch.score);
			result = new EntityMatchResult(threadMatch.score, inversion, true, bindings);

			Mark.say(debug, "Compared", pattern, datum, "yields", result.score);

		}

		// If both elements are functions
		else if (pattern.functionP() && datum.functionP()) {
			List<EntityMatchResult> results = new ArrayList<>();

			EntityMatchResult subjectResult = match(pattern.getSubject(), datum.getSubject());

			results.add(subjectResult);

			result = combineResults(threadMatch.score, inversion, results);
		}

		// If both elements are relations
		else if (pattern.relationP() && datum.relationP()) {

			List<EntityMatchResult> results = new ArrayList<>();

			EntityMatchResult objectResult = match(pattern.getObject(), datum.getObject());
			EntityMatchResult subjectResult = match(pattern.getSubject(), datum.getSubject());

			results.add(objectResult);
			results.add(subjectResult);
			
			if (!mustCheck(pattern, datum)) {
				threadMatch.score = -1;
			}



			result = combineResults(threadMatch.score, inversion, results);
		}

		// If both elements are sequences do this
		// Note that this is only required to match everything from the pattern (element1) not the datum (element2)
		// this is different than most the rest of the matcher where they are treated equivalently
		else if (pattern.sequenceP() && datum.sequenceP()) {
			List<EntityMatchResult> results = new ArrayList<>();

			boolean structureMatch = true;
			List<BindingPair> bindings = new ArrayList<>();

			if ((sequencePatternMatch && pattern.getNumberOfChildren() > datum.getNumberOfChildren())
			        || (!sequencePatternMatch && pattern.getNumberOfChildren() != datum.getNumberOfChildren())) {
				results.add(new EntityMatchResult());
			}

			for (Entity child1 : pattern.getElements()) {
				boolean found = false;
				EntityMatchResult childResult = null;
				EntityMatchResult bestResult = null;
				for (Entity child2 : datum.getElements()) {
					childResult = match(child1, child2);
					if (bestResult == null) bestResult = childResult;
					if (childResult.score > bestResult.score) bestResult = childResult;
				}
				if (bestResult != null && bestResult.score > 0) {
					results.add(bestResult);
				}
				else {
					results.add(new EntityMatchResult());
				}
			}

			if (results.isEmpty()) results.add(new EntityMatchResult(1, false, true, new ArrayList<BindingPair>()));

			result = combineResults(threadMatch.score, inversion, results);
		}

		if (includeAllBindings && !(pattern.entityP() && datum.entityP())) {
			// Add the current relation/function/seq
			result.bindings.add(new BindingPair(pattern, datum, threadMatch.score));
		}

		// If the elements do not have matching class types then there is
		// structural inconsistency that cannot be resolved at this time

		// If score mode, check for score threshold
		if (result.score < score_cutoff && patternMatchMode == MatchMode.SCORE) result.semanticMatch = false;

		// Return result

		Mark.say(debug, "Result", result);

		return result;
	}

	/**
	 * @param pattern
	 * @param datum
	 * @return
	 */
	private boolean mustCheck(Entity pattern, Entity datum) {
		if (Switch.useMustWhenMatching.isSelected()) {
			// For now, must match the musts
			if (Markers.MUST_WORD.equals(datum.getProperty(Markers.MODAL))) {
				if (!Markers.MUST_WORD.equals(pattern.getProperty(Markers.MODAL))) {
					return false;
				}
			}
			if (Markers.MUST_WORD.equals(pattern.getProperty(Markers.MODAL))) {
				if (!Markers.MUST_WORD.equals(datum.getProperty(Markers.MODAL))) {
					return false;
				}
			}
		}
		else {
			// Mark.say("Switch is", Switch.useMustWhenMatching.isSelected());
		}
		return true;
	}

	/**
	 * Combines the thread scores and all of the results from matching the children of elements
	 * 
	 * @param score
	 * @param results
	 * @return
	 */
	private EntityMatchResult combineResults(double score, boolean inversion, List<EntityMatchResult> results) {
		if (results.isEmpty()) return new EntityMatchResult();
		List<BindingPair> bindings = new ArrayList<>();
		boolean structureMatch = true;
		boolean match = true;
		boolean stackedInversion = false;
		for (EntityMatchResult result : results) {
			if (score >= 0) {
				if (result.score >= 0)
					score *= result.score;
				else {
					score = -1;
				}
			}
			if (result.inversion) stackedInversion = true;
			match = match && result.semanticMatch;
			bindings.addAll(result.bindings);
			structureMatch = structureMatch && result.structureMatch;
		}
		if (stackedInversion) inversion = !inversion;
		return new EntityMatchResult(match, score, inversion, structureMatch, bindings);
	}

	public static void main(String[] args) throws Exception {
		Mark.say("Initializing Start...");

		Translator basicTranslator = Translator.getTranslator();
		Generator generator = Generator.getGenerator();
		Start.getStart().setMode(Start.STORY_MODE);
		generator.flush();

		Mark.say("Doing translation...");

		Entity e1 = basicTranslator.translate("mary is a person").getElement(0);
		Entity e2 = basicTranslator.translate("john is a person").getElement(0);
		Entity e3 = basicTranslator.translate("sally is a person").getElement(0);
		Entity e4 = basicTranslator.translate("mark is a person").getElement(0);
		Entity e5 = basicTranslator.translate("Mary thinks Mark likes Sally if John tells Mary").getElement(0);
		Entity e6 = basicTranslator.translate("If john tells mary something then mary thinks something.").getElement(0);
		Entity e7 = basicTranslator.translate("John tells Mary, \"Mark likes Sally\".").getElement(0);
		Entity e8 = basicTranslator.translate("John tells Mary something.").getElement(0);
		Mark.say(e1);
		Mark.say(e2);
		Mark.say(e3);
		Mark.say(e4);
		Mark.say(e5);
		Mark.say(e6);
		Mark.say(e7);
		Mark.say(e8);
	}
}

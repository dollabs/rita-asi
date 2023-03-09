package matchers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bryanWilliams.Pair;
import bryanWilliams.Util;
import conceptNet.conceptNetModel.ConceptNetJustification;
import conceptNet.conceptNetModel.ConceptNetQueryJustification;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import constants.Markers;
import frames.entities.Entity;
import generator.RoleFrames;
import genesis.GenesisControls;
import matchers.original.BasicMatcherOriginal;
import matchers.representations.*;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/**
 * Encapsulates the new EntityMatcher, BindingValidator, and ThreadMatcher Provides easy access to common matching
 * functionality (such as binding lists) Replaces BasicMatcher, IdentityMatcher, and soon more (ScoreMatcher, etc.)
 * Currently uses a debugging switch to fallback on BasicMatcher as necessary See DemoMatcherAndSubstitutor for
 * examples.
 * 
 * @author Matthew
 * @date 2013
 */
public class StandardMatcher {
	private EntityMatcher em;

	public static final double CONCEPTNET_SIMILARITY_SCORE_CUTOFF = 0.330;
	
	private BindingValidator bv;

	private EntityMatcher getEntityMatcher() {
		return em;
	}

	private BindingValidator getBindingValidator() {
		return bv;
	}

	// Instantiates a StandardMatcher
	public StandardMatcher() {
		em = new EntityMatcher();
		bv = new BindingValidator();
	}

	// These getters are utility functions for getting old matcher functionalities
	private static StandardMatcher standardMatcher;

	/**
	 * Provides a StandardMatcher that provides typical matching functionality
	 * 
	 * @return Standard Matcher
	 */
	public static StandardMatcher getBasicMatcher() {
		if (standardMatcher == null) {
			standardMatcher = new StandardMatcher();
		}
		standardMatcher.getEntityMatcher().getThreadMatcher().searchAllThreads(GenesisControls.matchAllThreads.isSelected());
		return standardMatcher;
	}

	private static StandardMatcher identityMatcher;

	/**
	 * Provides a StandardMatcher that requires named entities to only match against entities of the same name.
	 * 
	 * @return Identity Matcher
	 */
	public static StandardMatcher getIdentityMatcher() {
		if (identityMatcher == null) {
			identityMatcher = new StandardMatcher();
			identityMatcher.getEntityMatcher().useIdentityMatching();
		}
		getBasicMatcher().getEntityMatcher().getThreadMatcher().searchAllThreads(GenesisControls.matchAllThreads.isSelected());
		return identityMatcher;
	}

	/**
	 * Shortcut method for enabling identity matching Identity matching requires that matched, named entities share a
	 * name
	 */
	public void useIdentityMatching() {
		em.useIdentityMatching();
	}
	
	/**
	 * Matches the two strings for similarity using ConceptNet.
	 */
	public static ConceptNetQueryResult<Double> similarityMatch(String s1, String s2) {
	    return ConceptNetClient.getSimilarityScore(s1, s2);
	}
	
	/**
	 * Decides whether the two strings are similar by using ConceptNet and comparing it to a cutoff value.
	 */
	public static boolean areSimilar(String s1, String s2) {
	    return similarityMatch(s1, s2).getResult() >= CONCEPTNET_SIMILARITY_SCORE_CUTOFF;
	}
	
	// Now the good stuff

	/**
	 * Matches two entities based on structure alone Historical, emulates BasicMatcher
	 * 
	 * @param pattern
	 * @param datum
	 * @return True on identical structures, false otherwise
	 */
	public boolean matchStructures(Entity pattern, Entity datum) {
		EntityMatchResult result = em.match(pattern, datum);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			boolean old_result = BasicMatcherOriginal.getBasicMatcher().matchStructures(pattern, datum);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (old_result != result.structureMatch) {
					Mark.err("Difference in matching detected!");
					Mark.say(pattern);
					Mark.say(datum);
					Mark.say("New Result: " + result.structureMatch);
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}
		return result.structureMatch;
	}

	/**
	 * Checks if an instantiation matches a particular rule. Historical, emulates BasicMatcher, may be out of
	 * date/unneeded?
	 * 
	 * @param pattern
	 * @param datum
	 * @return Binding list on match, null otherwise
	 */
	public LList<PairOfEntities> matchRuleToInstantiation(Entity rule, Entity instantiation) {
		EntityMatchResult object_result = em.match(rule.getObject(), instantiation.getObject());
		object_result.bindings = bv.validateBindings(object_result.bindings);
		if (object_result.bindings == null) return null;
		EntityMatchResult subject_result = em.match(rule.getSubject(), instantiation.getSubject());
		subject_result.bindings = bv.validateBindings(subject_result.bindings, object_result.bindings);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			LList<PairOfEntities> old_result = BasicMatcherOriginal.getBasicMatcher().matchRuleToInstantiation(rule, instantiation);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (!BindingValidator.equivalencyCheck(subject_result.toLList(), old_result)) {
					Mark.err("Difference in matching detected!");
					Mark.say(rule);
					Mark.say(instantiation);
					Mark.say("New Result: " + subject_result.toLList());
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}

		return subject_result.toLList();
	}

	/**
	 * Checks if a datum matches a particular pattern Historical, emulates BasicMatcher
	 * 
	 * @param pattern
	 * @param datum
	 * @return Binding list on match, null otherwise
	 */
	public LList<PairOfEntities> match(Entity pattern, Entity datum) {
		EntityMatchResult result = em.match(pattern, datum);
		result.bindings = bv.validateBindings(result.bindings);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			LList<PairOfEntities> old_result = BasicMatcherOriginal.getBasicMatcher().match(pattern, datum);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (!BindingValidator.equivalencyCheck(result.toLList(), old_result)) {
					Mark.err("Difference in matching detected!");
					Mark.say(pattern);
					Mark.say(datum);
					Mark.say("New Result: " + result.toLList());
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}

		return result.toLList();
	}

	/**
	 * Checks if a role frame pattern matches a role frame datum. Returns null if no match, otherwise roles left over in
	 * the form of a list of functions.
	 */
	public List<Entity> matchAndReportExcess(Entity pattern, Entity datum) {
		if (!RoleFrames.isRoleFrame(pattern) || !RoleFrames.isRoleFrame(datum)) {
			return null;
		}
		EntityMatchResult result = em.match(pattern, datum);
		result.bindings = bv.validateBindings(result.bindings);
		if (!result.isMatch()) {
			return null;
		}

		List<PairOfEntities> bindings = result.toLList().toList();
		List<Entity> patternRoles = pattern.getObject().getElements();
		List<Entity> datumRoles = datum.getObject().getElements();
		List<Entity> excess = new ArrayList<>();
		for (Entity datumRole : datumRoles) {
			boolean missing = true;
			String datumRoleType = datumRole.getType();
			Entity datumEntity = datumRole.getSubject();
			for (Entity patternRole : patternRoles) {
				String patternRoleType = patternRole.getType();
				Entity patternEntity = patternRole.getSubject();
				if (patternRoleType.equals(datumRoleType)) {
					if (datumEntity == getAssignment(patternEntity, bindings)) {
                	  missing = false;
                	  break;
                  }
				}
			}
			if (missing) {
				excess.add(datumRole);
			}
		}
		return excess;
	}

	private Entity getAssignment(Entity key, List<PairOfEntities> bindings) {
		for (PairOfEntities p : bindings) {
			if (key == p.getPattern()) {
				return p.getDatum();
			}
		}
		return null;
	}

	/**
	 * Checks if a datum matches a particular pattern Uses given bindings as constraints Historical, emulates
	 * BasicMatcher. If no match found, returns null
	 * 
	 * @param pattern
	 * @param datum
	 * @param constraints
	 * @return
	 */
	public LList<PairOfEntities> match(Entity pattern, Entity datum, LList<PairOfEntities> constraints) {
		EntityMatchResult result = em.match(pattern, datum);

		result.bindings = bv.validateBindings(result.bindings, constraints);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			LList<PairOfEntities> old_result = BasicMatcherOriginal.getBasicMatcher().match(pattern, datum, constraints);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (!BindingValidator.equivalencyCheck(result.toLList(), old_result)) {
					Mark.err("Difference in matching detected!");
					Mark.say(pattern);
					Mark.say(datum);
					Mark.say("New Result: " + result.toLList());
					Mark.say("Old Result: " + old_result);
					old_result = BasicMatcherOriginal.getBasicMatcher().match(pattern, datum, constraints);
					result = em.match(pattern, datum);
					result.bindings = bv.validateBindings(result.bindings, constraints);
				}
			}
			return old_result;
		}

		return result.toLList();
	}
	
	// Leaves markers untouched since normal matching neither creates nor destroys binding markers
	public BindingsWithProperties match(Entity pattern, Entity datum, BindingsWithProperties constraints) {
	    LList<PairOfEntities> matchResult = match(pattern, datum, constraints.getBindings());
	    if (matchResult == null) {
	        return null;
	    }
	    return constraints.withReplacedBindings(matchResult);
	}
    
	/**
	 * Generates justifications describing the similarity between relation types in the StructureMapping.
	 * Returns an empty list if the mapping does not contain any similar relations.
	 */
    private static List<ConceptNetJustification> relationSimilarityJustification(StructureMapping mapping) {
        List<ConceptNetQueryResult<Double>> relationSimilarityResults = mapping.getMapping().stream()
                .map(Pair::getObjs)
                .filter(objs -> objs.get(0).relationP() && objs.get(1).relationP())
                // convert each (rel1, rel2) to (relType1, relType2)
                .map(objs -> objs.stream().map(Entity::getType).collect(Collectors.toList()))
                .map(relTypes -> StandardMatcher.similarityMatch(relTypes.get(0), relTypes.get(1)))
                .collect(Collectors.toList());
        boolean allRelationsSimilar = relationSimilarityResults.stream()
                .allMatch(result -> result.getResult() >= CONCEPTNET_SIMILARITY_SCORE_CUTOFF);
        if (!allRelationsSimilar) {
            return Collections.emptyList();
        }
        return relationSimilarityResults.stream()
                .map(ConceptNetQueryJustification<Double>::new)
                .collect(Collectors.toList());
    }
    
    // for a structure mapping to be valid, each proper name must be bound to another proper name, and the non-proper-name
    // entity pairs have to match using traditional matching
    private boolean isValidStructureMapping(LList<PairOfEntities> bindings, LList<PairOfEntities> constraints) {
        List<BindingPair> bindingPairs = BindingValidator.convertFromLList(bindings);
        return bindingPairs.stream().allMatch(pair -> StructureMapper.boundEntitiesAgree(pair.getPattern(), pair.getDatum()))
                && (bv.validateBindings(BindingValidator.convertFromLList(bindings), constraints) != null);
    }
	
    /**
     * A more flexible form of matching. Allows for both traditional matching and matching based on relation type
     * similarity, e.g. "Matt tortures Josh" can match "Matt harms Josh." Uses ConceptNet.
     * 
     * This is the "Common Sense Enabled Rule Matching (CSERM)" referred to in Bryan Williams' M.Eng thesis
     * (http://groups.csail.mit.edu/genesis/papers/2017%20Bryan%20Williams.pdf) - see Chapter 5
     */
    @SuppressWarnings("unchecked")
    public BindingsWithProperties similarityMatch(Entity pattern, Entity datum, BindingsWithProperties constraints) {
        // if pattern and datum match using traditional matching, return that result
	    BindingsWithProperties normalMatchResults = match(pattern, datum, constraints);
	    if (normalMatchResults != null) {
	        return normalMatchResults;
	    }
	    
	    List<StructureMapping> allValidMappings = StructureMapper.getStructureMappings(pattern, datum)
	            .getMappings().stream()
	            .filter(mapping -> isValidStructureMapping(mapping.getPatternDatumEntityPairs(pattern), constraints.getBindings()))
	            .collect(Collectors.toList());
	    // each valid mapping is mapped to a corresponding list of justifications of how its relations are similar.
	    // if an (inner) list is empty, its relations are not similar
	    List<List<ConceptNetJustification>> possibleJustifications = allValidMappings.stream()
	            .map(StandardMatcher::relationSimilarityJustification)
	            .collect(Collectors.toList());
	    // if an inner list isn't empty, its relations are similar
	    if (possibleJustifications.stream().filter(list -> !list.isEmpty()).count() > 1) {
	        Mark.err("Haven't yet handled multiple valid similarity mappings! Pattern:", pattern, "; Datum: ", datum);
	    }
	    // pick the first nonempty justification list
	    int earliestJustifiableMappingIndex = IntStream.range(0, possibleJustifications.size())
	            .filter(i -> possibleJustifications.get(i).size() > 0)
	            .findFirst()
	            .orElse(-1);
	    // no justifiable mappings
	    if (earliestJustifiableMappingIndex == -1) {
	        return null;
	    }	    
	    StructureMapping similarMapping = allValidMappings.get(earliestJustifiableMappingIndex);
	    List<ConceptNetJustification> justification = possibleJustifications.get(earliestJustifiableMappingIndex);
	    LList<PairOfEntities> newBindings = similarMapping.getPatternDatumEntityPairs(pattern);
	    // update constraints to contain additional bindings and mark it with the property that 
	    // a similarity match was used
	    BindingsWithProperties similarityMatchResults = constraints.withAdditionalBindings(newBindings)
	            .addProperty(Markers.CONCEPTNET_SIMILARITY_MATCH, true, true);
	    // add justifications to list if already present, or create list with justifications if not
	    if (similarityMatchResults.hasProperty(Markers.CONCEPTNET_JUSTIFICATION)) {
	    	        List<ConceptNetJustification> newJustification = new ArrayList<>(
	                (List<ConceptNetJustification>) similarityMatchResults
	                .getValue(Markers.CONCEPTNET_JUSTIFICATION));
	        newJustification.addAll(justification);
	        similarityMatchResults.setValue(Markers.CONCEPTNET_JUSTIFICATION, newJustification);
	    } else {
	        similarityMatchResults = similarityMatchResults.addProperty(Markers.CONCEPTNET_JUSTIFICATION, justification, true);
	    }
	    return similarityMatchResults;
	}

	/**
	 * Checks if a datum matches a particular pattern Includes all levels of recursion in binding list Historical,
	 * emulates BasicMatcher Probably horribly not thread safe, don't run this function simultaneously with the other
	 * ones from the same object on separate threads (rare conditions)
	 * 
	 * @param pattern
	 * @param datum
	 * @return Binding list on match, null otherwise
	 */
	public LList<PairOfEntities> matchAll(Entity pattern, Entity datum) {
		em.includeAll();
		EntityMatchResult result = em.match(pattern, datum);
		em.includeOnlyEnitites();
		result.bindings = bv.validateBindings(result.bindings);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			LList<PairOfEntities> old_result = BasicMatcherOriginal.getBasicMatcher().matchAll(pattern, datum);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (!BindingValidator.equivalencyCheck(result.toLList(), old_result)) {
					Mark.err("Difference in matching detected!");
					Mark.say(pattern);
					Mark.say(datum);
					Mark.say("New Result: " + result.toLList());
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}

		return result.toLList();
	}

	/**
	 * Checks if a datum matches a particular pattern, with a negation detected Uses given bindings as constraints NOTE:
	 * For some reason datum and pattern are reversed for this one for backwards compatability, this should be fixed!
	 * 
	 * @param datum
	 * @param pattern
	 * @param constraints
	 * @return
	 */
	public LList<PairOfEntities> matchNegation(Entity pattern, Entity datum, LList<PairOfEntities> constraints) {
		EntityMatchResult result = em.match(pattern, datum);

		result.bindings = bv.validateBindings(result.bindings, constraints);

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			LList<PairOfEntities> old_result = BasicMatcherOriginal.getBasicMatcher().matchNegation(datum, pattern, constraints);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (!BindingValidator.equivalencyCheck(result.toNegationLList(), old_result)) {
					Mark.err("Difference in matching detected!");
					Mark.say(pattern);
					Mark.say(datum);
					Mark.say("New Result: " + result.toLList());
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}

		return result.toNegationLList();
	}
	
    // Leaves markers untouched since normal matching neither creates nor destroys markers
	public BindingsWithProperties matchNegation(Entity pattern, Entity datum, BindingsWithProperties constraints) {
	    LList<PairOfEntities> matchResult = matchNegation(pattern, datum, constraints.getBindings());
	    if (matchResult == null) {
	        return null;
	    }
	    return constraints.withReplacedBindings(matchResult);
	}

	/**
	 * This function tries to find a match of the pattern anywhere within the datum, it's unclear if this should really
	 * be a part of standard matcher, but it's implemented here for backwards compatibility purposes
	 * 
	 * @param pattern
	 * @param datum
	 * @return
	 */
	public LList<PairOfEntities> matchAnyPart(Entity pattern, Entity datum) {
		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				Mark.err("No validation check available for matchAnyPart");
			}
			return BasicMatcherOriginal.getBasicMatcher().matchAnyPart(pattern, datum);
		}

		if (datum == null || pattern == null) return null;

		EntityMatchResult result = em.match(pattern, datum);
		result.bindings = bv.validateBindings(result.bindings);
		if (result.toLList() != null) return result.toLList();

		// Can't recurse
		if (datum.entityP()) return null;

		if (datum.sequenceP()) {
			for (Entity e : datum.getElements()) {
				result = em.match(pattern, e);
				result.bindings = bv.validateBindings(result.bindings);
				if (result.toLList() != null) return result.toLList();
			}
		}

		if (datum.relationP()) {
			Entity e = datum.getSubject();
			result = em.match(pattern, e);
			result.bindings = bv.validateBindings(result.bindings);
			if (result.toLList() != null) return result.toLList();
		}

		if (datum.functionP() || datum.relationP()) {
			Entity e = datum.getSubject();
			result = em.match(pattern, e);
			result.bindings = bv.validateBindings(result.bindings);
			if (result.toLList() != null) return result.toLList();
		}

		return null;
	}

	/**
	 * Unclear if used, but returns a modified score based on the results of match all
	 * 
	 * @param imaginedDescription
	 * @param rememberedDescription
	 * @return
	 */
	public double distance(Entity imaginedDescription, Entity rememberedDescription) {
		em.includeAll();
		EntityMatchResult result = em.match(imaginedDescription, rememberedDescription);
		em.includeOnlyEnitites();

		double distance = 0;
		for (BindingPair pair : result.bindings) {
			distance += pair.getScore();
		}

		if (!GenesisControls.useNewMatcherCheckBox.isSelected()) {
			double old_result = BasicMatcherOriginal.getBasicMatcher().distance(imaginedDescription, rememberedDescription);
			if (GenesisControls.reportMatchingDifferencesCheckBox.isSelected()) {
				if (distance != old_result) {
					Mark.err("Difference in matching detected!");
					Mark.say("NOTE: Distances not gaurenteed to be equivalent, just compatable");
					Mark.say(imaginedDescription);
					Mark.say(rememberedDescription);
					Mark.say("New Result: " + distance);
					Mark.say("Old Result: " + old_result);
				}
			}
			return old_result;
		}

		return distance;
	}

	public static void main(String[] ignore) throws Exception {
		// Establish types of elements used in example
		Translator.getTranslator().translate("I am a person");
		// Translator.getTranslator().translate("xx is a person");
		// Translator.getTranslator().translate("yy is a person");
		// Translator.getTranslator().translate("zz is an entity");
		// Translator.getTranslator().translate("Macbeth is a person");
		Translator.getTranslator().translate("Duncan is a person");
		Translator.getTranslator().translate("Patrick is a person");
		// Translator.getTranslator().translate("Boris is a person");
		// Translator.getTranslator().translate("Who is a person");
		// Translator.getTranslator().translate("Whom is a person");
		// Translator.getTranslator().translate("King is a kind of noble");
		// Translator.getTranslator().translate("Dead is a kind of property");

		// Create entity. Not the get(0). Usually there is only one element in the sequence generated by the translator,
		// but there may be more. If for
		// example, there is a compound sentence "Macbeth kills Duncan and Sam", or if the translater does not
		// completely succeed. Usually what you want is in position 0.

		// Entity x1 = Translator.getTranslator().translate("Macbeth attacks Duncan").getElements().get(0);
		// Entity x2 = Translator.getTranslator().translate("Patrick attacks zz").getElements().get(0);
		//
		// Entity e1 = Translator.getTranslator().translate("Macbeth kills Duncan").getElements().get(0);
		//
		// Entity e2 = Translator.getTranslator().translate("Patrick kills Boris").getElements().get(0);
		//
		// Entity e3 = Translator.getTranslator().translate("xx kills yy").getElements().get(0);
		// Entity e4 = Translator.getTranslator().translate("xx loves yy").getElements().get(0);
		//
		// Entity e5 = Translator.getTranslator().translate("who kills Duncan").getElements().get(0);
		//
		// Entity e6 = Translator.getTranslator().translate("who kills whom").getElements().get(0);
		//
		// Entity m1 = Translator.getTranslator().translate("Macbeth became king").getElements().get(0);
		// Entity m2 = Translator.getTranslator().translate("Macbeth became dead").getElements().get(0);
		// Translator.getTranslator().translate("Macbeth became dead").getElements().get(0);
		// Entity t1 = Translator.getTranslator().translate("Patrick threw a ball").getElements().get(0);
		// Entity t2 = Translator.getTranslator().translate("Patrick threw a box").getElements().get(0);
		// Entity t3 = Translator.getTranslator().translate("Patrick threw a brick").getElements().get(0);
		//
		// Entity o1 = RoleFrames.getObject(t1);
		// Entity o2 = RoleFrames.getObject(t2);
		// Entity o3 = RoleFrames.getObject(t3);

		Entity q1 = Translator.getTranslator().translate("A duck quacked.").getElements().get(0);
		Entity q2 = Translator.getTranslator().translate("A animal quacked.").getElements().get(0);

		q1 = Translator.getTranslator().translate("I hit Patrick.").getElements().get(0);
		q2 = Translator.getTranslator().translate("Duncan hit Patrick.").getElements().get(0);

		// // Remove "did:
		// e5 = e5.getSubject();
		// Mark.say("E5 is", e5);
		//
		// Entity test = e1.getSubject();
		//
		// Mark.say("Test is", test, test.getName(), test.getType());
		//
		// Mark.say("XML is", test.toXML());
		//
		// LList<PairOfEntities> result = StandardMatcher.getBasicMatcher().match(e1, e2);
		// Mark.say("Result of matching e1 with e2", result);
		// // result = StandardMatcher.getBasicMatcher().match(e3, e2);
		// // Mark.say("Result of matching e3 with e2", result);
		// // result = StandardMatcher.getBasicMatcher().match(e4, e2);
		// // Mark.say("Result of matching e4 with e2", result);
		// //
		// // result = StandardMatcher.getBasicMatcher().match(e5, e1);
		// // Mark.say("Result of matching\n", e1, "\nwith\n", e5, "\n", result);
		// //
		// // result = StandardMatcher.getBasicMatcher().match(e6.getSubject(), e1);
		// // Mark.say("Result of matching\n", e1, "\nwith\n", e6, "\n", result);
		// //
		// result = StandardMatcher.getBasicMatcher().match(x1, x2);
		// Mark.say("Result of matching\n", x1, "\nwith\n", x2, "\n", result);
		// result = StandardMatcher.getBasicMatcher().match(x2, x1);
		// Mark.say("Result of matching\n", x2, "\nwith\n", x1, "\n", result);

		// LList<PairOfEntities> result = StandardMatcher.getBasicMatcher().match(o1, o2);
		// Mark.say("Result of matching o1 with o2", result);
		// result = StandardMatcher.getBasicMatcher().match(o1, o3);
		// Mark.say("Result of matching o1 with o3", result);
		//
		// result = StandardMatcher.getBasicMatcher().match(t1, t2);
		// Mark.say("Result of matching t1 with t2", result);
		// result = StandardMatcher.getBasicMatcher().match(t1, t3);
		// Mark.say("Result of matching t1 with t3", result);

		Translator t = Translator.getTranslator();
		t.translate("xx is a variable");
		t.translate("yy is a variable");
		t.translate("John is a person");

		q1 = Translator.getTranslator().translate("xx steals yy.").getElements().get(0);
		q2 = Translator.getTranslator().translate("John steals yy.").getElements().get(0);

		Mark.say(q1.toXML());
		Mark.say(q2.toXML());

		LList<PairOfEntities> result = StandardMatcher.getBasicMatcher().match(q1.getSubject(), q2.getSubject());
		Mark.say("Result of matching subjects of q1 with q2", result);

		// result = StandardMatcher.getBasicMatcher().match(q2.getSubject(), q1.getSubject());
		// Mark.say("Result of matching subjects of q2 with q1", result);

	}
}

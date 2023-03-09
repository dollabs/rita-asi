package rules;

import java.util.*;
import java.util.stream.Collectors;


import bryanWilliams.Util;
import bryanWilliams.Learning.ScoreSimilarityComparator;
import bryanWilliams.goalAnalysis.AspireEngine;
import connections.*;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.RoleFrames;
import matchers.StandardMatcher;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import utils.Mark;

/*
 * Created on Jul 31, 2015
 * @author phw
 */

public class RuleMemory extends AbstractWiredBox {

	public static String SORTER_PORT = "Sorter port";

	public RuleMemory() {
		Connections.getPorts(this).addSignalProcessor(SORTER_PORT, this::processSorter);
	}

	private Map<String, ArrayList<Memory>> ruleMap = new HashMap<>();

	private Sequence ruleSequence = new Sequence();
	
	// Keeps track of order in which rules were remembered
	private int counter = 0;

	// Determines if previous result is still valid
	private String previousKey = null;

	private ArrayList<Memory> previousResult;

	private RuleSorterInterface ruleSorterInterface;

	public List<Memory> findMatchingMemories(Entity element, StoryProcessor storyProcessor, String... types) {
		boolean debug = false;
		Mark.say(debug, "Entering findMatchingMemories with", element);
		List<Memory> result = new ArrayList<>();
		if (Switch.level5UseMentalModels.isSelected()) {
			List<MentalModel> mentalModels = storyProcessor.getPersonalityModels(element);

			if (mentalModels.size() > 0) {
				// Mark.say("Found", mentalModels.size(), "mental models");
				for (MentalModel mentalModel : mentalModels) {
					int before = result.size();
					List<Memory> matchers = mentalModel.getStoryProcessor().getRuleMemory().findMatchingMemories(element, types);
					// Added by PHW to fix code rot; must remember mental model host associated with
					// rule
					matchers.stream().forEach(e -> e.setHost(mentalModel));
					result.addAll(matchers);
					int after = result.size();
					// Mark.say("Mental model stats", before, after, after - before);
				}
			}
		}
		result.addAll(findMatchingMemories(element, types));

		Mark.say(debug, "For", element, result.size());

		result = getRuleSorter().sort(result);

		// result.stream().forEachOrdered(m -> Mark.say(m));

		Mark.say(debug, "For", element, "found", result, "matching memories");

		return result;
	}

	private List<Memory> findMatchingMemories(Entity element, String... types) {
		return findMatchingMemories(element).stream().filter(m -> isOneOf(m, types)).collect(Collectors.toList());
	}

	private boolean isOneOf(Memory m, String... types) {
		for (String type : types) {
			if (type == m.getType()) {
				return true;
			}
		}
		return false;
	}

	private List<Memory> findMatchingMemories(Entity entity) {
		boolean debug = false;

		Mark.say(debug, "\n>>>  Finding rules for", entity.asString());

		Mark.say(debug, "All memory keys:", ruleMap.keySet().size());

		// ruleMap.values().stream().forEach(e -> Mark.say("Rule found", e));

		Thread thread = entity.getThreadWith(Markers.ACTION_WORD);

		// ruleMap.keySet().stream().forEach(r -> Mark.say("key:", r, "rule:", ruleMap.get(r)));

		List<Memory> remembered = new ArrayList<>();

		if (thread == null) {
			Mark.say(debug, "No action thread for", entity);
			thread = entity.getPrimedThread();
		}

		if (thread.lastElement().equals(previousKey)) {
			Mark.say(debug, "Previous result still current for", entity);
			return previousResult;
		}

		// Climb classification thread, looking for rules
		for (String type : thread) {
			ArrayList<Memory> more = ruleMap.get(type);
			if (more != null) {
				remembered.addAll(more);
			}
			
			// if this is the thread's primary (most specific) type, also do similarity comparison
            // this is the "Common Sense Enabled Rule Matching (CSERM)" referred to in Bryan Williams'
            // M.Eng thesis (http://groups.csail.mit.edu/genesis/papers/2017%20Bryan%20Williams.pdf) - see Chapter 5
			if (Switch.similarityMatchCheckBox.isSelected() && type.equals(thread.getType())) {
			    List<Memory> similarMemories = ruleMap.keySet().stream()
			            .filter(key -> StandardMatcher.areSimilar(type, key) && !key.equals(type))
			            .map(key -> ruleMap.get(key))
			            .flatMap(l -> l.stream())
			            .collect(Collectors.toList());
			    if (similarMemories.size() > 0) {
			        Mark.say(debug, "Memories similar to "+entity+" through primary type "+type+": "+similarMemories);
			    }
			    remembered.addAll(similarMemories);    
			}
		}

		// Also look for rules indexed by "variable"
		ArrayList<Memory> more = ruleMap.get(Markers.VARIABLE_MARKER);
		if (more != null) {
			remembered.addAll(more);
		}

		// Now have them all, so sort by order

		Collections.sort(remembered);

		if (debug && !remembered.isEmpty()) {
			Mark.say("For", entity, "remembered:");
			remembered.stream().forEachOrdered(m -> Mark.say(m));
		}
		return remembered;

	}

	/**
	 * Determines rule type and adds to concept map
	 */
	public void recordRule(Relation source) {
		// Mark.say("Recording", source);
		// First, be sure it is a rule
		if (!isRule(source)) {
			Mark.err("Trying to process entity that is not a rule", source);
			return;
		}
		try {
			if (source.getObject().hasFeature(Markers.NOT) && source.getObject().hasProperty(Markers.MODAL, Markers.CAN)) {
				addByConsequent(Markers.CENSOR_RULE, constructRule(source, Markers.CENSOR_RULE));
			}
			else if (source.getObject().hasProperty(Markers.CERTAINTY, Markers.TENTATIVE)) {
              addByConsequent(Markers.EXPLANATION_RULE, constructRule(source, Markers.EXPLANATION_RULE));
			}
			else if (source.getObject().hasFeature(Markers.EVIDENTLY)) {
				addByConsequent(Markers.PROXIMITY_RULE, constructRule(source, Markers.EXPLANATION_RULE, Markers.PROXIMITY_RULE));
				RoleFrames.removeRole(source.getObject(), Markers.MANNER_MARKER);
			}
			else if (source.getObject().hasProperty(Markers.IMPERATIVE, true)) {
				// Mark.say("Working on abduction ", source);
				// Mark.say("Working on abduction rule", source);
				addByConsequent(Markers.ABDUCTION_RULE, constructInvertedRule(source, Markers.EXPLANATION_RULE, Markers.ABDUCTION_RULE));
			}
			else if (source.getObject().hasProperty(Markers.CERTAINTY, Markers.PRESUMPTION_RULE)) {
				// Mark.say(Markers.PRESUMPTION_RULE, constructRule(source, Markers.EXPLANATION_RULE,
				// Markers.PRESUMPTION_RULE));
				addByConsequent(Markers.PRESUMPTION_RULE, constructRule(source, Markers.EXPLANATION_RULE, Markers.PRESUMPTION_RULE));
			}
			else if (source.isA(Markers.ENABLE_WORD)) {
				// Mark.say(Markers.ENABLER_RULE, constructRule(source, Markers.EXPLANATION_RULE,
				// Markers.ENABLER_RULE));
				addByConsequent(Markers.ENABLER_RULE, constructRule(source, Markers.EXPLANATION_RULE, Markers.ENABLER_RULE));
			}
			else if (source.isA(Markers.ONSET_MARKER)) {
				// Mark.say("Adding onset rule");

			}
			else if (RoleFrames.hasRole(Markers.MANNER_MARKER, Markers.MAYBE_WORD, source.getObject())) {
				addByAntecedents(Markers.PREDICTION_RULE, null, constructRule(source, Markers.PREDICTION_RULE, Markers.INSTRUCTION_RULE));
			}
			else {
				addByAntecedents(Markers.PREDICTION_RULE, null, constructRule(source, Markers.PREDICTION_RULE));
			}
		}
		catch (Exception e) {
			Mark.err("Blew out trying to record rule", source);
			// e.printStackTrace();
		}

	}

	/**
	 * A special case for noting the onset of concepts
	 */
	public void recordRule(Relation source, String name) {
		addByAntecedents(Markers.ONSET_RULE, name, constructRule(source, Markers.ONSET_RULE));
	}

	private boolean isRule(Relation source) {
		if (source.relationP(Markers.CAUSE_MARKER) && source.getSubject().sequenceP(Markers.CONJUNCTION)) {
			return true;
		}
		return false;
	}

	private Relation constructRule(Relation source, String... markers) {
		Sequence antecedents = new Sequence(Markers.CONJUNCTION);
		source.getSubject().stream().forEachOrdered(a -> antecedents.addElement(a));
		Relation rule = new Relation(Markers.RULE, antecedents, source.getObject());
		rule.addType(Markers.CAUSE_MARKER);
		Arrays.asList(markers).stream().forEachOrdered(m -> rule.addType(m));
		return rule;
	}

	private Relation constructInvertedRule(Relation source, String... markers) {
		Sequence antecedents = new Sequence(Markers.CONJUNCTION);
		antecedents.addElement(source.getObject());
		// Complain if not exactly one element in the sequence
		if (!source.getSubject().sequenceP() || source.getSubject().getElements().size() != 1) {
			Mark.err("Wrong number of antecedents--must be just one for this kind of rule");
			return new Relation(Markers.RULE, antecedents, null);
		}
		Relation rule = new Relation(Markers.UNKNOWN, antecedents, source.getSubject().get(0));
		rule = constructRule(rule, markers);
		return rule;
	}

	/**
	 * A special case for noting the onset of concepts
	 */
	private void addByAntecedents(String type, String name, Relation rule) {
		Mark.say(false, "Adding by antecedents:", rule);
		for (Entity antecedent : ((Sequence) (rule.getSubject())).getElements()) {
			addRuleToMemory(antecedent.getType(), type, name, rule);
		}
		// 2016bmw commented out below
		//ruleSequence.addElement(rule);
	}

	private void addByConsequent(String type, Relation rule) {
		// Mark.say("Adding by consequent:", rule.getObject());
		// Special case if consequent is a variable
		if (rule.getObject().isA(Markers.VARIABLE_MARKER)) {
			addRuleToMemory(Markers.VARIABLE_MARKER, type, null, rule);
		}
		else {
			addRuleToMemory(rule.getObject().getType(), type, null, rule);
		}
	    // 2016bmw commented out below
		//ruleSequence.addElement(rule);
	}

	private void addRuleToMemory(String key, String type, String name, Relation rule) {
		// Mark.say(true, "Adding rule:", rule);
		ArrayList<Memory> partners = ruleMap.get(key);
		if (partners == null) {
			partners = new ArrayList<Memory>();
			ruleMap.put(key, partners);

		}
		// 2016bmw commented out below
        //partners.add(new Memory(counter++, type, name, rule));
        // 2016bmw added below
		if (partners.stream().noneMatch(m -> m.getRule().isDeepEqual(rule))) {
		    partners.add(new Memory(counter++, type, name, rule));
		}
        if (ruleSequence.stream().noneMatch(r -> r.isDeepEqual(rule))) {
		    ruleSequence.addElement(rule);
		}
	}

	public void clear() {
		ruleMap.clear();
		ruleSequence.getElements().clear();
		counter = 0;
		previousKey = null;
		previousResult = null;

	}

	public Map<String, ArrayList<Memory>> getRuleMap() {
		return ruleMap;
	}

	public void transferFrom(RuleMemory sourceRuleMemory) {
		Map<String, ArrayList<Memory>> ruleMap = sourceRuleMemory.getRuleMap();
		for (String key : ruleMap.keySet()) {
			for (Memory m : ruleMap.get(key)) {
				addRuleToMemory(key, m.getType(), m.getName(), m.getRule());
				// recordRule(m.getRule());
			}
		}
	}

	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	public List<Entity> getRuleList() {
		return getRuleSequence().getElements();
	}

	public RuleSorterInterface getRuleSorter() {
		if (ruleSorterInterface == null) {
			ruleSorterInterface = new DefaultRuleSorter();
		}
		return ruleSorterInterface;
	}

	public void setRuleSorter(RuleSorterInterface ruleSorterInterface) {
		this.ruleSorterInterface = ruleSorterInterface;
	}

	public void processSorter(Object signal) {
		if (signal instanceof RuleSorterInterface) {
			setRuleSorter((RuleSorterInterface) signal);
		}
	}

}

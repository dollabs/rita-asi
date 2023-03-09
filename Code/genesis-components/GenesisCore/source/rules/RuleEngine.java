package rules;

import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.text.Document;

import com.ascent.gui.frame.ABasicFrame;

import bryanWilliams.goalAnalysis.*;
import conceptNet.conceptNetModel.*;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Matcher;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Entity.LabelValuePair;
import generator.Generator;
import generator.RoleFrames;
import generator.Rules;
import genesis.GenesisGetters;
import matchers.*;
import matchers.original.BasicMatcherOriginal;
import matchers.representations.BindingsWithProperties;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import utils.*;
import utils.minilisp.LList;
import utils.tools.Predicates;

/**
 * Created on Aug 1, 2015
 * <p>
 * To do:
 * <ul>
 * <li>Censor rules should be handled here
 * <li>Should transmit rules to rule viewer
 * </ul>
 * 
 * @author phw
 */

public class RuleEngine extends AbstractWiredBox {
	
	JTextPane jpt = new JTextPane();

	public RuleEngine() {
		jpt.setFont(new Font("Helvetica", Font.BOLD, 25));
	}

	// public static final String PARTIAL_DEDUCTION = "Partial deduction rule";

	public static boolean USE_INSTRUCTIONS = false;

	private ArrayList<Relation> previousExplanations = new ArrayList<>();

	private ExplanationContinuationInterface explanationContinuationTestor;

	private RuleSorterInterface ruleSorter;
	
	public void process(Entity element, StoryProcessor storyProcessor) {
		RuleMemory memory = storyProcessor.getRuleMemory();

		// May, or may not, take care of becoming and stopping
		Vector<Entity> elements;
		if (storyProcessor.getStory().getElements().stream().anyMatch(e -> e.isA(Markers.DISAPPEAR_MARKER))) {
			elements = reverseAndFilterOutPreviousTransitions(storyProcessor.getStory().getElements());
		}
		else {
			elements = storyProcessor.getStory().getElements();
		}
		
		// First things first, see if element is censored, and if so, get rid of it
		if (isCensored(element, storyProcessor, elements)) {
			storyProcessor.getStory().getElements().remove(element);
			return;
		}

		// Next, try to explain how this element came to be
		List<Memory> memories;
		previousExplanations.clear();

		if (Switch.level3ExplantionRules.isSelected()) {
			String[] types = {

			        Markers.EXPLANATION_RULE, Markers.PROXIMITY_RULE,

			        Markers.ABDUCTION_RULE, Markers.PRESUMPTION_RULE,

			        Markers.ENABLER_RULE

			};

				memories = memory.findMatchingMemories(element, storyProcessor, types);
				// Mark.say("Ready to start explanation of", memories.size(), element);
				startExplanation(element, storyProcessor, elements, memories);


		}

		// Finally, make deductions
		if (Switch.Level2PredictionRules.isSelected()) {
			if (Switch.findConceptOnsets.isSelected()) {
				memories = memory.findMatchingMemories(element, storyProcessor, Markers.PREDICTION_RULE, Markers.ONSET_RULE);
			}
			else {
				memories = memory.findMatchingMemories(element, storyProcessor, Markers.PREDICTION_RULE);
			}
			memories.stream().forEach(r -> startInjectionDeduction(element, storyProcessor, elements, r));
		}

		if (Switch.performGoalAnalysis.isSelected()) {
			// For more information, see Chapter 6 of Bryan Williams' M.Eng thesis
			// available here: http://groups.csail.mit.edu/genesis/papers/2017%20Bryan%20Williams.pdf
			Mark.say(false, "Consulting ASPIRE");
			Set<GoalAnalysisResult> results = GenesisGetters.getAspireEngine().processEvent(element);
			for (GoalAnalysisResult goalResult : results) {
				Mark.say(false, "want:", goalResult.getWant());
				GoalContributionResult goalContributionInfo = goalResult.getContributionInfo();
				Entity want = goalResult.getWant();
				CharacterGoal goal = goalResult.getGoal();
				// use property instead of a feature because features screw with toEnglish()
				if (goal.hasCause()) {
					want.addProperty(Markers.GOAL_ANALYSIS, true, true);
				}
				Entity wantedAction = goalResult.getWantedAction();
				if (!element.isDeepEqual(wantedAction)) {
					// use property instead of a feature because features screw with toEnglish()
					wantedAction.addProperty(Markers.GOAL_ANALYSIS, true, true);
				}

				if (goal.hasCause() && goal.causationUsedConceptNet()) {
					// e.g. "Fred wants to relax because Fred works hard at the office"
					Relation goalCausation = constructCause(Arrays.asList(goalResult.getCause()), want, Collections.emptyList());
					goalCausation.addProperty(Markers.GOAL_ANALYSIS, true, true);
					goalCausation.addProperty(Markers.CONCEPTNET_JUSTIFICATION, goal.getCausationJustification(), true);
					storyProcessor.processBackwardRuleDirectCall(null, goalCausation);
				}

				// e.g. "Fred watches television because Fred wants to relax"
				Relation goalContribution = constructCause(Arrays.asList(want), element, Collections.emptyList());
				// use property instead of feature since features affect .toEnglish() generation
				goalContribution.addProperty(Markers.GOAL_ANALYSIS, true, true);
				if (goalContributionInfo.consultedConceptnet()) {
					goalContribution.addProperty(Markers.CONCEPTNET_JUSTIFICATION, goalContributionInfo.justification(), true);
				}
				Mark.say(false, "goal action:", goalContribution);
				storyProcessor.processBackwardRuleDirectCall(null, goalContribution);

				if (!element.isDeepEqual(wantedAction)) {
					// i.e. once wanting to relax has been fulfilled by, say, watching tv, instantiate explicit relax
					// event
					// e.g. "Fred relaxes"
					Relation goalRealization = constructCause(Arrays.asList(element), wantedAction, Collections.emptyList());
					// use property instead of feature since features affect .toEnglish() generation
					goalRealization.addProperty(Markers.GOAL_ANALYSIS, true, true);
					storyProcessor.processBackwardRuleDirectCall(null, goalRealization);
				}
			}
		}

		if (Switch.useExpertRules.isSelected()) {
			boolean askAbout = false;
			boolean alreadyExplained = false;
			boolean isInChain = false;
			boolean isMeans = false;
			// Temporary:
			if (!element.isA("action")) {
				askAbout = false;
			}
			else {
				boolean report = false;
				// if (element.isA("stab")) {
				// Mark.say("!!!!!!!!!!!!!!!", element);
				// report = true;
				// }

				for (Entity e : storyProcessor.getStory().getElements()) {
					if (Predicates.isCause(e) || Predicates.isEntail(e)) {
						if (e.getObject() == element) {
							Mark.say(report, "Ignore: already explained", element);
							alreadyExplained = true;
							break;
						}

						for (Entity a : e.getSubject().getElements()) {
							if (a == element) {
								isInChain = true;
								break;
							}
						}
						if (isInChain) {
							break;
						}
					}
				}
				// Check if it is a means, if so, forget it.
				for (Entity e : storyProcessor.getStory().getElements()) {
					if (Predicates.isMeans(e)) {
						for (Entity a : e.getSubject().getElements()) {
							if (a == element) {
								isMeans = true;
								break;
							}
						}
						if (isMeans) {
							// As if explained
							break;
						}
					}
				}

				if (alreadyExplained || !isInChain || isMeans) {
					Mark.say(report, "Ignore: not in causal chain", element);
				}
				else {
					askAbout = true;
				}
			}
			if (askAbout) {
				Mark.say("In expert code looking for explanation for", element);
			}
			else {
				// Mark.say("Ignore", element);
			}

			if (askAbout) {

				String[] options = new String[] { "Yes", "No" };

				// Entity generalization = generalize(element);

				String message = "Should I ask an expert why " + Generator.getGenerator().generate(element);



				jpt.setText(message);

				int response = JOptionPane
				        .showOptionDialog(ABasicFrame
				                .getTheFrame(), jpt, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				if (response == JOptionPane.YES_OPTION) {
					Mark.say("Ok, I'll ask");

					explainActionUsingExpert(element, storyProcessor);

				}
			}
		}

	}

	private void explainActionUsingExpert(Entity element, StoryProcessor storyProcessor) {
		Mark.say("Trying to explain....................", element);

		List<Memory> memories;
		String[] options;
		String message;
		int response;

		MentalModel expert = storyProcessor.getMentalModel().loadLocalMentalModel("wizard");

		String[] types = { Markers.EXPLANATION_RULE };

		memories = expert.getStoryProcessor().getRuleMemory().findMatchingMemories(element, expert.getStoryProcessor(), types);

		List<Entity> rules = memories.stream().map(m -> m.getRule()).collect(Collectors.toList());

		options = new String[] { "Ok", "Reject" };

		for (Memory memory : memories) {
			Entity rule = memory.getRule();
			Entity consequent = rule.getObject();
			List<Entity> antecedents = rule.getSubject().getElements();

			LList<PairOfEntities> bindings = null;

			bindings = StandardMatcher.getBasicMatcher().match(consequent, element);

			if (bindings == null) {
					continue;
			}

			Mark.say("Matched consequent", bindings);

			for (Entity antecedent : antecedents) {

				LList<PairOfEntities> newBindings = null;

				for (Entity x : storyProcessor.getStory().getElements()) {

					Mark.say("Matching\n>>>  ", antecedent, "with\n>>>  ", x);

					newBindings = StandardMatcher.getBasicMatcher().match(antecedent, x, bindings);

					Mark.say("Yields", bindings);

					if (newBindings == null) {
						continue;
					}
					else {
						break;
					}
				}
				if (newBindings != null) {
					bindings = newBindings;
					Mark.say("Matched antecedent", bindings);
					continue;
				}
				else {
					bindings = null;
					break;
				}
			}
			if (bindings != null) {
				Mark.say("Emerged from rule check with bindings", bindings);

				Entity result = Substitutor.substitute(rule, bindings);
				
				result = Rules.makeCause(result.getObject(), result.getSubject());

				message = "Expert thinks that " + Generator.getGenerator().generate(result);

				// response = JOptionPane .showOptionDialog(null, message, "Expert's suggestion",
				// JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				jpt.setText(message);

				response = JOptionPane
				        .showOptionDialog(ABasicFrame
				                .getTheFrame(), jpt, "Expert's suggestion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

				Mark.say("Response", response);
				if (response == 0) {

					List<Memory> newMemories = new ArrayList<>();
					newMemories.add(memory);

					startExplanation(element, storyProcessor, storyProcessor.getStory().getElements(), newMemories);

				}
			}
		}


		// for (Memory m : memories) {
		//
		// message = "Expert suggests that " + Generator.getGenerator().generateAsIf(m.getRule());
		//
		// response = JOptionPane
		// .showOptionDialog(null, message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
		// options, options[0]);
		// Mark.say("Response", response);
		// if (response == 0) {
		// Mark.say("Expert suggests that", Generator.getGenerator().generateAsIf(m.getRule()));
		// startExplanation(element, storyProcessor, storyProcessor.getStory().getElements(), memories);
		// }
		// }
	}

	private Entity generalize(Entity element) {
		Set<Entity> entities = extractEntities(element);
		Mark.say("Entities", entities);
		return element;
	}
	
	/**
	 * Under construction
	 * 
	 * @param e
	 * @return
	 */
	private Set<Entity> extractEntities(Entity e) {
		Set<Entity> result = new HashSet<>();
		if (e.entityP()) {
			result.add(e);
		}
		else if (e.functionP()) {
			extractEntities(e.getSubject()).add(e);
		}
		else if (e.relationP()) {

		}
		return result;
	}

	public BindingsWithProperties match(Entity pattern, Entity datum, BindingsWithProperties constraints) {
	    if (Switch.similarityMatchCheckBox.isSelected()) {
	        // this is the "Common Sense Enabled Rule Matching (CSERM)" referred to in Bryan Williams'
	        // M.Eng thesis (http://groups.csail.mit.edu/genesis/papers/2017%20Bryan%20Williams.pdf) - see Chapter 5
	        return StandardMatcher.getBasicMatcher().similarityMatch(pattern, datum, constraints);
	    }
	    return StandardMatcher.getBasicMatcher().match(pattern, datum, constraints);
	}

	private void startInjectionDeduction(Entity element, StoryProcessor storyProcessor, Vector<Entity> elements, Memory r) {
		// Mark.say("Working on injection into", storyProcessor.getName());
		// Mark.say("Entity", element);
		startDeduction(element, storyProcessor, elements, r);
	}

	private boolean isCensored(Entity element, StoryProcessor storyProcessor, Vector<Entity> elements) {
		List<Memory> memories = storyProcessor.getRuleMemory().findMatchingMemories(element, storyProcessor, Markers.CENSOR_RULE);
		return startCensor(element, storyProcessor, elements, memories);
	}

	public boolean startCensor(Entity element, StoryProcessor storyProcessor, Vector<Entity> elements, List<Memory> memories) {

		boolean debug = false;

		boolean explained = false;

		Mark.say(debug, "Trying to censor", element, "with rule count", memories.size());

		for (Memory memory : memories) {
			Relation rule = memory.getRule();

			Mark.say(debug, "Rule is:\n>>>  ", rule);

			BindingsWithProperties bindings = StandardMatcher.getBasicMatcher().matchNegation(rule.getObject(), element, new BindingsWithProperties());

			Mark.say(debug, "Matching negation of:\n>>>  ", rule.getObject(), "\n>>>  ", element, "\n>>>  ", bindings != null);

			Mark.say(debug, "Working on this can't be:  ", rule
			        .getObject(), "\n>>>  if this is true:", element, "\n>>>  ");

			if (bindings != null) {

				Mark.say(debug, "\n>>>  Attempting to censor\n>>>  ", element, "\n>>>  with", memory.getType(), "rule\n>>>  ", rule);

				LList<Entity> antecedents = new LList<>();
				for (Entity a : rule.getSubject().getElements()) {
					antecedents = antecedents.cons(a);
				}

				List<LList<Entity>> instantiatedAntecedents = completeExplanation(element, storyProcessor, elements, memory, antecedents, new LList<Entity>(), bindings);

				for (LList<Entity> instantiations : instantiatedAntecedents) {
					Relation explanation = instantiateDeductionWithProperties(instantiations.toList(), element, memory, bindings.getProperties());
					explained = true;
					break;
				}
			}
		}
		return explained;
	}

	public boolean startExplanation(Entity element, StoryProcessor storyProcessor, Vector<Entity> elements, List<Memory> memories) {

		boolean debug = false;

		Mark.say(debug, "Trying to explain", element, memories.size());

		for (Memory memory : memories) {

			Mark.say(debug, "Working on a memory", memory.getName());

			// Did not work 14 April 2017
			// if (getExplanationContinuationTestor().test(element, memory.getRule(), previousExplanations)) {
			// Mark.say(debug, "Stopping explanation process at one explanation");
			// break;
			// }

			// So substituting this:

			if (Switch.useOnlyOneExplanation.isSelected() && testForPreviousExplanation(element, storyProcessor.getStory())) {
				Mark.say(debug, "Stopping explanation process because previously explained");
				break;
			}

			// Why shouldn't means fire processing 26 May 2017
			if (false && testForMeans(element, storyProcessor.getStory())) {
				Mark.say(debug, "Stopping explanation process because involved in means");
				break;
			}

			Relation rule = memory.getRule();

			Mark.say(debug, "Testing match of \n>>>  ", rule.getObject(), "\n>>>  against\n>>>  ", element);

			BindingsWithProperties bindings = match(rule.getObject(), element, new BindingsWithProperties());
			        //StandardMatcher.getBasicMatcher().match(rule.getObject(), element, new LList<PairOfEntities>());
			if (bindings != null) {

				Mark.say(debug, "\n>>>  Attempting to explain\n>>>  ", element, "\n>>>  against\n>>>  ", memory.getType(), "rule\n>>>  ", rule);
				
				LList<Entity> antecedents = new LList<>();
				for (Entity a : rule.getSubject().getElements()) {
					antecedents = antecedents.cons(a);
				}

				List<LList<Entity>> instantiatedAntecedents = completeExplanation(element, storyProcessor, elements, memory, antecedents, new LList<Entity>(), bindings);

				for (LList<Entity> instantiations : instantiatedAntecedents) {

					if (Switch.useOnlyOneExplanation.isSelected()
					        && getExplanationContinuationTestor().test(element, memory.getRule(), previousExplanations)) {
						Mark.say(debug, "Stopping explanation process at one explanation");
						break;
					}
					Relation explanation = instantiateDeductionWithProperties(instantiations.toList(), element, memory, bindings.getProperties());
					storyProcessor.processBackwardRuleDirectCall(rule, explanation);
					previousExplanations.add(explanation);
				}
			}
			else {
				Mark.say(debug, "But found no match");
			}
		}
		return !previousExplanations.isEmpty();
	}

	private boolean testForPreviousExplanation(Entity consequent, Sequence story) {
		if (story.getElements().parallelStream().anyMatch(x -> isCauseConsequent(consequent, x))) {
			return true;
		}
		return false;
	}

	private boolean isCauseConsequent(Entity consequent, Entity element) {
		if (Predicates.isCause(element)) {
			if (consequent == element.getObject()) {
				return true;
			}
		}
		return false;
	}

	private boolean testForMeans(Entity antecedent, Sequence story) {
		if (story.getElements().parallelStream().anyMatch(x -> isMeansAntecedent(antecedent, x))) {
			return true;
		}
		return false;
	}

	private boolean isMeansAntecedent(Entity antecedent, Entity element) {
		if (Predicates.isMeans(element)) {
			for (Entity x : element.getSubject().getElements()) {
				if (x == antecedent) {
					return true;
				}
			}
		}
		return false;
	}

	private List<LList<Entity>> completeExplanation(Entity element, StoryProcessor storyProcessor,Vector<Entity>elements, Memory memory, LList<Entity> antecedents, LList<Entity> explanation, BindingsWithProperties bindings) {

		boolean debug = false;

		// Create list for explanations
		List<LList<Entity>> explanationList = new ArrayList<>();

		// If no antecedents left, explanation elements found
		if (antecedents.endP()) {
			explanationList.add(explanation);
			Mark.say(debug, "Explanation succeeded");
			return explanationList;
		}

		// Special case for negated antecedents
		if (antecedents.first().hasFeature(Markers.NOT)) {
			for (Entity x : elements) {
				BindingsWithProperties newBindings = StandardMatcher.getBasicMatcher().matchNegation(antecedents.first(), x, bindings);
				if (newBindings != null) {
					// Lost
	                // if we found an explanation, need to transfer the properties of the new binding back to the original
                    bindings.setProperties(newBindings.getProperties());
					return explanationList;

				}
			}
		}

		// Each entity potentially provides different bindings for a solution.

		// Note if no solutions found
		boolean succeeded = false;

		if (memory.getRule().isA(Markers.PROXIMITY_RULE)) {

			// If it is a proximity rule, only allowed to look at previous element
			// Awkward duplication
			for (int i = 1; i < elements.size(); ++i) {
				if (element == elements.get(i)) {
					Entity storyElement = elements.get(i - 1);
					BindingsWithProperties newBindings = match(antecedents.first(), storyElement, bindings);
					        //StandardMatcher.getBasicMatcher().match(antecedents.first(), storyElement, bindings);

					// Cannot stop here because match may be a blind ally

					if (newBindings != null) {
						List<LList<Entity>> explanations = completeExplanation(element, storyProcessor, elements, memory, antecedents.rest(), explanation
						        .cons(storyElement), newBindings);
						if (!explanations.isEmpty()) {
							succeeded = true;
							explanationList.addAll(explanations);
			                 // if we found an explanation, need to transfer the properties of the new binding back to the original
		                     bindings.setProperties(newBindings.getProperties());
						}
					}
					break;
				}
			}
		}
		else {
			for (Entity storyElement : elements) {
				BindingsWithProperties newBindings = match(antecedents.first(), storyElement, bindings);
				        //StandardMatcher.getBasicMatcher().match(antecedents.first(), storyElement, bindings);
				if (newBindings != null) {


				    
                    // prevent similarity matches from explaining an entity using itself
				    if (storyElement.equals(element) && newBindings.hasProperty(Markers.CONCEPTNET_SIMILARITY_MATCH)) {
				        continue;
				    }
				    
					List<LList<Entity>> explanations = completeExplanation(element, storyProcessor, elements, memory, antecedents.rest(), explanation
					        .cons(storyElement), newBindings);

					// Cannot stop here because match may be a blind ally

					// if (!explanations.isEmpty()) {
					// succeeded = true;
					// explanationList.addAll(explanations);
					// if (Switch.useOnlyOneExplanation.isSelected()) {
					// Mark.say(debug, "One explanation break");
					// break;
					// }
					// }
					explanationList.addAll(explanations);
					succeeded = true;
					// if we found an explanation, need to transfer the properties of the new binding back to the original
					if (!explanations.isEmpty()) {
					   bindings.setProperties(newBindings.getProperties());
					}
					Mark.say(debug, "Succesful match of\n>>>  ", antecedents.first(), "\n>>>  ", storyElement);
				}
				else {
					Mark.say(debug, "No match between\n>>>  ", antecedents.first(), "\n>>>  ", storyElement);
				}
			}
		}

		if (!succeeded && insertionType(memory.getType())) {
			// In these cases, assume antecedent

		    // Does not use markers since markers just apply to the overall causal relation, not its specific components
			Entity x = Matcher.instantiate(antecedents.first(), bindings.getBindings());

			Mark.say(debug, memory.getType(), "rule wants to assume", x);

			List<LList<Entity>> explanations = completeExplanation(element, storyProcessor, elements, memory, antecedents.rest(), explanation
			        .cons(x), bindings);
			if (!explanations.isEmpty()) {
				succeeded = true;
				explanationList.addAll(explanations);
			}
		}

		return explanationList;
	}

	private boolean insertionType(String type) {
		if (type == Markers.ABDUCTION_RULE || type == Markers.PRESUMPTION_RULE || type == Markers.ENABLER_RULE) {
			return true;
		}
		return false;
	}
	
	private static boolean hasProperty(List<LabelValuePair> properties, String property) {
	    return properties.stream().map(LabelValuePair::getLabel).anyMatch(label -> label.equals(property));
	}
	
	private Relation instantiateDeductionWithProperties(List<Entity> antecedents, Entity consequent, Memory memory, List<LabelValuePair> properties) {
	    Relation relation = instantiateDeduction(antecedents, consequent, memory);
	    if (hasProperty(properties, Markers.CONCEPTNET_SIMILARITY_MATCH)) {
	        // if we formed this cause using similarity matching, add the current rule this created cause is similar to as an additional 
	        // justification
	        ConceptNetRuleJustification ruleJust = new ConceptNetRuleJustification(memory.getRule());
	        @SuppressWarnings("unchecked")
            List<ConceptNetJustification> justification = (List<ConceptNetJustification>) properties.stream()
	                .filter(lvp -> lvp.getLabel().equals(Markers.CONCEPTNET_JUSTIFICATION))
	                .map(LabelValuePair::getValue)
	                .findFirst()
	                .orElseThrow(RuntimeException::new);
	        // may already contain rule justification because similarity based rule matching
	        // has already been fully applied and concluded a different way 
	        // if so, this will just move it to the end
	        justification.remove(ruleJust);
	        justification.add(ruleJust);
	    }
	    // if this memory is a GOAL_ANALYSIS rule, the created cause should be too
	    if (memory.getRule().hasProperty(Markers.GOAL_ANALYSIS) && !hasProperty(properties, Markers.GOAL_ANALYSIS)) {
	        properties.add(new LabelValuePair(Markers.GOAL_ANALYSIS, true, true));
	    }
	    relation.setPropertyList(new Vector<>(properties));
	    return relation;
	}

	private Relation instantiateDeduction(List<Entity> antecedents, Entity consequent, Memory memory) {
		Relation rule = memory.getRule();
		String type = memory.getType();

		Relation explanation = constructCause(antecedents, consequent, Arrays.asList(type));

		if (rule.getProbability() != null) {
			explanation.addProbability(rule.getProbability());
		}
		return explanation;
	}

	/**
	 * Start forward chaining by finding matching antecedent with element and setting up work on matching the other
	 * antecedents with the story.
	 */
	protected void startDeduction(Entity element, StoryProcessor storyProcessor, Vector<Entity> elements, Memory memory) {
		boolean debug = false;
		Relation rule = memory.getRule();
		Mark.say(debug, "Trying", memory.getType(), "rule:", rule);
		for (Entity antecedent : rule.getSubject().getElements()) {
			// Check this antecedent
			BindingsWithProperties bindings = new BindingsWithProperties();
			bindings = match(antecedent, element, bindings);
			        //StandardMatcher.getBasicMatcher().match(antecedent, element, bindings);
			if (bindings != null) {
				Mark.say(debug, "Successfully matched a rule antecedent\n>>>  ", antecedent, "\n>>>  ", element, "\n>>>  ", bindings);
				List<Entity> satisfied = new ArrayList<Entity>();
				List<Entity> unsatisfied = copy(((Sequence) (rule.getSubject())).getElements());
				satisfied.add(element);
				unsatisfied.remove(antecedent);
				completeDeduction(storyProcessor, elements, memory, satisfied, unsatisfied, bindings);
			}
			else {
				Mark.say(debug, "Failed to match\n>>> ", antecedent, "\n>>> ", element);
			}
		}
	}

	private Vector<Entity> reverseAndFilterOutPreviousTransitions(Vector<Entity> elements) {
		Vector<Entity> result = new Vector<>();
		LList<Entity> list = new LList<>();
		for (int i = 0; i < elements.size(); ++i) {
			list = list.cons(elements.get(i));
		}
		list = reverseAndFilterOutPreviousTransitions(list);
		while (!list.endP()) {
			result.add((Entity) (list.car()));
			list = list.rest();
		}
		// Mark.say("Entering\n\n");
		// result.stream().forEachOrdered(e -> Mark.say("Element:", e.asStringWithIndexes()));
		return result;
	}

	private LList<Entity> reverseAndFilterOutPreviousTransitions(LList<Entity> list) {
		if (list.endP()) {
			return list;
		}
		Entity first = list.first();
		// Mark.say("Checking", first.asStringWithIndexes(), first.isA(Markers.TRANSITION_MARKER));
		if (first.isA(Markers.TRANSITION_MARKER)) {
			// Mark.say("Excluding:", first.getSubject().asStringWithIndexes());
			return reverseAndFilterOutPreviousTransitions(first.getSubject(), list.rest()).cons(first);
		}
		else {
			return reverseAndFilterOutPreviousTransitions(list.rest()).cons(list.first());
		}
	}

	private LList<Entity> reverseAndFilterOutPreviousTransitions(Entity exclude, LList<Entity> list) {
		if (list.endP()) {
			return list;
		}
		// Speed this sucker up; doesn't actually help
		// LList<Entity> test = list;
		// boolean tobad = false;
		// while (!test.endP()) {
		// if (test.first().isA(Markers.TRANSITION_MARKER)) {
		// tobad = true;
		// break;
		// }
		// test = test.rest();
		// }
		// if (!tobad) {
		// return list;
		// }

		Entity first = list.first();
		// Mark.say("\n...", exclude.asStringWithIndexes(), "\n...", first.getSubject().asStringWithIndexes());
		if (first.isA(Markers.TRANSITION_MARKER)) {
			if (exclude == first.getSubject()) {
				// Mark.say("Ripping out!!!!!!!!!!!!!!!!!", first);
				return reverseAndFilterOutPreviousTransitions(exclude, reverseAndFilterOutPreviousTransitions(list.rest()));
			}
			else {
				return reverseAndFilterOutPreviousTransitions(first.getSubject(), reverseAndFilterOutPreviousTransitions(exclude, list.rest()))
				        .cons(first);
			}
		}
		else {
			return reverseAndFilterOutPreviousTransitions(exclude, list.rest()).cons(first);
		}
	}

	/*
	 * Have candidate rule and some number of satisfied antecedents. When all antecedents are satisfied, rule fires with
	 * complete binding list used to instantiate the consequent.
	 */
	public void completeDeduction(StoryProcessor storyProcessor, Vector<Entity> elements, Memory memory, List<Entity> satisfiedAntecedents, List<Entity> unsatisfiedAntecedents, BindingsWithProperties bindings) {
		// Mark.say("Working on\n", satisfiedAntecedents, "\n", unsatisfiedAntecedents);
		boolean debug = false;
		boolean manner = false;
		Relation rule = memory.getRule();

		if (unsatisfiedAntecedents.isEmpty()) {

			// No unmatched antecedents left. Must have succeeded.

			Mark.say(debug, "Fired rule", rule.asString());
			Entity consequent = memory.getRule().getObject();



			// Does not use properties since markers just apply to the overall causal relation, not its specific
			// components
			// consequent = Matcher.instantiate(consequent, bindings.getBindings());

			Mark.say(debug, "Bindings/Consequent A", bindings.getBindings(), "\n", consequent);

			consequent = Substitutor.substitute(consequent, bindings.getBindings());

			Mark.say(debug, "Bindings/Consequent B", bindings.getBindings(), "\n", consequent);

			consequent = removeActionReification(consequent);
			
			// prevent similarity rule matching from deducing an entity using itself
			if (bindings.hasProperty(Markers.CONCEPTNET_SIMILARITY_MATCH)) {
			    for (Entity ent : satisfiedAntecedents) {
			        if (ent.isDeepEqual(consequent)) {
			            return;
			        }
			    }  
			}

			// This became needed in 2014 following observation that multiple rules may indicate someone is, for
			// example, murderous. It needs to be the same trait as any previous murderous.

			if (storyProcessor.isInAnyScene(consequent)) {
				consequent = storyProcessor.retrieveFromCache(consequent);
			}

			if (RoleFrames.hasRole(Markers.MANNER_MARKER, Markers.MAYBE_WORD, consequent)) {
				Mark.say(debug, "Found maybe manner marker on", consequent);

				if (!USE_INSTRUCTIONS) {
					Mark.say("Ignoring maybe construction");
					return;
				}

				RoleFrames.removeRole(consequent, Markers.MANNER_MARKER);
				consequent.removeFeature(Markers.MAYBE_WORD);
				// consequent.removeProperty(Markers.PROPERTY_TYPE);

				manner = true;

			}

			// Why not show consequent again?
			if (Switch.useOnlyOneDeduction.isSelected() && storyProcessor.isInCurrentScene(consequent)) {
				Mark.say(debug, "Already predicted", consequent);
				return;
			}

			else if (isCensored(consequent, storyProcessor, elements)) {
				Mark.say(debug, "Censored", consequent);
				return;
			}
			else if (manner) {

				// Mark.say("Test", consequent.getFeatures());
				// Mark.say("Test", consequent.getPropertyList());

				Connections.getPorts(storyProcessor)
				        .transmit(StoryProcessor.INSTRUCTION_PORT, new BetterSignal(consequent, storyProcessor));
				return;
			}

			// Mark.say("Ugh", bindings);
			// Mark.say("Ugh", bindings.getProperties());

			Relation deduction = instantiateDeductionWithProperties(satisfiedAntecedents, consequent, memory, bindings.getProperties());

			Mark.say(debug, "\n>>>  Deduction of type", memory.getType(), "\n\n>>>", rule, "\n>>>", deduction);

			if (deduction.isA(Markers.ONSET_RULE)) {
				Mark.say(debug, "Found onset of", memory.getName());
				storyProcessor.processOnset(memory);
			}

			else {
				storyProcessor.processForwardRuleDirectCall(rule, deduction);
			}
			// }
		}
		else {

			// There must be more antecedents to match. Try to find a match for the first unsatisfied antecedent.
			Entity unsatisfiedAntecedent = unsatisfiedAntecedents.get(0);
			// Have to work with a copy of the story here, because additions to
			// the story will be made inside this loop.

			List<Entity> storyElements = copy(elements);

			// Mark.say("More stats", storyElements.size(), storyProcessor.getStory().getElements().size());

			boolean succeeded = false;

			for (int i = storyElements.size() - 1; i >= 0; i--) {

				Entity element = storyElements.get(i);
				// Mark.say("Stats", i, element);
				// Make sure story element not already in the list of story elements that have already matched an
				// antecedent. Presumably saves time.
				if (satisfiedAntecedents.contains(element)) {
					Mark.say(debug, "Already used\n", element.asString());
					continue;
				}

				Mark.say(debug, "Trying to match remaining antecedent to story element\n", unsatisfiedAntecedent, "\n", element, "\n", bindings);
				BindingsWithProperties newBindings = match(unsatisfiedAntecedent, element, bindings);
				        //StandardMatcher.getBasicMatcher().match(unsatisfiedAntecedent, element, bindings);
				if (newBindings != null) {
					Mark.say(debug, "Matched\n", unsatisfiedAntecedent, "\n", element, "\n", newBindings);
					// Great, found one way of matching. Make copies so as not to screw up other branches involving
					// matches to other story elements another time around the loop.
					succeeded = true;
					List<Entity> satisfied = copy(satisfiedAntecedents);
					List<Entity> unsatisfied = copy(unsatisfiedAntecedents);
					satisfied.add(element);
					unsatisfied.remove(unsatisfiedAntecedent);
					completeDeduction(storyProcessor, elements, memory, satisfied, unsatisfied, newBindings);
				}
				else {
					// Mark.say(debug, "Failed to match\n", unsatisfiedAntecedent.asString(), "\n", element.asString());
				}
			}
			// Special case:
			// If it is a negation, see if positive form matches. If not, will presume negation and assert it.
			if (!succeeded && unsatisfiedAntecedent.hasFeature(Markers.NOT)) {
				boolean found = false;
				for (Entity storyElement : storyElements) {
					BindingsWithProperties newBindings = StandardMatcher.getBasicMatcher()
					        .matchNegation(unsatisfiedAntecedent, storyElement, bindings);
					// If found, lose
					if (newBindings != null) {
						found = true;
						break;
					}
				}
				// False added 11 Apr 2017 because of bug uncovered by Jasmine in rule
				// If zz belongs to cabinet and zz does not support yy then yy is politically weak.
				// That one asserts as long as zz belongs to cabinet, yy is weak
				// Ooops 9 Sep 2017 breaks zookeeper
				// if (false && !found) {
				if (!found) {
					// If nothing found, win and insert negation.
					Mark.say(debug, "Successfully matched another antecedent by absence of positive form " + unsatisfiedAntecedent, bindings);
					List<Entity> satisfied = copy(satisfiedAntecedents);
					List<Entity> unsatisfied = copy(unsatisfiedAntecedents);
		            // Does not use properties since markers just apply to the overall causal relation, not its specific components
					Entity negation = Substitutor.substitute(unsatisfiedAntecedent, bindings.getBindings());
					negation = BasicMatcherOriginal.getBasicMatcher().dereference(negation, 0, storyProcessor.getStory());

					negation.addFeature(Markers.NOT);

					// Decided should not add negation to story

					// satisfied.add(negation);

					unsatisfied.remove(unsatisfiedAntecedent);
					completeDeduction(storyProcessor, elements, memory, satisfied, unsatisfied, bindings);
				}
			}
			// If we get here, cannot complete; limit to one unsatisfied antecedent
			// To do: Probably need to make sure there are no variables left in unsatisfied antecedent.
			if (unsatisfiedAntecedents.size() == 1) {
				// Entity consequent = memory.getRule().getObject();
				// Relation deduction = instantiateCause(satisfiedAntecedents, consequent, memory);
				// Mark.say("Partially satisfied rule\n", deduction);
				// Mark.say("Unsatisfied antecedent", Substitutor.substitute(unsatisfiedAntecedent, bindings));
				// Connections.getPorts(this).transmit(PARTIAL_DEDUCTION, new BetterSignal(deduction,
				// unsatisfiedAntecedent));
			}
		}
	}

	private static Entity removeActionReification(Entity t) {
		boolean debug = false;
		if (t.entityP()) {
			return t;
		}
		else if (t.relationP()) {
			t.setSubject(removeActionReification(t.getSubject()));
			t.setObject(removeActionReification(t.getObject()));
			return t;
		}
		else if (t.sequenceP()) {
			Vector<Entity> things = t.getElements();
			for (int i = 0; i < things.size(); ++i) {
				things.set(i, removeActionReification(things.get(i)));
			}
			return t;
		}
		// The big one
		else if (t.functionP()) {
			// Check to see if action inside of appear
			Function d = (Function) t;
			Entity subject = d.getSubject();
			if (d.isAPrimed(Markers.APPEAR_MARKER) && subject.isAPrimed(Markers.ACTION_WORD)) {
				Mark.say(debug, "Triggered dereification of", d.asString());
				return removeActionReification(subject);
			}
			else {
				Mark.say(debug, "Passing on dereification of ", d.asString());
				d.setSubject(removeActionReification(t.getSubject()));
				return d;
			}
		}
		System.err.println("Bug in StoryProcessor.removeActionReification; no if-else triggered");
		return null;
	}

	private static List<Entity> copy(List<Entity> input) {
		List<Entity> result = new ArrayList<>(input);
		return result;
	}

	private static List<Entity> copyAndReverse(List<Entity> things) {
		List<Entity> result = copy(things);
		Collections.reverse(result);
		return result;
	}

	private Relation constructCause(List<Entity> antecedents, Entity consequent, List<String> markers) {
		Sequence subject = new Sequence(Markers.CONJUNCTION);
		antecedents.stream().forEachOrdered(a -> subject.addElement(a));
		Relation rule = new Relation(Markers.CAUSE_MARKER, subject, consequent);
		markers.stream().forEachOrdered(m -> rule.addType(m));
		return rule;
	}

	public ExplanationContinuationInterface getExplanationContinuationTestor() {
		if (explanationContinuationTestor == null) {
			explanationContinuationTestor = new DefaultExplanationTestor();
		}
		return explanationContinuationTestor;
	}


	public void setExplanationContinuationTestor(ExplanationContinuationInterface explanationContinuationInterface) {
		this.explanationContinuationTestor = explanationContinuationInterface;
	}

	public void setRuleSorter(RuleSorterInterface ruleSorterInterface) {
		this.ruleSorter = ruleSorterInterface;
	}

}

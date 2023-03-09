package storyProcessor;

import generator.Generator;

import java.util.*;

import matchers.StandardMatcher;
import mentalModels.MentalModel;
import utils.Html;
import utils.minilisp.LList;
import utils.tools.Innerese;
import utils.*;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;

/**
 * There was a bug uncovered by Olga S. Now all rules associated with noted personality types are check with each
 * assertion. This could get expensive, so a possible upgrade would be to use hashing method developed in story
 * processor, but that should be done carefully so as not to duplicate code. Created on Feb 25, 2013
 * 
 * @author phw
 */

public class TraitExpert extends AbstractWiredBox {

	// HashMap<String, ArrayList<Entity>> predictionRuleMap = new HashMap<>();
	//
	// HashMap<String, ArrayList<Entity>> explanationRuleMap = new HashMap<>();
	//
	// public TraitExpert() {
	// super("Trait expert");
	// Connections.getPorts(this).addSignalProcessor(StoryProcessor.PERSONALITY_TRAIT_PORT, this::decodeAndProcess);
	// }
	//
	// public void clear() {
	// predictionRuleMap.clear();
	// explanationRuleMap.clear();
	// // Mark.say("Cleared");
	// }
	//
	// public void decodeAndProcess(Object o) {
	// if (true) {
	// return;
	// }
	// boolean debug = false;
	// if (o instanceof BetterSignal) {
	// BetterSignal signal = (BetterSignal) o;
	//
	// String key = signal.get(0, String.class);
	// Mark.say(debug, "Personality key", key);
	// if (key == StoryProcessor.PROCESS_ALL_EXPLANATIONS) {
	// processAllExplanations(signal.get(1, Entity.class), signal.get(2, StoryProcessor.class));
	// }
	// else if (key == StoryProcessor.PROCESS_TRAIT_PREDICTIONS) {
	// processTraitPredictions(signal.get(1, Entity.class), signal.get(2, StoryProcessor.class));
	// }
	// else if (key == StoryProcessor.FIND_TRAIT_CONCEPTS) {
	// findTraitSpecificConcepts(signal.get(1, Entity.class), signal.get(2, StoryProcessor.class));
	// }
	// else if (key == StoryProcessor.EVENT_ADDED) {
	// inferPersonalityType(signal.get(1, Entity.class), signal.get(2, ArrayList.class));
	// }
	// else if (key == StoryProcessor.EVENT_ADDED_WITH_TRAIT) {
	// notePersonalityType(signal.get(1, Entity.class), signal.get(2, MentalModel.class));
	// }
	// else if (key == StoryProcessor.CLEAR) {
	// clear();
	// }
	// }
	// }
	//
	// public void findTraitSpecificConcepts(Entity element, StoryProcessor processor) {
	//
	// boolean debug = false;
	//
	// ArrayList<MentalModel> personalityModels = processor.getPersonalityModels(element);
	//
	// Sequence concepts = new Sequence();
	//
	// for (MentalModel personalityModel : personalityModels) {
	// Mark.say(debug, "Getting personality concepts for", element.asString(), "from", personalityModel.getName());
	// BetterSignal signal = new BetterSignal(StoryProcessor.DELIVER_TRAIT_CONCEPTS,
	// personalityModel.getConceptPatterns());
	// Connections.getPorts(this).transmit(StoryProcessor.PERSONALITY_TRAIT_PORT, signal);
	// }
	// }
	//
	// public void processAllExplanations(Entity element, StoryProcessor processor) {
	// // boolean debug = false;
	// //
	// // // No need to explain if already explained:
	// //
	// // if (processor.isAlreadyPredictedSansMeans(element)) {
	// // return;
	// // }
	// //
	// // // For this want to use all rules at once
	// //
	// // if (Switch.level3ExplantionRules.isSelected()) {
	// //
	// // // Hash eliminates duplicates
	// //
	// // HashSet<Entity> localRuleSet = new HashSet<Entity>();
	// //
	// // localRuleSet.addAll(processor.getExplanationRules(element));
	// //
	// // // MentalModels addition using explanation rules from mental model
	// //
	// // // See prediction rules for additional comments
	// //
	// // if (Switch.level5UseMentalModels.isSelected()) {
	// //
	// // ArrayList<MentalModel> personalityModels = processor.getPersonalityModels(element);
	// //
	// // for (MentalModel personalityModel : personalityModels) {
	// // Connections.wire(StoryProcessor.NEW_ELEMENT_PORT, processor, StoryProcessor.INJECT_ELEMENT_INTO_TRAIT_MODEL,
	// // personalityModel
	// // .getStoryProcessor());
	// // // Connections.wire(StoryProcessor.COMPLETE_STORY_PORT, this, MentalModel.STOP_STORY,
	// // // personalityModel);
	// // ArrayList<Entity> personalityRules = personalityModel.getExplanationRules(element);
	// // localRuleSet.addAll(personalityRules);
	// // }
	// // }
	// //
	// // // End of addition
	// // localRuleSet.stream().forEach(rule -> StoryProcessor.addConsequentToRuleMap((Relation) rule,
	// // explanationRuleMap));
	// //
	// // Object copy = StoryProcessor.getExplanationRules(element, explanationRuleMap);
	// //
	// // // Connections.getPorts(this).transmit(StoryProcessor.TO_BACKWARD_CHAINER, new BetterSignal(element, copy,
	// // // processor));
	// //
	// // processor.getBackwardChainer().backwardChain(element, processor, (ArrayList<Relation>) copy);
	// //
	// // }
	// }
	//
	// public void processTraitPredictions(Entity element, StoryProcessor processor) {
	//
	// boolean debug = false;
	//
	// HashSet<Entity> localRuleSet = new HashSet<>();
	//
	// ArrayList<MentalModel> personalityModels = processor.getPersonalityModels(element);
	//
	// for (MentalModel personalityModel : personalityModels) {
	//
	// // Temporarily connect story processor to trait-specific mental models. Enables trait-specific
	// // model to note and display conclusions reached that are trait specific.
	//
	// // Mark.say("Attempting to connect", this, "to", personalityModel.getStoryProcessor());
	//
	// Connections.wire(StoryProcessor.NEW_ELEMENT_PORT, processor, StoryProcessor.INJECT_ELEMENT_INTO_TRAIT_MODEL,
	// personalityModel
	// .getStoryProcessor());
	// ArrayList<Entity> personalityRules = personalityModel.getPredictionRules(element);
	// if (debug) {
	// if (personalityRules.isEmpty()) {
	// Mark.say("Found NO personality rules for", element.asString());
	// }
	// else {
	// Mark.say("Found personality rules for", element.asString());
	// }
	// for (Entity t : personalityRules) {
	// Mark.say("        ", t.asString());
	// }
	// }
	// localRuleSet.addAll(personalityRules);
	//
	// Mark.say(debug, "Provided", localRuleSet.size(), "rules");
	// }
	//
	// // End of addition
	//
	// localRuleSet.stream().forEach(rule -> StoryProcessor.addAntecedentsToRuleMap((Relation) rule,
	// predictionRuleMap));
	//
	// // Mark.say(true, "Working on", element.asString(), "with", localRules.size(), "rules");
	// // if (localRules.size() > 0) {Mark.say("First is", localRules.get(0).asString());}
	//
	// Mark.say(debug, "Calling forward chainer");
	//
	// ArrayList<Entity> rules = StoryProcessor.getPredictionRules(element, predictionRuleMap, true);
	//
	// processor.getForwardChainer().forwardChain(element, processor, rules);
	// // Connections.getPorts(this).transmit(StoryProcessor.TO_FORWARD_CHAINER, new BetterSignal(element, rules,
	// // processor, 0));
	//
	// }
	//
	// public void notePersonalityType(Entity element, MentalModel personalityModel) {
	//
	// boolean debug = false;
	//
	// List<Entity> personalityExamples = personalityModel.getExamples();
	//
	// for (Entity personalityExample : personalityExamples) {
	//
	// if (StandardMatcher.getBasicMatcher().match(element, personalityExample) != null) {
	//
	// // Say person is acting representatively.
	//
	// Relation r = new Relation(Markers.PROPERTY_TYPE, element.getSubject(), personalityModel);
	// // Relation r = RoleFrames.makeRoleFrame(element.getSubject(), Markers.PROPERTY_TYPE, personalityModel);
	// r = ISpeak.makeCause(element, r);
	// String s = Generator.getGenerator().generate(r);
	//
	// Mark.say(debug, "Personality model", personalityModel.getName());
	// Mark.say(debug, s, "\n", element.asStringWithIndexes(), "\n", personalityExample.asStringWithIndexes());
	//
	// s = Html.p(s);
	// BetterSignal signal = new BetterSignal(Markers.PERSONALITY_ANALYSIS_TAB, s);
	//
	// Connections.getPorts(this).transmit(StoryProcessor.COMMENTARY_PORT, signal);
	// }
	// else {
	// // Mark.say("No match");
	// }
	// }
	//
	// }
	//
	// public void inferPersonalityType(Entity element, ArrayList<MentalModel> models) {
	// // See if personality type can be inferred
	//
	// boolean debug = true;
	//
	// Entity subject = element.getSubject();
	//
	// // Will look for trait behavior in all models
	// for (MentalModel model : models) {
	//
	// if (StoryProcessor.exists(subject, Markers.PERSONALITY_TRAIT, model)) {
	// String s = subject.asString() + " is " + model.getName() + " is already known";
	// continue;
	// }
	//
	// // Look in memory for this model
	// List<Entity> memory = model.getExamples();
	//
	// // Mark.say("Tick", model.getName(), memory.size());
	//
	// ArrayList<Entity> additions = new ArrayList<Entity>();
	// // Iterate over all memory elements
	// for (Entity rememberedElement : memory) {
	// // Mark.say("Remembering:\n", element.asStringWithIndexes(), "\n",
	// // rememberedElement.asStringWithIndexes());
	// // See if this one matches
	// LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(element, rememberedElement);
	// if (bindings != null) {
	// // Assert that action suggests personality type.
	// Relation r = new Relation(Markers.PROPERTY_TYPE, element.getSubject(), model);
	// r = ISpeak.makeRoleFrame(r, "seem");
	// r = ISpeak.makeCause(r, element);
	// String s = Generator.getGenerator().generate(r);
	//
	// s = Html.p(s);
	//
	// BetterSignal signal = new BetterSignal(Markers.PERSONALITY_ANALYSIS_TAB, s);
	//
	// // Hack
	// Connections.getPorts(this).transmit(StoryProcessor.COMMENTARY_PORT, signal);
	//
	// // Entity result = new Relation(Markers.PROPERTY_TYPE, element.getSubject(), model);
	// // result.addType(Markers.PERSONALITY_TRAIT);
	// Entity result = RoleFrames.makeRoleFrame(element.getSubject(), Markers.PROPERTY_TYPE, model);
	// result.addType(Markers.PERSONALITY_TRAIT);
	// // Mark.say("Relation adds trait", model);
	// Mark.say(debug, "About to try to add", result.asString(), "to the story");
	// Mark.say(debug, "Because", element.asString(), "matches", rememberedElement.asString(), "from", model.getName());
	// additions.add(result);
	//
	// BetterSignal signal2 = new BetterSignal(StoryProcessor.DELIVER_TRAIT_CHARACTERIZATION, result, element,
	// bindings);
	// Connections.getPorts(this).transmit(StoryProcessor.PERSONALITY_TRAIT_PORT, signal2);
	//
	// break;
	// // Mark.say("Noting:", result.asString());
	//
	// }
	//
	// // BetterSignal signal = new BetterSignal(Markers.PERSONALITY_ANALYSIS_TAB, s);
	// //
	// // Connections.getPorts(this).transmit(COMMENTARY_PORT, signal);
	// }
	// }
	// }

}

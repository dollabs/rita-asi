package storyProcessor;

import java.util.*;
import java.util.stream.Collectors;

import conceptNet.conceptNetModel.ConceptNetJustification;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Matcher;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import generator.Generator;
import generator.ISpeak;
import generator.RoleFrames;
import generator.Rules;
import genesis.StoryAnalysis;
import gui.*;
import matchers.*;
import mentalModels.*;
import rules.*;
import start.Start;


import translator.Translator;
import utils.*;
import utils.NewTimer;
import utils.minilisp.LList;
import utils.tools.Getters;
import utils.tools.Predicates;

/*
 * The purpose of this class to recognize the start and end of stories and to retain all the information in a story on a
 * sequence. When story is over, the sequence is emitted. Created on Mar 7, 2009
 * @author phw
 */

public class StoryProcessor extends AbstractWiredBox {
	
	public static boolean debug = false;

	public static final String PLOT_PLAY_BY_PLAY_PORT = "NEW_PLOT_ELEMENT_PORT";

	// Mostly ports

	// public final static String TEST_ME = "test me hard";

	public final static String FROM_SHORT_CUT = "from short cut";

	public final static String TAB = "tab";

	public static final String STORY_NAME = "story name";

	public static final String INJECT_RULE = "port for rule injection";

	public static final String INJECT_CONCEPT = "port for concept injection";

	public static final String INJECT_ELEMENT = "port for story element injection";

	public static final String INJECT_ELEMENT_WITHOUT_DEREFERENCE = "port for story element injection without dereference";

	public static final String TEST_ELEMENT = "port for testing to see if element supported by may rule";

	public static final String INJECT_ELEMENT_INTO_TRAIT_MODEL = "port for story element injection into trait-specific mental model";

	public final static String STOP = "Stop";

	public final static String STARTING = "Starting";

	public final static String CLEAR = "Clear";

	public final static String INFERENCE = "Inference";

	// Determines if anything is noted at all, including rules and concepts
	private boolean asleep = false;

	// Determines if story is in process of being read and added to
	private boolean storyInProgress = false;

	// private boolean milestoneEncountered = false;

	public static final String INFERENCES = "inferences";

	public static final String FINAL_INFERENCES = "final-inferences";

	public static final String FINAL_INPUTS = "final-inputs";

	// public static final String RULE = "rule";

	public static final String CONTROL = "control";

	public static final String DESCRIBE = "describe";

	public static final String CONCEPTS_VIEWER_PORT = "concepts port";

	// public static final String CONCEPT_ONSET_VIEWER_PORT = "concept onset port";

	public static final String INDICATIONS_PORT = "indications port";

	public static final String INSTRUCTION_PORT = "indications port";

	public static final String RULE_PORT = "rule port";

	// Legacy: contains explicit elements, inferred elements, and instantiated inferences
	public static final String COMPLETE_STORY_EVENTS_PORT = "complete story events port";

	// Better: contains more
	public static final String COMPLETE_STORY_ANALYSIS_PORT = "complete story analysis port";

	// Best: sends over whole story processor
	public static final String STORY_PROCESSOR_PORT = "story processor port";
	
	public static final String COMPLETE_CONCEPTNET_JUSTIFICATION_STRING_PORT = "complete conceptnet justification string port";

	// Might as well send along whole story processor when done

	public static final String STORY_PROCESSOR_SNAPSHOT = "story processor snapshot";

	public static final String BROADCAST_SNAPSHOT = "broadcast snapshot";

	public static final String COMPLETE_KNOWLEDGE_CONTENT_PORT = "complete knowledge content port";

	public static final String NEW_ELEMENT_PORT = "new  element port";

	public static final String BRIEFING_PORT = "briefing port";

	public static final String GAP_FILLER_PORT = "gap filler port";

	public static final String INCREMENT_PORT = "increment port";

	public static final String INCREMENT_PORT_COMPLETE = "increment port complete";

	public static final String STORY = "story";

	public static final String CLEAR_CONCEPTS = "clear concepts";

	// public static final String ONSET = "onset";

	public static final String TARGET_STORY = "target-story";

	public static final String STORY_PROCESSOR = "processor";

	public static final String STORY_WORKBENCH_INPUT = "story workbench input";

	// public static final String FROM_FORWARD_CHAINER = "from forward chainer port";

	// public static final String TO_FORWARD_CHAINER = "to forward chainer port";

	public static final String PREDICTION_RULES = "prediction rules";

	// public static final String FROM_BACKWARD_CHAINER = "from chainer chainer port";

	// public static final String TO_BACKWARD_CHAINER = "to backward chainer port";

	// public static final String TO_ONSET_DETECTOR = "to onset detector port";

	// public static final String TO_COMPLETION_DETECTOR = "to completion detector port";

	public static final String NEW_INFERENCE_PORT = "new inference port";

	public static final String EXPLANATION_RULES_PORT = "explanation rules port";

	public static final String PREDICTION_RULES_PORT = "prediction rules port";

	public static final String RECORD_PORT = "record port";

	public static final String COMMENTARY_PORT = "commentary port";

	public static final String CLUSTER_STORY_PORT = "cluster story port";

	public static final String TO_STATISTICS_BAR = "statistics bar output port";

	public static final String INCOMING_INSTANTIATIONS = "instantiated concepts from concept expert";

	public static final String INSTANTIATED_CONCEPTS = "instantiated concepts";

	public static final String INCOMING_CONCEPT_ANALYSIS = "concept analysis from concept expert";

	public static final String CONCEPT_ANALYSIS = "concept analysis";

	public static final String RESET_CONCEPTS_PORT = "reset concepts port";

	public static final String PERSONALITY_TRAIT_PORT = "personality port";

	public static final String RESET_RESULTS_VIEWER = "reset story";

	public static final String ONSET_VIEWER_PORT = "onset viewer port";

	// Indicators

	// public static final String EVENT_ADDED = "event added";
	// public static final String EVENT_ADDED_WITH_TRAIT = "event added with trait";

	public static final String PROCESS_TRAIT_PREDICTIONS = "process trait predictions";

	public static final String PROCESS_ALL_EXPLANATIONS = "process trait explanations";

	public static final String FIND_TRAIT_CONCEPTS = "find trait-specific concepts";

	public static final String DELIVER_TRAIT_CONCEPTS = "deliver trait-specific concepts";

	public static final String DELIVER_TRAIT_CHARACTERIZATION = "deliver trait characteization";

	// Be careful changing this. Use only for world viewing

	public static boolean allowMultipleEntriesInSameScene = false;
	// Other stuff

	private Sequence story = new Sequence(STORY);

	// private Sequence inferences = new Sequence();

	// private Sequence explicitElements = new Sequence();

	private Sequence concepts = new Sequence(Markers.CONCEPT_MARKER);

	private Sequence instantiatedConcepts = new Sequence();

	private Sequence instantiatedConceptPatterns;

	private ConceptAnalysis conceptAnalysis;

	// private Sequence concepts = new Sequence(CONCEPT);

	// private Sequence conceptOnsets = new Sequence(ONSET);

	// private HashMap<String, ArrayList<Entity>> predictionRuleMap = new HashMap<String, ArrayList<Entity>>();

	// private HashMap<String, ArrayList<Entity>> explanationRuleMap = new HashMap<String, ArrayList<Entity>>();
	//
	// private HashMap<String, ArrayList<Entity>> censorRuleMap = new HashMap<String, ArrayList<Entity>>();

	private HashMap<String, ArrayList<Entity>> onsetRuleMap = new HashMap<String, ArrayList<Entity>>();

	private HashMap<String, ArrayList<Entity>> completionRuleMap = new HashMap<String, ArrayList<Entity>>();

	private HashMap<String, ArrayList<Sequence>> conceptMap = new HashMap<String, ArrayList<Sequence>>();

	private ArrayList<HashMap<String, Entity>> sceneMapList;

	private HashMap<String, Entity> globalCache;

	private HashMap<String, Entity> mentalModelCache;

	private HashMap<String, Entity> wantCache;

	private HashSet<String> alreadyPredicted;

	// Elements appearing in story
	// private HashMap<String, Entity> storyCache;

	// Elements appearing in scene
	// private HashMap<String, Entity> sceneCache;

	// Elements appearing in story with embedded as well as top-level elements
	// private HashMap<String, Entity> completeStoryCache;

	// Elements appearing in scene with embedded as well as top-level elements
	// private HashMap<String, Entity> completeSceneCache;

	// private HashMap<String, Entity> embeddedCache;


	private String currentPersona = "default";

	// private SelfReflector selfReflector;

	private String name;

	private boolean playByPlayInput = false;

	private boolean precedentInput = false;

	int sizeAtPreviousQuiescence = 0;

	ConceptExpert conceptExpert;

	private MentalModel mentalModel;

	// For Sila

	public static String START_STORY_INFO_PORT = "start story info port";

	public static String LEARNED_RULE_PORT = "learned rule port";

	public static String NEW_RULE_MESSENGER_PORT = "new rule messenger port";

	public static String EXPLICIT_STORY = "explicit story";

	public static String COMMAND_PORT = "command port";

	private boolean newRuleComingIn = false;

	private Entity learnedRule = new Entity();

	// For Caroline
	public static String REDUNDANT_ELEMENT_PORT = "redundant element port";

	// For dealing with reader traints

	Entity reader;

	private boolean inert;

	// Personality traits from scrubbing of June 2015

	private Map<String, Set<String>> personalityTypes;

	public Map<String, Set<String>> getPersonalityTypes() {
		if (personalityTypes == null) {
			personalityTypes = new HashMap<>();
		}
		return personalityTypes;
	}

	public void addPersonalityType(String name, String type) {
		Set<String> types = getPersonalityTypes().get(name);
		if (types == null) {
			types = new HashSet<String>();
			personalityTypes.put(name, types);

		}
		if (!types.contains(type)) {
			// Mark.say("Adding personality type", type, "to", name);
			types.add(type);
		}
	}

	public void clearPersonalityTypes() {
		getPersonalityTypes().clear();
	}



	// NewTimer backwardChainingTimer = NewTimer.getTimer("Backward chaining time");
	// NewTimer forwardChainingTimer = NewTimer.getTimer("Forward chaining time");

	public StoryProcessor() {
		this("Unnamed story processor");
	}

	public StoryProcessor(String name) {
		super(name);

		// Connections.getPorts(this).addSignalProcessor(FROM_SHORT_CUT, this::processFromShortCut);

		// Principal inputs
		Connections.getPorts(this).addSignalProcessor(Port.INPUT, this::processElement);
		// Connections.getPorts(this).addSignalProcessor(FROM_FORWARD_CHAINER, this::processForwardRule);
		// Connections.getPorts(this).addSignalProcessor(FROM_BACKWARD_CHAINER, this::processBackwardRule);

		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::processStageDirections);

		// Internal connections out
		// Connections.wire(TO_FORWARD_CHAINER, this, getForwardChainer());
		// Connections.wire(TO_BACKWARD_CHAINER, this, getBackwardChainer());

		// Internal connections in
		// Connections.wire(getForwardChainer(), FROM_FORWARD_CHAINER, this);
		// Connections.wire(EXPLANATION_RULES_PORT, getBackwardChainer(), FROM_BACKWARD_CHAINER, this);

		// Connections.wire(PREDICTION_RULES_PORT, getForwardChainer(), PREDICTION_RULES_PORT, this);
		// Connections.wire(PREDICTION_RULES_PORT, getBackwardChainer(), PREDICTION_RULES_PORT, this);

		Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getConceptExpert(), ConceptExpert.INSTANTIATED_CONCEPTS, this);
		Connections.getPorts(this).addSignalProcessor(ConceptExpert.INSTANTIATED_CONCEPTS, this::processInstantiatedConcepts);

		// Fay's teaching ports
		Connections.getPorts(this).addSignalProcessor(PREDICTION_RULES_PORT, this::transmitUsedRules);
		Connections.getPorts(this).addSignalProcessor(CONCEPTS_VIEWER_PORT, "insertConceptSet");
		Connections.getPorts(this).addSignalProcessor(RULE_PORT, this::insertRuleSet);
		Connections.getPorts(this).addSignalProcessor(INCOMING_INSTANTIATIONS, this::setInstantiations);
		Connections.getPorts(this).addSignalProcessor(INCOMING_CONCEPT_ANALYSIS, this::setConceptAnalysis);

		// Sila's work: to become aware of learned rules
		Connections.getPorts(this).addSignalProcessor(LEARNED_RULE_PORT, this::recordRule);
		Connections.getPorts(this).addSignalProcessor(NEW_RULE_MESSENGER_PORT, this::processMessenger);
		Connections.getPorts(this).addSignalProcessor(INJECT_ELEMENT, this::injectElementWithDereference);
		Connections.getPorts(this).addSignalProcessor(INJECT_ELEMENT_WITHOUT_DEREFERENCE, this::injectElementWithoutDereference);
		Connections.getPorts(this).addSignalProcessor(TEST_ELEMENT, this::testElementInjection);
		// Connections.getPorts(this).addSignalProcessor(INJECT_ELEMENT_INTO_TRAIT_MODEL,
		// this::processElementInjectionWithMentalModelFilter);
		Connections.getPorts(this).addSignalProcessor(INJECT_RULE, this::processRuleInjection);
		Connections.getPorts(this).addSignalProcessor(INJECT_CONCEPT, this::processConceptInjection);

		Connections.getPorts(this).addSignalProcessor(this.PERSONALITY_TRAIT_PORT, this::processTraitAddition);
		
		// Capen's hack, purpose unknown
		// selfReflector = new SelfReflector(this);
	}

	public StoryProcessor(String name, MentalModel mentalModel) {
		this(name);
		setMentalModel(mentalModel);
	}

	//

	public ConceptExpert getConceptExpert() {
		if (conceptExpert == null) {
			conceptExpert = new ConceptExpert();
		}
		return conceptExpert;
	}

	// Sila's function for letting perspective know there is new rule. July 22,
	// 2011
	public void processMessenger(Object o) {
		newRuleComingIn = true;
	}

	public void processStageDirections(Object o) {
		if (!(o instanceof String)) {
			return;
		}
		String s = (String) o;

		if (s == Markers.RESET) {
			clearAllMemories();
			return;
		}

		// Mark.say("Stage direction:", s);
		if (s.equalsIgnoreCase(Start.NEITHER)) {
			// Mark.say("Setting", this.getName(), "asleep");
			setAwake(false);
		}
		else if (s.equalsIgnoreCase(this.getName()) || s.equalsIgnoreCase(Start.BOTH)) {
			// Mark.say("Setting", this.getName(), "awake");
			setAwake(true);
		}

		if (isAsleep()) {
			return;
		}
		if (s == Markers.THE_END) {
			stopStory();
			for (MentalModel model : getMentalModel().getLocalMentalModels().values()) {
				model.getStoryProcessor().stopStory();
			}

		}

		// else if (s == Markers.REPLAY) {
		// replay();
		// }
		// [laf 1 July 2011]
		else if (s == Markers.RADIATE_COMMONSENSE) {
			radiate();
		}
		// [laf 19 July 2011]
		else if (s == Markers.INSERT_BIAS) {
			insert();
		}
		else if (s == Markers.BROADCAST_STATUS) {
			broadcastStatus();

		}
	}

	// [laf 21 July 2011]
	// These stubs are used in order to process this idiom here, instead of any
	// local processor
	// hooked up to this port
	private void radiate() {
	}

	private void insert() {
	}

	// private void replay() {
	// // Mark.betterSay("Hello Leonid");
	// for (Entity t : story.getElements()) {
	// if (t.isA(Markers.CAUSE_MARKER)) {
	// continue;
	// }
	// Mark.say("Replay");
	// process(t);
	// }
	// }

	public void insertRuleSet(Object x) {
		if (x instanceof Sequence) {
			Sequence s = (Sequence) x;
			currentPersona = s.getType();
			for (Entity t : s.getElements()) {
				Mark.say("RULE MAYBE: ", t.asString());
				recordRule(t);
			}
		}
	}

	// public void insertConceptSet(Object x) {
	// if (x instanceof Sequence) {
	// Sequence s = (Sequence) x;
	// currentPersona = s.getType();
	// for (Thing t : s.getElements()) {
	// Sequence concept = (Sequence) t;
	// recordConcept(concept);
	// }
	// }
	// }

	public void processForwardRule(Object x) {
		Mark.say("processing " + x);
		BetterSignal signal = BetterSignal.isSignal(x);
		if (signal == null) {
			return;
		}

		// MentalModels addition---notes mental model location of rule that generates instantiated rule

		Entity instantiatedRule = signal.get(0, Entity.class);
		Entity rule = signal.get(1, Entity.class);

		processForwardRuleDirectCall((Relation) rule, (Relation) instantiatedRule);

		// processMentalModelConnections(rule, instantiatedRule);
		// addElement(instantiatedRule, story);
	}

	public void processForwardRuleDirectCall(Relation rule, Relation instantiatedRule) {
		// MentalModels addition---notes mental model location of rule that generates instantiated rule
		processMentalModelConnections(rule, instantiatedRule);
		// End of addition
		addElement(instantiatedRule, story);
	}

	public void processBackwardRule(Object x) {

		BetterSignal signal = BetterSignal.isSignal(x);
		if (signal == null) {
			return;
		}

		Entity instantiatedRule = signal.get(0, Entity.class);

		if (signal.size() >= 2) {
			Entity rule = signal.get(1, Entity.class);
			processMentalModelConnections(rule, instantiatedRule);
		}
		addElement(instantiatedRule, story);
	}

	public void processBackwardRuleDirectCall(Relation rule, Relation instantiatedRule) {
		if (rule != null) {
			processMentalModelConnections(rule, instantiatedRule);
		}
		// Mark.say("Adding", instantiatedRule);
		addElement(instantiatedRule, story);

	}

	/**
	 * Checks to see if rule is associated with mental model(s), and if so, connects instantiated rule to same mental
	 * model(s).
	 */
	public void processMentalModelConnections(Entity rule, Entity instantiatedRule) {
		instantiatedRule.addProperty(Markers.MENTAL_MODEL_HOST, rule.getProperty(Markers.MENTAL_MODEL_HOST));
	}

	public void transmitUsedRules(Object x) {
		BetterSignal signal = BetterSignal.isSignal(x);
		if (signal == null) {
			return;
		}
		Connections.getPorts(this).transmit(PREDICTION_RULES_PORT, signal.get(0, Entity.class));
	}

	/*
	 * Key to whole works. Adds elements, which may be instantiated rules.
	 */

	public boolean addElement(Entity element, Sequence story) {
		return addElement(element, story, true);
	}

	public boolean addElementWithoutDereference(Entity element, Sequence story) {
		return addElement(element, story, false);
	}

	public boolean addElement(Entity element, Sequence story, boolean dereference) {
		boolean debug = false;
		//Mark.say(debug, "Adding with addElement", element);
		if (element == null) {
			return false;
		}

		Connections.getPorts(this)
		        .transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.STORY_TIMER, NewTimer.storyTimer.time()));

		// for (Entity x : getCurrentSceneMap().values()) {
		// Mark.say("In the scene now:", x);
		// }

		// If it is a milestone element, add a new scene
		if (milestoneP(element)) {
			// Mark.say("Properties entering", element.getPropertyList());
			Mark.say(debug, "Checking", element);
			addSceneMap();
			// Mark.say("Hit new scene marker, now there are", getSceneMapList().size());
		}

		Mark.purple(debug, "Add element:   ", element.asStringWithIndexes());
		if (element.getType().equals("see")) {
			Z.understand(element);
		}

		Entity mentalModelElement = null;

		// At this point, need to check causes to see if antecedents or consequent is already in story and replace them
		// if so. This is needed because START does not note such correspondences and links would not be noted in
		// Genesis; BUT, don't do it if we are in a concept because asserts antecedent and consequent!

		// if (!story.isA(Markers.CONCEPT_MARKER)) {
		// element = dereferenceAntecedentsAndConsequents(element);
		// }

		// Mark.say("Before x", element.asStringWithIndexes());

		// Very delicate

		if (dereference) {
			element = reassembleAndDereference(element);
		}


		if (Predicates.isExplictCauseOrLeadsTo(element)) {
			// Want to add antecedents here after dereferencing; does nothing if already there
			for (Entity e : element.getSubject().getElements()) {
				processElement(e, dereference);
			}

		}

		// If already there, stop now
		// if (isInCurrentScene(element) && !isCommand(element)) {
		if (isInCurrentScene(element)) {
			// Mark.say("Is in current scene", element.asStringWithIndexes());

			// Mark.say("ELEMENT SEEN!!");
			// Mark.say(getCurrentSceneMap().get(hash(element)).);

			Connections.getPorts(this).transmit(REDUNDANT_ELEMENT_PORT, element);
			// Mark.say("the element is already present " + element);

			if (!allowMultipleEntriesInSameScene) {
				return false;
			}
		}

		try {
			if (isInCurrentScene(element.getObject())) {
				// Mark.say("OBJECT SEEN!!");
				// Mark.say(getCurrentSceneMap().get(hash(element.getObject())));

				/*
				 * public boolean isInCurrentScene(Entity e) { return isCached(e, getCurrentSceneMap()); } private
				 * boolean isCached(Entity t, HashMap<String, Entity> cache) { if (cache.get(hash(t)) != null) { return
				 * true; } return false; }
				 */

				Connections.getPorts(this).transmit(REDUNDANT_ELEMENT_PORT, element);
			}
		}
		catch (Exception e) {

		}

		// If it is a concept story, nothing more to do
		if (story.isAPrimed(Markers.CONCEPT_MARKER)) {
			augmentCurrentSceneIfNotInCurrentScene(element, story);
			return false;
		}

		// If it is a causal connection, rebuild antecedents using whole story, but using only scene for consequent.
		// Idea is that this may be a fresh action, say a new harming.
		if (element.relationP(Markers.CAUSE_MARKER) && !isInert()) {
			// Mark.say("Original", element.asStringWithIndexes());

			// Used lower down to put appropriate causes into mental model viewer
			MentalModel host = MentalModel.getMentalModelHosts(element);

			if (host != null) {
				host.getStoryProcessor().processElementWithInertInjection(element);
			}

			// Mark.say("Adding inference", element);

			// Add connection to inferences
			addInference(element);
			// Put it in current scene and into the story if not there
			augmentCurrentSceneIfNotInCurrentScene(element, story);
			if (story.isAPrimed(Markers.CONCEPT_MARKER)) {
				return false;
			}
			// Insert antecedents into current scene and trigger explanation rules if not there yet
			for (Entity antecedent : element.getSubject().getElements()) {
				if (augmentCurrentSceneIfNotInAnyScene(antecedent, story)) {
					Connections.getPorts(this).transmit(NEW_ELEMENT_PORT, antecedent);
					// Mark.say("1 Triggering on antecedent", antecedent);
					triggerRules(antecedent, story);
				}
			}
			// Insert consequent into current scene and trigger explanation rules if not there yet
			if (augmentCurrentSceneIfNotInCurrentScene(element.getObject(), story)) {
				Connections.getPorts(this).transmit(NEW_ELEMENT_PORT, element.getObject());
				// Make sure we don't try to explain consequent
				// Mark.say("2 Triggering on consequent", element.getObject());
				triggerRules(element.getObject(), story);
				addAlreadyPredicted(element.getObject());
			}

			noteInferenceCount();
			// }
			// else {
			// // Mark.say("Censored rule consequent", element.getObject());
			// return false;
			// }
		}
		// If not a cause, check to see if in current scene. If not, insert it.
		else {
			// Do nothing if censor says it cannot be.
			// if (!censored(element)) {
			// Otherwise rebuild and cache
			// Mark.say("Adding", element.asStringWithIndexes());
			// Mark.say("\n>>> Before", element);

			// element = makeNewVersion(element);

			// Mark.say("After ", element);
			// element = reassembleAndDereference(element);
			if (timeToBrief(element)) {
				// Mark.say("Transmitting on briefing port");
				Connections.getPorts(this).transmit(BRIEFING_PORT, this);
			}
			else if (augmentCurrentSceneIfNotInCurrentScene(element, story)) {
				// Connections.getPorts(this).transmit(NEW_ELEMENT_PORT, element);

				// Mark.say("3 Triggering on element", element);
				triggerRules(element, story);
			}
			// }
			// else {
			// // Mark.say("Censored element", element);
			// return false;
			// }
		}

		Connections.getPorts(this).transmit(NEW_ELEMENT_PORT, element);

		// mpfay 5/20, readded play by play output
		Connections.getPorts(this).transmit(PLOT_PLAY_BY_PLAY_PORT, new BetterSignal(element));

		// Trigger rules; note that it is possible for a cause to be an antecedent, so cannot do above where handling
		// cause

		// Diked out, causes problem with doubling of personality trait assumptions
		// triggerRules(element, story);

		if (Predicates.isExplictCauseOrLeadsTo(element)) {
			// Want to add consequent here after dereferencing
			processElement(element.getObject(), dereference);
		}

		return true;
	}

	private boolean timeToBrief(Entity element) {
		if (element.isA(Markers.BRIEFING_MARKER) && element.getSubject().isA(Markers.YOU) && RoleFrames.getObject(element).isA("it")) {
			// Mark.say("Time to brief!");
			return true;
		}
		return false;
	}

	// Functionality Moved
	private void noteIfElementExpressesReaderModel(Entity element, boolean debug) {
		if (!element.entityP() && element.getSubject().isA(Markers.i) && element.relationP(Markers.PERSONALITY_TRAIT)) {
			// String trait = element.getObject().getType();

			String trait = RoleFrames.getObject(element).getType();

			reader = element.getSubject();
			Mark.say("I note that the reader has personality trait", trait);
			MentalModel mentalModel = getMentalModel().getLocalMentalModel(trait);
			if (mentalModel != null) {
				MentalModel.transferAllKnowledge(mentalModel, getMentalModel());
				// ArrayList<Entity> predicters = mentalModel.getPredictionRules();
				// ArrayList<Entity> explainers = mentalModel.getExplanationRules();
				// Sequence concepts = mentalModel.getConceptPatterns();
				//
				// Mark.say(predicters != null, "Predicters", predicters.size());
				// Mark.say(explainers != null, "Explainers", explainers.size());
				// Mark.say(concepts != null, "Concepts", concepts.getElements().size());

			}
			else if (mentalModel == null) {
				Mark.err("I have no mental model for", trait);
			}
		}
	}

	private void noteInferenceCount() {
		Connections.getPorts(this).transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.INFERENCE_COUNT,
		        getInferredElements().getElements().size()));
	}

	private void replaceCachedElements(Entity element, HashMap<String, Entity> aMap, HashMap<String, Entity> cMap) {
		Sequence antecedents = new Sequence(element.getSubject().getType());
		// antecedents.setBundle(element.getSubject().getBundle());
		for (Entity t : element.getSubject().getElements()) {
			// Mark.say("Looking in cache for", asHash(t));
			// Mark.say("Cache contains");
			for (String s : aMap.keySet()) {
				// Mark.say(" Key:", s, "-->", aMap.get(s).asStringWithNames());
			}
			Entity extant = getFromCache(t, aMap);
			if (extant != t) {
				// Mark.say("Replacing antecedent", t.asString(), "with",
				// extant.asStringWithNames());
			}
			antecedents.addElement(extant);
		}
		element.setSubject(antecedents);
		Entity extant = getFromCache(element.getObject(), cMap);
		if (extant != element.getObject()) {
			// Mark.say("Replacing consequent", element.getObject().asString(),
			// "with", extant.asString());

			element.setObject(extant);
		}
	}

	private boolean testBindings(LList<PairOfEntities> bindings) {
		if (bindings == null) {
			return false;
		}
		for (Iterator<PairOfEntities> i = bindings.iterator(); i.hasNext();) {
			PairOfEntities pair = i.next();
			if (pair.getDatum() != pair.getPattern()) {
				return false;
			}
		}
		return true;
	}

	// private boolean censored(Entity element) {
	// if (true || element == null) {
	// return false;
	// }
	// boolean result = RuleMatcher.getRuleMatcher().censor(element, story, getCensorRules(element, censorRuleMap,
	// false));
	// return result;
	// }

	private synchronized void triggerRules(Entity element, Sequence story) {

		boolean debug = false;

		Mark.say(debug, "\n>>> Working on", element);

		NewTimer.ruleProcessingTimer.reset();

		// If it happens to be a time relation, process pieces, special case
		if (element.relationP() && element.isAPrimed(Markers.TIME_MARKER)) {
			Relation r = (Relation) element;
			addElement(r.getSubject(), story);
			addElement(r.getObject(), story);
			// Don't want to return here, may want to trigger on time related entities
			// return;
		}
		// Otherwise predict, explain, anticipate, complete

		getRuleEngine().process(element, this);

		if (Switch.levelLookForMentalModelEvidence.isSelected()) {
			// Now, see if action in line with personality type
			inferPersonalityType(element, getMentalModel().getLocalMentalModels());
		}

		if (Switch.level4ConceptPatterns.isSelected()) {

			// Idea is to add concepts as we go that come form observed personality types for use as we go or at
			// the end.

			if (Switch.level5UseMentalModels.isSelected()) {

				// Sends concepts back for use in this story processor
				BetterSignal signal = new BetterSignal(FIND_TRAIT_CONCEPTS, element, this);
				Connections.getPorts(this).transmit(PERSONALITY_TRAIT_PORT, signal);
			}

			// End of addition

			if (Switch.findConceptsContinuously.isSelected()) {
				Mark.say(debug, "Looking for concepts as I go");
				BetterSignal signal = new BetterSignal(concepts, story, getInferences());

				getConceptExpert().process(signal);
				// Connections.getPorts(this).transmit(TO_COMPLETION_DETECTOR, signal);
			}
		}
		NewTimer.ruleProcessingTimer.report(true, "Rule processing slow beyond description");
	}

	public void inferPersonalityType(Entity element, Map<String, MentalModel> map) {

		// See if personality type can be inferred
		boolean debug = false;

		Entity subject = element.getSubject();

		// Will look for trait behavior in all models
		for (MentalModel model : map.values()) {
			if (StoryProcessor.exists(subject, Markers.PERSONALITY_TRAIT, model)) {
				String s = subject.asString() + " is " + model.getName() + " is already known";
				continue;
			}
			// Look in memory for this model
			List<Entity> memory = model.getExamples();
			// Iterate over all memory elements
			for (Entity rememberedElement : memory) {
				// See if this one matches
				LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(element, rememberedElement);
				if (bindings != null) {
					// Assert that action suggests personality type.
					Entity r = new Relation(Markers.PROPERTY_TYPE, element.getSubject(), model);
					r = ISpeak.makeRoleFrame(r, "seem");
					r = ISpeak.makeCause(r, element);
					String s = Generator.getGenerator().generate(r);

					s = Html.p(s);

					BetterSignal signal = new BetterSignal(Markers.PERSONALITY_ANALYSIS_TAB, s);

					// Hack
					Connections.getPorts(this).transmit(StoryProcessor.COMMENTARY_PORT, signal);

					Entity result = RoleFrames.makeRoleFrame(element.getSubject(), Markers.PROPERTY_TYPE, model);
					result.addType(Markers.PERSONALITY_TRAIT);
					Mark.say(debug, "About to try to add", result.asString(), "to the story");
					Mark.say(debug, "Because", element.asString(), "matches", rememberedElement.asString(), "from", model.getName());
					processElement(result);

					break;

				}
			}
		}
	}

	/**
	 * See if connection exists
	 */
	public static boolean exists(Entity s, String r, Entity o) {

		if (s == null || r == null) {
			return false;
		}

		Vector<Function> relations = s.getSubjectOf(r);

		for (Entity relation : relations) {
			Entity object = relation.getObject();
			// Mark.say("LOOKING AT", object.getType(), "and", o.getType());
			if (object.getType().equals(o.getType())) {
				// Mark.say("yes");
				return true;
			}
		}
		// Mark.say("false");
		return false;
	}

	public ArrayList<MentalModel> getPersonalityModels(Entity element) {
		boolean debug = false;
		ArrayList<MentalModel> personalityModels = new ArrayList<>();
		ArrayList<Entity> participants = fetchParticipants(element);
		Mark.say(debug, "Participants count in", element, "is", participants.size(), participants);
		Set<String> modelTypes = new HashSet<>();
		for (Entity participant : participants) {
			Set<String> types = getPersonalityTypes().get(participant.getName());
			if (types != null) {
				modelTypes.addAll(types);
			}
		}
		for (String type : modelTypes) {
			MentalModel personalityModel = getMentalModel().getLocalMentalModel(type);

			if (personalityModel != null) {
				personalityModels.add(personalityModel);
			}
			else {
				// Mark.err("No personality model of type", type, "for", participants);
			}

		}
		return personalityModels;
	}

	// public ArrayList<MentalModel> getPersonalityModels(Entity element) {
	// boolean debug = false;
	// ArrayList<MentalModel> personalityModels = new ArrayList<MentalModel>();
	// ArrayList<Entity> entities = fetchParticipants(element);
	// // Mark.say("Participants", entities.size());
	// for (Entity subject : entities) {
	// if (subject.isA("person") || subject.isA(Markers.i)) {
	// // if (subject.isA("person") ) {
	// // Mark.say("Looking for personalities of", element.asString());
	// HashSet<String> personalities = new HashSet<String>();
	// // First, look for connection via personality_trait
	// for (Entity t : subject.getSubjectOf()) {
	// if (t.relationP(Markers.PERSONALITY_TRAIT)) {
	// Mark.say(debug, "Adding personality trait for", subject, "namely", RoleFrames.getObject(t));
	// // personalities.add(t.getObject().getType());
	// // Mark.say(debug, "Personalities", t, RoleFrames.getObject(t));
	// personalities.add(RoleFrames.getObject(t).getType());
	// }
	// }
	// // Now look for classification that is a personality type
	// for (Entity t : subject.getObjectOf()) {
	// if (t.relationP(Markers.CLASSIFICATION_MARKER)) {
	// if (t.getSubject().isA(Markers.PERSONALITY_TRAIT)) {
	// Mark.say(debug, "Adding personality type via classification", t.getSubject().getType());
	// personalities.add(t.getSubject().getType());
	// }
	// }
	// }
	// for (String personality : personalities) {
	// // Mark.say("Personality", personality);
	// MentalModel personalityModel = getMentalModel().getLocalMentalModel(personality);
	// if (personalityModel != null) {
	// if (!personalityModels.contains(personalityModel)) {
	// personalityModels.add(personalityModel);
	// // Mark.say("Adding personality model", personalityModel.getName());
	// }
	// }
	// }
	// }
	// else {
	// // Mark.say("Entity", subject.asString(), "is not a person");
	// }
	// }
	// return personalityModels;
	// }

	public ArrayList<Entity> fetchParticipants(Entity element) {
		ArrayList<Entity> entities = new ArrayList<Entity>();
		Entity subject = element.getSubject();
		if (subject == null) {
			// Mark.err("Ooops, null subject in", element);
		}
		else if (subject.entityP() && !entities.contains(subject)) {
			entities.add(subject);
		}
		if (element.relationP()) {
			Entity o = RoleFrames.getObject(element);
			if (o != null) {
				// Mark.say("Adding", o.asString());
				if (o.entityP() && !entities.contains(o)) {
					entities.add(o);

				}
			}
		}
		// for (Thing entity : entities) {
		// Mark.say("Entity", entity.asString());
		// }
		if (reader != null) {
			entities.add(reader);
			// Mark.say("Adding reader to participants");
		}
		return entities;
	}

	private boolean isTarget(Entity target, Sequence inferences) {
		for (Entity t : inferences.getElements()) {
			if (t.relationP()) {
				if (target == t.getObject()) {
					// Mark.say("Backward chaining blocked!!!");
					return true;
				}
			}
		}
		return false;
	}

	// public void processWithoutDereference(Object object) {
	// Entity element = (Entity) object;
	// process(element, false);
	// explicitElements.addElement(element);
	// // Mark.betterSay("Quiescence without dereference!!!");
	// }

	public boolean storyStarter(Entity t) {
		if (t.relationP("start") && t.getSubject().entityP("you")) {
			Entity x = extractObjectRole(t);
			// Mark.say("X\n", t, "\n", x);
			if (x == null) {
				return false;
			}
			else if (x.entityP(Markers.STORY_MARKER)) {
				// Mark.say("Story sans title");
				return true;
			}
			else if (x.functionP(Markers.STORY_MARKER)) {
				// Mark.say("Story with title,", x.getSubject().getType());
				return true;
			}
			else if (x.functionP(Markers.CONCEPT_MARKER)) {
				// Mark.say("Concept with title,", x.getSubject().getType());
				return true;
			}
			else if (x.entityP(Markers.PRECEDENT_MARKER)) {
				Mark.say("Starting video precedent");
				return true;
			}
			// Doesn't work;
			else if (x.functionP(Markers.VIDEO_MARKER)) {
				Mark.say("Starting video");
				return true;
			}
			// Doesn't work;
			else if (x.entityP(Markers.VIDEO_MARKER)) {
				Mark.say("Starting unnamed video");
				return true;
			}
		}
		return false;
	}

	public static Entity extractObjectRole(Entity t) {
		Entity roles = t.getObject();

		if (roles != null && roles.sequenceP(Markers.ROLE_MARKER)) {
			for (Entity r : roles.getElements()) {
				if (r.functionP(Markers.OBJECT_MARKER)) {
					Entity o = r.getSubject();
					return o;
				}
			}
		}
		return null;
	}

	public void initializeVideoVariables(Entity t) {
		// Mark.betterSay("Initializing Video variables");
		if (t.relationP() && t.getSubject().entityP("you")) {
			if (t.getObject().entityP(Markers.PRECEDENT_MARKER)) {
				Mark.say("Starting video precedent");
				precedentInput = true;
			}
			else if (t.getObject().entityP(Markers.VIDEO_MARKER)) {
				Mark.say("Starting unnamed video");
				setPlayByPlayInput(true);
			}
		}
	}

	public boolean storyClusterer(Entity t) {
		if (t.relationP(Markers.CLUSTER_MARKER) && t.getSubject().entityP("you")) {
			return true;
		}
		return false;
	}

	public void processRuleInjection(Object o) {
		Entity t = (Entity) o;
		recordRule(t);
	}

	public void processConceptInjection(Object o) {
		Sequence s = (Sequence) o;
		s.addType(Punctuator.conditionName(s.getType()));
		addConcept(s);
	}

	public void testElementInjection(Object o) {
		Mark.say("Testing injection of", o);
	}

	private void processElementWithInertInjection(Entity element) {
		try {
			setInert(true);
			injectElementWithDereference(element);
		}
		finally {
			setInert(false);
		}
	}

	/**
	 * Not in "detective mode." That is, trims off elements after final scene starter, then reinserts them. Added for
	 * possible use by Jessica Noss in connection with telling story from a particular player's perspective.
	 */
	public void injectElementWithDereferenceAtSceneStart(Object o) {
		boolean debug = false;
		Entity t = (Entity) o;
		Entity injection = dereferenceEntities(t, getStory());
		Mark.say(debug, "Injection", injection);
		// Now make list of explicit elements after the start of the scene
		ArrayList<Entity> elementsToBeReinjected = new ArrayList<>();
		elementsToBeReinjected.add(injection);

		boolean keep = false;
		int index = getLastSceneIndex();
		Entity sceneMarker = null;
		if (index >= 0) {
			sceneMarker = getStory().getElements().get(index);
		}
		else {
			keep = true;
		}
		for (Entity e : getExplicitElements().getElements()) {
			if (keep) {
				Mark.say(debug, "Keeping", e);
				elementsToBeReinjected.add(e);
			}
			else if (e == sceneMarker) {
				Mark.say(debug, "Switching", e);
				keep = true;
			}
			else {
				Mark.say(debug, "Skipping", e);
			}
		}
		Mark.say(debug, "Count", elementsToBeReinjected.size());
		// Need to this so previously inserted elements can be reinserted.
		removeCurrentSceneMap();
		// Trim out all the elements after the start of the scene
		removeStartingAt(index + 1, getStory().getElements());
		elementsToBeReinjected.stream().forEachOrdered(e -> {
			Mark.say(debug, "Reinjectng", e);
			// injectElementWithoutDereference(e);
			processElement(e);
		});

	}

	private void removeStartingAt(int index, Vector<Entity> elements) {
		while (elements.size() > index) {
			elements.remove(elements.size() - 1);
		}

	}

	private int getLastSceneIndex() {
		Vector<Entity> elements = story.getElements();
		int result = -1;
		for (int i = 0; i < elements.size(); ++i) {
			if (elements.get(i).getBooleanProperty(Markers.SCENE)) {
				result = i;
			}
		}
		return result;
	}

	/**
	 * Major challenge is that entity dereference no longer handled by START! Accordingly has to be done here.
	 */
	public void injectElementWithDereference(Object o) {
		processElementInjectionAux(o, true);
	}

	public void injectElementWithoutDereference(Object o) {
		processElementInjectionAux(o, false);
	}

	public void processElementInjectionAux(Object o, boolean dereference) {

		boolean debug = false;
		boolean success = false;
		boolean handle = this.isOpen();

		if (!handle) {
			openStory();
		}
		try {
			Entity t = (Entity) o;
			if (t.sequenceP()) {
				Mark.say(debug, "Sequence injected into story processor has", t.getElements().size(), "elements");
				Mark.say(debug, "Presumed to be a complete story");
				for (Entity e : t.getElements()) {
					// Next two lines a hack for persuader
					Entity injection = e;
					if (dereference) {
						injection = dereferenceEntities(e, story);
					}
					if (addElement(injection, story, dereference)) {
						for (String x : getAlreadyPredicted()) {
							Mark.say(debug, "Predicted", x);
						}

						for (Entity s : story.getElements()) {
							Mark.say(debug, "Story", s);
						}
						Mark.say(debug, "Injected at A in", getName(), isOpen(), injection);
						success = true;
						transmitStory();
					}
				}
				if (success) {
				}
				else {
					return;
				}
			}
			else {
				Entity injection = t;
				if (dereference) {
					injection = dereferenceEntities(t, story);
				}
				if (addElement(injection, story, dereference)) {
					Mark.say(debug, "Injected at B in\n", getName(), isOpen(), "\n", injection);
					success = true;
				}
				else {
					return;
				}
				Mark.say(debug, "Added", injection);

				// Removed, as may cause problems when dereference is false 2016.01.17
				// processElement(injection, dereference);
			}

			transmitStory();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {

			// Screwed up somehow. Look at first perspective. Story destroyed.

			if (success) {

				// A very special case, incredible hack. Reviews whole story after insertion, looking for elements newly
				// explainable; don't get here if no insertion.

				Vector<Entity> copy = (Vector<Entity>) (story.getElements().clone());

				for (Entity element : copy) {
					ArrayList<MentalModel> personalityModels = getPersonalityModels(element);
					Mark.say(debug, "Not already predicted", element);
					getRuleEngine().process(element, this);
				}

				if (Switch.level4ConceptPatterns.isSelected()) {
					Mark.say(debug, "Working on injection concept patterns");
					// Mark.say("Transmiting", concepts.getElements().size(), "concepts to completion detector");
					BetterSignal signal = new BetterSignal(concepts, story, getInferences());
					getConceptExpert().process(signal);
				}

				transmitStory();
			}
		}
		if (!handle) {
			closeStory();
		}
	}

	private Entity dereferenceEntities(Entity t, Sequence story) {
		boolean debug = false;

		// First, get the entities.

		List<Entity> entities = new ArrayList<>();
		extractEntities(t, entities);
		Mark.say(debug, "\n>>>  New element is", t);
		Mark.say(debug, "Entities in new element are", entities);
		// Now, work through story, looking for entities
		LList<PairOfEntities> linkedList = new LList<>();
		// Look for each entity
		for (Entity e : entities) {
			boolean found = false;
			// Look in each story element, starting with most recent
			for (int i = story.getElements().size() - 1; i >= 0; --i) {
				if (found) {
					break;
				}
				Mark.say(debug, "Checking for correspondence in", story.getElements().get(i));
				List<Entity> elementEntities = new ArrayList<>();
				extractEntities(story.getElements().get(i), elementEntities);
				Mark.say(debug, "Entities in story element are", elementEntities);
				// Now check for correspondence
				for (Entity elementEntity : elementEntities) {
					// Quick, dirty check
					if (e.getType().equals(elementEntity.getType())) {
						// Got it, store it
						Mark.say(debug, e, "seems to correspond to", elementEntity);
						linkedList = linkedList.cons(new PairOfEntities(e, elementEntity));
						found = true;
						break;
					}
				}
			}
		}
		Mark.say(debug, "\n>>>  LList", linkedList);
		Mark.say(debug, "Original ", t);
		Entity result = Matcher.instantiate(t, linkedList);
		Mark.say(debug, "Undereferenced", t);
		Mark.say(debug, "Dereferenced ", result);

		return result;
	}

	private void extractEntities(Entity t, List entities) {
		if (t == null) {
			Mark.err("Null entity in extractEntities!");
			return;
		}
		else if (t.entityP()) {
			Object marker = t.getProperty(Markers.PROPER);
			if (marker != null) {
				entities.add(t);
			}
		}
		else if (t.functionP()) {
			extractEntities(t.getSubject(), entities);
		}
		else if (t.relationP()) {
			extractEntities(t.getSubject(), entities);
			extractEntities(t.getObject(), entities);
		}
		else if (t.sequenceP()) {
			for (Entity x : t.getElements()) {
				extractEntities(x, entities);
			}
		}
		else {
			Mark.err("Unrecognized entity type in StoryProcessor.extractEntities", t);
		}
	}

	// public void testMe(Object object) {
	// Mark.say("test me received", object);
	// }
	//
	// public void processFromShortCut(Object object) {
	// Mark.say("Short cut passing to processElement", object);
	// processElement(object, true);
	// }

	public void processElement(Object object) {
		// Mark.say("Story processor processing", object);
		processElement(object, true);
	}

	public void processElementWithoutDereference(Object object) {
		processElement(object, false);
	}

	public void processElement(Object object, boolean dereference) {
		if (isAsleep() && object instanceof Entity) {
			//Mark.say("OOOPs, asleep, can't process", asleep, object);
		}

		if (isAsleep() || !(object instanceof Entity)) {
			// dxh
			//Mark.say("Not processing", object, isAsleep(), !(object instanceof Entity));
			return;
		}

		Entity element = (Entity) object;

		int storyLengthAtQuiescence = story.getElements().size();

		// Mark.say("Input to process element", isOpen(), element);

		if (storyStarter(element)) {
			startStory(element);
			return;
		}
		// An extreme hack
		else if (isCommand(element)) {
			Mark.say("\n>>>  Command sent", element);
			Connections.getPorts(this).transmit(COMMAND_PORT, new BetterSignal(element, this));
			Mark.say("Command back", element);
		}
		else if (storyClusterer(element)) {
			Connections.getPorts(this).transmit(CLUSTER_STORY_PORT, story);
		}
		// Clueless about following two else ifs
		else if (element.isAPrimed(Markers.CAUSE_MARKER) && element.getObject().hasFeature(Markers.POSSIBLY)) {
			// Mark.say("R1");
			recordRule(element.clone());
			element.getObject().removeFeature(Markers.POSSIBLY);
			return;
		}
		else if (element.isAPrimed(Markers.CAUSE_MARKER) && element.getObject().hasFeature(Markers.PRESUMPTION_RULE)) {
			// Mark.say("R2");
			recordRule(element.clone());
			element.getObject().removeFeature(Markers.PRESUMPTION_RULE);
			return;
		}
		else if (!isOpen() && !element.isAPrimed(Markers.CAUSE_MARKER)) {
			return;
		}
		else if (!isOpen() && element.isAPrimed(Markers.CAUSE_MARKER)) {
			// Mark.say("R3");
			recordRule(element);
			return;
		}
		// This is where we handle "I am eastern" and other phrases that add common sense and concepts to the reader
		else if (isMentalModelAssertion(element)) {
			assertPersonalityType(element);
		}
		else if (isMentalModelConstructionCommand(element)) {
			createMentalModelUsingConstructionCommand(element);
		}
		else if (isBeliefElement(element)) {
			// Mark.say("Belief:", element);
			sendElementToMentalModel(element);
			return;
		}
		else if (isThinksElement(element)) {
			processThinksElement(element);
		}
		else if (isEnterElement(element)) {
			processEntry(element);
		}
		else if (isExitElement(element)) {
			processExit(element);
		}

		try {
			// Mark.say("Story", story);
			addElement(element, story, dereference);
		}
		catch (Exception e1) {
			Mark.err("Blew out while trying to add", element, "to story");
			story.stream().forEachOrdered(e -> Mark.err("Element", e));
			e1.printStackTrace();
			return;
		}

		// Special case, make no inferences or further processing of element that is not really there, just there so
		// strike out can be shown.
		if (element.hasProperty(Markers.PROPERTY_TYPE, Markers.STRIKE)) {
			Mark.say("not really there");
			return;
		}

		// MentalModels addition looking for typical behavior
		if (Switch.levelLookForMentalModelEvidence.isSelected()) {
			if (getMentalModel() != null) {
				Map<String, MentalModel> models = getMentalModel().getLocalMentalModels();
				for (MentalModel model : models.values()) {

					// Mark.say("Got", model.getExamples().size(), "from", model.getName());
					for (Entity x : model.getExamples()) {
						// Mark.say("\n\nTrying to match from personality", model.getName(), "\n", element, "\n", x);
						LList bindings = StandardMatcher.getBasicMatcher().match(x, element);

						// if (bindings != null) {
						// Mark.say("Looks like score:", model.getName(), element);
						// }
					}
				}
			}
		}

		// End of MentalModels addition

		if (isPlayByPlayInput()) {
			try {
				String comment = Generator.getGenerator().generate(element);
				Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Results");
				Connections.getPorts(this).transmit(RECORD_PORT, comment);
			}
			catch (Exception e) {
				Mark.say("Unable to say", element.asString());
			}
			// Mark.say("Speaking", t.asString());

			// Talker.getTalker().speak(t);
		}
		if (!story.isA(Markers.CONCEPT_MARKER)) {
			transmitStory();
			Vector<Entity> currentStoryElements = story.getElements();
			int delta = currentStoryElements.size() - storyLengthAtQuiescence;
			Sequence increment = new Sequence();
			if (delta == 1) {
				increment.addElement(element);
			}
			else if (delta > 1) {
				for (int i = currentStoryElements.size() - delta; i < currentStoryElements.size(); ++i) {
					Entity addition = currentStoryElements.get(i);
					if (Predicates.isCause(addition)) {
						increment.addElement(currentStoryElements.get(i));
					}
				}

			}
			transmitDelta(increment);
		}
		// Now, ship this to all open mental models, but not if it would cause infinite recursion in mental model
		// construction!

		if (!element.isA(Markers.ENTER) && !element.isA(Markers.EXIT)) {
			if (!isMentalModelConstructionCommand(element) && !isThinksElement(element)) {

				for (MentalModel model : getMentalModel().getLocalMentalModels().values()) {
					if (model.getStoryProcessor().isOpen()) {
						// Mark.say(model.getName(), "notes", element);
						sendElementToMentalModel(model, element);
					}
				}
			}
		}
	}

	private boolean isThinksElement(Entity element) {
		if (element.isA(Markers.LIKE_WORD) && element.getSubject().isA(Markers.THINK_WORD)) {
			if (RoleFrames.getObject(element).isA(Markers.PERSONALITY_TRAIT)) {
				return true;
			}
		}
		return false;
	}

	private void processThinksElement(Entity element) {
		boolean debug = false;
		if (element.isA(Markers.LIKE_WORD) && element.getSubject().isA(Markers.THINK_WORD)) {
			Entity trait = RoleFrames.getObject(element);
			if (trait.isA(Markers.PERSONALITY_TRAIT)) {
				Entity subject = element.getSubject().getSubject();
				MentalModel currentModel = getMentalModel().getLocalMentalModel(subject.getName());
				if (currentModel == null) {
					currentModel = createMentalModel(subject);
				}
				MentalModel augmentingModel = getMentalModel().getLocalMentalModel(trait.getType());
				if (augmentingModel != null && currentModel != null) {
					Mark.say(debug, "Noting", subject, "thinks like", trait.getType());
					MentalModel.transferAllKnowledge(augmentingModel, currentModel);
				}
				else if (mentalModel == null) {
					Mark.err("I have no mental model for", trait);
				}
			}
		}
	}

	public void processExit(Entity element) {
		closeMentalModel(element.getSubject());
	}

	public void closeMentalModel(Entity element) {
		if (element.entityP()) {
			MentalModel model = getMentalModel().getLocalMentalModel(element.getName());
			if (model == null) {
				// Do nothing
			}
			else {
				Mark.say("Closing", element.getName());
				model.getStoryProcessor().closeStory();
			}
		}
	}

	public void processEntry(Entity element) {
		openMentalModel(element.getSubject());
	}

	public void openMentalModel(Entity element) {
		if (element.entityP()) {
			Mark.say("Opening", element.getName());
			MentalModel model = getMentalModel().getLocalMentalModel(element.getName());
			if (model == null) {
				model = createMentalModel(element);
			}
			else {
				model.getStoryProcessor().openStory();
			}
		}
	}

	private boolean isExitElement(Entity element) {
		if (element.relationP(Markers.EXIT) && element.getObject().sequenceP() && element.getObject().getElements().isEmpty()) {
			return true;
		}
		return false;
	}

	private boolean isEnterElement(Entity element) {
		if (element.relationP(Markers.ENTER) && element.getObject().sequenceP() && element.getObject().getElements().isEmpty()) {
			return true;
		}
		return false;
	}

	private boolean isMentalModelAssertion(Entity element) {
		if (element.relationP(Markers.PROPERTY_TYPE)) {
			Entity object = RoleFrames.getObject(element);
			if (object.isA(Markers.PERSONALITY_TRAIT)) {
				// Mark.say("Personality type noted", object);
				return true;
			}
		}
		return false;
	}

	/**
	 * If subject is I, then personality type determines the way the reader acts. Otherwise, personality type determines
	 * the way the subject acts.
	 * 
	 * @param element
	 */
	private void assertPersonalityType(Entity element) {
		boolean debug = false;
		if (element.relationP(Markers.PROPERTY_TYPE)) {
			Entity object = RoleFrames.getObject(element);
			if (object.isA(Markers.PERSONALITY_TRAIT)) {
				Entity subject = element.getSubject();
				if (subject.isA(Markers.i)) {
					MentalModel mentalModel = getMentalModel().getLocalMentalModel(object.getType());
					if (mentalModel != null) {
						Mark.say(debug, "Noting reader thinks like", object.getType());
						MentalModel.transferAllKnowledge(mentalModel, getMentalModel());
					}
					else {
						Mark.err("No mental model found for", object.getType());
					}
				}
				else {
					Mark.say(debug, "Noting", subject.getName(), "acts like", object.getType());
					addPersonalityType(subject.getName(), object.getType());
				}
			}
		}
	}

	private void sendElementToMentalModel(Entity element) {
		if (element.relationP(Markers.THINK_WORD)) {
			Entity subject = element.getSubject();
			if (subject.entityP()) {
				MentalModel mentalModel = getMentalModel().getLocalMentalModels().get(subject.getName());
				Entity object = RoleFrames.getObject(element);
				if (object != null) {
					sendElementToMentalModel(mentalModel, object);
				}
			}
		}
	}

	private void sendElementToMentalModel(MentalModel mentalModel, Entity object) {
		// Mark.say("Sending", object, "to", mentalModel.getName());
		if (mentalModel.getStoryProcessor().isOpen()) {
			mentalModel.getStoryProcessor().processElement(object);
		}
		else {
			mentalModel.getStoryProcessor().openStory();
			mentalModel.getStoryProcessor().processElement(object);
			mentalModel.getStoryProcessor().closeStory();
		}
	}

	private boolean isBeliefElement(Entity element) {
		if (element.relationP(Markers.THINK_WORD)) {
			Entity subject = element.getSubject();
			if (subject.entityP()) {
				return getMentalModel().getLocalMentalModels().get(subject.getName()) != null;
			}
		}
		return false;
	}

	private boolean isMentalModelConstructionCommand(Entity element) {
		if (element.relationP("construct")) {
			if (element.getSubject().isA("you")) {
				if (RoleFrames.getObject(element).isA("model")) {
					Entity target = RoleFrames.getRole("for", element);
					if (target != null) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void createMentalModelUsingConstructionCommand(Entity element) {
		if (element.relationP("construct")) {
			if (element.getSubject().isA("you")) {
				if (RoleFrames.getObject(element).isA("model")) {
					Entity target = RoleFrames.getRole("for", element);
					if (target != null) {
						MentalModel newModel = createMentalModel(target);
						newModel.getStoryProcessor().closeStory();
					}
				}
			}
		}

	}

	private MentalModel createMentalModel(Entity target) {
		MentalModel model = new MentalModel(target);
		MentalModel.transferAllKnowledge(getMentalModel(), model);
		getMentalModel().addLocalMentalModel(target.getName(), model);
		model.startStory();
		return model;
	}

	private void transmitStory() {
		// Mark.say("Transmitting story");
		Connections.getPorts(this).transmit(extractStory());
		// Mark.say("Transmitted!");
	}

	private Object copyIt(Entity t) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean milestoneP(Entity element) {
		// if (element.functionP(Markers.MILESTONE) && element.getSubject().isA("then")) {
		if (element.hasProperty(Markers.SCENE)) {
			// Mark.say("Milestone!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return true;
		}
		return false;
	}

	public void describe(Object x) {
		if (isAsleep()) {
			return;
		}
		if (!(x instanceof String)) {
			return;
		}
		String s = (String) x;
		Mark.say(true, "Starting description of concept", s);
		story = new Sequence(STORY);
		story.addType(Markers.CONCEPT_MARKER);
		// inferences = new Sequence();
		openStory();
		story.addType(s);
		closeStory();
	}

	public void recordRule(Object input) {

		if (isAsleep()) {
			return;
		}

		if (!(input instanceof Relation)) {
			return;
		}
		Relation rule = (Relation) input;

		// Mark.say("Recording rule", rule);
		
		// Make no rule while recording concept or reading a story, unless proximity rule
		if (isOpen() && !rule.getObject().hasFeature(Markers.POSSIBLY)) {
			if (!newRuleComingIn) {
				return;
			}
		}

		getRuleMemory().recordRule(rule);

		Connections.getPorts(this).transmit(RULE_PORT, getRuleMemory().getRuleSequence());

		Connections.getPorts(this).transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.INFERENCE_RULE_COUNT,
		        getRuleMemory().getRuleSequence().getElements().size()));

	}

	public static void addAntecedentsToRuleMap(Entity rule, HashMap<String, ArrayList<Entity>> ruleMap) {
		for (Entity antecedent : rule.getSubject().getElements()) {
			String type = getRuleKey(antecedent);
			// Need to deal with "occur" here because some rules will have occur in
			// antecedents
			// mpfay 3/19/2014, modified this code so it no longer causes rules to disappear
			if (antecedent.functionP(Markers.APPEAR_MARKER) && antecedent.getSubject().getType().equals(Markers.ACTION_WORD)) {
				String action_type = antecedent.getSubject().getType();
				if (!ruleMap.containsKey(action_type)) {
					ruleMap.put(action_type, new ArrayList<Entity>());
				}
				ruleMap.get(action_type).add(rule);
			}
			// Mark.say("Storing rule under", type);
			ArrayList<Entity> list = ruleMap.get(type);
			if (list == null) {
				list = new ArrayList<Entity>();
				ruleMap.put(type, list);
			}
			// Mark.say("Storing rule under", type, "namely", rule.asString());
			list.add(rule);
		}
	}

	public static void addConsequentToRuleMap(Entity rule, HashMap<String, ArrayList<Entity>> ruleMap) {
		Entity consequent = rule.getObject();
		String type = consequent.getType();
		// mpfay 3/19/2014 this makes sure action rules trigger properly
		if (consequent.functionP(Markers.APPEAR_MARKER) && consequent.getSubject().getType().equals(Markers.ACTION_WORD)) {
			String action_type = consequent.getSubject().getType();
			if (!ruleMap.containsKey(action_type)) ruleMap.put(action_type, new ArrayList<Entity>());
			ruleMap.get(action_type).add(rule);
		}
		// Mark.say("Adding explanation rule for " + type);
		ArrayList<Entity> list = ruleMap.get(type);
		if (list == null) {
			list = new ArrayList<Entity>();
			ruleMap.put(type, list);
		}
		list.add(rule);
	}

	public void addToRuleList(Entity rule) {
		// rules.addElement(rule);
		Mark.say("Transmitting", getRuleMemory().getRuleSequence());
		Connections.getPorts(this).transmit(RULE_PORT, getRuleMemory().getRuleSequence());
		Connections.getPorts(this).transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.INFERENCE_RULE_COUNT,
		        getRuleMemory().getRuleSequence().getElements().size()));
	}

	// private int countRules() {
	// return getExplanationRules().size() + getCensorRules().size();
	// }

	private Relation reviseRule(Entity cause, Relation rule) {
		if (true) {
			return rule;
		}
		Entity object = rule.getObject();
		if (cause.isA("action") && !object.isA("action")) {
			Function transition = new Function("action", object);
			transition.addType("transition");
			transition.addType("appear");
			rule.setObject(transition);
			// Mark.say("Revising rule, yielding" + rule.asString());
		}
		return rule;
	}

	public void startStory() {
		Function object = new Function(Markers.STORY_MARKER, new Entity("Inner Story"));
		Entity starter = ISpeak.makeRoleFrame(new Entity(Markers.YOU), "start", object);
		startStory(starter);
		setAwake(true);
	}

	public void startStory(Entity t) {


		boolean debug = false;

		Mark.say(debug, "Starting story");

		// Put start in story mode
		Connections.getPorts(this).transmit(Start.MODE, Start.STORY_MODE);

		Connections.getPorts(this).transmit(START_STORY_INFO_PORT, t);
		resetStoryVariables();
		Entity objectRole = extractObjectRole(t);
		if (objectRole == null) {
			return;
		}
		if (objectRole.functionP(Markers.CONCEPT_MARKER)) {
			story.addType(Markers.CONCEPT_MARKER);
			story.addType(objectRole.getSubject().getType());
		}
		if (objectRole.functionP(Markers.STORY_MARKER)) {
			// Mark.say(debug, "Starting story", objectRole);
			story.addType(Markers.STORY_MARKER);
			String name = objectRole.getSubject().getType();
			story.addType(name);
			// explicitElements.addType(name);
			// Mark.say("---------------------------Starting story", getName());
			Connections.getPorts(this).transmit(STORY_NAME, name);
			// addElement(t, true, story);

		}
		if (objectRole.entityP(Markers.STORY_MARKER)) {
			story.addType(Markers.STORY_MARKER);
			// addElement(t, true, story);


		}
		// initializeVideoVariables(t);
		// transmitCompleteKnowledgeContent();


		Mark.say(debug, "Starting story", story.getType(), isOpen());

	}

	public void resetStoryVariables() {
		// if (!story.getElements().isEmpty()) {
		// stopStory();
		// }
		resetStoryVariablesWithoutTransmission();

		// Connections.getPorts(this).transmit(PREDICTION_RULES, predictionRuleMap);
		Connections.getPorts(this).transmit(STARTING, STARTING);
		Connections.getPorts(this).transmit(RESET_CONCEPTS_PORT, Markers.RESET);
		Connections.getPorts(this).transmit(INFERENCES, getInferredElements());
		transmitStory();

	}

	private void resetStoryVariablesWithoutTransmission() {

		// clearStoryCache();
		// clearSceneCache();
		// clearEmbeddedCache();
		clearAlreadyPredicted();
		story = new Sequence(STORY);
		// inferences = new Sequence();
		// explicitElements = new Sequence();
		sizeAtPreviousQuiescence = 0;
		// milestoneEncountered = false;

		getSceneMapList().clear();
		getGlobalCache().clear();
		getMentalModelCache().clear();
		getWantCache().clear();

		openStory();

		NewTimer.storyTimer.reset();
	}

	public void clearAllMemories() {
		Mark.say("Clearing all memories in", getName());

		clearRules();
		clearConcepts();
		clearLocalMentalModels();
		// clearGlobalMentalModels();
		clearPersonalityTypes();
		roles = new ArrayList<Entity>();
		story = new Sequence();
		// explicitElements = new Sequence();
		// inferences = new Sequence();
		concepts = new Sequence();

		closeStory();

		getRuleMemory().clear();

		getAlreadyPredicted().clear();
		getSceneMapList().clear();
		getGlobalCache().clear();
		getMentalModelCache().clear();
		getWantCache().clear();

		Connections.getPorts(this).transmit(story);
		Connections.getPorts(this).transmit(RULE_PORT, getRuleMemory().getRuleSequence());
		Connections.getPorts(this).transmit(INFERENCES, getInferredElements());
		Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.RESET);
		Connections.getPorts(this).transmit(STORY_NAME, "");
		Connections.getPorts(this).transmit(PERSONALITY_TRAIT_PORT, new BetterSignal(StoryProcessor.CLEAR));
	}

	/**
	 * Makes it possible to add elements to the story without resetting the story variables. Used by mental model
	 * apparatus.
	 */
	public void openStory() {
		storyInProgress = true;
	}

	/**
	 * Makes it possible to shut down element additions to the story without reviewing the story for concept pattersn
	 * and such. Used by mental model apparatus.
	 */
	public void closeStory() {
		// Mark.say("Closing story....................");
		storyInProgress = false;
	}

	public boolean isOpen() {
		return storyInProgress;
	}

	public void stopStory() {

		// Mark.say("Stopping story", story.getType());

		closeStory();

		if (story != null) {
			// At this point, want to go back over elements of the story to see
			// if any explanations can be made or rules with negated antecedents
			// can fire. Keep going as long as story is getting longer
			// But, don't do it at all if it is a concept

			// Mark.say("Entered stopping story named", getName());

			if (!story.isA(Markers.CONCEPT_MARKER)) {




				boolean debugOutput = false;
				// Mark.say("---------------------------Stopping story",
				// getName());
				if (debugOutput) {
					Mark.say("Story has", story.getElements().size(), "elements");
					for (Entity t : story.getElements()) {
						// Mark.say(t.asString());
					}
					Mark.say("Story has", getCommonsenseRules().getElements().size(), "commonsense rules");
					for (Entity t : getCommonsenseRules().getElements()) {
						// Mark.say(t.asString());
					}
					Mark.say("Story has", concepts.getElements().size(), "concept patterns");
					for (Entity t : concepts.getElements()) {
						// Mark.say(t.asString());
					}

					int killRules = 0;

					for (Entity e : this.getRuleMemory().getRuleList()) {
						if (e.isA("prediction") || e.isA("explanation")) {
							if (containsKill(e)) {
								Mark.say("Kill rule:", Generator.getGenerator().generate(e));
								++killRules;
							}
						}
					}

					Mark.say("Story has", killRules, "kill rules");
					// Mark.say("Prediction rules", this.predictionRuleMap.size());
					// Mark.say("Explanation rules", this.explanationRuleMap.size());
				}

				transmitStory();
				Connections.getPorts(this).transmit(COMPLETE_STORY_EVENTS_PORT, story);

				Connections.getPorts(this).transmit(FINAL_INPUTS, getExplicitElements());
				Connections.getPorts(this).transmit(FINAL_INFERENCES, getInstantiatedRules());
				// Transmits entire concept collection and entire story to concept expert
				// Mark.say("Transmitting", signal, "to completion detector");
				if (Switch.level4ConceptPatterns.isSelected()) {
					// Mark.say("Transmiting", concepts.getElements().size(), "concepts to completion detector");
					BetterSignal signal = new BetterSignal(concepts, story, getInferences());

					getConceptExpert().process(signal);
					// Connections.getPorts(this).transmit(TO_COMPLETION_DETECTOR, signal);
				}
				if (isPlayByPlayInput()) {
					// Connections.getPorts(this).transmit(GAP_FILLER_PORT, new
					// BetterSignal(GapFiller.FILL_GAP, story));
				}
				else if (precedentInput) {
					// Connections.getPorts(this).transmit(GAP_FILLER_PORT, new
					// BetterSignal(GapFiller.ADD_PATTERN, story));
				}
				// Temporary
				else {
					// Connections.getPorts(this).transmit(GAP_FILLER_PORT, new
					// BetterSignal(GapFiller.ADD_PATTERN, story));
				}

				// This is where all story information is broadcast. The first element is the story, with explicit
				// elements, inferred elements, and instantiated inferences. The second consists of explicit elements
				// only. Third, instantiated commonsense rules. Fourth, instantiated concept patterns.

				// for (Entity e : story.getElements()) {
				// Mark.say("Element:", e.asString());
				// }


				transmitCompleteStoryAnalysis();
			}

			else {
				// Mark.say("It is a concept!");

				// Because it is a concept, put START back in sentence mode
				// Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);

				story.addType(Punctuator.conditionName(story.getType()));
				addConcept(story);
				// Better clear this, otherwise it hangs around with stuff in it until a story is read. Screwed up
				// mental model processing, because a mental model can have concepts with no story.
				// resetStoryVariablesWithoutTransmission();

				// Later: determined that this screws up story porocessing; worked around this way, but not clear if
				// other stuff should be reset too. Ignoring puzzle for now
				sizeAtPreviousQuiescence = 0;
				story = new Sequence(STORY);

				// Only stop dereferencing if it is in concept mode
				Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			}
			setPlayByPlayInput(false);
			precedentInput = false;
			// Mark.say("Stopping", this.getName(), "story completed");
		}

		// Mark.say( "Story stopped");

		Connections.getPorts(this)
		        .transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.STORY_TIMER, NewTimer.storyTimer.time()));

	}

	private void transmitIncrement(Entity element) {
		Sequence increment = new Sequence();
		increment.addElement(element);
		// Legacy, moved to transmitDelta
		// Connections.getPorts(this).transmit(INCREMENT_PORT, increment);
		// Better
		Connections.getPorts(this).transmit(INCREMENT_PORT_COMPLETE, new BetterSignal(element, story, this));
	}

	private void transmitDelta(Sequence increment) {
		// Legacy
		Connections.getPorts(this).transmit(INCREMENT_PORT, increment);
	}

	// private void transmitIncrement() {
	// boolean debug = false;
	// Sequence increment = new Sequence();
	// if (story != null && story.isA(Markers.STORY_MARKER)) {
	// sizeAtPreviousQuiescence = story.getElements().size();
	// if (story.getElements().size() >= sizeAtPreviousQuiescence) {
	// List<Entity> newStuff = story.getElements().subList(sizeAtPreviousQuiescence, story.getElements().size());
	// for (Entity t : newStuff) {
	// increment.addElement(t);
	// }
	// Mark.say(debug, "Increment:");
	// increment.getElements().stream().forEachOrdered(f -> Mark.say(debug, f));
	// Mark.say(debug, "Complete:");
	// story.getElements().stream().forEachOrdered(f -> Mark.say(debug, f));
	// Connections.getPorts(this).transmit(QUIESCENCE_PORT, increment);
	// Connections.getPorts(this).transmit(QUIESCENCE_PORT_COMPLETE, story);
	// }
	// else {
	// Mark.err("Problem with transmiting increment");
	// }
	// }
	//
	// }

	private void transmitCompleteStoryAnalysis() {
		if (getExplicitElements().getElements().size() > 10) {
			Mark.say(debug, "Story statistics for", story.getType());
			Mark.say(debug, "Explicit element count", getExplicitElements().getElements().size());

			int deductions = 0;
			int explanations = 0;
			int proximity = 0;
			int abductions = 0;
			int presumptions = 0;
			int enablement = 0;
			int censor = 0;
						
			for (Entity e : getInferences().getElements()) {
				if (e.isA(Markers.PREDICTION_RULE)) {
					++deductions;
				}
				else if (e.isA(Markers.EXPLANATION_RULE)) {
					++explanations;
				}
				else if (e.isA(Markers.ABDUCTION_RULE)) {
					++abductions;
				}
				else if (e.isA(Markers.PRESUMPTION_RULE)) {
					++presumptions;
				}
				else if (e.isA(Markers.PROXIMITY_RULE)) {
					++proximity;
				}
				else if (e.isA(Markers.ENABLER_RULE)) {
					++enablement;
				}
				else if (e.isA(Markers.CENSOR_RULE)) {
					++censor;
				}
			}	

			if (debug && !story.isA(Markers.CONCEPT_MARKER)) {
				Mark.say("   ", deductions, "deductions");
				Mark.say("   ", explanations, "explanations");
				Mark.say("   ", proximity, "proximities");
				Mark.say("   ", abductions, "abductions");
				Mark.say("   ", abductions, "presumptions");
				Mark.say("   ", enablement, "enablements");
				Mark.say("   ", censor, "censors");
			}
		}
		StoryAnalysis signal = new StoryAnalysis(story, getExplicitElements(), getInferredElements(), getInstantiatedConceptPatterns(),
		        getConceptAnalysis());
		// describeConceptAnalysis(getConceptAnalysis());
		Mark.say(debug, "Transmitting complete story analysis, with", story.getElements().size(), "elements from", this
		        .getName(), "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Connections.getPorts(this).transmit(COMPLETE_STORY_ANALYSIS_PORT, signal);
		Mark.say(debug, "Transmitting story processor to itself");
		Connections.getPorts(this).transmit(STORY_PROCESSOR_PORT, new BetterSignal(STORY_PROCESSOR_PORT, this));
		Mark.say(debug, "Transmitting story snapshot");
		Connections.getPorts(this).transmit(STORY_PROCESSOR_SNAPSHOT, this);
		Mark.say(debug, "Transmitting concept net justification string");
		Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "ConceptNet Knowledge");
		Connections.getPorts(this).transmit(COMPLETE_CONCEPTNET_JUSTIFICATION_STRING_PORT, getConceptNetJustificationString(story));

	}
	
	/**
	 * @param e
	 * @return
	 */
	private boolean containsKill(Entity e) {
		if (e.isA("kill")) {
			return true;
		}
		else if (e.entityP()) {
			return false;
		}
		else if (e.functionP()) {
			return containsKill(e.getSubject());
		}
		else if (e.relationP()) {
			return containsKill(e.getSubject()) || containsKill(e.getObject());
		}
		else if (e.sequenceP()) {
			for (Entity x : e.getElements()) {
				if (containsKill(x)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String getConceptNetJustificationString(Sequence story) {
        String text = "";
        for (Entity entity : story.getElements()) {
            if (entity.hasProperty(Markers.CONCEPTNET_JUSTIFICATION)) {
                @SuppressWarnings("unchecked")
                List<ConceptNetJustification> justifications = (List<ConceptNetJustification>) entity.getProperty(Markers.CONCEPTNET_JUSTIFICATION);
                String justificationStr = "<ul>" + justifications.stream()
                    .map(justification -> "<li>" + justification.getJustification())
                    .collect(Collectors.joining())
                    + "</ul>";
                String entEnglish = entity.toEnglish().replaceAll("\\.", "");
                text += "Justification for "+entEnglish+":\n"+justificationStr+"\n\n";
            }
        }
        return text;
	}

	private void broadcastStatus() {
		// Mark.say(concepts.getElements().size(), "concepts");
		// Mark.say(story.getElements().size(), "story elements");
		// Mark.say(getInferences().getElements().size(), "inferences");

		BetterSignal signal = new BetterSignal(concepts, story, getInferences());

		getConceptExpert().process(signal);
		// Connections.getPorts(this).transmit(TO_COMPLETION_DETECTOR, signal);

		Connections.getPorts(this).transmit(BROADCAST_SNAPSHOT, this);

	}

	// private void transmitCompleteKnowledgeContent() {
	// // Not yet!!!
	// // Mark.say("Transmitting complete knowledge content from", this.getName(),
	// // "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	// // Connections.getPorts(this).transmit(COMPLETE_KNOWLEDGE_CONTENT_PORT, getKnowledgeContent());
	// }

	public void receiveCompleteKnowledgeContent(Object object) {
		if (object instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) object;
			setKnowledgeContent(signal);
		}
	}

	public BetterSignal getKnowledgeContent() {
		// return new BetterSignal(conceptMap, onsetRuleMap, completionRuleMap);
		return new BetterSignal(conceptMap);
	}

	public void setKnowledgeContent(BetterSignal signal) {
		conceptMap = signal.get(0, HashMap.class);
		// predictionRuleMap = signal.get(1, HashMap.class);
		// explanationRuleMap = signal.get(2, HashMap.class);
		// censorRuleMap = signal.get(3, HashMap.class);
		// onsetRuleMap = signal.get(4, HashMap.class);
		// completionRuleMap = signal.get(5, HashMap.class);
		Mark.say("Set knowledge content in", getName());
	}

	private void clearGlobalMentalModels() {
		MentalModel.clearMentalModels();
	}

	private void clearLocalMentalModels() {
		MentalModel myMentalModel = getMentalModel();
		if (myMentalModel != null) {
			myMentalModel.clearLocalMentalModels();
		}
	}

	public void clearRules() {
		// predictionRuleMap = new HashMap<String, ArrayList<Entity>>();
		// explanationRuleMap = new HashMap<String, ArrayList<Entity>>();
		// censorRuleMap = new HashMap<String, ArrayList<Entity>>();
		// onsetRuleMap = new HashMap<String, ArrayList<Entity>>();
		// completionRuleMap = new HashMap<String, ArrayList<Entity>>();
		// rules = new Sequence();
	}

	public void clearConcepts() {
		concepts = new Sequence(Markers.CONCEPT_MARKER);
		conceptMap = new HashMap<String, ArrayList<Sequence>>();
		// conceptOnsets = new Sequence();
	}

	private void addConcept(Sequence completeStory) {
		String conceptName = completeStory.getType();
		if (conceptMap.containsKey(conceptName)) {
			// Do Check against what is known
			ArrayList<Sequence> conceptArray = conceptMap.get(conceptName);
			for (Sequence s : conceptArray) {
				if (s.isDeepEqual(completeStory)) {
					Mark.say("Duplicate concept detected: " + conceptName);
					return;
				}
			}
			// Seems new add it
			conceptArray.add(completeStory);
		}
		else {
			// No such name, add it and continue
			ArrayList<Sequence> conceptArray = new ArrayList<Sequence>();
			conceptArray.add(completeStory);
			conceptMap.put(conceptName, conceptArray);
		}

		Relation onsetRule = extractConceptOnsetRule(completeStory);
		// conceptOnsets.addElement(onsetRule);
		// Mark.say("Onset rule:", onsetRule);

		getRuleMemory().recordRule(onsetRule, completeStory.getType());

		// for (Entity indicator : onsetRule.getSubject().getElements()) {
		//
		// addToOnsetRules(indicator, onsetRule);
		//
		// }
		Sequence completionRule = extractConcept(completeStory);
		concepts.addElement(completionRule);

		// Sila
		// Mark.betterSay("FIRST ELEMENT OF CONCEPTS : ");
		// Mark.betterSay(concepts.getElements().elementAt(0).asString());

		// for (Thing t : concepts.getElements()) {
		// Mark.say("Concept:", t.asString());
		// }

		Connections.getPorts(this).transmit(CONCEPTS_VIEWER_PORT, concepts);
		// Connections.getPorts(this).transmit(CONCEPT_ONSET_VIEWER_PORT, conceptOnsets);
		// Connections.getPorts(this).transmit(StatisticsBar.CONCEPT_LABEL,
		// concepts.getElements().size());
		Connections.getPorts(this)
		        .transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.CONCEPT_COUNT, concepts.getElements().size()));

	}

	/**
	 * Keep only essentials of concept description. Does not include classifications, except those in sometimes clauses,
	 * because they only set up required types for the variables..
	 */
	private Sequence extractConcept(Sequence input) {
		// Mark.say("====================================\n", input.asString());
		Sequence concept = new Sequence(Markers.CONCEPT_MARKER);
		concept.addType(input.getType());
		for (Entity t : input.getElements()) {
			if (t.isAPrimed(Markers.DESCRIBE_MARKER)) {
				concept.addElement(t);
			}
			else if (t.isAPrimed(Markers.ENTAIL_RULE)) {
				concept.addElement(t);
			}
			else if (!t.isAPrimed(Markers.CLASSIFICATION_MARKER)) {
				concept.addElement(t);
			}
			else if (t.isAPrimed(Markers.CLASSIFICATION_MARKER) && t.hasProperty(Markers.IDIOM, Markers.SOMETIMES)) {
				concept.addElement(t);
			}
		}
		// Mark.say("\n", concept.asString());
		return concept;
	}

	/**
	 * Construct rule-like description of concept for onset detection. Makes one giant rule from all the antecedents of
	 * embedded leads-to rules + other expressions, but does not include classification expressions, which only set up
	 * required types for the variables. Makes consequent be a list of consequents. Removes intermediate consequents
	 * from consequents list. <p ? No attention yet to handling "sometimes" constructions
	 */
	private Relation extractConceptOnsetRule(Sequence input) {
		Sequence antecedents = new Sequence(Markers.CONJUNCTION);
		Sequence consequents = new Sequence(Markers.CONJUNCTION);
		Relation rule = new Relation(Markers.ONSET_MARKER, antecedents, consequents);

		rule.addType(Markers.CAUSE_MARKER);

		rule.addType(input.getType());

		antecedents.addType(Markers.INDICATORS);
		consequents.addType(Markers.INDICATIONS);

		for (Entity t : input.getElements()) {
			if (t.isAPrimed(Markers.DESCRIBE_MARKER)) {
				rule.addElement(t);
			}
			else if (t.isAPrimed(Markers.ENTAIL_RULE)) {
				Entity ifs = t.getSubject();
				Entity then = t.getObject();
				if (ifs.sequenceP(Markers.CONJUNCTION)) {
					for (Entity e : ifs.getElements()) {
						if (!antecedents.getElements().contains(e)) {
							antecedents.addElement(e);
						}
					}
				}
				consequents.addElement(then);
			}
			else if (!t.isAPrimed(Markers.CLASSIFICATION_MARKER)) {
				if (!antecedents.getElements().contains(t)) {
					antecedents.addElement(t);
				}
			}
		}
		for (Entity t : antecedents.getElements()) {
			consequents.getElements().remove(t);
		}
		rule.setSubject(antecedents);
		rule.setObject(consequents);
		return rule;
	}

	protected static Vector<Entity> copy(Vector<Entity> things) {
		Vector<Entity> newVector = new Vector<Entity>();
		newVector.addAll(things);
		return newVector;
	}

	/**
	 * Don't want means to block explanation processing---no, need means there to make concept pattern processing
	 * possible
	 */
	public void addInference(Entity target) {
		
		Mark.yellow("Add inference: ", target);
		// if (!inferences.contains(target) && !Predicates.isMeans(target)) {
		if (!getInferredElements().contains(target)) {

			// Mark.say("Yep, adding", target.asStringWithIndexes());
			// inferences.addElement(target);
			addAlreadyPredicted(target.getObject());
			Connections.getPorts(this).transmit(NEW_INFERENCE_PORT, target);
			Connections.getPorts(this).transmit(INFERENCES, getInstantiatedRules());
			// getInstantiatedRules().stream().forEachOrdered(r -> Mark.say("Transmitting", r));
		}
		else {
			// Mark.say("Nope, not adding", target.asStringWithIndexes());
		}
	}

	public Sequence getInferences() {
		Sequence story = getStory();
		Sequence inferences = new Sequence();
		for (Entity e : story.getElements()) {
			if (Predicates.isCause(e) || Predicates.isGoalInference(e)) {
				inferences.addElement(e);
			}
		}
		return inferences;
	}
	
	public Sequence getInferredElements() {
		Sequence story = getStory();
		Sequence inferences = new Sequence();
		for (Entity e : story.getElements()) {
			if (Predicates.isPrediction(e)) {
				if (!inferences.getElements().contains(e.getObject())) {
					inferences.addElement(e.getObject());
				}
			} else if (Predicates.isGoalInference(e) && !Predicates.isCause(e)) {
			    // just want inferred goal story events, not goal causal connections
			    if (!inferences.getElements().contains(e)) {
			        inferences.addElement(e);
			    }
			}
			else if (Predicates.isAbduction(e) || Predicates.isPresumption(e) || Predicates.isEnabler(e)) {
				// Mark.say("Adding abduction/predicate elements");
				// e.getSubject().getElements().stream().forEachOrdered(x -> Mark.say("Element", x));
				for (Entity x : e.getSubject().getElements()) {
					if (!inferences.getElements().contains(x)) {
						inferences.addElement(x);
					}
				}
			}
		}
		return inferences;
	}
	
	public Sequence getInstantiatedRules() {
		Sequence story = getStory();
		Sequence inferences = new Sequence();
		for (Entity e : story.getElements()) {
			if (Predicates.isPrediction(e) || Predicates.isAbduction(e) || Predicates.isPresumption(e) || Predicates.isEnabler(e)) {
				// Mark.say("Adding abduction/predicate elements");
				// e.getSubject().getElements().stream().forEachOrdered(x -> Mark.say("Element", x));
				if (!inferences.getElements().contains(e)) {
					inferences.addElement(e);
				}
			}
		}
		return inferences;
	}
	
	public static Sequence getExplicitElements(Sequence story) {
	    Sequence result = new Sequence();
	    List<Entity> inferences = new ArrayList<>();
	    for (Entity e : story.getElements()) {
	        if (Predicates.isPrediction(e)) {
	            inferences.add(e.getObject());
	        } else if (Predicates.isGoalInference(e) && !Predicates.isCause(e)) {
	            // just want inferred goal story events, not goal causal connections
	            inferences.add(e);
	        }
	        else if (Predicates.isAbduction(e) || Predicates.isPresumption(e) || Predicates.isEnabler(e)) {
	            // Mark.say("Adding abduction/predicate elements");
	            // e.getSubject().getElements().stream().forEachOrdered(x -> Mark.say("Element", x));
	            inferences.addAll(e.getSubject().getElements());
	        }
	    }
	    for (Entity e : story.getElements()) {
	        if (!Predicates.isCause(e) || Predicates.isExplictCauseOrLeadsTo(e)) {
	            if (!inferences.contains(e)) {
	                result.addElement(e);
	            }
	        }
	    }
	    return result;
	}
	
	public Sequence getExplicitElements() {
		return getExplicitElements(story);
	}

	public Sequence getStory() {
		return story;
	}

	public void setStory(Sequence summary) {
		boolean debug = false;
		Mark.say(debug, "Resetting....................");

		Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.RESET);
		sizeAtPreviousQuiescence = 0;
		getStory().clearElements();
		getExplicitElements().getElements().clear();
		getSceneMapList().stream().forEach(e -> e.clear());
		getAlreadyPredicted().clear();
		getInstantiatedConceptPatterns().getElements().clear();
		getInstantiatedConcepts().getElements().clear();
		getInferredElements().clearElements();
		for (Entity e : summary.getElements()) {
			Mark.say(debug, "Reprocessing", e);
			processElement(e);
		}
		BetterSignal signal = new BetterSignal(concepts, story, getInferences());

		getConceptExpert().process(signal);
		// Connections.getPorts(this).transmit(TO_COMPLETION_DETECTOR, signal);
	}

	public void transferStory(StoryProcessor source) {
		story = source.getStory();
		story.addType(source.getStory().getType());
		Mark.say("New name", source.getName());
		Connections.getPorts(this).transmit(STORY_NAME, story.getType());
		instantiatedConcepts = source.getInstantiatedConcepts();
		Connections.getPorts(getConceptExpert()).transmit(ConceptBar.CLEAR_CONCEPT_BUTTONS, Markers.RESET);
		transmitStory();
	}

	public Sequence extractStory() {
		int storyElementCount = countElementsSansInferences(story.getElements());
		// int oldExplicitInputs = storyElementCount - getInferredElements().getElements().size();
		int newExplicitInputs = getExplicitElements().getElements().size();

		Connections.getPorts(this).transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.STORY_ELEMENT_COUNT, storyElementCount));

		Connections.getPorts(this)
		        .transmit(StoryProcessor.TO_STATISTICS_BAR, new BetterSignal(StatisticsBar.EXPLICIT_STATEMENT_COUNT, newExplicitInputs));

		return story;
	}

	private int countElementsSansInferences(Vector<Entity> elements) {
		int size = 0;
		for (Entity t : elements) {
			if (!Predicates.isCause(t) || Predicates.isExplictCauseOrLeadsTo(t)) {
				++size;
			}
			else {
				// System.out.print(".");
			}
		}
		return size;
	}

	private class RulePackage {
		public Relation rule;

		public ArrayList<PairOfEntities> bindings;

		public Sequence antecedants;

		public RulePackage(Relation r, ArrayList<PairOfEntities> b) {
			rule = r;
			bindings = b;
			antecedants = new Sequence(Markers.CONJUNCTION);
		}

		public RulePackage(Relation r, ArrayList<PairOfEntities> b, Sequence a) {
			this(r, b);
			antecedants = a;
		}
	}

	public ArrayList<Entity> getCensorRules(Entity t, HashMap<String, ArrayList<Entity>> censorRuleMap, boolean includeHasNegation) {
		ArrayList<Entity> mappedRules = new ArrayList<Entity>();
		Thread thread = t.getThread(Markers.ACTION_WORD);
		if (thread == null) {
			thread = t.getPrimedThread();
		}
		if (thread == null) {
			thread = new Thread();
		}

		for (String type : thread) {

			// Mark.say("Looking for censor associated with", type);
			ArrayList<Entity> more = censorRuleMap.get(type);

			if (more != null) {
				for (Entity r : more) {
					if (r.isAPrimed(Markers.CENSOR)) {
						if (testRuleForNegation(r, includeHasNegation)) {
							mappedRules.add(r);
						}
					}
				}
			}
		}
		return mappedRules;
	}

	// private ArrayList<Entity> getExplanationRules(Entity element, ArrayList<MentalModel> personalityModels, boolean
	// includeHasNegation) {
	// ArrayList<Entity> mappedRules = new ArrayList<Entity>();
	// for (MentalModel mentalModel : personalityModels) {
	// mappedRules.addAll(mentalModel.getStoryProcessor().getExplanationRules(element, true));
	// }
	// return mappedRules;
	// }
	//
	// private ArrayList<Entity> getPredictionRules(Entity element, ArrayList<MentalModel> personalityModels, boolean
	// includeHasNegation) {
	// ArrayList<Entity> mappedRules = new ArrayList<Entity>();
	// for (MentalModel mentalModel : personalityModels) {
	// // mappedRules.addAll(mentalModel.getStoryProcessor().getPredictionRules(element, true));
	// }
	// return mappedRules;
	// }

	// public HashMap<String, ArrayList<Entity>> getExplanationRuleMap() {
	// return explanationRuleMap;
	// }

	// public HashMap<String, ArrayList<Entity>> getPredictionRuleMap() {
	// return predictionRuleMap;
	// }

	// public ArrayList<Entity> getExplanationRules(Entity t, boolean includeHasNegation) {
	// return getExplanationRules(t, getExplanationRuleMap(), includeHasNegation);
	// }

	// public ArrayList<Entity> getPredictionRules(Entity t, boolean includeHasNegation) {
	// return getPredictionRules(t, getPredictionRuleMap(), includeHasNegation);
	// }

	public ArrayList<Entity> getPredictionRules(Entity t, HashMap<String, ArrayList<Entity>> map, boolean includeHasNegation) {

		ArrayList<Entity> mappedRules = new ArrayList<Entity>();

		Thread thread = t.getThread(Markers.ACTION_WORD);
		if (thread == null) {
			thread = t.getPrimedThread();
		}
		if (thread == null) {
			thread = new Thread();
		}
		for (String type : thread) {
			ArrayList<Entity> more = map.get(type);
			if (more != null) {
				for (Entity r : more) {
					if (!r.isAPrimed(Markers.ENTAIL_RULE)) {
						if (testRuleForNegation(r, includeHasNegation)) {
							if (!mappedRules.contains(r)) {
								mappedRules.add(r);
							}
						}
					}
				}
			}
		}

		return mappedRules;
	}

	public static ArrayList<Entity> getExplanationRules(Entity t, HashMap<String, ArrayList<Entity>> explanationRuleMap, boolean ignore) {

		boolean debug = false;

		Mark.say(debug, "Finding rules for", t.asString());

		ArrayList<Entity> mappedRules = new ArrayList<Entity>();
		Mark.say(debug, "Explanation rule count:", explanationRuleMap.size(), t.asString());
		Thread thread = t.getThreadWith(Markers.ACTION_WORD);
		if (thread == null) {
			thread = t.getPrimedThread();
		}
		Mark.say(debug, "Thread", thread);
		for (String type : thread) {
			ArrayList<Entity> more = explanationRuleMap.get(type);
			if (more != null) {
				Mark.say(debug, "Working on type", type, "returning", more.size());
			}
			else {
				Mark.say(debug, "Working on type", type, "no result");
			}
			if (more != null) {
				for (Entity r : more) {
					if (!r.isAPrimed(Markers.ENTAIL_RULE)) {
						if (!mappedRules.contains(r)) {
							mappedRules.add(r);
						}
						// else {
						// Mark.say("Already got an explanation rule",
						// r.asString());
						// }
					}
				}
			}
		}

		Mark.say(debug, "Found", mappedRules.size(), "rules");

		return mappedRules;
	}

	// public void addExplanationRules(ArrayList<Entity> rules) {
	// for (Entity r : rules) {
	// addExplanationRule(r);
	// }
	// }
	//
	// public void addCensorRules(ArrayList<Entity> rules) {
	// for (Entity r : rules) {
	// addCensorRule(r);
	// }
	// }
	//
	// public void addPredictionRules(ArrayList<Entity> rules) {
	// for (Entity r : rules) {
	// addPredictionRule(r);
	// }
	// }

	// public ArrayList<Entity> getExplanationRules() {
	// return getRules(explanationRuleMap);
	// }
	//
	// public ArrayList<Entity> getCensorRules() {
	// return getRules(censorRuleMap);
	// }

	// public ArrayList<Entity> getPredictionRules() {
	// return getRules(predictionRuleMap);
	// }

	private ArrayList<Entity> getRules(HashMap<String, ArrayList<Entity>> map) {
		HashSet<Relation> rules = new HashSet<Relation>();
		for (ArrayList<Entity> l : map.values()) {
			for (Entity t : l) {
				rules.add((Relation) t);
			}
		}
		ArrayList<Entity> result = new ArrayList<>();
		for (Entity t : rules) {
			result.add((Relation) t);
		}
		return result;
	}

	private static boolean testRuleForNegation(Entity r, boolean includeHasNegation) {
		if (includeHasNegation) {
			return true;
		}
		if (r.functionP()) {
			Object antecedents = r.getSubject();
			if (antecedents instanceof Sequence) {
				for (Entity t : ((Sequence) antecedents).getElements()) {
					if (t.hasFeature(Markers.NOT)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/*
	 * Get's key for storage. Smart about storing rules involving "perform".
	 */
	private static String getRuleKey(Entity t) {
		if (t.relationP(Markers.PERFORM)) {
			return Markers.ACTION_MARKER;
		}
		// Not a perform, just return type
		return t.getType();
	}

	// private void addToPredictionRules(Entity antecedent, Relation rule) {
	// // Do not add prediction rule if it is a concept unless it is an
	// // entail
	// if (story.isA(Markers.CONCEPT_MARKER) && !rule.isA(Markers.ENTAIL_RULE)) {
	// // return;
	// }
	// String type = getRuleKey(antecedent);
	// // Need to deal with "occur" here because some rules will have occur in
	// // antecedents
	// // mpfay 3/19/2014, modified this code so it no longer causes rules to disappear
	// if (antecedent.functionP(Markers.APPEAR_MARKER) && antecedent.getSubject().getType().equals(Markers.ACTION_WORD))
	// {
	// String action_type = antecedent.getSubject().getType();
	// if (!predictionRuleMap.containsKey(action_type)) predictionRuleMap.put(action_type, new ArrayList<Entity>());
	// predictionRuleMap.get(action_type).add(rule);
	// }
	// // Mark.say("Storing rule under", type);
	// ArrayList<Entity> list = predictionRuleMap.get(type);
	// if (list == null) {
	// list = new ArrayList<Entity>();
	// predictionRuleMap.put(type, list);
	// }
	// // Mark.say("Storing rule under", type, "namely", rule.asString());
	// list.add(rule);
	// }

	// private void addToExplanationRules(Entity consequent, Relation rule) {
	// // if (rule.isAPrimed(Markers.IF_MARKER)) {
	// // // It is an if, so don't want to have it trigger.
	// // return;
	// // }
	// String type = consequent.getType();
	// // mpfay 3/19/2014 this makes sure action rules trigger properly
	// if (consequent.functionP(Markers.APPEAR_MARKER) && consequent.getSubject().getType().equals(Markers.ACTION_WORD))
	// {
	// String action_type = consequent.getSubject().getType();
	// if (!explanationRuleMap.containsKey(action_type)) explanationRuleMap.put(action_type, new ArrayList<Entity>());
	// explanationRuleMap.get(action_type).add(rule);
	// }
	// // Mark.say("Adding explanation rule for " + type);
	// ArrayList<Entity> list = explanationRuleMap.get(type);
	// if (list == null) {
	// list = new ArrayList<Entity>();
	// explanationRuleMap.put(type, list);
	// }
	// list.add(rule);
	// }

	// private void addToCensorRules(Entity consequent, Entity rule) {
	// if (!consequent.hasFeature(Markers.NOT)) {
	// return;
	// }
	// // consequent.removeFeature(Markers.NOT);
	// String type = consequent.getType();
	// // Mark.say("Adding censor rule", rule.asString(), "for " + type);
	//
	// ArrayList<Entity> list = censorRuleMap.get(type);
	// if (list == null) {
	// list = new ArrayList<Entity>();
	// censorRuleMap.put(type, list);
	// }
	// list.add(rule);
	// }

	private void addToOnsetRules(Entity antecedent, Relation rule) {
		String type = antecedent.getType();
		if (antecedent.functionP(Markers.APPEAR_MARKER) && antecedent.getSubject().getType().equals(Markers.ACTION_WORD)) {
			type = antecedent.getSubject().getType();
		}
		ArrayList<Entity> list = onsetRuleMap.get(type);
		if (list == null) {
			list = new ArrayList<Entity>();
			onsetRuleMap.put(type, list);
		}
		list.add(rule);
	}

	// private void addCompletionRule(Thing consequent, Sequence concept) {
	// String type = consequent.getType();
	// if (consequent.functionP(Markers.APPEAR_MARKER) &&
	// consequent.getSubject().getType().equals(Markers.ACTION_WORD)) {
	// type = consequent.getSubject().getType();
	// }
	// ArrayList<Thing> list = completionRuleMap.get(type);
	// if (list == null) {
	// list = new ArrayList<Thing>();
	// completionRuleMap.put(type, list);
	// }
	// list.add(concept);
	// }

	// public HashSet<String> getAlreadyInserted() {
	// if (alreadyInserted == null) {
	// alreadyInserted = new HashSet<String>();
	// }
	// return alreadyInserted;
	// }
	//
	private HashSet<String> getAlreadyPredicted() {
		if (alreadyPredicted == null) {
			alreadyPredicted = new HashSet<String>();
		}
		return alreadyPredicted;
	}

	//
	// public HashSet<String> getAlreadyUsed() {
	// if (alreadyUsed == null) {
	// alreadyUsed = new HashSet<String>();
	// }
	// return alreadyUsed;
	// }

	// public boolean isAlreadyInserted(Thing t) {
	// return getAlreadyInserted().contains(t.getName());
	// }
	//
	public boolean isAlreadyPredicted(Entity t) {
		// Mark.say("checking if " + t + " is predicted");
		return getAlreadyPredicted().contains(t.asStringWithIndexes());
	}

	public boolean isAlreadyPredictedSansMeans(Entity t) {
		Optional<Entity> optional = getInferredElements().getElements().stream().filter(f -> !Predicates.isMeans(f) && t == f.getObject())
		        .findFirst();
		// if (optional.isPresent()) {
		// Mark.say("Returning", optional.isPresent(), optional.get());
		// }
		return optional.isPresent();
	}

	//
	// public boolean isAlreadyUsed(Thing t) {
	// return getAlreadyUsed().contains(t.getName());
	// }
	//
	// public void addAlreadyInserted(Thing t) {
	// if (!isAlreadyInserted(t)) {
	// getAlreadyInserted().add(t.getName());
	// }
	// }
	//
	private void addAlreadyPredicted(Entity t) {
		if (!isAlreadyPredicted(t)) {
			getAlreadyPredicted().add(t.asStringWithIndexes());
		}
	}

	private void clearAlreadyPredicted() {
		getAlreadyPredicted().clear();
	}

	//
	// public void addAlreadyUsed(Thing t) {
	// getAlreadyUsed().add(t.getName());
	// }

	public static String getTitle(Sequence story) {
		for (Entity t : story.getElements()) {
			if (t.relationP("start")) {
				Relation r = (Relation) t;
				if (r.getSubject().isA("you")) {
					if (r.getObject().functionP()) {
						return r.getObject().getSubject().getType();
					}
				}
			}
		}
		return story.getType();
	}

	public static void main(String[] ignore) {
		StoryProcessor processor = new StoryProcessor("Foo");
		processor.story = new Sequence();
		Entity mark = new Entity("queen");
		Entity bill = new Entity("king");
		Relation hit = new Relation("hit", bill, mark);
		Entity cris = new Entity("knight");
		Relation friend = new Relation("friend", cris, mark);
		Relation harm = new Relation("harm", bill, cris);
		Sequence antecedants = new Sequence();
		antecedants.addElement(hit);
		antecedants.addElement(friend);
		Relation cause = new Relation("cause", antecedants, harm);

		// cause.addType(Markers.PREDICTION);

		cause.addType(Markers.EXPLANATION_RULE);

		ArrayList<Relation> rules = new ArrayList<Relation>();

		processor.recordRule(cause);

		// System.out.println(cause);
		processor.processElement(cause);
		processor.storyInProgress = true;
		Entity mary = new Entity("queen");
		mary.addType("name");
		mary.addType("mary");
		Entity blak = new Entity("king");
		blak.addType("name");
		blak.addType("blak");
		Relation hit2 = new Relation("hit", blak, mary);
		Entity curt = new Entity("knight");
		curt.addType("name");
		curt.addType("curt");
		Relation friend2 = new Relation("friend", curt, mary);
		processor.processElement(friend2);
		processor.processElement(hit2);

		Entity sam = new Entity("knight");
		sam.addType("name");
		sam.addType("sam");
		Relation friend3 = new Relation("friend", sam, mary);
		processor.processElement(friend3);

		Relation harm2 = new Relation("harm", blak, curt);
		Relation harm3 = new Relation("harm", blak, sam);

		processor.processElement(harm2);
		processor.processElement(harm3);

		// processor.addAlreadyInserted(friend3);
		// Mark.say("alreadyNoted", processor.isAlreadyInserted(friend2));

	}

	ArrayList<Entity> roles = new ArrayList<Entity>();

	private Entity replace(Entity role) {
		for (Entity candidate : roles) {
			if (Substitutor.matchTypesAndSign(role, candidate)) {
				return candidate;
			}
		}
		roles.add(role);
		return role;
	}

	private void rebuild(Entity element) {
		// Mark.say(debugRebuildx, "Rebuilding concept from",
		// element.asString());
		rebuildAux(element);
		// Mark.say(debugRebuild, " -->",
		// element.asString());
	}

	private void rebuildAux(Entity element) {
		if (element.entityP()) {
			// Should not get in here
			System.err.println("Bug--got into Thing part of StoryProcessor.thingP");
			throw new RuntimeException("Bug--got into Thing part of StoryProcessor.thingP");
		}
		if (element.functionP()) {
			if (element.getSubject().entityP()) {
				element.setSubject(replace(element.getSubject()));
			}
			else {
				rebuildAux(element.getSubject());
			}
		}
		if (element.relationP()) {
			if (element.getSubject().entityP()) {
				element.setSubject(replace(element.getSubject()));
			}
			else {
				rebuildAux(element.getSubject());
			}
			if (element.getObject().entityP()) {
				element.setObject(replace(element.getObject()));
			}
			else {
				rebuildAux(element.getObject());
			}
		}
		if (element.sequenceP()) {
			Vector<Entity> elements = element.getElements();
			Vector<Entity> scratch = (Vector<Entity>) (elements.clone());
			for (int i = 0; i < scratch.size(); ++i) {
				Entity e = scratch.get(i);
				if (e.entityP()) {
					elements.remove(i);
					elements.add(i, replace(e));
				}
				else {
					rebuildAux(e);
				}
			}

		}
	}

	// private Entity rebuildElement(Entity element, HashMap<String, Entity> cache) {
	// boolean debug = false;
	// Mark.say(debug, "Looking for", element.asString());
	// if (checkForElementsInCache(element, cache)) {
	// Mark.say(debug, "Working on replacement of", element.hash());
	// return rebuildElementAux(element, cache);
	// }
	// else {
	// Mark.say(debug, "No need to replace", element.hash());
	// // Not something that needs total or partial replacement, so return
	// // as
	// // is
	// return element;
	// }
	// }

	// private Entity rebuildWithCurrentScene(Entity element, HashMap<String, Entity> localMap) {
	// HashMap<String, Entity> cache = getCurrentSceneMap();
	// if (isCached(element, cache)) {
	// return getFromCache(element, cache);
	// }
	// return reassemble(element, localMap);
	// }
	//
	// private Entity rebuildWithWholeStory(Entity element, HashMap<String, Entity> localMap) {
	// ArrayList<HashMap<String, Entity>> cacheList = this.getSceneMapList();
	// for (HashMap<String, Entity> cache : cacheList) {
	// if (isCached(element, cache)) {
	// return getFromCache(element, cache);
	// }
	// }
	// return reassemble(element, localMap);
	// }

	// private Entity rebuildCauseRelation(Entity element) {
	// // At this point, cause relation has been dereferenced.
	//
	// // Goal now is to insert what needs to be inserted in current scene; this will include the consequent and any
	// // antecedents that are not in either the current scene or anywhere.
	//
	// if (!isInCurrentScene(element)) {
	// element = makeNewVersion(element);
	// }
	//
	// Vector<Entity> antecedents = ((Sequence) element.getSubject()).getElements();
	//
	// for (int i = 0; i < antecedents.size(); ++i) {
	// // See if antecedent is in the current scene.
	// Entity antecedent = antecedents.get(i);
	// if (!isInAnyScene(antecedent)) {
	// // Mark.say("Making new version of >>>>>>", antecedent.asStringWithIndexes());
	// Entity newVersion = makeNewVersion(antecedent);
	// antecedents.set(i, newVersion);
	// }
	// }
	// Entity consequent = element.getObject();
	// if (!isInCurrentScene(consequent)) {
	// Entity newVersion = makeNewVersion(consequent);
	// element.setObject(newVersion);
	// }
	// return element;
	// }

	// private Entity rebuildElementAux(Entity element, HashMap<String, Entity> cache) {
	// boolean debug = false;
	//
	// if (element.entityP()) {
	// // Changed 30 Apr 2013 to do post story reading dereferencing of things
	// // Previously did not cache thing instances
	// // Mark.say("Looking for", element.asStringWithIndexes(), "in cache");
	// Entity cachedElement = getFromCache(element, cache);
	// if (cachedElement != null) {
	// return cachedElement;
	// }
	// // Mark.say("Did not find it, returning as is");
	// return element;
	// }
	// if (isCached(element, cache)) {
	// Entity cachedElement = getFromCache(element, cache);
	// Mark.say(debug, "Found element in Cache\n", element.asStringWithIndexes(), "\n",
	// cachedElement.asStringWithIndexes());
	// return cachedElement;
	// }
	// // Otherwise, rebuild
	// Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
	// if (element.functionP()) {
	// element = ((Function) element).rebuild();
	// element.setSubject(rebuildElementAux(element.getSubject(), cache));
	// }
	// else if (element.relationP()) {
	// element = ((Relation) element).rebuild();
	// element.setSubject(rebuildElementAux(element.getSubject(), cache));
	// element.setObject(rebuildElementAux(element.getObject(), cache));
	// }
	// else if (element.sequenceP()) {
	// Vector<Entity> elements = element.getElements();
	// element = ((Sequence) element).rebuildWithoutElements();
	// for (Entity x : elements) {
	// element.addElement(rebuildElementAux(x, cache));
	// }
	// }
	// return element;
	// }

	// private Entity rebuildElementAux(Entity element, HashMap<String, Entity> cache) {
	// boolean debug = false;
	//
	// if (element.entityP()) {
	// // Changed 30 Apr 2013 to do post story reading dereferencing of things
	// // Previously did not cache thing instances
	// // Mark.say("Looking for", element.asStringWithIndexes(), "in cache");
	// Entity cachedElement = getFromCache(element, cache);
	// if (cachedElement != null) {
	// return cachedElement;
	// }
	// // Mark.say("Did not find it, returning as is");
	// return element;
	// }
	// if (isCached(element, cache)) {
	// Entity cachedElement = getFromCache(element, cache);
	// Mark.say(debug, "Found element in Cache\n", element.asStringWithIndexes(), "\n",
	// cachedElement.asStringWithIndexes());
	// return cachedElement;
	// }
	// // Otherwise, rebuild
	// Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
	// if (element.functionP()) {
	// // replacement = new
	// // Derivative(rebuildElementAux(element.getSubject(), cache));
	// element = new Function("temp", rebuildElementAux(element.getSubject(), cache));
	// }
	// else if (element.relationP()) {
	// element = new Relation("temp", rebuildElementAux(element.getSubject(), cache),
	// rebuildElementAux(element.getObject(), cache));
	// }
	// else if (element.sequenceP()) {
	// Sequence result = new Sequence("temp");
	// for (int i = 0; i < element.getElements().size(); ++i) {
	// result.addElement(rebuildElementAux(element.getElements().get(i), cache));
	// }
	// element = result;
	// }
	// return element;
	// }

	private boolean checkForElementsInCache(Entity element, HashMap<String, Entity> cache) {
		Mark.say("Checking up on", element.asString());
		if (element.entityP()) {
			// Do nothing with Things
			return false;
		}
		if (isCached(element, cache)) {
			return true;
		}
		// Otherwise, check parts
		if (element.functionP()) {
			return checkForElementsInCache(element.getSubject(), cache);
		}
		else if (element.relationP()) {
			return checkForElementsInCache(element.getSubject(), cache) || checkForElementsInCache(element.getObject(), cache);
		}
		else if (element.sequenceP()) {
			for (Entity e : element.getElements()) {
				if (checkForElementsInCache(e.getSubject(), cache)) {
					return true;
				}
			}
		}
		return false;
	}

	// private Entity getFromStoryCache(Entity t) {
	// return getFromCache(t, getStoryCache());
	// }
	//
	// private Entity getFromSceneCache(Entity t) {
	// return getFromCache(t, getSceneCache());
	// }

	private Entity getFromCache(Entity t, HashMap<String, Entity> map) {
		Entity result = map.get(hash(t));
		if (result == null) {
			// Mark.say(t.entityP(), "Nothing in cache", t.asStringWithIndexes(), hash(t));
			return t;
		}
		// Mark.say("Found result for", t.asStringWithIndexes(), ":", result.asStringWithIndexes());
		return result;
	}
	
	// if t is mapped, removes mapping and returns previous mapping for t; otherwise returns null
	private Entity removeFromCache(Entity t, HashMap<String, Entity> map) {
	    String hash = hash(t);
	    return map.remove(hash);
	}

	// private void addToStoryCache(Entity t) {
	// if (isCachedInStory(t)) {
	// return;
	// }
	// // Mark.say("Adding to story cache,", t.asStringWithNames());
	// insertIntoCache(t, getStoryCache());
	// addToCompleteCache(t, getCompleteStoryCache());
	// }
	//
	// private void addToSceneCache(Entity t) {
	// insertIntoCache(t, getSceneCache());
	// addToCompleteCache(t, getCompleteSceneCache());
	// }

	// private void addToEmbeddedCache(Entity t) {
	// addToCompleteCache(t, getEmbeddedCache());
	// }

	private void insertIntoCache(Entity t, HashMap<String, Entity> cache) {
		// Mark.say(true, "Inserting\n", t.asStringWithIndexes(), "\ninto cache at\n", hash(t));
		// Don't insert if not open
		// Mark.say("Inserting:", hash(t));

		if (isOpen()) {
			cache.put(hash(t), t);
		}
	}

	/**
	 * Create cache of elements appearing in story or scene
	 */
	private void addToCompleteCache(Entity t, HashMap<String, Entity> cache) {
		// Mark.say("Adding to complete cache", t.hash());
		if (t.entityP()) {

			insertIntoCache(t, cache);
			Entity x = getFromCache(t, cache);
			return;
		}
		else if (t.functionP()) {
			addToCompleteCache(t.getSubject(), cache);
		}
		else if (t.relationP()) {
			addToCompleteCache(t.getSubject(), cache);
			addToCompleteCache(t.getObject(), cache);
		}
		else if (t.sequenceP()) {
			for (Entity e : t.getElements()) {
				addToCompleteCache(e, cache);
			}
		}
		// Insert everything other than things; and maybe things too
		insertIntoCache(t, cache);
	}

	// private void clearStoryCache() {
	// // Mark.say("Clearing story caches");
	// getStoryCache().clear();
	// getCompleteStoryCache().clear();
	// }

	// private void clearSceneCache() {
	// getSceneCache().clear();
	// getCompleteSceneCache().clear();
	// }

	// private void clearEmbeddedCache() {
	// getEmbeddedCache().clear();
	// }
	//
	// private boolean isCachedInEmbeddedCache(Entity t) {
	// return isCached(t, getEmbeddedCache());
	// }

	// private boolean isCachedInStory(Entity t) {
	// return isCached(t, getStoryCache());
	// }
	//
	// private boolean isCachedInScene(Entity t) {
	// // Mark.say("Looking in cache for", t.hash());
	// return isCached(t, getSceneCache());
	// }

	private boolean isCached(Entity t, HashMap<String, Entity> cache) {
		if (cache.get(hash(t)) != null) {
			return true;
		}
		return false;
	}

	// private HashMap<String, Entity> getEmbeddedCache() {
	// if (embeddedCache == null) {
	// embeddedCache = new HashMap<String, Entity>();
	// }
	// return embeddedCache;
	// }

	// private HashMap<String, Entity> getStoryCache() {
	// if (storyCache == null) {
	// storyCache = new LinkedHashMap<String, Entity>();
	// }
	// return storyCache;
	// }

	// private HashMap<String, Entity> getSceneCache() {
	// if (sceneCache == null) {
	// sceneCache = new LinkedHashMap<String, Entity>();
	// }
	// return sceneCache;
	// }

	// private HashMap<String, Entity> getCompleteStoryCache() {
	// if (completeStoryCache == null) {
	// completeStoryCache = new LinkedHashMap<String, Entity>();
	// }
	// return completeStoryCache;
	// }

	// private HashMap<String, Entity> getCompleteSceneCache() {
	// if (completeSceneCache == null) {
	// completeSceneCache = new HashMap<String, Entity>();
	// }
	// return completeSceneCache;
	// }

	private String hash(Entity t) {
		// 4 Aug 2012 Seems like asString should work just fine, because retains
		// identity of things, but not derivatives, relations or sequences
		// Later on, may need to regularize order of sequences.
		String result = t.hash();
		// Mark.say("Hash", result);
		return result;

	}

	private boolean isAsleep() {
		return asleep;
	}

	private void setAsleep(boolean x) {
		// Mark.say("Setting asleep to", x);
		this.asleep = x;
	}

	public boolean isAwake() {
		return !asleep;
	}

	public void setAwake(boolean x) {
		// Mark.say("Setting awake to", x);
		setAsleep(!x);
	}

	public boolean isPlayByPlayInput() {
		return playByPlayInput;
	}

	public void setPlayByPlayInput(boolean playByPlayInput) {
		this.playByPlayInput = playByPlayInput;
	}

	// public boolean isStoryInProgress() {
	// return storyInProgress;
	// }
	//
	// public void setStoryInProgress(boolean storyInProgress) {
	// this.storyInProgress = storyInProgress;
	// }

	public void setInstantiations(Object o) {
		if (o instanceof Sequence) {
			instantiatedConcepts = (Sequence) o;
		}
		Connections.getPorts(this).transmit(INSTANTIATED_CONCEPTS, instantiatedConcepts);
	}

	public void setConceptAnalysis(Object o) {
		// Mark.say("Setting Concept Analysis in StoryProcessor");
		if (o == null) {
			Mark.say("CONCEPT NULL");
		}
		if (o instanceof ConceptAnalysis) {
			setConceptAnalysis((ConceptAnalysis) o);
			// describeConceptAnalysis(conceptAnalysis);
			// Mark.say("Transmitting on CONCEPT_ANALYSIS");
			Connections.getPorts(this).transmit(CONCEPT_ANALYSIS, conceptAnalysis);

		}
	}

	public static void describeConceptAnalysis(ConceptAnalysis conceptAnalysis) {
		Mark.say("Start of concept analyis");
		for (ConceptDescription d : conceptAnalysis.getConceptDescriptions()) {
			Mark.say("Concept description: ", d.getName());
			for (Entity a : d.getStoryElementsInvolved().getAllComponents()) {
				Mark.say(a.asString());
			}
		}
		Mark.say("End of concept analyis");
	}

	public ConceptAnalysis getConceptAnalysis() {
		return conceptAnalysis;
	}

	public void setConceptAnalysis(ConceptAnalysis conceptAnalysis) {
		boolean debug = false;

		this.conceptAnalysis = conceptAnalysis;

		conceptAnalysis.getConceptDescriptions().stream().forEach(cd -> {
			Mark.say(debug, "Adding concept to story", cd.getInstantiations());

			if (Switch.useInsertConceptConsequentsIntoStory.isSelected()) {
			
				Sequence concept = cd.getInstantiations();
				Sequence clone = (Sequence) (concept.clone());

				Mark.say(debug, "Found concept", concept);

				Sequence conjunction = new Sequence(Markers.CONJUNCTION);

				Sequence consequents = cd.getConsequences();

				for (Entity e : clone.getElements()) {

					if (!consequents.contains(e)) {
						conjunction.addElement(e);
						Mark.say(debug, "Adding antecedent", e);
					}

					// Mark.say(debug, "Element", e);
					// Entity type = RoleFrames.getRole(Markers.MANNER_MARKER, e);
					// Object property = e.getProperty(Markers.PROPERTY_TYPE);
					// // if (type != null && type.isA(Markers.CONSEQUENTLY)) {
					// Mark.say(debug, "Property type is", property);
					// if (property != null && ((String) property).equalsIgnoreCase(Markers.CONSEQUENTLY)) {
					// concept.removeElement(e);
					// RoleFrames.removeRole(e, Markers.MANNER_MARKER);
					// Entity cause = Rules.makeCause(e, concept);
					// queue.add(cause);
					// }
					// else if (property != null && ((String) property).equalsIgnoreCase(Markers.PREVIOUSLY)) {
					// Entity cause = Rules.makeCause(concept, e);
					// concept.removeElement(e);
					// RoleFrames.removeRole(e, Markers.MANNER_MARKER);
					// queue.add(cause);
					// }
				}
				

				consequents.stream().forEach(c -> {
					Entity dereferencedConsequent = this.reassembleAndDereference(c);
					cd.getStoryElementsInvolved().addElement(dereferencedConsequent);
					Entity cause = Rules.makeCause(c, conjunction);
					// This will still need consequent dereferenced
					processElement(cause);
				});
				
				// Put concept in too
				processElement(cd.getInstantiations());

			}

		});

	}

	public Sequence getCommonsenseRules() {
		// Mark.say("Returning rules:", rules.asString());
		return getRuleMemory().getRuleSequence();
	}

	public Sequence getConceptPatterns() {
		// Mark.say("Returning concepts:", conceptjs.asString());
		return concepts;
	}

	public void addConceptPatterns(Sequence concepts) {
		for (Entity s : concepts.getElements()) {
			addConcept((Sequence) s);
		}
	}

	public ArrayList<Sequence> getConcepts() {
		ArrayList<Sequence> result = new ArrayList<Sequence>();
		for (Entity t : getConceptPatterns().getElements()) {
			result.add((Sequence) t);
		}
		return result;
	}

	public void addConcepts(List<Sequence> concepts) {
		for (Sequence story : concepts) {
			addConcept(story);
		}
	}

	public Sequence getInstantiatedConcepts() {
		return instantiatedConcepts;
	}

	public MentalModel getMentalModel() {
		if (mentalModel == null) {
			mentalModel = new MentalModel(getName());
		}
		return mentalModel;
	}

	private void setMentalModel(MentalModel mentalModel) {
		this.mentalModel = mentalModel;
	}

	// public ArrayList<Entity> getPredictionRules(Entity element) {
	// return getPredictionRules(element, predictionRuleMap, true);
	// }

	// public ArrayList<Entity> getExplanationRules(Entity element) {
	// return getExplanationRules(element, explanationRuleMap, true);
	// }
	//
	// public ArrayList<Entity> getCensorRules(Entity element) {
	// return getCensorRules(element, censorRuleMap, true);
	// }

	public void processTraitAddition(Object o) {
		boolean debug = false;
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			if (signal.get(0, String.class) == DELIVER_TRAIT_CONCEPTS) {
				for (Entity concept : signal.get(1, Sequence.class).getElements()) {
					if (!concepts.contains(concept)) {
						Mark.say(debug, "Adding concept", concept.asString());
						concepts.addElement(concept);
					}
					else {
						Mark.say(debug, "Already have", concept.asString());
					}
				}
			}
			else if (signal.get(0, String.class) == DELIVER_TRAIT_CHARACTERIZATION) {
				Entity entity = signal.get(1, Entity.class);
				Mark.say(debug, "Delivering", entity);
				processElement(entity);
			}
		}
	}

	public void processInstantiatedConcepts(Object o) {
		if (o instanceof Sequence) setInstantiatedConceptPatterns((Sequence) o);
	}

	public void setInstantiatedConceptPatterns(Sequence instantiatedConceptPatterns) {
		this.instantiatedConceptPatterns = instantiatedConceptPatterns;
	}

	public Sequence getInstantiatedConceptPatterns() {
		return instantiatedConceptPatterns;
	}

	// // Major scrubbing material follows

	private boolean augmentCurrentSceneIfNotInAnyScene(Entity element, Sequence story) {
		if (!isInAnyScene(element)) {
			addToCurrentScene(element);

			// Mark.say("Adding because nowhere", element.asStringWithIndexes());
			return true;
		}
		return false;
	}

	public boolean isInAnyScene(Entity element) {
		for (HashMap<String, Entity> map : getSceneMapList()) {
			if (isCached(element, map)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Temporary hack
	 */
	private boolean isCommand(Entity entity) {
		return entity.getBooleanProperty(Markers.REPETITION_ALLOWED);
	}

	private boolean augmentCurrentSceneIfNotInCurrentScene(Entity element, Sequence story) {
		if (!isInCurrentScene(element) || allowMultipleEntriesInSameScene) {
			if (isInAnyScene(element)) {
				// Mark.say("Should reassemble", element);
				element = reassembleWithEntityDereference(element, this.globalCache);
			}
			// Mark.say("Adding to current scene.", element);
			addToCurrentScene(element);
			return true;
		}
		// else if (isCommand(element) && RoleFrames.isRoleFrame(element)) {
		// // Copy so that handled properly by elaboration graph display
		// Relation copy = new Relation(Markers.UNKNOWN, element.getSubject(), element.getObject());
		// element.transferBundle(copy);
		// addToCurrentScene(copy);
		// }
		return false;
	}

	private Entity reassembleWithEntityDereference(Entity element, HashMap<String, Entity> map) {
		boolean debug = false;
		Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
		if (element.entityP() && isCached(element, map)) {
			Mark.say(debug, "Got from cache", getFromCache(element, map));
			return getFromCache(element, map);
		}
		if (element.functionP()) {
			element = ((Function) element).rebuild();
			element.setSubject(reassembleWithEntityDereference(element.getSubject(), map));
			insertIntoCache(element, map);
		}
		else if (element.relationP()) {
			element = ((Relation) element).rebuild();
			element.setSubject(reassembleWithEntityDereference(element.getSubject(), map));
			element.setObject(reassembleWithEntityDereference(element.getObject(), map));
			insertIntoCache(element, map);
		}
		else if (element.sequenceP()) {
			Vector<Entity> elements = element.getElements();
			element = ((Sequence) element).rebuildWithoutElements();
			for (Entity x : elements) {
				element.addElement(reassembleWithEntityDereference(x, map));
			}
			insertIntoCache(element, map);
		}
		return element;
	}

	private Entity addToCurrentScene(Entity element) {
		// EXTREMELY hackish special case for wanting
		if (element.isA(Markers.WANT_MARKER)) {
			String hash = hash(RoleFrames.getObject(element));
			getWantCache().put(hash, element);
		}
		else {
			String hash = hash(element);
			Entity want = getWantCache().get(hash);
			if (want != null) {
				Getters.replaceObject(want, element);
				// Mark.say("Bingo!!!!!!!!!!!!!!!!!!!!", element);
			}
		}
		// Mark.say("Existance noted", y.asStringWithIndexes());
		// insertIntoCache(x, getCurrentSceneMap());

		insertIntoCache(element, getCurrentSceneMap());
		// Mark.say("Finally, adding", element);

		// Mark.say("Adding", element);
		// explicitElements.addElement(element);
		story.addElement(element);

		transmitIncrement(element);

		if (!story.isA(Markers.CONCEPT_MARKER) && Switch.slowMotionSwitch.isSelected()) {

			Mark.say("Adding", element);

			transmitStory();

			Sleep.sleep(1000);

		}

		return element;
	}

	public boolean isInCurrentScene(Entity e) {
		return isCached(e, getCurrentSceneMap());
	}


	public void addSceneMap() {
		// Most recent map in front to facilitate search into past
		getSceneMapList().add(0, new HashMap<String, Entity>());
	}

	public HashMap<String, Entity> getCurrentSceneMap() {
		if (getSceneMapList().isEmpty()) {
			addSceneMap();
		}
		return getSceneMapList().get(0);
	}

	public void removeCurrentSceneMap() {
		getSceneMapList().remove(0);
	}

	public ArrayList<HashMap<String, Entity>> getSceneMapList() {
		if (sceneMapList == null) {
			sceneMapList = new ArrayList<>();
		}
		return sceneMapList;
	}

	public HashMap<String, Entity> getGlobalCache() {
		if (globalCache == null) {
			globalCache = new LinkedHashMap<>();
		}
		return globalCache;
	}

	public HashMap<String, Entity> getMentalModelCache() {
		if (mentalModelCache == null) {
			mentalModelCache = new LinkedHashMap();
		}
		return mentalModelCache;
	}

	public HashMap<String, Entity> getWantCache() {
		if (wantCache == null) {
			wantCache = new LinkedHashMap();
		}
		return wantCache;
	}

	private Entity makeNewVersion(Entity element) {
		boolean debug = false;
		Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
		if (element.functionP()) {
			element = ((Function) element).rebuild();
		}
		else if (element.relationP()) {
			element = ((Relation) element).rebuild();
			// insertIntoCache(element, localMap);
		}
		else if (element.sequenceP()) {
			Vector<Entity> elements = element.getElements();
			element = ((Sequence) element).rebuildWithoutElements();
			for (Entity x : elements) {
				element.addElement(x);
			}
		}
		insertIntoCache(element, getGlobalCache());
		return element;
	}

	private Entity dereferenceAntecedentsAndConsequents(Entity element) {
		// Do nothing unless cause
		if (Predicates.isCause(element)) {
			// Ok, it is a cause, see if it is leads-to or because; other forms of backward chaining are linked up
			// elsewhere; these are not because the are explicit in the story.
			if (Predicates.isExplictCauseOrLeadsTo(element)) {
				// First, rebuild. May not be necessary, but if there are multiple interpretations, modifying given
				// connection may have unintended consequences
				Vector<Entity> currentAntecedents = element.getSubject().getElements();
				Sequence newAntecedents = new Sequence();
				for (Entity currentAntecedent : currentAntecedents) {
					if (isInAnyScene(currentAntecedent)) {
						newAntecedents.addElement(retrieveFromCache(currentAntecedent));
					}
					else {
						newAntecedents.addElement(currentAntecedent);
						processElement(currentAntecedent);
					}
				}
				Entity newConsequent = element.getObject();
				if (isInAnyScene(newConsequent)) {
					newConsequent = retrieveFromCache(newConsequent);
				}
				else {
					processElement(newConsequent);
				}

				element.getSubject().transferThreadsFeaturesAndProperties(newAntecedents);
				Relation connection = new Relation(Markers.UNKNOWN, newAntecedents, newConsequent);
				element.transferThreadsFeaturesAndProperties(connection);
				return connection;
			}
		}
		return element;
	}

	public Entity retrieveFromCache(Entity element) {
		String hash = hash(element);
		for (HashMap<String, Entity> map : getSceneMapList()) {

			Entity result = map.get(hash);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/*
	 * A bit of a crock. Without replaceNames, just finds matching structures. With replaceNames, looks for entities
	 * with same names in dereferencing process. Used, for example, when you ask
	 * "Did Duncan know that Macduff killed Macbeth." The "Macduff killed Macbeth" part would have been dereferenced
	 * anyway, but not the "Did Duncan know..." part, because that part is not in the story.
	 */
	public Entity reassembleAndDereferenceQuestion(Entity element) {
		boolean debug = false;
		HashMap<String, Entity> entities = findEntities(story);
		Mark.say(debug, "Original   ", element.asStringWithIndexes());
		replaceNames(element, entities);
		Mark.say(debug, "Replacement", element.asStringWithIndexes());
		element = reassembleAndDereference(element);
		Mark.say(debug, "Derefrenced", element.asStringWithIndexes());
		return element;
	}

	private Entity replaceNames(Entity x, HashMap<String, Entity> set) {
		boolean debug = false;
		if (x.sequenceP()) {
			Vector<Entity> newElements = new Vector<>();
			x.getElements().stream().forEach(f -> newElements.add(replaceNames(f, set)));
			x.getElements().clear();
			newElements.stream().forEach(f -> x.getElements().add(f));
		}
		else if (x.relationP()) {
			x.setSubject(replaceNames(x.getSubject(), set));
			x.setObject(replaceNames(x.getObject(), set));
		}
		else if (x.functionP()) {
			x.setSubject(replaceNames(x.getSubject(), set));
		}
		else if (x.entityP()) {
			// Ok, so it is an entity
			if (x.isA(Markers.NAME)) {
				Entity replacement = set.get(x.getType());
				if (replacement != null) {
					replacement.addProperty(Markers.REQUIRE_NAME_MATCH, true);
					return replacement;
				}
				// Did Duncan observe that Macduff killed Macbeth?
				Mark.say(debug, "Could not find replacement for", x, "in set of size", set.size());
				set.keySet().stream().forEach(f -> Mark.say(debug, f, "=", set.get(f)));
			}
			else {
				// Mark.say(x, "is not a name");
			}
		}
		return x;
	}

	private HashMap<String, Entity> findEntities(Entity story) {
		HashMap<String, Entity> result = new HashMap<>();
		findEntities(story, result);
		return result;
	}

	private void findEntities(Entity x, HashMap<String, Entity> set) {
		if (x.sequenceP()) {
			x.getElements().stream().forEach(f -> findEntities(f, set));
		}
		else if (x.relationP()) {
			findEntities(x.getSubject(), set);
			findEntities(x.getObject(), set);
		}
		else if (x.functionP()) {
			findEntities(x.getSubject(), set);
		}
		else if (x.entityP()) {
			// Ok, so it is an entity, see if it is a proper name
			if (x.isA(Markers.NAME)) {
				set.put(x.getType(), x);
			}
		}
	}
	
	/**
	 * If the current story element is not an inferred goal event, but an equivalent goal event exists that
	 * was inferred earlier, the earlier goal event is no longer "inferred" but explicit
	 * 
	 * @param element an entity explicitly in the story that does not have the Markers.GOAL_ANALYSIS property
	 * @param cache cache to search for equivalent event in
	 * @return event with inference erased if current element explicitly confirms this event, otherwise returns null
	 */
	private Entity eraseEarlierInferredGoalEvent(Entity element) {
	    // if element is an explicitly stated cause, must erase goal inferences in subject and effect, too
	    if (Predicates.isExplictCauseOrLeadsTo(element)) {
	        Entity subject = element.getSubject();
	        Entity object = element.getObject();
	        if (!subject.hasProperty(Markers.GOAL_ANALYSIS)) {
	            Entity erasedSubject = eraseEarlierInferredGoalEvent(subject);
	            if (erasedSubject != null) {
	                element.setSubject(subject);
	            }
	        }
	        if (!object.hasProperty(Markers.GOAL_ANALYSIS)) {
	            Entity erasedObject = eraseEarlierInferredGoalEvent(object);
	            if (erasedObject != null) {
	                element.setObject(object);
	            }
	        }
	    }
	    
	    HashMap<String, Entity> cache = this.getGlobalCache();
	    Thread originalThread = element.getPrimedThread();
	    // first, check that element is not an inferred goal event 
	    for (Entity cachedValue : cache.values()) {
	        // thread hack - one thread may be a superset of the other when the cachedValue
	        // and element are actually equivalent. allow both of them to take on superset thread
	        Thread cachedValueThread = cachedValue.getPrimedThread();
	        if (cachedValueThread.containsAll(originalThread)) {
	            element.setPrimedThread(cachedValueThread);
	        } else if (originalThread.containsAll(cachedValueThread)) {
	            cachedValue.setPrimedThread(originalThread);
	        }
	            
	        // check if we've seen an inferred goal event equivalent to element 
	        if (cachedValue.hasProperty(Markers.GOAL_ANALYSIS) && 
	                element.isDeepEqual(cachedValue)) {
	            HashMap<String, Entity> sceneCache = this.getCurrentSceneMap();
	            removeFromCache(cachedValue, cache);
	            removeFromCache(cachedValue, sceneCache);
	            cachedValue.removeProperty(Markers.GOAL_ANALYSIS);
	            // works even if cachedValue does not have CN justification property
	            cachedValue.removeProperty(Markers.CONCEPTNET_JUSTIFICATION);
	            insertIntoCache(cachedValue, cache);
	            insertIntoCache(cachedValue, sceneCache);
	            element.setPrimedThread(originalThread);
	            return cachedValue;
	        }
	        cachedValue.setPrimedThread(cachedValueThread);
	    }
	    
	    element.setPrimedThread(originalThread);
	    return null;
	}
	
    /**
     * If the current story element is an inferred goal event, but an equivalent explicitly stated story event already
     * exists, the current story event is no longer "inferred" but explicit
     * 
     * @param element an inferred goal event in the story (has the Markers.GOAL_ANALYSIS property)
     * @return event with inference erased if previous story event explicitly confirms this event, otherwise returns null
     */
    private Entity eraseCurrentInferredGoalEvent(Entity element) {
        if (Predicates.isCause(element)) {
            Entity subject = element.getSubject();
            if (subject.hasProperty(Markers.GOAL_ANALYSIS)) {
                Entity erasedSubject = eraseCurrentInferredGoalEvent(subject);
                if (erasedSubject != null) {
                    element.setSubject(erasedSubject);
                }
            }
            Entity object = element.getObject();
            if (object.hasProperty(Markers.GOAL_ANALYSIS)) {
                Entity erasedObject = eraseCurrentInferredGoalEvent(object);
                if (erasedObject != null) {
                    element.setObject(erasedObject);
                }
            }
        }
        
        for (Entity e : story.getElements()) {
            if (e.isDeepEqual(element)) {
                return e;
            }
        }
        
        return null;
	}
	
	public Entity reassembleAndDereference(Entity element) {
		boolean debug = false;
		HashMap<String, Entity> sceneMap = this.getCurrentSceneMap();
		HashMap<String, Entity> globalMap = this.getGlobalCache();
		

		if (!element.hasProperty(Markers.GOAL_ANALYSIS)) {
		    // if element is not an inferred goal event, but an equivalent inferred goal event exists,
	        // find goal event and erase inference so that event is explicit
		    Entity elementWithoutGoalInference = eraseEarlierInferredGoalEvent(element);
		    if (elementWithoutGoalInference != null) {
		        return elementWithoutGoalInference;
		    }
		} else if (element.hasProperty(Markers.GOAL_ANALYSIS)) {
		    // if element is inferred goal event but an equivalent story event already exists,
		    // return that event
		    Entity previousExplicitGoalEvent = eraseCurrentInferredGoalEvent(element);
		    if (previousExplicitGoalEvent != null) {
		        return previousExplicitGoalEvent;
		    }
		}
		
		// For testing only
		// if (element.isA("eat")) {
		// Entity test = Translator.getTranslator().translateToEntity("Mary squashed a worm");
		// injectElementWithDereferenceAtSceneStart(test);
		// }
		Entity result = reassemble(element, globalMap);

		Mark.say(debug, "Reassembled result", result);
		return result;
	}

	private Entity reassembleSansTop(Entity element, HashMap<String, Entity> map) {
		boolean debug = false;
		Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
		if (isCached(element, map)) {
			Mark.say(debug, "Got from cache", getFromCache(element, map));
			return getFromCache(element, map);
		}
		if (element.functionP()) {
			element = ((Function) element).rebuild();
			element.setSubject(reassemble(element.getSubject(), map));
			insertIntoCache(element, map);
		}
		else if (element.relationP()) {
			element = ((Relation) element).rebuild();
			element.setSubject(reassemble(element.getSubject(), map));
			element.setObject(reassemble(element.getObject(), map));
			insertIntoCache(element, map);
		}
		else if (element.sequenceP()) {
			Vector<Entity> elements = element.getElements();
			element = ((Sequence) element).rebuildWithoutElements();
			for (Entity x : elements) {
				element.addElement(reassemble(x, map));
			}
			insertIntoCache(element, map);
		}
		return element;
	}



	private Entity reassemble(Entity element, HashMap<String, Entity> map) {
		boolean debug = false;
		Mark.say(debug, "Working on replacement of", element.asStringWithIndexes());
		if (isCached(element, map)) {
//		    Entity cached = getFromCache(element, map);
//		    if (element.getPropertyList().size() > cached.getPropertyList().size()) {
//		        insertIntoCache(element, map);
//		        return element;
//		    }
//		    return cached;
			Mark.say(debug, "Got from cache", getFromCache(element, map));
			return getFromCache(element, map);
		}
		if (element.functionP()) {
			element = ((Function) element).rebuild();
			element.setSubject(reassemble(element.getSubject(), map));
			insertIntoCache(element, map);
		}
		else if (element.relationP()) {
			element = ((Relation) element).rebuild();
			element.setSubject(reassemble(element.getSubject(), map));
			element.setObject(reassemble(element.getObject(), map));
			insertIntoCache(element, map);
		}
		else if (element.sequenceP()) {
			Vector<Entity> elements = element.getElements();
			element = ((Sequence) element).rebuildWithoutElements();
			for (Entity x : elements) {
				element.addElement(reassemble(x, map));
			}
			insertIntoCache(element, map);
		}
		return element;
	}


	
	
	/**
	 * Determines if rules should fire
	 * 
	 * @return
	 */
	public boolean isInert() {
		return inert;
	}

	public void setInert(boolean inert) {
		this.inert = inert;
	}

	// Rule memory, added 1 Aug 2015

	private RuleMemory ruleMemory;

	public RuleMemory getRuleMemory() {
		if (ruleMemory == null) {
			ruleMemory = new RuleMemory();

			// Select appropriate rule sorter
			//// if (Switch.nonSchizophrenicGenesis.isSelected()) {
			//// ruleMemory.setRuleSorter(new NonSchizophrenicRuleSorter());
			//// }
			// if (!Switch.schizophrenicGenesis.isSelected()) {
			//// ruleMemory.setRuleSorter(new SchizophrenicRuleSorter());
			//// Mark.say("schizo gen");
			// while (true) {
			// Mark.say("hello");
			// }
			//
			// }
			// else { //Default case

			ruleMemory.setRuleSorter(new RuleSorter());

			// }

		}
		return ruleMemory;
	}

	private RuleEngine ruleEngine;

	public RuleEngine getRuleEngine() {
		if (ruleEngine == null) {
			ruleEngine = new RuleEngine();
		}
		return ruleEngine;
	}

	public void processOnset(Memory memory) {
		Connections.getPorts(this).transmit(ONSET_VIEWER_PORT, new BetterSignal(memory.getName()));
	}


}

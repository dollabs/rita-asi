package mentalModels;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import javax.swing.*;

import storyProcessor.CommandExpansionExpert;
import subsystems.recall.SessionStoryMemory;
import connections.*;
import connections.signals.BetterSignal;
import consciousness.*;
import constants.Markers;
import constants.Switch;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Sequence;
import genesis.*;
import gui.*;
import rules.InstructionBox;
import start.*;
import storyProcessor.*;
import translator.*;
import utils.*;

/**
 * A MentalModel instance contains an instance of a story processor. Story processor can be populated from a file if
 * fileName is not null. Records only information presented in the first perspective (following "First perspective." or
 * "Both perspectives." not "Second Perspective." in the English text).
 * <p>
 * If you want to have a file read after construction, send the file name in as a string via the INJECT_FILE port.
 * <p>
 * All story processor input and output ports are connected to corresponding mental model ports, so that a mental model
 * can be substituted for a story processor in older code.
 * <p>
 * Also, a mental model carries with it viewer apparatus, accessible by getters.
 * <p>
 * Also, a MentalModel instance is also a Thing instance, which allows mental models to participate in Innerese
 * constructions.
 */

public class MentalModel extends Entity implements WiredBox {

	// Work in progress. Check around before switching.
	public static boolean USE_ORIGINAL_WIRING = false;

	public static HashMap<String, MentalModel> globalModels;

	public Map<String, MentalModel> localMentalModels;

	public static final String INJECT_FILE = "port for file injection";

	public static final String INJECT_STORY = "port for story sequence injection";

	public static final String STOP_STORY = "time to run stop story";

	public static final String MENTAL_MODEL_SNAPSHOT = "mental model snapshot";

	public static final String COMPLETE_STORY_ANALYSIS_PORT = "analysis from story processor";

	private FileSourceReader reader;

	private StartPreprocessor startPreprocessor;

	private Start startParser;

	ParserTranslator parserTranslator;

	private StartPostprocessor startPostprocessor;

	private BasicTranslator basicTranslator;

	private StoryProcessor storyProcessor;

	private ShortCut shortCut;

	private I i;

	// private ElaborationViewer elaborationViewer;

	private JPanel plotDiagram;

	private JPanel analysisPanel;

	private ConceptBar conceptBar;

	private ConceptExpert conceptExpert;

	private CommandExpansionExpert commandExpansionExpert;


	// private TraitExpert traitExpert;

	private RuleViewer instantiatedRuleViewer;

	private StoryViewer instantiatedConceptViewer;

	private CauseGraphViewer causeGraphViewer;

	private JScrollPane plotUnitBarScroller;

	private StatisticsBar statisticsBar;

	public static final String RECORD_REFLECTION_ANALYSIS = "port for receiving information from ReflectionExpert";

	public static final String COMMENTARY = "commentary port";

	private ConceptAnalysis conceptAnalysis;

	private WiredProgressBar storyProgressBar;

	private NameLabel storyNameLabel;

	private ArrayList<Entity> examples;

	private SessionStoryMemory storyMemory;

	private Entity modeledEntity;

	private InstructionBox instructionBox;

	private ProblemSolver problemSolver;

	// private HashMap<String, MentalModel> personalities;

	// private Thing me = null;

	// public void loadPersonalities() {
	// loadMentalModel("psychotic", "psychotic");
	// loadMentalModel("neurotic", "neurotic");
	// }


	/**
	 * Constructs an instance of a mental model based on an entity
	 */
	public MentalModel(Entity entity) {
		this(entity.getName(), null);
		modeledEntity = entity;
	}

	public Entity getModeledEntity() {
		return modeledEntity;
	}

	/*
	 * Constructs an instance of an empty story processor "inside" an instance of this MentalModel class.
	 */
	public MentalModel(String processorName) {
		this(processorName, null);
	}

	public MentalModel(String processorName, String fileName) {
		this(processorName, fileName, false);
	}

	/**
	 * Includes code for identifying rules, concepts, and typical actions in file with this mental model.
	 */
	public MentalModel(String processorName, String fileName, boolean debug) {
		super("Mental model");
		this.addType(processorName);
		setName(processorName);
		Mark.say(debug, "\n>>>  Constructing mental model", processorName, "from file", fileName);

		wireUp(processorName);

		Mark.say(debug, "Wiring done");

		GenesisGetters.getMentalModelViewer().addTab(processorName, getViewerWrapper());
		GenesisGetters.getMentalModelViewer().setSelectedComponent(getViewerWrapper());

		// So first mental model, presumably reader, is always the visible one by default.
		GenesisGetters.getMentalModelViewer().setSelectedIndex(0);

		if (fileName != null) {
			Mark.say(debug, "Starting to read mental model file", fileName);
			getFileSourceReader().readTheWholeStoryWithoutTread(fileName);
			Mark.say(debug, "Done reading mental model file", fileName);
		}

		Mark.say(debug, "Rule count", this.getStoryProcessor().getRuleMemory().getRuleList().size());

		for (Sequence concept : this.getConcepts()) {
			// Mark.say("In", this.getName(), "have concept", concept.asString());
			concept.addProperty(Markers.MENTAL_MODEL_HOST, this);

		}

		// Now, if there is a story, treat it as source of examples and kill off the story:

		Vector<Entity> storyElements = getStoryProcessor().getStory().getElements();

		if (storyElements != null && !storyElements.isEmpty()) {
			// storyElements.remove(0);
			getExamples().addAll(storyElements);
			getStoryProcessor().getStory().getElements().clear();

		}

		// Mark.say("Constructed mental model", processorName);

	}

	public boolean entityP() {
		return true;
	}

	public void wireUp(String processorName) {
		// Mark.say("Start wiring", processorName);
		wireUpStoryProcessorInputPorts();
		// Mark.say("A");
		wireUpStoryProcessorOutputPorts();
		// Mark.say("B");
		wireUpInternals();
		// Mark.say("C");
		addSignalProcessors();
		// Mark.say("D");
		wireUpCommandProcessor();
		// Mark.say("Wiring complete");
	}

	private void wireUpCommandProcessor() {
		Connections.wire(StoryProcessor.COMMAND_PORT, getStoryProcessor(), CommandExpansionExpert.STORY, getCommandExpansionExpert());

	}

	private void wireUpStoryProcessorInputPorts() {
		// Principal input forwards; gleaned from StoryProcessor.

		Connections.forwardTo(Port.INPUT, this, getStoryProcessor());

		Connections.forwardTo(Port.INPUT, this, getStoryProgressBar());

		Connections.forwardTo(Start.STAGE_DIRECTION_PORT, this, getStoryProcessor());

		Connections.forwardTo(StoryProcessor.INJECT_ELEMENT_INTO_TRAIT_MODEL, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.INJECT_ELEMENT, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.INJECT_RULE, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.INJECT_CONCEPT, this, getStoryProcessor());

		Connections.forwardTo(ConceptBar.CLEAR_CONCEPT_BUTTONS, this, getConceptBar());

		Connections.forwardTo(StatisticsBar.CLEAR_COUNTS, this, getStatisticsBar());

		Connections.forwardTo(StatisticsBar.CLEAR_COUNTS, this, getStatisticsBar());

		// Fay's ports
		Connections.forwardTo(StoryProcessor.PREDICTION_RULES_PORT, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.CONCEPTS_VIEWER_PORT, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.RULE_PORT, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.INCOMING_INSTANTIATIONS, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.INCOMING_CONCEPT_ANALYSIS, this, getStoryProcessor());

		// Sila's work: to become aware of learned rules
		Connections.forwardTo(StoryProcessor.LEARNED_RULE_PORT, this, getStoryProcessor());
		Connections.forwardTo(StoryProcessor.NEW_RULE_MESSENGER_PORT, this, getStoryProcessor());

	}

	private void wireUpStoryProcessorOutputPorts() {
		Connections.forwardFrom(getStoryProcessor(), this);
		Connections.forwardFrom(Start.STAGE_DIRECTION_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(Start.MODE, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.RULE_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.FINAL_INFERENCES, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.CONCEPTS_VIEWER_PORT, getStoryProcessor(), this);
		// Connections.forwardFrom(StoryProcessor.TO_ONSET_DETECTOR, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.EXPLICIT_STORY, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.COMMENTARY_PORT, getStoryProcessor(), this);
		// Connections.forwardFrom(StoryProcessor.COMMENTARY_PORT, getTraitExpert(), this);
		Connections.forwardFrom(Markers.NEXT, getStoryProcessor(), this);
		Connections.forwardFrom(ConceptExpert.CONCEPT_ANALYSIS, getConceptExpert(), this);
		Connections.forwardFrom(StoryProcessor.COMPLETE_STORY_EVENTS_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.INCREMENT_PORT_COMPLETE, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.STORY_PROCESSOR_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.PLOT_PLAY_BY_PLAY_PORT, getStoryProcessor(), this);

		// Fay's ports
		Connections.forwardFrom(StoryProcessor.CLUSTER_STORY_PORT, getStoryProcessor(), this);

		// Sila's ports

		Connections.forwardFrom(StoryProcessor.INCREMENT_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.NEW_INFERENCE_PORT, getStoryProcessor(), this);
		Connections.forwardFrom(StoryProcessor.FINAL_INPUTS, getStoryProcessor(), this);

	}

	public void wireUpInternals() {

		Connections.wire(getFileSourceReader(), getStartPreprocessor());
		Connections.wire(FileSourceReader.STATUS, getFileSourceReader(), getStoryProgressBar());
		Connections.wire(StartPreprocessor.SELF, getStartPreprocessor(), getStartPreprocessor());

		Connections.wire(Start.STAGE_DIRECTION_PORT, getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, getStoryProcessor());
		Connections.wire(Start.STAGE_DIRECTION_PORT, getStartPreprocessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getConceptBar());
		Connections.wire(Start.STAGE_DIRECTION_PORT, getStartPreprocessor(), getInstantiatedConceptViewer());

		if (USE_ORIGINAL_WIRING) {
			Connections.wire(Start.MODE, getStartPreprocessor(), Start.MODE, getStartParser());
			Connections.wire(getStartPreprocessor(), Start.SENTENCE, getStartParser());
			Connections.wire(Start.PARSE, getStartParser(), BasicTranslator.PROCESS, getTranslator());
			Connections.wire(BasicTranslator.RESULT, getTranslator(), getStartPostprocessor());
		}
		else {
			Connections.wire(Start.MODE, getStartPreprocessor(), ParserTranslator.MODE, getParserTranslator());
			Connections.wire(getStartPreprocessor(), ParserTranslator.SENTENCE_IN, getParserTranslator());
			Connections.wire(ParserTranslator.ENTITY_OUT, getParserTranslator(), getStartPostprocessor());
		}


		// Mark.say("\n>>> Wiring short cut", this.getName(), getStoryProcessor().getName());

		Connections.wire(getStartPostprocessor(), getShortCut());
		Connections.wire(getShortCut(), getStoryProcessor());

		// Mark.say("Short cut wired", this.getName(), getStoryProcessor().getName());

		Connections.wire(Start.MODE, getStoryProcessor(), Start.MODE, getStartParser());

		Connections.wire(StoryProcessor.INFERENCES, getStoryProcessor(), getInstantiatedRuleViewer());
		Connections.wire(StoryProcessor.FINAL_INFERENCES, getStoryProcessor(), RuleViewer.FINAL_INFERENCE, getInstantiatedRuleViewer());

		Connections.wire(ConceptExpert.INSTANTIATED_CONCEPTS, getConceptExpert(), getInstantiatedConceptViewer());

		// Connections.wire(getInstantiatedRuleViewer(),
		// getElaborationViewer());
		// Connections.wire(getStoryProcessor(), getElaborationViewer());

		Connections.wire(getStoryProcessor(), ElaborationView.STORY, getElaborationView());


		// Connections.wire(StoryProcessor.TO_ONSET_DETECTOR, getStoryProcessor(), getOnsetDetector());

		// Connections.wire(StoryProcessor.TO_COMPLETION_DETECTOR, getStoryProcessor(), getConceptExpert());

		Connections.wire(ConceptExpert.INJECT_ELEMENT, getConceptExpert(), StoryProcessor.INJECT_ELEMENT, getStoryProcessor());

		Connections.wire(ConceptExpert.TEST_ELEMENT, getConceptExpert(), StoryProcessor.TEST_ELEMENT, getStoryProcessor());

		Connections.wire(ConceptBar.RESET, getConceptExpert(), ConceptBar.RESET, getConceptBar());
		Connections.wire(ConceptBar.CLEAR_CONCEPT_BUTTONS, getConceptExpert(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getConceptBar());
		// Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getPlotUnitBar(),
		// ElaborationViewer.FROM_CONCEPT_BAR, getElaborationViewer());

		Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getConceptBar(), ElaborationView.CONCEPT, getElaborationView());

		Connections.wire(ConceptBar.TO_ELABORATION_VIEWER, getConceptBar(), ElaborationView.INSPECTOR, getInspectionView());

		Connections.wire(ConceptBar.CONCEPT_BUTTON, getConceptExpert(), ConceptBar.CONCEPT_BUTTON, getConceptBar());

		Connections.wire(ConceptBar.TO_STATISTICS_BAR, getConceptBar(), StatisticsBar.FROM_COUNT_PRODUCER, getStatisticsBar());
		Connections.wire(StoryProcessor.TO_STATISTICS_BAR, getStoryProcessor(), StatisticsBar.FROM_COUNT_PRODUCER, getStatisticsBar());

		Connections.wire(StoryProcessor.RESET_CONCEPTS_PORT, getStoryProcessor(), ConceptBar.CLEAR_CONCEPT_BUTTONS, getConceptBar());

		Connections.wire(StoryProcessor.STORY_NAME, getStoryProcessor(), getStoryName());

		// Connections.biwire(StoryProcessor.PERSONALITY_TRAIT_PORT, getStoryProcessor(),
		// StoryProcessor.PERSONALITY_TRAIT_PORT, getTraitExpert());

		// Connections.wire(StoryProcessor.TO_FORWARD_CHAINER, getTraitExpert(),
		// getStoryProcessor().getForwardChainer());

		// Connections.wire(StoryProcessor.TO_BACKWARD_CHAINER, getTraitExpert(),
		// getStoryProcessor().getBackwardChainer());

		Connections.wire(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, getStoryProcessor(), StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this);

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getStoryProcessor(), MentalModel.COMPLETE_STORY_ANALYSIS_PORT, this);

		Connections.wire(this, getStoryViewer());

		Connections.wire(StoryProcessor.FINAL_INFERENCES, this, RuleViewer.FINAL_INFERENCE, getInstantiatedRuleViewer());
		Connections.wire(StoryProcessor.RULE_PORT, this, getRuleViewer());
		Connections.wire(StoryProcessor.CONCEPTS_VIEWER_PORT, this, getConceptViewer());

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getStoryProcessor(), CauseGraphViewer.MENTAL_MODEL_PORT, getCauseGraphViewer());

		Connections.wire(StoryProcessor.INSTRUCTION_PORT, getStoryProcessor(), getInstructionBox());

	}

	public void addSignalProcessors() {
		Connections.getPorts(this).addSignalProcessor(STOP_STORY, this::stopStory);
		Connections.getPorts(this).addSignalProcessor(INJECT_FILE, this::readFile);
		Connections.getPorts(this).addSignalProcessor(INJECT_STORY, this::injectStory);
		Connections.getPorts(this).addSignalProcessor(Start.STAGE_DIRECTION_PORT, this::processStageDirections);

		Connections.getPorts(this).addSignalProcessor(StoryProcessor.STORY_PROCESSOR_SNAPSHOT, this::transmitSnapshot);

		Connections.getPorts(this).addSignalProcessor(MentalModel.COMPLETE_STORY_ANALYSIS_PORT, this::recordStory);

	}

	public void recordStory(Object input) {
		if (input instanceof StoryAnalysis) {
			StoryAnalysis analysis = (StoryAnalysis) input;
			getStoryMemory().addStory(analysis);
		}
	}

	public void transmitSnapshot(Object o) {
		// Mark.say("Transmiting mental model snapshot");
		Connections.getPorts(this).transmit(MENTAL_MODEL_SNAPSHOT, this);
	}

	public void injectStory(Object o) {
		Mark.say("STORY INJECTED", o); // dxh
		if (o instanceof Sequence) {
			Sequence s = (Sequence) o;
			Mark.say(s);
			getStoryProcessor().setAwake(true);
			for (Entity e : s.getElements()) {
				Mark.say("storyproc", e, (e instanceof Entity));
				getStoryProcessor().processElement(e);
			}
		}
	}

	public void processStageDirections(Object o) {
		if (getStoryProcessor().isAwake()) {
			if (o instanceof BetterSignal) {
				BetterSignal bs = (BetterSignal) o;
				Object command = bs.get(0, Object.class);
				if (Markers.LOAD_PERSONALITY_FILE.equals(command)) {
					Entity entity = bs.get(1, Entity.class);
					Mark.say("Name is", name);
					Mark.say("Entity is", entity);
					Mark.say("Better load", name, "with trait", entity.getType(), entity);
					loadLocalMentalModelWithTrait(entity);
				}
			}
		}
	}

	public void describe() {
		Mark.say("Mental model description:");
		Mark.say("Name:", getName());
		Mark.say("Rules:", getStoryProcessor().getRuleMemory().getRuleSequence().getElements().size());
		Mark.say("Concepts:", getConceptPatterns().getElements().size());
		Mark.say("Instantiated rules", this.getInferences().getElements().size());
		Mark.say("Instantiated concepts", this.getInstantiatedConcepts().getElements().size());
	}

	public void startStory() {
		Mark.err("Suspicious about this, investigate when used"); //jmn seconds this sentiment but ultimately concluded that it seems okay (July 2016)
		getStoryProcessor().resetStoryVariables();
		// Put start in story mode
		Connections.getPorts(getStoryProcessor()).transmit(Start.MODE, Start.STORY_MODE);
	}

	public void startStory(Object o) {
		startStory();
	}

	public void stopStory(Object o) {
		Mark.say("Stopping mental model story");
		// Mark.say("Point S1");
		describe();
		getStoryProcessor().stopStory();
		// Mark.say("Point S2");
		// describe();
	}

	public void readFile(Object o) {
		if (o instanceof String) {
			String fileName = (String) o;
			getFileSourceReader().readTheWholeStoryWithoutTread(fileName);
			this.getPlotDiagram().revalidate();
		}
	}

	public FileSourceReader getFileSourceReader() {
		if (reader == null) {
			reader = new FileSourceReader();
			reader.setName("Mental model file reader");
		}
		return reader;
	}

	public StartPreprocessor getStartPreprocessor() {
		if (startPreprocessor == null) {
			startPreprocessor = new StartPreprocessor();
			startPreprocessor.setName("Mental model StartPreprocessor");
		}
		return startPreprocessor;
	}

	public Start getStartParser() {
		if (startParser == null) {
			startParser = new Start();
			startParser.setName("Mental model Start processor");
		}
		return startParser;
	}



	public BasicTranslator getTranslator() {
		if (basicTranslator == null) {
			basicTranslator = new BasicTranslator();
			basicTranslator.setName("Mental model translator");
		}
		return basicTranslator;
	}

	public ParserTranslator getParserTranslator() {
		if (parserTranslator == null) {
			parserTranslator = new ParserTranslator("ParserTranslator");
		}
		return parserTranslator;
	}

	public StartPostprocessor getStartPostprocessor() {
		if (startPostprocessor == null) {
			startPostprocessor = new StartPostprocessor();
			startPostprocessor.setName("Mental model post processor");
		}
		return startPostprocessor;
	}

	public StoryProcessor getStoryProcessor() {
		if (storyProcessor == null) {
			storyProcessor = new StoryProcessor(getName(), this);
			// Enormous two-day debugging hassle. Forgot this when adding gatekeep to wired box.
			// Must open it up for dealing with file reading.
			storyProcessor.getGateKeeper().setSelected(true);
		}
		return storyProcessor;
	}

	public ShortCut getShortCut() {
		if (shortCut == null) {
			shortCut = new ShortCut();
			shortCut.setName("Mental model shortcut");
		}
		return shortCut;
	}

	ElaborationView elaborationView;

	public ElaborationView getElaborationView() {
		if (elaborationView == null) {
			elaborationView = new ElaborationView();
		}
		return elaborationView;
	}

	StoryViewer storyViewer;

	public StoryViewer getStoryViewer() {
		if (storyViewer == null) {
			storyViewer = new StoryViewer();
			storyViewer.setName(this.getName() + "bonowitz viewer");
		}
		return storyViewer;
	}

	RuleViewer ruleViewer;

	public RuleViewer getRuleViewer() {
		if (ruleViewer == null) {
			ruleViewer = new RuleViewer();
			ruleViewer.setName(this.getName() + "rule viewer");
		}
		return ruleViewer;
	}

	StoryViewer conceptViewer;

	public StoryViewer getConceptViewer() {
		if (conceptViewer == null) {
			conceptViewer = new StoryViewer();
			conceptViewer.setName(this.getName() + "concept viewer");
		}
		return conceptViewer;
	}

	/*
	 * Used to display a small part of elaboration graph for study
	 */
	ElaborationView inspectionView;

	public ElaborationView getInspectionView() {
		if (inspectionView == null) {
			inspectionView = new ElaborationView();
			// inspectionView.setMode(ElaborationView.PLAIN);
			inspectionView.setAlwaysShowAllElements(true);
		}
		return inspectionView;
	}

	JTabbedPane viewerWrapper;

	public JTabbedPane getViewerWrapper() {
		if (viewerWrapper == null) {
			viewerWrapper = new JTabbedPane();

			viewerWrapper.add("Elaboration graph", getPlotDiagram());
			viewerWrapper.add("Entity sequence", getStoryViewer());
			viewerWrapper.add("Rules", getRuleViewer());
			viewerWrapper.add("Instantiated rules", getInstantiatedRuleViewer());
			viewerWrapper.add("Concepts", getConceptViewer());
			viewerWrapper.add("Instantiated concepts", getInstantiatedConceptViewer());
			viewerWrapper.add("Causation graph", getCauseGraphViewer());
		}
		return viewerWrapper;
	}


	public JComponent getPlotDiagram() {
		if (plotDiagram == null) {
			plotDiagram = new JPanel();
			plotDiagram.setBackground(Color.WHITE);
			plotDiagram.setOpaque(true);
			plotDiagram.setLayout(new BorderLayout());
			plotDiagram.add(getElaborationView());
			plotDiagram.add(getAnalysisPanel(), BorderLayout.SOUTH);
//			Mark.night("------------------", Switch.showStatisticsBar.isSelected());
			if(Switch.showStatisticsBar.isSelected()) plotDiagram.add(getStatisticsBar(), BorderLayout.WEST);
			plotDiagram.add(getStoryName(), BorderLayout.NORTH);
		}
		return plotDiagram;
	}

	protected NameLabel getStoryName() {
		if (storyNameLabel == null) {
			storyNameLabel = new NameLabel("h1", "center");
		}
		return storyNameLabel;
	}

	public JPanel getAnalysisPanel() {
		if (analysisPanel == null) {
			analysisPanel = new JPanel();
			analysisPanel.setLayout(new BorderLayout());
			analysisPanel.setPreferredSize(new Dimension(1000, 80));
			analysisPanel.setBorder(BorderFactory.createTitledBorder("Analysis"));
			analysisPanel.setBackground(Color.WHITE);
			analysisPanel.add(getConceptBar(), BorderLayout.CENTER);
			analysisPanel.add(getStoryProgressBar(), BorderLayout.SOUTH);
		}
		return analysisPanel;
	}

	// public ElaborationAdapter getElaborationAdapter() {
	// if (elaborationAdapter == null) {
	// elaborationAdapter = ElaborationAdapter.makeNetworkAdapter(getElaborationViewer());
	// }
	// return elaborationAdapter;
	// }

	public ConceptBar getConceptBar() {
		if (conceptBar == null) {
			conceptBar = new ConceptBar();
			conceptBar.setName("Concepts");
		}
		return conceptBar;
	}

	// public ConceptExpert getConceptExpert() {
	// if (conceptExpert == null) {
	// conceptExpert = new ConceptExpert();
	// conceptExpert.setName("Concept expert");
	// }
	// return conceptExpert;
	// }

	public ConceptExpert getConceptExpert() {
		return getStoryProcessor().getConceptExpert();
	}

	public RuleViewer getInstantiatedRuleViewer() {
		if (instantiatedRuleViewer == null) {
			instantiatedRuleViewer = new RuleViewer();
			instantiatedRuleViewer.setName("Instantiated rule viewer");
		}
		return instantiatedRuleViewer;
	}

	public StoryViewer getInstantiatedConceptViewer() {
		if (instantiatedConceptViewer == null) {
			instantiatedConceptViewer = new StoryViewer();
			instantiatedConceptViewer.setName("Instantiated concept viewer 1");
		}
		return instantiatedConceptViewer;
	}

	public CauseGraphViewer getCauseGraphViewer() {
		if (causeGraphViewer == null) {
		    causeGraphViewer = new CauseGraphViewer();
		    causeGraphViewer.setName("Cause graph view");
		}
		return causeGraphViewer;
	}

	public StatisticsBar getStatisticsBar() {
//		Mark.night("---------------- getStatisticsBar",Switch.showStatisticsBar.isSelected());
		if (statisticsBar == null) {
			statisticsBar = new StatisticsBar();
			statisticsBar.setName("Statistics bar");
		}
		return statisticsBar;
	}

	public WiredProgressBar getStoryProgressBar() {
		if (storyProgressBar == null) {
			storyProgressBar = new WiredProgressBar();
		}
		return storyProgressBar;
	}

	public Sequence getCommonsenseRules() {
		return getStoryProcessor().getCommonsenseRules();
	}

	public Sequence getInferences() {
		return getStoryProcessor().getInferredElements();
	}

	public void recordConceptAnalysis(Object object) {
		setConceptAnalysis((ConceptAnalysis) object);
	}

	public Sequence getInstantiatedConcepts() {
		return getStoryProcessor().getInstantiatedConcepts();
	}

	public ConceptAnalysis getConceptAnalysis() {
		return conceptAnalysis;
	}

	public void setConceptAnalysis(ConceptAnalysis conceptAnalysis) {
		this.conceptAnalysis = conceptAnalysis;
	}

	/*
	 * xaAA Keeps track of mental models; creates if needed
	 */

	// public static MentalModel getGlobalMentalModel(String name) {
	// if (globalModels == null) {
	// globalModels = new HashMap<String, MentalModel>();
	// }
	// MentalModel model = globalModels.get(name);
	// if (model == null) {
	// model = new MentalModel(name);
	// globalModels.put(name, model);
	// }
	// return model;
	// }
	//
	// /*
	// * Keeps track of mental models; creates if needed
	// */
	//
	// public static MentalModel getGlobalMentalModel(String name, String file)
	// {
	// if (globalModels == null) {
	// globalModels = new HashMap<String, MentalModel>();
	// }
	// MentalModel model = globalModels.get(name);
	// if (model == null) {
	// model = new MentalModel(name, file);
	// globalModels.put(name, model);
	// Mark.say("Created new mental model of", name);
	// }
	// else {
	// Mark.say("Already have mental model of", name);
	// }
	// return model;
	// }

	public void clearLocalMentalModels() {
		// Mark.say("Clearing local mental models in", getName());
		if (localMentalModels != null) {
			localMentalModels.clear();
			removeAllTabs();
		}
//		i = null;
//		getI();
	}

	public MentalModel getLocalMentalModel(String name) {
		//Note that name includes number, e.g. "david-11956" (not just "david")
		if (localMentalModels == null) {
			localMentalModels = new HashMap<String, MentalModel>();
		}
		return localMentalModels.get(name);
	}

	public Map<String, MentalModel> getLocalMentalModels() {
		if (localMentalModels == null) {
			localMentalModels = new HashMap<>();
		}
		return localMentalModels;
	}

	public void addLocalMentalModel(String name, MentalModel model) {
		getLocalMentalModels().put(name, model);
	}

	public MentalModel loadLocalMentalModelWithTrait(Entity entity) {
		boolean debug = false;
		try {
			String name = entity.getType();
			Mark.say(debug, "Looking for file", name + ".txt");
			ArrayList<URL> results = null;

			if (!Webstart.isWebStart()) {
				List<File> files = Webstart.getTextFile(name + ".txt");
				results = new ArrayList<>();
				for (File f : files) {
					Mark.say("Found mental model file", f);
					results.add(f.toURI().toURL());
				}
			}
			else {
				// Legacy code follows
				ArrayList<URL> initialResults = PathFinder.listStoryMatches(name);
				results = initialResults;

			}
			if (results.isEmpty()) {
				throw new IOException("Story " + name + " not Found!");
			}
			else {
				Mark.say(debug, "Found", name);
			}

			MentalModel model = loadLocalMentalModel(name, name);

			int size = model.getStoryProcessor().getRuleMemory().getRuleList().size();

			Mark.say(debug, "Found", size, "rules");

			transferAllKnowledge(model, this);

			model.setBundle((Bundle) (entity.getBundle().clone()));
			Mark.say(false, "Model", model.toXML());

			return model;
		}
		catch (IOException e) {
			Mark.err("For trait, " + name + ", no trait defintion file found for", entity, "!");
			e.printStackTrace();
			return null;
		}

	}

	public MentalModel loadLocalMentalModel(String name) {
		return loadLocalMentalModel(name, name);
	}

	public MentalModel loadLocalMentalModel(String name, String file) {
		boolean debug = false;

		if (localMentalModels == null) {
			localMentalModels = new HashMap<String, MentalModel>();
		}
		MentalModel model = localMentalModels.get(name);
		if (model != null) {
			Mark.say(debug, "Already have mental model of", name, "so reloading");
			removeTab(name);
		}
		Mark.say(debug, "Loading mental model for", name);
		model = new MentalModel(name, file);
		localMentalModels.put(name, model);
		Mark.say(debug, "Created new mental model of", name);
		return model;
	}

	private void removeAllTabs() {
		// Somehow causes one of the Is to disappear when doing Lu murder.
		// GenesisGetters.getMentalModelViewer().removeAll();
	}

	private void removeTab(String name) {
		for (int i = 0; i < GenesisGetters.getMentalModelViewer().getTabCount(); ++i) {
			if (name.equals(GenesisGetters.getMentalModelViewer().getTitleAt(i))) {
				GenesisGetters.getMentalModelViewer().remove(i);
				break;
			}
		}
	}

	public static MentalModel getGlobalMentalModel(String name) {
		return globalModels.get(name);
	}

	public static MentalModel loadGlobalMentalModel(String name) {
		return loadGlobalMentalModel(name, name);
	}

	public static MentalModel loadGlobalMentalModel(String name, String file) {
		if (globalModels == null) {
			globalModels = new HashMap<String, MentalModel>();
		}
		MentalModel model = globalModels.get(name);
		if (model != null) {
			Mark.say("Already have mental model of", name, "so reloading");
			return model;
		}
		model = new MentalModel(name, file);
		globalModels.put(name, model);
		Mark.say("Created new mental model of", name);

		return model;
	}

	public static void clearMentalModels() {
		// Mark.say("Clearing global mental models");
		if (globalModels == null) {
			return;
		}
		for (MentalModel m : globalModels.values()) {
			m.clearAllMemories();
		}
		globalModels.clear();
		GenesisGetters.getMentalModelViewer().removeAll();
	}

	/*
	 * Battery of useful getters and setters
	 */

//	 public ArrayList<Entity> getPredictionRules() {
//	 return getStoryProcessor().getPredictionRules();
//	 }
//
//	 public ArrayList<Entity> getPredictionRules(Entity element) {
//	 return getStoryProcessor().getPredictionRules(element);
//	 }
//
//	 public void addPredictionRules(ArrayList<Entity> rules) {
//	 getStoryProcessor().addPredictionRules(rules);
//	 }
//
//	 public static void transferPredictionRules(MentalModel source, MentalModel target) {
//	 target.addPredictionRules(source.getPredictionRules());
//	 }
//
//	 public ArrayList<Entity> getExplanationRules() {
//	 return getStoryProcessor().getExplanationRules();
//	 }
//
//	 public ArrayList<Entity> getExplanationRules(Entity element) {
//	 return getStoryProcessor().getExplanationRules(element);
//	 }
//
//	 public void addExplanationRules(ArrayList<Entity> rules) {
//	 getStoryProcessor().addExplanationRules(rules);
//	 }
//
//	 public static void transferExplanationRules(MentalModel source, MentalModel target) {
//	 target.addExplanationRules(source.getExplanationRules());
//	 }
//
//	 public ArrayList<Entity> getCensorRules() {
//	 return getStoryProcessor().getCensorRules();
//	 }
//
//	 public ArrayList<Entity> getCensorRules(Entity element) {
//	 return getStoryProcessor().getCensorRules(element);
//	 }
//
//	 public void addCensorRules(ArrayList<Entity> rules) {
//	 getStoryProcessor().addCensorRules(rules);
//	 }
//
//	 public static void transferCensorRules(MentalModel source, MentalModel target) {
//	 target.addCensorRules(source.getCensorRules());
//	 }

	public Sequence getConceptPatterns() {
		return getStoryProcessor().getConceptPatterns();
	}

	public void addConceptPatterns(Sequence conceptPatterns) {
		getStoryProcessor().addConceptPatterns(conceptPatterns);
	}

	public ArrayList<Sequence> getConcepts() {
		return getStoryProcessor().getConcepts();
	}

	public void addConcepts(List<Sequence> concepts) {
		// Mark.say("Adding", concepts.size(), "concepts !!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		getStoryProcessor().addConcepts(concepts);
	}

	public void clearRules() {
		getStoryProcessor().clearRules();
	}

	public void clearConcepts() {
		getStoryProcessor().clearConcepts();
	}

	/*
	 * Includes clearing story memory and results
	 */
	public void clearAllMemories() {
		getStoryProcessor().clearAllMemories();
	}

	/**
	 * Does not clear.
	 */
	public static void transferRules(MentalModel source, MentalModel target) {

		// Following code bypasses story processor's normal rule-recording mechanism.
		target.getStoryProcessor().getRuleMemory().transferFrom(source.getStoryProcessor().getRuleMemory());

		// Following code passes rules through story processor's normal mechanism.
		// StoryProcessor storyProcessorSource = source.getStoryProcessor();
		//
		// StoryProcessor storyProcessorTarget = target.getStoryProcessor();
		//
		// storyProcessorSource.getRuleMemory().getRuleList().stream().forEachOrdered(r -> {
		// Mark.say("Tranferring rule", r);
		// storyProcessorTarget.recordRule(r);
		// });

	}

	/**
	 * Does not clear
	 */
	public static void transferConcepts(MentalModel source, MentalModel target) {
		target.addConcepts(source.getConcepts());
	}

	/**
	 * Does not clear
	 */
	public static void transferMentalModels(MentalModel source, MentalModel target) {
		// Mark.say("S", source.getLocalMentalModels().size());
		for (String name : source.getLocalMentalModels().keySet()) {
			target.addLocalMentalModel(name, source.getLocalMentalModels().get(name));
		}
		// Mark.say("T", target.getLocalMentalModels().size());

	}

	/**
	 * Does not clear
	 */
	public static void transferAllKnowledge(MentalModel source, MentalModel target) {
		transferRules(source, target);
		transferConcepts(source, target);
		transferMentalModels(source, target);
	}

	/**
	 * Get's thing, used to represent this mental model in Innerese.
	 */
	// public Thing getMe() {
	// return this;
	// }

	public static MentalModel getMentalModelHosts(Entity t) {
		return (MentalModel) (t.getProperty(Markers.MENTAL_MODEL_HOST));
	}

	public static boolean hasMentalModelHost(Entity t) {
		return t.hasProperty(Markers.MENTAL_MODEL_HOST);

	}

	public ArrayList<Entity> getExamples() {
		if (examples == null) {
			examples = new ArrayList<Entity>();
		}
		// Mark.say("Example count:", examples.size());
		return examples;
	}

	// public void addExample(Entity e) {
	// getExamples().add(e);
	// }

	// public TraitExpert getTraitExpert() {
	// if (traitExpert == null) {
	// traitExpert = new TraitExpert();
	// }
	// return traitExpert;
	// }

	public BetterSignal getKnowledgeContent() {
		return getStoryProcessor().getKnowledgeContent();
	}

	public void setKnowledgeContent(BetterSignal signal) {
		getStoryProcessor().setKnowledgeContent(signal);
	}

	public I getI() {
		if (i == null) {
			// Mark.say("Getting I for", this.getName());
			i = new I(this);
		}
		return i;
	}

	public SessionStoryMemory getStoryMemory() {
		if (storyMemory == null) {
			storyMemory = new SessionStoryMemory();
		}
		return storyMemory;
	}

	public CommandExpansionExpert getCommandExpansionExpert() {
		if (commandExpansionExpert == null) {
			commandExpansionExpert = new CommandExpansionExpert();
		}
		return commandExpansionExpert;
	}


	public InstructionBox getInstructionBox() {
		if (instructionBox == null) {
	 		instructionBox = new InstructionBox();
		}
		return instructionBox;
	}

	public ProblemSolver getProblemSolver() {
		if (problemSolver == null) {
			problemSolver = new ProblemSolver(this);
		}
		return problemSolver;
	}

}

package genesis;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.Thread;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import com.ascent.gui.frame.ABasicFrame;

import bryanWilliams.goalAnalysis.AspireEngine;
import cagriZaman.SimulatorPanel;
//import carynKrakauer.*;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import connections.*;
import connections.signals.BetterSignal;
import connections.views.Adapter;
import consciousness.*;
import constants.*;
import dictionary.BundleGenerator;
import dictionary.DictionaryPage;
import genesis.Disambiguator;
import dylanHolmes.WhatIfContainer;
import expert.*;
import frames.BensGauntletComponent;
import frames.ForceInterpreter;
import frames.entities.Entity;
import frames.entities.Sequence;
import frames.memories.EntityMemory;
import generator.Generator;
import gui.*;
import gui.panels.*;
import dictionary.PageWordNetGUI;
import javafx.embed.swing.JFXPanel;
import kevinWhite.*;
import matthewFay.ClusterProcessor;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.Exporter.ExperimentExportProcessor;
import matthewFay.StoryAlignment.*;
import matthewFay.StoryThreading.*;
import matthewFay.Utilities.Pair;
import matthewFay.viewers.*;
import memory.Memory;
import memory.utilities.Distances;
import memory2.*;
import memory2.gui.ChainViewer;
import memory2.gui.M2Viewer;
import memory2.gui.PredictionsViewer;
import memory2.gui.RepViewer;
import mentalModels.MentalModel;

//import rachelChaney.RachelsPictureExpert;
import silaSayan.*;
import silasAast.*;
import start.*;
import start.portico.Combinator;
import start.portico.IdiomSplitter;
import storyProcessor.*;
import subsystems.blocksWorld.BlocksWorldApplication;
import tomLarson.*;
import translator.*;
import utils.Talker;
import utils.minilisp.LList;
import viz.videos.MovieDescription;
import viz.videos.MovieManager;
import utils.*;
import zhutianYang.*;

/*
 * Created on Jul 10, 2006 @author phw
 */
public class GenesisGetters extends GenesisControls implements WiredBox {

	// -----------------------------
	//
	// 		I : All the viewers
	//
	// -----------------------------

	private CauseExpert causeExpert;

	private TimeExpert timeExpert;

	private CoercionExpert coercionExpert;

	private TrajectoryExpert trajectoryExpert;

	private PathExpert pathExpert;

	private PathElementExpert pathElementExpert;

	private PlaceExpert placeExpert;

//	private TransitionExpert transitionExpert;

	private TransferExpert transferExpert;

	private RoleExpert roleExpert;

	private AgentExpert agentExpert;

	private BeliefExpert beliefExpert;

	private ExpectationExpert expectationExpert;

	private PersuationExpert persuationExpert;

	private IntentionExpert intentionExpert;

	private SocialExpert socialExpert;

	private ThreadExpert threadExpert;

	private MoodExpert moodExpert;

	private PropertyExpert propertyExpert;

	private PersonalityExpert personalityExpert;

	private JobExpert jobExpert;

	private PartExpert partExpert;

	private PossessionExpert possessionExpert;

	private ComparisonExpert comparisonExpert;

	private QuestionExpert questionExpert;

	private ProblemSolver problemSolver;

	private ProblemSolver problemSolverHelper;

	private WhatIfExpert whatIfExpert;

	private StoryExpert storyExpert;

	private CommandExpert commandExpert;

	private GoalExpert goalExpert;

	private DescribeExpert describeExpert;

	private StateExpert stateExpert;

	private static AnaphoraExpert anaphoraExpert;

	private IdiomExpert idiomExpert;

	private StoryRecallViewer storyRecallViewer1;

	private StoryRecallViewer storyRecallViewer2;

	private StoryRecallViewer storyWordRecallViewer1;

	private StoryRecallViewer storyWordRecallViewer2;

	private IdiomSplitter idiomSplitter;

	private Combinator combinator;

	private WiredDistributorBox inputDistributor;

	private OnsetViewer onsetViewer1;

	private OnsetViewer onsetViewer2;

	// private JPanel ruleViewer1Wrapper;

	// private JPanel ruleViewer2Wrapper;

	// private RuleViewer ruleViewer1;

	// private RuleViewer ruleViewer2;

	// private ElaborationViewer elaborationViewer2;

	private TextViewer briefingPanel;

	private JPanel instantiatedRuleViewer1Wrapper;

	private JPanel instantiatedRuleViewer2Wrapper;

	private RuleViewer instRuleViewer1;

	private RuleViewer instRuleViewer2;

	private TabbedTextViewer sourceContainer;

	private TabbedTextViewer resultContainer;

	private TabbedTextViewer introspectionContainer;

	private TabbedTextViewer explanationContainer;

	private static TabbedTextViewer commentaryContainer;

	private TabbedTextViewer summaryContainer;

	private TabbedTextViewer retellingContainer;

	private TabbedTextViewer storyContainer;

	private TabbedTextViewer scopeContainer;

	private TabbedTextViewer conceptContainer;

	protected WindowGroupManager windowGroupManager;

	private WindowGroupHost leftPanel;

	private WindowGroupHost rightPanel;

	private WindowGroupHost bottomPanel;

	private EntityMemory thingMemory;

	private WiredSplitPane elaborationPanel;

	private WiredSplitPane inspectorPanel;

	private WiredSplitPane reflectionPanel;

	private WiredSplitPane reflectionOnsetPanel;

	private WiredSplitPane onsetPanel;

	private WiredSplitPane ruleViewerPanel;

	private WiredSplitPane instantiatedRuleViewerPanel;

	private WiredSplitPane recallPanel;

	private WiredSplitPane wordRecallPanel;

	static final String ARROW = "Arrow port";

	public static final String CLOSE = "Close";

	// Ports
	static final String MAGIC = "Magic port";

	public static final String OPEN = "Open";

	public static int RED = 0, YELLOW = 1, GREEN = 2;

	static final String STUB = "Stub port";

	static final String VIEW = "Viewer port";

	protected JMenu aboutMenu = new JMenu("About");

	protected JMenu readFileAgainItem = new JMenu("Read file again");

	private BackgroundWiredBox backgroundMemoryBox;

	protected BensGauntletComponent benji;

	private BlinkingBoxPanel blinkingBoxPanel;

	private JPanel buttonPanel;

	private WiredBlinkingBox blockProbeBlinkingBox;

	private BlockViewer blockViewer;

	private WiredBlinkingBox causeProbeBlinkingBox;

	private NewFrameViewer closestThingViewer;

	private WiredBlinkingBox coercionBlinker;

	private WiredBlinkingBox controlProbeBlinkingBox; // private

	private DictionaryPage dictionary;

	private JLabel jenniferPanel;

	private PageHowToLearner howToLearnerPage;

	private PageNoviceLearner noviceLearnerPage;

	private PageStoryLearner storyLearnerPage;

	private RecipeExpert recipeExpert;

	private PageWordNetGUI wordNetGui;

	private PageTranslatorGenerator translatorGenerator;

	private Disambiguator2 disambiguator;

	private Disambiguator3 linkDisambiguator;

	private Disambiguator3 startDisambiguator;

	private Disambiguator newDisambiguator;

	private DisambiguatorViewer disambiguatorViewer;

	private NewFrameViewer everythingViewer;

	private Distributor expertDistributionBox;

	protected JMenu fileMenu = new JMenu("File");

	protected StateMaintainer stateMaintainer;

	private FileReaderPanel fileReaderFrame;

	private ForceInterpreter coerceInterpreter;

	private ForceInterpreter causeInterpreter;

	private WiredBlinkingBox forceProbeBlinkingBox;

	private ForceViewer forceViewer;

	private WiredBlinkingBox geometryProbeBlinkingBox;

	private GeometryViewer geometryViewer;

	private HardWiredTranslator hardWiredTranslator;

	private BasicTranslator basicTranslator;

	private ExperimentalParserTap experimentalParserTap;

	// private Gate imaginationGate;

	private WiredBlinkingBox imagineBlinker;

	private JTabbedPane inputTabbedPane;

	private SimpleGenerator internalToEnglishTranslator;

	private EventKnowledgeViewer knowledgeWatcher;

	private WiredBlinkingBox knowledgeWatcherBlinker;

	// private Understand learningTranslator;

	// private LinkParser linkParser;

	private Start startParser;

	private NewFrameViewer linkViewer;

	private StoryViewer startViewer;

	// private Gate memoryGate;

	private M2Viewer m2Viewer;

	private ChainViewer chainViewer;

	private PredictionsViewer predViewer;

	protected JMenuBar menuBar;

	protected MovieManager movieManager;

	// private ImagePanel lightBulbViewer;

	private MovieViewerExternal externalMovieViewer;

	private JSplitPane northSouthSplitPane;

	private JPanel topPanel;

	private JTabbedPane outputTabbedPane;

	private DistributionBox parserDistributionBox;

	private WiredBlinkingBox parserProbeBlinkingBox;

	private WiredBlinkingBox pathElementBlinkder;

	private WiredBlinkingBox pathProbeBlinkingBox;

	private NewFrameViewer pathViewer;

	private PictureFinder pictureFinder;

	private WiredBlinkingBox pictureProbeBlinkingBox;

	private PictureViewer pictureViewer;

	private WiredBlinkingBox placeBlinker;

	private TalkingFrameViewer predictionViewer;

	private QueuingWiredBox queuingPictureBox;

//	private RachelsPictureExpert rachelsPictureExpert;

	private WiredBlinkingBox roleBlinker;

	private WiredBlinkingBox pictureBlinker;

	private WiredBlinkingBox beliefBlinker;

	private WiredBlinkingBox predictionBlinker;

	private WiredBlinkingBox persuationBlinker;

	private WiredBlinkingBox intentionBlinker;

	private WiredBlinkingBox socialBlinker;

	SimpleGenerator simpleGenerator;

	SimpleGenerator descriptionGenerator;

	private SomTrajectoryBox somTester;

	private JSplitPane splitPane;

	private TalkBackViewer talkBackViewer;

	protected Talker talker;

	private TextEntryBox textEntryBox;

	private WiredBlinkingBox threadBlinker;

	private WiredBlinkingBox moodBlinker;

	private WiredBlinkingBox jobBlinker;

	private WiredBlinkingBox agentBlinker;

	private WiredBlinkingBox partBlinker;

	private WiredBlinkingBox possessionBlinker;

	private WiredBlinkingBox propertyBlinker;

	private WiredBlinkingBox personalityBlinker;

	private WiredBlinkingBox comparisonBlinker;

	private WiredBlinkingBox timeBlinker;

	// Sila
	private StoryTeller storyTeller;

	private SummaryHelper summaryHelper;

	private StoryEnvironment storyEnvironment;

	private InternalNarrator internalNarrator;

	private StaticAudienceModeler staticAudienceModeler;

	private GoalSpecifier goalSpecifier;

	private GoalTracker goalTracker;

	private StoryPreSimulator storyPresimulator;

	private StorySimulator storySimulator;

	private StoryModifier storyModifier;

	private StoryPublisher storyPublisher;

	private ModelEvaluator modelEvaluator;

	// added by zhutian on 23 Mar 2019
	private PageStoryAligner storyAlignerPage;

	private StoryAligner storyAligner;

	// Caryn
//	private SimilarityProcessor similarityProcessor;
//	private SimilarityViewer similarityViewer;

	TrafficLight trafficLight;

	JPanel trafficLightPanel;

	private WiredBlinkingBox trajectoryBlinker;

	private WiredBlinkingBox transitionBlinker;

	private WiredBlinkingBox goalBlinker;

	private WiredBlinkingBox transferBlinker;

	// private ParseTreeViewer treePanel;

	private UnderstandProcessor understandProcessor;

	protected TabbedTextAdapter remarksAdapter;

	protected TabbedTextAdapter mysteryAdapter;

	protected TabbedTextAdapter startProcessingViewer;

	protected TabbedTextAdapter conceptAdapter;

	protected static final String IDIOM = "idiom";

	protected static final String SET_RIGHT_PANEL_TO_RESULTS = "set right panel to results";

	protected static final String SET_BOTTOM_PANEL_TO_IMAGINATION = "set bottom panel to imagination";

	protected static final String SET_LEFT_PANEL_TO_ONSET = "set left panel to onset";

	// private MindsEyeMovieViewer mindsEyeMovieViewer;

	// private MindsEyeMoviePlayer mindsEyeMoviePlayer;


	private NameLabel storyNameLabel1;

	private NameLabel storyNameLabel2;

	private JMenuItem currentMenuItem;

	// Private stuff

	private JPanel personalGuiPanel;

	private JPanel personalButtonPanel;

	private File selectedFile;

	// The one-off constructors

	// Hiba Causal viewer
	private TextViewer causalTextView;

	private JessicasDisplay jessicasDisplay;

	private HumorDisplay humorDisplay;

	private JComponent iViewer1;

	private static JFXPanel characterVisualizer;

	// private VisionCommunicator visionCommunicator;

	private JPanel wiringDiagram;

	private JPanel plotDiagram1;

	private JPanel plotDiagram2;

	// memory gui circles
	private RepViewer repBlockViewer;

	private RepViewer repCauseViewer;

	private RepViewer repCoerceViewer;

	private RepViewer repForceViewer;

	private RepViewer repGeometryViewer;

	private RepViewer repPathElementViewer;

	private RepViewer repPathViewer;

	private RepViewer repPlaceViewer;

	private RepViewer repRoleViewer;

	private RepViewer repActionViewer;

	private RepViewer repBeliefViewer;

	private RepViewer repSocialViewer;

	private RepViewer repMoodViewer;

	private RepViewer repJobViewer;

	private RepViewer repPropertyViewer;

	private RepViewer repComparisonViewer;

	private RepViewer repTimeViewer;

	private RepViewer repTrajectoryViewer;

	private RepViewer repTransitionViewer;

	private RepViewer repTransferViewer;

	private TextBox textBox;

	private static MentalModel mentalModel1;

	private static MentalModel mentalModel2;

	private StoryProcessor storyProcessorSimulation; // Sila

	private StoryViewer storyViewer1;

	private StoryViewer storyViewer2;

	private StoryViewer storyViewerForGenesisTesting;

	// private StoryViewer instantiatedConceptViewer1;

	// private StoryViewer instantiatedConceptViewer2;

	private StoryViewer conceptViewer1;

	private StoryViewer conceptViewer2;

	public Color greenish = new Color(153, 255, 51);

	private ConceptBar instantiatedRuleConceptBar1;

	private ConceptBar instantiatedRuleConceptBar2;

	private ConceptBar ruleConceptBar1;

	private ConceptBar ruleConceptBar2;

	// private GapFiller gapFiller;
	//
	// private GapViewer gapViewer;

	private AlignmentViewer alignmentViewer;

	private CharacterViewer characterViewer;

	private TraitViewer traitViewer;

	// New gui for 2011

	private MasterPanel masterPanel;

	private StandardPanel standardPanel;

	private static final AspireEngine aspireEngine = new AspireEngine();


	// Suggestions

	String luStory = "Try asking:<br/>" + "Did Lu kill (Shan or Goertz or an associate professor or himself) because:" + "<ol>" + "<li>Lu is insane"
	        + "<li>America is individualistic" + "<li>American media glorifies violence" + "<li>Goertz fails to help Lu</ol>";

	String mcIlvaneStory = "Try asking:<br/>" + "Did McIlvane kill (supervisor or himself) because:" + "<ol>" + "<li>McIlvane is insane"
	        + "<li>America is individualistic" + "<li>American media glorifies violence" + "<li>supervisor fails to help McIlvane</ol>";


	// -----------------------------
	//
	// 		II : All the get methods for viewers
	//
	// -----------------------------

	public static AspireEngine getAspireEngine() {
        return aspireEngine;
    }

	public TabbedTextAdapter getRemarksAdapter() {
		if (remarksAdapter == null) {
			remarksAdapter = new TabbedTextAdapter("Remarks", getResultContainer());
		}
		return remarksAdapter;
	}

	public TabbedTextAdapter getStartProcessingViewer() {
		if (startProcessingViewer == null) {
			startProcessingViewer = new TabbedTextAdapter("Start result", getScopeContainer());
		}
		return startProcessingViewer;
	}

	// added by Hiba:
	public TextViewer getCausalTextView() {
		if (causalTextView == null) {
			causalTextView = new TextViewer();
			causalTextView.setName("Causal view");
		}
		return causalTextView;
	}

	public JessicasDisplay getJessicasDisplay() {
		if (jessicasDisplay == null) {
			jessicasDisplay = new JessicasDisplay();
		}
		return jessicasDisplay;
	}

	public HumorDisplay getHumorDisplay() {
		if (humorDisplay == null) {
			humorDisplay = new HumorDisplay();
		}
		return humorDisplay;
	}

	// public Arrow getArrow() {
	// return getBlinkingBoxPanel().getArrow();
	// }

	public BackgroundWiredBox getBackgroundMemoryBox() {
		if (backgroundMemoryBox == null) {
			backgroundMemoryBox = new BackgroundWiredBox();
		}
		return backgroundMemoryBox;
	}

	public BensGauntletComponent getBensComponent() {
		if (benji == null) {
			benji = new BensGauntletComponent();
		}
		return benji;
	}

	public BlinkingBoxPanel getExpertsPanel() {
		if (blinkingBoxPanel == null) {
			blinkingBoxPanel = new BlinkingBoxPanel();
			blinkingBoxPanel.setName("Experts");

		}
		return blinkingBoxPanel;
	}

	public WiredBlinkingBox getBlockProbeBlinkingBox() {
		if (blockProbeBlinkingBox == null) {
			blockProbeBlinkingBox = new WiredBlinkingBox();
			blockProbeBlinkingBox.setTitle("Blockage");
			// blockProbeBlinkingBox.setMemory(getSomBlockViewer());
			blockProbeBlinkingBox.setMemory(getRepBlockViewer());
			blockProbeBlinkingBox.setGraphic(getBocksViewer());
			blockProbeBlinkingBox.setName("Blockage  blinker");
			getExpertsPanel().add(blockProbeBlinkingBox);
		}
		return blockProbeBlinkingBox;
	}

	public BlockViewer getBocksViewer() {
		if (blockViewer == null) {
			blockViewer = new BlockViewer();
		}
		return blockViewer;
	}

	NewFrameViewer getClosestThingViewer() {
		if (closestThingViewer == null) {
			closestThingViewer = new NewFrameViewer();

			closestThingViewer.setOpaque(false);
			closestThingViewer.setName("Distance expert");
		}
		return closestThingViewer;
	}

	public ForceInterpreter getCoerceInterpreter() {
		if (coerceInterpreter == null) {
			coerceInterpreter = new ForceInterpreter();
			coerceInterpreter.setName("Coerce intepreter");
		}
		return coerceInterpreter;
	}

	// public ConceptPanel getConceptPanel() {
	// if (conceptPanel == null) {
	// conceptPanel = new ConceptPanel();
	// conceptPanel.setPreferredSize(new Dimension(300, 300));
	// conceptPanel.setName("Concept viewer");
	// }
	// return conceptPanel;
	// }

	// public WiredOnOffSwitch getConceptSwitch() {
	// if (conceptSwitch == null) {
	// conceptSwitch = new WiredOnOffSwitch("Use concepts");
	// }
	// return conceptSwitch;
	// }
	//
	//
	//
	// public WiredOnOffSwitch getDisambiguatorSwitch() {
	// if (disambiguatorSwitch == null) {
	// disambiguatorSwitch = new WiredToggleSwitch("Use disambiguator");
	// }
	// return disambiguatorSwitch;
	// }

	// public ConceptMemoryPanel getConceptMemoryPanel(){
	// if (conceptMemoryPanel == null){
	// conceptMemoryPanel = new ConceptMemoryPanel();
	// conceptMemoryPanel.setOpaque(false);
	// conceptMemoryPanel.setName("Concept Memory Viewer");
	// }
	// return conceptMemoryPanel;
	// }
	//
	// public ConceptDescriber getConceptDescriber() {
	// if (conceptDescriber == null) {
	// conceptDescriber = new ConceptDescriber();
	// conceptDescriber.setName("Concept Describer");
	// }
	// return conceptDescriber;
	// }
	//
	// public ConceptParser getConceptParser() {
	// if (conceptParser == null) {
	// conceptParser = new ConceptParser();
	// conceptParser.setName("Concept Parser");
	// }
	// return conceptParser;
	// }
	//
	// public ConceptManager getConceptManager(){
	// if (conceptManager == null){
	// conceptManager = new ConceptManager();
	// conceptManager.setName("Concept Manager");
	// }
	// return conceptManager;
	// }

	public WiredBlinkingBox getControlProbeBlinkingBox() {
		if (controlProbeBlinkingBox == null) {
			controlProbeBlinkingBox = new WiredBlinkingBox();
			controlProbeBlinkingBox.setTitle("Control");
			// controlProbeBlinkingBox.add(getControlViewer());
			controlProbeBlinkingBox.setName("Control blinker");
			getExpertsPanel().add(controlProbeBlinkingBox);
		}
		return trajectoryBlinker;
	}

	public UnderstandProcessor getDeriver() {
		if (understandProcessor == null) {
			understandProcessor = new UnderstandProcessor();
			understandProcessor.setName("Deriver");
		}
		return understandProcessor;
	}

	public DictionaryPage getDictionary() {
		if (dictionary == null) {
			dictionary = new DictionaryPage();
			dictionary.setName("Dictionary");
		}
		return dictionary;
	}

	public JLabel getJenniferPanel() {
		if (jenniferPanel == null) {
			jenniferPanel = new JLabel("Hello world");
			jenniferPanel.setName("Jennifer");
		}
		return jenniferPanel;
	}

	public PageHowToLearner getPageHowToBookLearner() {
		if (howToLearnerPage == null) {
			howToLearnerPage = new PageHowToLearner();
			howToLearnerPage.setName("Learn PS from HowTo Books");
		}
		return howToLearnerPage;
	}

	public PageNoviceLearner getPageNoviceLearner() {
		if (noviceLearnerPage == null) {
			noviceLearnerPage = new PageNoviceLearner();
			noviceLearnerPage.setName("Learn PS from Conversations");
		}
		return noviceLearnerPage;
	}

	public PageStoryLearner getPageStoryLearner() {
		if (storyLearnerPage == null) {
			storyLearnerPage = new PageStoryLearner();
			storyLearnerPage.setName("Learn PS from Stories");
		}
		return storyLearnerPage;
	}

	public RecipeExpert getRecipeExpert() {
		if (recipeExpert == null) {
			recipeExpert = new RecipeExpert();
		}
		return recipeExpert;
	}

	public PageWordNetGUI getWordNetGUI() {
		if (wordNetGui == null) {
			wordNetGui = new PageWordNetGUI();
			wordNetGui.setName("WordNetGUI");
		}
		return wordNetGui;
	}

	public PageTranslatorGenerator getPageTranslatorGenerator() {
		if (translatorGenerator == null) {
			translatorGenerator = new PageTranslatorGenerator();
			translatorGenerator.setName("Translator/Generator");
		}
		return translatorGenerator;
	}


	public Disambiguator2 getDisambiguator() {
		if (disambiguator == null) {
			disambiguator = new Disambiguator2(Switch.disambiguatorSwitch);
		}
		return disambiguator;
	}

	public Disambiguator3 getLinkDisambiguator() {
		if (linkDisambiguator == null) {
			linkDisambiguator = new Disambiguator3();
			linkDisambiguator.setName("Link disambiguator");
		}
		return linkDisambiguator;
	}

	public Disambiguator3 getStartDisambiguator() {
		if (startDisambiguator == null) {
			startDisambiguator = new Disambiguator3(Switch.disambiguatorSwitch);
			startDisambiguator.setName("Start disambiguator");
		}
		return startDisambiguator;
	}

	public Disambiguator getNewDisambiguator() {
		if (newDisambiguator == null) {
			newDisambiguator = new Disambiguator();
		}
		return newDisambiguator;
	}

	DisambiguatorViewer getDisambiguatorViewer() {
		if (disambiguatorViewer == null) {
			disambiguatorViewer = new DisambiguatorViewer();

			disambiguatorViewer.setOpaque(false);
		}
		return disambiguatorViewer;
	}

	public Distributor getDistributionBox() {
		if (expertDistributionBox == null) {
			expertDistributionBox = new Distributor();
			expertDistributionBox.setName("Distributor");
		}
		return expertDistributionBox;
	}

	public FileReaderPanel getFileReaderFrame() {
		if (fileReaderFrame == null) {
			fileReaderFrame = new FileReaderPanel();
		}
		return fileReaderFrame;
	}

	public ForceInterpreter getCauseInterpreter() {
		if (causeInterpreter == null) {
			causeInterpreter = new ForceInterpreter();
			causeInterpreter.setName("Cause intepreter");
		}
		return causeInterpreter;
	}

	public WiredBlinkingBox getGeometryProbeBlinkingBox(GeometryViewer viewer) {
		if (geometryProbeBlinkingBox == null) {
			geometryProbeBlinkingBox = new WiredBlinkingBox(viewer);
			geometryProbeBlinkingBox.setTitle("Geometry");
			// geometryProbeBlinkingBox.setMemory(getSomGeometryViewer());
			geometryProbeBlinkingBox.setMemory(getRepGeometryViewer());
			geometryProbeBlinkingBox.setGraphic(getGeometryViewer());
			geometryProbeBlinkingBox.setName("Geometry blinker");
			getExpertsPanel().add(geometryProbeBlinkingBox);
		}
		return geometryProbeBlinkingBox;
	}

	public GeometryViewer getGeometryViewer() {
		if (geometryViewer == null) {
			geometryViewer = new GeometryViewer();
		}
		return geometryViewer;
	}

	public HardWiredTranslator getHardWiredTranslator() {
		if (hardWiredTranslator == null) {
			hardWiredTranslator = new HardWiredTranslator(this);
			hardWiredTranslator.setName("Static translator");
		}
		return hardWiredTranslator;
	}

	ParserTranslator parserTranslator;



	public ParserTranslator getParserTranslator() {
		if (parserTranslator == null) {
			parserTranslator = new ParserTranslator("ParserTranslator");
		}
		return parserTranslator;
	}

	public BasicTranslator getNewSemanticTranslator() {
		if (basicTranslator == null) {
			basicTranslator = new BasicTranslator(this);
			basicTranslator.setName("Semantic translator");
		}
		return basicTranslator;
	}


	public ExperimentalParserTap getExperimentalParserTap() {
		if (experimentalParserTap == null) {
			experimentalParserTap = new ExperimentalParserTap();
			experimentalParserTap.setName("Experimental Parser Tap");
		}
		return experimentalParserTap;
	}

	private JComponent getWiringDiagram() {
		if (wiringDiagram == null) {
			wiringDiagram = new JPanel();
			wiringDiagram.setLayout(new BorderLayout());
			wiringDiagram.setName("Wiring diagram");
			Adapter adapter = Adapter.makeConnectionAdapter();
			// wiringDiagram.add(new JScrollPane(adapter.getViewer()),
			// BorderLayout.CENTER);
			wiringDiagram.add(adapter.getViewer(), BorderLayout.CENTER);
			wiringDiagram.add(adapter.getViewer().getSlider(), BorderLayout.NORTH);
		}
		return wiringDiagram;
	}


	public WiredSplitPane getElaborationPanel() {
		if (elaborationPanel == null) {
			elaborationPanel = new WiredSplitPane(getMentalModel1().getViewerWrapper(), getMentalModel2().getViewerWrapper());
			elaborationPanel.setName("Elaboration graph");
		}
		return elaborationPanel;
	}

	// public WiredSplitPane getConceptsViewerPanel() {
	// if (reflectionOnsetPanel == null) {
	// reflectionOnsetPanel = new WiredSplitPane(getConceptViewer1(), getConceptViewer2());
	// reflectionOnsetPanel.setName("Concepts");
	// }
	// return reflectionOnsetPanel;
	// }

	public WiredSplitPane getOnsetPanel() {
		if (onsetPanel == null) {
			onsetPanel = new WiredSplitPane(getOnsetViewer1(), getOnsetViewer2());
			onsetPanel.setName("Onsets");
		}
		return onsetPanel;
	}

	public WiredSplitPane getRecallPanel() {
		if (recallPanel == null) {
			recallPanel = new WiredSplitPane(getStoryRecallViewer1(), getStoryRecallViewer2());
			recallPanel.setName("Precedent concept recall");
		}
		return recallPanel;
	}

	public WiredSplitPane getWordRecallPanel() {
		if (wordRecallPanel == null) {
			wordRecallPanel = new WiredSplitPane(getStoryWordRecallViewer1(), getStoryWordRecallViewer2());
			wordRecallPanel.setName("Precedent word Recall");
		}
		return wordRecallPanel;
	}

	protected NameLabel getStoryName2() {
		if (storyNameLabel2 == null) {
			storyNameLabel2 = new NameLabel("h1", "center");
		}
		return storyNameLabel2;
	}

	public ConceptBar getInstantiatedRuleConceptBar1() {
		if (instantiatedRuleConceptBar1 == null) {
			instantiatedRuleConceptBar1 = new ConceptBar();
			instantiatedRuleConceptBar1.setName("Concepts");
			instantiatedRuleConceptBar1.setOpaque(true);
			instantiatedRuleConceptBar1.setPreferredSize(new Dimension(500, 20));
		}
		return instantiatedRuleConceptBar1;
	}

	public ConceptBar getInstantiatedRuleConceptBar2() {
		if (instantiatedRuleConceptBar2 == null) {
			instantiatedRuleConceptBar2 = new ConceptBar();
			instantiatedRuleConceptBar2.setName("Concepts");
			instantiatedRuleConceptBar2.setOpaque(true);
			instantiatedRuleConceptBar2.setPreferredSize(new Dimension(500, 20));
		}
		return instantiatedRuleConceptBar2;
	}

	public ConceptBar getRuleConceptBar1() {
		if (ruleConceptBar1 == null) {
			ruleConceptBar1 = new ConceptBar();
			ruleConceptBar1.setName("Concepts");
			ruleConceptBar1.setOpaque(true);
			ruleConceptBar1.setPreferredSize(new Dimension(500, 20));
		}
		return ruleConceptBar1;
	}

	public ConceptBar getRuleConceptBar2() {
		if (ruleConceptBar2 == null) {
			ruleConceptBar2 = new ConceptBar();
			ruleConceptBar2.setName("Concepts");
			ruleConceptBar2.setOpaque(true);
			ruleConceptBar2.setPreferredSize(new Dimension(500, 20));
		}
		return ruleConceptBar2;
	}

	public SimpleGenerator getInternalToEnglishTranslator() {
		if (internalToEnglishTranslator == null) {
			internalToEnglishTranslator = new SimpleGenerator();
			internalToEnglishTranslator.setName("Internal generator");
		}
		return internalToEnglishTranslator;
	}

	public EventKnowledgeViewer getKnowledgeWatcher() {
		if (knowledgeWatcher == null) {
			knowledgeWatcher = new EventKnowledgeViewer();
			knowledgeWatcher.setName("Event viewer");
		}
		return knowledgeWatcher;
	}

	public WiredBlinkingBox getKnowledgeWatcherBlinker() {
		if (knowledgeWatcherBlinker == null) {
			knowledgeWatcherBlinker = new WiredBlinkingBox(getKnowledgeWatcher());
			knowledgeWatcherBlinker.setGraphic(getKnowledgeWatcher());
			knowledgeWatcherBlinker.setName("Knowledge blinker");
		}
		return knowledgeWatcherBlinker;
	}

	// public Understand getLearningTranslator() {
	// if (learningTranslator == null) {
	// System.out.println("Calling Understand loader");
	// URL url = Understand.class.getResource("rules.ser.gz");
	// System.out.println("Loading rules from serialized file " + url);
	// learningTranslator = Understand.load(url);
	// learningTranslator.setName("Learning translator");
	// }
	// return learningTranslator;
	// }

	// public LinkParser getLinkParser() {
	// if (linkParser == null) {
	// linkParser = new LinkParser();
	// linkParser.setName("Link parser");
	// }
	// return linkParser;
	// }

	public Start getStartParser() {
		if (startParser == null) {
			startParser = Start.getStart();
			startParser.setName("Start parser");
		}
		return startParser;
	}

	NewFrameViewer getLinkViewer() {
		if (linkViewer == null) {
			linkViewer = new NewFrameViewer();
			linkViewer.setOpaque(false);
			linkViewer.setName("Link viewer");
		}
		return linkViewer;
	}

	public StoryViewer getStartViewer() {
		if (startViewer == null) {
			startViewer = new StoryViewer();
			startViewer.setOpaque(false);
			startViewer.setName("Start viewer");
		}
		return startViewer;
	}

	public Memory getMemory() {
		return Memory.getMemory();
	}

	public Mem getM2() {
		return M2.getMem();
	}

	public M2Viewer getM2Viewer() {
		if (m2Viewer == null) {
			m2Viewer = new M2Viewer();
			m2Viewer.setOpaque(false);
			m2Viewer.setName("New memory viewer");
		}
		return m2Viewer;
	}

	public ChainViewer getChainViewer() {
		if (chainViewer == null) {
			chainViewer = new ChainViewer();
			chainViewer.setOpaque(false);
			chainViewer.setName("Chain viewer");
		}
		return chainViewer;
	}

	public PredictionsViewer getPredictionsViewer() {
		if (predViewer == null) {
			predViewer = new PredictionsViewer();
			predViewer.setOpaque(false);
			predViewer.setName("Prediction viewer");
		}
		return predViewer;
	}

	public ArrayList<MovieDescription> getMovieDescriptions() {
		return getMovieManager().getMovieDescriptions();
	}

	public MovieManager getMovieManager() {
		if (movieManager == null) {
			movieManager = new MovieManager(this);
		}
		return movieManager;
	}

	public MovieViewerExternal getExternalMovieViewer() {
		if (externalMovieViewer == null) {
			externalMovieViewer = new MovieViewerExternal();
		}
		return externalMovieViewer;
	}

	public WindowGroupHost getLeftPanel() {
		if (leftPanel == null) {
			String panelContent = Preferences.userRoot().get(GenesisConstants.LEFT, "Controls");
			leftPanel = getWindowGroupManager().getHost(panelContent);
			leftPanel.setName(GenesisConstants.LEFT);
		}
		return leftPanel;
	}

	public WindowGroupHost getRightPanel() {
		if (rightPanel == null) {
			String panelContent = Preferences.userRoot().get(GenesisConstants.RIGHT, "Elaboration graph");
			rightPanel = getWindowGroupManager().getHost(panelContent);
			rightPanel.setName(GenesisConstants.RIGHT);
		}
		return rightPanel;
	}

	public WindowGroupHost getBottomPanel() {
		if (bottomPanel == null) {
			String panelContent = Preferences.userRoot().get(GenesisConstants.BOTTOM, "Experts");
			bottomPanel = getWindowGroupManager().getHost(panelContent);
			bottomPanel.setName(GenesisConstants.BOTTOM);

		}
		return bottomPanel;
	}

	public Talker getTalker() {
		if (talker == null) {
			try {
				talker = new Talker(Switch.useSpeechCheckBox);
				talker.setName("Talker");
				// talker.setTabs(this);
			}
			catch (Exception e) {
				System.out.println("Failed to construct talker");
				e.printStackTrace();
			}
		}
		return talker;
	}

	public DistributionBox getParserDistributionBox() {
		if (parserDistributionBox == null) {
			parserDistributionBox = new DistributionBox(this);
			parserDistributionBox.setName("Parser distributor");
		}
		return parserDistributionBox;
	}

	// public ParseTreeViewer getParseTreeViewer() {
	// if (treePanel == null) {
	// treePanel = new ParseTreeViewer();
	// treePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	// treePanel.setName("Parse viewer");
	// }
	// return treePanel;
	// }

	public WiredBlinkingBox getPathProbeBlinkingBox() {
		if (pathProbeBlinkingBox == null) {
			pathProbeBlinkingBox = new WiredBlinkingBox(getPathViewer());
			pathProbeBlinkingBox.setTitle("Path");
			// pathProbeBlinkingBox.setMemory(getSomPathViewer());
			pathProbeBlinkingBox.setMemory(getRepPathViewer());
			pathProbeBlinkingBox.setGraphic(getPathViewer());
			pathProbeBlinkingBox.setName("Path blinker");
			getExpertsPanel().add(pathProbeBlinkingBox);
		}
		return pathProbeBlinkingBox;
	}

	public NewFrameViewer getPathViewer() {
		if (pathViewer == null) {
			pathViewer = new NewFrameViewer();
			pathViewer.setName("Path viewer");

		}
		return pathViewer;
	}

	public PictureFinder getPictureFinder() {
		if (pictureFinder == null) {
			pictureFinder = new PictureFinder();
			pictureFinder.setName("Picture finder");
		}
		return pictureFinder;
	}

	public PictureViewer getPictureViewer() {
		if (pictureViewer == null) {
			pictureViewer = new PictureViewer();
			pictureViewer.setName("Picture viewer");
			pictureViewer.setPreferredSize(new Dimension(200, 400));

			TitledBorder border = BorderFactory.createTitledBorder("Image");
			// Bug, see other use
			// border.setTitleFont(new Font(font.getName(), font.getStyle(),
			// font.getSize() * 2));
			Font newFont = new Font("Serif", Font.PLAIN, 10);
			border.setTitleFont(newFont);
			pictureViewer.setBorder(border);
			pictureViewer.setYOffset(25);
			getExpertsPanel().add(pictureViewer);
		}
		return pictureViewer;
	}

	public TalkingFrameViewer getPredictionViewer() {
		if (predictionViewer == null) {
			predictionViewer = new TalkingFrameViewer(this);
			predictionViewer.setOpaque(false);
			predictionViewer.setName("Prediction talker");
		}
		return predictionViewer;
	}

	public QueuingWiredBox getQueuingPictureBox() {
		if (queuingPictureBox == null) {
			queuingPictureBox = new QueuingWiredBox(500, 50);
			queuingPictureBox.setName("Picture queue");
		}
		return queuingPictureBox;
	}

//	public RachelsPictureExpert getRachelsPictureExpert() {
//		if (rachelsPictureExpert == null) {
//			rachelsPictureExpert = new RachelsPictureExpert();
//			rachelsPictureExpert.setName("Picture Expert");
//		}
//		return rachelsPictureExpert;
//	}

	public SimpleGenerator getSimpleGenerator() {
		if (simpleGenerator == null) {
			simpleGenerator = new SimpleGenerator();
			simpleGenerator.setName("Generator");
		}
		return simpleGenerator;
	}

	public SimpleGenerator getDescriptionGenerator() {
		if (descriptionGenerator == null) {
			descriptionGenerator = new SimpleGenerator();
		}
		return descriptionGenerator;
	}

	public SomTrajectoryBox getSomTestor() {
		if (somTester == null) {
			somTester = new SomTrajectoryBox();
			somTester.setName("Cause memory");
		}
		return somTester;
	}

	public RepViewer getRepBlockViewer() {
		if (repBlockViewer == null) {
			repBlockViewer = new RepViewer();
			repBlockViewer.setName("Blocker memory");
		}
		return repBlockViewer;
	}

	public RepViewer getRepCauseViewer() {
		if (repCauseViewer == null) {
			repCauseViewer = new RepViewer();
			repCauseViewer.setName("Cause memory");
		}
		return repCauseViewer;
	}

	public RepViewer getRepCoerceViewer() {
		if (repCoerceViewer == null) {
			repCoerceViewer = new RepViewer();
			repCoerceViewer.setName("Coerce memory");
		}
		return repCoerceViewer;
	}

	public RepViewer getRepForceViewer() {
		if (repForceViewer == null) {
			repForceViewer = new RepViewer();
			repForceViewer.setName("Force memory");
		}
		return repForceViewer;
	}

	public RepViewer getRepGeometryViewer() {
		if (repGeometryViewer == null) {
			repGeometryViewer = new RepViewer();
			repGeometryViewer.setName("Geometry viewer");
		}
		return repGeometryViewer;
	}

	public RepViewer getRepPathElementViewer() {
		if (repPathElementViewer == null) {
			repPathElementViewer = new RepViewer();
			repPathElementViewer.setName("Path element memory");
		}
		return repPathElementViewer;
	}

	public RepViewer getRepPathViewer() {
		if (repPathViewer == null) {
			repPathViewer = new RepViewer();
			repPathViewer.setName("Path memory");
		}
		return repPathViewer;
	}

	public RepViewer getRepPlaceViewer() {
		if (repPlaceViewer == null) {
			repPlaceViewer = new RepViewer();
			repPlaceViewer.setName("Place memory");
		}
		return repPlaceViewer;
	}

	public RepViewer getRepRoleViewer() {
		if (repRoleViewer == null) {
			repRoleViewer = new RepViewer();
			repRoleViewer.setName("Role memory");
		}
		return repRoleViewer;
	}

	public RepViewer getRepActionViewer() {
		if (repActionViewer == null) {
			repActionViewer = new RepViewer();
			repActionViewer.setName("Action memory");
		}
		return repActionViewer;
	}

	public RepViewer getRepBeliefViewer() {
		if (repBeliefViewer == null) {
			repBeliefViewer = new RepViewer();
			repBeliefViewer.setName("Belief memory");
		}
		return repBeliefViewer;
	}

	public RepViewer getRepSocialViewer() {
		if (repSocialViewer == null) {
			repSocialViewer = new RepViewer();
			repSocialViewer.setName("Social memory");
		}
		return repSocialViewer;
	}

	public RepViewer getRepMoodViewer() {
		if (repMoodViewer == null) {
			repMoodViewer = new RepViewer();
			repMoodViewer.setName("Mood memory");
		}
		return repMoodViewer;
	}

	public RepViewer getRepJobViewer() {
		if (repJobViewer == null) {
			repJobViewer = new RepViewer();
			repJobViewer.setName("Job memory");
		}
		return repJobViewer;
	}

	public RepViewer getRepPropertyViewer() {
		if (repPropertyViewer == null) {
			repPropertyViewer = new RepViewer();
			repPropertyViewer.setName("Property memory");
		}
		return repPropertyViewer;
	}

	public RepViewer getRepComparisonViewer() {
		if (repComparisonViewer == null) {
			repComparisonViewer = new RepViewer();
			repComparisonViewer.setName("Comparison memory");
		}
		return repComparisonViewer;
	}

	public RepViewer getRepTimeViewer() {
		if (repTimeViewer == null) {
			repTimeViewer = new RepViewer();
			repTimeViewer.setName("Time memory");
		}
		return repTimeViewer;
	}

	public RepViewer getRepTrajectoryViewer() {
		if (repTrajectoryViewer == null) {
			repTrajectoryViewer = new RepViewer();
			repTrajectoryViewer.setName("Trajectory memory");
		}
		return repTrajectoryViewer;
	}

	public RepViewer getRepTransitionViewer() {
		if (repTransitionViewer == null) {
			repTransitionViewer = new RepViewer();
			repTransitionViewer.setName("Transition memory");
		}
		return repTransitionViewer;
	}

	public RepViewer getRepTransferViewer() {
		if (repTransferViewer == null) {
			repTransferViewer = new RepViewer();
			repTransferViewer.setName("Transfer memory");
		}
		return repTransferViewer;
	}

	public TalkBackViewer getTalkBackViewer() {
		if (talkBackViewer == null) {
			talkBackViewer = new TalkBackViewer();
			talkBackViewer.setName("Play by play");
		}
		return talkBackViewer;
	}

	public TextBox getTextBox() {
		if (textBox == null) {
			textBox = new TextBox(this);
			textBox.setName("Talker text");
		}
		return textBox;
	}

	public TextEntryBox getTextEntryBox() {
		if (textEntryBox == null) {
			textEntryBox = new TextEntryBox();
			textEntryBox.setName("Text entry");
			if (!Switch.showTextEntryBox.isSelected()) {
				getTextEntryBox().zero();
			}
			else {
				getTextEntryBox().normal();
			}
			GenesisGetters.this.revalidate();
			GenesisGetters.this.getTextEntryBox().revalidate();
		}
		return textEntryBox;
	}

	public TrafficLight getTrafficLight() {
		if (trafficLight == null) {
			trafficLight = new TrafficLight();
		}
		return trafficLight;
	}

	public JPanel getTrafficLightPanel() {
		if (trafficLightPanel == null) {
			trafficLightPanel = new JPanel();
			trafficLightPanel.setOpaque(false);
			TrafficLight trafficLight = getTrafficLight();
			trafficLightPanel.add(trafficLight);
			trafficLight.setPreferredSize(new Dimension(45, 90));
			trafficLight.setName("Traffic light");
		}
		return trafficLightPanel;
	}

	// public VisionCommunicator getVisionCommunicator() {
	// if (visionCommunicator == null) {
	// visionCommunicator = new VisionCommunicator(this);
	// visionCommunicator.setName("Vision system");
	// }
	// return visionCommunicator;
	// }

	public static MentalModel getMentalModel1() {
		if (mentalModel1 == null) {
			mentalModel1 = new MentalModel(Start.LEFT);
			mentalModel1.getStoryProcessor().setAwake(true);
		}
		return mentalModel1;
	}

	public static MentalModel getMentalModel2() {
		if (mentalModel2 == null) {
			mentalModel2 = new MentalModel(Start.RIGHT);
			mentalModel2.getStoryProcessor().setAwake(true);
		}
		return mentalModel2;
	}

	// Sila
	// POSSIBLE PROBLEM HERE.
	public StoryProcessor getStoryProcessorSimulation() {
		if (storyProcessorSimulation == null) {
			storyProcessorSimulation = new StoryProcessor();
		}
		return storyProcessorSimulation;
	}

	// public StoryViewer getStoryViewer1() {
	// if (storyViewer1 == null) {
	// storyViewer1 = getMentalModel1().getStoryViewer();
	// storyViewer1.setName("First perspective viewer");
	// }
	// return storyViewer1;
	// }
	//
	// public StoryViewer getStoryViewer2() {
	// if (storyViewer2 == null) {
	// storyViewer2 = getMentalModel2().getStoryViewer();
	// storyViewer2.setName("Second perspective viewer");
	// }
	// return storyViewer2;
	// }

	public StoryViewer getStoryViewerForGenesisTesting() {
		if (storyViewerForGenesisTesting == null) {
			storyViewerForGenesisTesting = new StoryViewer();
			storyViewer2.setName("Story viewer for Genesis testing");
		}
		return storyViewerForGenesisTesting;
	}

	// public ElaborationViewer getElaborationViewer1() {
	// if (elaborationViewer1 == null) {
	// elaborationViewer1 = new ElaborationViewer();
	// }
	// return elaborationViewer1;
	// }

	public StoryRecallViewer getStoryRecallViewer1() {
		if (storyRecallViewer1 == null) {
			storyRecallViewer1 = new StoryRecallViewer();
		}
		return storyRecallViewer1;
	}

	public StoryRecallViewer getStoryRecallViewer2() {
		if (storyRecallViewer2 == null) {
			storyRecallViewer2 = new StoryRecallViewer();
		}
		return storyRecallViewer2;
	}

	public StoryRecallViewer getStoryWordRecallViewer1() {
		if (storyWordRecallViewer1 == null) {
			storyWordRecallViewer1 = new StoryRecallViewer();
		}
		return storyWordRecallViewer1;
	}

	public StoryRecallViewer getStoryWordRecallViewer2() {
		if (storyWordRecallViewer2 == null) {
			storyWordRecallViewer2 = new StoryRecallViewer();
		}
		return storyWordRecallViewer2;
	}

	public OnsetViewer getOnsetViewer1() {
		if (onsetViewer1 == null) {
			onsetViewer1 = new OnsetViewer();
		}
		return onsetViewer1;
	}

	public OnsetViewer getOnsetViewer2() {
		if (onsetViewer2 == null) {
			onsetViewer2 = new OnsetViewer();
		}
		return onsetViewer2;
	}

	// public WiredSplitPane getRuleViewerPanel() {
	// if (ruleViewerPanel == null) {
	// ruleViewerPanel = new WiredSplitPane(getRuleViewer1Wrapper(), getRuleViewer2Wrapper());
	// ruleViewerPanel.setName("Rules");
	// }
	// return ruleViewerPanel;
	// }

	// public JPanel getRuleViewer1Wrapper() {
	// if (ruleViewer1Wrapper == null) {
	// ruleViewer1Wrapper = new JPanel();
	// ruleViewer1Wrapper.setLayout(new BorderLayout());
	// ruleViewer1Wrapper.add(getRuleViewer1());
	// ruleViewer1Wrapper.add(getRuleConceptBar1(), BorderLayout.SOUTH);
	// }
	// return ruleViewer1Wrapper;
	// }
	//
	// public JPanel getRuleViewer2Wrapper() {
	// if (ruleViewer2Wrapper == null) {
	// ruleViewer2Wrapper = new JPanel();
	// ruleViewer2Wrapper.setLayout(new BorderLayout());
	// ruleViewer2Wrapper.add(getRuleViewer2());
	// ruleViewer2Wrapper.add(getRuleConceptBar2(), BorderLayout.SOUTH);
	// }
	// return ruleViewer2Wrapper;
	// }
	//
	// public RuleViewer getRuleViewer1() {
	// if (ruleViewer1 == null) {
	// ruleViewer1 = getMentalModel1().getRuleViewer();
	// ruleViewer1.setName("Rules");
	// }
	// return ruleViewer1;
	// }
	//
	// public RuleViewer getRuleViewer2() {
	// if (ruleViewer2 == null) {
	// ruleViewer2 = getMentalModel2().getRuleViewer();
	// ruleViewer2.setName("Rules");
	// }
	// return ruleViewer2;
	// }

	// public StoryViewer getConceptViewer1() {
	// if (conceptViewer1 == null) {
	// conceptViewer1 = getMentalModel1().getConceptViewer();
	// conceptViewer1.setName("Concept viewer 1");
	// }
	// return conceptViewer1;
	// }
	//
	// public StoryViewer getConceptViewer2() {
	// if (conceptViewer2 == null) {
	// conceptViewer2 = getMentalModel2().getConceptViewer();
	// conceptViewer2.setName("Concept viewer 2");
	// }
	// return conceptViewer2;
	// }

	// public WiredSplitPane getInstantiatedConceptViewerPanel() {
	// if (reflectionPanel == null) {
	// reflectionPanel = new WiredSplitPane(getMentalModel1().getInstantiatedConceptViewer(),
	// getMentalModel2().getInstantiatedConceptViewer());
	// reflectionPanel.setName("Instantiated concepts");
	// }
	// return reflectionPanel;
	// }

	// public StoryViewer getInstantiatedConceptViewer1() {
	// if (instantiatedConceptViewer1 == null) {
	// instantiatedConceptViewer1 = new StoryViewer(this);
	// instantiatedConceptViewer1.setName("Instantiated reflection viewer 1");
	// }
	// return instantiatedConceptViewer1;
	// }
	//
	// public StoryViewer getInstantiatedConceptViewer2() {
	// if (instantiatedConceptViewer2 == null) {
	// instantiatedConceptViewer2 = new StoryViewer(this);
	// instantiatedConceptViewer2.setName("Instantiated reflection viewer 2");
	// }
	// return instantiatedConceptViewer2;
	// }

	// public WiredSplitPane getInstantiatedRuleViewerPanel() {
	// if (instantiatedRuleViewerPanel == null) {
	// instantiatedRuleViewerPanel = new WiredSplitPane(getInstantiatedRuleViewer1Wrapper(),
	// getInstantiatedRuleViewer2Wrapper());
	// instantiatedRuleViewerPanel.setName("Instantiated rules");
	// }
	// return instantiatedRuleViewerPanel;
	// }

	// public JPanel getInstantiatedRuleViewer1Wrapper() {
	// if (instantiatedRuleViewer1Wrapper == null) {
	// instantiatedRuleViewer1Wrapper = new JPanel();
	// instantiatedRuleViewer1Wrapper.setLayout(new BorderLayout());
	// instantiatedRuleViewer1Wrapper.add(getMentalModel1().getInstantiatedRuleViewer());
	// instantiatedRuleViewer1Wrapper.add(getInstantiatedRuleConceptBar1(), BorderLayout.SOUTH);
	// }
	// return instantiatedRuleViewer1Wrapper;
	// }
	//
	// public JPanel getInstantiatedRuleViewer2Wrapper() {
	// if (instantiatedRuleViewer2Wrapper == null) {
	// instantiatedRuleViewer2Wrapper = new JPanel();
	// instantiatedRuleViewer2Wrapper.setLayout(new BorderLayout());
	// instantiatedRuleViewer2Wrapper.add(getMentalModel2().getInstantiatedRuleViewer());
	// instantiatedRuleViewer2Wrapper.add(getInstantiatedRuleConceptBar2(), BorderLayout.SOUTH);
	// }
	// return instantiatedRuleViewer2Wrapper;
	// }

	// public RuleViewer getInstantiatedRuleViewer1() {
	// if (instRuleViewer1 == null) {
	// instRuleViewer1 = new RuleViewer();
	// instRuleViewer1.setName("Instantiated rules 1");
	// }
	// return instRuleViewer1;
	// }
	//
	// public RuleViewer getInstantiatedRuleViewer2() {
	// if (instRuleViewer2 == null) {
	// instRuleViewer2 = new RuleViewer(this);
	// instRuleViewer2.setName("Instantiated rules 2");
	// }
	// return instRuleViewer2;
	// }

	public void openGates() {
		System.out.println("Opening gates");
		// getImaginationGate().open();
		memorySwitch.open();
	}

	public void openInterface() {
		changeState(GenesisGetters.OPEN);
	}

	public class SomTrajectoryBox extends AbstractWiredBox {
		public SomTrajectoryBox() {
			super("SOM trajectory box");
			Connections.getPorts(this).addSignalProcessor("process");
		}

		public void process(Object input) {
			List<Entity> stuff = getMemory().getBestMatches(input);
			if (input instanceof Entity && stuff.size() > 0) {
				Entity t = (Entity) input;
				for (int i = 0; i < stuff.size(); ++i) {
					System.out.println("Distance " + Distances.distance(t, stuff.get(i)));
				}
				double d = Distances.distance(t, stuff.get(0));
				// System.out.println("Transmiting " + stuff.get(0));
				// getTabbedPane().setSelectedComponent(Gauntlet.this.getClosestThingViewer());
				Connections.getPorts(this).transmit(stuff.get(0));
				if (d > 0.005) {
					// Gauntlet.this.getEverythingViewer().setBackground(Color.pink);
				}
				else {
					// Gauntlet.this.getEverythingViewer().setBackground(Color.white);
				}

			}
		}
	}

	// public JPanel getButtonPanel() {
	// if (buttonPanel == null) {
	// buttonPanel = new JPanel();
	// buttonPanel.setLayout(new GridLayout(1, 0));
	// buttonPanel.add(getNextButton());
	// buttonPanel.add(getRunButton());
	// }
	// return buttonPanel;
	// }

	public ComparisonExpert getComparisonExpert() {
		if (comparisonExpert == null) {
			comparisonExpert = new ComparisonExpert();
		}
		return comparisonExpert;
	}

	public QuestionExpert getQuestionExpert() {
		if (questionExpert == null) {
			questionExpert = new QuestionExpert();
		}
		return questionExpert;
	}

	public ProblemSolver getProblemSolver() {
		if (problemSolver == null) {
			problemSolver = getMentalModel1().getProblemSolver();
		}
		return problemSolver;
	}

	public ProblemSolver getProblemSolverHelper() {
		if (problemSolverHelper == null) {
			problemSolverHelper = getMentalModel2().getProblemSolver();
		}
		return problemSolverHelper;
	}

	public WhatIfExpert getWhatIfExpert() {
		if (whatIfExpert == null) {
			whatIfExpert = new WhatIfExpert();
		}
		return whatIfExpert;
	}

	public StoryExpert getStoryExpert() {
		if (storyExpert == null) {
			storyExpert = new StoryExpert();
		}
		return storyExpert;
	}

	public CommandExpert getCommandExpert() {
		if (commandExpert == null) {
			commandExpert = new CommandExpert();
		}
		return commandExpert;
	}

	public DescribeExpert getDescribeExpert() {
		if (describeExpert == null) {
			describeExpert = new DescribeExpert();
		}
		return describeExpert;
	}

	public StateExpert getStateExpert() {
		if (stateExpert == null) {
			stateExpert = new StateExpert();
		}
		return stateExpert;
	}

	public static AnaphoraExpert getAnaphoraExpert() {
		if (anaphoraExpert == null) {
			anaphoraExpert = new AnaphoraExpert();
		}
		return anaphoraExpert;
	}

	public IdiomExpert getIdiomExpert() {
		if (idiomExpert == null) {
			idiomExpert = new IdiomExpert();
		}
		return idiomExpert;
	}

	public StateMaintainer getStateMaintainer() {
		if (stateMaintainer == null) {
			stateMaintainer = new StateMaintainer(this);
		}
		return stateMaintainer;
	}

	public FileSourceReader getFileSourceReader() {
		return FileSourceReader.getFileSourceReader();
	}

	// Sila
	public StoryTeller getStoryTeller() {
		if (storyTeller == null) {
			storyTeller = new StoryTeller();
		}
		return storyTeller;
	}

	public SummaryHelper getSummaryHelper() {
		if (summaryHelper == null) {
			summaryHelper = new SummaryHelper();
		}
		return summaryHelper;
	}

	public StoryEnvironment getStoryEnvironment() {
		if (storyEnvironment == null) {
			storyEnvironment = new StoryEnvironment();
		}
		return storyEnvironment;
	}

	public InternalNarrator getInternalNarrator() {
		if (internalNarrator == null) {
			internalNarrator = new InternalNarrator();
		}
		return internalNarrator;
	}

	public StaticAudienceModeler getStaticAudienceModeler() {
		if (staticAudienceModeler == null) {
			staticAudienceModeler = new StaticAudienceModeler();
		}
		return staticAudienceModeler;
	}

	public GoalSpecifier getGoalSpecifier() {
		if (goalSpecifier == null) {
			goalSpecifier = new GoalSpecifier();
		}
		return goalSpecifier;
	}

	public GoalTracker getGoalTracker() {
		if (goalTracker == null) {
			goalTracker = new GoalTracker();
		}
		return goalTracker;
	}

	public StoryPreSimulator getStoryPresimulator() {
		if (storyPresimulator == null) {
			storyPresimulator = new StoryPreSimulator();
		}
		return storyPresimulator;
	}

	public StorySimulator getStorySimulator() {
		if (storySimulator == null) {
			storySimulator = new StorySimulator();
		}
		return storySimulator;
	}

	public StoryModifier getStoryModifier() {
		if (storyModifier == null) {
			storyModifier = new StoryModifier();
		}
		return storyModifier;
	}

	public StoryPublisher getStoryPublisher() {
		if (storyPublisher == null) {
			storyPublisher = new StoryPublisher();
		}
		return storyPublisher;
	}

	// added by Zhutian on 23 Mar 2019 for AAAI Symposium
	public PageStoryAligner getPageStoryAligner() {
		if (storyAlignerPage == null) {
			storyAlignerPage = new PageStoryAligner();
			storyAligner = new StoryAligner();
			storyAlignerPage.setName("Z Story Aligner Gallery");
		}
		return storyAlignerPage;
	}


	public void initializeListeners() {
		GeneralPurposeListener l = new GeneralPurposeListener(this);
		// panel.getFileChooserButton().addActionListener(listener);
		this.disambiguationButton.addActionListener(l);
		this.wordnetPurgeButton.addActionListener(l);
		this.startPurgeButton.addActionListener(l);
		this.conceptNetPurgeButton.addActionListener(l);
		this.runAligner.addActionListener(l);
		this.experienceButton.addActionListener(l);
		this.focusButton.addActionListener(l);
		this.clearMemoryButton.addActionListener(l);
		this.eraseTextButton.addActionListener(l);
		this.clearSummaryTableButton.addActionListener(l);

		FileReaderButtonListener m = new FileReaderButtonListener();
		this.testSentencesButton.addActionListener(m);
		this.testOperationsButton.addActionListener(m);
		this.testStoriesButton.addActionListener(m);
		this.demonstrateConnectionsButton.addActionListener(m);
		this.demonstrateConceptsButton.addActionListener(m);
		this.debugButton1.addActionListener(m);
		this.debugButton2.addActionListener(m);
		this.debugButton3.addActionListener(m);
		this.runWorkbenchTest.addActionListener(m);

		this.rerunExperiment.addActionListener(m);
		this.rereadFile.addActionListener(m);
		this.simulateCo57Button.addActionListener(m);
		this.connectTestingBox.addActionListener(m);
		this.disconnectTestingBox.addActionListener(m);
		this.demoSimulator.addActionListener(m);
		this.debugVideoFileButton.addActionListener(m);
		this.loopButton.addActionListener(m);
		this.kjFileButton.addActionListener(m);
		this.kjeFileButton.addActionListener(m);
		this.loadVideoPrecedents.addActionListener(m);
		this.visionFileButton.addActionListener(m);

		RunListener n = new RunListener();
		this.nextButton.addActionListener(n);
		this.runButton.addActionListener(n);

		TestSwitchListener s = new TestSwitchListener();
		Switch.useStartBeta.addActionListener(s);

	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.setOpaque(false);
			topPanel.add(getSplitPane(), BorderLayout.CENTER);
			topPanel.add(getTextEntryBox(), BorderLayout.SOUTH);
		}
		return topPanel;
	}

	public void initializeGraphics() {
		add(getNorthSouthSplitPane(), BorderLayout.CENTER);
		getExpertsPanel().setOpaque(false);
		// getExpertsPanel().add(getTrafficLightPanel());
		// add(getButtonPanel(), BorderLayout.NORTH);
		this.setSize(800, 550);

	}

	public MasterPanel getMasterPanel() {
		if (masterPanel == null) {
			masterPanel = new MasterPanel();
		}
		return masterPanel;
	}

	class RunListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == getRunButton()) {
				getNextButton().setEnabled(false);
				getRunButton().setEnabled(false);
				getFileSourceReader().readRemainingSentences();
				getContinueButton().setEnabled(false);
				GenesisGetters.getContinueButton().setOpaque(false);
			}
			if (e.getSource() == getNextButton()) {
				ArrayList<String> sentences = getFileSourceReader().getSentenceQueue();
				if (!sentences.isEmpty()) {
					Mark.say("Working on:", sentences.get(0));
				}
				getFileSourceReader().readNextSentence();
			}
		}
	}

	class FileReaderButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setBottomPanel("Elaboration graph");
			memorySwitch.setSelected(false);
			if (e.getSource() == testSentencesButton) {
				setBottomPanel("Experts");
				getFileSourceReader().readTheWholeStoryWithThread("representation test.txt");
			}
			else if (e.getSource() == testOperationsButton) {
				getFileSourceReader().readTheWholeStoryWithThread("Test operations.txt");
			}
			else if (e.getSource() == testStoriesButton) {
				setBottomPanel("Elaboration graph");
				getPersuader().getGateKeeper().setEnabled(true);
				getSummarizer().getGateKeeper().setEnabled(true);
				getFileSourceReader().readTheWholeStoryWithThread("Test demo stories.txt");
			}
			else if (e.getSource() == demonstrateConnectionsButton) {
				setBottomPanel("Elaboration graph");
				getPersuader().getGateKeeper().setEnabled(true);
				getSummarizer().getGateKeeper().setEnabled(true);
				getFileSourceReader().readTheWholeStoryWithThread("Causal varieties.txt");
			}
			else if (e.getSource() == demonstrateConceptsButton) {
				setBottomPanel("Elaboration graph");
				getPersuader().getGateKeeper().setEnabled(true);
				getSummarizer().getGateKeeper().setEnabled(true);
				getFileSourceReader().readTheWholeStoryWithThread("conceptExamples.txt");
			}
			else if (e.getSource() == rereadFile) {
				getFileSourceReader().rerun();
			}
			else if (e.getSource() == rerunExperiment) {
				Mark.say("Rerun experiment");
				if (currentMenuItem != null) {
					currentMenuItem.doClick();
				}
				else {
					Mark.err("Cannot rerun because no experiment has been run yet");
				}
			}
			else if (e.getSource() == debugButton1) {
				setRightPanel("Mental Models");
				setBottomPanel("Elaboration graph");
				getFileSourceReader().readTheWholeStoryWithThread("debug1.txt");
			}
			else if (e.getSource() == debugButton2) {
				setRightPanel("Mental Models");
				setBottomPanel("Elaboration graph");
				getFileSourceReader().readTheWholeStoryWithThread("debug2.txt");
			}

			else if (e.getSource() == debugButton3) {
				setBottomPanel("Elaboration graph");
				getFileSourceReader().readTheWholeStoryWithThread("debug3.txt");
			}

			else if (e.getSource() == runWorkbenchTest) {
				setRightPanel("Mental Models");
				setBottomPanel("Elaboration graph");
				runWorkbenchTest();
			}

//			else if (e.getSource() == simulateCo57Button) {
//				Co57Simulator.Simulate();
//			}

			else if (e.getSource() == connectTestingBox) {
				Mark.say("Connecting");
				disconnectTestingBox.setEnabled(true);
				connectTestingBox.setEnabled(false);
				Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), TestStoryOutputBox.getBox());
			}
			else if (e.getSource() == disconnectTestingBox) {
				Mark.say("Disconnecting");
				disconnectTestingBox.setEnabled(false);
				connectTestingBox.setEnabled(true);
				Connections.disconnect(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), TestStoryOutputBox.getBox());
			}
			else if (e.getSource() == demoSimulator) {
				Connections.getPorts(StartPreprocessor.getStartPreprocessor()).transmit(StartPreprocessor.SELF, ("Interpret video test"));
			}
			else if (e.getSource() == debugVideoFileButton) {
				getFileSourceReader().readTheWholeStoryWithThread("debugVideo.txt");
			}
			else if (e.getSource() == loopButton) {
				getFileSourceReader().readTheWholeStoryWithThread("loop.txt");
				// file = new StoryAnchor().get("LostInTranslation.txt");
				// getFileSourceReader().setFile(new File(file));
				// file = new StoryAnchor().get("Macbeth2.txt");
				// getFileSourceReader().setFile(new File(file));
			}
			else if (e.getSource() == kjFileButton) {
				getFileSourceReader().readTheWholeStoryWithThread("KoreanSlander.txt");
			}
			else if (e.getSource() == kjeFileButton) {
				getFileSourceReader().readTheWholeStoryWithThread("KJER.txt");
			}
			else if (e.getSource() == loadVideoPrecedents) {
				getFileSourceReader().readTheWholeStoryWithThread("debugVideoPrecedents.txt");
			}
			else if (e.getSource() == visionFileButton) {
				getFileSourceReader().readTheWholeStoryWithThread("dialog.txt");
			}

		}
	}

	// Direct insertion hacka

	private boolean workBenchConnectionWired = false;

	private void runWorkbenchTest() {
		if (!workBenchConnectionWired) {
			Mark.say("Creating connection to WorkBench");
			Connections.wire(WorkbenchConnection.getWorkbenchConnection(), getMentalModel1());
			workBenchConnectionWired = true;
		}
		getMentalModel1().clearAllMemories();
		WorkbenchConnection.getWorkbenchConnection().test();
		getMentalModel1().getStoryProcessor().stopStory();
	}

	private JMenu affixMenu(String name, JMenu top) {
		JMenu menu = new JMenu(name);
		top.add(menu);
		return menu;
	}

	private boolean readStory(String file) {
		try {
			getNorthSouthSplitPane().setDividerLocation(0.5);
			getSplitPane().setDividerLocation(0.25);
			setBottomPanel("Elaboration graph");
			getFileSourceReader().readTheWholeStoryWithThread(file, this);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	private void affixAction(JMenu menu, String label, ActionListener listener) {
		JMenuItem item = new JMenuItem(label);
		menu.add(item);
		item.addActionListener(listener);
	}

	public static void clickToValue(AbstractButton button, boolean value) {
		FileSourceReader.clickToValue(button, value);
	}

	private JMenu affixLibrary(String menu) {
		JMenu top = new JMenu(menu);
		affixShakespeareDemonstrations(top);
		affixOnsetDemonstrations(top);
		affixPrecedentDemonstrations(top);
		affixSummaryDemonstrations(top);
		affixPersuasionDemonstrations(top);
		affixInstructionDemonstrations(top);
		if (!Webstart.isWebStart()) {
			affixEastWestDemonstrations(top);
		}
		affixConflictDemonstrations(top);
		affixEmotionDemonstrations(top);
		affixAlignmentDemonstrations(top);
		affixSimilarityDemonstrations(top);
		affixLearningAndPlotGenerationDemonstrations(top);
		affixMiscellaneousDemonstrations(top);
		return top;
	}

	private void affixShakespeareDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Shakespeare, basic", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getEscalationExpert1().getGateKeeper().setSelected(true);
				readStory("Macbeth1.txt");
			}

		});
		affixAction(menu, "Macbeth, two cultures", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				Switch.includePersonalityExplanationCheckBox.setSelected(false);
				Switch.includeCauseExplanationCheckBox.setSelected(true);
				Switch.includeConceptExplanationCheckBox.setSelected(true);
				readStory("Macbeth2.txt");
			}
		});
		affixAction(menu, "Macbeth, with/without traits", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Traits-Macbeth.txt");
			}
		});
		affixAction(menu, "Macbeth with expert", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Macbeth with expert.txt");
			}
		});
		affixAction(menu, "Hamlet", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Hamlet1.txt");
			}
		});
		affixAction(menu, "Caesar", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Caesar1.txt");
			}
		});
		affixAction(menu, "Hamlet + Caesar", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Hamlet-Caesar.txt");
			}
		});
		affixAction(menu, "Macbeth with what-if question", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				// readStory("What if Macbeth.txt");
				readStory("macbeth simple what if.txt");
			}
		});
	}

	private void affixOnsetDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Shakespeare, onset", top);
		affixAction(menu, "Macbeth, one culture", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("MacbethOnset1.txt");
			}
		});
		affixAction(menu, "Macbeth, two cultures", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("MacbethOnset2.txt");
			}
		});
	}

	private void affixPrecedentDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Shakespeare, precedents", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Macbeth1.txt");
				setLeftPanel("Precedent concept recall");
			}
		});
		affixAction(menu, "Hamlet", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Hamlet1.txt");
				setLeftPanel("Precedent concept recall");
			}
		});
	}

	private void affixSummaryDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Summary", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getSummarizer().getGateKeeper().setSelected(true);
				readStory("Macbeth1 summary.txt");
				setRightPanel("Summary");
			}
		});
		// affixAction(menu, "Estonia", new ActionListener() {
		// public void actionPerformed(ActionEvent event) {
		// initializeDemon(event);
		// getSummarizer().getGateKeeper().setSelected(true);
		// readStory("Estonia1 summary.txt");
		// setRightPanel("Summary");
		// }
		// });
		affixAction(menu, "Mini murder", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getSummarizer().getGateKeeper().setSelected(true);
				readStory("Swindle.txt");
				setRightPanel("Summary");
			}
		});
	}

	private void affixPersuasionDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Persuasion", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getPersuader().getGateKeeper().setSelected(true);
				readStory("Macbeth with persuasion.txt");
				setRightPanel("Retelling");
			}
		});
		affixAction(menu, "Estonia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getPersuader().getGateKeeper().setSelected(true);
				readStory("Estonia with persuasion.txt");
				setRightPanel("Retelling");
			}
		});
		affixAction(menu, "Hansel and Gretel", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getPersuader().getGateKeeper().setSelected(true);
				readStory("Hansel and Gretel run.txt");
				setRightPanel("Retelling");
			}
		});
	}

	private void affixInstructionDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Instruction", top);
		// Doesn't work s of 23 Sep 2014 phw
		// affixAction(menu, "Teacher vs Student", new ActionListener() {
		// public void actionPerformed(ActionEvent event) {
		// initializeDemon(event);
		// setBottomPanel("Elaboration graph");
		// readStory("MacbethTeacherStudent.txt");
		// }
		// });
		affixAction(menu, "Spoon feeding only", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				setBottomPanel("Story");
				clickToValue(Radio.spoonFeedButton, true);
				clickToValue(Radio.tellStoryButton, true);
				readStory("Macbeth2sila.txt");
			}
		});
		affixAction(menu, "Explanation provided", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				setBottomPanel("Story");
				clickToValue(Radio.primingButton, true);
				clickToValue(Radio.tellStoryButton, true);
				readStory("Macbeth2sila.txt");
			}
		});
		affixAction(menu, "Principle taught", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				setBottomPanel("Story");
				clickToValue(Radio.primingWithIntrospectionButton, true);
				clickToValue(Radio.tellStoryButton, true);
				readStory("Macbeth2sila.txt");
			}
		});

	}

	private void affixEastWestDemonstrations(JMenu top) {
		JMenu menu = affixMenu("East-West", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Macbeth2");
			}
		});
		affixAction(menu, "Lu murders (from Morris & Peng)", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Lu Murder Story.txt");
			}
		});
		affixAction(menu, "McIlvane murders (from Morris & Peng)", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("McIlvane Murder Story.txt");
			}
		});
	}

	private void affixConflictDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Conflict", top);
		affixAction(menu, "Russia vs Estonia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Estonia2");
			}
		});
		affixAction(menu, "Russia vs Georgia/Russia vs Estonia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("GeorgiaEstonia.txt");
			}
		});
		affixAction(menu, "Lost in translation", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("LostInTranslation.txt");
			}
		});
	}

	private void affixEmotionDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Emotional impact", top);
		affixAction(menu, "Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("MacbethWithEmotions.txt");
			}
		});
		affixAction(menu, "Estonia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Estonia with emotions.txt");
			}
		});
	}

	private void affixAlignmentDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Alignment", top);
		affixAction(menu, "Tet offensive", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				clickToValue(Radio.alignmentButton, true);
				readStory("tet.txt");
				clickToValue(Radio.alignmentButton, true);

			}
		});
		affixAction(menu, "Macbeth, two cultures", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				clickToValue(Radio.alignmentButton, true);
				readStory("Macbeth2.txt");
				clickToValue(Radio.alignmentButton, true);

			}
		});
	}

	private void affixSimilarityDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Recall", top);
		affixAction(menu, "Vector demonstration", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getStoryRecallExpert1().clearMemory();
				getStoryRecallExpert2().clearMemory();
				getStoryRecallExpert1().getGateKeeper().setSelected(true);
				readStory("Vector.txt");
			}
		});
		affixAction(menu, "Three stories", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("_read3.txt");
				clickToValue(Radio.calculateSimilarityButton, true);

			}
		});
		affixAction(menu, "Five stories", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("_read5.txt");
				clickToValue(Radio.calculateSimilarityButton, true);
			}
		});
		affixAction(menu, "Ten stories", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("_read10.txt");
				clickToValue(Radio.calculateSimilarityButton, true);
			}
		});
		affixAction(menu, "All thirteen stories", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("_read15.txt");
				clickToValue(Radio.calculateSimilarityButton, true);

			}
		});
	}
	private void affixSchizophreniaViaHPDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Schizophrenia, modeled via Hyper-presumption", top);

		affixAction(menu, "Intention Attribution Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo IntentionAttributionTask SchizoViaHP top");
			}
		});
		affixAction(menu, "Hinting Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo HintingTask SchizoViaHP top");
			}
		});
		affixAction(menu, "Paranoid Delusion", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ParanoidDelusionTask SchizoViaHP top");
			}
		});
		affixAction(menu, "Persistence of Delusion", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo PersistenceOfDelusionTask SchizoViaHP top");
			}
		});
		affixAction(menu, "Control task: Physical causality with Objects Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ControlTask-PCwOTask SchizoViaHP top");
			}
		});
		affixAction(menu, "Control task: Physical causality with Objects and Characters Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ControlTask-PCwOCTask SchizoViaHP top");
			}
		});
	}
	private void affixSchizophreniaViaFaMDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Schizophrenia, modeled via Failure at Mentalizing", top);

		affixAction(menu, "Intention Attribution Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo IntentionAttributionTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "False Belief Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo FalseBeliefTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "Hinting Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo HintingTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "Paranoid Delusion", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ParanoidDelusionTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "Persistence of Delusion", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo PersistenceOfDelusionTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "Control task: Physical causality with Objects Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ControlTask-PCwOTask SchizoViaFaM top");
			}
		});
		affixAction(menu, "Control task: Physical causality with Objects and Characters Task", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizo ControlTask-PCwOCTask SchizoViaFaM top");
			}
		});
	}

	private void affixMiscellaneousDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Miscellaneous", top);

		affixAction(menu, "Crime story", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Godfather.txt");
			}
		});

		affixAction(menu, "Hi-low culture", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Piano.txt");
			}
		});

		affixAction(menu, "Schizophrenia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Schizophrenia.txt");
			}
		});

		affixAction(menu, "Suprise/Animacy", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Macbeth1 summary.txt");
			}
		});

		affixAction(menu, "Zookeeper", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Zookeeper.txt");
			}
		});

		affixAction(menu, "Scripting, with traits", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("What if Macbeth.txt");
			}
		});
		affixAction(menu, "Basic generator", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				setBottomPanel("Results");
				Generator.getGenerator().runBasicTests();

			}
		});
		affixAction(menu, "Advanced generator", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				setBottomPanel("Results");
				Generator.getGenerator().runSubordinateTests();
			}
		});

	}


	private void affixLearningAndPlotGenerationDemonstrations(JMenu top) {
		JMenu menu = affixMenu("Learning and Plot generation", top);
		affixAction(menu, "War for Western Europe", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("War for Western Europe.txt");
			}
		});
		affixAction(menu, "Oedipus Much", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Oedipus Much.txt");
			}
		});
		affixAction(menu, "Hansel sans Gretel", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Hansel without Gretel.txt");
			}
		});
		affixAction(menu, "Alignment based trait learning", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("near_miss_trait_learning.txt");
			}
		});
	}

	private JMenu affixRerun(String name) {
		JMenu menu = new JMenu(name);
		affixAction(menu, "Read file again", new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				getFileSourceReader().rerun();

			}
		});
		return menu;
	}

	private JMenu affixDemonstrations(String name) {
		JMenu menu = new JMenu(name);
		affixAction(menu, "Basic story understanding: Macbeth plot", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getEscalationExpert1().getGateKeeper().setSelected(true);
				readStory("Basic story understanding demo");
			}

		});
		affixAction(menu, "Cultural differences in interpretation: Eastern vs Western", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getEscalationExpert1().getGateKeeper().setSelected(true);
				readStory("Cultural differences in interpretation demo");
			}

		});
		affixAction(menu, "Allegiance differences in interpretation: Estonia vs Russia", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Allegiance differences in interpretation demo");
			}
		});
		affixAction(menu, "Crow creation myth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Crow creation myth demo");
			}
		});
		affixAction(menu, "Persuasive telling: Hansel and Gretel", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getPersuader().getGateKeeper().setSelected(true);
				readStory("Persuasive telling demo");
			}
		});

		affixAction(menu, "Summary: Macbeth plot", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Macbeth summary demo");
			}
		});

		affixAction(menu, "Alignment: Tet offensive with Yom Kippur war", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Tet demo");
			}
		});

		// dxh to uncommment
		if (true) {
			affixAction(menu, "Hypothetical reasoning", new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					initializeDemon(event);
					readStory("Hypotheticals demo");
				}
			});
		}
		affixAction(menu, "Causal varieties: a catalog", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Causal varieties demo");
			}
		});

		affixAction(menu, "ConceptNet & ASPIRE: Mexican-American War", new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        initializeDemon(event);
		        readStory("Mexican American war demo");
		    }
		});

		affixAction(menu, "CSERM (ConceptNet-assisted Rule Matching): Bullying", new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        initializeDemon(event);
		        readStory("CSERM demo");
		    }
		});


		// added by PHW on 1 DEC 2018
		affixAction(menu, "What comes next", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("What comes next");
			}
		});

		if (!Webstart.isWebStart()) {
			affixAction(menu, "Question-driven reflection: Lu murder", new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					initializeDemon(event);
					readStory("Question-driven reflection demo");
				}
			});
		}

		// added by PHW on January 2019
		affixAction(menu, "Analyze story modulation: rebel versus terrorist", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("RASHI demo");
			}
		});
		// added by Suri (scb) on 29 January 2019
		affixAction(menu, "Analyze story modulation: blame, sympathy, doubt", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("RASHI Blame Sympathy Doubt demo");
			}
		});
		// added by Suri (scb) on 1 February 2019
		affixAction(menu, "Analyze story modulation: climate change", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("RASHI Climate Change demo");
			}
		});

		// added by Zhutian on 23 Aug 2019
		// this.affixSchizophreniaViaHPDemonstrations(menu);
		// this.affixSchizophreniaViaFaMDemonstrations(menu);


		// added by Zhutian on 6 Oct 2018
		affixAction(menu, "Learn problem-solving from expert: Read books", new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
		        initializeDemon(event);
		        readStory("Z Read demo");
		    }
		});


		// added by Zhutian on 6 Oct 2018
		affixAction(menu, "Learn problem-solving from two stories: Replace Battery", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				readStory("Z Learn from two stories demo");
			}
		});

		// added by PHW on 27 November Oct
		affixAction(menu, "Learn to mix a martini", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				getBlocksWorld().getModel().initializeMartini();
				getBlocksWorld().getModel().initializeOnTable();
				readStory("Mix martini demo");
			}
		});

		return menu;
	}

	private void affixSamples(JMenu top) {
		JMenu menu = affixMenu("Macbeth", top);
		affixAction(menu, "Record Macbeth", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				initializeRecording();
				Switch.showDisconnectedSwitch.setSelected(false);
				Switch.showDisconnectedSwitch.doClick();
				getFileSourceReader().readTheWholeStoryWithThread("Macbeth1.txt");
			}
		});
		affixAction(menu, "Record Macbeth with question", new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initializeDemon(event);
				initializeRecordingOfLeftAndBottom();
				getFileSourceReader().readTheWholeStoryWithThread("Macbeth2.txt");
				// Now done with entry in text file
		        // setRightPanel("Results");
		        // showTextBox();
		        // getTextEntryBox().setText("Why did Macduff kill Macbeth?");
			}
		});
	}

	private void initializeDemon(ActionEvent event) {
		currentMenuItem = (JMenuItem) event.getSource();
		getPersuader().getGateKeeper().setSelected(false);
		getSummarizer().getGateKeeper().setSelected(false);
		getEscalationExpert1().getGateKeeper().setSelected(false);
		getEscalationExpert2().getGateKeeper().setSelected(false);
		getStoryRecallExpert1().getGateKeeper().setSelected(false);
		getStoryRecallExpert2().getGateKeeper().setSelected(false);
//		getPredictionExpert1().getGateKeeper().setSelected(false);

		FileSourceReader.rememberSwitches();

		Switch.findConceptOnsets.setSelected(false);

	}

	private JMenu affixRecording(String menu) {
		JMenu top = new JMenu(menu);
		affixSamples(top);
		return top;
	}

	private void pause() {
		try {
			Thread.sleep(3000);
		}
		catch (InterruptedException e) {
		}
	}

	public void showUnconnectedElements() {
		Switch.showDisconnectedSwitch.setSelected(false);
		Switch.showDisconnectedSwitch.doClick();
	}

	private void prepareSwitchesForTest() {
		Switch.showTextEntryBox.setSelected(false);
		Switch.useStartCache.setSelected(false);
		Switch.useWordnetCache.setSelected(false);
		Switch.useStartBeta.setSelected(false);
	}

	private void setToVideoRecordingDimensions(Rectangle r) {
		Mark.say("Adjusting size");
		ABasicFrame.getFrame().setBounds(r);
		ABasicFrame.getFrame().invalidate();
	}

	public void showTextBox() {
		Switch.showTextEntryBox.setSelected(false);
		Switch.showTextEntryBox.doClick();
	}

	public void initializeRecording() {
		setToVideoRecordingDimensions(new Rectangle(0, 0, 1600, 1200));
		getNorthSouthSplitPane().setDividerLocation(0.0);
		// getSplitPane().setDividerLocation(0.0);
		setBottomPanel("Elaboration graph");
		pause();
	}

	public void initializeRecordingOfLeftAndBottom() {
		setToVideoRecordingDimensions(new Rectangle(0, 0, 1600, 1200));
		getNorthSouthSplitPane().setDividerLocation(0.3);
		getSplitPane().setDividerLocation(0.0);
		setBottomPanel("Elaboration graph");
		pause();
	}

	class OpenReferenceFile extends Thread {
		String string;

		public OpenReferenceFile(String string) {
			this.string = string;
		}

		public void run() {
			// String command = StandardsAnchor.getStoryAnchor().getFile(string);
			// command = "\"" + command.substring(1) + "\"";
			// WindowsConnection.run(command, null, null);
		}
	}

	private void suggestAfterReading(String suggestion) {
		Mark.say("Entering suggestAfterReading");
		new Suggest(suggestion).start();
	}

	class Suggest extends Thread {
		String suggestion;

		public Suggest(String s) {
			suggestion = s;
		}

		public void run() {
			if (getFileSourceReader().getTheReaderThread() != null) {
				try {
					Mark.say("Pausing...");
					getFileSourceReader().getTheReaderThread().join();
					Mark.say("Going...");
					getResultContainer().process(new BetterSignal("Suggestion", TabbedTextViewer.CLEAR));

					getResultContainer().process(new BetterSignal("Suggestion", suggestion));

				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	class TestSwitchListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if (source == Switch.useStartBeta) {
				if (Switch.useStartBeta.isSelected()) {
					Mark.say("Use");
					Switch.useStartServer.setSelected(false);
					Switch.useStartCache.setSelected(false);
				}
				else {
					Switch.useStartServer.setSelected(true);
					Switch.useStartCache.setSelected(true);
				}
			}
		}
	}

	class GeneralPurposeListener implements ActionListener {
		Component component;

		public GeneralPurposeListener(Component component) {
			this.component = component;
		}

		public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			FileReaderPanel fileReaderPanel = getFileReaderFrame();

			Switch.showOnsetSwitch.setSelected(false);

			if (source == printMenuItem) {
				printMe();
			}
			// else if (source == BlinkingBox.getMemoryItem()) {
			// getBlinkingBoxPanel().redo();
			// }

			else if (source == Radio.alignmentButton) {
				Mark.say("Hello Patrick");
			}

			// else if (source == Switch.showInferences || source == Switch.showWires) {
			// getMentalModel1().getElaborationViewer().changed();
			// getMentalModel2().getElaborationViewer().changed();
			// }

			else if (source == Switch.showTextEntryBox) {
				if (!Switch.showTextEntryBox.isSelected()) {
					getTextEntryBox().zero();
				}
				else {
					getTextEntryBox().normal();
				}
				GenesisGetters.this.revalidate();
				GenesisGetters.this.getTextEntryBox().revalidate();
			}

			else if (source == Switch.showStatisticsBar) {
				if (!Switch.showStatisticsBar.isSelected()) {
//					getPlotDiagram().add(getStatisticsBar(), BorderLayout.WEST); // TODO switch between having statistics bar and not
				}
				else {
					getTextEntryBox().normal();
				}
			}

			else if (source == contributorMenuItem) {
				showContributors();
			}
			else if (source == readFileAgainItem) {
				getFileSourceReader().rerun();
			}
			else if (source == experienceButton) {
				Switch.disambiguatorSwitch.setSelected(false);
				memorySwitch.setSelected(false);
				getMovieManager().loadMovieDescriptions();
				experienceButton.setBackground(greenish);
				memorySwitch.setSelected(true);

				Switch.useSpeechCheckBox.setSelected(true);
			}
			else if (source == disambiguationButton) {
				String file = new Anchor().get("disambiguation.txt");
				File selected = new File(file);
				memorySwitch.setSelected(true);
				if (selected.exists()) {
					// getTextEntryBox().setText("");
					getFileSourceReader().readTheWholeStoryWithThread(file);
					disambiguationButton.setBackground(greenish);
				}
				Switch.disambiguatorSwitch.setSelected(true);
				getNewDisambiguator().flushLibrary("");
			}
			else if (source == wordnetPurgeButton) {
				BundleGenerator.purgeWordnetCache();
				BundleGenerator.writeWordnetCache();
			}
			else if (source == startPurgeButton) {
				Start.purgeStartCache();
			} else if (source == conceptNetPurgeButton) {
			    ConceptNetClient.purgeCache();
			} else if (source == runAligner) {
				Mark.say("Running aligner now");
				Sequence seqA = GenesisGetters.getMentalModel1().getStoryProcessor().getStory();
				Sequence seqB = GenesisGetters.getMentalModel2().getStoryProcessor().getStory();
				;

				Aligner aligner = new Aligner();
				SortableAlignmentList sal = aligner.align(seqA, seqB);

				// Shows which story enties match.
				SequenceAlignment bestAlignment = (SequenceAlignment) sal.get(0);
				LList<PairOfEntities> bestBindings = bestAlignment.bindings;
				Mark.say("\nBest entity bindings");
				for (PairOfEntities p : bestBindings) {
					Mark.say("Entity binding:", p);
				}

				// Shows which story elements match.
				Mark.say("\nBest element bindings");
				bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
				Mark.say("\n>>> Not good match", sal.size(), ":");
				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

				sal = aligner.align(seqB, seqA);
				Mark.say("\n>>> Reverse,", sal.size(), ":");
				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

				sal = aligner.align(seqA, seqA);
				Mark.say("\n>>> Self alignment A,", sal.size(), ":");
				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

				Mark.say("\n>>> Self alignment B,", sal.size(), ":");
				sal = aligner.align(seqB, seqB);
				sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

			}
			else if (source == clearSummaryTableButton) {
				getSummarizer().initializeTable();
			}

			else if (source == genesisStories) {
				Mark.say("Switch to genesis stories");
				setBottomPanel("Elaboration graph");
				setRightPanel("Sources");

			}

			else if (source == readDirectoryItem) {
				Mark.say("Entering readDirectory listener");
				String directory = Preferences.userRoot().get(GenesisConstants.STORY_ROOT, "c:/");
				Mark.say("Preferred directory is", directory);
				JFileChooser chooser = fileReaderPanel.getFileChooser(directory);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(component);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					Mark.say("You chose to use this directory as the root: " + selected.getAbsolutePath());
					Preferences.userRoot().put(GenesisConstants.STORY_ROOT, selected.getAbsolutePath());
				}
				else {
					System.out.println("You did not chose to select a root directory");
				}
			}
			else if (source == readStoryItem) {
				boolean debug = true;
				Mark.say(debug, "Entering readStory listener");
				String defaultDirectory = Preferences.userRoot().get(GenesisConstants.STORY_ROOT, "C:/");
				String path = Preferences.userRoot().get(GenesisConstants.STORY_FILE, defaultDirectory);
				Mark.say(debug, "Preferred story is", path);
				JFileChooser chooser = fileReaderPanel.getFileChooser(path);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = chooser.showOpenDialog(component);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					// File selected = conditionFileName(chooser.getSelectedFile());

					File selected = chooser.getSelectedFile();

					Mark.say("Selected file is", selected);

					selectedFile = selected;

					Mark.say("You chose to open this file: " + selected);

					Preferences.userRoot().put(GenesisConstants.STORY_FILE, selected.getAbsolutePath());
					if (selected.exists()) {
						FileSourceReader.fileChooserDirectory = selected.getParentFile();
						getFileSourceReader().readTheWholeStoryLocally(selected.getName());
					}
					else {
						System.err.println(selected + " does not exist!");
						fileReaderPanel.setState(FileReaderPanel.stopped);
					}
				}
				else {
					System.out.println("You did not chose to open a file");
				}
			}
			// else if (source == focusButton) {
			// selectFocusDirectory();
			// }

			else if (source == eraseTextButton) {
				getSourceContainer().clear();
				getResultContainer().clear();
				getConceptContainer().clear();
			}

			else if (source == clearMemoryButton) {
				// getMemory().clear();
				// getSomTrajectoryViewer().validate();
				// getSomTrajectoryViewer().repaint();
				// TODO: make button to clear memory
				System.err.println("ERROR: clear button not yet supported with the new memory -- talk to Sam");

				// Use to clear story background temporarily--talk to Patrick
				GenesisGetters.this.getCauseExpert().clearRuleMemory();

			}

		}

		private boolean initializeMindsEye = false;

		/*
		 * Called when mode is switched
		 */

		private File conditionFileName(File file) {
			String name = file.getPath();
			if (name.indexOf('.') < 0) {
				name += ".txt";
				return new File(name);
			}
			return file;
		}



		private void showContributors() {
			String s = "";
			s += "<html><center>";
			s += contributors;
			s += "</center></html>";
			JLabel label = new JLabel(s);
			JOptionPane.showMessageDialog(GenesisGetters.this, label, "Contributors", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	String contributors = "Patrick Winston, ";

	private void addContributor(String name, String contribution) {
		// contributors += "<tr><td>" + name + "</td></tr>\n";
		contributors += name + ", ";
	}

	private void addOthers() {
		contributors += "and others.";
	}

	private void addContributor(String name) {
		addContributor(name, "");
	}

	private void computeContributors() {
		addContributor("Caroline Aronoff", "Bayesian trait calculation");
		addContributor("Hiba Awad", "Morris-Peng stories and analysis");
		addContributor("Jake Beal", "Let's do it");
		addContributor("Michael Behr", "Vision interface");
		addContributor("Adam Belay", "Onset of plot units");
		addContributor("Rachel Chaney", "Visualization");
		addContributor("Alexander Cherian", "Plans");
		addContributor("Harold Cooper", "Syntax to semantics");

		addBreak();
		addContributor("Matthew Fay", "Alignment and generation");
		addContributor("Mark Finlayson", "Infrastructure");
		addContributor("Beth Hadley", "Low-context--high-context cultures");
		addContributor("Josh Haimson", "Disambiguation");
		addContributor("Dylan Holmes", "What if");
		addContributor("Vanessa Galani", "Knitting together news");
		addContributor("Sam Glidden", "Memory");
		addContributor("Christopher Grimm", "Self aware performance");
		addContributor("Mike Klein", "Syntax to semantics");
		addContributor("Adam Kraft", "Syntax to semantics");

		addBreak();
		addContributor("Caryn Krakauer", "Story comparison");
		addContributor("Benjamin Lamothe", "Representations");
		addContributor("Tom Larsen", "Disambiguation");
		addContributor("Capen Low", "Rule generalization");
		addContributor("Robert McIntyre", "Infrastructure and scrubbing");
		addContributor("Marina Morozova", "Polit situations and stories");
		addContributor("Royal Morris", "Polti situations");
		addContributor("David Nackoul", "Plot unit translation");
		addContributor("Jessica Noss", "Story from participant perspective");

		addBreak();
		addContributor("Sila Sayan", "Teaching and persuading");
		addContributor("Olga Shestopalova", "What happens next");
		addContributor("Vivaek Shivakumar", "Talking to self");
		addContributor("Susan Song", "Personality traits");
		addContributor("Erek Speed", "Visual stories (the cat problem)");
		addContributor("Eann Tuan", "Genesis stories");
		addContributor("Kevin White", "Graphics panels and disambiguation");
		addContributor("Victor Yarlott", "Crow stories and analysis");
		addOthers();

	}

	private void addBreak() {
		contributors += "<br/>";

	}

	public JMenuBar getMenuBar() {

		if (menuBar == null) {

			menuBar = new JMenuBar();

			GeneralPurposeListener gListener = new GeneralPurposeListener(this);

			readStoryItem.addActionListener(gListener);
			readDirectoryItem.addActionListener(gListener);

			genesisStories.addActionListener(gListener);

			printMenuItem.addActionListener(gListener);
			loadAnnotationsItem.addActionListener(gListener);

			readMenu.add(readStoryItem);
			readMenu.add(readDirectoryItem);

			menuBar.add(affixDemonstrations("Demonstrations"));

			menuBar.add(affixLibrary("Library"));

			menuBar.add(readMenu);

			// menuBar.add(affixRerun("Rerun"));

			// item.addActionListener(listener);

			menuBar.add(affixRecording("Record"));

			if (Webstart.isWebStart()) {
				// Record with stat counter
				// Must display JEditorPane to get URL read
				try {
					URL url = new URL("http://people.csail.mit.edu/phw/genesis-runs.html");
					JEditorPane pane = new JEditorPane(url);
					getMainMatrix().add(pane, 0, 0, 1, 1);
					// Also works:
					// url = new
					// URL("http://c.statcounter.com/8107293/0/ed8c7d60/0/");
					// TextIO.readStringFromURL(url);
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			Switch.showTextEntryBox.addActionListener(gListener);
			Radio.alignmentButton.addActionListener(gListener);

			aboutMenu.add(contributorMenuItem);
			contributorMenuItem.addActionListener(gListener);

			menuBar.add(aboutMenu);

			menuBar.add(new JSeparator());

			menuBar.add(NewTimer.startDirectTimer.getOnOffLabel());
			menuBar.add(NewTimer.translationTimer.getOnOffLabel());
			menuBar.add(NewTimer.generatorTimer.getOnOffLabel());
			menuBar.add(NewTimer.conceptNetTimer.getOnOffLabel());

			menuBar.add(new JSeparator());

			JButton button;

			if (!Webstart.isWebStart()) {

				button = new JButton("Debug 1");
			button.addActionListener(e -> {
				setRightPanel("Mental Models");
				setBottomPanel("Elaboration graph");
				getFileSourceReader().readTheWholeStoryWithThread("debug1.txt");
			});
				menuBar.add(button);
				button = new JButton("Debug 2");
				button.addActionListener(e -> {
					setRightPanel("Mental Models");
					setBottomPanel("Elaboration graph");
					getFileSourceReader().readTheWholeStoryWithThread("debug2.txt");
				});
				menuBar.add(button);
				button = new JButton("Debug 3");
				button.addActionListener(e -> {
					setRightPanel("Mental Models");
					setBottomPanel("Elaboration graph");
					getFileSourceReader().readTheWholeStoryWithThread("debug3.txt");
				});
				menuBar.add(button);

				button = getRerunButton();
				button.addActionListener(e -> getFileSourceReader().rerun());
				menuBar.add(button);

				button = getContinueButton();
				button.addActionListener(e -> {
					ArrayList<String> sentences = getFileSourceReader().getSentenceQueue();
					if (!sentences.isEmpty()) {
						// Mark.say("Working on:", sentences.get(0));
					}
					getFileSourceReader().readRemainingSentences();
				});
				menuBar.add(button);


			}
		}
		return menuBar;
	}

	static JButton continueButton;

	public static JButton getContinueButton() {
		if (continueButton == null) {
			continueButton = new JButton("Continue");
			continueButton.setOpaque(true);
		}
		return continueButton;
	}

	static JButton rerunButton;

	public static JButton getRerunButton() {
		if (rerunButton == null) {
			rerunButton = new JButton("Rerun");
		}
		return rerunButton;
	}

	JMenuItem nextMenuItem;

	public JMenuItem getNextMenuItem() {
		if (nextMenuItem == null) {
			nextMenuItem = new JMenuItem("Continue");
			nextMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ArrayList<String> sentences = getFileSourceReader().getSentenceQueue();
					if (!sentences.isEmpty()) {
						Mark.say("Working on:", sentences.get(0));
					}
					getFileSourceReader().readRemainingSentences();
				}
			});
		}
		return nextMenuItem;
	}

	SimulatorPanel fancySimulator = null;

	public JPanel getFancySimulator() {
		if (fancySimulator == null) {
			fancySimulator = new SimulatorPanel();
			fancySimulator.setName("Fancy simulator");
			JLabel label = new JLabel("");
			fancySimulator.add(label);
		}
		return fancySimulator;
	}





	public WiredSplitPane getInspectorPanel() {
		if (inspectorPanel == null) {
			inspectorPanel = new WiredSplitPane(getMentalModel1().getInspectionView(), getMentalModel2().getInspectionView());
			inspectorPanel.setName("Inspector");
		}
		return inspectorPanel;
	}

	public TabbedTextViewer getSourceContainer() {
		if (sourceContainer == null) {
			sourceContainer = new TabbedTextViewer(this, "Sources");
			sourceContainer.switchTab("Commonsense knowledge");
		}
		return sourceContainer;
	}

	public TabbedTextViewer getSummaryContainer() {
		if (summaryContainer == null) {
			summaryContainer = new TabbedTextViewer(this, "Summary");
		}
		return summaryContainer;
	}


	public TabbedTextViewer getResultContainer() {
		if (resultContainer == null) {
			resultContainer = new TabbedTextViewer(this, "Results");
		}
		return resultContainer;
	}

	// public TabbedTextViewer getIntrospectionContainer() {
	// if (introspectionContainer == null) {
	// introspectionContainer = new TabbedTextViewer();
	// introspectionContainer.setName("Introspection");
	// getWindowGroupManager().addJComponent(introspectionContainer);
	// }
	// return introspectionContainer;
	// }

	public TabbedTextViewer getExplanationContainer() {
		if (explanationContainer == null) {
			explanationContainer = new TabbedTextViewer(this, "Explanation");
		}
		return explanationContainer;
	}

	public TabbedTextViewer getCommentaryContainer() {
		if (commentaryContainer == null) {
//			Mark.say("Creating commentary container");
			commentaryContainer = new TabbedTextViewer(this, "Commentary");
		}
		return commentaryContainer;
	}

	public TabbedTextViewer getScopeContainer() {
		if (scopeContainer == null) {
			scopeContainer = new TabbedTextViewer(this, "Scope");
		}
		return scopeContainer;
	}

	public TabbedTextViewer getRetellingContainer() {
		if (retellingContainer == null) {
			retellingContainer = new TabbedTextViewer(this, "Retelling");
		}
		return retellingContainer;
	}

	public TabbedTextViewer getStoryContainer() {
		if (storyContainer == null) {
			storyContainer = new TabbedTextViewer(this, "Story");
		}
		return storyContainer;
	}

	public TabbedTextViewer getConceptContainer() {
		if (conceptContainer == null) {
			conceptContainer = new TabbedTextViewer(this, "Concepts");
		}
		return conceptContainer;
	}

	// public TextViewer getConceptViewer() {
	// if (conceptViewer == null) {
	// conceptViewer = new TextViewer(getResultContainer());
	// conceptViewer.setName("Concept knowledge");
	// }
	// return conceptViewer;
	// }

	// public TextViewer getMovieDiscussionViewer() {
	// if (movieDiscussionViewer == null) {
	// movieDiscussionViewer = new TextViewer(getResultContainer());
	// movieDiscussionViewer.setName("Video discussion");
	// }
	// return movieDiscussionViewer;
	// }
	//
	//
	//
	// public TextViewer getPlotUnitAnalysisViewer() {
	// if (plotUnitAnalysisViewer == null) {
	// plotUnitAnalysisViewer = new TextViewer(getResultContainer());
	// plotUnitAnalysisViewer.setName("Concept analysis");
	// }
	// return plotUnitAnalysisViewer;
	// }

	CommandList commandList;

	public CommandList getCommandList() {
		if (commandList == null) {
			Mark.say("Creating commentary container");
			commandList = new CommandList("Command list");
		}
		return commandList;
	}

	public static void main(String[] ignore) {
		// Mark.say("File is", findFile("Macbeth2.txt"));
		// Mark.say("URL is", FileSourceReader.findURL("Macbeth2.txt"));
		// Mark.say(System.getProperty("java.io.tmpdir"));
		JFrame frame = new JFrame();
		JEditorPane editor = null;
		try {
			URL url = new URL("http://people.csail.mit.edu/phw/genesis-runs.html");
			editor = new JEditorPane(url);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		frame.getContentPane().add(editor);
		frame.pack();
		frame.setVisible(true);

	}

	public EntityMemory getThingMemory() {
		if (thingMemory == null) {
			thingMemory = new EntityMemory();
		}
		return thingMemory;
	}

	public IdiomSplitter getIdiomSplitter() {
		if (idiomSplitter == null) {
			idiomSplitter = new IdiomSplitter();
			idiomSplitter.setName("Idiom splitter");
		}
		return idiomSplitter;
	}

	public Combinator getCombinator() {
		if (combinator == null) {
			combinator = new Combinator();
			combinator.setName("Combinator");
		}
		return combinator;
	}

	public void setLeftPanelToOnset(Object ignore) {
		clickToValue(Switch.showOnsetSwitch, true);
		setLeftPanel("Onsets");
	}

	public void setRightPanelToResults(Object ignore) {
		setRightPanel("Results");
	}

	public void setBottomPanelToImagination(Object ignore) {
		Mark.say("C");
		setBottomPanel("Imagination");
	}

	// public ButtonGroup getLeftRightGroup() {
	// if (leftRightGroup == null) {
	// leftRightGroup = new ButtonGroup();
	// leftRightGroup.add(left);
	// leftRightGroup.add(right);
	// leftRightGroup.add(both);
	// left.setSelected(true);
	// MyPerspectiveListener l = new MyPerspectiveListener();
	// left.addActionListener(l);
	// right.addActionListener(l);
	// both.addActionListener(l);
	// }
	// return leftRightGroup;
	// }

	public GenesisGetters() {
		setName("Getters");
		// Connections.getPorts(this).addSignalProcessor(IDIOM,
		// "processStageDirection");
		// Connections.getPorts(this).addSignalProcessor(this.SET_RIGHT_PANEL_TO_RESULTS, "setRightPanelToResults");
		// Connections.getPorts(this).addSignalProcessor(this.SET_BOTTOM_PANEL_TO_IMAGINATION,
		// "setBottomPanelToImagination");
		Connections.getPorts(this).addSignalProcessor(this.SET_LEFT_PANEL_TO_ONSET, this::setLeftPanelToOnset);

		Connections.getPorts(this).addSignalProcessor(PortNames.SET_PANE, this::setPanel);
		// leftButton = left;
		// rightButton = right;
		// bothButton = both;

		Connections.getPorts(this).addSignalProcessor(IDIOM, this::processIdiom);

		computeContributors();

	}

	public void processIdiom(Object o) {
		if (o == Markers.ACTUATE_SUMMARIZER) {
			getSummarizer().getGateKeeper().setSelected(true);
		}
	}

	public void setLeftPanel(String identifier) {
		getWindowGroupManager().setGuts(getLeftPanel(), identifier);
		// Mark.say("Setting left panel memory to", identifier);
		Preferences.userRoot().put(GenesisConstants.LEFT, identifier);
	}

	public void setRightPanel(String identifier) {
		getWindowGroupManager().setGuts(getRightPanel(), identifier);
		// Mark.say("Setting right panel memory to", identifier);
		Preferences.userRoot().put(GenesisConstants.RIGHT, identifier);
	}

	public void setBottomPanel(String identifier) {
		getWindowGroupManager().setGuts(getBottomPanel(), identifier);
		// Mark.say("Setting bottom panel memory to", identifier);
		Preferences.userRoot().put(GenesisConstants.BOTTOM, identifier);
	}

	public void setPanel(Object o) {
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			Object guts = null;
			if (signal.size() >= 2) {
				String identifier = signal.get(1, String.class);
				String panel = signal.get(0, String.class);
				if (panel == GenesisConstants.LEFT) {
					setLeftPanel(identifier);
					guts = getLeftPanel().getGuts();

				}
				else if (panel == GenesisConstants.RIGHT) {
					setRightPanel(identifier);
					guts = getRightPanel().getGuts();
				}
				else if (panel == GenesisConstants.BOTTOM) {
					setBottomPanel(identifier);
					guts = getBottomPanel().getGuts();
				}
			}
			if (signal.size() == 3 && guts != null && guts instanceof TabbedTextViewer) {
				String tab = signal.get(2, String.class);
				TabbedTextViewer ttv = (TabbedTextViewer) guts;
				Mark.say("Switching to", tab);
				ttv.switchTab(tab);
			}
		}
	}

	public WiredBlinkingBox getAgentBlinker() {
		if (agentBlinker == null) {
			agentBlinker = new WiredBlinkingBox("Agent", getAgentExpert(), new RelationViewer(), getExpertsPanel());
		}
		return agentBlinker;
	}

	public AgentExpert getAgentExpert() {
		if (agentExpert == null) {
			agentExpert = new AgentExpert();
		}
		return agentExpert;
	}

//	public WiredBlinkingBox getPictureBlinker() {
//		if (pictureBlinker == null) {
//			pictureBlinker = new WiredBlinkingBox("Picture", getRachelsPictureExpert(), new PictureViewer(), getExpertsPanel());
//			pictureBlinker.setBlinkSwitch(false);
//
//		}
//		return pictureBlinker;
//	}

	public WiredBlinkingBox getRoleBlinker() {
		if (roleBlinker == null) {
			roleBlinker = new WiredBlinkingBox("Role frame", getRoleExpert(), new RoleViewer(), getExpertsPanel());
		}
		return roleBlinker;
	}

	public RoleExpert getRoleExpert() {
		if (roleExpert == null) {
			roleExpert = new RoleExpert();
		}
		return roleExpert;
	}

	public WiredBlinkingBox getJobBlinker() {
		if (jobBlinker == null) {
			jobBlinker = new WiredBlinkingBox("Job", getJobExpert(), new RelationViewer(), getExpertsPanel());
		}
		return jobBlinker;
	}

	public JobExpert getJobExpert() {
		if (jobExpert == null) {
			jobExpert = new JobExpert();
		}
		return jobExpert;
	}

	public WiredBlinkingBox getPersonalityBlinker() {
		if (personalityBlinker == null) {
			personalityBlinker = new WiredBlinkingBox("Personality", getPersonalityExpert(), new RelationViewer(), getExpertsPanel());
		}
		return personalityBlinker;
	}

	public WiredBlinkingBox getPropertyBlinker() {
		if (propertyBlinker == null) {
			propertyBlinker = new WiredBlinkingBox("Property", getPropertyExpert(), new RelationViewer(), getExpertsPanel());
		}
		return propertyBlinker;
	}

	public PersonalityExpert getPersonalityExpert() {
		if (personalityExpert == null) {
			personalityExpert = new PersonalityExpert();
		}
		return personalityExpert;
	}

	public PropertyExpert getPropertyExpert() {
		if (propertyExpert == null) {
			propertyExpert = new PropertyExpert();
		}
		return propertyExpert;
	}

	public WiredBlinkingBox getPartBlinker() {
		if (partBlinker == null) {
			partBlinker = new WiredBlinkingBox("Part", getPartExpert(), new PartPanel(), getExpertsPanel());
		}
		return partBlinker;
	}

	public PartExpert getPartExpert() {
		if (partExpert == null) {
			partExpert = new PartExpert();
		}
		return partExpert;
	}

	public WiredBlinkingBox getComparisonBlinker() {
		if (comparisonBlinker == null) {
			comparisonBlinker = new WiredBlinkingBox("Comparison", getComparisonExpert(), new ComparisonViewer(), getExpertsPanel());
		}
		return comparisonBlinker;
	}

	public WiredBlinkingBox getSocialBlinker() {
		if (socialBlinker == null) {
			socialBlinker = new WiredBlinkingBox("Social", getSocialExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return socialBlinker;
	}

	public SocialExpert getSocialExpert() {
		if (socialExpert == null) {
			socialExpert = new SocialExpert();
		}
		return socialExpert;
	}

	public WiredBlinkingBox getMoodBlinker() {
		if (moodBlinker == null) {
			moodBlinker = new WiredBlinkingBox("Mood", getMoodExpert(), new MoodViewer(), getExpertsPanel());
		}
		return moodBlinker;
	}

	public MoodExpert getMoodExpert() {
		if (moodExpert == null) {
			moodExpert = new MoodExpert();
		}
		return moodExpert;
	}

	public WiredBlinkingBox getBeliefBlinker() {
		if (beliefBlinker == null) {
			beliefBlinker = new WiredBlinkingBox("Belief", getBeliefExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return beliefBlinker;
	}

	public BeliefExpert getBeliefExpert() {
		if (beliefExpert == null) {
			beliefExpert = new BeliefExpert();
		}
		return beliefExpert;
	}

	public WiredBlinkingBox getPredictionBlinker() {
		if (predictionBlinker == null) {
			predictionBlinker = new WiredBlinkingBox("Prediction", getPredictionExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return predictionBlinker;
	}

	public ExpectationExpert getPredictionExpert() {
		if (expectationExpert == null) {
			expectationExpert = new ExpectationExpert();
		}
		return expectationExpert;
	}

	public WiredBlinkingBox getIntentionBlinker() {
		if (intentionBlinker == null) {
			intentionBlinker = new WiredBlinkingBox("Intention", getIntentionExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return intentionBlinker;
	}

	public IntentionExpert getIntentionExpert() {
		if (intentionExpert == null) {
			intentionExpert = new IntentionExpert();
		}
		return intentionExpert;
	}

	public WiredBlinkingBox getPersuationBlinker() {
		if (persuationBlinker == null) {
			persuationBlinker = new WiredBlinkingBox("Persuation", getPersuationExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return persuationBlinker;
	}

	public PersuationExpert getPersuationExpert() {
		if (persuationExpert == null) {
			persuationExpert = new PersuationExpert();
		}
		return persuationExpert;
	}

	public WiredBlinkingBox getCoercionBlinker() {
		if (coercionBlinker == null) {
			coercionBlinker = new WiredBlinkingBox("Coercion", this.getCoerceInterpreter(), new ForceViewer(), getExpertsPanel());
		}
		return coercionBlinker;
	}

	public CoercionExpert getCoercionExpert() {
		if (coercionExpert == null) {
			coercionExpert = new CoercionExpert();
		}
		return coercionExpert;
	}

	public WiredBlinkingBox getPossessionBlinker() {
		if (possessionBlinker == null) {
			possessionBlinker = new WiredBlinkingBox("Possession", this.getPossessionExpert(), new PossessionPanel(), getExpertsPanel());
		}
		return possessionBlinker;
	}

	public PossessionExpert getPossessionExpert() {
		if (possessionExpert == null) {
			possessionExpert = new PossessionExpert();
		}
		return possessionExpert;
	}

	public WiredBlinkingBox getThreadBlinker() {
		if (threadBlinker == null) {
			threadBlinker = new WiredBlinkingBox("Class", getThreadExpert(), new ThreadViewer(), getExpertsPanel());
		}
		return threadBlinker;
	}

	public ThreadExpert getThreadExpert() {
		if (threadExpert == null) {
			threadExpert = new ThreadExpert();
		}
		return threadExpert;
	}

	public WiredBlinkingBox getTrajectoryBlinker() {
		if (trajectoryBlinker == null) {
			trajectoryBlinker = new WiredBlinkingBox("Trajectory", getTrajectoryExpert(), new TrajectoryViewer(), getExpertsPanel());
		}
		return trajectoryBlinker;
	}

	public TrajectoryExpert getTrajectoryExpert() {
		if (trajectoryExpert == null) {
			trajectoryExpert = new TrajectoryExpert();
		}
		return trajectoryExpert;
	}

	public PathExpert getPathExpert() {
		if (pathExpert == null) {
			pathExpert = new PathExpert();
		}
		return pathExpert;
	}

	public WiredBlinkingBox getPathElementBlinker() {
		if (pathElementBlinkder == null) {
			pathElementBlinkder = new WiredBlinkingBox("Path", getPathElementExpert(), new PathElementViewer(), getExpertsPanel());
		}
		return pathElementBlinkder;
	}

	public PathElementExpert getPathElementExpert() {
		if (pathElementExpert == null) {
			pathElementExpert = new PathElementExpert();
		}
		return pathElementExpert;
	}

	public WiredBlinkingBox getPlaceBlinker() {
		if (placeBlinker == null) {
			placeBlinker = new WiredBlinkingBox("Place", getPlaceExpert(), new PlaceViewer(), getExpertsPanel());
		}
		return placeBlinker;
	}

	public PlaceExpert getPlaceExpert() {
		if (placeExpert == null) {
			placeExpert = new PlaceExpert();
		}
		return placeExpert;
	}

	public WiredBlinkingBox getCauseBlinker() {
		if (causeProbeBlinkingBox == null) {
			causeProbeBlinkingBox = new WiredBlinkingBox("Cause", getCauseExpert(), new NewFrameViewer(), getExpertsPanel());
		}
		return causeProbeBlinkingBox;
	}

	public CauseExpert getCauseExpert() {
		if (causeExpert == null) {
			causeExpert = new CauseExpert();
		}
		return causeExpert;
	}

	public WiredBlinkingBox getGoalBlinker() {
		if (goalBlinker == null) {
			goalBlinker = new WiredBlinkingBox("Goal", getGoalExpert(), new GoalPanel(), getExpertsPanel());
		}
		return goalBlinker;
	}

	public GoalExpert getGoalExpert() {
		if (goalExpert == null) {
			goalExpert = new GoalExpert();
		}
		return goalExpert;
	}

//	public WiredBlinkingBox getTransitionBlinker() {
//		if (transitionBlinker == null) {
//			transitionBlinker = new WiredBlinkingBox("Transition", getTransitionExpert(), new TransitionViewer(), getExpertsPanel());
//		}
//		return transitionBlinker;
//	}

//	public TransitionExpert getTransitionExpert() {
//		if (transitionExpert == null) {
//			transitionExpert = new TransitionExpert();
//		}
//		return transitionExpert;
//	}

	public WiredBlinkingBox getTimeBlinker() {
		if (timeBlinker == null) {
			timeBlinker = new WiredBlinkingBox("Time", getTimeExpert(), new TimeViewer(), getExpertsPanel());
		}
		return timeBlinker;
	}

	public TimeExpert getTimeExpert() {
		if (timeExpert == null) {
			timeExpert = new TimeExpert();
		}
		return timeExpert;
	}

	public WiredBlinkingBox getTransferBlinker() {
		if (transferBlinker == null) {
			transferBlinker = new WiredBlinkingBox("Transfer", getTransferExpert(), new TransferViewer(), getExpertsPanel());
		}
		return transferBlinker;
	}

	public TransferExpert getTransferExpert() {
		if (transferExpert == null) {
			transferExpert = new TransferExpert();
		}
		return transferExpert;
	}

	public JPanel getPersonalGuiPanel() {
		if (personalGuiPanel == null) {
			personalGuiPanel = new JPanel();
			personalGuiPanel.setName("Personal panel");
		}
		return personalGuiPanel;
	}

//	public SimilarityViewer getSimilarityViewer() {
//		if (similarityViewer == null) {
//			similarityViewer = new SimilarityViewer();
//			similarityViewer.initialize(getSimilarityProcessor());
//			similarityViewer.setName("Similarity panel");
//		}
//		return similarityViewer;
//	}
//
//	public SimilarityProcessor getSimilarityProcessor() {
//		if (similarityProcessor == null) {
//			similarityProcessor = new SimilarityProcessor(getSimilarityViewer());
//		}
//		return similarityProcessor;
//	}

	public JPanel getPersonalButtonPanel() {
		if (personalButtonPanel == null) {
			personalButtonPanel = new JPanel();
			getControls().addTab("Personal controls", personalButtonPanel);

		}
		return personalButtonPanel;
	}

	// private class MyPerspectiveListener implements ActionListener {
	// public void actionPerformed(ActionEvent e) {
	// Mark.say("Action not yet implemented");
	// }
	//
	// }

	MindsEyeProcessor mindsEyeProcessor;

	public MindsEyeProcessor getMindsEyeProcessor() {
		if (mindsEyeProcessor == null) {
			mindsEyeProcessor = new MindsEyeProcessor();
		}
		return mindsEyeProcessor;
	}

	// public GapFiller getGapFiller() {
	// if (gapFiller == null) {
	// gapFiller = new GapFiller();
	// }
	// return gapFiller;
	// }
	//
	// public GapViewer getGapViewer() {
	// if (gapViewer == null) {
	// gapViewer = new GapViewer();
	// getWindowGroupManager().addJComponent(gapViewer);
	// }
	// return gapViewer;
	// }

	private CharacterProcessor characterProcessor = null;

	public CharacterProcessor getCharacterProcessor() {
		if (characterProcessor == null) {
			characterProcessor = new CharacterProcessor();
		}
		return characterProcessor;
	}

	public AlignmentViewer getAlignmentViewer() {
		if (alignmentViewer == null) {
			alignmentViewer = new AlignmentViewer();
		}
		return alignmentViewer;
	}

	public CharacterViewer getCharacterViewer() {
		if (characterViewer == null) {
			characterViewer = new CharacterViewer();
		}
		return characterViewer;
	}

	public TraitViewer getTraitViewer() {
		if (traitViewer == null) {
			traitViewer = TraitViewer.getTraitViewer();
		}
		return traitViewer;
	}

	private StoryThreadingViewer storyThreadingViewer;

	public StoryThreadingViewer getStoryThreadingViewer() {
		if (storyThreadingViewer == null) {
			storyThreadingViewer = new StoryThreadingViewer();
		}
		return storyThreadingViewer;
	}

	private AlignmentProcessor alignmentProcessor;

	public AlignmentProcessor getAlignmentProcessor() {
		if (alignmentProcessor == null) alignmentProcessor = new AlignmentProcessor();
		return alignmentProcessor;
	}

	private StoryThreadingProcessor storyThreadingProcessor = null;

	public StoryThreadingProcessor getStoryThreadingProcessor() {
		if (storyThreadingProcessor == null) {
			storyThreadingProcessor = new StoryThreadingProcessor();
		}
		return storyThreadingProcessor;
	}

	private ClusterProcessor clusterProcessor;

	public ClusterProcessor getClusterProcessor() {
		if (clusterProcessor == null) {
			clusterProcessor = new ClusterProcessor();
		}
		return clusterProcessor;
	}

	EntityExpert entityExpert;

	public EntityExpert getEntityExpert() {
		if (entityExpert == null) {
			entityExpert = new EntityExpert();
		}
		return entityExpert;

	}

	private WhatIfContainer whatIfContainer;

	/**
	 * Dylan's version always returned a new container each time, only one wired up.
	 *
	 * @return
	 */
	// public WhatIfContainer getWhatIfContainer() {
	// return whatIfContainer == null ? new WhatIfContainer() : whatIfContainer;
	// // if (whatIfContainer == null) {
	// // whatIfContainer = new JLabel("This is where the what-if display will be", JLabel.CENTER);
	// // whatIfContainer.setName("What if?");
	// // }
	// // return whatIfContainer;
	// }

	public WhatIfContainer getWhatIfContainer() {
		if (whatIfContainer == null) {
			whatIfContainer = new WhatIfContainer();
		}
		return whatIfContainer;
	}

	public JComponent getIViewer1() {
		if (iViewer1 == null) {
			iViewer1 = getMentalModel1().getI().getPlotDiagram();
			iViewer1.setName("Introspector");
		}
		return iViewer1;
	}

	// public static JFXPanel getCharacterVisualizer() {
	// if (characterVisualizer == null) {
	// characterVisualizer = new JFXPanel();
	// characterVisualizer.setName("Character visualizer");
	// Scene scene = CharacterVisualization.createScene();
	// characterVisualizer.setScene(scene);
	// }
	// return characterVisualizer;
	// }


	RealTimeBriefer realTimeBriefer;

	public RealTimeBriefer getRealTimeBriefer() {
		if (realTimeBriefer == null) {
			realTimeBriefer = new RealTimeBriefer("Briefer");
		}
		return realTimeBriefer;
	}

	JPanel briefingPanelWrapper;

	public JPanel getBriefingPanelWrapper() {
		if (briefingPanelWrapper == null) {
			briefingPanelWrapper = new JPanel();
			briefingPanelWrapper.setLayout(new BorderLayout());
			briefingPanelWrapper.add(getBriefingPanel(), BorderLayout.CENTER);
			briefingPanelWrapper.add(RealTimeBriefer.getProceedButton(), BorderLayout.SOUTH);
			briefingPanelWrapper.setName("Briefing");
		}
		return briefingPanelWrapper;
	}

	public TextViewer getBriefingPanel() {
		if (briefingPanel == null) {
			briefingPanel = new TextViewer();
			briefingPanel.setName("Briefing panel");
		}
		return briefingPanel;
	}

	private static JPanel blocks;

	private static BlocksWorldApplication blocksWorld;

	public static BlocksWorldApplication getBlocksWorld() {
		// To ensure exists
		getBlocksWorldViewer();
		return blocksWorld;
	}

	public static JComponent getBlocksWorldViewer() {
		if (blocks == null) {
			blocks = new JPanel();
			blocks.setName("Blocks");
			blocks.setLayout(new BorderLayout());
			blocksWorld = new BlocksWorldApplication();
			blocks.add(blocksWorld.getView());
			blocks.add(blocksWorld.getControlBar(), BorderLayout.NORTH);
		}
		return blocks;
	}


	// -----------------------------
	//
	// 		II : All the interface related methods
	//
	// -----------------------------
	protected void changeState(Object o) {
		// Mark.say("Changing state", o == Genesis.OPEN);
		try {
			if (o == GenesisGetters.OPEN) {
				// Mark.say("Opening interface");
				GenesisGetters.this.getTrafficLight().setGreen(true);
				GenesisGetters.this.getTextEntryBox().setEnabled(true);
				GenesisGetters.this.experienceButton.setEnabled(true);
				// Getters.this.focusButton.setEnabled(true);
				GenesisGetters.this.disambiguationButton.setEnabled(true);
				GenesisGetters.this.wordnetPurgeButton.setEnabled(true);
				GenesisGetters.this.startPurgeButton.setEnabled(true);
				GenesisGetters.this.conceptNetPurgeButton.setEnabled(true);
				// Getters.this.clearMemoryButton.setEnabled(true);
				GenesisGetters.this.eraseTextButton.setEnabled(true);
				GenesisGetters.this.testSentencesButton.setEnabled(true);
				GenesisGetters.this.testOperationsButton.setEnabled(true);
				GenesisGetters.this.testStoriesButton.setEnabled(true);
				GenesisGetters.this.demonstrateConnectionsButton.setEnabled(true);
				GenesisGetters.this.demonstrateConceptsButton.setEnabled(true);
				GenesisGetters.this.rereadFile.setEnabled(true);
				GenesisGetters.this.rerunExperiment.setEnabled(true);
				GenesisGetters.this.loopButton.setEnabled(true);

			}
			else if (o == GenesisGetters.CLOSE) {
				GenesisGetters.this.getTrafficLight().setRed(true);
				// Mark.say("Closing interface");
				// Getters.this.getTextEntryBox().setEnabled(false);
				GenesisGetters.this.experienceButton.setEnabled(false);
				// Getters.this.focusButton.setEnabled(false);
				GenesisGetters.this.disambiguationButton.setEnabled(false);
				GenesisGetters.this.wordnetPurgeButton.setEnabled(false);
				GenesisGetters.this.startPurgeButton.setEnabled(false);
				// Getters.this.clearMemoryButton.setEnabled(false);
				GenesisGetters.this.eraseTextButton.setEnabled(false);
				GenesisGetters.this.testSentencesButton.setEnabled(false);
				GenesisGetters.this.testOperationsButton.setEnabled(false);
				GenesisGetters.this.testStoriesButton.setEnabled(false);
				GenesisGetters.this.demonstrateConnectionsButton.setEnabled(false);
				GenesisGetters.this.demonstrateConceptsButton.setEnabled(false);
				GenesisGetters.this.rereadFile.setEnabled(false);
				GenesisGetters.this.rerunExperiment.setEnabled(false);
				GenesisGetters.this.loopButton.setEnabled(false);

			}
		}
		catch (RuntimeException e) {
			System.err.println("Blew out of opening or closing interface");
			e.printStackTrace();
		}
	}

	protected void closeGates() {
		// System.out.println("Closing gates");
		// this.getImaginationGate().close();
		// this.getMemoryGate().close();
	}

	public void closeInterface() {
		changeState(GenesisGetters.CLOSE);
	}

	public GenesisGetters getApplication() {
		return this;
	}


	public JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setLeftComponent(getLeftPanel());
			splitPane.setRightComponent(getRightPanel());
			splitPane.setOneTouchExpandable(true);
			splitPane.setOpaque(false);
			splitPane.setResizeWeight(0.5);

			splitPane.addPropertyChangeListener(new MyEastWestListener());

			splitPane.getRightComponent().setMinimumSize(new Dimension());

			splitPane.getLeftComponent().setMinimumSize(new Dimension());

			int dividerLocation = Preferences.userRoot().getInt("eastWestDivider", 200);

			if (dividerLocation > 100) {
				dividerLocation += 50;
			}

			// Mark.say("Setting divider to", dividerLocation);

			splitPane.setDividerLocation(dividerLocation);
		}
		return splitPane;
	}

	public JSplitPane getNorthSouthSplitPane() {
		if (northSouthSplitPane == null) {
			northSouthSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			northSouthSplitPane.setDividerSize(20);

			northSouthSplitPane.setOneTouchExpandable(true);

			northSouthSplitPane.setTopComponent(getTopPanel());
			northSouthSplitPane.setBottomComponent(getBottomPanel());

			Dimension minimumSize = new Dimension(100, 0);
			getTopPanel().setMinimumSize(minimumSize);
			getBottomPanel().setMinimumSize(minimumSize);

			Dimension preferredSize = new Dimension(1000, 0);

			getTopPanel().setPreferredSize(preferredSize);
			getBottomPanel().setPreferredSize(preferredSize);

			// northSouthSplitPane.setDividerLocation(400);
			northSouthSplitPane.setResizeWeight(0.5);

			northSouthSplitPane.addPropertyChangeListener(new MyNorthSouthListener());

			northSouthSplitPane.getBottomComponent().setMinimumSize(new Dimension());

			int dividerLocation = Preferences.userRoot().getInt("northSouthDivider", 200);

			if (dividerLocation > 100) {
				dividerLocation += 50;
			}

			northSouthSplitPane.setDividerLocation(dividerLocation);

		}
		return northSouthSplitPane;
	}

	private class MyEastWestListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("dividerLocation".equals(evt.getPropertyName())) {
				int newValue = (int) (evt.getNewValue());
				// Mark.say("Setting divider to", newValue);
				Preferences.userRoot().putInt("eastWestDivider", newValue);
			}
		}

	}

	private class MyNorthSouthListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("dividerLocation".equals(evt.getPropertyName())) {
				int newValue = (int) (evt.getNewValue());
				Preferences.userRoot().putInt("northSouthDivider", newValue);
			}
		}

	}

	// the main wiring function!! commented by Z
	public WindowGroupManager getWindowGroupManager() {
		if (windowGroupManager == null) {
			
			// the first 10 panels will show on main menu
			windowGroupManager = new WindowGroupManager(10);
			windowGroupManager.addJComponent(getControls()); // Do not add any subsystems
//			windowGroupManager.addJComponent(getTabbedSubsystems());
			windowGroupManager.addJComponent(getStartViewer());
//			windowGroupManager.addJComponent(getExpertsPanel());
			windowGroupManager.addJComponent(getElaborationPanel());
			windowGroupManager.addJComponent(getInspectorPanel());
//			windowGroupManager.addJComponent(getFancySimulator());
			windowGroupManager.addJComponent(getSourceContainer());
			windowGroupManager.addJComponent(getResultContainer());
			windowGroupManager.addJComponent(getWiringDiagram());

			// the rest of the panels will show in the drop-down menu
			windowGroupManager.addJComponent(getSummaryContainer());
			windowGroupManager.addJComponent(getRetellingContainer());
			windowGroupManager.addJComponent(getStoryContainer());
//			windowGroupManager.addJComponent(getWhatIfContainer());
//			windowGroupManager.addJComponent(getSimilarityViewer());
			windowGroupManager.addJComponent(getDictionary());
			windowGroupManager.addJComponent(getWordNetGUI());
			windowGroupManager.addJComponent(getPageTranslatorGenerator());
			windowGroupManager.addJComponent(getOnsetPanel());
			windowGroupManager.addJComponent(getKnowledgeWatcherBlinker());
			windowGroupManager.addJComponent(getPredictionsViewer());
			windowGroupManager.addJComponent(getAlignmentViewer());
			windowGroupManager.addJComponent(getTalkBackViewer());
			windowGroupManager.addJComponent(getMentalModelViewer());
			windowGroupManager.addJComponent(getRecallPanel());
			windowGroupManager.addJComponent(getWordRecallPanel());
			windowGroupManager.addJComponent(getCausalTextView());// added by Hiba
			windowGroupManager.addJComponent(ExperimentExportProcessor.getExperimentExportProcessor());
			windowGroupManager.addJComponent(getHumorDisplay());
			windowGroupManager.addJComponent(getJessicasDisplay());
//			windowGroupManager.addJComponent(getIViewer1());   // commented by Zhutian

			// Anna needs following, but may screw up others with older Java
			// windowGroupManager.addJComponent(getCharacterVisualizer());
			windowGroupManager.addJComponent(getBriefingPanelWrapper());
			windowGroupManager.addJComponent(getBlocksWorldViewer());

			// added by Zhutian for Event2State
			// Commented out by phw 31 Oct 2018
			// uncommented by Z on 6 Nov 2018
			windowGroupManager.addJComponent(getPageHowToBookLearner());
			windowGroupManager.addJComponent(getPageNoviceLearner());
//			windowGroupManager.addJComponent(getPageStoryLearner());

			windowGroupManager.addJComponent(getJenniferPanel());
			windowGroupManager.addJComponent(getPageStoryAligner());

		}
		return windowGroupManager;
	}



}

package genesis;

import java.awt.*;
import java.util.Arrays;

import javax.swing.*;

//import adaTaylor.humor.HumorExpert;
//import carolineAronoff.PsychologicallyPlausibleModel;
import connections.ButtonSwitchBox;
import consciousness.RecipeFinder;
import constants.Radio;
import constants.Switch;
import dylanHolmes.MeansEndsProcessor;
import expert.EscalationExpert;
import expert.JessicasExpert;
import gui.*;
import gui.panels.BorderedParallelJPanel;
import gui.panels.ParallelJPanel;
//import olgaShestopalova.PredictionExpert;
import subsystems.ExplanationBox;
import subsystems.rashi.RashisExperts;
import subsystems.rashi.SummarizeIntentions;
import subsystems.recall.StoryRecallExpert;
import subsystems.summarizer.Persuader;
import subsystems.summarizer.Summarizer;
import expert.StatisticsExpert;

import utils.*;
import zhutianYang.StoryAligner;

/*
 * Created on May 8, 2010
 * @author phw
 */

public class GenesisControls extends GenesisMenus {

	private SwitchPanel storyTellingControls;

	private SwitchPanel schizophreniaControls;

	private SwitchPanel conceptNetControls;

	private SwitchPanel rashiControls;

	private SwitchPanel expertControls;

	private SwitchPanel badGrammarControls;

	private SwitchColumns storyReadingControls;

	private SwitchPanel storySummaryControls;

	private SwitchColumns humorControls;

	private BorderedParallelJPanel storyPersuasionControls;

	protected static JButton nextButton;

	protected static JButton runButton;

	private ButtonSwitchBox syntaxToSemanticsSwitchBox;

//	 protected JRadioButton left = new JRadioButton("P1");
//	
//	 protected JRadioButton right = new JRadioButton("P2");
//	
//	 protected JRadioButton both = new JRadioButton("Both");

	public static JRadioButton leftButton;

	public static JRadioButton rightButton;

	public static JRadioButton bothButton;

	public static JComboBox teachingLevel;

	public static JCheckBoxMenuItem collaboration;

	public static JButton makeLargeVideoRecordingButton = new JButton("Set video recording dimensions to 1600 x 1200 (4:3)");

	public static JButton makeSmallVideoRecordingButton = new JButton("Set video recording dimensions to 1024 x 768 (4:3, best for PowerPoint)");

	public static JButton makeMediumVideoRecordingButton = new JButton("Set video recording dimensions to 1280 x 1024");

	public static JButton makeCoUButton = new JButton("Set video recording dimensions to 1920 x 1080 (best for 10-250)");

	protected JButton wordnetPurgeButton = new JButton("Purge WordNet cache");

	protected JButton startPurgeButton = new JButton("Purge Start cache");

	protected JButton conceptNetPurgeButton = new JButton("Purge ConceptNet cache");

	protected JButton runAligner = new JButton("Run Aligner");

	protected JButton clearMemoryButton = new JButton("Clear memory");

	protected JButton eraseTextButton = new JButton("Erase text");

	protected JButton disambiguationButton = new JButton("Load disambiguating events");

	protected JButton experienceButton = new JButton("Load visual events");

	protected JButton focusButton = new JButton("Select focus experience");

	protected JButton clearSummaryTableButton = new JButton("Clear summary table");

	protected ButtonGroup leftRightGroup;

	private JTabbedPane controls;

	private JTabbedPane tabbedSubsystems;

	private static TabbedMentalModelViewer mentalModelViewer;

	private ParallelJPanel conceptOptionsPanel;

	private ParallelJPanel behaviorOptionsPanel;

	private SwitchColumns summaryPersuaderPanel;

	private SwitchColumns consciousnessPanel;

	private ParallelJPanel subsystemsPanel;

	private ParallelJPanel storyTellingButtons = null;

	private ParallelJPanel debuggingPanel;

	private ParallelJPanel checkInPanel;

	private ParallelJPanel co57Panel;

	private ParallelJPanel videoPanel;

	private ParallelJPanel graveyardPanel;

	private ParallelJPanel actionButtons;

	protected JButton testSentencesButton = new JButton("Run test sentences");

	protected JButton testStoriesButton = new JButton("Run test stories");

	protected JButton testOperationsButton = new JButton("Run test operations");

	protected JButton demonstrateConnectionsButton = new JButton("Demonstrate connection types");

	protected JButton demonstrateConceptsButton = new JButton("Demonstrate concept options");

	protected JButton rerunExperiment = new JButton("Run experiment again");

	protected JButton rereadFile = new JButton("Read file again");

	protected JButton demoSimulator = new JButton("Demo simulator");

	protected JButton debugVideoFileButton = new JButton("Demo video story");

	protected JButton loopButton = new JButton("Run loop");

	protected JButton kjFileButton = new JButton("Korea/Japen");

	protected JButton kjeFileButton = new JButton("Korea/Japan-Estonia/Russia");

	protected JButton loadVideoPrecedents = new JButton("Load precedents");

	protected JButton visionFileButton = new JButton("Vision commands");

	protected JButton debugButton1 = new JButton("Debug text in debug1.txt");

	protected JButton debugButton2 = new JButton("Debug text in debug2.txt");

	protected JButton debugButton3 = new JButton("Debug text in debug3.txt");

	protected JButton runWorkbenchTest = new JButton("Run Story Workbench test");

	protected JButton simulateCo57Button = new JButton("Test/Simulate Co57");

	protected JButton connectTestingBox = new JButton("Connect story testing box");

	protected JButton disconnectTestingBox = new JButton("Disconnect story testing box");

	protected JButton test1Button = new JButton("Check in test 1");

	protected JButton test2Button = new JButton("Check in test 2");

	public static JCheckBox useNewMatcherCheckBox = new JCheckBox("Use unified matcher", true);

	public static JCheckBox matchAllThreads = new JCheckBox("Match all threads", true);

	public static JCheckBox reportMatchingDifferencesCheckBox = new JCheckBox("Report Matcher Differences", false);

	public static JRadioButton co57LocalPassthrough = new JRadioButton("Use Local Passthrough Server", true);

	public static JRadioButton co57Passthrough = new JRadioButton("Use Co57 Passthrough Server");

	public static JRadioButton co57SimulatorAndTranslator = new JRadioButton("Use Beryl Simulator Translator", true);

	public static JRadioButton co57JustTranslator = new JRadioButton("Only start Beryl Translator");

	private EscalationExpert escalationExpert1;

	private EscalationExpert escalationExpert2;

	private StoryRecallExpert storyRecallExpert1;

	private StoryRecallExpert storyRecallExpert2;

//	private PredictionExpert predictionExpert1;

	private StatisticsExpert statisticsExpert;

	private JessicasExpert jessicasExpert;

	//private CarolinesExpert carolinesExpert;

//	private PsychologicallyPlausibleModel carolinesExpert;

	private MeansEndsProcessor dylansExpert;

//	private HumorExpert humorExpert;

	public static JLabel testStartConnection = new JLabel("Test");

	class BorderedSwitchPanelWithExplanation extends JPanel {
		BorderedSwitchPanel bsp;

		public BorderedSwitchPanelWithExplanation(String name, String description) {
			setLayout(new BorderLayout());
			bsp = new BorderedSwitchPanel(name);
			super.add(bsp, BorderLayout.CENTER);
			ExplanationBox box = new ExplanationBox(description);
			super.add(box, BorderLayout.SOUTH);
		}

		public void add(JToggleButton b) {
			bsp.addLeft(b);
		}

		public void add(JToggleButton b, String e) {
			add(b);
			bsp.addCenter(e);
		}

	}

	/**
	 * Expects arguments in pairs: switch, explanation, but after title
	 */
	class BorderedSwitchPanel extends BorderedParallelJPanel {
		public BorderedSwitchPanel(Object... objects) {
			super((String) (objects[0]));
			setOpaque(true);
			setBackground(Color.WHITE);
			for (int i = 1; i < objects.length; i = i + 2) {
				JToggleButton s = (JToggleButton) (objects[i]);
				String e = (String) (objects[i + 1]);
				this.addLeft(s);
				this.addCenter(e);
			}
		}

		public void add(JToggleButton b) {
			addLeft(b);
		}

		public void add(JToggleButton b, String e) {
			add(b);
			addCenter(e);
		}

	}

	class SwitchPanelWithExplanation extends JPanel {
		SwitchPanel bsp;

		public SwitchPanelWithExplanation(String descriptions) {
			setLayout(new BorderLayout());
			bsp = new SwitchPanel();
			super.add(bsp, BorderLayout.CENTER);
			ExplanationBox box = new ExplanationBox(descriptions);
			super.add(box, BorderLayout.SOUTH);
		}

		public void add(JToggleButton b) {
			bsp.addLeft(b);
		}

		public void add(JToggleButton b, String e) {
			add(b);
			bsp.addCenter(e);
		}

	}

	/**
	 * Expects arguments in pairs: switch, explanation
	 */
	class SwitchPanel extends ParallelJPanel {
		public SwitchPanel(Object... objects) {
			super();
			setOpaque(true);
			setBackground(Color.WHITE);
			for (int i = 0; i < objects.length; i = i + 2) {
				JToggleButton s = (JToggleButton) (objects[i]);
				String e = (String) (objects[i + 1]);
				this.addLeft(s);
				this.addRight(e);
			}
		}

		public void add(JToggleButton b) {
			this.addLeft(b);
		}

		public void add(JToggleButton b, String e) {
			add(b);
			this.addCenter(e);
		}

	}

	class SwitchColumns extends JPanel {
		public SwitchColumns(JPanel... panels) {
			this.setLayout(new GridLayout(1, 0));
			Arrays.asList(panels).stream().forEachOrdered(p -> this.add(p));
		}
	}



	public JTabbedPane getControls() {
		if (controls == null) {
			controls = new JTabbedPane();
			controls.setBackground(Color.WHITE);
			controls.setOpaque(true);
			controls.setName("Controls");
			controls.addTab("Main", getMainMatrix()); // Do not add any subsystems
			// controls.addTab("Subsystems", getTabbedSubsystems());
			// controls.addTab("Read/Tell", getReadTellMatrix());
			setMinimumHeight(controls, 0);
			// controls.addTab("System options", getSystemSwitches());
			controls.addTab("Miscellaneous", getMiscellaneousMatrix());
			if (!Webstart.isWebStart()) {
				controls.addTab("Debugging", getDebuggingMatrix());
				controls.addTab("Graveyard", getGraveyardMatrix());
			}
		}
		return controls;
	}

	public JTabbedPane getTabbedSubsystems() {
		if (tabbedSubsystems == null) {
			tabbedSubsystems = new JTabbedPane();
			tabbedSubsystems.setName("Subsystems");
			tabbedSubsystems.setBackground(Color.WHITE);
			tabbedSubsystems.setOpaque(true);
			tabbedSubsystems.addTab("Reader", getStoryReadingControls());
			tabbedSubsystems.addTab("Summarizer/persuader", getStorySummaryControls());
			tabbedSubsystems.addTab("Self-aware responder", getConsciousnessPanel());
			tabbedSubsystems.addTab("Basic responser", getExplanationPanel());
			tabbedSubsystems.addTab("Story teller", getStoryTellingControls());
			tabbedSubsystems.addTab("Surprise and Humor", getHumorControls());
			tabbedSubsystems.addTab("Schizophrenia", getSchizophreniaControls());
			tabbedSubsystems.addTab("BadGrammar sliders", getBadGrammarControls());
			tabbedSubsystems.addTab("Expert advice link", getExpertControls());
			tabbedSubsystems.addTab("Concept net link", getConceptNetControls());
		}
		return tabbedSubsystems;
	}


	public SwitchColumns getStoryReadingControls() {
		if (storyReadingControls == null) {

			storyReadingControls = new SwitchColumns();

			BorderedSwitchPanel bsp = new BorderedSwitchPanel("Minsky reasoning level");
			bsp.add(Switch.level5UseMentalModels, "Minsky levels 5 & 6, actor-specific thinking");
			bsp.add(Switch.level4ConceptPatterns, "Minsky level 4, reflective thinking");
			bsp.add(Switch.level3ExplantionRules, "Minsky level 3, deliberative thinking");
			bsp.add(Switch.Level2PredictionRules, "Minsky levels 1 & 2, reactive thinking");
			storyReadingControls.add(bsp);

			bsp = new BorderedSwitchPanel("Concept management");
			bsp.add(Switch.reportSubConceptsSwitch);
			bsp.add(Switch.findConceptOnsets);
			bsp.add(Switch.findConceptsContinuously);
			storyReadingControls.add(bsp);




		}

		return storyReadingControls;
	}

	BorderedSwitchPanel miscellaneousPanel;

	public BorderedSwitchPanel getMiscellaneousPanel() {
		if (miscellaneousPanel == null) {
			miscellaneousPanel = new BorderedSwitchPanel("Miscellaneous");
			miscellaneousPanel.add(Switch.useInsertConceptConsequentsIntoStory, "");
			miscellaneousPanel.add(Switch.useOnlyOneDeduction, "");
			miscellaneousPanel.add(Switch.useOnlyOneExplanation, "");
			miscellaneousPanel.add(Switch.levelLookForMentalModelEvidence, "");
			miscellaneousPanel.add(Switch.pullNonEventsToLeft, "");
			miscellaneousPanel.add(Switch.useColorInElaborationGraph, "");
			miscellaneousPanel.add(Switch.showConnectionTypeInElaborationGraph, "");

			getStoryReadingControls().add(miscellaneousPanel);
		}
		return miscellaneousPanel;
	}

	BorderedSwitchPanel matcherControlPanel;

	public BorderedSwitchPanel getMatcherControlPanel() {
		if (matcherControlPanel == null) {
			matcherControlPanel = new BorderedSwitchPanel("Matcher Control");
			matcherControlPanel.add(Switch.useFeaturesWhenMatching, "");
			matcherControlPanel.add(Switch.useMustWhenMatching, "");
			matcherControlPanel.add(Switch.splitNamesWithUnderscores, "");
		}
		return matcherControlPanel;
	}

	BorderedParallelJPanel simulatorControlPanel;

	public BorderedParallelJPanel getSimulatorControlPanel() {
		if (simulatorControlPanel == null) {
			simulatorControlPanel = new BorderedParallelJPanel("Simulator Control");
			simulatorControlPanel.addCenter(Radio.blocksWorldSimulator);
			simulatorControlPanel.addCenter(Radio.robotSimulator);
			simulatorControlPanel.addCenter(Radio.realRobot);
			simulatorControlPanel.addCenter(Radio.justPlan);

			ButtonGroup group = new ButtonGroup();
			group.add(Radio.blocksWorldSimulator);
			group.add(Radio.robotSimulator);
			group.add(Radio.realRobot);
			group.add(Radio.justPlan);
		}
		return simulatorControlPanel;
	}


	// ----------------------------------------------------------------------------------------
	// added by Zhutian on 16 March 2019 for different modes of story aligner/ analogy making
	BorderedParallelJPanel alignerControlPanel;

	public BorderedParallelJPanel getAlignerControlPanel() {
		if (alignerControlPanel == null) {
			alignerControlPanel = new BorderedParallelJPanel("Story Aligner Control");
			alignerControlPanel.addCenter(Radio.learnProcedure);
			alignerControlPanel.addCenter(Radio.learnConcept);
			alignerControlPanel.addCenter(Radio.learnDifference);

			ButtonGroup group = new ButtonGroup();
			group.add(Radio.learnProcedure);
			group.add(Radio.learnConcept);
			group.add(Radio.learnDifference);
		}
		return alignerControlPanel;
	}
	// ----------------------------------------------------------------------------------------


	public SwitchColumns getConsciousnessPanel() {
		if (consciousnessPanel == null) {

			consciousnessPanel = new SwitchColumns();
			BorderedSwitchPanelWithExplanation bsp = new BorderedSwitchPanelWithExplanation("Inclusions", "Determines what is included in the elaboration graph.");
			bsp.add(Switch.useNegativesInExplanationsBox, "");
			bsp.add(Switch.buildTree, "");
			bsp.add(Switch.reportSteps, "");
			bsp.add(Switch.reportComments, "");
			consciousnessPanel.add(bsp);

			bsp = new BorderedSwitchPanelWithExplanation("Reporting level", "Determines how many levels of the goal tree are reported.");
			bsp.add(Radio.psLevel0, "");
			bsp.add(Radio.psLevel1, "");
			bsp.add(Radio.psLevel2, "");
			bsp.add(Radio.psLevel3, "");
			bsp.add(Radio.psLevelX, "");
			bsp.add(Radio.psLevelP, "");
			consciousnessPanel.add(bsp);

			bsp = new BorderedSwitchPanelWithExplanation("Miscellaneous", "Miscellaneous switches.");
			bsp.add(Switch.deplyNovice, "");
			consciousnessPanel.add(bsp);


			ButtonGroup group = new ButtonGroup();
			group.add(Radio.psLevel0);
			group.add(Radio.psLevel1);
			group.add(Radio.psLevel2);
			group.add(Radio.psLevel3);
			group.add(Radio.psLevelX);
			group.add(Radio.psLevelP);

			Radio.psLevelX.setSelected(true);
		}
		return consciousnessPanel;
	}

	public SwitchColumns getStorySummaryControls() {
		if (summaryPersuaderPanel == null) {
			summaryPersuaderPanel = new SwitchColumns();

			BorderedSwitchPanel bsp = new BorderedSwitchPanel("Summarizer");
			bsp.add(Switch.includeUnabriggedProcessing, "");
			bsp.add(Switch.includeAgentRolesInSummary, "");
			bsp.add(Switch.includeSurprises, "");
			bsp.add(Switch.includeAbductions, "");
			bsp.add(Switch.includePresumptions, "");
			bsp.add(Switch.includeExplanations, "");
			bsp.add(Switch.eliminateMeans, "");
			bsp.add(Switch.eliminateIfFollowsFromPrevious, "");
			bsp.add(Switch.showMarkup, "");
			summaryPersuaderPanel.add(bsp);
			bsp = new BorderedSwitchPanel("Persuader");
			bsp.add(Switch.showContrastInPersuasion);
			summaryPersuaderPanel.add(bsp);
		}
		return summaryPersuaderPanel;
	}

	public SwitchPanelWithExplanation getExplanationPanel() {
		if (explanationPanel == null) {
			explanationPanel = new SwitchPanelWithExplanation("Determines what is included in the answer to <i>why</i> questions. Used only when in basic question-answering mode.");
			explanationPanel.add(Switch.includePersonalityExplanationCheckBox, "");
			explanationPanel.add(Switch.includeCauseExplanationCheckBox, "");
			explanationPanel.add(Switch.includeConceptExplanationCheckBox, "");
		}
		return explanationPanel;
	}


	public SwitchPanel getStoryTellingControls() {
		if (storyTellingControls == null) {
			storyTellingControls = new SwitchPanel();
			storyTellingControls.add(Radio.tellStoryButton);
			storyTellingControls.add(Radio.spoonFeedButton);
			storyTellingControls.add(Radio.primingButton);
			storyTellingControls.add(Radio.primingWithIntrospectionButton);
			ButtonGroup group = new ButtonGroup();
			group.add(Radio.spoonFeedButton);
			group.add(Radio.primingButton);
			group.add(Radio.primingWithIntrospectionButton);
			Radio.spoonFeedButton.setSelected(true);
		}
		return storyTellingControls;
	}

	public SwitchColumns getHumorControls() {
		if (humorControls == null) {
			humorControls = new SwitchColumns();

			BorderedSwitchPanel surprise_panel = new BorderedSwitchPanel("Surprise Analysis");
			surprise_panel.addLeft(Switch.humorCheckBox);
			surprise_panel.addLeft(Switch.humorContradictionCheckBox);
			surprise_panel.addLeft(Switch.humorParadoxCheckBox);
			//public static final CheckBoxWithMemory adaFlipCauseEffectCheckBox = new CheckBoxWithMemory("Err: Effect/Cause", false);
			surprise_panel.addLeft(Switch.humorLikelinessCheckBox);
			humorControls.add(surprise_panel);

			BorderedSwitchPanel resolution_panel = new BorderedSwitchPanel("Humor Resolution");
			resolution_panel.addLeft(Switch.humorCharacter);
			resolution_panel.addLeft(Switch.humorMorbid);
			resolution_panel.addLeft(Switch.humorLanguage);
			resolution_panel.addLeft(Switch.humorTopic);
			resolution_panel.addLeft(Switch.humorMeaningAssignment);
			resolution_panel.addLeft(Switch.humorParseAmbiguity);
			humorControls.add(resolution_panel);
		}
		return humorControls;
	}

	public SwitchPanel getSchizophreniaControls() {
		if (schizophreniaControls == null) {
			schizophreniaControls = new SwitchPanel();
			schizophreniaControls.add(Radio.sch_offButton);
			schizophreniaControls.add(Radio.sch_nonSchizophrenicButton);
			schizophreniaControls.add(Radio.sch_hyperpresumptionButton);
			schizophreniaControls.add(Radio.sch_failedInferringWantButton);

			ButtonGroup group = new ButtonGroup();
			group.add(Radio.sch_offButton);
			Radio.sch_offButton.setSelected(true);
			group.add(Radio.sch_nonSchizophrenicButton);
			group.add(Radio.sch_hyperpresumptionButton);
			group.add(Radio.sch_failedInferringWantButton);
		}
		return schizophreniaControls;
	}

	public SwitchPanel getExpertControls() {
		if (expertControls == null) {
			expertControls = new SwitchPanel();
			expertControls.addLeft(Switch.useExpertRules);
		}
		return expertControls;
	}

	public SwitchPanel getConceptNetControls() {
		if (conceptNetControls == null) {
			conceptNetControls = new SwitchPanel();
			conceptNetControls.addLeft(Switch.performGoalAnalysis);
			conceptNetControls.addLeft(Switch.similarityMatchCheckBox);
			conceptNetControls.addLeft(Switch.useConceptNetCache);
		}
		return conceptNetControls;
	}

	public SwitchPanel getBadGrammarControls() {
		if (badGrammarControls == null) {
			badGrammarControls = new SwitchPanel();
			badGrammarControls.addLeft(Switch.ECSlider1);
			badGrammarControls.addLeft(Switch.ECSlider2);
			badGrammarControls.addLeft(Switch.ECSlider3);
			badGrammarControls.addLeft(Switch.ECSlider4);
			badGrammarControls.addLeft(Switch.ECSlider5);
			badGrammarControls.addLeft(Switch.ECSlider6);
			badGrammarControls.addLeft(Switch.ECSlider7);
		}
		return badGrammarControls;
	}

	public JPanel getSubsystemsPanel() {
		if (subsystemsPanel == null) {
			subsystemsPanel = new BorderedParallelJPanel("Subsystems active");
			subsystemsPanel.addLeft(getSummarizer().getGateKeeper());
			subsystemsPanel.addLeft(getPersuader().getGateKeeper());
			subsystemsPanel.addLeft(getEscalationExpert1().getGateKeeper());
			subsystemsPanel.addLeft(getStoryRecallExpert1().getGateKeeper());
//			subsystemsPanel.addLeft(getPredictionExpert1().getGateKeeper());
			subsystemsPanel.addRight(getStatisticsExpert().getGateKeeper());
			subsystemsPanel.addRight(getJessicasExpert().getGateKeeper());
//			subsystemsPanel.addRight(getCarolinesExpert().getGateKeeper());
			subsystemsPanel.addRight(getDylansExpert().getGateKeeper());
			subsystemsPanel.addRight(Switch.storyAlignerCheckBox);
			subsystemsPanel.addRight(getRashiExperts().getGateKeeper()); // to add port Rashi
//			subsystemsPanel.addRight(Switch.uppCheckBox);
//			subsystemsPanel.addRight(getHumorExpert().getGateKeeper());

		}
		return subsystemsPanel;
	}

	protected ButtonSwitchBox getSyntaxToSemanticsSwitchBox() {
		if (syntaxToSemanticsSwitchBox == null) {
			syntaxToSemanticsSwitchBox = new ButtonSwitchBox(Switch.useUnderstand);
			syntaxToSemanticsSwitchBox.setName("Switch");
		}
		return syntaxToSemanticsSwitchBox;
	}

	public static JButton getNextButton() {
		if (nextButton == null) {
			nextButton = new JButton("Next");
			nextButton.setName("Next button");
			nextButton.setEnabled(false);
		}
		return nextButton;
	}

	public static JButton getRunButton() {
		if (runButton == null) {
			runButton = new JButton("Run");
			runButton.setName("Run button");
			runButton.setEnabled(false);
		}
		return runButton;
	}


	public static TabbedMentalModelViewer getMentalModelViewer() {
		if (mentalModelViewer == null) {
			mentalModelViewer = new TabbedMentalModelViewer();

			mentalModelViewer.setBackground(Color.WHITE);
			mentalModelViewer.setOpaque(true);
			mentalModelViewer.setName("Mental Models");
			setMinimumHeight(mentalModelViewer, 0);
		}
		return mentalModelViewer;
	}

	public JMatrixPanel getMainMatrix() {
		if (mainMatrix == null) {
			mainMatrix = new JMatrixPanel();
			mainMatrix.setMinimumSize(new Dimension(460, 350));
			mainMatrix.add(getActionButtons(), 0, 0, 20, 10);
			mainMatrix.add(getQuestionsPanel(), 20, 0, 10, 10);
			// mainMatrix.add(getModePanel(), 10, 0, 10, 6);
			// mainMatrix.add(getStorySummaryControls(), 10, 0, 10, 6);
			mainMatrix.add(getConnectionPanel(), 0, 10, 10, 10);
//			mainMatrix.add(getSubsystemsPanel(), 10, 10, 10, 10); // Do not all any subsystems
			mainMatrix.add(getPresentationPanel(), 20, 10, 10, 10);

		}
		return mainMatrix;
	}

	/**
	 * All that is left here is for Fay's story generator, which is dormant
	 *
	 * @return
	 */
	// public JMatrixPanel getReadTellMatrix() {
	// if (readTellMatrix == null) {
	// readTellMatrix = new JMatrixPanel();
	// // readTellMatrix.add(getStoryReadingControls(), 0, 0, 15, 6);
	// // readTellMatrix.add(getConsciousnessPanel(), 15, 0, 15, 6);
	//
	// // readTellMatrix.add(getSubsystemsPanel(), 30, 0, 10, 6);
	// // readTellMatrix.add(getSchizophreniaControls(), 40, 0, 10, 6);
	//
	// // readTellMatrix.add(getStorySummaryControls(), 0, 6, 20, 6);
	//
	//
	//
	// // readTellMatrix.add(getExplanationPanel(), 20, 6, 10, 4);
	// readTellMatrix.add(getModePanel(), 30, 6, 10, 4);
	// // readTellMatrix.add(getStoryTellingControls(), 40, 6, 10, 4);
	//
	// // readTellMatrix.add(getStoryPersuasionControls(), 20, 10, 30, 2);
	//
	// // readTellMatrix.add(getStorySummaryControls(), 30, 0, 10, 10);
	//
	// }
	// return readTellMatrix;
	// }

	public JMatrixPanel getMiscellaneousMatrix() {
		if (miscellaneousMatrix == null) {
			miscellaneousMatrix = new JMatrixPanel();
			// miscellaneousMatrix.add(getElaborationOptionsPanel(), 0, 0, 3, 10);

			// ----------------------------------------------------------------------------------------
			// added by Zhutian on 16 March 2019 for different modes of story aligner/ analogy making
			if(StoryAligner.showOptionsInControls) {
				miscellaneousMatrix.add(getMiscellaneousPanel(), 0, 0, 10, 5);
				miscellaneousMatrix.add(getAlignerControlPanel(), 0, 5, 10, 5);
			} else {
				miscellaneousMatrix.add(getMiscellaneousPanel(), 0, 0, 10, 10);
			}
//			miscellaneousMatrix.add(getMiscellaneousPanel(), 0, 0, 10, 10);  // uncomment this if want to hide the options
			// ----------------------------------------------------------------------------------------

			miscellaneousMatrix.add(getMatcherControlPanel(), 10, 0, 10, 5);
			miscellaneousMatrix.add(getSimulatorControlPanel(), 10, 5, 10, 5);
			miscellaneousMatrix.add(getVideoPanel(), 0, 10, 20, 5);
			// miscellaneousMatrix.add(getGraveyardPanel(), 0, 25, 10, 20);
		}
		return miscellaneousMatrix;
	}

	public JMatrixPanel getDebuggingMatrix() {
		if (debuggingMatrix == null) {
			debuggingMatrix = new JMatrixPanel();
			debuggingMatrix.add(getDebuggingPanel(), 0, 0, 10, 5);
			debuggingMatrix.add(getCheckInPanel(), 0, 5, 2, 5);
			debuggingMatrix.add(getCo57Panel(), 5, 5, 5, 5);
			debuggingMatrix.add(getTestPanel(), 2, 5, 3, 5);
		}
		return debuggingMatrix;
	}

	public JMatrixPanel getGraveyardMatrix() {
		if (graveyardMatrix == null) {
			graveyardMatrix = new JMatrixPanel();
			graveyardMatrix.add(getGraveyardPanel(), 0, 0, 10, 10);
			graveyardMatrix.add(getCorpsePanel(), 0, 10, 10, 10);
		}
		return graveyardMatrix;
	}

	public ParallelJPanel getCo57Panel() {
		if (co57Panel == null) {
			co57Panel = new BorderedParallelJPanel("Co57 communication");
			ButtonGroup group = new ButtonGroup();
			group.add(co57LocalPassthrough);
			group.add(co57Passthrough);

			co57Panel.addRight(co57LocalPassthrough);
			co57Panel.addRight(co57Passthrough);

			ButtonGroup group2 = new ButtonGroup();
			group2.add(co57SimulatorAndTranslator);
			group2.add(co57JustTranslator);

			co57Panel.addRight(co57SimulatorAndTranslator);
			co57Panel.addRight(co57JustTranslator);
		}
		return co57Panel;
	}

	public ParallelJPanel getCheckInPanel() {
		if (checkInPanel == null) {
			checkInPanel = new BorderedParallelJPanel("Check in tests");
			checkInPanel.addLeft(test1Button);
			checkInPanel.addLeft(test2Button);
		}
		return checkInPanel;
	}

	public ParallelJPanel getDebuggingPanel() {
		if (debuggingPanel == null) {
			debuggingPanel = new BorderedParallelJPanel("Miscellaneous");
			debuggingPanel.setBackground(Color.WHITE);
			debuggingPanel.setOpaque(true);

			debuggingPanel.addLeft(Switch.showStartProcessingDetails);
			debuggingPanel.addLeft(Switch.countConceptNetWords);

			debuggingPanel.addCenter(Switch.showElaborationViewerDetails);
			debuggingPanel.addCenter(Switch.showTranslationDetails);
			// debuggingPanel.addCenter(Switch.useFestival);

			// mpfay 2013
			debuggingPanel.addCenter(useNewMatcherCheckBox);
			debuggingPanel.addCenter(matchAllThreads);
			debuggingPanel.addCenter(reportMatchingDifferencesCheckBox);

			debuggingPanel.addRight(this.debugButton1);
			debuggingPanel.addRight(this.debugButton2);
			debuggingPanel.addRight(this.debugButton3);

			debuggingPanel.addRight(clearSummaryTableButton);

			debuggingPanel.addRight(simulateCo57Button);
			debuggingPanel.addRight(connectTestingBox);
			debuggingPanel.addRight(disconnectTestingBox);
			disconnectTestingBox.setEnabled(false);

		}
		return debuggingPanel;
	}

	public ParallelJPanel getVideoPanel() {
		if (videoPanel == null) {
			videoPanel = new BorderedParallelJPanel("Recording");
			videoPanel.addLeft(makeSmallVideoRecordingButton);
			videoPanel.addLeft(makeLargeVideoRecordingButton);
			videoPanel.addLeft(makeMediumVideoRecordingButton);
			videoPanel.addLeft(makeCoUButton);
		}
		return videoPanel;

	}

	public BorderedParallelJPanel getPresentationPanel() {
	    if (presentationPanel == null) {
	        presentationPanel = new BorderedParallelJPanel("Presentation");
	        presentationPanel.addLeft(Switch.showTextEntryBox);
	        presentationPanel.addLeft(Switch.showOnsetSwitch);
					presentationPanel.addLeft(Switch.showStatisticsBar);
	        presentationPanel.addLeft(Switch.showDisconnectedSwitch);
	        presentationPanel.addLeft(Switch.showCausationGraph);
	        presentationPanel.addLeft(Switch.showCommonsenseCausationReasoning);
	        presentationPanel.addLeft(Switch.slowMotionSwitch);
	        presentationPanel.addLeft(Switch.stepThroughNextStory);
	        presentationPanel.addLeft(Switch.useSpeechCheckBox);
	    }
	    return presentationPanel;
	}

	private JMatrixPanel mainMatrix;

	private JMatrixPanel readTellMatrix;

	private JMatrixPanel miscellaneousMatrix;

	private JMatrixPanel debuggingMatrix;

	private JMatrixPanel graveyardMatrix;

	private BorderedParallelJPanel presentationPanel;

	private BorderedParallelJPanel modePanel;

	private SwitchPanelWithExplanation explanationPanel;

	private BorderedParallelJPanel questionHandlerPanel;

	private BorderedParallelJPanel connectionPanel;

	private BorderedParallelJPanel testPanel;

	private BorderedParallelJPanel corpsePanel;

	// private BorderedParallelJPanel elaborationOptionsPanel;

	public BorderedParallelJPanel getModePanel() {
		if (modePanel == null) {
			modePanel = new BorderedParallelJPanel("Mode");
			modePanel.addLeft(Radio.normalModeButton);
			modePanel.addLeft(Radio.tellStoryButton);
			modePanel.addLeft(Radio.calculateSimilarityButton);
			modePanel.addLeft(Radio.alignmentButton);
		}
		return modePanel;

	}



	public BorderedParallelJPanel getQuestionsPanel() {
		if (questionHandlerPanel == null) {
			questionHandlerPanel = new BorderedParallelJPanel("Question responder mode");
			questionHandlerPanel.addCenter(Radio.qToLegacy);
			questionHandlerPanel.addCenter(Radio.qToPHW);
			questionHandlerPanel.addCenter(Radio.qToDXH);
			questionHandlerPanel.addCenter(Radio.qToJMN);
			questionHandlerPanel.addCenter(Radio.qToCA);
			questionHandlerPanel.addCenter(Radio.qToZTY);
			questionHandlerPanel.addCenter(Radio.qToZTY36);
			questionHandlerPanel.addCenter(Radio.qToZTYBTS);
			ButtonGroup group = new ButtonGroup();
			group.add(Radio.qToLegacy);
			group.add(Radio.qToPHW);
			group.add(Radio.qToDXH);
			group.add(Radio.qToJMN);
			group.add(Radio.qToCA);
			group.add(Radio.qToZTY);
			group.add(Radio.qToZTY36);
			group.add(Radio.qToZTYBTS);
		}
		return questionHandlerPanel;

	}

	/*
	 * public static final JRadioButton qToLegacy = new JRadioButton("Basic"); public static final JRadioButton qToPHW =
	 * new JRadioButton("Self aware"); public static final JRadioButton qToJMN = new JRadioButton("Agent focused");
	 * public static final JRadioButton qToJMN = new JRadioButton("Development focused");
	 */

	public JPanel getConnectionPanel() {
		if (connectionPanel == null) {
			connectionPanel = new BorderedParallelJPanel("Connections");

			// connectionPanel.addLeft(NewTimer.startDirectTimer.getOnOffLabel());
			// connectionPanel.addLeft(NewTimer.translationTimer.getOnOffLabel());
			// connectionPanel.addLeft(NewTimer.generatorTimer.getOnOffLabel());

			connectionPanel.addCenter(NewTimer.ruleProcessingTimer.getOnOffLabel());
			connectionPanel.addCenter(NewTimer.conceptProcessingTimer.getOnOffLabel());

			connectionPanel.addRight(NewTimer.startFailureTimer.getOnOffLabel());
			connectionPanel.addRight(NewTimer.bundleGeneratorTimer.getOnOffLabel());
			connectionPanel.addRight(Switch.useWordnetCache);
			connectionPanel.addRight(Switch.useStartCache);

		}
		return connectionPanel;
	}

	public JPanel getTestPanel() {
		if (testPanel == null) {
			testPanel = new BorderedParallelJPanel("Test");
			testPanel.addLeft(NewTimer.startBetaTimer.getOnOffLabel());
			testPanel.addLeft(NewTimer.generatorBetaTimer.getOnOffLabel());
			testPanel.addLeft(Switch.useStartBeta);
			testPanel.addLeft(Switch.activateExperimentalParser);
		}
		return testPanel;
	}

	public JPanel getActionButtons() {
		if (actionButtons == null) {
			actionButtons = new BorderedParallelJPanel("Actions");
			actionButtons.setBackground(Color.WHITE);
			actionButtons.setOpaque(true);
			actionButtons.addRight(this.rerunExperiment);
			actionButtons.addRight(this.rereadFile);
			actionButtons.addLeft(this.testSentencesButton);
			actionButtons.addLeft(this.testStoriesButton);
			actionButtons.addLeft(this.testOperationsButton);

			// actionButtons.addLeft(this.loopButton);
			actionButtons.addLeft(eraseTextButton);
			// actionButtons.addCenter(disambiguationButton);
			// actionButtons.addLeft(experienceButton);

			actionButtons.addRight(getNextButton());
			actionButtons.addRight(getRunButton());

			actionButtons.addLeft(startPurgeButton);
			actionButtons.addRight(wordnetPurgeButton);

			actionButtons.addLeft(this.demonstrateConnectionsButton);
			actionButtons.addRight(this.demonstrateConceptsButton);

			actionButtons.addLeft(conceptNetPurgeButton);

			actionButtons.addRight(runAligner);

			rereadFile.setEnabled(false);
			rerunExperiment.setEnabled(false);

			ButtonGroup group = new ButtonGroup();
			group.add(Radio.normalModeButton);
			group.add(Radio.tellStoryButton);
			group.add(Radio.calculateSimilarityButton);
			group.add(Radio.alignmentButton);

			Radio.normalModeButton.setOpaque(false);
			Radio.tellStoryButton.setOpaque(false);
			Radio.calculateSimilarityButton.setOpaque(false);

		}
		return actionButtons;
	}










	public static final JRadioButton summarize = new RadioButtonWithDefaultValue("Summarize");

	public BorderedParallelJPanel getCorpsePanel() {
		if (corpsePanel == null) {
			corpsePanel = new BorderedParallelJPanel("Obsoleted/Deprecated/Obscure/Rotted/Moribund---off by default");
			corpsePanel.addLeft(Switch.disambiguatorSwitch);
			corpsePanel.addCenter(Switch.showBackgroundElements);
			corpsePanel.addLeft(memorySwitch);
			corpsePanel.addCenter(Switch.conceptSwitch);
            corpsePanel.addCenter(Switch.detectMultipleReflectionsSwitch);

			corpsePanel.addLeft(Switch.allowRepeatedCommands);
		}
		return corpsePanel;
	}


	// public JPanel getBehaviorOptionsPanel() {
	// if (behaviorOptionsPanel == null) {
	// behaviorOptionsPanel = new BorderedParallelJPanel("Behavior");
	//
	// }
	// return behaviorOptionsPanel;
	// }





	// public BorderedParallelJPanel getElaborationOptionsPanel() {
	// if (elaborationOptionsPanel == null) {
	// elaborationOptionsPanel = new BorderedParallelJPanel("Elaboraton options");
	// elaborationOptionsPanel.addLeft(Switch.showInferences);
	// elaborationOptionsPanel.addLeft(Switch.showWires);
	//
	// }
	// return elaborationOptionsPanel;
	// }

	public ParallelJPanel getGraveyardPanel() {
		if (graveyardPanel == null) {
			graveyardPanel = new BorderedParallelJPanel("Graveyard");
			graveyardPanel.setBackground(Color.WHITE);
			graveyardPanel.setOpaque(true);

			deprecateLeft(Switch.workWithVision);
			deprecateLeft(Switch.useUnderstand);
			deprecateLeft(Switch.stepParser);

			deprecateCenter(debugVideoFileButton);
			deprecateCenter(demoSimulator);
			deprecateCenter(kjFileButton);
			deprecateCenter(kjeFileButton);
			deprecateRight(visionFileButton);
			deprecateRight(loadVideoPrecedents);

			deprecateRight(clearMemoryButton);
			deprecateRight(focusButton);
		}
		return graveyardPanel;
	}

	private void deprecateLeft(JComponent c) {
		getGraveyardPanel().addLeft(c);
		c.setEnabled(false);
	}

	private void deprecateCenter(JComponent c) {
		getGraveyardPanel().addCenter(c);
		c.setEnabled(false);
	}

	private void deprecateRight(JComponent c) {
		getGraveyardPanel().addRight(c);
		c.setEnabled(false);
	};

	// These have to be here, instead of in GenesisGetters, because used in dsplay panels

	private Summarizer summarizer;

	private Persuader persuader;

	public Persuader getPersuader() {
		if (persuader == null) {
			persuader = Persuader.getPersuader();
		}
		return persuader;
	}

	public Summarizer getSummarizer() {
		if (summarizer == null) {
			summarizer = Summarizer.getSummarizer();
		}
		return summarizer;
	}

	// protected void setPreferredWidth(Component c, int w) {
	// int h = c.getPreferredSize().height;
	// c.setPreferredSize(new Dimension(w, h));
	// }
	//
	// protected void setPreferredHeight(Component c, int h) {
	// int w = c.getPreferredSize().width;
	// c.setPreferredSize(new Dimension(w, h));
	// }
	//
	// protected void setMinimumWidth(Component c, int w) {
	// int h = c.getMinimumSize().height;
	// c.setMinimumSize(new Dimension(w, h));
	// }
	//
	// protected void setMinimumHeight(Component c, int h) {
	// int w = c.getMinimumSize().width;
	// c.setMinimumSize(new Dimension(w, h));
	// }

	// Sample runtime switch

	// if (!Webstart.isWebStart())

	public EscalationExpert getEscalationExpert1() {
		if (escalationExpert1 == null) {
			escalationExpert1 = new EscalationExpert();
			escalationExpert1.setGateKeeper(Switch.escalationCheckBox);
		}
		return escalationExpert1;
	}

	public EscalationExpert getEscalationExpert2() {
		if (escalationExpert2 == null) {
			escalationExpert2 = new EscalationExpert();
			escalationExpert2.setGateKeeper(Switch.escalationCheckBox);
		}
		return escalationExpert2;
	}

	public StoryRecallExpert getStoryRecallExpert1() {
		if (storyRecallExpert1 == null) {
			storyRecallExpert1 = new StoryRecallExpert();
			storyRecallExpert1.setGateKeeper(Switch.storyRecallCheckBox);
		}
		return storyRecallExpert1;
	}

	public StoryRecallExpert getStoryRecallExpert2() {
		if (storyRecallExpert2 == null) {
			storyRecallExpert2 = new StoryRecallExpert();
			storyRecallExpert2.setGateKeeper(Switch.storyRecallCheckBox);
		}
		return storyRecallExpert2;
	}

//	public PredictionExpert getPredictionExpert1() {
//		if (predictionExpert1 == null) {
//			predictionExpert1 = new PredictionExpert();
//			predictionExpert1.setGateKeeper(Switch.predictionCheckBox);
//		}
//		return predictionExpert1;
//	}

	public StatisticsExpert getStatisticsExpert() {
		if (statisticsExpert == null) {
			statisticsExpert = new StatisticsExpert("Statistics", Switch.statisticsCheckBox);
		}
		return statisticsExpert;
	}

	public JessicasExpert getJessicasExpert() {
		if (jessicasExpert == null) {
			jessicasExpert = new JessicasExpert();
		}
		return jessicasExpert;
	}

	/*public CarolinesExpert getCarolinesExpert() {
		if (carolinesExpert == null) {
			carolinesExpert = new CarolinesExpert();
		}
		return carolinesExpert;
	}*/

//	public PsychologicallyPlausibleModel getCarolinesExpert() {
//		if (carolinesExpert == null) {
//			carolinesExpert = new PsychologicallyPlausibleModel();
//		}
//		return carolinesExpert;
//	}

	public MeansEndsProcessor getDylansExpert() {
		if (dylansExpert == null) {
			dylansExpert = new MeansEndsProcessor();
			dylansExpert.setGateKeeper(Switch.meansToEndCheckBox);
		}
		return dylansExpert;
	}

	RecipeFinder recipeFinder = null;

	public RecipeFinder getRecipeFinder() {
		if (recipeFinder == null) {
			recipeFinder = new RecipeFinder("Recipe finder");
			recipeFinder.setGateKeeper(Switch.storyAlignerCheckBox);
		}
		return recipeFinder;
	}

//	public HumorExpert getHumorExpert() {
//		if (humorExpert == null) {
//			humorExpert = new HumorExpert();
//		}
//		return humorExpert;
//	}


	// RASHI 26 Jan 2019

	RashisExperts rashiExperts;

	SummarizeIntentions summarizeIntentions;

	/*
	 * Get an instance of LocalProcessor to do something with the output of a complete story object from a story
	 * processor.
	 */
	public RashisExperts getRashiExperts() {
		if (rashiExperts == null) {
			rashiExperts = new RashisExperts();
		}
		return rashiExperts;
	}

	/*
	 * Get an instance of HighlightIntentions. HighlightIntentions analyzes a story about an author's decisions.
	 */
	public SummarizeIntentions getSummarizerOfIntentions() {

		if (summarizeIntentions == null) {
			summarizeIntentions = new SummarizeIntentions();
		}

		return summarizeIntentions;
	}

}

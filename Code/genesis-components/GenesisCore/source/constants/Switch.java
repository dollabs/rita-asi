package constants;

import java.util.*;

import javax.swing.*;

import connections.*;
import gui.SliderWithMemory;

/*
 * Created on Nov 23, 2012
 * @author phw
 */

public class Switch {

	// Presentation

	public static final CheckBoxWithMemory showTextEntryBox = new CheckBoxWithMemory("Show text box", false);

	public static final CheckBoxWithMemory showOnsetSwitch = new CheckBoxWithMemory("Show onsets", false);

	public static final CheckBoxWithMemory showStatisticsBar = new CheckBoxWithMemory("Show statistics bar", false);

	public static final CheckBoxWithMemory showDisconnectedSwitch = new CheckBoxWithMemory("Show all story elements", true);

	public static final CheckBoxWithMemory slowMotionSwitch = new CheckBoxWithMemory("Show in slow motion", false);

	public static final CheckBoxWithMemory stepThroughNextStory = new CheckBoxWithMemory("Step through next story", false);

	public static final CheckBoxWithMemory useSpeechCheckBox = new CheckBoxWithMemory("Use speech output", true);

	public static final CheckBoxWithMemory useStartBeta = new CheckBoxWithMemory("Use experimental START", false);

	public static final CheckBoxWithMemory useStartBeta2 = new CheckBoxWithMemory("Use experimental START", false);

	public static final JCheckBox activateExperimentalParser = new CheckBoxWithMemory("Activate experimental parser", false);

	public static final CheckBoxWithMemory showCausationGraph = new CheckBoxWithMemory("Show causation graph", true);

	public static final CheckBoxWithMemory showCommonsenseCausationReasoning = new CheckBoxWithMemory("Show commonsense causation reasoning", false);

	// Interpretation

	public static final CheckBoxWithMemory levelLookForMentalModelEvidence = new CheckBoxWithMemory(
	        "Look for personality-trait indicators in actions",
	        true);

	public static final CheckBoxWithMemory level5UseMentalModels = new CheckBoxWithMemory(
	        "Use mental models", true);

	public static final CheckBoxWithMemory level4ConceptPatterns = new CheckBoxWithMemory("Use concept patterns", true);

	public static final CheckBoxWithMemory level3ExplantionRules = new CheckBoxWithMemory("Use explanation rules", true);

	public static final CheckBoxWithMemory Level2PredictionRules = new CheckBoxWithMemory("Use deduction rules", true);

	public static final CheckBoxWithMemory useOnlyOneDeduction = new CheckBoxWithMemory("Use first-found deduction only", true);

	public static final CheckBoxWithMemory useOnlyOneExplanation = new CheckBoxWithMemory("Use first-found explanation only", true);

	public static final CheckBoxWithMemory useInsertConceptConsequentsIntoStory = new CheckBoxWithMemory("Insert consequents of concepts",
	        false);

	// Switches for controlling underscored noun phrases returned from start and adjectives in matcher

	public static final CheckBoxWithMemory useFeaturesWhenMatching = new CheckBoxWithMemory("Use features when matching", false, false);

	public static final CheckBoxWithMemory useMustWhenMatching = new CheckBoxWithMemory("Note modal must when matching", false, false);

	public static final CheckBoxWithMemory splitNamesWithUnderscores = new CheckBoxWithMemory("Split names with underscores", false, false);

	public static final CheckBoxWithMemory useFancySimulator = new CheckBoxWithMemory("Use fancy simulator", true, false);

	// Switches for controlling simulation

	// Miscellaneous--on by default
	//
	// public static final JCheckBoxWithMemory nonSchizophrenicGenesis = new JCheckBoxWithMemory("Healthy Genesis",
	// true);
	// public static final JCheckBoxWithMemory schizophrenicGenesis = new JCheckBoxWithMemory("Schizophrenic Genesis -
	// Hyperpresumption", true);
	//
	public static final CheckBoxWithMemory reportSubConceptsSwitch = new CheckBoxWithMemory("Report sub concepts", false);

	public static final CheckBoxWithMemory findConceptsContinuously = new CheckBoxWithMemory("Find concepts while reading", false);

	public static final CheckBoxWithMemory findConceptOnsets = new CheckBoxWithMemory("Find concept onsets while reading", false);

	public static final CheckBoxWithMemory useColorInElaborationGraph = new CheckBoxWithMemory("Show elaboration graph in color", true);

	public static final CheckBoxWithMemory showConnectionTypeInElaborationGraph = new CheckBoxWithMemory("Show elaboration graph with connection types", false);

	public static final CheckBoxWithMemory pullNonEventsToLeft = new CheckBoxWithMemory("Migrate unsupported facts to left", true);

	public static final JCheckBox useWordnetCache = new JCheckBox("Use Wordnet cache", true);

	public static final CheckBoxWithMemory detectMultipleReflectionsSwitch = new CheckBoxWithMemory("Find multiple interpretations", true);

	public static final CheckBoxWithMemory allowRepeatedCommands = new CheckBoxWithMemory("Allow repeated commands", false);

	// public static final JCheckBoxWithMemory showInferences = new JCheckBoxWithMemory("Show inferences", true);

	// public static final JCheckBoxWithMemory showWires = new JCheckBoxWithMemory("Show wires", true);

	// Miscellaneous--off by default

	public static final JCheckBox useStartServer = new JCheckBox("Use Start server", false);

	public static final CheckBoxWithMemory showContrastInPersuasion = new CheckBoxWithMemory("Make others have opposite quality", false);

	public static final CheckBoxWithMemory includeUnabriggedProcessing = new CheckBoxWithMemory("Include unabridged version", true);

	public static final CheckBoxWithMemory includeExplanations = new CheckBoxWithMemory("Include explanations", true);

	public static final CheckBoxWithMemory includeSurprises = new CheckBoxWithMemory("Always include surprises", true);

	public static final CheckBoxWithMemory includePresumptions = new CheckBoxWithMemory("Include presumptions", true);

	public static final CheckBoxWithMemory includeAbductions = new CheckBoxWithMemory("Include abductions", true);

	public static final CheckBoxWithMemory eliminateIfFollowsFromPrevious = new CheckBoxWithMemory("Filter out if follows from", true);

	public static final CheckBoxWithMemory eliminateMeans = new CheckBoxWithMemory("Filter out means", true);

	public static final CheckBoxWithMemory showMarkup = new CheckBoxWithMemory("Show markup", false);

	public static final CheckBoxWithMemory includeAgentRolesInSummary = new CheckBoxWithMemory("Include agent roles", false);

	public static final CheckBoxWithMemory countConceptNetWords = new CheckBoxWithMemory("Count concept net words", false);

	// Obsolete, deprecated, obscure, rotted, off by default

	public static final CheckBoxWithMemory showBackgroundElements = new CheckBoxWithMemory("Don't show background elements", false);

	public static final CheckBoxWithMemory conceptSwitch = new CheckBoxWithMemory("Use concepts", false);

	public static final JCheckBox useStartCache = new JCheckBox("Use Start cache", true);

	// Debugging--off by default

	public static final CheckBoxWithMemory showStartProcessingDetails = new CheckBoxWithMemory("Show start processing on console", false);

	public static final CheckBoxWithMemory showElaborationViewerDetails = new CheckBoxWithMemory("Show elaboration details", false);

	public static final CheckBoxWithMemory showTranslationDetails = new CheckBoxWithMemory("Note translation details", false);

	public static final CheckBoxWithMemory showTranslationDetails2 = new CheckBoxWithMemory("Note translation details", false);

	public static final CheckBoxWithMemory useFestival = new CheckBoxWithMemory("Use festival", false);

	// Other

	public static final CheckBoxWithMemory stepParser = new CheckBoxWithMemory("Step the parser", false);

	public static final CheckBoxWithMemory usePhysicalConceptMemory = new CheckBoxWithMemory("Store CMEM on drive", false);

	public static final CheckBoxWithMemory useUnderstand = new CheckBoxWithMemory("Use Understand", false);

	public static final CheckBoxWithMemory workWithVision = new CheckBoxWithMemory("Work with vision", false);

	// Wiring switches

	public static final WiredOnOffSwitch disambiguatorSwitch = new WiredToggleSwitch("Use disambiguator");

	// Explanation

	public static final CheckBoxWithMemory includePersonalityExplanationCheckBox = new CheckBoxWithMemory("Personality", false);

	public static final CheckBoxWithMemory includeCauseExplanationCheckBox = new CheckBoxWithMemory("Cause", false);

	public static final CheckBoxWithMemory includeConceptExplanationCheckBox = new CheckBoxWithMemory("Concept", false);

	// Miscellaneous

	public static final CheckBoxWithMemory predictionCheckBox = new CheckBoxWithMemory("Prediction expert", false);

	public static final CheckBoxWithMemory storyRecallCheckBox = new CheckBoxWithMemory("Precedent expert", false);

	public static final CheckBoxWithMemory escalationCheckBox = new CheckBoxWithMemory("Escalation expert", false);

	public static final CheckBoxWithMemory statisticsCheckBox = new CheckBoxWithMemory("Statistics expert", false);

	public static final CheckBoxWithMemory meansToEndCheckBox = new CheckBoxWithMemory("Means-to-an-end detector", false);

	public static final CheckBoxWithMemory storyAlignerCheckBox = new CheckBoxWithMemory("Story aligner", false, false);

	public static final CheckBoxWithMemory Reprocess = new CheckBoxWithMemory("Reprocess", false, false);


	// Third argument means means reset to unchecked on reload
//	public static final CheckBoxWithMemory uppCheckBox = new CheckBoxWithMemory("Emanuele's UPP", false, false);

	// Gate keepers, replaces tith gatekeeper mechanism mostly

	// public static final CheckBoxWithMemory jessicaCheckBox = new CheckBoxWithMemory("Jessica's expert", false);

	public static final CheckBoxWithMemory similarityMatchCheckBox = new CheckBoxWithMemory("Use similarity rule matching", false);


	public static final CheckBoxWithMemory humorCheckBox = new CheckBoxWithMemory("Humor Handler", true);
	public static final CheckBoxWithMemory humorContradictionCheckBox = new CheckBoxWithMemory("Err: Contradiction", false);
	public static final CheckBoxWithMemory humorParadoxCheckBox = new CheckBoxWithMemory("Err: Paradox Loop", false);
	//public static final CheckBoxWithMemory adaFlipCauseEffectCheckBox = new CheckBoxWithMemory("Err: Effect/Cause", false);
	public static final CheckBoxWithMemory humorLikelinessCheckBox = new CheckBoxWithMemory("Err: Unlikely Surprise", false);

	public static final CheckBoxWithMemory humorCharacter = new CheckBoxWithMemory("Class Trait Humor", true);
	public static final CheckBoxWithMemory humorMorbid = new CheckBoxWithMemory("Morbidity Level", false);
	public static final CheckBoxWithMemory humorLanguage = new CheckBoxWithMemory("Language Level", false);
	public static final CheckBoxWithMemory humorTopic = new CheckBoxWithMemory("Topic Shift", false);
	public static final CheckBoxWithMemory humorMeaningAssignment = new CheckBoxWithMemory("Meaning Assignment", false);
	public static final CheckBoxWithMemory humorParseAmbiguity = new CheckBoxWithMemory("Parse Ambiguity", false);

	public static final JCheckBox useConceptNetCache = new JCheckBox("Use ConceptNet cache", true);

	public static final CheckBoxWithMemory performGoalAnalysis = new CheckBoxWithMemory("Perform goal analysis (ASPIRE)", false);

	public static final CheckBoxWithMemory useExpertRules = new CheckBoxWithMemory("Use expert's rules", false);


	// public static final JCheckBoxWithMemory summarizerCheckBox = new JCheckBoxWithMemory("Summarizer", false);

	// public static final JCheckBoxWithMemory persuaderCheckBox = new JCheckBoxWithMemory("Persuader", false);

	private static List<CheckBoxWithMemory> checkBoxes;

	private static List<JRadioButton> radioButtons;

	public static List<JRadioButton> getRadioButtons() {
		if (radioButtons == null) {
			JRadioButton[] buttons = {

			        Radio.alignmentButton

			};
			radioButtons = Arrays.asList(buttons);
		}
		return radioButtons;
	}

	/***
	 * Consiousness buttons
	 */


	public static final CheckBoxWithMemory useChecksInExplanationsBox = new CheckBoxWithMemory("Include checks", true);

	public static final CheckBoxWithMemory useNegativesInExplanationsBox = new CheckBoxWithMemory("Include negatives", true);



	public static final CheckBoxWithMemory reportSteps = new CheckBoxWithMemory("Include steps in graph", true);

	public static final CheckBoxWithMemory buildTree = new CheckBoxWithMemory("Include goals tree in graph", false);

	public static final CheckBoxWithMemory reportComments = new CheckBoxWithMemory("Include comments in graph", false);

	public static final CheckBoxWithMemory deplyNovice = new CheckBoxWithMemory("Deploy novice first", false);

	// public static List<JCheckBoxWithMemory> getCheckBoxes() {
	// if (checkBoxes == null) {
	// JCheckBoxWithMemory[] boxes = { Switch.includeUnabriggedProcessing, Switch.includeAgentRolesInSummary,
	// Switch.includeAbductions,
	// Switch.includePresumptions, Switch.includeExplanations, Switch.includeSurprises, Switch.eliminateMeans,
	// Switch.eliminateIfFollowsFromPrevious, Switch.showMarkup, Switch.showContrastInPersuasion,
	//
	// Switch.showTextEntryBox, Switch.showOnsetSwitch, Switch.showDisconnectedSwitch, Switch.stepThroughNextStory,
	// Switch.useSpeechCheckBox,
	//
	// Switch.summarizerCheckBox, Switch.persuaderCheckBox, Switch.escalationCheckBox, Switch.storyRecallCheckBox,
	// Switch.predictionCheckBox,
	//
	// Switch.includePersonalityExplanationCheckBox, Switch.includeCauseExplanationCheckBox,
	// Switch.includeConceptExplanationCheckBox, };
	// checkBoxes = Arrays.asList(boxes);
	// }
	// return checkBoxes;
	// }

	/**
	 * Bad grammar buttons
	 */

	public static final SliderWithMemory ECSlider1 = new SliderWithMemory("Default Decay");

	public static final SliderWithMemory ECSlider2 = new SliderWithMemory("Context Decay");

	public static final SliderWithMemory ECSlider3 = new SliderWithMemory("Ungrammatical Penalty");

	public static final SliderWithMemory ECSlider4 = new SliderWithMemory("Rule Breaking Penalty");

	public static final SliderWithMemory ECSlider5 = new SliderWithMemory("Stopping Threshold");

	public static final SliderWithMemory ECSlider6 = new SliderWithMemory("Maximum Calls");

	public static final SliderWithMemory ECSlider7 = new SliderWithMemory("Currently Unused");

}

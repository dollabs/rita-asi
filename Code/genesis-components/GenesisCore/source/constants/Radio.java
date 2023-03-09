package constants;

import gui.RadioButtonWithDefaultValue;

import javax.swing.JRadioButton;

/*
 * Created on Nov 24, 2012
 * @author phw
 */

public class Radio {

	public static final JRadioButton normalModeButton = new RadioButtonWithDefaultValue("Read", true);

	public static final JRadioButton tellStoryButton = new RadioButtonWithDefaultValue("Tell");

	public static final JRadioButton calculateSimilarityButton = new JRadioButton("Compare");

	public static final JRadioButton alignmentButton = new RadioButtonWithDefaultValue("Align");

	public static final JRadioButton spoonFeedButton = new RadioButtonWithDefaultValue("Spoon feed");

	public static final JRadioButton primingButton = new RadioButtonWithDefaultValue("Explain");

	public static final JRadioButton primingWithIntrospectionButton = new RadioButtonWithDefaultValue("Teach");

	/***
	 * Schizophrenia buttons
	 */
	public static final JRadioButton sch_offButton = new RadioButtonWithDefaultValue("Off", true);

	public static final JRadioButton sch_nonSchizophrenicButton = new RadioButtonWithDefaultValue("Healthy");

	public static final JRadioButton sch_hyperpresumptionButton = new RadioButtonWithDefaultValue("Hyper-presumption");

//	Old version of this button:
//	public static final JRadioButton sch_failedInferringWantButton = new RadioButtonWithDefaultValue("Failure at Inferring Want");
	public static final JRadioButton sch_failedInferringWantButton = new RadioButtonWithDefaultValue("Failure-at-Mentalizing");

/***
 * 	This Schizophrenia mechanism was never implemented. If it is implemented, this can be uncommented to create the new button.
 */
//	public static final JRadioButton sch_extremeFailedSourceMonitoringButton = new JTransparentRadioButton("Extreme Failure at Source Monitoring");

	/***
	 * Where questions go
	 */

	public static final JRadioButton qToLegacy = new RadioButtonWithDefaultValue("Basic", true);

	public static final JRadioButton qToPHW = new RadioButtonWithDefaultValue("Self aware");

	public static final JRadioButton qToJMN = new RadioButtonWithDefaultValue("Agent focused");

	public static final JRadioButton qToDXH = new RadioButtonWithDefaultValue("Hypotheticals focused");

	public static final JRadioButton qToCA = new RadioButtonWithDefaultValue("Development focused");

	public static final JRadioButton qToZTY = new RadioButtonWithDefaultValue("Expert teaches novice");

	public static final JRadioButton qToZTY36 = new RadioButtonWithDefaultValue("Genesis advises human");

	public static final JRadioButton qToZTYBTS = new RadioButtonWithDefaultValue("Human tells Genesis");

	/***
	 * Consiousness buttons
	 */

	public static final JRadioButton psLevel0 = new JRadioButton("Level 0");

	public static final JRadioButton psLevel1 = new JRadioButton("Level 1");

	public static final JRadioButton psLevel2 = new JRadioButton("Level 2");

	public static final JRadioButton psLevel3 = new JRadioButton("Level 3");

	public static final JRadioButton psLevelX = new JRadioButton("Unlimited");

	public static final JRadioButton psLevelP = new JRadioButton("Solutions only");

	/***
	 * Robot choice
	 */

	public static final JRadioButton blocksWorldSimulator = new RadioButtonWithDefaultValue("Blocks world", true);

	public static final JRadioButton robotSimulator = new RadioButtonWithDefaultValue("Robot simulator");

	public static final JRadioButton realRobot = new RadioButtonWithDefaultValue("Real robot");
	public static final JRadioButton justPlan = new RadioButtonWithDefaultValue("Just plan");
	
	
	// ----------------------------------------------------------------------------------------
	// added by Zhutian on 16 March 2019 for different modes of story aligner/ analogy making
	/***
	 * Robot choice
	 */
	public static final JRadioButton learnProcedure = new RadioButtonWithDefaultValue("Learn procedure", true);
	
	public static final JRadioButton learnConcept = new RadioButtonWithDefaultValue("Learn concept");
	
	public static final JRadioButton learnDifference = new RadioButtonWithDefaultValue("Find difference");
	
	// ----------------------------------------------------------------------------------------

}
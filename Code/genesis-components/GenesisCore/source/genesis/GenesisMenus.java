package genesis;

import javax.swing.*;


import connections.*;
import constants.Radio;

/*
 * Created on May 8, 2010
 * @author phw
 */

public class GenesisMenus extends GenesisFoundation {

	// Other menu items

	protected JMenu demonstrationMenu = new JMenu("Demonstrate");

	protected JMenu readMenu = new JMenu("Read");

	protected JMenu recordMenu = new JMenu("Record");

	// protected JMenu controlMenu = new JMenu("Controls");

	protected JMenu parserMenu = new JMenu("Parser");

	protected JMenuItem contributorMenuItem = new JMenuItem("Contributors");

	protected JMenuItem readStoryItem = new JMenuItem("Select story");

	protected JMenuItem readDirectoryItem = new JMenuItem("Select story directory");

	// protected JMenuItem readDemoItem = new
	// JMenuItem("Select demonstration x");

	protected JMenuItem macbeth2align = new JMenuItem("Macbeth Two Perspectives");

	protected JMenuItem macbethPlusOnset = new JMenuItem("Macbeth, with onset detection");

	protected JMenuItem hamletRecall = new JMenuItem("Hamlet, with precedent recall");

	protected JMenuItem macbethTwoCulturesPlusOnset = new JMenuItem("Macbeth, two cultures, with onset detection");

	protected JMenuItem macbethTwoCultures = new JMenuItem("Macbeth, two cultures");

	protected JMenuItem macbethWithCulturalCompletion = new JMenuItem("Macbeth, cultural completion");

	protected JMenuItem mentalModelDemonstration = new JMenuItem("Trait development");

	protected JMenuItem playByPlayMenuItem = new JMenuItem("Play by play");

	protected JMenuItem loadAnnotationsItem = new JMenuItem("Load event annotations");

	protected JMenuItem printMenuItem = new JMenuItem("Print");

	protected JMenu viewMenu = new JMenu("View");

	protected JMenuItem genesisStories = new JMenuItem("GenesisCore/stories");

	protected JMenuItem genesisVision = new JMenuItem("GenesisCore/vision");

	public static final WiredOnOffSwitch memorySwitch = new WiredOnOffSwitch("Use memory");

	protected WiredToggleSwitch useExternalMovieViewer = new WiredToggleSwitch("Use external movie viewer");

	// Getters

	public static JRadioButton getSpoonFeedButton() {
		return Radio.spoonFeedButton;
	}

	public static JRadioButton getPrimingButton() {
		return Radio.primingButton;
	}

	public static JRadioButton getPrimingWithIntrospectionButton() {
		return Radio.primingWithIntrospectionButton;
	}

}

package matthewFay.Depricated;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import matthewFay.Demo;
import matthewFay.Depricated.SequenceAligner.AlignmentType;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.RankedSequenceAlignmentSet;
import matthewFay.StoryAlignment.SequenceAlignment;

import utils.Mark;

import connections.*;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Sequence;

@SuppressWarnings({ "unused", "deprecation" })
@Deprecated
public class StoryAligner extends AbstractWiredBox implements ActionListener {
	private static final String DO_SCENE_DEMO = "doSceneDemo";

	private static final String DO_BEST_ALIGNMENTS_DEMO = "doBestAlignmentsDemo";

	private static final String COMPLETE_SEARCH = "complete";

	private static final String FASTER_SEARCH = "faster";

	private static final String LAZY_SEARCH = "lazy";

	public static final String STORY_PORT = "story port";

	public static final String STORY_PORT2 = "story port2";

	public static final String REMEMBER_STORY = "remember story";

	public static final boolean debugBest = true;

	public static final String STAGE_DIRECTION = "stage direction";

	private static final boolean ignoreLowSocres = true;

	public static final boolean restrictMatches = true;

	Sequence story1;

	Sequence story2;

	List<Sequence> rememberedStories;

	GapFiller gf = new GapFiller();

	JPanel storyAlignmentPanel = null;

	public JPanel getStoryAlignmentPanel() {
		if (storyAlignmentPanel == null) {
			storyAlignmentPanel = new JPanel();
			storyAlignmentPanel.setName("StoryAlignment");
			try {
				LocalGenesis.localGenesis().getWindowGroupManager().addJComponent(storyAlignmentPanel);
			}
			catch (Exception e) {
				Mark.say("Not in Local Genesis");
			}
		}
		return storyAlignmentPanel;
	}

	JTabbedPane tabbedPane = null;

	JPanel alignmentPanel;

	JPanel controlPanel;

	JPanel treePanel;

	JCheckBox niceOutputCheckBox;

	ButtonGroup alignmentTypeGroup;

	JRadioButton completeAlignmentSearchButton;

	JRadioButton fasterAlignmentSearchButton;

	JRadioButton lazyAlignmentSearchButton;

	AlignmentType alignmentType = AlignmentType.NORMAL;

	JCheckBox gapFill;

	JButton sceneDemoButton;

	JButton bestAlignmentsDemoButton;

	public StoryAligner() {

		super("StoryAligner");

		Connections.getPorts(this).addSignalProcessor(STORY_PORT, "processStory");
		Connections.getPorts(this).addSignalProcessor(STORY_PORT2, "processStory2");

		Connections.getPorts(this).addSignalProcessor(REMEMBER_STORY, "rememberStory");
		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION, "processDirection");

		rememberedStories = new ArrayList<Sequence>();

		JPanel panel = getStoryAlignmentPanel();
		panel.setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		alignmentPanel = new JPanel();
		alignmentPanel.setLayout(new BorderLayout());
		tabbedPane.addTab("Alignments", alignmentPanel);
		tabbedPane.addTab("Match Tree", treePanel = new JPanel(new BorderLayout()));
		tabbedPane.addTab("Controls", controlPanel = new JPanel(new GridLayout(3, 3, 10, 10)));

		niceOutputCheckBox = new JCheckBox("English Output");
		niceOutputCheckBox.setSelected(SequenceAligner.generateNiceOutput);
		controlPanel.add(niceOutputCheckBox);

		completeAlignmentSearchButton = new JRadioButton("Complete Alignment Search");
		fasterAlignmentSearchButton = new JRadioButton("Faster Alignment Search");
		lazyAlignmentSearchButton = new JRadioButton("Lazy Alignment Search");

		completeAlignmentSearchButton.setSelected(false);
		fasterAlignmentSearchButton.setSelected(false);
		lazyAlignmentSearchButton.setSelected(true);

		completeAlignmentSearchButton.addActionListener(this);
		fasterAlignmentSearchButton.addActionListener(this);
		lazyAlignmentSearchButton.addActionListener(this);

		completeAlignmentSearchButton.setActionCommand(COMPLETE_SEARCH);
		fasterAlignmentSearchButton.setActionCommand(FASTER_SEARCH);
		lazyAlignmentSearchButton.setActionCommand(LAZY_SEARCH);

		alignmentTypeGroup = new ButtonGroup();
		alignmentTypeGroup.add(completeAlignmentSearchButton);
		alignmentTypeGroup.add(fasterAlignmentSearchButton);
		alignmentTypeGroup.add(lazyAlignmentSearchButton);

		controlPanel.add(completeAlignmentSearchButton);
		controlPanel.add(fasterAlignmentSearchButton);
		controlPanel.add(lazyAlignmentSearchButton);

		gapFill = new JCheckBox("Do Gap Filling");
		gapFill.setSelected(false);
		controlPanel.add(gapFill);
		sceneDemoButton = new JButton("Do Scene Alignment Demo");
		sceneDemoButton.setActionCommand(DO_SCENE_DEMO);
		sceneDemoButton.addActionListener(this);
		controlPanel.add(sceneDemoButton);
		bestAlignmentsDemoButton = new JButton("Do Best Alignments Demo");
		bestAlignmentsDemoButton.setActionCommand(DO_BEST_ALIGNMENTS_DEMO);
		bestAlignmentsDemoButton.addActionListener(this);
		controlPanel.add(bestAlignmentsDemoButton);

		panel.add(tabbedPane, BorderLayout.CENTER);
	}

	public void processDirection(Object o) {
		if (!Radio.alignmentButton.isSelected()) return;
		if (o == Markers.RESET) {
			rememberedStories.clear();
			story1 = null;
			story2 = null;
			gf.clearPatternBuffer();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(DO_SCENE_DEMO)) {
			SceneRankDemo();
		}
		if (e.getActionCommand().equals(DO_BEST_ALIGNMENTS_DEMO)) {
			BestAlignmentsDemo();
		}
		if (e.getActionCommand().equals(COMPLETE_SEARCH)) {
			alignmentType = AlignmentType.COMPLETE;
		}
		if (e.getActionCommand().equals(FASTER_SEARCH)) {
			alignmentType = AlignmentType.FASTER;
		}
		if (e.getActionCommand().equals(LAZY_SEARCH)) {
			alignmentType = AlignmentType.NORMAL;
		}
	}

	public void rememberStory(Object signal) {
		if (!Radio.alignmentButton.isSelected()) return;
		if (!(signal instanceof Sequence)) return;
		Sequence storySignal = (Sequence) signal;
		Mark.say("Remembering: ", storySignal.asString());
		rememberedStories.add((Sequence) storySignal.deepClone());
		gf.addPattern((Sequence) storySignal.deepClone());
		story1 = null;
		story2 = null;
	}

	public void processStory(Object input) {
		if (!Radio.alignmentButton.isSelected()) return;
		if (!(input instanceof Sequence)) return;
		Sequence storySignal = (Sequence) input;
		// Mark.say("Story1: "+storySignal.asString());
		alignStory(storySignal, 1);
	}

	public void processStory2(Object input) {
		if (!Radio.alignmentButton.isSelected()) return;
		if (!(input instanceof Sequence)) return;
		Sequence storySignal = (Sequence) input;
		// Mark.say("Story2: "+storySignal.asString());
		alignStory(storySignal, 2);
	}

	public SequenceAlignment alignStory(Sequence inputStory, int inputSlot) {
		SequenceAligner.generateNiceOutput = niceOutputCheckBox.isSelected();
		SequenceAlignment alignment = null;
		if (inputSlot == 1 && story1 == null) {
			story1 = inputStory;
		}
		if (inputSlot == 2 && story2 == null) {
			story2 = inputStory;
		}
		if (story1 != null && story2 != null) {
			if (gapFill.isSelected()) {
				gf.addPattern(story1);
				gf.fillGap(story2);

				alignment = gf.lastAlignment;

				alignmentPanel.removeAll();
				alignmentPanel.add(GapViewer.generateTable(alignment), BorderLayout.CENTER);
				tabbedPane.repaint();
			}
			else {
				SequenceAligner.generateNiceOutput = niceOutputCheckBox.isSelected();

				SequenceAligner aligner = new SequenceAligner();

				RankedSequenceAlignmentSet<Entity, Entity> bestAlignments = aligner.align(story1, story2, alignmentType);

				alignmentPanel.removeAll();
				alignmentPanel.add(GapViewer.generateFullTable(bestAlignments), BorderLayout.CENTER);
				tabbedPane.repaint();

				alignment = bestAlignments.get(0);
			}
			story1 = null;
			story2 = null;
		}
		else if (story2 != null && !rememberedStories.isEmpty()) {
			if (gapFill.isSelected()) {
				gf.fillGap(story2);

				alignment = gf.lastAlignment;

				alignmentPanel.removeAll();
				alignmentPanel.add(GapViewer.generateTable(alignment), BorderLayout.CENTER);
				tabbedPane.repaint();
			}
			else {
				SequenceAligner.generateNiceOutput = niceOutputCheckBox.isSelected();
				SequenceAligner aligner = new SequenceAligner();

				for (Sequence s : rememberedStories) {
					Mark.say("Pattern: ", s.asString());
				}

				RankedSequenceAlignmentSet<Entity, Entity> alignments = aligner.findBestAlignments(rememberedStories, story2);
				alignments.globalAlignment();

				alignmentPanel.removeAll();
				alignmentPanel.add(GapViewer.generateFullTable(alignments), BorderLayout.CENTER);
				// alignmentPanel.add(SequenceAligner.generateTable(alignments), BorderLayout.CENTER);
				tabbedPane.repaint();
			}
		}
		return alignment;
	}

	public void BestAlignmentsDemo() {
		Sequence exchangeStory = Demo.ExchangeStory();
		Sequence giveStory = Demo.GiveStory();

		SequenceAligner aligner = new SequenceAligner();
		RankedSequenceAlignmentSet<Entity, Entity> bestAlignments = aligner.align(giveStory, exchangeStory, AlignmentType.COMPLETE);

		alignmentPanel.removeAll();
		alignmentPanel.add(GapViewer.generateFullTable(bestAlignments), BorderLayout.CENTER);
		tabbedPane.repaint();

		for (Alignment<Entity, Entity> a : bestAlignments) {
			Mark.say("Alignment:");
			SequenceAligner.outputAlignment(a);
		}
	}

	public void SceneRankDemo() {
		SequenceAligner.generateNiceOutput = niceOutputCheckBox.isSelected();

		Sequence gapStory = Demo.GapStory();
		Sequence giveStory = Demo.GiveStory();
		Sequence fleeStory = Demo.FleeStory();
		Sequence takeStory = Demo.TakeStory();
		Sequence throwCatchStory = Demo.ThrowCatchStory();
		Sequence followStory = Demo.FollowStory();
		Sequence exchangeStory = Demo.ExchangeStory();

		List<Sequence> patterns = new ArrayList<Sequence>();
		patterns.add(giveStory);
		// patterns.add(fleeStory);
		patterns.add(takeStory);
		patterns.add(throwCatchStory);
		// patterns.add(followStory);
		patterns.add(exchangeStory);

		SequenceAligner aligner = new SequenceAligner();
		RankedSequenceAlignmentSet<Entity, Entity> alignments = aligner.findBestAlignments(patterns, gapStory);
		alignmentPanel.removeAll();
		alignmentPanel.add(GapViewer.generateTable(alignments), BorderLayout.CENTER);
		tabbedPane.repaint();
	}

	public static void main(String[] args) {
		Sequence give = Demo.GiveStory();
		Sequence gap = Demo.GapStory();

		StoryAligner storyAligner = new StoryAligner();
		storyAligner.alignStory(give, 1);
		Alignment<Entity, Entity> alignment = storyAligner.alignStory(gap, 2);
	}
}

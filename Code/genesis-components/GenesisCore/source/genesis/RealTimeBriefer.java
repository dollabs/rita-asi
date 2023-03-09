package genesis;

import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JButton;

import connections.*;
import connections.signals.BetterSignal;
import constants.GenesisConstants;
import frames.entities.Entity;
import frames.entities.Sequence;
import gui.TextViewer;
import rules.RuleEngine;
import storyProcessor.*;
import subsystems.summarizer.Summarizer;
import utils.*;

/*
 * Created on Apr 25, 2017
 * @author phw
 */

public class RealTimeBriefer extends AbstractWiredBox {

	static JButton PROCEED;

	static boolean stall = false;

	public static JButton getProceedButton() {
		if (PROCEED == null) {
			PROCEED = new JButton("Proceed");
			PROCEED.setEnabled(false);
			PROCEED.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					stall = false;
				}
			});
		}
		return PROCEED;
	}

	public RealTimeBriefer(String name) {
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object signal) {
		boolean debug = false;
		if (signal instanceof StoryProcessor) {
			StoryProcessor storyProcessor = (StoryProcessor) signal;
			Mark.say(debug, "Ready to process briefing");

			// Prepare briefing pane
			Genesis.getGenesis().setPanel(new BetterSignal(GenesisConstants.RIGHT, "Briefing"));
			TextViewer briefingPane = Genesis.getGenesis().getBriefingPanel();
			briefingPane.clear();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
			briefingPane.addText(Html.center(Html.bold("Briefing " + dateFormat.format(new Date()))));


			// Get interim concepts
			BetterSignal info = new BetterSignal(storyProcessor.getConceptPatterns(), storyProcessor.getStory(), storyProcessor.getInferences());
			storyProcessor.getConceptExpert().process(info);

			// Prepare concept centered summary
			Summarizer summarizer = Summarizer.getSummarizer();
			Sequence completeStorySequence = storyProcessor.getStory();
			Sequence explicitStorySequence = storyProcessor.getExplicitElements();

			List<ConceptDescription> conceptDescriptions = storyProcessor.getConceptAnalysis().getConceptDescriptions();
			Set<Entity> conceptFeeders = summarizer.composeConceptFeedersForConceptCenteredSummaries(completeStorySequence, conceptDescriptions);
			Sequence entitySummary = summarizer.composeEntitySummaryFromFeeders(completeStorySequence, conceptFeeders);

			if (!entitySummary.getElements().isEmpty()) {
				Set<String> conceptNames = summarizer.extractConceptNames(conceptDescriptions);
				String englishSummary = summarizer.composeEnglishSummary(entitySummary);
				briefingPane.addText(Html.p(Html.bold("The story is about " + Punctuator.punctuateAnd(new ArrayList(conceptNames)))));

				briefingPane.addText(Html.p(englishSummary));
			}
			else {
				// Noconcept centered summary; do simpler summary
				Set<Entity> relevantElements = summarizer.retainIfAntecedentOrExplicitCause(explicitStorySequence, completeStorySequence);
				entitySummary = summarizer.composeEntitySummaryFromFeeders(completeStorySequence, relevantElements);
				String englishSummary = summarizer.composeEnglishSummary(entitySummary);
				briefingPane.addText(englishSummary);
			}

			// Wait for user to indicate it is time to proceed
			treadWater();

			// Review situation to date, look for maybe-triggered rules
			RuleEngine.USE_INSTRUCTIONS = true;
			// Clone to avoid concurent modification error
			Vector<Entity> clone = (Vector) storyProcessor.getStory().getElements().clone();
			int sizeBeforeQuestions = clone.size();
			Entity lastElementBeforeQuestions = storyProcessor.getStory().getElements().lastElement();
				clone.stream().forEachOrdered(e -> {
				int sizeBefore = storyProcessor.getStory().getElements().size();
				storyProcessor.getRuleEngine().process(e, storyProcessor);
				int sizeAfter = storyProcessor.getStory().getElements().size();
			});

			RuleEngine.USE_INSTRUCTIONS = false;

			// If any changes, brief again!

			int sizeAfterQuestions = storyProcessor.getStory().getElements().size();
			Entity lastElementAfterQuestions = storyProcessor.getStory().getElements().lastElement();


			if (sizeAfterQuestions != sizeBeforeQuestions && lastElementBeforeQuestions != lastElementAfterQuestions) {
				Mark.say(debug, "Sizes", sizeBeforeQuestions, sizeAfterQuestions);
				Mark.say(debug, "Final elements", "\n", lastElementBeforeQuestions, "\n", lastElementAfterQuestions);
				process(storyProcessor);
			}
			else {
				Mark.say(debug, "No change during briefing");
			}

		}
	}



	private void treadWater() {
		stall = true;
		getProceedButton().setEnabled(true);
		getProceedButton().requestFocus();
		while (stall) {
			Sleep.sleep(1000);
		}
		getProceedButton().setEnabled(false);
	}

}

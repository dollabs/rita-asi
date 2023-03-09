package subsystems.summarizer;

import generator.Generator;
import gui.*;

import java.util.*;
import java.util.prefs.Preferences;

import utils.*;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import mentalModels.MentalModel;
import storyProcessor.*;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import utils.tools.Predicates;
import utils.tools.Search;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/**
 * Summarizer is a class whose methods summarize stories. Stories are received on input ports (LEFT_INPUT and
 * RIGHT_INPUT). There are two story ports so that the summaries of two stories can be compared, as in two
 * interpretations of Macbeth or two interpretations of the Russian-Estonian cyberwar of 2007.
 * <p>
 * English summaries are broadcast on an output port (REPORT_OUTPUT).
 * <p>
 * Section title "summary methods" contains methods that do the actual summary
 * <p>
 * Stories are provided as sequences of inner language elements translated from English augmented by inferences enabled
 * by the explicit story elements. The inner language is described in detail in Winston, Patrick.
 * "The Strong Story Hypothesis and the Directed Perception Hypothesis" AAAI Fall Symposium Series (2011): � 2011
 * Association for the Advancement of Artificial Intelligence
 * <p>
 * Class created on Nov 16, 2013; augmented 27 Oct 2014 to support direct call; comments expanded 20 Dec 2014
 * 
 * @author phw
 */

public class Summarizer extends AbstractWiredBox {

	// // // Constructor and instance getter

	/**
	 * Create a summarizer instance and connect ports to methods. Constructor is private and accessed through the
	 * getSummarizer() method.
	 */
	private Summarizer() {
		super("Summarizer");
		// Key ports for reset and receiving stories
		Connections.getPorts(this).addSignalProcessor(Port.RESET, this::assignAndClearTabs);
		Connections.getPorts(this).addSignalProcessor(LEFT_INPUT, this::processSignalLeft);
		Connections.getPorts(this).addSignalProcessor(RIGHT_INPUT, this::processSignalRight);
		Connections.getPorts(this).addSignalProcessor(SELECTED_TAB, this::processTab);
		// Port for reworking for paricular concept
		Connections.getPorts(this).addSignalProcessor(LEFT_TARGET_CONCEPT, this::processLeftConcept);
		Connections.getPorts(this).addSignalProcessor(RIGHT_TARGET_CONCEPT, this::processRightConcept);
		initializeTable();
	}

	/**
	 * Summarizer instance variable.
	 */
	private static Summarizer summarizer;

	/**
	 * Summarizer instance getter.
	 */
	public static Summarizer getSummarizer() {
		if (summarizer == null) {
			summarizer = new Summarizer();
		}
		return summarizer;
	}

	// // // Methods that initiate processing

	/**
	 * Initiate processing of story coming in through LEFT_INPUT.
	 */
	public void processSignalLeft(Object o) {
		Mark.say("Actuating left-side summary");
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			if (signal.size() < 5) {
				return;
			}

			// Initialize various variables
			leftAnalysis = signal.get(0, Sequence.class);
			leftSummaryDescription.clear();
			rememberTab = false;
			assignAndClearTabs(Markers.RESET);

			// Extract various story descriptors from the signal arriving on one of the input ports.
			Sequence completeStorySequence = pruneStory(signal.get(0, Sequence.class));
			Sequence explicitStorySequence = pruneStory(signal.get(1, Sequence.class));

			leftCompleteStorySequence = completeStorySequence;
			leftExplicitStorySequence = explicitStorySequence;

			List<ConceptDescription> conceptDescriptions = signal.get(4, ConceptAnalysis.class).getConceptDescriptions();

			// Initialize another variable
			leftSummaryDescription.setConceptDescriptions(conceptDescriptions);

			// Call various alternative methods for story summary; key methods are those that use concepts
			if (Switch.includeUnabriggedProcessing.isSelected()) {

				composeCompleteStory(explicitStorySequence, completeStorySequence, leftSummaryDescription);

			}
			composeSummaryFromAntecedentsAndExplicitCauses(explicitStorySequence, completeStorySequence, leftSummaryDescription);
			composeConceptCenteredSummaries(explicitStorySequence, completeStorySequence, conceptDescriptions, leftSummaryDescription);
			composeDominantConceptCenteredSummary(explicitStorySequence, completeStorySequence, conceptDescriptions, leftSummaryDescription);

			// Vanessa's summarizer, rotted
			// composeHumanLikeSummary(explicitStorySequence, completeStorySequence, conceptDescriptions,
			// leftSummaryDescription);

			// Call reporting methods
			transmitStatistics();
			rememberTab = true;
			Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(rememberedTab.getString(), ""));
			Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(this.type5, ""));
		}
	}

	public Sequence processConceptCenteredSummaryDirectly(MentalModel mm) {


		Sequence complete = pruneStory(mm.getStoryProcessor().getStory());

		Sequence explicit = pruneStory(mm.getStoryProcessor().getExplicitElements());

		Sequence result = composeConceptCenteredSummaries(explicit, complete, mm.getStoryProcessor().getConceptAnalysis()
		        .getConceptDescriptions(), leftSummaryDescription);

		return result;

	}

	/**
	 * Initiate processing of story coming in through RIGHT_INPUT. Differs only in calling for summary comparison at the
	 * end. Main work is done by various alternative methods called for story summary.
	 */
	public void processSignalRight(Object o) {
		// Mark.say("Actuating right-side summary");
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			if (signal.size() < 5) {
				return;
			}

			// Initialize various variables
			rightAnalysis = signal.get(0, Sequence.class);
			rightSummaryDescription.clear();
			rememberTab = false;

			// Extract various story descriptors from the signal arriving on one of the input ports.
			Sequence complete = pruneStory(signal.get(0, Sequence.class));
			Sequence explicit = pruneStory(signal.get(1, Sequence.class));

			rightCompleteStorySequence = complete;
			rightExplicitStorySequence = explicit;

			List<ConceptDescription> conceptDescriptions = signal.get(4, ConceptAnalysis.class).getConceptDescriptions();

			// Initialize another variable
			rightSummaryDescription.setConceptDescriptions(conceptDescriptions);

			// Call various alternative methods for story summary; key methods are those that use concepts

			if (Switch.includeUnabriggedProcessing.isSelected()) {
				composeCompleteStory(explicit, complete, rightSummaryDescription);
			}
			composeSummaryFromAntecedentsAndExplicitCauses(explicit, complete, rightSummaryDescription);
			composeConceptCenteredSummaries(explicit, complete, conceptDescriptions, rightSummaryDescription);
			composeDominantConceptCenteredSummary(explicit, complete, conceptDescriptions, rightSummaryDescription);
			composeHumanLikeSummary(explicit, complete, conceptDescriptions, rightSummaryDescription);

			// Compare operation on the two stories coming in on left and right ports
			compareSummaryDescriptions(leftSummaryDescription, rightSummaryDescription);

			// Call reporting methods
			transmitStatistics();
			rememberTab = true;
			Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(rememberedTab.getString(), ""));
			Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(this.type5, ""));
		}
	}

	public void processLeftConcept(Object o) {
		if (o instanceof ConceptDescription) {
			ConceptDescription d = (ConceptDescription) o;
			if (d.getName() != null) {
				List<ConceptDescription> conceptDescriptions = new ArrayList<>();
				conceptDescriptions.add(d);
				Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(type5, TabbedTextViewer.CLEAR));
				composeDominantConceptCenteredSummary(leftExplicitStorySequence, leftCompleteStorySequence, conceptDescriptions, leftSummaryDescription);
			}
		}
	}

	public void processRightConcept(Object o) {
		if (o instanceof ConceptDescription) {
			ConceptDescription d = (ConceptDescription) o;
			if (d.getName() != null) {
				List<ConceptDescription> conceptDescriptions = new ArrayList<>();
				conceptDescriptions.add(d);
				Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(type5, TabbedTextViewer.CLEAR));
				composeDominantConceptCenteredSummary(rightExplicitStorySequence, rightCompleteStorySequence, conceptDescriptions, rightSummaryDescription);
			}
		}
	}

	// // // Summary methods

	/**
	 * The simplest of the summary methods. Eliminates unconnected elements.
	 * <p>
	 * Summary includes only elements from the original story that are antecedents of some inference, but not a
	 * consequence of any inference. Also keeps explicit causal connections.
	 * <p>
	 * summaryDescription is a variable used to retain a memory of the summary for use in experiment analysis
	 */
	public void composeSummaryFromAntecedentsAndExplicitCauses(Sequence explicitStorySequence, Sequence completeStorySequence, SummaryDescription summaryDescription) {
		// Filter out all but antecedents.
		Set<Entity> relevantElements = retainIfAntecedentOrExplicitCause(explicitStorySequence, completeStorySequence);
		// Insert summary into summary memory for experiment analysis
		summaryDescription.setConnected(relevantElements);

		Sequence entitySummary = composeEntitySummaryFromFeeders(completeStorySequence, relevantElements);
		String englishSummary = composeEnglishSummary(entitySummary);
		// Compose; note null argument because no concepts are involved in this method. type2 indicates the
		// type of summary done by this method.
		transmitSummaryDescription(type2, completeStorySequence
		        .getType(), storySize(explicitStorySequence), storySize(entitySummary), null, englishSummary);
	}

	/**
	 * A key summary method. Eliminates elements not connected to concepts
	 * <p>
	 * Tracks back from elements found in concepts, through a chain of 0 or more causal connections to the explicit
	 * causes of those elements. All other causes in the causal chain are eliminated, on the ground they will be
	 * inferred by the reader, with an exception for elements involved in explanation rules. These are retained, because
	 * inferences done by such rules are not as obvious as those done by deduction rules (aka prediction rules).
	 */
	public Sequence composeConceptCenteredSummaries(Sequence explictStorySequence, Sequence completeStorySequence, List<ConceptDescription> conceptDescriptions, SummaryDescription summaryDescription) {
		boolean debug = false;
		// Find elements that are explicit and lead through causal connections to concepts. Note several stages of
		// additional filtering beyond that done by keepIfConceptFeeder. Each type of filtering is described in comments
		// attached to method definitions.
		Set<Entity> conceptFeeders = composeConceptFeedersForConceptCenteredSummaries(completeStorySequence, conceptDescriptions);

		// Compose entity summary by removing redundant entities and adjusting time line
		Sequence entitySummary = composeEntitySummaryFromFeeders(completeStorySequence, conceptFeeders);





		// Remainder of method composes a "summary" consisting of random elements to demonstrate quality of generated
		// summary relative to summary consisting of the same number of random elements

		Set<Entity> explicitElements = findExplicitElements(completeStorySequence);
		Set<Entity> selectedElements = pickAtRandom(conceptFeeders.size(), explicitElements);
		summaryDescription.setRandom(selectedElements);
		entitySummary = composeEntitySummaryFromFeeders(completeStorySequence, selectedElements);

		// Compose English summary using Innerese to English translator
		String englishSummary = composeEnglishSummary(entitySummary);

		// Compose; note final argument is not null because concepts are involved in this method. type4 indicates the
		// type of summary done by this method.
		// Establish names of concepts involved
		Set<String> conceptNames = extractConceptNames(conceptDescriptions);
		// Insert summary into summary memory for experiment analysis
		summaryDescription.setConcept(conceptFeeders);

		Mark.say(debug, "All/feeders", conceptDescriptions.size(), "/", conceptFeeders.size());

		transmitSummaryDescription(type4, completeStorySequence
		        .getType(), storySize(explictStorySequence), storySize(entitySummary), conceptNames, englishSummary);
		transmitSummaryDescription(type0, completeStorySequence
		        .getType(), storySize(explictStorySequence), storySize(entitySummary), conceptNames, englishSummary);
		// For benefit of direct call
		// Mark.say("English:", englishSummary);
		return entitySummary;
	}

	public Set<Entity> composeConceptFeedersForConceptCenteredSummaries(Sequence completeStorySequence, List<ConceptDescription> conceptDescriptions) {
		Set<Entity> conceptFeeders = removeIfInCause(processMeansElements(processAbductionElements(removeIfExplanation(keepIfConceptFeeder(completeStorySequence, conceptDescriptions)))));
		return conceptFeeders;
	}



	/**
	 * A key summary method. Similar to composeConceptCenteredSummaries, but limited to concepts considered dominant.
	 * See composeConceptCenteredSummaries for further explanation.
	 */
	public Sequence composeDominantConceptCenteredSummary(Sequence explictStorySequence, Sequence completeStorySequence, List<ConceptDescription> conceptDescriptions, SummaryDescription summaryDescription) {
		boolean debug = false;

		// Key differece relative to composeConceptCenteredSummaries; only some of the concept descriptions are
		// retained.
		ArrayList<ConceptDescription> relevantConcepts = limitToDominantConcepts(conceptDescriptions);

		// See comments for analogous calls in composeConceptCenteredSummaries

		Set<Entity> conceptFeeders = removeIfInCause(processMeansElements(processAbductionElements(removeIfExplanation(keepIfConceptFeeder(completeStorySequence, relevantConcepts)))));

		Mark.say(debug, "Feeders:", conceptFeeders.size());

		Set<String> conceptNames = extractConceptNames(relevantConcepts);

		summaryDescription.setDominant(conceptFeeders);

		if (relevantConcepts.size() > 0) {

			Set<Entity> dominateConceptFeeders = FeederFinder.getFeederFinder()
			        .findSummaryFeeders(relevantConcepts.get(0), completeStorySequence.getElements(), false);

			noteUnresolvedQuestions(type7, explictStorySequence, completeStorySequence
			        .getType(), completeStorySequence, dominateConceptFeeders, summaryDescription);

		}

		Mark.say(debug, explictStorySequence.getElements().size(), completeStorySequence.getElements().size(), conceptFeeders.size(), conceptNames);

		Sequence entitySummary = composeEntitySummaryFromFeeders(completeStorySequence, conceptFeeders);

		String englishSummary = composeEnglishSummary(entitySummary);


		transmitSummaryDescription(type5, completeStorySequence
		        .getType(), storySize(explictStorySequence), storySize(entitySummary), conceptNames, englishSummary);

		// Mark.say("English:", englishSummary);
		return entitySummary;

	}

	/**
	 * Returns a comprehensive human like summary
	 */

	public void composeHumanLikeSummary(Sequence explicitStorySequence, Sequence completeStorySequence, List<ConceptDescription> conceptDescriptions, SummaryDescription summaryDescription) {

		Set<Entity> conceptFeeders = composeConceptFeedersForConceptCenteredSummaries(completeStorySequence, conceptDescriptions);

		Set<String> conceptNames = extractConceptNames(conceptDescriptions);

		Set<Entity> relevantElements = retainIfAntecedentOrExplicitCause(explicitStorySequence, completeStorySequence);

		conceptFeeders.addAll(relevantElements);

		conceptFeeders.addAll(Search.findLongestPath(completeStorySequence));

		summaryDescription.setConcept(conceptFeeders);

		Sequence entitySummary = composeEntitySummaryFromFeeders(completeStorySequence, conceptFeeders);

		String englishSummary = composeEnglishSummary(entitySummary);

		Mark.say("Vanessa's summary", englishSummary);

		transmitSummaryDescription(type8, completeStorySequence
		        .getType(), storySize(explicitStorySequence), storySize(entitySummary), conceptNames, englishSummary);

	}

	public Set<Entity> getDifferencesInMentalModels(Object signal) {

		if (signal instanceof StoryProcessor) {

			StoryProcessor processor = (StoryProcessor) signal;

			ArrayList<Vector<Entity>> storiesFormDifferentPerspectives = new ArrayList<Vector<Entity>>();
			MentalModel mentalModel = processor.getMentalModel();
			Map<String, MentalModel> mentalModels = mentalModel.getLocalMentalModels();
			Mark.say("Mental models:");
			for (MentalModel mm : mentalModels.values()) {
				storiesFormDifferentPerspectives.addAll((Collection<? extends Vector<Entity>>) mm.getStoryProcessor().getStory());
				StoryProcessor sp = mm.getStoryProcessor();
				storiesFormDifferentPerspectives.add(sp.getStory().getElements());
				Mark.say("Story:", mm.getName());
				for (Entity e : sp.getStory().getElements()) {
					Mark.say("Element:", e);
				}
			}

			Vector<Entity> story1 = storiesFormDifferentPerspectives.get(0);
			Vector<Entity> story2 = storiesFormDifferentPerspectives.get(1);
			Aligner aligner = new Aligner();
			SortableAlignmentList alignments = aligner.align(story1, story2);
			SequenceAlignment bestAlignment = (SequenceAlignment) alignments.get(0);
			LList<PairOfEntities> bestBindings = bestAlignment.bindings;

			for (PairOfEntities pair : bestBindings) {

				Mark.say("Binding", pair);

			}
			;
		}

		return null;

	}

	/**
	 * A method for use wired connections.
	 */
	public Sequence composeEntitySummaryFromFeeders(Sequence completeStorySequence, Set conceptFeeders) {
		return removeRedundantElements(reviseTimeLine(completeStorySequence), conceptFeeders);
	}

	/**
	 * A method for direct call, not used in wired connections.
	 */
	public Sequence composeSummarySequenceUsingDominantConcepts(Sequence completeStorySequence, List<ConceptDescription> relevantConcepts) {
		relevantConcepts = limitToDominantConcepts(relevantConcepts);
		return composeSummarySequenceUsingConcepts(completeStorySequence, relevantConcepts);
	}

	/**
	 * Another method for direct call, not used in wired connections.
	 */
	public Sequence composeSummarySequenceUsingDominantConcepts(Sequence completeStorySequence, List<ConceptDescription> relevantConcepts, boolean withExplanations) {
		Switch.includeExplanations.setSelected(withExplanations);
		relevantConcepts = limitToDominantConcepts(relevantConcepts);
		return composeSummarySequenceUsingConcepts(completeStorySequence, relevantConcepts);
	}

	/**
	 * A method for direct call, not used in wired connections.
	 */
	public Sequence composeSummarySequenceUsingConcepts(Sequence completeStorySequence, List<ConceptDescription> relevantConcepts) {
		Sequence result = new Sequence();
		Set<Entity> conceptFeeders = composeConceptFeedersForConceptCenteredSummaries(completeStorySequence, relevantConcepts);
		Sequence summaryElements = removeRedundantElements(reviseTimeLine(completeStorySequence), conceptFeeders);
		result = summaryElements;
		return result;
	}

	/**
	 * Method for highlighting parts of summary in bold or striking them out to assist reader in noting what has been
	 * done during summarization.
	 */
	public String composeEnglishSummary(Sequence story) {
		String english = "";
		for (Entity entity : story.getElements()) {
			String sentence = Generator.getGenerator().generate(entity);
			// Mark.say(":::\n", entity, "\n", sentence);
			if (sentence != null) {
				if (Switch.showMarkup.isSelected()) {
					if (entity.getProperty(Markers.MARKUP) == Markers.STRIKE) {
						// Mark.say("Noted markup", sentence);
						sentence = Html.strike(Html.red(sentence));
					}
					if (entity.getProperty(Markers.MARKUP) == Markers.HIGHLIGHT) {
						// Mark.say("Noted markup", sentence);
						sentence = Html.green(sentence);
					}
				}
				english += sentence.trim() + "  ";
			}
			else {
				Mark.err("Unexpected null sentence in Summarizer");
			}
		}
		return english;
	}

	/**
	 * Tells story as originally told. No summary is attempted. Intended to be a reference point for evaluating various
	 * kinds of summary.
	 */
	public void composeCompleteStory(Sequence explicitStorySequence, Sequence completeStorySequence, SummaryDescription summaryDescription) {

		// Shut off all compression switches:
		boolean abductionMemory = Switch.includeAbductions.isSelected();
		boolean meansMemory = Switch.eliminateMeans.isSelected();
		boolean postHocMemory = Switch.eliminateIfFollowsFromPrevious.isSelected();
		boolean markupMemory = Switch.showMarkup.isSelected();
		boolean explanationMemory = Switch.includeExplanations.isSelected();
		boolean surpriseMemory = Switch.includeSurprises.isSelected();
		boolean presumptionMemory = Switch.includePresumptions.isSelected();
		Switch.includeAbductions.setSelected(false);
		Switch.eliminateMeans.setSelected(false);
		Switch.eliminateIfFollowsFromPrevious.setSelected(false);
		Switch.showMarkup.setSelected(false);
		Switch.includeExplanations.setSelected(false);
		Switch.includeSurprises.setSelected(false);

		// Tell as told, tricking composeSummary into doing the work by indicating all elements are relevant.
		Set<Entity> relevantElements = new HashSet<Entity>();

		relevantElements.addAll(explicitStorySequence.getElements());

		Sequence entitySummary = composeEntitySummaryFromFeeders(explicitStorySequence, relevantElements);

		String englishSummary = composeEnglishSummary(entitySummary);

		// Mark.say("Complete english summary", englishSummary);

		transmitSummaryDescription(type1, completeStorySequence
		        .getType(), storySize(explicitStorySequence), storySize(entitySummary), null, englishSummary);

		// Restore all compression switches
		Switch.includeAbductions.setSelected(abductionMemory);
		Switch.eliminateMeans.setSelected(meansMemory);
		Switch.eliminateIfFollowsFromPrevious.setSelected(postHocMemory);
		Switch.showMarkup.setSelected(markupMemory);
		Switch.includeExplanations.setSelected(explanationMemory);
		Switch.includeSurprises.setSelected(surpriseMemory);
		Switch.includePresumptions.setSelected(presumptionMemory);
	}

	// // // Composition method

	/**
	 * A key method. Used once relevant elements have been determined in the various ways embodied in the various
	 * summary methods
	 * <p>
	 * mode determines exactly which of several summarizing options is used. Summary may include, for example, all
	 * connected elements, all elements that lead to any discovered concept, all elements that lead to a particular
	 * concept or concepts, etc.
	 * <p>
	 * explicitStorySequence contains story elements that appear explicitly in the story as told. explicitStorySequence
	 * is included so that the system can report the size of the original story.
	 * <p>
	 * completeStorySequence contains all explicitStorySequence elements plus all inferences.
	 * <p>
	 * relevantElements are those that have been judged useful in the summary, mainly by determining those explicit
	 * elements that lead to, via inferences, to elements involved in various concepts. concept names are included so
	 * that the system can report the concepts used in summary
	 * <p>
	 * completeStorySequence retains the order of elements in the original story; relevantElements does not, so
	 * relevantElements variable is used to determine which elements in the ordered completeStorySequence should be
	 * retained.
	 */

	/**
	 * Disappeared in refactoring. Functions moved and method replaced by transmitSummaryDescription
	 */

	// private Sequence composeSummary(String mode, Sequence explicitStorySequence, Sequence completeStorySequence,
	// Set<Entity> relevantElements, Set<String> conceptNames) {
	// // Filter out irrelevant elements from the story as told plus inferences:
	//
	// Sequence summaryElements = removeRedundantElements(reviseTimeLine(completeStorySequence), relevantElements);
	//
	// // summaryElements.getElements().stream().forEachOrdered(f -> Mark.say("Element", f));
	//
	// // Translate relevant elements from internal representation into English:
	// String englishSummary = composeEnglishSummary(summaryElements);
	//
	// // Obtain name of story and sizes for reporting to user
	// String name = completeStorySequence.getType();
	// int orignalSize = storySize(explicitStorySequence);
	// int summarySize = storySize(summaryElements);
	//
	// // Assemble various facts about the analysis, plus the summary itself, into an overall English description
	// String summary = combineFactsAndStatisticsWithSummary(mode, name, conceptNames, orignalSize, summarySize,
	// englishSummary);
	//
	// // Transmit the result to GUI
	// Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(mode, summary));
	//
	// return summaryElements;
	// }

	private void transmitSummaryDescription(String mode, String storyName, int originalSize, int summarySize, Set<String> conceptNames, String englishSummary) {
		// Assemble various facts about the analysis, plus the summary itself, into an overall English description
		String summary = combineFactsAndStatisticsWithSummary(mode, storyName, conceptNames, originalSize, summarySize, englishSummary);

		// Transmit the result to GUI
		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(mode, summary));

	}

	// // // Key supporting methods

	/**
	 * Finds elements (the feeders) that lead to specified concepts. Calls a static method in the FeederFinder class to
	 * do the work. The work is done by tracing back through the causal connections in the story and inferred from the
	 * story.
	 */
	public Set<Entity> keepIfConceptFeeder(Sequence story, List<ConceptDescription> conceptCandidates) {
		boolean debug = false;
		Set<Entity> feeders = new HashSet<>();
		for (ConceptDescription description : conceptCandidates) {
			// boolean debug = "Revenge".equals(description.getName());

			Set<Entity> conceptFeeders = FeederFinder.getFeederFinder().findSummaryFeeders(description, story.getElements(), debug);
			Mark.say(debug, "From", description.getName(), "got", conceptFeeders.size());
			feeders.addAll(conceptFeeders);
			if (debug) {
				for (Entity e : conceptFeeders) {
					if (!Predicates.isCause(e) || Predicates.isExplanation(e)) {
						if (Predicates.isExplanation(e)) {
							Mark.say("Feeder:", Generator.getGenerator().generate(e.getObject()));
						}
						else {
							Mark.say("Feeder:", Generator.getGenerator().generate(e));
						}
					}
				}
			}
		}
		Mark.say(debug, "Returning", feeders.size(), "feeders");
		feeders.stream().forEachOrdered(f -> Mark.say(debug, "Feeder Element", f));

		return feeders;
	}

	/**
	 * Establish dominant concepts; at present, viewed as concepts that involve the maximum number of story elements.
	 */
	public static ArrayList<ConceptDescription> limitToDominantConcepts(List<ConceptDescription> conceptDescriptions) {
		ArrayList<ConceptDescription> biggestConcepts = new ArrayList<>();
		int maxSize = 0;
		for (ConceptDescription candidate : conceptDescriptions) {
			int size = 0;
			// Biggest by number of story elements involved
			size = candidate.getStoryElementsInvolved().getElements().size();
			// If same as biggest so far, keep it
			if (size == maxSize) {
				biggestConcepts.add(candidate);
			}
			// If bigger than biggest so far, keep only it
			else if (size > maxSize) {
				maxSize = size;
				biggestConcepts.clear();
				biggestConcepts.add(candidate);
			}
			// Else ignore
			else {
			}
		}
		// Add surprizes back in
		if (Switch.includeSurprises.isSelected()) {
			for (ConceptDescription candidate : conceptDescriptions) {
				if (candidate.getName().startsWith("Surprise")) {
					Mark.say("Surprise!!!!!!!!!!!!!!!!!!!!!!", candidate.getName());
					if (!biggestConcepts.contains(candidate)) {
						biggestConcepts.add(candidate);
					}
				}
			}
		}
		return biggestConcepts;
	}

	/**
	 * Compares multiple summaries. Used in analyzing what-if scenarios. Might want to note, for example, that Macbeth
	 * no longer murder's Duncan if Lady Macbeth is not greedy.
	 */
	private void compareSummaryDescriptions(SummaryDescription left, SummaryDescription right) {
		HashSet<String> leftNames = new HashSet<>();
		HashSet<String> rightNames = new HashSet<>();
		List<String> missingInRight = new ArrayList<>();
		List<String> missingInLeft = new ArrayList<>();
		for (ConceptDescription r : left.getConceptDescriptions()) {
			leftNames.add(r.getName());
		}
		for (ConceptDescription r : right.getConceptDescriptions()) {
			rightNames.add(r.getName());
		}
		for (String s : leftNames) {
			if (!rightNames.contains(s)) {
				missingInRight.add(s);
			}
		}
		for (String s : rightNames) {
			if (!leftNames.contains(s)) {
				missingInLeft.add(s);
			}
		}
		String output = "I note that ";
		if (!missingInRight.isEmpty()) {
			output += Punctuator.punctuateAnd(missingInRight);
			if (missingInRight.size() == 1) {
				output += " no longer occurs";
			}
			else {
				output += " no longer occur";
			}
		}
		if (missingInRight.isEmpty() && missingInLeft.isEmpty()) {
			output += "there are no noted conceptual differences.";
		}
		else if (!missingInRight.isEmpty() && missingInLeft.isEmpty()) {
			output += "although nothing is missing, ";
		}
		else if (missingInRight.isEmpty() && !missingInLeft.isEmpty()) {
			output += ".";
		}
		else if (!missingInRight.isEmpty() && !missingInLeft.isEmpty()) {
			output += "; but ";
		}
		if (!missingInLeft.isEmpty()) {
			output += Punctuator.punctuateAnd(missingInLeft);
			if (missingInRight.size() == 1) {
				output += " does occur.";
			}
			else {
				output += " do occur.";
			}
		}
		Mark.say(output);

		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(type4, Html.h2(Html.normal("What-if results"))));

		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(type4, Html.p(Html.normal(output))));
	}

	// // // Filters.

	/**
	 * Filter used by composeSummaryFromAntecedentsAndExplicitCauses.
	 */
	public Set<Entity> retainIfAntecedentOrExplicitCause(Sequence explicit, Sequence complete) {
		Set<Entity> keepers = new HashSet<>();
		for (Entity element : complete.getElements()) {
			if (appearsIn(element, explicit)) {
				// If it is an explicit causal connection, keep it
				if (Predicates.isCause(element)) {
					keepers.add(element);
				}
				// If it causes, but is not caused, keep it.
				else if (isAntecedentButNotConsequent(element, complete)) {
					keepers.add(element);
				}
			}
		}
		return keepers;
	}

	/**
	 * Filters out excess material.
	 * <p>
	 */
	private Sequence removeRedundantElements(Sequence completeStorySequence, Set<Entity> relevantElements) {
		Set<Entity> entities = getRolePlayersInSetOfEntities(relevantElements);
		Set<Entity> roleElements = new HashSet<Entity>();
		// If switch on, find classifications of characters, job types, personality traits, and properties. Suppose
		// story includes "Macbeth is a thane"; normally this would not occur in the summary because not connected, but
		// will be supplied if switch is on.
		if (Switch.includeAgentRolesInSummary.isSelected()) {
			roleElements = extractRoleElements(completeStorySequence, entities);
		}

		// If an event is embedded in a causal connection, it is removed. Suppose both "Macduff kills Macbeth"
		// and "Macduff kills Macbeth because Macbeth angered Macduff" are in relevantElements, then Macduff
		// kills Macbeth is redundant.
		Set<Entity> trimmedElements = removeIfInCause(relevantElements);
		// Now arrange remaining elements in same order as story as told and inferences made:
		Sequence elementsRetained = removeIfNotARelevantElement(completeStorySequence, trimmedElements, roleElements);
		// If switch on, then selected because statements are eliminated because they will be readily inferred by the
		// reader because the elements are next to each other. Suppose relevantElements contains
		// "Macbeth murdered Lady Macduff because Macduff fled" and
		// "Macbeth angered Macduff because Macbeth murdered Lady Macduff". Then, this can be reduced to
		// "Macbeth murdered Lady Macduff because Macduff fled" and
		// "Macbeth angered Macduff" because the causal connection between the murder and the anger is readily inferred.
		if (Switch.eliminateIfFollowsFromPrevious.isSelected()) {
			elementsRetained = removeAntecedentsUsingPostHocErgoProperHoc(elementsRetained);
		}
		return elementsRetained;
	}

	/**
	 * Rearranges causes so as to fix time line
	 */
	private Sequence reviseTimeLine(Sequence sequence) {
		Vector<Entity> elements = sequence.getElements();
		Vector<Entity> copy = (Vector) (elements.clone());
		for (int i = 0; i < copy.size(); ++i) {
			Entity e1 = copy.get(i);
			if (Predicates.isCause(e1)) {
				for (int j = i; j < copy.size(); ++j) {
					Entity e2 = copy.get(j);
					if (Predicates.isCause(e2)) {
						if (e1.getSubject().getElements().contains(e2.getObject())) {
							elements.set(i, e2);
							elements.set(j, e1);
						}
					}
				}
			}
		}
		return sequence;
	}

	/**
	 * Filter out elements that are contained in a cause element elsewhere in same set.
	 */
	protected Set<Entity> removeIfInCause(Set<Entity> elements) {
		Set<Entity> result = new HashSet<>();
		for (Entity candidate : elements) {
			boolean found = false;
			for (Entity possibleCause : elements) {
				if (Predicates.isCause(possibleCause)) {
					// Special case for abduction rules
					if (!Switch.includeAbductions.isSelected() && candidate.isA(Markers.ABDUCTION_RULE)) {
						if (!Predicates.equals(possibleCause, candidate) && Predicates.equals(candidate.getObject(), possibleCause.getObject())) {
							found = true;
							break;
						}
					}
					// Now see if candidate is consequent of a cause element
					else if (Predicates.equals(candidate, possibleCause.getObject()) && possibleCause.getProperty(Markers.MARKUP) != Markers.STRIKE) {
						found = true;
						break;
					}
					// ...or antecedent
					else if (possibleCause.getSubject().getElements() != null) {
						if (possibleCause.getSubject().getElements().contains(candidate)) {
							found = true;
							break;
						}
					}
				}
			}
			if (!found) {
				result.add(candidate);
			}
			else {
			}
		}
		return result;
	}

	/**
	 * Get rid of elements that explain details of how something is done or happens.
	 */
	private Set<Entity> removeIfMeans(Set<Entity> input) {
		if (!Switch.eliminateMeans.isSelected()) {
			// Do nothing in this case
			return input;
		}
		// Want to remove means as well as connection to event
		Set<Entity> withoutMeans = new HashSet<>();
		for (Entity e : input) {
			boolean ok = true;
			for (Entity connection : input) {
				if (Predicates.isMeans(connection) && connection.getSubject().getElements().contains(e)) {
					ok = false;
					break;
				}
			}
			if (ok) {
				withoutMeans.add(e);
			}
		}
		Set<Entity> withoutMeansExpressions = new HashSet<>();
		for (Entity e : withoutMeans) {
			if (!Predicates.isMeans(e)) {
				withoutMeansExpressions.add(e);
			}
		}
		return withoutMeansExpressions;
	}

	/**
	 * Remove if easily inferred by abduction (if a kills b, then a must be insane).
	 */
	private Set<Entity> removeIfAbduction(Set<Entity> input) {
		if (Switch.includeAbductions.isSelected()) {
			// Do nothing in this case
			return input;
		}
		// Want to remove abduction as well as connection to event
		Set<Entity> withoutAbduction = new HashSet<>();
		for (Entity e : input) {
			boolean ok = true;
			for (Entity connection : input) {
				if (Predicates.isAbduction(connection) && connection.getSubject().getElements().contains(e)) {
					ok = false;
					Mark.say("Removing abduction", e);
					break;
				}
			}
			if (ok) {
				withoutAbduction.add(e);
			}
		}
		Set<Entity> withoutAbductionExpressions = new HashSet<>();
		for (Entity e : withoutAbduction) {
			if (!Predicates.isAbduction(e)) {
				withoutAbductionExpressions.add(e);
			}
		}
		return withoutAbductionExpressions;
	}

	/**
	 * Remove if an explanation, unless explanation switch set to show it; highlight if retained.
	 */
	private Set<Entity> removeIfExplanation(Set<Entity> input) {
		Set<Entity> result = new HashSet<>();
		for (Entity e : input) {
			if (Predicates.isExplanation(e)) {
				// Mark.say("Highlighting", e);
				e.addProperty(Markers.MARKUP, Markers.HIGHLIGHT);
			}
			if (!Predicates.isPrediction(e)) {
				if (Predicates.isPresumption(e)) {
					if (Switch.includePresumptions.isSelected()) {
						// Include presumption
						result.add(e);
					}
					else {
						// result.add(e.getObject());
					}
				}
				else if (Predicates.isExplanation(e)) {
					// This works, but not sure why
					if (Switch.includeExplanations.isSelected()) {
						// Include explanation
						result.add(e);
					}
					else {
						// Include only consequent, which is in the story, else it wouldn't have been attached to an
						// explanation
						// Mark.say("Adding explained element", e.getObject());
						result.add(e.getObject());
					}
				}
				else {
					result.add(e);
				}
			}
		}
		return result;
	}

	/**
	 * Removes elements deemed not relevant; puts relevant elements in same order as in original story analysis.
	 */
	public Sequence removeIfNotARelevantElement(Sequence completeStorySequence, Set<Entity> relevantElements, Set<Entity> roleElements) {
		boolean debug = false;

		Sequence elementsRetained = new Sequence();
		// Remember what has been told
		Set<Entity> alreadyProcessed = new HashSet<Entity>();
		// Keeps track of previous element in telling
		for (Entity element : completeStorySequence.getElements()) {
			if (relevantElements.contains(element)) {
				elementsRetained.addElement(element);
			}
			// Include agent roles, but not super roles of roles.
			else if (roleElements.contains(element)) {
				Mark.say(debug, "Element to generate", element);
				alreadyProcessed.add(element);
				elementsRetained.addElement(element);
			}
		}

		return elementsRetained;
	}

	/**
	 * Removes antecedents from causal explanation if antecedent would be presumed to be previous element.
	 */
	public static Sequence removeAntecedentsUsingPostHocErgoProperHoc(Sequence completeStorySequence) {
		boolean debug = false;
		Sequence elementsRetained = new Sequence();
		// Remember what has been told
		Set<Entity> alreadyProcessed = new HashSet<Entity>();
		// Keeps track of previous element in telling
		Entity penultimate = null;
		for (Entity element : completeStorySequence.getElements()) {
			// Don't tell it twice
			if (alreadyProcessed.contains(element)) {
				continue;
			}
			// Here is a nice, special case; if previous element provides an antecedent of the current
			// causal statement, then just remove it on grounds of post hoc ergo propter hoc
			if (penultimate != null) {
				if (Predicates.isCause(element)) {
					List<Entity> elementAntecedents = element.getSubject().getElements();
					if (Predicates.contained(penultimate, elementAntecedents)) {
						// Need to reconstitute cause here, so as not to screw up anything
						Mark.say(debug, "Post hoc processing triggered on", element);
						Object o = element.getProperty(Markers.MARKUP);
						element = copyWithoutGivenAntecedent(penultimate, element);
						// Retain markup
						if (o != null) {
							element.addProperty(Markers.MARKUP, o);
						}
						Mark.say(debug, "Post hoc processing trims to", element);
					}
				}
			}
			alreadyProcessed.add(element);
			elementsRetained.addElement(element);

			penultimate = element;
			if (Predicates.isCause(penultimate)) {
				penultimate = penultimate.getObject();
			}
		}
		return elementsRetained;
	}

	/**
	 * Remove elements that explain details of how an act is accomplished.
	 */
	public static Set<Entity> removeMeans(Set<Entity> relevantElements) {
		boolean debug = false;
		Set<Entity> elementsRetained = new HashSet<>();
		// Remember what has been told
		Set<Entity> alreadyProcessed = new HashSet<Entity>();
		for (Entity element : relevantElements) {
			// Don't tell it twice
			if (alreadyProcessed.contains(element)) {
				continue;
			}
			if (Predicates.isMeans(element)) {
				element = element.getObject();
			}
			Mark.say(debug, "Ordinary element to generate", element);
			alreadyProcessed.add(element);
			elementsRetained.add(element);
		}
		return elementsRetained;
	}

	/**
	 * Remove elements readily inferred by abduction.
	 */
	protected Set<Entity> removeAbductions(Set<Entity> relevantElements) {
		boolean debug = false;
		Set<Entity> elementsRetained = new HashSet<>();
		// Remember what has been told
		Set<Entity> alreadyProcessed = new HashSet<Entity>();
		for (Entity element : relevantElements) {
			// Don't tell it twice
			if (alreadyProcessed.contains(element)) {
				continue;
			}
			// If abduction, reader can deduce connection, just as if prediction.
			if (element.isA(Markers.ABDUCTION_RULE)) {
				// Mark.say("Abduction found", element);
				Mark.say(debug, "Abduction element to generate", element);
				alreadyProcessed.add(element);
				alreadyProcessed.add(element.getObject());
				elementsRetained.add(element);
			}
			else {
				Mark.say(debug, "Ordinary element to generate", element);
				alreadyProcessed.add(element);
				elementsRetained.add(element);
			}
		}
		return elementsRetained;
	}

	// // // Methods for direct call.

	/**
	 * Used when summarization is called as a subroutine by another method, rather than actuated by a signal arriving on
	 * a port.
	 */
	public Sequence composeConceptCenteredSummary(BetterSignal signal) {
		if (signal.size() < 5) {
			Mark.say("BetterSignal argument has too few elements");
			return null;
		}
		Sequence completeStory = pruneStory(signal.get(0, Sequence.class));
		List<ConceptDescription> conceptDescriptions = signal.get(4, ConceptAnalysis.class).getConceptDescriptions();
		Set<Entity> conceptFeeders = composeConceptFeedersForConceptCenteredSummaries(completeStory, conceptDescriptions);

		return removeRedundantElements(completeStory, conceptFeeders);
	}

	/**
	 * Used when summarization is called as a subroutine by another method, rather than actuated by a signal arriving on
	 * a port.
	 */
	public Sequence composeDominantConceptCenteredSummary(BetterSignal signal) {
		if (signal.size() < 5) {
			Mark.say("BetterSignal argument has too few elements");
			return null;
		}
		Sequence completeStory = pruneStory(signal.get(0, Sequence.class));

		List<ConceptDescription> conceptDescriptions = signal.get(4, ConceptAnalysis.class).getConceptDescriptions();
		ArrayList<ConceptDescription> biggestConcepts = limitToDominantConcepts(conceptDescriptions);
		Set<Entity> conceptFeeders = removeIfInCause(processMeansElements(processAbductionElements(removeIfExplanation(keepIfConceptFeeder(completeStory, biggestConcepts)))));
		return removeRedundantElements(completeStory, conceptFeeders);

	}

	// // // Striking and highlighting

	/**
	 * Markup method, determining whether material is removed or struck.
	 */
	private Set<Entity> processMeansElements(Set<Entity> set) {
		if (Switch.showMarkup.isSelected()) {
			return strikeIfMeans(set);
		}
		return removeIfMeans(set);
	}

	/**
	 * Markup method, determining whether material is removed or struck.
	 */
	private Set<Entity> processAbductionElements(Set<Entity> set) {
		if (Switch.showMarkup.isSelected()) {
			return strikeIfAbduction(set);
		}
		return removeIfAbduction(set);
	}

	/**
	 * Strike out if it merely reports on how something is done.
	 */
	private Set<Entity> strikeIfMeans(Set<Entity> input) {
		for (Entity e : input) {
			if (Predicates.isMeans(e)) {
				e.addProperty(Markers.MARKUP, Markers.STRIKE);
			}
		}
		return input;
	}

	/**
	 * Strike out if it is readily inferred by abduction (If a kills b, then a must be crazy).
	 */
	private Set<Entity> strikeIfAbduction(Set<Entity> input) {
		Set<Entity> result = new HashSet<>();
		for (Entity e : input) {
			if (Predicates.isAbduction(e)) {
				e.addProperty(Markers.MARKUP, Markers.STRIKE);
				result.add(e.getObject());
				Mark.say("Abduction translated into", e.getObject());
			}
			result.add(e);
		}
		return result;
	}

	// // // Miscellaneous methods

	/**
	 * Utility method for combining summary and various facts about the summary into a nicely displayed form.
	 */
	private String combineFactsAndStatisticsWithSummary(String mode, String name, Set<String> conceptNames, int storySize, int asToldSize, String revisedStory) {
		String percent = String.format("%3.1f", asToldSize * 100.0 / storySize);
		// Add name to table
		if (row.isEmpty()) {
			row.add(Html.size3(Html.bold(Punctuator.conditionName(name))));
		}
		// Add numbers to table
		// if (mode != type0 && mode != type6 && mode != type1) {
		if (mode != type0 && mode != type1) {
			row.add(Html.size3(percent));
		}

		String summary = "";

		if (conceptNames != null && !conceptNames.isEmpty()) {
			List<String> names = new ArrayList<>(conceptNames);
			summary += "The story is about " + Punctuator.punctuateAnd(names);
		}

		if (revisedStory.trim().isEmpty()) {
			summary += "  There is no summary based on the indicated conceptual content.";
		}

		summary += Html.p(revisedStory);

		// summary += Html.p("Summary contains " + asToldSize + " of " + storySize + " elements in the story or " +
		// percent + "%.");

		summary += Html.p("Story contains " + storySize + " elements, summary " + asToldSize + ", or " + percent + "%.");

		// Transmit to appropriate tab in summary pane

		if (mode == type5) {
			summary = Html.h2(Punctuator.conditionName(name)) + Html.p(Html.normal(summary));
		}
		else {
			summary = Html.h2(mode + " summary of " + Punctuator.conditionName(name)) + Html.p(Html.normal(summary));
		}
		return summary;
	}

	/**
	 * Notes unexplained actions. Used to establish what must be considered believable by user for story to be
	 * believable. For example, in a version of Macbeth, "Lady Macbeth wanted to be queen," could be an unexplained but
	 * believable action.
	 * 
	 * @param explictStorySequence
	 */
	private void noteUnresolvedQuestions(String mode, Sequence explictStorySequence, String name, Sequence analysis, Set<Entity> summaryElements, SummaryDescription summaryDescription) {
		Set<Entity> processed = new HashSet<>();
		Set<Entity> unprocessed = new HashSet<>();
		List<Entity> questions = new ArrayList<>();

		Set<Entity> storyElements = new HashSet<>(explictStorySequence.getElements());

		Generator generator = Generator.getGenerator();
		// Prepare summary title
		String summary = "";
		// Expand unprocessed to include antecedents of causes
		for (Entity element : summaryElements) {
			// Mark.say("Checking", element);
			if (Predicates.isCause(element) && !Predicates.isMeans(element)) {
				// Mark.say("Viewing", element);
				for (Entity antecedent : element.getSubject().getElements()) {
					if (storyElements.contains(antecedent)) {
						unprocessed.add(antecedent);
					}
				}
			}
		}
		for (Entity element : unprocessed) {
			// Mark.say("Looking at", element);
			if (processed.contains(element)) {
				// Already processed
				// Mark.say("Ignoring", element);
				continue;
			}
			else if (!Predicates.isCause(element) && element.isA(Markers.ACTION_MARKER)) {
				if (!isConsequent(element, analysis)) {
					// Mark.say("Not consequent", element);
					questions.add(element);
				}
				else {
					// Mark.say("Consequent", element);
				}
			}
			processed.add(element);
		}
		summaryDescription.setQuestions(questions);
		if (questions.isEmpty()) {
			summary += "  There are no questions.";
			summary += Html.p("");
		}
		else {
			summary += "<ul>";
			for (Entity element : questions) {
				String question = generator.generateInPastTense(new Function(Markers.WHY_QUESTION, element));
				if (question != null) {
					summary += "<li>" + question + "</li>\n";
				}
				else {
					Mark.err("Could not generate question from", element);
				}
			}
			summary += "</ul>";
		}
		summary += "Questions are about unexplained actions that lead to the central concept.";

		summary = Html.h2(mode + " in " + Punctuator.conditionName(name)) + Html.normal(summary);

		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(mode, summary));
	}

	/**
	 * Clear summary description here and in GUI.
	 */
	public void assignAndClearTabs(Object o) {
		if (o == Markers.RESET) {
			leftSummaryDescription = new SummaryDescription();
			rightSummaryDescription = new SummaryDescription();
			rememberTab = false;
			for (String s : modes) {
				Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(s, TabbedTextViewer.CLEAR));
			}
			rememberTab = true;
			// Mark.say("Remembered", rememberedTab.getString());
			Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(rememberedTab.getString(), ""));
		}
	}

	/**
	 * Initialize table of experimental results in the GUI.
	 */
	public void initializeTable() {
		table = Html.tableWithPadding(15, Html.size3("Story"), Html.size3(type2), Html.size3("Concepts"), Html.size3("Dominant"));
		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(stats, TabbedTextViewer.CLEAR)); //
		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(stats, table)); //
	}

	/**
	 * Determines if element is a consequent in some causal connection emebeded in the sequence.
	 */
	private boolean isConsequent(Entity element, Sequence sequence) {
		for (Entity x : sequence.getElements()) {
			if (Predicates.isCause(x) && Predicates.equals(x.getObject(), element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Prepares GUI to receive summary results.
	 */
	public void transmitStatistics() {
		initializeTable();
		table = Html.tableAddRow(table, row);
		row.clear();
		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(stats, TabbedTextViewer.CLEAR));
		Connections.getPorts(this).transmit(REPORT_OUTPUT, new BetterSignal(stats, table));
	}

	/**
	 * Prunes antecedent out of causal connection. Used when user would naturally infer that one of the antecedents of
	 * the consequent is an immediately preceeding element, already in the summary.
	 */
	private static Entity copyWithoutGivenAntecedent(Entity penultimate, Entity cause) {
		Sequence sequence = new Sequence(Markers.CONJUNCTION);
		for (Entity antecedent : cause.getSubject().getElements()) {
			if (!Predicates.equals(penultimate, antecedent)) {
				// Mark.say("Adding", antecedent);
				sequence.addElement(antecedent);
			}
			else {
				// Mark.say("Pruned out", antecedent);
			}
		}
		// If nothing left, just return consequent
		if (sequence.getElements().isEmpty()) {
			return cause.getObject();
		}
		// Else, return reduced copy of cause
		Relation copy = new Relation(Markers.CAUSE_MARKER, sequence, cause.getObject());
		copy.setBundle((Bundle) (cause.getBundle().clone()));
		return copy;
	}

	/**
	 * Translates summary type into English description for consumption by user.
	 */

	public String addDescription(String mode) {
		String summary = "";
		if (mode == type0) {
			summary = "\nSummary consists of random elements in the story.";
		}
		else if (mode == type1) {
			summary = "\nComplete story consists of all explicit elements.";
		}
		else if (mode == type2) {
			summary = "\nSummary consists of all elements in the story that cause or are caused.";
		}
		else if (mode == type4) {
			summary = "\nSummary consists of all elements in the story that cause, but are not caused, and lead to concepts, plus helpful inferences.";
		}
		else if (mode == type5) {
			summary = "\nSummary consists of all elements in the story that cause, but are not caused, and lead to key concept, plus helpful inferences.";
		}
		else if (mode == type8) {
			summary = "\nSummary consists of elemets in the story most likely to be chosen by a human subject.";
		}
		// else if (mode == type6) {
		// summary = "\nSummary consists of all elements in the story that cause, but are not caused, and lead to " +
		// testConcept
		// + " concept, plus helpful inferences.";
		// }
		summary += "  ";
		return Html.br(summary);
	}

	/**
	 * Counts elements in story sequence.
	 */
	protected int storySize(Sequence story) {
		return countElements(story.getElements());
	}

	/**
	 * Counts elements in list of story elements. Returns element count + 1 for each causal connection; hashing prevents
	 * double counting of elements that appear in more than one causal connection. Used in experiment analysis.
	 */
	private int countElements(List<Entity> list) {
		int r = 0;
		Set<String> elements = new HashSet<>();
		for (Entity e : list) {
			if (e.getProperty(Markers.MARKUP) == Markers.STRIKE) {
				// Ignore, not really there
			}
			else if (Predicates.isCause(e)) {
				Entity consequent = e.getObject();
				Vector<Entity> antecedents = e.getSubject().getElements();
				// Antecedents
				for (Entity x : antecedents) {
					if (!elements.contains(x.toString())) {
						++r;
						elements.add(x.toString());
					}
				}
				// Consequent
				if (!elements.contains(consequent.toString())) {
					++r;
					elements.add(consequent.toString());
				}
			}
			// Catches whole entity whether cause or not
			if (!elements.contains(e.toString())) {
				elements.add(e.toString());
				++r;
			}
		}
		return r;
	}

	/**
	 * Get rid of junk elements including classifications and character markers.
	 */
	private Sequence pruneStory(Sequence story) {
		boolean debug = false;
		// Have to clone this for bundle to be same in original story and in result
		Bundle bundle = (Bundle) (story.getBundle().clone());
		Sequence result = new Sequence(bundle);
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.CLASSIFICATION_MARKER) && element.getSubject().isA(Markers.CHARACTER)) {
				Mark.say(debug, "No character classification in summary");
			}
			// On the contrary, in Estonia story, need to know for summary
			// else if (element.getSubject().isA("i") || element.getObject().isA("i")) {
			// Mark.say(debug, "No I in summary");
			// }
			else if (element.relationP("start")) {
				Mark.say(debug, "No Start in summary");
			}
			else {
				result.addElement(element);
			}
		}
		return result;
	}

	/**
	 * Find elements that classify characters according to types, job types, personality traits, and properties.
	 */
	public Set<Entity> extractRoleElements(Sequence story, Set<Entity> entities) {
		// The characterizations
		Set<Entity> results = new HashSet<>();
		// The role players
		Set<Entity> included = new HashSet<>();
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.JOB_TYPE_MARKER) && entities.contains(element.getSubject())) {
				if (element.getSubject().getProperty(Markers.PROPER) != null) {
					included.add(element.getSubject());
					results.add(element);
				}
			}
		}
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.CLASSIFICATION_MARKER)) {
				// Hack to deal with Matt's hack of marking entities as "is a character".
				if (!element.getSubject().getType().equals(Markers.CHARACTER)) {
					if (entities.contains(element.getObject())) {
						if (element.getObject().getProperty(Markers.PROPER) != null) {
							// Ignore if already know job
							if (!included.contains(element.getObject())) {
								results.add(element);
							}
						}
					}
				}
			}
		}
		for (Entity element : story.getElements()) {
			if (element.relationP(Markers.PERSONALITY_TRAIT) || element.relationP(Markers.PROPERTY_TYPE)) {
				results.add(element);
			}
		}
		return results;
	}

	/**
	 * Finds role players embedded in a set. See getRolePlayersInEntity for further explanation.
	 */
	protected HashSet<Entity> getRolePlayersInSetOfEntities(Set<Entity> set) {
		HashSet<Entity> result = new HashSet<Entity>();
		for (Entity x : set) {
			result.addAll(getRolePlayersInEntity(x));
		}
		return result;
	}

	/**
	 * Finds role players embedded in inner language element. Role players are, for example, actors in actions.
	 */
	private HashSet<Entity> getRolePlayersInEntity(Entity x) {
		HashSet<Entity> result = new HashSet<Entity>();
		if (x.entityP()) {
			result.add(x);
		}
		else if (x.functionP()) {
			result.addAll(getRolePlayersInEntity(x.getSubject()));
		}
		else if (x.relationP()) {
			result.addAll(getRolePlayersInEntity(x.getSubject()));
			result.addAll(getRolePlayersInEntity(x.getObject()));
		}
		else if (x.sequenceP()) {
			for (Entity e : x.getElements()) {
				result.addAll(getRolePlayersInEntity(e));
			}
		}
		return result;
	}

	/**
	 * Finds entities inside instantiations of explanation rules and in leads-to constructions
	 */
	private boolean mayOrLeadsTo(Entity givenElement, Sequence original) {
		if (Predicates.isCause(givenElement)) {
			if (givenElement.isA(Markers.EXPLANATION_RULE) || givenElement.isA(Markers.ENTAIL_RULE)) {
				for (Entity originalElement : original.getElements()) {
					if (Predicates.equals(givenElement.getObject(), originalElement)) {
						return true;
					}
					else {
						if (Predicates.contained(originalElement, givenElement.getSubject().getElements())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determines if entity is inside most forms of cause
	 */
	private boolean isAntecedentButNotConsequent(Entity element, Sequence analysis) {
		boolean ok = false;
		for (Entity entity : analysis.getElements()) {
			if (Predicates.isPrediction(entity)) {
				if (entity.getSubject().getElements().contains(element)) {
					ok = true;
				}
				else if (Predicates.equals(element, entity.getObject())) {
					return false;
				}
			}
		}
		return ok;
	}

	/**
	 * Determines if element appears is in a sequence
	 */
	private boolean appearsIn(Entity element, Sequence original) {
		return Predicates.contained(element, original.getElements());
	}

	/**
	 * Makes list of names of concepts provided, such as "revenge" or "skepticism."
	 */
	public Set<String> extractConceptNames(List<ConceptDescription> concepts) {
		Set<String> names = new HashSet<>();
		for (ConceptDescription x : concepts) {
			names.add(x.getName());
		}
		return names;
	}

	/**
	 * Picks random set of elements of specified size.
	 */
	private Set<Entity> pickAtRandom(int size, Set<Entity> explicitElements) {
		boolean debug = false;
		Mark.say(debug, "Size of random story desired", size);
		List<Entity> candidates = new ArrayList<Entity>(explicitElements);
		Set<Entity> result = new HashSet<Entity>();
		if (explicitElements.size() <= size) {
			return explicitElements;
		}
		while (result.size() < size) {
			int index = (int) (Math.random() * explicitElements.size());
			result.add(candidates.get(index));
		}
		Mark.say(debug, "Size of random story actual", result.size());
		return result;
	}

	/**
	 * Finds elements in story explicitly provided by weeding out inferences.
	 */
	private Set<Entity> findExplicitElements(Sequence story) {
		Set<Entity> result = new HashSet<>();
		Set<Entity> ineligible = new HashSet<>();
		List<Entity> storyElements = story.getElements();
		for (Entity element : storyElements) {
			// If one of these, then put in by common sense, so eliminate
			if (element.isA(Markers.PREDICTION_RULE) || element.isA(Markers.EXPLANATION_RULE) || element.isA(Markers.ABDUCTION_RULE)
			        || element.isA(Markers.ENTAIL_RULE)) {
				ineligible.add(element);
				ineligible.add(element.getObject());
			}
			// If an explicit cause, then keep it, but not its parts
			else if (Predicates.isCause(element)) {
				for (Entity entity : element.getSubject().getElements()) {
					ineligible.add(entity);
				}
				ineligible.add(element.getObject());
			}
		}
		// Retain all that is not ineligible
		for (Entity element : storyElements) {
			if (!ineligible.contains(element)) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Provides story elements involved in summary to GUI for display.
	 */
	public void processTab(Object o) {
		if (o instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) o;
			if (signal.elementIsType(0, String.class) && signal.get(0, String.class).equals(TabbedTextViewer.SELECTED_TAB)) {
				String tab = signal.get(1, String.class);
				if (rememberTab) {
					rememberedTab.getString(tab);
					// Mark.say("Tab", tab, rememberedTab.getString(), "selected");
				}

				Set<Entity> leftSummary;
				Set<Entity> rightSummary;
				if (tab == type0) {
					leftSummary = leftSummaryDescription.getRandomSummary();
					rightSummary = rightSummaryDescription.getRandomSummary();
				}
				else if (tab == type1) {
					leftSummary = leftSummaryDescription.getCompleteStory();
					rightSummary = rightSummaryDescription.getCompleteStory();
				}
				else if (tab == type2) {
					leftSummary = leftSummaryDescription.getConnected();
					rightSummary = rightSummaryDescription.getConnected();
				}

				else if (tab == type4) {
					leftSummary = leftSummaryDescription.getConcept();
					rightSummary = rightSummaryDescription.getConcept();
				}
				else if (tab == type5) {
					leftSummary = leftSummaryDescription.getDominant();
					rightSummary = rightSummaryDescription.getDominant();
				}
				// else if (tab == type6) {
				// leftSummary = leftSummaryDescription.getSpecial();
				// rightSummary = rightSummaryDescription.getSpecial();
				// }
				else if (tab == type7) {
					leftSummary = leftSummaryDescription.getDominant();
					rightSummary = rightSummaryDescription.getDominant();
				}
				else if (tab == type7) {
					leftSummary = leftSummaryDescription.getQuestions();
					rightSummary = rightSummaryDescription.getQuestions();
				}
				else if (tab == type8) {
					leftSummary = leftSummaryDescription.getConnected();
					rightSummary = rightSummaryDescription.getConnected();
				}
				else {
					leftSummary = new HashSet<Entity>();
					rightSummary = new HashSet<Entity>();
				}
				// Mark.say("Sending", leftSummary.size(), "elements");
				Connections.getPorts(this).transmit(ConceptBar.RESET, ConceptBar.RESET);
				Connections.getPorts(this).transmit(SELECTED_LEFT_DESCRIPTION, new BetterSignal(leftSummary, leftAnalysis));
				Connections.getPorts(this).transmit(SELECTED_RIGHT_DESCRIPTION, new BetterSignal(rightSummary, rightAnalysis));
			}
		}
	}

	// Controls

	private boolean rememberTab = false;

	private StringWithMemory rememberedTab = new StringWithMemory("summaryTab", type5);

	// Variables

	private Sequence leftAnalysis = new Sequence();

	private Sequence leftCompleteStorySequence = new Sequence();

	private Sequence leftExplicitStorySequence = new Sequence();

	private Sequence rightAnalysis = new Sequence();

	private Sequence rightCompleteStorySequence = new Sequence();

	private Sequence rightExplicitStorySequence = new Sequence();

	// private static String testConcept = "Answered prayer";

	public static String type0 = "Random";

	public static String type1 = "Complete";

	public static String type2 = "Connected";

	public static String type4 = "Concept centered";

	public static String type5 = "Dominant concept centered";

	public static String type8 = "Human_Like";

	// private static String type6 = testConcept;

	private static String type7 = "Unresolved questions";

	private static String stats = "Table";

	// private String[] modes = { type0, type1, type2, type4, type5, type6, type7, stats };

	private String[] modes = { type0, type1, type2, type4, type5, type7, type8, stats };

	private String table;

	private ArrayList<String> row = new ArrayList<String>();

	private SummaryDescription leftSummaryDescription = new SummaryDescription();;

	private SummaryDescription rightSummaryDescription = new SummaryDescription();;

	// Markers

	public static final String SELECTED_TAB = "selected tab";

	// Inputs

	public static final String LEFT_INPUT = "Left perspective input";

	public static final String RIGHT_INPUT = "Right perspective input";

	public static final String LEFT_TARGET_CONCEPT = "Left targeted Concept";

	public static final String RIGHT_TARGET_CONCEPT = "Right targeted Concept";

	// Outputs

	public static final String REPORT_OUTPUT = "Report";

	public static final String SUMMARY_OUTPUT = "summary";

	public static final String SELECTED_LEFT_DESCRIPTION = "Selected left description to display";

	public static final String SELECTED_RIGHT_DESCRIPTION = "Selected right description to display";

	public class StringWithMemory {

		String name;

		String string;

		public String getString() {
			return string;
		}

		public void getString(String theString) {
			Preferences.userRoot().put(name, string);
			this.string = theString;
		}

		public StringWithMemory(String name, String defaultValue) {
			this.name = name;
			this.string = Preferences.userRoot().get(name, defaultValue);
		}
	}

}



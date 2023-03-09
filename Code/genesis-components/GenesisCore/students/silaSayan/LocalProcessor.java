package silaSayan;

import generator.Generator;
import genesis.*;
import gui.TabbedTextViewer;

import java.util.ArrayList;

import matchers.StandardMatcher;
import storyProcessor.*;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Sequence;

public class LocalProcessor extends AbstractWiredBox {

	// SWITCHES

	private boolean isItOnlyRelevant = false; // indicates filtering for reflection rules

	private boolean isItFullStory = true; // indicates telling of the whole story stream as it would be told

	private boolean isItMixture = false;

	// PORTS

	public final String MY_INPUT_PORT = "my input port";

	public final String MY_OUTPUT_PORT = "my output port";

	public static final String STORY1 = "story 1";

	public static final String STORY2 = "story 2";

	public static final String PLOT = "story plot";

	public static final String QUIESCENCE_PORT1 = "quiescence port 1";

	public static final String QUIESCENCE_PORT2 = "quiescence port 2";

	public static final String RULE_PORT = "rules";

	public static final String REFLECTION_PORT1 = "reflection port 1";

	public static final String REFLECTION_PORT2 = "reflection port 2";

	public static final String START_STORY_INFO1 = "start story info 1";

	public static final String START_STORY_INFO2 = "start story info 2";

	public static final String INFERENCES = "inferences";

	public static final String INCREMENT = "increment";

	public static String TEACH_RULE_PORT = "teach rule port";

	public static String NEW_RULE_MESSENGER_PORT = "new rule messenger port";

	// FIELDS

	private ArrayList<Entity> unmatchedList = new ArrayList<Entity>(); // store already unmatched components to avoid
																	   // printing them over and over

	private boolean isOneQuiet = false; // indicates perspective one reaching quiescence

	private boolean isTwoQuiet = false; // indicates perspective two reaching quiescence

	private Sequence quietIntervalOne = new Sequence(); // what perspective one has accumulated since last quiescence

	private Sequence quietIntervalTwo = new Sequence(); // what perspective one has accumulated since last quiescence

	private Sequence rules = new Sequence(); // stores uninstantiated rules

	private Sequence rulesAlreadyReported = new Sequence(); // stores uninstantiated rules already reported as being
															// related to missing instantiated rules

	private Sequence reflectionRules = new Sequence(); // stores all rules used in reflections

	private Sequence relevantRules = new Sequence(); // stores all rules used in reflections that are missing in second
													 // perspective

	private Sequence missingRulesToCompareToReflectionRules = new Sequence(); // stores all missing rules

	@SuppressWarnings("unused")
	private Entity startInfo1 = new Entity(); // info to trigger start of first story

	@SuppressWarnings("unused")
	private Entity startInfo2 = new Entity(); // info to trigger start of second story

	private Sequence plotReceivedSoFar = new Sequence();

	private Sequence storyToldSoFar = new Sequence(); // stores story told so far

	private boolean storyHasStarted = false;

	private Sequence preRelevantStoryStream = new Sequence();

	private Sequence relevantStoryStream = new Sequence();

	public LocalProcessor() {
		this.setName("My story processor");

		/** COMPLETE. gets complete story, outputs it. **/
		// Connections.getPorts(this).addSignalProcessor("processSignal");

		/**
		 * Uncomment the following two lines to revert back to original signal processors. July 19: Doesn't work.
		 * Doesn't do anything. FIXED. the problem was checking that signal is a sequence. but this port transmits thing
		 * by thing, without packaging into sequences.
		 **/
		// Connections.getPorts(this).addSignalProcessor(STORY1, "processSignal1");
		// Connections.getPorts(this).addSignalProcessor(STORY2, "processSignal2");

		// These connections used to read story and do perspective comparison.
		Connections.getPorts(this).addSignalProcessor(QUIESCENCE_PORT1, "processQuiescence1");
		Connections.getPorts(this).addSignalProcessor(QUIESCENCE_PORT2, "processQuiescence2");

		// This connection used to figure out base rules for the Priming with Introspection level.
		Connections.getPorts(this).addSignalProcessor(RULE_PORT, "processRules");

		// This connection used to figure out what rules are crucial for reflections.
		Connections.getPorts(this).addSignalProcessor(REFLECTION_PORT1, "processReflectionsForRules1");

		Connections.getPorts(this).addSignalProcessor(PLOT, "processPlot");
	}

	/** Should handle the printing out of the whole story. **/
	@SuppressWarnings("unused")
	public void processPlot(Object o) {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		Generator generator = Generator.getGenerator();

		if (o instanceof Entity) {
			Entity e = (Entity) o;
			if (e.relationP() && e.getSubject().entityP("you")) {
				if (e.getObject().functionP(Markers.STORY_MARKER)) {
					Mark.say("Story has started!!");
					storyHasStarted = true;
				}
			}

			if (storyHasStarted) {
				if (!e.functionP()) {
					if (!plotReceivedSoFar.containsDeprecated(e)) {
						plotReceivedSoFar.addElement(e);
						Mark.say("Just added ", e.asString(), "to plotReceivedSoFar");
					}

				}

			}

		}
	}

	/** Extracts rules used in reflections. */
	public void processReflectionsForRules1(Object o) {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		if (o instanceof ConceptAnalysis) {
			ConceptAnalysis conceptAnalysis = (ConceptAnalysis) o;
			for (ConceptDescription description : conceptAnalysis.getConceptDescriptions()) {
				for (Entity t : description.getRules().getElements()) {
					reflectionRules.addElement(t);
				}
			}

			// Tell the whole story (i.e. with plot) but first it will be sorted for what's relevant and what's not.
			if (isItFullStory) {

				filterStoryForRelevance();
			}
			// Tell only the missing stuff, and among those, only those which are relevant.
			else {
				outputRelevantRules();
			}

		}
	}

	@SuppressWarnings("unused")
	public void filterStoryForRelevance() {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Generator generator = Generator.getGenerator();

		// Mark.say("Size of preRelevantStoryStream: ", preRelevantStoryStream.getElements().size());

		for (Entity t : preRelevantStoryStream.getElements()) {

			if (plotReceivedSoFar.containsDeprecated(t)) {
				relevantStoryStream.addElement(t);
				String plotResult = generator.generate(t);
				Mark.say("RELEVANT Plot/Rule:  ", plotResult);
				BetterSignal signal = new BetterSignal("Story teller", plotResult);
				Connections.getPorts(this).transmit(signal);
			}
			else if (reflectionRules.containsDeprecated(t)) {
				relevantStoryStream.addElement(t);
				String ruleResult = generator.generate(t);
				String ruleResultFinal = "***" + ruleResult + "***";
				Mark.say("RELEVANT Plot/Rule:  ", ruleResultFinal);
				BetterSignal signal = new BetterSignal("Story teller", ruleResultFinal);
				Connections.getPorts(this).transmit(signal);
			}
		}
	}

	/** Outputs those rules missing in the second perspective that are relevant to reflections. **/
	public void outputRelevantRules() {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Generator generator = Generator.getGenerator();
		Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Story teller");

		// For each rule missing in the listener perspective
		for (Entity t : missingRulesToCompareToReflectionRules.getElements()) {
			// Look through the rules necessary for reflections
			for (Entity r : reflectionRules.getElements()) {

				// If missing rule necessary for reflection
				if (matcher.match(t, r) != null) {
					if (!relevantRules.containsDeprecated(t)) {
						relevantRules.addElement(t);
						Mark.say("Second perspective missing these RELEVANT RULES: ");
						String result = generator.generate(t);
						String finalResult = "***" + result + "***";
						if (result != null) {
							BetterSignal signal = new BetterSignal("Story teller", finalResult);
							Connections.getPorts(this).transmit(signal);
							Mark.say(result);
						}
						else {
							Mark.say("RESULT IS NULL!!");
						}
					}
				}
			}
		}
	}

	/** Extracts UNINSTANTIATED RULES from signal */
	public void processRules(Object o) {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		if (o instanceof Sequence) {
			rules = (Sequence) o;
		}
	}

	/** Signals quiescence of first perspective. Extracts buffer contents at quiescence. */
	public void processQuiescence1(Object o) {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		if (o instanceof Sequence) {
			Sequence increment = (Sequence) o;

			isOneQuiet = true;
			quietIntervalOne = increment;

		}
	}

	// @SuppressWarnings("unchecked")

	public void processQuiescence2(Object o) {

		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		if (o instanceof Sequence) {
			Sequence increment = (Sequence) o;
			Generator generator = Generator.getGenerator();

			// To transmit the whole story stream (i.e. plot + missing rules) rather than just the missing rules.
			if (isItFullStory) {

				int plotSize = plotReceivedSoFar.getElements().size();

				if (plotSize != 0) {
					Entity mostRecentPlot = plotReceivedSoFar.getElement(plotSize - 1);
					String mostRecentPlotString = generator.generate(mostRecentPlot);
					if (!storyToldSoFar.containsDeprecated(mostRecentPlot)) {
						storyToldSoFar.addElement(mostRecentPlot);
						if (mostRecentPlotString != null) {
							// if we're transmitting all of story and inferences
							if (!isItOnlyRelevant) {
								// Mark.say("PLOT ELEMENT: ", mostRecentPlotString);
								BetterSignal signal = new BetterSignal("Story teller", mostRecentPlotString);
								Connections.getPorts(this).transmit(signal);

							}

							// if we want only the relevant rules
							else {
								Mark.say("PPPP adding to preRelevantStoryStream: ", mostRecentPlotString);
								preRelevantStoryStream.addElement(mostRecentPlot);

							}
						}
					}
				}
			}

			isTwoQuiet = true;
			quietIntervalTwo = increment;

			if (isOneQuiet && isTwoQuiet) {

				StandardMatcher matcher = StandardMatcher.getBasicMatcher();

				for (Entity e : quietIntervalOne.getElements()) {

					int matchCount = 0;

					for (Entity f : quietIntervalTwo.getElements()) {

						// If there is a match:
						if (matcher.match(e, f) != null) {
							matchCount++;
							break;
						}
					}

					/* If e from sequenceOne wasn't found in sequenceTwo */
					if (matchCount == 0) {
						if (unmatchedList.contains(e)) {
							continue;
						}

						else {
							unmatchedList.add(e);
						}

						int listSize = unmatchedList.size();

						if (listSize != 0) {

							Entity mostRecentMiss = unmatchedList.get((listSize - 1));
							Generator generate = Generator.getGenerator();

							// SPOON FEEDING
							if (GenesisMenus.getSpoonFeedButton().isSelected() && !isItMixture) {
								// Mark.say("Now in spoon feeding");
								// If we only want the rules relevant to reflections
								if (isItOnlyRelevant) {
									if (!mostRecentMiss.isA(Markers.PREDICTION_RULE) && !mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										missingRulesToCompareToReflectionRules.addElement(mostRecentMiss);
										preRelevantStoryStream.addElement(mostRecentMiss);
									}
								}
								else {
									// We want to provide only the surface facts, so check that it is not a prediction
									// or explanation.
									if (!mostRecentMiss.isA(Markers.PREDICTION_RULE) && !mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										String mostRecentGenerated = generate.generate(mostRecentMiss);
										String finalString = "*** " + mostRecentGenerated + " ***";
										if (mostRecentGenerated != null) {
											BetterSignal signal = new BetterSignal("Story teller", finalString);
											Connections.getPorts(this).transmit(signal);

										}
									}
								}
							}

							// PRIMING
							else if (GenesisMenus.getPrimingButton().isSelected()) {

								// If we only want to fill in the rules relevant to reflections
								if (isItOnlyRelevant) {
									if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										missingRulesToCompareToReflectionRules.addElement(mostRecentMiss);
										preRelevantStoryStream.addElement(mostRecentMiss);
									}
								}
								// If we want to fill in all rules
								else {
									if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										Mark.say("Second perspective is missing the following: ");
										String mostRecentGenerated = generate.generate(mostRecentMiss);
										String finalString = "*** " + mostRecentGenerated + " ***";

										if (mostRecentGenerated != null) {
											BetterSignal signal = new BetterSignal("Story teller", finalString);
											Connections.getPorts(this).transmit(signal);
											Mark.say(mostRecentGenerated);
										}
									}
								}
							}

							// PRIMING WITH INTROSPECTION
							else if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {

								StandardMatcher match = StandardMatcher.getBasicMatcher();

								if (isItOnlyRelevant) {
									if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										missingRulesToCompareToReflectionRules.addElement(mostRecentMiss);
										preRelevantStoryStream.addElement(mostRecentMiss);
									}
								}

								else {
									if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
										String mostRecentGenerated = generator.generate(mostRecentMiss);
										String finalString = "*** " + mostRecentGenerated + " ***";
										BetterSignal signal = new BetterSignal("Story teller", finalString);
										Connections.getPorts(this).transmit(signal);

										Mark.say("Second perspective is missing the following: ");
										Mark.say(mostRecentGenerated);

										// Look through all uninstantiated rules the system has
										for (Entity rule : rules.getElements()) {
											// To see which one the missing instantiated rule matches
											if (match.matchRuleToInstantiation(rule, mostRecentMiss) != null) {
												if (!rulesAlreadyReported.containsDeprecated(rule)) {
													rulesAlreadyReported.addElement(rule);
													String result = generator.generate(rule);
													String finalResult = "*** " + result + " ***";
													if (result != null) {
														Mark.say("GENERAL RULE: ");
														Mark.say(result);
														BetterSignal signalTwo = new BetterSignal("Story teller", finalResult);
														Connections.getPorts(this).transmit(signalTwo);
														Mark.say("ABOUT TO TRANSMIT NEW RULE ON TEACH-RULE-PORT");
														Connections.getPorts(this).transmit(TEACH_RULE_PORT, rule);
														Connections.getPorts(this).transmit(NEW_RULE_MESSENGER_PORT, true);

													}

												}
												break;
											}

										}
									}
								}
							}

							// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Story teller");
						}
					}
				}

				isOneQuiet = false;
				isTwoQuiet = false;

			}
		}

	}

	public void processSignal1(Object signal) {

		if (signal instanceof Entity) {
			Generator generator = Generator.getGenerator();
			Entity e = (Entity) signal;

			if (e.relationP() && e.getSubject().entityP("you")) {
				if (e.getObject().functionP(Markers.STORY_MARKER)) {
					storyHasStarted = true;
					Mark.say("stroy has started and storyHasStarted set to ", storyHasStarted);
				}
			}

			if (storyHasStarted) {
				if (!e.functionP()) {
					String element = generator.generate(e);
					Mark.say(element);
				}

			}

			Connections.getPorts(this).transmit(e);
			Connections.getPorts(this).transmit(MY_OUTPUT_PORT, e);
		}
	}

	public void processSignal2(Object signal) {

		if (signal instanceof Entity) {
			Generator generator = Generator.getGenerator();
			Entity e = (Entity) signal;
			String element = generator.generate(e);
			Mark.say(element);
			Connections.getPorts(this).transmit(e);
			Connections.getPorts(this).transmit(MY_OUTPUT_PORT, e);
		}
	}

}
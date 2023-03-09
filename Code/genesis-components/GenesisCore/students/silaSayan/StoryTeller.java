package silaSayan;

import generator.Generator;
import genesis.GenesisMenus;
import gui.TabbedTextViewer;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;
import java.util.regex.Matcher;

import javax.swing.JCheckBox;

import matchers.StandardMatcher;
import storyProcessor.*;
import utils.Html;
import utils.minilisp.LList;
import translator.BasicTranslator;
import utils.*;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Sequence;

public class StoryTeller extends AbstractWiredBox {

	// Other switches, may migrate to gui, currently in legacy status
	JCheckBox conceptSummaryButton = new JCheckBox("Concept summary", true);

	JCheckBox generalSummaryButton = new JCheckBox("General summary", true);

	// SWITCHES
	@SuppressWarnings("unused")
	private boolean isItFullStory = true; // indicates telling of the whole
	                                      // story stream as it would be told

	@SuppressWarnings("unused")
	private boolean debug = false;

	// PORTS

	public static final String QUIESCENCE_PORT1 = "quiescence port 1";

	public static final String QUIESCENCE_PORT2 = "quiescence port 2";

	public static final String RULE_PORT = "rules";

	public static final String CONCEPT_PORT1 = "concept port 1";

	public static final String CONCEPT_PORT2 = "concept port 2";

	public static final String TEACHER_INFERENCES = "teacher inferences";

	public static final String STUDENT_INFERENCES = "inferences";

	public static final String INCREMENT = "increment";

	public static final String STAGE_DIRECTION_PORT = "stage direction port";

	public static String TEACH_RULE_PORT = "teach rule port";

	public static String NEW_RULE_MESSENGER_PORT = "new rule messenger port";

	public static String INSTANTIATED_CONCEPTS = "instantiated concepts";

	public static String COMPLETE_STORY = "complete story";

	public static String CONCEPT_ANALYSIS = "concept analysis";

	public static String PLOT_PORT = "plot of story";

	public static String EXPLICIT_STORY = "explicit story";

	public static String CLEAR = "clear";

	public static String FROM_SUMMARY_HELPER = "from summary helper";

	// FIELDS

	// March 2013. Playing around with user supplied goals.
	private String[][] userGoalArray = { { "CONCEPT_CENTRIC", "revenge" }, { "CHARACTER_CENTRIC", "favor Macbeth" } };

	// private String[][] userGoalArray = {{"CONCEPT_CENTRIC","insanity"},{"CHARACTER_CENTRIC","disfavor Macbeth"}};

	@SuppressWarnings("unused")
	private String[] intendedConcepts;

	private String focalCharacter; // character whom goal indicates

	private String intendedDisposition; // favor or disfavor.

	private boolean gotWholeStory = false;

	@SuppressWarnings("unused")
	private boolean narrationDone = false;

	private ArrayList<Entity> unmatchedList = new ArrayList<Entity>(); // store
	                                                                   // already
	                                                                   // unmatched
	                                                                   // components
	                                                                   // to avoid
	                                                                   // printing
	                                                                   // them
	                                                                   // over and
	                                                                   // over

	private boolean isOneQuiet = false; // indicates perspective one reaching
	                                    // quiescence

	private boolean isTwoQuiet = false; // indicates perspective two reaching
	                                    // quiescence

	private Sequence quietIntervalOne = new Sequence(); // what perspective one
	                                                    // has accumulated since
	                                                    // last quiescence

	private Sequence quietIntervalTwo = new Sequence(); // what perspective one
	                                                    // has accumulated since
	                                                    // last quiescence

	private Sequence studentPerspectiveUnderstanding = new Sequence();

	private Sequence studentInferences = new Sequence();

	private Sequence teacherInferences = new Sequence();

	private Sequence rules = new Sequence(); // stores uninstantiated rules

	private Sequence rulesAlreadyReported = new Sequence(); // stores
	                                                        // uninstantiated
	                                                        // rules already
	                                                        // reported as being
	                                                        // related to
	                                                        // missing
	                                                        // instantiated
	                                                        // rules

	private Sequence conceptRules = new Sequence(); // stores all rules used
	                                                // in concepts

	private Sequence relevantRules = new Sequence(); // stores all rules used in
	                                                 // concepts that are
	                                                 // missing in second
	                                                 // perspective

	private Sequence missingRulesToCompareToConceptRules = new Sequence(); // stores
	                                                                       // all
	                                                                       // missing
	                                                                       // rules

	private Sequence completeStory = new Sequence();

	private Set<Object> completeStorySet = new HashSet<Object>();

	private Sequence storySummarized = new Sequence();

	private ArrayList<Entity> conceptParts = new ArrayList<Entity>();

	private Sequence storyConcepts = new Sequence();

	private ConceptAnalysis conceptAnalysis;

	private Sequence explicitStory = new Sequence();

	private ArrayList<String> storyPlotStringRep = new ArrayList<String>();

	private LinkedList<Entry<Entity, Integer>> finalSummary = new LinkedList<Map.Entry<Entity, Integer>>();

	public static String conceptBeingSummarized;

	private LinkedList<ArrayList<Object>> storyAndComprehensionCompilation = new LinkedList<ArrayList<Object>>();

	private boolean recordToCompilation = false;

	private boolean startIncrement = false;

	Sequence inferencesInSummary = new Sequence();

	// private Map<String, Integer> orderingMap = new HashMap<String, Integer>();
	private Map<String, ArrayList<Object>> orderingMap = new HashMap<String, ArrayList<Object>>();

	int orderCount = 0;

	// private ArrayList<ArrayList<Object>> orderingList = new ArrayList<ArrayList<Object>>();

	public StoryTeller() {
		super("My story processor");

		// // ==> not implemented

		// These connections used to read story and do perspective comparison.
		Connections.getPorts(this).addSignalProcessor(QUIESCENCE_PORT1, this::processQuiescence1);
		Connections.getPorts(this).addSignalProcessor(QUIESCENCE_PORT2, this::processQuiescence2);

		// This connection used to figure out base rules for the Priming with
		// Introspection level.
		Connections.getPorts(this).addSignalProcessor(RULE_PORT, this::processRules);

		// This connection used to figure out what rules are crucial for
		// concepts.
		Connections.getPorts(this).addSignalProcessor(CONCEPT_PORT1, this::processConceptsForRules1);

		// // Connections.getPorts(this).addSignalProcessor(INSTANTIATED_CONCEPTS, this::storeConcepts);

		Connections.getPorts(this).addSignalProcessor(COMPLETE_STORY, this::setCompleteStory);

		// // Connections.getPorts(this).addSignalProcessor(CONCEPT_ANALYSIS, this::processConceptAnalysis);

		// // Connections.getPorts(this).addSignalProcessor(PLOT_PORT, this::setPlot);

		Connections.getPorts(this).addSignalProcessor(EXPLICIT_STORY, this::setExplicitStory);

		Connections.getPorts(this).addSignalProcessor(STUDENT_INFERENCES, this::processStudentInferences);
		Connections.getPorts(this).addSignalProcessor(TEACHER_INFERENCES, this::processTeacherInferences);

		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION_PORT, this::processStageDirections);
		// // Connections.getPorts(this).addSignalProcessor(FROM_SUMMARY_HELPER, this::processSummaryHelper);

	}

	@SuppressWarnings("unused")
	public void goalStrategyPicker(String[][] userGoalArray) {
		Mark.say("IN NEW FUNCTION GOAL STRATEGY PICKER!!!!!!!!!!!!!");

		for (String[] goalTypeTuple : userGoalArray) {
			String userGoalType = goalTypeTuple[0];
			Mark.say("userGoalType: ", userGoalType);
			String userGoal = goalTypeTuple[1];
			Mark.say("userGoal: ", userGoal);

			if (userGoalType == "CONGRUENCE") {
				return; // Everything proceeds as before. Nothing special done yet.
			}
			else if (userGoalType == "CHARACTER_CENTRIC") {
				String[] goalParts = userGoal.split("\\s");

				intendedDisposition = goalParts[0];
				Mark.say("intended Disposition: ", intendedDisposition, "goalParts[0]: ", goalParts[0]);
				focalCharacter = goalParts[1];
				Mark.say("focalCharacter: ", focalCharacter, "goalParts[1]: ", goalParts[1]);
			}
			else if (userGoalType == "CONCEPT_CENTRIC") {
				Mark.say("   PROFVIDED GOAL CONCEPT CENTRIC");
				Mark.say("UserGoal is: ", userGoal);
				String[] goalParts = userGoal.split(",");
				int i = 0;
				String primingMessage = "This is a story about";
				while (i < goalParts.length) {
					// Mark.say("part of goal: ", goalParts[i]);
					primingMessage += " " + goalParts[i];
					i++;
				}
				primingMessage += ". ";
				String primingMessageFormatted = Html.green(primingMessage);
				Mark.say(primingMessage);
				BetterSignal signal = new BetterSignal("Story teller", primingMessageFormatted);
				Connections.getPorts(this).transmit(signal);
			}
			else if (userGoalType == "MORALISTIC") {
				String[] goalParts = userGoal.split("\\s");
			}
		}

	}

	public void processSummaryHelper(BetterSignal o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		Mark.say("PROCESSING SUMMARY HELPER");
		if (o instanceof BetterSignal) {
			Mark.say("Got BetterSignal");
			BetterSignal signal = (BetterSignal) o;
			Connections.getPorts(this).transmit(signal);
		}
	}

	public void conceptSummary(ConceptAnalysis conceptAnalysis, boolean ready) throws Exception {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		if (!ready) {
			return;
		}
		Sequence conceptSummary = new Sequence();
		ConceptDescription description = conceptAnalysis.getConceptDescriptions().get(0);
		// for (ConceptDescription d: conceptAnalysis.getConceptDescriptions()){
		conceptBeingSummarized = description.getName();
		String intro = "From the perspective of " + conceptBeingSummarized + ": ";
		BetterSignal signal = new BetterSignal(conceptBeingSummarized, intro);
		Connections.getPorts(this).transmit(signal);
		// conceptBeingSummarized = d.getName();
		conceptSummary = conceptSummaryWrapper(description, completeStory);
		// conceptSummary = conceptSummaryWrapper(d,completeStory);
		Sequence conceptSummaryPart = divideSummary(conceptSummary);
		if (GenesisMenus.getSpoonFeedButton().isSelected()) {
			Mark.say("CALLED SPOONFEED SUMMARY");
			spoonfeedSummary(conceptSummaryPart);
		}
		if (GenesisMenus.getPrimingButton().isSelected()) {
			justifySummary(conceptSummaryPart);
		}
		if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {
			teachSummary(conceptSummaryPart);
		}
		// }

	}

	public Sequence conceptSummaryWrapper(ConceptDescription concept, Sequence searchSpace) {
		if (!Radio.tellStoryButton.isSelected()) {
			return null;
		}
		if (!gotWholeStory) {
			Mark.say("NULLLL!!!:  WASN'T READY YET");
			return null;
		}
		SummaryHelper SummaryHelper = new SummaryHelper();

		// Map<Thing, ArrayList<Thing>> rootMap = SummaryHelper.rootMapper(searchSpace);
		Sequence miniSummary = SummaryHelper.filterSummary(concept);
		// Sequence fullConceptSummary = SummaryHelper.populateConceptSummary(miniSummary, rootMap);
		// return fullConceptSummary;
		return miniSummary;
	}

	public void clearMemories() {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("SILAAAAAAAA :   I'M CLEARING YOUR MEMORIES NOWWWWWE!!!");
		gotWholeStory = false;
		narrationDone = false;
		unmatchedList.clear();

		isOneQuiet = false; // indicates perspective one reaching
		                    // quiescence

		isTwoQuiet = false; // indicates perspective two reaching
		                    // quiescence

		quietIntervalOne.clearElements();
		quietIntervalTwo.clearElements();

		studentPerspectiveUnderstanding.clearElements();

		studentInferences.clearElements();
		teacherInferences.clearElements();

		rules.clearElements();

		rulesAlreadyReported.clearElements();
		conceptRules.clearElements();
		relevantRules.clearElements();
		missingRulesToCompareToConceptRules.clearElements();
		completeStory.clearElements();
		completeStorySet.clear();
		storySummarized.clearElements();
		conceptParts.clear();
		storyConcepts.clearElements();
		explicitStory.clearElements();
		storyPlotStringRep.clear();
		finalSummary.clear();
		recordToCompilation = false;
		storyAndComprehensionCompilation.clear();
		startIncrement = false;

		orderingMap.clear();
		orderCount = 0;

		inferencesInSummary.clearElements();
		Connections.getPorts(this).transmit(CLEAR, "clear");
	}

	public void processStageDirections(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("Entering processStageDirections with", o);
		if (!(o instanceof String)) {
			return;
		}
		String s = (String) o;
		if (s == Markers.RESET) {
			Mark.say("Clearing memories now!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			clearMemories();
		}
	}

	public void setExplicitStory(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("Trying to setExplicitStory");

		if (o instanceof Sequence) {
			explicitStory = (Sequence) o;
		}
		// Mark.say("RECORDED: ");
		// for(ArrayList<Object> recordedElements:storyAndComprehensionCompilation){
		// Mark.say("Element: ", ((Thing) recordedElements.get(0)).asString());
		// Mark.say("TYPE: ", recordedElements.get(1));
		// }
	}

	@SuppressWarnings("unused")
	public void processStudentInferences(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("GOT STUDENT INFERENCES!");
		if (o instanceof Sequence) {
			studentInferences = (Sequence) o;
			if (studentInferences.getElements().isEmpty()) {
				// Mark.say("STUDENT INFERENCES EMPTY!");
			}
			for (Entity t : studentInferences.getElements()) {
				// Mark.say("ST INFERENCE: ", t.asString());
			}
		}
	}

	@SuppressWarnings("unused")
	public void processTeacherInferences(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("GOT TEACHER INFERENCES!");
		if (o instanceof Sequence) {
			teacherInferences = (Sequence) o;
			if (teacherInferences.getElements().isEmpty()) {
				// Mark.say("TEACHER INFERENCES EMPTY!");
			}
			for (Entity t : teacherInferences.getElements()) {
				// Mark.say("TC INFERENCE: ", t.asString());
			}
		}
	}

	/*** Stores the plot of the story in String format. ***/
	public void setPlot(Object sig) throws Exception {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		if (sig instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) sig;

			String plotString = signal.get(1, String.class);
			storyPlotStringRep.add(plotString);
		}

	}

	/*** Stores the whole story (explicit and implicit included). ***/
	public void setCompleteStory(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Mark.say("IN SETCOMPLETESTORY");
		if (o instanceof Sequence) {
			completeStory = (Sequence) o;
			// Mark.say("STORY: ");
			for (Entity t : completeStory.getAllComponents()) {
				completeStorySet.add(t);
				// Mark.say(t.asString());
			}
			gotWholeStory = true;
			// goalStrategyPicker(userGoalType,userGoal);
		}
	}

	public void orderSummary2(List<SortableThing> unordered) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Collections.sort(unordered, new OrderingComparator());
	}

	class OrderingComparator implements Comparator<SortableThing> {

		@Override
		public int compare(SortableThing o1, SortableThing o2) {
			if (o1.index < o2.index) {
				return -1;
			}
			else if (o1.index > o2.index) {
				return +1;
			}
			return 0;
		}

	}

	class SortableThing {
		int index;

		Entity thing;

		String type;

		public SortableThing(int index, Entity thing, String type) {
			this.index = index;
			this.thing = thing;
			this.type = type;
		}
	}

	@SuppressWarnings("unused")
	public void orderSummary(Sequence unordered) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Sequence resultingSummary = new Sequence();
		LinkedList<ArrayList<Object>> elementTypeList = new LinkedList<ArrayList<Object>>();
		Map<Integer, ArrayList<Object>> mapToOrder = new HashMap<Integer, ArrayList<Object>>();
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Mark.say("orderingMap size: ", orderingMap.size());
		Mark.say("Unordered summary elements: ");
		for (String t : orderingMap.keySet()) {
			Mark.say("KEY: ", t);
			Mark.say("VALUE: ", orderingMap.get(t));
		}
		for (Entity t : unordered.getAllComponents()) {
			// Mark.say("   ", t.asString());
			String hashedElement = t.hash();
			Mark.say("HASH: ", hashedElement);
			if (orderingMap.containsKey(hashedElement)) {
				int orderNumber = (Integer) orderingMap.get(hashedElement).get(0);
				ArrayList<Object> elementTypeTuple = new ArrayList<Object>();
				String type = (String) orderingMap.get(hashedElement).get(1);
				elementTypeTuple.add(0, t);
				elementTypeTuple.add(1, type);
				Mark.say("FOUND IT!!! ", orderNumber);
				mapToOrder.put(orderNumber, elementTypeTuple);
			}

		}
		Mark.say("mapToOrder size: ", mapToOrder.size());
		TreeSet<Integer> keySet = new TreeSet<Integer>(mapToOrder.keySet());
		Mark.say("keySet size: ", keySet.size());
		for (Integer key : keySet) {
			ArrayList<Object> elementAndType = mapToOrder.get(key);
			elementTypeList.add(elementAndType);
			// Thing seqToAdd = mapToOrder.get(key);
			// Mark.say(key, " : ", seqToAdd.asString());
			// resultingSummary.addElement(seqToAdd);
		}
		makeSummaryPretty(elementTypeList);
		// Mark.say("ORDERED SUMMARY : ");
		// for (Thing h: resultingSummary.getAllComponents()){
		// Mark.say(h.asString());
		// String str = Generator.getGenerator().generate(h);
		// BetterSignal signal = new BetterSignal("NewSummary",str);
		// Connections.getPorts(this).transmit(signal);
		// }

	}

	/***
	 * Puts argument into appropriate chronological order derived from the story.
	 ***/
	public void putSummaryIntoOrder(ArrayList<ArrayList<Object>> unordered) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		SummaryHelper SummaryHelper = new SummaryHelper();

		if (unordered.isEmpty()) {
			Mark.say("Map is empty!");
			return;
		}

		ArrayList<ArrayList<Object>> map = new ArrayList<ArrayList<Object>>();

		for (ArrayList<Object> l : unordered) {
			Entity key = (Entity) l.get(0);
			int value = (Integer) l.get(1);

			if (completeStory.containsDeprecated(key) && value != 2) {
				int index = key.getID();
				ArrayList<Object> m = new ArrayList<Object>();
				m.add(0, (double) index);
				m.add(1, key);
				m.add(2, value);
				map.add(m);
			}
			else if (value == 2) {

				int preindex = key.getID();

				double index = (double) preindex;
				index = index + 0.1;
				// index = index - 0.9;

				ArrayList<Object> m = new ArrayList<Object>();
				m.add(0, (double) index);
				m.add(1, key);
				m.add(2, value);
				map.add(m);
			}
		}

		LinkedList<ArrayList<Object>> sortedSummary = SummaryHelper.summarySorter(map);
		makeSummaryPrettyTeach(sortedSummary);

	}

	public void putSummaryIntoOrder(Map<Entity, Integer> unordered) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("PUTTING SUMMARY INTO ORDER!");
		if (unordered.getClass() == Map.class) {
			if (unordered.isEmpty()) {
				Mark.say("Map is empty!");
				return;
			}
		}
		HashMap<Entity, Integer> unorderedSummary = (HashMap<Entity, Integer>) unordered;
		Map<Double, Map.Entry<Entity, Integer>> map = new TreeMap<Double, Map.Entry<Entity, Integer>>();
		Iterator<Map.Entry<Entity, Integer>> it = unorderedSummary.entrySet().iterator();
		// Mark.say("ORDER ORDER ORDER: ");

		while (it.hasNext()) {
			Map.Entry<Entity, Integer> pair = (Map.Entry<Entity, Integer>) it.next();
			Entity key = pair.getKey();
			if (completeStory.containsDeprecated(key) && unorderedSummary.get(key) != 2) {
				int index = key.getID();
				map.put((double) index, pair);
				Mark.say(index, " : ", pair.getKey().asString());

			}
			else if (unorderedSummary.get(key) == 2) {
				int preindex = key.getID();
				double index = (double) preindex;
				index = index - 0.9;
				map.put(index, pair);
				// Mark.say(index, " : ", pair.getKey().asString());
			}
		}

		TreeSet<Double> keySet = new TreeSet<Double>(map.keySet());
		Mark.say("Ordered: ");
		for (Double key : keySet) {
			Map.Entry<Entity, Integer> seqToAdd = map.get(key);
			Mark.say(key, " : ", seqToAdd.getKey().asString());
			finalSummary.add(seqToAdd);
		}
		if (finalSummary.isEmpty()) {
			Mark.say("FINAL SUMMARY EMPTY!");
		}
		// makeSummaryPretty(finalSummary);
	}

	public void makeSummaryPrettyTeach(LinkedList<ArrayList<Object>> summaryList) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In makeSummaryPrettyTeach");
		Generator generator = Generator.getGenerator();
		for (ArrayList<Object> entry : summaryList) {
			if ((Integer) entry.get(1) == 0) {
				Entity summaryElement = (Entity) entry.get(0);

				String finalString = generator.generate(summaryElement);

				if (generalSummaryButton.isSelected()) {

					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(TabbedTextViewer.TAB, signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(StoryTeller.conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(TabbedTextViewer.TAB, signal);

				}
			}
			else if ((Integer) entry.get(1) == 1) {
				Entity summaryElement = (Entity) entry.get(0);
				String str;
				if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {
					str = generator.generateXPeriod(summaryElement);
				}
				else {
					str = generator.generate(summaryElement) + " ";
				}

				String finalString = Html.red(str);

				if (generalSummaryButton.isSelected()) {

					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(StoryTeller.conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(signal);
				}

			}
			else if ((Integer) entry.get(1) == 2) {
				Entity summaryElement = (Entity) entry.get(0);

				String str = " because " + generator.generateAsIf(summaryElement.getElement(0)) + " ";
				String finalString = Html.blue(str);

				if (generalSummaryButton.isSelected()) {

					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(StoryTeller.conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(signal);
				}
			}
		}
	}

	public void makeSummaryPretty(LinkedList<ArrayList<Object>> summaryList) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("MKAING SUMMARY PRETTY!");
		Generator generator = Generator.getGenerator();
		for (ArrayList<Object> entry : summaryList) {
			if (entry.get(1) == "REGULAR") {
				Entity summaryElement = (Entity) entry.get(0);
				String finalString = generator.generate(summaryElement);
				// String finalString = summaryElement;
				if (generalSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(signal);

				}
			}
			else if (entry.get(1) == "MISSING") {
				Entity summaryElement = (Entity) entry.get(0);
				String str;
				if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {
					str = generator.generateXPeriod(summaryElement);
				}
				else {
					str = generator.generate(summaryElement) + " ";
				}

				String finalString = Html.red(str);
				// String finalString = Html.red(summaryElement);

				if (generalSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(signal);
				}

			}
			else if (entry.get(1) == "RULE") {
				Entity summaryElement = (Entity) entry.get(0);
				String str = " because " + generator.generateAsIf(summaryElement) + " ";
				String finalString = Html.blue(str);
				if (generalSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal("Summary", finalString);
					Connections.getPorts(this).transmit(signal);
				}
				else if (conceptSummaryButton.isSelected()) {
					BetterSignal signal = new BetterSignal(conceptBeingSummarized, finalString);
					Connections.getPorts(this).transmit(signal);
				}
			}
		}

	}

	/***
	 * Method stores explicit and implicit story elements relevant to higher level concepts
	 * 
	 * @throws Exception
	 ***/
	@SuppressWarnings("unused")
	public void processConceptAnalysis(Object o) throws Exception {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("Processing ConceptAnalysis!");
		Generator generator = Generator.getGenerator();
		if (o instanceof ConceptAnalysis) {
			conceptAnalysis = (ConceptAnalysis) o;
			for (ConceptDescription d : conceptAnalysis.getConceptDescriptions()) {
				for (Entity t : d.getStoryElementsInvolved().getElements()) {
					// Mark.say("Story elements in Concept Description: ", generator.generate(t));
					if (!storyConcepts.containsDeprecated(t)) {
						storyConcepts.addElement(t);
					}
				}
			}

			if (conceptSummaryButton.isSelected()) {
				conceptSummary(conceptAnalysis, true);
			}
			if (!generalSummaryButton.isSelected()) {
				return;
			}

			else {
				if (storyConcepts.getAllComponents().isEmpty()) {
					Mark.say("EMPTY: StoryConcepts.");
					return;
				}
				Mark.say("Going to call DivideSummary.");
				Sequence summaryPart = divideSummary(storyConcepts);
				Mark.say("Back and about to call appropriate SUMMARY,");
				if (GenesisMenus.getSpoonFeedButton().isSelected()) {
					Mark.say("CALLED SPOONFEED SUMMARY");
					spoonfeedSummary(summaryPart);
				}
				if (GenesisMenus.getPrimingButton().isSelected()) {
					justifySummary(summaryPart);
				}
				if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {
					teachSummary(summaryPart);
				}

			}
		}
	}

	@SuppressWarnings("unused")
	public Sequence divideSummary(Sequence wholeSummary) {
		if (!Radio.tellStoryButton.isSelected()) {
			return wholeSummary;
		}

		Mark.say("In DivideSummary.");
		Sequence summaryToSort = wholeSummary;
		Sequence storyCopy = explicitStory;
		Sequence inferencesSummarized = new Sequence();
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		if (explicitStory.getAllComponents().isEmpty()) {
			Mark.say("EMPTY! explicit story");
		}
		for (Entity f : explicitStory.getAllComponents()) {
			// Mark.say("Explicit story: ", f.asString());
		}

		for (Entity t : summaryToSort.getAllComponents()) {
			// Mark.say("Summary elt to group as STORY: ", t.asString());
			for (Entity g : storyCopy.getAllComponents()) {
				LList<PairOfEntities> match = matcher.matchAnyPart(t, g);
				boolean usefulMatch = false;

				if (match != null) {
					usefulMatch = usefulPartialMatch(match);

					if (usefulMatch && !storySummarized.containsDeprecated(t)) {
						storySummarized.addElement(t);
						storyCopy.removeElement(g);
						summaryToSort.removeElement(t);
						usefulMatch = false;
						// Mark.say("ADDED to storySummarized!: ",
						// t.asString());
					}
				}
			}
		}
		for (Entity m : summaryToSort.getAllComponents()) {
			inferencesSummarized.addElement(m);
			// Mark.say("ADDED to inferencesSummarized!: ", m.asString());
		}
		if (inferencesSummarized.getElements().isEmpty()) {
			Mark.say("EMPTY: inference part of summary.");
		}

		return inferencesSummarized;
	}

	public void spoonfeedSummary(Sequence summaryPart) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In spoonfeed summary!");
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();

		Map<Entity, Integer> toIncludeInSummary = new HashMap<Entity, Integer>();

		for (Entity t : summaryPart.getElements()) {
			for (Entity f : studentInferences.getAllComponents()) {
				if (matcher.matchAnyPart(t, f) != null) {
					break;
				}
				else {
					if (!t.isA(Markers.PREDICTION_RULE) && !t.isA(Markers.EXPLANATION_RULE)) {
						inferencesInSummary.addElement(t);
						toIncludeInSummary.put(t, 1);
					}
				}
			}
		}
		if (toIncludeInSummary.isEmpty()) {
			Mark.say("SUMMARY OF IFERENCES EMPTY!");
		}
		consolidateSummary(toIncludeInSummary);
	}

	public void justifySummary(Sequence summaryPart) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In justified summary!");
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Map<Entity, Integer> toIncludeInSummary = new HashMap<Entity, Integer>();
		for (Entity t : summaryPart.getElements()) {
			// Mark.say("Summary elt to match to inferences: ", t.asString());
			for (Entity f : studentInferences.getAllComponents()) {
				LList<PairOfEntities> match = matcher.matchAnyPart(t, f);
				if (match != null) {
					if (usefulPartialMatch(match)) {
						break;
					}

				}
				else {
					for (Entity h : completeStory.getAllComponents()) {
						if (h.isA(Markers.EXPLANATION_RULE) || h.isA(Markers.PREDICTION_RULE)) {
							LList<PairOfEntities> matchFull = matcher.matchAnyPart(t, h.getObject());
							if (matchFull != null) {
								if (usefulPartialMatch(matchFull)) {
									toIncludeInSummary.put(h, 1);
								}
							}
						}
					}
				}
			}
		}
		consolidateSummary(toIncludeInSummary);

	}

	public void teachSummary(Sequence summaryPart) throws Exception {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In teachSummary");
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Generator generator = Generator.getGenerator();
		ArrayList<ArrayList<Object>> toIncludeInSummary = new ArrayList<ArrayList<Object>>();
		Sequence associatedRules = new Sequence();
		for (Entity t : summaryPart.getElements()) {
			for (Entity f : studentInferences.getAllComponents()) {
				LList<PairOfEntities> match = matcher.matchAnyPart(t, f);
				if (match != null) {
					if (usefulPartialMatch(match)) {
						break;
					}
				}
				else {

					for (Entity h : completeStory.getAllComponents()) {
						if (h.isA(Markers.EXPLANATION_RULE) || h.isA(Markers.PREDICTION_RULE)) {
							LList<PairOfEntities> matchFull = matcher.matchAnyPart(t, h.getObject());
							if (matchFull != null) {
								if (usefulPartialMatch(matchFull)) {

									for (Entity rule : rules.getAllComponents()) {
										LList<PairOfEntities> matchRule = matcher.matchRuleToInstantiation(rule, h);
										if (matchRule != null) {
											associatedRules.addElement(rule);
										}
									}
									String compoundRule;
									if (associatedRules.getAllComponents().size() == 1) {
										compoundRule = generator.generate(associatedRules.getElement(0));
									}
									else {
										compoundRule = generator.generateXPeriod(associatedRules.getElement(0));
										associatedRules.removeElement(associatedRules.getElement(0));
										for (Entity matchingRule : associatedRules.getAllComponents()) {
											String addOn = " and " + generator.generateXPeriod(matchingRule);
											compoundRule = compoundRule + addOn;
										}
										compoundRule = compoundRule + ".";
									}
									BasicTranslator basicTranslator = BasicTranslator.getTranslator();
									Entity compoundRuleThing = basicTranslator.translate(compoundRule);
									if (compoundRuleThing == null) {
										Mark.say("COULDNT TRANSLATE!!!!!!");
										return;
									}

									ArrayList<Object> summaryTuple = new ArrayList<Object>();
									summaryTuple.add(0, t);
									summaryTuple.add(1, 1);
									toIncludeInSummary.add(summaryTuple);
									Entity newRule = new Entity();
									newRule = compoundRuleThing;
									String associatedName = (h.getNameSuffix()).substring(1);
									int associatedID = Integer.parseInt(associatedName);

									int ruleID = associatedID + 1;

									String ruleName = Integer.toString(ruleID);

									String ruleNameFinal = "-" + ruleName;
									newRule.setNameSuffix(ruleNameFinal);

									ArrayList<Object> summaryTupleForRules = new ArrayList<Object>();
									summaryTupleForRules.add(0, newRule);
									summaryTupleForRules.add(1, 2);
									toIncludeInSummary.add(summaryTupleForRules);
									break;

								}
							}
						}
					}
				}
			}
		}
		// Mark.say("TO INCLUDE IN SUMMARY: ");
		// for (ArrayList<Object> b: toIncludeInSummary){
		// Mark.say(((Thing)b.get(0)).asString());
		// }
		consolidateSummary(toIncludeInSummary);
	}

	public void consolidateSummary(ArrayList<ArrayList<Object>> inferenceParts) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In consolidateSummary!");
		// Map<Thing, Integer> fullEditedSummary = new HashMap<Thing, Integer>();
		ArrayList<ArrayList<Object>> fullEditedSummary = new ArrayList<ArrayList<Object>>();
		for (Entity t : storySummarized.getAllComponents()) {
			ArrayList<Object> l = new ArrayList<Object>();
			l.add(0, t);
			l.add(1, 0);
			fullEditedSummary.add(l);
		}
		for (ArrayList<Object> l : inferenceParts) {
			fullEditedSummary.add(l);
		}
		// fullEditedSummary.putAll(inferenceParts);
		putSummaryIntoOrder(fullEditedSummary);
	}

	@SuppressWarnings("unused")
	public void consolidateSummary(Map<Entity, Integer> inferenceParts) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Mark.say("In consolidateSummary!");
		Sequence summaryBeforeOrdering = new Sequence();
		Map<Entity, Integer> fullEditedSummary = new HashMap<Entity, Integer>();
		for (Entity t : storySummarized.getAllComponents()) {
			summaryBeforeOrdering.addElement(t);
			// fullEditedSummary.put(t, 0);
		}
		for (Entity m : inferencesInSummary.getAllComponents()) {
			summaryBeforeOrdering.addElement(m);
		}
		// fullEditedSummary.putAll(inferenceParts);
		orderSummary(summaryBeforeOrdering);
		// putSummaryIntoOrder(fullEditedSummary);

	}

	/***
	 * Extracts Reflective Knowledge Rules that were used in achieving higher concepts in the given story.
	 ***/
	public void processConceptsForRules1(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		if (o instanceof Sequence) {
			Sequence conceptAnalysis = (Sequence) o;
			for (Entity description : conceptAnalysis.getElements()) {
				conceptRules.addElement(description);

				for (Entity part : description.getElements()) {

					if (part instanceof Sequence) {
						Sequence seqPart = new Sequence();
						seqPart = (Sequence) part;
						for (Entity concept : seqPart.getElements()) {
							conceptParts.add(concept);
						}
					}
				}
			}
		}
	}

	/** Extracts UNINSTANTIATED RULES from signal */
	public void processRules(Object o) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		if (o instanceof Sequence) {
			rules = (Sequence) o;
		}
	}

	/**
	 * Signals quiescence of first perspective. Extracts buffer contents at quiescence.
	 */
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

	private void recordIntoCompilation(boolean on, Entity toRecord, String type) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		if (!on) {
			return;
		}
		ArrayList<Object> recording = new ArrayList<Object>();
		recording.add(0, toRecord);
		recording.add(1, type);
		storyAndComprehensionCompilation.add(recording);
	}

	@SuppressWarnings("unused")
	public void processQuiescence2(Object o) {
		// Check that story telling is enabled before proceeding.
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}

		for (Entity t : ((Entity) o).getAllComponents()) {
			// Mark.say(t.asString());
			if (t != null && t.relationP("start")) {
				// Mark.say(t.asString());
				Mark.say("WE GOT START!!!!!!!!!");
				goalStrategyPicker(userGoalArray);
				recordToCompilation = true;
				startIncrement = true;
			}
		}

		if (o instanceof Sequence) {
			Sequence increment = (Sequence) o;
			Generator generator = Generator.getGenerator();

			isTwoQuiet = true;
			quietIntervalTwo = increment;

			Entity mostRecentMiss;

			if (isOneQuiet && isTwoQuiet) {
				StandardMatcher matcher = StandardMatcher.getBasicMatcher();

				if (increment.sequenceP() && !increment.getAllComponents().isEmpty()) {
					for (Entity m : increment.getAllComponents()) {
						recordIntoCompilation(recordToCompilation, m, "REGULAR");
					}
				}

				if (startIncrement) {
					boolean foundCharacter = false; // this is for CHARACTER_CENTRIC GOALS
					// ArrayList<Object> elementOrderPair = new ArrayList<Object>();
					// elementOrderPair.add(0, increment);
					// elementOrderPair.add(1,orderCount);
					// orderingList.add(elementOrderPair);
					for (Entity t : increment.getAllComponents()) {
						Mark.say(t.asString(), " added to orderingMap at ", orderCount);
						String hashedIncrement = t.hash();
						ArrayList<Object> orderTypeTuple = new ArrayList<Object>();
						orderTypeTuple.add(0, orderCount);
						orderTypeTuple.add(1, "REGULAR");
						orderingMap.put(hashedIncrement, orderTypeTuple);
						orderCount++;
						// Checking for focal character of the goal.
						if (!foundCharacter && t.relationP("classification")) {
							String object = t.getObject().asString();
							String regPattern = "\\s" + focalCharacter + "-";
							Pattern pattern = Pattern.compile(regPattern, Pattern.CASE_INSENSITIVE);
							Matcher regexMatcher = pattern.matcher(object);
							while (regexMatcher.find()) {
								Mark.say("Found: ", regexMatcher.group());
								foundCharacter = true;
								String characterMsg = "";
								Pattern newPattern = Pattern.compile("dis");
								Matcher regMatcher = newPattern.matcher(intendedDisposition);
								if (regMatcher.find()) {
									characterMsg = focalCharacter + " is crazy. ";
								}
								else {
									characterMsg = focalCharacter + " is good. ";
								}
								// Mark.say("intendedDisposition: ", intendedDisposition);
								// if (intendedDisposition=="favor"){
								// Mark.say("FAVOR!");
								// characterMsg = focalCharacter + " is good. ";
								// }else if(intendedDisposition=="disfavor"){
								// Mark.say("DISFAVOR!");
								// characterMsg = focalCharacter + " is evil. ";
								// }
								Mark.say("CharacterMsg: ", characterMsg);
								String characterMsgGreen = Html.green(characterMsg);
								BetterSignal signal = new BetterSignal("Story teller", characterMsgGreen);
								Connections.getPorts(this).transmit(signal);
							}

						}
					}
				}

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
							mostRecentMiss = unmatchedList.get((listSize - 1));
							Generator generate = Generator.getGenerator();

							String mostRecentMissString = generate.generate(mostRecentMiss);

							// SPOON FEEDING
							if (GenesisMenus.getSpoonFeedButton().isSelected()) {
								// We want to provide only the surface facts, so
								// check that it is not a prediction or
								// explanation.
								if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
									if (mostRecentMiss.getObject() != null) {
										if (startIncrement) {
											for (Entity f : mostRecentMiss.getAllComponents()) {
												Mark.say(f.asString(), " added to orderingMap at ", orderCount);
												String hashedMiss = f.hash();
												ArrayList<Object> orderTypeTuple = new ArrayList<Object>();
												orderTypeTuple.add(0, orderCount);
												orderTypeTuple.add(1, "MISSING");
												orderingMap.put(hashedMiss, orderTypeTuple);
												orderCount++;
											}

										}
										recordIntoCompilation(recordToCompilation, mostRecentMiss.getObject(), "MISSING");
										String mostRecentGenerated = generate.generate(mostRecentMiss.getObject()) + " ";
										Mark.say("Most recent", mostRecentGenerated);
										String finalString = Html.red(mostRecentGenerated);
										if (mostRecentGenerated != null) {
											Mark.say("Final string", finalString);
											BetterSignal signal = new BetterSignal("Story teller", finalString);
											Connections.getPorts(this).transmit(signal);
										}
									}

								}
							}

							// PRIMING
							else if (GenesisMenus.getPrimingButton().isSelected()) {
								if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
									// Mark.say("Second perspective is missing the following: ");
									if (startIncrement) {
										for (Entity f : mostRecentMiss.getAllComponents()) {
											Mark.say(f.asString(), " added to orderingMap at ", orderCount);
											String hashedMiss = f.hash();
											ArrayList<Object> orderTypeTuple = new ArrayList<Object>();
											orderTypeTuple.add(0, orderCount);
											orderTypeTuple.add(1, "MISSING");
											orderingMap.put(hashedMiss, orderTypeTuple);
											orderCount++;
										}
									}
									recordIntoCompilation(recordToCompilation, mostRecentMiss, "MISSING");
									String mostRecentGenerated = generate.generate(mostRecentMiss) + "  ";
									String finalString = Html.red(mostRecentGenerated);

									if (mostRecentGenerated != null) {
										BetterSignal signal = new BetterSignal("Story teller", finalString);
										Connections.getPorts(this).transmit(signal);
										// Mark.say(mostRecentGenerated);
									}
								}
							}

							// PRIMING WITH INTROSPECTION
							else if (GenesisMenus.getPrimingWithIntrospectionButton().isSelected()) {

								StandardMatcher match = StandardMatcher.getBasicMatcher();
								if (mostRecentMiss.isA(Markers.PREDICTION_RULE) || mostRecentMiss.isA(Markers.EXPLANATION_RULE)) {
									if (startIncrement) {
										for (Entity f : mostRecentMiss.getAllComponents()) {
											Mark.say(f.asString(), " added to orderingMap at ", orderCount);
											String hashedMiss = f.hash();
											ArrayList<Object> orderTypeTuple = new ArrayList<Object>();
											orderTypeTuple.add(0, orderCount);
											orderTypeTuple.add(1, "MISSING");
											orderingMap.put(hashedMiss, orderTypeTuple);
											orderCount++;
										}
									}
									recordIntoCompilation(recordToCompilation, mostRecentMiss, "MISSING");
									// Mark.say("MISSING: ", mostRecentMiss.asString());
									// if (mostRecentMiss.getObject()!=null){
									// Mark.say("OBJECT: ", mostRecentMiss.getObject().asString());
									// }
									// if (mostRecentMiss.getSubject()!=null){
									// Mark.say("SUBJECT: ", mostRecentMiss.getSubject().asString());
									// for (Thing t: mostRecentMiss.getSubject().getAllComponents()){
									// // Mark.say("         : ", t.asString());
									// }
									// }

									// Look through all uninstantiated rules the
									// system has
									// Mark.say("Find rule for: ", mostRecentMiss.asString());
									String friendRule = "(r prediction (s conjuction (r enemy (t zz-516) (t xx-502)) (r enemy (t zz-516) (t yy-511))) (r friend (t yy-511) (t xx-502)))";
									for (Entity rule : rules.getElements()) {
										// if ((rule.asString()).equals(friendRule)){
										// Mark.say(" FRIEND RULE BEING CONSIDERED!!!!!!!!!!");
										// if (match.matchRuleToInstantiation(rule, mostRecentMiss)==null){
										// Mark.say("NO MATCH WITH : ", rule.asString());
										// }
										// }
										// To see which one the missing
										// instantiated rule matches
										if (match.matchRuleToInstantiation(rule, mostRecentMiss) != null) {
											recordIntoCompilation(recordToCompilation, mostRecentMiss, "RULE");
											// Mark.say("MATCH: ", rule.asString());
											if (!rulesAlreadyReported.containsDeprecated(rule)) {
												rulesAlreadyReported.addElement(rule);
												if (startIncrement) {
													for (Entity f : mostRecentMiss.getAllComponents()) {
														Mark.say(f.asString(), " added to orderingMap at ", orderCount);
														String hashedMiss = f.hash();
														ArrayList<Object> orderTypeTuple = new ArrayList<Object>();
														orderTypeTuple.add(0, orderCount);
														orderTypeTuple.add(1, "RULE");
														orderingMap.put(hashedMiss, orderTypeTuple);
														orderCount++;
													}
												}
												String mostRecentGenerated = generator.generateXPeriod(mostRecentMiss.getObject()) + " ";
												String finalString = Html.red(mostRecentGenerated);
												BetterSignal signal = new BetterSignal("Story teller", finalString);
												Connections.getPorts(this).transmit(signal);
												Mark.say("Rule in Story teller", rule);
												String result = " because " + generator.generateAsIf(rule) + " ";
												String finalResult = Html.blue(result);
												if (result != null) {
													BetterSignal signalTwo = new BetterSignal("Story teller", finalResult);
													Connections.getPorts(this).transmit(signalTwo);

													Connections.getPorts(this).transmit(NEW_RULE_MESSENGER_PORT, true);
													Connections.getPorts(this).transmit(TEACH_RULE_PORT, rule);

												}

											}
											break;
										}

									}
								}

							}

						}
					}
				}

				isOneQuiet = false;
				isTwoQuiet = false;

			}
		}

	}

	public void extractSummaryFromStory(Sequence explicitStory, Sequence conceptElements, LinkedList<ArrayList<Object>> storyCompilation) {
		if (!Radio.tellStoryButton.isSelected()) {
			return;
		}
		Generator generator = Generator.getGenerator();
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		Set<Entity> alreadyReported = new HashSet<Entity>();

		Mark.say("NECESSARY FOR CONCEPT : ");
		for (Entity t : conceptElements.getAllComponents()) {
			Mark.say(t.asString());
		}

		for (ArrayList<Object> recordedElement : storyCompilation) {
			Entity storyElement = (Entity) recordedElement.get(0);
			Mark.say("Trying: ", storyElement.asString());
			String type = (String) recordedElement.get(1);

			for (Entity t : conceptElements.getAllComponents()) {
				LList<PairOfEntities> match = matcher.match(t, storyElement);
				if (match != null) {
					if (type == "REGULAR") {
						if (!alreadyReported.contains(t)) {
							for (Entity d : explicitStory.getAllComponents()) {
								LList<PairOfEntities> explicitMatch = matcher.matchAnyPart(d, storyElement);
								if (explicitMatch != null) {

									alreadyReported.add(t);
									String toPublish = generator.generate(t);
									BetterSignal toGuiTab = new BetterSignal("Summary", toPublish);
									Connections.getPorts(this).transmit(toGuiTab);
									break;
								}
							}
						}
					}
					else if (type == "MISSING") {
						if (!alreadyReported.contains(storyElement)) {
							alreadyReported.add(storyElement);
							String toPublish = generator.generate(storyElement);
							String formatted = Html.red(toPublish);
							BetterSignal toGuiTab = new BetterSignal("Summary", formatted);
							Connections.getPorts(this).transmit(toGuiTab);
							break;
						}
					}
					else if (type == "RULE") {
						String toPublish = generator.generate(storyElement);
						String formatted = Html.blue(toPublish);
						BetterSignal toGuiTab = new BetterSignal("Summary", formatted);
						Connections.getPorts(this).transmit(toGuiTab);
						break;
					}

				}
			}
		}
	}

	public boolean usefulPartialMatch(LList<PairOfEntities> match) {
		if (!Radio.tellStoryButton.isSelected()) {
			return false;
		}
		if (match.first() != null) {
			Entity first_a = match.first().getDatum();
			Entity first_b = match.first().getPattern();
			if (match.second() != null) {
				Entity second_a = ((PairOfEntities) match.second()).getDatum();
				Entity second_b = ((PairOfEntities) match.second()).getPattern();
				if (first_a.equals(first_b) && second_a.equals(second_b)) {
					return true;
				}
			}
			else {
				if (first_a.equals(first_b)) {
					return true;
				}
			}
		}

		return false;
	}
}

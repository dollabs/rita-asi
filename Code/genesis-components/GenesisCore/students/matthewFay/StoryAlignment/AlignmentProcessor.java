package matthewFay.StoryAlignment;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

import matthewFay.Utilities.Pair;
import matthewFay.Utilities.StopWatch;
import matthewFay.VideoAnalysis.EventPredictor;
import matthewFay.viewers.AlignmentViewer;
import mentalModels.MentalModel;
import utils.*;
import utils.minilisp.LList;
import connections.*;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Sequence;

public class AlignmentProcessor extends AbstractWiredBox {

	public static final String CONCEPT_ALIGNMENT_OUTPUT = "REFLECTION ALIGNMENT OUTPUT";

	public static final String GRAPH_PORT_OUTPUT = "Graph Port Output";

	public static final String RESET_PORT = "reset port";

	public static final String STAGE_DIRECTION_PORT = "stage direction";

	public static final String COMPLETE_STORY_ANALYSIS_PORT = "COMPLETE STORY ANALYSIS PORT";

	public static final String COMPLETE_STORY_ANALYSIS_PORT2 = "COMPLETE STORY ANALYSIS PORT2";

	public static final String INSERT_PORT = "INSERT ELEMENT PORT";

	private static String ALIGNMENT_PROCESSOR = "AlignmentProcessor";

	public AlignmentProcessor() {
		super(ALIGNMENT_PROCESSOR);

		Connections.getPorts(this).addSignalProcessor(COMPLETE_STORY_ANALYSIS_PORT, "processCompleteAnalysis");
		Connections.getPorts(this).addSignalProcessor(COMPLETE_STORY_ANALYSIS_PORT2, "processCompleteAnalysis2");

		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION_PORT, "processDirection");
		Connections.getPorts(this).addSignalProcessor(RESET_PORT, "reset");

		instance = this;
		// this.loadLibrary();
	}

	private static AlignmentProcessor instance = null;

	public static AlignmentProcessor getAlignmentProcessor() {
		return instance;
	}

	private Aligner aligner = new Aligner();

	public boolean useReflections = true;

	public boolean gapFilling = false;

	public void setFlags() {
		MatchTree.debugPrintOutputDuringGeneration = AlignmentViewer.debugTreeGeneration.isSelected();
		useReflections = AlignmentViewer.useConcepts.isSelected();
		gapFilling = AlignmentViewer.gapFilling.isSelected();
		if (AlignmentViewer.simpleScorer.isSelected()) {
			Mark.say("Using simple scorer");
		}
		else {
			Mark.say("Using penalizing scorer");
		}
	}

	HashMap<Integer, Sequence> stories = new HashMap<Integer, Sequence>();

	HashMap<Integer, Sequence> reflections = new HashMap<Integer, Sequence>();

	ArrayList<Sequence> patterns = new ArrayList<Sequence>();

	public void saveLibrary() {
		try {
			FileOutputStream fout = new FileOutputStream("patternlibrary.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(patterns);
			oos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void loadLibrary() {
		try {
			FileInputStream fin = new FileInputStream("patternlibrary.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			patterns = (ArrayList<Sequence>) ois.readObject();
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processDirection(Object o) {
		if (Markers.RESET.equals(o)) {
			reset(null);
		}
	}

	/**
	 * Resets the state of the system and clears the pattern library
	 * 
	 * @param o
	 */
	public void reset(Object o) {
		stories.clear();
		reflections.clear();
		patterns.clear();
		// this.loadLibrary();
	}

	public void processCompleteAnalysis(Object o) {
		BetterSignal ca = BetterSignal.isSignal(o);
		if (ca == null) return;
		doProcessing(0, ca);
	}

	public void processCompleteAnalysis2(Object o) {
		BetterSignal ca = BetterSignal.isSignal(o);
		if (ca == null) return;
		doProcessing(1, ca);
	}

	public void doProcessing(int id, BetterSignal bs) {
		if (!Radio.alignmentButton.isSelected()) return;

		Sequence story = bs.get(0, Sequence.class);
		Sequence conceptPatterns = bs.get(3, Sequence.class);

		Mark.say(story.asString());

		if (storyRememberer(story.getElement(story.getNumberOfChildren() - 1))) {
			story.removeElement(story.getElement(story.getNumberOfChildren() - 1));
			patterns.add(story);
		}

		if (storyPatternAligner(story.getElement(story.getNumberOfChildren() - 1))) {
			story.removeElement(story.getElement(story.getNumberOfChildren() - 1));
			alignToPatterns(story);
		}
		else if (storyTraitEventPrediction(story.getElement((story.getNumberOfChildren() - 1)))) {
			story.removeElement(story.getElement(story.getNumberOfChildren() - 1));
			// Hack-ish, search story for personality traits.
			// Load stories for traits
			// Find good predictor from traits for next event
			ArrayList<Sequence> traitStories = new ArrayList<Sequence>();
			for (Entity t : story.getElements()) {
				if (storyPersonalityTrait(t)) {
					String trait = getPersonalityTrait(t);
					Sequence traitStory = MentalModel.loadGlobalMentalModel(trait).getStoryProcessor().getStory();
					traitStories.add(traitStory);
				}
			}
			traitStories.addAll(patterns);

			EventPredictor ep = new EventPredictor();
			Entity prediction = ep.predictMostLikelyNextEvent(story, traitStories);
			Mark.say("Prediction: ", prediction.asString());
			Connections.getPorts(this).transmit(INSERT_PORT, prediction);
			// story.addElement(prediction);
		}
		else {

			stories.put(id, story);
			reflections.put(id, conceptPatterns);
			BetterSignal alignmentSignal = alignStories(stories, reflections);
			Connections.getPorts(this).transmit(alignmentSignal);
		}

	}

	public boolean storyPersonalityTrait(Entity t) {
		if (t.relationP("personality_trait")) return true;
		return false;
	}

	public String getPersonalityTrait(Entity t) {
		return t.getObject().getType();
	}

	public boolean storyRememberer(Entity t) {
		if (t.relationP(Markers.REMEMBER_MARKER) && t.getSubject().entityP("you")) {
			return true;
		}
		return false;
	}

	public boolean storyPatternAligner(Entity t) {
		if (t.relationP(Markers.PATTERN_ALIGNER_MARKER) && t.getSubject().entityP("you")) {
			return true;
		}
		return false;
	}

	public boolean storyTraitEventPrediction(Entity t) {
		if (t.relationP(Markers.TRAIT_EVENT_PREDICTION_MARKER) && t.getSubject().entityP("you")) {
			return true;
		}
		return false;
	}

	private void alignToPatterns(Sequence datum) {
		SortableAlignmentList patternMatches = new SortableAlignmentList();
		for (Sequence pattern : patterns) {
			Mark.say("Aligning: ", datum.asString());
			Mark.say("To: ", pattern.asString());
			// patternMatches.add(this.alignStories(pattern, datum, null));
			patternMatches.add(aligner.align(pattern, datum).get(0));
		}
		patternMatches.sort();

		BetterSignal patternMatchSignal = new BetterSignal();
		for (int i = 0; i < patternMatches.size(); i++) {
			patternMatchSignal.add(patternMatches.get(i));
		}
		Connections.getPorts(this).transmit(patternMatchSignal);
	}

	public void addPatterns(ArrayList<Sequence> patternsToAdd) {
		if (patternsToAdd != null) {
			patterns.addAll(patternsToAdd);
		}
	}

	@SuppressWarnings("unchecked")
	public Alignment<Entity, Entity> alignStories(Sequence story1, Sequence story2, LList<PairOfEntities> bootstrappedBindings) {
		HashMap<Integer, Sequence> stories = new HashMap<Integer, Sequence>();
		stories.put(0, story1);
		stories.put(1, story2);

		if (bootstrappedBindings == null) bootstrappedBindings = new LList<PairOfEntities>();

		BetterSignal bs = alignStories(stories, bootstrappedBindings);
		return bs.get(0, Alignment.class);
	}

	private BetterSignal alignStories(HashMap<Integer, Sequence> stories, LList<PairOfEntities> bootstrappedBindings) {
		BetterSignal bs = null;
		if (stories != null && stories.keySet().size() > 1) {
			bs = alignStories(stories, new HashMap<Integer, Sequence>(), bootstrappedBindings);
		}
		return bs;
	}

	private BetterSignal alignStories(HashMap<Integer, Sequence> stories, HashMap<Integer, Sequence> reflections) {
		BetterSignal bs = null;
		if (stories != null && stories.size() > 1) {
			if (reflections != null && reflections.size() > 1) {
				bs = alignStories(stories, reflections, new LList<PairOfEntities>());
			}
		}
		return bs;
	}

	private HashMap<Integer, Sequence> last_stories;

	private HashMap<Integer, Sequence> last_reflections;

	private LList<PairOfEntities> last_bindings;

	public BetterSignal rerunLastAlignment() {
		return alignStories(last_stories, last_reflections, last_bindings);
	}

	private BetterSignal alignStories(HashMap<Integer, Sequence> stories, HashMap<Integer, Sequence> reflections, LList<PairOfEntities> bindings) {
		last_stories = stories;
		last_reflections = reflections;
		last_bindings = bindings;

		setFlags();

		if (stories.size() < 2) {
			Mark.say("Nothing to align");
			return new BetterSignal();
		}

		StopWatch reflectionStopWatch = new StopWatch();
		Mark.say("Start Reflection Watch");
		reflectionStopWatch.start();

		if (useReflections && reflections.containsKey(0) && reflections.containsKey(1)) {
			Mark.say("Boot Strapping Reflective Knowledge!");
			LList<PairOfEntities> newBindings = aligner.getPlotUnitBindings(reflections.get(0), reflections.get(1), bindings);
			bindings = bindings.append(newBindings);
			Mark.say("New bindings from reflection:");
			Mark.say(bindings);
			Connections.getPorts(this).transmit(CONCEPT_ALIGNMENT_OUTPUT, new BetterSignal(aligner.getLastReflectionAlignment()));
		}

		reflectionStopWatch.stop();
		Mark.say("Reflection Alignment Time: " + reflectionStopWatch.getElapsedTime());

		StopWatch stopWatch = new StopWatch();
		Mark.say("Start Watch");
		stopWatch.start();

		SortableAlignmentList alignments = aligner.align(stories.get(0), stories.get(1), bindings);
		Connections.getPorts(this).transmit(GRAPH_PORT_OUTPUT, new BetterSignal(aligner.getLastMatchTree().graph));

		Mark.say("Alignment Time (before gap filling): " + stopWatch.getElapsedTime());

		BetterSignal alignmentSignal = new BetterSignal();
		for (Alignment<Entity, Entity> alignment : alignments) {
			// Do Gap Filling
			if (gapFilling) {
				alignment = fillGaps(alignment);
			}
			alignmentSignal.add(alignment);
		}

		stopWatch.stop();
		Mark.say("Alignment Time: " + stopWatch.getElapsedTime());

		return alignmentSignal;
	}

	private Alignment<Entity, Entity> fillGaps(Alignment<Entity, Entity> alignment) {
		// COLLABORATIVE GAP FILLING HACK! //
		SequenceAlignment sa = (SequenceAlignment) alignment;
		sa.fillGaps();
		return alignment;
	}

	private Entity findAndReplace(Entity element, LList<PairOfEntities> bindings) {
		if (element.entityP()) {
			// Find Replacement and return it
			for (PairOfEntities pair : bindings) {
				if (pair.getPattern().isDeepEqual(element)) {
					return pair.getDatum();
				}
			}
			// /Couldn't find binding... Trying fliped bindings
			for (PairOfEntities pair : bindings) {
				if (pair.getDatum().isDeepEqual(element)) {
					return pair.getPattern();
				}
			}
			return new Entity();
		}
		if (element.relationP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings));
			element.setObject(findAndReplace(element.getObject(), bindings));
			return element;
		}
		if (element.functionP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings));
			return element;
		}
		if (element.sequenceP()) {
			int i = 0;
			Sequence s = (Sequence) element;
			while (i < element.getNumberOfChildren()) {
				Entity child = element.getElement(i);
				child = findAndReplace(child, bindings);
				s.setElementAt(child, i);
				i++;
			}
			return element;
		}

		return element;
	}

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	public static void main(String[] args) {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		URL serverURL = null;
		try {
			serverURL = new URL(wireServer);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String input = "";
			AlignmentProcessor.ALIGNMENT_PROCESSOR = "AlignmentProcessorService";
			Connections.useWireServer(serverURL);
			AlignmentProcessor ap = new AlignmentProcessor();
			Connections.publish(ap, "AlignmentProcessorService");

			System.out.println("AlignmentProcessorService started, input commands");

			while (!input.toLowerCase().equals("quit")) {
				input = in.readLine().trim().intern();
				BetterSignal b = new BetterSignal();
				String[] sigargs = input.split(" ");
				for (String s : sigargs) {
					b.add(s);
				}
			}

		}
		catch (NetWireException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

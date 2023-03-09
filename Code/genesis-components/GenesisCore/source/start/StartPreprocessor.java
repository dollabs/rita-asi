package start;

import generator.Generator;
import genesis.*;
import gui.*;
import mentalModels.MentalModel;
import start.portico.ElizaMatcher;
import start.transitionSpace.Co57Processor;

import java.util.*;

import storyProcessor.StoryProcessor;
import utils.Talker;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import consciousness.ProblemSolver;
import constants.*;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * Processes idioms, such as: Start story titled "Shakespeare's Macbeth". <p> Also processes stage directions, such as:
 * Show both perspectives. Created on May 10, 2011
 * @author phw
 */

public class StartPreprocessor extends AbstractWiredBox {

	private boolean debug = false;

	private boolean sleepOnInsert = true;

	private static final String FRAME = "frame port";

	public static final String SHORTCUT = "stage direction port";

	public static final String SELF = "self";

	public static final String VIDEO_MEMORY_PORT = "video memory port";

	public static final String RESET_TEXT_DISPLAYS = "reset port";

	private boolean listenToCo57 = false;

	public static final String TRACES_PORT = "traces port";

	public static final String STORY_TEXT = "story text";

	public static final String COMMENT_PORT = "Comment";

	public static final String POPUP_PORT = "Popup";

	public static final String INITIALIZE = "Initialize";

	private int SILENT = 0, COMMONSENSE = 1, CONCEPT = 2, STORY = 3;

	private int mode = SILENT;

	public static String RESET_STATS_PORT = "Reset stats port";

	public static String TO_TEXT_ENTRY_BOX = "To text entry box";

	public static String TO_PERSUADER = "to persuader";

	public static String INCONSISTENCY = "Preprocessor opposite port";

	private static StartPreprocessor startPreprocessor;

	public static StartPreprocessor getStartPreprocessor() {
		if (startPreprocessor == null) {
			startPreprocessor = new StartPreprocessor();
		}
		return startPreprocessor;
	}

	private ElizaMatcher matcher = ElizaMatcher.getElizaMatcher();

	private static Set<String> variablesSetToTrue = new HashSet<>();

	public StartPreprocessor() {
		super("StartPreprocessor");
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	/**
	 * Runs several kinds of preprocessors, most importantly:
	 * <p>
	 * Stage direction idioms
	 * <p>
	 * Eliza script
	 * <p>
	 * Also appears to run some obsolete stuff, most notably
	 * <p>
	 * Marked text (denoted by : characer, purpose forgotten)
	 * <p>
	 * Attention trace (denoted by hashmap)
	 * <p>
	 * Frame (denoted by ! character, purpose forgotten)
	 */

	public void process(Object object) {
		boolean debug = false;
		Mark.say(debug, "StartPreprocessor A", object);
		if (object instanceof Entity) {
			if (object instanceof Sequence) {
				for (Entity t : ((Sequence) object).getElements()) {
					String sentence = Generator.getGenerator().generate((Entity) t);
					if (sentence != null) {
						Connections.getPorts(this).transmit(sentence);
					}
					else {
						Mark.err("Unable to generate from", ((Entity) object).asString());
					}
				}
			}
			return;
		}
		else if (processAttentionTrace(object)) {
			return;
		}
		else if (!(object instanceof String)) {
			return;
		}
		Mark.say(debug, "StartPreprocessor B");
		String input = (String) object;
		if (input.length() == 0) {
			Mark.err("Empty string appeared in preprocesor, no action");
			return;
		}
		int index = input.indexOf(';');
		Mark.say(debug, "StartPreprocessor C");
		if (index > 0) {
			if (index == 0) {
				Mark.err("Semicolon at beginning of sentence string in StartPreprocessor.process");
				process(input.substring(1));
				return;
			}
			String[] parts = input.split(";");
			for (int i = 0; i < parts.length - 1; ++i) {
				String antecedent = trimAndRemovePeriod(parts[i]);
				String consequent = trimAndRemovePeriod(parts[i + 1]);
				process("Evidently, " + consequent + " because " + antecedent + ".");
			}
			return;
		}


		if (processStageDirections(input)) {
			Mark.say(debug, "Preprocessor processed stage direction", input);
			return;
		}
		else if (false && markedText(input)) {
			String marker = extractMarker(input);
			String text = removetMarker(input);
			Connections.getPorts(this).transmit(new BetterSignal(marker, text));
			return;
		}
		else if (processFrame(input)) {
			Mark.say("Preprocessor processed frame", input);
			return;
		}
		else if (processWithEliza(input)) {
			Mark.say(debug, "Preprocessor processed Eliza script", input);
			return;
		}

		Mark.say(debug, "StartPreprocessor E");
		// Mark.say("Preprocessor forwarded", input);

		if (mode == STORY) {
			Connections.getPorts(this).transmit(STORY_TEXT, new BetterSignal("Story teller", input));
		}

		Mark.say(debug, "StartPreprocessor F", input);
		Connections.getPorts(this).transmit(input);
		Mark.say(debug, "StartPreprocessor G", object);
	}

	private String trimAndRemovePeriod(String string) {
		string = string.trim();
		if (string.endsWith(".")) {
			return string.substring(0, string.length() - 1);
		}
		return string;
	}

	private String removetMarker(String input) {
		int index = input.indexOf(':');
		if (index < 0) {
			return null;
		}
		return input.substring(index + 1);
	}

	private String extractMarker(String input) {
		int index = input.indexOf(':');
		if (index < 0) {
			return null;
		}
		return input.substring(0, index);
	}

	private boolean markedText(String input) {
		if (input.contains(":")) {
			return true;
		}
		return false;
	}

	private boolean processAttentionTrace(Object input) {
		if (input instanceof HashMap && getListenToCo57()) {
			// Mark.say("Got hash map", ((HashMap)input).toString());
			// Wire, if not wired
			Co57Processor.getCo57Processor();
			Connections.getPorts(this).transmit(TRACES_PORT, input);
			return true;
		}
		return false;
	}

	private boolean processFrame(String input) {
		if (input.isEmpty()) {
			return false;
		}
		else if (input.charAt(0) == '!') {
			input = input.substring(1);
		}
		// else if (input.indexOf("\" ") > 0) {
		// }
		else {
			return false;
		}
		HashMap<String, String> result = new HashMap<String, String>();
		// Expect input to be alternate key/value pairs with keys having :'s
		// at the end
		ArrayList<String> keysAndValues = split(input.substring(1, input.length() - 1));
		String key = null;
		String value = "";
		int i = 0;
		while (true) {
			String word = keysAndValues.get(i);
			if (isKey(word)) {
				if (key != null) {
					result.put(strip(key), value);
					value = "";
				}
				key = word;
			}
			else {
				value = value.trim() + " " + word;
			}
			++i;
			if (i >= keysAndValues.size()) {
				if (key != null) {
					result.put(strip(key), value);
				}
				break;
			}
			analyze(input, result);
			return true;
		}
		return false;
	}

	private void analyze(String source, HashMap<String, String> result) {
		if (true) {
			Mark.say("Frame analyzer dealing with", source);
		}
		// Mark.say("Transmitting on frame port:", result);
		Connections.getPorts(this).transmit(FRAME, result);
	}

	private ArrayList<String> split(String x) {
		x = x.trim();
		ArrayList<String> result = new ArrayList<String>();
		while (true) {
			if (x.isEmpty()) {
				break;
			}
			int index = x.indexOf(' ');
			if (index >= 0) {
				result.add(x.substring(0, index).trim());
				x = x.substring(index).trim();
			}
			else {
				result.add(x.trim());
				break;
			}

		}
		return result;
	}

	private String strip(String key) {
		return key.substring(0, key.length() - 1);
	}

	private boolean isKey(String word) {
		if (word.charAt(word.length() - 1) == ':') {
			return true;
		}
		return false;
	}

	private boolean processWithEliza(String input) {
		boolean debug = false;

		Mark.say(debug, "Entering Eliza with", input);

		if (matcher.match("Assert thread *t", input.trim())) {
			String thread = matcher.getBindings().get("*t");
			installThreads(thread);
			return true;
		}
		else {
			Mark.say(debug, "No match to", input);
		}

		if (matcher.match("?t is a kind of personality trait", input)) {
			Mark.say(debug, "Match A");
			String name = matcher.getBindings().get("?t");
			name = removeQuotes(name);
			// Important special case. Must know that word is a personality trait before loading file, else use of
			// personality trait in rules will not have the correct thread. So, want regular START interpretation as
			// well as reading of personality file.

			Bundle bundle = BundleGenerator.getBundle(name);

			Entity entity = new Entity(bundle);

			Connections.getPorts(this).transmit(input);

			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, new BetterSignal(Markers.LOAD_PERSONALITY_FILE, entity));

			return true;
		}

		else if (matcher.match("Load ?t personality file", input)) {
			Mark.say(debug, "Match B");
			String name = matcher.getBindings().get("?t");

			Bundle bundle = BundleGenerator.getBundle(name);
			Mark.say("Bundle", name, bundle);

			Entity entity = new Entity(bundle);

			Mark.say("Entity", name, entity.toXML());

			// Mark.say("Preprocessor finds personality instruction in at", input);
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, new BetterSignal("LoadPersonalityFile", entity));
			return true;
		}
		else if (matcher.match("Set left panel to *t at *u", input)) {

			String name = matcher.getBindings().get("*t").trim();
			String tab = matcher.getBindings().get("*u").trim();

			Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.LEFT, name, tab));
			return true;
		}
		else if (matcher.match("Set left panel to *t", input)) {
			String name = matcher.getBindings().get("*t");
			Mark.say(debug, "Match C, setting left panel to", name);
			Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.LEFT, name));
			return true;
		}
		else if (matcher.match("Set right panel to *t", input)) {
			String name = matcher.getBindings().get("*t");
			Mark.say(debug, "Match D, setting right panel to", name);
			Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.RIGHT, name));
			return true;
		}
		else if (matcher.match("Set bottom panel to *t", input)) {
			String name = matcher.getBindings().get("*t");
			Mark.say(debug, "Match E, setting bottom panel to", name);
			Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.BOTTOM, name));
			return true;
		}
		// Following obsolete and seemed to screw up Jasmine
		// else if (matcher.match("Interpret video named *t", input) || matcher.match("Interpret video *t", input)) {
		// Mark.say(debug, "Match F");
		// String name = matcher.getBindings().get("*t");
		// name = trimOff('\"', name);
		// Mark.say("Interpreting video titled", name);
		// Connections.getPorts(this).transmit(SELF, "Start video");
		// String message = name + ".mov";
		// RemoteAnnotations.getInstance().playVideo(message);
		// return true;
		// }
		else if (matcher.match("Cut connection to *t", input)) {
			Mark.say(debug, "Match G");
			String name = matcher.getBindings().get("*t");
			name = trimOff('\"', name);
			// Mark.say("Value of mental model is", name);
			MentalModel model = MentalModel.getGlobalMentalModel(name);
			Connections.disconnect(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, model
			        .getStoryProcessor());
			Connections.disconnect(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), model.getStoryProcessor());
			return true;
		}
		else if (matcher.match("Open connection to *t", input)) {
			Mark.say(debug, "Match H");
			String name = matcher.getBindings().get("*t");
			name = trimOff('\"', name);
			Mark.say("Value of mental model is", name);
			MentalModel model = MentalModel.getGlobalMentalModel(name);
			Connections.wire(Start.STAGE_DIRECTION_PORT, StartPreprocessor.getStartPreprocessor(), Start.STAGE_DIRECTION_PORT, model
			        .getStoryProcessor());
			Connections.wire(Markers.NEXT, GenesisGetters.getAnaphoraExpert(), model.getStoryProcessor());
			return true;
		}
		else if (matcher.match("Transfer story", input)) {
			Mark.say("Transfering story from mental model to story processor");
			MentalModel mm = Genesis.getMentalModel1();
			StoryProcessor storyProcessorMM = mm.getStoryProcessor();
			StoryProcessor storyProcessorI = mm.getI().getStoryProcessor();
			storyProcessorMM.transferStory(storyProcessorI);
			return true;
		}
		else if (matcher.match("Transfer knowledge from ?s to ?t", input)) {
			Mark.say(debug, "Match I");
			String source = matcher.getBindings().get("?s");
			source = trimOff('\"', source);
			Mark.say("Value of source mental model is", source);
			String target = matcher.getBindings().get("?t");
			target = trimOff('\"', target);
			Mark.say("Value of target mental model is", target);
			MentalModel sourceModel = MentalModel.getGlobalMentalModel(source);
			MentalModel targetModel = MentalModel.getGlobalMentalModel(target);

			MentalModel.transferAllKnowledge(sourceModel, targetModel);
			// Mark.say("Prediction rule count in target", targetModel.getPredictionRules().size());
			return true;
		}
		else if (matcher.match("Connect to Co57", input)) {
			Mark.say(debug, "Match J");
			Mark.say("Connecting to Co57");
			Connections.getPorts(this).transmit(SELF, "Start video");
			return true;
		}
		else if (matcher.match("Assert thread *t", input)) {
			String thread = matcher.getBindings().get("*t");
			Mark.say("Hello world");
			installThreads(thread);
			return true;
		}
		else if (matcher.match("?x is incompatible with ?y", input)) {
			String x = matcher.getBindings().get("?x");
			String y = matcher.getBindings().get("?y");
			// Mark.say("Transmitting from preprocessor", x, "opposite of", y);
			Connections.getPorts(this).transmit(INCONSISTENCY, new BetterSignal(Markers.OPPOSITES, x, y));
		}
		else if (matcher.match("?x is the opposite of ?y", input)) {
			String x = matcher.getBindings().get("?x");
			String y = matcher.getBindings().get("?y");
			// Mark.say("Transmitting from preprocessor", x, "opposite of", y);
			Connections.getPorts(this).transmit(INCONSISTENCY, new BetterSignal(Markers.OPPOSITES, x, y));
			Connections.getPorts(this).transmit(TO_PERSUADER, new BetterSignal(Markers.OPPOSITES_COMMAND, x, y));
			return true;
		}
		else if (matcher.match("Make ?p be ?q", input)) {
			String person = matcher.getBindings().get("?p");
			String quality = matcher.getBindings().get("?q");
			Mark.say("Got", person, quality);
			Connections.getPorts(this).transmit(TO_PERSUADER, new BetterSignal(Markers.EMPHASIZE_COMMAND, person, quality));
			return true;
		}
		else if (matcher.match("Make ?p be ?q and not ?x", input)) {
			String person = matcher.getBindings().get("?p");
			String quality = matcher.getBindings().get("?q");
			String exclusion = matcher.getBindings().get("?x");
			// Mark.say("Got", person, quality, exclusion);
			Connections.getPorts(this).transmit(TO_PERSUADER, new BetterSignal(Markers.EMPHASIZE_DEEMPHASIZE_COMMAND, person, quality, exclusion));
			return true;
		}
		return false;
	}

	private void installThreads(String thread) {
		boolean debug = true;
		Mark.say(debug, "Thread:", thread);
		String[] classes = thread.split(",");
		Vector<String> vector = new Vector<>();
		for (String name : classes) {
			name = name.trim();
			vector.add(name);
			Thread t = Thread.constructThread(vector);
			Bundle b = BundleGenerator.getBundle(name);
			if (t.size() > 1 && !b.hasThread(t)) {
				b.setPrimedThread(t);
				BundleGenerator.setBundle(name, b);
				Mark.say(debug, "Tried to make bundle for", name, "be", b);
				Mark.say(debug, "Bundle for |", name, "| is", BundleGenerator.getBundle(name));
			}
			else {
				Mark.say(debug, name, "already has thread", t);
			}
			if (name.equals("dishonest 1")) {
				Mark.say(debug, BundleGenerator.getBundle("dishonest 1"));
			}
		}

	}

	private String removeQuotes(String name) {
		if (name.startsWith("\"")) {
			name = name.substring(1);
		}
		if (name.endsWith("\"")) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	private String trimOff(char c, String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		if (name.charAt(0) == c) {
			name = name.substring(1);
		}
		if (name.charAt(name.length() - 1) == c) {
			name = name.substring(0, name.length() - 1);
		}
		return name;
	}

	private boolean processStageDirections(String s) {
		boolean debug = false;
		String string = stripPunctuation(s);
		// Mark.say("Entering StartPreprocessor.processStageDirections", s);
		// Mark.say(string);

		if (Markers.FIRST_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.NEITHER);
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.LEFT);
			return true;
		}
		else if (Markers.SECOND_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.NEITHER);
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.RIGHT);
			return true;
		}
		else if (Markers.BOTH_PERSPECTIVES_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.BOTH);
			// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.LEFT);
			// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.RIGHT);
			return true;
		}
		else if (Markers.NEITHER_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.NEITHER);
			return true;
		}
		else if (Markers.SHOW_FIRST_PERSPECTIVE.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_LEFT);
			return true;
		}
		else if (Markers.SHOW_SECOND_PERSPECTIVE.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_RIGHT);
			return true;
		}
		else if (Markers.SHOW_BOTH_PERSPECTIVES.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_BOTH);
			return true;
		}
		else if (Markers.CLEAR_EXPLANATORY_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(StartPreprocessor.RESET_TEXT_DISPLAYS, TabbedTextViewer.ALL);
			return true;
		}
		else if (Markers.CLEAR_STORY_MEMORY_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.RESET);
			return true;
		}
		else if (Markers.CLEAR_MENTAL_MODELS.equalsIgnoreCase(string)) {
			// MentalModel.clearMentalModels();
			return true;
		}
		else if (Markers.START_GENERAL_KNOWLEDGE_TEXT.equalsIgnoreCase(string)) {
			// Mark.say("Starting silence");
			Connections.getPorts(this).transmit(TabbedTextViewer.TAB, TabbedTextViewer.SILENCE);
			Mark.say("L");
			Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			return true;
		}
		else if (string.startsWith(Markers.PAUSE_TEXT)) {
			String minis = string.substring(Markers.PAUSE_TEXT.length()).trim();
			if (minis.isEmpty()) {
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.PAUSE);
			}
			else {
				Talker.getTalker().sleep(minis);
			}
			sleepOnInsert = false;
			return true;
		}
		else if (Markers.REPLAY_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.REPLAY);
			return true;
		}
		// [laf 30 Jun 2011]
		else if (Markers.RADIATE_COMMONSENSE_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.RADIATE_COMMONSENSE);
			// Mark.say("M");
			Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			return true;
		}
		// [laf 19 July 2011]
		else if (Markers.INSERT_BIAS_TEXT.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.INSERT_BIAS);
			// TODO: probably I should do something with the mode as well)
			return true;
		}
		else if (string.startsWith(Markers.SET_SWITCH_TEXT) && string.indexOf(Markers.SET_SWITCH_SEPARATOR_TEXT) > 0) {
			setCheckBoxUsingIdiom(string);
			return true;
		}
		else if (string.startsWith(Markers.RESET_SWITCH_TEXT)) {
			resetCheckBoxesUsingIdiom();
			resetRadioButtonsUsingIdiom();
			// This is a hack. Reseting should set all radio buttions to default values
			return true;
		}
		else if (string.startsWith(Markers.SET_BUTTON_TEXT) && string.indexOf(Markers.SET_BUTTON_SEPARATOR_TEXT) > 0) {
			setRadioButtonsUsingIdiom(string);
			return true;
		}
		else if (string.startsWith(Markers.RESET_BUTTON_TEXT)) {
			resetSwitchesUsingIdiom(string);
			return true;
		}

		else if (string.startsWith(Markers.PERSONA_MARKER)) {
			Connections.getPorts(this).transmit(Start.PERSONA, string.substring(Markers.PERSONA_MARKER.length()).trim());
			return true;
		}
		// Deprecated
		else if (Markers.SWITCH_TO_CONCEPT_REPORTING.equalsIgnoreCase(string)) {
			// Mark.say("Start concept knowledge noted");
			// Mark.say("Starting reflective knowledge");
			// mode = this.CONCEPT;
			// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Concept knowledge");
			// Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, "Concept knowledge");
			return true;
		}
		// Deprecated
		else if (Markers.SWITCH_TO_COMMONSENSE_REPORTING.equalsIgnoreCase(string)) {
			// Mark.say("Start commonsense knowledge noted");
			// Mark.say("Starting commonsense knowledge");
			// mode = this.COMMONSENSE;
			// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Commonsense knowledge");
			// Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			return true;
		}
		// Names
		else if (string.startsWith(Markers.START_NAMING_PREFIX)) {
			int prefix = Markers.START_NAMING_PREFIX.length();
			if (string.endsWith(Markers.START_NAMING_SUFFIX)) {
				int suffix = Markers.START_NAMING_SUFFIX.length();
				String name = string.substring(prefix, string.length() - suffix).trim();
				Mark.say(debug, "Noting", name, "is a name");
				Start.getStart().processName(name);
				return true;
			}
			else if (string.endsWith(Markers.START_MALE_SUFFIX)) {
				int suffix = Markers.START_MALE_SUFFIX.length();
				String name = string.substring(prefix, string.length() - suffix).trim();
				Mark.say(debug, "Noting", name, "is a masculine name");
				Start.getStart().processName(name, "masculine");
				return true;
			}
			else if (string.endsWith(Markers.START_FEMALE_SUFFIX)) {
				int suffix = Markers.START_FEMALE_SUFFIX.length();
				String name = string.substring(prefix, string.length() - suffix).trim();
				Mark.say(debug, "Noting", name, "is a feminine name");
				Start.getStart().processName(name, "feminine");
				return true;
			}
			// Verbs
			else if (string.endsWith(Markers.START_VERB_SUFFIX)) {
				int suffix = Markers.START_VERB_SUFFIX.length();
				String definition = string.substring(prefix, string.length() - suffix).trim();
				// Mark.say("Noting", definition, "is a verb");
				Start.getStart().processVerb(definition);
				return true;
			}
			// Adverbs
			else if (string.endsWith(Markers.START_ADVERB_SUFFIX)) {
				int suffix = Markers.START_ADVERB_SUFFIX.length();
				String name = string.substring(prefix, string.length() - suffix).trim();
				Mark.say(debug, "Noting", name, "is an adverb");
				Start.getStart().processCategory(name, "adverb");
				return true;
			}
			// Adjectives
			else if (string.endsWith(Markers.START_ADJECTIVE_SUFFIX)) {
				int suffix = Markers.START_ADJECTIVE_SUFFIX.length();
				String name = string.substring(prefix, string.length() - suffix).trim();
				Mark.say(true, "Noting", name, "is an adjective");
				Start.getStart().processCategory(name, "adjective");
				return true;
			}
		}
		// Alternate expression
		else if (string.endsWith(Markers.START_NAMING_SUFFIX)) {
			int suffix = Markers.START_NAMING_SUFFIX.length();
			String name = string.substring(0, string.length() - suffix).trim();
			Mark.say(debug, "Noting", name, "is a name");
			Start.getStart().processName(name);
			return true;
		}

		else if (string.startsWith(Markers.START_EXPERIMENT) || string.startsWith(Markers.INSERT_START_EXPERIMENT_FILE)) {
			// Mark.say("Transmitting reset marker");
			try {
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.RESET);
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.LEFT);
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.RIGHT);

				Connections.getPorts(this).transmit(StartPreprocessor.RESET_TEXT_DISPLAYS, TabbedTextViewer.ALL);
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.NEITHER);
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.LEFT);
				Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_LEFT);

				Start.getStart().clearLocalTripleMaps();

				// Adapted from Markers.THE_END_TEXT handler
				mode = this.SILENT;
				// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.THE_END);

				// Yes, want to change to sentence mode at start of experiment
				Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
				Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Commonsense knowledge");

				Connections.getPorts(this).transmit(TO_TEXT_ENTRY_BOX, "");

				GenesisGetters.getMentalModel1().getProblemSolver().clearData();
				GenesisGetters.getMentalModel2().getProblemSolver().clearData();

			}
			catch (Exception e) {
				Mark.say("Mysterious exception");
			}

			return true;
		}
		// This is needed because no longer flip out of story mode with "The end.", so need way to really say that the
		// story is done, so as to enable more background knowledge before another story in the experiment.
		else if (string.startsWith(Markers.STOP_STORY_TEXT)) {
			Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
		}
		else if (string.startsWith(Markers.START_CONCEPT_TEXT)) {
			Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Concept knowledge");
			// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, "Concept knowledge");
		}
		else if (string.startsWith(Markers.START_STORY_TEXT)) {
			// Mark.say("Starting story knowledge");
			mode = this.STORY;

			// Connections.getPorts(this).transmit(STORY_TEXT, new
			// BetterSignal("Story teller", TabbedTextViewer.CLEAR));
			Connections.getPorts(this).transmit(STORY_TEXT, new BetterSignal("Story teller", TabbedTextViewer.CLEAR));
			Connections.getPorts(this).transmit(STORY_TEXT, new BetterSignal("Predictions", TabbedTextViewer.CLEAR));
			Connections.getPorts(this).transmit(STORY_TEXT, new BetterSignal("Concept analysis", TabbedTextViewer.CLEAR));

			// Clear reflection bar

			Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Story");


			// No return here, need to process story in story processor.
		}
		else if (string.startsWith(Markers.INSERT_TEXT) || startsWith(string, Markers.INSERT_QUESTION)) {
			String text = "";
			if (string.startsWith(Markers.INSERT_TEXT)) {
				text = string.substring(Markers.INSERT_TEXT.length()).trim() + ".";
			}
			else if (string.startsWith(Markers.INSERT_QUESTION)) {
				text = string.substring(Markers.INSERT_QUESTION.length()).trim() + "?";
			}
			if (text.startsWith(":")) {
				text = text.substring(1).trim();
			}
			// Mark.say("\n>>> Inserting into text box", text);

			boolean handle = Switch.showTextEntryBox.isSelected();

			Switch.showTextEntryBox.setSelected(false);
			Switch.showTextEntryBox.doClick();

			Switch.showTextEntryBox.setDefault(handle);
			;

			try {
				if (sleepOnInsert) {
				java.lang.Thread.sleep(2000);
				}

			}
			catch (InterruptedException e) {
			}

			sleepOnInsert = false;

			// Mark.say("Inserted", text, "into box and returning true");

			Connections.getPorts(this).transmit(TO_TEXT_ENTRY_BOX, text);



			return true;
		}
		else if (string.startsWith(Markers.BROADCAST_STATUS)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.BROADCAST_STATUS);
			return true;
		}
		// else if (string.startsWith(Markers.CONTINUE_STORY)) {
		// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.CONTINUE_STORY);
		// return true;
		// }
		// else if (string.startsWith(Markers.SUSPEND_STORY)) {
		// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.SUSPEND_STORY);
		// return true;
		// }
		else if (Markers.THE_END_TEXT.equalsIgnoreCase(string)) {
			mode = this.SILENT;
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.THE_END);

			// Do not want to stop story_mode; may be more insertions into the story
			// Oops, screw up questions because START still in story mode.
			Connections.getPorts(this).transmit(Start.MODE, Start.SENTENCE_MODE);
			// Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Start.PAUSE);
			Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Commonsense knowledge");
			return true;
		}
		else if ("Clear video memory".equalsIgnoreCase(string)) {
			// Connections.getPorts(this).transmit(StartPreprocessor.VIDEO_MEMORY_PORT,
			// new BetterSignal(GapFiller.CLEAR_PATTERNS));
			return true;
		}
		else if (Markers.ACTUATE_SUMMARIZER.equalsIgnoreCase(string)) {
			Connections.getPorts(this).transmit(Start.STAGE_DIRECTION_PORT, Markers.ACTUATE_SUMMARIZER);
			return true;
		}
		else if (string.startsWith(Markers.COMMENT)) {
			String comment = string.substring(Markers.COMMENT.length()).trim();
			Connections.getPorts(this).transmit(COMMENT_PORT, new BetterSignal("Comment", comment));
			return true;
		}
		else if (string.startsWith(Markers.SET_VARIABLE_TEXT)) {
			int t = string.indexOf(Markers.SET_VARIABLE_TRUE);
			int f = string.indexOf(Markers.SET_VARIABLE_FALSE);
			String variable = null;
			if (t >= 0) {
				variable = string.substring(Markers.SET_VARIABLE_TEXT.length(), t).trim();
				variablesSetToTrue.add(variable);
				Mark.say("Adding variable", variable, "to true list");
				Mark.say("List as is", variablesSetToTrue);
			}
			else if (f >= 0) {
				variable = string.substring(Markers.SET_VARIABLE_TEXT.length(), f).trim();
				variablesSetToTrue.remove(variable);
				Mark.say("Removing variable", variable, "from true list");
			}
			else {
				Mark.err("Oops, bad construction", string);
			}
			Mark.say("Variable is", variable, t, f, "in", variablesSetToTrue);

			return true;
		}
		else if (string.startsWith(Markers.CHECK_VARIABLE_VALUE)) {
			int t = string.indexOf(Markers.IS_VARIABLE_TRUE);
			int f = string.indexOf(Markers.IS_VARIABLE_FALSE);
			String variable = null;
			if (t >= 0) {
				variable = string.substring(Markers.CHECK_VARIABLE_VALUE.length(), t).trim();
				Mark.say("Starting with", variablesSetToTrue);
				if (variablesSetToTrue.contains(variable)) {
					String x = string.substring(t + Markers.IS_VARIABLE_TRUE.length()).trim();
					Mark.say("Variable", variable, "is true, executing", x);
					process(x);
				}
				else {
					Mark.say("Variable", variable, "is false, not executing", string);
					Mark.say("Not in", variablesSetToTrue);
				}
			}
			else if (f >= 0) {
				variable = string.substring(Markers.CHECK_VARIABLE_VALUE.length(), f).trim();
				if (!variablesSetToTrue.contains(variable)) {
					String x = string.substring(f + Markers.IS_VARIABLE_FALSE.length()).trim();
					// Mark.say("Variable", variable, "is false, executing", x);
					process(x);
				}
				else {
					// Mark.say("Variable", variable, "is true, not executing", string);
				}
			}

			else {
				Mark.err("Oops, bad construction", string);
			}
			return true;
		}

		return false;
	}

	private boolean startsWith(String string, String x) {
		if (string.trim().toLowerCase().startsWith(x.trim().toLowerCase())) {
			return true;
		}
		return false;
	}

	private void resetSwitchesUsingIdiom(String string) {
		resetCheckBoxesUsingIdiom();
		Switch.includeCauseExplanationCheckBox.setSelected(true);
		Switch.includeConceptExplanationCheckBox.setSelected(true);

		Switch.showTextEntryBox.setSelected(true);
		Switch.showTextEntryBox.doClick();
	}

	public void resetCheckBoxesUsingIdiom() {
		// Mark.say("Resetting check box switches");
		CheckBoxWithMemory.getCheckBoxes().stream().forEachOrdered(f -> f.reset());
	}

	public void resetRadioButtonsUsingIdiom() {
		// Mark.say("Resetting radio buttons");
		RadioButtonWithDefaultValue.getButtons().stream().forEachOrdered(f -> f.reset());
	}

	private void resetRadioButtonsUsingIdiom(String string) {
		Switch.getRadioButtons().stream().forEach(f -> {
			f.setSelected(false);
		});
	}

	boolean done = false;

	private void setCheckBoxUsingIdiom(String string) {
		boolean debug = false;
		Mark.say(debug, "Switch idiom text:", string);
		String command = string.substring(Markers.SET_SWITCH_TEXT.length()).trim();
		int index = command.indexOf(Markers.SET_SWITCH_SEPARATOR_TEXT);
		String control = command.substring(0, index);
		String setting = command.substring(index + Markers.SET_SWITCH_SEPARATOR_TEXT.length()).trim();
		done = false;
		CheckBoxWithMemory.getCheckBoxes().stream().forEachOrdered(f -> {
			// Mark.say("Control:", f.getText());
			if (control.equalsIgnoreCase(f.getText())) {
				Mark.say(debug, "Programmatically setting", control, "to", setting);
				done = true;
				boolean handle = f.isSelected();
				if (setting.equalsIgnoreCase("true")) {
					f.setSelected(false);
					f.doClick();
				}
				else if (setting.equalsIgnoreCase("false")) {
					f.setSelected(true);
					f.doClick();
				}
				else {
					Mark.err("Bad switch setting command", string);
				}
				f.setDefault(handle);
			}
		});
		if (!done) {
			Mark.err("Switch", control, "not recognized");
		}
	}

	private void setRadioButtonsUsingIdiom(String string) {
		String command = string.substring(Markers.SET_BUTTON_TEXT.length()).trim();
		int index = command.indexOf(Markers.SET_BUTTON_SEPARATOR_TEXT);
		String control = command.substring(0, index);
		String setting = command.substring(index + Markers.SET_BUTTON_SEPARATOR_TEXT.length()).trim();
		done = false;
		RadioButtonWithDefaultValue.getButtons().stream().forEachOrdered(f -> {
			// Mark.say("Control:", f.getText());
			if (control.equalsIgnoreCase(f.getText())) {
				done = true;
				if (setting.equalsIgnoreCase("true")) {
					Mark.say("Setting", f.getText(), "to true");
					// Transition may matter, may be listener
					f.setSelected(false);
					f.doClick();
				}
				else if (setting.equalsIgnoreCase("false")) {
					// f.setSelected(false);
				}
				else {
					Mark.say("Bad switch setting command", string);
				}
			}
		});
		if (!done) {
			Mark.err("Switch", control, "not recognized");
		}
	}

	private String stripPunctuation(String s) {
		s = s.trim();
		int l = s.length();
		if (l > 0) {
			char c = s.charAt(l - 1);
			if (".?!".indexOf(c) >= 0) {
				return s.substring(0, l - 1);
			}
		}
		return s;
	}

	public boolean getListenToCo57() {
		return listenToCo57;
	}

	public void setListen(boolean listen) {
		this.listenToCo57 = listen;
		if (this.listenToCo57) {
			Mark.say("Connecting to Co57 attention traces");
		}
		else {
			Mark.say("Disconnecting from Co57");
		}
	}

	public static void main(String[] ignore) {
		// new StartPreprocessor().process("John loves Mary; John kisses Mary; Mary slaps John");
		Mark.say(BundleGenerator.getBundle("Trickster"));
	}

}

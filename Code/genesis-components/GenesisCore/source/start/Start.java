package start;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/*
 * Created on Apr 1, 2009 rlm -- added getTriples for igorm and dxh
 * @author phw
 */

import generator.Generator;
import translator.BasicTranslator;
import utils.*;

/*
 * Translates output of Start into a sequence of relations
 */
public class Start extends StartFoundation implements WiredBox, IParser {

	// Controlling switches

	private boolean debug = false;
	
	static public boolean DEBUG_SPEED = false;

	private static HashMap<String, ArrayList<String>> meaningRestrictionMap = new HashMap<String, ArrayList<String>>();

	// Parsing cache

	private static HashMap<String, String> startParserCache;

	// Web

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	RPCBox clientBox;

	WiredBox clientProxy;

	RPCBox serverBox;

	WiredBox serverProxy;

	// Stage directions

	private final String normalServer = "genesis";

	private final String experimentalServer = "e-genesis";

	// public static String currentServer = normalServer;

	public static final String LEFT = "First perspective";

	public static final String RIGHT = "Second perspective";

	public static final String BOTH = "Both perspectives";

	public static final String NEITHER = "Neither perspective";

	public static final String STOP = "Stop";

	public static final String PAUSE = "Pause";

	// Ports

	public static final String SELF = "self port";

	public static final String TRIPLES = "tripple port";

	public static final String PERSONA = "persona port";

	// Continue

	public final static String START_VIEWER_PORT = "start viewer port";

	// public final static String STORY_MODE = "use-kb&pi=instances";

	// public final static String STORY_MODE = "use-kb&dg=no";

	public final static String STORY_MODE = "yes";

	public final static String SENTENCE_MODE = "no";

	public static String STAGE_DIRECTION_PORT = "stage direction port";

	private String mode = SENTENCE_MODE;

	public final static String SENTENCE = "sentence";

	public final static String MODE = "change-mode";

	public final static String PARSE = "parse";

	public final static String TAP = "tap for experiments";

	public final static String TEST_PARSE = "test parse";

	private String name;

	private HashMap<String, Entity> thingMap;

	private HashMap<String, Relation> relationMap;

	private HashMap<String, String> nameSet;

	private HashMap<String, Entity> sessionMap;

	private int dummyIndex = 0;

	private static Start start = null;

	UUID uuid = UUID.randomUUID();

	private String processedSentence;

	public static Start getStart() {
		if (start == null) {
			start = new Start();
		}
		return start;
	}

	public Start() {
		// Preserved as public because mental models want their own copy
		super();
		name = "Start connection";

		Connections.getPorts(this).addSignalProcessor(SENTENCE, this::process);
		Connections.getPorts(this).addSignalProcessor(MODE, this::setMode);
		Connections.getPorts(this).addSignalProcessor(PERSONA, this::testPersonaPort);

		// Web
		establishNetworkConnection();
		// createClient();
	}

	private void establishNetworkConnection() {
		if (!createClient()) {
			StartServerBox.getStartServerBox();
			createClient();
		}
	}

	private boolean createClient() {
			clientBox = PhraseFactory.getPhraseFactory().getClientBox();
		// clientProxy = Connections.subscribe(Constants.server, 5);
		// clientBox = (RPCBox) clientProxy;
		if (clientBox != null) {
			return true;
		}
		// Mark.err("Failed to create Start client");
		return false;
	}

	public void testPersonaPort(Object o) {
		Mark.say("Load persona from", o);
	}

	// public void setMode(String mode) {
	// this.mode = mode;
	// // Mark.say("Mode set to", mode);
	// }

	public void setMode(Object input) {
		// Mark.say("setting mode to", input);
		if (!(input instanceof String)) {
			return;
		}
		else if (!(input == STORY_MODE) && !(input == SENTENCE_MODE)) {
			return;
		}
		// Mark.say("Setting mode to", input);
		this.mode = ((String) input);
	}

	// private void setStoryMode() {
	// setMode(Start.STORY_MODE);
	// }
	//
	// private void setSentenceMode() {
	// setMode(SENTENCE_MODE);
	// }

	// -------------- Z: I don't think this is used in parsing
	public void process(Object input) {
		boolean debug = false;
//		Mark.say("Start.process", input);
		String marker = null;
		if (input instanceof BetterSignal) {
			marker = ((BetterSignal) input).get(0, String.class);
			input = ((BetterSignal) input).get(1, String.class);
			Mark.say("It is a BetterSignal!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", marker, input);
			Mark.say("Process STAGE_DIR: ", input);
		}
		if (!(input instanceof String)) {
			Mark.err("Bad input is a", input.getClass());
			return;
		}

		String string = conditionString((String) input);

		Sequence result = parse(string);

		Mark.say(debug, "Parse results\n>>> ", string, "\n>>>  ", result);

		// Mark.say("Sequence: ", result);

		if (marker == null && Switch.activateExperimentalParser.isSelected()) {
			Connections.getPorts(this).transmit(TAP, new BetterSignal(string, result));
		}


		if (marker == null) {
			Connections.getPorts(this).transmit(PARSE, result);
		}
		else {
			Connections.getPorts(this).transmit(PARSE, new BetterSignal(marker, result));
		}
		if (Switch.showStartProcessingDetails.isSelected()) {
			Connections.getPorts(this).transmit(START_VIEWER_PORT, "<hr/>");
		}
	}

	public Sequence processForTestor(Object input) {
		// Mark.say("Start.process", input);
		String marker = null;
		if (input instanceof BetterSignal) {
			marker = ((BetterSignal) input).get(0, String.class);
			input = ((BetterSignal) input).get(1, String.class);
			Mark.say("Process STAGE_DIR: ", input);
		}
		if (!(input instanceof String)) {
			Mark.err("Bad input is a", input.getClass());
			return null;
		}
		String string = conditionString((String) input);
		Sequence normalResult = null;
		Sequence experimentalResult = null;
		Sequence transmittedResult = null;
		String normalString = "";
		String testString = "";

		// In any case, run the normal way

		transmittedResult = normalResult;
		if (Switch.useStartBeta.isSelected()) {
			experimentalResult = parse(string);
			transmittedResult = experimentalResult;
			testExperimentalStart(string, testString, normalString);
		}
		else {
			normalResult = parse(string);
			transmittedResult = normalResult;
		}
		return transmittedResult;
	}

	private boolean testExperimentalStart(String input, String testString, String normalString) {
		if (testString.equals(normalString)) {
			// Mark.say("Test results are same for", input);
			return false;
		}
		else {
			Mark.err("Difference noted on", input);
		}
		int indexL = 0;
		while ((indexL = normalString.indexOf('(', indexL)) >= 0) {
			int indexR = normalString.indexOf(')', indexL);
			String test = normalString.substring(indexL, indexR + 1);
			if (testString.indexOf(test) < 0) {
				Mark.err("Current has:     ", test);
			}
			indexL = indexR;
		}
		while ((indexL = testString.indexOf('(', indexL)) >= 0) {
			int indexR = testString.indexOf(')', indexL);
			String test = testString.substring(indexL, indexR + 1);
			if (normalString.indexOf(test) < 0) {
				Mark.err("Experimental has:", test);
			}
			indexL = indexR;
		}
		return true;
	}

	// private boolean processStageDirections(String string) {
	// Mark.say("Processing stage direction", string);
	// if (Markers.FIRST_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, NEITHER);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, LEFT);
	// return true;
	// }
	// else if (Markers.SECOND_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, NEITHER);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, RIGHT);
	// return true;
	// }
	// else if (Markers.BOTH_PERSPECTIVES_TEXT.equalsIgnoreCase(string)) {
	// Mark.say("B");
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, NEITHER);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, LEFT);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, RIGHT);
	// return true;
	// }
	// else if (Markers.NEITHER_PERSPECTIVE_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, NEITHER);
	// return true;
	// }
	// else if (Markers.SHOW_FIRST_PERSPECTIVE.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_LEFT);
	// return true;
	// }
	// else if (Markers.SHOW_SECOND_PERSPECTIVE.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_RIGHT);
	// return true;
	// }
	// else if (Markers.SHOW_BOTH_PERSPECTIVES.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, WiredSplitPane.SHOW_BOTH);
	// return true;
	// }
	// else if (Markers.CLEAR_STORY_MEMORY_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, Markers.RESET);
	// return true;
	// }
	// else if (Markers.SWITCH_TO_CONCEPT_REPORTING.equalsIgnoreCase(string)) {
	// Mark.say("Switching to reflective knowledge tab");
	// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Concept knowledge");
	// Connections.getPorts(this).transmit(SELF, Start.SENTENCE_MODE);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, "Concept knowledge");
	// return true;
	// }
	// else if (Markers.SWITCH_TO_COMMONSENSE_REPORTING.equalsIgnoreCase(string)) {
	// Mark.say("Switching to commonsense knowledge tab");
	// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Commonsense knowledge");
	// Connections.getPorts(this).transmit(SELF, Start.SENTENCE_MODE);
	// return true;
	// }
	// else if (Markers.START_GENERAL_KNOWLEDGE_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, TabbedTextViewer.SILENCE);
	// Connections.getPorts(this).transmit(SELF, Start.SENTENCE_MODE);
	// return true;
	// }
	// else if (string.startsWith(Markers.PAUSE_TEXT)) {
	// String minis = string.substring(Markers.PAUSE_TEXT.length()).trim();
	// if (minis.isEmpty()) {
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, PAUSE);
	// }
	// else {
	// Talker.getTalker().sleep(minis);
	// }
	// return true;
	// }
	// else if (Markers.THE_END_TEXT.equalsIgnoreCase(string)) {
	// Connections.getPorts(this).transmit(SELF, Start.SENTENCE_MODE);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, Markers.THE_END_TEXT);
	// Connections.getPorts(this).transmit(STAGE_DIRECTION_PORT, PAUSE);
	// return true;
	// }
	// else if (string.startsWith(Markers.PERSONA_MARKER)) {
	// Connections.getPorts(this).transmit(PERSONA, string.substring(Markers.PERSONA_MARKER.length()).trim());
	// return true;
	// }
	// else if (string.startsWith(Markers.START_STORY_TEXT)) {
	// Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Story");
	// }
	// return false;
	// }

	private String conditionString(String s) {
		StringBuffer buffer = new StringBuffer(s.trim());

		while (".?".indexOf(buffer.charAt(buffer.length() - 1)) >= 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		int index;
		while ((index = buffer.indexOf("\n")) >= 0) {
			buffer.replace(index, index + 1, " ");
		}
		while ((index = buffer.indexOf("  ")) >= 0) {
			buffer.replace(index, index + 1, "");
		}
		return buffer.toString();
	}


	// --------------------------------
	// 
	// Z: output all sequences of triples got from START, including syntactic ones
	public Sequence parse(String sentence) {

		// Z: don't know what's going on here
		// Use if experimental START prevents loading.
		// Switch.testStartBeta.setSelected(false);
		if (mode != Start.STORY_MODE) {
			clearLocalTripleMaps();
		}
		else {
			// Have to clear this; otherwise START's partial dereferencing goes
			// forward and screws up translation.
			getRelationMap().clear();
		}
		Mark.say(DEBUG_SPEED, "before process Sentence: " + sentence);
		
		
		
		// Z: processedSentence contains the triples
		processedSentence = processSentence(sentence);
		Mark.say(DEBUG_SPEED, "after process Sentence: " + processedSentence);
		if (processedSentence == null || processedSentence.trim().isEmpty()) {
			Mark.err("No Start result for " + sentence);
			return new Sequence();
		}

		// Z: result contains the sequence of entities
		Sequence result = processTripples(processedSentence);
		if (result.getElements().isEmpty()) {
			Mark.err("Start parser failed produce from tripples " + processedSentence);
			return null;
		}
		else {
//			Mark.say("Successfully parsed", sentence, result);
			return result;
		}
	}
	
	// --------------------------------
	// turn triples from START output string form into Triple form
	public ArrayList<Triple> getTriples(String string) {
		StringBuffer buffer = new StringBuffer(string);
		int start;
		int end;
		// Form triples
		ArrayList<Triple> triples = new ArrayList<Triple>();
		while ((start = buffer.indexOf("[")) >= 0) {
			++start;
			end = buffer.indexOf("]", start);
			triples.add(new Triple(buffer.substring(start, end)));
			buffer.delete(0, end + 1);
		}
		// Temporary, deals with lack of indexes on proper names
		processGerunds(triples);
		processProperNames(triples);
		processIdentifiers(triples);
		return triples;
	}

	// --------------------
	// 
	// Z: Main function to create Entity, Relation, and Sequence
	// 	all entities and relations are in the thingMap and relationMap
	// 
	public Sequence processTripples(String string) {
		
		debug = false;
		
		// Mark.say("Triples in Start:", triples);
		Sequence sequence = new Sequence(Markers.SEMANTIC_INTERPRETATION);
		ArrayList<Triple> triples = getTriples(string);
		Connections.getPorts(this).transmit(TRIPLES, triples);
		
		// Make sure that things are added to thingMap and relations are added to relationMap
		for (Triple t : triples) {
			Mark.say(Z.START_DEBUG, "Triple:", t);
			
			String firstString = t.getFirst();
			recordThing(firstString);
			
			String secondString = t.getSecond();
			recordRelation(secondString);
			// relationMap looks like {get+1=(rel get (ent null-105) (ent null-105))}
			
			String thirdString = t.getThird();
			recordThing(thirdString);
			
			if (thingMap != null && relationMap != null) {
				
			}
		}

		// From all entities and relations created, nest them to form sequences
		for (Triple t : triples) {
			String firstString = t.getFirst();
			String secondString = t.getSecond();
			String thirdString = t.getThird();

			// The first thing can be a relation or a thing
			Entity t1 = getRelation(firstString);
			if (t1 == null) {
				t1 = getThing(firstString);
			}
			
			// The second thing can only be a relation
			Entity t2 = getRelation(secondString);
			
			// The third thing can be a relation or a thing
			Entity t3 = getRelation(thirdString);
			if (t3 == null) {
				t3 = getThing(thirdString);
			}
			
			// the triples take <subject relation object> form
			t2.setSubject(t1);
			t2.setObject(t3);
			
			// add relation to sequence
			sequence.addElement(t2);
		
			Mark.say(Z.START_DEBUG, "Map:", t, "       ",  t2.asString());
		}

		return sequence;
	}

	private String extractTriples(String string) {
		StringBuffer result = new StringBuffer();
		StringBuffer buffer = new StringBuffer(string);
		int start;
		int end;
		while ((start = buffer.indexOf("[")) >= 0) {
			end = buffer.indexOf("]", start);
			result.append(buffer.substring(start, end + 1) + "\n");
			buffer.delete(0, end + 1);
		}
		return result.toString();
	}

	private void recordRelation(String string) {
		Relation secondThing = getRelationMap().get(string);
		if (secondThing == null) {
			getRelationMap().put(string, makeRelation(strip(string)));
		}
	}

	private void recordThing(String string) {
		// See if special case, no index from start, zero index here
		if (string.endsWith("-0")) {
			Entity t = getSessionMap().get(string);
			if (t == null) {
				// If not, put it there
				Entity x = makeThing(string);
				x.setNameSuffix("-0");
				getThingMap().put(string, x);
			}
		}
		else {
			
			// Z: all the entities are in the getThingMap()
			Entity t = getThingMap().get(string);
			if (t == null) {
				// If not, put it there
				Entity x = makeThing(string);
				getThingMap().put(string, x);
			}
		}
	}

	private void processProperNames(ArrayList<Triple> triples) {
		for (Triple triple : triples) {
			if (strip(triple.getSecond()).equals(Markers.IS_PROPER) && strip(triple.getThird()).equals(Markers.YES)) {
				String name = triple.getFirst();
				getNameSet().put(triple.getFirst(), name);
			}
		}
	}

	private void processGerunds(ArrayList<Triple> triples) {
		HashMap<String, String> gerunds = new HashMap<String, String>();
		for (Triple triple : triples) {
			if (triple.getSecond().equals("gerund_of")) {
				gerunds.put(triple.getFirst(), addIdentifierIfNone(triple.getFirst()));
			}
		}
		for (Triple triple : triples) {
			Object o = gerunds.get(triple.getFirst());
			if (o != null) {
				triple.setFirst((String) o);
			}
			o = gerunds.get(triple.getThird());
			if (o != null) {
				triple.setThird((String) o);
			}

		}
	}

	private void processIdentifiers(ArrayList<Triple> triples) {
		for (Triple triple : triples) {
			triple.setFirst(addIdentifierIfNone(triple.getFirst()));
			triple.setSecond(addIdentifierIfNone(triple.getSecond()));
			triple.setThird(addIdentifierIfNone(triple.getThird()));
		}
	}

	public static Entity makeThing(String x) {
		// Mark.say("Making thing for", x);

		if (x.equalsIgnoreCase("null")) {
			return Markers.NULL;
		}
		String word = strip(x);

		Entity thing;

		// Special case code for combinations returned from START with _
		// Treat words before final _ as adjectives

		if (Switch.splitNamesWithUnderscores.isSelected()) {
			String parts[] = word.split("_");
			int length = parts.length;
			if (length > 1) {
				word = parts[length - 1];
				thing = new Entity(word);
				for (int i = 0; i < length - 1; ++i) {
					thing.addFeature(parts[i]);
				}
			}
			else {
				thing = new Entity(word);
			}
		}
		else {
			thing = new Entity(word);
		}


		Bundle bundle = null;
		
		// if word is an integer
		try {
			Integer.parseInt(word);
			bundle = new Bundle();
			Thread thread = new Thread();
			thread.add("number");
			thread.add("integer");
			thread.add(word);
			bundle.add(thread);

		}
		
		// if word is just a word, get bundle from WordNet
		catch (NumberFormatException e) {
			bundle = restrict(word, BundleGenerator.getBundle(word));
		}
		if (bundle != null && bundle.size() > 0) {
			// Mark.say("Bundle is", bundle);
			// Mark.say("Thing is", thing.asString());
			thing.setBundle(bundle);
			// Mark.say("Thing", thing);
		}
		return thing;
	}

	public static Relation makeRelation(String word, Entity s, Entity o) {
		Relation r = makeRelation(word);
		r.setSubject(s);
		r.setObject(o);
		return r;
	}

	private static Relation makeRelation(String word) {
		Relation relation = new Relation(strip(word), Markers.NULL, Markers.NULL);
		Bundle bundle = restrict(word, BundleGenerator.getBundle(word));
		if (bundle != null && bundle.size() > 0) {
			relation.setBundle(bundle);
		}
		return relation;
	}

	int identifier = 0;

	private String addIdentifierIfNone(String s) {
		if (s.equalsIgnoreCase("null")) {
			return s;
		}
		String name = getNameSet().get(s);
		if (name != null) {
			Mark.say(debug, "It's a name", s, name);
			return name;
		}
		int index = Math.max(s.lastIndexOf('-'), s.lastIndexOf('+'));
		// if (index < 0) {
		// index = s.lastIndexOf('+');
		// }
		// if (index < 0) {
		// return s + "-" + dummyIndex++;
		// }
		String candidate = s.substring(index + 1);
		try {
			Integer.parseInt(candidate);
			return s;
		}
		catch (NumberFormatException e) {
			// Mark.say("Candidate failed,", candidate);
			// return s + "-" + dummyIndex++;
			return s + "-" + ++identifier;
		}
	}

	public static String strip(String s) {
		int index = s.lastIndexOf('-');
		index = Math.max(index, s.lastIndexOf('+'));
		if (index < 0) {
			return s;
		}
		String candidate = s.substring(index + 1);
		try {
			Integer.parseInt(candidate);
		}
		catch (NumberFormatException e) {
			return s;
		}
		String result = s.substring(0, index);
		return result;
	}

	public static String suffix(String s) {
		int index = s.lastIndexOf('-');
		index = Math.max(index, s.lastIndexOf('+'));
		if (index < 0) {
			return null;
		}
		return s.substring(index + 1);
	}

	public void clearLocalTripleMaps() {
		Mark.red(Z.START_DEBUG, getThingMap());
		Mark.red(Z.START_DEBUG, getRelationMap());
		Mark.red(Z.START_DEBUG, getNameSet());
		Mark.red(Z.START_DEBUG, meaningRestrictionMap);
		
		getThingMap().clear();
		getRelationMap().clear();
		getNameSet().clear();
		meaningRestrictionMap.clear();
	}

	public String clearStartReferences() {
		// Mark.say("Clearing start dereferencing memory");
		String probe = "query=(flush-lf-kpartition)&action=query&server=" + getServer();
		StringBuffer buffer = processProbe(probe);
		if (buffer.length() == 0 || !getThingMap().isEmpty()) {
			Mark.err("Start.clearStartReferences failed");
		}
		return probe.toString();
	}

	public static boolean isExperimentalStart() {
		if (Switch.useStartBeta.isSelected()) {
			return true;
		}
		return false;
	}

	public String getServer() {
		Mark.say(DEBUG_SPEED, "Before geting Server Server");
		return getNormalServer();
	}

	public String getNormalServer() {
		return normalServer;
	}

	public String getExperimentalServer() {
		return experimentalServer;
	}

	public String processSentence(String sentence) {
		// Mark.say("Processing sentence", sentence);
		String s = prepareSentence(sentence);
		s = escapeQuotesHack(s);  // added by Jennifer
		// First, check cache

		Mark.say(DEBUG_SPEED, "Before searching cache");
		if (Switch.useStartCache.isSelected() && !Switch.useStartBeta.isSelected()) {
			String result = processViaCache(s);
			Mark.say(DEBUG_SPEED, "After searching cache");
			if (result != null && !result.trim().isEmpty()) {
				return result;
			}
		}

		if (mode != Start.STORY_MODE) {
			Mark.say(DEBUG_SPEED, "Before clearing references");
			clearStartReferences();
			Mark.say(DEBUG_SPEED, "After clearing references");
		}
		Mark.say(DEBUG_SPEED, "Before processing with Start");
		String result = processWithStart(s);
		Mark.say(DEBUG_SPEED, "After processing with Start");

		if (result == null || result.trim().isEmpty()) {
			Mark.err("No Start result in processSentence for " + sentence);
			return "";
		}

		// Put result in cache whether using it or not
		Mark.say(DEBUG_SPEED, "Before getting Start Parser Cache");
		getStartParserCache().put(s, result);
		Mark.say(DEBUG_SPEED, "After getting Start Parser Cache");

		return result;
	}

	public String processWithStart(String sentence) {
		String result = null;

		// First, check for testing mode
		try {
			turnStartLightOn();
			Mark.say(DEBUG_SPEED, "Before processing directly");
			if (Switch.useStartBeta.isSelected()) {
				result = processDirectly(sentence, getExperimentalServer());
				Mark.say(DEBUG_SPEED, "Using experimental Server");
			}
			else {
				Mark.say(DEBUG_SPEED, "Before geting Server Server");
				result = processDirectly(sentence, getServer());
			}
			Mark.say(DEBUG_SPEED, "After processing directly");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			turnStartLightOff("Parse slow beyond description for " + sentence);
		}
		return result;

		// else {
		// NewTimer.startServerTimer.reset();
		// result = processViaServer(sentence, getServer());
		// NewTimer.startServerTimer.report(true, "Parse slow beyond description for " + sentence);
		// }
		// return result;
	}

	private boolean checkTranslations(Sequence nTranslation, Sequence eTranslation) {
		Mark.say("Checking");
		List<String> nStrings = nTranslation.stream().map(x -> x.asStringSansIndexes()).collect(Collectors.toList());
		List<String> eStrings = eTranslation.stream().map(x -> x.asStringSansIndexes()).collect(Collectors.toList());
		for (String s : nStrings) {
			if (!eStrings.contains(s)) {
				return false;
			}
		}
		for (String s : eStrings) {
			if (!nStrings.contains(s)) {
				return false;
			}
		}
		return true;
	}

	private String processViaCache(String sentence) {
		return getStartParserCache().get(sentence);
	}

	/*
	 * Note that these not use cache and does not offer a server option. Viewed as a rapid prototype. Considered ok
	 * because rarely used. One stated, permanent until START is rebooted
	 */
	public void processName(String request) {
		processName(request, "neuter");
	}

	public void processName(String request, String gender) {
		boolean debug = false;
		Mark.say(debug, "processName request", request);
		request = quoteArguments(request);
		// Mark.say("Defining noun", request);
		// Mark.say("Processing name", request);
		String header = "server=" + getServer() + "&action=add-word&query=";
		String content = "(noun " + request + " nil " + gender + " t nil)";
		Mark.say(debug, "Content value is", content);
		String trailer = "";
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(content, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String probe = header + encodedString + trailer;
		// Mark.say("Probe is", probe);
		processProbe(probe);
		Mark.say(debug, "Name probe is:", probe);
		Mark.say(debug, "Name processed directly via:", getServer());
	}
	
	// Work-around hack for START quote issue added on 4/22/20 by jmadiedo. Can
	// remove once START issue fixed (ask Sue)
	public String escapeQuotesHack(String sentence) {
		String result = sentence.replace("\"", "\\\"");
		result = result.replace("'", "\\'");
		return result;
	}

	private String quoteArguments(String s) {
		String[] tokens = s.split("\\s+");
		String result = "";
		for (String t : tokens) {
			if (t.startsWith("\"")) {
				t = t.substring(1);
			}
			if (t.endsWith("\"")) {
				int l = t.length();
				t = t.substring(0, l - 1);
			}
			// if (t.equals("t") || t.equals("nil") || t.startsWith("(") || t.endsWith(")")) {
			// result += t + " ";
			// }
			// else {
			// result = "\"" + t + "\" ";
			// }
			result += t + " ";
		}
		return escapeQuotesHack("\"" + result.trim() + "\"");
//		return "\"" + result.trim() + "\"";
	}

	/*
	 * Note that this does not use cache and does not offer a server option. Viewed as a rapid prototype. Considered ok
	 * because rarely used. See START documentation for parameters
	 */
	public void processVerb(String request) {
		boolean debug = false;
		request = quoteArguments(request);
		// Mark.say("Processing name", request);
		String header = "server=" + getServer() + "&action=add-word&query=";
		String content = "(verb " + request + ")";
		Mark.say(debug, "Content value is", content);
		String[] tokens = content.split("\\s+");
		Mark.say("Attempting to defineverb", tokens[1]);
		String trailer = "";
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(content, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String probe = header + encodedString + trailer;
		processProbe(probe);
		// Also have to tell Genesis that this is an action
		String label = tokens[1];
		label = label.substring(1, label.length() - 1);
		Thread thread = new Thread();
		thread.add(Markers.ACTION_MARKER);
		thread.add(label);
		Bundle bundle = new Bundle(thread);
		BundleGenerator.setBundle(label, bundle);
		Mark.say(debug, "Verb probe is:", probe);
		Mark.say(debug, "Verb processed directly via:", getServer());
	}

	/*
	 * Note that this does not use cache and does not offer a server option. Viewed as a rapid prototype. Considered ok
	 * because rarely used.
	 */
	public void processCategory(String request, String category) {
		boolean debug = true;
		// Mark.say("Processing name", request);
		String header = "server=" + getServer() + "&action=add-word&query=";
		String content = "(" + category + "\"" + request + "\")";
		String trailer = "";
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(content, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String probe = header + encodedString + trailer;
		// Mark.say("Probe is", probe);
		processProbe(probe);
		Mark.say(debug, "Name probe is:", probe);
		Mark.say(debug, "Name processed directly via:", getServer());
	}

	public String processDirectly(String sentence, String server) {
		
		// Mark.say("Working via", server, "on", sentence);
		boolean debug = false;
		String header = "query=";
		String result = null;

		// Use use-kb if you do not wish to flush; parse if you wish to flush.
		String trailer = "&action=parse&dg=no&kb=" + mode + "&server=" + server;
		String encodedString = "";
		
		if (Switch.showStartProcessingDetails.isSelected()) {
			Connections.getPorts(this).transmit(START_VIEWER_PORT, Html.normal(Html.bold("Working on: '" + sentence + "'.")));
		}
		
		try {
			encodedString = URLEncoder.encode(sentence, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String request = header + encodedString + trailer;

		try {


			Mark.say(DEBUG_SPEED, "Before process request");
			result = processParseRequest(request);
			Mark.say(DEBUG_SPEED, "After process request");

			Mark.say(debug, "\n>>> Request is", request);
			Mark.say(debug, "\n>>> Result is", result);

			if (request.trim().isEmpty()) {
				Mark.err("Unable to parse in processDirectly", request);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String processViaServer(String sentence, String mode) {
		String result = null;
		try {
			Object[] arguments = { sentence, mode, getServer(), uuid.toString() };
			Mark.say(".");

			Object value = clientBox.rpc("remoteParse", arguments);


			if (value != null) {
				result = (String) value;
			}
		}
		catch (Exception e) {
			NewTimer.startFailureTimer.reset();
			// Mark.say("Bug in effort to process sentence remotely! Give up and issue start request locally.");
			result = processDirectly(sentence, getServer());
			NewTimer.startServerTimer.report(true, "Remote parse failed on " + sentence);
		}
		return result;
	}

	/*
	 * Convert indexes to a canonical form, so as to benefit from cache
	 */
	private String remap(String triples) {
		HashMap<String, String> substitutions = new HashMap<String, String>();
		int index = 0;
		StringBuffer buffer = new StringBuffer(triples);
		// Look for +, see if in cache, if so, substitute, if not put in cache
		// and substitute
		int from = 0;
		while (true) {
			int nextPlus = buffer.indexOf("+", from);
			if (nextPlus < 0) {
				break;
			}
			// There is one, so find where number ends
			int nextSpace = buffer.indexOf(" ", nextPlus);
			int nextBracket = buffer.indexOf("]", nextPlus);
			int winner = 0;
			if (nextSpace >= 0 && nextBracket >= 0) {
				winner = Math.min(nextSpace, nextBracket);
			}
			else if (nextSpace >= 0) {
				winner = nextSpace;
			}
			else if (nextBracket >= 0) {
				winner = nextBracket;
			}
			else {
				Mark.err("Ooops, bug in Start.remap");
			}
			String key = buffer.substring(nextPlus, winner);
			String substitution = substitutions.get(key);
			if (substitution == null) {
				substitution = Integer.toString(index++);
				substitutions.put(key, substitution);
			}
			buffer.replace(nextPlus + 1, winner, substitution);
			from = nextPlus + 1;
		}
		return buffer.toString();
	}

	public String generate(String triples) {
		turnGeneratorLightOn();
		boolean debug = false;
		String remap = remap(triples);
		Mark.say(debug, "Generating from", remap);
		String result = condition(generateWithStart(remap), triples);
		turnGeneratorLightOff("Generation slow beyond description for " + result.trim());
		Mark.say(debug, result);
		return result;
	}

	private void turnGeneratorLightOff(String... messages) {

		if (Switch.useStartBeta.isSelected()) {
			NewTimer.generatorBetaTimer.report(true, messages);
		}
		else {
			NewTimer.generatorTimer.report(true, messages);

		}
	}

	private void turnGeneratorLightOn() {

		if (Switch.useStartBeta.isSelected()) {
			NewTimer.generatorBetaTimer.reset();
		}
		else {
			NewTimer.generatorTimer.reset();
			}

	}

	private void turnStartLightOff(String... messages) {
		NewTimer.startBetaTimer.turnOff();
		NewTimer.startDirectTimer.turnOff();
		if (Switch.useStartBeta.isSelected()) {
			NewTimer.startBetaTimer.report(true, messages);
		}
		else {
			NewTimer.startDirectTimer.report(true, messages);
			}

	}

	private void turnStartLightOn() {

		if (Switch.useStartBeta.isSelected()) {
			NewTimer.startBetaTimer.reset();
		}
		else {
			NewTimer.startDirectTimer.reset();
			}

	}
	
	public Boolean isSameString(String one, String two) {
		one = one.trim().replace("to be ", "").replace("being ", "").replace("that ", "");
		two = two.trim().replace("to be ", "").replace("being ", "").replace("that ", "");
		if (one.equals(two))
			return true;
		return false;
	}

	public String generateWithStart(String input) {
		String result = null;

		// Mark.say("Generating from:", input);
		
		// First, check for testing mode
		if (Switch.useStartBeta.isSelected()) {
			String normalResult = generateDirectly(input, getNormalServer()).trim();
			String experimentalResult = generateDirectly(input, getExperimentalServer()).trim();
			
			if (normalResult == null || !isSameString(experimentalResult,normalResult)) {
//				Mark.yellow(normalResult + normalResult.length());
//				Mark.night(experimentalResult + experimentalResult.length());
				Mark.err("Experimental generator test failed:\n  Genesis:", normalResult, "\ne-Genesis:", experimentalResult, "\nInput:", input, "\n");
			}
			return experimentalResult;
		}

		if (clientBox != null && Switch.useStartServer.isSelected() && !Switch.useStartBeta.isSelected()) {
			result = generateViaServer(input);
			if (result != null) {
				return result;
			}
		}
		result = generateDirectly(input, getServer());
		return result;
	}

	public String generateDirectly(String request, String server) {
		boolean debug = false;
		String result = null;
		String header = "server=" + server + "&te=formated-text&de=n&action=generate&query=";
		
		// ashmore is currently down
//		String header = "machine=ashmore&server=" + server + "&fg=no&te=formated-text&de=n&action=generate&query=";
		// Use use-kb if you do not wish to flush; parse if you wish to
		// flush.
		String trailer = "";
		String encodedString = "";
		if (Switch.showStartProcessingDetails.isSelected()) {
			Connections.getPorts(this).transmit(START_VIEWER_PORT, Html.normal(Html.bold("Working on: '" + request + "'.")));
		}
		try {
			encodedString = URLEncoder.encode(request, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String probe = header + encodedString + trailer;


//		Mark.say("Probe", probe);

		result = processGeneratorRequest(probe);


		Mark.say(debug, "Generated directly via:", server);
		Mark.say(debug, "Result:", probe, "\n", result);
		return result;
	}

	private String generateViaServer(String request) {
		boolean debug = false;
		String result = null;
		try {
			String id = "&uuid=" + uuid + "&fg=no";
			Mark.say(debug, "Call:", request + id);
			Object[] arguments = { request, id };
			// Mark.say("Request for server use of Start:", request);

			NewTimer.generatorServerTimer.reset();
			Object value = clientBox.rpc("remoteGenerate", arguments);
			NewTimer.generatorServerTimer.report(true, "Generator slow beyond description for " + result);

			if (value != null) {
				result = ((String) value).trim();
				Mark.say(debug, "Generated remotely using generator server:", result);
			}
		}
		catch (Exception e) {
			Mark.say("Bug in effort to process sentence with generator server!  Give up and issue start generator request locally.");
			result = generateDirectly(request, getServer());
		}
		return result;
	}

	private String condition(String result, String triples) {
		if (result != null && result.indexOf("One of the servers") >= 0) {
			// Mark.say("Mystery result is |", result, "|");
			// Mark.say("Triples were", triples);
			return "Unable to generate from" + result.toString();
		}
		return result;
	}

	private String prepareSentence(String s) {
		// Mark.say("Preparing sentence:", s);
		s = extractMeaningMarkers(s);
		StringBuffer buffer = new StringBuffer(s);
		int index;
		while ((index = buffer.indexOf(" '")) >= 0) {
			buffer.deleteCharAt(index);
		}
		while ((index = buffer.indexOf("leads to")) >= 0) {
			buffer.replace(index, index + "leads to".length(), "entails");
		}
		return buffer.toString();
	}

	// ---------------------------------------------------------------------------
	//
	// Z: extract the types of entities through brackets
	// "The robin (flew fly travel) to a (tree organism)"
	// 
	private String extractMeaningMarkers(String s) {
		meaningRestrictionMap.clear();
		StringBuffer buffer = new StringBuffer(s.trim());
		while (true) {
			int b = buffer.indexOf("(");
			int e = buffer.indexOf(")", b);
			if (b >= 0 && e > b) {
				String[] elements = buffer.substring(b + 1, e).split(" ");
				String word = elements[0];
				String root = elements[0];
				ArrayList<String> categories = new ArrayList<String>();
				for (int i = 1; i < elements.length; ++i) {
					categories.add(elements[i]);
				}
				if (elements.length >= 2) {
					root = elements[0];
					meaningRestrictionMap.put(root, categories);
				}
				buffer.replace(b, e + 1, word);
				Mark.say("Extracted meaning markers for", word, root, meaningRestrictionMap.get(root));
			}
			else {
				break;
			}
		}
		// Mark.say("HashMap:", this.meaningRestrictionMap);
		return buffer.toString();
	}

	public static Bundle restrict(String word, Bundle bundle) {
		boolean debug = false;
		ArrayList<String> restrictions = meaningRestrictionMap.get(word);
		// Mark.say("Entering restrict for", word, restrictions);
		if (restrictions != null) {
			Mark.say(debug, "Winning bundle for", word, "is", bundle);
			Bundle restrictedBundle = new Bundle();
			for (Thread thread : bundle) {
				boolean winner = true;
				for (String category : restrictions) {
					if (!thread.contains(category)) {
						winner = false;
						break;
					}
				}
				if (winner) {
					restrictedBundle.add(thread);
				}
			}
			int size = restrictedBundle.size();
			if (size == 0) {
				Mark.err("Bundle restricted by", restrictions, "has no threads");
			}
			else if (size == 1) {
				// Mark.say("Just one thread left", restrictedBundle.get(0).asString());
				return restrictedBundle;
			}
			else {
				// Mark.say("Bundle for", word, "restricted by", restrictions, "had", bundle.size(),
				// "initially, now has", size, "threads, using the first");
				// Mark.say("Candidates:", restrictedBundle);

				// Should be all left
				// Thread first = restrictedBundle.get(0);
				// restrictedBundle.clear();
				// restrictedBundle.add(first);
				return restrictedBundle;
			}
			// Mark.say("Ok, returning", restrictedBundle);

		}
		return bundle;
	}
	// ---------------------------------------------------------------------------
	
	
	
	
	
	

	@Override
	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	public Entity getThing(String key) {
		Entity t = getSessionMap().get(key);
		if (t != null) {
			return t;
		}
		return getThingMap().get(key);
	}

	public HashMap<String, Entity> getSessionMap() {
		if (sessionMap == null) {
			sessionMap = new HashMap<String, Entity>();
		}
		return sessionMap;
	}

	public HashMap<String, Entity> getThingMap() {
		if (thingMap == null) {
			thingMap = new HashMap<String, Entity>();
		}
		return thingMap;
	}

	public Relation getRelation(String key) {
		return getRelationMap().get(key);
	}

	public HashMap<String, Relation> getRelationMap() {
		if (relationMap == null) {
			relationMap = new HashMap<String, Relation>();
		}
		return relationMap;
	}

	public HashMap<String, String> getNameSet() {
		if (nameSet == null) {
			nameSet = new HashMap<String, String>();
		}
		return nameSet;
	}

	public static class Triple {
		public Triple(String first, String second, String third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		public String toString() {
			return "[" + first + " " + second + " " + third + "]";
		}

		public void setFirst(String first) {
			this.first = first;
		}

		public void setSecond(String second) {
			this.second = second;
		}

		public void setThird(String third) {
			this.third = third;
		}

		public Triple(String string) {
			String[] elements = string.split(" ");
			if (elements.length != 3) {
				Mark.err("Start.processTripple not handed a tripple: " + string);
			}
			else {
				// Mark.say("Tripple is " + string);
				// first = addIdentifierIfNone(elements[0]);
				// second = addIdentifierIfNone(elements[1]);
				// third = addIdentifierIfNone(elements[2]);
				first = elements[0];
				second = elements[1];
				third = elements[2];
			}
		}

		String first, second, third;

		public String getFirst() {
			return first;
		}

		public String getSecond() {
			return second;
		}

		public String getThird() {
			return third;
		}

	}

	public String getMode() {
		return mode;
	}

	public static HashMap<String, String> getStartParserCache() {
		if (startParserCache == null) {
			startParserCache = new HashMap<String, String>();
		}
		return startParserCache;
	}

	public static void setStartParserCache(HashMap<String, String> cache) {
		startParserCache = cache;
	}

	public static void purgeStartCache() {
		Mark.say("Purged Start parser cache,", System.getProperty("user.home") + File.separator + "parser.data", "of", getStartParserCache()
		        .size(), "items");
		getStartParserCache().clear();
		Mark.say("Purged Start generator cache,", System.getProperty("user.home") + File.separator + "generator.data", "of", Generator
		        .getStartGeneratorCache().size(), "items");
		getStartParserCache().clear();
		Generator.getStartGeneratorCache().clear();
		writeStartCaches();
	}

	public static void readStartCaches() {

		File file = new File(System.getProperty("user.home") + File.separator + "parser.data");
		if (!file.exists()) {
			Mark.say("No Start parser cache,", file, "to load");
			return;
		}
		Mark.say(true, "Loading Start parser cache");
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;
		try {
			fileInputStream = new FileInputStream(System.getProperty("user.home") + File.separator + "parser.data");
			objectInputStream = new ObjectInputStream(fileInputStream);
			Object object = objectInputStream.readObject();
			if (object != null) {
				setStartParserCache((HashMap<String, String>) object);

			}
			objectInputStream.close();
			fileInputStream.close();
		}
		catch (Exception e) {
			Mark.say("Start parser cache could not be read, sticking with existing cache");
		}

		file = new File(System.getProperty("user.home") + File.separator + "generator.data");
		if (!file.exists()) {
			Mark.say("No Start generator cache,", file, "to load");
			return;
		}
		Mark.say(true, "Loading Start generator cache");
		try {
			fileInputStream = new FileInputStream(System.getProperty("user.home") + File.separator + "generator.data");
			objectInputStream = new ObjectInputStream(fileInputStream);
			Object object = objectInputStream.readObject();
			if (object != null) {
				Generator.setStartGeneratorCache((HashMap<String, String>) object);
				Mark.say(true, "Number of generator items read: " + Generator.getStartGeneratorCache().size());
			}
			objectInputStream.close();
			fileInputStream.close();
		}
		catch (Exception e) {
			Mark.say("Start generator cache could not be read, sticking with existing cache");
		}
	}

	public static void writeStartCaches() {
		Mark.say(true, "Writing Start cache");
		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(System.getProperty("user.home") + File.separator + "parser.data");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(getStartParserCache());
			objectOutputStream.close();

			fileOutputStream = new FileOutputStream(System.getProperty("user.home") + File.separator + "generator.data");
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(Generator.getStartGeneratorCache());
			objectOutputStream.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Mark.say("Number of Start parser items written:", getStartParserCache().size());
		Mark.say("Number of Start generator items written:", Generator.getStartGeneratorCache().size());
	}

	public String getProcessedSentence() {
		if (processedSentence == null) {
			processedSentence = "";
		}
		return processedSentence;
	}
	
	public static ArrayList getRawStartOutput (String sentence) {
		return getStart().getTriples(getStart().processSentence(sentence));
	}

	public static void main(String[] ignore) throws Exception {
		Mark.say("Starting");
		Mark.say("Triples:", Start.getRawStartOutput("A bird flew to a tree."));
	}

}

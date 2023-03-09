package consciousness;

import java.io.File;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import com.ascent.gui.frame.ABasicFrame;

import connections.Connections;
import connections.signals.BetterSignal;
import constants.*;
import edu.stanford.nlp.util.logging.Color;
import expert.QuestionExpert;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import genesis.*;
import gui.*;
import matchers.*;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/**
 * Many auxiliary methods moved to superclass
 * <p>
 * When created, intention was to lay out various hierarchies and connections among them following work of Langley et
 * al.
 * <p>
 * Created on Jan 4, 2016
 *
 * @author phw
 */

public class ProblemSolver extends ProblemSolverAuxiliaries {

	public static final int PROBLEM = 0, SOLUTION = 1, INTENTION = 2, CONDITION = 3, METHOD = 4, RESULT = 5, COMMENT = 6, REQUEST = 7;

	public static final String NODE_TYPE = "problem solver node type";

	private LinkedHashMap<Entity, Sequence> problems = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> solutions = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> intentions = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> conditions = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> methods = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> requests = new LinkedHashMap<>();

	private LinkedHashMap<Entity, Sequence> results = new LinkedHashMap<>();

	public MentalModel reader = new MentalModel("Reader");

	public MentalModel mm;
	
	public static Entity instantiatedAction;  // for failing an approach when more than one instance is provided in instantiation in approach 

	// These ports for external checking


	public static final String TO_EXTERNAL_CHECKER = "To external checker";

	// No longer used
	// public static String FROM_EXTERNAL_CHECKER = "From external checker";

	// Wire this way

	// Connections.wire(TO_EXTERNAL_CHECKER, <this problem solver>, <External checker>);

	// Connections.wire(<External checker>, FROM_EXTERNAL_CHECKER, <this problem solver>);

	// This local variable captures result of external checking

	// No longer created here
	// BetterSignal externalCheckerResult = null;

	// This list is used to prevent looping

	private List<Entity> activeProblems = new ArrayList<>();

	public ProblemSolver(String problemSolvingStories) {
		super();

		// Needed to prevent trying to display elements for which there is no English

		Connections.disconnect(reader.getStoryProcessor(), ElaborationView.STORY, reader.getElaborationView());
		Connections.getPorts(this).addSignalProcessor(SOLVER_INPUT_PORT, this::processInput);
		textColor = "blue";
		if (problems.isEmpty()) {
			Mark.say("Evidently have not readstories from a file; reading now from\n", problemSolvingStories);
			readData(problemSolvingStories);
		}
		
		// No longer used
		// Connections.getPorts(this).addSignalProcessor(FROM_EXTERNAL_CHECKER, this::processExternalCheck);

	}

	public ProblemSolver(MentalModel m) {
		this();
		if (Markers.FIRST_PERSPECTIVE_TEXT.equals(m.getName())) {
			setTextColor("black");
		}
		else {
			setTextColor("blue");
		}
		mm = m;
	}

	private ProblemSolver() {
		super();
		// Needed to prevent trying to display elements for which there is no English

		Connections.disconnect(reader.getStoryProcessor(), ElaborationView.STORY, reader.getElaborationView());
		Connections.getPorts(this).addSignalProcessor(SOLVER_INPUT_PORT, this::processInput);
		textColor = "blue";

		// No longer used
		// Connections.getPorts(this).addSignalProcessor(FROM_EXTERNAL_CHECKER, this::processExternalCheck);

	}

	// No longer used.
	// public void processExternalCheck(Object input) {
	// if (input instanceof BetterSignal) {
	// BetterSignal bs = (BetterSignal) input;
	// externalCheckerResult = bs;
	// }
	// }

	public void processInput(Object input){
		Mark.say("Asking PHW's problem solver", input);

		if (Switch.deplyNovice.isSelected()) {
			Mark.say("Handing problem directly to novice");
			getNovice().processInput(input, "Problem solving stories.txt");
		}
		else {
			processInput(input, "Problem solving stories.txt");
		}
		// Legacy arrangement
		// clearData();
	}

	public BetterSignal processInput(Object input, String problemSolvingStories) {

		Mark.say("Calling PHW's problem solver", input);

		boolean debug = false;

		Connections.getPorts(this).transmit(COMMENTARY, TextViewer.CLEAR);

		Connections.getPorts(this).transmit(COMMENTARY, new BetterSignal(GenesisConstants.LEFT, Markers.COMMAND_TAB, TextViewer.CLEAR));

		Connections.getPorts(this).transmit(COMMANDS, new BetterSignal(CommandList.CLEAR));

		Mark.say(debug, "Entering ProblemSolver.processInput with", input);
		if (problemSolvingStories != null){
			if (problems.isEmpty()) {
				Mark.say("Evidently have not readstories from a file; reading now.");
				readData(problemSolvingStories);
				Mark.say("Data read from standard file");
			}
		}

		Translator.getTranslator().clearPatterns();

		if (input instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) input;

			if (bs.size() != 3) {
				Mark.err("Wrong number of arguments received by", this.getClass());
				return new BetterSignal(false);
			}


			Object name = bs.get(0, Object.class);

			Mark.say(debug, "Name", name);
			Entity focus = bs.get(1, Entity.class);
			Mark.say(debug, "Focus", focus);
			// Now supplied in constructor
			// MentalModel mm = bs.get(2, MentalModel.class);
			Mark.say(debug, "Mental model", mm);

			// This is so asking same question multiple times produces elaboration graph elements each time; otherwise,
			// multiple events would be eliminated.
			MentalModel i = mm.getI();
			StoryProcessor myStoryProcessor = i.getStoryProcessor();

			myStoryProcessor.addSceneMap();
			Mark.say(debug, "\n>>>  Added scene map for a question, now there are", myStoryProcessor.getSceneMapList().size());

			Mark.say(debug, "The question is:\n>>>  ", focus, "\n>>>  ", Generator.getGenerator().generate(focus));

			// if (!USE_ORIGINAL_WIRING) {


				Mark.say(debug, "Name is", name);
				Mark.say(debug, "Focus is", focus);

				if (focus.hasProperty(Markers.IMPERATIVE, true)) {
				Mark.say("Received command", focus);
				// return;
				}

				activeProblems.clear();
			BetterSignal solution = solve(0, focus, new Sequence("answer"), null, mm);

			Connections.getPorts(this).transmit(COMMANDS, new BetterSignal(CommandList.DONE));

			return solution;

			// else {
			// // Mark.say("Ready to work on", focus);
			// Entity translation = Transform.transformCauseToPathProblem(focus);
			// Problem problem = makeLeadsToProblem(translation);
			// boolean solution = processProblem(0, focus, problem, mm).isTrue();
			//
			// if (solution) {
			// Mark.say("Problem solved ;)");
			// }
			// else {
			// Mark.say("Could not find a solution ;(");
			// }
			// focus.toXML();
			// }
		}
		return new BetterSignal(false);
	}

	/**
	 * New attempt below
	 */
	private void readData(){
		readData("Problem solving stories.txt");
	}

	public void clearData() {
		Mark.say("Clearing all problem solving data");
		problems.clear();
		solutions.clear();
		intentions.clear();
		conditions.clear();
		methods.clear();
		requests.clear();
		results.clear();
		JustDoIt.clear();
		
	}

	public void readData(File stories) {
		readData(stories.getName());
		// boolean debug = true;
		// try {
		// Mark.say(debug, "Stories in", stories);
		// URL url = stories.toURI().toURL();
		// Mark.say(debug, "URL is", url.toExternalForm());
		// readData(url);
		// }
		// catch (MalformedURLException e) {
		// e.printStackTrace();
		// Mark.err("Bug in readData");
		// }
	}

	public void readData(String stories) {
		Mark.say("String argument to readData is", stories);
		String text = null;
		if (!Webstart.isWebStart()) {
			// New way
			text = Webstart.readTextFile(stories);
			Mark.say(false, "Read character, count is", text.length());
		}
		else {
			// Legacy
			URL url = ProblemSolver.class.getResource(stories);
			try {
				Mark.say("Reading data into", this.iAmExpert() ? "expert" : "novice", "from", url.toExternalForm());
				text = TextIO.readStringFromURL(url);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		readTextData(text);
	}


	public void readTextData(String text) {

		boolean debug = false;

		StoryProcessor sp = reader.getStoryProcessor();
		String problemStarter = "If the problem is";
		String questionStarter = "If the question is";
		String resultStarter = "If the result is";
		String consequenceStarter = "If the consequence is";
		String intentionStarter = "If the intention is";
		String conditionStarter = "If the condition is";
		String stepStarter = "If the step is";
		String methodStarter = "If the method is";
		String requestStarter = "If the request is";
		String prehandle = null;
		String handle = null;

		// Mark.say("Text is", text);
		processProblemSolvingKnowledgeText(text);

		text = Comments.dike(text);

		String[] sentences = text.split("\\.");
		Mark.say(debug, "Sentences", sentences.length);
		Sequence story = null;
		List<String> elements = new ArrayList<>();
		for (String x : sentences) {
				prehandle = handle;
				handle = x;
				if (x.startsWith("//")) {
					Mark.say("Comment:", x);
					continue;
				}
				String sentence = x.trim();
				if (sentence.startsWith(problemStarter) || sentence.startsWith(questionStarter)) {
					story = new Sequence("Problem");
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.startsWith(consequenceStarter) || sentence.startsWith(resultStarter)) {
					story = new Sequence("Result");
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.startsWith(intentionStarter)) {
					// Mark.say("Found intention starter", sentence);
					story = new Sequence("Intention");
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.startsWith(conditionStarter)) {
					story = new Sequence("Condition");
					specialize(sp, story, elements, sentence);
				}
				// Steps are just like conditions
				else if (sentence.startsWith(stepStarter)) {
					story = new Sequence("Condition");
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.startsWith(methodStarter)) {
					story = new Sequence("Method");
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.startsWith(requestStarter)) {
					story = new Sequence("Request");
					Mark.say(debug, "Found request", sentence);
					specialize(sp, story, elements, sentence);
				}
				else if (sentence.toLowerCase().equals("the end")) {
					Entity index = readBody(story, elements);
					Mark.say(debug, "\n>>>  Type/index", story.getType(), index);
					elements.stream().forEachOrdered(e -> Mark.say(debug, "Element", e));
					if (story.isA("Problem")) {
						// Mark.say("Adding problem descriptor\n", index, "\n", story);
						problems.put(index, story);
					}
					else if (story.isA("Result")) {
						results.put(index, story);
					}
					else if (story.isA("Intention")) {
						// Mark.say("Storing intention under", index);
						intentions.put(index, story);
						// Mark.say("Retriving", intentions.get(index));
						// Mark.say("Contained", intentions.containsKey(index));
					}
					else if (story.isA("Condition")) {
						conditions.put(index, story);
					}
					else if (story.isA("Method")) {
						methods.put(index, story);
					}
					else if (story.isA("Request")) {
						requests.put(index, story);
					}
					else {
						Mark.say("No interpretation for", story);
					}
					story = null;
					elements.clear();
				}
				else {
					// Ok, just read the sentence
					if (story == null && !sentence.isEmpty()) {
						Mark.say(debug, "Reading", sentence);
						Translator.getTranslator().internalize(sentence);
					}
					else {
						elements.add(sentence);
					}
			}
		}

		if (false) {
			Mark.say(">>>  ");
			Mark.say("Problems", problems.size());
			Mark.say("Results", results.size());
			Mark.say("Intentions", intentions.size());
			Mark.say("Conditions", conditions.size());
			Mark.say("Methods", methods.size());
			Mark.say("Requests", requests.size());

			problems.keySet().stream().forEach(k -> {
				Mark.say("Insight description:\n");
				problems.get(k).stream().forEach(e -> {
					Mark.say(e);
				});
			});

			intentions.keySet().stream().forEach(k -> {
				Mark.say("Approach description:\n");
				intentions.get(k).stream().forEach(e -> {
					Mark.say(e);
				});
			});

		}
	}



	private static void processProblemSolvingKnowledgeText(String text) {
		text = Comments.dike(text);
		// Mark.say("Text:\n", text);

	}

	private static void specialize(StoryProcessor sp, Sequence story, List<String> elements, String sentence) {
		sp.startStory();
		String quotedPart = extractQuotedString(sentence);
		elements.add("Specialty: " + quotedPart);
		story.addProperty("Signature", quotedPart);
	}

	private static String extractQuotedString(String sentence) {
		int start = sentence.indexOf('"');
		int end = sentence.lastIndexOf('"');
		if (start < 0 || end <= start) {
			Mark.err("Bad string argument in extractQuotedString\n|", sentence, "|");
		}
		return sentence.substring(start, end + 1);
	}

	/**
	 * This of course could be much more sophisticated, looking at story and other mental model considerations.
	 */
	private List<Sequence> findInsights(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in problems for", key);
		return findMatch(key, problems);
	}

	private List<Sequence> findResults(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in results for", key);
		return findMatch(key, results);
	}

	private List<Sequence> findApproches(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in intentions for", key);
		return findMatch(key, intentions);
	}

	private List<Sequence> findCheckers(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in conditions for", key);
		return findMatch(key, conditions);
	}

	private List<Sequence> findExecutors(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in methods for", key);
		return findMatch(key, methods);
	}

	private List<Sequence> findHelpers(Entity key, MentalModel mm) {
		Mark.say(false, "Looking in methods for", key);
		Mark.say(false, "Found", findMatch(key, requests).size());
		return findMatch(key, requests);
	}

	private List<Sequence> findMatch(Entity key, LinkedHashMap<Entity, Sequence> map) {
		return findMatch(false, key, map);
	}


	private List<Sequence> findMatch(boolean debug, Entity key, LinkedHashMap<Entity, Sequence> map) {
		Mark.say(debug, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Mark.say(debug, "The key is", key);
		List<Sequence> result = new ArrayList<>();
		TreeSet<Sequence> participants = new TreeSet<>(new Comparator<Sequence>() {
			@Override
			public int compare(Sequence l, Sequence m) {
				int a = countEntities(extractSpecialtyEntity(l));
				int b = countEntities(extractSpecialtyEntity(m));
				if (a > b) {
					return -1;
				}
				else if (a < b) {
					return 1;
				}
				else {
					return l.toString().compareTo(m.toString());
				}
			}
		});
		Mark.say(debug, "The map size is", map.size());
		for (Entity pattern : map.keySet()) {
			Mark.say(debug, "Checking\nPattern:", pattern);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pattern, key);
			Mark.say(debug, "Bindings", bindings);
			if (bindings != null) {
				Sequence value = map.get(pattern);
				Sequence substitution = (Sequence) (Substitutor.substitute(value, bindings));
				substitution.addProperty("Signature", map.get(pattern).getProperty("Signature"));
				
				// added by Zhutian on 20 Nov 2018 to preserve syntactic information of sentences after "Verify"
				//   without the following line, "you can find a battery" becomes "you find a battery"
				substitution.getElement(1).getSubject().setPropertyList(value.getElement(1).getSubject().getPropertyList());
//				Mark.night("================");
//				Mark.night(value.getElement(1).getSubject());
//				Z.understand(value.getElement(1).getSubject());
//				Mark.night(substitution.getElement(1).getSubject());
//				Z.understand(substitution.getElement(1).getSubject());				
				
				Mark.say(debug, "Raw:         ", value);
				Mark.say(debug, "Substitution:", substitution);
				result.add(substitution);
				

			}
		}
		Mark.say(debug, "Found", result.size(), "matches for", key);
		return result;
	}

	private List<Sequence> findMatchingPatterns(boolean debug, Entity key, LinkedHashMap<Entity, Sequence> map) {
		Mark.say(debug, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Mark.say(debug, "The key is", key);
		List<Sequence> result = new ArrayList<>();
		TreeSet<Sequence> participants = new TreeSet<>(new Comparator<Sequence>() {
			@Override
			public int compare(Sequence l, Sequence m) {
				int a = countEntities(extractSpecialtyEntity(l));
				int b = countEntities(extractSpecialtyEntity(m));
				if (a > b) {
					return -1;
				}
				else if (a < b) {
					return 1;
				}
				else {
					return l.toString().compareTo(m.toString());
				}
			}
		});
		Mark.say(debug, "The map size is", map.size());
		for (Entity pattern : map.keySet()) {
			Mark.say(debug, "Checking\nPattern:", pattern);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pattern, key);
			Mark.say(debug, "Bindings", bindings);
			if (bindings != null) {
				Sequence value = map.get(pattern);
				result.add(value);
			}
		}
		Mark.say(debug, result.isEmpty(), "Found", result.size(), "matches for", key);
		return result;
	}

	private List<Sequence> findInsightPatterns(Entity key) {
		Mark.say(false, "Looking in problems for", key);
		return findMatchingPatterns(key, problems);
	}

	private List<Sequence> findApproachPatterns(Entity key) {
		Mark.say(false, "Looking in problems for", key);
		return findMatchingPatterns(key, intentions);
	}

	private List<Sequence> findMatchingPatterns(Entity key, LinkedHashMap<Entity, Sequence> map) {
		return findMatchingPatterns(false, key, map);
	}


	private int countEntities(Entity x) {
		if (x.entityP()) {
			return 1;
		}
		else if (x.functionP()) {
			return 1 + countEntities(x.getSubject());
		}
		else if (x.relationP()) {
			return 1 + countEntities(x.getSubject());
		}
		else {
			// Must be sequence
			return 1 + x.stream().map(e -> countEntities(e)).reduce(0, (a, b) -> a + b);
		}
	}



	private Entity readBody(Sequence story, List<String> strings) {
		// Cannot do following; screws up question in reading via start; it tries to answer!
		// Start.getStart().setMode(Start.STORY_MODE);
		// Mark.say("Reading microstory");

		boolean debug = false;

		StoryProcessor sp = reader.getStoryProcessor();

		sp.startStory();

		strings.stream().forEachOrdered(s -> {
			// Mark.say("Working on", s);
			if (s.equals("The end")) {
			}
			else {
				Mark.say(debug, "Working on", s);
				Entity e = translateWithMarkup(s);
				if (e != null) {
					e.addProperty(Markers.SPECIAL, true);
					try {
						sp.injectElementWithDereference(e);
					}
					catch (Exception e1) {
						Mark.say("Unable to operate here");
						Mark.say("sp", sp);
						Mark.say("e", e);
						e1.printStackTrace();
					}
				}
				else {
					Mark.say(debug, "Bungled", s);
				}
			}

		});
		sp.stopStory();

		sp.getStory().stream().forEachOrdered(e -> story.addElement(e));

		// See above
		// Start.getStart().setMode(Start.SENTENCE_MODE);
		// Flushes story mode
		Entity index = null;
		for (Entity e : story.getElements()) {
			if (e.isA("specialty")) {
				index = e;
			}
			// Mark.say("Element:", e);
		}
		// For the present, only one key possible
		// Mark.say("Index", index);

		if (index == null) {
			Mark.say("\n\n\n>>>  Ooops, null index");
			story.stream().forEachOrdered(e -> Mark.say("Oops element", e));
			strings.stream().forEachOrdered(e -> Mark.say("String element", e));
		}

		if (index != null) {
			return index.getSubject();
		}
		return index;
	}

	private static Entity translateWithMarkup(String s) {
		Entity result = null;
		if (result == null) {
			result = constructRoleFrame("Solve", s);
		}
		if (result == null) {
			result = constructRoleFrame("Problem", s);
		}
		if (result == null) {
			result = constructRoleFrame("Question", s);
		}
		if (result == null) {
			result = constructRoleFrame("Contradiction", s);
		}
		if (result == null) {
			result = constructRoleFrame("Intention", s);
		}
		if (result == null) {
			result = constructRoleFrame("Specialty", s);
		}
		// Try = Intention
		if (result == null) {
			result = constructRoleFrame("Try", s);
		}
		// Legacy, Check = Condition
		if (result == null) {
			result = constructRoleFrame("Check", s);
		}
		if (result == null) {
			result = constructRoleFrame("Condition", s);
		}
		if (result == null) {
			result = constructRoleFrame("Step", s);
		}
		if (result == null) {
			result = constructRoleFrame("Instantiate", s);
		}
		if (result == null) {
			result = constructRoleFrame("Do", s);
		}
		if (result == null) {
			result = constructRoleFrame("Method", s);
		}
		if (result == null) {
			result = constructRoleFrame("Execute", s);
		}
		if (result == null) {
			result = constructRoleFrame("Verify", s);
		}
		if (result == null) {
			result = constructRoleFrame("Communicate", s);
		}
		if (result == null) {
			result = constructRoleFrame("Consequence", s);
		}
		if (result == null) {
			result = constructRoleFrame("Success", s);
		}
		if (result == null) {
			result = constructRoleFrame("Failure", s);
		}
		if (result == null) {
			result = constructRoleFrame("Request", s);
			// if (result != null) {
			// Mark.say("Result", result);
			// }
		}
		if (result == null) {
			// Mark.say("S is", s);
			// result = internalize(s);
		}
		return result;
	}


	private static Entity internalize(String s) {
		s = stripExternalQuotes(s);
		Entity result = checkForIdiom(s);
		if (result != null) {
			return result;
		}
		result = Translator.getTranslator().internalize(s);
		return result;
	}

	private static String stripExternalQuotes(String s) {
		s = s.trim();
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	/**
	 * Alas, this is needed because START is notoriously bad at dealing with sentences involving variables
	 */
	private static Entity checkForIdiom(String s) {
		if (false && s.toLowerCase().trim().equals("why did xx")) {
			Entity xx = Translator.getTranslator().internalize("xx is a variable").getObject();
			Entity result = new Function(Markers.WHY_QUESTION, xx);
			Mark.say("Idiomatic reading of\n", s, ":", result);
			return result;
		}
		else if (s.toLowerCase().trim().equals("explain why xx")) {
			Entity xx = Translator.getTranslator().internalize("xx is a variable").getObject();
			Entity result = new Relation(Markers.WHY_QUESTION, RoleFrames.makeRoleFrame(Markers.YOU, "explain"), xx);
			// Mark.say("Idiomatic reading of\n", s, ":", result);
			return result;
		}
		else {
			// Mark.say("|", s, "| is not an idiom");
		}
		return null;
	}

	private static Entity constructRoleFrame(String key, String s) {
		if (key != null && s.startsWith(key + ':')) {
			String x = s.substring(key.length() + 2);
			Entity e = internalize(x);
			return new Function(key.toLowerCase(), e);
		}
		return null;
	}

	/**
	 * solve uses a question to suggest insights.
	 */
	public BetterSignal solve(int level, Entity question, Sequence answer, Entity trigger, MentalModel mm) {
		if (level == 0) {
			// Need to start a new story. As of 30 Dec 2016, injection method for adding to the story does not get this
			// complex dereferencing right.
			mm.getI().getStoryProcessor().startStory();

		}

		boolean debug = false;
		Sequence s = new Sequence();
		s.addElement(new Function("specialty", question));
		traceIn(level, "problem", s);
		BetterSignal result = new BetterSignal(false);
		Sequence steps = constructStepSequence();

		// Used to prevent looping

		for (Entity active : activeProblems) {
			// Mark.say("Check out false statement in following line");
			if (false && StandardMatcher.getIdentityMatcher().match(question, active) != null) {
				Mark.say(true, "Looping!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				traceOut(level, "problem", s, result);
				return composeAnswerNode(RESULT, result, steps, question, s, mm);
			}
		}

		activeProblems.add(question);

		Entity thought = null;

		Mark.say(debug, "Question", question);

		if (question.hasProperty(Markers.IMPERATIVE, true)) {
			thought = iObey(question);
		}
		else {
			thought = iAsk(question);
		}

		thought.addProperty(NODE_TYPE, RESULT);

		if (trigger != null) {
			blurtInPlus(level, trigger, thought, mm);
		}
		else {
			blurtInPlus(level, question, thought, mm);
		}

		blurtMe(level, "Problem:", question);

		// Look for insights that connect problem to intentions or equivalent problem
		List<Sequence> insights = findInsights(question, mm);
		Mark.say(debug, "Found", insights.size(), "matching insights for\n>>>  ", question);

		for (Sequence insight : insights) {

			if (debug) {
				Mark.say("Insight is:");
				insight.stream().forEachOrdered(e -> {
					Mark.say("Element:", e);
				});
			}

			result = tryInsight(level + 1, question, insight, answer, thought, mm);

			if (result.isTrue()) {
				Mark.say(debug, "Succeeded");
				addStep(level, result.get(1, Entity.class), steps);
				break;
			}
			else {
				Mark.say(debug, "Failed");
				addStep(level, result.get(1, Entity.class), steps);
			}
		}

		// This block of code arranges for helper to ask expert
		// how to solve problem
		if (result.isFalse() && iAmHelper()) {
			boolean debugHelp = true;

			Entity request = makeRequest(question);
			blurtCommunication(level, request, "green");

			// ask(level, question, mm);
			List<Sequence> noviceKnowledge = findMatch(question, problems);
			List<Sequence> expertKnowledge = getExpert().findInsightPatterns(question);

			int originalSize = noviceKnowledge.size();

			expertKnowledge.stream().forEach(i -> {
				Entity key = i.getElements().get(0).getSubject();
				problems.put(key, i);
			});
			noviceKnowledge = findMatch(question, problems);
			int revisedKnowledgeSize = noviceKnowledge.size();
			answerRequest(level, expertKnowledge, question);

			boolean won = revisedKnowledgeSize > originalSize;

			if (won) {
				if (!Radio.realRobot.isSelected()) Sleep.pause(5);
				BetterSignal attempt = solve(level, question, answer, trigger, mm);
				if (attempt.isTrue()) {
					Entity advice = new Entity("advice");
					advice.addFeature("insight");
					Entity message = RoleFrames.makeRoleFrame("Helper", "exploit", "advice");
					message.addFeature(Markers.PAST);
					blurtCommunication(level, message, "orange");
					return attempt;
				}

			}
		}

		// Connections.getPorts(this).transmit(COMMENTARY, new BetterSignal(GenesisConstants.LEFT,
		// Markers.INTROSPECTION_TAB, Html.p(" ")));

		if (answer.getElements().size() > 0) {
			answer.stream().forEachOrdered(e -> {
				blurtOut(level, e, mm);
				blurtMe(level, "Answer:", e);
			});
		}
		else {
			Entity consequence = null;
			if (question.isA(Markers.QUESTION_MARKER)) {
				// It is a question
				consequence = prepareConsequence(result.isTrue(), iConclude(question.getSubject()));
			}
			else {
				// It is a command.
				consequence = prepareConsequence(result.isTrue(), iDid(question));
			}

			Mark.say(debug, "Consequence is", consequence);

			consequence.addProperty(NODE_TYPE, RESULT);

			blurtOutPlus(level, thought, consequence, mm);

			if (result.isTrue()) {
				blurtMe(level, "Problem solved:", RoleFrames.getObject(consequence));
			}

			else {
				blurtMe(level, "Problem unsolved:", RoleFrames.getObject(consequence));
			}

		}

		traceOut(level, "problem", s, result);

		Mark.say(debug, "\n>>>  Problem-solving episode is now complete");
		Mark.say(debug, "level: ", level);
		Mark.say(debug, "result: ", result, result.isTrue());
		Mark.say(debug, "question: ", question);
		BetterSignal bs = composeAnswerNode(RESULT, result, steps, question, s, mm);
		return bs;
	}

	private Entity makeRequest(Entity original) {
		Entity what = RoleFrames.makeRoleFrame(Markers.I, original.getType());
		RoleFrames.getRoles(original).stream().forEachOrdered(r -> {
			RoleFrames.addRole(what, r.getType(), r.getSubject());
		});
		Entity how = RoleFrames.makeIntoHowQuestion(what);

		// Entity advice = RoleFrames.makeIntoAdviceQuestion(what);

		return how;
	}

	private void answerRequest(int level, List<Sequence> expertApproaches, Entity question) {
		Mark.say("Entering new answerRequest");
		List<Sequence> instantiations = new ArrayList<>();
		for (Entity e : expertApproaches) {
			Entity x = e.get(0);
			Entity y = x.getSubject();
			Mark.say("Attempting match\n", y, "\n", question);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(y, question);
			Mark.say("Match result:", bindings);
			Entity i = Substitutor.substitute(e, bindings);
			Mark.say("Instantiation:", i);
			instantiations.add((Sequence) i);
		}
		answerRequest(level, instantiations);
	}

	private void answerRequest(int level, List<Sequence> expertApproaches) {
		Entity way = RoleFrames.makeRoleFrame("expert", "suggests", "steps");

		blurtCommunication(level, way, ":", "purple");

		expertApproaches.stream().forEach(i -> {
			i.stream().forEachOrdered(e -> {
				if (e.isA("specialty")) {
					// blurtCommunication(level, "Specialty", e.getSubject(), "purple");
				}
				else if (e.isA("step")) {
					blurtCommunication(level, "Step", e.getSubject(), "purple");
				}
				else if (e.isA("intention")) {
					blurtCommunication(level, "Intention", e.getSubject(), "purple");
				}
				else if (e.isA("condition")) {
					blurtCommunication(level, "Condition", e.getSubject(), "purple");
				}

				else if (e.isA("method")) {
					blurtCommunication(level, "Method", e.getSubject(), "purple");
				}
			});
		});
	}

	/**
	 * An insight is called via question; it hands off to an approach or an equivalent problem/question
	 */
	private BetterSignal tryInsight(int level, Entity question, Sequence insight, Sequence answer, Entity trigger, MentalModel mm) {
		boolean debug = false;

		traceIn(level, "insight", insight);

		BetterSignal result = new BetterSignal(false);
		Sequence steps = constructStepSequence();

		Entity thought = iThinkAbout(question);

		thought.addProperty(NODE_TYPE, PROBLEM);

		blurtInPlus(level, trigger, thought, mm);

		for (Entity insightElement : insight.getElements()) {
			// Look for approach that links intention to checks and method; done if any true
			if (intentionP(insightElement)) {
				List<Sequence> approaches = findApproches(insightElement.getSubject(), mm);

				for (Sequence approach : approaches) {

					if (debug) {
						Mark.say("Approach is:");
						insight.stream().forEachOrdered(e -> {
							Mark.say("Element:", e);
						});
					}

					result = tryApproach(level, insightElement.getSubject(), approach, answer, thought, mm);
					addStep(level, result.get(1, Entity.class), steps);
					if (result.isTrue()) {
						break;
					}
				}
				if (result.isTrue()) {
					break;
				}
			}
			// Try to solve equivalent problem; done if any solved
			else if (problemP(insightElement)) {
				Mark.say(debug, "Solving equivalent problem", generate(insightElement.getSubject()));
				result = solve(level, insightElement.getSubject(), answer, thought, mm);
				addStep(level, result.get(1, Entity.class), steps);
				if (result.isTrue()) {
					break;
				}
			}
			// Work in progress
			else if (contradictionP(insightElement)) {
				Mark.say(debug, "Solving contradicting problem", generate(insightElement.getSubject()));
				result = solve(level, insightElement.getSubject(), answer, thought, mm);
				addStep(level, result.get(1, Entity.class), steps);
				if (result.isTrue()) {
					Mark.say(debug, "Solved contradicting problem");
					result = new BetterSignal(false);
					break;
				}
			}
		}

		// This block of code arranges for helper to ask expert for an approach
		for (Entity element : insight.getElements()) {
			// Look for approach that links intention to checks and method; done if any true
			if (intentionP(element)) {
				if (result.isFalse() && iAmHelper()) {

					// Entity message = RoleFrames.makeRoleFrame("helper", "request", advice);
					// Entity aboutPhrase = element.getSubject();
					// RoleFrames.addRole(message, "about", aboutPhrase);

					Entity original = element.getSubject();

					Entity request = makeRequest(original);

					blurtCommunication(level, request, "?", "green");

					List<Sequence> noviceKnowledge = findMatch(question, intentions);
					List<Sequence> expertKnowledge = getExpert().findApproachPatterns(question);

					int originalSize = noviceKnowledge.size();

					expertKnowledge.stream().forEach(i -> {
						Entity key = i.getElements().get(0).getSubject();
						intentions.put(key, i);
					});
					noviceKnowledge = findMatch(question, intentions);
					int revisedKnowledgeSize = noviceKnowledge.size();
					answerRequest(level, expertKnowledge, question);

					boolean won = revisedKnowledgeSize > originalSize;

					if (won) {
						if (!Radio.realRobot.isSelected()) {
							stop();
						}
						BetterSignal attempt = tryInsight(level, question, insight, answer, trigger, mm);
						if (attempt.isTrue()) {
							Entity advice = new Entity("advice");
							advice.addFeature("approach");
							Entity message = RoleFrames.makeRoleFrame("Helper", "exploit", "advice");
							message.addFeature(Markers.PAST);
							blurtCommunication(level, message, "orange");
							return attempt;
						}
					}
				}
			}
		}

		if (result.isTrue()) {
			for (Entity insightElement : insight.getElements()) {
				// If the insight or alternative problem produced an answer, then actuate result production
				if (consequenceP(insightElement)) {
					List<Sequence> results = findResults(insightElement.getSubject(), mm);
					Mark.say(debug, "Found", results.size(), "matching results");
					for (Sequence byProductOfSolution : results) {
						BetterSignal bs = tryResult(level + 1, insightElement.getSubject(), byProductOfSolution, answer, thought, mm);
						addStep(level, bs.get(1, Entity.class), steps);
					}
				}
			}
		}
		traceOut(level, "insight", insight, result);
		Entity consequence = prepareConsequence(result.isTrue(), iReachedConclusionAbout(question));
		consequence.addProperty(NODE_TYPE, PROBLEM);
		blurtOutPlus(level, thought, consequence, mm);
		return composeAnswerNode(PROBLEM, result, steps, consequence, insight, mm);
	}

	/**
	 * An result is called via a result; it hands off to methods
	 */

	private BetterSignal tryResult(int level, Entity problemElement, Sequence resultSequence, Sequence answer, Entity trigger, MentalModel mm) {
		boolean debug = false;
		traceIn(level, "result", resultSequence);

		BetterSignal result = new BetterSignal(false);
		Sequence steps = constructStepSequence();

		for (Entity intentionElement : resultSequence.getElements()) {
			if (methodP(intentionElement)) {
				List<Sequence> methods = findExecutors(intentionElement.getSubject(), mm);
				Mark.say(debug, "Found", methods.size(), "matching methods");
				for (Sequence method : methods) {
					result = tryExecutor(level + 1, intentionElement.getSubject(), method, method, null, mm);
					if (result.isTrue()) {
						blurtOut(level, iUse(problemElement), mm);
					}
					// No break here, want all results executed.
				}
			}
		}
		Entity consequence = prepareConsequence(true, iUse(problemElement));
		traceOut(level, "result", resultSequence, result);
		return composeAnswerNode(RESULT, result, steps, consequence, resultSequence, mm);

	}

	/**
	 * An approach is called via an intention; it hands off to conditions and methods
	 */
	private BetterSignal tryApproach(int level, Entity problemElement, Sequence approach, Sequence answer, Entity trigger, MentalModel mm) {
		boolean debug = true;
		boolean debugExternalChecker = true;
		Mark.say(debug, "In tryApproach");
		traceIn(level, "Intention:", approach);
		BetterSignal result = new BetterSignal(false);
		Sequence steps = constructStepSequence();

		Entity thought = iApproach(problemElement);
		thought.addProperty(NODE_TYPE, INTENTION);
		
		blurtInPlus(level, trigger, thought, mm);

		blurtMe(level, "Intention:", problemElement);

		// Prepare draft results

		Mark.say(debug, "Element count", approach.getElements().size());

		if (debug) {
			approach.stream().forEachOrdered(e -> Mark.say("Element:", e));
		}

		// Variable will be set to true as soon as method is encountered
		boolean hasMethod = false;

		Sequence instructions = new Sequence();
		for (Entity e : approach.getElements()) {
			instructions.addElement(e);
		}
		
		
		// -------------------------------------------------------------------
		// added by Zhutian on 2018.11.21 for instantiate problem in intention
		boolean instantiated = false;
		Sequence problemInstructions = new Sequence();
		// -------------------------------------------------------------------
		
		for (Entity intentionElement : instructions.getElements()) {
			
			Mark.say("Method element is..................", intentionElement);
			if (verifyP(intentionElement)) {
				
				// Added by phw 2018.10.30
				hasMethod = true;
//				Mark.mit("In code for verify", intentionElement.getSubject());
				result = JustDoIt.zAsk(intentionElement.getSubject());
				if (result.isFalse()) {
					break;
				}
				
			// -------------------------------------------------------------------
			// added by Zhutian on 2018.11.21 for instantiate problem in intention
			} else if (instantiateP(intentionElement)) {
				problemInstructions.addElement(intentionElement);
				
			} else if (problemP(intentionElement)) {
				instantiated = true;
				Mark.mit(intentionElement);
				problemInstructions.addElement(intentionElement);
				instantiatedAction = intentionElement;

			// -------------------------------------------------------------------
				
			} else if (conditionP(intentionElement)) {

				if (stepP(intentionElement)) {
					result = this.solve(level + 1, intentionElement.getSubject(), answer, trigger, mm);
					if (result.isTrue()) {
						Mark.say(debug, "Adding step........................");
						addStep(level, result.get(1, Entity.class), steps);
						continue;
					}
					hasMethod = true;
					break;
				}
				///// /*
				Mark.say(debug, "Working on intention element", intentionElement);

				Mark.say(debugExternalChecker, "Transmitting from basic problem solver +++");
				// BetterSignal gets second argument, which is reset in external checker
				BetterSignal transmissionArgument = new BetterSignal(intentionElement, BetterSignal.NO_ANSWER);
				Connections.getPorts(ProblemSolver.this).transmit(TO_EXTERNAL_CHECKER, transmissionArgument);

				boolean conditionSatisfied = false;
				boolean externalCheckerYesOrNo = false;
				BetterSignal bs = null;

				// if (!externalCheckerResult.elementIsType(0, Integer.class)) {
				// Mark.err("External check return wrong result type");
				// }
				if (transmissionArgument.isYes()) {
					externalCheckerYesOrNo = true;
					conditionSatisfied = true;
					bs = new BetterSignal(true, iConfirm(intentionElement));
					Mark.say(debugExternalChecker, "External checker returned YES");
				}
				else if (transmissionArgument.isNo()) {
					externalCheckerYesOrNo = true;
					Mark.say(debugExternalChecker, "External checker returned NO");
				}
				else if (transmissionArgument.isNoOpinion()) {
					Mark.say(debugExternalChecker, "External checker returned NO OPINION");
				}
				else if (transmissionArgument.isNoAnswer()) {
					Mark.say(debugExternalChecker, "External checker returned NO ANSWER");
				}


				if (!externalCheckerYesOrNo) {
					List<Sequence> checkers = findCheckers(intentionElement.getSubject(), mm);
					Mark.say(debug, "Found", checkers.size(), "matching checkers among", conditions.size());
					for (Sequence condition : checkers) {
						bs = tryChecker(level + 1, intentionElement.getSubject(), condition, answer, thought, mm);
						if (bs.isTrue()) {
							// If any way of satisfying condition succeeds, great, done
							conditionSatisfied = true;
							break;
						}
					}
				}
				if (!conditionSatisfied) {
					// If any condition fails, add step and fail
					if (bs != null) {
						// Mark.say("Fix me", bs.get(1, Entity.class));
						addStep(level, negate(bs.get(1, Entity.class)), steps);
					}
					// Entity conclusion = negate(iConfirm(problemElement));
					Entity consequence = prepareConsequence(false, intentionSuccess(problemElement));
					traceOut(level, "approach", approach, result);
					return composeAnswerNode(INTENTION, result, steps, consequence, approach, mm);
				}
				else {
					// If condition satisfied, add step
					// Mark.say("\n>>> Fix me");
					addStep(level, bs.get(1, Entity.class), steps);
				}
				///// */
				
			}
			else if (methodP(intentionElement)) {
				hasMethod = true;
				List<Sequence> executors = findExecutors(intentionElement.getSubject(), mm);
				if (executors.isEmpty()) {
					Mark.say(debug, "Found no executors for", intentionElement);
				}
				else {
					Mark.say(debug, "Found", executors.size(), "matching executors");
				}
				for (Sequence executor : executors) {
					BetterSignal bs = tryExecutor(level + 1, intentionElement.getSubject(), executor, answer, thought, mm);
					result = bs;
					addStep(level, bs.get(1, Entity.class), steps);
					if (result.isTrue()) {
						// If any method works, great, done
						break;
					}
				}
				if (result.isTrue()) {
					break;
				}
			}
			// New 8 Jan 2018 for requests
			else if (requestP(intentionElement)) {
				hasMethod = true;
				List<Sequence> helpers = findHelpers(intentionElement.getSubject(), mm);
				if (helpers.isEmpty()) {
					Mark.say(debug, "Found no helpers for", intentionElement);
				}
				else {
					Mark.say(debug, "Found", helpers.size(), "matching helpers");
				}
				for (Sequence helper : helpers) {
					BetterSignal bs = tryHelper(level + 1, intentionElement.getSubject(), helper, answer, thought, mm);
					result = bs;
					addStep(level, bs.get(1, Entity.class), steps);
					if (result.isTrue()) {
						// If any method works, great, done
						break;
					}
				}
				if (result.isTrue()) {
					break;
				}
			}
		}
		// If no method, suffices for any conditions to have been met
		// Reset result to reflect success

		if (hasMethod) {
			Mark.say(debug, "A method was found in approach");
		}
		else {
			Mark.say(debug, "No method found in approach");
			result = new BetterSignal(true);
		}
		
		debug = true;
		// -------------------------------------------------------------------
		// added by Zhutian on 2018.11.21 for instantiate problem in intention
		for (Entity problem : problemInstructions.getElements()) {
			Mark.mit(debug, "Working on problem in approach", problem.getSubject());
			Mark.mit(debug, answer);
			
			result = solve(level + 1, problem.getSubject(), answer, thought, mm);
			Mark.mit(debug, ((BetterSignal) result).get(0, Object.class));
			Entity action = ((BetterSignal) result).get(1, Entity.class);
			Mark.mit(debug,action);
			if (result.isFalse()) {
				Mark.say(problemInstructions);
				result = new BetterSignal(false);
				break;
			} else {
				addStep(level, result.get(1, Entity.class), steps);
				// If it is an assignment element, assign; this is for when following problem needs argument
				// Note that problem may be explicitly marked but need not be because assignment will be noted in
				// answer
				if (instantiateP(problem) || assignmentReturned(answer)) {
					assign(answer, problem, problemInstructions);
					Mark.mit(debug, "This is assignment element", problem);
					result = new BetterSignal(false);
				}
				// If not assignment element, keep going around loop only if making progress
				else if (result.isTrue()) {
					Mark.mit(debug, "This is not an assignment element", problem);
					Mark.mit(debug, "True result for", problem);
					break;
				}
				else {
					Mark.mit(debug, "False result for", problem);
				}
			}
		}
		// -------------------------------------------------------------------

		
		Entity consequence = prepareConsequence(result.isTrue(), intentionSuccess(problemElement));
		consequence.addProperty(NODE_TYPE, INTENTION);

		blurtOutPlus(level, thought, consequence, mm);

		if (result.isTrue()) {

			blurtMe(level, "Approach succeeded:", problemElement);
		}
		else {
			blurtMe(level, "Approach failed:", problemElement);
		}

		traceOut(level, "approach", approach, result);


		return composeAnswerNode(INTENTION, result, steps, consequence, approach, mm);
	}

	/**
	 * A checker is called via a condition; it hands off to a method or an equivalent question/problem.
	 */
	private BetterSignal tryChecker(int level, Entity intentionElement, Sequence instructions, Sequence answer, Entity trigger, MentalModel mm) {
		boolean debug = false;
		Connections.getPorts(this).transmit(instructions);
		traceIn(level, "checker", instructions);
		Sequence steps = constructStepSequence();

		Entity thought = iCheck(intentionElement);
		thought.addProperty(NODE_TYPE, CONDITION);

		blurtInPlus(level, trigger, thought, mm);

		blurtMe(level, "Condition:", intentionElement);

		// First, divide into a predicate and problems to solve if predicate
		// not satisfied:

		Sequence predicateInstructions = new Sequence();

		Sequence problemInstructions = new Sequence();

		for (Entity e : instructions.getElements()) {
			if (methodP(e)) {
				predicateInstructions.addElement(e);
			}
			else if (problemP(e)) {
				problemInstructions.addElement(e);
			}
			else if (instantiateP(e)) {
				problemInstructions.addElement(e);
			}
			else if (specialtyP(e)) {
				// Ignore this one.
			}
			else {
				Mark.err("tryChecker encountered element that is neither predicat nor problem\n", e);
			}
		}

		// Check to be sure there is just one predicate:

		if (predicateInstructions.size() == 0) {
			Mark.err(debug, "tryChecker did not see a predicate");
		}
		else if (predicateInstructions.size() > 1) {
			Mark.err(debug, "trychecker saw more than one predicate");
		}

		// Start looping:

		BetterSignal result = new BetterSignal(false);

		boolean repeat = true;

		while (repeat) {

			Mark.say(debug, "Starting condition loop");

			// Cloning needed because variable instantiation will happen each time around loop

			Sequence predicates = (Sequence) (predicateInstructions.clone());

			Sequence problems = (Sequence) (problemInstructions.clone());

			// Stop after this interation, unless problem is newly solved

			repeat = false;

			// Check predicate:

			if (!predicates.getElements().isEmpty()) {

				Entity predicate = predicates.get(0);

				List<Sequence> methods = findExecutors(predicate.getSubject(), mm);
				// Mark.say(debug, "Found", methods.size(), "matching methods");
				for (Sequence method : methods) {
					result = tryExecutor(level + 1, predicate.getSubject(), method, answer, thought, mm);
					if (result.isTrue()) {
						Mark.say(debug, "Condition test came out true");
						addStep(level, result.get(1, Entity.class), steps);
						break;
					}
					else {
						Mark.say(debug, "Condition test came out false");
						addStep(level, result.get(1, Entity.class), steps);
					}
				}
			}

			// If predicate satisfied, succeed by stopping loop:

			if (result.isTrue()) {
				break;
			}

			// If not, try solving problem:

			for (Entity problem : problems.getElements()) {
				Mark.say(debug, "Working on problem in checker", problem.getSubject());
				result = solve(level + 1, problem.getSubject(), answer, thought, mm);
				addStep(level, result.get(1, Entity.class), steps);
				// If it is an assignment element, assign; this is for when following problem needs argument
				// Note that problem may be explicitly marked but need not be because assignment will be noted in
				// answer
				if (instantiateP(problem) || assignmentReturned(answer)) {
					assign(answer, problem, problems);
					Mark.say(debug, "This is assignment element", problem);
					result = new BetterSignal(false);
				}
				// If not assignment element, keep going around loop only if making progress
				else if (result.isTrue()) {
					Mark.say(debug, "This is not an assignment element", problem);
					Mark.say(debug, "True result for", problem);
					repeat = true;
					break;
				}
				else {
					Mark.say(debug, "False result for", problem);
				}
			}
			if (repeat) {
				// If no predicates, then done!
				if (predicates.getElements().isEmpty()) {
					break;
				}
			}
			else if (!repeat) {
				Mark.say(debug, "Too bad, predicate not satisfied and no problem solved");
				break;
			}
		}
		Entity consequence = prepareConsequence(result.isTrue(), iConfirm(intentionElement));
		consequence.addProperty(NODE_TYPE, CONDITION);
		blurtOutPlus(level, thought, consequence, mm);

		if (result.isTrue()) {
		blurtMe(level, "Condition satisfied:", intentionElement);
		}
		else {
			blurtMe(level, "Condition unsatisfied:", intentionElement);
		}

		traceOut(level, "checker", instructions, result);
		return composeAnswerNode(CONDITION, result, steps, consequence, instructions, mm);
	}

	private Entity copy(Entity condition) {
		if (condition.entityP()) {
			return condition;
		}
		Entity result = null;
		if (condition.functionP()) {
			result = new Function("thing", copy(condition.getSubject()));
		}
		else if (condition.relationP()) {
			result = new Relation("thing", copy(condition.getSubject()), copy(condition.getObject()));
		}
		else if (condition.sequenceP("thing")) {
			result = new Sequence();
			for (Entity e : condition.getElements()) {
				result.addElement(copy(e));
			}
		}
		else {
			Mark.err("No trigger......................................");
		}
		Bundle bundle = condition.getBundle().getClone();
		result.setBundle(bundle);
		return result;
	}

	private boolean assignmentReturned(Sequence answer) {
		if (answer.getElements().isEmpty()) {
			return false;
		}
		Entity first = (answer.getElements().get(0));
		if (first.isA("assign")) {
			return true;
		}
		return false;
	}

	private boolean assign(Sequence answer, Entity problem, Entity problems) {
		boolean debug = false;
		if (answer.getElements().isEmpty()) {
			return false;
		}
		if (debug) {
			Mark.say(debug, "Here I am in assign.  Problem:\n", problem);
			problems.stream().forEachOrdered(a -> Mark.say("Problem", a));
			answer.stream().forEachOrdered(a -> Mark.say("Answer", a));
		}
		Entity first = (answer.getElements().get(0));
		if (first.isA("assign")) {
			answer.getElements().remove(0);
			// Aha, looks like an assignment answer
			Entity pattern = RoleFrames.getObject(first);
			Entity datum = RoleFrames.getRole("to", first);
			PairOfEntities pair = new PairOfEntities(pattern, datum);
			LList bindings = new LList();
			bindings = bindings.cons(pair, bindings);
			// Mark.say("....................", pattern, datum, bindings);
			for (int i = 0; i < ((Sequence) problems).size(); ++i) {

				Entity replacement = Substitutor.substitute(problems.get(i), bindings);
				Mark.say(debug, "Condition  ", problems.get(i));
				Mark.say(debug, "Replacement", replacement);
				problems.getElements().set(i, replacement);
			}
			return true;
		}
		return false;

	}

	private Entity prepareConsequence(boolean b, Entity e) {
		if (!b) {
			// Have to clone so doesn't screw up second attempt to look for satisfied condition test
			e = e.deepClone();
			// Fix me!!!
			negate(e);
		}
		return e;
	}

	/**
	 * An executor is called via a method; it calls a grounding procedure.
	 */
	private BetterSignal tryExecutor(int level, Entity conditionElement, Sequence method, Sequence answer, Entity thought, MentalModel mm) {
		boolean debug = false;

		// blurtCommand(level, conditionElement, "red");

		blurtMe(level, "Method:", conditionElement, "red");

		blurtJustDoIt(level, CommandList.ADD_REQUEST, conditionElement, "red");

		Mark.say(debug, ">>>>>>>>>>>>>>>>\n", conditionElement, "\n", method);

		Connections.getPorts(this).transmit(conditionElement);
		traceIn(level, "execution", method);
		BetterSignal result = new BetterSignal(false);

		Sequence steps = constructStepSequence();

		for (Entity methodElement : method.getElements()) {
			Mark.say("Method element is..................", methodElement);
			if (executeP(methodElement)) {
				result = call(methodElement, answer, mm, this);
				if (result.isFalse()) {
					break;
				}
			}
			else if (verifyP(methodElement)) {
				Mark.mit("------------");
				Mark.mit(methodElement);
				Mark.mit("------------");
				result = JustDoIt.zAsk(methodElement.getSubject());
				if (result.isFalse()) {
					break;
				}
			}
		}

		// blurtResult(level, result.isTrue(), "red");

		// conditionElement.addFeature(Markers.FUTURE);

		if (result.isTrue()) {
			blurtMe(level, "Just did:", conditionElement, "red");
			blurtJustDoIt(level, CommandList.ADD_SUCCESS, conditionElement, "red");
		}
		else {
			blurtJustDoIt(level, CommandList.ADD_FAILURE, conditionElement, "red");
		}

		Mark.say(debug, "Result of method is", result.isTrue(), conditionElement);
		traceOut(level, "execution", method, result);

		Entity consequence = prepareConsequence(result.isTrue(), conditionElement);
		return composeAnswerNode(METHOD, result, steps, consequence, method, mm);
	}

	/**
	 * A helper.
	 */
	private BetterSignal tryHelper(int level, Entity conditionElement, Sequence request, Sequence answer, Entity thought, MentalModel mm) {
		boolean debug = false;
		traceIn(level, "helper", request);
		Entity message = RoleFrames.makeRoleFrame("Expert", "request", "help");
		blurtCommunication(level, message, "?", "purple");

		Mark.say(debug, "Trace in helper", level, "\n", request);
		BetterSignal result = new BetterSignal(false);

		Sequence steps = constructStepSequence();

		for (Entity methodElement : request.getElements()) {
			if (communicateP(methodElement)) {
				Mark.say(debug, "Executing helper");

				result = communicate(level, methodElement, answer, mm, this);

				// result = call(methodElement, answer, mm, this);

			}
			if (result.isTrue()) {
				break;
			}
		}

		Mark.say(debug, "Result of helper is", result, conditionElement);
		traceOut(level, "helper", request, result);
		Entity consequence = prepareConsequence(result.isTrue(), conditionElement);
		Mark.say(debug, "BlurtAux gets at output result, thought\n", consequence);
		message = RoleFrames.makeRoleFrame("Expert", "get", "help");
		message.addFeature(Markers.PAST);
		blurtCommunication(level, message, "green");
		return composeAnswerNode(METHOD, result, steps, consequence, request, mm);
	}

	public static void addSolutionElement(Entity element, Sequence answer) {
		for (Entity e : answer.getElements()) {
			if (StandardMatcher.getBasicMatcher().match(element, e) != null) {
				// Mark.say("Skipping new element", element);
				return;
			}
		}
		answer.addElement(element);
	}

	private BetterSignal composeAnswerNode(int type, BetterSignal result, Sequence steps, Entity consequence, Sequence instructions, MentalModel mm) {
		boolean debug = false;

		Mark.say(debug, "Entering composeAnswerNode", consequence);

		consequence.addProperty(NODE_TYPE, type);

		if (!steps.getElements().isEmpty() && Switch.buildTree.isSelected()) {
			injectConnection(steps, consequence, mm);
		}

		Entity comment = null;
		if (instructions == null) {
			// Do nothing
		}
		else if (result.isTrue()) {
			comment = findSuccess(instructions);
		}
		else if (result.isFalse()) {
			comment = findFailure(instructions);
		}
		if (comment != null && Switch.reportComments.isSelected()) {
			steps = constructStepSequence();
			addStep(0, consequence, steps);
			comment.addProperty(NODE_TYPE, COMMENT);
			injectConnection(steps, comment, mm);
			Mark.say(debug, "Using success/failure comment\n>>> ", consequence, "\n>>> ", comment);
		}

		return new BetterSignal(result.isTrue(), consequence);
	}

	private Entity findSuccess(Sequence instructions) {
		Optional<Entity> optional = instructions.stream().filter(e -> successP(e)).findAny();
		if (optional.isPresent()) {
			return optional.get().getSubject();
		}
		return null;
	}

	private Entity findFailure(Sequence instructions) {
		// instructions.stream().forEachOrdered(e -> Mark.say(">>>>", e));
		Optional<Entity> optional = instructions.stream().filter(e -> failureP(e)).findAny();
		// Mark.say("Optional", optional);
		if (optional.isPresent()) {
			return optional.get().getSubject();
		}
		return null;
	}

	/**
	 * This method uses hairy reflection cut and paste job from web
	 *
	 * @param problemSolver
	 */
	private BetterSignal call(Entity e, Sequence answer, MentalModel mm, ProblemSolver problemSolver) {
		Entity description = e.getSubject();
		String method = RoleFrames.getObject(description).getType();
		Object[] arguments = convertToArray(mm, problemSolver, RoleFrames.getRoles("with", description));
		Class<?> parameterTypes[] = new Class[arguments.length];
		for (int i = 0; i < arguments.length; ++i) {
			if (arguments[i] instanceof MentalModel) {
				parameterTypes[i] = MentalModel.class;
			}
			else if (arguments[i] instanceof ProblemSolver) {
				parameterTypes[i] = ProblemSolver.class;
			}
			else if (arguments[i] instanceof Entity) {
				parameterTypes[i] = Entity.class;
			}
			else {
				Mark.err("Oops, unrecognized type in arguments to JustDoIt call");
			}
		}
		try {
			// Here is were hairy reflection happens
			Method theMethod = JustDoIt.class.getDeclaredMethod(method, parameterTypes);
			// Mark.say("The method is", theMethod);
			// Strange first argument in invoke, but hey, it works
			return transferAnswerElements((BetterSignal) theMethod.invoke(JustDoIt.getJustDoIt(), arguments), answer);
		}
		catch (Exception exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}

		return new BetterSignal(false);
	}

	public static void pause() {
		JOptionPane.showMessageDialog(ABasicFrame.getTheFrame(), "Paused");
	}

	/**
	 * This method uses nothing hairy.
	 *
	 * @param problemSolver
	 */
	private BetterSignal communicate(int level, Entity e, Sequence answer, MentalModel mm, ProblemSolver problemSolver) {

		boolean debug = false;

		Entity description = e.getSubject();

		Mark.say(debug, "Communicate received\n", description);

		Mark.say(debug, "Arguments received\n", level, "\n", e, "\n", answer);

		// BetterSignal solution = JustDoIt.jdAskForHelper(level, mm, problemSolver, description);

		Mark.say(debug, "Calling helper");

		BetterSignal solution = problemSolver.getNovice().solve(level, e.getSubject(), new Sequence(), null, mm);

		return solution;
	}

	public BetterSignal whatIf(MentalModel mm, Entity first) {
		boolean debug = false;
		Mark.say(debug, "\n>>> Perform what-if analysis here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		Connections.getPorts(this).transmit(QuestionExpert.TO_DXH, new BetterSignal(QuestionExpert.WHAT_IF, first, mm));
		return new BetterSignal(false);
	}



	private BetterSignal transferAnswerElements(BetterSignal bs, Sequence answer) {
		if (bs.size() > 1) {
			bs.get(1, Sequence.class).stream().forEachOrdered(e -> answer.addElement(e));
		}
		else {
			// Mark.err("transferAnswerElements got signal with too few elements");
		}
		return bs;
	}

	// private BetterSignal whatIf(Sequence answer, MentalModel mm, ProblemSolver solver, Entity... arguments) {
	// boolean debug = true;
	// BetterSignal result = JustDoIt.getJustDoIt().whatIf(arguments[0], mm.getStoryProcessor().getStory());
	// return result;
	// }

	// private BetterSignal isInOrder(Sequence answer, MentalModel mm, ProblemSolver solver, Entity... arguments) {
	// boolean debug = true;
	// Mark.say(debug, "\n>>> Looking for correct order:");
	// Arrays.asList(arguments).stream().forEachOrdered(e -> Mark.say(debug, ">>> inStory argument", e));
	// BetterSignal result = JustDoIt.getJustDoIt().isInOrder(mm, arguments[0], arguments[1]);
	// return result;
	// }

	// private BetterSignal explainExplicitConsequents(Sequence answer, MentalModel mm, ProblemSolver solver, Entity...
	// arguments) {
	// boolean debug = false;
	// Mark.say(debug, "\n>>> Looking for recipe in story:");
	// Arrays.asList(arguments).stream().forEachOrdered(e -> Mark.say(debug, ">>> inStory argument", e));
	// Sequence miniStory = new Sequence();
	// BetterSignal result = JustDoIt.getJustDoIt().describeExplicitConsequents(mm, arguments[0]);
	// if (result.isTrue()) {
	// Mark.say(debug, "Found consequence in the story");
	// }
	// else {
	// Mark.say(debug, "Did not find consequence in the story");
	// }
	// return result;
	// }

	// private BetterSignal composeSummary(MentalModel mm, Entity... arguments) {
	// boolean debug = true;
	// Mark.say(debug, "\n>>> Time to compose a summary");
	// Sequence answer = new Sequence();
	// answer.addAll(Summarizer.getSummarizer().processConceptCenteredSummaryDirectly(mm));
	// return new BetterSignal(true, answer);
	// }

	// private BetterSignal findRecipe(Sequence answer, MentalModel mm, ProblemSolver solver, Entity... arguments) {
	// boolean debug = false;
	// Mark.say(debug, "\n>>> Looking for recipe in story:");
	// Arrays.asList(arguments).stream().forEachOrdered(e -> Mark.say(debug, ">>> inStory argument", e));
	// Sequence miniStory = new Sequence();
	// JustDoIt
	// .findRecipe(answer, solver, arguments[0], mm.getStoryProcessor().getStory(),
	// mm.getStoryProcessor().getExplicitElements(), miniStory);
	// if (result.isTrue()) {
	// Mark.say(debug, "Found recipe in the story");
	// }
	// else {
	// Mark.say(debug, "Did not find recipe in the story");
	// }
	// return result;
	// }


	// private BetterSignal isBelief(MentalModel mm, Entity... arguments) {
	// boolean debug = false;
	// Mark.say(debug, "\n>>> Looking for element in beliefs:");
	// // Arrays.asList(arguments).stream().forEachOrdered(e -> Mark.say(debug, ">>> isBelief argument", e));
	// // Make a JustDoIt
	// boolean result = isMyBeliefP(arguments[0], mm);
	// if (result) {
	// Mark.say(debug, "Found", arguments[0], "in beliefs");
	// }
	// else {
	// Mark.say(debug, "Did not find", arguments[0], "in beliefs");
	// }
	// Mark.say(debug, "Result is", new BetterSignal(result));
	// return new BetterSignal(result);
	// }



	// private BetterSignal insert(MentalModel mm, Entity argument) {
	// boolean debug = false;
	// Mark.say(debug, "\n>>> Injecting element into story:");
	// // Arrays.asList(arguments).stream().forEachOrdered(e -> Mark.say(debug, ">>> inject argument", e));
	// Mark.say(debug, "Injecting", argument);
	// // Make a JustDoIt; also, probably does not belong here! Should be moved to story.
	// mm.getStoryProcessor().injectElementWithDereference(argument);
	// return new BetterSignal(true);
	// }

	private Entity[] convertToArray(List<Entity> list) {
		Entity[] arguments = new Entity[list.size()];
		list.toArray(arguments);
		return arguments;
	}

	private Object[] convertToArray(MentalModel mm, ProblemSolver problemSolver, List<Entity> list) {
		Object[] arguments = new Object[list.size() + 2];
		arguments[0] = mm;
		arguments[1] = problemSolver;
		for (int i = 2; i < arguments.length; ++i) {
			arguments[i] = list.get(i - 2);
		}
		return arguments;
	}

	private boolean conditionP(Entity element) {
		if (element.functionP("check") || element.functionP("condition") || element.functionP("step")) {
			return true;
		}
		return false;
	}

	private boolean stepP(Entity element) {
		if (element.functionP("step")) {
			return true;
		}
		return false;
	}

	private boolean instantiateP(Entity element) {
		if (element.functionP("instantiate")) {
			return true;
		}
		return false;
	}

	private boolean successP(Entity element) {
		if (element.functionP("success")) {
			return true;
		}
		return false;
	}

	private boolean failureP(Entity element) {
		if (element.functionP("failure")) {
			return true;
		}
		return false;
	}

	private boolean tryP(Entity element) {
		if (element.functionP("try")) {
			return true;
		}
		return false;
	}

	private boolean intentionP(Entity element) {
		if (element.functionP("intention")) {
			return true;
		}
		return false;
	}

	private boolean methodP(Entity element) {
		if (element.functionP("method") || element.functionP("check")) {
			return true;
		}
		return false;
	}

	private boolean requestP(Entity element) {
		// Mark.say("checking if is request", element);
		if (element.functionP("request") || element.functionP("ask")) {
			return true;
		}
		return false;
	}

	private boolean executeP(Entity element) {
		if (element.functionP("execute")) {
			return true;
		}
		return false;
	}

	private boolean verifyP(Entity element) {
		if (element.functionP("verify")) {
			return true;
		}
		return false;
	}

	private boolean communicateP(Entity element) {
		if (element.functionP("communicate")) {
			return true;
		}
		return false;
	}

	private boolean problemP(Entity element) {
		if (element.functionP("solve") || element.functionP("problem") || element.functionP("question")) {
			return true;
		}
		return false;
	}

	private boolean contradictionP(Entity element) {
		if (element.functionP("contradiction")) {
			return true;
		}
		return false;
	}

	// private boolean resultP(Entity element) {
	// if (element.functionP("result")) {
	// return true;
	// }
	// return false;
	// }

	private boolean consequenceP(Entity element) {
		if (element.functionP("consequence")) {
			return true;
		}
		return false;
	}

	private boolean specialtyP(Entity element) {
		if (element.functionP("specialty") || element.functionP("intention")) {
			return true;
		}
		return false;
	}



	public static void main (String[] ignore) {
		Translator t = Translator.getTranslator();
		ProblemSolver ps = new ProblemSolver();
	}

	/**
	 * Problem solving knowledge starts here
	 */

	public List<Intention> getIntentions(MentalModel mm) {
		List<Intention> intentions = new ArrayList<>();
		if (true || intentions == null) {
			Mark.err("Be sure to reverse this debugging condition!!!!");
			intentions = new ArrayList<>();
			intentions.add(makeIntentionAddElementToStory(mm));
			// intentions.add(makeAddElementToStoryIntention(mm));
		}
		return intentions;

	}

	public List<Test> getTests(MentalModel mm) {
		if (true || tests == null) {
			Mark.err("Be sure to reverse this debugging condition!!!!");
			tests = new ArrayList<>();
			tests.add(makeTestIsConnectingCausalPath(mm));
		}
		return tests;

	}


	public Test makeTestIsConnectingCausalPath(MentalModel mm) {
		Entity description = Translator.getTranslator().translateToEntityWithVariables("A path leads from xx to yy", "xx", "yy");
		Test test = new Test("Test for causal path", description, entity -> {
			// Dig out elements and test
			Entity from = RoleFrames.getRole("from", entity);
			Entity to = RoleFrames.getRole("to", entity);
			// Now perform breadth first search
			return JustDoIt.getJustDoIt().findPath(mm, this, from, to).isTrue();
		});
		return test;
	}

	public Intention makeIntentionAddElementToStory(MentalModel mm) {

		Entity question = Translator.getTranslator().translateToEntityWithVariables("A path leads from xx to yy", "xx", "yy");
		Entity condition = Translator.getTranslator().translateToEntityWithVariables("I believe xx", "xx");

		Intention intention = new Intention("Add belief to story", question, entity -> {
			// Dig out elements and test
			Entity theBelief = RoleFrames.getRole(Markers.FROM, entity);
			// Mark.say("The belief to insert is", theBelief);
			mm.getStoryProcessor().injectElementWithDereference(theBelief);
			return true;
		}, makeConditionElementIsBelief(condition, mm));

		return intention;
	}

	public Condition makeConditionElementIsBelief(Entity conditionDescription, MentalModel mm) {
		return new Condition("Test is belief condition", conditionDescription, entity -> {
			Entity theBelief = RoleFrames.getObject(entity);
			// If already in the story, no advance to put it in again.
		    // A hack. This should be a separate condition
			for (Entity element : mm.getStoryProcessor().getStory().getElements()) {
				if (StandardMatcher.getBasicMatcher().match(theBelief, element) != null) {
					return false;
				}
			}
			if (JustDoIt.getJustDoIt().isBelief(mm, this, theBelief).isTrue()) {
				return true;
			}
			return false;
		});

	}

	boolean trace = true;

	String[] markerArray = { "Question", "Intention", "Method", "Check", "Execute", "Success", "Failure", "Result" };

	List<String> markers = Arrays.asList(markerArray);

	public void traceIn(int level, String title, Sequence s) {

		String signature = (String) (s.getProperty("signature"));

		String spaces = "";
		for (int i = 0; i < level; ++i) {
			spaces += " ";
		}
		String message = "\n>>>  " + spaces + level + " Working on " + title;
		if (s != null) {

			if (!title.equalsIgnoreCase("problem")) {
				message += " with signature " + s.getProperty("Signature");
				message += "\n>>>   " + spaces + "Matched: " + extractSpecialty(s);
				for (int i = 1; i < s.size(); ++i) {
					Entity e = s.get(i);
					message += "\n>>>   " + spaces + e.getType() + ": " + generate(e.getSubject()).trim();
				}
			}
			else {
				message += " " + extractSpecialty(s);
			}

			if (title.equalsIgnoreCase("helper")) {
				message += "!!!!!!!!!!!!!!!!!!!!!!!!";
			}

			for (Entity e : s.getElements()) {
				for (String marker : markers) {
					if (e.isA(marker.toLowerCase())) {
						if (marker.toLowerCase().equals("success")) {
							// message += "\n>>> " + spaces + marker + ": " + generate(e.getSubject()).trim();
						}
						else if (marker.toLowerCase().equals("failure")) {
							// message += "\n>>> " + spaces + marker + ": " + generate(e.getSubject()).trim();
						}
						else if (marker.toLowerCase().equals("execute")) {
							message += "\n>>>   " + spaces + marker + ": " + RoleFrames.getObject(e.getSubject()).getType();
						}
						else {
							message += "\n>>>   " + spaces + marker + ": " + generate(e.getSubject()).trim();
							// result += "\n+++ " + spaces + marker + ": " + e.getSubject();
						}
					}
				}
			}
		}
		System.out.println(message);
		
	}

	public void traceOut(int level, String title, Sequence s, BetterSignal result) {

		String signature = (String) (s.getProperty("signature"));

		String spaces = "";
		for (int i = 0; i < level; ++i) {
			spaces += " ";
		}
		String message = "\n>>>  " + spaces + level + (result.isTrue() ? " Succeeded" : " Failed") + " working on " + title;
		if (s != null) {

			if (!title.equalsIgnoreCase("problem")) {
				message += " with signature " + s.getProperty("Signature");
				message += "\n>>>   " + spaces + "Match: " + extractSpecialty(s);
			}
			else {
				message += " " + extractSpecialty(s);
			}
			for (Entity e : s.getElements()) {
				for (String marker : markers) {
					if (e.isA(marker.toLowerCase())) {
						if (marker.toLowerCase().equals("success")) {
							if (result.isTrue()) {
							message += "\n>>>   " + spaces + marker + ": " + generate(e.getSubject()).trim();
							}
						}
						else if (marker.toLowerCase().equals("failure")) {
							if (result.isFalse()) {
							message += "\n>>>   " + spaces + marker + ": " + generate(e.getSubject()).trim();
							}
						}
						else if (marker.toLowerCase().equals("execute")) {
							message += "\n>>>   " + spaces + marker + ": " + RoleFrames.getObject(e.getSubject()).getType();
						}
						else {
							message += "\n>>>   " + spaces + marker + ": " + generate(e.getSubject()).trim();
							// result += "\n+++ " + spaces + marker + ": " + e.getSubject();
						}
					}
				}
			}
		}
		System.out.println(message);
	}

	private Entity extractSpecialtyEntity(Sequence s) {
		if (s != null) {
			for (Entity e : s.getElements()) {
				if (e.isA("specialty")) {
					Entity result = e.getSubject();
					return result;
				}
			}
		}
		return new Entity(Markers.UNKNOWN);
	}

	private String extractSpecialty(Sequence s) {
		if (s != null) {
			for (Entity e : s.getElements()) {
				if (e.isA("specialty")) {
					String result = generate(e.getSubject()).trim();
					if (result.equals("")) {
						return e.getSubject().toString();
					}
					else {
						return result;
					}
				}
			}
		}
		return "unknown";
	}

	private void extractMethod(Sequence s) {
		String result = "";
		for (Entity e : s.getElements()) {
			if (e.isA("execute")) {
				result += " " + "Method: " + RoleFrames.getObject(e.getSubject()).getType();
			}
		}
		if (!result.isEmpty()) {
			Mark.say(trace, result);
		}
	}

	private void recite(Sequence s) {
		s.stream().forEachOrdered(e -> Mark.say(e));
	}

	///// Get helper and ask for help

	ProblemSolver expert;

	ProblemSolver novice;

	public ProblemSolver getExpert() {
		if (expert == null) {
			expert = GenesisGetters.getMentalModel1().getProblemSolver();
		}
		if (this == expert) {
			return null;
		}
		return expert;
	}

	public ProblemSolver getNovice() {
		if (novice == null) {
			novice = GenesisGetters.getMentalModel2().getProblemSolver();
		}
		if (this == novice) {
			return null;
		}
		return novice;
	}

	public boolean iAmExpert() {
		if (getExpert() == null) {
			return true;
		}
		return false;
	}

	public boolean iAmHelper() {
		if (getNovice() == null) {
			return true;
		}
		return false;
	}

	protected void ask(int level, Entity e, MentalModel mm) {
		blurtAux(level, e, mm);
	}

	public static void stop() {
		int response = JOptionPane.showOptionDialog(ABasicFrame.getTheFrame(),

		        "Click ok to continue", "Pause",

		        JOptionPane.DEFAULT_OPTION,

		        JOptionPane.PLAIN_MESSAGE,

		        null, null, null);

	}

}

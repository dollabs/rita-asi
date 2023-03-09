package consciousness;

import java.lang.Thread;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import com.ascent.gui.frame.ABasicFrame;
import cagriZaman.RobotInterface;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import generator.Rules;
import genesis.GenesisGetters;
import matchers.StandardMatcher;
import mentalModels.MentalModel;
import storyProcessor.ConceptDescription;
import subsystems.blocksWorld.models.Block;
import subsystems.blocksWorld.models.Brick;
import subsystems.summarizer.Summarizer;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;
import utils.tools.Predicates;
import zhutianYang.RecipeExpert;

import java.io.*;  
import java.net.*; 
/*
 * Created on Jan 6, 2016
 * @author phw
 */

public class JustDoIt extends AbstractWiredBox {


	private static JustDoIt singleton;
	public static List<String> conditions = new ArrayList<String>();
	public static List<Entity> foundContained = new ArrayList<>();
	static Map<Entity, List<Entity>> multipleContained = new HashMap<Entity, List<Entity>>();
	static Map<String, Boolean> questionsAsked = new HashMap<String, Boolean>();

	public JustDoIt() {
		Connections.getPorts(this).addSignalProcessor(this::updateCondition);
	}
	
	public static void clear() {
		questionsAsked = new HashMap<String, Boolean>();
		foundContained = new ArrayList<>();
	}

	/**
	 * Gets one-off instantiation. Used as alternative to making everything static.
	 */
	public static JustDoIt getJustDoIt() {
		if (singleton == null) {
			singleton = new JustDoIt();
		}
		return singleton;
	}

	/**
	 * The method searches back through causes to elements that appear explicitly in the story. Method returns a
	 * BetterSignal, whose first element is true or false depending on whether any such elements are found. Second
	 * element is a sequence consisting of just one element, a cause relation. The consequent of the cause relation is
	 * the element supplied as the argument; the antecedents are all the explicit story elements that were found in the
	 * search. Reason for the sequence of just one element is uniformity with other JustDoIt methods. If no antecedent
	 * elements are found, result includes a role frame indicating no explanation was found.
	 */
	public BetterSignal explain(MentalModel mm, ProblemSolver ps, Entity entity) {
		boolean debug = false;
		Mark.say(debug, "\n>>>  Looking for explanation of", entity, "in story of length", mm.getStoryProcessor().getStory().getElements().size());
		// First, reset entity to be matching structure in the story
		for (Entity e : mm.getStoryProcessor().getStory().getElements()) {
			if (StandardMatcher.getIdentityMatcher().match(entity, e) != null) {
				entity = e;
				break;
			}
		}
		List<Entity> antecedents = findExplicitAntecedents(entity, mm);
		Sequence answer = new Sequence();
		if (antecedents.size() > 0) {
			Entity[] array = new Entity[antecedents.size()];
			antecedents.toArray(array); // fill the array
			Entity cause = Rules.makeCause(entity, array);
			Mark.say(debug, "Caused element is", cause);
			ProblemSolver.addSolutionElement(cause, answer);
			return new BetterSignal(true, answer);
		}
		else {
			Entity roleFrame = RoleFrames.makeRoleFrame(entity, "have", "explanation");
			roleFrame.addFeature(Markers.NOT);
			ProblemSolver.addSolutionElement(roleFrame, answer);
			return new BetterSignal(false, answer);
		}
	}

	/**
	 * The method searches forward through causes to elements that either appear explicitly in the story or have not
	 * consequents. Method returns a BetterSignal, whose first element is true or false depending on whether any such
	 * elements are found. Second element is a sequence consisting of possibly many cause relations linking the element
	 * to those found in the search. If no consequent elements are found, result includes a role frame indicating no
	 * explanation was found.
	 */
	public BetterSignal findConsequences(MentalModel mm, ProblemSolver ps, Entity entity) {
		// Mark.say("\n>>> Looking for explanation of", entity, "in story of length", story.getElements().size());
		// First, reset entity to be matching structure in the story
		for (Entity e : mm.getStoryProcessor().getStory().getElements()) {
			if (StandardMatcher.getIdentityMatcher().match(entity, e) != null) {
				entity = e;
				break;
			}
		}
		List<Entity> consequents = findExplicitConsequents(entity, mm);
		Sequence answer = new Sequence();
		if (consequents.size() > 0) {
			for (Entity c : consequents) {
				Entity[] array = new Entity[consequents.size()];
				consequents.toArray(array); // fill the array
				Entity cause = Rules.makeCause(c, entity);
				ProblemSolver.addSolutionElement(cause, answer);
			}
			return new BetterSignal(true, answer);
		}
		else {
			Entity roleFrame = RoleFrames.makeRoleFrame(entity, "have", "consequence");
			roleFrame.addFeature(Markers.NOT);
			ProblemSolver.addSolutionElement(roleFrame, answer);
			return new BetterSignal(false, answer);
		}

	}

	/**
	 * All following methods return a BetterSignal instance. If method just looks for yes-no, then the BetterSignal has
	 * just one returned element, but some also return a sequence of entities used in preparing an answer to a question
	 * that is not yes-no.
	 */

	/**
	 * Performs breadth first search to see if two matching elements are connected.
	 */
	public BetterSignal findPath(MentalModel mm, ProblemSolver ps, Entity fromElement, Entity toElement) {
		List<List<Entity>> queue = new ArrayList<>();
		// Identify matching from and to in story
		Entity from = null;
		Entity to = null;

		for (Entity element : mm.getStoryProcessor().getStory().getElements()) {
			if (from == null && StandardMatcher.getBasicMatcher().match(fromElement, element) != null) {
				from = element;
			}
			else if (to == null && StandardMatcher.getBasicMatcher().match(toElement, element) != null) {
				to = element;
			}
		}
		if (from == null || to == null) {
			return new BetterSignal(false);
		}
		// Mark.say("From/to patterns\n", fromPattern, "\n", toPattern);
		// Mark.say("From/to elements\n", from, "\n", to);
		// Initialize queue
		List<Entity> path = new ArrayList();
		path.add(from);
		queue.add(path);
		while (!queue.isEmpty()) {
			// Look at first path
			path = queue.get(0);
			queue.remove(0);
			// Look at last element in path
			Entity last = path.get(path.size() - 1);
			// If it is what you want, done.
			if (to == last) {
				// path.stream().forEachOrdered(e -> Mark.say("Path element:", e));
				return new BetterSignal(true);
			}
			// Run through story, looking for causal elements in which last element is an antecedent
			List<Entity> consequents = mm.getStoryProcessor().getStory().stream().filter(e -> {
				return Predicates.isCause(e) && Predicates.contained(last, e.getSubject().getElements());
			}).map(c -> c.getObject()).collect(Collectors.toList());
			// For each consequent of last, add a new path to the queue

			for (Entity consequent : consequents) {
				if (Predicates.contained(consequent, path)) {
					continue;
				}
				List<Entity> newPath = new ArrayList<>();
				for (Entity x : path) {
					newPath.add(x);
				}
				newPath.add(consequent);
				queue.add(newPath);
			}
		}
		return new BetterSignal(false);
	}

	/**
	 * Checks beliefs to see in matching element is present.
	 */
	public BetterSignal isBelief(MentalModel mm, ProblemSolver ps, Entity assertion) {
		boolean debug = true;
		Mark.say(debug, "Entering isMyBelief", assertion.prettyPrint());
		List<MentalModel> traits = collectMentalModels(Markers.I, mm);
		for (MentalModel traitModel : traits) {
			Mark.say(debug, "Checking trait", traitModel.getType(), "for", assertion.prettyPrint());

			List<Entity> beliefs = traitModel.getExamples();
			Mark.say(debug, beliefs.size(), "elements");

			// reportQuestion(assertion, mm);

			for (Entity element : beliefs) {
				Mark.say(debug, "Checking trait element", element);
				if (element.relationP(Markers.BELIEVE) && (element.getSubject().getType().equalsIgnoreCase(Markers.I))) {
					Entity belief = RoleFrames.getObject(element);
					Mark.say(debug, "Checking belief", belief);
					// Mark.say("Belief ", belief);
					// Mark.say("Assertion", assertion);
					// Mark.say("Matching\n", belief.toXML(), "\n", assertion.toXML());
					Mark.say(debug, "Matching", "\n", assertion, "\n", belief);
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(belief, assertion);
					Mark.say(debug, "Bindings are", bindings);
					if (bindings != null) {
						// reportPositiveAnswer(element, mm);
						Mark.say(debug, "Returning true for belief", assertion);
						return new BetterSignal(true);
					}
				}
			}
		}
		Mark.say(debug, "Returning true for belief", assertion);
		return new BetterSignal(false);
	}

	/**
	 * Asks user if user believes something.
	 */
	public BetterSignal askAboutBelief(MentalModel mm, ProblemSolver ps, Entity argument) {
		boolean debug = false;
		boolean result = false;

		Entity question = RoleFrames.makeRoleFrame(Markers.I, "believe", argument);
		question = new Function(Markers.QUESTION, question);
		question.addType("do");
		String message = Generator.getGenerator().generate(question);
		Mark.say(debug, "\n>>> Asking human about belief");
		String[] options = new String[] { "Yes", "No", "I don't know" };
		int response = JOptionPane
		        .showOptionDialog(ABasicFrame
		                .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (response == JOptionPane.YES_OPTION) {
			result = true;
		}
		return new BetterSignal(result);
	}

	/**
	 * Inserts element, after dereferencing, into story.
	 */
	public BetterSignal insert(MentalModel mm, ProblemSolver ps, Entity argument) {
		boolean debug = true;
		Mark.say(debug, "Injecting", argument);
		mm.getStoryProcessor().injectElementWithDereference(argument);
		return new BetterSignal(true);
	}

	/**
	 * Checks to see if matching element is in the story.
	 */
	public BetterSignal inStory(MentalModel mm, ProblemSolver ps, Entity entity) {
		boolean debug = false;
		for (Entity element : mm.getStoryProcessor().getStory().getElements()) {
			if (StandardMatcher.getIdentityMatcher().match(entity, element) != null) {
				Mark.say(debug, "Matched\n", entity, "\n", element);
				return new BetterSignal(true);
			}
		}
		return new BetterSignal(false);
	}


	/**
	 * Finds antecedents of means connection whose consequent is matching element.
	 */
	public BetterSignal findRecipe(MentalModel mm, ProblemSolver ps, Entity entity) {
		// Mark.say("\n>>> Looking for recipe for", entity, "in story of length",
		// mm.getStoryProcessor().getStory().getElements().size());

		Sequence answer = new Sequence();

		for (Entity e : mm.getStoryProcessor().getStory().getElements()) {
			if (Predicates.isMeans(e)) {
				if (StandardMatcher.getIdentityMatcher().match(entity, e.getObject()) != null) {
					// Mark.say("Got it!");
					ProblemSolver.addSolutionElement(e, answer);
					return new BetterSignal(true, answer);
				}
			}
		}

		Entity roleFrame = RoleFrames.makeRoleFrame(entity, "have", "explanation");

		roleFrame.addFeature(Markers.NOT);

		ProblemSolver.addSolutionElement(roleFrame, answer);

		return new BetterSignal(false);
	}

	/**
	 * Checks story to see if two elements both appear and appear in first-second order.
	 */
	public BetterSignal isInOrder(MentalModel mm, ProblemSolver ps, Entity first, Entity second) {
		boolean debug = false;
		boolean moveOn = false;
		Mark.say(debug, "Is in order\n", first, "\n", second);
		for (Entity x : mm.getStoryProcessor().getStory().getElements()) {
			if (moveOn == false && StandardMatcher.getBasicMatcher().match(first, x) != null) {
				moveOn = true;
			}
			else if (moveOn == true && StandardMatcher.getBasicMatcher().match(second, x) != null) {
				Mark.say(debug, "It's true!");
				return new BetterSignal(true);
			}
			else {
			}
		}
		Mark.say(debug, "No, not true!");
		return new BetterSignal(false);
	}



	private List<MentalModel> collectMentalModels(String name, MentalModel mentalModel) {
		List<MentalModel> result = new ArrayList<>();
		Sequence story = mentalModel.getStoryProcessor().getStory();
		for (Entity element : new Vector<>(story.getElements())) {
			if (element.relationP(Markers.PERSONALITY_TRAIT) && (element.getSubject().getType().equalsIgnoreCase(name))) {
				String traitName = RoleFrames.getObject(element).getType();
				MentalModel traitModel = mentalModel.getLocalMentalModel(traitName);
				result.add(traitModel);
				// Mark.say("Adding", traitModel);
			}
		}
		return result;
	}

	private boolean isElement(Entity entity, Vector<Entity> explicits) {
		return explicits.contains(entity);
	}

	/**
	 * Helper method.
	 */
	private List<Entity> findExplicitAntecedents(Entity entity, MentalModel mm) {
		boolean debug = false;

		// Vector<Entity> story = storySequence.getElements();
		// Vector<Entity> explicits = explicitSequence.getElements();

		Vector<Entity> story = mm.getStoryProcessor().getStory().getElements();
		Vector<Entity> explicits = mm.getStoryProcessor().getExplicitElements().getElements();

		List<List<Entity>> queue = new ArrayList<>();

		List<Entity> ancestors = new ArrayList<>();

		List<Entity> path = new ArrayList<>();
		path.add(entity);
		queue.add(path);

		while (!queue.isEmpty()) {
			// Look at first path
			path = queue.get(0);
			queue.remove(0);
			// Look at last element in path
			Entity last = path.get(path.size() - 1);
			// If it is what you want, done, add it to antecedents
			if (last != entity && isElement(last, explicits)) {
				if (!ancestors.contains(last)) {
					ancestors.add(last);
				}
				continue;
			}
			// Run through story, looking for causal elements in which last element is a consequent
			for (Entity e : story) {
				if (Predicates.isCause(e) && !Predicates.isMeans(e) && e.getObject() == last) {
					for (Entity a : e.getSubject().getElements()) {
						// For each antecedent, add a new path to the queue
						List<Entity> newPath = new ArrayList();
						newPath.addAll(path);
						// Add new path unless circular
						if (!newPath.contains(a)) {
							newPath.add(a);
							queue.add(newPath);
						}
						else {
							Mark.err("findExplicitAntecedents found causal loop");
						}
					}
				}
			}
		}
		ancestors.stream().forEach(a -> Mark.say(debug, "Ancestor", a));
		return ancestors;
	}


	/**
	 * Helper method.
	 */
	public List<Entity> findExplicitConsequents(Entity entity, MentalModel mm) {
		Vector<Entity> story = mm.getStoryProcessor().getStory().getElements();
		Vector<Entity> explicits = mm.getStoryProcessor().getExplicitElements().getElements();

		List<List<Entity>> queue = new ArrayList<>();

		List<Entity> consequents = new ArrayList<>();

		List<Entity> path = new ArrayList<>();
		path.add(entity);
		queue.add(path);
		while (!queue.isEmpty()) {
			// Look at first path
			path = queue.get(0);
			queue.remove(0);
			// Look at last element in path
			Entity last = path.get(path.size() - 1);
			// // If it is what you want, done, add it to antecedents
			// if (last != entity && isElement(last, explicits)) {
			// if (!consequents.contains(last)) {
			// consequents.add(last);
			// }
			// continue;
			// }
			// Run through story, looking for causal elements in which last element is an antecedent
			boolean foundOne = false;
			for (Entity e : story) {
				if (Predicates.isCause(e) || Predicates.isMeans(e)) {
					if (e.getSubject().getElements().contains(last)) {
						// Add a new path to the queue
						List<Entity> newPath = new ArrayList();
						newPath.addAll(path);
						foundOne = true;
						// Add new path unless circular
						if (!newPath.contains(e.getObject())) {
							newPath.add(e.getObject());
							queue.add(newPath);
						}
						else {
							Mark.err("findExplicitConsequents found causal loop");
						}
					}
				}
			}
			// If no explicit consequent of inexplicit consequent, use inexplicit consequent
			if (!foundOne && last != entity) {
				consequents.add(last);
			}
		}
		// consequents.stream().forEach(a -> Mark.say("Descendant", a));
		return consequents;
	}



	public BetterSignal findMostConnected(MentalModel mm, ProblemSolver ps) {
		Mark.err("Not ready: looking for most connected in story of length", mm.getStoryProcessor().getStory().getElements().size());
		return new BetterSignal(true);
	}

	public BetterSignal explainMostConnected(MentalModel mm, ProblemSolver ps, Entity argument) {
		Mark.err("Not ready: explaining why", argument, "is most connected in story of length", mm.getStoryProcessor().getStory().getElements()
		        .size());
		return new BetterSignal(true);
	}

	public BetterSignal unexplained(MentalModel mm, ProblemSolver ps) {

		List<Entity> unexplained = mm.getStoryProcessor().getStory().stream()
		        .filter(e -> !e.isA(Markers.CAUSE_MARKER) && !e.isA(Markers.CLASSIFICATION_MARKER)
		                && mm.getStoryProcessor().getStory().stream()
		                        .anyMatch(x -> x.isA(Markers.CAUSE_MARKER) && x.getSubject().getElements().contains(e))
		                && !mm.getStoryProcessor().getStory().stream().anyMatch(x -> x.isA(Markers.MEANS) && x.getSubject().getElements().contains(e))
		                && !(mm.getStoryProcessor().getStory().stream().anyMatch(x -> x.isA(Markers.CAUSE_MARKER) && e == x.getObject())))
		        .collect(Collectors.toList());
		Sequence answer = new Sequence();
		for (Entity e : unexplained) {
			if (!answer.getElements().contains(e)) {
				// Mark.say("Adding", e.asStringWithIndexes());
				answer.addElement(e);
			}
		}
		return new BetterSignal(!answer.getElements().isEmpty(), answer);
	}

	public BetterSignal listConcepts(MentalModel mm, ProblemSolver ps) {
		List<String> names = mm.getStoryProcessor().getInstantiatedConceptPatterns().stream().map(c -> c.getName()).collect(Collectors.toList());
		List<ConceptDescription> dominant = Summarizer.limitToDominantConcepts(mm.getStoryProcessor().getConceptAnalysis().getConceptDescriptions());
		Sequence answers = new Sequence();
		if (dominant.size() > 0) {
			String main = dominant.get(0).getName();
			Entity rf = RoleFrames.makeRoleFrame("story", "be");
			RoleFrames.addRole(rf, Markers.ABOUT_MARKER, adjectivize(main));

			Entity x = RoleFrames.makeRoleFrame("I", "think", rf);

			answers.addElement(rf);
			// answers.addElement(x);
			return new BetterSignal(true, answers);
		}
		dominant.stream().forEach(n -> Mark.say("Dominant name is", n));
		Entity a = RoleFrames.makeRoleFrame("I", "find", "concepts");
		a.addFeature(Markers.NOT);
		Entity c = RoleFrames.makeRoleFrame("I", "know");
		c.addFeature(Markers.NOT);
		Entity x = Rules.makeCause(c, a);
		answers.addElement(x);
		return new BetterSignal(false, answers);
	}

	private Entity adjectivize(String main) {
		if (main.indexOf(" ") > 0) {
			String[] split = main.split(" ");
			List<String> pieces = Arrays.asList(split);
			Entity e = new Entity(pieces.get(pieces.size() - 1));
			pieces.subList(0, pieces.size() - 1).stream().forEachOrdered(p -> e.addFeature(p));
			return e;
		}
		return new Entity(main);
	}

	public BetterSignal jdParkFastHand(MentalModel mm, ProblemSolver ps) {
		// Mark.say("+++++++++++++++++ Parking fast arm");
		GenesisGetters.getBlocksWorld().getModel().parkFastHand();
		return new BetterSignal(true);

	}

	public BetterSignal jdParkStrongHand(MentalModel mm, ProblemSolver ps) {

		// Mark.say("+++++++++++++++++ Parking strong arm");
		GenesisGetters.getBlocksWorld().getModel().parkStrongHand();
		return new BetterSignal(true);
	}


	public BetterSignal jdPutOn(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		return jdPutOnWithSpeed(mm, ps, travelor, support);
	}

	public BetterSignal jdPutOnWithSpeed(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			zCommand("in, " + Z.getFullName(travelor) + ", "+Z.getFullName(support));
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			// Cagri's call goes here.
			Mark.say("Telling robot to move ",travelor.getType(), " to ", support.getType());
			RobotInterface.placeXonY(travelor.getType(), support.getType());

			while(!RobotInterface.isRobotReady()){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			} //Changed this for thread safety.

			return new BetterSignal(true);
		}
		else {
			// Mark.say("Calling phw version of put on");
			return jdPutOnWithSpeedPHW(mm, ps, travelor, support);
		}

	}

	public BetterSignal jdPutOver(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		return jdPutOverWithSpeed(mm, ps, travelor, support);
	}

	public BetterSignal jdPutOverWithSpeed(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			zCommand("over, " + Z.getFullName(travelor) + ", "+Z.getFullName(support));
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			// Cagri's call goes here.
			Mark.say("Cagri's robot does not know how to do this");
			return new BetterSignal(false);
		}
		else {
			// Mark.say("Calling phw version of put on");
			return jdPutOverWithSpeedPHW(mm, ps, travelor, support);
		}

	}

	public BetterSignal jdPutOnWithSpeedPHW(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		Block t = getBlock(travelor);
		Block s = getBlock(support);

		if (s instanceof Brick) {
			GenesisGetters.getBlocksWorld().getModel().putOnWithSpeed(t, (Brick) s, null);
			return new BetterSignal(true);
		}
		else {
			Mark.err("Cannot put someing on something that is not a brick");
			return new BetterSignal(false);
		}
	}

	public BetterSignal jdPutOverWithSpeedPHW(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		Block t = getBlock(travelor);
		Block s = getBlock(support);

		if (s instanceof Brick) {
			GenesisGetters.getBlocksWorld().getModel().putOver(t, (Brick) s, null);
			return new BetterSignal(true);
		}
		else {
			Mark.err("Cannot put someing on something that is not a brick");
			return new BetterSignal(false);
		}
	}

	public BetterSignal jdTilt(MentalModel mm, ProblemSolver ps, Entity block) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			zCommand("rotate");
			return new BetterSignal(true);
		}

		Block t = getBlock(block);
		Mark.say("Block to be tilted is", t, "from", block);



		GenesisGetters.getBlocksWorld().getModel().tilt(t, null);
		return new BetterSignal(true);
	}

	public BetterSignal jdAskStrongHelper(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {

		boolean debug = true;
		Mark.say(true, "Helper helping!!!!!!!!!!!!!!!!!!!!!!!");

		ProblemSolver helper = ps.getNovice();

		helper.readData("Problem solving helper stories.txt");

		Entity request2 = Translator.getTranslator().translateToEntity("Put " + travelor.getType() + " on " + support.getType());

		Entity request = RoleFrames.makeRoleFrame(new Entity(Markers.YOU), "put", travelor);

		// Nothing happens, may need note what tracing actually does with arguments

		// ps.traceIn(2, "Atlas helping out problem solver", new Sequence());
		// helper.traceIn(2, "Atlas helping out helper", new Sequence());

		RoleFrames.addRole(request, "on", support);

		// Request should work, but doesn't even thought looks like request2

		Mark.say(debug, "Request ", request);
		Mark.say(debug, "Request2", request2);

		Mark.say(debug, "Match results:", StandardMatcher.getBasicMatcher().match(request, request2));

		BetterSignal solution = helper.solve(0, request2, new Sequence(), null, mm);

		return new BetterSignal(solution.isTrue(), new Sequence());

	}

	public static BetterSignal jdAskForHelper(int level, MentalModel mm, ProblemSolver ps, Entity request) {

		boolean debug = false;
		Mark.say(debug, "Helper helping");

		ProblemSolver helper = ps.getNovice();

		helper.readData("Problem solving helper stories.txt");

		BetterSignal solution = helper.solve(level, request, new Sequence(), null, mm);

		Mark.say(debug, "Helper produced", solution.isTrue());

		return new BetterSignal(solution.isTrue(), new Sequence());

	}

	public BetterSignal jdPutOnWithStrength(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {

		// Mark.say("+++++++++++++++++ Using strong arm");
		Block t = getBlock(travelor);
		Block s = getBlock(support);

		if (s instanceof Brick) {
			GenesisGetters.getBlocksWorld().getModel().putOnWithStrength(t, (Brick) s, null);
			return new BetterSignal(true);
		}
		else {
			Mark.err("Cannot put someing on something that is not a brick");
			return new BetterSignal(false);
		}
	}

	public BetterSignal jdFindSpace(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			//Cagri's simulator.
			return new BetterSignal(RobotInterface.hasSpace(support.getType()));
		}

			return jdFindSpacePHW(mm, ps, travelor, support);

	}

	public BetterSignal jdFindSpacePHW(MentalModel mm, ProblemSolver ps, Entity travelor, Entity support) {
		boolean debug = false;
		Mark.say(debug, "Time to find space for", travelor, "on", support);

		Block t = getBlock(travelor);
		Block s = getBlock(support);

		Mark.say(debug, "That is, space for", t, "on", s);

		if (s instanceof Brick) {
			if (GenesisGetters.getBlocksWorld().getModel().findLocation(t, (Brick) s) != null) {
				return new BetterSignal(true);
			}
			else {
				return new BetterSignal(false);
			}

		}
		else {
			Mark.err("Cannot put someing on something that is not a brick");
			return new BetterSignal(false);
		}
	}

	public BetterSignal jdClearTop(MentalModel mm, ProblemSolver ps, Entity target) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			if(!RobotInterface.hasSpace(target.getType())){
				Entity you = new Entity("you");
				String supportedName=RobotInterface.whatIsOnTop(target.getType());
				if(supportedName!=null){
					Entity supported = new Entity(supportedName);
					Entity command = RoleFrames.makeRoleFrame(you, "put", supported.getName(), "on","Ground");
					Mark.say("Should get rid of", supported);
				}

			}
			return new BetterSignal(true);
		}

			return jdClearTopPHW(mm, ps, target);

	}
	public BetterSignal jdClearTopPHW(MentalModel mm, ProblemSolver ps, Entity target) {
		Mark.say("Time to clear the top of", target);
		Block t = getBlock(target);
		if (t instanceof Brick) {
			Brick brick = (Brick) t;
			Vector<Block> supported = brick.getSupported();
			if (supported.isEmpty()) {
				return new BetterSignal(true);
			}
			Block b = supported.get(0);
			Entity you = new Entity("you");
			Entity command = RoleFrames.makeRoleFrame(you, "put", new Entity(b.getName()),

			        "on", "table0"

			);

			Mark.say("Should get rid of", supported);

			BetterSignal bs = new BetterSignal(true);
			// ps.processInput(bs);
			return new BetterSignal(true);
		}
		else {
			Mark.err("Cannot clear the top of something that is not a brick");
			return new BetterSignal(false);
		}
	}


	public BetterSignal jdHasClearTop(MentalModel mm, ProblemSolver ps, Entity target) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
		Mark.say("Checking if ", target.getType(), " has clear top on Simulator");
		return new BetterSignal(RobotInterface.hasSpace(target.getType()));
	}
	return jdHasClearTopPHW(mm, ps, target);
	}
	public BetterSignal jdHasClearTopPHW(MentalModel mm, ProblemSolver ps, Entity target) {
		boolean debug = false;
		Mark.say(debug, "Time to check top of", target);
		Block t = getBlock(target);
		if (t instanceof Brick) {
			boolean result = GenesisGetters.getBlocksWorld().getModel().hasClearTop((Brick) t, null);
			if (result) {
				Mark.say(debug, "Top is clear");
			}
			else {
				Mark.say(debug, "Top is not clear");
			}
			return new BetterSignal(result);
		}
		else {
			Mark.err("Cannot clear the top of", t, "because is not a brick");
			return new BetterSignal(false);
		}
	}
	public BetterSignal jdIsLight(MentalModel mm, ProblemSolver ps, Entity e) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			//There is no heavy item in the simulator. For now..
			return new BetterSignal(true,new Sequence());

		}
		return jdIsLightPHW(mm, ps, e);
	}

	public BetterSignal jdIsLightPHW(MentalModel mm, ProblemSolver ps, Entity e) {
		Block block = getBlock(e);
		// Mark.say("isLight block is", block);
		return new BetterSignal(block.isLight(), new Sequence());
	}

	public BetterSignal jdIsHeavy(MentalModel mm, ProblemSolver ps, Entity e) {
		Block block = getBlock(e);
		// Mark.say("!!!!!!!!!!!!!!! Calling jdIsHeavy on", block, block.isHeavy());
		return new BetterSignal(block.isHeavy(), new Sequence());
	}

	public BetterSignal jdIsTall(MentalModel mm, ProblemSolver ps, Entity e) {
		Block block = getBlock(e);
		// Mark.say("!!!!!!!!!!!!!!! Calling jdIsHeavy on", block, block.isHeavy());
		return new BetterSignal(block.isTall(), new Sequence());
	}

	public BetterSignal jdSupportedBy(MentalModel mm, ProblemSolver ps, Entity target, Entity supported) {
		boolean debug=false;
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
		Mark.say("Target type is ", target.getType());
		String ontop = RobotInterface.whatIsOnTop(target.getType());
		Mark.say("What we are looking for as supported is " , RobotInterface.nameFromGenesis(supported.getType()));
		Mark.say("What we have as supported is ", RobotInterface.nameFromGenesis(ontop));
		if(RobotInterface.nameFromGenesis(supported.getType()).equals(RobotInterface.nameFromGenesis(ontop))&& ontop!=null){
			return new BetterSignal(true);
		}

		Mark.say(debug,"Not true ",target, "does not support",supported);
		return new BetterSignal(false);
	}

	return jdSupportedByPHW(mm, ps, target, supported);

	}
	public BetterSignal jdSupportedByPHW(MentalModel mm, ProblemSolver ps, Entity target, Entity supported) {
		boolean debug = false;
		Mark.say(debug, "in jdSupportedBy with", target, supported);
		try {
			Thread.sleep(2000);
		}
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Block t = getBlock(target);
		Block s = getBlock(supported);

		// String tName = t.getName();
		// String sName = s.getName();

		Sequence answer = new Sequence();

		Mark.say(debug, "Checking whether", t, "supports", s);

		if (t.supportsP(s)) {
			Mark.say(debug, "It is true!", t, "supports", s);
			// Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", supported, "to", target);
			// ProblemSolver.addSolutionElement(rf, answer);
				return new BetterSignal(true, answer);
			}
		Mark.say(debug, "Not true,", t, "does not support", s);

		return new BetterSignal(false, answer);
	}


	public BetterSignal jdSupported(MentalModel mm, ProblemSolver ps, Entity target, Entity supported) {
		boolean debug = false;
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			Sequence answer=new Sequence();
			String ontop = RobotInterface.whatIsOnTop(target.getType());
			Mark.say("Cagri's jdSupported call ++++++++++++++++++++++++");
			Mark.say(ontop);
			if(ontop!=null){
				Entity e = new Entity(ontop);
				Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", supported,"to",e);
				Mark.say(debug,"Supported",target.getType(),ontop,rf);
				ProblemSolver.addSolutionElement(rf, answer);
				return new BetterSignal(true, answer);
			}
			Mark.say("No supports found");
			return new BetterSignal(false, answer);
		}
		return jdSupportedPHW(mm, ps, target, supported);
	}
	public BetterSignal jdSupportedPHW(MentalModel mm, ProblemSolver ps, Entity target, Entity supported) {
		boolean debug = false;
		Mark.say(debug, "Entering jdSupported with target/supported\n", target, "\n", supported);
		Block s = getBlock(target);
		Mark.say(debug, "Continuing....s is", s);
		Mark.say(debug, target, "supports", ((Brick) s).getSupported());
		Sequence answer = new Sequence();
		for (Object b : ((Brick) s).getSupported()) {
			Entity e = new Entity(((Brick) b).getName());

			String magic = getPseudonym(((Block) b));

			Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", supported, "to", e);
			Mark.say(debug, "Supported", b, e, rf);
			ProblemSolver.addSolutionElement(rf, answer);
			return new BetterSignal(true, answer);

		}
		Mark.say(debug, "No supports found");
		return new BetterSignal(false, answer);
	}

	Map<String, String> memory = new HashMap<>();

	public BetterSignal jdRemember(MentalModel mm, ProblemSolver ps, Entity name, Entity block) {
		// Mark.say("Entered jdRemember with name/block", name, block);
		// memory.add(e);
		// memory.put(name.getType(), block.getType());
		memory.put(block.getType(), name.getType());
		return new BetterSignal(true, new Sequence());
	}

	private String getPseudonym(Block block) {
		for (String key : memory.keySet()) {
			String value = memory.get(key);
			if (block.getName().equalsIgnoreCase(value)) {
				return key;
			}
		}
		return block.getName();
	}

	private Block getBlock(Entity e) {
		boolean debug = false;
		Vector blocks = GenesisGetters.getBlocksWorld().getModel().getBlocks();

		String name = e.getType();

		String association = memory.get(name);

		Mark.say(debug, "Entity/name/association", e, name, association);

		if (association != null) {
			name = association;
		}

		for (Object b : blocks) {
			Block x = (Block) b;
			Mark.say(debug, "Testing", name, "vs", x);
			if (x instanceof Block && ((Block) b).getName().equalsIgnoreCase(name)) {
				return (Block) b;
			}
		}
		return null;
	}

	public BetterSignal jdTestCellPhone(MentalModel mm, ProblemSolver ps) {
		if (Radio.justPlan.isSelected()) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			return new BetterSignal(true);
		}
		else if (Radio.robotSimulator.isSelected()) {
			RobotInterface.rotatePhone();
			while(!RobotInterface.isRobotReady()){try{Thread.sleep(200);}catch(InterruptedException e){};}
			RobotInterface.testPhone();
		}
		return jdTestCellPhonePHW(mm, ps);
	}
	public BetterSignal jdTestCellPhonePHW(MentalModel mm, ProblemSolver ps) {
		boolean debug = false;
		boolean result = false;

		Mark.say("In test cell phone!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		String message = "Does it work?";

		Mark.say(debug, "\n>>> Asking human about belief");
		String[] options = new String[] { "Yes", "No", "I don't know" };
		int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (response == JOptionPane.YES_OPTION) {
			result = true;
		}
		return new BetterSignal(result);
	}



	public BetterSignal jdAskIfType(MentalModel mm, ProblemSolver ps, Entity x, Entity y) {
		boolean debug = false;
		boolean result = false;

		Mark.say("In jdAskIfType!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		Entity question = new Relation("instance", y, x);

		question = new Function(Markers.QUESTION_MARKER, question);

		question.addType(Markers.DID_QUESTION);

		String message = Generator.getGenerator().generate(question);

		Mark.say("Message", question.toXML());

		Mark.say("Message", message);

		Mark.say(debug, "\n>>> Asking human about type");
		String[] options = new String[] { "Yes", "No", "I don't know" };
		int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (response == JOptionPane.YES_OPTION) {
			result = true;
		}
		return new BetterSignal(result);
	}
	// public BetterSignal jdBind(MentalModel mm, ProblemSolver ps, Entity variable, Entity type) {
	// Mark.say("Entered jdBind", variable, type);
	// Sequence answer = new Sequence();
	// for (Entity e : memory) {
	// if (e.isA(type.getType())) {
	// Mark.say("Got it!!!", e.toXML());
	// Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", variable, "to", e);
	// Mark.say("Got it+++", rf);
	// ProblemSolver.addSolutionElement(rf, answer);
	// return new BetterSignal(true, answer);
	// }
	// }
	// return new BetterSignal(false, new Sequence());
	// }


	public static BetterSignal jdAsk(Entity entity) {

		boolean debug = true;
		boolean result = false;

		Mark.say("In asking test!!!!!!!!!!!!!!!!!!!!!!!!!!!");

		Mark.say("Entity is:", entity);

		// Prof Winston's
//		String message = Generator.getGenerator().generate(entity);
//
//		Mark.say(debug, "\n>>> Asking human about belief");
//		
//		String[] options = new String[] { "Succeeded", "Failed" };
		
		// Zhutian's version on 180916
		Mark.say(debug, "\n>>> Asking human about belief");
		String[] options;
		String message;
		Mark.say("----------------------------------------------------------------------------------------------------");
		Z.understand(entity);
		entity = Z.translateAgain(entity);
		Z.understand(entity);
		if (Z.isImperative(entity)) {
			options = new String[] { "Succeeded", "Failed" };
			message = Generator.getGenerator().generate(entity);
		} else {
			options = new String[] { "Yes", "No" };
			message = Z.yesNoQuestion(entity);
		}
		
		int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (response == JOptionPane.YES_OPTION) {
			result = true;
		}
		return new BetterSignal(result);
	}
	
	
	// ===========================================================================================================
	//
	//      Zhutian's learner problem solving
	//
	// ===========================================================================================================

	public static String zCommand(String command) {
		if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			try{      
				command = command.replace("black white", "black and white");  // for the bottle of nuts
				Mark.say("JustDoIt for robot: "+command);
			    Socket soc = new Socket("localhost",2004);  
			    
			    // Send the message to the server
			    DataOutputStream dout=new DataOutputStream(soc.getOutputStream());  
			    dout.writeUTF(command+"\n");
			    dout.flush();
			    System.out.println("Message sent to the server : " +command);
			     
			    // Get the return message from the server
			    String readedLine = null;
			    Mark.say("111 "+new Date());
			    BufferedReader din = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			    Mark.say("222 "+new Date());
			    readedLine = din.toString();
	            
			    while ((readedLine = din.readLine()) != null)  {
	            	
	                // Upon response of succcess, continue problem solving
			    	System.out.println("Message received from the server : " +readedLine);
	                dout.close();
	                Mark.say("333 "+new Date());
	                din.close();
	                Mark.say("444 "+new Date());
	    		    soc.close();
	    		    Mark.say("555 "+new Date());
	    		    return readedLine;
	            }
	            
			} catch(Exception e){
			    e.printStackTrace();
			}  
		}
		return null;
	}
	
	
	public static BetterSignal zAsk(Entity entity) {
		
		Mark.say("In asking test!!!!!!!!!!!!!!!!!!!!!!!!!!!");

//		Mark.mit("Entity is:", entity);
//		Z.understand(entity);

		// zhutian's version on 22 Oct 2018
		Mark.mit("before: ",questionsAsked);
		String condition = Generator.getGenerator().generate(entity);

		Mark.mit(condition);
		
		// if the same question has been asked
		if(questionsAsked.containsKey(condition)) {
			Mark.mit("has answered the same question ======================");
			if (questionsAsked.get(condition) == true) {
				Mark.show(condition, true);
				return new BetterSignal(true);
			} else {
				Mark.show(condition, false);
				return new BetterSignal(false);
			}
		}

		// if an opposite question has been asked
		if(Z.isTranslatable(condition)) {
			String oldKeyIfAny = RecipeExpert.oppositeQuestion(questionsAsked,condition);
			if(!oldKeyIfAny.equals("")) {
				Mark.mit("has answered the opposite question ======================");
				if(questionsAsked.get(oldKeyIfAny)==false) {
					Mark.show(condition, true);
					questionsAsked.put(condition,true);
					return new BetterSignal(true);
				} else if(questionsAsked.get(oldKeyIfAny)==true) {
					Mark.show(condition, false);
					questionsAsked.put(condition,false);
					return new BetterSignal(false);
				}
			}
		}
		
		Boolean reverse = false;
		if(entity.getFeatures().contains(Markers.NOT)) reverse = true;

		// -------------------------------------------------------------------------------
		// if question can be automatically answered
		// -------------------------------------------------------------------------------
		if(entity.getType().equals(Z.INSTANCE)) {
			Mark.mit("INSTANCE OF ======================");
//			Z.understand(entity);
			Boolean askContains = false;
			Boolean answer = Z.isInstance(entity.getObject(),entity.getSubject())
					&&!foundContained.contains(entity.getObject());
			if(reverse) {
				if (answer==false) {
					answer = true;
				} else {
					answer = false;
				}
			} else {
				askContains = true;
			}
			Mark.show(condition, answer);
			if(answer==false) {
				questionsAsked.put(condition,false);
				return new BetterSignal(false);
			} else if(answer==true) {
				if(askContains) 
					questionsAsked.put(condition,true);
				return new BetterSignal(true);
			}
		}
		
		if(entity.getType().equals(Z.OBSERVE)) {
			Boolean result = false;
			String name = Z.getFullName(entity.getObject().getElement(0).getSubject());
			
			if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
				Mark.mit("looking for ======================");
				
				if (name.startsWith("a ") || name.startsWith("an ") || name.startsWith("the ")) {
					name = name.substring(name.indexOf(" ")+1);
				}
				String answer = zCommand("find, " + name);
				Mark.night("robot answered ======================");
				Mark.night("look for " + name,": ", answer);
				
				// send response
				if (answer.equals(Z.TRUE)) {
					questionsAsked.put(condition,true);
					result = true;
				} else if (answer.equals(Z.FALSE)) {
					questionsAsked.put(condition,false);
				}
			} else {
				if(name.toString().contains("cup") || name.toString().contains("bottle")
						 || name.toString().contains("phone")
						 || name.toString().contains("battery")) {
					result = true;
				} 
				if(reverse) {
					if(result == true) result = false;
					else result = true;
				}
				if(result == true) Mark.night("assume true ======================");
				else Mark.night("assume false ======================");
			}
			return new BetterSignal(result);
		}
		
		// -------------------------------------------------------------------------------

		// if new question and worth asking
		String message = Z.yesNoQuestion(entity);
		String answer = "";
		if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			answer = zCommand("ask, " + message);
			if(answer.startsWith(" ")) answer = answer.substring(1);
			Mark.night("robot answered ======================");
			Mark.night(message,": ", answer, answer.length());
		} else {
			String[] options = new String[] { "Yes", "No", "I don't know" };
			int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (response == JOptionPane.YES_OPTION) answer = Z.YES_WORD;
			else if (response == JOptionPane.NO_OPTION) answer = Z.NO_WORD;
			else answer = Z.DONT_KNOW;
		}
		
		if(reverse) {
			if (answer.equals(Z.YES_WORD)) {
				answer = Z.NO_WORD;
			} else if (answer.equals(Z.NO_WORD)) {
				answer = Z.YES_WORD;
			}
		}
		
		// send response
		if (answer.equals(Z.YES_WORD)) {
			questionsAsked.put(condition,true);
			return new BetterSignal(true);
		} else if (answer.equals(Z.NO_WORD)) {
			questionsAsked.put(condition,false);
			return new BetterSignal(false);
		} else {
			return new BetterSignal(false);
		}
		
	}
	
	
	public BetterSignal zCheckStore(MentalModel mm, ProblemSolver ps, Entity figure, Entity ground) {
		
		Mark.night(figure);
		String question = Z.whereQuestion(figure);
	    String place = "";
	    
	    Mark.night("----------", foundContained);
	    Mark.night("----------", figure);
	    List<Entity> sources = new ArrayList<>();
	    if(foundContained.contains(figure)) {
	    	sources = multipleContained.get(figure);
	    	
	    } else {
	    	if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
				// Use Zhutian's code here, modify following line accordingly
				place = zCommand("ask, " + question);
			} else {
				place = JOptionPane.showInputDialog(ABasicFrame.getTheFrame(), question);
			}
	    	sources = Z.place2Ground(place);
	    	multipleContained.put(figure, sources);
	    	foundContained.add(figure);
	    }
	    
	    Sequence answer = new Sequence();
	    if(sources.size()>1) {
	    	Mark.mit(sources);
	    	return new BetterSignal(false);
	    } else {
	    	Entity source = sources.get(0);
	    	Mark.mit(source);
		    if(source!=null) {
		    	Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground, "to", source);
		    	Mark.mit("assign", ground, "to", source);
		    	ground = source;
		    	ProblemSolver.addSolutionElement(rf, answer);
		    	Mark.mit(answer);
				return new BetterSignal(true, answer);
		    }
			return new BetterSignal(false, answer);
	    }
	}
	
	public BetterSignal zCheckStoreTwo(MentalModel mm, ProblemSolver ps, Entity figure, Entity ground, Entity ground2) {
		
		Mark.night(figure);
		List<Entity> sources = multipleContained.get(figure);
	    Sequence answer = new Sequence();
	    
	    if(sources.size()==2) {
	    	Entity source = sources.get(0);
    		Mark.mit(source);
	    	Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground, "to", source);
	    	Mark.mit("assign", ground, "to", source);
	    	ground = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(1);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground2, "to", source);
	    	Mark.mit("assign", ground2, "to", source);
	    	ground2 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	return new BetterSignal(true, answer);
	    }
	    return new BetterSignal(false);
	}
	
	public BetterSignal zCheckStoreThree(MentalModel mm, ProblemSolver ps, Entity figure, Entity ground, Entity ground2, Entity ground3) {
		
		Mark.night(figure);
		List<Entity> sources = multipleContained.get(figure);
	    Sequence answer = new Sequence();
	    
	    if(sources.size()==3) {
	    	Entity source = sources.get(0);
    		Mark.mit(source);
	    	Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground, "to", source);
	    	Mark.mit("assign", ground, "to", source);
	    	ground = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(1);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground2, "to", source);
	    	Mark.mit("assign", ground2, "to", source);
	    	ground2 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(2);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground3, "to", source);
	    	Mark.mit("assign", ground3, "to", source);
	    	ground3 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	return new BetterSignal(true, answer);
	    }
	    return new BetterSignal(false, answer);
	}
	
	public BetterSignal zCheckStoreFour(MentalModel mm, ProblemSolver ps, Entity figure, Entity ground, Entity ground2, Entity ground3, Entity ground4) {
		
		Mark.night(figure);
		List<Entity> sources = multipleContained.get(figure);
	    Sequence answer = new Sequence();
	    
	    if(sources.size()==4) {
	    	Entity source = sources.get(0);
    		Mark.mit(source);
	    	Entity rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground, "to", source);
	    	Mark.mit("assign", ground, "to", source);
	    	ground = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(1);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground2, "to", source);
	    	Mark.mit("assign", ground2, "to", source);
	    	ground2 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(2);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground3, "to", source);
	    	Mark.mit("assign", ground3, "to", source);
	    	ground3 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	source = sources.get(3);
    		Mark.mit(source);
	    	rf = RoleFrames.makeRoleFrame(mm.getI(), "assign", ground4, "to", source);
	    	Mark.mit("assign", ground4, "to", source);
	    	ground4 = source;
	    	ProblemSolver.addSolutionElement(rf, answer);
	    	
	    	return new BetterSignal(true, answer);
	    }
	    return new BetterSignal(false, answer);
	}


	
	public BetterSignal zAssumeSuccess(MentalModel mm, ProblemSolver ps) {
		return new BetterSignal(true);
	}
	
	public BetterSignal zPrint(MentalModel mm, ProblemSolver ps, Entity entity) {

		String action = entity.toString();
		Mark.say("+++ received +++++++++++++++++ ", action);
		action = action.substring(5,action.lastIndexOf("-"));
		Mark.say("+++ subed +++++++++++++++++ ", action);
		action = action.replace("_", " ");
		Mark.say("+++ extracted +++++++++++++++++ ", action);

		return new BetterSignal(true);
	}
	
	public BetterSignal zOnTable(MentalModel mm, ProblemSolver ps, Entity entity) {

		if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
			// Use Zhutian's code here, modify following line accordingly
			String name = Z.getFullName(entity);
			if (name.startsWith("a ") || name.startsWith("an ") || name.startsWith("the ")) {
				name = name.substring(name.indexOf(" "));
			}
			zCommand("look, " + name);
			return new BetterSignal(true);
		} 
		return new BetterSignal(true);
	}

	public void updateCondition(Object object) {
		conditions = (List<String>) object;
	}
	
	

	
//------------------------------------ NOT USED ---------------------------------------------------------

//	public BetterSignal zAsk(String command, String expected) {
//		
//		if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO) {
//			// Use Zhutian's code here, modify following line accordingly
//			zCommand("ask, " + command + "," + expected);
//			return new BetterSignal(true);
//		} 
//		return new BetterSignal(false);
//	}
	
//	public BetterSignal zCheckInstanceNot(MentalModel mm, ProblemSolver ps, Entity token, Entity type) {
//		return new BetterSignal((Z.isInstance(token,type)&&!foundContained.contains(token))?false:true);
//	}
//	
//	public BetterSignal zCheckInstance(MentalModel mm, ProblemSolver ps, Entity token, Entity type) {
//		return new BetterSignal(Z.isInstance(token,type));
//	}
	
//	public BetterSignal zCheck(MentalModel mm, ProblemSolver ps) {
//		boolean debug = true;
//		boolean result = false;
//
//		String message = "Are you making the salad for me?";
//		message = Z.sentence2Chat(message);
//		if (Radio.realRobot.isSelected() && RecipeExpert.ROBOT_DEMO)) {
//			return zAsk(message, Markers.YES);
//		} else {
//			Mark.say(debug, "\n>>> Asking human about belief");
//			String[] options = new String[] { "Yes", "No", "I don't know" };
//			int response = JOptionPane.showOptionDialog(ABasicFrame
//			        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//			if (response == JOptionPane.YES_OPTION) {
//				result = true;
//			}
//			return new BetterSignal(result);
//		}
//		
//	}
	
//	public BetterSignal jdInsertFruits(MentalModel mm, ProblemSolver ps) {
//		boolean debug = true;
//		boolean result = false;
//
//		Mark.say("In test insert fruit!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//		String message = "Fruit inserted?";
//
//		Mark.say(debug, "\n>>> Asking human about belief");
//		String[] options = new String[] { "Yes", "No", "I don't know" };
//		int response = JOptionPane.showOptionDialog(ABasicFrame
//		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//		if (response == JOptionPane.YES_OPTION) {
//			result = true;
//		}
//		return new BetterSignal(result);
//	}
//
//	public BetterSignal jdInsertSugar(MentalModel mm, ProblemSolver ps) {
//		boolean debug = true;
//		boolean result = false;
//
//		Mark.say("In test insert sugar!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//		String message = "Sugar inserted?";
//
//		Mark.say(debug, "\n>>> Asking human about belief");
//		String[] options = new String[] { "Yes", "No", "I don't know" };
//		int response = JOptionPane.showOptionDialog(ABasicFrame
//		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//		if (response == JOptionPane.YES_OPTION) {
//			result = true;
//		}
//		return new BetterSignal(result);
//	}
//
//
//	public BetterSignal jdILikeSugar(MentalModel mm, ProblemSolver ps) {
//		boolean debug = true;
//		boolean result = false;
//
//		Mark.say("In test I like sugar!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//		String message = "Do I like sugar?";
//
//		Mark.say(debug, "\n>>> Asking human about belief");
//		String[] options = new String[] { "Yes", "No", "I don't know" };
//		int response = JOptionPane.showOptionDialog(ABasicFrame
//		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//		if (response == JOptionPane.YES_OPTION) {
//			result = true;
//		}
//		return new BetterSignal(result);
//	}
//
//	public BetterSignal jdIDoNotLikeSugar(MentalModel mm, ProblemSolver ps) {
//		boolean debug = true;
//		boolean result = false;
//
//		Mark.say("In test I like sugar!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//		String message = "Do I not like sugar?";
//
//		Mark.say(debug, "\n>>> Asking human about belief");
//		String[] options = new String[] { "Yes", "No", "I don't know" };
//		int response = JOptionPane.showOptionDialog(ABasicFrame
//		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
//		if (response == JOptionPane.YES_OPTION) {
//			result = true;
//		}
//		return new BetterSignal(result);
//	}

}


//// BEGIN CLASS ADDED BY TRISTAN FOR BHPN JUST DO IT METHODS //
//class BHPNStorySubscriber implements LCMSubscriber {
//
//	private string_tensor_2d_t responseMsg = null;
//	private final String channel;
//	private final LCM lcm;
//
//	public BHPNStorySubscriber(String channel, LCM lcm) throws IOException {
//		this.channel = channel;
//		this.lcm = lcm;
//		this.lcm.subscribe(channel, this);
//	}
//
//	public string_tensor_2d_t getCurrentResponseMsg() {
//		return this.responseMsg;
//	}
//
//	@Override
//	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
//		if (channel.equals(this.channel)) {
//			string_tensor_2d_t msg;
//			try {
//				msg = new string_tensor_2d_t(ins);
//			} catch (IOException ex) {
//				System.out.println("Error decoding message on channel '" + channel + "': " + ex);
//				return;
//			}
//			this.responseMsg = msg;
//		}
//	}
//}
////END CLASS ADDED BY TRISTAN FOR BHPN JUST DO IT METHODS ////



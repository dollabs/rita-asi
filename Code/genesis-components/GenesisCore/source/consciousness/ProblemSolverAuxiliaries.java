package consciousness;

import java.util.*;
import java.util.stream.Collectors;

import connections.*;
import connections.signals.BetterSignal;
import constants.*;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import matchers.StandardMatcher;
import mentalModels.MentalModel;
import utils.minilisp.LList;
import utils.tools.Predicates;
import translator.Translator;
import utils.*;

/*
 * Created on Apr 9, 2016
 * @author phw
 */

public class ProblemSolverAuxiliaries extends AbstractWiredBox {

	public static final String SOLVER_INPUT_PORT = "solver input port";

	public static final String COMMENTARY = "commentary port";

	public static final String COMMANDS = "command port";

	// List of intention descriptions

	public static List<Intention> intentions;


	public static List<Test> tests;

	// Problem constants

	public static final String PROBLEM = "problem";

	public static final String QUESTION = "question";

	public static final String YES_NO = "yes/no";

	public static final String BECAUSE = "because";

	public static final String LEADS = "lead";

	public static final String BELIEF = "belief";

	protected Entity I;

	protected String textColor = "black";

	// Intention constants

	/**
	 * Note complexity needed for matching.
	 * 
	 * @return
	 */
	public Entity getI() {
		if (I == null) {
			Translator t = Translator.getTranslator();
			Entity e = t.translateToEntity("I is a person");
			Z.understand(e);
			I = e.getSubject();
//			I = Translator.getTranslator().translateToEntity("I is a person").getSubject();
			I.addProperName("i");
			I.addType(Markers.NAME);
			I.addType("i");
			/// Mark.say("I generated as", I, I.getPrimedThread());
		}
		return I;
	}

	public static final String INTENTION = "intention";

	public static final String FROM = "from";

	public static final String TO = "to";

	public static final String IN = "in";

	public static final String VIA = "via";

	public static final String WITH = "with";

	public static final String SEARCH = "search";

	// Condition constants

	public static final String CONDITION = "condition";

	public static final String CONTAIN = "contain";

	// Constructor

	public ProblemSolverAuxiliaries() {
		super("Problem solver");
		
		// Sets up I value, commented out by Z
//		getI();

	}

	/*****
	 * Major auxiliaries
	 */

	protected List<Intention> findPlausibleIntention(int level, Problem p, List<Intention> candidates, MentalModel mm) {
		List<Intention> intentions = candidates.stream().filter(i -> {
			LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(i.getIntentionDescription(), p.getProblemDescription());
			if (match != null) {
				i.setBindings(match);
				return true;
			}
			return false;
		}).collect(Collectors.toList());
		return intentions;
	}

	protected List<Test> findPlausibleTest(int level, Problem p, List<Test> candidates, MentalModel mm) {
		boolean debug = false;
		List<Test> tests = candidates.stream().filter(t -> {
			Mark.say(debug, "Checking", t.getName());
			LList<PairOfEntities> match = StandardMatcher.getBasicMatcher().match(t.getConditionDescription(), p.getProblemDescription());
			if (match != null) {
				t.setBindings(match);
				Mark.say(debug, "Test matches");
				return true;
			}
			// Mark.say("Test does not match");
			return false;
		}).collect(Collectors.toList());
		// Mark.say("Size of test list", tests.size());
		return tests;

	}

	/*****
	 * Minor auxiliaries
	 */
	
	// protected void addAStep(int level, Entity step, Entity top, Sequence steps) {
	// step.addProperty(Markers.SPECIAL, true);
	// steps.addElement(step);
	// Mark.say("Connecting\n", step, "\nto\n", top);
	// }


	protected void injectConnection(Sequence antecedents, Entity consequent, MentalModel mm) {
		boolean debug = false;
		if (debug) {
			Mark.say("\n>>> Injecting connection\n>>> ", consequent);
			antecedents.stream().forEachOrdered(a -> Mark.say(a));
		}

		antecedents.stream().forEachOrdered(a -> elaborate(a, mm));

		elaborate(consequent, mm);

		elaborate(makeMeansRelation(consequent, antecedents), mm);

	}


	protected Sequence constructStepSequence() {
		return new Sequence(Markers.CONJUNCTION);
	}

	// public boolean isMyBeliefP(Entity assertion, MentalModel mm) {
	// boolean debug = false;
	// Mark.say(debug, "Entering isMyBelief", assertion.prettyPrint());
	// List<MentalModel> traits = collectMentalModels(Markers.I, mm);
	// for (MentalModel traitModel : traits) {
	// Mark.say(debug, "Checking trait", traitModel.getType(), "for", assertion.prettyPrint());
	//
	// List<Entity> beliefs = traitModel.getExamples();
	// Mark.say(debug, beliefs.size(), "elements");
	//
	// // reportQuestion(assertion, mm);
	//
	// for (Entity element : beliefs) {
	// Mark.say(debug, "Checking trait element", element);
	// if (element.relationP(Markers.BELIEVE) && (element.getSubject().getType().equalsIgnoreCase(Markers.I))) {
	// Entity belief = RoleFrames.getObject(element);
	// Mark.say(debug, "Checking belief", belief);
	// // Mark.say("Belief ", belief);
	// // Mark.say("Assertion", assertion);
	// // Mark.say("Matching\n", belief.toXML(), "\n", assertion.toXML());
	// Mark.say(debug, "Matching", "\n", assertion, "\n", belief);
	// LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(belief, assertion);
	// Mark.say(debug, "Bindings are", bindings);
	// if (bindings != null) {
	// // reportPositiveAnswer(element, mm);
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }

	// protected List<MentalModel> collectMentalModels(String name, MentalModel mentalModel) {
	// List<MentalModel> result = new ArrayList<>();
	// Sequence story = mentalModel.getStoryProcessor().getStory();
	// for (Entity element : new Vector<>(story.getElements())) {
	// if (element.relationP(Markers.PERSONALITY_TRAIT) && (element.getSubject().getType().equalsIgnoreCase(name))) {
	// String traitName = RoleFrames.getObject(element).getType();
	// MentalModel traitModel = mentalModel.getLocalMentalModel(traitName);
	// result.add(traitModel);
	// // Mark.say("Adding", traitModel);
	// }
	// }
	// return result;
	// }

	protected String generate(Entity e) {

		String r = Translator.getTranslator().generate(e);

		// Mark.say("\n>>> !!!!!Entity", e);
		// Mark.say("Translation", r);

		return Translator.getTranslator().generate(e);
	}

	protected Entity translate(String s) {
		try {
			return Translator.getTranslator().translateToEntity(s);
		}
		catch (Exception e) {
			Mark.err("Translation error translating", s);
			e.printStackTrace();
			return null;
		}
	}

	protected void addStep(int level, Entity step, Sequence steps) {

		if (!Switch.useNegativesInExplanationsBox.isSelected()) {
			if (step.hasFeature(Markers.NOT)) {
				return;
			}
		}

		step.addProperty(Markers.SPECIAL, true);
		steps.addElement(step);

	}

	protected Relation makeMeansRelation(Entity consequent, Sequence antecedents) {
		boolean debug = false;
		Relation cause = new Relation(Markers.CAUSE_MARKER, antecedents, consequent);
		cause.addType(Markers.MEANS);
		if (debug) {
			Mark.say(debug, "\n>>>  ", "Consequent", consequent);
			for (Entity a : antecedents.getElements()) {
				Mark.say(debug, "Antecedent", a);
			}
		}
		return cause;
	}

	protected Problem makeLeadsToProblem(Entity description) {
		// description.setBundle(new Bundle());
		// description.addTypes(PROBLEM, QUESTION, YES_NO, LEADS);
		return new Problem(description);

	}

	protected Problem translateConditionToProblem(Condition c) {
		Mark.say("\n>>>  The condition is", c.getInstantiatedConditionDescription());
		return new Problem(c.getInstantiatedConditionDescription());
	}

	protected Entity did(Entity x) {
		Entity e = new Function("thing", x);
		e.addType("question");
		e.addType("did");
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iSolve(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "solve", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iShow(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "show", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iThink(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "think", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iBelieve(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "believe", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iAffirm(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "affirm", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iSee(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "see", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iConfirm(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "confirm", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iDid(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "did", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iPerform(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "perform", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iIntend(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "intend", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iTry(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "try", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity intentionSuccess(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "succeed");
		RoleFrames.addRole(e, Markers.WITH_MARKER, x);
		return e;
	}

	protected Entity intentionFailure(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "fail");
		RoleFrames.addRole(e, Markers.WITH_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iNote(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "note", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iConclude(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "conclude", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iUse(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "use", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iRequest(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "request", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iGet(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "get", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iAsk(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "ask", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iObey(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "obey", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iApproach(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "approach", x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iThinkAbout(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "think");
		RoleFrames.addRole(e, Markers.ABOUT_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iReachedConclusionAbout(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "reach", "conclusion");
		RoleFrames.addRole(e, Markers.ABOUT_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iReachedOppositeConclusionAbout(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "reach", "conclusion");
		RoleFrames.getObject(e).addFeature("opposite");
		RoleFrames.addRole(e, Markers.ABOUT_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iLookFor(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "look");
		RoleFrames.addRole(e, Markers.FOR_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iCheck(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "check", x);
		// Mark.say("Check", I);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity iFindMethodFor(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(I, "find", "method");
		RoleFrames.addRole(e, Markers.FOR_MARKER, x);
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity makeIsTrueRoleFrame(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(x, "property", "true");
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity makeIsFalseRoleFrame(Entity x) {
		Entity e = RoleFrames.makeRoleFrame(x, "property", "false");
		e.addProperty(Markers.SPECIAL, true);
		return e;
	}

	protected Entity negate(Entity e) {
		// Mark.say("Negating", e);
		e.addFeature(Markers.NOT);
		return e;
	}


	protected void blurtInPlus(int level, Entity input, Entity entity, MentalModel mm) {
		if (Switch.reportSteps.isSelected()) {
			Sequence steps = constructStepSequence();
			addStep(level, input, steps);
			injectConnection(steps, entity, mm);
		}

		// blurtAux(level, entity, mm);
	}

	

	protected void blurtOutPlus(int level, Entity input, Entity entity, MentalModel mm) {
		if (Switch.reportSteps.isSelected()) {
			Sequence steps = constructStepSequence();
			addStep(level, input, steps);
			injectConnection(steps, entity, mm);
		}
		// blurtAux(level, entity, mm);
	}

	protected void blurtOut(int level, Entity x, MentalModel mm) {
		// blurtAux(level, x, mm);
	}

	protected void blurtAux(int level, Entity x, MentalModel mm) {

		boolean negate = x.hasFeature(Markers.NOT);
		if (x.functionP(Markers.DID_QUESTION)) {
			x = x.getSubject();
			if (negate) {
				x = negate(x);
			}
		}


		// Mark.say("Blurt out Level is", level);
		if (Radio.psLevel0.isSelected() && level > 0) {
			return;
		}
		else if (Radio.psLevel1.isSelected() && level > 1) {
			return;
		}
		else if (Radio.psLevel2.isSelected() && level > 2) {
			return;
		}
		else if (Radio.psLevel3.isSelected() && level > 3) {
			return;
		}
		else if (Radio.psLevelP.isSelected()) {
			if (!x.hasProperty(ProblemSolver.NODE_TYPE, ProblemSolver.RESULT)) {
				return;
			}
		}
		if (!Switch.useChecksInExplanationsBox.isSelected()) {
			if (x.isA(Markers.CHECK)) {
				return;
			}
		}
		if (!Switch.useNegativesInExplanationsBox.isSelected()) {
			if (x.hasFeature(Markers.NOT) && level > 0) {
				return;
			}
		}

		blurtCommunication(level, x, textColor);
	}

	protected void blurtResult(int level, boolean b, String color) {
		boolean debug = false;
		Mark.say(debug, "Blurting result!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", color);
		if (b) {
			blurtCommand(level, RoleFrames.makeRoleFrame(Markers.I, "succeed"), color);
		}
		else {
			blurtCommand(level, RoleFrames.makeRoleFrame(Markers.I, "fail"), color);
		}
	}

	protected void blurtMe(int level, String prefix, Entity e) {
		blurtMe(level, prefix, e, textColor);
	}

	protected void blurtMe(int level, String prefix, Entity e, String color) {
		String command = Generator.getGenerator().generate(e);
		// reportDescription(level, prefix + " " + command, color, Markers.COMMAND_TAB, ".");
		reportDescription(level, prefix + " " + command, color, Markers.INTROSPECTION_TAB, ".");
	}

	protected void blurtJustDoIt(int level, String type, Entity e, String color) {
		String command = Generator.getGenerator().generate(e);
		String prefix = "Method:";
		if (type == CommandList.ADD_SUCCESS) {
			prefix = "Just did:";
		}
		else if (type == CommandList.ADD_FAILURE) {
			prefix = "Did not:";
		}
		reportDescription(-1, prefix + " " + command, color, Markers.COMMAND_TAB, ".");
		String s = type + " " + command;
		Connections.getPorts(this).transmit(COMMANDS, new BetterSignal(type, s));
	}

	protected void blurtCommand(int level, Entity e, String color) {
		Mark.say("Blurting command", e);
		String command = Generator.getGenerator().generate(e);
		reportDescription(level, command, color, Markers.COMMAND_TAB, ".");
		reportDescription(level, command, color, Markers.INTROSPECTION_TAB, ".");
	}

	protected void blurtCommunication(int level, Entity e, String color) {
		blurtCommunication(level, e, ".", color);
	}

	protected void blurtCommunication(int level, String prefix, Entity e, String color) {
		blurtCommunication(level, prefix, e, ".", color);
	}

	protected void blurtCommunication(int level, Entity e, String punctuation, String color) {
		if (e.isA(Markers.DID_QUESTION) && e.hasFeature(Markers.NOT)) {
			// This trap is to catch form that generator screws up.
			e = RoleFrames.makeRoleFrame(Markers.I, "fail");
		}
		String communication = Generator.getGenerator().generate(e);
		blurtCommunication(level, communication, punctuation, color);
	}

	protected void blurtCommunication(int level, String prefix, Entity e, String punctuation, String color) {
		if (e.isA(Markers.DID_QUESTION) && e.hasFeature(Markers.NOT)) {
			// This trap is to catch form that generator screws up.
			e = RoleFrames.makeRoleFrame(Markers.I, "fail");
		}
		String communication = prefix + ": " + Generator.getGenerator().generate(e);
		blurtCommunication(level, communication, punctuation, color);
	}

	protected void blurtCommunication(int level, String s, String punctuation, String color) {
		reportDescription(level, s, color, Markers.INTROSPECTION_TAB, punctuation);
	}

	protected void reportDescription(int level, String s, String color, String tab, String punctuation) {
		String spaces = "&nbsp;";

		for (int i = 0; i < level; ++i) {
			spaces += "&nbsp;";
		}

		s = Generator.stripPeriod(s) + punctuation;

		if (level >= 0) {
			s = Html.small(level + " " + s);
		}

		s = Html.coloredText(color, s);

		String pre = "<table><tr><td>";

		String mid = "</td><td>";

		String space = "";

		for (int i = 0; i < level; ++i) {
			space += "&nbsp;";
		}

		String post = "</td></tr></table>";

		s = "<p>" + space + s;

		Connections.getPorts(this).transmit(COMMENTARY, new BetterSignal(GenesisConstants.LEFT, tab, s));
	}

	protected void elaborate(Object o, MentalModel mm) {
		boolean debug = false;
		if (o instanceof String) {
			Entity result;
			try {
				result = translate((String) o);
				if (result.getElements().size() != 1) {
					Mark.err("Strange result here!");

				}
				else {
					elaborate(result.get(0), mm);
				}
			}
			catch (Exception e) {
				Mark.err("Error in reporting");
			}
		}
		else if (o instanceof Entity) {
			Entity e = (Entity) o;
			// Hack, see definition
			// Mark.say("\n>>> Replace in ", e);
			replaceI(e);
			// Mark.say("Replace out", e);
			e.addProperty(Markers.SPECIAL, true);
			if (Predicates.isCause(e)) {
				e.getSubject().stream().forEach(a -> a.addProperty(Markers.SPECIAL, true));
				e.getObject().addProperty(Markers.SPECIAL, true);
			}

			Mark.say(debug, "Injecting ", e);
			mm.getI().getStoryProcessor().injectElementWithDereference(e);
		}
	}

	/**
	 * A hack. Without this, get mighty confused about versions of I. Also, had to make adjustment in dereferencing
	 * code; was cloning, but that screwed things up too. Taking out the cloinging of entities seems to have fixed
	 * problem, but took a long time to track down.
	 * <p>
	 * Good topic for summer scrubbing.
	 */
	protected void replaceI(Entity e) {

		Entity subject = e.getSubject();
		Entity object = e.getObject();
		Vector<Entity> elements = e.getElements();

		if (subject != null && (subject.isA("i") || subject.isA("I"))) {
			e.setSubject(I);
		}
		if (object != null && (object.isA("i") || object.isA("I"))) {
			e.setObject(I);
		}
		if (subject != null && !subject.entityP()) {
			replaceI(subject);
		}
		if (object != null && !object.entityP()) {
			replaceI(object);
		}
		if (elements != null) {
			elements.stream().forEach(x -> replaceI(x));
		}

	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

}

package generator;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import frames.entities.Thread;
import gui.TabbedTextViewer;
import start.Start;
import translator.*;
import utils.*;
import utils.tools.Predicates;
import zhutianYang.AcquireRecipes.RecipeGenerator;

/**
 * See DemoTranslator class for examples.
 */

public class Generator extends AbstractWiredBox {

	public static final String SAY = "say";

	public static final String EXPECTATION = "expectation";

	public static final String IMAGINE = "imagine";

	public static final String LEARNED = "learned";

	public static final String DISAMBIGUATED = "disambiguated";

	public static final String TEST = "test";

	public static int MUTE = 0, ACTIVE = 1;

	private static ArrayList<String> features;

	private int mode = ACTIVE;

	private Start start;

	private static Generator generator;

	protected static List<String> connectors = Arrays.asList("before", "after", "while", "because", "that", "whether");

	public static Generator getGenerator() {
		if (generator == null) {
			generator = new Generator();
		}
		return generator;
	}

	// private static PhraseFactory factory = PhraseFactory.getPhraseFactory();

	private Start getStart() {
		if (start == null) {
			start = Start.getStart();
		}
		return start;
	}

	public Generator() {
		super("Generator");
		Connections.getPorts(this).addSignalProcessor("processSay");
		Connections.getPorts(this).addSignalProcessor(EXPECTATION, "processExpectation");
		Connections.getPorts(this).addSignalProcessor(SAY, "processSay");
		Connections.getPorts(this).addSignalProcessor(LEARNED, "processLearning");
		Connections.getPorts(this).addSignalProcessor(IMAGINE, "processImagine");
		Connections.getPorts(this).addSignalProcessor(DISAMBIGUATED, "processDisambiguation");
		features = new ArrayList<String>();
		features.add("dead");
		features.add("alive");
	}

	public String generateXPeriod(Entity t) {
		return generateXPeriod(t, Markers.PRESENT);
	}

	public String generateXPeriod(Entity t, String tense) {
		return stripPeriod(generate(t, tense));
	}

	public static String stripPeriod(String r) {
		if (r != null) {
			r = r.trim();
			if (r.lastIndexOf('.') == r.length() - 1

			        || r.lastIndexOf('?') == r.length() - 1

			) {
				return r.substring(0, r.length() - 1);
			}
			else {
				return r;
			}
		}
		return null;
	}

	public String generateTriples(Entity t) {
		return newGenerate(t).getRendering();
	}

	public String comment(Entity t) {
		return generate(generateFromEntity(t).present().progressive());
	}

	public String playByPlay(Entity t, String time, boolean progressive) {
		RoleFrame frame = generateFromEntity(t);
		if (progressive) {
			frame.progressive();
		}
		if (time == Markers.PAST) {
			frame.past();
		}
		else if (time == Markers.PRESENT) {
			frame.present();
		}
		else {
			frame.future();

		}
		return generate(frame);
	}

	public String generateWithoutCache(Entity t) {
		String result = generateAux(t, Markers.PRESENT);
		return result;
	}

	public String generateWithoutCache(Entity t, String tense) {
		String result = generateAux(t, tense);
		return result;
	}

	public String generateInPastTense(Entity t) {
		if (Switch.useStartCache.isSelected() && !Switch.useStartBeta.isSelected()) {
			String result = generateViaCache(t, Markers.PAST);
			if (result != null) {
				return result;
			}
		}
		return generateWithoutCache(t, Markers.PAST);

	}

	public String generate(Entity t) {
		String result = "Nothing generated";
		if (t.getBooleanProperty(Markers.SPECIAL)) {
			result = Translator.getTranslator().generate(t);
		}
		else {
			
			// commented by Z on 7 Feb for generating "the player will go into the door"
//			result = generate(t, t.getProperty(Markers.TENSE).toString()).trim(); 
			
//			Z.understand(t);
			if (t.getType().equals(Markers.SEMANTIC_INTERPRETATION)) {
				t = t.getElement(0);
			}
			result = generate(t, Markers.PRESENT).trim();
		}
		return result;
	}

	public String generate(Entity t, String tense) {
		boolean debug = false;
		Mark.say(debug, "Generating from", t);
		Mark.yellow(debug, t.getPropertyList().toString());
//		Mark.yellow(t.getProperty(Markers.TENSE).toString());

		if (Switch.useStartCache.isSelected() && !Switch.useStartBeta.isSelected()) {
			String result = generateViaCache(t);
			if (result != null) {
				Mark.say(debug, "Generating from cache");
				return result;
			}
		}
		String result = null;
		try {
			result = generateAux(t, tense);
		}
		catch (Exception e) {
			Mark.err("Unable to generate from", t);
			// e.printStackTrace();
			return "???";
		}
		if (Switch.useStartCache.isSelected() && !Switch.useStartBeta.isSelected()) {
			if (result != null) {
				saveInCache(t, tense, result);
			}
		}
		Mark.say(debug, "Result is", result);
		return result;
	}

	public String generateAux(Entity e, String tense) {
		boolean debug = false;
		Mark.say(debug, "Generating in generateAux from", e);

		// Because trap; don't like start's treatment, as it tendsw to put in subordinate clauses
		if (Predicates.isExplanation(e) || Predicates.isAbduction(e)) {
			Mark.say(debug, "Explanation!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			e.addProperty(Markers.PROPERTY_TYPE, Markers.EXPLANATION_RULE);
		}
		// else if (Predicates.isAbduction(e)) {
		// for (Entity a : e.getSubject().getElements()) {
		// Mark.say(debug, "Abduction!");
		// a.addProperty(Markers.PROPERTY_TYPE, Markers.EXPLANATION);
		// }
		// }
		if (Predicates.isCause(e) && !e.isA(Markers.ENTAIL_RULE) && !e.isA(Markers.MEANS)) {
			return composeCauseSentence(e, tense);
		}
		else {
			return generateStringFromEntity(e, tense);
		}
	}

	public String composeCauseSentence(Entity e, String tense) {
		boolean debug = false;
		Mark.say(debug, "Trapped!!!!!!!!!!!!!!!!!!");
		String consequent = stripPeriod(generateStringFromEntity(e.getObject(), tense));
		if (e.hasProperty(Markers.PROPERTY_TYPE, Markers.EXPLANATION_RULE)) {
			consequent += ", probably";
		}
		consequent += " because ";
		ArrayList<String> antecedants = new ArrayList<String>();
		for (Entity x : e.getSubject().getElements()) {
			String antecedant = stripPeriod(generateStringFromEntity(x, tense));
			antecedants.add(antecedant);
		}
		consequent += composeAnd(antecedants);
		return consequent + ".";
	}

	public String generateStringFromEntity(Entity t, String tense) {
		boolean debug = false;
		Mark.say(debug, "Generating from generateStringFromEntity", t);
		RoleFrame roleFrame = generateFromEntity(t);
		if (roleFrame == null) {
			if(RecipeGenerator.VERBOSE) Mark.err("Unable to generate role frame from", t.toString());
			return t.toString();
		}
		if (tense == Markers.PAST) {
			roleFrame.makePast();
		}
		else if (tense == Markers.FUTURE) {
			roleFrame.makeFuture();
		}
		String result = generate(roleFrame);
		if (result == null) {
			// Mark.say("Unable to generate sentence from role frame generated by\n", t.toString(), "\n",
			// roleFrame.rendering);
			return t.toString();
		}
		Mark.say(debug, "Result is", result);
		return result;
	}

	public String generateAsIf(Entity t) {
		boolean debug = false;
		Mark.say(debug, "Generating from:", t.toString());
		// Because trap:
		// Note special case; wordnet things move is a kind of cause
		if (Predicates.isCause(t)) {
			// Separately generate antecedants and consequent
			Mark.say(debug, "Trapped!!!!!!!!!!!!!!!!!!");
			String consequent = this.generateXPeriod(t.getObject(), Markers.PRESENT);
			if (t.isA(Markers.EXPLANATION_RULE)) {
				consequent += " may be a consequence of ";
			}
			else {
				consequent += " whenever ";
			}
			ArrayList<String> antecedants = new ArrayList<String>();
			for (Entity x : t.getSubject().getElements()) {
				String antecedant = generateXPeriod(x, Markers.PRESENT);
				antecedants.add(antecedant);
			}
			consequent += composeAnd(antecedants);
			return consequent + ".";
		}
		RoleFrame roleFrame = newGenerate(t);
		if (roleFrame == null) {
			Mark.say("Unable to generate role frame from", t.toString());
			return t.toString();
		}
		String result = generate(roleFrame);
		if (result == null) {
			Mark.say("Unable to generate sentence from role frame", roleFrame);
			return t.toString();
		}
		return result;
	}

	public String verticalize(String rendering) {
		StringBuffer buffer = new StringBuffer(rendering);
		int index = -1;
		while ((index = buffer.indexOf("][")) > 0) {
			buffer.replace(index, index + 2, "]\n[");
		}
		return "\n" + buffer.toString();
	}

	public static String composeAnd(List<String> s) {
		String result = "";
		if (s == null || s.isEmpty()) {
			return result;
		}
		else if (s.size() == 1) {
			result += s.get(0);
		}
		else if (s.size() == 2) {
			result += stripPeriod(s.get(0)) + ", and " + s.get(1);
		}
		else {
			for (int i = 0; i < s.size() - 1; ++i) {
				result += stripPeriod(s.get(i)) + ", ";
			}
			result += "and " + s.get(s.size() - 1);
		}
		return result.trim();
	}

	public String generate(RoleFrame roleFrame) {
		boolean debug = false;

		String roleResult = null;
		String rendering = getCompleteRendering(roleFrame);
		if (roleFrame != null) {
			Mark.say(debug, "Role frame triples:", rendering);
			// roleResult = factory.generate(roleFrame);
			roleResult = getStart().generate(rendering);
		}
		else {
			// Mark.say("No role frame triples, cannot generate sentence");
		}
		if (roleResult != null && roleResult.indexOf("<P>") < 0) {
			return roleResult;
		}
		return null;
	}

	public String getCompleteRendering(RoleFrame r) {
		String triples = r.getRendering() + r.makeProperty(r.getHead(), "is_main", "Yes");
		return triples;
	}

	public String interpret(String triples) {
		return Start.getStart().generate(triples);
	}

	public String generateTriplesDeprecated(Entity t) {
		String result = null;
		// Pair pair = processTop(t);
		// if (pair == null) {
		// return result;
		// }
		// // Mark.say("Triples sent to Start:", pair.getTriples());
		// result = pair.getTriples();
		// // Mark.say("Sentence received from Start:", result);
		return result;
	}

	private void test() {
		BasicTranslator basicTranslator = new BasicTranslator();
		ArrayList<String> tests = makeTests();
		for (String sentence : tests) {
			Mark.say("");
			Mark.say("The sentence:                     ", sentence);
			String startTriples = this.getStart().processSentence(sentence);
			Mark.say("Generation from START's triples:  ", this.getStart().generate(startTriples));
			Entity parse = basicTranslator.interpret(this.getStart().parse(sentence));
		}
	}

	public void test(String sentence, String... strings) {
		BasicTranslator basicTranslator = new BasicTranslator();
		ArrayList<String> tests = makeTests();
		Mark.say("");

		String startTriples = this.getStart().processSentence(sentence);
		Entity parse = basicTranslator.interpret(this.getStart().parse(sentence));
		Entity x = parse.getElement(0);

		RoleFrame rf = newGenerate(x);
		for (String s : strings) {
			rf.addTriple(s);
		}
		String result = Generator.getGenerator().generate(x);

		sentence = sentence.trim();
		if (sentence.charAt(sentence.length() - 1) != '.') {
			sentence += '.';
		}

		if (sentence.equalsIgnoreCase(result.trim())) {
			Mark.say("Ok:", result);
		}
		else {
			Mark.err("Ooops:\nDesired:", sentence, "\nGot:    ", result);
			Mark.say("The sentence:", sentence);
			Mark.say("Start triples:", verticalize(startTriples));

			Mark.say("Parse is:", parse.asStringWithIndexes());
			Mark.say("Working on:", x.toString());
			Mark.say("Role frame yields:", verticalize(rf.getRendering()));
			Mark.say("Generation from START's triples:", this.getStart().generate(startTriples));
			Mark.say("Generation from innerese:       ", result);
			// Mark.say("Second opinion: ",
			// this.getStart().generate(rf.getRendering()));
		}
	}

	public void test(RoleFrame rf, String result) {
		test(rf, result, false);
	}

	public void test(RoleFrame rf, String result, boolean showTriples) {
		String generation = "";
		try {
			generation = Generator.getGenerator().generate(rf);
			if (generation != null) {
				generation = generation.trim();
			}
			result = result.trim();
			Mark.say(showTriples, "\n|", result, "|\n|", generation, "|");
			if (result == null) {
				Mark.err("Oops, generator produced null, should have got", result);
			}
			else if (result.trim().isEmpty()) {
				Mark.err("Oops, haven't specified output desired; got", generation);
			}
			else if (result.equalsIgnoreCase(generation)) {
				// Mark.say("Ok:", result);
				BetterSignal signal = new BetterSignal("Generator test", Html.line("Ok: " + result));
				Connections.getPorts(Generator.this).transmit(TEST, signal);
				return;
			}
			else {
				Mark.say("Diferences:\n|" + result + "|\n|" + generation + "|");
				Connections.getPorts(this).transmit(TEST, new BetterSignal("Generator test", Html.line("Ooops:")));
				Connections.getPorts(this).transmit(TEST, new BetterSignal("Generator test", Html.line("Desired: " + result)));
				Connections.getPorts(this).transmit(TEST, new BetterSignal("Generator test", Html.line("But got: " + generation)));
				return;
			}
		}
		catch (Exception e) {
			Mark.err("Hit exception on", result);
			Mark.err("Role frame produced:\n", addCr(rf.getRendering()));
			Mark.err("Start triples:\n", removeSpans(Start.getStart().processSentence(result)));
			e.printStackTrace();
		}
		Connections.getPorts(this)
		        .transmit(TEST, new BetterSignal("Generator test", Html.line("Failed on: " + result + ", generated: " + generation)));
	}

	private String addCr(String s) {
		while (true) {
			int index = s.indexOf("][");
			if (index >= 0) {
				s = s.substring(0, index + 1) + "\n" + s.substring(index + 1);
			}
			else {
				break;
			}
		}
		return s;
	}

	private String removeSpans(String s) {
		if (s == null) {
			return s;
		}
		while (true) {
			int start = s.indexOf("<");
			if (start >= 0) {
				int end = s.indexOf(">");
				s = s.substring(0, start) + s.substring(end + 1);
			}
			else {
				break;
			}
		}
		return s;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public static String merge(List<String> list) {
		int l = list.size();
		if (l == 1) {
			return list.get(0);
		}
		else if (l == 2) {
			return "<ul><li>" + Punctuator.removePeriod(list.get(0)) + "; <li>" + list.get(1) + "</ul>";
		}
		else if (l < 1) {
			Mark.say("Bug in question expert.  No antecedents");
			return null;
		}
		String result = "<ul>";
		for (int i = 0; i < l; ++i) {
			result += "<li>" + Punctuator.removePeriod(list.get(i)) + "; ";
		}
		result += "</ul>";
		return result;
	}

	private ArrayList<String> makeTests() {
		ArrayList<String> tests = new ArrayList<String>();
		tests.add("The king hates the prince because prince hates the king");
		tests.add("Sonja is a dog"); // Class
		tests.add("The bird flew"); // Trajectory
		// Transition
		tests.add("The king appeared");
		tests.add("The king became dead");
		tests.add("Macbeth became king.");
		// Transfer
		tests.add("The king loves beer because the king is nice"); // Cause
		// Goal
		tests.add("The king wants to drink beer");
		tests.add("Lady Macbeth wants to become queen.");
		// Persuation
		tests.add("A mouse persuaded a bull to drink beer");
		tests.add("Lady Macbeth persuaded Macbeth to want to become the king.");
		tests.add("The king forced the queen to love beer"); // Coersion
		// Belief
		tests.add("The king believes the queen is nice");

		tests.add("The king is happy"); // Mood
		tests.add("The king has wings"); // Part
		tests.add("The king is nice"); // Property
		tests.add("The king owns a dog"); // Possession
		tests.add("The king loves his dog"); // Possession
		tests.add("Patrick is the king"); // Job
		tests.add("The dog is the man's friend"); // Social
		tests.add("The king drank beer after the queen drank wine"); // Time

		tests.add("The king is smarter than the prince"); // Comparison
		// Role
		tests.add("Jack murdered Jill with a knife.");
		tests.add("The king ate a pear with a knife");
		// Action
		tests.add("The king drinks beer");
		tests.add("Lady Macbeth kills Duncan.");
		tests.add("The king drinks beer with a stein"); // Action
		tests.add("A mouse persuaded a bull to drink beer"); // Persuation
		// Negation & thinkng
		tests.add("The king does not think that John does not love Mary");
		tests.add("The king thinks that John loves Mary");
		tests.add("George does not think that John does not love Mary");
		tests.add("Lady Macbeth thinks King Hamlet loves Princess Jessica");
		// Names
		tests.add("Hamlet is King Hamlet's friend"); // Action
		tests.add("George's wanting to fly leads to George's flying"); // Action
		tests.add("George flew because a cat appeared and a dog appeared."); // Action
		tests.add("Boris fought Sue with a knife."); // Action
		tests.add("Boris relocated a town."); // Action
		tests.add("Start story titled \"Cyber war.\"");
		tests.add("A girl gave a hammer to another girl");
		tests.add("I have learned from experience that The girl takes the ball");
		tests.add("Imagine a jump");
		tests.add("The car left the tree");
		tests.add("Macbeth persuaded Caesar to murder Boris");
		tests.add("Estonia believes computer networks are valuable");
		tests.add("I am Talliban's ally");
		// z tests.add("The first object is bigger than the second object");
		tests.add("The first object is not approaching the second object");
		tests.add("Macbeth murders duncan");
		tests.add("Georgia believes computer networks are important");

		tests.add("Patrick appears to be vicious");

		// tests.add("Claudius murdered King Hamlet");
		// tests.add("Paul dies");
		// tests.add("A girl killed a boy");
		// Currently in test
		// tests.add("Macbeth wants to become king because Lady Macbeth persuaded him to want to become king");
		// tests.add("Macbeth may want to become king");
		// tests.add("Boris relocated a town."); // Action
		return tests;
	}

	public void flush() {
		Start.getStart().clearLocalTripleMaps();
		Start.getStart().clearStartReferences();
	}

	public void setStoryMode() {
		Start.getStart().setMode(Start.STORY_MODE);

	}

	public void setRegularMode() {
		Start.getStart().setMode(Start.STORY_MODE);

	}

	public RoleFrame generateFromEntity(Entity t) {
		// Mark.say("Entering generateFromThing", t.toString());
		return newGenerate(t);
	}

	private RoleFrame generateEntityWithQuantity(boolean debug, Entity entity) {

		// Special case for I, blind patch
		Mark.say(debug, "A", entity.toString());
		RoleFrame result = makeEntity(entity);
		Object o = entity.getProperty(Markers.QUANTITY);
		if (o != null) {
			result.addQuantity((String) o);
		}
		return result;
	}

	private RoleFrame newGenerateFunction(boolean debug, Entity entity) {
		RoleFrame result = null;
		if (entity.functionP(Markers.EXPECTATION)) {
			Mark.say(debug, "B", entity.toString());
			result = newGenerate(entity.getSubject());
		}
		else if (entity.isA(NewRuleSet.pathPrepositions)) {
			Mark.say(debug, "C", entity.toString());
			Entity reference = entity.getSubject();
			if (reference.functionP("at")) {
				Mark.say(debug, "C1", entity.toString());
				result.addRole(entity.getType(), makeEntity(reference.getSubject()));
			}
			else {
				Mark.say(debug, "C2", entity.toString());
				RoleFrame part = makeEntity(reference.getType());
				result.addRole(entity.getType(), part);
				RoleFrame decoration = makeEventFrame(entity, part, "related-to", makeEntity(reference.getSubject()));
				result.combine(decoration);
			}
		}
		else if (entity.functionP(Markers.COMMAND_MARKER)) {
			Mark.say(debug, "D", entity.toString());
			Entity s = entity.getSubject();
			result = makeEventFrame(entity, "you", "imagine", newGenerate(s)).attitude("imperative");
		}
		else if (entity.functionP(Markers.TRANSITION_MARKER) && entity.getSubject().entityP()) {
			Mark.say(debug, "E", entity.toString());
			Entity s = entity.getSubject();
			result = makeEventFrame(entity, makeEntity(s), entity.getType(), null);
		}
		else if (entity.functionP(Markers.TRANSITION_MARKER)) {
			Mark.say(debug, "F", entity.toString());
			Entity s = entity.getSubject();
			String action = "become";

			if (entity.getSubject().isA(Markers.JOB_TYPE_MARKER)) {
				Mark.say(debug, "F1", entity.toString());

				result = makeEventFrame(entity, makeEntity(s.getSubject()), action, makeEntity(RoleFrames.getObject(s)));
			}
			else if (entity.getSubject().isA(Markers.CLASSIFICATION_MARKER)) {
				Mark.say(debug, "F2", entity.toString());
				Entity object = s.getObject();
				if (RoleFrames.isRoleFrame(entity)) {
					object = RoleFrames.getObject(entity);
				}
				result = makeEventFrame(entity, makeEntity(object), action, makeEntity(s.getSubject()));
			}
			else {
				Mark.say(debug, "F3", entity.toString());
				if (RoleFrames.isRoleFrame(s)) {
					Mark.say(debug, "F3A");



					// object = RoleFrames.getObject(entity);
					// result = newGenerate(s);
					result = makeEventFrame(entity, makeEntity(s.getSubject()), action, makeEntity(RoleFrames.getObject(s)));

				}
				else {
					Mark.say(debug, "F3B");
					Entity object = s.getObject();
					result = makeEventFrame(entity, makeEntity(s.getSubject()), action, makeEntity(object));
				}
			}

		}
		else if (entity.functionP(Markers.WHY_QUESTION)) {
			Mark.say(debug, "W", entity.toString());
			result = newGenerate(entity.getSubject()).makeWhyQuestion();
		}
		else if (entity.functionP(Markers.WHAT_IF_QUESTION)) {
			Mark.say(debug, "W", entity.toString());
			result = newGenerate(entity.getSubject()).makeWhatIfQuestion();
		}
		else if (entity.functionP(Markers.HOW_QUESTION)) {
			Mark.say(true, "W", entity.toString());
			result = newGenerate(entity.getSubject()).makeHowQuestion();
		}
		else if (entity.functionP(Markers.QUESTION)) {
			result = newGenerate(entity.getSubject()).makeQuestion();
		}
		else if (entity.functionP()) {
			Mark.say(debug, "G", entity.toString());
			result = makeEntity(entity.getType()).addRole("related-to", newGenerate(entity.getSubject()));
		}
		return result;
	}

	private RoleFrame newGenerateSequence(boolean debug, Entity entity) {

		Sequence sequence = (Sequence) entity;

		if (sequence.getElements().isEmpty()) {
			return null;
		}

		// Mark.say("First element is", sequence.getElements().get(0));

		RoleFrame result = newGenerate(sequence.getElements().get(0));

		for (int i = 1; i < sequence.getElements().size(); ++i) {
			result.combine(newGenerate(sequence.getElements().get(i)));
		}

		// Mark.say("Result is", result);

		return result;
	}

	private RoleFrame newGenerateRelation(Entity entity) throws Exception {
		boolean debug = false;
		RoleFrame result = null;
		Mark.say(debug, "Relation is", entity);
		if (entity.relationP("start") && entity.getSubject().isA("you")) {
			// Mark.say("Triggered on start word");
			RoleFrame you = makeEntity(new Entity("you"));
			RoleFrame story = makeEntity(new Entity("story"));
			result = new RoleFrame(you, "start", story);
		}
		else if (entity.isA(connectors)) {
			Mark.say(debug, "A", entity.toString());
			RoleFrame first = newGenerate(entity.getSubject());
			RoleFrame second = newGenerate(entity.getObject());
			result = first.connect(entity.getType(), second);
		}
		else if (entity.relationP(Markers.SOCIAL_MARKER) && entity.getSubject().entityP() && entity.getObject().entityP()) {
			Mark.say(debug, "B", entity.toString());
			RoleFrame subject = makeEntity(entity.getSubject());
			RoleFrame object = makeEntity(entity.getObject());
			StartEntity x = makeEntity(entity.getType());
			x.addPossessor(subject);
			RoleFrame roleFrame = makeEventFrame(entity, object, Markers.IS_A, makeEntity(entity.getType()).addPossessor(subject));
			result = roleFrame;
		}
		else if (entity.relationP(Markers.CALLED)) {
			Mark.say(debug, "C", entity.toString());
			result = makeEventFrame(entity, makeEntity(entity.getSubject().getType()), Markers.CALLED, makeEntity(entity.getObject().getType())
			        .noDeterminer());
		}
		else if (RoleFrames.isRoleFrame(entity)) {
			Mark.say(debug, "D", entity.toString());
			Sequence sequence = (Sequence) (entity.getObject());
			if (!sequence.getElements().isEmpty()) {
				Mark.say(debug, "D1", entity.toString());
				Entity object = getObject(sequence.getElements());
				if (object != null && object.sequenceP(Markers.PATH_MARKER)) {
					Mark.say(debug, "D11", entity.toString());
					result = makeEventFrame(entity, generateEntityWithQuantity(debug, entity.getSubject()), entity.getName(), null);
					for (Entity role : withoutObject(object.getElements())) {
						addPathElement(result, role);
					}
				}
				else if (object != null && object.sequenceP()) {
					Mark.say(debug, "D12", entity.toString());
					result = makeEventFrame(entity, makeEntity(entity.getSubject()), entity.getName(), null);
					for (Entity role : withoutObject(object.getElements())) {
						addPathElement(result, role);
					}
				}
				else if (object != null) {
					Mark.say(debug, "D13", entity.toString());
					RoleFrame objectFrame = newGenerate(object);
					Mark.say(debug, "Object is", object);
					if (Predicates.isCause(object)) {
						Mark.say(debug, "D131", object.toString());
						objectFrame.that();
						result = makeEventFrame(entity, newGenerate(entity.getSubject()), entity.getType(), objectFrame);
					}
					else if (isExpression(entity)) {
						Mark.say(debug, "D132", object.toString());
						result = makeEventFrame(entity, newGenerate(entity.getSubject()), "is", objectFrame);
					}
					else {
						Mark.say(debug, "D133", object.toString());
						objectFrame.to();
						result = makeEventFrame(entity, newGenerate(entity.getSubject()), entity.getType(), objectFrame);
					}
				}
				Mark.say(debug, "D14", result);
				for (Entity role : withoutObject(sequence.getElements())) {
					if (result == null) {
						Mark.say(debug, "D141");
						result = makeEventFrame(entity, newGenerate(entity.getSubject()), entity.getName(), null);
					}
					if (role.isA(Markers.MANNER_MARKER)) {
						Mark.say(debug, "D142");
						if (role.getSubject().isA(Markers.CONSEQUENTLY)) {
							result.addModifier(role.getSubject().getType());
						}
						else if (role.getSubject().isA(Markers.EVIDENTLY)) {
							result.addModifier(role.getSubject().getType());
						}
						else if (role.getSubject().isA(Markers.PREVIOUSLY)) {
							result.addModifier(role.getSubject().getType());
						}
						else {
							Mark.say(debug, "D143");
							result.addTrailingModifier(role.getSubject().getType());
						}
					}
					else {
						result.addRole(role.getType(), newGenerate(role.getSubject()));
					}
				}
			}
			else {
				Mark.say(debug, "D2", entity.toString());
				Entity subject = entity.getSubject();
				if (subject.relationP(Markers.PROPERTY_TYPE)) {
					Mark.say(debug, "D21", entity.toString());
					result = makeEventFrame(entity, newGenerate(subject), entity.getName(), null);
				}
				else {
					Mark.say(debug, "D22", entity.toString());
					result = makeEventFrame(entity, newGenerate(entity.getSubject()), entity.getName(), null);
				}
			}
		}
		else if (entity.relationP(Markers.PART_OF)) {
			Mark.say(debug, "E", entity.toString());
			result = makeEntity(entity.getObject()).partOf(makeEntity(entity.getSubject()));
		}
		else if (entity.relationP(Markers.RELATIVE)) {
			Mark.say(debug, "F", entity.toString());
			RoleFrame owner = makeEntity(entity.getSubject());
			RoleFrame owned = makeEntity(entity.getObject());
			RoleFrame type = new RoleFrame(entity.getType());
			RoleFrame main = makeEventFrame(entity, owned, "is", type);
			RoleFrame relation = makeEventFrame(entity, type, "related-to", owner);
			result = main.combine(relation);
		}
		else if (isExpression(entity)) {
			Mark.say(debug, "G", entity.toString());
			result = makeEventFrame(entity, makeEntity(entity.getSubject(), entity.getObject().getType()), "is", makeEntity(entity.getObject()));
		}
		else if (entity.relationP(Markers.JOB_TYPE_MARKER)) {
			Mark.say(debug, "H", entity.toString());
			result = makeEventFrame(entity, makeEntity(entity.getSubject()), "is-a", makeEntity(entity.getObject()).indefinite());
		}
		else if (entity.relationP(Markers.CLASSIFICATION_MARKER)) {
			Mark.say(debug, "I", entity.toString());
			StartEntity subject = makeEntity(entity.getObject());
			StartEntity object = makeEntity(entity.getSubject()).indefinite();
			object.indefinite();
			result = makeEventFrame(entity, subject, "is", object);
		}
		else if (entity.relationP(Markers.BELIEF_MARKER)) {
			Mark.say(debug, "J", entity.toString());
			RoleFrame subject = makeEntity(entity.getSubject());
			RoleFrame object = newGenerate(entity.getObject());
			result = makeEventFrame(entity, subject, entity.getType(), object);
		}

		else if (entity.relationP(Markers.GOAL_MARKER) || entity.relationP(Markers.COERCE_MARKER) || entity.relationP(Markers.PERSUATION_MARKER)) {
			Mark.say(debug, "K", entity.toString());
			RoleFrame subject = makeEntity(entity.getSubject());
			RoleFrame object = newGenerate(entity.getObject());
			if (entity.getObject().relationP() && entity.getObject().isA(Markers.ACTION_MARKER)) {
				Mark.say(debug, "K1", entity.toString());
				RoleFrame intermediate = makeEntity("to");
				result = makeEventFrame(entity, subject, entity.getType(), makeEntity(entity.getObject().getSubject()));
				result.combine(makeEventFrame(entity, result.extractHead(result), intermediate, object));
			}
			else {
				Mark.say(debug, "K2", entity.toString());
				result = makeEventFrame(entity, subject, entity.getType(), object);
			}
		}
		// Believed now caught by D
		// else if (entity.relationP(Markers.WANT_MARKER) || entity.relationP(Markers.THINK_MARKER)) {
		// Mark.say(true, "L", entity.toString());
		// RoleFrame subject = makeEntity(entity.getSubject());
		// RoleFrame object = newGenerate(entity.getObject());
		// if (entity.getObject().relationP() && entity.getObject().isA(Markers.ACTION_MARKER)) {
		// Mark.say(true, "L1", entity.toString());
		// RoleFrame intermediate = makeEntity("to");
		// result = makeEventFrame(entity, subject, entity.getType(), makeEntity(entity.getObject().getSubject()));
		// result.combine(makeEventFrame(entity, result.extractHead(result), intermediate, object));
		// }
		// else {
		// Mark.say(true, "L2", entity.toString());
		// result = makeEventFrame(entity, subject, entity.getType(), object);
		// }
		// }
		else if (entity.relationP(Markers.OWNER_MARKER) || entity.relationP(Markers.BODY_PART_MARKER)) {
			Mark.say(debug, "M", entity.toString());
			RoleFrame subject = makeEntity(entity.getSubject());
			RoleFrame object = makeEntity(entity.getObject());
			if (entity.relationP(Markers.BODY_PART_MARKER)) {
				Mark.say(debug, "M1", entity.toString());
				RoleFrame handle = subject;
				subject = object;
				object = handle;
			}
			object.indefinite();
			result = makeEventFrame(entity, subject, "have", object);
		}
		else if (entity.relationP(Markers.TIME_MARKER)) {
			Mark.say(debug, "N", entity.toString());
			RoleFrame subject = newGenerate(entity.getSubject());
			RoleFrame object = newGenerate(entity.getObject());
			result = makeEventFrame(entity, subject, entity.getType(), object);
		}
		else if (entity.relationP(Markers.COMPARISON_MARKER)) {
			Mark.say(debug, "O", entity.toString());
			RoleFrame subject = makeEntity(entity.getSubject());
			RoleFrame object = makeEntity(entity.getObject());
			RoleFrame x = makeEventFrame(entity, subject, "is", entity.getType());
			RoleFrame y = makeEventFrame(entity, x.extractHead(x), "than", object);
			result = x.combine(y);
		}
		else if (entity.relationP(Markers.ENTAIL_RULE)) {
			Mark.say(debug, "P", entity.toString());
			RoleFrame consequentRole = newGenerate(entity.getObject());
			consequentRole.addDecoration(consequentRole.getHead(), "has_tense", "ing");
			result = new RoleFrame();
			for (Entity x : entity.getSubject().getElements()) {
				String connector = Markers.LEADS_TO;
				String consequentHead = consequentRole.getHead();
				RoleFrame antecedentRole = newGenerate(x);
				RoleFrame oneClause = makeEventFrame(entity, antecedentRole, connector, consequentRole);
				// oneClause.addDecoration(consequentHead, "has_comp", "in_order");
				oneClause.addDecoration(consequentHead, "has_position", "leading");
				oneClause.addDecoration(oneClause.getHead(), "is_main", "yes");
				oneClause.addDecoration(consequentHead, "has_tense", "ing");
				oneClause.addDecoration(antecedentRole.getHead(), "has_tense", "ing");
				result.combine(oneClause);
				result.setHead(oneClause.getHead());
			}
		}
		else if (entity.relationP(Markers.MEANS)) {
			Mark.say(debug, "Q", entity.toString());
			RoleFrame consequentRole = newGenerate(entity.getObject());
			result = new RoleFrame();
			for (Entity x : entity.getSubject().getElements()) {
				String connector = Markers.HAS_PURPOSE;
				String consequentHead = consequentRole.getHead();
				RoleFrame oneClause = makeEventFrame(entity, newGenerate(x), connector, consequentRole);
				oneClause.addDecoration(consequentHead, "has_comp", "in_order");
				oneClause.addDecoration(consequentHead, "has_position", "leading");
				oneClause.addDecoration(oneClause.getHead(), "is_main", "yes");
				result.combine(oneClause);
				result.setHead(oneClause.getHead());
			}
		}
		else if (entity.relationP(Markers.CAUSE_MARKER)) {
			Mark.say(debug, "R", entity.toString());
			RoleFrame consequentRole = newGenerate(entity.getObject());
			result = new RoleFrame();
			for (Entity x : entity.getSubject().getElements()) {
				String connector = Markers.BECAUSE;
				if (entity.isAPrimed(Markers.IF_MARKER)) {
					connector = "if";
				}
				RoleFrame oneClause = makeEventFrame(entity, consequentRole, connector, newGenerate(x));
				result.combine(oneClause);
				result.setHead(oneClause.getHead());
			}
		}
		else if (entity.relationP(Markers.TO_MARKER)) {
			Mark.say(debug, "S", entity.toString());
			result = newGenerate(entity.getSubject());
			result.addRole(entity.getName(), makeEntity(entity.getObject().getType()));
		}
		else if (entity.relationP(Markers.PLACE_MARKER)) {
			Mark.say(debug, "T", entity.toString());
			Entity object = entity.getObject();
			result = makeEventFrame(entity, newGenerate(entity.getSubject()), "be", null);
			if (object.sequenceP(Markers.LOCATION_TYPE)) {
				Mark.say(debug, "X1", entity.toString());
				if (!object.getElements().isEmpty()) {
					for (Entity role : object.getElements()) {
						addPathElement(result, role);
					}
				}
			}
		}
		else if (entity.relationP(Markers.AT) || entity.relationP(Markers.ON) || entity.relationP(Markers.IN)) {
			Mark.say(debug, "U", entity.toString());
			if (entity.getSubject().relationP(Markers.PLACE_MARKER)) {
				Mark.say(debug, "U1", entity.toString());
				result = newGenerate(entity.getSubject());
			}
			else if (entity.getSubject().relationP(Markers.PROPERTY_TYPE)) { // Currently does same as above
				Mark.say(debug, "U2", entity.toString());
				result = newGenerate(entity.getSubject());
			}
			else {
				Mark.say(debug, "U3", entity.toString());
				result = makeEventFrame(entity, makeEntity(entity.getSubject()), "be", null);
			}
			Function loc = new Function(entity.getName(), entity.getObject());
			addPathElement(result, loc);
		}
		else if (entity.relationP(Markers.WITH_MARKER)) {
			Mark.say(debug, "V", entity.toString());
			result = newGenerate(entity.getSubject());
			result.addRole("with", newGenerate(entity.getObject()));
		}
		else {
			Mark.say(debug, "Nothing triggered", entity.toString());
		}
		if (entity.hasProperty(Markers.PROPERTY_TYPE)) {
			Mark.say(debug, "Z");
			String property = (String) entity.getProperty(Markers.PROPERTY_TYPE);
			if (property == Markers.EXPLANATION_RULE) {
				result.addInternalModifier("probably");
				Mark.say(debug, "PPP1");
			}
		}
		if (entity.getSubject().isA(Markers.SOMEBODY)) {
			Mark.say(debug, "Somebody trap");
			result.makePassive();

		}
		return result;
	}

	private RoleFrame newGenerate(Entity entity) {
		boolean debug = false;
		RoleFrame result = null;
		Mark.say(debug, "Entering newGenerate with", entity);
		if (entity == null) {
			Mark.err("newGenerate got null argument!");
			result = makeEntity("HelloWorld");
		}
		else if (entity.entityP()) {
			Mark.say(debug, "Generating Entity...", entity.toString());
			result = generateEntityWithQuantity(debug, entity);
			Mark.say(debug, "Done generating Entity.", entity.toString());
		}
		else if (entity.functionP()) {
			Mark.say(debug, "Generating Function...", entity.toString());
			if (entity.isA(Markers.DISAPPEAR_MARKER)) {
				Entity replacement;
				if (entity.getSubject().isA(Markers.PROPERTY_TYPE)) {
					Entity roleFrame = RoleFrames.makeRoleFrame(entity.getSubject().getSubject(), "is", RoleFrames.getObject(entity.getSubject()));
					replacement = RoleFrames.makeRoleFrame(entity.getSubject().getSubject(), "stop", roleFrame);
				}
				else {
					replacement = RoleFrames.makeRoleFrame(entity.getSubject().getSubject(), "stop", entity.getSubject());
				}
				result = newGenerate(replacement);

			}
			else if (entity.isA(Markers.APPEAR_MARKER)) {
				Entity replacement;
				if (entity.getSubject().isA(Markers.PROPERTY_TYPE)) {
					replacement = RoleFrames.makeRoleFrame(entity.getSubject().getSubject(), "become", RoleFrames.getObject(entity.getSubject()));
				}
				else {
					replacement = RoleFrames.makeRoleFrame(entity.getSubject().getSubject(), "begin", entity.getSubject());
				}
				result = newGenerate(replacement);
			}
			else {
				result = newGenerateFunction(debug, entity);
				Mark.say(debug, "Done generating Function.", entity.toString());
			}
		}
		else if (entity.sequenceP()) {
			Mark.say(debug, "Generating Sequence...", entity.toString());
			result = newGenerateSequence(debug, entity);
			Mark.say(debug, "Done generating Sequence.", entity.toString());
			debug = false;
		}
		else if (entity.relationP()) {
			Mark.say(debug, "Generating Relation...", entity.toString());
			try {
				result = newGenerateRelation(entity);
			}
			catch (Exception e) {
				Mark.err("Unable to generate relation from", entity);
			}
			Mark.say(debug, "Done generating Relation.", entity.toString());
		}
		if (result == null) {
			Mark.say(debug, "No result for", entity.toString());
		}
		else if (!entity.entityP() && result != null) {
			if (entity.hasFeature("not")) {
				result.negative();
			}
		}

		if (entity.hasFeature(Markers.PASSIVE)) {
			result.passive();
		}
		if (entity.hasFeature(Markers.PAST)) {
			result.past();
		}
		// if (entity.hasProperty(Markers.MODAL)) {
		// result.makeModal((String) entity.getProperty(Markers.MODAL));
		// }
		if (entity.hasProperty(Markers.PROPERTY_TYPE)) {
			Mark.say(debug, "PPP");
			String property = (String) entity.getProperty(Markers.PROPERTY_TYPE);
			if (property == Markers.EXPLANATION_RULE) {
				result.addInternalModifier("probably");
				Mark.say(debug, "PPP1");
			}
			// else if (property == Markers.ABDUCTION) {
			// Mark.say(debug, "PPP2");
			// // result.must();
			// }
		}
		return result;
	}

	public static void noteClauseHolders(Entity e) {
		e.addProperty(Markers.CLAUSE_HOLDERS, getClauseHolders(e));
	}

	private static Vector<Entity> getClauseHolders(Entity e) {
		Vector<Entity> result = new Vector<Entity>();
		if (e.entityP()) {
			if (e.getProperty(Markers.CLAUSES) != null) {
				if (!result.contains(e)) {
					result.add(e);
				}
			}
		}
		else if (e.functionP()) {
			addIfAbsent(getClauseHolders(e.getSubject()), result);
		}
		else if (e.relationP()) {
			addIfAbsent(getClauseHolders(e.getSubject()), result);
			addIfAbsent(getClauseHolders(e.getObject()), result);
		}
		else if (e.sequenceP()) {
			for (Entity x : e.getElements()) {
				addIfAbsent(getClauseHolders(x), result);
			}
		}
		return result;
	}

	private static void addIfAbsent(Vector<Entity> more, Vector<Entity> list) {
		for (Entity e : more) {
			if (list.contains(e)) {
				continue;
			}
			list.add(e);
		}
	}

	private RoleFrame makeEventFrame(Entity entity, Object subject, Object action, Object object) {
		RoleFrame roleFrame = new RoleFrame(subject, action, object);
		String tense = (String) (entity.getProperty(Markers.TENSE));
		String modal = (String) (entity.getProperty(Markers.MODAL));
		// String amount = (String) (entity.getProperty(Markers.QUANTITY));

		// Deal with modifying clauses

		// ArrayList<Entity> holders = getClauseHolders(entity);
		//
		// Mark.say("There are", holders.size(), "holders");
		//
		// if (!entity.entityP()) {
		// for (Entity holder : holders) {
		// for (Entity clause : (Vector<Entity>) holder.getProperty(Markers.CLAUSES)) {
		// roleFrame.combine(newGenerate(clause));
		// }
		// }
		// }

		if (entity.hasProperty(Markers.CLAUSE_HOLDERS)) {
			for (Entity holder : (Vector<Entity>) entity.getProperty(Markers.CLAUSE_HOLDERS)) {
				for (Entity clause : (Vector<Entity>) holder.getProperty(Markers.CLAUSES)) {
					roleFrame.combine(newGenerate(clause));
				}
			}
		}

		// Deal with tense
		if (Markers.PRESENT.equals(tense)) {
			if (Markers.WILL_WORD.equals(modal)) {
				roleFrame.future();
			}
			else if (Markers.MAY_WORD.equals(modal)) {
				roleFrame.may();
			}
		}
		else if (Markers.PAST.equals(tense)) {
			roleFrame.past();
		}
		else if (Markers.FUTURE.equals(tense)) {
		}

		// Deal with imperatives
		if (entity.getSubject().isA("you")) {
			roleFrame.imperative();
		}

		// Deal with progressive verbs
		if (entity.hasProperty(Markers.PROGRESSIVE)) {
			roleFrame.progressive();
		}

		// Deal with perfective construction
		if (entity.hasProperty(Markers.PERFECTIVE)) {
			roleFrame.perfective();
		}

		// Deal with adverbs
		ArrayList<Object> features = entity.getFeatures();
		for (Object feature : features) {
			// The possibly part deals with special case of using "possibly" to mark proximity rules;
			if (feature.equals(Markers.NOT) || feature.equals(Markers.POSSIBLY) || feature == Markers.ASSUMPTION) {
				continue;
			}
			// Cause doubling of modifier instruction, which caused appearance inside as well as at end.
			// roleFrame.addModifier(feature.toString());
		}

		// Get rid of junk from translation process
		// Needs more thinking. Screws up translation
		// entity.removeProperty(Markers.PROCESSED);
		return roleFrame;
	}

	private void addPathElement(RoleFrame result, Entity role) {
		// Mark.say("Entering addPathElement with", role.toString());
		if (role.getSubject().functionP("at")) {
			// Mark.say("Role sans function:", role.getType(), role.toString());
			result.addRole(role.getType(), newGenerate(role.getSubject().getSubject()));
		}
		else {
			// Mark.say("Role with function:", role.getType(), role.getSubject().toString());
			result.addRole(role.getType(), newGenerate(role.getSubject()));
		}

	}

	private Vector<Entity> withoutObject(Vector<Entity> elements) {

		Vector<Entity> result = new Vector<Entity>();
		for (Entity t : elements) {
			if (!t.functionP(Markers.OBJECT_MARKER)) {
				result.add(t);
			}
		}
		return result;
	}

	// private RoleFrameGrandParent theObject(Vector<Entity> elements) {
	// for (Entity t : elements) {
	// if (t.functionP(Markers.OBJECT_MARKER)) {
	// return makeEntity(t.getSubject());
	// }
	// }
	// return null;
	// }

	private Entity getObject(Vector<Entity> elements) {
		for (Entity t : elements) {
			if (t.functionP(Markers.OBJECT_MARKER)) {
				return t.getSubject();
			}
		}
		return null;
	}

	private boolean isExpression(Entity t) {
		if (t.relationP(Markers.PROPERTY_TYPE) || t.relationP(Markers.MENTAL_STATE_MARKER) || Predicates.thingRelationP(t, Markers.POSITION_TYPE)) {
			if (!t.relationP(Markers.ENTAIL_RULE)) {
				return true;
			}
		}
		return false;
	}

	// added by ahd for direct use in JSONEntity
	public RoleFrame makeEntityRoleFrame(Entity e) {
		return makeEntity(e);
	}

	private StartEntity makeEntity(String s) {
		StartEntity result = new StartEntity(s);
		return result;
	}

	private StartEntity makeEntity(Entity t) {
		return makeEntity(t, null);
	}


	private StartEntity makeEntity(Entity entity, String exclusion) {
		boolean debug = false;

		if (entity == null) {
			Mark.err("Null entity passed!");
			return null;
		}
		// Mark.say("Entering makeEntity for", t.toString());
		StartEntity result = null;
		Mark.say(debug, "Entity is", entity);
		if (false && entity.getType().equals("i")) {
			result = makeEntity(entity.getType() + "_" + entity.getName());
			Mark.say(debug, "I is", result);
		}
		// Not sure what this did; had effect of doubling xx_xx etc.
		// else if (variable(entity)) {
		// result = makeEntity(getTypeAboveName(entity) + "_" + entity.getName());
		// }
		else {
			result = makeEntity(entity.getName());
		}

		// At this point deal, for now, only with ownership

//		Object owner = entity.getProperty(Entity.OWNER);
//		if (owner != null && owner instanceof Entity) {
//			Mark.say("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			result.possessor(((Entity) owner).getType());
//		}

		boolean legacy = true;

		boolean talk = false;

		if (legacy) {
			Mark.say(talk, "Processing legacy generation");
			Object owner = entity.getProperty(Entity.OWNER);
			if (owner != null && owner instanceof Entity) {
				result.possessor(((Entity) owner).getType());
			}
		}
		else {
			Mark.say(talk, "Processing new analysis");
			if (entity.getProperty("owned") != null) {
				for (Entity subjectOf : entity.getSubjectOf()) {
					for (Entity elementOf : subjectOf.getElementOf()) {
						for (Entity objectOf : elementOf.getObjectOf()) {
							if (objectOf.isA("owns")) {
								Entity owner = objectOf.getSubject();
								result.possessor(((Entity) owner).getType());
							}
						}
					}
				}
			}
		}

		// // Deal with quantity here
		//
		// Object amount = entity.getProperty(Markers.QUANTITY);
		// if (amount != null){
		// result.addQuantity((String)amount);
		// }

		// Deal with features here

		ArrayList features = entity.getFeatures();

		if (features != null) {
			Mark.say(debug, "Features for", entity.toString(), "are", features);
		}

		if (features != null && features.size() > 0) {
			for (int i = 0; i < features.size(); ++i) {
				Object feature = features.get(i);
				if (feature.equals(exclusion)) {
					continue;
				}
				else if (feature != Markers.DEFINITE && feature != Markers.INDEFINITE && feature != Markers.NONE && feature != Markers.ANOTHER) {
					result.addFeature(features.get(i).toString());
				}
			}
		}

		Object determiner = entity.getProperty(Markers.DETERMINER);

		// First, check determiner thread
		if (entity.isAPrimed(Markers.DESCRIPTOR)) {
			result.noDeterminer();
			Mark.say(debug, "Entity", entity.toString(), "no determiner B");
		}
		else if (determiner == null) {
			result.noDeterminer();
			Mark.say(debug, "Entity", entity.toString(), "no determiner C");
		}
		else if (determiner.equals(Markers.ANOTHER)) {
			result.another();
			Mark.say(debug, "Entity", entity.toString(), "is another");
		}
		else if (determiner.equals(Markers.NONE)) {
			result.noDeterminer();
			Mark.say(debug, "Entity", entity.toString(), "no determiner D");
		}
		else if (determiner.equals(Markers.DEFINITE)) {
			result.definite();
			Mark.say(debug, "Entity", entity.toString(), "is definite");
		}
		else if (determiner.equals(Markers.INDEFINITE)) {
			result.indefinite();
			Mark.say(debug, "Entity", entity.toString(), "is indefinite");
		}
		else if (entity.isAPrimed(Markers.NAME)) {
			result.noDeterminer();
			Mark.say(debug, "Entity", entity.toString(), "no determiner A");
		}
		else if (entity.hasProperty(Markers.NAME) || entity.hasProperty(Markers.PROPER)) {
			result.noDeterminer();
			Mark.say(debug, "Entity", entity.toString(), "no determiner A");
		}
		// else if (t.isA(Markers.NAME)) {
		// result.noDeterminer();
		// Mark.say(debug, "Entity", t.toString(), "no determiner E");
		// }
		else {
			// result.definite();
			Mark.say(debug, "Entity", entity.toString(), "has no marker");
		}
		return result;
	}

	private String getTypeAboveName(Entity t) {
		Thread x = t.getPrimedThread();
		for (int i = 0; i < x.size() - 1; ++i) {
			if (x.get(i + 1).equalsIgnoreCase(Markers.NAME)) {
				return x.get(i);
			}
		}
		return x.getType();
	}

	private boolean variable(Entity t) {
		if (t == null) {
			Mark.err("Null Entity object passed!");
			return false;
		}
		String type = t.getType();
		if (type == null) {
			return false;
		}
		else if (type.equalsIgnoreCase("aa") || type.equalsIgnoreCase("ww") || type.equalsIgnoreCase("xx") || type.equalsIgnoreCase("yy")
		        || type.equalsIgnoreCase("zz")) {
			return true;
		}
		return false;
	}

	public void runAllTests() {
		Connections.getPorts(this).transmit(TEST, TabbedTextViewer.CLEAR);
		new RunTests().start();
	}

	private class RunTests extends java.lang.Thread {
		public void run() {
			Mark.say("Running tests");
			// runProblemTests();
			runBasicTests();
			// runEmbeddingTests();
			runSubordinateTests();
		}
	}

	public void runSubordinateTests() {
		// Connections.getPorts(this).transmit(TEST, TabbedTextViewer.CLEAR);
		StartEntity e = new StartEntity("package");

		RoleFrame rf1 = new RoleFrame(e, "disappear").past();

		Generator.getGenerator().test(rf1, "The package disappeared.");

		RoleFrame rf2 = new RoleFrame("man", "bring", e).past();

		Generator.getGenerator().test(rf2, "The man brought the package.");

		e.that(rf2);

		RoleFrame rf3 = new RoleFrame(e, "disappear").past();

		Generator.getGenerator().test(rf3, "The package that the man brought disappeared.");

		e = new StartEntity("package");

		rf1 = new RoleFrame(e, "disappear").past();

		rf2 = new RoleFrame("man", "bring", e).past();

		Generator.getGenerator().test(rf2, "The man brought the package.");

		e.which(rf2);

		rf3 = new RoleFrame(e, "disappear").past();

		Generator.getGenerator().test(rf3, "The package which the man brought disappeared.");

		StartEntity e1 = new StartEntity("soldier");
		StartEntity e2 = new StartEntity("soldier");
		StartEntity e3 = new StartEntity("officer");

		RoleFrame x1 = new RoleFrame(e1, "dug", "hole").past();
		RoleFrame x2 = new RoleFrame(e3, "commend", e2).past();
		e1.who(x1);
		e2.whom(x2);
		RoleFrame x3 = new RoleFrame(e1, "approach", e2);

		Generator.getGenerator().test(x3, "The soldier who dug the hole approaches the soldier whom the officer commended.");

		e1 = new StartEntity("soldier");
		e2 = new StartEntity("soldier");
		e3 = new StartEntity("officer");

		x1 = new RoleFrame(e1, "dug", "hole").past();
		x2 = new RoleFrame(e3, "commend", e1).past();
		e1.who(x1);
		e1.reset();
		e1.whom(x2);
		x3 = new RoleFrame(e1, "approach", e1);

		Generator.getGenerator().test(x3, "The soldier whom the officer commended approaches himself.");

	}

	public void runProblemTests() {

		Generator generator = Generator.getGenerator();

		RoleFrame c1 = new RoleFrame("man", "ran");

		RoleFrame c2 = new RoleFrame("soldier", "appear");
		generator.test(new StartEntity("dog").possessor("man"), "The man's dog");
	}

	public void runBasicTests() {
		Connections.getPorts(this).transmit(TEST, TabbedTextViewer.CLEAR);
		// Obtain singleton of generator class
		Generator generator = Generator.getGenerator();

		// // Test basic operation

		// Entities

		System.out.println("\nEntities\n");

		generator.test(new StartEntity("man"), "The man");
		generator.test(new StartEntity("man").definite(), "The man");
		generator.test(new StartEntity("man").indefinite(), "A man");
		generator.test(new StartEntity("men").noDeterminer(), "Men");
		generator.test(new StartEntity("man").feature("large"), "The large man");
		generator.test(new StartEntity("dog").possessor("man"), "The man's dog");
		generator.test(new StartEntity("arm").possessor("man"), "The man's arm");

		// System.out.println("\nRole frames\n");

		generator.test(new RoleFrame("man", "dig", "hole"), "The man digs the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").present(), "The man digs the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").past(), "The man dug the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").future(), "The man will dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").passive(), "The hole is dug by the man.");
		generator.test(new RoleFrame("man", "dig", "hole").negative(), "The man doesn't dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").may(), "The man may dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").progressive(), "The man is digging the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").modify("quickly"), "The man digs the hole quickly."); // Connections

		System.out.println("\nConnections\n");

		RoleFrame c1 = new RoleFrame("man", "ran");
		RoleFrame c2 = new RoleFrame("soldier", "appear");
		generator.test(c1.connect("after", c2), "The man runs after the soldier appears.");
		generator.test(c1.after(c2), "The man runs after the soldier appears.");
		generator.test(c1.before(c2), "The man runs before the soldier appears.");
		generator.test(c1.makeWhile(c2), "The man runs while the soldier appears.");
		generator.test(c1.because(c2), "The man runs because the soldier appears."); // Miscellaneous

		// System.out.println("\nMiscellaneous\n");

		generator.test(new RoleFrame("boy", "hit", "ball").addRole("with", "bat")
		        .addRole("toward", "fence"), "The boy hits the ball with the bat toward the fence.");
		generator.test(new RoleFrame("boy", "hit", "ball").makeNegative().makePast().makePassive(), "The ball wasn't hit by the boy."); // eye

		System.out.println("\nMind's Eye\n");

		StartEntity man = new StartEntity("man").addFeature("large").makeIndefinite();
		StartEntity woman = new StartEntity("woman").addFeature("tall");
		StartEntity soldier = new StartEntity("soldier");
		StartEntity box = new StartEntity("box").addFeature("heavy").addFeature("black").makeIndefinite();
		StartEntity ball = new StartEntity("ball").addFeature("green");
		generator.test(new RoleFrame(ball, "bounce").progressive(), "The green ball is bouncing.");
		generator.test(new RoleFrame(man, "give", box).addRole("to", woman).makePast(), "A large man gave a heavy black box to the tall woman.");
		generator.test(new RoleFrame(man.definite(), "flee").future()
		        .after(new RoleFrame(soldier, "appear").present()), "The large man will flee after the soldier appears.");

	}

	public void runEmbeddingTests() {

		// // Obtain singleton of generator class

		Generator generator = Generator.getGenerator();

		// // Test using objects identified by that, which, who,
		// and whom

		// Set up entities

		StartEntity x = new StartEntity("man-1").addFeature("tall");
		StartEntity y = new StartEntity("man-2").addFeature("short");
		StartEntity c = new StartEntity("woman");
		StartEntity p1 = new StartEntity("package");
		StartEntity p2 = new StartEntity("package");
		StartEntity p3 = new StartEntity("package").restrict("with", new StartEntity("ribbon").indefinite());
		StartEntity d = new StartEntity("child");

		// Identify entities by participation in actions
		p1.that(new RoleFrame(c, "bring", p1).past());

		p2.which(new RoleFrame(c, "bring", p2).past());

		d.whom(new RoleFrame(c, "bring", d).past());

		// Embed identified entities in higher-level actions

		RoleFrame g1That = new RoleFrame(x, "give", p1).past();

		g1That.addRole("to", y);

		g1That.addModifier("now");

		RoleFrame g2Which = new RoleFrame(x, "give", p2).past();

		g2Which.addRole("to", y);

		g2Which.addModifier("now");

		RoleFrame g3Whom = new RoleFrame(y, "kiss", d).past();

		generator.test(g1That, "The tall man gave the package that the woman brought to the short man now.");

		generator.test(g2Which, "The tall man gave the package which the woman brought to the short man now.");

		generator.test(g3Whom, "The short man kissed the child whom the woman brought.");
	}

	// Redone cache to work on entities, not triples

	private static HashMap<String, String> startGeneratorCache;

	public String generateViaCache(Entity t) {
	    return generateViaCache(t, Markers.PRESENT);
	}

	private String generateViaCache(Entity t, String marker) {
	    if (t.relationP()) {
	        t.addFeature(marker);
	    }
		// Mark.say("Entering generate via cache");
		String key = t.hashForGenerator();

		String result = getStartGeneratorCache().get(key);

		if (result == null) {
			// Mark.say("Nothing saved for", key);
		}
		else {
			// Mark.say("Found", key, "in cache");
		}
		return result;
	}

	private void saveInCache(Entity t, String marker, String result) {
	    if (t.relationP()) {
	        t.addFeature(marker);
	    }
		// Mark.say("Hash is", t.hash());
		// String key = marker + ": " + t.hashForGenerator();
		String key = t.hashForGenerator();
		// Mark.say("Generator storage hash is", key);
		// Mark.say("Saving in cache ", key);
		getStartGeneratorCache().put(key, result);
	}

	public static HashMap<String, String> getStartGeneratorCache() {
		if (startGeneratorCache == null) {
			startGeneratorCache = new HashMap<String, String>();
		}
		return startGeneratorCache;
	}

	public static void setStartGeneratorCache(HashMap<String, String> cache) {
		startGeneratorCache = cache;
	}

	public static void main(String[] ignore) throws Exception {
		BasicTranslator t = BasicTranslator.getTranslator();
		Generator g = Generator.getGenerator();
		/*
		 * Entity s = t.translate("Macbeth wants to be king").get(0); Mark.say(s); Mark.say(g.generate(s)); Entity w =
		 * new Function("question", s); w.addType("why"); Mark.say("S", s); Mark.say("G", g.generate(s)); Mark.say("w",
		 * w); Mark.say("x", g.generate(w)); Mark.say("y", g.generateTriples(w)); // Entity s =
		 * t.translate("Macbeth plays drums with gusto with Mary."); // Entity x = s.getElements().get(0); //
		 * Mark.say("Role frame", x); // Mark.say("English from Innerese", Generator.getGenerator().generate(x)); s =
		 * t.translate("A bird flew to a tree because a cat appeared"); s =
		 * t.translate("In order to become the king, Macbeth murdered Duncan and  killed the guards"); Mark.say(s);
		 */

		// Entity e = t.translate("Mary stops being criminal");

		// Entity e = t.translate("Macbeth is the king");

		// Entity e = t.translateToEntity("You put bowl on table");
		// e = RoleFrames.makeRoleFrame(new Entity("i"), "did", e);
		// String s = g.generate(e);
		// Mark.say("Result:\n", e, "\n", s);

		// Entity e = t.translateToEntity("In order to construct a tower, George follows these steps");
		// String s = g.generate(e);
		// Mark.say("Result:\n", e, "\n", s);
		//
		// e = t.translateToEntity("In order to construct a tower, I follow these steps");
		// s = g.generate(e);
		// Mark.say("Result:\n", e, "\n", s);
		//
		// e = t.translateToEntity("In order to construct a tower, you follow these steps");
		// s = g.generate(e);
		// Mark.say("Result:\n", e, "\n", s);
		
		Entity way = RoleFrames.makeRoleFrame("teacher", "offers", "nothing");
		String s = g.generate(way);
		Mark.say("Result:\n", s);


	}

}

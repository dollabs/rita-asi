package translator;

import java.util.*;

import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
//import emanueleCeccarelli.UPP.UnifiedPlausibilityParser;
import generator.Generator;
import generator.RoleFrames;
import genesis.GenesisGetters;
import start.Start;
import utils.*;
import utils.tools.Predicates;

/*
 * See DemoTranslator class for examples.
 */

public class BasicTranslator extends NewRuleSet {

	private boolean debug = false;

	public final static String LEFT = "left";

	public final static String RIGHT = "right";

	public final static String RESULT = "result";

	public final static String DEBUGGING_RESULT = "debugging result";

	public final static String DIALOG_PORT = "dialog port";

	private static BasicTranslator basicTranslator;

	private GenesisGetters gauntlet;


	public static BasicTranslator getTranslator() {
		if (basicTranslator == null) {
			basicTranslator = new BasicTranslator();
		}
		return basicTranslator;
	}

	public BasicTranslator() {
		this(null);
	}

	/*
	 * Establishes the initial values of the parse and the transformation list
	 */
	public BasicTranslator(GenesisGetters gauntlet) {
		super();
		this.gauntlet = gauntlet;

		Connections.getPorts(this).addSignalProcessor(PARSE, this::setInput);
		// Connections.getPorts(this).addSignalProcessor(LEFT, "setInputLeft");
		// Connections.getPorts(this).addSignalProcessor(RIGHT,
		// "setInputRight");
		Connections.getPorts(this).addSignalProcessor(PROCESS, this::process);
		// Connections.getPorts(this).addSignalProcessor(STEP, "step");
	}

	public void process(Object object) {

		boolean debug = false;
		Mark.say(debug, "Process input", object);

		String marker = null;
		if (object instanceof BetterSignal) {
			marker = ((BetterSignal) object).get(0, String.class);
			object = ((BetterSignal) object).get(1, Object.class);

		}
		if (!(object instanceof Sequence)) {
			return;
		}

		Entity x = ((Sequence) object).getElement(0);

		if (Predicates.isCause(x)) {
			Mark.say("Translator input", x, x.getObject().getPropertyList(), x.getObject().getProbability());
		}

		Mark.say(debug, "Semantic expert gets:", ((Sequence) object).asString());
		Entity result = interpret(object);
		Mark.say(debug, "Semantic expert reports:", result.asString());
		// removeProcessedFeature(result);

		if (marker != null) {
			Mark.err("What is going on here, no reciever for transmission");
			Connections.getPorts(this).transmit(DIALOG_PORT, new BetterSignal(marker, result));
		}
		else {
			Mark.say(debug, "Transmitted result is", result);
			Connections.getPorts(this).transmit(RESULT, result);
		}
	}

	private void reportCall(Object object, String label) {
		Mark.say("\n>>>  Translator received on", label);
		Sequence s = (Sequence) object;
		for (Entity e : s.getElements()) {
			Mark.say("Entity", e);
		}
	}

	public Entity parse(String s) throws Exception {
		return Start.getStart().parse(s);
	}

	public Entity translateToEntity(String input) {
		Entity s = translate(input);
		if (s.getElements().size() != 1) {
			Mark.err("Unable to translate", s, "into a single entity");
			return null;
		}
		else {
			return s.get(0);
		}
	}

	public Entity translate(String s) {
		return translateWithoutUpp(s);
	}

	public Entity translateWithoutUpp(String s) {
		boolean debug = true;
		Mark.purple(Z.START_DEBUG, s);
		try {
			
			// all the sequences created by nesting basic relations together, including syntactic features
			Sequence parse = (Sequence) parse(s);
			Mark.purple(Z.START_DEBUG, parse.getElements().size());
//			Z.understand(parse);
			
			
			Entity result = interpret(parse);
			Mark.purple(Z.START_DEBUG, result.getElements().size());
			if(Z.START_DEBUG) Z.understand(result);
			
			if (result == null) {
				Mark.say(debug, "Bad news working on", s);
				Mark.say(debug, "Parse is", parse);
				Mark.say(debug, "Parse produces", result);
			}
			return result;
		}
		catch (Exception e) {
			Mark.err("Unable to translate", s);
			e.printStackTrace();
			return null;
		}
	}

	// Special case translator for Mind's Eye
	public Entity translate(String s, Integer start, Integer end) throws Exception {
		Entity result = interpret(parse(s));
		return translate(result, start, end);
	}

	public Entity translate(Entity result, Integer start, Integer end) {
		if (result != null) {
			if (result.relationP() && result.getObject().sequenceP()) {
				Sequence roles = (Sequence) (result.getObject());
				if (start != null) {
					Entity from = new Entity(start.toString());
					from.addFeature(Markers.NONE);
					if (end == null) {
						roles.addElement(new Function(Markers.AT, from));
					}
					else {
						roles.addElement(new Function(Markers.FROM, from));
					}
				}
				if (end != null) {
					Entity to = new Entity(end.toString());
					to.addFeature(Markers.NONE);
					if (start == null) {
						roles.addElement(new Function(Markers.AT, to));
					}
					else {
						roles.addElement(new Function(Markers.TO, to));
					}
				}
			}
		}
		return result;
	}



	public Entity interpret(Object o) {
		boolean debug = false;
		if (o instanceof Sequence) {
			Mark.say(debug, "Interpreting", o);
			parse = (Sequence) o;
			setInput(parse);

			// Z: use all translation rules/experts to form the final sequence
			NewTimer.translationTimer.reset();
			Sequence result = (Sequence) getResult();
			NewTimer.translationTimer.report(true, "Translator slow beyond description for " + result);

			// Z: TODO: Haven't know what the Group thing does
			removeProcessedFeature(result);
			Mark.say(debug, "Interpretation is", result);
			List<Entity> elements = result.getElements();
			if (elements.size() > 1) {
				elements.stream().forEach(e -> {
					e.addProperty(Markers.GROUP, elements);
				});

			}
			// Not needed here, done elsewhere?
			// ifGroupAddGroupProperty(result);
			return result;
		}
		return null;
	}

	private void removeProcessedFeature(Entity link) {
		if (link.hasFeature(Markers.PROCESSED)) {
			link.removeFeature(Markers.PROCESSED);
		}
		if (link.entityP()) {
		}
		else if (link.functionP()) {
			Function d = (Function) link;
			removeProcessedFeature(d.getSubject());
		}
		else if (link.relationP()) {
			Relation r = (Relation) link;
			removeProcessedFeature(r.getSubject());
			removeProcessedFeature(r.getObject());
		}
		else if (link.sequenceP()) {
			Sequence s = (Sequence) link;
			for (Entity element : s.getElements()) {
				removeProcessedFeature(element);
			}
		}
	}

	public boolean rachet() {
		int size = getTransformations().size();
		transform();
		if (getTransformations().size() == size) {
			return false;
		}
		return true;
	}

	// Stuff below here obsolescent; may be used by Demo for debugging

	private class LocalGoClass extends java.lang.Thread {
		
		public void run() {
			Mark.mit("run");
			try {
				while (step()) {
					if (gauntlet != null && Switch.stepParser.isSelected()) {
						try {
							sleep(delta);
						}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				Sequence result = (Sequence) getResult();
				// Mark.say("Done", result.asString());
				reportLocalGoResult(result);
			}
			catch(ArrayIndexOutOfBoundsException e) {
				
			}
		}
		
		

	}

	private void reportLocalGoResult(Sequence result) {
		Connections.getPorts(this).transmit(DEBUGGING_RESULT, result);
	}

	private void ifGroupAddGroupProperty(Sequence sequence) {
		List<Entity> elements = sequence.getElements();
		if (elements.size() > 1) {
			elements.stream().forEach(e -> {
				e.addProperty(Markers.GROUP, elements);
			});

		}
	}

	// private class ShowResult implements Runnable {
	// public void run() {
	// Connections.getPorts(HardWiredTranslator.this).transmit(PROGRESS,
	// getTransformations().get(getTransformations().size() - 1));
	// }
	//
	// }

	private static int delta = 0;

	public static String PARSE = "parse", STEP = "step", PROCESS = "process", RUN = "run";

	public static String PROGRESS = "progress";

	// The link parse provided by the parser
	private Sequence parse;

	// A sequence of transformations of the parse.
	private ArrayList<Sequence> transformations;

	private boolean transmittable = false;

	public Sequence getParse() {
		if (parse == null) {
			parse = new Sequence();
		}
		return parse;
	}

	public ArrayList<Sequence> getTransformations() {
//		Mark.purple(transformations);
		if (transformations == null) {
			transformations = new ArrayList<Sequence>();
		}
		return transformations;
	}

	public void go() {
		new LocalGoClass().start();
	}

	private void removeParts(Entity thing, Vector v) {
		Vector links = (Vector) v.clone();
		for (Iterator<Entity> i = links.iterator(); i.hasNext();) {
			Entity t = i.next();
			if (!t.isA("parse-link")) {
				v.remove(t);
			}
		}
	}

	public void setInput(Object o) {

		if (o instanceof Sequence) {
			parse = (Sequence) o;
			getTransformations().clear();
			getTransformations().add(parse);
		}
		else {
			if (o == null) {
				Mark.err("Input is null");
			}
			else {
			Mark.err("Failed to recognize type in Translator.setInput", o.getClass());
			}}
	}

	public void setInputAndStep(Object o) {
		if (o instanceof Sequence) {
			parse = (Sequence) o;
			setInput(parse);
			go();
		}
	}

	public void setParse(Sequence parse) {
		this.parse = parse;
	}

	public void setTransformations(ArrayList<Sequence> transformations) {
		this.transformations = transformations;
	}

	public Entity getResult() {
//		Mark.yellow("get Result");
		while (step()) {
		}
		return getTransformations().get(getTransformations().size() - 1);
	}

	public boolean step() {
		if (rachet()) {
			// SwingUtilities.invokeLater(new ShowResult());
			Sequence sequence = getTransformations().get(getTransformations().size() - 1);
			Connections.getPorts(BasicTranslator.this).transmit(PROGRESS, sequence);
			return true;
		}
		else {
			Sequence result = getTransformations().get(getTransformations().size() - 1);

			ifGroupAddGroupProperty(result);
			Connections.getPorts(BasicTranslator.this).transmit(PROGRESS, result);

			Vector v = result.getElements();

			Sequence sequence = new Sequence(Markers.SEMANTIC_INTERPRETATION);
			for (Iterator i = v.iterator(); i.hasNext();) {
				Entity x = (Entity) (i.next());
				if (!x.relationP()) {
					continue;
				}
				Relation t = (Relation) x;
				if (t.getSubject().isA("root")) {
					sequence.addElement(t.getObject());
				}
			}
			Connections.getPorts(this).transmit(sequence);
			return false;
		}
	}

	public boolean step(Object o) {
		return step();
	}

	/*
	 * Attempts to extend the transformation list.
	 */
	public void transform() {
		int lastIndex = getTransformations().size() - 1;
		Sequence sequence = getTransformations().get(lastIndex);
//		Mark.night(sequence);
		Sequence result = transform(sequence);
		if (result != null) {
			getTransformations().add(result);
		}
	}

	private boolean transform(BasicRule runnable, Sequence s) {
		runnable.setLinks(s);
		if (s.getElements().size() < 1) {
			return false;
		}
		if (runnable instanceof BasicRule3) {
			// Looks at three links
			for (int i = 0; i < s.getElements().size(); ++i) {
				if (s.getElements().get(i) == null) {
					continue;
				}
				for (int j = i + 1; j != i && j < s.getElements().size(); ++j) {
					if (s.getElements().get(j) == null) {
						continue;
					}
					for (int k = j + 1; k != j && k != i && k < s.getElements().size(); ++k) {
						if (s.getElements().get(k) == null) {
							continue;
						}
						runnable.setLinks(s.getElements().get(i), s.getElements().get(j), s.getElements().get(k));
						runnable.run();
						if (runnable.hasSucceeded()) {
							if (Switch.showTranslationDetails.isSelected()) {
								// Do not comment this out.
								// Change switch "Note translation details" setting in controls
								Mark.say("Rule " + runnable.getClass().getName() + " succeeded");
							}
							return true;
						}
					}
				}
			}
		}
		else if (runnable instanceof BasicRule2) {
			// Looks at two links
			for (int i = 0; i < s.getElements().size(); ++i) {
				if (s.getElements() == null) {
					continue;
				}
				for (int j = i + 1; j != i && j < s.getElements().size(); ++j) {
					if (s.getElements().get(j) == null) {
						continue;
					}
					runnable.setLinks(s.getElements().get(i), s.getElements().get(j));
					runnable.run();
					if (runnable.hasSucceeded()) {
						if (Z.START_DEBUG && Switch.showTranslationDetails.isSelected()) {
							// Do not comment this out.
							// Change switch "Note translation details" setting in controls
							Mark.say("Rule " + runnable.getClass().getName() + " succeeded");
						}
						return true;
					}
				}
			}
		}
		else if (runnable instanceof BasicRule) {

			// Looks at single link
			for (int i = 0; i < s.getElements().size(); ++i) {
				if (s.getElements().get(i) == null) {
					continue;
				}
				runnable.setLinks(s.getElements().get(i));
				runnable.run();
				if (runnable.hasSucceeded()) {
					if (Switch.showTranslationDetails.isSelected()) {
						// Do not comment this out.
						// Change switch "Note translation details" setting in controls
						Mark.say(Z.START_DEBUG, "Rule " + runnable.getClass().getName() + " succeeded");
					}
					return true;
				}
			}

		}
		return false;

	}

	/*
	 * Examines a particular transform and attempts to transform it using recognition rules
	 */
	private Sequence transform(Sequence s) {
		
		String[] expertIKnow = {
				
				"InputJunkExpert", 
				// remove all the syntactic relations from the sequence of relations:
				// remove "verb_root", "has_person", "has_quality", "has_sign", "has_number", "is_clausal"
				// "has_surface_form", "has_position", "has_category", "has_voice", "has_argument"
				// "has_clause_type", "has_conjunction", "has_counter", "is_wh", "has_nominalization",
				// "is_topic", "has_surface_subject", "has_quoting"
				
				// -----------
				"ingExpert", // given "(rel gerund_of (ent jumping-103) (ent jump-168))", change "(ent jumping-103)" to "(ent jump-168)" in all frames
				
				"PartOfExpert", // change "(rel of (ent part-96) (ent ...))" into "part_of" frame
				
				"PropertyExpert", // BasicRule2, change "is" and "has_property" to "has-mental-state" if the object is a kind of "mental_state"
				// e.g., "[henry is+1 happy-1]" and "[henry has_property+1 happy-2]"
				
//				"SometimesTrap", // change "sometimes" in "has_modifier" to to a "property" frame
				// then add "IDIOM" property equals "sometimes" to the subject,
				// e.g., "[entail+2 has_modifier+1 sometimes-1]"
				// then PropertyAbsorber will continue working on it
				
				"PropertyAbsorber", // unwrap the "property" frame and retain only the subject
				
				"ConceptExpert", // change "describe" frame to "reflection" frame when parsing "Start description of \"Concept\"."
				
				"StoryExpert", // BasicRule2; change "(rel start (ent you-92) (ent story-117))" and "(rel is_called (ent story-117) (ent macbeth_plot-125))" 
				// into " (rel start (ent you-92) (fun story (ent macbeth_plot-125)))"
				
				"ActionExpert", // turn relation frames into sequence frames
				// 	the relation is not CAUSE: "cause" "move" "help" "prepare" "trigger"; "do"; "establish"; and it doesn't have "roles" sequence frame
				
				
				// -------------
				"CheckOnExpert", // unwrap "whether" frame inside "check" frame
				
				"ModeExpert", // (1) extract the modal word in "has_modal" to a property of the subject, e.g., "may" in (rel has_modal (rel steal ...) (ent may-173))
				// (2) unwrap "then" in "(rel has_modifier (rel RR) (ent then-158))", create new scene entity
				// (3) unwrap modifier such as"possibly" in "(rel has_modifier (rel fly ...) (ent possibly-132))" and make it a "manner" of the subject
				
				"ThreadExpert", // change "(rel is-a (ent xx-92) (ent dog-98))" to "(rel classification (ent dog-98) (ent xx-92))"
				// use function "translateIsAToRoleFrame()" that handles different "is-a" types, by "IfClassExpert"
				// 
				
				"RoleExpert", // change relations to functions if they are the following words:
				// roleWords = "of", "by", "as", "with", "for", "at", "about", "in", "against", "whether", Markers.MANNER_MARKER
				// timeWords = "before", "after", "while"
				// placePrepositions = "at", "side", "top", "bottom", "left", "right", "inside", "front", "back", "on", "next_to"
				// pathPrepositions = "to_the_left_of", "to_the_right_of", "across", "from", "to", "into", "under", "toward", "towards", "via", "behind", "between", "past", "by", "over", "above", "down", "up", "below", "on", "in", "off"
				// locationPrepositions = "to_the_left_of", "to_the_right_of", "next_to", "close to", "far away from", "around", "over", "under", "behind", "between", "by", "above", "down", "up", "below", "on", "in", "near"
		        // Example: [run+4 to+4 lake+7893]         (rel to (rel run (ent cat-210) (ent null-97)) (ent lake-223))
				
				"TransitionExpert", // replace words with their type in transitionWords 
				// = "occur", "happen", "appear", "disappear", "change", "increase", "decrease";
				
				"TransferExpert", // for words in transferWords "give", "take", "throw", "pass", "receive"
				// add verb "transfer" to their threads

				"SocialExpert", // change relation "is-a" R that has B "related-to" agent A to R(A, B)
				
				"JobExpert", // if the relation is not an action and the object is a job word, such as
				// 	"ruler", "legislator", "administrator", "leader", "professional", "judge"

				"PossessionExpert2", // change "related-to" frame into "owner" property 
				
				"NameExpert", // processes "has_det" and "is_proper" 

				"GoalExpert", // add "action > goal > goalWord" as the thread of goalWords, including  "want" and "try"

				"ForceAndPersuadeExpert", // add "persuade" marker to persuadeWords ("persuade", "ask"); add "force" marker to forceWords = ("force", "coerce") 
				
				"NegationExpert", // change "is_negative" to "not"+

				"AdjectiveExpert", // given "has_property" frame, put the object as the feature of subject
				// e.g., "(rel has_property (ent event-97) (ent jump-168))"
				
				// --------- 
				"EntailExpert", // add "cause" type to "entail" frame (translated from "leads to") to ""
				
				"DidQuestionExpert", // change "is_question" with object "yes" into a question frame
				// from the object of the subject, we add the type of question, (RoleFrames.getObject(firstLinkSubject))
				// e.g., "(rel is_question (rel move (ent person-92) (ent null-96)) (ent yes-211))"
				
				"ProgressiveExpert", // detect progressive tense and mark as property
				//------
				
				"IfExpert", // change "if" frame to "cause" frame with a type "if"
				
				"CauseExpert", // change "because" frame to "cause" frame
				
				"CauseAntecedantsStarter", // wrap the frame of cause in conjunction sequence, in case there are multiple antecedants
				
				"CauseAntecedantsExpert", // BasicRule2, merge two causes in conjunction frames
				
				"ImperativesAndQuestionsExpert", // BasicRule2, change "is_imperative" to feature "is_imperative=yes"
				// e.g., first relation "(rel start (ent you-92) (seq roles (fun object (fun reflection (ent revenge-122)))))"
				// second relation "(rel is_imperative (rel start (ent you-92) (seq roles (fun object (fun reflection (ent revenge-122))))) (ent yes-167))"
				
				"ImperativesAndQuestionsExpert1", // (2) change "imagine" relation as "imagine" function
				
				"MainClauseExpert", // process "is_main" to mark the main relation of a sentence
				
				"RelativeClauseExpert", // unwrap "has_rel_clause" relation to clause inside entity frames
				// (rel has_rel_clause (ent bird-113) (rel want (ent somebody-120) (seq roles (fun object (rel fly (ent bird-113) (seq roles))))))
				
				"ClauseExpert", // BasicRule2, if the first relation has property "main" and second relation has property "has_rel_clause", 
				// put the second relation as the "clause" property of the first thing and remove the second thing from the list of sequences
				
				
				// ------
				"AdvanceExpert", // sentence "Advance video to frame 27" has triple "[advance+1 has_purpose+1 frame+2]"
				// thus take "has_purpose" frame to create "advance(video, frame)" frame
				
				
				"PurgeEmbeddingsExpert", // remove a relation if it is the subject of another relation, which is likely to be "passive_aux" and "has_tense"
				
				// ---------
				"MannerExpert", // change "has_method" to function "by"
				// e.g., original: "(rel has_method (rel murder (ent null-96) (ent null-96)) (rel stab (ent null-96) (ent null-96)))"
				// intermediate frames fill out the subjects and objects
				// modified: " (rel murder (ent macbeth-105) (seq roles (fun object (ent duncan-108)) (fun by (rel stab (ent macbeth-105) (seq roles (fun object (ent duncan-108)) (fun with (ent knife-112)))))))"
				
				"NoteGrammarFeatures", // process "passive_aux" and "has_tense" that wrap relations, then unwrap that relation
				
				"EachOtherExpert", // BasicRule2, get the subjects of two "each_other" frames and place in the other one's object flot
				// using source.innerese.ispeak.getSlot
				// e.g., John and Mary love each other
				
				// --------------------------
				"TimeMarkerExpert", // (1) if relation is "property" and object is timeMarkerWords = ("then", "later", "afterward", "next");
				// (2) if relation is "pass" and subject is a kind of ("time")
				// make new function "milestone" with object of the relation as the new relation
				
				
				"OutputJunkExpert" // clear all other frames with relations:
				// "has_attribute", "has_root", "has_det", "is_pp", "has_comp", "related-to", "happen"
				
				// there should be a question expert
				
				};
		
		Sequence old = s;
		Boolean skipDetails = false;
		for (Rule rule : getRuleSet()) {
			// New approach using runnables
			BasicRule runnable = rule.getRunnable();
			if (runnable != null) {
				if (transform(runnable, s)) {
					if (s.toString().equalsIgnoreCase(old.toString())) {
						
						for (String expert: expertIKnow) {
							if (rule.getName().equalsIgnoreCase(expert)) {
								skipDetails = true;
							}
						}
						
						if (!skipDetails) {
							for (Entity ent : s.getElements()) {
//								Mark.yellow(ent); // TODO: Unblock me
//								if (!ent.getType().equals("has_tense") &&
//										!ent.getType().equals("passive_aux")) {
//									Z.understand(ent);
//								}
							}
						}
						if(Z.START_DEBUG) System.out.println();
						
						old = s;
						skipDetails = false;
					}
					return s;
				}
			}
		}
		return null;
	}
	
	public static void test() {
		BasicTranslator t = BasicTranslator.getTranslator();
		List<String> strings = new ArrayList<>();
		strings.add("I found a path from \"John loves Mary\" to \"Mary is smart.\"");
		strings.add("John loves Mary");
		strings.add("John says \"Mary is crazy.\"");
		strings.add("John: Mary is crazy.");
		strings.add("I believe America is militaristic");
		strings.add("Find a path from xx to yy.");
		strings.add("You put bowl on table");
		strings.add("Macbeth murders Duncan");
		for (String str : strings) {
			Mark.red(str);
			Entity ent = t.translate(str);
			ent.prettyPrint();
			ent.details();
		}
	}

	public static void main(String[] ignore) throws Exception {
		
		/*
		 * Mark.say("Starting"); // Translator returns a sequence, need to get first element Entity e =
		 * Translator.getTranslator().translate("Paul shaveddddd himself with a razor").getElements().get(0); e =
		 * Translator.getTranslator().translate("Mary was kissed by Paul with passion").getElements().get(0);
		 * Mark.say("Result", e); // Entity e is a relation between Paul, the relation, subject, and a set of roles, the
		 * relation object. // // From another perspective, entity e is a role frame. The subject roles and object roles
		 * are the same. // // Object role not to be confused with object of relation. Entity subjectOfRelation =
		 * e.getSubject(); Entity objectOfRelation = e.getObject(); Entity roleFrameObject = RoleFrames.getObject(e);
		 * Mark.say("Role frame:          ", e); Mark.say("Subject of relation  ", subjectOfRelation); Mark.say(
		 * "Object of relation   ", objectOfRelation); Mark.say("Object of role frame ", roleFrameObject); Mark.say(
		 * "Equal?               ", subjectOfRelation == roleFrameObject); Mark.say("XML", e.toXML()); String sentence =
		 * "It survives for many reasons."; Mark.say("X"); e = Translator.getTranslator().parse(sentence);
		 * Mark.say("Y:", e); e = Translator.getTranslator().translate(sentence); Mark.say("Z:", e);
		 */
		// Mark.say(Translator.getTranslator().translate("John loves mary and mary loves john"));
		//
		// Translator.getTranslator().translate("John loves mary and mary loves john").stream().forEach(e ->
		// Mark.say("Element", e));

		test();
		
//		Mark.say(BasicTranslator.getTranslator().translate("Macbeth murders Duncan"));
//		Entity e = BasicTranslator.getTranslator().translate("Macbeth murders Duncan").get(0);
//		Mark.say("Entity", e);
//
//		Mark.say("English", Generator.getGenerator().generate(e));
//
//		Entity roleframe = RoleFrames.makeRoleFrame(new Entity("ernie"), "give", new Entity("ball"));
//
//		RoleFrames.addRole(roleframe, "to", new Entity("harry"));
//
//		Mark.say("Role frame", roleframe);
//
//		Mark.say("English", Generator.getGenerator().generate(roleframe));

	}
}

package expert;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import consciousness.*;
import constants.*;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import generator.Rules;
import genesis.*;
import gui.TabbedTextViewer;
import matchers.StandardMatcher;
import mentalModels.MentalModel;

import storyProcessor.*;
import subsystems.summarizer.Summarizer;
import utils.*;
import utils.minilisp.LList;
import utils.tools.Predicates;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class QuestionExpert extends AbstractWiredBox {

	List<String> problemSolvingCommands = Arrays.asList(
	        "compose", "put", "clear", "get_rid_of",

	        "make", "replace", "assemble",

	        "demonstrate", "build", "mix"

	);

	MentalModel mentalModel;

	// private static String leftReader = "Asian";
	//
	// private static String rightReader = "Western";

	private static String leftReader = "Dr. Jekyll";

	private static String rightReader = "Mr. Hyde";

	// private MentalModel leftMentalModel;

	// private MentalModel rightMentalModel;

	// private ArrayList<ConceptDescription> leftPlotUnits;
	//
	// private ArrayList<ConceptDescription> rightPlotUnits;

	public static final String CLEAR = "clear";

	// public static final String MENTAL_MODEL_1 = "left story";

	// public static final String MENTAL_MODEL_2 = "right story";

	// public static final String LEFT_PLOT_UNITS = "left plot units";

	// public static final String RIGHT_PLOT_UNITS = "right plot units";

	public static final String WHETHER_PORT = "whether port";

	public static final String INSERT_PORT = "insert element port";



	// public static final String LEFT_CAUSE_PATH = "left causal path";

	// public static final String RIGHT_CAUSE_PATH = "right causal path";

	// New port for new solver

	public static final String TO_PHW = "to phw";

	public static final String TO_DXH = "to dxh";

	public static final String TO_JMN = "to jmn";

	public static final String TO_CA = "to ca";

	public static final String TO_ZTY = "to zty recipe expert";
	
	public static final String TO_ZTY_36 = "to zty stratagem expert";
	
	public static final String TO_ZTY_BTS = "to zty bedtime story learner";

	// New port and keys for Dylan


	public static final String STORY_KEY = "story key";

	public static final String WHAT_IF = "what if question key";

	public static final String COMMENTARY = "introspection";

	public static final String EXPLANATION = "explanation";

	public static final String SPEECH = "speech";

	public static final String TO_SB = null;

	public QuestionExpert() {
		super("Question expert");
		Connections.getPorts(this).addSignalProcessor(this::processInThread);
		// Connections.getPorts(this).addSignalProcessor(this::process);
		// Connections.getPorts(this).addSignalProcessor(this.MENTAL_MODEL_1, this::setLeftStory);
		// Connections.getPorts(this).addSignalProcessor(this.MENTAL_MODEL_2, this::setRightStory);
	}

	/**
	 * Filters
	 */

	/**
	 * Did John love Mary?  Does John love Mary?
	 */
	public static boolean isDid(Entity e) {
		return is(e, Markers.DID_QUESTION);
	}

	/**
	 * Why did John love Mary?
	 */
	public static boolean isWhy(Entity e) {
		return is(e, Markers.WHY_QUESTION);
	}

	/**
	 * What would happen if John loves Mary?
	 */
	public static boolean isWhatIf(Entity e) {
		return is(e, Markers.WHAT_IF_QUESTION);
	}

	/**
	 * When did John love Mary?
	 */

	public static boolean isWhen(Entity e) {
		return is(e, Markers.WHEN_QUESTION);
	}

	/**
	 * Where did John marry Mary?
	 */
	public static boolean isWhere(Entity e) {
		return is(e, Markers.WHERE_QUESTION);
	}

	/**
	 * How did John marry Mary?
	 */
	public static boolean isHow(Entity e) {
		return is(e, Markers.HOW_QUESTION);
	}

	// /**
	// * Following not yet implemented:
	// */
	// public static boolean isWho(Entity e) {
	// return false;
	// }
	// public static boolean isWhat(Entity e) {
	// return false;
	// }
	// public static boolean isWhether(Entity e) {
	// return false;
	// }
	// public static boolean isWhatHappensWhen(Entity e) {
	// return false;
	// }
	// public static boolean isIS(Entity e) {
	// return false;
	// }

	public static boolean is(Entity e, String marker) {
		return e.isA(marker);
	}

	/**
	 * Main method
	 */

	public void process(Object object) {

		boolean debug = false;

		setReaders();

		Mark.say(debug, "Entering question expert with", object);

		if (object == Markers.RESET) {
			// clearStories();
			return;
		}

		if (!(object instanceof Entity)) {
			return;
		}

		Entity entity = (Entity) object;

		if (Radio.qToZTY.isSelected()) {
			Connections.getPorts(this).transmit(TO_ZTY, new BetterSignal(entity));
			return;
		}

		if (Radio.qToZTY36.isSelected()) {
			Connections.getPorts(this).transmit(TO_ZTY_36, new BetterSignal(entity));
			return;
		}
		
		if (Radio.qToZTYBTS.isSelected()) {
			Connections.getPorts(this).transmit(TO_ZTY_BTS, new BetterSignal(entity));
			return;
		}

		if (identifyModulationCommand(entity)) {
			// Mark.say("Trigger is", entity);
			Connections.getPorts(this).transmit(TO_SB, entity);

		}
		else if (identifyDoTestCommand(entity)) {
			Mark.say("Testing...................");
//			GenesisGetters.getMentalModel1().readFile("Replace cellphone bettery.txt");
			GenesisGetters.getMentalModel1().readFile("Find a soul mate_0430_142459.txt");
//			GenesisGetters.getMentalModel1().readFile("Assemble battery in cellphone.txt");
			// GenesisGetters.getMentalModel2().readFile("suritestb.txt");
		}

		else if (entity.hasProperty(Markers.IMPERATIVE) && Radio.qToPHW.isSelected()) {
			// Any verb should work, we think, PHW 18 Apr 2018
			// Nope 6 Mar 2018

//			if (entity.isA(this.problemSolvingCommands)) {
//				Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));
//			}
			if (true) {
				Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));
			}

		}
		else if (entity.functionP(Markers.QUESTION_MARKER) || entity.relationP(Markers.QUESTION_MARKER)) {

			Entity question = entity.getSubject();

			String type = entity.getType();

			Entity leftFocus = getLeftStoryProcessor().reassembleAndDereferenceQuestion(question);

			Mark.say(debug, "Question processor working on", type, "\n", question.asStringWithIndexes(), "\n", leftFocus.asStringWithIndexes());

			Entity rightFocus = null;

			if (getRightStoryProcessor() != null) {
				rightFocus = getRightStoryProcessor().reassembleAndDereferenceQuestion(question);
				Mark.say(debug, "Question processor also working on\n", rightFocus);
			}

			Mark.say(debug, "Question entity", entity);

			if (Radio.qToLegacy.isSelected()) {
				// Fall through for now
			}

			else if (Radio.qToPHW.isSelected()) {

				// transmitIntrospection(TabbedTextViewer.CLEAR);

				Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.RIGHT, "Mental models"));

				Connections.getPorts(this).transmit(COMMENTARY, TabbedTextViewer.CLEAR);

				// Mark.say("B");
				Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));
				return;
			}

			else if (Radio.qToJMN.isSelected()) {
				Mark.say("Sending to Jessica:", entity);
				Connections.getPorts(this).transmit(TO_JMN, new BetterSignal(leftFocus, rightFocus, entity));
				return;
			}

			else if (Radio.qToCA.isSelected()) {
				Mark.say("sending to Caroline: ", entity);
				Connections.getPorts(this).transmit(TO_CA, new BetterSignal(leftFocus, rightFocus, entity, GenesisGetters.getMentalModel1()));
				Connections.getPorts(this).transmit(TO_CA, new BetterSignal(leftFocus, rightFocus, entity, GenesisGetters.getMentalModel2()));
				return;
			}

			else if (Radio.qToDXH.isSelected()) {
				Connections.getPorts(this).transmit(PortNames.SET_PANE, new BetterSignal(GenesisConstants.RIGHT, "Results"));

				Connections.getPorts(this).transmit(TO_DXH, new BetterSignal(WHAT_IF, question, getLeftMentalModel()));
				return;
			}

			if (getLeftStoryProcessor() == null) {
				Mark.say("No story yet!");
				return;
			}

			// Mark.say("Story", getLeftStoryProcessor().getStory());



			if (entity.isAPrimed(Markers.DID_QUESTION) && entity instanceof Function) {
				Mark.say(debug, "Encountered did question", entity);
				Entity subject = entity.getSubject();
				if (false && subject.isA(Markers.CAUSE_MARKER) || subject.isA(Markers.BE_MARKER)) {

					transmitIntrospection(TabbedTextViewer.CLEAR);
					Mark.say(debug, "Transmitting from question expert to solver!!!!!!!!!!!!!!!!!!", entity);
					Mark.say("C");
					Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));

				}
				else {
					// transmitExplanation(TabbedTextViewer.CLEAR);
					answerDidQuestion(leftFocus, rightFocus); // I think this is never called. -jmn 1/11/2017
					// A question such as "What if Amy wants the toy?" can apparently cause this to be called. -dxh 2/24/2017
				}
				// transmitAnswer(result);

				// transmitAnswer(INTROSPECTION, composeExplanation());

			}
			else if (entity.isAPrimed(Markers.WHY_QUESTION) && entity instanceof Function) {
				Mark.say(debug, "Encountered why question:", Generator.getGenerator().generate(entity));

				Mark.say(debug, "Left focus:", leftFocus);

				Connections.getPorts(this).transmit(COMMENTARY, TabbedTextViewer.CLEAR);

				Connections.getPorts(this).transmit(EXPLANATION, TabbedTextViewer.CLEAR);

				if (entity.getSubject().isA(Markers.SAY)) {

					String answer = answerWhySayQuestion(leftFocus);
					if (answer != null) {
						transmitExplanation(answer);
					}
				}
				else {
					if (true) {
						if (Switch.includePersonalityExplanationCheckBox.isSelected()) {
							transmitIntrospection(answerWhyQuestionWithTrait(leftFocus, rightFocus));
						}
						if (Switch.includeCauseExplanationCheckBox.isSelected()) {
							transmitIntrospection(answerWhyQuestion(leftFocus, rightFocus));
						}
						if (Switch.includeConceptExplanationCheckBox.isSelected()) {
							transmitIntrospection(reflectOnWhyQuestion(leftFocus, rightFocus));
						}
					}
					else {
						Mark.say("D");
						Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));
					}
				}
			}
			else if (entity.isAPrimed(Markers.HOW_QUESTION) && entity instanceof Function) {
				Entity subject = entity.getSubject();
				if (subject.isA(Markers.CAUSE_MARKER) || subject.isA(Markers.BE_MARKER)) {
					transmitIntrospection(TabbedTextViewer.CLEAR);
					Mark.say(debug, "Transmitting from question expert to solver!!!!!!!!!!!!!!!!!!");
					Mark.say("E");
					Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));

				}
				else {
				String result = answerHowQuestion(leftFocus, rightFocus);
					transmitIntrospection(result);
				}
			}
			else if (entity.isAPrimed(Markers.WHAT_QUESTION) && entity.functionP()) {
				Mark.say(debug, "Triggering on explain command.");
				Entity subject = entity.getSubject();
				if (subject.isA(Markers.CAUSE_MARKER) || subject.isA(Markers.BE_MARKER)) {
					transmitIntrospection(TabbedTextViewer.CLEAR);
					Mark.say(debug, "Transmitting from question expert to solver!!!!!!!!!!!!!!!!!!");
					Mark.say("F");
					Connections.getPorts(this).transmit(TO_PHW, new BetterSignal(leftReader, entity, getLeftMentalModel()));
				}
				else {
					Mark.say("Cannot handle explain command in current mode!");
				}
			}

			else if (entity.isAPrimed(Markers.WHERE_QUESTION) && entity instanceof Function) {
				Mark.say(debug, "Encountered where question");
				Function d = (Function) entity;
				iDoNotKnow(d);
				Connections.getPorts(this).transmit(Markers.VIEWER, d);
			}

			else if (entity.isAPrimed(Markers.WHEN_QUESTION) && entity instanceof Function) {
				Mark.say(debug, "Encountered when question");
				Function d = (Function) entity;
				iDoNotKnow(d);
				Connections.getPorts(this).transmit(Markers.VIEWER, d);
			}
			else if (entity.isAPrimed(Markers.WHETHER_QUESTION) && entity instanceof Function) {
				Mark.say(debug, "Encountered whether question");
				Function d = (Function) entity;
				Connections.getPorts(this).transmit(WHETHER_PORT, d.getSubject());
				Connections.getPorts(this).transmit(Markers.VIEWER, d);
			}
			else if (entity.isAPrimed(Markers.WHAT_IF_QUESTION) && entity instanceof Function) {
				// Function d = (Function) entity;

				// dylanToHandle(d);
				// Connections.getPorts(this).transmit(Markers.VIEWER, d);

				// Oh boy, this is where Dylan can transmit question to his box!
				// Note that question must be of the form What happens if ...
				// or What would happen if ...
				// and note that START cannot parse What if ...
				// Connections.getPorts(this).transmit(Markers.VIEWER, "hello world");
				// QUESTION_KEY will enable him to determine if signal is story or question
				Mark.say(true, "Encountered what-would-happen-if question\n", question, "\n", leftFocus);

				Connections.getPorts(this).transmit(TO_DXH, new BetterSignal(WHAT_IF, question, getLeftMentalModel()));
			}

			else {
				Mark.err("Question expert encounted unknown question type", entity);
				Connections.getPorts(this).transmit(Markers.NEXT, entity);
			}
		}
		else {
			// Mark.say("Transmitting from question expert", entity);
			Connections.getPorts(this).transmit(Markers.NEXT, entity);
		}

	}

	private String extractModulatorFileName(Entity entity) {
		String fileName = RoleFrames.getRole("for", entity).getType();
		fileName += ".txt";
		return fileName;
	}

	private boolean identifyModulationCommand(Entity entity) {
		if (entity.isA("identify")

		        && entity.getSubject().isA(Markers.YOU)

		        && RoleFrames.getObject(entity).isA("modulation")) {

			String fileName = RoleFrames.getRole("for", entity).getType();

			if (fileName != null) {
				return true;
			}
		}
		return false;
	}

	private boolean identifyDoTestCommand(Entity entity) {
		if (entity.isA("run")
		        && entity.getSubject().isA(Markers.YOU)
		        && RoleFrames.getObject(entity).isA("test")) {
				return true;
		}
		return false;
	}

	private void setReaders() {

		// leftReader = null;
		// rightReader = null;

		Sequence leftStory = this.getLeftMentalModel().getStoryProcessor().getStory();

		Sequence rightStory = this.getRightMentalModel().getStoryProcessor().getStory();

		if (leftStory.size() > 1) {
			leftReader = "the " + capitalize(leftStory.getType()) + " reader ";
		}

		if (rightStory.size() > 1) {
			rightReader = "The " + capitalize(rightStory.getType()) + " reader ";
		}
	}

	private String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private String answerWhySayQuestion(Entity leftFocus) {
		boolean debug = false;
		Entity question = RoleFrames.getObject(leftFocus);
		Mark.say(debug, "Story count:", getLeftMentalModel().getStoryMemory().size());
		List<String> concepts = new ArrayList<>();
		concepts.add("Angry consequence");
		Set<ConceptDescription> conceptDescriptions = getLeftMentalModel().getStoryMemory().getInstantiatedConceptPatterns(concepts);
		List<ConceptDescription> conceptDescriptionList = new ArrayList<>();
		conceptDescriptionList.addAll(conceptDescriptions);
		for (ConceptDescription conceptDescription : conceptDescriptions) {
			Sequence instantiations = conceptDescription.getInstantiations();
			if (instantiations.sequenceP()) {
				for (Entity element : instantiations.getElements()) {
					if (Predicates.isEntail(element)) {
						Entity firstElement = element.getElement(0);
						Mark.say(debug, "Why say question:   ", question);
						Mark.say(debug, "Instantiated element", firstElement);
						Mark.say(debug, "Match:", StandardMatcher.getBasicMatcher().match(question, firstElement));

						Sequence summary = Summarizer.getSummarizer()
						        .composeSummarySequenceUsingConcepts(conceptDescription.getStory(), conceptDescriptionList);
						String englishSummary = Summarizer.getSummarizer().composeEnglishSummary(summary);
						// summary.stream().forEachOrdered(e -> Mark.say("Summary:", e));
						return "I recall that " + englishSummary;
					}
				}
			}
		}
		return null;
	}

	public void clearStories() {
		Mark.err("Should not be called!");
		// leftMentalModel = null;
		// rightMentalModel = null;
		// rightPlotUnits = null;
		// leftPlotUnits = null;
	}

	// public void setLeftPlotUnits(Object object) {
	// rightPlotUnits = null;
	// leftPlotUnits = ((ConceptAnalysis) object).getConceptDescriptions();
	// }
	//
	// public void setRightPlotUnits(Object object) {
	//
	// rightPlotUnits = ((ConceptAnalysis) object).getConceptDescriptions();
	// }

	// public void setLeftStory(Object object) {
	// if (object instanceof MentalModel) {
	// rightMentalModel = null;
	// leftMentalModel = (MentalModel) object;
	// // Mark.say("Left story has", leftMentalModel.getStoryProcessor().getStory().getElements().size(),
	// // "elements");
	// }
	//
	// // No need to pass story to what-would happen if box; it goes along with the question
	// }
	//
	// public void setRightStory(Object object) {
	// if (object instanceof MentalModel) {
	// rightMentalModel = (MentalModel) object;
	// // Mark.say("Right story has", rightMentalModel.getStoryProcessor().getStory().getElements().size(),
	// // "elements");
	// }
	// }

	private void answerBecauseQuestion(String leftReader, Entity leftFocus, String rightReader, Entity rightFocus) {
		answerBecauseQuestion(leftReader, leftFocus, getLeftMentalModel());
		// answerBecauseQuestion(rightReader, rightFocus, getRightMentalModel());
	}

	private void answerDidQuestion(Entity leftFocus, Entity rightFocus) {
		Connections.getPorts(this).transmit(TO_JMN, new BetterSignal(leftFocus, rightFocus));
		//Connections.getPorts(this).transmit(TO_CA, new BetterSignal(leftFocus, rightFocus));
	}

	private String answerWhyQuestion(Entity leftFocus, Entity rightFocus) {
		String title = Html.p(Html.bold("On a common-sense level, "));
		String jekyll = answerWhyQuestion(leftFocus, getLeftStoryElements());
		String hyde = answerWhyQuestion(rightFocus, getRightStoryElements());
		// Mark.say("Answers:", focus.asString(), "\n", jekyll, "\n", hyde);
		return composeWhyAnswer(title, leftFocus, jekyll, rightFocus, hyde);
	}

	private String answerWhyQuestion(Entity focus, Vector<Entity> elements) {
		boolean debug = false;
		if (elements == null) {
			return null;
		}
		List<String> answers = new ArrayList<String>();
		Mark.say(debug, "Looking for cause of", focus.asStringWithIndexes());
		for (Entity element : elements) {
			if (element.isAPrimed(Markers.CAUSE_MARKER) && !element.isAPrimed(Markers.MEANS) && element.relationP()) {
				Relation causeRelation = (Relation) element;
				Entity conclusion = causeRelation.getObject();
				if (conclusion.getType().equals("kill")) {
					Mark.say(debug, "Focus     ", focus.asStringWithIndexes());
					Mark.say(debug, "Conclusion", conclusion.asStringWithIndexes());
				}
				if (StandardMatcher.getIdentityMatcher().match(focus, conclusion) != null) {
					Mark.say(debug, "Equal focus     ", focus.asStringWithIndexes());
					Mark.say(debug, "Equal conclusion", conclusion.asStringWithIndexes());
					answers.add(constructWhyAnswer(causeRelation));
				}
			}
		}
		if (answers != null && !answers.isEmpty()) {
			return Generator.merge(answers);
		}
		return null;
	}

	private String answerWhyQuestionWithTrait(Entity leftFocus, Entity rightFocus) {
		String title = Html.p(Html.bold("From a personality perspective, "));
		String jekyll = answerWhyQuestionWithTrait(leftFocus, getLeftStoryElements());
		String hyde = answerWhyQuestionWithTrait(rightFocus, getRightStoryElements());
		return composeWhyAnswer(title, leftFocus, jekyll, rightFocus, hyde);
	}

	private String answerWhyQuestionWithTrait(Entity focus, Vector<Entity> elements) {
		if (focus == null || elements == null) {
			return null;
		}
		List<String> answers = new ArrayList<String>();
		for (Entity element : elements) {
			if (element.relationP(Markers.CAUSE_MARKER) && MentalModel.hasMentalModelHost(element)) {
				// Mark.say("Found mental model host", element);
				Relation winner = (Relation) element;

				Entity conclusion = winner.getObject();

				if (StandardMatcher.getIdentityMatcher().match(focus, conclusion) != null) {
					String answer = constructWhyAnswerWithTrait(winner);
					if (!answers.contains(answer)) {
						answers.add(answer);
					}
				}
			}
		}
		if (answers != null && !answers.isEmpty()) {
			return Generator.composeAnd(answers);
		}
		return null;
	}

	private String answerHowQuestion(Entity leftFocus, Entity rightFocus) {
		String title = Html.p(Html.bold("With respect to means") + ", ");
		String jekyll = answerHowQuestion(leftFocus, getLeftStoryElements());
		String hyde = answerHowQuestion(rightFocus, getRightStoryElements());

		return composeWhyAnswer(title, leftFocus, jekyll, rightFocus, hyde);
	}

	private String answerHowQuestion(Entity focus, Vector<Entity> elements) {
		if (focus == null || elements == null) {
			return null;
		}
		List<String> answers = new ArrayList<String>();
		for (Entity element : elements) {
			if (element.isAPrimed(Markers.CAUSE_MARKER) && element.isAPrimed(Markers.MEANS) && element.relationP()) {
				Relation meansRelation = (Relation) element;
				Entity conclusion = meansRelation.getObject();
				// need to use isDeepEqual even though we dereferenced earlier because conclusion may have some
				// identifier properties that focus does not have, so it's possible dereferencing did not resolve
				// them to be the same object
				if (focus.isDeepEqual(conclusion)) {
				    answers.add(constructWhyAnswer(meansRelation));
				}
			}
		}
		if (answers != null && !answers.isEmpty()) {
			return Generator.merge(answers);
		}
		return null;
	}

	private void answerBecauseQuestion(String reader, Entity focus, MentalModel m) {

		// Mark.say("Reader", reader);
		// Mark.say("Focus", focus);
		// Mark.say("Mental model", m);

		if (focus == null || m == null) {
			return;
		}

		m.getI().startStory(m);

		noteReader(reader);
		noteExplanation(reader);
		Entity antecedent = focus.getSubject().getElement(0);
		Entity consequent = focus.getObject();

		m.getI().noteThinkAbout(focus);

		boolean antecedentAsserted = false;

		// If in the story, no need to consult memory
		if (appearsInStoryP(antecedent, m)) {
			m.getI().noteStoryElement(antecedent);
			antecedentAsserted = true;
			explainFound(antecedent);
		}
		// Otherwise, consult memory
		else {
			m.getI().noteQuestionAntecedent(antecedent);
			MentalModel believer = isMyBeliefP(antecedent, m);
			if (believer != null) {
				m.getI().noteBelief(believer.getType(), antecedent);
				insertElement(antecedent, m);
				m.getI().noteInsertion(antecedent);
				antecedent = dereference(antecedent, m);
				explainReflection(antecedent);
				antecedentAsserted = true;
			}
		}
		if (antecedentAsserted) {
			m.getI().noteQuestionCauses(antecedent, consequent);
			Vector<Entity> path = noteIfIsConnected(m, antecedent, consequent);
			if (path != null && !path.isEmpty()) {

				Sequence pathSequence = new Sequence(Markers.PATH_MARKER);
				path.stream().forEachOrdered(f -> pathSequence.addElement(f));
				m.getI().noteLeadsTo(antecedent, consequent);
				m.getI().notePath(pathSequence);
				m.getI().noteConclusion(consequent, antecedent);
				// m.getI().noteQuestionAnsweredByLeadsTo(antecedent, consequent);
				m.getI().noteQuestionAnsweredByLeadsToComplete(antecedent, consequent);
				explainConnectionEnablesBelief(consequent);
			}
			else {
				m.getI().noteNoConnection(antecedent, consequent);
				explainNoConnectionPreventsBelief(consequent);
			}
		}
		else {
			m.getI().noteFalsePremise(antecedent, consequent);
			explainDisbelief(antecedent, focus);
		}

		noteIfIsConnected(m, antecedent, consequent);

		linkUp(m.getI().getStoryProcessor().getStory(), m.getI());

		m.getI().noteTheEnd(m);

		GenesisGetters.getMentalModelViewer().setSelectedIndex(0);
	}

	private void linkUp(Sequence story, I i) {
		boolean debug = false;
		boolean debug2 = false;
		Generator g = Generator.getGenerator();
		Mark.say(debug, "The inner story", story.getElements().size());
		// story.getElements().stream().forEachOrdered(e -> Mark.say(debug2, g.generate(e)));
		Vector<Entity> revision = new Vector<>();
		Entity penultimate = null;
		for (Entity e : story.getElements()) {
			Mark.say(debug, "Element >>>>>>>", g.generate(e));
			// Mark.say(debug2, "Looping with\n", penultimate, "to\n", e);
			if (penultimate == null) {
				penultimate = e;
			}
			else {
				Vector<Entity> antecedents = null;
				Entity consequent = null;
				Entity p = penultimate;
				if (Predicates.isCause(e)) {
					antecedents = e.getSubject().getElements();
					antecedents.stream().forEachOrdered(a -> {
						Mark.say(debug, "Antecedent", a);
						if (StandardMatcher.getBasicMatcher().match(a, p) == null) {
							addLink(revision, a);
							Mark.say(debug, "Antecedent added", a);
							Relation inference = Rules.makeCause(a, p);
							addLink(revision, inference);
							i.getStoryProcessor().addInference(inference);
							Mark.say(debug2, "X connecting\n", p, "to\n", a);
						}
						else {
							Mark.say(debug, "Antecedent matched", p);
						}
					});
					addLink(revision, e);
					consequent = e.getObject();
					Mark.say(debug, "Consequent added", consequent);
					addLink(revision, consequent);
					penultimate = consequent;
				}
				else {
					if (StandardMatcher.getBasicMatcher().match(e, p) == null) {
						Relation inference = Rules.makeCause(e, p);
						addLink(revision, inference);
						i.getStoryProcessor().addInference(inference);
					}
					penultimate = e;
					addLink(revision, penultimate);
					Mark.say(debug2, "Y connecting\n", p, "to\n", e);

				}
			}
		}
		story.getElements().clear();
		revision.stream().forEachOrdered(e -> story.getElements().addElement(e));
	}

	private void addLink(Vector<Entity> revision, Entity e) {
		if (!revision.contains(e)) {
			revision.add(e);
		}
		else {
			// Mark.say("Already present:", e);
		}

	}

	private Entity dereference(Entity antecedent, MentalModel mentalModel) {
		return mentalModel.getStoryProcessor().reassembleAndDereference(antecedent);
	}

	private void insertElement(Entity antecedent, MentalModel mentalModel) {
		mentalModel.getStoryProcessor().injectElementWithDereference(antecedent);
	}

	private void explainDisbelief(Entity antecedent, Entity focus) {
		transmitExplanation("<b>on reflection, does not believe</b> " + Generator.getGenerator().generateXPeriod(antecedent)
		        + " and therefore&nbsp;<b>cannot believe</b> that " + Generator.getGenerator().generate(focus));
	}

	private void explainNoConnectionPreventsBelief(Entity consequent) {
		transmitExplanation(" but the reader still does not believe that " + Generator.getGenerator().generate(consequent));
	}

	private void explainConnectionEnablesBelief(Entity consequent) {
		transmitExplanation(" which <b>enables him to believe</b>&nbsp;" + Generator.getGenerator().generate(consequent));
	}

	private void explainFound(Entity antecedent) {
		transmitExplanation(" <b>notes that the story indicates</b> " + Generator.getGenerator().generateXPeriod(antecedent));
	}

	private void explainReflection(Entity antecedent) {
		transmitExplanation(" <b>on reflection, believes</b> " + Generator.getGenerator().generateXPeriod(antecedent));
	}

	private void noteExplanation(String reader) {
		transmitExplanation(Html.p(Html.large(Html.bold(reader) + ", ")));
	}

	private void noteReader(String reader) {
		transmitIntrospection(Html.large("I am " + Html.bold(reader) + "."));
	}

	public Vector<Entity> noteIfIsConnected(MentalModel mentalModel, Entity antecedent, Entity consequent) {
		Sequence story = mentalModel.getStoryProcessor().getStory();
		// Mark.say("Looking for connection between", antecedent.asStringWithIndexes(), "and",
		// consequent.asStringWithIndexes());

		return StoryMethods.findPath(findEntityInStory(antecedent, story), findEntityInStory(consequent, story), story);
	}

	/**
	 * Unbelievable hack
	 */
	private Entity findEntityInStory(Entity cause, Sequence story) {
		Optional<Entity> optional = story.getElements().parallelStream().filter(f -> f == cause).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public boolean appearsInStoryP(Entity element, MentalModel mentalModel) {
		boolean result = mentalModel.getStoryProcessor().getStory().getElements().parallelStream().anyMatch(f -> f == element);
		if (result) {
			return true;
		}
		return false;
	}

	private List<MentalModel> collectMentalModels(String name, MentalModel mentalModel) {
		List<MentalModel> result = new ArrayList<>();
		Sequence story = mentalModel.getStoryProcessor().getStory();
		for (Entity element : new Vector<>(story.getElements())) {
			if (element.relationP(Markers.PERSONALITY_TRAIT) && (element.getSubject().getType().equalsIgnoreCase(name))) {
				String traitName = RoleFrames.getObject(element).getType();
				MentalModel traitModel = mentalModel.getLocalMentalModel(traitName);
				result.add(traitModel);
			}
		}
		return result;
	}

	public MentalModel isMyBeliefP(Entity assertion, MentalModel mentalModel) {
		List<MentalModel> traits = collectMentalModels(Markers.I, mentalModel);
		for (MentalModel traitModel : traits) {
			// Mark.say("Checking trait", traitModel.getType());
			Sequence traitModelStory = traitModel.getStoryProcessor().getExplicitElements();
			for (Entity element : traitModelStory.getElements()) {
				// Mark.say("Checking element", element);
				if (element.relationP(Markers.BELIEVE) && (element.getSubject().getType().equalsIgnoreCase(Markers.I))) {
					Entity belief = RoleFrames.getObject(element);
					// Mark.say("Belief ", belief);
					// Mark.say("Assertion", assertion);
					// Mark.say("Matching\n", belief.toXML(), "\n", assertion.toXML());
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(belief, assertion);
					// Mark.say("Bindings are", bindings);
					if (bindings != null) {
						return traitModel;
					}
				}
			}
		}
		return null;
	}

	private String reflectOnWhyQuestion(Entity leftFocus, Entity rightFocus) {
		String title = Html.p(Html.bold("On a concept level, "));

		String jekyll = reflectOnWhyQuestion(leftFocus, getLeftConceptAnalysis());
		String hyde = reflectOnWhyQuestion(rightFocus, getRightConceptAnalysis());

		return composeWhyAnswer(title, leftFocus, jekyll, rightFocus, hyde);
	}

	private String reflectOnWhyQuestion(Entity focus, ConceptAnalysis conceptAnalysis) {
		if (focus == null || conceptAnalysis == null || conceptAnalysis == null) {
			return null;
		}

		Set<String> result = new HashSet<String>();

		for (ConceptDescription conceptDescription : conceptAnalysis.getConceptDescriptions()) {
			String plotUnitName = Punctuator.conditionName(conceptDescription.getName());
			ArrayList<Entity> candidates = new ArrayList<Entity>();
			// Collect all elements
			for (Entity e : conceptDescription.getStoryElementsInvolved().getElements()) {
				if (e.sequenceP()) {
					candidates.addAll(((Sequence) e).getElements());
				}
				else {
					candidates.add(e);
				}
			}
			// Now, see if focus is in the elements
			for (Entity candidate : candidates) {
				if (StandardMatcher.getIdentityMatcher().match(focus, candidate) != null) {
					result.add(plotUnitName);
				}
			}
		}
		if (result.isEmpty()) {
			return null;
		}
		List<String> list = new ArrayList<>();
		list.addAll(result);
		return constructWhyConcept(focus, list);
	}

	private String constructWhyAnswer(Relation r) {
		String result = "";
		if (r.relationP(Markers.MEANS)) {
			List<String> steps = new ArrayList<>();
			for (Entity step : r.getSubject().getElements()) {
				String english = Generator.getGenerator().generateXPeriod(step);
				Mark.say("English", english);
				// result += Html.bullet(english);
				steps.add(english);

			}
			// result = Html.list(result);
			// result = Generator.merge(steps);
			result = Punctuator.punctuateAnd(steps);
		}
		else {
			result = Generator.getGenerator().generate(r);
		}
		return result;
	}

	private String constructWhyAnswerWithTrait(Relation r) {
		Entity c = r.getObject();
		Entity agent = c.getSubject();
		// Just use first one for now
		Entity model = MentalModel.getMentalModelHosts(r);
		if (model == null) {
			return "";
		}
		Relation characterization = new Relation(Markers.CLASSIFICATION_MARKER, model, agent);
		Relation cause = new Relation(Markers.BECAUSE, c, characterization);
		String result = Generator.getGenerator().generate(cause);
		return result;
	}

	private String constructWhyConcept(Entity focus, List<String> elements) {
		// String result = SimpleGenerator.generate(focus);
		String result = Generator.getGenerator().generateXPeriod(focus);
		int size = elements.size();
		if (size == 0) {
			return null;
		}
		else if (size == 1) {
			result += " is part of act of ";
		}
		else if (size > 1) {
			result += " is part of acts of ";
		}
		result += Punctuator.punctuateAnd(elements);
		return Punctuator.addPeriod(result);
	}

	private void iDoNotKnow(Function d) {
		// String result = "I don't know " + SimpleGenerator.translate(d);
		String result = "I don't know " + Generator.getGenerator().generate(d);
		// Mark.say(result);
		if (getLeftMentalModel() != null) {
			// Mark.say(leftStory.asString());
		}
		if (getRightMentalModel() != null) {
			// Mark.say(rightStory.asString());
		}
		Connections.getPorts(this).transmit(result);
	}

	/**
	 * Use this so that question can appear in text box while processing takes place.
	 */
	public void processInThread(Object object) {
		new ProcessThread(object).start();
	}

	private class ProcessThread extends java.lang.Thread {
		Object object;

		public ProcessThread(Object object) {
			this.object = object;
		}

		public void run() {
			process(object);
		}
	}

	private void transmitExplanation(String result) {
		BetterSignal signal = new BetterSignal(GenesisConstants.LEFT, Markers.EXPLANATION_TAB, result);
		Connections.getPorts(this).transmit(EXPLANATION, signal);
	}

	private void transmitIntrospection(String result) {
		BetterSignal signal = new BetterSignal(GenesisConstants.RIGHT, Markers.INTROSPECTION_TAB, result);
		Connections.getPorts(this).transmit(COMMENTARY, signal);
	}

	private void dylanToHandle(Function d) {
		String result = "Dylan will handle " + Generator.getGenerator().generate(d);
		Connections.getPorts(this).transmit(result);
	}

	private StoryProcessor getLeftStoryProcessor() {
		return getLeftMentalModel() == null ? null : getLeftMentalModel().getStoryProcessor();
	}

	private StoryProcessor getRightStoryProcessor() {
		return getRightMentalModel() == null ? null : getRightMentalModel().getStoryProcessor();
	}

	private Vector<Entity> getLeftStoryElements() {
		return getLeftStoryProcessor() == null ? null : getLeftStoryProcessor().getStory().getElements();
	}

	private Vector<Entity> getRightStoryElements() {
		return getRightStoryProcessor() == null ? null : getRightStoryProcessor().getStory().getElements();
	}

	private ConceptAnalysis getLeftConceptAnalysis() {
		return getLeftStoryProcessor() == null ? null : getLeftStoryProcessor().getConceptAnalysis();
	}

	private ConceptAnalysis getRightConceptAnalysis() {
		return getRightStoryProcessor() == null ? null : getRightStoryProcessor().getConceptAnalysis();
	}

	// Gathering

	private String composeWhyAnswer(String title, Entity leftFocus, String jekyll, Entity rightFocus, String hyde) {
		String p = "";
		if (leftReader != null) {
			if (jekyll != null && !jekyll.isEmpty()) {
				p += thinks(leftReader, jekyll);
			}
			else if (leftFocus != null && jekyll == null) {
				p += noOpinion(leftReader);
			}
		}
		if (false && rightReader != null) {
			if (hyde != null && !hyde.isEmpty()) {
				p += "<p>" + thinks(rightReader, hyde);
			}
			else if (rightFocus != null && hyde == null) {
				p += noOpinion(rightReader);
			}
		}
		p += "  ";
		return title + p;
	}

	private String noOpinion(String reader) {
		return (Html.bold(reader) + " has no opinion.");
	}

	private String thinks(String reader, String answer) {
		return (Html.bold(reader) + " thinks " + answer + " ");
	}

	private MentalModel getLeftMentalModel() {
		return GenesisGetters.getMentalModel1();
	}

	private MentalModel getRightMentalModel() {
		return GenesisGetters.getMentalModel2();
	}

}

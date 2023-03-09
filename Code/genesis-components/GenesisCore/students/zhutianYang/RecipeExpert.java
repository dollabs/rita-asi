package zhutianYang;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.JOptionPane;
import com.ascent.gui.frame.ABasicFrame;
import matchers.StandardMatcher;
import start.Start;
import utils.Html;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;
import connections.*;
import connections.signals.BetterSignal;
import consciousness.JustDoIt;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import generator.Generator;
import genesis.FileSourceReader;
import gui.TextViewer;

/**
 * @author zhutian Yang, March 2019
 * @time updated 16 Apr
 */

public class RecipeExpert extends AbstractWiredBox{

	// ---------------------------------------
	// Program settings
	// ---------------------------------------

	// Demo options
	public static Boolean ROBOT_DEMO = false;
	public static final Boolean DEMO = true;
	public static final Boolean USE_KNOWLEDGE_MAP = true;
	public static final Boolean WRITE_KNOWLEDGE_MAP = true;
	public static String NAME_EXPERT = "Z.Yang";//"Human";//
	public static String NAME_NOVICE = "Genesis";// "Novice";//

	// Debug options
	public static final Boolean SHOW_INNERESE = false;

	// Leaning options
	public static final Boolean ASK_CONFIRMATION = true; // do I always
	public static final Boolean ASK_CONSEQUENCE = false;

	// Recipe options
	public static final Boolean STEP_TO_STATE = true;
	public static final Boolean SPEED_UP_BY_GOAL = false;
	public static final String KNOWLEDGE_FOR_BLOCKS_WORLD = "blocks world";
	public static final String KNOWLEDGE_FOR_ROBOT = "JARVIS";
	public static String KIND_OF_KNOWLEDGE = KNOWLEDGE_FOR_ROBOT;

	// ---------------------------------------
	// Program states
	// ---------------------------------------

	// Program states
	public static Boolean INITIALIZED = false;
	public static Boolean ROBOT_ACTIVATED = false;
	public static Boolean ASK_FURTHER = true;
	public static Boolean HAVE_CONFIRMED = false;

	public static List<String> stack = new ArrayList<>();
	public static final String HOW_TO = "waiting for the goal";
	public static final String LEARN = "learning problem-solving";
	public static final String LEARN_SPECIFIC = "learn specific situations";
	public static final String READY_TO_REMEMBER = "ready to write micro-stories";
	public static final String REMEMBERED = "I remembered steps";
	public static final String LACK_CONDITION_SINGLE = "lack condition after given instruction";

		// learn from instructions
	public static final String LEARN_FROM_INSTRUCTION = "learning from instructions";
	public static final String CONFIRM_STEPS = "confirm if any condition missing";
	public static final String UNKNOWN_STEPS = "don't know the transition rules on this step";
	public static final String UNKNOWN_OBJECTS = "don't see some objects mentioned by humans"; // for robots
	public static final String TO_IDENTIFY_ELEMENTS = "Genesis needs to identify elements";
	public static final String IDENTIFIED_ELEMENTS = "Genesis is ready to show";

		// learn from stories
	public static final String LEARN_FROM_STORY = "learning from stories";
	public static final String LACK_CONDITION_MULTIPLE = "lack condition after reading story";
	public static final String LEARNED_NO_STEP = "can't figure out the steps from the stories";

	// Human states
	public static String humanTone;
	public static Map<String, Object> humanIntention = new HashMap<String, Object>();
	public static final String YES = "positive response";
	public static final String NO = "negative response";
	public static final String SKIP = "skip this response";
	public static final String NORMAL = "normal response";
	public static final String DONT_KNOW = "I don't know response";

	// ---------------------------------------
	// Program knowledge
	// ---------------------------------------

	// Long-term memory for knowledge
	public static final String outputFolder = "corpora/zMemory/";
	public static String pathOfGenesis = "";//"/Users/oxen/git/genesis/";

	public static final String expertKnowledgeFile = "corpora/zMemory/ZZ Expert knowledge.txt";
//	public static final String transitionRulesFile = "students/zhutianYang/zTransitionRulesBlocks.txt";
	public static final String knowledgeMapFile = "students/zhutianYang/zKnowledgeMap.txt";
	public static final String hintStoriesFile = "students/zhutianYang/zHintStories.txt";
	public static final String starterInstructionFile = "students/zhutianYang/zStarterTeachCritical.txt";

//	public static List<String> transitionRules;
	public static List<String> knowledgeMap;
	public static List<String> hintStories;
	public static List<String> starterInstruction;

	// Short-term memory for learning
	public static String humanEnglish = ""; //	public static Entity humanInnerese;
	public static String date = "";
	public static String goal = "";
	public static String firstGoal = ""; // so that lator when asked "always this method" is about global goal
	public static String matchedGoal = "";
	public static String stepsFileString = ""; // micro-story file
	public static String stepsFileName = ""; // micro-story file.txt -- start with "Recipe -"
	public static String testFileName = ""; // Genesis readable file.txt
	public static Translator t = Translator.getTranslator();

		// learn from instructions
	public static List<String> steps = new ArrayList<String>();
	public static Stack<String> questions = new Stack<String>();
	public static List<String> unknownSteps = new ArrayList<>();
	public static List<String> unknownObjects = new ArrayList<>();
	public static List<String> instructions = new ArrayList<String>();
	public static List<String> missingMeanings = new ArrayList<>();  // for unknown steps
	public static List<String> missingReferences = new ArrayList<>();  // for unseen objects

		// learn from stories
	public static List<String> storyNames = new ArrayList<>();
	public static List<String> storyNamesRead = new ArrayList<>();
	public static List<String> missingConditions = new ArrayList<>();
	public static List<List<ZRelation>> stories = new ArrayList<>();

		// during problem solving
	Map<String, Boolean> questionsAsked = new HashMap<String, Boolean>();
	public static Boolean have_translated = false;

	// ---------------------------------------
	// Box connections
	// ---------------------------------------

	// learning
	public static final String FROM_TEXT_ENTRY_BOX = " (which receives user queries in English)";
	public static final String FROM_QUESTION_EXPERT = "from question expert";

	// problem solving
	public static final String FROM_PROBLEM_SOLVER = "from problem solver";
	public static final String FROM_COMMAND_LIST = "from command list";

	// execution in robots
	public static final String FROM_ROBOT_LISTENER = "from robot listener";
	public static final String TO_ROBOT_LISTENER  = "to robot listener";

	// interface
	public static final String TO_CLEAR_TEXT_ENTRY_BOX  = "to clear text entry";
	public static final String TO_COMMENTARY = "recipe expert to commentary";
	public static final String FROM_NOVICE_PAGE = "from novice page";
	public static final String TO_NOVICE_PAGE  = "to print on novice page";

	public static void main(String[] args) {

	}

	public RecipeExpert() {
		super("Recipe expert");
		stack.add(HOW_TO); //wait for goal

		Connections.getPorts(this).addSignalProcessor(FROM_TEXT_ENTRY_BOX, this::getResponse);
		Connections.getPorts(this).addSignalProcessor(FROM_NOVICE_PAGE, this::getResponse);
		Connections.getPorts(this).addSignalProcessor(FROM_ROBOT_LISTENER, this::getResponse);
		Connections.getPorts(this).addSignalProcessor(FROM_PROBLEM_SOLVER, this::checkCondition);
		Connections.getPorts(this).addSignalProcessor(FROM_COMMAND_LIST, this::getCommandList);
	}

	public void getCommandList(Object signal) {
		if (signal instanceof BetterSignal) {
			List<String> commands = ((BetterSignal) signal).get(0, ArrayList.class);
			Mark.say("Received from commands!!!!!!!!!!!!!!!", commands);
			if(goal.equals("")) { // for use in testing
				goal = "make a breakfast cereal";
			}
			Mark.show(goal);
			JustDoIt.zCommand("report, " + Z.haveDone(goal));
			Mark.mit("report, " + Z.haveDone(goal));
		}
	}

	public void checkCondition(Object signal) {
		Mark.say("Entering process check in Local processor !!!");

		Mark.say("before: ",questionsAsked);
		if (signal instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) signal;
			Entity e = bs.get(0, Entity.class);
			String condition = Generator.getGenerator().generate(e.getSubject());
			String message = "Check: " + condition;

			// if the same question has been asked
			if(questionsAsked.containsKey(condition)) {
				if (questionsAsked.get(condition) == true) {
					bs.replace(1, BetterSignal.YES);
					Mark.night("1  external: YES");
				} else {
					bs.replace(1, BetterSignal.NO);
					Mark.night("1  external: NO");
				}
				return;
			}

			// if an opposite question has been asked
			String oldKeyIfAny = oppositeQuestion(questionsAsked,condition);
			if(!oldKeyIfAny.equals("")) {
				if(questionsAsked.get(oldKeyIfAny)==false) {
					bs.replace(1, BetterSignal.YES);
					questionsAsked.put(condition,true);
					Mark.night("2  external: YES");
				} else if(questionsAsked.get(oldKeyIfAny)==true) {
					bs.replace(1, BetterSignal.NO);
					questionsAsked.put(condition,false);
					Mark.night("2  external: NO");
				}
				return;
			}

			// if new question and worth asking
			String[] options = new String[] { "Yes", "No", "I don't know" };
			int response = JOptionPane.showOptionDialog(ABasicFrame
		        .getTheFrame(), message, "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (response == JOptionPane.YES_OPTION) {
				bs.replace(1, BetterSignal.YES);
				questionsAsked.put(condition,true);
				Mark.night("3  external: YES");
			} else if (response == JOptionPane.NO_OPTION) {
				bs.replace(1, BetterSignal.NO);
				questionsAsked.put(condition,false);
				Mark.night("3  external: NO");
			} else {
				bs.replace(1, BetterSignal.NO_OPINION);
				questionsAsked.put(condition,false);
				Mark.night("3  external: NO OPINION");
			}
			Mark.say("after: ",questionsAsked);
			return;
		}
	}

	// react to human questions
	public void getResponse(Object object) {

		// if not for recipe expert, don't response to text in text field
		if(!Radio.qToZTY.isSelected()) return;

		// unpack the signal
		if(object instanceof String) {
			humanEnglish = (String) object;
		} else if (object instanceof BetterSignal) {
			humanEnglish = ((BetterSignal) object).get(0, String.class);
		} else {
			Mark.say("What is this signal?");
			return;
		}
		humanEnglish = humanEnglish.replace("\r", "").replace("\n", "").trim();

		// if coming from robot, (1) activate (2) monitor objects (3) say hi
		if (Radio.realRobot.isSelected()) {

			// the first sentence is used to activate robot listener
			if(!ROBOT_ACTIVATED) {
				ROBOT_ACTIVATED = true;
				Connections.getPorts(this).transmit(TO_ROBOT_LISTENER, new BetterSignal(""));
				return;
			}

			// extract the descriptions of all objects on the table
			List<String> observedObjects = new ArrayList<>();
			if (humanEnglish.contains(ZSay.R)) {
				observedObjects = Arrays.asList(
						humanEnglish.substring(humanEnglish.indexOf(ZSay.R),humanEnglish.length())
						.replace("[[.", "").replace(".]]", "").split("', '"));
				Mark.say("I see: ", observedObjects);
				humanEnglish = humanEnglish.substring(0, humanEnglish.indexOf(ZSay.R));
				Mark.mit(humanEnglish);
			}

			// if robot sent an empty string, reply with hi
			if (humanEnglish.startsWith(ZSay.EMPTY)) {
				Mark.mit("empty string");
				if (object instanceof BetterSignal) {
					BetterSignal bs = (BetterSignal) object;
					bs.replace(0, ZSay.HI);
				}
				return;
			}
		}

		loadLongTermMemory();
		Mark.mit("============== check translatability ==================");
		for(String sentence: Z.story2Sentences(humanEnglish)) {
			sentence = sentence.replace(".", "");
			Mark.mit(sentence);
			humanTone = Z.getTone(sentence);

			if(humanTone!=Z.NORMAL) {
				humanIntention.put(Z.RESPONSE, humanTone);
			} else {
				if(Z.isTranslatable(sentence)) {
					humanIntention.putAll(Z.classify(sentence));

				// remind human of bad English
				} else if (!sentence.equals(ZSay.ACTIVATE)){
					Mark.night("CANNOT TRANSLATE: "+sentence);
					printCommentary(Z.string2Capitalized(humanEnglish),"Expert", object);
					printCommentary(say(ZSay.PARDON),"Novice", object);
					return;
				}
			}
		}
		Mark.mit("============== can translate ==================");


		// print to commentary
		Mark.say(!DEMO, "\n\n\n!!!!!!!!!!!!!human English:", humanEnglish);
		printCommentary("stack before = "+ stack.toString(), "---", object);
		printCommentary("instructions before = "+ instructions.toString(), "!", object);
		printCommentary(Z.string2Capitalized(humanEnglish),"Expert", object);
		String name = Z.stringIsIAm(humanEnglish);
		if (name.length()>0) {
			NAME_EXPERT = name;
			printCommentary(say(ZSay.HELLO,name),"Novice", object);
			return;
		}

		// so that "No" is printed but not in instructions list
		humanEnglish = humanEnglish.replace("no,", "").replace("No, ", "").replace("no.", "").replace("No. ", "");

		humanEnglish = humanEnglish.toLowerCase();

		/* ========================================
		 *   Feature 0 - React to Yes-No questions
		 ========================================== */
		if(todo()==CONFIRM_STEPS) {
			if(humanTone==YES) {
				printCommentary("cleared unknown condition: "+instructions.toString(),"!", object);
				printCommentary(ZSay.SAY_IS_THAT_ALL,"Novice", object);
				stack.remove(CONFIRM_STEPS);
				stack.add(LEARN_SPECIFIC);
				return;

			// if lack condition to quantify methods learned from instuctions //TODO
			} else if(humanTone==NO) {
				stack.remove(CONFIRM_STEPS);
				stack.add(LEARN_SPECIFIC);
//				stack.add(LACK_CONDITION_SINGLE);
//				printCommentary(instructions.toString(),"!", object);
//				printCommentary("When should I apply the method?","Novice", object);
//				printCommentary("stack after = "+ stack.toString(), "===", object);
				return;
			}
		}

		// if lack conditions to differentiate methods learned from stories
		if(todo()==LACK_CONDITION_MULTIPLE && humanTone==YES) {
			printCommentary(instructions.toString(),"!", object);
			printCommentary("When should I apply the first method in "+storyNamesRead.get(0)+"'s story?","Novice", object);
			printCommentary("stack after = "+ stack.toString(), "===", object);
			return;
		}

		// if learning specific situations
		if(todo()==LEARN_SPECIFIC && humanTone==YES) {
			stack.remove(LEARN_SPECIFIC);
			stack.add(READY_TO_REMEMBER);
		}



		/* ========================================
		 *   Step 1 - find the goal
		 ========================================== */
		if(todo()==HOW_TO) {

			if(humanEnglish.contains("show me") && humanEnglish.length()<10) {
				printCommentary(say(ZSay.START_EXECUTION, Z.sentence2Chat(goal)),"Novice", object);
				runProblemSolving(testFileName);
			}

			String isHowTo = Z.question2Goal(humanEnglish);

			if(isHowTo!=null) {
				initializeNewProblem();
				stack.remove(HOW_TO);

				// find goal in problem
				goal = isHowTo;
				if(firstGoal.length()==0) firstGoal = goal.toLowerCase().replace(".", "");
				printCommentary("Find goal: "+ goal + "...","!", object);

				/* ========================================
				 *   Feature 2 - relate to past problems
				 ========================================== */
				if(USE_KNOWLEDGE_MAP) {

					getKnowledgeMap();

					// search in knowledge map and match goal
	    			for(String knowledgeFileName: knowledgeMap) {

	    				String knowledgeGoal = knowledgeFileName.substring(0, knowledgeFileName.indexOf("  @  "));
	    				knowledgeFileName = outputFolder + knowledgeFileName.substring(knowledgeFileName.indexOf("  @  ")+5,knowledgeFileName.length());

	    				// TODO Hack
	    				t.translate("apple salad is a thing");
	    				t.translate("banana salad is a thing");
	    				Entity pastProblem = t.translateToEntity(knowledgeGoal);
	    				Entity currentProblem = t.translateToEntity(goal);
	    				LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(pastProblem, currentProblem);

	    				// found exact same problem
	    				if(pastProblem.isDeepEqual(currentProblem)) {
	    					printCommentary("I know how "+Z.sentence2Chat(goal)+". Let me show you!","Novice", object);
	    					matchedGoal = knowledgeFileName;
		    				runProblemSolving(matchedGoal);

		    			// found similar problem
	    				} else if(bindings!=null) {
	    					Mark.mit(bindings);
	    					matchedGoal = copyKnowledge(knowledgeFileName, bindings, goal);
	    					if (WRITE_KNOWLEDGE_MAP) writeKnowledgeMap(matchedGoal);
	    					testFileName = matchedGoal;
	    					stepsFileName = copyKnowledge(knowledgeFileName.replace("zMemory/", "zMemory/Steps_"), bindings);
	    					printCommentary("I don't know know how to "+goal+", but I guess it is similar to "+knowledgeGoal+".",
	    							"Novice", object);
	        				runProblemSolving(matchedGoal);
	    				}
	    			}
				}

				// if never seen problem before
				if(matchedGoal.length()==0) { // TODO teach me how to
					stack.add(LEARN);
					printCommentary(say(ZSay.TEACH_ME, Z.sentence2Chat(goal.toLowerCase()).replace("I ", "")),
							"Novice", object); //  <font color=\"#DC143C\"> </font>
					// add to instructions
					if(!humanEnglish.endsWith("?"))  humanEnglish = humanEnglish + "?";
					if (!SPEED_UP_BY_GOAL) instructions.add(humanEnglish);

					// begin new learning
					if (date.length()==0) date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
					stepsFileString = outputFolder + "Steps_" + goal +"_" + date;
					stepsFileName = stepsFileString + ".txt";
					testFileName = outputFolder + Z.string2Capitalized(goal) + "_" + date+ ".txt";

				}
				printCommentary("stack after = "+ stack.toString(), "===", object);
				return;
			}

		}

		if(!humanEnglish.endsWith(".")) humanEnglish += ".";

		/* ========================================
		 *   Feature 6-3 - Request definition of verbs
		 ========================================== */
		// if ask for consequence, ask for what's the consequence
		if(todo() == UNKNOWN_STEPS) {

			stack.remove(UNKNOWN_STEPS);

			if(humanEnglish.endsWith(".")) humanEnglish = humanEnglish.replace(".", "");
			missingMeanings.add(humanEnglish);
			printCommentary("missingMeanings: "+missingMeanings.toString(), "===", object);
			printCommentary("stack after = "+ stack.toString(), "===", object);
//			List<String> newStories = new ArrayList<>();

			// if have collected all missing physical meanings
			if(todo() != UNKNOWN_STEPS) {

				// update the unknown steps
//				stack.add(READY_TO_REMEMBER);
				have_translated = true;

				for(int i=0;i<missingMeanings.size();i++) {
					if(!missingMeanings.get(i).contains("skip")) {
						String step = unknownSteps.get(i).trim();
						String state = missingMeanings.get(i).trim();
						try {
							RecipeLearner.updateAssumeSuccess(stepsFileName, step, state);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						Z.writeRuleToFile(Z.strings2Rules(before, after),transitionRulesFile);
					}
				}

				Mark.say("!!!!!!!!!!!! Successful updated story");
				printCommentary(say(ZSay.LEARNED, Z.sentence2Chat(goal).toLowerCase()),"Novice", object);
				printCommentary("stack after = "+ stack.toString(), "===", object);
				initializeNewProblem();

				// unknown objects, used to be asked during learning time
//				if (unknownObjects.isEmpty()) {
//					stack.add(REMEMBERED);
//					Mark.say("!!!!!!!!!!!! Successful updated story");
//					printCommentary(say(ZSay.LEARNED, Z.sentence2Chat(goal)),"Novice", object);
//					printCommentary("stack after = "+ stack.toString(), "===", object);
//
//				} else {
//					stack.add(UNKNOWN_OBJECTS);
//					Mark.say("!!!!!!!!!!!! Asking about unseen objects in human speech");
//					printCommentary(say(ZSay.LEARNED, Z.sentence2Chat(goal)),"Novice", object);
//					printCommentary("stack after = "+ stack.toString(), "===", object);
//				}

			// if there are still conditions missing
			} else {
				printCommentary("OK. "+say(ZSay.ASK_FOR_CONSEQUENCE, unknownSteps.get(missingMeanings.size())),
						"Novice", object);
				return;
			}

		}

		// TODO unknown objects
//		if(todo()==UNKNOWN_OBJECTS) {
//			stack.remove(UNKNOWN_OBJECTS);
//
//			// if have collected all missing physical meanings
//			if(todo() != UNKNOWN_OBJECTS) {
//
//				stack.add(REMEMBERED);
//
//				for(int i=0;i<missingReferences.size();i++) {
//					if(!missingReferences.get(i).contains("skip")) {
//						String object1 = unknownObjects.get(i).trim();
//						String place1 = missingReferences.get(i).trim();
//						try {
//							RecipeLearner.updateUnknownObjects(stepsFileName, object1, place1); // TODO
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//
//			// if there are still unseen objects missing
//			} else {
//				printCommentary("OK. "+ ZSay.whereIs(unknownSteps.get(missingReferences.size())),
//						"Novice", object);
//				return;
//			}
//		}

		// haven't reach critical states
		if(todo()!=READY_TO_REMEMBER && stack.contains(LEARN)) {

			/* ========================================
			 *   Feature 3 - learn from instructions
			 ========================================== */
			List<String> step11 = Z.stringIsSteps(humanEnglish);
			if(step11.size()!=0) {
				if(!stack.contains(LEARN_FROM_INSTRUCTION)) stack.add(LEARN_FROM_INSTRUCTION);

				// add step to instructions
				instructions.add(humanEnglish);
//				if (humanEnglish.contains(" if ") || humanEnglish.contains(" If ")  // TODO
//						|| humanEnglish.contains(" when ") || humanEnglish.contains(" When ")) {
				if (humanIntention.containsKey(Z.CONDITION)) {
					HAVE_CONFIRMED = true;
				}
				if (todo()==LEARN_SPECIFIC) {
					printCommentary(ZSay.SAY_IS_THAT_ALL,"Novice", object);
					return;
				} else {
					stack.add(READY_TO_REMEMBER);
				}

//				if(ASK_CONFIRMATION) {
//					// ask if condition applies
//					printCommentary(say(ZSay.ASK_FOR_CONDITIONS),"Novice", object);
//					stack.add(CONFIRM_STEPS);
//					printCommentary("stack after = "+ stack.toString(), "===", object);
//					return;
//				} else {
//					stack.add(READY_TO_REMEMBER);
//				}

			}

			/* ========================================
			 *   Feature 4-1 - learn conditions for multiple ways to solve
			 ========================================== */
			List<String> condition11 = Z.stringIsCondition(humanEnglish);
			if(condition11.size()!=0) {

				String condition = condition11.get(0);
				String step = "";

				/* ========================================
				 *   Feature 4-2 - Learn from instructions, ask for conditions if not given
				 ========================================== */
				if(todo()==LACK_CONDITION_SINGLE) {

					stack.add(READY_TO_REMEMBER);
					humanEnglish = Z.sentenceToCondition(humanEnglish).get(0);
					step = instructions.get(1);
					instructions.remove(step);
//					instructions.add("if " + humanEnglish + ", "+step);
					instructions.add("if " + humanEnglish + ", "+step);

					printCommentary("cleared the missing condition: "+instructions.toString(),"!", object);
					printCommentary("stack after = "+ stack.toString(), "===", object);

				/* ========================================
				 *   Feature 4-3 - Learn from stories, ask for conditions for multiple stories
				 ========================================== */
				} else if (todo() == LACK_CONDITION_MULTIPLE) {

					stack.remove(LACK_CONDITION_MULTIPLE);
					humanEnglish = Z.sentenceToCondition(humanEnglish).get(0);
					missingConditions.add(humanEnglish);

					// if have collected all missing conditions
					if(todo() != LACK_CONDITION_MULTIPLE) {
						stack.add(READY_TO_REMEMBER);

						step = instructions.get(0);
						instructions.remove(step);
						instructions.add(step);

						for(String condition1: missingConditions) {
							step = instructions.get(0);
							instructions.remove(step);
							instructions.add("if " + condition1 + ", "+step);
						}
						printCommentary("cleared the last condition: "+instructions.toString(),"!", object);
						printCommentary("stack after = "+ stack.toString(), "===", object);

					// if there are still missing conditions
					} else {
						printCommentary("OK. When should I apply the " + Z.number2Oridinal(missingConditions.size())+
								" method in "+storyNamesRead.get(missingConditions.size())+"'s story?","Novice", object);
						printCommentary("cleared "+Z.number2Oridinal(missingConditions.size()-1)+" condition"+instructions.toString(),
								"!", object);
						printCommentary("stack after = "+ stack.toString(), "===", object);
						return;
					}
				} else {

					if(!stack.contains(LEARN_FROM_INSTRUCTION)) stack.add(LEARN_FROM_INSTRUCTION);

					// add condition + step to instructions
					instructions.add(humanEnglish);

					printCommentary(ZSay.SAY_IS_THAT_ALL,"Novice", object);
					stack.add(LEARN_SPECIFIC);
					printCommentary("stack after = "+ stack.toString(), "===", object);
					return;
				}
			}

			/* ========================================
			 *   Feature 5 - learn from stories
			 ========================================== */
			String story = Z.stringIsStory(humanEnglish,storyNames);
			if(story!="") {

				ASK_FURTHER = false;

				if(!stack.contains(LEARN_FROM_STORY)) stack.add(LEARN_FROM_STORY);
				storyNamesRead.add(story.split(" ")[0]);

				// print out the story
				List<String> sentences = Z.readStoryFromFile(story).get(Z.SENTENCES);
				String all = "";
				for(String sentence: sentences) all = all + " "+ sentence.trim();
				printCommentary(all,"Read", object);
				String toPrint = "";
				String toPrintRelations = "";

				// learn the relations in each story
				if(stories.size()>0){
					ZRelation.reset();
				}

				List<ZRelation> oneStory = ZRelation.story2ZRelations(story,""); // changed on 3 July
				stories.add(oneStory);

				// every time reading a story, the steps are redefined
				List<String> learnedSteps = new ArrayList<>();
				List<String> learnedRelations = new ArrayList<>();
				instructions = new ArrayList<>();
				instructions.add("do you know how to "+goal+"?");

				learnedRelations = ZRelation.listPrintRelations(oneStory);
				int count = 0;
				toPrintRelations = "";
				for(String to:learnedRelations) {
					toPrintRelations = toPrintRelations + "\n  " + (++count) + "    " + to;
				}

				printCommentary(toPrintRelations,"Relations", object);

				// if the first story, print all relations
				if(stories.size()==1) {

					stack.add(LACK_CONDITION_SINGLE);

					Mark.show("   steps:");
					// get steps from one story
					learnedSteps = ZRelation.listPrintStepsPresent(oneStory);
//					learnedRelations = ZRelation.listPrintRelations(oneStory);
					instructions.add("you need to "+listToStory(learnedSteps)+Z.FINISHED); // so we can generate two separate recipes

					// prepare output string
					toPrint = say(ZSay.I_LEARN_STEPS_HOW, story);
					count = 0;
					for(String to:learnedSteps) {
						steps.add(to);
						toPrint = toPrint +	"\n    <font color=\"#FFA500\">" + (++count) + "    " + to + "</font>"; // #FFA500
					}



				// if more instructions, print the relevant steps in each one
				} else {

					steps = new ArrayList<>();
					if (stack.contains(LACK_CONDITION_SINGLE)) {
						stack.remove(LACK_CONDITION_SINGLE);
						stack.add(LACK_CONDITION_MULTIPLE);
					}
					stack.add(LACK_CONDITION_MULTIPLE);

					toPrint = toPrint + "I learned "+stories.size()+" ways to " + goal + ":";
					for(int j=0;j<stories.size();j++) {

						// get steps for each story
						List<List<ZRelation>> manyStories = ZRelation.listItemToEnd(stories, j);
						learnedSteps = ZRelation.listPrintStepsPresent(ZRelation.getCommonSubgoals(manyStories));
						Mark.show(learnedSteps.size());
						if(!learnedSteps.isEmpty()) {
							instructions.add("you need to "+listToStory(learnedSteps)+Z.FINISHED); // so we can generate two separate recipes

							// prepare output string
							toPrint = toPrint + "\nThe "+ Z.number2Oridinal(j) +
									" method from "+ storyNamesRead.get(j)+"'s story:";
							count = 0;
							for(String to:learnedSteps) {
								steps.add(to);
								toPrint = toPrint +	"\n    <font color=\"#FFA500\">" + (++count) + "    " + to + "</font>";
							}

						} else {
							stack.add(LEARNED_NO_STEP);
							printCommentary("Damn... I could not figure out how to do it.","Novice", object);
						}

					}

				}

				toPrint = toPrint + "\n Is it correct?";
				printCommentary(toPrint,"Novice", object);
				printCommentary("instructions: "+instructions.toString(),"!", object);
				printCommentary("stack after = "+ stack.toString(), "===", object);
				return;
			}
		}

		/* ========================================
		 *   Feature 6-9 - Remember steps
		 ========================================== */
		if(todo()==READY_TO_REMEMBER) {// || humanEnglish.startsWith("yes")
			printCommentary("Ready to remember: "+instructions.toString(),"!", object);

			String newGoal;
			stack.remove(READY_TO_REMEMBER);

			if(!humanEnglish.contains(ZSay.JUST_DO_IT)) {
				List<List<String>> toReturns = RecipeLearner.instructionsToMicroStories(instructions, date, KIND_OF_KNOWLEDGE);
				unknownSteps = toReturns.get(0);
				unknownObjects = toReturns.get(1);
				newGoal = toAsk(unknownSteps);
			} else {
				instructions = new ArrayList<>();
				newGoal = toAsk();
			}

			if(newGoal!=null && !have_translated && ASK_FURTHER) {

				// ask "what is the meaning of ... sub-steps"
				if(ASK_CONSEQUENCE) {
					int count = unknownSteps.size();
					for(int i=0;i<count;i++) stack.add(UNKNOWN_STEPS);
					printCommentary(say(ZSay.ASK_FOR_CONSEQUENCE, unknownSteps.get(0)),"Novice", object);
					printCommentary("stack after = "+ stack.toString(), "===", object);
					return;

				// ask "how do I .... sub-steps"
				} else {
//					stack.add(LEARN);
//					String newGoal = say(ZSay.HOW_DO_I, unknownSteps.get(0).toLowerCase());
//					unknownSteps.remove(unknownSteps.get(0));

					printCommentary(newGoal, "Novice", object); //  <font color=\"#DC143C\"> </font>
					instructions.add(newGoal);
					return;
				}
			}

			if(!HAVE_CONFIRMED && ASK_CONFIRMATION) {
				HAVE_CONFIRMED = true;
				RecipeLearner.goal = RecipeLearner.firstGoal;
				printCommentary(ZSay.doIAlways(Z.sentence2Chat(goal.toLowerCase()).replace("I ", "")),"Novice", object);
				stack.add(CONFIRM_STEPS);
				printCommentary("stack after = "+ stack.toString(), "===", object);
				return;
			} else {
//				stack.add(READY_TO_REMEMBER);

				// ready to remember
				stack.add(REMEMBERED);

				Mark.say("!!!!!!!!!!!! Successful created blocks world story");
				printCommentary(say(ZSay.LEARNED, Z.sentence2Chat(goal).toLowerCase()),"Novice", object);
				printCommentary("stack after = "+ stack.toString(), "===", object);
				initializeNewProblem();
				return;
			}



//			if(STEP_TO_STATE && !have_translated) {
//				// check steps whose physical meanings are unknown
//				unknownSteps = Z.findUnknownSteps(steps, transitionRules);
//			}
//
//			/* ========================================
//			 *   Feature 6-4 - ask for physical meaning of unknown steps
//			 ========================================== */
//			if(!unknownSteps.isEmpty() && !have_translated) {
//
//				int count = unknownSteps.size();
//				for(int i=0;i<count;i++) stack.add(UNKNOWN_STEPS);
//				printCommentary("What is the physical meaning of "+unknownSteps.get(0)+"?","Novice", object);
//				printCommentary("stack after = "+ stack.toString(), "===", object);
//
//			// ready to remember
//			} else {
//				stack.remove(READY_TO_REMEMBER);
//				stack.add(REMEMBERED);
//
//				RecipeLearner.instructionsToMicroStories(instructions, date, KIND_OF_KNOWLEDGE);
//				Mark.say("!!!!!!!!!!!! Successful created blocks world story");
//				printCommentary("I learned how to "+ goal+".","Novice", object);
//				printCommentary("stack after = "+ stack.toString(), "===", object);
//				return;
//			}
		}

		// demo after learned problem-solving
		if(todo() == REMEMBERED) {

			if(humanEnglish.startsWith("show me")) {
				// TODO TO standardize
				if (KIND_OF_KNOWLEDGE==KNOWLEDGE_FOR_BLOCKS_WORLD) {
					stack.add(TO_IDENTIFY_ELEMENTS);
					printCommentary("Could you help me identify the objects in the blocks world? (answer \"skip\" to skip simulation)",
							"Novice", object);
				} else {

					printCommentary(say(ZSay.START_EXECUTION, Z.sentence2Chat(goal)),"Novice", object);
//					runProblemSolving(testFileName);// TODO
//					internalizeExpertKnowledge(stepsFileName);
					return;
				}
			}
		}

		if(todo() == TO_IDENTIFY_ELEMENTS) {
			if(humanTone==SKIP) { // TODO
				printCommentary(say(ZSay.START_EXECUTION, goal),"Novice", object);
				runProblemSolving(testFileName);
//				internalizeExpertKnowledge(stepsFileName);
				return;
			} else {
				// ask for mapping of physical elements to blocks
				List<String> blocks = Arrays.asList("b1", "b2", "b3",
						"b4", "b5", "b6", "b7", "b8", "b9", "b10");
				for(String block: blocks) {
					if(humanEnglish.contains(block)) {
						stack.add(IDENTIFIED_ELEMENTS);
						stack.remove(TO_IDENTIFY_ELEMENTS);

						Mark.say(block+" in "+humanEnglish);
						addIdentifyElements(expertKnowledgeFile, humanEnglish);
						printCommentary(say(ZSay.START_EXECUTION, goal),"Novice", object);
						runProblemSolving(testFileName);
//						internalizeExpertKnowledge(stepsFileName);
						printCommentary("stack after = "+ stack.toString(), "===", object);
						break;
					}
				}
			}
		}

		printCommentary("stack after = "+ stack.toString(),"!", object);
	}

	public static void internalizeExpertKnowledge(String stepsFileName){
		try {

			List<String> allLines = new ArrayList<>(Files.readAllLines(Paths.get(expertKnowledgeFile), StandardCharsets.UTF_8));
			FileWriter writer = new FileWriter(stepsFileName, true);
			for (String line: allLines)
				writer.write("\n"+line);

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String toAsk(List<String> steps) {

		if(steps.size()<=3) {
			questions.addAll(Z.reverse(steps));
		}
		return toAsk();


	}

	public static String toAsk() {

		if(questions.size()>0) {
			String toAsk = Z.howQuestion(questions.pop(),1);
			Mark.night(toAsk);
			return toAsk;
		} else {
			return null;
		}

	}


	/* ========================================
	 *   Create knowledge
	 ========================================== */
	public static void writeTestFile(String testerFileName,String test,String goal) {

		try {
			FileWriter writer = new FileWriter(testerFileName, false);
			writer.write("Start experiment.\r\n\r\n");
			writer.write("Set Deploy novice first switch to false.\r\n\r\n");
			writer.write("Set self aware button to true.\r\n\r\n");

			if(ROBOT_DEMO) {
				writer.write("Set Real robot button to true.\r\n\r\n");
			} else {
				writer.write("Set Just plan button to true.\r\n\r\n");
			}

			// blocks world applications
			if(NAME_EXPERT.equals(Z.Yang)) {
				writer.write("Set left panel to Commentary.\r\n\r\n");
//				writer.write("Set right panel to Blocks.\r\n\r\n");
				writer.write("Read knowledge in ZZ Basic blocks knowledge.\r\n\r\n");

			// talk talk just
			} else {
				writer.write("Set right panel to Commentary.\r\n\r\n");
				writer.write("Read knowledge in ZZ Basic PS knowledge.\r\n\r\n");
			}

			writer.write("Read knowledge in "+test+".\r\n\r\n");
			writer.write("Insert into text box: "+goal+".");
			writer.close();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	public static void writeTestFileBlocksWorld(String testerFileName,String test,String goal) {

		try {
			FileWriter writer = new FileWriter(testerFileName, false);

			writer.write("Start experiment.\r\n\r\n");
			writer.write("Set all switches to defaults.\r\n\r\n");
			writer.write("Set Deploy novice first switch to true.\r\n\r\n");
			writer.write("Set Use fancy simulator switch to false.\r\n\r\n");
			writer.write("Set Self aware button to true.\r\n\r\n");
			writer.write("Set Use features when matching switch to false.\r\n\r\n");
			writer.write("Set Split names with underscores switch to false. \r\n\r\n");
			writer.write("Set left panel to commentary.\r\n\r\n");
			writer.write("Set right panel to blocks.\r\n\r\n");
			writer.write("Set bottom panel to mental models.\r\n\r\n");
			writer.write("Read knowledge in ZZ Expert knowledge.\r\n\r\n");
			writer.write("Read helper knowledge in ZZ Basic blocks knowledge.\r\n\r\n");
			writer.write("Read helper knowledge in ZZ Basic PS knowledge.\r\n\r\n");
			writer.write("Read helper knowledge in "+test+".\r\n\r\n");
			writer.write("Insert into text box: "+Z.string2Capitalized(goal)+".");
			writer.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	// map the learned knowledge to a problem statement
	public static void writeKnowledgeMap(String fileName) {
		try {
			FileWriter writer = new FileWriter(knowledgeMapFile, true);
			Mark.night(fileName);
			Mark.night(fileName.replace(outputFolder, ""));
			writer.write(ZSay.KNOWLEDGE_MAP.replace("goal",goal).replace("file", fileName.replace(outputFolder, ""))+"\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/* ========================================
	 *   Modify knowledge
	 ========================================== */
	// after human teaches Genesis to identify elements, add the new step to knowledge file
	public static void addIdentifyElements(String fileName, String elements) {

		if(elements.endsWith(".")) elements = elements.replace(".", "");

		// to extract the goal from the name of the file
		Mark.say("... to add Identify Element in file: ",fileName);
		String goalHere = goal;
		goalHere = Z.string2Capitalized(goalHere);
		Mark.say(goalHere);

		// read all lines for later add and append
		List<String> allLines = new ArrayList<>();

		try {
			String toFind = "If the intention is \""+goalHere+"\".";
			allLines = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
			int times = Z.countListItemStartsWith(allLines,toFind);
			for(int i=0;i<times;i++) {
				int count = 0;
				int skip = i;
				for(String line: allLines) {
					if(line.contains(toFind)) {
						skip--;
						if(skip==-1) {
							Mark.say("!!!!!!!!If the intention is \""+goalHere+"\".");
							if(allLines.get(count+1).startsWith("Step")) {
								allLines.add(count+1,"Step: Identify elements.");
								break;
							} else if(allLines.get(count+2).startsWith("Step")) {
								allLines.add(count+2,"Step: Identify elements.");
								break;
							} else if(allLines.get(count+3).startsWith("Step")) {
								allLines.add(count+3,"Step: Identify elements.");
								break;
							} else if(allLines.get(count+4).startsWith("Step")) {
								allLines.add(count+4,"Step: Identify elements.");
								break;
							}

						}
					}
					count++;
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// for cleaning the language to B1, B2 ...
		for(int i=1;i<10;i++) {
			elements = elements.replace("b"+i,"B"+i);
		}

		// for getting components of an AND sentence
		String[] identities;
		if(elements.contains(", and")) {
			identities = elements.split(",");
		} else if(elements.contains("and")) {
			identities = elements.split("and");
		} else {
			identities = new String[1];
			identities[0] = elements;
		}

		//
		try {
			FileWriter writer = new FileWriter(fileName, false);
			for(String line: allLines) {
				writer.write(line+"\r\n");
			}
			writer.write("\r\n"+"\r\n"+"If the intention is \"Identify elements\"."+"\r\n");
			for(String identity: identities) {

				// to prune the elements to only verb phrase
				identity = identity.trim().replace(".", "");
				identity = identity.replace("\n", "").replace("\r", "");
				if(identity.startsWith("and")) {
					identity = identity.replace("and ", "");
				}

				// use basic method "is known as"
				if(!identity.contains("is known as")) {
					identity = identity.replace("is ", "is known as ");
				}

				// write processed identity
				writer.write("Step: "+identity+"."+"\r\n");
			}
			writer.write("The end."+"\r\n");
			writer.close();

			Z.printTXTFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// transfer knowledge by replacing entity names
	public static String copyKnowledge(String matched, String before, String after) {
		String newName = matched.replace(before, after);
		try {
			List<String> newKnowledge = new ArrayList<>(Files.readAllLines(Paths.get(matched), StandardCharsets.UTF_8));
			FileWriter writer = new FileWriter(testFileName, false);

			for (String knowledge:newKnowledge) {
				if (knowledge.contains(before)) {
					writer.write(knowledge.replace(before, after));
				}else {
					writer.write(knowledge);
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return newName;
	}

	public static String copyKnowledge(String matched, LList<PairOfEntities> bindings) {
		return copyKnowledge(matched, bindings, "");
	}

	// transfer knowledge by replacing by bindings
	public static String copyKnowledge(String matched, LList<PairOfEntities> bindings, String goal) {
		String newKnowledgeFileName = matched;
		try {
			List<String> oldKnowledge = new ArrayList<>(Files.readAllLines(Paths.get(matched), StandardCharsets.UTF_8));
			Mark.say("Read!");

			// for every pair
			int count = 0;
			String[] parts = bindings.toString().split(" <");
			for (String part : parts) {
				String before = part.substring(0,part.indexOf("-"));
				if(count ==0) {
					before = before.replace("(<", "");
				}
//				Mark.say(before);
				part = part.substring(part.indexOf(",")+2,part.length());
				String after = part.substring(0, part.indexOf("-"));
//				Mark.say(after);

				List<String> newKnowledge = new ArrayList<>();
				newKnowledgeFileName = newKnowledgeFileName.replace(before+"_", after+"_");
				for (String line:oldKnowledge) {
					newKnowledge.add(line.replace(before, after));
				}
				oldKnowledge = newKnowledge;
				count++;
			}

			Mark.say(newKnowledgeFileName);

			try {

				FileWriter writer = new FileWriter(newKnowledgeFileName, false);

				for (String line:oldKnowledge) {
					if(line.contains(Markers.INSERT_TEXT) && goal.length()>0) {
						line = Markers.INSERT_TEXT + ": "+ Z.string2Capitalized(goal) +".";
					}
					writer.write(line);
					writer.write("\r\n");
				}
				writer.close();

			} catch (IOException e) {
				Mark.say("Cannot create!");
				e.printStackTrace();
			}

		} catch (IOException e) {
			Mark.say("Miss!");
			e.printStackTrace();
		}

		return newKnowledgeFileName;
	}


	/* ========================================
	 *   For Human-computer interaction
	 ========================================== */
	// return the top on stack of metal problems
	public String todo() {
		if(stack.size()>=1) {
			return stack.get(stack.size()-1);
		} else {
			return "null";
		}
	}

	// run the problem solving using 2D blocks world
	public static void runProblemSolving(String storyName) {

		FileSourceReader storyReader = new FileSourceReader();

		File file;
		try {
			file = new File("").getCanonicalFile();
			pathOfGenesis = file.getParent() + "/GenesisCore/";
			System.out.println("Parent directory : " + file.getParent());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Mark.say("I am running: "+pathOfGenesis+storyName);
		file = new File(pathOfGenesis+storyName);
		if(storyName.startsWith(pathOfGenesis)) {
			file = new File(storyName);
		}
		storyReader.fileChooserDirectory = file.getParentFile();
		String fileString = storyName.substring(storyName.lastIndexOf("/")+1, storyName.length());
		Mark.say(fileString);
		storyReader.getFileSourceReader().readTheWholeStoryLocally(fileString);
	}

	public void printCommentary(Object messageObject, String name, Object signal) {

		// HTML color names
		//      https://www.w3schools.com/colors/colors_names.asp

		String message = (String) messageObject;
		String original = message;
		if (Radio.qToZTY.isSelected()) {
			String tabname = "Conversation";

			// signal beginning of conversation
			if(name == "---") {
				message = Html.coloredText("#C0C0C0","\n"+ message); // silver
				message = Html.center(message);
				message = Html.footnotesize(message);
				message = "<hr>" + message;
				if (DEMO) message = "";

			// signal ending of conversation
			} else if(name == "===") {
				message = Html.coloredText("#C0C0C0","\n"+ message); // silver
				message = Html.center(message);
				message = Html.footnotesize(message);
				message = message + "<hr>";
				if (DEMO) message = "";

			// for debugging
			} else if(name == "!") {
				message = Html.coloredText("#FFA500","\n"+ message); // orange
				message = Html.footnotesize(message);
				if (DEMO) message = "";

			// for reading stories
			} else if(name=="Read") {
				message = Html.bold("\n\n "+name+": ") + message;
				message = Html.coloredText("#4169E1",message);  // royal blue
				message = Html.small(message);

			// for printing relations
			} else if(name=="Relations") {
				message = Html.bold("\n\n State Changes: ") + message;
				message = Html.coloredText("#C0C0C0","\n"+ message); // silver
				message = Html.footnotesize(message);

			} else if(name=="Expert") {
				message = Html.bold("\n\n "+NAME_EXPERT+": ") + message;
				message = Html.normal(message);

			} else if(name=="Novice") {
				message = Html.bold("\n "+NAME_NOVICE+": ") + message;
				message = Html.normal(message);

			// for Innerese and Geneses
			} else if(name=="Innerese" || name =="Generated English") {
				message = Html.bold("\n "+name+": ") + message;
				message = Html.coloredText("#4169E1",message); // royal blue
				message = Html.tiny(message);
				if (DEMO || !SHOW_INNERESE) message = "";

			} else if(name=="error") {
				message = Html.bold("\n "+name+": ") + message;
				message = Html.center(message);
				message = Html.coloredText("#DC143C",message); // not too red
				message = Html.tiny(message);
				if (DEMO) message = "";
			} else {
				message = "who is speaking?" + message;
				if (DEMO) message = "";
			}

			BetterSignal bs = new BetterSignal(tabname, message);
			Connections.getPorts(this).transmit(TO_COMMENTARY, bs);
			bs = new BetterSignal(tabname, message, humanIntention);
			Connections.getPorts(this).transmit(TO_NOVICE_PAGE, bs);
			clearTextEntryBox();

			if (Radio.realRobot.isSelected() && message.contains(NAME_NOVICE)) {
//				BetterSignal bs2 = new BetterSignal(message);
//				Connections.getPorts(this).transmit(TO_ROBOT_LISTENER, bs2);

				if (signal instanceof BetterSignal) {
					BetterSignal bs2 = (BetterSignal) signal;
					String toReplace = "";
					if (humanEnglish.length()==0) {
						toReplace = ZSay.HI;
					} else {
						toReplace = original;
					}
					Mark.say("Recipe Expert is going to reply: ", toReplace);
					bs2.replace(0, toReplace);
				}

			}

		}

	}

	private void clearCommentary() {
		String tabname = "Conversation tab";
		BetterSignal bs = new BetterSignal(tabname, TextViewer.CLEAR);
		Connections.getPorts(this).transmit(TO_COMMENTARY, bs);
	}

	private void clearTextEntryBox() {
		Connections.getPorts(this).transmit(TO_CLEAR_TEXT_ENTRY_BOX, "");
	}


	/* ========================================
	 *   Class tools
	 ========================================== */

	public static String say(String wrapper, String content) {
		return ZSay.say(wrapper, content);
	}

	public static String say(String wrapper) {
		return wrapper;
	}


	public static String oppositeQuestion(Map<String, Boolean> questionsAsked, String condition) {
		for ( String key : questionsAsked.keySet() ) {
		   if(Z.isNegation(key,condition)) {
			   return key;
		   }
		}
		return "";
	}

	// make a list of events into a single sentence of story
	public static String listToStory(List<String> methods) {
		String story = "";
		for(String one:methods) {
			if(one.endsWith(".")) {
				one = one.substring(0,one.length()-1);
			}
			one = Z.verbs2Present(one);
			story = story + " " + one + ",";
		}
		story = Z.stringReplaceLast(story, ", ", ", and ");
		story = Z.stringReplaceLast(story, ",", ".");
		Mark.say("story:", story);
		return story;
	}

	// to read language hints and knowledge
	public static void loadLongTermMemory() {

		storyNames = Arrays.asList(
				"Alice replaces cellphone battery",
				"Bob replaces cellphone battery",
				"Carl replaces soccer player",
				"Yang says goodbye",
				"Jin says goodbye"
				);

		if (!INITIALIZED) {
			try {

				hintStories = new ArrayList<>(Files.readAllLines(Paths.get(hintStoriesFile), StandardCharsets.UTF_8));
				hintStories.removeAll(Arrays.asList("", null));
				hintStories = Z.listToLower(hintStories);

				getKnowledgeMap();

//				transitionRules = new ArrayList<>(Files.readAllLines(Paths.get(transitionRulesFile), StandardCharsets.UTF_8));
//				transitionRules.removeAll(Arrays.asList("", null));
//				transitionRules = Z.listToLower(transitionRules);
//				transitionRules = Z.listRemoveComment(transitionRules);
			} catch (IOException e) {
				e.printStackTrace();
			}
			INITIALIZED = true;
		}

	}

	// initialize all variables for a new learning experience
	public static void initializeNewProblem() {
		have_translated = false;
		stack = new Stack<>();
		stack.add(HOW_TO);
		date = "";
//		goal = "";
		matchedGoal = "";

		// learn from instructions
		steps = new ArrayList<String>();
		unknownSteps = new ArrayList<>();
		instructions = new ArrayList<String>();
		missingMeanings = new ArrayList<>();

		// learn from stories
		storyNames = Arrays.asList(
				"Alice replaces cellphone battery",
				"Bob replaces cellphone battery",
				"Carl replaces soccer player",
				"Yang says goodbye",
				"Jin says goodbye"
				);
		storyNamesRead = new ArrayList<>();
		stories = new ArrayList<>();
		missingConditions = new ArrayList<>();

		getKnowledgeMap();

	}

	public static void getKnowledgeMap() {
		try {
			knowledgeMap = new ArrayList<>(Files.readAllLines(Paths.get(knowledgeMapFile), StandardCharsets.UTF_8));
			knowledgeMap.removeAll(Arrays.asList("", null));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// for box and wire
	public void receiveStory(Object object) {
		String string = (String) object;
		Mark.say("hahahahaha++++++++++++++ "+ string);
		if (string.contains("with signature ")) {
			Mark.say("hahahahaha++++++++++++++ "+ string.substring(string.indexOf("with signature ")+14),string.lastIndexOf("\""));
		}
		printCommentary(string,"!", object);

	}

}

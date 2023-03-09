/**
 * This is the toolbox for Z's programs, featuring:
 *
 * 		- Innerese Manipulation
 *  		- Sentence Classification
 * 		- String Matching
 * 		- String Manipulation
 * 		- List Manipulation
 *  		- File Manipulation
 *
 *   	- Specific functions for Recipe Expert
 *   	- Specific test cases for Recipe Expert
 */
package utils;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.xstream.mapper.CGLIBMapper.Marker;

import consciousness.JustDoIt;
import constants.Markers;
import constants.Radio;
import constants.Switch;
import dictionary.WordNet;

//import de.linguatools.disco.Compositionality;
//import de.linguatools.disco.CorruptConfigFileException;
//import de.linguatools.disco.DISCO;
//import de.linguatools.disco.DISCO.SimilarityMeasure;
//import de.linguatools.disco.ReturnDataBN;
//import de.linguatools.disco.ReturnDataCol;
//import de.linguatools.disco.WrongWordspaceTypeException;

import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.TreeReader;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import matchers.EntityMatcher;
import matchers.StandardMatcher;
import matchers.Substitutor;
import matchers.representations.EntityMatchResult;
import start.Start;
import storyProcessor.StoryProcessor;
import translator.Translator;
import utils.minilisp.LList;
import utils.tools.JFactory;
import zhutianYang.BedtimeStoryLearner;
import zhutianYang.PageHowToLearner;
import zhutianYang.RGoal;
import zhutianYang.RecipeExpert;
import zhutianYang.RecipeLearner;
import zhutianYang.StratagemExpert;
import zhutianYang.ZRelation;
import zhutianYang.ZSay;

/**
 * @author zhutian Yang,
 * @time updated 2 Feb 2020
 */

public class Z {
	
	public static Boolean START_DEBUG = false;

	public static final String CONDITION_ALWAYS = "always";
	public static Boolean suppressPrinting = true;
	public static Boolean DEBUG = true;
	
	// ------ for ZRelation
	// for keeping track of the entities appeared, 
	//      replace whatever placeholders there are in the tansition rule with this set in sequence
	public static String[] holders = new String[]{"aaa", "bbb", "ccc", "ddd", "eee", "fff",
			"ggg", "hhh", "iii", "jjj", "kkk", "lll", "mmm", "nnn", "ooo", "ppp",
			"qqq", "rrr", "sss", "ttt", "uuu", "vvv", "www", "xxx", "yyy", "zzz",
			"abc", "xyz", "omg", "nph", "lmk", "sgd", "hah", "yzt", "xly", "jzj",
			"dyg", "tww", "xxp", "ywd", "ljk", "mak", "wyg", "wsm", "wjy", "dhk",
			"zjp", "zgf", "zty", "kfz", "rzy", "rzc", "xbs", "xxj", "xxy"}; 
	public static int NofObjects = 0; // for ZRelation
	public static int NofObj = 0; // for RGoal
	
	// Long-term memory for knowledge
	public static String storiesPath = "corpora/zStoriesForPS/";
	public static final String filePath = "corpora/zMemory/";
	public static final String knownStepsFile = "students/zhutianYang/zKnownSteps.txt";
	public static final String transitionRulesFile = "students/zhutianYang/zTransitionRulesBlocks.txt";
	public static final String transitionRulesFile1 = "corpora/zStoriesOfWar/zTransitionRules1.txt";

	public static List<String> transitionRules;
	public static List<String> knownSteps;
	public static Translator t = getTranslator();
	public static Generator g = Generator.getGenerator();
	public static List<String> nounNames = new ArrayList<>();
	public static List<Entity> nounEntities = new ArrayList<>();
	public static Map<Entity, Entity> nounEntityOwner = new HashMap<>();
	public static NewTimer timer = NewTimer.zTimer;
	public static WordNet wordNet = new WordNet();
	
	// for ZRelation
	public static final String ASSIGNMENTS = "story assignments";
	public static final String SENTENCES = "story sentences";
	public static final String SHE = "she";
	public static final String HE = "he";
	public static final String IT = "it";
	public static final String THAT = "that";
	public static final String THEY = "they";
	public static final String PERSON = "person";
	public static final String THING = "thing";
	public static final List<String> CHANGELESS = Arrays.asList("forget", "fail", "avoid", "neglect");
	public static final List<String> POSITIONS = Arrays.asList("in", "inside", "out_of", "outside",
			"on", "on_top_of", "under", "above", "around", "over");
	public static final String MENTAL = "mental action";
	
	// for RGoal
	public static final String CONTACT = "contact";
	public static final String STATE = "state";
	public static final String WHAT = "?";
	public static final String ITSELF = "itself"; // for substituting object to translated sentences with "it"
	public static final String ACTION = "action";
	public static final String ADWORD = "ad_word";
	public static final String ATTRIBUTE = "attribute";
	public static final String MEASURE = "measure";
	public static final String SHAPE = "shape";
	public static final String TOOL = "tool";
	
	public static final String UP = "up";
	public static final String OF = "of";
	public static final String INTO = "into";
	public static final String ONTO = "onto";
	public static final String OVER = "over";
	public static final String DOWN = "down";
	public static final String UNDER = "under";
	public static final String AROUND = "around";
	public static final String INSIDE = "inside";
	public static final String ONTOPOF = "on_top_of";
	public static final String TOP = "top";
	
	public static final String WITHOUT = "without";
	public static final String OUTOF = "out_of";
	public static final String OUT = "out";
	public static final String TOWARD = "toward";
    public static final String OUTSIDE = "outside"; 
	
	// for Recipe Expert
	public static final String INSERT_CONVERSATION = "Insert conversation";
	public static final String ASSUME_SUCCESS = "Assume success";
	public static final String VERIFY = "Verify: ";
	public static final String SUBJECT = "subject";
	public static final String FINALLY = "finally";
	public static final String AS = "as";
	public static final String WISH = "wish";
	public static final String NEED = "need";
	public static final String LET = "let";
	
	public static final String GOAL = "goal";
	public static final String STEPS = "steps";
	public static final String CONDITION = "condition";
	public static final String WHATIF = "what if";
	public static final String RESPONSE = "response";
	public static final String EXECUTE = "execute";
	public static final String SHOWME = "show me";
	public static final String Yang = "Yang";
	public static final String Human = "Human";
	
	// for Problem Solver
	public static final String INSTANCE = "instance";
	public static final String OBSERVED = "observed";
	public static final String OBSERVE = "observe";
	public static final String TRUE = "True";
	public static final String FALSE = "False";
	public static final String YES_WORD = "yes";
	public static final String NO_WORD = "no";
	
	// for interfaces
	public static final String YES = "positive response";
	public static final String NO = "negative response";
	public static final String NONE = "empty response";
	public static final String SKIP = "skip this response";
	public static final String NORMAL = "normal response";
	public static final String DONT_KNOW = "I don't know response";
	public static String toPageHistorian = "nothing";
	
	// Color.decode("0x"+Z.COLOR_UI_GREY)
	public static String COLOR_BLACK = "34495e";
	public static String COLOR_RED = "e74c3c";
	public static String COLOR_GREEN = "2ecc71";
	public static String COLOR_BLUE = "3498db";
	public static String COLOR_GREY = "7f8c8d";
	public static String COLOR_UI_GREY = "efefef";
	public static String COLOR_LIGHT_GREEN = "C5F0C5";
	public static String COLOR_LIGHT_YELLOW = "FFEEA7";
	public static String COLOR_LIGHT_CYAN = "D1EEFE";
	public static String COLOR_LIGHT_PINK = "FFEFF3";
	
	public static Color BLACK = Color.decode("0x"+COLOR_BLACK);
	public static Color RED = Color.decode("0x"+COLOR_RED);
	public static Color GREEN = Color.decode("0x"+COLOR_GREEN);
	public static Color BLUE = Color.decode("0x"+COLOR_BLUE);
	public static Color GREY = Color.decode("0x"+COLOR_GREY);
	public static Color UI_GREY = Color.decode("0x"+COLOR_UI_GREY);
	public static Color LIGHT_GREEN = Color.decode("0x"+COLOR_LIGHT_GREEN);
	public static Color LIGHT_YELLOW = Color.decode("0x"+COLOR_LIGHT_YELLOW);
	public static Color LIGHT_CYAN = Color.decode("0x"+COLOR_LIGHT_CYAN);
	public static Color LIGHT_PINK = Color.decode("0x"+COLOR_LIGHT_PINK);
	
	// for recipe learner
	public static final String FINISHED = "finished.";
	
	public static void main(String[] args) {

//		testCountCharInString();
//		testGetRoles();
		testSteps2States();
//		testSentence2Chat();
//		testDescription2Entity();
//		testFindUnknownObject();
//		testDSICOSimilarity(); // not working
//		testStringIsSteps();
//		testStringIsCondition();
//		testGetSentenceType();
//		testHasCan();
//		testIsImperative();
//		testYesNoQuestion();  
//		testGetEntities();
//		testHasCan();
//		testVerbMatching();
		
//		String string = "pour water out of mouth";
//		Entity entity = t.translate(string).getElement(0);
//		Z.understand(entity.getSubject());
		
//		testMarkSay();
//		testConsoleColor();
		
		//// for event2relation
//		testNewsDemo();
//		testGetPrettyTree();
//		testGetNounEntityOwner();
//		testAction2PastState();
//		testGetRoleEntities();
//		testRepairAdWord();
		
		//// for recipe expert
//		testStory2Sentences();
//		testAddAfterLine();
//		testRemoveAfterLine();
//		testNoAdv();
//		testMatchingForRead();
//		testHowQuestion();
//		testReverse();
//		testGetSentenceType();
//		testClassify();
//		testquestion2Goal();
//		testStringIsSteps();
//		testStringIsIAm();
//		testSteps2States(); 
//		testWhereQuestion();
//		testPlace2Ground();
//		testPrintThread();
//		testIsInstance();
//		testSimplifySentence();
//		testSentence2Chat();
//		testquestion2Goal();
//		testSteps2States();
		
		//// for two batteries
//		testBatteryStory();
//		testIsNegation();
		
		//// for Martini demo
//		testRel2RGoal();
//		testEntity2String();
//		testGetDifferences();
//		testRepairAdWord();
//		testMatchHowToGoals();
		
		//// for HowTo Book demo
//		testRepairSentence();
//		testcanBeNoun();
//		testGetInnerese();
		
		//// for robot chef demo
//		testHaveDone();
//		testCheckObserve();
//		testGetFullName();
//		testSteps2States();
//		testGetRoles();
//		testPlace2Ground();
//		testWhereQuestion();
//		testDoIAlways();
//		testquestion2Goal();
//		testQuestion2Goal();
		
		//// for new battery demo
//		testLearnFromStoryEnsembles();
//		testEvent2SimpleAction();
		
		//// for symposium presentation
//		testMeans2Want();
//		testGetBinds();
//		testRoleFrameMaker();
//		testGetPrettyTree();
//		Z.understand("John believes \"Mary thinks < Sally knows \"Susan loves Paul\" > \".");
//		RGoal.testRGoalFromString();
		
//		Z.printInnereseTree("I love Mary and John");
//		textString2Sentences();
		
//		testTranslatorRuleSet();
	}
	
	public static void test() {
		List<String> strings = new ArrayList<>();
		strings.add("");
		for(String string: strings) {
			Mark.say(stringIsIAm(string));
		}
	}

	public static void testTranslatorRuleSet() {
		List<String> strings = new ArrayList<>();
		
		// ------------ TODO
		strings.add("Did the girl take the ball?");
//		strings.add("Why did the man run to a hole");
		
		// ------------------------
		
//		strings.add("Bob got mad and he informed the teacher");
//		strings.add("The man believed the bird flew");
//		strings.add("The man believed that the bird wanted to fly.");
//		strings.add("Duncan is king because Macbeth defeated the rebels.");
//		strings.add("The boy disappeared because a dog barked and a bear appeared and a cat ran to a lake");
//		strings.add("Henry is happy because James defeated the rebel.");
//		strings.add("England's power became weaker than Poland's power");
//		strings.add("XX becomes happy because XX wanted an event to occur and the event occurred");
		//		there are two "because" relations
		
//		strings.add("If James harms Henry and Henry does not become dead, then James angers Henry.");
//		strings.add("the king persuaded the people to kill macbeth");
//		strings.add("Start description of \"Revenge\".");
//		strings.add("Start story titled \"Macbeth plot\".");
//		strings.add("Duncan, who is Macduff's friend, ran");
//		strings.add("Then, the boy ran.");
//		strings.add("Time passes.");
//		strings.add("Describe a ball.");
//		strings.add("Macbeth murdered Duncan by stabbing him with a knife");
//		strings.add("xx is a Dog.");
//		strings.add("If xxx murder yyy then yyy is dead.");
//		strings.add("why did macduff kill macbeth");
//		strings.add("xx's being not sane leads to xx's killing yy");
//		strings.add("xx's wanting an action leads to the action.");
//		strings.add("John may steal money from Henry because Henry trusts John");
//		strings.add("Hamlet and Polonius are persons");
//		strings.add("Imagine a jumping event");
//		strings.add("Imagine that a bird flew");
//		strings.add("Is the first person moving toward the second person");
//		strings.add("Advance video to frame 27");
//		strings.add("web site is part of computer network");
//		strings.add("birds have wings");
//		strings.add("The robin (flew fly travel) to a (tree organism)");
//		strings.add("The robin flew to a tree");
//		strings.add("John gave a ball to his honey for a rock");
//		strings.add("if a cat appears a bird may possibly fly");
//		strings.add("Sometimes, Patrick's killing of Macbeth leads to Macbeth's hating of Patrick.");
//		strings.add("Sally married Patrick because patrick is tall and patrick is short.");
//		strings.add("John and Mary love each other");
//		strings.add("check whether john loves mary");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
//		strings.add("");
		for(String string: strings) {
			Entity ent = t.translate(string); 
			Mark.say(ent);
		}
	}
	
	public static void testRoleFrameMaker() {
		List<String> strings = new ArrayList<>();
//		strings.add();
//		strings.add("Tom loves eating cheese cakes");
//		for(String string: strings) {
//			Mark.say(Z.getPrettyTree(string));
//			understand(string);
//		}
		String string = "Does Tom love eating cheese cakes?";
		Mark.say(Z.getPrettyTree(string));
		understand(string);
		
		Entity rel1 = t.translate("Tom loves eating cheese cakes").getElement(0);
		Function question = new Function("does", rel1);
		Mark.say(question);
		understand(question);
		Mark.night(g.generate(question));
	}
	
	

	
	public static void testGetBinds() {
		t.internalize("Sisk is a person.");
		t.internalize("Hazel is a person.");
		t.internalize("xx is a person.");
		t.internalize("yy is a person.");
		Entity ent1 = t.translate("Sisk pressed Hazel's chest.");
		Entity ent2 = t.translate("xx pressed yy's chest.");
		
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(ent1, ent2);
		Mark.say(getBinds(bindings));
	}
	
	public static List<List<Entity>> getBinds(LList<PairOfEntities> bindings) {
		List<List<Entity>> result = new ArrayList<>();
		for(PairOfEntities pair: bindings) {
			result.add(Arrays.asList(pair.getPattern(), pair.getDatum()));
		}
		return result;
	}
	
	public static void testMeans2Want() {
		
		List<String> strings = new ArrayList<>();
		strings.add("Scar killed Mufasa in order to become the king.");
		strings.add("I go to school in order to learn");
		for(String string: strings) {
			Mark.show(string);
			Mark.say(means2Want(string));
		}
		
	}
	
	public static Entity means2Want(String string) {
		return means2Want(t.translate(string));
	}
	
	public static Entity means2Want(Entity whole) {
		if (isSeq(whole)) {
			whole = whole.getElement(0);
		}
		Entity ent = whole.getObject();
		Entity name = whole.getSubject().getElement(0).getSubject();
		
		Sequence roles = new Sequence(Markers.ROLE_MARKER);
		roles.addElement(ent);
		Relation want = new Relation(Markers.WANT_MARKER, name, roles);
		
		for(Entity ent2: Z.getNounEntities(ent)) {
			if(ent2.getType().equals(name.getType())) {
				want.setSubject(ent2);
			}
		}
		return want;
	}
	

	public static void testLearnFromStoryEnsembles() {
		List<String> strings = new ArrayList<>();
		strings.add("corpora/Ensembles/Repair a phone.txt");
		for(String string: strings) {
			learnFromStoryEnsembles(string);
		}
	}
	
	public static Map<String, List<String>> learnFromStoryEnsembles(String storyPath) {
		Map<String, List<String>> toReturn = new HashMap<>();
		try {
			List<String> lines = new ArrayList<>();
			lines = new ArrayList<>(Files.readAllLines(Paths.get(storyPath), StandardCharsets.UTF_8));
			lines.removeAll(Arrays.asList("", null));
			
			int mode = 0;
			List<String> assignments = new ArrayList<>(); // mode 1
			List<String> rules = new ArrayList<>(); // mode 2
			List<String> storyOne = new ArrayList<>(); // mode 3
			List<String> storyTwo = new ArrayList<>(); // mode 4
			
			for(String line: lines) {
				if(line.startsWith(Markers.BOTH_PERSPECTIVES_TEXT)) { mode = 1;
				} else if (line.startsWith(Markers.START_CONCEPT_TEXT)) { mode = 0;
				} else if (line.startsWith(Markers.THE_END_TEXT) && mode == 0) { mode = 2;
				} else if (line.startsWith(Markers.SHOW_BOTH_PERSPECTIVES)) { mode = 0;
				} else if (line.startsWith(Markers.FIRST_PERSPECTIVE_TEXT)) { mode = 5;
				} else if (line.startsWith(Markers.START_STORY_TEXT) && mode == 5) { mode = 3;
				} else if (line.startsWith(Markers.THE_END_TEXT) && mode == 3) { mode = 0;
				} else if (line.startsWith(Markers.SECOND_PERSPECTIVE_TEXT)) { mode = 6;
				} else if (line.startsWith(Markers.START_STORY_TEXT) && mode == 6) { mode = 4;
				} else if (line.startsWith(Markers.THE_END_TEXT) && mode == 4) { mode = 0;
				} else if (!line.startsWith("//")) {
					line = line.startsWith(" ")? line.substring(1):line;
					if(mode==1) { assignments.add(line);
					} else if (mode==2 && line.contains(Markers.MUST_WORD)) { rules.add(line);
					} else if (mode==3) { storyOne.add(line);
					} else if (mode==4) { storyTwo.add(line);}
				}
			}
			
			
//			Mark.show("file");
//			Z.printList(lines);
//			Mark.show("assignments");
//			Z.printList(assignments);
//			Mark.show("rules");
//			Z.printList(rules);
//			Mark.show("storyOne");
//			Z.printList(storyOne);
//			Mark.show("storyTwo");
//			Z.printList(storyTwo);
			
			toReturn.put("story1", storyOne);
			toReturn.put("story2", storyTwo);
			
			for(String assignment: assignments) {
				Entity ent = ZRelation.t.translate(assignment);
				Mark.say("    internalizing...", assignment);
				List<String> nounEntities = Z.getNounNames(assignment);
				if (nounEntities.contains(Z.PERSON)) {
					ZRelation.persons.add(ent.getElement(0).getObject());
				}
			}
			
			String storyName = storyPath.substring(storyPath.lastIndexOf("/")+1, storyPath.length()).replace(".txt", "");
			
			Mark.say("\n\n Reading the 1st story: ");
			List<ZRelation> ones = ZRelation.sentences2ZRelations(storyName, storyOne,rules);
//			ZRelation.listPrint(ones);
			
			Mark.say("\n Reading the 2nd story: ");
			Z.NofObjects = 0;
			ZRelation.currentPerson = null;
			List<ZRelation> twos = ZRelation.sentences2ZRelations(storyName, storyTwo,rules);
//			ZRelation.listPrint(twos);
			
			
			Mark.say("\n\n\n Events in the 1st story: ");
			toReturn.put("events1", ZRelation.listPrintSteps(ones));
			
			Mark.say("\n Events in the 2nd story: ");
			toReturn.put("events2", ZRelation.listPrintSteps(twos));
			
			
			Mark.say("\n\n\n Goals in the 1st story: ");
			toReturn.put("goals1", ZRelation.listPrintRelations(ones));
			
			Mark.say("\n Goals in the 2nd story: ");
			toReturn.put("goals2", ZRelation.listPrintRelations(twos));
			
			
			// find common steps
			Mark.say("\n\n\n Relevant events in the 1st story: ");
			List<ZRelation> commons = ZRelation.getCommonSubgoals(Arrays.asList(twos, ones));
			List<String> stepsGeneral = ZRelation.stepsGeneral;
			
			List<String> steps1 = ZRelation.listPrintSteps(commons);
			List<String> newSteps1 = new ArrayList<>();
			for(String step: steps1) {
				newSteps1.add(event2SimpleAction(step));
			}
			toReturn.put("steps1", newSteps1);
			
			Mark.say("\n Relevant events in the 2nd story: ");
			commons = ZRelation.getCommonSubgoals(Arrays.asList(ones,twos)); 
			List<String> steps2 = ZRelation.listPrintSteps(commons);
			List<String> newSteps2 = new ArrayList<>();
			for(String step: steps2) {
				newSteps2.add(event2SimpleAction(step));
			}
			toReturn.put("steps2", newSteps2);
			
			Mark.say("\n General goals learned from two stories: ");
			toReturn.put("stepsGeneral", ZRelation.listPrintList(stepsGeneral));
			
			// generate recipes
			Z.suppressPrinting = true;
			String parentFolderPath = storyPath.substring(0, storyPath.lastIndexOf("/")+1);
			RecipeLearner.filePath = parentFolderPath;
			
			String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
			List<String> newSteps0 = new ArrayList<>();
			newSteps0.add("can you "+storyName.toLowerCase()+"?");
			for(String step: stepsGeneral) {
				newSteps0.add(step);
			}
			RecipeLearner.instructionsToMicroStories(newSteps0, date, "");
			String recipeFile = parentFolderPath+RecipeLearner.stepsFileNameString+".txt";
			Mark.say("\n\n\n Generated micro-stories for the general goals at "+recipeFile+
					"\n Generated problem solving test file for the general goals at "+recipeFile.replace("Steps_", ""));

			
			RecipeLearner.hasWrittenTester = false;
			date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
			List<String> newSteps11 = new ArrayList<>();
			newSteps11.add("can you "+storyName.toLowerCase()+"?");
			for(String step: newSteps1) {
				newSteps11.add(step);
			}
			RecipeLearner.instructionsToMicroStories(newSteps11, date, "");
			recipeFile = parentFolderPath+RecipeLearner.stepsFileNameString+".txt";
			Mark.say("\n\n Generated micro-stories for the 1st story at "+recipeFile+
					"\n Generated problem solving test file for the 1st story at "+recipeFile.replace("Steps_", ""));
			
			
			RecipeLearner.hasWrittenTester = false;
			date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
			List<String> newSteps21 = new ArrayList<>();
			newSteps21.add("can you "+storyName.toLowerCase()+"?");
			for(String step: newSteps2) {
				newSteps21.add(step);
			}
			RecipeLearner.instructionsToMicroStories(newSteps21, date, "");
			recipeFile = parentFolderPath+RecipeLearner.stepsFileNameString+".txt";
			Mark.say("\n\n Generated micro-stories for the 2nd story at "+recipeFile+
					"\n Generated problem solving test file for the 2nd story at "+recipeFile.replace("Steps_", ""));
			
			Files.copy(Paths.get("corpora/zMemory/ZZ Basic blocks knowledge.txt"), 
					Paths.get(parentFolderPath + "ZZ Basic blocks knowledge.txt"), 
					StandardCopyOption.REPLACE_EXISTING);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return toReturn;
	}
	
	public static void testEvent2SimpleAction() {
		List<String> strings = new ArrayList<>();
//		strings.add("Bob wants to expose the phone's parts");
//		strings.add("Bob wants to expose my phone's parts");
//		strings.add("Bob wants to expose his phone's parts");
//		strings.add("He wants to expose my phone's parts");
//		strings.add("He wants to expose his phone's parts");
		
//		strings.add("Bob wants to expose the client's phone's parts");
//		strings.add("Alice wants to expose the phone's parts.");
//		strings.add("Bob wants to separate the old battery from the client's phone.");
//		strings.add("Alice wants to separate the old battery from the phone.");
//		strings.add("Bob wants to incorporate the replacement battery into the client's phone.");
//		strings.add("Alice wants to incorporate the replacement battery into the phone.");
//		strings.add("Client wants to attach the cover to the client's phone.");
//		strings.add("Alice wants to attach the cover to the phone.");
		
//		strings.add("Alice removes the cover from the phone.");
//		strings.add("Alice collects the old battery from the phone.");
//		strings.add("Alice inserts the replacement battery into the phone.");
//		strings.add("Alice puts the cover on the phone.");
//		
//		strings.add("Bob slides down the cover from the client's phone.");
//		strings.add("Bob takes the old battery from the client's phone out.");
//		strings.add("Bob puts the replacement battery in the client's phone.");
//		strings.add("Client puts the cover on the client's phone.");
		
//		strings.add("Client removes the cover from the phone quickly.");
		
		for(String string: strings) {
			Mark.show(event2SimpleAction(string));
		}
	}
	
	public static String event2SimpleAction(String temp1){
		Entity entity1 = t.translate(temp1).getElement(0);
		return event2SimpleAction(temp1, entity1);
	}
	
	public static String event2SimpleAction(String temp1, Entity entity1){
//		if(entity1.getType().equals(Markers.WANT_MARKER)) {
			
			// -------------- Method 1 for matching
//			entity1 = entity1.getObject().getElement(0).getSubject();
//			temp1 = entity1.toString().replaceAll("[0-9]","");
			
			
			// -------------- Method 2 for matching
			if(entity1.getType().equals(Markers.WANT_MARKER)) {
				entity1 = entity1.getObject().getElement(0).getSubject();
			}
			
			// ------ Become present tense
			if (entity1.hasFeature(Markers.PAST)) {
				entity1.removeFeature(Markers.PAST);
			}
			if (entity1.hasFeature(Markers.PASSIVE)) {
				entity1.removeFeature(Markers.PASSIVE);
			}
			if (entity1.hasProperty(Markers.PROGRESSIVE)) {
				entity1.removeProperty(Markers.PROGRESSIVE);
			}
			entity1.setSubject(new Entity("i"));
			temp1 = g.generate(entity1);  // will miss "the" at "Bob wants to expose the phone's parts"
			
			List<Entity> nouns1 = Z.getNounEntities(entity1);
			Map<String, String> owners = new HashMap<>();
			for(Entity noun: nouns1) {
				if(noun.hasProperty(Markers.OWNER_MARKER)) {
					Entity name = (Entity) noun.getProperty(Markers.OWNER_MARKER);
//					understand(name);
					owners.put(noun.getType(), name.getType());
				}
			}
			
			// ------ Eliminate owners
			for(String object: owners.keySet()) {
				String owner = owners.get(object);
				if(owner.equals("i")) temp1 = temp1.replace("my "+object, "the "+object);
				else if(owner.equals("we")) temp1 = temp1.replace("our "+object, "the "+object);
				else if(owner.equals("you")) temp1 = temp1.replace("your "+object, "the "+object);
				else if(owner.equals("he")) temp1 = temp1.replace("his "+object, "the "+object);
				else if(owner.equals("she")) temp1 = temp1.replace("her "+object, "the "+object);
				else if(owner.equals("it")) temp1 = temp1.replace("its "+object, "the "+object);
				else if(owner.equals("they")) temp1 = temp1.replace("them "+object, "the "+object);
				else {
					temp1 = temp1.replace(owner+"\'s "+object, object);
					temp1 = temp1.replace(Z.string2Capitalized(owner)+"\'s "+object, "the "+object);
				}
			}
			temp1 = temp1.substring(2);
			
			// ------ Eliminate adverb
			List<String> manners = new ArrayList<>();;
			for(Entity entity: entity1.getObject().getElements()) {
				if(entity.getType().equals(Markers.MANNER_MARKER)) {
					manners.add(entity.getSubject().getType());
				}
			}
			for(String manner: manners) {
				temp1 = temp1.replace(" "+manner, "");
			}
			
//		} else {
//			temp1 = temp1.replace("She ", "one ");
//			temp1 = temp1.replace("He ", "one ");
//		}

		return temp1;
	}
	
	
	public static void testGetFullName() {
		List<String> strings = new ArrayList<>();
//		strings.add("the red apple can be observed");
//		strings.add("a red apple can be observed");
//		strings.add("an apple can be observed");
//		strings.add("two red apples can be observed");
//		strings.add("some apples can be observed");
		
		// do not deal with
//		strings.add("my apples can be observed");
//		strings.add("a cup of apples can be observed");   // two relations, first is OF
		strings.add("two boxes of apples can be observed");
		
		for(String string: strings) {
			Entity entity = t.translate(string).getElement(0);
			Mark.say(entity.getType());
			Z.printInnereseTree(entity);
			understand(entity);
			entity = entity.getObject().getElement(0).getSubject();
			Mark.show(getFullName(entity));
		}
	}
	
	public static String getFullName(Entity entity) {
		String name = entity.getType();
		Mark.night("=+++++++++++++++++ get name ++++++++++++++++++++=");
		Z.understand(entity);
		
		// big red apple
		if(entity.getFeatures().size()!=0) name = entity.getFeatures().toString().replace(",", "").replace("[", "").replace("]", "")+ " "+name;
		
		// two apples
		if(entity.hasProperty(Markers.QUANTITY)) name = entity.getProperty(Markers.QUANTITY) + " " + name;
		
		// some apples
		if(entity.hasProperty(Markers.QUANTIFIER)) name = entity.getProperty(Markers.QUANTIFIER) + " " + name;
		
		// an apple, the apple
		if(entity.hasProperty(Markers.DETERMINER)) {
			String det = entity.getProperty(Markers.DETERMINER).toString();
			String article = "a";
			if (det.equals(Markers.DEFINITE)) {
				article = "the";
			} else {
				String firstWord = name.indexOf(" ")>0?name.substring(0, name.indexOf(" ")):name;
				List<String> anExceptions = Arrays.asList("hour", "heir", "honor",
						"honest");
				List<String> aExceptions = Arrays.asList("eu", "use", "one");
				if( firstWord.startsWith("a") || firstWord.startsWith("e") || firstWord.startsWith("i") ||
						firstWord.startsWith("o") || firstWord.startsWith("u")) {
					article = "an";
					for(String a:aExceptions) {
						if(firstWord.startsWith(a)) article = "a";
					}
				} else if(anExceptions.contains(firstWord)) {
					article = "an";
				}
			}
			name = article + " " + name;
		} 
		
		
		Mark.night(name);
		return name;
	}
	
	public static void testCheckObserve() {
		List<String> strings = new ArrayList<>();
		strings.add("the red apple can be observed");
		for(String string: strings) {
			Entity entity = t.translate(string).getElement(0).getObject().getElement(0).getSubject();
			Z.printInnereseTree(entity);
			Z.understand(entity);
		}
	}
	
	public static void testMatchHowToGoals() {
		matchHowToGoals("make a banana salad", "make an apple salad");
	}
	
	public static void matchHowToGoals(String oldGoal, String newGoal) {
		t.translate("apple salad is a thing");
		t.translate("banana salad is a thing");
		Entity oldProblem = t.translateToEntity(oldGoal);
		Entity newProblem = t.translateToEntity(newGoal);
		
		printInnereseTree(oldProblem);
		printInnereseTree(newProblem);
		
		understand(oldProblem);
		understand(newProblem);
		
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(oldProblem, newProblem);

		// found exact same problem
		if(oldProblem.isDeepEqual(newProblem)) {
			Mark.say("I know how to "+Z.sentence2Chat(newGoal)+". Let me show you!");

		// found similar problem
		} else if(bindings!=null) {
			Mark.say("I don't know know how to "+newGoal+", but I guess it is similar to "+oldGoal);
			
		} else {
			Mark.say("I don't know how to "+newGoal);
		
		}
	}
	
	public static void matchEntities(Entity one, Entity two) {
		Mark.show(one);
		printList(Z.getWordnetThreads(one.getType()));
		Mark.show(two);
		printList(Z.getWordnetThreads(two.getType()));
	}
	
	public static void testGetDifferences() {
		Mark.mit(getDifferences("wet the augothus bitters with itself", "wet the two drops with itself"));
		Mark.mit(getDifferences("wet the two drops with itself", "wet the augothus bitters with itself"));
		Mark.mit(getDifferences("I love you", "I love you"));
	}
	
	public static Map<String, String> getDifferences(String one, String two){
		Map<String, String> differences = new HashMap<String, String>();
		one = one.replace(".", "").replace("?", "").replace("!", "") + " ";
		two = two.replace(".", "").replace("?", "").replace("!", "") + " ";

		int startDifference = 0;
		for(int i=0;i<one.length();i++) {
			if(i<two.length()) {
				char a = one.charAt(i);
				char b = two.charAt(i);
				if(a==b) {
//					Mark.say(a, b);
					startDifference = i;
				} else {
					break;
				}
			}
		}
		for(int i=startDifference;i>=0;i--) {
			if(Character.toString(one.charAt(i)).equals(" ")) {
				startDifference = i+1;
				break;
			}
		}
//		Mark.say(startDifference);
		
		int endDifference = 0;
		for(int i=0;i<one.length();i++) {
			if(i<two.length()) {
				char a = one.charAt(one.length()-1-i);
				char b = two.charAt(two.length()-1-i);
				if(a==b) {
//					Mark.say(a,b);
					endDifference = i;
				} else {
					break;
				}
			}
		}
//		Mark.say(endDifference);
		for(int i=one.length()-endDifference;i<one.length();i++) {
			Mark.say(one.charAt(i));
			if(Character.toString(one.charAt(i)).equals(" ")) {
				endDifference = one.length()-i-1;
				break;
			}
		}
//		Mark.say(endDifference);
		
		
//		Mark.say(one.length(), two.length());
		if(startDifference<endDifference) {
			String one1 = one.substring(startDifference, one.length()-endDifference-1);
			String one2 = two.substring(startDifference, two.length()-endDifference-1);
			differences.put(one1, one2);
		}
		
		return differences;
	}
	public static void testRel2RGoal() {
		List<String> strings = new ArrayList<>();
		strings.add("In an Old-Fashioned glass, muddle the bitters.");
		strings.add("Add a large ice cube.");
		strings.add("stir with your bar spoon.");
		for(String string: strings) {
			RGoal.rel2RGoal(t.translate(string).getElement(0), false);
			RGoal.printRGoals(RGoal.rGoals);
		}
	}
	
	public static void strangeReport() {
		understand("he isn't seated");
		understand("he is not seated");
	}
	
	public static Entity clearEntity(Entity entity) {
		if (entity==null) return entity;
		if (entity.getType().equals(Markers.ROLE_MARKER)) entity = entity.getElement(0);
		if (entity.getType().equals(Markers.OBJECT_MARKER)) entity = entity.getSubject();
		return entity;
	}
	
	public static void testBatteryStory() {
		String story = "Alice replaces cellphone battery";
		
		List<ZRelation> oneStory = ZRelation.story2ZRelations(story,""); 

		// every time reading a story, the steps are redefined
		List<String> learnedSteps = new ArrayList<>();
		List<String> learnedRelations = new ArrayList<>();
		List<String> instructions = new ArrayList<>();
		List<String> steps = new ArrayList<>();
		instructions.add("do you know how to "+story+"?");

		learnedRelations = ZRelation.listPrintRelations(oneStory);
		int count = 0;
		String toPrintRelations = "";
		for(String to:learnedRelations) {
			toPrintRelations = toPrintRelations + "\n  " + (++count) + "    " + to;
		}

		// if the first story, print all relations
		// get steps from one story
		learnedSteps = ZRelation.listPrintStepsPresent(oneStory);
//					learnedRelations = ZRelation.listPrintRelations(oneStory);
		instructions.add("you need to "+RecipeExpert.listToStory(learnedSteps));

		// prepare output string
		String toPrint = "I learned the steps how " + story;
		count = 0;
		for(String to:learnedSteps) {
			steps.add(to);
			toPrint = toPrint +	"\n    <font color=\"#FFA500\">" + (++count) + "    " + to + "</font>"; // #FFA500
		}
	}
	
	public static Entity simplifySentence(String string) {
		Entity entity = t.translate(string).getElement(0);
		return simplifyEntity(entity);
	}
	
	public static Entity simplifyEntity(Entity entity) {
		
		// "as we have suggested ...."
		if(entity.getType().equals(Z.AS)) {
			try{
				String triples = g.generateTriples(entity);
			} catch (Exception e) {
				Mark.night("cannot");
//				understand(entity);
				entity = entity.getSubject();
				try{
					String triples = g.generateTriples(entity);
				} catch (Exception e1) {
					Mark.night("still cannot");
					return null;
				}
			}
		}
		return entity;
	}
	public static void testSimplifySentence() {
		List<String> strings = new ArrayList<>();
		strings.add("as we have suggested, you should first inspect the book.");
		for(String string: strings) {
			Mark.say(simplifySentence(string));
		}
	}
	
	public static void testIsInstance() {
		List<String> strings = new ArrayList<>();
		strings.add("bitter");
		strings.add("bitters");
		strings.add("tea");
		strings.add("apple");
		for(String string: strings) {
			Mark.show(string);
			Mark.say("",isInstance(string, "beverage"));
		}
	}
	
	public static Boolean isInstance(Entity token, Entity type) {
//		Mark.night(token);
//		Mark.night(type);
//		Mark.night(token.getType());
//		Mark.night(type.getType());
//		Mark.night(getName(token));
//		Mark.night(getName(type));
		return isInstance(getName(token), getName(type));
	}
	
	public static Boolean isInstance(Entity token, String type) {
//		Mark.night(token);
//		Mark.night(type);
		return isInstance(getName(token), type);
	}
	
	public static Boolean isInstance(String token, String type) {
		String oldToken = token;
		if(token.contains("_")) {
//			if(getWordnetThread(token).length()==0) {
//				token = token.substring(token.lastIndexOf("_")+1, token.length());
//			}
			token = token.substring(token.lastIndexOf("_")+1, token.length());
			if(isInstance(token, Z.ATTRIBUTE)) {
				token = oldToken.substring(0, oldToken.lastIndexOf("_"));
			}
//			Mark.night(getWordnetThread(token));
		}
		
		if(getWordnetThread(token).contains(" "+type+" ")) {
//			Mark.say("    True categorization",token,type);
			return true;
		} else {
//			Mark.say("    False categorization",token,type);
			return false;
		}
	}
	
	public static void testPlace2Ground() {
		List<String> strings = new ArrayList<>();
//		strings.add("it's in the blue cup");
//		strings.add("in the blue cup");
//		strings.add("inside the blue cup");
//		strings.add("in the box");
		strings.add("in the cup on the left");
//		strings.add("the apples are in the bottle and the pears are in the cup");
//		strings.add("the apples are in the blue cup and the banana is in the green cup");
//		strings.add("the apples are in the blue cup, the banana is in the green cup, and the melons are in the yellow cup");
//		strings.add("the apples are in the green cup, the watermelon is in the red cup, and the kiwis are in the green cup");
//		strings.add("the apples are in the green cup, the watermelon is in the red cup, the kiwis are in the green cup, and the blueberries are in the blue cup.");
//		strings.add("the apples are in the green cup the watermelons are in the red cup the grapes are in the purple cup and the berries are in the blue cup.");
		for(String string: strings) {
			place2Ground(string);
		}
		
	}
	
	public static List<Entity> place2Ground(String place) {
		List<Entity> returns = new ArrayList<>();
		if(!isTranslatable(place)) {
			place = "the girl is " + place;
		}
		Entity entity = t.translate(place);
		understand(entity);
		// if a single place
		if(entity.getElements().size()==1) {
			// "in the box" will be translated as Entity: (ent in_the_box-116)
			if(Z.countCharInString(entity.getElement(0).getType(), "_")>=1) {
				place = "the girl is " + place;
				entity = t.translate(place);
			}
		} 
		for(Entity entity1: entity.getElements()) {
			Mark.night(place);
			understand(entity1);
			Map<String, Entity> roles = getRoleEntities(entity1);
			if (roles.keySet().contains(Markers.IN)) {
				returns.add(roles.get(Markers.IN));
			}
			if (roles.keySet().contains(Z.INSIDE)) {
				returns.add(roles.get(Z.INSIDE));
			}
			if (roles.keySet().contains(Markers.ON)) {
				Entity object = returns.get(returns.size()-1);
				returns.remove(object);
				object.addFeature(roles.get(Markers.ON).getType());
				returns.add(object);
			}
		}
		for(Entity entity1: returns) {
			Mark.night(entity1);
			understand(entity1);
		}
		
		
		return returns;
	}
	
	public static void testWhereQuestion() {
		List<String> strings = new ArrayList<>();
//		strings.add("sugar");
//		strings.add("hamburgers");
//		strings.add("I");
//		strings.add("bitters");
//		strings.add("apples");
//		strings.add("he");
		for(String string: strings) {
			Mark.say(whereQuestion(new Entity(string)));
		}
	}
	
	public static String whereQuestion(Entity entity) {
//		if(Z.countCharInString(entity.getType(), "_")>0) {
//			entity = t.translate("I love "+entity.getType().replace("_", " ")).getElement(0);
//			entity = entity.getObject().getElement(0).getSubject();
//			understand(entity);
//		}
		
		Sequence seq = new Sequence(Markers.ROLE_MARKER);
		Relation rel = new Relation(Markers.BE_MARKER, entity, seq);
		String triples = g.generateTriples(rel);
		String verb = getVerbInTriples(triples);
		String noun = entity.toString().replace(")", "").replace("(ent ", "");
		String newTriples = "["+noun+" has_det definite]"
				+ "["+noun+" "+verb+" null]"
				+ "["+verb+" is_question yes]"
				+ "["+verb+" has_location where]";
		
		String string = Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
		entity = t.translate(string).getElement(0);
		// because "where is fruit pieces" is translated into "(fun where (rel classification (ent slices-21782) (ent fruit-21780)))"
		if(string.endsWith("s?")) string.replace(" is ", " are ");
		return string;
	}
	
	public static void testStringIsIAm() {
		List<String> strings = new ArrayList<>();
		strings.add("I am Da Vinci");
		strings.add("I am Ray Dalio");
		for(String string: strings) {
			Mark.say(stringIsIAm(string));
		}
	}
	
	public static String stringIsIAm(String string) {
		Entity entity = t.translate(string).getElement(0);
		string = "";
		if(hasType(entity, Markers.CLASSIFICATION_MARKER)) {
			if(g.generate(entity.getObject()).equals(Markers.I)) {
				string = g.generate(entity.getSubject());
			}
		}
		if(string.length()!=0) {
			List<String> words = Arrays.asList(string.split(" "));
			string = "";
			for(String word : words) {
				string += Z.string2Capitalized(word) + " ";
			}
			string = string.substring(0, string.length()-1);
		}
		return string;
	}
	
	public static void testMatchingForRead() {
		t.translate("contents are things");
		String string1 = "read the book's contents finally";
		String string2 = "read the contents of the book";
		Mark.say(Z.matchTwoSentences(string1, string2));
		
		string1 = "read the book's contents finally";
		string2 = "read the contents of the book";
		Mark.say(Z.matchTwoSentences(string1, string2));
	}
	
	public static void testNoAdv() {
		String string = "Read its contents finally";
		Entity entity = translateNoAdv(string);
		understand(entity);
		Mark.show(g.generate(entity));
		
	}
	
	public static Entity noAdv(Entity entity) {
		String triples = g.generateTriples(entity);
		String verb = getVerbInTriples(triples);
		
		if(triples.contains(verb + " has_modifier")) {
			String[] temps = triples.split("]");
			String newTriples = "";

			for(String temp: temps) {
				if(!temp.contains(verb + " has_modifier") ) {
					newTriples = newTriples + temp + "]";
				}
			}
			String string = Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
			entity = t.translate(string).getElement(0);
		}
		
		return entity;
	}
	
	public static Entity translateNoAdv(String string) {
		Entity entity = t.translate(string).getElement(0);
		return noAdv(entity);
	}
	
	public static void testAction2PastState() {
		List<String> strings = new ArrayList<>();
//		strings.add("they stir with a rod");
//		strings.add("I go to school every day");
//		strings.add("He thinks that I like playing the piano");
//		strings.add("happily stir with a rod");
//		strings.add("often go to school");
//		strings.add("really think that I like playing the piano");
		strings.add("stir");
		strings.add("go");
		strings.add("think");
		for(String string: strings) {
			Mark.say(action2PastState(string));
		}
	}
	
	public static String action2PastState(String string) {
		Mark.night(string);
		if(string.indexOf(" ")<=0) {
			string = "I " + string;
		} 
		if(!isTranslatable(string)) {
			string += " it";
		}
		Entity entity = t.translate(string).getElement(0);
		entity.setSubject(new Entity("I"));
		String triples = g.generateTriples(entity);
		String verb = getVerbInTriples(triples);

		Mark.show(triples);
		if(triples.contains(verb + " has_tense present")) {
			triples = triples.replace(" has_tense present", " has_tense past");
		} else {
			triples += "["+verb+" has_tense past]";
		}
		Mark.say(triples);
		string = Start.getStart().generate(triples).replace("\n", "").replace(".", "");
		string = string.substring(2);
		if(string.indexOf(" ")<=0) {
			return string;
		}
		return string.substring(0, string.indexOf(" "));
	}
	
	
	public static void testCountCharInString() {
		Mark.say(countCharInString("I have an apple", "p"));
		Mark.say(countCharInString("wow Wow wow", "wow"));
	}
	public static int countCharInString(String string, String ch) {
		return (string.length() - string.replace(ch, "").length())/ch.length();
	}
	public static String checkPeriod(String string) {
		String terminators = "\\?|\n|\\.";
		if(terminators.indexOf(string.charAt(string.length()-1))<0) string += ".";
		return string;
	}
	
	public static void testRemoveAfterLine() {
		List<String> sentences = new ArrayList<String>();
		sentences.add("Alice forgot to take out her Nokia phone from her pants.");
		sentences.add("Then, she put her pants in the working machine.");
		sentences.add("The phone got all wet and stopped working.");
		sentences.add("added after 2");
		sentences.add("added again after 2");
		sentences.add("She decided to replace the battery of the phone first.");
		Z.printList(removeFromLine(sentences,2,5));
	}
	
	public static List<String> removeFromLine(List<String> fileContent, int i, int numberToRemove){
		List<String> newContent = new ArrayList<>();
		for(int j = 0;j<fileContent.size();j++) {
			if(!(j>=i && j< i+numberToRemove)) {
				newContent.add(fileContent.get(j));
			}
		}
        return newContent;
	}
	
	public static List<String> removeFromLine(List<String> fileContent, int i){
        return removeFromLine(fileContent,i,1);
	}
	public static void testAddAfterLine() {
		List<String> sentences = new ArrayList<String>();
		sentences.add("0 If the intention is \"Skim the book\".");
		sentences.add("1 Step: Read the title page.");
		sentences.add("2 The end.");
		sentences.add("3 ");
		sentences.add("4 // ------------ Step 1: Read the title page");
		sentences.add("5 If the problem is \"Read the title page\".");
		sentences.add("6 Intention: Read the title page.");
		sentences.add("7 The end.");
		sentences.add("8 ");
		sentences.add("9 If the intention is \"Read the title page\".");
		sentences.add("10 Method: Assume successful.");
		sentences.add("11 The end.");
		sentences.add("12");
		List<String> strings = new ArrayList<>();
		strings.add("If the intention is \"Skim the book\".///");
		strings.add("Step: Read the title page.///");
		strings.add("The end.///");
		strings.add("///");
		Z.printList(addAfterLine(sentences,3,strings));
	}
	
	public static List<String> addAfterLine(List<String> fileContent, int i, List<String> strings){
		
		// copy the last few lines as the extra lines 
		int size = fileContent.size();
		Z.printList(fileContent);
		for(int j=strings.size();j>0;j--) {
			fileContent.add(fileContent.get(size-j));
		}
		
		// move down the lines between
		Z.printList(fileContent);
		for(int j = size-1;j>i+1;j--) {
			fileContent.set(j, fileContent.get(j-strings.size()));
			Z.printList(fileContent);
		}
		
		// add in the new lines
		for(int j = 0; j< strings.size();j++) {
			fileContent.set(i+1+j, strings.get(j));
		}
        
        return fileContent;
	}
	
	public static List<String> addAfterLine(List<String> fileContent, int i, String string){
		fileContent.add(fileContent.get(fileContent.size()-1));
		for(int j = fileContent.size()-1;j>i+1;j--) {
			fileContent.set(j, fileContent.get(j-1));
		}
        fileContent.set(i+1, string);
        return fileContent;
	}
	
	public static void testStory2Sentences() {
		List<String> strings = new ArrayList<>();
		strings.add("Read the title page. Read the preface if the book has it. Study the table of contents to obtain a general sense of the book’s structure. Check the index to determine the important terms. Read the blurb for an accurate summary of the book. Look at the chapters that seem pivotal to its argument. Finally, turn the pages to look for signs of the main contention.\n");
		Z.printList(story2Sentences(strings));
	}
	
	public static List<String> story2Sentences(List<String> strings){
		List<String> sentences = new ArrayList<>();
		for(String string : strings) {
			sentences.addAll(story2Sentences(string));
		}
		return sentences;
	}
		
	public static List<String> story2Sentences(String string){
		String terminators = "\\?|\n|\\.";
		List<String> sentences = new ArrayList<>();
		List<String> newSentences = new ArrayList<>();
		int ending = 0;
		
		if(terminators.indexOf(string.charAt(string.length()-1))<0) string += ".";
		sentences =  new ArrayList<String>(Arrays.asList(string.split(terminators)));
		
		for(String sentence: sentences) {
			ending += sentence.length();
			if(sentence.startsWith(" ")) sentence = sentence.substring(1);
			newSentences.add(sentence + Character.toString(string.charAt(ending++)));
		}
		return newSentences;
	}
	
	public static void testGetPrettyTree() {
		List<String> sentences = new ArrayList<String>();
//		sentences.add("Put the dinner plate on the bottom center of the table cloth");
//		sentences.add("I give you a cup of coffee");
//		sentences.add("place the salad knife to the right of the dinner knife, its blade facing left");
//		sentences.add("put the dinner fork on the left side of the charger");
		sentences.add("place the dinner knife on the right hand side of the dinner plate");
		for(String string : sentences) {
			Mark.say(getPrettyTree(string));
		}
		
	}
	
	public static String bb(int level, int leftMargin) {
		return bb(level, leftMargin, " ");
	}
	
	public static String bb(int level, int leftMargin, String space) {
		String result = "";
		int indent = 4;
		for(int i=0;i<level*indent+leftMargin;i++) {
			result+=space;
		}
		return result;
	}
	
	public static String getPrettyTree(String string) {
		return getPrettyTree(string, 0);
	}
	
	public static String getPrettyTree(String string, int leftMargin) {
		
		Boolean debug = false;
		string = string.replace(", its", ", with its");
		Map<String, String> replace = new HashMap<>();
		String of = "(fun of object owner)";
		String with = "(fun with object)";
		String end = " ";
		
		// Step 1:   translate
		String result = "";
		if(!string.startsWith("(")) {
			Entity entity = t.translate(string);
			Z.understand(debug, entity);
			string = entity.toString();
			
			// get "with"
			Map<String, Entity> roles = Z.getRoleEntities(entity);
			for (String role : roles.keySet()) {
				Entity roleClause = roles.get(role);
				if(role.equals(Markers.WITH_MARKER)) {
					Z.understand(debug, roleClause);
					if(roleClause.hasProperty(Markers.CLAUSES)) {
						end += roleClause.getProperty(Markers.CLAUSES).toString();
					}
					replace.put(with.replace("object", roleClause.toString()), "");
				}
				Mark.night(debug, role, roles.get(role));
			}
			
			for(Entity entity1: entity.getAllFramesDeep()) {
				// get "side"
				if(entity1.getType().equals("side")) {
					if(entity1.hasFeature("left")) {
						replace.put("(fun side", "(fun left_side");
					}
					else if(entity1.hasFeature("right")) {
						replace.put("(fun side", "(fun right_side");
					}
				}
				// get "hand side"
				if(entity1.getType().equals("hand_side")) {
					if(entity1.hasFeature("left")) {
						replace.put("(ent hand_side", "(ent left_hand_side");
					}
					else if(entity1.hasFeature("right")) {
						replace.put("(ent hand_side", "(ent right_hand_side");
					}
				}
			}
			
			
			// get owners of nouns
			List<Entity> ents = Z.getNounEntities(entity);
			for (Entity ent : ents) {
				if(ent.hasProperty(Markers.OWNER_MARKER)) {
					String owner = ent.getProperty(Markers.OWNER_MARKER).toString();
					String object = ent.toString();
					replace.put(object, of.replace("object", object).replace("owner", owner));
					Z.understand(debug, ent);
				}
			}
		}
		
		
		// Step 2:   trim
		Boolean ifShowFirstLayer = false;
		if(!ifShowFirstLayer) {
			string = string.replace("(seq semantic-interpretation ", "");
			string = string.substring(0, string.length() - 1);
		}
		string += end;
		for (String name : replace.keySet())  {
			string = string.replace(name, replace.get(name));
		}
		string = string.replaceAll("[0-9]", "").replace("-", "");
		
		
		// Step 3:   format
		int level = -1;
		int back = 0;
		List<String> items = Arrays.asList(string.substring(1).split("\\("));
		for (String item: items) {
			level = level - back + 1;
			if(item.startsWith("ent")) {
				item = "("+item.substring(4);
			} else if (item.startsWith("seq")){
				item = "(";
			} else {
				item = item.substring(4).toUpperCase() + "(";
			}
			result += bb(level,leftMargin)+item + "\n";
			back = item.length() - item.replace(")", "").length();
		}
		return result;
	}
	
	public static void testGetInnerese() {
//		Mark.say(getInnerese("I give you a cup of coffee that I like"));
//		Mark.say(getInnerese("I give you a cup of good coffee"));
		Mark.say(getInnerese("I give you my cup of coffee"));
	}
	
	public static String getInnerese(String string) {
		return getInnerese(string, 0);
	}
	
	public static String getInnerese(String string, int leftMargin) {
		
		// translate
		String result = "\n";
		if(!string.startsWith("(")) {
			Entity entity = t.translate(string);
			string = entity.toString();
			
			// trim()
			Boolean ifShowFirstLayer = false;
			if(!ifShowFirstLayer) {
				string = string.replace("(seq semantic-interpretation ", "");
				string = string.substring(0, string.length() - 1);
			}
			
			List<Entity> nouns = getNounEntities(entity);
			
			int level = -1;
			int back = 0;
			List<String> items = Arrays.asList(string.substring(1).split("\\("));
			for (String item: items) {
				level = level - back + 1;
				if(item.startsWith("ent")) {
					for(Entity noun:nouns) {
						if(noun.getType().equals(item.substring(4, item.indexOf("-")))) {
//							understand(noun);
							if(noun.hasProperty(Markers.CLAUSES)) {
								List<Entity> clauses = (List<Entity>) noun.getProperty(Markers.CLAUSES);
								item += "[ clause: ";
								for(Entity clause:clauses) {
									item += g.generate(clauses.get(0)).replace(".", "").replace(noun.getType(), "") + " ";
								}
								item += "]";
							}
							if(noun.hasProperty(Markers.FEATURE)) {
								item += "[ feature: "+noun.getProperty(Markers.FEATURE)+" ]";
							}
							if(noun.hasProperty(Markers.OWNER_MARKER)) {
								String owner = noun.getProperty(Markers.OWNER_MARKER).toString();
								if(!item.contains(owner)) {
									item += "[ owner: "+owner.substring(owner.indexOf(" "), owner.indexOf("-"))+" ]";
								}
							}
						}
					}
				} else if (!item.startsWith("seq")) {
					item = item.substring(0,4) + item.substring(4).toUpperCase();
				}
				result += bb(level,leftMargin, ".")+"("+item + "\n";
				back = item.length() - item.replace(")", "").length();
			}
		} 
		return result;
	}
	
	public static String toPrettyFormat(String jsonString) {
	      JsonParser parser = new JsonParser();
	      JsonObject json = parser.parse(jsonString).getAsJsonObject();

	      Gson gson = new GsonBuilder().setPrettyPrinting().create();
	      String prettyJson = gson.toJson(json);

	      return prettyJson;
	  }
	
	public static void testMarkSay() {
		Mark.show("If you want to see some colors in debugging logs:"
				+ "\n     Step 1: Git pull Genesis master"
				+ "\n     Step 2: Go to https://marketplace.eclipse.org/content/ansi-escape-console"
				+ "\n             or Search \'ansi escape eclipse\' to download the plugin");
		Mark.say("Mark.say()");
		Mark.red("Mark.red()");
		Mark.green("Mark.green()");
		Mark.yellow("Mark.yellow()");
		Mark.blue("Mark.blue()");
		Mark.purple("Mark.purple()");
		Mark.show("Mark.show()");
		Mark.night("Mark.night()");
		Mark.mit("Mark.mit()");
	}
	
	public static void testConsoleColor() {
		
		
        System.out.println("\033[0m BLACK");
        System.out.println("\033[31m RED");
        System.out.println("\033[32m GREEN");
        System.out.println("\033[33m YELLOW");
        System.out.println("\033[34m BLUE");
        System.out.println("\033[35m MAGENTA");
        System.out.println("\033[36m CYAN");
        System.out.println("\033[37m WHITE");
//        System.out.println("\033[0m ANSI_RESET");
        System.out.println("\033[40m BLACK");
        System.out.println("\033[41m RED");
        System.out.println("\033[42m GREEN");
        System.out.println("\033[43m YELLOW");
        System.out.println("\033[44m BLUE");
        System.out.println("\033[45m MAGENTA");
        System.out.println("\033[46m CYAN");
        System.out.println("\033[47m WHITE");
        System.out.println("\033[0m ANSI_RESET");
        Mark.say("testing");
        Mark.show("testing");
	}
	
	public static void testPrintThread() {
		Entity A = t.translate("hide");
		Entity B = t.translate("travel");
		Entity C = t.translate("run");
		printThread(A);
		printThread(B);
		printThread(C);
		
		Mark.say(StandardMatcher.getBasicMatcher().match(B, A));
		Mark.say(StandardMatcher.getBasicMatcher().match(A, B));
		Mark.say(StandardMatcher.getBasicMatcher().match(B, C));
		Mark.say(StandardMatcher.getBasicMatcher().match(C, B));
		Mark.say(StandardMatcher.getBasicMatcher().match(C, A));
		Mark.say(StandardMatcher.getBasicMatcher().match(A, C));
	}
	
	public static String printThread(String verb) {
		Entity entity = new Entity();
		if(!isTranslatable(verb)) {
			entity = new Entity(verb); // it may be a noun instead
		} else {
			entity = t.translate(verb).getElement(0);
		}
		return printThread(entity);
	}
	
	public static String printThread(Entity entity) {
		String cue = Z.ACTION;
		if(entity.getType().equals(Markers.SEMANTIC_INTERPRETATION)) {
			entity = entity.getElement(0);
		}
		if(isEnt(entity)) {
			cue = Markers.THING_WORD;
		}
		String toPrint = entity.getThread(cue).toString().replace("\n<thread>", "").replace("</thread>", "");
		Mark.say(toPrint);
		return toPrint;
	}
	
	public static void testSubtitutor() {
		Entity entity = t.translate("I love Mary");
		t.substitute(new Entity("Bob"), "Mary", entity);
		Mark.say(g.generate(entity));
	}
	
	public static void testVerbMatching() {
		
		Entity run = t.translate("run").getElement(0);
		Entity travel = t.translate("travel").getElement(0);
		Mark.say(run.getTypes());
		Mark.say(travel.getTypes());
		Mark.say(StandardMatcher.getBasicMatcher().match(run, travel));
		Mark.say(StandardMatcher.getBasicMatcher().match(travel, run));
		
		t.internalize("Tom is a person");
		Entity A = Translator.getTranslator().translate("Tom runs to a rock");
		Entity B = Translator.getTranslator().translate("Tom travels to a rock");
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(A, B);
		Mark.say("A:", A);
		Mark.say("B:", B);
		Mark.say("Bindings:", bindings);
		Mark.say("Bindings:", StandardMatcher.getBasicMatcher().match(B, A));
	}
	
	public static void testMatcher() {
		t.internalize("Tom is a person");
		t.internalize("Jerry is a person");
		t.internalize("Alice is a person");
		t.internalize("Bob is a person");
		t.internalize("xx is a person");
		t.internalize("kk is an entity");
		t.internalize("yy is an entity");

		String string1 = "Tom runs to Jerry";
		String string2 = "Alice retreated to Bob.";
		Entity entity1 = t.translate(string1).getElement(0);
		Entity entity2 = t.translate(string2).getElement(0);
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(entity1, entity2);
		Mark.say("Bindings: ", bindings);
		bindings = StandardMatcher.getBasicMatcher().match(entity2, entity1);
		Mark.say("Bindings: ", bindings);
		
		String ruleStr = "If xx forgets to take yy from kk, then kk has yy's contact.";
		String eventStr = "Alice forgot to take her phone from her pants.";
		Entity rule = t.translate(ruleStr).getElement(0);
		Entity antecedent = rule.getSubject().getElements().get(0);
		Entity consequent = rule.getObject();
		Entity event = t.translate(eventStr).getElement(0);
		
		LList<PairOfEntities> bindings2 = StandardMatcher.getBasicMatcher().match(antecedent, event);
		Mark.say("Bindings: ", bindings2);
	}

	public static void testNewsDemo() {

		// 180917 News demo
		String string = "Sarah was doing rescue breathing.";
		t.internalize("rescue breathing is an entity");
		printInnereseTree(t.translate(string));
	}


	// ====================================================================================
	//
	//   Basics: Read Innerese
	//
	// ====================================================================================

	// ==========================================
	//  print Innerese in a beautifully indented form
	// ==========================================
	public static void printInnereseTree(String sentence) {
		Entity innerese = Translator.getTranslator().translate(sentence);
		printInnereseTree(innerese);
	}
	
	public static void printInnereseTree(Entity entity, Boolean ifShowFirstLayer){
		Boolean ifPrintXML = false;

		String InnereseString = entity.toString();
		// if to delete the outer layer of "semantic-interpretation"
		if(!ifShowFirstLayer) {
			InnereseString = InnereseString.replace("(seq semantic-interpretation ", "");
			InnereseString = InnereseString.substring(0, InnereseString.length() - 1);
			Mark.say(InnereseString);
		}

		// temp file for making tree
		String InnereseFile = "students/zhutianYang/Innerese.txt";
		PrintWriter out;
		try {
			out = new PrintWriter(InnereseFile);
			out.println(InnereseString);
			out.close();

			// read tree and print out
			TreeFactory tf = new LabeledScoredTreeFactory();
		    Reader r = new BufferedReader(new FileReader(InnereseFile));
		    TreeReader tr = new PennTreeReader(r, tf);
		    Tree tt;
			try {
				tt = tr.readTree();
				Mark.say(DEBUG, tt.toString());
			    if(DEBUG) tt.pennPrint();

			    // print the XML sequence of all bundles
			    if (ifPrintXML) Mark.say(DEBUG, entity.toXML());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

	public static void printInnereseTree(Entity entity) {
		Boolean ifShowFirstLayer = true;
		printInnereseTree(entity, ifShowFirstLayer);
	}

	public static void printSubTrees(Tree t) {
        if (t.isLeaf())
            return;
        System.out.println(t);
        for (Tree subTree : t.children()) {
            printSubTrees(subTree);
        }
    }


	// ==========================================
	//  print out all the Innerese information of an entity
	// ==========================================
	public static void understand(Boolean debug, Object object) {
		if(debug) {
			understand(object);
		}
	}
	
	public static void understand(Object object) {
		if(object.toString().startsWith("(")) {
			understandEntity((Entity) object);
		} else {
			understandEnglish((String) object);
		}
	}

	public static void understandEntity(Entity entity) {
		boolean debug = true;
		if(getName(entity).equals(Markers.SEMANTIC_INTERPRETATION)) {
			for(Entity entity1:entity.getElements()) {
				understandEntity(entity1);
			}
		} else {
//			entity = entity.getElement(0);
			Mark.say(debug, "  Entity:", entity);
			Mark.say(debug, "     Subject:", entity.getSubject());
			Mark.say(debug, "     Object:", entity.getObject());
			Mark.say(debug, "     Features:", entity.getFeatures());
			Mark.say(debug, "     Properties", entity.getPropertyList().toString());
			Mark.say(debug, "     Types", entity.getTypes().toString());
			Mark.say(debug, "     Thread", entity.getPrimedThread());
			// Mark.say(debug, " Elements", entity.getElements().toString());
		}

	}

	public static void understandEnglish(String sentence) {
		Mark.say("Sentence:", sentence);
		Entity entity = Translator.getTranslator().translate(sentence);
		for(Entity entity1:entity.getElements()) {
			understandEntity(entity1);
		}
	}

	public static void understandEnglishDeep(String sentence) {
		Generator gn = Generator.getGenerator();
		Mark.say("Sentence:", sentence);
		Entity entity = Translator.getTranslator().translate(sentence).getElement(0);
		Mark.say("  Entity:", entity);
		Mark.say("  Features:", entity.getFeatures());
		Mark.say("  Properties", entity.getPropertyList().toString());
		Mark.say("  Types", entity.getTypes().toString());
		Mark.say("  Elements", entity.getElements().toString());

		List<Entity> entities = new ArrayList<>(entity.getChildren());
		List<Entity> newEntities = new ArrayList<>();
		for(Entity entity1: entities) {
			if(!isEnt(entity1)) {
				newEntities.add(entity1);
			}
		}
		Mark.say("  Children", newEntities.toString());
		if(entity.hasProperty(Markers.IMPERATIVE)) {
			Mark.say("         Object:", gn.generate(entity));
			Mark.say("         Action:", entity.getSubject());
		}
	}


	// ==========================================
	//  get information in Innerese
	// ==========================================
	// return the name of the entity
	public static String entity2Name(Entity entity) {
//		Mark.say(entity);
		String string = entity.toString();
		if (string.startsWith("(ent")) {
			return string.substring(5,string.indexOf("-"));
		} else if (string.equals("(seq roles)")) {
			return Markers.ROLE_MARKER;
		} else {
			return string.substring(5,string.indexOf(" ("));
		}
		
//		return string.substring(string.indexOf("(ent ")+5,string.indexOf("-"));
	}

	// return the name of the entity
	public static String getName(Entity entity) {
		List<String> types = entity.getTypes();
		if(types.get(types.size()-1).equals(Markers.NAME)) {
			return types.get(types.size()-2);
		} else {
			return types.get(types.size()-1);
		}
	}
	
	// return the name of the entity
	public static String getWholeName(Entity entity) {
		List<String> types = entity.getTypes();
		String feature = "";
		if(!entity.getFeatures().isEmpty())
			feature = entity.getSubject().getFeatures().toString().replaceAll("[\\[,\\]]", "") + " ";
		if(types.get(types.size()-1).equals(Markers.NAME)) {
			return feature + types.get(types.size()-2);
		} else {
			return feature + types.get(types.size()-1);
		}
	}

	// find the verb in tripes
	public static String getVerbInTriples(String triples) {
		String[] temps = triples.split("]");
		String verb = "";
		String subject = temps[0];
		subject = subject.substring(subject.indexOf("["),subject.indexOf(" "));
		for(String temp: temps) {
			String[] elements = temp.split(" ");
			if(elements[0].contains("+") && containsNumber(elements[0])
					&& elements[1].contains("+") && containsNumber(elements[1])
					&& elements[0].equals(subject)) {
				if(verb.equals("")) {
					verb = elements[1];
				} else if(!temp.contains(verb)&&verb.contains("make")) {
					verb = elements[1];
				}
			}
		}
		return verb;
	}

	// discard (seq semantic-interpretation
	public static Entity getRel(Entity entity) {
		if(isSeq(entity)) {
			entity = entity.getElement(0);
		}
		return entity;
	}

	// see if a sentence is positive or negative
	public static String getTone(String sentence) {
		sentence = sentence.toLowerCase();
		if(sentence.startsWith("yes")) {
			return YES;
		} else if (sentence.startsWith("none")) {
			return NONE;
		} else if (sentence.startsWith("no")) {
			return NO;
		} else if (sentence.startsWith("skip")) {
			return SKIP;
		} else if (sentence.contains("i don't know")) {
			// entityAnswer.isEqual(t.translate("I don't know").getElement(0))
			return DONT_KNOW;
		}
		return NORMAL;
	}


	// ==========================================
	//  get all entities
	// ==========================================
	public static void testGetEntities() {
		List<String> steps = new ArrayList<>();
		steps.add("you need to pour sauce from the cup to the bowl");
		steps.add("pour sauce from the cup to the bowl");
		steps.add("find a bowl");
		steps.add("she is putting fruits in the bowl");
		steps.add("Mary give John a show");
		for(String step: steps) {
			Mark.say("\n\n-----------------------",step);
			Mark.say(getNounNames(step));
			Mark.say(getNounEntities(t.translate(step).getElement(0)));
		}
	}

	public static List<String> getNounNames(String sentence){
		Entity entity = getTranslator().translate(sentence).getElement(0);
		return getNounNames(entity);
	}

	public static List<String> getNounNames(Entity entity) {
		nounNames = new ArrayList<>();
		getChildrenName(entity);
		return nounNames;
	}

	public static void getChildrenName(Entity entity){
		if(isSeq(entity)) {
			for(Entity entity1:entity.getElements()){
				getChildrenName(entity1);
			}
		} else if (isRel(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenName(entity1);
			}
		} else if (isFun(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenName(entity1);
			}
		} else {
			nounNames.add(entity2Name(entity));
		}
	}
	
	public static void printAllNounEntities(Entity ent2) {
		for (Entity ent: Z.getNounEntities(ent2)) {
			Mark.say("\n\n");
			Mark.show(ent.getType());
			Z.understand(ent);
		}
	}
	
	public static List<Entity> getNounEntities(Entity entity) {
		nounEntities = new ArrayList<>();
		getChildrenEntity(entity);
		return nounEntities;
	}
	
	public static List<String> getNamesFromEntities(List<Entity> nounEntities){
		List<String> names = new ArrayList<>();
		for(Entity nounEntity: nounEntities) {
			names.add(getName(nounEntity));
		}
		return names;
	}

	public static void getChildrenEntity(Entity entity){
		if(isSeq(entity)) {
			for(Entity entity1:entity.getElements()){
				getChildrenEntity(entity1);
			}
		} else if (isRel(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenEntity(entity1);
			}
		} else if (isFun(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenEntity(entity1);
			}
		} else {
			nounEntities.add(entity);
		}
	}
	
	public static void testGetNounEntityOwner() {
		Mark.say(getNounEntityOwner("Pare an orange peel and express its oil to the old fashioned."));
	}
	
	public static Map<Entity, Entity> getNounEntityOwner(String string) {
		nounEntityOwner = new HashMap<>();
		Entity entity = t.translate(string);
		for(Entity entity1: entity.getElements()) {
			getChildrenEntityOwner(entity1);
		}
		return nounEntityOwner;
	}
	
	public static Map<Entity, Entity> getNounEntityOwner(Entity entity) {
		nounEntityOwner = new HashMap<>();
		getChildrenEntityOwner(entity);
		return nounEntityOwner;
	}

	public static void getChildrenEntityOwner(Entity entity){
		if(isSeq(entity)) {
			for(Entity entity1:entity.getElements()){
				getChildrenEntityOwner(entity1);
			}
		} else if (isRel(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenEntityOwner(entity1);
			}
		} else if (isFun(entity)) {
			for(Entity entity1:entity.getChildren()){
				getChildrenEntityOwner(entity1);
			}
		} else {
			if(entity.hasProperty(Markers.OWNER_MARKER)) {
				nounEntityOwner.put(entity, (Entity) entity.getProperty(Markers.OWNER_MARKER));
			}
		}
	}
	
	public static void testEntity2String() {
		List<String> strings = new ArrayList<>();
		strings.add("it is a late-summer Saturday");
		for(String string: strings) {
			Entity entity = t.translate(string);
			List<Entity> nounEntities = getNounEntities(entity);
			for(Entity nounEntity:nounEntities) {
				Mark.say(entity2String(nounEntity));
			}
		}
	}
	
	// get name and features of an entity
	public static String entity2String(Entity entity) {
		Boolean done = false;
		String string = Z.getName(entity);
		
		if(entity.hasProperty(Markers.OWNER_MARKER)) {
			done = true;
			Entity owner = (Entity) entity.getProperty(Markers.OWNER_MARKER);
			
			// "two drops of bitters"
			if(isInstance(entity, Z.MEASURE)) {
				string = owner.getType() + "["+string+"]";
				
			// "his attention is drawn to"
			} else {
				string = string + "["+owner.getType()+"]";
			}
		}
		
		if(!done && entity.getFeatures().size()>0) {
			string += entity.getFeatures().toString();
		}
		
		if(entity.hasProperty(Markers.QUANTITY)) {
			string += entity.getProperty(Markers.QUANTITY).toString();
			string = string.replace("]", " ") + "]";
		}
		return string;
	}
	
//	// make ownership explicit  "an orange peel" = peel of orange
//	public static List<RGoal> getExplicitOwner(Entity entity) {
//		List<RGoal> goals = new ArrayList<>();
//		Map<Entity, Entity> owners = Z.getNounEntityOwner(entity);
//		if(owners.size()>0) {
//			for(Entity owner: owners.keySet()) {
//				goals.add(new RGoal(getName(owner),getName(owners.get(owner)),Z.OF,true));
//			}
//		}
//		return goals;
//	}


	// ==========================================
	//  get all names of roles (object, in, on)
	// ==========================================
	public static void testGetRoles() {
		List<String> steps = new ArrayList<>();
//		steps.add("you need to pour sauce from the cup to the bowl");
//		steps.add("pour sauce from the cup to the bowl");
//		steps.add("find a bowl");
//		steps.add("put fruits in the bowl");
//		steps.add("add sauces on top of the table");
//		steps.add("pour the milk out of the box");
		steps.add("Toss the blueberries into the bowl");
		for(String step: steps) {
			Mark.say(getRoles(step));
		}
	}

	public static Map<String, String> getRoles(String sentence){
		Map<String, String> roles = new HashMap<>();
		Entity entity;
		try {
			entity = t.translate(sentence).getElement(0);
		} catch (ArrayIndexOutOfBoundsException e) {
			entity = new Entity();
			Mark.say("Cannot get roles because cannot translate(sentence)");
		}

		return getRoles(entity);
	}

	public static Map<String, String> getRoles(Entity entity){
		Map<String, String> roles = new HashMap<>();
//				roles.put("subject", getName(entity.getSubject()));
		String entityStr = entity.toString();
		if(entityStr.length() - entityStr.replace("(seq", "").length() > 5) entity = entity.getObject();

		entity = clearEntity(entity);
		if (entity instanceof Relation) {
			
			if(entity.getSubject() instanceof Relation) {
				roles.put(getName(entity), getName(entity.getObject()));
				entity = entity.getSubject();
				roles.put(Z.ACTION, getName(entity));
				if(Z.countCharInString(entity.getObject().toString(), "(ent")>0) {
					roles.put(Markers.OBJECT_MARKER, getName(entity.getObject().getElement(0).getSubject()));
				}
				return roles;
			} else {
				roles.put(Z.ACTION, getName(entity));
				entity = entity.getObject();
			}
		}
		for(Entity entity1:entity.getElements()) {
			String toPut = "";
			if(!entity1.getSubject().getFeatures().isEmpty())
				toPut += entity1.getSubject().getFeatures().toString().replaceAll("[\\[,\\]]", "") + " ";
			toPut += getName(entity1.getSubject());
			roles.put(getName(entity1),toPut);
		}
//		Mark.mit(roles);
		return roles;
	}

	public static void testGetRoleEntities() {
		List<String> steps = new ArrayList<>();
//		steps.add("you need to pour sauce from the lower cup to the bowl");
//		steps.add("pour sauce from the cup to the bowl");
		steps.add("find a lage bowl");
//		steps.add("put fruits in the bowl");
//		steps.add("add spicy sauces on top of the table");
//		steps.add("pour the saturated milk out of the box");
		for(String step: steps) {
			Mark.night(step);
			Map <String, Entity> roles = getRoleEntities(step);
			Mark.say("\n\n"+getRoles(step));
			for(String role: roles.keySet()) {
				System.out.println(role + " = "+ getName(roles.get(role))+roles.get(role).getFeatures().toString());
			}
		}
	}

	public static Map<String, Entity> getRoleEntities(String sentence){
		Entity entity;
		try {
			entity = t.translate(sentence);
			// ignore "somebody chopped the chopped fruit" in "toss the chopped fruits into the bowl"
			// ignore "then, do something"
			entity = ignorePassiveAndScene(entity).get(0);
			Mark.show(DEBUG, entity);
		} catch (ArrayIndexOutOfBoundsException e) {
			entity = new Entity();
			Mark.say("Cannot get roles because cannot translate(sentence)");
		}

		return getRoleEntities(entity);
	}

	public static Map<String, Entity> getRoleEntities(Entity entityy){
//		printInnereseTree(entity);
		Map<String, Entity> roles = new HashMap<>();
		
		if (entityy.getElements().size() == 0) {
			Entity entity = new Sequence(Markers.ROLE_MARKER);
			entity.addElement(new Function(Markers.OBJECT_MARKER,entityy));
			entityy = entity;
		}

		for(Entity entity:entityy.getElements()) {

			roles.put("subject", entity.getSubject());
			String entityStr = entity.toString();
			if(CHANGELESS.contains(entity.getType())) {
				roles.put(Z.MENTAL, entity);
			}
			if(entityStr.startsWith("(seq roles")) {
				entity = entity.getObject();
			}

			entity = clearEntity(entity);
			if (entity instanceof Relation) {
				
				if(entity.getSubject() instanceof Relation) {
					roles.put(getName(entity), entity.getObject());
					entity = entity.getSubject();
					roles.put(Z.ACTION, entity);
					if(Z.countCharInString(entity.getObject().toString(), "(ent")>0) {
//						roles.put(Markers.OBJECT_MARKER, entity.getObject().getElement(0).getSubject());
					}
					return roles;
				} else {
					roles.put(Z.ACTION, entity);
					entity = entity.getObject();
				}
			}
			for(Entity entity1:entity.getElements()) {
//				understand(entity1);
//				String toPut = "";
//				if(!entity1.getSubject().getFeatures().isEmpty())
//					toPut += entity1.getSubject().getFeatures().toString().replaceAll("[\\[,\\]]", "") + " ";
//				toPut += getName();
				roles.put(getName(entity1),entity1.getSubject());
			}
//			for(String key:roles.keySet()) {
//				Mark.mit(key, ": ",roles.get(key).getType().toUpperCase());
//			}
		}
		
		return roles;
	}
	
	public static String getRepairSuggestions(String string) {
		String results = "";
		
		Map<String, String> heuristics = new HashMap<String, String>();
		String say = "Try getting rid of \"///\"";
		String z = "///";
		heuristics.put("or", say.replace(z, "or"));
		
		for(String key: heuristics.keySet()) {
			if(contains(string, key)) {
				results += heuristics.get(key) + "\n";
			}
		}
		
		String[] words = string.split(" ");
		for(String word: words) {
			if(isInstance(word, Markers.FEATURE) && !word.equals(Markers.BY)) {
				results += say.replace(z, word) + "\n";
			}
		}
		
		return results;
	}
	
	public static void testRepairSentence() {
		List<String> strings = new ArrayList<>();
		strings.add("If the book has a dust jacket, you look at the publisher’s blurb.");
		for(String string: strings) {
			Mark.say(repairSentence(string));
		}
	}
	
	public static List<String> repairSentence(String sentence) {
		return repairSentence(sentence, -1);
	}
	
	public static List<String> repairSentence(String sentence, int listIndex) {
		List<String> returns = new ArrayList<>();
		String headerRed = "<font color=\"#"+COLOR_RED+"\"><b>";
		String headerGreen = "<font color=\"#"+COLOR_GREEN+"\"><b>";
		String trailer = "</b></font>";
		
		// -------------------------------------   1  ------------
		// "xxx’s yyy" -> "yyy of xxx"
		// -------------------------------------------------------
		for(String prime: Arrays.asList("’s ", "\'s ")) { // two variations
			int indexPoss = sentence.indexOf(prime);
			if(indexPoss>0) {
				String before = "xxx"+prime+"yyy";
				String after = "yyy of xxx";
				String the = "";
				
				// find xxx
				String owner = sentence.substring(0, indexPoss);
				int indexBlank = owner.lastIndexOf(" ");
				if(indexBlank>0) {
					String maybeThe = owner.substring(0, indexBlank);
					owner = owner.substring(indexBlank+1, owner.length());
					if(maybeThe.endsWith("the")) {
						the = "the ";
					}
				}
				
				// find yyy
				Boolean find = true;
				String rest = sentence.substring(indexPoss+2, sentence.length());
				rest = stripPunctuation(rest);
				String object = "";
				String[] words = rest.split(" ");
				for(String word:words) {
					if(word.length()>1) {
						if(canBeNoun(word)) {
							if(object.length()==0) {
								object += word; 
							} else {
								object += word + " "; 
							}
						} else {
							break;
						}
					}
				}
				
				before = before.replace("xxx",owner).replace("yyy",object);
				returns.add(sentence.replace(before, 
						after.replace("xxx",the+owner).replace("yyy",object)));
				returns.add(sentence.replace(before, 
						headerGreen + after.replace("xxx",the+owner).replace("yyy",object)+ trailer));
				
				if(listIndex>-1) {
					PageHowToLearner.sentencesOriginalPrint.set(listIndex, 
							sentence.replace(before, headerRed + before + trailer));
				}
				
			}
		}
		
		return returns;
	}
	
	public static void testcanBeNoun() {
		List<String> strings = new ArrayList<>();
		strings.add("grand");
		strings.add("book");
		strings.add("me");
		strings.add("happiness");
		strings.add("happy");
		for(String string: strings) {
			Mark.say(string, canBeNoun(string)?"yes":"");
		}
	}
	
	// old fashioned martini
	public static Boolean canBeNoun(String word) {
		String thread = Z.getWordnetThread(word);
		return thread.contains(Markers.THING_WORD)?true:false;
	}
	
	public static void testRepairAdWord() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("In an Old-Fashioned glass, muddle the sugar cube and bitters.");
//		sentences.add("Garnish it with an orange twist if it is desired.");
//		sentences.add("Strain the cocktail into a rocks glass over one large ice cube.");
		sentences.add("Give me two drops of Angostura bitters.");
//		sentences.add("Put the bourbon and Angostura bitters in a mixing glass.");
		
//		sentences.add("This is a salad fork");
		
//		sentences.add("This is an annual summer party");
		for(String sentence: sentences) {
			for(Entity noun: getNounEntities(t.translate(sentence).getElement(0))) {
				Mark.mit(noun);
				understand(noun);
				Mark.say(g.generate(repairAdWord(noun)));
			}
		}
	}
	
	public static Entity repairAdWord(Entity entity) {
		Entity repaired = new Entity();
		Boolean repair = false;
		
//		Mark.purple(entity);
		String name = Z.getName(entity);
		if(entity.hasProperty(Markers.QUANTITY)) {
			name = entity.getProperty(Markers.QUANTITY) +"_"+ name;
		}
		if(entity.getFeatures().size()>0) {
			name = entity.getFeatures().toString().replace("[","").replace(", ","_").replace("]","_") 
					+ name;
		}
//		Mark.yellow(name);
		if(name.indexOf("_")>0) {
			List<String> adjs = new ArrayList<String>(Arrays.asList(name.split("_")));
			String object = adjs.get(adjs.size()-1);
			
			// "two drops of bitters"
			Boolean isMeasure = true;
			for(String adj: adjs) {
				if(!isInstance(adj,Z.MEASURE)) {
					isMeasure = false;
				}
			}
			if(isMeasure && entity.hasProperty(Markers.OWNER_MARKER)) {
				
				repaired = (Entity) entity.getProperty(Markers.OWNER_MARKER);
				repaired.addFeature(name);
				repair = true;
			
			// "sugar cube"
			} else if(getWordnetThread(object).contains(Z.SHAPE) && ! getWordnetThread(object).contains(Z.TOOL)) {
				
				repaired = new Entity(adjs.get(adjs.size()-2));
				repaired.addFeature(object);
				repair = true;
			
			// "Old-Fasioned glass"
			} else {
				adjs.remove(object);
				repaired = new Entity(object);
				for(String adj: adjs) {
					String wordn = getWordnetThread(adj);
					// "annual summer party", "warm late-summer Saturday"
					repaired.addFeature(adj);
					repair = true;
//					if(wordn.contains(Z.ADWORD) || wordn.contains(Z.MEASURE)) {
//						
//					}
				}
			}
			
			if (repair) {
				Mark.show("\n----",repaired, "\n----");
				understand(repaired);
				return repaired;
			}
		}
		return entity;
	}
	
	public static String getWordnetThread(String word) {
		String result = "";
		Bundle bundles = wordNet.lookup(word);
//		Mark.say(bundles);
		if (bundles.size() != 0) {
			for (Iterator i = bundles.iterator(); i.hasNext();) {
				frames.entities.Thread thread = (frames.entities.Thread) (i.next());
				for (Iterator j = thread.iterator(); j.hasNext();) {
					result += j.next() + " ";
				}
			}
		}
//		Mark.mit(result);
		return result;
	}
	
	public static List<String> getWordnetThreads(String word) {
		List<String> result = new ArrayList<>();
		Bundle bundles = wordNet.lookup(word);
//		Mark.say(bundles);
		if (bundles.size() != 0) {
			for (Iterator i = bundles.iterator(); i.hasNext();) {
				frames.entities.Thread thread = (frames.entities.Thread) (i.next());
				for (Iterator j = thread.iterator(); j.hasNext();) {
					result.add(j.next().toString());
				}
			}
		}
//		Mark.mit(result);
		return result;
	}
	
	public static List<List<String>> getWordnetAllThreads(String word) {
		Bundle bundles = wordNet.lookup(word);
		return getWordnetAllThreads(bundles);
	}
	
	public static List<List<String>> getWordnetAllThreads(Entity entity) {
		Bundle bundles = entity.getBundle();
		return getWordnetAllThreads(bundles);
	}
	
	public static List<List<String>> getWordnetAllThreads(Bundle bundles) {
		List<List<String>> results = new ArrayList<>();
//		Mark.say(bundles);
		if (bundles.size() != 0) {
			for (Iterator i = bundles.iterator(); i.hasNext();) {
				List<String> result = new ArrayList<>();
				frames.entities.Thread thread = (frames.entities.Thread) (i.next());
				for (Iterator j = thread.iterator(); j.hasNext();) {
					result.add(j.next().toString());
				}
				results.add(result);
			}
		}
//		Mark.mit(result);
		return results;
	}
	
	public static String getWordnetAllThreadsString(Entity entity) {
		Bundle bundles = entity.getBundle();
		String results = "";
		if (bundles.size() != 0) {
			for (Iterator i = bundles.iterator(); i.hasNext();) {
				List<String> result = new ArrayList<>();
				frames.entities.Thread thread = (frames.entities.Thread) (i.next());
				for (Iterator j = thread.iterator(); j.hasNext();) {
					result.add(j.next().toString());
				}
				results += result + " ";
			}
		}
		return results;
	}
	
	
	// get a initiated translator
	public static Translator getTranslator() {
		Translator t = Translator.getTranslator();
//			t.internalize("she is a thing");
//			t.internalize("he is a thing");
//			t.internalize("Alice is a thing");
//			t.internalize("Bob is a thing");
//			t.internalize("phone is a thing");
//			t.internalize("battery is a thing");
//			t.internalize("team member is a thing");
//			t.internalize("soccer team is a thing");

//			t.internalize("xx is a thing");
//			t.internalize("zz is a thing");
//			t.internalize("yy is a thing");
//			t.internalize("kk is a thing");
//			t.internalize("oo is a thing");
//			t.internalize("nn is a thing");

		return t;
	}

	// see if an English sentence can be translated into Innerese
	public static Boolean isTranslatable(String humanEnglish) {
		if(humanEnglish.toLowerCase().startsWith("skip")) return true;
		Boolean result = false;
		try {
			Entity human = Translator.getTranslator().translate(humanEnglish);
			Entity test = new Sequence();
			if(!human.isEqual(test)) result = true;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
//		Mark.say("[translatable = "+result.toString()+"]", humanEnglish);
		return result;
	}



	// ====================================================================================
	//
	//   Basic Operations 1/4: Semantic Classification
	//
	// ====================================================================================


	// ==========================================
	//  classify the entity as four kinds
	// ==========================================
	public static Boolean isSeq(Entity entity) {
		if(entity.toString().startsWith("(seq")) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isFun(Entity entity) {
		if(entity.toString().startsWith("(fun")) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isRel(Entity entity) {
		if(entity.toString().startsWith("(rel")) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isEnt(Entity entity) {
		if(entity.toString().startsWith("(ent")) {
			return true;
		} else {
			return false;
		}
	}


	// ==========================================
	//  see if a sentence has modal word "can"
	// ==========================================
	public static void testHasCan() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("You can make a living");
//		sentences.add("The engine can do wrong");
//		sentences.add("It can't be done in four months");
		sentences.add("could you answer my question?");
//		sentences.add("She could not believe in him.");
		for(String sentence: sentences) {
			Mark.say("-----",sentence);
			check(hasCan(sentence));
			check(hasCan(t.translate(sentence)));
		}
	}

	public static Boolean hasCan(String string) {
		// must use string matching because "could you ..." does not have <modal, can>
		List<String> words = Arrays.asList(string.split(" "));
		return words.contains("can") || words.contains("could")
				|| words.contains("cannot") || words.contains("can't")
				|| words.contains("couldn't");
	}

	public static Boolean hasCan(Entity entity) {
		if(isSeq(entity)) {
			entity = entity.getElement(0);
		}
//		understand(entity);
		if(entity.hasProperty(Markers.MODAL)) {
			if(entity.getProperty(Markers.MODAL).equals(Markers.CAN)) {
				return true;
			}
		}
		return false;
	}


	// ==========================================
	//  see if a sentence is imperative
	// ==========================================
	public static void testIsImperative() {
		List<String> sentences = new ArrayList<>();
		sentences.add("You are making the salad for me.");
		sentences.add("find a bowl.");
//		sentences.add("You can make a living");
//		sentences.add("Make a living");
//		sentences.add("Be strong");
//		sentences.add("Being strong is difficult");
		for(String sentence: sentences) {
			Mark.say("-----",sentence);
			check(isImperative(sentence));
			check(isImperative(t.translate(sentence)));
		}
	}

	public static Boolean isImperative(Entity entity) {
//		understand(getRel(entity));
		// if using getTriples(), "you can do sth" will also be categorized as imperatives
		if(getRel(entity).hasProperty(Markers.IMPERATIVE)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isImperative(String string) {
		Entity entity = t.translate(string).getElement(0);
		return isImperative(entity);
	}


	// ==========================================
	//  see if a sentence is of specific type (e.g. is how to question)
	// ==========================================
	public static Boolean hasType(String sentence, String bundle) {
		Entity entity = Translator.getTranslator().translate(sentence).getElement(0);
		if(entity.getTypes().contains(bundle)) {
			return true;
		}else {
			return false;
		}
	}

	public static Boolean hasType(Entity entity, String bundle) {
//		entity = entity.getElement(0);
		if(entity.getTypes().contains(bundle)) {
			return true;
		}else {
			return false;
		}
	}


	// ==========================================
	//  for Recipe Expert: story into maps of (condition + step)
	// ==========================================
	public static void testGetSentenceType() {
		List<String> sentences = new ArrayList<>();

		// problems
		sentences.add("can you make a fruit salad for me?");
		sentences.add("can you show me how to make a fruit salad?");
		sentences.add("make me a fruit salad");
		sentences.add("let's make me a fruit salad!");
		sentences.add("could you please make me a fruit salad!");

//		// steps
//		sentences.add("you need to find a bowl and put apples in it");
//		sentences.add("you can find a bowl and put apples in it");
//		sentences.add("you can try to find a bowl and put apples in it");
//		sentences.add("find a bowl and put apples in it");
//		sentences.add("He found a bowl and put apples in it");
//
//		// conditions
//		sentences.add("if you are rich, you need to buy flowers");
//		sentences.add("you follow these steps if you are making the salad for me");
//		sentences.add("when I come back, make me a cup of tea");
//		sentences.add("you don't do the second step if you are tired");
//		sentences.add("you also need to wash your hand if you poured sauces");

//		// recipe 1
//		sentences.add("you need to find a bowl and put apples in it");
//		sentences.add("if you are rich, you need to buy flowers");
//		Mark.say(getConditionSteps(sentences));
//
//		// recipe 2
//		sentences = new ArrayList<>();
//		sentences.add("you can find a bowl and put apples in it");
//		sentences.add("then, you need to find some bananas and put them into the bowl");
//		Mark.say(getConditionSteps(sentences));
//
//		// recipe 3
//		sentences = new ArrayList<>();
//		sentences.add("you need to find a bowl and put apples in it");
//		sentences.add("you follow these steps if you are making the salad for me");
		
		Mark.say(getConditionSteps(sentences));
	}

	public static Map<String, List<String>> getConditionSteps(List<String> strings) {
		// I have enough money: steps
		// always: steps
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Mark.say("==================");
		for(String string: strings) {
			String condition = CONDITION_ALWAYS;
			List<String> steps = new ArrayList<>();
			Mark.say("-------------\n\n",string);

			Entity entity = t.translate(string);
			List<String> types = Arrays.asList(entity.getElement(0).getBundle().getAllTypes());

			// ----------- for condition + steps
			if(types.contains(Markers.IF_MARKER) || types.contains(Markers.WHEN_QUESTION)) {
				List<String> returns = sentence2Condition(entity);
				condition = returns.get(0);
				steps.addAll(returns.subList(1, returns.size()));
				toReturn.put(condition, steps);

			// ----------- for only steps
			} else {
				// for steps
				for (Entity entity1: entity.getElements()) {
					if(!entity1.toString().startsWith("(ent scene")) {
						if(entity1.hasProperty(Markers.MODAL)) {
							if(entity1.hasProperty(Markers.GROUP)) {
								for(Entity entity2: (List<Entity>) entity1.getProperty(Markers.GROUP)) {
									steps.add(g.generate(entity2).replace(".", ""));
								}
								break;
							} else {
								steps.add(g.generate(entity1).replace(".", ""));
								break;
							}
						} else if(entity1.getTypes().contains(Markers.HAVE)) {
							entity1 = entity1.getObject();
						}
						steps.add(Z.sentence2Action(entity1));
					}
				}
				if(toReturn.containsKey(CONDITION_ALWAYS)) {
					List<String> conbinedSteps = new ArrayList<>();
					conbinedSteps.addAll(toReturn.get(CONDITION_ALWAYS));
					conbinedSteps.addAll(steps);
					toReturn.put(condition, conbinedSteps);
				}
			}
		}

		return toReturn;
	}
	
	public static void testClassify() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("what would you do if you want to be happy");
//		sentences.add("what would you do to be happy");
//		sentences.add("how to be happy");
//		sentences.add("can you tell me how to be happy");
//		sentences.add("what do you do to be happy");
//		sentences.add("tell me how you be happy");
//		sentences.add("if you want to pass exams, you should read more books");
//		
//		sentences.add("if the weather is good, go out");
//		sentences.add("if today is Sunday, you should go out");
//		sentences.add("if she likes it, you should go out");
//		sentences.add("if it is cloudy or windy, you should stay at home");
//		sentences.add("forget about the matter if you don't have time");
//		
//		sentences.add("go out and talk to people");
//		sentences.add("you should learn with your eyes too");
//		sentences.add("just learn with stories");
//		
//		sentences.add("show me");
//		sentences.add("can you show me how you do it");
		for(String sentence: sentences) {
			Mark.show(classify(sentence));
		}
	}
	
	public static Map<String, Object> classify(String string){
		Boolean debug = false;
		Map<String, Object> toReturn = new HashMap<String, Object>();
		List<String> steps = new ArrayList<>();
		Mark.say(debug,"-------------\n\n",string);

		Entity entity = t.translate(string);
		List<String> types = Arrays.asList(entity.getElement(0).getBundle().getAllTypes());

		for (Entity entity1: entity.getElements()) {
			
			if(debug) understand(entity1);
			Mark.night(debug,"          ");
			
			if(!entity1.toString().startsWith("(ent scene")) {
				
				// for condition steps
				if(stringIsCondition(types)) {
					
					// "if you want to be more"
					Entity cause = entity1.getSubject().getElement(0);
					Entity consequence = entity1.getObject();
					if(getName(cause).equals(Markers.WANT_MARKER)) {
						
						if(isRel(consequence)) {
							steps.add(g.generate(consequence).replace(".", ""));
						} else {
							for(Entity entity2: consequence.getElements()) {
								steps.add(g.generate(entity2).replace(".", ""));
							}
						}
						toReturn.put(Z.GOAL, g.generate(cause.getObject().getElement(0).getSubject()));
						
					} else {
						List<String> returns = sentence2Condition(entity);
						String condition = returns.get(0);
						steps.addAll(returns.subList(1, returns.size()));
						toReturn.put(Z.CONDITION, condition);
					}

				// for goals
				} else if(stringIsGoal(types)) {
					
					types = Arrays.asList(entity1.getSubject().getBundle().getAllTypes());
					
					if(types.contains(Markers.IF_MARKER) || types.contains(Markers.WHEN_QUESTION)) {
						
						String whatif = g.generate(entity1.getSubject().getSubject()).toLowerCase().replace(".", "");
						toReturn.put(WHATIF, whatif);
					
					// "can you show me how you did it"
					} else if(entity1.hasProperty(Markers.GROUP)) {
						
						List<Entity> ste = (List<Entity>) entity1.getProperty(Markers.GROUP);
						for(Entity entity2: ste.get(0).getChildren()) {
							String goal = g.generate(entity2);
							// in case some unwanted rel in Group such as "what"
							if(goal.indexOf(" ")>0) {
								toReturn.put(GOAL, goal);
							}
						}
					} else {
						toReturn.put(GOAL, question2Goal(entity1));
					}
				
				// for goals
				} else if(types.contains(Markers.MEANS)) {
					
					if(entity1.hasProperty(Markers.GROUP)) {
						List<Entity> ste = (List<Entity>) entity1.getProperty(Markers.GROUP);
						for(Entity entity2: ste.get(0).getChildren()) {
							String goal = g.generate(entity2);
							// in case some unwanted rel in Group such as "what"
							if(goal.indexOf(" ")>0) {
								toReturn.put(GOAL, goal);
							}
						}
					} else {
						steps.add(g.generate(entity1).replace(".", ""));
					}
					
				// for goals
				} else if(types.contains(Markers.WANT_MARKER)) {
					
					entity1 = entity1.getObject().getElement(0).getSubject();
					toReturn.put(GOAL, want2Goal(entity1));
					
					
				// for steps
				} else {
					steps = stringIsSteps(entity);
				}
			}
		}
		
		// eliminate the periods
		if (toReturn.containsKey(GOAL)) {
			String goal = toReturn.get(GOAL).toString().replace(".", "");
			if(goal.startsWith("Are")) {
				goal = goal.replace("Are", "Be");
			} 
			if(!goal.equalsIgnoreCase(Z.SHOWME)) {
				toReturn.put(GOAL, goal);
			} else {
				toReturn.put(EXECUTE, goal);
			}
		}
		if(steps.size()>0) {
			List<String> stepsNew = new ArrayList<>();
			for(int i=0; i<steps.size(); i++) {
				String step = steps.get(i);
				if(step.equalsIgnoreCase(Z.SHOWME)) {
					toReturn.put(EXECUTE, step);
				} else {
					stepsNew.add(step.replace(".", ""));
				}
			}
			if(story2Sentences(string).size()>1) {
				stepsNew.add("...");
			}
			toReturn.put(STEPS, stepsNew);
		}
		return toReturn;
	}


	// ==========================================
	//  for Recipe Expert: sentence to condition + step
	// ==========================================
	public static void testStringIsCondition() {
		List<String> steps = new ArrayList<>();
		steps.add("determine whether the author has solved his problems");
//		steps.add("you follow the steps if you are making the salad for me");
//		steps.add("you follow the steps if I want to have fun");
//		steps.add("if you are making the salad for me, you follow the steps");
//		steps.add("if you have money, you need to buy flowers and chocolates");
//		steps.add("if you have money, I need to buy flowers and chocolates");
//		steps.add("if you have money, you need to buy flowers and give chocolates");
//
//		steps.add("you follow the steps when you are making the salad for me");
//		steps.add("you follow the steps when I want to have fun");
//		steps.add("when you are making the salad for me, you follow the steps");
//		steps.add("when you have money, you need to buy flowers and chocolates");
//		steps.add("when you have money, I need to buy flowers and chocolates");
//		steps.add("when you have money, you need to buy flowers and give chocolates");
		for(String step: steps) {
			Mark.say("\n\n");
			Mark.say(stringIsCondition(step));
		}
	}

	// see if a sentence tells a condition
	public static Boolean stringIsCondition(List<String> types) {
		if(types.contains(Markers.IF_MARKER) || types.contains(Markers.WHEN_QUESTION)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static List<String> stringIsCondition(String sentence) {
		Entity entity = Translator.getTranslator().translate(sentence);
		List<String> types = Arrays.asList(entity.getElement(0).getBundle().getAllTypes());
		if(types.contains(Markers.IF_MARKER) || types.contains(Markers.WHEN_QUESTION)) {
			return sentence2Condition(entity);
		} else {
			return new ArrayList<>();
		}
	}

	public static void testSentenceToCondition() {
		List<String> strings = new ArrayList<>();
//		strings.add("you love jogging");
		strings.add("there is a bug");
		strings.add("there were a bug");
		strings.add("there will be bugs");
		for(String string:strings) {
			Mark.say("======== "+string);
			sentenceToCondition("you use the method when "+string);
			sentenceToCondition("when "+string+ ", you use the method.");
			sentenceToCondition("you use the method if "+string);
			sentenceToCondition("if "+string+ ", you use the method.");
		}
	}

	// output the condition and action of "do ... if ..." or "if ... do ..."
	public static List<String> sentenceToCondition(String sentence) {
		Entity innerese = Translator.getTranslator().translate(sentence);
		return sentence2Condition(innerese);
	}

	// output the condition and action of "do ... if ..." or "if ... do ..."
	public static List<String> sentence2Condition(Entity innerese) {
		List<String> toReturn = new ArrayList<>();
		String condition = "";
		List<String> steps = new ArrayList<>();
		
		for(Entity ifthen: innerese.getElements()) {
			List<String> types = new ArrayList<>(ifthen.getTypes());
			if(types.contains(Markers.IF_MARKER)) {

				// get condition
				Entity entIf = ifthen.getSubject().getElement(0);
				
				condition = g.generate(entIf).toLowerCase();
				if(entIf.getSubject().toString().contains(" you-")) {
					if(entIf.hasProperty(Markers.MODAL)) {
						if(entIf.getProperty(Markers.MODAL).equals(Markers.CAN)) {
							condition = "you can " + condition;
						} else {
							condition = "you " + condition;
						}
					} else {
						condition = "you " + condition;
					}
					
				}
				

				// get action
				Entity entThen = ifthen.getObject();
				if(entThen.getType().equals(Z.NEED)) {
					entThen = entThen.getObject();
					entThen = clearEntity(entThen);
				}
				steps.add(g.generate(entThen).toLowerCase().replace(".", ""));

			} else if (types.contains(Markers.WHEN_QUESTION)){

				// get condition
				Entity entWhen = ifthen.getObject();
				condition = Generator.getGenerator().generate(entWhen).toLowerCase();
				if(entWhen.getSubject().toString().contains(" you-")) {
					condition = "you " + condition;
				}

				// get action
				Entity entThen = ifthen.getSubject();
				steps.add(g.generate(entThen).toLowerCase().replace(".", ""));

			}
			condition = condition.replace(".", "");

		}

//		List<String> bes = Arrays.asList("is", "was", "are", "were");
//		for(String be:bes) {
//			if(condition.endsWith(be)) {
//				condition = condition.replace(" "+be, "");
//				condition = condition.replace("if", "if there "+be);
//			}
//		}
		toReturn.addAll(Arrays.asList(condition));
		toReturn.addAll(steps);
//		Mark.say("condition: ",condition);
//		Mark.say("steps: ",steps);
		return toReturn;
	}


	// ==========================================
	//  for Recipe Expert: sentence to steps
	// ==========================================
	public static void testStringIsSteps() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("you need to find a bowl");
//		sentences.add("you need to find a bowl and put apples in it");
//		sentences.add("you can find a bowl");
//		sentences.add("you can find a bowl and put apples in it");
//		sentences.add("you have to find a bowl");
//		sentences.add("you have to find a bowl and put apples in it");
//		sentences.add("you should find a bowl");
//		sentences.add("you should find a bowl and put apples in it");
//		sentences.add("you can try to find a bowl");
//		sentences.add("you can try to find a bowl and put apples in it");
//		sentences.add("find a bowl");
//		sentences.add("find a bowl and put apples in it");
//		sentences.add("He found a bowl and put apples in it");
		sentences.add("first, you skim the book");
		for (String sentence: sentences) {
			Mark.say(stringIsSteps(sentence));
		}
	}

	public static List<String> stringIsSteps(String string) {
		Mark.say("-------------\n\n",string);

		Entity entity = t.translate(string);
		return stringIsSteps(entity);
	}
	
	public static List<String> stringIsSteps(Entity entity) {
		List<String> steps = new ArrayList<>();
		for (Entity entity1: entity.getElements()) {
			if(entity1.hasProperty(Markers.MODAL)) {
				if(entity1.hasProperty(Markers.GROUP)) {
					for(Entity entity2: (List<Entity>) entity1.getProperty(Markers.GROUP)) {
						steps.add(g.generate(entity2).replace(".", ""));
					}
					break;
				} else {
					steps.add(g.generate(entity1).replace(".", ""));
					break;
				}
			} else if(entity1.getTypes().contains(Markers.HAVE)) {
				entity1 = entity1.getObject();
				steps.add(Z.sentence2Action(entity1));
			} else if (entity1.hasProperty(Markers.IMPERATIVE) || entity1.getTypes().contains(Markers.ACTION_MARKER)) {
				steps.add(Z.sentence2Action(entity1));
			}
		}
		return steps;
	}


	// ==========================================
	//  for Recipe Expert: see if a string suggests a goal, can be a question or a request
	// ==========================================
	public static void testStringIsGoal() {
		List<String> sentences = new ArrayList<>();
		sentences.add("I don't like food");
		sentences.add("I wasn't bad");
		sentences.add("I won't do that");
		sentences.add("I like food");
		sentences.add("I was good");
		sentences.add("I will do that");
		for(String sentence:sentences) {
			check(isNegative(sentence));
		}
	}
	
	public static Boolean stringIsGoal(String sentence) {
		Entity entity = Translator.getTranslator().translate(sentence).getElement(0);
		if (entity.getType().equals(Markers.SCENE)) {
			return false;
		}
		List<String> types = entity.getTypes();
		return stringIsGoal(types);
	}
	
	public static Boolean stringIsGoal(List<String> types) {
		if(types.contains(Markers.QUESTION) && types.contains(Markers.DID_QUESTION) || types.contains(Markers.HOW_QUESTION)) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean stringIsHowTo(Entity entity) {
		return Z.hasType(entity, Markers.HOW_QUESTION);
	}
	
	public static Boolean stringIsHowTo(String sentence) {
		return Z.hasType(sentence, Markers.HOW_QUESTION);
	}

	public static Boolean stringIsRequest(String sentence) {
		return stringIsRequest(t.translate(sentence).getElement(0));
	}
	
	public static Boolean stringIsRequest(Entity entity) {
		List<String> types = entity.getTypes();
		if(types.contains("question") && types.contains("did")) {
			return true;
		} else {
			return false;
		}
	}


	// ==========================================
	//  for Recipe Expert: see if a string refers to a story in database, read it if yes
	// ==========================================
	public static String stringIsStory(String sentence,List<String> storyNames) {
		for(String story : storyNames) {
			if(sentence.contains(story.toLowerCase())) {
				return story;
			}
		}
		return "";
	}


	// ==========================================
	//  for Bedtime story learner: see if a sentence can be just done
	// ==========================================
	public static Boolean isJustDoIt(Entity entity1) {
		if(entity1.getTypes().contains("like")
				|| entity1.getTypes().contains("dislike")
				|| entity1.getProperty("modal")=="can") {
			return true;
		} else {
			return false;
		}
	}


	// ==========================================
	// see if two sentenses are negative pairs
	// ==========================================
	public static void testIsNegation() {
//		check(isNegation("I don't like food", "I like go fishing"));
//		check(isNegation("I like go fishing", "I don't like food"));
//		check(isNegation("I don't like food", "I like food"));
		check(isNegation("I don't like going fishing", "I like going fishing"));
//		check(isNegation("you don't have a replacement battery", 
//				"you have a replacement battery"));
		
	}
	
	public static Boolean isNegation(String one, String two) {
		one = repairNegation(one);
		two = repairNegation(two);
		Boolean result = false;
		Translator t = Translator.getTranslator();
		Entity entityOne = t.translate(one).getElement(0);
		Entity entityTwo = t.translate(two).getElement(0);
		if(entityOne.hasFeature(Markers.NOT)) {
			entityOne.removeFeature(Markers.NOT);
			if(entityTwo.isDeepEqual(entityOne)) {
				result = true;
			}
		}
		if(entityTwo.hasFeature(Markers.NOT)) {
			entityTwo.removeFeature(Markers.NOT);
			if(entityOne.isDeepEqual(entityTwo)) {
				result = true;
			}
		}
		return result;
	}
	
	public static String repairNegation(String one) {
		return one.replace("don't", "do not").replace("didn't", "did not")
				.replace("isn't", "is not").replace("wasn't", "was not")
				.replace("aren't", "are not").replace("weren't", "were not")
				.replace("amn't", "am not").replace("can't", "can not");
	}
	
	public static void testIsNegative() {
		List<String> sentences = new ArrayList<>();
		sentences.add("I don't like food");
		sentences.add("I wasn't bad");
		sentences.add("I won't do that");
		sentences.add("I like food");
		sentences.add("I was good");
		sentences.add("I will do that");
		for(String sentence:sentences) {
			check(isNegative(sentence));
		}
	}
	
	public static Boolean isNegative(String string) {
		return isNegative(t.translate(string).getElement(0));
	}
	
	public static Boolean isNegative(Entity entity) {
		return entity.hasFeature(Markers.NOT);
	}


	// ====================================================================================
	//
	//   Basics Operations 2/4: Semantic Reformation
	//
	// ====================================================================================

	// yes to no
	public static String sentence2Negation(String sentence) {
		Entity entity = t.translate(sentence).getElement(0);
		String triples = g.generateTriples(entity);
		String verb = getVerbInTriples(triples);

		if(triples.contains(verb + " is_negative Yes")) {
			String[] temps = triples.split("]");
			String newTriples = "";

			for(String temp: temps) {
				if(!temp.contains(verb + " is_negative Yes") ) {
					newTriples = newTriples + temp + "]";
				}
			}
			sentence = Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
		} else {
			triples += "["+getVerbInTriples(triples) +" is_negative Yes]";
			sentence = Start.getStart().generate(triples).replace("\n", "").replace(".", "");
		}

		sentence = sentence.replace("isn't", "is not").replace("wasn't", "was not")
				.replace("aren't", "are not").replace("weren't", "were not");
		return sentence;
	}

	//  "sth" -> "make sth"
	public static String state2Goal(String string) {
		Entity entity = t.translate(string).getElement(0);
		Sequence sequence = new Sequence(Markers.ROLE_MARKER);
		sequence.addElement(new Function(Markers.OBJECT_MARKER,entity));
		Relation relation = new Relation(Markers.MAKE, new Entity("you"), sequence);
		string = g.generate(relation).toLowerCase();
//		Mark.say(string);
		return string;
	}
	
	public static void testHaveDone() {
//		Mark.say(haveDone("make a Thanksgiving Day salad"));
		Mark.say(haveDone("make a fruit salad"));
//		Mark.say(haveDone("make a salad"));
	}

	//  "sth" -> "sth has happened"
	public static String haveDone(String string) {
		return haveDone(string, new Entity("I"));
	}
	
	public static String haveDone(String string, Entity subject) {
		Entity entity = t.translate(string).getElement(0);

		String triples = g.generateTriples(entity);
		triples += "["+getVerbInTriples(triples) +" is_perfective yes]";
		if(triples.contains(Markers.IS_IMPERATIVE_MARKER)){
			triples = triples.replace(Markers.IS_IMPERATIVE_MARKER+" yes",
					Markers.IS_IMPERATIVE_MARKER+" no");
		}

		string = Start.getStart().generate(triples);
		entity = t.translate(string).getElement(0);
		entity.setSubject(subject);
		
		return Z.string2Capitalized(g.generate(entity).toLowerCase());
	}
	
	public static void testAssumeHasDone() {
		assumeHasDone("make a fruit salad");
	}

	//  "sth" -> "assume that sth has happened"
	public static String assumeHasDone(String string) {
//		printInnereseTree(string);

		Entity entity = t.translate(string).getElement(0);

		String triples = g.generateTriples(entity);
		triples += "["+getVerbInTriples(triples) +" is_perfective yes]";
		if(triples.contains(Markers.IS_IMPERATIVE_MARKER)){
			triples = triples.replace(Markers.IS_IMPERATIVE_MARKER+" yes",
					Markers.IS_IMPERATIVE_MARKER+" no");
		}

		string = Start.getStart().generate(triples);
		entity = t.translate(string).getElement(0);

//		understand(entity);
		Sequence sequence = new Sequence(Markers.ROLE_MARKER);
		sequence.addElement(new Function(Markers.OBJECT_MARKER,entity));
		Relation relation = new Relation(Markers.ASSUME, new Entity("you"), sequence);

		string = g.generate(relation);
		Mark.say("@Z. Assume has done: ",string);
		return string;
	}

	//  "did sth" -> "do sth"
	public static String verbs2Present(String sentence) {

		if(sentence.split(" ").length==1) {
			sentence = "she "+sentence + " something";
		}
		Translator translator = Translator.getTranslator();
		Generator gn = Generator.getGenerator();
		Entity entity = translator.translate(sentence).getElement(0);

		if (entity.hasFeature(Markers.PAST)) {
			entity.removeFeature(Markers.PAST);
		}
		if (entity.hasFeature(Markers.PASSIVE)) {
			entity.removeFeature(Markers.PASSIVE);
		}
		if (entity.hasProperty(Markers.PROGRESSIVE)) {
			entity.removeProperty(Markers.PROGRESSIVE);
		}

		entity.setSubject(translator.translate("I"));
		if (!entity.hasProperty(Markers.IMPERATIVE)) {
			entity.addProperty(Markers.IMPERATIVE, true);
		}

		String english = gn.generate(entity);
		english = english.replace("I ", "");
		english = english.replace(".", "");
		if (english.endsWith("something")) english = english.replace(" something", "");
		return english;
	}

	public static List<String> toPresentTense(String string) {
		List<String> strings = new ArrayList<>();
		t = BedtimeStoryLearner.getTranslator();
		Entity entities = t.translate(string);
		for(Entity entity: entities.getElements()) {
			if (entity.hasFeature(Markers.PAST)) {
				entity.removeFeature(Markers.PAST);
			}
			if (entity.hasFeature(Markers.PASSIVE)) {
				entity.removeFeature(Markers.PASSIVE);
			}
			if (entity.hasProperty(Markers.PROGRESSIVE)) {
				entity.removeProperty(Markers.PROGRESSIVE);
			}
			String triples = g.generateTriples(entity).toLowerCase();
			String[] temps = triples.split("]");
			triples = "";
			for(String temp: temps) {
				if(!temp.contains(" has_comp in_order") && !temp.contains(" has_position leading")
						&& !temp.contains(" is_main yes") && !temp.contains(" has_tense to")
						&& !temp.contains(" related-to") && !temp.contains(" has_modifier")) {
					triples = triples + temp + "]";
				}
			}
			string = Start.getStart().generate(triples);
			string = string.replace("\n", "");
			string = string.replace(" being ", " is ");
			Mark.say(string);
			strings.add(string.replace(".", ""));
		}
		return strings;
	}

	// output the "n-th" form of numbers in English
	public static String number2Oridinal(int j) {
		String number = "";
		switch(j) {
			case 0: number = "first"; break;
			case 1: number = "second"; break;
			case 2: number = "third"; break;
			case 3: number = "fourth"; break;
			case 4: number = "fifth"; break;
			case 5: number = "sixth"; break;
			case 6: number = "seventh"; break;
			case 7: number = "eighth"; break;
			case 8: number = "nineth"; break;
			case 9: number = "tenth"; break;
		}
		return number;
	}

	// correct badly generated English
	public static String generate(Entity entity) {
		String string = "";
		if(entity.getElements().size()<=1) {
			String triples = g.generateTriples(entity).toLowerCase();
			String[] temps = triples.split("]");
			String newTriples = "";

			for(String temp: temps) {
				if(!temp.contains(" has_comp in_order") && !temp.contains(" has_position leading")
						&& !temp.contains(" is_main yes") && !temp.contains(" has_tense to")
						&& !temp.contains(" related-to ")) {
					newTriples = newTriples + temp + "]";
				}
			}

			string = Start.getStart().generate(newTriples);
			string = string.replace("\n", "");
		} else {
			string = g.generate(entity);
		}

		Mark.say("generated Good English:",string);
		return string;
	}

	// change subject in innerese
	public static String stringChangeSubject(String english) {
		Entity entity = getTranslator().translate(english).getElement(0);
		entity.setSubject(new Entity("she"));
		english = Generator.getGenerator().generate(entity);
		english = english.toLowerCase();
		return english;
	}

	// ==========================================
	//  reform a question into a goal
	// ==========================================
	public static void testQuestion2Goal() {

		List<String> sentences = new ArrayList<>();
//		sentences.add("can you replace cellphone battery?");
//		sentences.add("do you know how to go to school?");
//		sentences.add("how do I go to school?");
//		sentences.add("I want to know how to replace cellphone battery");
//		sentences.add("Can you make an apple salad?");
//		sentences.add("Could you please make a fruit salad?");
	
//		sentences.add("can you make a fruit salad for me?");
//		sentences.add("can you show me how to make a fruit salad?");
//		sentences.add("could you please make me a fruit salad!");
//		sentences.add("make me a fruit salad");
		sentences.add("let's season the pasta!");
		
		// null examples
//		sentences.add("if the weather is good, I go to school."); // this is a condition+step
//		sentences.add("You need to know how to replace cellphone battery."); // this is a step
//		sentences.add("Do you know how much I love you?"); // always fail because START doesn't do how much
		
		for(String sentence:sentences) {
			Mark.say("   \n\n------",question2Goal(sentence),"------\n\n");
			System.out.print("\n\n");
		}
	}
	
	public static String question2Goal(String sentence) {
		// TODO "how do you make a salad with chopped fruit"
		return question2Goal(t.translate(sentence).getElement(0));
	}

	// get the goal in a how-to question
	public static String question2Goal(Entity entity) {
		String english = "";
		
		understand(entity);
		
		if(Z.stringIsWant(entity)) {
			return want2Goal(entity);
			
		} else if(Z.stringIsRequest(entity)) {
			
			// eliminate manner in goal
			Sequence newRoles = new Sequence("roles");
			for(Entity entity1: entity.getSubject().getObject().getElements()) {
				if (!entity1.getType().equals(Markers.MANNER_MARKER)) {
					newRoles.addElement(entity1);
				}
			}
			entity.getSubject().setObject(newRoles);
			english = g.generate(entity.getSubject()).replace(".", "");

		} else if(Z.stringIsHowTo(entity)) {

//			Mark.say("Entity before:", entity);
//			Mark.say("Features before:", entity.getFeatures());
//			Mark.say("Properties before", entity.getPropertyList().toString());

			if (entity.hasFeature(Markers.PAST)) entity.removeFeature(Markers.PAST);
			if (entity.hasFeature(Markers.PASSIVE)) entity.removeFeature(Markers.PASSIVE);
			if (entity.hasProperty(Markers.GROUP)) {
				@SuppressWarnings("unchecked")
				List<Entity> yes = (List<Entity>) entity.getProperty(Markers.GROUP);
//				Mark.say("   found GROUP",yes.get(0).getSubject());
				entity = yes.get(0).getObject();
			} else {
				String check = g.generate(entity);
//				Mark.say("++ English check:", check);
				if(check.startsWith("How")) {
					entity = entity.getChildren().iterator().next();
				}
			}

//			Mark.say(entity);
//			entity.setSubject(t.translate("I"));
			if (entity.hasFeature(Markers.TO)) entity.removeFeature(Markers.TO);
		
		} else if(entity.getType().equals(Z.LET)) {
			entity = entity.getObject().getElement(0).getSubject();
			
		} else if(entity.hasProperty(Markers.IMPERATIVE)) {
			
		} else {
			return null;
		}

		// "want to know how to"
		if(Z.getName(entity).equals(Markers.HOW_QUESTION)) {
			entity = entity.getObject();
		}
		
		understand(entity);
		
		// why is this happening
		if(isEnt(entity.getSubject())) {
			entity.setSubject(new Entity("you"));
			english = string2Capitalized(g.generate(entity).replace("are", "be")).replace("?", "").replace(".", "");
		}
//		if(english.startsWith("I")) {
//			english = string2Capitalized(english.replace("I ", ""));
//		}
		return english;
	}
	
	public static String want2Goal(Entity entity) {
		String goal = "";
		entity = entity.getObject().getElement(0).getSubject();
		understand(entity);
		
		// "I want to know how to ..."
		if(isRel(entity.getObject())) {
			goal = string2Capitalized(g.generate(entity.getObject())).replace("?", "").replace(".", "");
		} else {
			entity.setSubject(new Entity("you"));
			goal = string2Capitalized(g.generate(entity).replace("are", "be")).replace("?", "").replace(".", "");
		}
		
		return goal;
	}
	
	public static Boolean stringIsWant(Entity entity) {
		return Z.hasType(entity, Markers.WANT_MARKER);
	}

	// ==========================================
	//  reform a statement into a how to question
	// ==========================================
	public static void testHowQuestion() {
		List<String> sentences = new ArrayList<>();
		sentences.add("criticize the book's merits");
		sentences.add("play the piano");
		sentences.add("john feels sad");
		sentences.add("make john feel sad");
		for(String sentence:sentences) {
			Mark.say("How questions: ", howQuestion(sentence, 2));
			Mark.say("How questions: ", howQuestion(sentence, 1));
			System.out.print("\n\n");
		}
	}
	
	// "King kills Yuan" --> "when will King kill Yuan?"
	public static String howQuestion(String string, int person) {
		Entity entityAnswer = t.translate(string);
//		understand(entityAnswer);
		return howQuestion(entityAnswer.getElement(0), person);
	}
	
	public static String howQuestion(String string) {
		Entity entityAnswer = t.translate(string).getElement(0);
		return howQuestion(entityAnswer, 2);
	}
	
	public static String howQuestion(Entity entityAnswer) {
		return howQuestion(entityAnswer, 2);
	}


	public static String howQuestion(Entity entityAnswer, int person) {
		
		if(person==2) {
			if(Z.entity2Name(entityAnswer.getSubject()).equals("i")) {
				entityAnswer.setSubject(new Entity("you"));
			}
		} else {
			if(Z.entity2Name(entityAnswer.getSubject()).equals("you")) {
				entityAnswer.setSubject(new Entity("I"));
			}
		}

		Function questionNew = new Function(Markers.WHEN_QUESTION, entityAnswer);
		questionNew.addType(Markers.QUESTION);

		String triples = g.generateTriples(questionNew).toLowerCase();
//		Mark.purple(triples);
		String[] temps = triples.split("]");
		String verb = Z.getVerbInTriples(triples);

		if(triples.contains(Markers.IS_IMPERATIVE_MARKER)) {
			triples = triples.replace(Markers.IS_IMPERATIVE_MARKER+" yes", Markers.IS_IMPERATIVE_MARKER+" no");
		}

		if(triples.contains("is_negative yes")) {
			triples += "["+verb +" has_modifier no_longer]";
		}
		if(person==2) {
			triples += "["+verb +" has_modal can]";
			triples += "["+verb +" has_tense past]";
		}
		triples += "["+verb +" is_question yes]";
		triples += "["+verb +" has_method how]";

		temps = triples.split("]");
		triples = "";
		for(String temp: temps) {
			if(!temp.contains(" has_comp in_order") && !temp.contains(" has_position leading")
					&& !temp.contains(" is_main yes") && !temp.contains(" has_tense to")
//					&& !temp.contains(" related-to ")
					) {
				triples = triples + temp + "]";
			}
		}
		Mark.purple(triples);
		String string = Start.getStart().generate(triples);
		string = string.replace("\n", "");
		return string;
	}


	// ==========================================
	//  reform a statement into a yesno question
	// ==========================================
	public static void testYesNoQuestion() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("we are making the salad for me");
//		sentences.add("you are making the salad for me");
//		sentences.add("you need to be ready");
//		sentences.add("I am making the salad for you?");
//		sentences.add("give me the salad");
//		sentences.add("answer my question");
//		sentences.add("I need to be ready");
//		sentences.add("she is a good friend of mine");
//		sentences.add("I am short in time");
//		sentences.add("I don't have enough time");
//		sentences.add("My bike is new");
//		sentences.add("she likes you");
		sentences.add("he stands");
		sentences.add("he is not seated");
		sentences.add("he is not giving an argument");
		sentences.add("the contents are persuasive");
		sentences.add("you are changing battery for a wet phone");

		// failure
//		sentences.add("be a good man");

		for(String string: sentences) {
			Mark.say("\n\n ------------------", string);
			Mark.say(yesNoQuestion(t.translate(string)));
			Mark.say(yesNoQuestion(string));
		}
	}

	// "you have the money" --> "Do you have the money?"
	public static String yesNoQuestion(String string) {
		string.replace("isn't", "is not");
		string.replace("aren't", "are not");
		string.replace("wasn't", "was not");
		string.replace("weren't", "were not");
//			Generator g = Generator.getGenerator();
		Entity entity = t.translate(string).getElement(0);
		return yesNoQuestion(entity);
	}

	public static String yesNoQuestion(Entity entity) {
		understand(entity);
		String triples = g.generateTriples(entity).toLowerCase();
		String verb = Z.getVerbInTriples(triples);
//		Mark.say(verb);

		// STEP 1 ---- take a note if need negative answer
		if(triples.contains("is_negative yes") || triples.contains("is_negative Yes")) {
			StratagemExpert.waitNegative = true;
			triples = triples.replace("["+verb+" is_negative yes]", "")
					.replace("["+verb+" is_negative Yes]", "");
		}

		// STEP 2 ---- become question
		triples += "["+verb +" is_question yes]";

		// STEP 3 ---- special case: "give me the cup" -> "can you give me the cup?"
		if(entity.hasProperty(Markers.IMPERATIVE)) {
			triples += "["+verb + " has_modal can]";
			triples += "["+verb + " has_tense past]";
			triples += "["+verb + " is_main yes]";
		}
		if(entity.hasProperty(Markers.MODAL)) {
			if(entity.getProperty(Markers.MODAL).equals(Markers.CAN))
				triples += "["+verb + " has_modal can]";
		} 

		// STEP 4 ---- get rid of imperative marker, need to structure
		String[] temps = triples.split("]");
		triples = "";
		for(String temp: temps) {
			if(!(temp.contains("is_imperative yes") || temp.contains(verb +" has_tense to") ||
					(temp.contains(" need+") && temp.contains(verb)))) {
				triples = triples + temp + "]";
//				Mark.say(triples);
			}
		}

		// STEP 5 ---- switch "I" into "you"
		if(triples.contains("[you+")||triples.contains("i+")) {
			triples = triples.replace("[you+", "[he+").replace(" you+", " he+");
			triples = triples.replace("[i+", "[you+").replace(" i+", " you+");
			triples = triples.replace("[he+", "[i+").replace(" he+", " i+");
		}

		return Start.getStart().generate(triples).replace("\n", "");
	}


	// ==========================================
	//  reform a statement into a whatif question   ,
	// ==========================================
	public static String whatIfQuestion(String string) {
//		"you have the money" --> "what if you don't have the money?"
		Entity entityAnswer = t.translate(string).getElement(0);
		String triples = g.generateTriples(entityAnswer).toLowerCase();
		String verb = Z.getVerbInTriples(triples);
		if(triples.contains(Markers.IS_IMPERATIVE_MARKER)) {
			triples = triples.replace(Markers.IS_IMPERATIVE_MARKER+" yes", Markers.IS_IMPERATIVE_MARKER+" no");
		}

		if(triples.contains("is_negative yes")) {
			StratagemExpert.waitNegative = true;
			triples = triples.replace("["+verb+" is_negative yes", "");
		} else {
			triples += "["+verb +" is_negative yes]";
		}

		string = ZSay.SAY_WHAT_IF + Start.getStart().generate(triples).toLowerCase();
		string = string.replace("\n", "");
		if(string.endsWith(".")) {
			string = string.replace(".", "?");
		} else {
			string += "?";
		}
		Mark.say("What if question: ",string);
		return string;
	}


	// ==========================================
	//  "apples on table" -> "apples" .  descriptions into entity.
	// ==========================================
	public static void testDescription2Entity() {
		List<String> steps = new ArrayList<>();
//			steps.add("red bowl on the table");
//			steps.add("a green cup on the table");
		steps.add("red apple on table");
		for(String step: steps) {
			Mark.say(description2Entity(step));
		}
	}

	public static String description2Object(String string) {
		Entity entity = t.translateToEntity(string);
		if(entity == null) {
			entity = t.translateToEntity("I see "+string);
		}
		Mark.say(entity);
		Map<String, String> roles = getRoles(entity);
		string = roles.get(Markers.OBJECT_MARKER);
		Mark.say(string);
		return string;
	}

	public static Entity description2Entity(String string) {
		Entity entity = t.translateToEntity("I see "+string);
		entity = entity.getObject();
		entity = entity.getElement(0);
		entity = entity.getSubject();
		return entity;
	}


	// ==========================================
	//  "can you make a salad for me" -> "can I make a salad for you"
	// ==========================================
	public static void testSentence2Chat() {
		List<String> sentences = new ArrayList<>();
//		sentences.add("are you making the salad for me?");
//		sentences.add("you need to be ready");
//		sentences.add("am I making the salad for you?");
//		sentences.add("give me the salad");
//		sentences.add("I need to be ready");
//		sentences.add("she is a good friend of mine");
//		sentences.add("I am short in time");
//		sentences.add("I don't have enough time");
//		sentences.add("My bike is new");
//		sentences.add("she likes you");
//		sentences.add("read a book");
		sentences.add("make an apple salad");
		for(String string: sentences) {
			Mark.say(sentence2Chat(string));
		}

	}

	public static String sentence2Chat(String string) {

		Entity entity = t.translate(string);
		String triples = g.generateTriples(entity);
		if(triples.contains("is_imperative yes")) {
			String[] temps = triples.split("]");
			triples = "";
			for(String temp: temps) {
				if(!temp.contains("is_imperative yes")) {
					triples = triples + temp + "]";
				}
			}
		}
		triples = triples.replace("[you+", "[he+").replace(" you+", " he+");
		triples = triples.replace("[i+", "[you+").replace(" i+", " you+");
		triples = triples.replace("[he+", "[i+").replace(" he+", " i+");

		string = Start.getStart().generate(triples).replace("\n", "").replace(".", "");

		return Z.string2Capitalized(string);
	}


	// ==========================================
	//   sentence to actions
	// ==========================================
	
	public static void testSentence2Action() {

		String string = "look at the chapters that seem pivotal to its argument.";
//		string = "study the table of contents to obtain a general sense of the structure of the book. ";
		Entity entity = t.translate(string).getElement(0);
		Mark.say(g.generate(entity));
		Mark.say(Z.sentence2Action(entity));
		
	}
	
	public static String sentence2Action(Entity entity) {
		String triples = g.generateTriples(entity);
		String verb = getVerbInTriples(triples);
		String[] temps = triples.split("]");
		String newTriples = "";

		for(String temp: temps) {
			if(!temp.contains(verb+" has_tense to") &&
					!temp.endsWith(verb) &&
					!(temp.contains("need") && temp.contains("is_imperative yes"))) {
				newTriples = newTriples + temp + "]";
			}
		}
		// "turns off the button"
		if(newTriples.contains("[one+")) {
			newTriples = newTriples.replace("[one+", "[you+");
			newTriples = newTriples + "[" + verb + " " + Markers.IS_IMPERATIVE_MARKER + " yes]";
		}
		if(newTriples.contains("[he+")) {
			newTriples = newTriples.replace("[he+", "[you+");
			newTriples = newTriples + "[" + verb + " " + Markers.IS_IMPERATIVE_MARKER + " yes]";
		}
		if(newTriples.contains("[she+")) {
			newTriples = newTriples.replace("[she+", "[you+");
			newTriples = newTriples + "[" + verb + " " + Markers.IS_IMPERATIVE_MARKER + " yes]";
		}
		if(newTriples.contains("[it+")) {
			newTriples = newTriples.replace("[it+", "[you+");
			newTriples = newTriples + "[" + verb + " " + Markers.IS_IMPERATIVE_MARKER + " yes]";
		}
		
		// the same triple cannot generate back the same sentence
		if (newTriples.equals(triples)) {
			return g.generate(entity).replace(".", "");
		}
		return Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
	}

	public static String sentence2Action(String sentence) {
		try {
			Entity entity = t.translate(sentence).getElement(0);
			try {
				String triples = g.generateTriples(entity);
				String verb = getVerbInTriples(triples);
				String[] temps = triples.split("]");
				String newTriples = "";

				for(String temp: temps) {
					if(!temp.contains(verb+" has_tense to") &&
							!temp.endsWith(verb) &&
							!(temp.contains("need") && temp.contains("is_imperative yes"))) {
						newTriples = newTriples + temp + "]";
					}
				}
				// "turns off the button"
				if(newTriples.contains("[one+")) {
					newTriples = newTriples.replace("[one+", "[you+");
					newTriples = newTriples + "[" + verb + " " + Markers.IS_IMPERATIVE_MARKER + " yes]";
				}
				try {
					sentence = Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
				} catch (NullPointerException e){
					Mark.say("Cannot extract action from sentence: Problem with Start.getStart().generate(newTriples)");
					sentence = sentence + "..";
				}
			} catch (NullPointerException e){
				Mark.say("Cannot extract action from sentence: Problem with Generator.generateTriples(entity)");
				sentence = sentence + "..";
			}
		} catch (ArrayIndexOutOfBoundsException a) {
			Mark.say("Cannot extract action from sentence: Problem with Translator.translate(sentence)");
			sentence = sentence + "..";
		}

		return sentence;
	}


	// ==========================================
	//  steps to states
	// ==========================================
	public static void testSteps2States() {
		List<String> steps = new ArrayList<>();
//		steps.add("you need to pour sauce from the cup to the bowl");
//		steps.add("pour sauce from the cup to the bowl");
//		steps.add("find a bowl");
//		steps.add("put fruits in the bowl");
//		steps.add("pour sauces into the bowl");
//		steps.add("pour sauces from the cup into the bowl");
//		steps.add("remove the battery from the phone");
//		steps.add("remove the battery of the phone");
//		steps.add(" You spread nuts into the bowl");
//		steps.add("Toss the chopped fruits into the bowl");
//		steps.add("then, toss the chopped fruits into the bowl");
		steps.add("Place the eggs into a saucepan");
		Mark.mit(steps2States(steps));
		
//		List<String> sentences = new ArrayList<String>();
//		sentences.add("Alice forgot to take out her Nokia phone from her pants.");
//		sentences.add("Then, she put her pants in the working machine.");
//		sentences.add("The phone got all wet and stopped working.");
//		sentences.add("She decided to replace the battery of the phone first.");
//		
//		Mark.mit(steps2States(sentences));
	}

//	// turn steps into subgoals of physical changes
//	public static String step2State(String step){
//
//		String newStep = "";
//		Mark.say("--> ",step);
//		String state = "";
////		understand(t.translate(step).getElement(0).getObject());
//
//		if(state!="") {
//			newStep = state;
//			Mark.say("!!!!!!!!   New Steps:  "+newStep);
//		}
//
//		return newStep;
//
//	}
	
	public static List<Entity> ignorePassiveAndScene(Entity entity) {
		List<Entity> entities = new ArrayList<>();;
		if(entity.getElements().size()>1) {
			for(Entity entity1:entity.getElements()) {
				if(!entity1.getType().equals(Markers.SCENE)) {
					if(!entity1.getSubject().getType().equals(Markers.SOMEBODY)) {
						entities.add(entity1);
					}
				}
			}
		} else {
			entities.add(entity.getElement(0));
		}
		return entities;
	}

	// turn steps into subgoals of physical changes
	public static List<String> steps2States(List<String> steps){
		List<String> newSteps = new ArrayList<>();
		List<String> unknownSteps = new ArrayList<>();

		for(String step: steps) {
			String state = step;
			Boolean changeless = false;

			Mark.say(DEBUG, "\n\n\n--> ",step);
			Map<String, Entity> roles = getRoleEntities(step);
			// "I want to put the apple in the bowl" haven't done
			if(roles.containsKey(Z.MENTAL)) {
				if(CHANGELESS.contains(roles.get(Z.MENTAL).getType())) {
					changeless = true;
				}
			}
			if(!changeless) {
				Mark.say(DEBUG, roles);
				Entity object = roles.get(Markers.OBJECT_MARKER);
				
				if (roles.containsKey(Markers.FROM)) {
					Sequence path = JFactory.createPath();

					Entity from = roles.get(Markers.FROM);
					Function origin = JFactory.createPathElement(Markers.FROM, from);
					path.addElement(origin);

					if(roles.containsKey(Markers.TO)) {
						Entity to = roles.get(Markers.TO);
						Function destination = JFactory.createPathElement(Markers.TO, to);
						path.addElement(destination);
						
					} else if (roles.containsKey(Z.INTO)) {
						Entity to = roles.get(Z.INTO);
						Function destination = JFactory.createPathElement(Markers.TO, to);
						path.addElement(destination);
					} else {
						Entity to = Start.makeThing("table");
						Function destination = JFactory.createPathElement(Markers.TO, to);
						path.addElement(destination);
					}
					
					if(object==null) {
						object = roles.get(Z.DOWN);
					}
					Entity trajectory = JFactory.createTrajectory(object, Markers.MOVE_MARKER, path);
//					Mark.night(object);
//					Mark.night(trajectory);
//					Mark.night(step);
					state = g.generate(trajectory);
					// TODO path has no into
//					state = Z.replaceString(state, Markers.TO, Markers.INTO);

				} else if (roles.containsKey(Z.INTO)) {

					state = createPositionalState(object, roles.get(Z.INTO), Markers.IN);
					
//					Sequence path = JFactory.createPath();
//					Entity to = roles.get(Markers.INTO);
//					Function destination = JFactory.createPathElement(Markers.TO, to);
//					path.addElement(destination);
//
//					Entity object = roles.get(Markers.OBJECT_MARKER);
//					Entity trajectory = JFactory.createTrajectory(object, Markers.MOVE_MARKER, path);
//					state = g.generate(trajectory);

				} else if (roles.containsKey(Markers.TO)) {
					
					Entity ground = roles.get(Markers.TO);
					for(String place: RGoal.inPlaces) {
						if(isInstance(ground, place)) {
							state = createPositionalState(object, ground, Markers.IN);
						}
					}
					for(String place: RGoal.onPlaces) {
						if(isInstance(ground, place)) {
							state = createPositionalState(object, ground, Markers.ON);
						}
					}

				} else {
					for(String key: POSITIONS) {
						if(roles.containsKey(key)) {
							state = createPositionalState(object, roles.get(key), key);
						}
					}
				}
				Mark.show(DEBUG, state);
				newSteps.add(state);
				if(state.equals(step)) unknownSteps.add(step);
			}
		}
		Mark.say(DEBUG, "!!!!!!!!   New Steps:  "+newSteps.toString());
		Mark.say(DEBUG, "!!!!!!!!   Unknown Steps:  "+unknownSteps.toString());
		return newSteps;
	}
	
	public static List<String> rGoals2States(List<RGoal> rgoals){
		List<String> newSteps = new ArrayList<>();
		List<String> unknownSteps = new ArrayList<>();
		
		for(RGoal rgoal: rgoals) {
			Entity figure = rgoal.getFigure();
			Entity ground = rgoal.getGround();
			String relation = rgoal.getRelation();
			String step = rgoal.getAction();
			String state = step;
			Mark.say("\n\n\n--> ",step);
			
			// --- source: from
			if (relation.equals(Markers.FROM)) {
				
				Sequence path = JFactory.createPath();
				Function origin = JFactory.createPathElement(Markers.FROM, figure);
				path.addElement(origin);
				Entity trajectory = JFactory.createTrajectory(ground, Markers.MOVE_MARKER, path);
				state = g.generate(trajectory);
				// TODO path has no into
				state = Z.replaceString(state, Markers.TO, Z.INTO);
				
//			// --- destination: into
//			} else if (relation.equals(Markers.INTO)) {
//				
//				Sequence seq = new Sequence(Markers.ROLE_MARKER);
//				Relation rel = new Relation(Markers.BE_MARKER, figure, seq);
//				Function fun = new Function(Markers.IN, ground);
//				seq.addElement(fun);
//				state = g.generate(rel);
//				
//			// --- destination: onto
//			} else if (relation.equals(Z.ONTO)) {
//				
//				Sequence seq = new Sequence(Markers.ROLE_MARKER);
//				Relation rel = new Relation(Markers.BE_MARKER, figure, seq);
//				Function fun = new Function(Markers.ON, ground);
//				seq.addElement(fun);
//				state = g.generate(rel);
//				
//			// --- destination: to
//			} else if (relation.equals(Markers.TO)) {	
				
				
			// --- location: on, in, over, under
			} else if (!relation.equals(Z.WHAT)){
				
				state = createPositionalState(figure, ground, relation);
			}
			Mark.say(state);
			newSteps.add(state);
			if(state.equals(step)) unknownSteps.add(step);
		}
		Mark.say("!!!!!!!!   New Steps:  "+newSteps.toString());
		Mark.say("!!!!!!!!   Unknown Steps:  "+unknownSteps.toString());
		return newSteps;
	}
	
	public static String createPositionalState(Entity figure, Entity ground, String relation) {
		Sequence seq = new Sequence(Markers.ROLE_MARKER);
		Relation rel = new Relation(Markers.BE_MARKER, figure, seq);
		Function fun = new Function(relation, ground);
		seq.addElement(fun);
		return g.generate(rel).replace(".", "");
	}

	// turn steps into subgoals of physical changes
	public static String step2State(String step, List<String> transitionRules){
		if(transitionRules.isEmpty()) {
			try {
				transitionRules = new ArrayList<>(Files.readAllLines(Paths.get(transitionRulesFile), StandardCharsets.UTF_8));
				transitionRules.removeAll(Arrays.asList("", null));
				transitionRules = Z.listToLower(transitionRules);
				transitionRules = Z.listRemoveComment(transitionRules);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String newStep = "";
		Mark.say("--> ",step);
		String state = "";
//		understand(t.translate(step).getElement(0).getObject());
//			for(String rule:transitionRules) {
//				Mark.say("--- "+rule);
//				Translator t = Translator.getTranslator();
//				t.internalize("xx is a thing");
//				t.internalize("yy is a thing");
//				t.internalize("zz is a thing");
//				Entity thisKnownStep = Translator.getTranslator().translateToEntity(rule);
//				Entity thisIntention = Translator.getTranslator().translateToEntity(step);
//
//				Entity antecedent = thisKnownStep.getSubject().getElements().get(0);
//				Entity consequent = thisKnownStep.getObject();
//				LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, thisIntention);
//				if(bindings!=null) {
//					Entity result = Substitutor.substitute(consequent, bindings);
//					state = Generator.getGenerator().generate(result);
//					if(state.endsWith(".")) state = state.replace(".","");
//					break;
//				}
//			}
		if(state!="") {
			newStep = state;
			Mark.say("!!!!!!!!   New Steps:  "+newStep);
		}

		return newStep;

	}

	// turn steps into subgoals of physical changes
	public static List<String> steps2States(List<String> steps, List<String> transitionRules){
		if(transitionRules.isEmpty()) {
			try {
				transitionRules = new ArrayList<>(Files.readAllLines(Paths.get(transitionRulesFile), StandardCharsets.UTF_8));
				transitionRules.removeAll(Arrays.asList("", null));
				transitionRules = Z.listToLower(transitionRules);
				transitionRules = Z.listRemoveComment(transitionRules);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		List<String> newSteps = new ArrayList<>();
		for(String step: steps) {
			Mark.say("--> ",step);
			String state = "";
			for(String rule:transitionRules) {
//					String verb = rule.substring(rule.indexOf("xx")+3,rule.indexOf("yy")-1);
//					if(step.toLowerCase().contains(verbs2Present(verb))) {
					Mark.say("     "+rule);
					Translator t = Translator.getTranslator();
					t.internalize("xx is a thing");
					t.internalize("yy is a thing");
					t.internalize("zz is a thing");
					Entity thisKnownStep = Translator.getTranslator().translateToEntity(rule);
					Entity thisIntention = Translator.getTranslator().translateToEntity(step);

					Entity antecedent = thisKnownStep.getSubject().getElements().get(0);
					Entity consequent = thisKnownStep.getObject();
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, thisIntention);
					if(bindings!=null) {
						Entity result = Substitutor.substitute(consequent, bindings);
						state = Generator.getGenerator().generate(result);
						if(state.endsWith(".")) state = state.replace(".","");
						Mark.say("         Found");
						break;
					}

//					}
			}
			if(state!="") {
				newSteps.add(state);
			}else {
				newSteps.add(step);
			}
		}
		Mark.say("!!!!!!!!   New Steps:  "+newSteps.toString());
		return newSteps;

	}

	// output the steps whose physical outcomes are unknown
	public static List<String> findUnknownSteps(List<String> steps, List<String> transitionRules){

		List<String> unknownSteps  = new ArrayList<>();
		List<String> states = Z.steps2States(steps,transitionRules);

		for(int i=0; i<states.size();i++) {
			if(states.get(i)==steps.get(i)) {
				Mark.say("!!!!!!!!!!!!!!??????   Untransformed Steps: ", steps.get(i));
				unknownSteps.add(steps.get(i).toLowerCase());
			}
		}
		return unknownSteps;
	}

	// see of a string contains the steps whose physical meanings are known
	public static Boolean ifKnownStep(String step, List<String> knownSteps) {
		if(knownSteps.isEmpty()) {
			try {
				knownSteps = new ArrayList<>(Files.readAllLines(Paths.get(knownStepsFile), StandardCharsets.UTF_8));
				knownSteps.removeAll(Arrays.asList("", null));
				knownSteps = Z.listToLower(knownSteps);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		// intention match existing steps
		for(String knownStep: knownSteps) {
			Translator t = Translator.getTranslator();
			t.internalize("xx is a thing");
			t.internalize("yy is a thing");
			t.internalize("zz is a thing");
			Entity thisKnownStep = t.translateToEntity(knownStep);
			Entity thisIntention = t.translateToEntity(step);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(thisKnownStep, thisIntention);
			if(bindings!=null) {
				return true;
			}
		}
		return false;
	}

	// find unknown objects given mentioned objects and observed objects
	public static void testFindUnknownObject() {
		List<String> mentionedObjects = new ArrayList<>();
		mentionedObjects.add("a bowl");
		mentionedObjects.add("sauces");
		mentionedObjects.add("the apple");

		List<String> observedObjects = new ArrayList<>();
		observedObjects.add("red bowl on the table");
		observedObjects.add("a green cup on the table");
		observedObjects.add("red apple on table");

		findUnknownObject(mentionedObjects, observedObjects);
	}

	public static List<String> findUnknownObject(List<String> mentionedObjects, List<String> observedObjects) {
		List<String> unknown = new ArrayList<>();

		for(String mentioned: mentionedObjects) {
			Boolean matched = false;
			mentioned = mentioned.replace("a ", "").replace("the ", "");
			for(String observed: observedObjects) {
				if(observed.contains(mentioned)) {
					matched = true;
				}
			}
			if(!matched) {
				unknown.add("the "+mentioned);
			}
		}
//		List<Entity> mentionedEntities = new ArrayList<>();
//		List<Entity> observedEntities = new ArrayList<>();
//		for(String mentioned: mentionedObjects) {
//			mentionedEntities.add(t.translate(mentioned));
//		}
//		for(String observed: observedObjects) {
//			observedEntities.add(description2Entity(observed));
//		}
//		for(int i = 0; i< mentionedEntities.size();i++) {
//			Boolean matched = false;
//			for(Entity observedEnt: observedEntities) {
//				if(StandardMatcher.getBasicMatcher()
//						.match(mentionedEntities.get(i), observedEnt)!=null) {
//					matched = true;
//				}
//			}
//			if(!matched) {
//				unknown.add(mentionedObjects.get(i));
//			}
//		}
		Mark.say(unknown);
		return unknown;
	}



	// ====================================================================================
	//
	//   Basic Operations 3/4: Semantic Matching
	//
	// ====================================================================================

	// ==========================================
	//  For debugging: print out if two sentences are similar, or identical in Innerese
	// ==========================================
	// test: matching use feature
	public static void testMatchingUseFeature(String xx, String yy, Boolean ifSelected) {
		Translator basicTranslator = Translator.getTranslator();
		Generator generator = Generator.getGenerator();
		Start.getStart().setMode(Start.STORY_MODE);
		generator.flush();

		Entity xxx = basicTranslator.translate(xx).getElement(0);
		Entity yyy = basicTranslator.translate(yy).getElement(0);

		Switch.useFeaturesWhenMatching.setSelected(ifSelected);
		Mark.say("useFeaturesWhenMatching: ",Switch.useFeaturesWhenMatching.isSelected());

		/* "I am a (good) person" and "she is a bad person"
		 * FALSE =====>
		 * 		(bindings:
					{ 'binding':['(ent i-522)', '(ent mary-91)'], 'allowed':true, 1.0 }
					{ 'binding':['(ent person-528)', '(ent person-580)'], 'allowed':true, 1.0 }
				)
		 * TRUE =====> nothing
		 */

		EntityMatcher hi =  new EntityMatcher();
		EntityMatchResult result = hi.match(xxx,yyy);
		Mark.say(result);
	}

	public static void testMatching() {

		List<String> sentences = new ArrayList<>();
		sentences.add("Do you know how to replace cellphone battery?");
		sentences.add("replace cellphone battery");
		sentences.add("replace battery of electrical torch");
		sentences.add("replace friend of electrical torch");
		sentences.add("I love John");
		sentences.add("replace cellphone battery");
		sentences.add("do you know how to go to school?");
		sentences.add("how do I go to school?");
		sentences.add("Do you know how much I love you?");
		sentences.add("I want to know how to replace cellphone battery");
		sentences.add("You need to know how to replace cellphone battery.");
		sentences.add("Here is how to replace cellphone battery.");
		sentences.add("I am teaching you how you can replace cellphone battery.");
		for(String sentence:sentences) {
			question2Goal(sentence);
			System.out.print("\n\n");
		}
		compareTwoStrings(sentences.get(0),sentences.get(1));
		compareTwoStrings(sentences.get(1),sentences.get(2));

	}

	public static void testMatchingGeneral() {

		/* ========================================
		 *   180613 Matching problem
		 ========================================== */
		List<String> rules = new ArrayList<>();
		Translator t = getTranslator();
		rules.add("if xx places the cover on yy, then yy doesn't has air's contact.");
		rules.add("if xx puts the cover on yy, then yy doesn't has air's contact.");
		String stringOne = "Bob placed the cover on the phone.";
//			Entity entityOne = t.translate(stringOne).getElement(0);

		Entity testOne = t.translate(rules.get(0)).getElement(0).getSubject().getElements().get(0);
		Entity testTwo = t.translate(rules.get(1)).getElement(0).getSubject().getElements().get(0);

		Mark.say(testOne);
		Mark.say(testTwo);

		LList<PairOfEntities> bindingsOne = StandardMatcher.getBasicMatcher().match(testOne, testTwo);
		if(bindingsOne != null) {
			Mark.say("      Bindings: ", bindingsOne);
		}

		LList<PairOfEntities> bindingsTwo = StandardMatcher.getBasicMatcher().match(testTwo, testOne);
		if(bindingsTwo != null) {
			Mark.say("      Bindings: ", bindingsTwo);
		}

	}


	public static void compareTwoStrings(String str1, String str2) {
		Entity ent1 = Translator.getTranslator().translate(str1).getElement(0);
		Entity ent2 = Translator.getTranslator().translate(str2).getElement(0);
		compareTwoEntities(ent1,ent2);
	}

	public static void compareTwoEntities(Entity ent1, Entity ent2) {
		Mark.say("ent1: ",ent1);
		Mark.say("ent2: ",ent2);
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(ent1, ent2);
		Mark.say(ent1.isEqual(ent2), " IS similar");
		Mark.say(!ent1.isEqual(ent2), " IS NOT similar");
		Mark.say(ent1.isDeepEqual(ent2), " IS equal");
		Mark.say(!ent1.isDeepEqual(ent2),  " IS NOT equal");
		Mark.say("Bindings", bindings, "\n\n");
	}


	// ==========================================
	//  For debugging: find if one sentence is a subtree of the other
	// ==========================================
	public static Boolean contains(List<String> items, String sentence) {
		for(String item: items) {
			if(Z.matchTwoSentences(sentence,item)!=null) {
				return true;
			}
		}
		return false;
	}
	
	public static Boolean contains(String string, String substring) {
		int after = replaceString(string, substring, "").length();
		return (string.length()!=after)?true:false;
	}

	public static LList<PairOfEntities> matchTwoSentences(String one, String two){
		Entity yourOne = t.translate(one).getElement(0);
		Entity yourTwo = t.translate(two).getElement(0);

		return matchTwoEntities(yourOne, yourTwo);
	}

	public static LList<PairOfEntities> matchTwoEntities(Entity yourOne, Entity yourTwo){
		if(isFun(yourOne)) {
			yourOne = yourOne.getSubject();
		}
		if(isFun(yourTwo)) {
			yourOne = yourTwo.getSubject();
		}
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(yourOne, yourTwo);
		return bindings;
	}


	// ==========================================
	//  get the list of common words in two strings
	// ==========================================
	public static List<String> findCommonWordsInStrings(String line1, String line2) {
		List<String> matches = new ArrayList<>();
		line1 = stripPunctuation(line1);
		line2 = stripPunctuation(line2);
		String[] elements1 = line1.split(" ");
		String[] elements2 = line2.split(" ");
		for(String element1:elements1) {
			for(String element2:elements2) {
				if(element1.equalsIgnoreCase(element2)) {
					matches.add(element1);
				}
			}
		}
		return matches;
	}

	public static List<String> findCommonEntitiesInStrings(String line1, String line2) {
		List<String> matches = new ArrayList<>();
		line1 = stripPunctuation(line1);
		line2 = stripPunctuation(line2);
		List<String> entities1 = getNounNames(line1);
		List<String> entities2 = getNounNames(line2);
		for(String entity1: entities1) {
			for(String entity2: entities2) {
				if(entity1.equalsIgnoreCase(entity2) && line1.contains(entity1) && line2.contains(entity2)) {
					matches.add(entity1);
				}
			}
		}
		Mark.say(matches);
		return matches;
	}




	// ===================================================================================
	//
	//   Debugging Tools:
	//
	// ===================================================================================


	public static void check(Boolean result) {
		Mark.say("",result);
	}

	public static Entity translateAgain(Entity entity) {
		return t.translate(g.generate(entity)).getElement(0);
	}

	public static void testTimer() {
		
		Translator t = Translator.getTranslator();
		Mark.say("initialized");
		
		timer.initialize();
		timer.lapTime(true, "1");
		Mark.say(t.translate("Can you make an apple salad for me?"));
		timer.lapTime(true, "2");
	}
	

	// ===================================================================================
	//
	//   Tools: String Manipulation
	//
	// ===================================================================================

	// ==========================================
	//  trim()
	// ==========================================
	
	// outputs a string capitalized
	public static String string2Capitalized(String line) {
		if(!line.isEmpty()) {
			line = Character.toUpperCase(line.charAt(0)) + line.substring(1);
		}
		return line;

	}

	// outputs a string capitalized without punctuations
	public static String stringPure(final String line) {
		String string = Character.toUpperCase(line.charAt(0)) + line.substring(1);
	    return string.trim().replaceAll("[^a-z\\sA-Z]","");
	}

	// remove all punctuation in a string
	public static String stripPunctuation(String input) {
		return input.replaceAll("[^a-z\\sA-Z]","");
	}
	
	// ==========================================
	//  replace()
	// ==========================================
	// replace only the last occurrence of an element in string
	public static String stringReplaceLast(String string, String old, String replacement){
		int index = string.lastIndexOf(old);
		if (index == -1) return string;
		return string.substring(0, index) + replacement + string.substring(index+old.length());
	}

	public static String replaceString(String string, String oldStr, String newStr) {
//		String c = newStr.substring(0,1);
//		if(c.toUpperCase()==c) {
//
//		}
//		string = string.toLowerCase();
		List<String> punctuations = Arrays.asList(",", ".","\"","?");
		if(string.startsWith(oldStr+" ")) {
			string = string.replace(oldStr+" ", newStr+" ");
		}
		if(string.endsWith(" "+oldStr)) {
			string = string.replace(" "+oldStr," "+newStr);
		}
		if(string.endsWith(" "+oldStr+"\".")) {
			string = string.replace(" "+oldStr," "+newStr);
		}
		for(String punctuation:punctuations) {
			string = string.replace(" "+oldStr+punctuation, " "+newStr+punctuation);
			string = string.replace(punctuation+oldStr+" ", punctuation+newStr+" ");
		}
		string = string.replace(" "+oldStr+" ", " "+newStr+" ");
		return string;
	}

	// replace occurances by binding, used when entity has multiple index in different sentences so cannot substitute
	public static String replaceByBindings(String value, LList<PairOfEntities> bindings) {
		int cc = 0;
		String[] parts = bindings.toString().split(" <");

		for (String part : parts) {
			String holder = part.substring(0,part.indexOf("-"));
			if(cc==0) {
				holder = holder.replace("(<", "");
			}
			part = part.substring(part.indexOf(",")+2,part.length());
			String object = part.substring(0, part.indexOf("-"));
			value = value.replace(holder, object);
		}
		return value;
	}

	// replace only word, not substring that is part of a word
	public static String replace(String sentence, String before, String after) {
		sentence = sentence.replace(", ", " , ").replace(".", " .")
				.replace("!", " !").replace("?", " ?");
		String[] words = sentence.split(" ");
		for(int i=0;i<words.length;i++) {
			if(words[i].equalsIgnoreCase(before))
				words[i] = after;
		}
		sentence = joinString(words," ").replace(" , ", ", ")
				.replace(" .", ".").replace(" !", "!").replace(" ?", "?");
		return sentence;
	}

	// replace only word, not substring that is part of a word
	public static String stringReplaceAll(String sentence, Map<String, String> anonymousMap) {
		sentence = sentence.replace(", ", " , ").replace(".", " .")
				.replace("!", " !").replace("?", " ?");
		String[] words = sentence.split(" ");
		for(int i=0;i<words.length;i++) {
			for(String key : anonymousMap.keySet() ) {
				if(words[i].equalsIgnoreCase(key)) words[i] = anonymousMap.get(key);
			}
		}
		sentence = joinString(words," ").replace(" , ", ", ")
				.replace(" .", ".").replace(" !", "!").replace(" ?", "?");
		return sentence;
		}

	
	// ==========================================
	//  +
	// ==========================================
	public static String joinString(String str, String ch) {
		String[] words = str.split(" ");
		return joinString(words, ch);
	}

	public static String joinString(String[] words, String ch) {
		String joined = String.join(ch, words);
		return joined;
	}

	// output if-then rule given two half-sentences
	public static String strings2Rules(String line1, String line2) {
		List<String> holders = Arrays.asList("yy","zz","vv");
		List<String> matches = findCommonEntitiesInStrings(line1,line2);
//		List<String> matches = findCommonWordsInStrings(line1,line2);
//		Mark.say(matches);
		String finals = "if xx "+line1+", then "+line2+".";
		for(int i=0;i<matches.size();i++) {
			finals=finals.replace(matches.get(i), holders.get(i));
		}
		finals = finals.replace(" the xx", " xx");
		finals = finals.replace(" the yy", " yy");
		finals = finals.replace(" the zz", " zz");
		return finals;
	}

	
	// ==========================================
	//  -
	// ==========================================
	public static int countOccuance(String sentence, String ch) {
		return sentence.length() - sentence.replace(ch, "").length();
	}

	public static String getRidOfYesNo(String humanEnglish, String humanTone){
		humanEnglish = humanEnglish.toLowerCase();
		if(humanTone==RecipeExpert.NO && humanEnglish.contains("no,")) {
			humanEnglish = humanEnglish.replace("no,", "");
		}
		if(humanTone==RecipeExpert.YES && humanEnglish.contains("yes,")) {
			humanEnglish = humanEnglish.replace("yes,", "");
		}
		return humanEnglish;
	}

	// get the first sentence in a long string of sentences
	public static String getFirstSentence(String sentences) {
		if(Z.countOccuance(sentences,".")>=2) {
			sentences = sentences.substring(sentences.indexOf(".")+2, sentences.length());
		}
		return sentences;
	}

	// outputs a string between two strings
	public static String substringBetween(String str, String open, String close) {
		if (str == null || open == null || close == null) {
			return null;
			}
		int start = str.indexOf(open);
		if (start != -1) {
			int end = str.indexOf(close, start + open.length());
			if (end != -1) {
				return str.substring(start + open.length(), end);
			}
		}
		return null;
	}

	// check if string contains a number
	public static Boolean containsNumber(String string) {
		Pattern p = Pattern.compile("([0-9])");
		Matcher m = p.matcher(string);

		return m.find();
	}

	public static String[] splitAll(String string) {
		return string.split("[.,!?:;\" ]+\\s*");
	}

	
	
	
	// used for parsing paragraphs of texts
	// split a list of strings into a list of individual sentences
	public static void textString2Sentences() {
		List<String> sentences = new ArrayList<String>();
		sentences.add("Mark collects the pile of silver bags. He carries them inside. Mark stands in the kitchen, surrounded by silver bags. He fills a large container with water from the Reclaimer. He dumps in the contents of the compost bin. Then he stares at the bags. He does not look happy. He tears open a bag and dumps the contents into the bin. Then he tears open another bag. As he does so, he starts to GAG. Mark scoops Martian dirt into a container with a small shovel. He carries the container to the airlock. Mark enters the Hab, dumps his container of dirt into a corner where he’s cleared an empty area. Mark enters with another container. There’s now a HUGE PILE of dirt in the corner. Mark has spread the dirt over a third of the Hab floor. He stares at the compost bin. Eyes it like it’s his nemesis. Then he takes a deep breath. Opens the bin. Begins dumping it over the Martian dirt. He can’t hold his breath forever. He breathes eventually. Oh god, that’s horrible. Mark cuts each potato into four quarters, making sure each quarter has at least two eyes. He begins planting each potato quarter in nice, orderly rows. The ENTIRE HAB is now covered in SOIL. Not just the floor. Mark has cleared every available surface -- bunks, countertops, table -- and covered it with his dirt.");
		for (String sentence : sentences) {
			for (String ss : strings2sentences(sentence)) {
				Mark.night(ss);
			}
		}
	}
	
	public static List<String> strings2sentences(List<String> strings) {
		List<String> sentences = new ArrayList<String>();
		for (String string : strings) {
			for (String sentence : strings2sentences(string)) {
				sentences.add(sentence);
			}
		}
		return sentences;
	}
	
	public static List<String> strings2sentences(String string) {
		List<String> sentences = new ArrayList<String>();
		String whole = string;
		for (String sentence : string.split("[.!?]+\\s*")) {
			whole = whole.substring(whole.indexOf(sentence));
			char mark = whole.charAt(sentence.length());
			sentences.add(sentence + mark);
		}
		return sentences;
	}



	// ===================================================================================
	//
	//   Tools: List Manipulation
	//
	// ===================================================================================

	// output the number of items in a list start with a string pattern
	public static int countListItemStartsWith(List<String> items, String starter){
		int count = 0;
		for(String item:items) {
			if(item.startsWith(starter)) count++;
		}
		return count;
	}
	
	public static void testReverse() {
		List<String> items = new ArrayList<>();
		items.add("1");
		items.add("2");
		items.add("3");
		Mark.night(items);
		Mark.night(reverse(items));
	}

	// reverse the items in a list
	public static List<String> reverse(List<String> items){
		List<String> newItems = new ArrayList<>();
		for(int i = items.size();i>0;i--) {
			newItems.add(items.get(i-1));
		}
		return newItems;
	}

	// make each item in a list to start with lower case letter
	public static List<String> listToLower(List<String> strings){
	    ListIterator<String> iterator = strings.listIterator();
	    while (iterator.hasNext()){
	        iterator.set(iterator.next().toLowerCase());
	    }
	    return strings;
	}

	// split story elements such that each sentence has one story element
	public static List<String> getSeparated(List<String> items){
		List<String> newItems = new ArrayList<>();
		for(String item:items) {
			newItems.addAll(toPresentTense(item));
		}
		return newItems;
	}

	// ==========================================
	//  modifying strings read from files
	// ==========================================
	// clear all comments, blanks, and special Markers in a list of story elements
	public static List<String> listRemoveComment(List<String> strings){
		for (Iterator<String> iter = strings.listIterator(); iter.hasNext(); ) {
		    String a = iter.next().trim();
		    if (a.startsWith("//") || a.startsWith("Start experiment")
		    		|| a.startsWith("Start story ") || a.startsWith("The end")
		    		|| a.startsWith("Insert file ") || a.length()<3) {
		        iter.remove();
		    }
		}
		return strings;
	}

	// remain only the sentences between two lines
	public static List<String> listElementsBetween(List<String> strings, String start, String end){

		List<String> newStrings = new ArrayList<>();
		Boolean findStart = false;
		Boolean findEnd = false;
		start = start.toLowerCase();
		end = end.toLowerCase();
		for(String string: strings) {
			if(findStart && string.toLowerCase().startsWith(end)) {
				findEnd = true;
				findStart = false;
			}

			if(findStart && !findEnd) {
				newStrings.add(string);
			}

			if(!findStart && string.toLowerCase().startsWith(start)) {
				findStart = true;
				findEnd = false;
			}
		}
		return newStrings;
	}

	// remain only the story sentences in a list of story elements
	public static List<String> listStoryElements(List<String> strings){
		return listElementsBetween(strings,"start story","the end");
	}

	// remain only the declration before story sentences for translator internalization
	public static List<String> listAssignmentElements(List<String> strings){
		List<String> declarations = new ArrayList<>();
		List<String> sentences = listElementsBetween(strings,"Start experiment","Start story titled");
		for (String declaration: sentences) {
			if (!declaration.contains(Markers.INSERT_FILE)) {
				declarations.add(declaration);
			}
		}
		return declarations;
//		return listElementsBetween(strings,"// start declarations","// end declarations");
	}



	// ===================================================================================
	//
	//   Tools: File manipulation
	//
	// ===================================================================================

	// delete a file if exist
	public static Boolean deleteFile(String fileName) {
		File file = new File(fileName);
		try {
			return Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// replace all word with its pair according to a predefined list
	public static String wordReplace(String mine, String fileName) {
		Map <String, String> verbs = new HashMap<String, String>();
		if(fileName=="")
			fileName = "students/zhutianYang/zVerbsPastPresent.exc";
		try {
			List<String> verbPairs = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
			for(String verbPair: verbPairs) {
				String[] temp = verbPair.split(" ");
				if(temp.length==2) verbs.put(temp[0],temp[1]);
			}
			Mark.say("before: ", mine);
			for(String key : verbs.keySet()) {
				if (mine.contains(" "+key+" ")) {
					mine = mine.replace(key, verbs.get(key));
				}
			}
			mine = mine.replace("her ", "");
			mine = mine.replace("his ", "");
			mine = mine.replace("She ", "");
			mine = mine.replace("He ", "");
			mine = mine.replace(" she ", " ");
			mine = mine.replace(" he ", " ");
			Mark.say("after: ", mine);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mine;
	}

	// read stories by FILENAME; read only the story sentences in a story file
	public static Map<String, List<String>> readStoryFromFile(String name) {
		Map<String, List<String>> toReturn = new HashMap<>();
		List<String> sentences = new ArrayList<String>();
		List<String> assignments = new ArrayList<String>();
		String storyPath = storiesPath+name+".txt";
		if(name.startsWith("Stratagem")) {
			storyPath = "corpora/zStoriesOfWar/"+name+".txt";
		} else if (name.contains("/")) {
			storyPath = name;
		}
		try {
			sentences = new ArrayList<>(Files.readAllLines(Paths.get(storyPath), StandardCharsets.UTF_8));
			sentences.removeAll(Arrays.asList("", null));
			assignments = Z.listAssignmentElements(sentences);
			sentences = Z.listStoryElements(sentences);
			sentences = Z.listRemoveComment(sentences);
			toReturn.put(SENTENCES, sentences);
			toReturn.put(ASSIGNMENTS, assignments);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Mark.say("sentences",sentences);
//		Mark.say("assignments",assignments);
		return toReturn;
	}

	// read stories by FILENAME; read only the story sentences in a story file
	public static List<String> readAssignmentsFromFile(String name) {
		List<String> sentences = new ArrayList<String>();
		if(name.startsWith("Stratagem")) {
			storiesPath = "corpora/zStoriesOfWar/";
		}
		try {
			sentences = new ArrayList<>(Files.readAllLines(Paths.get(storiesPath+name+".txt"), StandardCharsets.UTF_8));
			sentences.removeAll(Arrays.asList("", null));
			sentences = Z.listAssignmentElements(sentences);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sentences;
	}
	
	// used by HowTo book reader
	public static List<String> getStoryText(String fileName) {
		List<String> lines = null;
		try {
			lines = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
			lines = Z.listRemoveComment(lines);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return lines;
	}
	
	public static String listToStory(List<String> lines) {
		String result = "";
		for(String line:lines) {
			result += line + " ";
		}
		return result;
	}

	// read stories by CODE
	public static List<String> readStory(String name) {
		List<String> sentences = new ArrayList<String>();
		if (name == "Alice") {
			sentences.add("Alice forgot to take out her Nokia phone from her pants.");
			sentences.add("Then, she put her pants in the working machine.");
			sentences.add("The phone got all wet and stopped working.");
			sentences.add("She decided to replace the battery of the phone first.");
			sentences.add("She quickly removed the cover of the phone and collects the old battery from the phone.");
			sentences.add("Then, she bought the flowers online.");
			sentences.add("Then, she bought the replacement battery online.");
			sentences.add("Later, the replacement battery arrived and she inserted it into the phone.");
			sentences.add("She installed the cover of the phone.");
			sentences.add("She recharged her phone for 4 hours. ");
			sentences.add("Bravo!");
			sentences.add("The phone started working again!");

		} else if (name == "Bob"){

			sentences.add("Bob is asked to change the battery of a Samsung phone.");
			sentences.add("First, he found a replacement battery on his workstation.");
			sentences.add("Second, he found an apple on his workstation.");
			sentences.add("Then, he slid down the cover of the phone.");
			sentences.add("He removed the old battery from the phone and put the replacement battery into the phone.");
			sentences.add("Lastly, he slid up the cover of the phone.");
			sentences.add("The problem is solved. The client was happy!");

		} else if (name == "Carl"){

			sentences.add("Carl wanted to play the soccer ball game for the glory of MIT.");
			sentences.add("But there was no vacancy in the team.");
			sentences.add("Yesterday, one team member graduated and left the team.");
			sentences.add("Carl joined the team and played his role.");

		} else if (name == "David") {

			sentences.add("Monster David ate all the batteries in the phone.");

		} else if (name == "Eager"){

			sentences.add("Miss Eager put a battery in the phone.");

		} else if (name == "Frank") {
			sentences.add("Frank wanted to play the soccer ball game for the glory of MIT.");
			sentences.add("But there was no vacancy in the team.");
			sentences.add("Yesterday, one team member got sick and had to drop out of the team.");
			sentences.add("David joined the team to play his role.");

		} else if (name == "XX"){

			sentences.add("Miss Eager removed the old battery from the phone.");
			sentences.add("Miss Eager put a replacement battery in the phone.");

		} else if (name == "YY"){
			sentences.add("David slides up the cover of the phone.");
			sentences.add("David puts a replacement battery in the phone.");
			sentences.add("David removes the old battery from the phone.");

		} else {
			Mark.say("No story named "+ name);
		}
		return sentences;
	}

	// output list of rules from file, comments removed
	public static List<String> readRulesInFile(String filePath){
		List<String> rules = new ArrayList<>();
		try {
			rules = new ArrayList<>(Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8));
			rules.removeAll(Arrays.asList("", null));
			rules = Z.listToLower(rules);
			rules = Z.listRemoveComment(rules);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rules;
	}

	// add a new if-then rule, i.e. transition rule to file
	public static void writeRuleToFile(String rule, String transitionRulesFile) {
		if(rule.endsWith("..")) {
			rule.replace("..",".");
		}else if(!rule.endsWith(".")) {
			rule = rule + ".";
		}
		try {
			Mark.say("added new rule: "+rule);
			FileWriter writer = new FileWriter(transitionRulesFile, true);
			writer.write("\r\n// learned rule at "+ new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime())+"\r\n");
			writer.write(rule+"\r\n \r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// print the output text file
	public static String printTXTFile(String fileName){
		
		if(fileName.length()==0) return "";

		String results = "";
		Mark.say(!suppressPrinting, "Printing: "+fileName);
		File file = new File(fileName);
		@SuppressWarnings("resource")
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String st;
			try {
				while ((st = br.readLine()) != null) {
					if(!suppressPrinting) System.out.println(st);
					results += st+"\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
			Mark.red(!suppressPrinting,"file does not exist yet: "+fileName);
		}
		Mark.say(!suppressPrinting,results);
		toPageHistorian = results;
		return results;
	}

	// print the output text file
	public static void printTXTFileInInnerese(String fileName) throws IOException {

		Translator t = getTranslator();
		Mark.say("Printing: "+fileName);
		File file = new File(fileName);
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		while ((st = br.readLine()) != null) {
			if(st.startsWith("//") || st.isEmpty()) {
				System.out.println(st);
			} else {
				if(isTranslatable(st)) {
					System.out.println(t.translate(st).getElement(0));
				} else {
					System.out.println("cannot be translated");
				}

			}

		}

	}

	public static String printList(List<String> list, int spacing) {
		String results = "";
		if(list.size()==0) {
			Mark.say(!suppressPrinting, "Empty list");
		} else {
			for(String item:list) {
				if(!suppressPrinting) System.out.println(item);
				results += item+"\n";
				for(int i=0;i<spacing;i++) {
					results += "\n";
				}
			}
		}
		if(!suppressPrinting) System.out.println("=======================================================");
		return results;
	}
	
	public static String printList(List<String> list) {
		return printList(list,0);
	}
	
	public static String getDate() {
		return new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
	}

	

	/* ========================================
	 *   180713 No property of modal "can"
	 ========================================== */
//	Entity entity = t.translate("I can spread rumor to make King believe that Yuan has excessive power.").getElement(0);
//	understand(entity);
//	entity = entity.getSubject();
//	understand(entity);

//	testMatching();
//	testUnderstandingSentences();

	/* ========================================
	 *   180607 Matcher with features
	 ========================================== */
//	String xx = "I am a new battery";
//	String yy = "she is an old battery.";
//	testMatchingUseFeature(xx,yy,true);
//	testMatchingUseFeature(xx,yy,false);

	/* ========================================
	 *   180613 Parser
	 ========================================== */
//	isTranslatable("yes");
//	isTranslatable("no");
//	isTranslatable("cry");
//	isTranslatable("eat");
//	isTranslatable("drink");
//	isTranslatable("laugh");

	/* ========================================
	 *   180615 Parser
	 ========================================== */
//	isTranslatable("xx may kill");
//	isTranslatable("killing can stop bad things");
//	isTranslatable("if killing can stop bad things, then xx may kill.");
//	isTranslatable("good people can stop bad things");
//	isTranslatable("if good people can stop bad things, then xx may kill.");

	/* ========================================
	 *   180616 Parser
	 ========================================== */
//	String ruleOne = "apply the knowledge you have learned";
//	try {
//		printInnereseTree(ruleOne);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	understand(ruleOne);

	/* ========================================
	 *   180616 Generator
	 ========================================== */
//	String ruleOne = "I love you";
//	Entity entityOne = Translator.getTranslator().translate(ruleOne).getElement(0);
//	Mark.say(entityOne.getPropertyList());
//	entityOne.addProperty("modal", "may");
//	Mark.say(entityOne.getPropertyList());
//	ruleOne = Generator.getGenerator().generate(entityOne);
//	understand(ruleOne);
//	Mark.say(ruleOne);




	/* ========================================
	 *   Testing Templates
	 ========================================== */
//	Translator t = getTranslator();
//	Mark.say(t.translate("killing can stop bad things"));
//	try {
//		printInnereseTree("killing can stop bad things");
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

//	String ruleOne = "having a rest";
//	ruleOne = "xx " + ruleOne;
//	Entity entityOne = Translator.getTranslator().translate(ruleOne).getElement(0);
//	entityOne.removeFeature("ing");
//	understand(entityOne);
//	ruleOne = RuleGenerator.setSubjectXX(entityOne);
//	Mark.say(ruleOne);

//	try {
//		printInnereseTree(ruleOne);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

//	Translator t = Translator.getTranslator();
//	List<String> steps = new ArrayList<>();
//	steps.add("remove the cover from the phone");
//	steps.add("buy the replacement battery");
//	steps.add("insert the replacement battery into the phone");
//	steps.add("put the cover on the phone");
//	steps.add("found a replacement battery");
//	steps.add("slide down the cover from the phone");
//	steps.add("remove the old battery from the phone");
//	steps.add("place the cover on the phone");
//
//	transitionRules = ZRelation.initializeRules();
//	List<String> unknownSteps = Z.findUnknownSteps(steps, new ArrayList<>());

//	String step = "put ham into cup";
//	String rule = "if xx put yy into zz, yy is in zz.";
//	t.internalize("xx is a thing");
//	t.internalize("yy is a thing");
//	t.internalize("zz is a thing");
//	Entity thisKnownStep = Translator.getTranslator().translateToEntity(rule);
//	Entity thisIntention = Translator.getTranslator().translateToEntity(step);
//
//	Entity antecedent = thisKnownStep.getSubject().getElements().get(0);
//	Entity consequent = thisKnownStep.getObject();
//	LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, thisIntention);
//	if(bindings!=null) {
//		Entity result = Substitutor.substitute(consequent, bindings);
//		Mark.say(result);
//		Mark.say(Generator.getGenerator().generate(result));
//	}


//	testGetCommon();
//	String hi = "Alice has trouble";
//	String ha = "Bob has trouble";
//	Entity hhi = getTranslator().translate(hi);

//	Mark.say(strings2Rules("insert the replacement battery into the phone", "the replacement battery is on the phone."));
//	testWarStory("Stratagem3_Kill with borrowed knife_test",transitionRulesFile1);
//	testWarStory("Stratagem3_Kill with borrowed knife_Huang",transitionRulesFile1);
//	testWarStory("Stratagem3_Kill with borrowed knife_Cao",transitionRulesFile1);
//	testGetCommonWar("Stratagem3_Kill with borrowed knife_Huang",
//			"Stratagem3_Kill with borrowed knife_Huang2");

//	Mark.say(matchTwoSentences("who can kill Yuan","who kills Yuan"));
//	Mark.say(matchTwoSentences("who can kill yuan", "King can kill Yuan."));


//	String string = "";
//	string = StratagemExpert.yesNoQuestion("you can kill him");
//	string = StratagemExpert.yesNoQuestion("you cannot kill him");
////	string = StratagemExpert.yesNoQuestion("you have enough money");
////	string = StratagemExpert.yesNoQuestion("she doesn't have enough money");
////	string = StratagemExpert.yesNoQuestion("the weather is bad");
////	string = StratagemExpert.yesNoQuestion("king believes that yuan is disloyal");
//
//	string = StratagemExpert.whatIfQuestion("King does not trust xx");
//	string = StratagemExpert.howQuestion("she doesn't have enough money");
////	string = StratagemExpert.howQuestion("the weather is bad");
//	string = StratagemExpert.howQuestion("king believes that yuan is disloyal");
//


//	String string = "how could king believe in him";
//	Entity entity = t.translate(string);
//	String triples = g.generateTriples(entity);
//	Mark.say(getVerbInTriples(triples));

//	Mark.say(matchTwoSentences("The king is bad", "kings are bad"));

//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "No, he doesn't."));
//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "Yes, he does."));
//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "I don't know"));
//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "Then, Official can kill Yuan."));
//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "If king trusts yuan, then I will make king distrust yuan"));
//	Mark.say(StratagemExpert.responseWhatIf("what if king trusts xx?", "If I make king distrust yuan, then king will not trust yuan"));
//	Mark.say(true,StratagemExpert.justifyWhatIf("what if king doesn't trust xx?", "he does."));
//	Mark.say(true,StratagemExpert.justifyWhatIf("what if king doesn't trust xx?", "he trust."));
//	Mark.say(true,StratagemExpert.justifyWhatIf("what if king has money?", "he has."));
//	Mark.say(true,StratagemExpert.justifyWhatIf("what if king doesn't have money?", "he have."));

//	Entity entity = t.translate("If king trust yuan, then I will make king distrust yuan").getElement(0).getObject();
//	Mark.say(getNounEntity(entity));
//	Mark.say(true, Z.isNegation("king trust yuan", "king will not trust yuan"));

//	try {
//		printInnereseTree("the lady can make Yuan fall in love with her and she can poison him.");
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

//	String goal = "I can spread rumor to make King believe that Yuan has excessive power";
//	Entity goalEntity = t.translate(goal);
//	String english = StratagemExpert.generateGoodEnglish(goalEntity);
//	Mark.say(english);

//	// TODO
//	BedtimeStoryLearner bt = new BedtimeStoryLearner();
//	List<String> sentences = BedtimeStoryLearner.getTestStory();
//	for(String humanEnglish:sentences) {
//		Mark.say(humanEnglish);
////		understand(humanEnglish);
//		toPresentTense(humanEnglish);
////		String subject = bt.getSubject(humanEnglish);
////		String object = bt.getObject(humanEnglish);
////		System.out.println("         subject: "+subject);
////		System.out.println("          object: "+object);
////		Entity entity = t.translate(sentence);
//
//	}
//	Translator t = Translator.getTranslator();
//	t.internalize("yuan is a person");
//	t.internalize("Yuan is a person");
//	Mark.say(t.translate("Tai made Chong believe that Yuan is disloyal").getObject());
//	understand();

//	Entity entity = t.translate(sentences.get(0)).getElement(0);
//	List<Entity> entities = new ArrayList<>();
//	entities.addAll(entity.getChildren());
//	entity = entities.get(0).getSubject();
//	understand(entity);
//	String string = .getName();
//	string = string.substring(0,string.indexOf("-"));
//	Mark.say(g.generate(entity.getSubject()));

//	Entity sentenceEntity = t.translateToEntity("Yuan fought against Huang to protect Ming Dynasty.");
//	Entity ruleEntity = t.translateToEntity("xx fights against yy.");
//
//	LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(ruleEntity, sentenceEntity);
//	if(bindings!=null) {
//		Mark.say("binded");
//	}

//	try {
//		printInnereseTree(string);
//	} catch (IOException e) {
//		e.printStackTrace();
//	}

//	entity.getSubject().setSubject(new Entity("you"));
//	understand(entity);
//	Mark.say(StratagemExpert.howQuestion("I make king distrust yuan"));
//	String subject = g.generate(entity.getSubject());
//	String object = g.generate(entity.getObject());
//	Mark.say("Subject",isNegation(subject, goal));
//	Mark.say("Object", isNegation(object, goal));
//	understand(entity);

//	Mark.say(replaceString("Tom loves me but I hate Tom","Tom", "Hanks"));

//	String string = "i can bribe official";
//	Entity entityAnswer = t.translate(string);
//	Entity entity = entityAnswer.getElement(0);
//	Mark.say(entity.getProperty(Markers.MODAL));
//	.equals(Markers.CAN);

//	String string = "i can spread rumor to make King believe that Yuan has excessive power";
//	Entity entityAnswer = t.translate(string);
////	entityAnswer.setSubject(new Entity("you"));
//	Mark.say(g.generate(entityAnswer));

//	String string = "";
//	string = "How do I make Yuan become dead";
//	string = "How could you ask Winston?";

//	Entity entityAnswer = t.translate(string);
//	Mark.say(entityAnswer);
//	for(int i = entityAnswer.getElements().size(); i>0;i--) {
//		understand(entityAnswer);
//		Mark.say(g.generate(entityAnswer));
//	}

//	String string = "How do you make an old fashioned cocktail?";
//	understand(string);

//	String string = "Can you make a cocktail?";
//	understand(string);
//	Mark.say(question2Goal(string));
//
//	string = "how to make a cocktail?";
//	understand(string);
//	Mark.say(question2Goal(string));

//	String humanEnglish = "Move the bowl to the center";
//	Entity entity = t.translateToEntity(humanEnglish);
//	Mark.say(entity);
//	Mark.say(sentence2Negation(humanEnglish));
//
//	humanEnglish = "I like sugar";
//	Mark.say(sentence2Negation(humanEnglish));
//
//	humanEnglish = "I don't like sugar";
//	Mark.say(sentence2Negation(humanEnglish));

//	Mark.say("ha",Z.stringIsHowTo());
	



	// ===================================================================================
	//
	//   Tests for Z Relation
	//
	// ===================================================================================

	public static void testGetCommon() {

		List<List<ZRelation>> stories = new ArrayList<>();
		List<String> learnedSteps =  new ArrayList<>();

		String story = "Alice replaces cellphone battery";
		List<ZRelation> oneStory = ZRelation.story2ZRelations(story,"");
		stories.add(oneStory);
		ZRelation.listPrintRelations(oneStory);
		learnedSteps = ZRelation.listPrintStepsPresent(oneStory);

		story = "Bob replaces cellphone battery";
		List<ZRelation> twoStory = ZRelation.story2ZRelations(story,"");
		stories.add(twoStory);
		ZRelation.listPrintRelations(twoStory);
		learnedSteps = ZRelation.listPrintStepsPresent(twoStory);

		List<List<ZRelation>> manyStories = ZRelation.listItemToEnd(stories, 0);
		learnedSteps = ZRelation.listPrintStepsPresent(ZRelation.getCommonSubgoals(manyStories));
		printList(learnedSteps);

		manyStories = ZRelation.listItemToEnd(stories, 1);
		learnedSteps = ZRelation.listPrintStepsPresent(ZRelation.getCommonSubgoals(manyStories));
		printList(learnedSteps);

	}

	public static void testWarStory(String name,String transitionRulesFile) {
		List<ZRelation> relations = ZRelation.story2ZRelations(name, transitionRulesFile);
		ZRelation.listPrint(relations);
	}

	public static void testGetCommonWar(String nameOne, String nameTwo) {

		List<List<ZRelation>> stories = new ArrayList<>();
		List<String> learnedSteps =  new ArrayList<>();

		List<ZRelation> oneStory = ZRelation.story2ZRelations(nameOne,transitionRulesFile1);
		stories.add(oneStory);
		ZRelation.listPrintRelations(oneStory);
//		learnedSteps = ZRelation.listPrintStepsPresent(oneStory);

		List<ZRelation> twoStory = ZRelation.story2ZRelations(nameTwo,transitionRulesFile1);
		stories.add(twoStory);
		ZRelation.listPrintRelations(twoStory);
//		learnedSteps = ZRelation.listPrintStepsPresent(twoStory);

		List<List<ZRelation>> manyStories = ZRelation.listItemToEnd(stories, 0);
		learnedSteps = ZRelation.listPrintRelations(ZRelation.getCommonSubgoals(manyStories));
		printList(learnedSteps);

		manyStories = ZRelation.listItemToEnd(stories, 1);
		learnedSteps = ZRelation.listPrintStepsPresent(ZRelation.getCommonSubgoals(manyStories));
		printList(learnedSteps);

	}




	// ===================================================================================
	//
	//    Hell: for statistics
	//
	// ==========================================

//	public static double getVariance(List<Double> data) {
//
//		double[] array = new double[data.size()];
//        for (int i = 0; i < array.length; i++)
//        		array[i] = data.get(i);
//
//        double sum = 0.0;
//        for(double a : data)
//            sum += a;
//        double mean = sum/array.length;
//
//        double temp = 0;
//        for(double a :data)
//            temp += (a-mean)*(a-mean);
//        double variance = temp/(array.length-1);
//
//		return variance;
//	}
//
//	public class ZStatistics {
//	    double[] data;
//	    int size;
//
//	    public ZStatistics(double[] data) {
//	        this.data = data;
//	        size = data.length;
//	    }
//
//	    double getMean() {
//	        double sum = 0.0;
//	        for(double a : data)
//	            sum += a;
//	        return sum/size;
//	    }
//
//	    double getVariance() {
//	        double mean = getMean();
//	        double temp = 0;
//	        for(double a :data)
//	            temp += (a-mean)*(a-mean);
//	        return temp/(size-1);
//	    }
//
//	    double getStdDev() {
//	        return Math.sqrt(getVariance());
//	    }
//
//	    public double median() {
//	       Arrays.sort(data);
//
//	       if (data.length % 2 == 0) {
//	          return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
//	       }
//	       return data[data.length / 2];
//	    }
//	}



	// ==========================================
	//
	//   Hell for messuring word distance
	//
	// ==========================================

	//import edu.cmu.lti.lexical_db.ILexicalDatabase;
	//import edu.cmu.lti.lexical_db.NictWordNet;
	//import edu.cmu.lti.ws4j.RelatednessCalculator;
	//import edu.cmu.lti.ws4j.impl.HirstStOnge;
	//import edu.cmu.lti.ws4j.impl.JiangConrath;
	//import edu.cmu.lti.ws4j.impl.LeacockChodorow;
	//import edu.cmu.lti.ws4j.impl.Lesk;
	//import edu.cmu.lti.ws4j.impl.Lin;
	//import edu.cmu.lti.ws4j.impl.Path;
	//import edu.cmu.lti.ws4j.impl.Resnik;
	//import edu.cmu.lti.ws4j.impl.WuPalmer;
	//import edu.cmu.lti.ws4j.util.WS4JConfiguration;
	//
//	public static void testWordNetSimilarity() {
//		String word1 = "like";
//		String word2 = "love";
//		Mark.say(wordnetSimilarity(word1, word2));
//	}
//
//	public static double wordnetSimilarity(String w1, String w2) {
//		ILexicalDatabase db = new NictWordNet();
//		RelatednessCalculator lin = new Lin(db);
//	    RelatednessCalculator wup = new WuPalmer(db);
//	    RelatednessCalculator path = new Path(db);
//
//        System.out.println(lin.calcRelatednessOfWords(w1, w2));
//        System.out.println(wup.calcRelatednessOfWords(w1, w2));
//        System.out.println(path.calcRelatednessOfWords(w1, w2));
//
//		WS4JConfiguration.getInstance().setMFS(true);
//		double s = new WuPalmer(db).calcRelatednessOfWords(w1, w2);
//		return s;
//	}
//
//	public static void testDSICOSimilarity() {
//		String word1 = "like";
//		String word2 = "love";
//		Mark.say(discoSimilarity(word1, word2));
//	}
//
//	public static double discoSimilarity(String w1, String w2) {
//		DISCO disco;
//		double sim = 0.0;
//		try {
//			Mark.say("starting to load DISCO vectors");
//			disco = DISCO.load("students/zhutianYang/enwiki-20130403-word2vec-lm-mwl-lc-sim.denseMatrix");
////			Mark.say("starting to compare semantic similarity");
////			sim = disco.semanticSimilarity(w1, w2, DISCO.getVectorSimilarity(SimilarityMeasure.COSINE));
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (CorruptConfigFileException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
////		Mark.say("similarity between "+w1+" and "+w2+": "+sim);
////		// get word vector for "Haus" as map
////		Map<String,Float> wordVectorHaus = disco.getWordvector("Haus");
////		// get word embedding for "Haus" as float array
////		float[] wordEmbeddingHaus = ((DenseMatrix) disco).getWordEmbedding("Haus");
////		// solve analogy x is to "Frau" as "König" is to "Mann"
////		List<ReturnDataCol> result = Compositionality.solveAnalogy("Frau", "König", "Mann", disco);
////		Mark.say(result);
//		return sim;
//	}
//
//	public static List<String> sentenceToCondition(Entity innerese) {
//	String condition = "";
//	String todo = "";
//	List<String> types = new ArrayList<>(innerese.getTypes());
//	if(types.contains(Markers.IF_MARKER)) {
//
//		// get condition
//		Entity entity = innerese.getSubject();
//		String generated1 = g.generate(entity).toLowerCase().replace(".", "");
//		types = new ArrayList<>(entity.getTypes());
//
//		if(types.contains(Markers.CONJUNCTION)) {
//
//			if(entity.getElement(0).hasProperty(Markers.PROGRESSIVE)) {
//				condition = "you " + generated1;
//			} else {
//
//				List<Entity> entities1 = new ArrayList<>(entity.getChildren());
//				if(entities1.get(0).getTypes().contains(Markers.MENTAL_STATE_MARKER)) {
//					generated1 = "you " + generated1;
//				}
//				if(t.translate(generated1).getElement(0).hasProperty(Markers.IMPERATIVE)) {
//					generated1 = "you " + generated1;
//				}
//				condition = Markers.IF_MARKER+" "+generated1;
//			}
//		}
//
//		// get action
//		entity = innerese.getObject();
//		todo = g.generate(entity).toLowerCase().replace(".", "");
//		Mark.say(todo);
//
//	} else if (types.contains(Markers.WHEN_QUESTION)){
//		Entity entity = innerese.getObject();
//		String generated1 = Generator.getGenerator().generate(entity).toLowerCase();
//		types = new ArrayList<>(entity.getTypes());
//		if(types.contains(Markers.MENTAL_STATE_MARKER)) {
//			generated1 = "you " + generated1;
//		}
//		if(t.translate(generated1).getElement(0).hasProperty(Markers.IMPERATIVE)) {
//			generated1 = "you " + generated1;
//		}
//		condition = Markers.IF_MARKER+" "+generated1.replace(".", "");
//	}
//	List<String> bes = Arrays.asList("is", "was", "are", "were");
//	for(String be:bes) {
//		if(condition.endsWith(be)) {
//			condition = condition.replace(" "+be, "");
//			condition = condition.replace("if", "if there "+be);
//		}
//	}
//	Mark.say(condition);
//	return Arrays.asList(condition, todo);
//}

}

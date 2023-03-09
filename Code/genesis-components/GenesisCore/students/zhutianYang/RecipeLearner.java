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

import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.NewTimer;
import utils.Z;
import utils.tools.JFactory;

/**
 * @author zhutian Yang, March 2018
 * @time updated 8 Jun
 */

public class RecipeLearner extends AbstractWiredBox {

	// Program options
	public static Boolean DEBUG = true;
	public static final String COMMENT = "Generated on date by students/zhutianYang/RecipeLearner.java";
	public static Boolean USE_COMMENT = false;

	// Long-term memory for knowledge
	private static boolean INITIALIZED = false;
	public static String filePath = "corpora/zMemory/";
//	public static String expertKnowledgeFile = "corpora/zMemory/ZZ Expert knowledge.txt";
//	public static String hintsGoalFile = "students/zhutianYang/zHintsGoal.txt"; // "Do you know how to ...?"
//	public static String starterTeachCriticalFile = "students/zhutianYang/zstarterTeachCritical.txt"; // "you have to ..."
//	public static String knownStepsFile = "students/zhutianYang/zKnownSteps.txt"; // "try ..."
//	public static String transitionRulesFile = "students/zhutianYang/zTransitionRulesBlocks.txt";
	public static List<String> hintsGoal;
	public static List<String> starterTeachCritical;
	public static List<String> knownSteps;
	public static List<String> transitionRules;

	// Short-term memory for learning
	public static String firstGoal = "";
	public static String testFileName = "";
	public static String hasWrittenGoal = "";
	public static String stepsFileNameString = "dustbin";
	public static String stepsFileName = filePath + "dustbin.txt";
	public static String goal = "";
	public static String condition = "";
	
	public static Boolean skipOneProblemIntentionStep = false;
	public static Boolean startNewRecipe = false;
	public static Boolean hasWrittenTester = false;
	public static Boolean missingCondition = false;
	public static Boolean hasGenerated = false;
	public static Boolean addStepToEnd = false;
	public static Translator t = Translator.getTranslator();
	public static Generator g = Generator.getGenerator();
	public static NewTimer timer = NewTimer.zTimer;

	// Box connections
	public static final String FROM_START = "from reading story";
	public static final String TO_COMMENTARY = "story learner to commentary";
	
	// 
	public static Entity currentSubject;
	public static Entity currentObject;
	public static Stack<String> questions = new Stack<String>();


	public RecipeLearner() {
		super("Story Learner");
		Connections.getPorts(this).addSignalProcessor(FROM_START, this::makeKnowledgeFile);
	}

	public static void main(String[] args) throws IOException {
//		loadLongTermMemory();
		
//		Mark.say(t.translate("in order to make hard boiled eggs, you make hard boiled eggs"));
//		Entity entity = t.translateToEntity("in order to make hard boiled eggs, you make hard boiled eggs");
//		Z.understand(entity.getSubject().getElement(0).getSubject());
//		Z.understand(entity.getObject().getObject().getElement(0).getSubject());
		
		testSimpleStories();
//		testMakeSalad();
//		testReadBook();
//		testReadBook2();
//		testDrawSpeaker();
//		testBatteryStories();
//		testFruitSalad();
	}
	
	public static String toAsk(List<String> steps) {
		
		if(steps.size()<=3) {
			questions.addAll(Z.reverse(steps));
		}
		String toAsk = Z.howQuestion(questions.pop(),1);
		Mark.night(toAsk);
		return toAsk;
	}
	
	public static void testFruitSalad() {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		List<String> sentences = new ArrayList<String>();
		sentences.add("how do you make a fruit salad?"
				+ "You toss the fruits into the bowl then you load the bowl with nuts."
				);
		List<List<String>> toReturns = instructionsToMicroStories(sentences, date, "");
		List<String> unknownSteps = toReturns.get(0);
		List<String> unknownObjects = toReturns.get(1);
		String newGoal = "";
		for(String step: unknownSteps) {
			newGoal = Z.howQuestion(step, 1);
			Mark.say(newGoal);
		}
		
	}
	
	
	
	public static void testBatteryStories() {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		List<String> sentences = new ArrayList<String>();
		sentences.add("do you know how to Replace cellphone battery? "
				+ "if the phone is wet, you need to remove the cover from the phone, collect the old battery from the phone, insert the replacement battery into the phone, and put the cover on the phone. Finished."
//				+ "if you have a replacement battery, you need to remove the old battery from the phone, and place the cover on the phone."
				);
		instructionsToMicroStories(sentences, date, "").get(0);
	}
	
	public static void testDrawSpeaker() {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		List<String> sentences = new ArrayList<String>();
		sentences.add("When you wish to draw a speaker, consider his contents and adapt his action to it." + 
				"If the contents are persuasive, let his right hand hold his left finger." + 
				"If he is giving an argument, turn his face towards the people and make his mouth open slightly."+
//				"If the contents set forth an argument, let his face be alert, his body turn towards the people, and his mouth be slightly open."+
				"If he is seated, let him appear soon to rise, with his head forward. " + 
				"If he stands, make him lean slightly forward with his head towards the people. " + 
				"You should make the audience look silent and attentive. "+
//				"They should all watching the face of the orator with gestures of admiration. " + 
				"And make some old men astonished at the contents."
//				"And make some old men in astonishment at what they hear, with the corners of their mouths pulled down drawing back the cheeks in many furrows with their eyebrows raised where they meet. "
				+ "Make many wrinkles on the old men's foreheads. "
//				+ "Make some men sitting with their fingers clasped over their weary knees."
//				+ "Make some bent old man, with one knee crossed over the other and one hand resting upon it"
//				+ "make some bent old man, with one knee crossed over the other and one hand resting upon it and holding his other elbow and the hand supporting the bearded chin." + 
				);
		instructionsToMicroStories(sentences, date, "").get(0);
	}
	
	public static void testReadBook2() {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		List<String> sentences = new ArrayList<String>();
		sentences.add("As we have suggested, you should first inspect the book. "
				+ "You read the title, the subtitle, and the table of contents. "
				+ "You glance at the preface written by the author."
				+ "If the book has a dust jacket, you look at the blurb."
				+ "It is not his fault if you will not stop, look, and listen."
				+ "");
		instructionsToMicroStories(sentences, date, "").get(0);
	}

	public static void testReadBook() throws IOException {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		String toAsk = "";
		
		List<String> sentences = new ArrayList<String>();
		sentences.add("Do you know how to read a book?");
//		sentences.add("First, you skim the book. Then, analyze its contents. Finally, you should criticize its merits.");
//		toAsk = toAsk(instructionsToMicroStories(sentences, date, "").get(0));
//		
//		sentences = new ArrayList<String>();
//		sentences.add(toAsk);
//		sentences.add(""
//				+ "Read the title page. "
//				+ "Read the preface if the book has it. "
//				+ "Study the table of contents to obtain a general sense of the structure of the book. "
//				+ "Check the index to find the important terms. "
//				+ "Read the blurb for an accurate summary of the book. "
//				+ "Look at the chapters that seem pivotal to its argument. "
//				+ "Finally, turn the pages to look for signs of the main contention.");
//		toAsk = toAsk(instructionsToMicroStories(sentences, date, "").get(0));
//		
//		
//		sentences = new ArrayList<String>();
//		sentences.add(toAsk);
////		sentences.add("How do I analyze the contents of the book?");
//		sentences.add("Find the scope of the book and interpret its content.");
//		toAsk = toAsk(instructionsToMicroStories(sentences, date, "").get(0));
//		
//		
//		sentences = new ArrayList<String>();
//		sentences.add(toAsk);
////		sentences.add("How do I find the scope of the book?");
//		sentences.add(""
//				+ "Classify the book by its subject matter. "
//				+ "Briefly state its scope. "
//				+ "Enumerate and outline its major parts. "
//				+ "Define the problems the author is trying to solve.");
//		toAsk = toAsk(instructionsToMicroStories(sentences, date, "").get(0));
//
//		
//		sentences = new ArrayList<String>();
//		sentences.add(toAsk);
//		sentences.add("How do I interpret the content of the book?");
//		sentences.add(""
//				+ "Interpret the key words of the author."
//				+ "Grasp his leading propositions. "
//				+ "Know his arguments. "
//				+ "Examine whether the author has solved his problems.");
//		toAsk = toAsk(instructionsToMicroStories(sentences, date, "").get(0));
//
//		
//		sentences = new ArrayList<String>();
//		sentences.add(toAsk);
////		sentences.add("How do I criticize the merits of the book?");
//		sentences.add("you can judge the author's soundness and his completeness.");
//		toPrint = instructionsToMicroStories(sentences, date, "").get(0);
	}

	public static void testMakeSalad() throws IOException {
		NewTimer timer = NewTimer.zTimer;
		timer.initialize();
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		List<String> sentences = new ArrayList<String>();
		String goal = "make an apple salad for me";
//		sentences.add("can you make an apple salad for me?");
		sentences.add("you can find a bowl, put apples in it, and pour sauces into it. ");
		sentences.add("You follow the steps if you are making the salad for me. ");

		instructionsToMicroStories(sentences, date, "");
//		instructionsToMicroStories(sentences, date, "");
		timer.lapTime(true, "1");
		timer.summarize();
	}
	
	public static void testSimpleStories() {
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		List<String> sentences = new ArrayList<String>();


//		sentences.add("To replace battery in cellphone, you need to prepare materials; replace the battery; and make the phone useable.");
//		sentences.add("To prepare materials, you need to go to the market.");
//		sentences.add("To think like Minsky, you need to sleep.");

//		sentences.add("How to replace battery in smartphone? ");
//		sentences.add("If the phone is an Android phone, you have to prepare materials, turn on the TV, and go to bed. ");
//		sentences.add("If the phone is an iPhone, you need to go to Apple Store and be nice.");
//		sentences.add("To prepare materials, you need to go to the market.");

//		sentences.add("How to replace battery in smartphone? ");
//		sentences.add("If the phone is an Android phone, you need to prepare materials.");
//		sentences.add("If the phone is an iPhone, you need to go to Apple Store.");
//		sentences.add("To prepare materials, you need to go to the market.");

//		sentences.add("do you know how to find a soul mate?");
//		sentences.add("if you are rich, you need to buy flowers.");
//		sentences.add("if you are stupid, you need to be smart.");

//		sentences.add("to assemble small tower, you need to construct small tower.");
//		sentences.add("to construct small tower, you need to get my help.");

//		sentences.add("How to replace battery of phone?");
//		sentences.add("If the phone is wet, you need to take out the old battery from the phone, buy a new battery, and put the new battery into the phone.");
//		sentences.add("If you have a new battery, you need to find the new battery, remove the old battery from the phone, and insert the new battery into the phone.");
//
//		sentences.add("do you know how to go to bed?");
//		sentences.add("if the phone is wet, you need to slide up the phone's cover.");
//		sentences.add("if you can find a replacement battery, you need to do it.");

//		sentences.add("do you know how to mix martini?");
//		sentences.add("you need to pour gin into glass and place lemon in glass.");

//		sentences.add("do you know how to avoid trouble?");
//		sentences.add("you need to change names and go outside and enjoy day.");

//		sentences.add("do you know how to replace cellphone battery?");
//		sentences.add("if your phone is wet, you need to buy the replacement battery, and insert the replacement battery into the phone.");
//		sentences.add("if you have a replacement battery, you need to found a replacement battery, and put the replacement battery in the phone.");

//		sentences.add("can you make a fruit salad?");
//		sentences.add("you toss the fruits into the bowl and add salad dressing on the bowl.");
		
		sentences.add("how to make hard Boiled Eggs?");
		sentences.add("Place eggs into a saucepan and pour cold water to cover the eggs."); // cover the eggs with cold water
		sentences.add("Place the saucepan over high heat.");
		sentences.add("When the water starts to simmer, turn off heat.");// When the water just starts to simmer
		sentences.add("Cover pan with a lid, and let the  pan rest for 17 minutes.");
		sentences.add("Drain the hot water and pour cold water over eggs.");
		sentences.add("Drain the cold water. Refill the saucepan with additional cold water.");
		sentences.add("Let the eggs stand to cool down."); // Allow the eggs to stand until they are cool.
		sentences.add("Wait for 20 minutes.");
		sentences.add("Peel eggs under running water.");
		
		
		instructionsToMicroStories(sentences, date, "");
	}

	public static void testWriteKnowledgeFile() throws IOException {
		String fileName= filePath+"play.txt";
		Z.deleteFile(fileName);
		String goal = "replace cellphone battery";
		String condition = "you are rich";
		List<String> steps = new ArrayList<>();
		steps.add("go to sleep");
		steps.add("pour gin into glass");
		writeKnowledgeFile(fileName, goal, condition, steps);

		goal = "replace cellphone battery";
		condition = "I am happy";
		steps = new ArrayList<>();
		steps.add("pour gin into glass");
		writeKnowledgeFile(fileName, goal, condition, steps);
	}

	public static List<List<String>> instructionsToMicroStories(List<String> sentences, String date, String KIND_OF_KNOWLEDGE){

		// specify whether to use the blocks world simulation
		if(KIND_OF_KNOWLEDGE=="") KIND_OF_KNOWLEDGE = RecipeExpert.KIND_OF_KNOWLEDGE;

		// initialize variables
		if (!INITIALIZED) {
			stepsFileNameString = "dustbin";
			stepsFileName = filePath + "dustbin.txt";
			goal = "";
			condition = "";
			INITIALIZED = true;
		}
		
		List<String> steps = new ArrayList<String>();
		List<String> unknownSteps = new ArrayList<String>();
		List<String> unknownObjects = new ArrayList<String>();
		int count = 0;

		// analyse each sentence
		sentences = Z.listToLower(Z.story2Sentences(sentences));
		Mark.say(DEBUG, "all sentences: ",sentences);
		
		for (String sentence:sentences) {
			Boolean copyKnowledge = false;
			sentence = sentence.trim().toLowerCase();
			Mark.say(DEBUG, "#",++count,sentence);

			if(sentence.contains(Z.FINISHED)) {
				startNewRecipe = true;
			} else {
				// if the sentence is a goal
				if(Z.stringIsGoal(sentence)) {
					
					goal = Z.question2Goal(sentence);
					if (firstGoal=="") {
						firstGoal = goal;
						stepsFileNameString = "Steps_"+ firstGoal +"_" + date;
						stepsFileName = filePath + stepsFileNameString + ".txt";
						testFileName = filePath + firstGoal + "_" + date + ".txt";
					}

					Mark.show(DEBUG, "Goal --------- "+ goal);
				
				
				} else {

					// if the sentence is a condition
					List<String> returns = Z.stringIsCondition(sentence);
					if(returns.size()!=0) {
						sentence = returns.get(1);
						if (firstGoal=="") {
							goal = returns.get(0);
							firstGoal = goal;
						} else {
							condition = returns.get(0);
							Mark.show(DEBUG, "Condition --------- "+ condition);
							if(missingCondition) {
								if(sentence.contains("in the end")) { // TODO
									sentence = sentence.replace("in the end.", "");
									addStepToEnd = true;
								}
							}
							// if the conditions are given after the knowledge has been generated
							if(hasGenerated && sentence.contains("follow") && sentence.contains("steps")) {
								addCondition(stepsFileName, goal, condition);
								sentence = "";
							}
						}
						returns.remove(returns.get(0));
						steps.addAll(returns);
						
					} else {
						missingCondition = true;
						
						Mark.night(DEBUG, sentence);
						// if the sentences are steps
						if(Z.isTranslatable(sentence)) {
							Mark.show(DEBUG, "Steps --------- "+ sentence);
							if(!addStepToEnd) {
								steps = new ArrayList<String>();
							}
						
							// classify the elements
							Entity entity = t.translate(sentence);
							// ignore "somebody chopped the chopped fruit" in "toss the chopped fruits into the bowl"
							List<Entity> entities = Z.ignorePassiveAndScene(entity);
							for(Entity entity1 : entity.getElements()) {
								
								entity1 = Z.simplifyEntity(entity1);
								try {
									entity1 = Z.noAdv(entity1);
								} catch (Exception e) {
									Mark.mit("remove adv unsuccessfully");
//									e.printStackTrace();
								}
								
								if(!Z.getName(entity1).equals(Markers.SCENE)) {
									Z.printInnereseTree(entity1);
									// deal with pronouns
									Map<String, Entity> roles = Z.getRoleEntities(entity1);
									if(roles.containsKey(Z.SUBJECT)) currentSubject = roles.get(Z.SUBJECT);
									if(roles.containsKey(Markers.OBJECT_MARKER)) {
										Boolean skip = false;
										Entity object = roles.get(Markers.OBJECT_MARKER);
										if(object.hasProperty(Markers.OWNER_MARKER)) {
											if(Z.getName((Entity) object.getProperty(Markers.OWNER_MARKER)).equals(Z.IT)) {
												object.addProperty(Markers.OWNER_MARKER,currentObject);
												skip = true;
											}
										}
										if(!skip) {
											currentObject = object;
										}
									}
									
									// achieve goal and extra steps for "read ... to ..."
									if(entity1.getTypes().contains(Markers.CAUSE_MARKER)) {
										List<String> stepsNew = new ArrayList<>();
										stepsNew.add(g.generate(entity1.getSubject()).replace(".", ""));
										
										entity1 = Z.noAdv(entity1.getObject());
										String goalNew = g.generate(entity1).replace(".", "");
										skipOneProblemIntentionStep = true;
										unknownSteps.addAll(writeKnowledgeFile(stepsFileName, goalNew, condition, stepsNew));
									}

									String object = Z.sentence2Action(entity1);
									if (firstGoal=="") {
										goal = object;
										firstGoal = goal;
										Mark.show(DEBUG, "Goal --------- "+ goal);
									} else {
										steps.add(object);
										object = Z.getRoles(entity1).get(Markers.OBJECT_MARKER);
										unknownObjects.add(object);
									}
								}
							}
						}
					}
				}
				
				// after analyzing each sentence
				if(goal.length()!=0 && (steps.size()!=0 || condition.length()!=0)) {
					
					
					// revise the goal
					Entity goalEntity = t.translate(goal).getElement(0);
					if(goalEntity.getType().equals(Z.WISH)) {
						goalEntity = goalEntity.getObject();
						goalEntity = Z.clearEntity(goalEntity);
						String newGoal = g.generate(goalEntity).replace(".", "");
						if(!newGoal.equals(goal)) {
							if(goal.equals(firstGoal)) {
								firstGoal = newGoal;
							}
							goal = newGoal;
						}
					}
					
					// repair the conditions
					condition = condition.replace("isn't", "is not").replace("wasn't", "was not")
							.replace("aren't", "are not").replace("weren't", "were not");
					
					// make the files
					if (goal.equals(firstGoal)) {
						stepsFileNameString = "Steps_"+ firstGoal +"_" + date;
						stepsFileName = filePath + stepsFileNameString + ".txt";
						testFileName = filePath + firstGoal + "_" + date + ".txt";
						Mark.show(DEBUG, "Goal --------- "+ goal);
					}
					
					// report data
					Mark.mit(DEBUG, "Goal --------- ", goal);
					Mark.mit(DEBUG, "Condition --------- ", condition);
					Mark.mit(DEBUG, "Steps --------- "+ steps);
					
					// write tester file
					if(!hasWrittenTester) {
						if(KIND_OF_KNOWLEDGE==RecipeExpert.KNOWLEDGE_FOR_BLOCKS_WORLD) {
							RecipeExpert.writeTestFileBlocksWorld(testFileName,stepsFileNameString,firstGoal);
						} else {
							RecipeExpert.writeTestFile(testFileName,stepsFileNameString,firstGoal);
						}
						hasWrittenTester = true;
					}
					
					// write steps file
					unknownSteps.addAll(writeKnowledgeFile(stepsFileName, goal, condition, steps));
					hasGenerated = true;
					condition = "";
					steps = new ArrayList<>();
				}
			
			}
		}	
		startNewRecipe = false;
		List<List<String>> toReturn = new ArrayList<>();
		toReturn.add(unknownSteps);
		toReturn.add(unknownObjects);
		RecipeExpert.instructions = new ArrayList<>();
		return toReturn;
	}
	
	public static List<String> writeKnowledgeFile(String fileName, String goal, String condition, List<String> steps) {
		List<String> states = Z.steps2States(steps);
		for (String step : steps) {
			Mark.yellow(step);
		}
		for (String state : states) {
			Mark.purple(state);
		}
		
		return writeKnowledgeFile(fileName, goal, condition, steps, states);
	}
	
	public static List<String> writeKnowledgeFile(String fileName, String goal, List<RGoal> rgoals) {
		List<String> steps = new ArrayList<>();
		List<String> states = Z.rGoals2States(rgoals);
		for(RGoal rgoal:rgoals) {
			steps.add(rgoal.getAction());
		}
		return writeKnowledgeFile(fileName, goal, "", steps, states);
	}
	
	public static List<String> writeKnowledgeFile(String goal, List<RGoal> rgoals){
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		stepsFileName = RecipeExpert.outputFolder + "Steps_" + goal +"_" + date+".txt";
		testFileName = RecipeExpert.outputFolder + Z.string2Capitalized(goal) + "_" + date+ ".txt";
		Mark.mit(stepsFileName);
		Mark.mit(testFileName);
		RecipeExpert.NAME_EXPERT = Z.Yang;
		RecipeExpert.writeTestFile(testFileName, "Steps_" + goal +"_" + date, goal);
		writeKnowledgeFile(stepsFileName, goal, rgoals);
		return Arrays.asList(stepsFileName, testFileName);
	}

	public static List<String> writeKnowledgeFile(String fileName, String goal, String condition, List<String> steps, List<String> states) {

		List<String> unknownSteps = new ArrayList<>();
		Boolean skipProblemIntention = false;
		Boolean skipProblemIntentionStep = skipOneProblemIntentionStep;
		Boolean hasConditioned = false;
//		Mark.night(steps);

		// if needed to modify knowledge
		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) {
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
				
				for (int i = fileContent.size()-1; i >0; i--) {
					
					if(startNewRecipe) {
						startNewRecipe = false;
						break;
					}

				    if (fileContent.get(i).toLowerCase().equals("if the intention is \""+goal.toLowerCase() + "\".")) {

				    	// if the goal exists in the file as step
				    	if(fileContent.get(i+1).contains(Z.ASSUME_SUCCESS)
				    			&& fileContent.get(i+2).contains(Markers.THE_END_TEXT)) {
				    		skipProblemIntention = true;
				    		fileContent = Z.removeFromLine(fileContent,i,3);

					    // if the goal exists in the file as higher-level goal
				    	} else {
				    		skipProblemIntentionStep = true;
				    		
				    		List<String> approach = new ArrayList<>();
				    		int j = i;
				    		int countVerify = 0;
				    		while(true) {
				    			if(condition.length()>0) approach.add(fileContent.get(j));
				    			if(fileContent.get(j).contains(Z.VERIFY)) countVerify++;
				    			if(fileContent.get(++j).contains(Markers.THE_END_TEXT)) {
				    				if(condition.length()>0) {
				    					approach.add(fileContent.get(j));
				    					approach.add("");
				    				}
				    				
				    				List<String> newSteps = new ArrayList<>();
				    				for(String step: steps) newSteps.add("Step: "+Z.checkPeriod(step));
				    				
				    				if(condition.length()>0) {
				    					
				    					// copy entire approach below
				    					fileContent = Z.addAfterLine(fileContent, j+1, approach);
				    					
				    					// add new steps in the approach below
					    				fileContent = Z.addAfterLine(fileContent, j-1 + approach.size(), newSteps);
					    				
					    				// add new condition to approach below
					    				fileContent = Z.addAfterLine(fileContent, i + approach.size(), 
					    						"Verify: " + condition +".");

					    				// add new condition to appraoch original
					    				fileContent = Z.addAfterLine(fileContent, i, "Verify: " + Z.sentence2Negation(condition) + ".");

//					    				hasConditioned = true;
						    		} else {
						    			fileContent = Z.addAfterLine(fileContent, j-1, newSteps);
						    		}
				    				break;
				    			}
				    		}
				    	}
//				        if(hasConditioned) break;
				    }
				}
				
				fileContent = eliminateEmptyLines(fileContent);

				if(addStepToEnd) {
					for (int i = 0; i < fileContent.size(); i++) {
					    if (fileContent.get(i).equals("If the intention is \""+Z.string2Capitalized(goal) + "\".")) {
					    	fileContent = Z.addAfterLine(fileContent, i, "Condition: "+Z.sentence2Negation(condition));
					        break;
					    }
					}
				}

				Files.write(Paths. get(fileName), fileContent, StandardCharsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {

			FileWriter writerTwo = new FileWriter(fileName, true);
			FileWriter writer = writerTwo;

			// step 0 - write comments
			String date = Z.substringBetween(fileName, "_", ".txt");
			if (USE_COMMENT) {
				writer.write("/* ------------------------------");
				writer.write("\r\n " + COMMENT.replace("date", date));
				writer.write("\r\n --------------------------------*/\r\n");
			} else {
				writer.write("// ------------------------------");
			}	
			

//			FileWriter writer = new FileWriter(expertKnowledgeFile, false);
//			writerTwo.write("/* \r\n" + COMMENT.replace("date", "0"+date));
//			writerTwo.write("\r\n*/\r\n");
//			writerTwo.write("\r\n// Goal: "+goal);
//			writerTwo.write("\r\n");

			List<String> strings = new ArrayList<>();

			if(!skipProblemIntention && !skipProblemIntentionStep) {
				// step 1 - high-level insight = problem -> intention
				if(!goal.equals(hasWrittenGoal)) {
					strings = writeProblemIntention(goal ,goal);
					for (String string : strings) writerTwo.write(string+"\r\n");
					writerTwo.write("\r\n");
					hasWrittenGoal = goal;
				}
				
			}
			if(!skipProblemIntentionStep) {
				// step 2 - high-level approach = intention -> step + condition
				strings = writeIntentionSteps(goal, condition, steps);
				for (String string : strings) writer.write(string+"\r\n");
				writer.write("\r\n");

//				// step 3 - high-level conditions, if any
//				if(condition!= "") {
//					strings = writeCondition(condition);
//					for (String string : strings) writerTwo.write(string+"\r\n");
//					writerTwo.write("\r\n");
//				}
			}

			String lastStep = "";
			String lastState = "";
			// step 4 - lower-level method = step -> method
			for (int i = 0; i<steps.size(); i++) {

				String step = steps.get(i);
				String state = states.get(i);
				
				// for new steps
				if(!step.equals(lastStep)) {
					lastStep = step;
					lastState = state;
					
//					writerTwo.write("// ------------ Step "+ (1+i)); // +": "+step);
					writerTwo.write("\r\n");

					strings = writeProblemIntention(step, step);
					for (String string : strings) writerTwo.write(string+"\r\n");
					writerTwo.write("\r\n");

					// if translated into states, write the transition knowledge
					strings = writeIntentionMethod(step, condition, state);
					for (String string : strings) writerTwo.write(string+"\r\n");
					writerTwo.write("\r\n");

					if(step.equals(state)) {
						for(String noun: Z.getNounNames(step)) {
//							if(noun) {
								
//							}
						}
						unknownSteps.add(step);
					}
					
				// add state to steps
				} else if (!state.equals(lastState)) {
					writer.close();
					writerTwo.close();
					
					File f2 = new File(fileName);
					List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));
					int size = fileContent.size();
					fileContent.set(size-2, "Step: "+state + ".");
					fileContent.set(size-1, "The end.");
					fileContent.add("\r\n");
					Files.write(Paths. get(fileName), fileContent, StandardCharsets.UTF_8);
					
					writerTwo = new FileWriter(fileName, true);
					writer = writerTwo;
				}
			}

			writer.close();
			writerTwo.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		Z.printTXTFile(fileName);
		skipOneProblemIntentionStep = false;
		return unknownSteps;
	}

	/* ========================================
	 *   Create knowledge
	 ========================================== */
	// Problem ...  Intention
	public static List<String> writeProblemIntention(String problem, String intention){

		if (RecipeExpert.WRITE_KNOWLEDGE_MAP) {
			try {
				FileWriter writer = new FileWriter(RecipeExpert.knowledgeMapFile, true);
				writer.write(ZSay.KNOWLEDGE_MAP.replace("goal",problem).replace("file", testFileName.replace(RecipeExpert.outputFolder, ""))+"\r\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		List<String> insight = new ArrayList<String>();
		insight.add("If the problem is \""+problem + "\".");
		insight.add("Intention: "+intention + ".");
		insight.add("The end.");

		return insight;
	}

	// Intention ...  Condition + Step
	public static List<String> writeIntentionSteps(String intention, String condition, List<String> steps){
		List<String> approach = new ArrayList<String>();
		approach.add("If the intention is \""+intention + "\".");
		if(condition !="") {
			approach.add("Verify: "+condition + ".");
		}
		String lastStep = "";
		for (String step : steps) {
			if(!step.equals(lastStep)) {
				approach.add("Step: "+step + ".");
				lastStep = step;
			}
		}
		approach.add("The end.");

		return approach;
	}

	// Condition ... Method
	public static List<String> writeCondition(String condition){
		List<String> strings = new ArrayList<String>();
		strings.add("");
		strings.add("If the condition is \""+condition + "\".");
//		strings.add("Method: Print ask_"+Z.joinString(condition,"_") + ".");
		strings.add("Method: Ask a question.");
		strings.add("The end.");
		return strings;
	}

	// Intention ...  Step
	public static List<String> writeIntentionMethod(String intention, String condition, String step){
		Mark.say(DEBUG, "!!!!!!!!!!",intention, step);
		List<String> approach = new ArrayList<String>();
		approach.add("If the intention is \""+intention + "\".");
//		approach.add("Method: Print "+Z.joinString(step,"_") + ".");
		if(!intention.equals(step)) {
			approach.add("Step: "+Z.stripPunctuation(step)+".");
		} else {
			approach.add("Method: "+Z.ASSUME_SUCCESS+".");
		}
		approach.add("The end.");
		return approach;
	}

	// Step ...  Solve
	public static List<String> writeStep(String intention, String step){
		List<String> approach = new ArrayList<String>();

		approach.add("If the step is \""+intention + "\".");
		approach.add("Solve: "+step + ".");
		approach.add("The end.");
		return approach;
	}

	/* ========================================
	 *   Tools
	 ========================================== */
	public void makeKnowledgeFile(Object story_paragraph) {
		Mark.say(DEBUG, "!!!!!!!!!!!!",story_paragraph);
	}

	public static List<String> extractToList(String string, String open, String close, List<String> list) {
		list.add(Z.string2Capitalized(Z.substringBetween(string, open, close)));
		return list;
	}

	public static void loadLongTermMemory() {

//		if (!INITIALIZED) {

//			try {
//
//				hintsGoal = new ArrayList<>(Files.readAllLines(Paths.get(hintsGoalFile), StandardCharsets.UTF_8));
//				hintsGoal.removeAll(Arrays.asList("", null));
//				hintsGoal = Z.listToLower(hintsGoal);
//
//				starterTeachCritical = new ArrayList<>(Files.readAllLines(Paths.get(starterTeachCriticalFile), StandardCharsets.UTF_8));
//				starterTeachCritical.removeAll(Arrays.asList("", null));
//				starterTeachCritical = Z.listToLower(starterTeachCritical);

//				knownSteps = new ArrayList<>(Files.readAllLines(Paths.get(knownStepsFile), StandardCharsets.UTF_8));
//				knownSteps.removeAll(Arrays.asList("", null));
//				knownSteps = Z.listToLower(knownSteps);
//
//				transitionRules = new ArrayList<>(Files.readAllLines(Paths.get(transitionRulesFile), StandardCharsets.UTF_8));
//				transitionRules.removeAll(Arrays.asList("", null));
//				transitionRules = Z.listToLower(transitionRules);
//				transitionRules = Z.listRemoveComment(transitionRules);

//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			INITIALIZED = true;
//		}

	}

	public static void addCondition(String fileName, String goal, String condition){

		condition = Z.string2Capitalized(condition);
		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) {
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));

				// add condition into intention
				for (int i = 0; i < fileContent.size(); i++) {
					if (fileContent.get(i).equals("If the intention is \""+Z.string2Capitalized(goal) + "\".")) {
				    	fileContent.add(fileContent.get(fileContent.size()-1));
			    		for(int j = fileContent.size()-1;j>i+1;j--) {
			    			fileContent.set(j, fileContent.get(j-1));
			    		}
				        fileContent.set(i+1, "Verify: "+ condition + ".");
				        break;
					}
				}

				// add method to cope with the condition
//				fileContent.add("// ---------------- condition");
//				fileContent.addAll(writeCondition(condition));

				// overwrite the original file
				Files.write(Paths.get(fileName), fileContent, StandardCharsets.UTF_8);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Z.printTXTFile(fileName);
	}

	public static void updateUnknownObjects(String fileName, String old, String new1) throws IOException {

		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) {
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));

				for (int i = 0; i < fileContent.size(); i++) {
					fileContent.set(i, Z.replaceString(fileContent.get(i), old, new1));
				}

				Files.write(Paths.get(fileName), fileContent, StandardCharsets.UTF_8);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Z.printTXTFile(fileName);
	}

	public static void updateAssumeSuccess(String fileName, String goal, String state) throws IOException {

		File f = new File(fileName);
		if(f.exists() && !f.isDirectory()) {
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8));

				// add condition into intention
				for (int i = 0; i < fileContent.size(); i++) {
					if (fileContent.get(i).equals("If the intention is \""+Z.string2Capitalized(goal) + "\".")) {
						fileContent.set(i+1, "Method: "+Z.string2Capitalized(state) + ".");
				        break;
					}
				}

				// overwrite the original file
				Files.write(Paths.get(fileName), fileContent, StandardCharsets.UTF_8);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Z.printTXTFile(fileName);
	}

	public static List<String> eliminateEmptyLines(List<String> lines){
		List<String> newLines = new ArrayList<>();
		Boolean firstEmpty = false;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			// if this line is empty
		    if (line.length()==0) {
		    	if(!firstEmpty) { // if this is the first empty line
		    		firstEmpty = true;
		    		newLines.add(line);
		    	}
		    } else{
	    		firstEmpty = false;
	    		newLines.add(line);
		    }
		}
		return newLines;
	}

}

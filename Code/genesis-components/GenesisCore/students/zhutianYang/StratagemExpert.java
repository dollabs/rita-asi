package zhutianYang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import matchers.StandardMatcher;
import matchers.Substitutor;
import start.Start;
import utils.Html;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;

public class StratagemExpert extends AbstractWiredBox {

	public static Boolean DEBUG = false;
	public static Boolean MAKE_RULES_FOR_HUMAN = false;
	public static String HumanName = "Yang";
	public static String CounsellorName = "Genesis";

	// long term memory
	public static final String stratagemRulesFile = "corpora/zStoriesOfWar/zLearnedRules.txt";
//	public static final String stratagemMicroStoriesFile = "corpora/zStoriesOfWar/zLearnedMicroStories.txt";
	public static final String stratagemMicroStoriesFile = "corpora/zStoriesOfWar/zLearnedMicroStoriesYang.txt";

	// Short term memory
	public static Generator g = Generator.getGenerator();
	public static Translator t = getTranslator();
	public static String goalInitial;
	public static String goal;
	public static String question;
	public static String humanEnglish;
	public static List<String> names = new ArrayList<>();
	public static List<String> notNames = Arrays.asList("someone","so","therefore", "he", "nobody");
	public static List<String> steps = new ArrayList<>();
	public static Map <Entity, Entity> rules = new HashMap<Entity, Entity>();
	public static Map <String, List<String>> MSes =  new HashMap<String, List<String>>();
	public static Stack<Stack<String>> todo = new Stack<Stack<String>>();

	// learn microstories
	public static String learnProblem;
	public static List<String> learnChecks = new ArrayList<>();
	public static List<String> learnSteps = new ArrayList<>();
	public static List<String> learnHappens = new ArrayList<>();

	// Program states
	public static List<String> expectation = new ArrayList<>();
	public static Boolean continueStack = false;
	public static Boolean waitNegative = false;
	public static Boolean waitConfirm = false;
	public static Boolean learningMS = false;
	    // Makers for expected human input
	public static final String WAIT_START = "ask Genesis counsellor questions";
	public static final String WAIT_YESNO = "wait for yes/no answer";
	public static final String WAIT_ANSWER = "wait for matching answer";
	public static final String WAIT_IFTHEN = "wait for WhatIf question answer";
	public static final String WAIT_LEARN_CHECK = "wait for missing conditions for method to apply";
		// Makers for Genesis's response
	public static final String ASK_NORMAL = "Genesis to ask the next question";
	public static final String ASK_CONFIRMATION = "Genesis ask for confirmation of suggestion";
	public static final String ASK_WHATIF = "Genesis ask WhatIf question";
	public static final String ASK_LEARN_CHECK = "Genesis ask if the answer is always true";
	public static final String GIVEUP = "have no moreadvice";
	public static final String END = "ready to list the whole plan";
		// Makers when Genesis check human answer
	public static final String ANSWER_CORRECT = "matched micro-story";
	public static final String ANSWER_TO_CHECK = "matched micro-story, but need to check conditions";
	public static final String ANSWER_TO_LEARN = "no matching micro-story, to create new ones";
		// Makers when Genesis check human answer
	public static final String ANSWER_WONT_HAPPEN = "The hypothetical situation won't happen";
	public static final String ANSWER_DONT_KNOW = "Don't know if the hypothetical situation is true";
	public static final String ANSWER_THEN_ANOTHER = "If that happen, then I try another method";
	public static final String ANSWER_MEET_CONDITION = "If I make it false by .., then it will not happen";

	// Human states
	public static String humanTone;

	// Micro-story markers
	public static final String CHECK_MARKER = "check";
	public static final String ASK_MARKER = "ask";
	public static final String HAPPEN_MARKER = "happen";
	public static final String STEP_MARKER = "step:";
	// Ports
	public static final String FROM_QUESTION_EXPERT = "question expert to stratagem expert";
	public static final String TO_COMMENTARY = "stratagem expert to commentary";
	public static final String FROM_TEXT_ENTRY_BOX = "text entry box to stratagem expert";
	public static final String TO_CLEAR_TEXT_ENTRY_BOX  = "stratagem expert to clear text entry";

	public StratagemExpert() {

		super("Stratagem expert");
		expectation.add(WAIT_START);
//		initializeStratagemRules();
		MSes = readMicroStoriesFromFile(stratagemMicroStoriesFile);
		Connections.getPorts(this).addSignalProcessor(FROM_TEXT_ENTRY_BOX, this::getResponse);

	}

	public void getResponse(Object object) {

		if(!Radio.qToZTY36.isSelected()) return;

		// print human input to commentary
		humanEnglish = (String) object;
		recognizeNames(humanEnglish);
		printCommentary("Before: expectation = "+ expectation.toString(), "!");
		printCommentary("Before: todo = "+ todo.toString(), "!");
		printCommentary(humanEnglish,"Human");

		if(humanEnglish.equals("Professors help me by writing strong recommendation letters.")) {
			humanEnglish = "Professors help me.";
		}

		// get human basic response: yes/no/don't know/skip/...
		humanTone = Z.getTone(humanEnglish);
		humanEnglish = Z.getRidOfYesNo(humanEnglish, humanTone); // lowercased
		humanEnglish = humanEnglish.replace(".", "");
		humanEnglish = humanEnglish.replace("prof winston", "Prof Winston");
		// ------------------------------------------
		// Case 0: Human starts How-To question
		// ------------------------------------------
		if(expecting()==WAIT_START) {

			Entity humanEntity = t.translate(humanEnglish).getElement(0);
			if(humanEntity.getTypes().contains(Markers.HOW_QUESTION)) {
				expectation.remove(WAIT_START);
				printCommentary("Start: expectation = "+ expectation.toString(), "!");
				printCommentary("Start: todo = "+ todo.toString(), "!");

				// cope with "how do I make (state)" kind of question
				if(Z.entity2Name(humanEntity.getSubject()).equalsIgnoreCase("i")) {

					if(humanEntity.getSubject().getTypes().contains("make")) {
						humanEntity = humanEntity.getSubject().getObject().getElement(0).getSubject();
					} else {
						humanEntity = humanEntity.getSubject();
					}

					goal = g.generate(humanEntity);
					goalInitial = goal.replace(".", "");
					printCommentary("First goal: "+goal,"!");
					steps.add(goal);
					add(goal);

					// ------------------------------------------
					// Case 0: Answer the first question
					// ------------------------------------------
					if(searchInMS()) {
						expectation.add(ASK_NORMAL);
						printCommentary("Start ask: expectation = "+ expectation.toString(), "!");
						printCommentary("Start ask: todo = "+ todo.toString(), "!");
					} else {
						expectation.add(GIVEUP);
					}
				} else {
					// TODO other ways of asking the question
				}
			}
		}

		// =============================================
		// Case 1: respond to answers to conditions & confirm answers of suggested answer
		// =============================================
		if(expecting()==WAIT_YESNO) {

			// if answer with yes and no
			if(humanTone==Z.NO || humanTone==Z.YES) {
				expectation.remove(WAIT_YESNO); // remove current state

				if(Z.matchTwoSentences("i works hard and be nice to people", peek())!=null) {
					expectation.add(END);
				} else {
					// decide which plan to continue
					if((waitNegative && humanTone==Z.NO) || (!waitNegative && humanTone==Z.YES)) {

						if(!waitConfirm) {
							pop();
						} else {
							printCommentary("------ Add step: "+goal,"!");
							steps.add(goal);

						}

					} else {
						todo.pop();
					}
					waitNegative = false;
					waitConfirm = false;

					// continue with the plan
					if(!todo.isEmpty()) {
						if(continueAdvising()) {
							expectation.add(ASK_CONFIRMATION);
						} else if(expecting()!=ASK_CONFIRMATION) {
							expectation.add(ASK_NORMAL);
						}
					} else {
						expectation.add(ASK_NORMAL);
					}
				}

			}
		}

		// =============================================
		// Case 3: confirm answers of suggested answer
		// =============================================
		if(expecting()==WAIT_ANSWER) {

			// ------------------------------------------
			// Case 2-1: Human don't know, Genesis suggests possible answers or admit that it doesn't know
			// ------------------------------------------
			if(humanTone==Z.DONT_KNOW) {

				if(searchInMS()) {
					goal = peek();
					if(!goal.startsWith(CHECK_MARKER)) {
						expectation.add(ASK_CONFIRMATION);
					} else {
						expectation.add(ASK_NORMAL);
					}
				} else {
					expectation.add(ASK_NORMAL);
				}

			// ------------------------------------------
			// Human answered,
			// ------------------------------------------
			} else if (humanTone==Z.NORMAL){

				// ------------------------------------------
				// Case 2-1: Human answered critical stratagem question
				// ------------------------------------------
				if(peek().startsWith(ASK_MARKER)) {
					pop();
					printCommentary("Question answered: "+question,"!");
				}

				Entity entityAnswer = t.translate(humanEnglish);

				for(int i = entityAnswer.getElements().size(); i>0;i--) {
					Entity entity = entityAnswer.getElement(i-1);
					Boolean justDoIt = false;

					// check if it is just-do-it method
					if(entity.getProperty(Markers.MODAL)!=null) {
						if(entity.getProperty(Markers.MODAL).equals(Markers.CAN)) {
							justDoIt = true;
							pop();
						}
					}
					if(humanEnglish.contains("i can ")) {
						justDoIt = true;
					}

					// check if we know the answer
					goal = humanEnglish;
					if(!justDoIt) {
						add(goal);
					}
					printCommentary("current goal: "+goal,"!");
					printCommentary("!!!!! Check human answer for: "+question,"!");

					String checked = checkMS();
					printCommentary("!!!!! Check human answer: "+checked,"!");

					// ------------------------------------------
					// Case 2-3: Human answered "I can do it" - list all answers as the plan
					// ------------------------------------------
					if(justDoIt) {

						if(checked==ANSWER_TO_CHECK) {
							printCommentary("before what if condition: expectation after = "+ expectation.toString(), "!");
							printCommentary("before what if condition: todo after = "+ todo.toString(), "!");
							for(String st:todo.peek()) {
								expectation.add(ASK_WHATIF);
							}
						} else {

//							pop();
							entity.removeProperty(Markers.MODAL);
//							if(Z.entity2Name(entity.getSubject()).equalsIgnoreCase("i")) {
//								entity.setSubject(new Entity("you"));
//							}
							String english = Z.replace(Z.generate(entity), "i", "You");
							printCommentary("------ Add step: "+english,"!");
							steps.add(english);

							if(i==1) {
								if(checked==ANSWER_TO_LEARN) {
									startLearningMS();

									learnProblem = Z.question2Goal(question);
									learnHappens.add(humanEnglish);
									learnHappens = Z.reverse(learnHappens);

									// generate the steps knowledge
									List<String> learned = StratagemLearner.writeMicroStory(learnProblem, learnChecks, learnHappens, learnSteps, names);
									printCommentary(ZSay.SAY_LEARNED,"Comment");
									for(String string : learned) {
										printCommentary(string,"Comment");
									}
									learningMS = false;
									pop();
								}
							}

							if(todo.isEmpty()) {
								expectation.add(END);
							} else {
								expectation.add(ASK_NORMAL);
							}
						}

					// ------------------------------------------
					// Case 2-4: Human answered, Genesis check his answer and react
					// ------------------------------------------
					} else {
						if(checked==ANSWER_TO_CHECK) {
							printCommentary("before what if condition: expectation after = "+ expectation.toString(), "!");
							printCommentary("before what if condition: todo after = "+ todo.toString(), "!");
							for(String st:todo.peek()) {
								expectation.add(ASK_WHATIF);
							}
						} else {
							printCommentary("Question answered: "+question,"!");
							printCommentary("------ Add step: "+goal,"!");
							steps.add(goal);
							add(goal);  // help yang
							// to learn micro-stories
							if(checked==ANSWER_TO_LEARN) {
								startLearningMS();
								expectation.remove(ASK_LEARN_CHECK);
								expectation.add(WAIT_LEARN_CHECK);

								learnProblem = Z.question2Goal(question);

								// check if it is just-do-it method
								if(entity.getProperty(Markers.MODAL)!=null) {
									if(entity.getProperty(Markers.MODAL).equals(Markers.CAN)) {
										justDoIt = true;
									}
								}

								if(justDoIt) {
									learnHappens.add(humanEnglish);
								} else {
									learnSteps.add(humanEnglish);
								}
								printCommentary(ZSay.SAY_WHAT_CONDITION,"Counsellor");
								return;

							} else {
								expectation.add(ASK_NORMAL);
							}
						}
					}
				}
			}
		}

		if(expecting()==WAIT_IFTHEN) {
			expectation.remove(WAIT_IFTHEN);
			String response = responseWhatIf(question,humanEnglish);

			if(response == ANSWER_DONT_KNOW) {
				expectation.add(ASK_CONFIRMATION);

			} else if(response.equals(ANSWER_WONT_HAPPEN)) {
				expectation.add(ASK_NORMAL);
				printCommentary("------ Add step: "+goal,"!");
				steps.add(goal);

			} else if(response.equals(ANSWER_THEN_ANOTHER)) {
				List<String> learned = StratagemLearner.writeMicroStory(learnProblem, learnChecks, learnHappens, learnSteps, names);
				printCommentary(ZSay.SAY_LEARNED,"Comment");
				for(String string : learned) {
					printCommentary(string,"Comment");
				}
				learningMS = false;
				expectation.add(ASK_NORMAL);
				steps.add(learnProblem);

			} else if(response.equals(ZSay.HEAR_GARBAGE)) {
				printCommentary("what??????","Counsellor");
				return;
			}
		}

		if(expecting()==WAIT_LEARN_CHECK) {
			if(humanTone==Z.YES) {
				expectation.remove(WAIT_LEARN_CHECK);
				expectation.add(ASK_NORMAL);
				// generate the first intention knowledge
				List<String> learned = StratagemLearner.writeMicroStory(learnProblem, learnChecks, learnHappens, learnSteps, names);
				printCommentary(ZSay.SAY_LEARNED,"Comment");
				for(String string : learned) {
					printCommentary(string,"Comment");
				}
				learningMS = false;

			} else if (humanTone==Z.NONE) {
				expectation.remove(WAIT_LEARN_CHECK);
				expectation.add(ASK_NORMAL);
				pop();
				// generate the first intention knowledge
				List<String> learned = StratagemLearner.writeMicroStory(learnProblem, learnChecks, learnHappens, learnSteps, names);
				printCommentary(ZSay.SAY_LEARNED,"Comment");
				for(String string : learned) {
					printCommentary(string,"Comment");
				}
				learningMS = false;

			} else{
				learnChecks.add(humanEnglish);
				printCommentary(ZSay.SAY_IS_THAT_ALL,"Counsellor");
				return;

			}
		}

		// =============================================
		// Case 2: what if questions
		// =============================================
		if(expecting()==ASK_WHATIF) {
			expectation.remove(ASK_WHATIF);
			expectation.add(WAIT_IFTHEN);

			String toCheck = peek();
			question = Z.whatIfQuestion(toCheck);

			printCommentary(question,"Counsellor");
			printCommentary("what if: expectation after = "+ expectation.toString(), "!");
			printCommentary("what if: todo after = "+ todo.toString(), "!");
			return;
		}

		if(expecting()==ASK_CONFIRMATION) {
			expectation.remove(ASK_CONFIRMATION);
			expectation.add(WAIT_YESNO);
			waitConfirm = true;
			continueStack = false;
			goal = peek();
			printCommentary("That may happen if "+ Z.replaceString(goal.toLowerCase(), "i", "you") + ZSay.SAY_DOES_IT_APPLY_HERE,"Counsellor");
			printCommentary("Answered question: "+goal,"!");
			printCommentary("search-did: expectation after = "+ expectation.toString(), "!");
			printCommentary("search-did: todo after = "+ todo.toString(), "!");
			return;
		}

		if(expecting()==ASK_NORMAL) {
			expectation.remove(ASK_NORMAL);
			question = questionToAsk();
			printCommentary(question,"Counsellor");
			printCommentary("ask: expectation after = "+ expectation.toString(), "!");
			printCommentary("ask: todo after = "+ todo.toString(), "!");
			return;
		}

		if(expecting()==GIVEUP) {
			expectation.remove(GIVEUP);
			printCommentary("Great minds think a like. I have no idea too!","Counsellor");
			printCommentary("start-failed: expectation after = "+ expectation.toString(), "!");
			printCommentary("start-failed: todo after = "+ todo.toString(), "!");
			return;
		}

		if(expecting()==END) {
			expectation.remove(END);
			int total = steps.size();
			if(total>1) {
				String toPrint = steps.get(total-1);
				toPrint = "\n   1    " + toPrint.substring(0,1).toUpperCase() + toPrint.substring(1);
				for(int i = 1; i<total; i++) {
					String temp = steps.get(total-i-1).toLowerCase();
					if(temp.startsWith("you")) {
						toPrint = toPrint + "\n   " + (i+1) + "   and Y" + temp.substring(1);
					} else {
						toPrint = toPrint + "\n   " + (i+1) + "   so " + temp;
					}

				}
				printCommentary("Perfect. Here is how to " + Z.state2Goal(goalInitial).replace(".", "") +":"+toPrint,"Counsellor");
				printCommentary("I-can-do-it answer: expectation after = "+ expectation.toString(), "!");
				printCommentary("I-can-do-it answer: todo after = "+ todo.toString(), "!");
			} else {
				printCommentary("I actually have no idea how to " + Z.state2Goal(goalInitial.replace(".", "")), "Counsellor");
				printCommentary("I-can-do-it answer: expectation after = "+ expectation.toString(), "!");
				printCommentary("I-can-do-it answer: todo after = "+ todo.toString(), "!");
			}
			return;
		}
	}

	// recognize proper nouns in human input as human names or place names
	public static void recognizeNames(String string) {
		List<String> words = Arrays.asList(Z.splitAll(string));
		if(Z.isTranslatable(string)) {
			Entity entity = t.translate(string);
			for(Entity entity1:entity.getElements()) {

				List<String> nouns = Z.getNounNames(entity1);
				for(String noun:nouns) {
					if(words.contains(Z.string2Capitalized(noun))&&!names.contains(noun)&&!notNames.contains(noun)) {
						names.add(noun);
					}
				}
			}
		}
	}

	// capitalize proper nouns in reply
	public static String capitalizeNames(String string, List<String> thisNames) {
		names = thisNames;
		return capitalizeNames(string);
	}
	public static String capitalizeNames(String string) {
		for(String name:names) {
			string = Z.replace(string, name, Z.string2Capitalized(name));
		}
		return string;
	}

	// initialize learning memory
	public static void startLearningMS() {
		learningMS = true;
		learnProblem = "";
		learnChecks = new ArrayList<>();
		learnSteps = new ArrayList<>();
		learnHappens = new ArrayList<>();
	}

	// --------------------------------------------------
	// Core thinking processes
	// -------------------------------------------------------
	// return the question to ask using counsellor's knowledge
	// also determine the expected answer from humans by updating expectation
	public static String questionToAsk() {

		String nextQuestion = "";
		if(!todo.isEmpty()) {
			goal = peek();
			Mark.say("goal: "+goal+ " -- "+todo);
		} else {
			return "Empty To Do List!";
		}

		// =========================================
		// just happen and go on
		if (goal.startsWith(HAPPEN_MARKER)) {

			nextQuestion = goal.substring(goal.indexOf(HAPPEN_MARKER)+8, goal.indexOf(".")-1).toLowerCase();
			Mark.say("   happen:    "+nextQuestion);
			nextQuestion = Z.assumeHasDone(nextQuestion);

		// =========================================
		// for condition check, rewrite the sentence into question
		} else if (goal.startsWith(CHECK_MARKER)) {

			expectation.add(WAIT_YESNO);

			// e.g., check: "you cannot kill"
			nextQuestion = goal.substring(goal.indexOf(CHECK_MARKER)+7, goal.indexOf(".")-1);
			nextQuestion = Z.yesNoQuestion(nextQuestion);
			Mark.say("   check:    "+nextQuestion);

		// =========================================
		// for key strategic questions
		} else if (goal.startsWith(ASK_MARKER)) {

			expectation.add(WAIT_ANSWER);
			nextQuestion = Z.string2Capitalized(goal.substring(goal.indexOf(ASK_MARKER)+5, goal.indexOf(".")-1));
			if(!nextQuestion.endsWith("?")) {
				nextQuestion = nextQuestion + "?";
			}
			Mark.say("   ask:    "+nextQuestion);

		// =========================================
		// for condition meet or normal steps
		} else {
			nextQuestion = Z.howQuestion(goal);
		}

//		nextQuestion = .replace(" me", newChar)
		return nextQuestion;
	}

	// after checking conditions for a suggestion to apply
	public static Boolean continueAdvising() {
		String goal = peek();
		return continueStack && !goal.startsWith(ASK_MARKER)
				&& !goal.startsWith(HAPPEN_MARKER) && !goal.startsWith(CHECK_MARKER);
	}

	// add new steps to todo stack based on micro-stories
	public static Boolean searchInMS() {

		Boolean found = false;
		Entity goalEntity = t.translate(goal).getElement(0);
		Stack<Stack<String>> toAddAll = new Stack<Stack<String>>();


		for(String key : MSes.keySet() ) {
			Entity keyEntity = t.translate(key).getElement(0);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(keyEntity, goalEntity);
			Stack<String> toAdd = new Stack<String>();

			// if the current problem matched with existing knowledge
			if(bindings != null) {
				found = true;

				int cc = 0;

				List<String> temp = MSes.get(key);

				for(int i=temp.size()-1; i>=0;i--) {
					String value = temp.get(i);

					// replace place holders with current entity names
					// if contains "check:", "ask:", "happen:" ...
					if(value.contains("\"")){
						String before = value.split("\"")[1];
						String after = before;

						String[] parts = bindings.toString().split(" <");

						for (String part : parts) {
							String holder = part.substring(0,part.indexOf("-"));
							if(cc==0) {
								holder = holder.replace("(<", "");
							}
							part = part.substring(part.indexOf(",")+2,part.length());
							String object = part.substring(0, part.indexOf("-"));
							after = after.replace(holder, object);
						}
						value = value.replace(before, after);

					} else {

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
					}
					Mark.say("   add:     "+value.replace("_", " "));
					toAdd.add(value);
				}
				toAddAll.add(toAdd);
			}
		}

		if(found) {
			todo.pop();
			continueStack = false;
			for(Stack<String> toAdd: toAddAll) {
				todo.add(toAdd);
			}
			return true;
		} else {
			return false;
		}

	}

	// add new steps to todo stack based on micro-stories
	public static String checkMS() {

		Boolean found = false;
		Entity goalEntity = t.translate(Z.question2Goal(question)).getElement(0);

		for(String key : MSes.keySet() ) {
			Entity keyEntity = t.translate(key).getElement(0);
			LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(keyEntity, goalEntity);

			// if the current problem matched with existing knowledge
			if(bindings != null) {
				found = true;

				Mark.say(found);
				List<String> temp = MSes.get(key);
				List<String> steps = new ArrayList<String>();
				Stack<String> conditions = new Stack<String>();

				for(int i=temp.size()-1; i>=0;i--) {
					String value = temp.get(i);

					// replace place holders with current entity names
					// if contains "check:"
					if(value.contains(CHECK_MARKER)){
						value = value.substring(value.indexOf(CHECK_MARKER)+7, value.indexOf(".")-1);
						value = Z.replaceByBindings(value, bindings);
						conditions.add(value);

					} else if(!value.contains(ASK_MARKER)) {

						if(value.contains(HAPPEN_MARKER)) {
							value = value.substring(value.indexOf(HAPPEN_MARKER)+8, value.indexOf(".")-1).toLowerCase();
						}
						value = Z.replaceByBindings(value, bindings);
						steps.add(value);
					}
				}

				for(String step: steps) {
					// if the current problem matched with existing knowledge
					if(Z.matchTwoSentences(step,humanEnglish) != null) {
						if(!conditions.isEmpty()) {
							todo.add(conditions);
							return ANSWER_TO_CHECK;
						} else {
							pop();
							return ANSWER_CORRECT;
						}
					}
				}
				return ANSWER_TO_LEARN;
			}
		}
		// TODO if multiple matches, which one to check
		return ANSWER_TO_LEARN;
	}

	// --------------------------------------------------
	// Semantic modification see Z.java
	// -------------------------------------------------------



	public static String responseWhatIf(String question, String answer) {
		question = question.replace(ZSay.SAY_WHAT_IF,"").replace("?", "");
//		humanTone = Z.getTone(answer);
//		answer = Z.getRidOfYesNo(answer,humanTone);
		if(humanTone==Z.YES || humanTone==Z.NO) {

			if(answer.endsWith(".")) answer = answer.replace(".", "");
			String answerNew = answer;
			if(!Z.isTranslatable(answerNew)) {
				answerNew = answer + " do";
				if(!Z.isTranslatable(answerNew)) {
					answerNew = answer + " it";
				}
			}
			Mark.say("    "+answerNew);
			Entity entityAnswer = t.translate(answerNew).getElement(0);
			String triples = g.generateTriples(entityAnswer).toLowerCase();
			Boolean negative = triples.contains("is_negative yes");
			if((negative&&waitNegative) || (!negative&&!waitNegative)) {
				pop();
				waitNegative = false;
				return ANSWER_WONT_HAPPEN;
			} else {
				waitNegative = false;
				return ANSWER_DONT_KNOW;
			}

		} else if (humanTone == Z.DONT_KNOW){
			waitNegative = false;
			return ANSWER_DONT_KNOW;
		} else {
			waitNegative = false;
			Entity entity = t.translate(answer);
			steps.add(goal);
			if(entity.getElements().size()>1) {
				goal = g.generate(entity.getElement(1));
//				steps.add(goal); // help yang
				pop();
				return ANSWER_THEN_ANOTHER;
			} else {
				entity = entity.getElement(0);
				if(entity.getTypes().contains(Markers.CAUSE_MARKER)){
					String subject = Z.generate(entity.getSubject());
					String object = Z.generate(entity.getObject());
					steps.add(object); // help yang
					startLearningMS();
					goal = peek();
					learnProblem = goal;
					pop();
					if(Z.isNegation(subject, goal)) {
						learnSteps.add(object);
						add(object);
					} else if (Z.isNegation(object, goal)) {
						learnSteps.add(subject);
						add(subject);
					}
					return ANSWER_THEN_ANOTHER;
				}
			}
		}
		return ZSay.HEAR_GARBAGE;
	}

	// --------------------------------------------------
	// Communication processes
	// -------------------------------------------------------
	public void printCommentary(Object messageObject, String name) {

		// HTML color names
		//      https://www.w3schools.com/colors/colors_names.asp

		String message = (String) messageObject;
		message = capitalizeNames(message);
		message = message.substring(0,1).toUpperCase() + message.substring(1);
		message = message.replace("_", " ");

		if (Radio.qToZTY36.isSelected()) {
			String tabname = "Conversation";

			// for debugging
			if(name == "!") {
				message = Html.coloredText("#FFA500","\n"+ message); // orange
				message = Html.footnotesize(message);
				if(!DEBUG) message = "";

			// for commenting learned micro-story
			} else if(name=="Comment") {
				message = Html.bold("\n ") + message;
				message = Html.coloredText("#4169E1",message);  // royal blue
				message = Html.small(message);

			} else  if(name=="Human") {
				message = Html.bold("\n "+HumanName+": ") + message;
				message = Html.normal(message);

			} else if(name=="Counsellor") {
				message = Html.bold("\n\n "+CounsellorName+": ") + message;
				message = Html.normal(message);

			}

			if(message != "") {
				BetterSignal bs = new BetterSignal(tabname, message);
				Connections.getPorts(this).transmit(TO_COMMENTARY, bs);
				clearTextEntryBox();
			}
		}

	}

	public void clearTextEntryBox() {
		Connections.getPorts(this).transmit(TO_CLEAR_TEXT_ENTRY_BOX, "");
	}

	// --------------------------------------------------
	// Tools for Stratagem Expert
	// -------------------------------------------------------
	// construct translator that recognize names as persons
	public static Translator getTranslator() {
		Translator t = Translator.getTranslator();
//		t.internalize("xx is a person");
//		t.internalize("yy is a person");
//
//		t.internalize("Yuan is a person");
//		t.internalize("King is a person");
//		t.internalize("Secretary is a person");
//		t.internalize("Huang is a person");
//		t.internalize("Someone is a person");
//
//		t.internalize("Winston is a person");
		return t;
	}

	// read micro-stories from file into map
	public static Map <String, List<String>> readMicroStoriesFromFile(String fileName){
		Map <String, List<String>> MSes = new HashMap<String, List<String>>();
		List<String> MSesRaw;

		try {
			MSesRaw = new ArrayList<>(Files.readAllLines(Paths.get(stratagemMicroStoriesFile), StandardCharsets.UTF_8));
			MSesRaw.removeAll(Arrays.asList("", null));
			MSesRaw = Z.listToLower(MSesRaw);

			String tempStr = "";
			Stack<String> tempList = new Stack<String>();
			for(String MSRaw: MSesRaw) {
				if(!MSRaw.endsWith(".")) {
					MSRaw = MSRaw + ".";
				}
				if(MSRaw.startsWith("//")) {

				} else if (MSRaw.contains("if the intention is")) {
					tempStr = MSRaw.substring(MSRaw.indexOf("is ")+4, MSRaw.indexOf(".")-1);
				} else if (MSRaw.contains("the end")) {
					MSes.put(tempStr, tempList);
					tempStr = "";
					tempList = new Stack<String>();
				} else {
					tempList.add(MSRaw.substring(MSRaw.indexOf(": ")+2,MSRaw.indexOf(".")) + ".");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return MSes;
	}

	// get the newest task to do
	public static String expecting() {
		if(expectation.size()>=1) {
			return expectation.get(expectation.size()-1);
		} else {
			return "null";
		}
	}

	// pop the surface of the surface stack
	public static void pop() {
		Stack<String> temp = todo.peek();
		temp.pop();
		todo.pop();
		if(!temp.isEmpty()) {
			continueStack = true;
			todo.add(temp);
		} else {
			continueStack = false;
		}
	}

	// add to the surface of a surface stack
	public static void add(String string) {
		Stack<String> stack = new Stack<String>();
		stack.add(string);
		todo.add(stack);
	}

	public static String peek() {
		if(!todo.isEmpty()) {
			if(!todo.peek().isEmpty()) {
				return todo.peek().peek();
			}
		}
		return null;
	}

	// initialize micro-stories in the form of IF-THEN rules
	public static void initializeStratagemRules() {
		// initialize rules from file
		List<String> rulesRaw = Z.readRulesInFile(stratagemRulesFile);

		for(String sentence: rulesRaw) {
			Entity entity = t.translate(sentence).getElement(0);
			Entity antecedent = entity.getSubject().getElements().get(0);
			Entity consequent = entity.getObject();
			rules.put(consequent, antecedent);
		}
		Mark.say("knowledge of counsellor: ",rules);
	}

	// --------------------------------------------------
	// Tests for new features
	// -------------------------------------------------------
	// solve problem: predict from the first goal until meeting a stopping state based on rules
	public static void applyRulesFromFileToGoal(String ruleFileName, String goal, String stop) {

		Map <Entity, Entity> rules = new HashMap<Entity, Entity>();
		List<Entity> elements = new ArrayList<>();
		List<String> storyElements = new ArrayList<>();

		List<String> ruleSentences = Z.readRulesInFile(ruleFileName);
		for(String sentence: ruleSentences) {
			Entity entity = t.translate(sentence).getElement(0);
			Entity antecedent = entity.getSubject().getElements().get(0);
			Entity consequent = entity.getObject();
			rules.put(consequent, antecedent);
		}
		Mark.say(rules);

		Entity goalEntity = t.translate(goal).getElement(0);
		Entity stopEntity = t.translate(stop).getElement(0);
		int count = 0;
		Mark.say("goal: "+g.generate(stopEntity));
		LList<PairOfEntities> bindingsBig = null;
		while (bindingsBig==null) {
			Mark.say(count++);
			for(Entity key : rules.keySet() ) {
				LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(key, goalEntity);
				if(bindings!=null) {
					goalEntity = Substitutor.substitute(rules.get(key), bindings);
					String string = g.generate(goalEntity);
					elements.add(goalEntity);
					Mark.say("    "+string);
				}
			}
			bindingsBig = StandardMatcher.getBasicMatcher().match(goalEntity, stopEntity);
		}

		int total = rules.size();
		for(int i = 0; i<total;i++) {
			Entity entity = elements.get(total-i-1);
			entity = Substitutor.substitute(entity, bindingsBig);
			storyElements.add(g.generate(entity));
		}
		Z.printList(storyElements);


//		// learn rules
//		if(MAKE_RULES_FOR_HUMAN) {
//
//			Entity entityQuestion = t.translate(question).getElement(0);
//			if(entityQuestion.getTypes().contains(Markers.WHEN_QUESTION)) {
//				entityQuestion = entityQuestion.getSubject();
//			}
//
//			// construction "(consequent)... because ...(antecedent)" rules
//			Sequence conjunction = new Sequence(Markers.CONJUNCTION);
//			conjunction.addElement(entityAnswer);
//			Relation rule = new Relation(Markers.CAUSE_MARKER,conjunction,entityQuestion);
//			printCommentary("Learned rule: "+g.generate(rule),"!");
//		}

	}

	// when will consequent? antecedent.  -->   if antecedent, then consequent
	public static void learnRuleFromQuestionAnswer(String question, String answer) {

		Entity entityQuestion = t.translate(question).getElement(0);
		Entity entityAnswer = t.translate(answer).getElement(0);
		Z.understand(entityQuestion);
		if(entityQuestion.getTypes().contains(Markers.WHEN_QUESTION)) {
			entityQuestion = entityQuestion.getSubject();
		}
		Z.understand(entityQuestion);
		Function questionNew = new Function(Markers.WHEN_QUESTION, entityAnswer);
		questionNew.addType(Markers.QUESTION);
		Mark.say(questionNew);
		Z.understand(questionNew);
		Mark.say(g.generate(questionNew));

		String triples = g.generateTriples(questionNew);
		Mark.say(triples);
		Mark.say(Start.getStart().generate(triples));
		String[] temps = triples.split("]");
		Mark.say(temps);

		String verb = "";
		for(String temp: temps) {
			if(temp.contains(Markers.IS_QUESTION_MARKER)) {
				verb = temp.substring(1,temp.indexOf(Markers.IS_QUESTION_MARKER)-1);
			}
		}
		Mark.say(verb);
		triples += "["+verb +" has_modal will]";
		triples += "["+verb +" has_time when]";
		Mark.say(triples);
		Mark.say(Start.getStart().generate(triples));

		Mark.say(entityQuestion);
		Mark.say(entityAnswer);

		Sequence conjunction = new Sequence(Markers.CONJUNCTION);
		conjunction.addElement(entityAnswer);

		Relation rule = new Relation(Markers.CAUSE_MARKER,conjunction,entityQuestion);
		Mark.say(g.generate(rule));
	}

	// test
	public static void main(String[] args) {

		recognizeNames("Yuan loves Tang");
		Mark.say(names);
	}

}

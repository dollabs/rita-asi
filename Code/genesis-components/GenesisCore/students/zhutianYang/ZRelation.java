/**
 * 
 *  ZRelation  is a class for representing relations between two entities based on 
 *     transition rules (like “If xx puts yy into zz, then zz has yy's contact.”, 
 *                            “If xx kills yy, then yy has xx’s harm”). 
 *                            
 *  It keeps track of the entities appeared by placeholders (like xx), 
 *     and relations appeared in a story by the index of the sentence it appears.  
 *     
 */
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

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import matchers.StandardMatcher;
import matchers.Substitutor;
import translator.Translator;
import utils.Mark;
import utils.NewTimer;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;

public class ZRelation {
	
	public static Boolean DEBUG = false;
	public static List<String> stepsGeneral = new ArrayList<>();
	
	public static String transitionRulesBattery = "students/zhutianYang/zTransitionRulesByCategory.txt";
	public static String transitionRulesAlice = "students/zhutianYang/zTransitionRulesAlice.txt";
	public static String transitionRulesNews = "students/zhutianYang/zTransitionRulesNews.txt";
	
	public static String transitionRulesDefault = transitionRulesBattery;
	public static List<String> transitionRules = new ArrayList<>(); 
	public static Translator t = Translator.getTranslator();
	
	// for replacing "she" and "he" with the person
	public static List<Entity> persons = new ArrayList<>();
	public static Entity currentPerson;
	
	// list of attributes explained
	//    for example, when rule "if xx puts yy in zz, zz has yy's contact"
	//       is applied to sentence in "Alice put battery in phone." in Alice's story,
	//       the resulting relation is "phone has battery's contact" 
	Entity AAA = new Entity(); // the first entity in relation, i.e., "phone"
	Entity BBB = new Entity(); // the second entity, i.e., "battery"
	String AAAHolder = "";  // the placeholder of the first entity, i.e., "bb" (Alice is "aa")
	String BBBHolder = "";  // the placeholder of the second entity, i.e., "cc" 
	String type = ""; // the type of relation = the last word of transition rule, i.e., "contact"
	Boolean state = false; // the state of the relation as a result of the transition, i.e., true	
	Entity relationInInnerese = new Entity(); 
	String relation = ""; // i.e., "phone has battery's contact" 
	Entity relationPatternInInnerese = new Entity(); 
	String relationPattern = ""; // i.e., "bb has cc's contact" 
	Entity stepInInnerese = new Entity();
	String step = ""; // i.e., "Alice puts battery in phone" 
	
	
	
	String storyName = ""; // i.e., "Alice"
	int sentenceIndex = 0; // i.e., 1
	
	// to manually create a new ZReltation
	public ZRelation(Entity AAA, Entity BBB, String AAAHolder, String BBBHolder, 
			Boolean state, String type, 
			Entity transition, Entity GeneralTransition, Entity step, 
			String transitionStr, String GeneralTransitionStr, String stepStr, 
			String storyName, int sentenceNum) {
		
		this.AAA = AAA;
		this.BBB = BBB;
		this.AAAHolder = AAAHolder;
		this.BBBHolder = BBBHolder;
		this.state = state;
		this.type = type;
		this.relationInInnerese = transition;
		this.relationPatternInInnerese = GeneralTransition;
		this.stepInInnerese = step;
		this.relation = transitionStr;
		this.relationPattern = GeneralTransitionStr;
		this.step = stepStr;
		this.storyName = storyName;
		this.sentenceIndex = sentenceNum;
		
	}
	
	// to manually create a new ZReltation
	public ZRelation(Entity AAA, Entity BBB, Boolean state, String type) {
		
		this.AAA = AAA;
		this.BBB = BBB;
		this.AAAHolder = Z.holders[Z.NofObj++];
		this.BBBHolder = Z.holders[Z.NofObj++];
		this.state = state;
		this.type = type;
		
	}
	
	// to translate a story element into a ZRelation given a translator and a set of transition rules
	public static List<ZRelation> ZRelation(Entity event, Translator t, List<String> transitionRules) {
		return ZRelation(event, t, transitionRules, "-", 0);
	}
	
	public static List<ZRelation> ZRelation(Entity event, Translator t, List<String> transitionRules, String storyName, int sentenceIndex) {
		
		Boolean DEBUG = false;
		if(event.getType().equals(Markers.SCENE)) {
			return new ArrayList<>();
		}
		List<ZRelation> zRelations = new ArrayList<>();
		List<String> already = new ArrayList<>();
		Mark.show(DEBUG, event);
		Mark.say(DEBUG, "\n\n         ",persons+"\n\n");
		// multiple rules might be applied to each event/ story element
		for (String ruleStr:transitionRules){
			Mark.say(DEBUG,ruleStr);
			Entity foundRule = null;
			Entity entities = t.translate(ruleStr);
			
			// each rule might have multiple relations
			for(int i = 0; i < entities.getNumberOfChildren(); i ++) {
				Entity rule = entities.getElement(i);
				
				if (rule.getSubject().getElements().size() == 1) {
					// Dig out antecedent and consequent
					Entity antecedent = rule.getSubject().getElements().get(0);
					Entity consequent = rule.getObject();
					Mark.say(DEBUG, antecedent);
					Mark.say(DEBUG, event);
					// Match antecedent with event, produce binding list
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, event);
					Mark.say(DEBUG,"Bindings: ", bindings);
					
					
					// for testing matcher
//					String ruleStr1 = "If xx forgets to take yy from zz, then zz has yy's contact.";
//					String eventStr = "Alice forgot to take her phone from her pants.";
//					Entity antecedent1 = t.translate(ruleStr1).getElement(0).getSubject().getElements().get(0);
//					Entity event1 = t.translate(eventStr).getElement(0);
//					Mark.say(DEBUG, antecedent1);
//					Mark.say(DEBUG, event1);
//					Mark.say("Bindings: ", StandardMatcher.getBasicMatcher().match(antecedent1, event1));
					
					if(bindings != null) {
						
						Map <String, String> objects = new HashMap<String, String>();
						
						if(foundRule == null) {
							foundRule = rule;
						} else {
//							Entity testOne = foundRule.getSubject().getElements().get(0);
//							Entity testTwo = rule.getSubject().getElements().get(0);
//							Mark.say("    "+foundRule);
//							Mark.say("    "+Generator.getGenerator().generate(foundRule));
//							Mark.say("    "+rule);
//							Mark.say("    "+Generator.getGenerator().generate(rule));
//							
//							// if the previous found is more specific, ignore it
//							LList<PairOfEntities> bindingsTwo = StandardMatcher.getBasicMatcher().match(testTwo, testOne);
//							if(bindingsTwo != null) {
//								Mark.say("skipped a more general rule");
//								break;
//							} 
						}
						
						// Check
//						Mark.say(DEBUG,"----------");
//						Mark.say(DEBUG,"Event: ", event.toString());
//						Mark.say(DEBUG,"Antecedent", antecedent);
//						Mark.say(DEBUG,"Consequent", consequent);

						List<String> oldHolders = new ArrayList<>();
						List<String> newHolders = new ArrayList<>();
						
						// Step 1 -- Use binding results to replace with new place holders
						String[] parts = bindings.toString().split(" <");
						
						for (String part : parts) {
							String holder = part.substring(0,part.indexOf("-")).replace("(<", "");
							Mark.say(DEBUG,"     holder: ",holder);
							part = part.substring(part.indexOf(",")+2,part.length());
							String object = part.substring(0, part.indexOf("-"));
							Mark.say(DEBUG,"     object: ",object);
							
							if(!objects.containsKey(object)) {
								
								objects.put(object, Z.holders[Z.NofObjects]);
//								Mark.mit(Z.NofObjects);
//								Mark.show(objects);
								Z.NofObjects++;
							}
							
							oldHolders.add(holder);
							newHolders.add(objects.get(object));
						}
						
//						Mark.say(DEBUG,"objects: ",objects);
//						Mark.say(DEBUG,"oldHolders: ",oldHolders);
//						Mark.say(DEBUG,"newHolders: ",newHolders);
						
						// get consequence in rule, eg, "PP has QQ's contact"
						String english = Generator.getGenerator().generate(consequent);
						
						// replace the place holders with new ones, eg, "CC has BB's contact"
						for(int k=0; k < oldHolders.size();k++){
							english = english.replace(oldHolders.get(k).toUpperCase(), newHolders.get(k).toUpperCase());
						}
						
						// get relation, eg, "phone has pants' contact"
						Entity result = Substitutor.substitute(consequent, bindings);
						String resultTranslated = Generator.getGenerator().generate(result);
						
						// get step, eg, "Alice forgets to take her phone from her pants"
						Entity step = Substitutor.substitute(antecedent, bindings);
						String stepTranslated = Generator.getGenerator().generate(step);
						
						// create ZRelation
						String type = resultTranslated.substring(resultTranslated.lastIndexOf(" ")+1,resultTranslated.length()-1);
						Entity objectAAA = null;
						Entity objectBBB = null;
						Mark.say(DEBUG,"  "+bindings);
						for(PairOfEntities binding : bindings) {
							String realObject = Z.entity2Name(binding.getDatum());
//							Mark.say(DEBUG,binding);
//							if(resultTranslated.toLowerCase().replace("the", "").contains(realObject.replace("_", " "))) {
								if(objectAAA==null) {
									objectAAA = binding.getDatum();
								} else if(objectBBB==null) {
									objectBBB = binding.getDatum();
								}
//							}
						}
						
						String AAAHolder = Z.entity2Name(objectAAA);
						AAAHolder = objects.get(AAAHolder);
						String BBBHolder = "";
						if (objectBBB==null) {
							String newEntityStr = resultTranslated.substring(0,resultTranslated.indexOf("'s"));
							newEntityStr = newEntityStr.substring(newEntityStr.lastIndexOf(" ")+1,newEntityStr.length());
							objectBBB = new Entity(newEntityStr);
							BBBHolder = Z.entity2Name(objectBBB);
							BBBHolder = newEntityStr;
						} else {
							BBBHolder = Z.entity2Name(objectBBB);
							BBBHolder = objects.get(BBBHolder);
						}
						
						Boolean relation;
						if(Z.isNegative(resultTranslated)) {
							relation = false;
						} else {
							relation = true;
						}
						
						if(!already.contains(resultTranslated)) {
							ZRelation a = new ZRelation(
									objectAAA,objectBBB, AAAHolder, BBBHolder, relation, type, 
									result, consequent, step, resultTranslated, english, stepTranslated, 
									storyName, sentenceIndex);
							Mark.show(DEBUG, a);
							Mark.mit(DEBUG, a.getRelation());
							Mark.mit(DEBUG, a.getStep());
							zRelations.add(a);
							already.add(resultTranslated);
						}
					}
				} else {
					t.internalize(ruleStr);
				}		
			}	
		}
		return zRelations;
	}
	
	// to translate a story element into a ZRelation given a translator and a set of transition rules
	public static List<ZRelation> ZRelation(String sentence, Translator t, List<String> transitionRules) {
		List<ZRelation> zRelations = new ArrayList<>();
		Entity events = t.translate(sentence);
		// there might be multiple events/ story elements in a sentence
		for(int j = 0; j < events.getNumberOfChildren(); j ++) {
			Entity event = events.getElement(j);
			zRelations.addAll(ZRelation(event, t, transitionRules));
		}
		return zRelations;
	}
	
	// -------------------------------------
	// to translate a story into ZRelations
	// -------------------------------------
	public static List<ZRelation> story2ZRelations(String story, String transitionRulesName){
		if(transitionRulesName.equalsIgnoreCase("")) {
			transitionRules = initializeRules(transitionRulesDefault);
		} else {
			transitionRules = initializeRules(transitionRulesName);
		}
		return story2ZRelations(story, transitionRules);
	}
	
	public static List<ZRelation> story2ZRelations(String story, List<String> transitionRules){
		
		Boolean debug = false;
		
		List<ZRelation> zrelations = new ArrayList<>(); 
		List<String> sentences = new ArrayList<>(); 
		
		// read the story and internalize person assignments
		
		story = story.trim();
		if(!story.equals("")) {
			if(!story.contains(" ") || story.length()<10) {
				sentences = Z.readStory(story);
				Mark.say("\n\nRead "+ story+"'s story: ");
			} else {
				Map<String, List<String>> returns = Z.readStoryFromFile(story);
				sentences = returns.get(Z.SENTENCES);
				for (String sentence: returns.get(Z.ASSIGNMENTS)) {
					Entity assignment = t.translate(sentence);
					Mark.say("    internalizing...", sentence);
					List<String> nounEntities = Z.getNounNames(assignment);
					if (nounEntities.contains(Z.PERSON)) {
						persons.add(assignment.getElement(0).getObject());
					}
				}
				Mark.say("\n\nRead story of "+story);
			}
		}
		
		return sentences2ZRelations(story, sentences, transitionRules);
		
	}
	public static List<ZRelation> sentences2ZRelations(String story, List<String> sentences, List<String> transitionRules){

		Boolean debug = DEBUG;
		
		List<ZRelation> zrelations = new ArrayList<>(); 
		
//		sentences = Z.tracePersons(sentences); //TODO
//		if(story.startsWith("Stratagem")) {
//			try {
//				declarations = new ArrayList<>(Files.readAllLines(Paths.get("corpora/zStoriesOfWar/"+story+".txt"), StandardCharsets.UTF_8));
//				declarations.removeAll(Arrays.asList("", null));
//				declarations = Z.listDeclarationElements(declarations);
//				for(String declaration: declarations) {
//					t.internalize(declaration);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
//		NewTimer timer = NewTimer.zTimer;
//		timer.initialize();
//		timer.lapTime(true, "1");
		
		int count = 0;
		for (String sentence:sentences){
//			timer.lapTime(true, "2");
			
			count ++;
			Mark.say(count, sentence);
			Mark.say(debug, "-----------------\n\n", count,"\n\n");
			
			Entity events = t.translate(sentence);
			for(int j = 0; j < events.getNumberOfChildren(); j ++) {
				Entity event = events.getElement(j);
				
				// replace "she" and "he" with the character's name
				List<String> nounEntities = Z.getNounNames(event);
				if (nounEntities.contains(Z.SHE)) {
					t.substitute(currentPerson, Z.SHE, event);
					Mark.say(debug, "    subtitutin for..." + currentPerson);
					Mark.say(debug, "\n\n              ", event,"\n\n");
				} 
				if (nounEntities.contains(Z.HE)) {
					t.substitute(currentPerson, Z.HE, event);
					Mark.say(debug, "    subtitutin for..." + currentPerson);
					Mark.say(debug, "\n\n              ", event,"\n\n");
				}
				
				// update character's name that the "she" and "he" are referring to
				for (String noun:nounEntities) {
					for (Entity person:persons) {
						if (noun.equalsIgnoreCase(Z.entity2Name(person))) {
							currentPerson = person;
							Mark.say(debug, "    current person..." + currentPerson);
						}
					}
				}
				
				List<ZRelation> zrelation = ZRelation(event, t, transitionRules, story, count);
				if (zrelation!=null) {
					zrelations.addAll(zrelation);
					Mark.show(debug, "   "+zrelation);
				}
			}
		}
//		String last = "";
//		List<ZRelation> relations = new ArrayList<>();
//		for(ZRelation z: zrelations) {
//			if(!last.equals(z.getRelation())) {
//				relations.add(z);
//			}
//			last = z.getRelation();
//		}
		return zrelations;
	}

	public Entity getAAA() {
		return AAA;
	}

	public void setAAA(Entity aAA) {
		AAA = aAA;
	}

	public Entity getBBB() {
		return BBB;
	}

	public void setBBB(Entity bBB) {
		BBB = bBB;
	}

	public String getAAAHolder() {
		return AAAHolder;
	}

	public void setAAAHolder(String aAAHolder) {
		AAAHolder = aAAHolder;
	}

	public String getBBBHolder() {
		return BBBHolder;
	}

	public void setBBBHolder(String bBBHolder) {
		BBBHolder = bBBHolder;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Boolean getState() {
		return state;
	}
	
	public void setState(Boolean relation) {
		this.state = relation;
	}

	public Entity getRelationInInnerese() {
		return relationInInnerese;
	}

	public void setRelationInInnerese(Entity relationInInnerese) {
		this.relationInInnerese = relationInInnerese;
	}

	public Entity getRelationPatternInInnerese() {
		return relationPatternInInnerese;
	}

	public void setrelationPatternInInnerese(Entity relationPatternInInnerese) {
		this.relationPatternInInnerese = relationPatternInInnerese;
	}
	
	public Entity getStepInInnerese() {
		return stepInInnerese;
	}

	public void setStepInInnerese(Entity step) {
		this.stepInInnerese = step;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getRelationPattern() {
		return relationPattern;
	}

	public void setRelationPattern(String relationPattern) {
		this.relationPattern = relationPattern;
	}
	
	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getStoryName() {
		return storyName;
	}

	public void setStoryName(String storyName) {
		this.storyName = storyName;
	}

	public int getSentenceIndex() {
		return sentenceIndex;
	}

	public void setSentenceIndex(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

	public String toString() {
		
		String result = "AAA = " + this.AAA;
		result = result + "   BBB = " + this.BBB;
		result = result + "   AAAHolder = " + this.AAAHolder;
		result = result + "   BBBHolder = " + this.BBBHolder;
		result = result + "   state = " + this.state;
		result = result + "   type = " + this.type;
		result = result + "   relation = " + this.relation;
		result = result + "   relationPattern = " + this.relationPattern;
		result = result + "   step = " + this.step;
		result = result + "   storyName = " + this.storyName;
		result = result + "   sentenceIndex = " + this.sentenceIndex;
		return result;
		
	}
	
	// my neat representation of relations: "phone + battery contact"
	public String toEquation() {
		return this.AAAHolder + (this.state? " + ":" - ") + this.BBBHolder + " " + this.type;
	}
	
	// my neat representation of relations: "phone + battery contact"
	public String toRGoal() {
		return "  " + (this.state?"+ ":"- ") + this.type + " ( " + Z.entity2Name(this.AAA) + ", "+ Z.entity2Name(this.BBB) +" )";
	}
	
	// In progress. To match relations that are only different in one place in relationPatterns
	public String[] similarTo(ZRelation zRelation2) {
		
		Boolean SimRelation = false;
		Boolean SimType = false;
		
		int difference = 0;
		String MyBefore = "";
		String HisBefore = "";
		
		if(zRelation2.getAAAHolder()!=this.getAAAHolder()) {
			difference++;
			MyBefore = this.getAAAHolder();
			HisBefore = zRelation2.getAAAHolder();
//			Mark.say("AAAHolder: ", MyBefore, HisBefore);
		}
		
		if(zRelation2.getBBBHolder()!=this.getBBBHolder()) {
			difference++;
			MyBefore = this.getBBBHolder();
			HisBefore = zRelation2.getBBBHolder();
//			Mark.say("BBBHolder: ", MyBefore, HisBefore);
		}
//		Mark.say("difference: ", difference);
		
		if(zRelation2.getState()==this.getState()) {
			SimRelation = true;
		}
		
		if(zRelation2.getType().equalsIgnoreCase(this.getType())) {
			SimType = true;
		}
		
		if (SimType && SimRelation && (difference == 1)) {
			return new String[] {MyBefore,HisBefore};
		} else {
			return null;
		}
		
	}
	
	public static Boolean isHolder(String string) {
		for (String str : Z.holders) {
			if(str.equalsIgnoreCase(string)) {
				return true;
			}
		}
		return false;
	}
	
	// to rewrite placeholders after matching
	public static List<ZRelation> switchHolder(List<ZRelation> relationsBefore, String before, String after){
		for(ZRelation relationBefore: relationsBefore) {
//			Mark.say("Before: ",relationBefore.toEquation());
			if(relationBefore.getAAAHolder()==before) {
				relationBefore.setAAAHolder(after);
			}
			if(relationBefore.getBBBHolder()==before) {
				relationBefore.setBBBHolder(after);
			}
//			Mark.say("After: ",relationBefore.toEquation());
		}
		return relationsBefore;
	}
	
	// to construct a list of transition rules from file
	public static List<String> initializeRules() {
		return initializeRules(transitionRulesDefault);
	}
	
	public static List<String> initializeRules(String transitionRulesFile) {
		try {
			List<String> rules = new ArrayList<>();
			// start the conversation with How To questions
			rules = new ArrayList<>(Files.readAllLines(Paths.get(transitionRulesFile), StandardCharsets.UTF_8));
			rules.removeAll(Arrays.asList("", null));
			rules = Z.listRemoveComment(rules);
			rules = Z.listToLower(rules);
			for (String rule: rules) {
				if (rule.startsWith(Markers.IF_MARKER)) {
					transitionRules.add(rule);
				} else {
					t.translate(rule);
					Mark.say("    internalizing...", rule);
				}
			}
//			Mark.say(transitionRules);
			t.translate("she is a person");
			t.translate("he is a person");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return transitionRules;
	}
	
	public static void listPrintEquations(List<ZRelation> relations) {
		int count= 0;
		for(ZRelation relation: relations) {
			count++;
			Mark.say(count+"    "+relation.toEquation());
		}
	}
	
	public static String listPrintRGoals(List<ZRelation> relations) {
		int count= 0;
		String toPrint = "";
		for(ZRelation relation: relations) {
			count++;
			toPrint += count+"   "+relation.toRGoal() + "\n";
			Mark.say(count+"   "+relation.toRGoal());
		}
		return toPrint;
	}
	
	public static List<String> listPrintSteps(List<ZRelation> relations) {
		int count= 0;
		List<String> toPrints =  new ArrayList<>();
		for(ZRelation relation: relations) {
			count++;
			Mark.say("    "+count+"    "+relation.getStep());
			toPrints.add(relation.getStep());
			
		}
		return toPrints;
	}
	
	public static List<String> listPrintList(List<String> strings) {
		int count= 0;
		List<String> toPrints =  new ArrayList<>();
		for(String string: strings) {
			count++;
			Mark.say("    "+count+"    "+string);
			toPrints.add(string);
			
		}
		return toPrints;
	}
	
	public static List<String> listPrintStepsPresent(List<ZRelation> relations) {
		int count= 0;
		List<String> toPrints =  new ArrayList<>();
		List<Integer> hasSentence = new ArrayList<>();
		for(ZRelation relation: relations) {
			int index = relation.sentenceIndex;
			if(!hasSentence.contains(index)) {
//				hasSentence.add(index);
				count++;
				Mark.say("    "+count+"    "+Z.verbs2Present(relation.getStep()));
				toPrints.add(Z.verbs2Present(relation.getStep()));
			}
		}
		return toPrints;
	}
	
	public static List<String> listPrintRelations(List<ZRelation> relations) {
		int count= 0;
		List<String> toPrints =  new ArrayList<>();
		for(ZRelation relation: relations) {
			count++;
			Mark.say("    "+count+"    "+relation.getRelation());
			toPrints.add(relation.getRelation());
			
		}
		return toPrints;
	}
	
	public static List<String> listPrintRelationPatterns(List<ZRelation> relations) {
		int count= 0;
		List<String> toPrints =  new ArrayList<>();
		for(ZRelation relation: relations) {
			count++;
			Mark.say(count+"    "+relation.getRelationPattern());
			toPrints.add(relation.getRelationPattern());
		}
		return toPrints;
	}
	
	public static void testTranslateStory() {
		// test one story
		initializeRules(); 
		
//		List<ZRelation> ones = story2ZRelations("Alice",new ArrayList<>(),t,transitionRules);
		List<ZRelation> twos = story2ZRelations("Alice replaces cellphone battery",transitionRules);
		
		listPrintRelations(twos);
	
	}
	
	public static void reset() {
		Z.NofObjects = 0;
		persons = new ArrayList<>();
		currentPerson = null;
	}

	public static void testAliceBobReplaceBattery() {
		List<ZRelation> ones = story2ZRelations("Alice replaces cellphone battery",transitionRulesDefault);
		reset();
		List<ZRelation> twos = story2ZRelations("Bob replaces cellphone battery",transitionRulesDefault);
		
//		Mark.say("\n\n Steps in Alice's story: ");
//		listPrintSteps(ones);
//		Mark.say("\n Steps in Bob's story: ");
//		listPrintSteps(twos);
//		
		Mark.say("\n\n Relation pattens in Alice's story: ");
		listPrintRelationPatterns(ones);
		Mark.say("\n Relation pattens Bob's story: ");
		listPrintRelationPatterns(twos);
		
		Mark.say("\n\n Relations in Alice's story: ");
		listPrintRelations(ones);
		Mark.say("\n Relations in Bob's story: ");
		listPrintRelations(twos);
		
		List<ZRelation> commons = getCommonSubgoals(Arrays.asList(ones,twos)); 
		Mark.say("list: ");
		listPrintSteps(commons);
	}
	
	public static void testSentence() {
		t.translate("Bob is a person");
		t.translate("Phone is a thing");
		t.translate("Cover is a thing");
//		t.translate("battery is a thing");
		transitionRules = ZRelation.initializeRules(transitionRulesDefault);
		List<ZRelation> zz = ZRelation("Then, Bob slid down the cover from the phone.", t, transitionRules);
		listPrintRelations(zz);
		listPrintSteps(zz);
		
	}
	
	public static void main(String[] args) {
		
//		testAliceBobReplaceBattery();
		testSentence();
	}
	
	public static void listPrint(List<ZRelation> relations) {
		for(ZRelation relation:relations) {
			Mark.say(relation.toString());
		}
	}
	
	public static List<ZRelation> getCommonSubgoals(List<List<ZRelation>> stories){
		
		Boolean DEBUG = false;
		List<ZRelation> commons = new ArrayList<>();
		int countStory = 0;
		for(List<ZRelation> story: stories) {
			if (countStory==0) {
				commons = new ArrayList<ZRelation>(story);
			} else {
				List<ZRelation> newCommons = new ArrayList<>();
				for (ZRelation relation : story) {
					Mark.night(DEBUG, "check..."+relation.getRelation());
					for(ZRelation common:commons) {
						
						String temp1 = common.getRelation();
						temp1 = Z.event2SimpleAction(temp1).replace("the ", "");
						
						String temp2 = relation.getRelation();
						temp2 = Z.event2SimpleAction(temp2);
						String temp3 = temp2.replace("the ", "");
						
						Mark.say(DEBUG,"temp1: ",temp1);
						
						if(temp1.equalsIgnoreCase(temp3)){
							if(!stepsGeneral.contains(temp2) && !stepsGeneral.contains(temp3)) {
								stepsGeneral.add(temp2);
							}
							Mark.show(DEBUG,relation);
							newCommons.add(relation);
							break;
						}
					}
				}
				commons = new ArrayList<>(newCommons);
			}
			countStory++;
		}
		List<String> relations = new ArrayList<>();
		List<ZRelation> newCommons = new ArrayList<>();
		for(ZRelation common: commons) {
			if(!relations.contains(common.getRelation())) {
				relations.add(common.getRelation());
				newCommons.add(common);
			}
		}
		Mark.mit(DEBUG,newCommons.size());
		return newCommons;
	}
	
	public static List<List<ZRelation>> listItemToEnd(List<List<ZRelation>> ones, int j) {
		List<List<ZRelation>> twos = new ArrayList<>(ones);
		twos.add(twos.remove(j));
		return twos;
	}
	
	public static void listItemToEndStr(List<List<String>> ones, int j) {
		ones.add(ones.remove(j));
	}
	


}

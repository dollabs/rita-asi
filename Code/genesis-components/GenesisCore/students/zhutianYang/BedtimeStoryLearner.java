/**
 * 
 */
package zhutianYang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import constants.Radio;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import matchers.StandardMatcher;
import matchers.Substitutor;
import utils.Html;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;

public class BedtimeStoryLearner extends AbstractWiredBox {
	
	public static Boolean DEBUG = false;

	// long term memory
	public static final String relationRulesFile = "corpora/zStoriesOfWar/zBedtimeRelationRules.txt";

	// Short term memory
	public static Generator g = Generator.getGenerator();
	public static Translator t = getTranslator();
	public static String humanEnglish;
	public static String previousEnglish;
	public static String previousObject;
	public static String currentObject;
	public static String currentSubject;
	public static List<String> names = new ArrayList<>();
	public static List<String> notNames = StratagemExpert.notNames;
	public static Map<String, ZAgent> agents = new HashMap<String, ZAgent>();
	
	// Template expressions
	public static final String SAY_LEARNED = "// Learned new problem-solving micro-story:";
	
	// Ports
	public static final String FROM_QUESTION_EXPERT = "question expert to bedtime story learner";
	public static final String TO_COMMENTARY = "bedtime story learner to commentary";
	public static final String FROM_TEXT_ENTRY_BOX = "text entry box to bedtime story learner";
	public static final String TO_CLEAR_TEXT_ENTRY_BOX  = "bedtime story learner to clear text entry";
	
	public BedtimeStoryLearner() {
		super("Bedtime story learner");
		Connections.getPorts(this).addSignalProcessor(FROM_TEXT_ENTRY_BOX, this::getResponse);
	}
	
	public void getResponse(Object thing) {
		if (!Radio.qToZTYBTS.isSelected())  return;
		
		// print human input to commentary
		humanEnglish = (String) thing;
		recognizeNames(humanEnglish);
		printCommentary(humanEnglish,"Human");
		if(humanEnglish.equals("He wanted to know what is important in doing business.")) {
			humanEnglish = "He wanted to know what-is-important";
		}
		if(humanEnglish.equals("So, he called CEOs at Silicon Valley to ask them.")) {
			humanEnglish = "So, Steve asked CEOs.";
		}
		Boolean agentsUpdated = false;
		
		// ------------------------------------------
		// Step 1: get subject
		// ------------------------------------------
		recognizeNames(humanEnglish);
		String subject = getSubject(humanEnglish);
		String object = getObject(humanEnglish);
		if(!agents.containsKey(subject)) {
			ZAgent agent = new ZAgent();
			agent.setName(subject);
			agents.put(subject, agent);
			agentsUpdated = true;
			currentSubject = subject;
		} else {
			currentSubject = subject;
		}
		
		// ------------------------------------------
		// Step 2: learn knowledge if is consequence
		// ------------------------------------------
		String problem;
		List<String> checks = new ArrayList<>();
		List<String> steps = new ArrayList<>();
		List<String> happens = new ArrayList<>();
		Entity entity = t.translate(humanEnglish).getElement(0);
		List<String> learned = new ArrayList<>();
		if(entity.getFeatures().contains("therefore")
				|| entity.getFeatures().contains("so")){
			humanEnglish = humanEnglish.replace("Therefore, ", "").replace("So, ", "");
			
			problem = agents.get(subject).getMentality("goal");
			steps.add(humanEnglish);
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) {
				checks.addAll(agents.get(object).getEvents());
				agents.get(object).resetEvents();
			}
			checks = checks.stream().distinct().collect(Collectors.toList());
			learned = StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
		
		} else if (subject.equals(previousObject)) {
			
			problem = humanEnglish;
			steps.add(previousEnglish);
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) {
				checks.addAll(agents.get(object).getEvents());
				agents.get(object).resetEvents();
			}
			checks = checks.stream().distinct().collect(Collectors.toList());
			learned = StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
		
		} else if (entity.getTypes().contains(Markers.CAUSE_MARKER)){
			
			problem = Z.generate(entity.getObject());
			Entity entity1 = entity.getSubject();
			if(Z.isJustDoIt(entity1)) {
				happens.add(Z.generate(entity1));
			} else {
				steps.add(Z.generate(entity1));
			}
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) {
				checks.addAll(agents.get(object).getEvents());
				agents.get(object).resetEvents();
			}
			checks = checks.stream().distinct().collect(Collectors.toList());
			learned = StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
			
		}
		
		if(!learned.isEmpty()) {
			printCommentary(SAY_LEARNED,"Comment");
			for(String string : learned) {
				printCommentary(string,"Comment");
			}
		}
		
		// ------------------------------------------
		// Step 3: extract and remember information
		// ------------------------------------------
		Mark.say(entity.getTypes());
		if(entity.getTypes().contains("social relation")) {
			
			String relationship = entity.getTypes().get(entity.getTypes().size()-1);
			String humanEnglish = Z.entity2Name(entity.getSubject()).replace("_", " ");
			agents.get(subject).addRelationship(relationship, humanEnglish);
			if(humanEnglish.contains(" are ")) {
				agents.get(subject).setKind(ZAgent.KIND_HUMAN_GROUP);
			}
			agentsUpdated = true;
			
		} else if(entity.getTypes().contains("action") &&
				(entity.getTypes().contains("goal") || entity.getTypes().contains("plan"))){
//			String mentality = entity.getTypes().get(entity.getTypes().size()-1);
			
			entity = entity.getObject();
			if(entity.getName().startsWith("roles")) {
				List<Entity> entities = new ArrayList<>();
				entities.addAll(entity.getChildren());
				entity = entities.get(0);
			}
			if(entity.getName().startsWith("object")) {
				entity = entity.getSubject();
			}
			if(entity.getTypes().contains("action")) {
				entity.setSubject(new Entity(subject));
				String value = g.generate(entity);
				agents.get(subject).addMentality("goal", value);
				agentsUpdated = true;
			}
			
		} else if(entity.getTypes().contains("cause")
				&& entity.getTypes().contains("means")){
			
			entity = entity.getObject();
			entity.setSubject(new Entity(subject));
			entity.addProperty(Markers.IS_IMPERATIVE_MARKER, "yes");
			String value = g.generate(entity);
			agents.get(subject).addMentality("goal", value);
			agentsUpdated = true;
		
		} else {
			
			entity.setSubject(new Entity(subject));
			humanEnglish = g.generate(entity);
			agents.get(subject).addEvent(humanEnglish);
			if(agents.containsKey(object)) agents.get(object).addEvent(humanEnglish);
		}
		
		// ------------------------------------------
		// Step 4: print all agents
		// ------------------------------------------
		if(agentsUpdated) {
			for(String key:agents.keySet()) {
				printCommentary(agents.get(key).toString(),"Imagination");
			}
		}
		
		previousEnglish = humanEnglish;
		previousObject = object;
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
//		Mark.say("-------- names --------");
//		Mark.say(names);
//		Mark.say("-------- ----- --------");
	}
		
	// --------------------------------------------------
	// Communication processes
	// -------------------------------------------------------
	public void printCommentary(Object messageObject, String name) {

		// HTML color names
		//      https://www.w3schools.com/colors/colors_names.asp

		String message = (String) messageObject;
		message = StratagemExpert.capitalizeNames(message, names);
		
		if (Radio.qToZTYBTS.isSelected()) {
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
				
			// for commenting learned micro-story
			} else if(name=="Imagination") {
				message = Html.bold("\n ") + message;
				message = Html.coloredText("#FFA500",message); // orange
				message = Html.small(message);

			} else  if(name=="Human") {
				message = Html.bold("\n\n "+name+": ") + message;
				message = Html.normal(message);

			} else if(name=="Genesis") {
				message = Html.bold("\n ") + message;
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
	
	public static Translator getTranslator() {
		Translator t = Translator.getTranslator();
//		t.internalize("xx is a person");
//		t.internalize("yy is a person");
//		t.internalize("xxx is a person");
//		t.internalize("yyy is a person");
//		t.internalize("zzz is a person");
//		t.internalize("mmm is a person");
//		t.internalize("yuan is a person");
//		t.internalize("Yuan is a person");
//		t.internalize("Chong is a person");
//		t.internalize("Tai is a person");
//		t.internalize("Huang is a person");
//		t.internalize("Someone is a person");
		return t;
	}

	public static String getObject(String string) {
		Entity entity = t.translate(string).getElement(0);
		String subject = "";
		String object = "";
		string = string.toLowerCase();
		
//		if(entity.getTypes().contains(Markers.CAUSE_MARKER)) {
//			entity = entity.getSubject();
//		}
		if(!entity.getTypes().contains("relation") 
				&& !entity.getTypes().contains(Markers.SOCIAL_MARKER) ) {
			object = entity.getSubject().toString();
			int countBracket = object.length() - object.replace("(", "").length();
			if(countBracket==1) {
				subject = object.substring(5,object.indexOf("-"));
				object = entity.getObject().toString();
			}
			
			if (object.contains("(fun object ")) {
				String objectStr = object.substring(object.indexOf("(fun object")+11);
				objectStr = objectStr.substring(objectStr.indexOf("(ent ")+5);
				object = objectStr.substring(0,objectStr.indexOf("-"));
				if(object.equals(subject)) {
					if (objectStr.contains("(fun object ")) {
						object = objectStr.substring(objectStr.indexOf("(fun object")+11);
						object = object.substring(object.indexOf("(ent ")+5);
						object = object.substring(0,object.indexOf("-"));
					}
				}
			} else if (object.contains("(fun ")) {
				object = object.substring(object.indexOf("(fun")+4);
				object = object.substring(object.indexOf("(ent ")+5);
				object = object.substring(0,object.indexOf("-"));
			} else {
				object = "I don't know";
			}
		
//			if (object.contains("(fun object (rel believe (ent ")) {
//				object = object.substring(object.indexOf("(fun object (rel believe (ent ")+30);
//				object = object.substring(0,object.indexOf("-"));
//			} else if (object.contains("(fun object (ent ")) {
//				object = object.substring(object.indexOf("(fun object (ent")+17);
//				object = object.substring(0,object.indexOf("-"));
//			} else if (object.contains("(fun object ")) {
//				object = object.substring(object.indexOf("(fun object")+11);
//				object = object.substring(object.indexOf("(ent ")+5);
//				object = object.substring(0,object.indexOf("-"));
//			} else if (object.contains("(fun ")) {
//				object = object.substring(object.indexOf("(fun")+4);
//				object = object.substring(object.indexOf("(ent ")+5);
//				object = object.substring(0,object.indexOf("-"));
//			} else {
//				object = "I don't know";
//			}
		}
		
//		List<String> nouns = Z.getNounEntity(entity);
//		if(nouns.size()==2) {
//			for(String noun: nouns) {
//				if(noun!=subject) object = noun;
//			}
//		} 
		
		return object.toLowerCase().replace("_"," ");
	}
	
	public static String getSubject(String string) {
		Entity entity = t.translate(string).getElement(0);
		String subject = "";
		string = string.toLowerCase();
//		Mark.say("       types: ",entity.getTypes());
		if(entity.getTypes().contains("social relation")) {
			subject = Z.entity2Name(entity.getObject());
		} else if (entity.getTypes().contains("action")
				|| entity.getTypes().contains("goal")
				|| entity.getTypes().contains("desire")
				|| entity.getTypes().contains("want")){
			subject = Z.entity2Name(entity.getSubject());
		}

		// refer to an existing character
		if(subject.equals("") && string.startsWith("he ") || subject.equals("he")) {
			subject = currentSubject;
			if(agents.containsKey(subject)) {
				agents.get(subject).setGender("male");
			}
		} else if(subject.equals("") && string.startsWith("she ") || subject.equals("she")) {
			subject = currentSubject;
			if(agents.containsKey(subject)) {
				agents.get(subject).setGender("female");
			}
		} else if(subject.equals("") && string.startsWith("they ") || subject.equals("they")) {
			subject = currentSubject;
			if(agents.containsKey(subject)) {
				agents.get(subject).setGender("male");
			}
		} else if (subject.equals("")){
			subject = string.substring(0,string.indexOf(" "));
		} else {
			currentSubject = subject;
		}
		return subject.toLowerCase().replace("_"," ");
	}
	
	
	public static List<String> storyToRelations(List<String> sentences){
		List<String> newSentences = new ArrayList<>();
		List<String> rules = Z.readRulesInFile(relationRulesFile);
		for(String sentence: sentences) {
			newSentences.add(sentence);
			Entity sentenceEntity = t.translate(sentence);
			
			// get all story elements in a sentence
			List<Entity> variations = new ArrayList<>();
			for(Entity entity: sentenceEntity.getElements()) {
				variations.add(entity);
				
				if(entity.getSubject()!=null) 
					variations.add(entity.getSubject());
				
				if(entity.getObject()!=null) 
					variations.add(entity.getObject());
			}
//			Mark.say(variations);
			
//			// get rid of all prepositions
//			List<Entity> variations2 = new ArrayList<>();
//			for(Entity entity:variations2) {
//				if(Z.isRel(entity)) {
//					Sequence newSequence = new Sequence("roles");
//					Entity sequence = entity.getObject();
//					for(Entity entity1: sequence.getElements()) {
//						if(entity1.getName().startsWith("object")) {
//							newSequence.addElement(entity1);
//						}
//					}
////					Relation newRelation = new Relation(entity.getName());
//				}
//			}
			
			// for each elements, match rules
			for(Entity entity:variations) {
				if(entity.getName().startsWith("means")) {
					entity = entity.getSubject();
				}
				if(entity.getName().startsWith("recipe")) {
					entity = entity.getElement(0);
				}
				for(String rule:rules) {
					Entity ruleEntity = t.translateToEntity(rule);
					Entity antecedent = ruleEntity.getSubject().getElements().get(0);
					Entity consequent = ruleEntity.getObject();
					LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, entity);
					if(bindings!=null) {
						Entity result = Substitutor.substitute(consequent, bindings);
						String temp = Generator.getGenerator().generate(result);
						if(!newSentences.contains(temp)) {
							newSentences.add(temp);
						}
					}
				}
			}
		}
		return newSentences;
	}
	

	public static void understandSentence(String string) {
		
		recognizeNames(string);
		if(string.equals("He wanted to know what is important in doing business.")) {
			string = "He wanted to know what-is-important";
		}
		if(string.equals("So, he called CEOs at Silicon Valley to ask them.")) {
			string = "So, Steve asked CEOs.";
		}
		String subject = getSubject(string).toLowerCase();
		String object = getObject(string).toLowerCase();
		System.out.println("       subject: "+ subject);
		System.out.println("        object: "+ object);
		if(!agents.containsKey(subject)) {
			ZAgent agent = new ZAgent();
			agent.setName(subject);
			agents.put(subject, agent);
			currentSubject = subject;
		} else {
			currentSubject = subject;
		}
		
		
		Entity entity = t.translate(string).getElement(0);
		
		// learn knowledge
		String problem;
		List<String> checks = new ArrayList<>();
		List<String> steps = new ArrayList<>();
		List<String> happens = new ArrayList<>();
		
		if(entity.getFeatures().contains("therefore")
				|| entity.getFeatures().contains("so")){
			string = string.replace("Therefore, ", "").replace("So, ", "");
			
			problem = agents.get(subject).getMentality("goal");
			steps.add(string);
//			checks.add(previousEnglish);
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) checks.addAll(agents.get(object).getEvents());
			checks = checks.stream().distinct().collect(Collectors.toList());
			StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
		
		} else if (subject.equals(previousObject)) {
			problem = string;
			steps.add(previousEnglish);
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) checks.addAll(agents.get(object).getEvents());
			checks = checks.stream().distinct().collect(Collectors.toList());
			StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
		
		} else if (entity.getTypes().contains(Markers.CAUSE_MARKER)){
			problem = Z.generate(entity.getObject());
			
			Entity entity1 = entity.getSubject();
			if(Z.isJustDoIt(entity1)) {
				happens.add(Z.generate(entity1));
			} else {
				steps.add(Z.generate(entity1));
			}
			checks.addAll(agents.get(subject).getEvents());
			checks.remove("Guo is a guo.");
			agents.get(subject).resetEvents();
			if(agents.containsKey(object)) checks.addAll(agents.get(object).getEvents());
			checks = checks.stream().distinct().collect(Collectors.toList());
			StratagemLearner.writeMicroStory(problem, checks, happens, steps, names);
		}
		
		
		// remember information
//		Mark.say(entity.getTypes());
		if(entity.getTypes().contains("social relation")) {
			String relationship = entity.getTypes().get(entity.getTypes().size()-1);
			String agent = Z.entity2Name(entity.getSubject()).replace("_", " ");
			agents.get(subject).addRelationship(relationship, agent);
			if(string.contains(" are ")) {
				agents.get(subject).setKind(ZAgent.KIND_HUMAN_GROUP);
			}
		
		} else if(entity.getTypes().contains("action") &&
				(entity.getTypes().contains("goal") || entity.getTypes().contains("plan"))){
//			String mentality = entity.getTypes().get(entity.getTypes().size()-1);
			
			entity = entity.getObject();
			if(entity.getName().startsWith("roles")) {
				List<Entity> entities = new ArrayList<>();
				entities.addAll(entity.getChildren());
				entity = entities.get(0);
			}
			if(entity.getName().startsWith("object")) {
				entity = entity.getSubject();
			}
			if(entity.getTypes().contains("action")) {
				entity.setSubject(new Entity(subject));
				String value = g.generate(entity);
				agents.get(subject).addMentality("goal", value);
			}
			
//		} else if(entity.getTypes().contains("action")
//				&& entity.getTypes().contains("plan")){
//			entity = entity.getObject();
//			entity.setSubject(new Entity(subject));
//			String value = g.generate(entity);
//			agents.get(subject).addMentality("goal", value);
			
		} else if(entity.getTypes().contains("cause")
				&& entity.getTypes().contains("means")){
			entity = entity.getObject();
			entity.setSubject(new Entity(subject));
			entity.addProperty(Markers.IS_IMPERATIVE_MARKER, "yes");
			String value = g.generate(entity);
			agents.get(subject).addMentality("goal", value);
		
		} else {
			entity.setSubject(new Entity(subject));
			string = g.generate(entity);
			agents.get(subject).addEvent(string);
			if(agents.containsKey(object)) agents.get(object).addEvent(string);
		}
		
		
		System.out.println();
		for(String key:agents.keySet()) {
			System.out.println(agents.get(key).toString());
		}
		System.out.println();
		
		previousEnglish = string;
		previousObject = object;
	}
	
	public static void main(String[] args) {
		List<String> sentences = getTestStory();
		
//		sentences = storyToRelations(sentences);
//		Mark.say(sentences);
		for(int i = 0;i<sentences.size();i++) {
			Mark.say((i+1), "  ---- ", sentences.get(i));
			understandSentence(sentences.get(i));
		}
	}

	public static List<String> getTestStory() {
		List<String> sentences = new ArrayList<>();
		sentences.add("Steve is a creator of Apple I.");
		sentences.add("He wanted to know what is important in doing business.");
		sentences.add("So, he called CEOs at Silicon Valley to ask them.");
		sentences.add("CEOs mentored Steve.");
		sentences.add("Steve eventually created a great company.");
		
		
//		sentences.add("Huang is the leader of the Mongolia tribe.");
//		sentences.add("He wanted to overturn the Ming Dynasty.");
//		sentences.add("Yuan is a general of the Ming Dynasty.");
//		sentences.add("Yuan fought against Huang to protect Ming Dynasty.");
//		sentences.add("Huang could not defeat Yuan on the battle field.");
//		sentences.add("Therefore, Huang planned to kill Yuan secretly.");
//		sentences.add("Chong is the king of Ming Dynasty.");
//		sentences.add("Tai is the secretary of Chong.");
//		sentences.add("Chong trusts Tai very much.");
//		sentences.add("So, Huang bribed Tai.");
//		sentences.add("Tai made the Chong believe that Yuan is disloyal.");
//		sentences.add("Chong believed that Yuan is disloyal.");
//		sentences.add("He went furious.");
//		sentences.add("Because Chong hates disloyal generals, he executed Yuan immediately.");
//		sentences.add("Nobody could defeat Huang, so Huang conquered the Ming Dynasty and created the Qing Dynasty.");
		return sentences;
	}
}

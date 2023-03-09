package zhutianYang;

import java.util.ArrayList;
import java.util.List;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.Z;

public class ZSay {
	
	// ----------- for Recipe Expert
	public static final String B = "///";

	public static final String PARDON = "Pardon? Could you say it in a more concise and grammatical way?";
	
	public static final String DO_IT_DIFFERENTLY = "Do I /// differently under other conditions?";
	
	public static final String ASK_FOR_CONSEQUENCE = "What is the consequence of ///?";
	
	public static final String TEACH_ME = "Can you teach me how to ///?";
	
	public static final String LEARNED = "I learned how to ///.";
	
	public static final String START_EXECUTION = "Let me show you how ///.";
	
	public static final String JUST_DO_IT = "just do it";
	
	public static final String KNOWLEDGE_MAP = "goal  @  file";
	
	public static final String OK = "Ok.";
	
	public static final String HELLO = "Hi, ///. I am Genesis.";
	
	// ----------- for Recipe Expert learn from stories
	public static final String I_LEARN_STEPS_HOW = "I learned the steps how ///:";
	
	
	// ----------- for Stratagem Expert
	public static final String SAY_DOES_IT_APPLY_HERE = " Does this apply to your problem?";
	
	public static final String SAY_WHAT_IF = "What if ";
	
	public static final String SAY_NO_PROBLEM = "That isn't a problem.";
	
	public static final String SAY_WHAT_CONDITION = "What are the conditions of this method?";
	
	public static final String SAY_IS_THAT_ALL = "Is that all?";
	
	public static final String SAY_LEARNED = "// Learned new problem-solving micro-story:";
	
	public static final String HEAR_DONT_KNOW = "I don't know.";
	
	public static final String HEAR_GARBAGE = "...";
	
	// ----------- for robot
	// for observed objects in the world
	public static final String R = "[[";
		
	public static final String HI = "Hi, I am ready to learn something!";
	
	public static final String EMPTY = "empty";
	
	public static final String ACTIVATE = "activate";
	
	public static Translator t = Translator.getTranslator();
	public static Generator g = Generator.getGenerator();
	
	// say(ZSay.ASK_FOR_CONSEQUENCE, unknownSteps.get(0))
	public static String say(String wrapper, String content) {
		return wrapper.replace(B, content);
	}
	
	public static String whereIs(String object) {
		if(object.endsWith("s")) {
			return "Where are " + object;
		} else {
			return "Where is " + object;
		}
	}
	
	public static void testGenerateQuestions() {
		List<String> strings = new ArrayList<>();
		strings.add("make a salad");
		for(String string: strings) {
//			Mark.say(howDoI(string));
//			Mark.say(canYouTeachMe(string));
//			Mark.say(doIAlways(string));
		}
		Z.printInnereseTree("can you teach me something");
//		Z.printInnereseTree("how do i make a salad");
	}
	
	
//	public static final String HOW_DO_I = "How do I ///?";
	public static String howDoI(String string) {
		Entity action = t.translateToEntity(string);
		if(action.getSubject().getType().equals(Markers.YOU)) {
			action.setSubject(new Entity(Markers.I));
		}
		action = new Function(Markers.HOW_QUESTION, action);
		return g.generate(action);
	}

	public static String canYouTeachMe(String string) {
		Entity action = t.translateToEntity(string);
		if(action.getSubject().getType().equals(Markers.YOU)) {
			action.setSubject(new Entity(Markers.I));
		}
		
		Sequence roles = new Sequence(Markers.ROLE_MARKER);
		roles.addElement(new Function(Markers.OBJECT_MARKER, new Entity("something")));
		roles.addElement(new Function(Markers.TO_MARKER, new Entity(Markers.I)));
		
		Relation teach = new Relation("teach", new Entity("you"), roles);
		action = new Function(Markers.DID_QUESTION, teach);
		
		return g.generate(action);
		
		
//		Entity entity = t.translate(string).getElement(0);
//		String triples = g.generateTriples(entity);
//		String verb = Z.getVerbInTriples(triples);
//		String[] temps = triples.split("]");
//		String newTriples = "";
//
//		for(String temp: temps) {
//			if(!temp.contains(verb + " is_imperative") &&
//					!temp.contains(verb + " has_tense to")) {
//				newTriples = newTriples + temp + "]";
//			}
//		}
//		newTriples = newTriples.replace("[you+", "[one+");
//		newTriples = newTriples + "[teach+1 how+1 make+2][teach+1 has_person 2][teach+1 has_tense present]\n" + 
//				"[teach+1 has_modal can][teach+1 is_question yes]"
//				+ "[you teach+1 i][you has_number singular][you is_proper yes]" 
//				+ "[i has_number singular][i is_proper yes][how+1 is_clausal yes][how+1 is_main yes]";
//		newTriples = newTriples + "["+verb+" has_person 3]"
//				+ "["+verb+" has_tense to]"
//				+ "["+verb+" has_clause_type conn-to]";
//		return Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
	}

	
//	public static final String ASK_FOR_CONDITIONS = "Should I always use this method?";
	public static String doIAlways(String string) { 
//		return say(DO_IT_DIFFERENTLY,string); // TODO
		Entity entity = t.translate(string).getElement(0);
		String triples = g.generateTriples(entity);
		String verb = Z.getVerbInTriples(triples);
		String[] temps = triples.split("]");
		String newTriples = "";

		for(String temp: temps) {
			if(!temp.contains(verb + " is_imperative") ) {
				newTriples = newTriples + temp + "]";
			}
		}
		newTriples = newTriples.replace("[you+", "[i+");
		newTriples = newTriples + "[way+25520 has_det this][has_modifier+26 has_position mid_verbal]";
		newTriples = newTriples + "["+verb+" in+4 way+25520]"
				+ "["+verb+" has_modifier+26 always]"
				+ "["+verb+" is_question yes]";
		return Start.getStart().generate(newTriples).replace("\n", "").replace(".", "");
	}
	
	public static void main(String[] args) {
		testGenerateQuestions();
	}
	
}

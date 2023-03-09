package subsystems.rashi;

import frames.entities.Entity;
import matchers.Substitutor;
import translator.Translator;
import utils.Mark;

/**
 * Class dedicated to generated sentences (entities) for composing the author story 
 * i.e, the story about what the author is doing in a story.
 * @author Suri_Bandler
 *
 */
public class AuthorStoryGenerators {

	
	/**
	 * Given a sentence, translate and replace bob 
	 * Assumes sentence is of the form "xx refers to Bob as a yy"
	 */
	private static Entity replaceBobObject(String authorSentence, Entity substiution) {
		
		Translator translator = Translator.getTranslator();
		Entity general = translator.translate(authorSentence);
		//Mark.say(general.get(0).get(1).get(0))
		Entity bob = general.get(0).getObject().get(0).getSubject();
		Entity withBobSubbed = Substitutor.substitute(substiution, bob, general);
		return withBobSubbed;
	}
	
	/**
	 * Returns the sentence(entity) "Author refers to entity as a beneficiary"
	 * @param object
	 */
	public static Entity beneficiaryMetric(Entity object) {
		String authorSentence = "author refers to Bob as a beneficiary";
		return replaceBobObject(authorSentence, object);
	}
	
	/**
	 * Returns the sentence(entity) "Author refers to entity as a victim"
	 * @param object
	 * @return
	 */
	public static Entity victimMetric(Entity object){
		
		String authorSentence = "author refers to Bob as a victim";
		return replaceBobObject(authorSentence, object);
		
		/*
		Translator translator = Translator.getTranslator();
		Entity general = translator.translate("author refers to Bob as a victim");
		//Mark.say(general.get(0).get(1).get(0))
		Entity bob = general.get(0).getObject().get(0).getSubject();
		Entity withBobSubbed = Substitutor.substitute(object, bob, general);
		return withBobSubbed;
		*/
	}
	
	
	/**
	 * Given a sentence, translate and replace bob 
	 * Assumes sentence is of the form "xx says that Bob AA a DD action [against a QQ object]"
	 */
	
	private static Entity replaceBobAgent(String authorSentence, Entity substitution) {
		
		Translator translator = Translator.getTranslator();
		
		Entity general = translator.translate(authorSentence);
		//Mark.say("BOB COMMITS A NEGATIVE ACTION", general.get(0).getObject().get(0).getSubject().getSubject());
		Entity bob = general.get(0).getObject().get(0).getSubject().getSubject();
		Entity withBobSubbed = Substitutor.substitute(substitution, bob, general);
		Mark.say("SUBBED", withBobSubbed.toEnglish());
		
		return withBobSubbed;
		
	}
	
	/**
	 * Get a sentence (entity) depicting that the author says that xx carries out a positive action [to (type of) object]
	 */
	
	public static Entity benefactorMetric(Entity subject, boolean objectIsBad) {
		
		String authorSentence = "author says that Bob carries out a kindness";//"author says that Bob carries out a positive action";
		if(objectIsBad) authorSentence += " against a malefactor";//" against a negative object";
		
		return replaceBobAgent(authorSentence, subject);
		
	}
	
	/**
	 * Get a sentence (entity) depicting that the author says that xx did a negative action  [to (type of) object].
	 * @param subject
	 * @return
	 */
	public static Entity blameMetric(Entity subject, boolean objectIsBad){
		
		//Translator translator = Translator.getTranslator();
		
		String authorSentence = "author says that Bob commits an inequity";// inequity "author says that Bob commits a negative action";
		if(objectIsBad) authorSentence += " against a malefactor";//" against a negative object";
		
		return replaceBobAgent(authorSentence, subject);
		/*
		Entity general = translator.translate(authorSentence);
		//Mark.say("BOB COMMITS A NEGATIVE ACTION", general.get(0).getObject().get(0).getSubject().getSubject());
		Entity bob = general.get(0).getObject().get(0).getSubject().getSubject();
		Entity withBobSubbed = Substitutor.substitute(subject, bob, general);
		Mark.say("SUBBED", withBobSubbed.toEnglish());
		return withBobSubbed;
		*/
		
	}

	/**
	 * Get sentence (entity) for author story indicating what the author invokes for a specific character 
	 * according to their role (subject/object) in positive/negative action.
	 * @param invokation
	 * @param person
	 * @param classification
	 * @param actionsWhat
	 * @return
	 */
	public static Entity getEntityResultSubject(String invokation, String person, String classification, String actionsWhat, Boolean printAll){
		Translator translator = Translator.getTranslator();
		String sentence = null;
		if(classification.equals("positive")){
			//This links the action mentioned (by ID).
			// TODO: Maybe say original action is bad activity?  
			Entity action = translator.translate("The action is positive");
			sentence = "The author "+ invokation + " for " + person +" because " + person + " is an " +actionsWhat + " of a positive action"; //+actionsWhat + ".";
			
		}
		if(classification.equals("negative")){
			//This links the activity's mentioned (by ID)
			Entity activity = translator.translate("The activity is negative");
			sentence = "The author "+ invokation + " for " + person +" because " + person + " is an "+actionsWhat + " of a negative activity";// +actionsWhat + ".";
			
		}
		
		//String sentence = "The author "+ invokation + " for the " + person +" because he is a " +classification+" action's " +actionsWhat + ".";
		Entity entity = translator.translate(sentence);
		//entity.getBundle(), entity.getThreadWith("action"), 
		if(printAll) Mark.say("Resulting sentence is ", entity.toEnglish());
		if(printAll) Mark.say(person, entity);
		
		return entity;
		
	}
}



package subsystems.rashi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import translator.Translator;
import utils.Mark;

/** 
 * Class dedicated to all methods used for anlalyzing an author's use of passive voice.
 * @author Suri_Bandler
 *
 */
public class PassiveMechanism {

	
	/**
	 * Generate conclusions regarding passive observations
	 * @param seq
	 * @param passiveConclusons
	 * @param authorStory
	 * @param author
	 * @param printAll
	 */
	public static void getPassiveConclusions(Entity seq, Entity subject, Entity object, Map<Entity, Entity> passiveConclusons, 
			ArrayList<Entity> authorStory, Map<Entity, List<Entity>> authorStoryLineToSources, Entity author, Boolean printAll){
		String sentence;
		
		sentence = "The author decreases attention on the role of the subject in the sentence because the sentence is passive.";
			
		Translator translator = Translator.getTranslator();
		Entity resultPassiveAnalysis = translator.translate(sentence);
		passiveConclusons.put(seq, resultPassiveAnalysis);
			
			
		Entity desiredObject = subject;
			
		//Entity authorPassiveResults = RoleFrames.makeRoleFrame(author, "uses", "passivity");
		//RoleFrames.addRole(authorPassiveResults, "for", desiredObject);
		
		Entity authorPassiveResults = Translator.getTranslator().translate(author.toEnglish() + " uses passivity for " + desiredObject.toEnglish());
		Entity authorPassiveResultsObject = Translator.getTranslator().translate(author.toEnglish() + " labels " + object.toEnglish() + " as object of passivity ");
		
		if(printAll){
			Mark.say("Currently analyzing the following seq for passivity ", seq.toEnglish());
			Mark.say("RESULTS:");
			Mark.say("Innerese:", authorPassiveResults);
			Mark.say("Innerese:", authorPassiveResultsObject);
			Mark.say("Translation:", Generator.getGenerator().generate(authorPassiveResults));
			Mark.say("Translation:", Generator.getGenerator().generate(authorPassiveResultsObject));
		}
	
		
		RashiHelpers.putInDictKeytoList(authorStoryLineToSources, authorPassiveResults, seq);
		authorStory.add(authorPassiveResults);
		authorStory.add(authorPassiveResultsObject);
			
	}
	
}

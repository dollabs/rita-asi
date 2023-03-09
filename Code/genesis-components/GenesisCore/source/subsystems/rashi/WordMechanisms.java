package subsystems.rashi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Entity.LabelValuePair;
import utils.Mark;

/** 
 * Class dedicated to mechanisms that rely on specific words, such as "allege" and its synonyms.
 * @author Suri_Bandler
 *
 */
public class WordMechanisms {
		
	
	/*
	 * Process the specified "aspect" for example, "juvenile" 
	 * Namely, see which subjects/objects contain the specified underlying "theme word"
	 * 
	 * Params: 
	 * 	subjects: Map subject Entity --> Vector of String attributes (ex: <"minor", "juvenile">)
	 *  objects: same but for object Entity 
	 *  aspect: to be analyzed if contained in entity's attributes
	 *  
	 * Returns: Map: "aspect" --> Map("subject" or "object" --> subject or object Entity)
	 * 
	 */
	public static Map<String, Map<String, List<Entity>>> processAspect(Map<Entity, Vector<String>> subjects, Map<Entity, Vector<String>> objects, String commonWord, ArrayList<String> validProperties ){
		Map<String, Map<String,List<Entity>>> result = new HashMap<String, Map<String,List<Entity>>>();
		result.put(commonWord, new HashMap<String,List<Entity>>());
		
		//By now, subjects and objects should NOT have any null values...
		for(Entity subject: subjects.keySet() ){
			Vector<String> associatedWords = subjects.get(subject);
			Vector<LabelValuePair> propertyList = subject.getPropertyList();
			
			Boolean containsValidProperty = false;
			if(!propertyList.isEmpty()) {
				containsValidProperty = validProperties.contains(propertyList.get(0).getValue());
			}
			
			//Double similarityScore = ConceptNetClient.getSimilarityScore(commonWord, ).getResult();
			//Mark.say("COMPARING", commonWord, "orphan", similarityScore, subject.getName());
			Mark.say("SUBJECT", subject, "Associated words", associatedWords, "common word", commonWord);
			//TODO: Concept Net here?
			if(associatedWords.contains(commonWord) || containsValidProperty){//||  similarityScore > .3){
				result.get(commonWord).putIfAbsent("subject", new ArrayList<Entity>());
				result.get(commonWord).get("subject").add(subject);
			}
				
		}
		for(Entity object: objects.keySet() ){
			Vector<String> associatedWords = objects.get(object);
			Mark.say("OBJECT", object, "Associated words", associatedWords, "common word", commonWord);
			//TODO: Concept Net here?
			//Double similarityScore = ConceptNetClient.getSimilarityScore(commonWord, associatedWords.get(0)).getResult();
			Vector<LabelValuePair> propertyList = object.getPropertyList();
			
			
			Boolean containsValidProperty = false;
			if(!propertyList.isEmpty()) {
				containsValidProperty = validProperties.contains(propertyList.get(0).getValue());
			}
			
			
			if(associatedWords.contains(commonWord) || containsValidProperty){// ||  similarityScore > .3){
				result.get(commonWord).putIfAbsent("object", new ArrayList<Entity>());
				result.get(commonWord).get("object").add(object);
			}
				
		}
		return result;
	}
	
	
}

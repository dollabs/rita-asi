package subsystems.rashi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import frames.entities.Entity;
import generator.Generator;
import translator.Translator;
import utils.Mark;

public class EmphasisMechanism {
	
	
	/**
	 * Returns a dictionary mapping the generated form of an author action to the 
	 * number of times that it appears in the author story. 
	 * 
	 * @param authorStory
	 * @return
	 */
	public static HashMap<String, Integer> countRepeatAuthorActions(ArrayList<Entity> authorStory){
		
		HashMap<String, Integer> statementToCount = new HashMap<String, Integer>();
		
		for(Entity s : authorStory){
			
			String authorStoryPart = Generator.getGenerator().generate(s);
			
			statementToCount.putIfAbsent(authorStoryPart, 0);
			int currentVal = statementToCount.get(authorStoryPart);
			statementToCount.put(authorStoryPart, currentVal + 1);
			
			
		}
		
		return statementToCount;

	}
	
	
	public static ArrayList<Entity> processRepeats(ArrayList<Entity> repeatedEntities, Map<Entity, List<Entity>> authorStoryLineToSources){
		
		ArrayList<Entity> results = new ArrayList<>();
		
		for(Entity e : repeatedEntities) {
			
			String stringForm = Generator.getGenerator().generate(e);
			//Entity object = e.getObject();
			
			int index = stringForm.toLowerCase().indexOf("author");
			int lenOfWordAuthor = "author".length();
			String emphasisAdded = "author repeatedly " + stringForm.substring(index+lenOfWordAuthor, stringForm.length());
			//Entity object2 = e.get(0).getObject();
			//Entity object3 = e.get(0).getSubject();
			//Set<Entity> object4 = e.get(0).getChildren();
			
			//Mark.say(e.getBundle().getThreadContaining("action"));
			
			Entity statementOfRepitition = Translator.getTranslator().translateToEntity(emphasisAdded);
			Mark.say("New emphasis addition", statementOfRepitition.toEnglish());
			results.add(statementOfRepitition);
			
			for(Entity source : authorStoryLineToSources.get(e)) {
				RashiHelpers.putInDictKeytoList(authorStoryLineToSources, statementOfRepitition, source);
			}
				
		}
		
		return results;
		
		
	}
}

package dylanHolmes;

import java.util.Arrays;
import java.util.List;

import dictionary.WordNet;
import frames.entities.Bundle;
import frames.entities.Entity;
import utils.Mark;

/**
 * EntityGister transforms English text directly into Entities, complementing syntax-based methods.
 * 
 * EntityGister provides methods for Entity pattern-matching, and for near-miss pattern consolidation.
 * 
 * @author Dylan Holmes
 *
 */
public class EntityGister {
	
	// state variables
	protected WordNet wordNet;
	
	// constructors
	public EntityGister(){
		wordNet = new WordNet();
	}
	
	public List<Bundle> sentenceToThreads(String sentence){
		// todo: accomodate compound words.
		List<Bundle> bundles = Arrays.asList();
		List<String> words = Arrays.asList(sentence.split("\\s+"));
		for (String word : words) {
			bundles.add(wordNet.lookup(word));
		}
		return bundles;
	}
	
	
	/**
	 * Determine whether the sentence matches the Entity.
	 */
	public Entity match(Entity pattern, List<Bundle> bundles) {
		return new Entity();
	}
	
	// construct an near-miss rule from one positive and one negative example.
	public Entity ruleNearMiss(Entity pattern_1, Entity pattern_2){
		return pattern_1;
	}
	
	
	
	
	// entry point
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		EntityGister g = new EntityGister();
		frames.entities.Thread t;
		String w = "dog";
		Bundle threads = g.wordNet.lookup(w);

		Mark.say(threads);
	}
}
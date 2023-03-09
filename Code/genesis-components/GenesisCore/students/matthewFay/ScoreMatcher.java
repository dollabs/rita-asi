package matthewFay;

import generator.Generator;

import java.util.*;

import matchers.ThreadMatcher;
import matthewFay.Utilities.HashMatrix;
import matthewFay.viewers.AlignmentViewer;
import translator.BasicTranslator;
import utils.*;
import utils.minilisp.LList;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * An attempt to provide a partial matching system
 * with a simple scoring metric potentially based on WordNET threads
 * 
 * The goal is that a match will be given a percent match
 * based on how well the elements in the match coinside.
 * 
 * Since there are many kinds of Things, needs to be done
 * recursively with each type of thing being handled
 * appropriately
 * 
 * Initial plan is for a perfect match to have a score of 1.0
 * The match scores for elements will be multiplied together for all Things
 * This means a complete mismatch on an element will cause a score of 0 to be obtained.
 * 
 * Arithmatic means will be allowed as well for an alternative scoring metric.
 */
public class ScoreMatcher {
	
	//Controls whether to actually do scoring or just to rely on structure
	boolean fast_matching = true;
	
	public ScoreMatcher() {
		fast_matching = AlignmentViewer.simpleScorer.isSelected();
		threadMatcher.two_way_match = true;
		threadMatcher.searchAllThreads(false);
	}
	
	private boolean use_thread_matcher = true;
	private static ThreadMatcher threadMatcher = new ThreadMatcher();
	
	public static boolean useBindingHashes = false;
	//public static HashMap<String, String> bindingHashMap = new HashMap<String, String>();
	public static HashMatrix<String, String, Boolean> bindingHashMatrix = new HashMatrix<String, String, Boolean>();
	public static HashMap<String, HashMap<String, Float>> typeScoreHashMap = new HashMap<String, HashMap<String,Float>>();
	
	// For close consistency with BasicMatcher, thing1 should be the pattern and thing2 should be the datum//
	public float scoreMatch(Entity thing1, Entity thing2, LList<PairOfEntities> bindings) {
		if(thing1.functionP() && thing2.functionP()) {
			//Only checks the elements, needs to check verb etc..
			Entity subject1 = thing1.getSubject();
			Entity subject2 = thing2.getSubject();
			// Check subjects
			float subjectMatch =  scoreMatch(subject1, subject2, bindings);
			
			///////////////////////////////////////
			// TODO: Check other derivative details
			///////////////////////////////////////
			String type1 = thing1.getCachedType();
			String type2 = thing2.getCachedType();
			float derivativeMatch = (type1.equals(type2) ? 1 : 0);
			///////////////////////////////////////
			
			return subjectMatch*derivativeMatch;
			
		} else if(thing1.featureP() && thing2.featureP()) {
			// TODO: Add feature support
			Mark.say("Features not handled yet, assuming non-match!");
			return 0;
		} else if(thing1.relationP() && thing2.relationP()) {
			//Match the subjects
			Entity subject1 = thing1.getSubject();
			Entity subject2 = thing2.getSubject();
			float subjectMatch = scoreMatch(subject1, subject2, bindings);
			//Match the objects
			Entity object1 = thing1.getObject();
			Entity object2 = thing2.getObject();
			float objectMatch = scoreMatch(object1, object2, bindings);
			/////////////////////////////////////////////////////////
			// TODO: Match the rest better using wordnet or something
			/////////////////////////////////////////////////////////
			String type1 = thing1.getCachedType();
			String type2 = thing2.getCachedType();
			float relationMatch = (type1.equals(type2) ? 1 : 0);
			//mpfay: too slow!
//			if(relationMatch == 0) {
//				if(thing1.getPrimedThread().containsAll(thing2.getPrimedThread()) ||
//						thing2.getPrimedThread().containsAll(thing1.getPrimedThread()))
//					relationMatch = 1;
//			}
			/////////////////////////////////////////////////////////
			
			return subjectMatch*objectMatch*relationMatch;
		} else if(thing1.sequenceP() && thing2.sequenceP()) {
			// TODO: Add better sequence support
			int childrenCount1 = thing1.getNumberOfChildren();
			int childrenCount2 = thing2.getNumberOfChildren();
			
			// If children are out of order this will fail to give good results
			int min = (childrenCount1 < childrenCount2 ? childrenCount1 : childrenCount2);
			int max = (childrenCount1 < childrenCount2 ? childrenCount2 : childrenCount1);
			// TODO: determine best way to deal with children scores
			float childrenScoreProduct = 1;
			for(int i=0; i<min; i++) {
				Entity child1 = thing1.getElement(i);
				Entity child2 = thing2.getElement(i);
				float score =scoreMatch(child1, child2, bindings);
				childrenScoreProduct *= score;
			}
			
			// TODO: Match the rest
			String type1 = thing1.getCachedType();
			String type2 = thing2.getCachedType();
			float sequenceMatch = (type1.equals(type2) ? 1 : 0);
			
			return childrenScoreProduct*sequenceMatch;
		} else if(thing1.entityP() && thing2.entityP()) {
			String suffix1 = thing1.getNameSuffix();
			String suffix2 = thing2.getNameSuffix();
			if(useBindingHashes) {
				if(!bindingHashMatrix.contains(suffix1, suffix2)) {
					if(bindingHashMatrix.keySetRows().contains(suffix1) || bindingHashMatrix.keySetCols().contains(suffix2))
						return 0;
					else
						return 1;
				} else {
					return 1;
				}
			}
			else {
				boolean bad_found = false;
				for(PairOfEntities binding : bindings) {
					if(binding.getPattern().getNameSuffix().equals(thing1.getNameSuffix())) {
						if(!binding.getDatum().getNameSuffix().equals(thing2.getNameSuffix()))
							bad_found = true;
						else
							if(fast_matching)
								return 1;
							else
								bad_found = false;
								break;
					} else {
						if(binding.getDatum().getNameSuffix().equals(thing2.getNameSuffix()))
							bad_found = true;
					}
				}
				if(bad_found)
					return 0;
			}
			if(use_thread_matcher) {
				if(threadMatcher.match(thing1, thing2).match) {
					return 1;
				} else {
					return 0;
				}
			}
			if (fast_matching) {
				return 1;
			}
			
			float typeScore = 0;
			if(typeScoreHashMap.containsKey(suffix1)) {
				if(typeScoreHashMap.get(suffix1).containsKey(suffix2)) {
					typeScore = typeScoreHashMap.get(suffix1).get(suffix2);
					return typeScore;
				}
					
			}
			// TODO: Eventually should compare whole bundle?
			Vector<String> types1 = thing1.getTypes();
			Vector<String> types2 = thing2.getTypes();
			int length1 = types1.size();
			int length2 = types2.size();
			int length = Math.min(length1, length2);
			int matches = 0;
			
			
			//This is too slow better way!
			int type_iterator = 0;
			while(type_iterator < length) {
				String type1 = types1.get(type_iterator);
				String type2 = types2.get(type_iterator);
				if(type1.equals(Markers.NAME) && type2.equals(Markers.NAME)) {
					matches++;
				}
				if(type1.equals(type2)) {
					matches++;
				} else {
					break;
				}
				type_iterator++;
			}
			
			if(typeScore == 0) {
				if(matches >= Math.max(length1, length2)) {
					typeScore = 1;
				} else {
					//typeScore = (float) ((1.0-Math.pow(alpha, (-1)*matches)));
					typeScore = (float)matches/(float)Math.max(length1, length2);
				}
			}
			if(typeScoreHashMap.containsKey(suffix1)) {
				typeScoreHashMap.get(suffix1).put(suffix2, typeScore);
			} else {
				typeScoreHashMap.put(suffix1, new HashMap<String, Float>());
				typeScoreHashMap.get(suffix1).put(suffix2, typeScore);
			}
			return typeScore;
		}
		if(!thing1.entityP() && !thing1.sequenceP() && !thing1.functionP() && !thing1.featureP() && !thing1.relationP()) {
			Mark.say("Non match, or non-detected Thing Type");
			Mark.say("Thing1: ", thing1.asString());			
		}
		if(!thing2.entityP() && !thing2.sequenceP() && !thing2.functionP() && !thing2.featureP() && !thing2.relationP()) {
			Mark.say("Non match, or non-detected Thing Type");
			Mark.say("Thing2: ", thing2.asString());			
		}
		return 0;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		
		generator.setStoryMode();
		
		Entity s2_A= basicTranslator.translate("Matt is a person").getElement(0);
		Entity s2_B= basicTranslator.translate("Mary is a person").getElement(0);
//		Thing s2_C= translator.translate("The bowl is an object").getElement(0);
		Entity s2_D = basicTranslator.translate("Matt owns the bowl").getElement(0);
		Entity s2_E = basicTranslator.translate("Matt gives the bowl to Mary").getElement(0);
		Entity s2_F = basicTranslator.translate("Mary owns the bowl").getElement(0);
		Entity story_s2 = new Sequence();
		story_s2.addElement(s2_A);
		story_s2.addElement(s2_B);
		//story_s2.addElement(s2_C);
		story_s2.addElement(s2_D);
		story_s2.addElement(s2_E);
		story_s2.addElement(s2_F);
		
		generator.flush();
		
		Entity take_A= basicTranslator.translate("Mark is a person").getElement(0);
		Entity take_B= basicTranslator.translate("Sally is a person").getElement(0);
//		Thing take_C= translator.translate("The cup is an object").getElement(0);
		Entity take_D = basicTranslator.translate("Mark owns the cup").getElement(0);
		Entity take_E = basicTranslator.translate("Sally takes the cup from Mark").getElement(0);
		Entity take_F = basicTranslator.translate("Sally owns the cup").getElement(0);
		Entity story_take = new Sequence();
		story_take.addElement(take_A);
		story_take.addElement(take_B);
//		story_take.addElement(take_C);
		story_take.addElement(take_D);
		story_take.addElement(take_E);
		story_take.addElement(take_F);
		
		generator.flush();
		
		Entity matt = s2_D.getSubject();
		Entity mark = take_D.getSubject();
		
		Entity cup = take_D.getObject().getElement(0);
		
		Mark.say(take_E.asString());
		
		
		ScoreMatcher matcher = new ScoreMatcher();
		//Mark.say("Match Score: ", matcher.scoreMatch(matt, mark, null));
		
		Entity cat = basicTranslator.translate("Mark owns the cat").getElement(0).getObject().getElement(0);
		Entity lion = basicTranslator.translate("Mark owns the lion").getElement(0).getObject().getElement(0);
		Entity dog = basicTranslator.translate("Mark owns the dog").getElement(0).getObject().getElement(0);
		Entity wolf = basicTranslator.translate("Mark owns the wolf").getElement(0).getObject().getElement(0);
		Entity person = basicTranslator.translate("Mark owns the person").getElement(0).getObject().getElement(0);
		Entity bear = basicTranslator.translate("Mark owns the bear").getElement(0).getObject().getElement(0);
		Entity airplane = basicTranslator.translate("Mark owns the airplane").getElement(0).getObject().getElement(0);
		Entity car = basicTranslator.translate("Mark owns the car").getElement(0).getObject().getElement(0);
		Entity bus = basicTranslator.translate("Mark owns the bus").getElement(0).getObject().getElement(0);
		Entity truck = basicTranslator.translate("Mark owns the truck").getElement(0).getObject().getElement(0);
		
		List<Entity> things = new ArrayList<Entity>();
		things.add(cat);
		things.add(lion);
		things.add(dog);
		things.add(wolf);
		things.add(person);
		things.add(bear);
		things.add(airplane);
		things.add(car);
		things.add(bus);
		things.add(truck);
		
		Mark.say(cat.asString()+" and "+lion.asString()+" , Match Score: ", matcher.scoreMatch(cat, lion, null));
		Mark.say(cat.asString()+" and "+dog.asString()+" , Match Score: ", matcher.scoreMatch(cat, dog, null));
		Mark.say(dog.asString()+" and "+wolf.asString()+" , Match Score: ", matcher.scoreMatch(dog, wolf, null));
		
		float score[][] = new float[things.size()][things.size()];
		
		int i = 0;
		int j = 0;
		for(i=0;i<things.size();i++) {
			for(j=0;j<things.size();j++) {
				score[i][j] = matcher.scoreMatch(things.get(i), things.get(j), null);
			}
		}
		
		for(i=0;i<things.size();i++) {
			Entity t1 = things.get(i);
			Mark.say(t1.asString()+":");
			for(j=0;j<things.size();j++) {
				Entity t2 = things.get(j);
				Mark.say("..."+t2.asString()+": "+score[i][j]);
			}
		}
				
		Mark.say(cat.toString());
		Mark.say(lion.toString());
		Mark.say(person.toString());
		Mark.say(car.toString());
		//Mark.say("Match Score: ", matcher.scoreMatch(s2_E, take_E, null));
		//Mark.say("Match Score: ", matcher.scoreMatch(story_s2, story_take, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

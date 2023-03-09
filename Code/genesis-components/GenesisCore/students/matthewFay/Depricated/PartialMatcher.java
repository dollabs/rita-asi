package matthewFay.Depricated;

import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import translator.BasicTranslator;
import utils.*;
import utils.minilisp.LList;

/*
 * An attempt to provide a partial matching system
 * with a simple scoring metric
 * 
 * The goal is that a match will be given a percent match
 * based on how well the elements in the match coinside.
 * 
 * Since there are many kinds of Things, needs to be done
 * recursively with each type of thing being handled
 * appropriately
 */
@Deprecated
public class PartialMatcher {

	public static float match(Entity thing1, Entity thing2, LList<PairOfEntities> bindings) {
		if(bindings == null)
			bindings = new LList<PairOfEntities>();
		if(thing1.functionP() && thing2.functionP()) {
			//Only checks the elements, needs to check verb etc..
			Entity subject1 = thing1.getSubject();
			Entity subject2 = thing2.getSubject();
			// Check subjects
			float subjectMatch =  match(subject1, subject2, bindings);
			// TODO: Check other derivative details
			float derivativeMatch = 1;
			//Come up with a better combining metric than arithmatic mean?
			return (subjectMatch+derivativeMatch)/2;
		} else if(thing1.featureP() && thing2.featureP()) {
			// TODO: Add feature support
			Mark.say("Features not handled yet...");
			return 1;
		} else if(thing1.relationP() && thing2.relationP()) {
			//Match the subjects
			Entity subject1 = thing1.getSubject();
			Entity subject2 = thing2.getSubject();
			float subjectMatch = match(subject1, subject2, bindings);
			//Match the objects
			Entity object1 = thing1.getObject();
			Entity object2 = thing2.getObject();
			float objectMatch = match(object1, object2, bindings);
			// TODO: Match the rest better using wordnet or something
			float relationMatch = (thing1.getType() == thing2.getType() ? 1 : 0);
			
			return (subjectMatch+objectMatch+relationMatch)/3;
		} else if(thing1.sequenceP() && thing2.sequenceP()) {
			// TODO: Add better sequence support
			int childrenCount1 = thing1.getNumberOfChildren();
			int childrenCount2 = thing2.getNumberOfChildren();
			
			// If children are out of order this will fail to give good results
			int min = (childrenCount1 < childrenCount2 ? childrenCount1 : childrenCount2);
			int max = (childrenCount1 < childrenCount2 ? childrenCount2 : childrenCount1);
			// TODO: determine best way to deal with children scores
			float childrenScoreSum = 0;
			for(int i=0; i<min; i++) {
				Entity child1 = thing1.getElement(i);
				Entity child2 = thing2.getElement(i);
				childrenScoreSum += match(child1, child2, bindings);
			}
			
			// TODO: Match the rest
			float sequenceMatch = 1;
			
			return (childrenScoreSum+sequenceMatch)/(1+max);
		} else if(thing1.entityP() && thing2.entityP()) {
			if (OneToOneMatcher.getOneToOneMatcher().match(thing1, thing2, bindings) != null)
				return 1;

			return 0;
		}
		Mark.say("Non match, or non-detected Thing Type");
		Mark.say("Thing1: ", thing1.asString());
		Mark.say("Thing2: ", thing2.asString());
		return 0;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		Generator generator = Generator.getGenerator();
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		
		generator.setStoryMode();
		
		Entity s2_A= basicTranslator.translate("Matt is a person").getElement(0);
		Entity s2_B= basicTranslator.translate("Mary is a person").getElement(0);
		Entity s2_C= basicTranslator.translate("The bowl is an object").getElement(0);
		Entity s2_D = basicTranslator.translate("Matt owns the bowl").getElement(0);
		Entity s2_E = basicTranslator.translate("Matt gives the bowl to Mary").getElement(0);
		Entity s2_F = basicTranslator.translate("Mary owns the bowl").getElement(0);
		Entity story_s2 = new Sequence();
		story_s2.addElement(s2_A);
		story_s2.addElement(s2_B);
		story_s2.addElement(s2_C);
		story_s2.addElement(s2_D);
		story_s2.addElement(s2_E);
		story_s2.addElement(s2_F);
		
		generator.flush();
		
		Entity take_A= basicTranslator.translate("Mark is a person").getElement(0);
		Entity take_B= basicTranslator.translate("Sally is a person").getElement(0);
		Entity take_C= basicTranslator.translate("The cup is an object").getElement(0);
		Entity take_D = basicTranslator.translate("Mark owns the cup").getElement(0);
		Entity take_E = basicTranslator.translate("Sally takes the cup from Mark").getElement(0);
		Entity take_F = basicTranslator.translate("Sally owns the cup").getElement(0);
		Entity story_take = new Sequence();
		story_take.addElement(take_A);
		story_take.addElement(take_B);
		story_take.addElement(take_C);
		story_take.addElement(take_D);
		story_take.addElement(take_E);
		story_take.addElement(take_F);
		
		generator.flush();
		
		Entity matt = s2_D.getSubject();
		Entity mark = take_D.getSubject();
		Entity cup = take_D.getObject();
		Entity bowl = s2_D.getObject();
		
		
		Mark.say(take_E.asString());
		
		Mark.say(cup.asString(), bowl.asString());
		
		PartialMatcher matcher = new PartialMatcher();
		Mark.say("Partial Match Score: ", PartialMatcher.match(matt, mark, null));
		Mark.say("Partial Match Score: ", PartialMatcher.match(cup, bowl, null));
		Mark.say("Partial Match Score: ", PartialMatcher.match(s2_E, take_E, null));
		Mark.say("Partial Match Score: ", PartialMatcher.match(story_s2, story_take, null));
	}
}

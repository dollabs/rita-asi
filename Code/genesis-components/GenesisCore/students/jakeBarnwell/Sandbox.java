package jakeBarnwell;

import frames.entities.Entity;
import generator.RoleFrames;
import matchers.StandardMatcher;
import translator.BasicTranslator;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class Sandbox {
	
	public static frames.entities.Thread getThread(String word) {
		return RoleFrames.makeEntity(word).getBundle().getPrimedThread();
	}
	
	public static void main(String[] args) {
		// Make sure people are persons; otherwise, John might be a toilet, etc.
		BasicTranslator.getTranslator().translateToEntity("Sue is a person");
		BasicTranslator.getTranslator().translateToEntity("Mary is a person");
//		BasicTranslator.getTranslator().translateToEntity("Macbeth is a person");
//		BasicTranslator.getTranslator().translateToEntity("Duncan is a person");

		// To convert sentence into innerese
		Entity translation = BasicTranslator.getTranslator().translateToEntity("Mary's harming Sue leads to Sue's harming Mary and Sue's killing Mary.");
		Mark.say("Translation:", translation);
		translation = BasicTranslator.getTranslator().translateToEntity("The dog may eat the food.");
		Mark.say("Translation:", translation);
		
//		Entity q1 = Translator.getTranslator().translateToEntity("Mary harms Sue.");
//		Entity q2 = Translator.getTranslator().translateToEntity("Mary hurts Sue.");
//		LList<PairOfEntities> match_result = StandardMatcher.getBasicMatcher().match(q1, q2);
//		Mark.say("Result of match with arguments q1 and q2", match_result);
		
	}
}

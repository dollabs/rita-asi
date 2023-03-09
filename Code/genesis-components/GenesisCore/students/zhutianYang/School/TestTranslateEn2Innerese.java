package zhutianYang.School;

import frames.entities.Entity;
import translator.BasicTranslator;
import translator.Translator;
import utils.Mark;

public class TestTranslateEn2Innerese {

	public static void main(String[] args) {
		
		// Make sure people are persons; otherwise, John might be a toilet, etc.
				BasicTranslator.getTranslator().translateToEntity("John is a person");
				BasicTranslator.getTranslator().translateToEntity("Mary is a person");
				BasicTranslator.getTranslator().translateToEntity("Macbeth is a person");
				BasicTranslator.getTranslator().translateToEntity("Duncan is a person");

				// To convert sentence into innerese
				Entity translation = BasicTranslator.getTranslator().translateToEntity("John loves Mary");
				Mark.say("Translation:", translation);
				translation = BasicTranslator.getTranslator().translateToEntity("John wants to kiss Mary");
				Mark.say("Translation:", translation);
				translation = BasicTranslator.getTranslator().translateToEntity("John loves Mary because Mary is smart");
				Mark.say("Translation:", translation);


		Translator translator = Translator.getTranslator();
		Mark.say(Translator.getTranslator().translate("I believe John is here"));
		Mark.say(translator.translate("My friend Jack is super happy because you know him."));
		Mark.say(translator.translate("Jack is super happy because Jack know Jack."));
		Mark.say(translator.translate("My friend is super happy because you know him."));
		/*
		 * (seq semantic-interpretation 
		 * 		(rel cause 
		 * 			(seq conjuction 
		 * 				(rel know 
		 * 					(ent you-144) 
		 * 					(seq roles 
		 * 						(fun object 
		 * 							(ent friend-119)
		 * 						)
		 * 					)
		 * 				)
		 * 			) 
		 * 			(rel has-mental-state 
		 * 				(ent friend-119) 
		 * 				(seq roles 
		 * 					(fun object 
		 * 						(ent happy-126)
		 * 					)
		 * 				)
		 * 			)
		 * 		)
		 * 		(rel cause 
		 * 			(seq conjuction 
		 * 				(rel know 
		 * 					(ent you-144)
		 *  					(seq roles 
		 *  						(fun object 
		 *  							(ent friend-119)
		 *  						)
		 *  					)
		 *  				)
		 *  			) 
		 *  			(rel property 
		 *  				(ent friend-119) 
		 *  				(seq roles 
		 *  					(fun object 
		 *  						(ent super-136)
		 *  					)
		 *  				)
		 *  			)
		 *  		)
		 *  )
		 */
		
		
	}

}

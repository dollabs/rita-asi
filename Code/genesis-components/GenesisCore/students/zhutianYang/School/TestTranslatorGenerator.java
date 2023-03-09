package zhutianYang.School;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import translator.Translator;
import utils.Mark;

public class TestTranslatorGenerator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		   Translator translator = Translator.getTranslator();
		   Generator generator = Generator.getGenerator();
		   
		   Translator tt = Translator.getTranslator();
		   Entity ee = tt.translateToEntity("John loves Mary");
		   Entity ss = ee.getSubject();
		   Mark.say("Before Imparting:", ss.toXML());

		   tt.translateToEntity("John is a person");
		   ss = ee.getSubject();
		   Mark.say("After Imparting:", ss.toXML());
		   
		   tt.translateToEntity("John is a person");
		   ee = tt.translateToEntity("John loves Mary");
		   ss = ee.getSubject();
		   Mark.say("The order matters:", ss.toXML());
		   
		   tt.translateToEntity("John is a person");
		   ee = tt.internalize("John loves Mary");
		   ss = ee.getSubject();
		   Mark.say("The order matters:", ss.toXML());
		
		   // 2 Translator & Generator
		   // between role frames and Genesis English

		   // 2-1 Translator Example
		   String sentence = "John marries Mary because John loves money";
		   Entity entity = translator.translate(sentence);
		   Mark.say(entity);
		   // the object is not an entity like the subject, but a function
		   
		   /* (seq semantic-interpretation 
		    * 	(rel cause 
		    * 		(seq conjuction 
		    * 			(rel love 
		    * 				(ent john-120) 
		    * 				(seq roles 
		    * 					(fun object 
		    * 						(ent money-130)
		    * 					)
		    * 				)
		    * 			)
		    * 		) 
		    * 		(rel marry 
		    * 			(ent john-120) 
		    * 			(seq roles 
		    * 				(fun object 
		    * 					(ent mary-127)
		    * 				)
		    * 			)
		    * 		)
		    * 	)
		    * ) 
		    */
		   
		   
		   // 2-2 Translator Example
		   String sentence2 = "John killed Peter with a knife";
		   Entity entity2 = translator.translate(sentence2);
		   Mark.say(entity2);
		   
		   /* (seq semantic-interpretation 
		    * 	(rel kill 
		    * 		(ent john-225) 
		    * 		(seq roles 
		    * 			(fun object 
		    * 				(ent peter-244)
		    * 			) 
		    * 			(fun with 
		    * 				(ent knife-249)
		    * 			)
		    * 		)
		    * 	)
		    * )
		    * 
		    */
		   
		   
		   //  2-3 The Reverse = English generator
		   Mark.say(generator.generate(entity.getElements().get(0)));
		   
		   Entity Peter = new Entity("Peter");
		   Entity knife = new Entity("knife");
		   Sequence roles2 = new Sequence("roles");
		   roles2.addElement(new Function("object", Peter));
		   roles2.addElement(new Function("with", knife));
		   
		   Sequence entity3 = new Sequence("semantic-interpretation");
		   entity3.addElement(new Relation("kill", new Entity("John"), roles2));
		   Mark.say(generator.generate(entity3.getElements().get(0)));
		   
		   // 2-4 try the fast way
		   Mark.say(RoleFrames.makeRoleFrame(new Entity("John"), "kill", new Entity("John"), "with", new Entity("John")));
		   // Mark.say(roles2.toXML());
	}

}

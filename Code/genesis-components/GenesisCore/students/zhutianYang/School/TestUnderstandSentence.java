// why the subject in a possessive/ passive voice is the owner?
package zhutianYang.School;

import java.util.List;

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import translator.Translator;
import utils.Mark;

/*
 * Created on Nov 26, 2016
 * @author phw
 */

public class TestUnderstandSentence {

	public static void main(String[] ignore) {
		
		Translator t = Translator.getTranslator();
		Generator g = Generator.getGenerator();

		// #1 --------------- An owner: subject + Property + object
		Entity x = t.translateToEntity("Paul's army fled.");
		Mark.say("\nEntity:", x);
		Mark.say("Entity type:", x.getType());
		// Mark.say("English:", g.generate(x));

		Entity subject = x.getSubject();
		Mark.say("Subject:", subject);
		Mark.say("Owner:", subject.getProperty(Markers.OWNER_MARKER));
		
		
		// #1 --------------- A relationship: subject + object 
		x = t.translateToEntity("George is husband of Mary"); // Mary is the subject here
		Mark.say("\nEntity:", x);
		Mark.say("Entity type:", x.getType());
		Entity s = x.getSubject();
		Entity o = x.getObject();
		Mark.say("Subject and object:", s, o);
		
		
		// #2 --------------- A passive voice: subject + role frame
		x = t.translateToEntity("George is killed by Mary"); // Mary is the subject here
		Mark.say("\nEntity:", x);
		s = x.getSubject();
		o = x.getObject();
		Mark.say("Subject and object:", s, o);
		o = RoleFrames.getObject(x);               //  It is a role frame. Use special getters:
		Mark.say("Subject and object:", s, o);
		
		
		// #3 --------------- A lot of prepositions: subject + role frame
		x = t.translateToEntity("George helped Mary with Peter with insights");
		Mark.say("\nEntity:", x);
		Mark.say("Entity type:", x.getType()); // entity type is help
		
		s = RoleFrames.getSubject(x);
		o = RoleFrames.getObject(x);
		Mark.say("Subject and object:", s, o);
		
		// Here is how to get other roles
		List<Entity> roles = RoleFrames.getRoles("with", x);
		roles.stream().forEachOrdered(role -> Mark.say("Role with \"with\" preposition:", role));
		
		
		// **** Check the kind of entity (1) getType (2) getPrimedThread get the entire primed thread
		Mark.say("Type:", x.getType());
		Mark.say("Primed thread:", x.getPrimedThread());
		// Print everything in the thread:
		x.getPrimedThread().stream().forEachOrdered(c -> Mark.say("It is a:", c));
		
		
		
		// **** Check the kind of sentence as entity
		x = t.translateToEntity("A bouvier is a kind of dog");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		
		x = t.translateToEntity("John is fat");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
	}

}

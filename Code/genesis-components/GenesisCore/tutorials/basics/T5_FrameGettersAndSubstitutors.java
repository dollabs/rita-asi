package basics;

import java.util.List;

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.Substitutor;
import start.Start;
import translator.Translator;
import utils.Mark;

public class T5_FrameGettersAndSubstitutors {
	
	static Translator t = Translator.getTranslator();

	public static void main(String[] args) {
		demoGetters();
//		demoSubstitutors();
	}
	
	public static void demoGetters() {
		
		// Translate a sentence
		Entity x = t.translateToEntity("George helped Mary with Peter with insights");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		
		// Use entity.getObject() to look at the object frame of the entity
		Mark.say("Subject frame:", x.getSubject());
		Mark.say("Object frame:", x.getObject());
		
		// Use RoleFrames.getObject() to get the real object, instead of the whole object frame
		Mark.say("Subject:", RoleFrames.getSubject(x));
		Mark.say("Object:", RoleFrames.getObject(x));
		
		// Use RoleFrames.getRoles("role name", entity) to get the specific field
		List<Entity> roles = RoleFrames.getRoles("with", x);
		roles.stream().forEachOrdered(role -> Mark.say("Role with \"with\" preposition:", role));
		
		// Get the owner of an entity
		x = t.translateToEntity("Paul's army fled.");
		Entity subject = x.getSubject();
		Mark.say("Subject:", subject);
		Mark.say("Owner:", subject.getProperty(Markers.OWNER_MARKER));
		
	}
	
	public static void demoSubstitutors() {
		
		// Cast the types of names
		t.translateToEntity("John is a person");
		t.translateToEntity("Mary is a person");
		t.translateToEntity("Susan is a person");

		Entity john = Start.makeThing("john");
		Entity mary = Start.makeThing("mary");
		Entity susan = Start.makeThing("susan");

		Entity roleFrame = RoleFrames.makeRoleFrame(john, "loves", mary);
		Mark.say("Original role frame:  ", roleFrame);

		Entity newRoleFrame = Substitutor.substitute(susan, mary, roleFrame);
		Mark.say("Result of substitution", newRoleFrame);

		Mark.say("In English:\n", 
				Generator.getGenerator().generate(roleFrame), "\n", 
				Generator.getGenerator().generate(newRoleFrame), "\n");
	}

}

package zhutianYang.School;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import utils.Mark;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class TestGeneratorInnerese2En {

	public static void main(String[] ignore) {
		
		// Make a role frame for demonstration
		Entity entity = RoleFrames.makeRoleFrame("John", "love", "Mary");
		Mark.say(entity);
		String english = Generator.getGenerator().generate(entity);
		Mark.say(english);
		
		// More complicated
		Entity entity2 = RoleFrames.makeRoleFrame("John", "want", entity);
		english = Generator.getGenerator().generate(entity2);
		Mark.say(english);
	}
}

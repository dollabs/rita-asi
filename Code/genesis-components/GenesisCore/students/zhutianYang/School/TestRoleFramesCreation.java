package zhutianYang.School;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import utils.Mark;

/*
 * Created on Mar 5, 2016
 * @author phw
 */

public class TestRoleFramesCreation {

	public static void main(String[] ignore) throws Exception {

		// Method uses word net threads for Peter and Paul.
		Entity e = RoleFrames.makeRoleFrame("Peter", "slept");
		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// With object
		e = RoleFrames.makeRoleFrame("Peter", "stabbed", "Paul");
		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// Alternatively, entities are ok instead of strings
		Entity x = RoleFrames.makeEntity("Mary");
		e = RoleFrames.makeRoleFrame("Peter", "stabbed", x);
		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// Ok to add other roles
		RoleFrames.addRole(e, "with", "knife");
		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

	}

}

package consciousness;


import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import start.Start;
import utils.Mark;

/*
 * Created on Jan 9, 2016
 * @author phw
 */

public class Transform {

	public static Entity transformCauseToPathProblem(Entity input) {
		Entity cause = input.getSubject();
		Entity from = cause.getSubject().get(0);
		Entity to = cause.getObject();
		Entity path = Start.makeThing("path");

		Entity description = RoleFrames.makeRoleFrame("path", "lead");

		RoleFrames.addRole(description, "from", from);
		RoleFrames.addRole(description, "to", to);
		
		// description.setBundle(new Bundle());
		// description.addTypes(Solver.PROBLEM, Solver.QUESTION, Solver.YES_NO, Solver.LEADS);

		// description.setBundle(new Bundle());
		// description.addTypes(Solver.INTENSION, Solver.SEARCH);
		//
		// String f = Generator.stripPeriod(Special.getSpecial().generate(from));
		// String t = Generator.stripPeriod(Special.getSpecial().generate(to));
		//
		// String s = "A path leads from \"" + f + "\" to \"" + t + "\"";
		//
		// Mark.say("!!!! s", s);
		//
		// Entity e = Special.getSpecial().translate(s);
		//
		// e.setBundle(new Bundle());
		// e.addTypes(Solver.INTENSION, Solver.SEARCH);
		//
		// Mark.say("!!!! e", e);
		// Mark.say("!!!! d", description);
		//
		//

		return description;

	}

}

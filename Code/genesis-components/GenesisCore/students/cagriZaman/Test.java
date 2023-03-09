package cagriZaman;

import frames.entities.Entity;
import generator.RoleFrames;
import generator.Rules;
import utils.Mark;

/*
 * Created on Sep 29, 2017
 * @author phw
 */

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] ignore) {
		Entity xx = new Entity("thing");
		Entity yy = new Entity("thing");
		Entity zz = new Entity("thing");

		Entity rf1 = RoleFrames.makeRoleFrame(xx, "appear");
		Entity rf2 = RoleFrames.makeRoleFrame(yy, "appear");

		RoleFrames.addRole(rf1, "above", yy);
		RoleFrames.addRole(rf2, "above", zz);

		Entity rfc = RoleFrames.makeRoleFrame(xx, "appear");
		RoleFrames.addRole(rfc, "above", zz);

		Entity rule = Rules.makePredictionRule(rfc, rf1, rf2);

		Mark.say("Rule", rule.toXML());

	}

}

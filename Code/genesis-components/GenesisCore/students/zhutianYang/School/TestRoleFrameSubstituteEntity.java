// t.translateToEntity("John is a person");

package zhutianYang.School;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.*;
import start.Start;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Mar 15, 2016
 * @author phw
 */

public class TestRoleFrameSubstituteEntity {

	public static void main(String[] ignore) throws Exception {
		Translator t = Translator.getTranslator();

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

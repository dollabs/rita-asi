/* matcher match nouns in two sentences
 * the sentence can either be created by translator or role frame
 * // impart rules for this story
		t.translate("xx is a person");
	// defining a person
		Entity john = t.translateToEntity("John is a person").getObject();
 */

package zhutianYang.School;

import java.util.List;

import frames.entities.Entity;
import generator.RoleFrames;
import matchers.StandardMatcher;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Created on Feb 21, 2016
 * @author phw
 */

public class TestAlignArguments {

	public static void main(String[] ignore) throws Exception {
		Translator t = Translator.getTranslator();

		LList<PairOfEntities> result;
		
		// impart rules for this story
		t.translate("xx is a person");
		t.translate("yy is a country");
		t.translate("I am a person");

		// defining a person
		Entity john = t.translateToEntity("John is a person").getObject();
		Entity mary = t.translateToEntity("Mary is a person").getObject();
		Entity susan = t.translateToEntity("Susan is a person").getObject();

		// construct it with translator
		Entity q1 = Translator.getTranslator().translateToEntity("I believe america is individualistic.");
		Entity q2 = Translator.getTranslator().translateToEntity("xx believes america is individualistic.");
		Entity q3 = Translator.getTranslator().translateToEntity("yy believes america is individualistic.");
		result = StandardMatcher.getBasicMatcher().match(q1, q2);
		Mark.say("Matching arguments q1 and q2", result);
		result = StandardMatcher.getBasicMatcher().match(q1, q3);
		Mark.say("Matching arguments q1 and q3", result); // result is null because yy is a country

		// Now do it with constructed role frames
		Entity rf1 = RoleFrames.makeRoleFrame(john, "loves", mary);
		Entity rf2 = RoleFrames.makeRoleFrame(john, "loves", susan);
		result = StandardMatcher.getBasicMatcher().match(rf1, rf2);
		Mark.say("Matching arguments rf1 and rf2", result);



	}
}

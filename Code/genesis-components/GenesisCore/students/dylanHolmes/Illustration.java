package dylanHolmes;

import frames.entities.Entity;
import matchers.StandardMatcher;
import translator.BasicTranslator;
import utils.Mark;
import utils.minilisp.LList;

/*
 * Created on Mar 7, 2014
 * @author phw
 */

public class Illustration {
	
	public static void main(String[] ignore) throws Exception {

		BasicTranslator.getTranslator().translate("Patrick is an entity");

		BasicTranslator.getTranslator().translate("Macbeth is a person");

		Entity x = BasicTranslator.getTranslator().translate("Macbeth murdered Duncan").getElements().get(0);
		Entity y = BasicTranslator.getTranslator().translate("Patrick murdered Duncan").getElements().get(0);
		Mark.say(x);
		Mark.say(y);
		LList match = StandardMatcher.getBasicMatcher().match(y, x);
		Mark.say(match);

		// match = StandardMatcher.getBasicMatcher().match(y, x);
		// Mark.say(match);

		// Entity subject = y.getSubject();
		// Mark.say(subject.getBundle());
		// Mark.say(subject.getBundle().get(0));

	}

}

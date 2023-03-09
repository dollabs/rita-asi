package consciousness;

import java.util.function.Predicate;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.StandardMatcher;
import translator.BasicTranslator;
import utils.Mark;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class Test extends Condition {

	public Test(String name, Entity entity, Predicate<Entity> function) {
		super(name, entity, function);
	}

	public static void main(String[] ignore) {
		Entity e = BasicTranslator.getTranslator().translateToEntity("Inspect torch assembly.");
		Entity f = BasicTranslator.getTranslator().translateToEntity("Inspect camera assembly.");
		Entity g = BasicTranslator.getTranslator().translateToEntity("vv is a variable.");
		Entity h = BasicTranslator.getTranslator().translateToEntity("Inspect vv.");
		Mark.say("Innerese\n", e, "\n", f);
		Mark.say("English\n", Generator.getGenerator().generate(e), "\n",  Generator.getGenerator().generate(f));
		Mark.say("Match", StandardMatcher.getBasicMatcher().match(e, f));
		Mark.say("Match", StandardMatcher.getBasicMatcher().match(e, h));
		Mark.say("Match", StandardMatcher.getBasicMatcher().match(h, e));
		Mark.say("Match", StandardMatcher.getBasicMatcher().match(f, h));
		Mark.say("Match", StandardMatcher.getBasicMatcher().match(h, f));
	}

}
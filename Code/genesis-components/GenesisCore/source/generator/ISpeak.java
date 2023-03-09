package generator;

import translator.BasicTranslator;
import utils.Mark;

import consciousness.I;
import frames.entities.Entity;
import frames.entities.Relation;
import mentalModels.MentalModel;

/*
 * Created on Mar 14, 2015
 * @author phw
 */

public class ISpeak extends Thoughts {

	private static ISpeak iSpeak;

	public static ISpeak getISpeak() {
		if (iSpeak == null) {
			iSpeak = new ISpeak();
		}
		return iSpeak;
	}

	private static void demonstrateRules() throws Exception {
		Entity reference = BasicTranslator.getTranslator().translate("John loves mary because Mary is nice").getElements().get(0);
		Mark.say("Reference     ", reference);
		Entity consequent = BasicTranslator.getTranslator().translate("John loves Mary").getElements().get(0);
		Entity antecedent = BasicTranslator.getTranslator().translate("Mary is nice").getElements().get(0);
		Entity test = makeCause(consequent, antecedent);
		Mark.say("cause         ", test);
		Relation rule = makeAbductionRule(reference.getObject(), reference.getSubject());
		Mark.say("Abduction ", rule);
	}

	private static void demonstrateRoles() {
		Entity john = makeEntity("John");
		Entity mary = makeEntity("Mary", "person");
		Mark.say("Test:", makeRoleFrame(john, "loves", mary));

	}

	private static void test(Entity e) {
		Generator g = Generator.getGenerator();
		Mark.say("\n>>>  Innerese:   ", e);
		String s = "Could not generate";
		try {
			s = g.generate(e);
		}
		catch (Exception e1) {
		}
		Mark.say("Translation:", s);
	}

	private static void demonstrate() throws Exception {

		Entity peter = makeEntity("Peter");
		Entity paul = makeEntity("Paul");
		Entity mary = makeEntity("Mary");
		Entity susan = makeEntity("Susan");

		Entity antecedent = makeRoleFrame(peter, "love", mary);
		Entity consequent = makeRoleFrame(paul, "love", susan);

		MentalModel me = new MentalModel("ignore").getI();

		// Entity x = t.translate("I think Peter loves Mary because John loves Mary").get(0);

		test(ISpeak.conclude(me, consequent));
		test(ISpeak.conclude(me, consequent, antecedent));

		test(ISpeak.think(me, consequent));
		test(ISpeak.think(me, consequent, antecedent));

		test(ISpeak.believe(me, consequent));
		test(ISpeak.believe(me, consequent, antecedent));

		test(ISpeak.haveTrait(me, "asian"));

		test(ISpeak.negate(ISpeak.believe(me, consequent)));
		test(ISpeak.negate(ISpeak.believe(me, consequent, antecedent)));

		test(ISpeak.askMyself(me, antecedent));

		test(ISpeak.conclude(me, ISpeak.think(me, ISpeak.believe(me, consequent, antecedent))));

	}

	public static void main(String[] ignore) {
		try {
			// demonstrateRules();
			// demonstrateRoles();

			demonstrate();

		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

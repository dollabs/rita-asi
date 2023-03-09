package basics;

import java.util.List;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrames;
import matchers.StandardMatcher;
import matchers.Substitutor;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class T6_FrameMatcher {

	public static void main(String[] args) {
		demoMatchFrames();
//		demoMatchFrameWithRule();
	}

	static void demoMatchFrames() {
		
		Translator t = Translator.getTranslator();
		LList<PairOfEntities> bindings;
		
		// Example 1: Frames match if the frame types and the entity types are the same
		t.translate("xx is a person");
		t.translate("yy is a country");
		t.translate("I am a person");

		Entity q1 = t.translateToEntity("I believe America is individualistic.");
		Entity q2 = t.translateToEntity("xx believes Franch is individualistic.");
		Entity q3 = t.translateToEntity("yy believes America is individualistic.");

		Mark.say("RF1", q1);
		Mark.say("RF2", q2);
		Mark.say("RF3", q3);
		
		bindings = StandardMatcher.getBasicMatcher().match(q1, q2);
		Mark.say("Result of match with arguments q1 and q2", bindings);

		bindings = StandardMatcher.getBasicMatcher().match(q1, q3);
		Mark.say("Result of match with arguments q1 and q3", bindings);
		Mark.say("===========================================================\n\n");

		
		
		// Example 2: match constructed role frames

		Entity john = t.translateToEntity("John is a person").getObject();
		Entity mary = t.translateToEntity("Mary is a person").getObject();
		Entity susan = t.translateToEntity("Susan is a person").getObject();

		Entity rf1 = RoleFrames.makeRoleFrame(john, "loves", mary);
		Entity rf2 = RoleFrames.makeRoleFrame(john, "loves", susan);

		Mark.say("RF1", rf1);
		Mark.say("RF2", rf2);

		bindings = StandardMatcher.getBasicMatcher().match(rf1, rf2);
		Mark.say("Result of match with arguments rf1 and rf2", bindings);
		Mark.say("===========================================================\n\n");

		
		
		// Example 3: Adjectives doesn't matter in matching

		q1 = Translator.getTranslator().translateToEntity("The big strong seaman replaces a big tall battery");
		q2 = Translator.getTranslator().translateToEntity("The big seaman replaces a big tall battery");
		q3 = Translator.getTranslator().translateToEntity("The small seaman replaces a tall battery");

		bindings = StandardMatcher.getBasicMatcher().match(q1, q1);
		Mark.say("Result of match with arguments q1 q1", bindings);

		bindings = StandardMatcher.getBasicMatcher().match(q1, q2);
		Mark.say("\n>>>  Result of match with arguments q1 q2", bindings);

		bindings = StandardMatcher.getBasicMatcher().match(q1, q3);
		Mark.say("\n>>>  Result of match with arguments q1 q3", bindings);
		Mark.say("===========================================================\n\n");
		

		// Example 4: Nominal nouns matter in matching

		q1 = Translator.getTranslator().translateToEntity("The big strong seaman replaces a big camera battery");
		q2 = Translator.getTranslator().translateToEntity("The big seaman replaces a camera battery");
		q3 = Translator.getTranslator().translateToEntity("The small seaman replaces a car battery");

		bindings = StandardMatcher.getBasicMatcher().match(q1, q2);
		Mark.say("\n>>>  Result of match with arguments q1 q2", bindings);

		bindings = StandardMatcher.getBasicMatcher().match(q1, q3);
		Mark.say("\n>>>  Result of match with arguments q1 q3", bindings);
		Mark.say("===========================================================\n\n");

		
		// Example 5: Excessive roles are allowed in matching
		
		t.translate("John is a person");
		t.translate("Mary is a person");
		t.translate("Susan is a person");
		t.translate("Peter is a person");
		t.translate("Paul is a person");
		t.translate("Patrick is a person");

		Entity rfp = Translator.getTranslator().translateToEntity("John stabbed Mary with Susan.");
		Entity rfd = Translator.getTranslator().translateToEntity("Peter stabbed Patrick with Paul with a knife at night.");

		bindings = StandardMatcher.getBasicMatcher().match(rfp, rfd);
		List<Entity> excess = StandardMatcher.getBasicMatcher().matchAndReportExcess(rfp, rfd);
		Mark.say("\n>>>  Result of match with arguments rfp and rfd", bindings);
		Mark.say("Excessive of match with arguments rfp and rfd", excess);
		Mark.say("===========================================================\n\n");
		

		// Example 6: Matching only works if you match a specific type to a general type, i.e., B is more general than A
		Entity A = t.translateToEntity("Tom runs to a rock");
		Entity B = t.translateToEntity("Tom travels to a rock");

		Mark.say("A:", A.getPrimedThread());
		Mark.say("B:", B.getPrimedThread());
		Mark.say("Bindings:", StandardMatcher.getBasicMatcher().match(A, B));
		Mark.say("Bindings:", StandardMatcher.getBasicMatcher().match(B, A));
		Mark.say("===========================================================\n\n");
		
	}

	static void demoMatchFrameWithRule() {

		Translator t = Translator.getTranslator();
		LList<PairOfEntities> bindings;

		t.translate("Alice is a person");
		t.translate("xx is a person");
		t.translate("kk is an entity");
		t.translate("yy is an entity");

		Entity rule = t.translateToEntity("If xx forgets to take yy out of kk, then xx may be forgetful.");
		Entity antecedent = rule.getSubject().getElements().get(0);
		Entity consequent = rule.getObject();
		Mark.say("Antecedent:", antecedent);

		Entity event = t.translateToEntity("Alice forgets to take phone out of pants.");
		Mark.say("Event:     ", event);

		bindings = StandardMatcher.getBasicMatcher().match(antecedent, event);
		Mark.say("Bindings:  ", bindings);
		
		// Draw inference by replacing the binded entities
		Entity effect = Substitutor.substitute(consequent, bindings);
		Mark.say("Consequent:", Generator.getGenerator().generate(consequent));
		Mark.say("Effect:    ", Generator.getGenerator().generate(effect));
		
	}

}

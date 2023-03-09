package zhutianYang.School;

import java.util.List;

import frames.entities.Entity;
import generator.RoleFrames;
import matchers.*;
import translator.Translator;
import utils.*;
import utils.minilisp.LList;

/*
 * Purpose Created on 21 April, 2016
 * @author phw
 */

public class TestMatchDeduction {

	public static void main(String[] ignore) throws Exception {
		Translator t = Translator.getTranslator();
		// Get everything to have appropriate class
		t.internalize("xx is a person.");
		t.internalize("yy is a person.");
		t.internalize("Peter is a person");
		t.internalize("Paul is a person.");
		
		Entity rule = t.internalize("If xx kills yy, then yy is dead");
		Entity antecedent = rule.getSubject().getElements().get(0);
		Entity consequent = rule.getObject();
		Mark.say("Rule", rule);
		Mark.say("Antecedent", antecedent);
		Mark.say("Consequent", consequent);
		
		Entity event = t.internalize("Peter kills Paul");
		Mark.say("Event", event);
		
		// Match antecedent with event, produce binding list
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(antecedent, event);
		Mark.say("Bindings", bindings);

		// Now, substitute into consequent using bindings
		Entity result = Substitutor.substitute(consequent, bindings);
		Mark.say("Result", result);

	}
}

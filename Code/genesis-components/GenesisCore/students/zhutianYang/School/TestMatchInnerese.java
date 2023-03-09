package zhutianYang.School;

import java.util.List;

import frames.entities.Entity;
import matchers.StandardMatcher;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;

/*
 * Created on Mar 5, 2016
 * @author phw
 */

public class TestMatchInnerese {

	public static void main(String[] ignore) throws Exception {

		Translator t = Translator.getTranslator();
		t.translate("John is a person");
		t.translate("Susan is a person");
		t.translate("Peter is a person");
		t.translate("Paul is a person");

		Entity AA = Translator.getTranslator().translateToEntity("John stabbed Mary with Susan.");
		Entity BB = Translator.getTranslator().translateToEntity("Peter stabbed Patrick with Paul with a knife at night.");

		LList<PairOfEntities> matches = StandardMatcher.getBasicMatcher().match(AA, BB);
		Mark.say("Similarity between AA and BB", matches);
		
		List<Entity> excess = StandardMatcher.getBasicMatcher().matchAndReportExcess(AA, BB);
		Mark.say("Difference between AA and BB", excess);
		
		
		t.translate("John is a person");
		t.translate("Mary is a person");
		t.translate("Susan is a person");
		t.translate("Peter is a person");
		t.translate("Paul is a person");
		t.translate("Patrick is a person");

		Entity rfp = Translator.getTranslator().translateToEntity("John stabbed Mary with Susan.");
		Entity rfd = Translator.getTranslator().translateToEntity("Peter stabbed Patrick with Paul with a knife at night.");

		excess = StandardMatcher.getBasicMatcher().matchAndReportExcess(rfp, rfd);
		Mark.say("Result of matchRoleFrames with arguments rfp and rfd", excess);
		
	}

}

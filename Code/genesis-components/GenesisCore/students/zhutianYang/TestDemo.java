package zhutianYang;

import java.io.IOException;

import frames.entities.Entity;
import generator.Generator;
import matchers.StandardMatcher;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;

public class TestDemo {

	public static void main(String[] args) throws IOException {
		Translator t = Translator.getTranslator();
		Generator g = Generator.getGenerator();
		
//		String english = "move";
//		Entity entity = t.translate(english).getElement(0);
//		Mark.say("     Thread", entity.getPrimedThread());
//		
//		english = "transfer";
//		entity = t.translate(english).getElement(0);
//		Mark.say("     Thread", entity.getPrimedThread());
		
		String string1 = "replace battery of cellphone";
		String string2 = "replace cellphone battery";
		Entity entity1 = t.translate(string1);
		Entity entity2 = t.translate(string2);
		Z.compareTwoEntities(entity1,entity2);
		Z.compareTwoEntities(entity2,entity1);
		
//		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(entity1, entity2);
//		Mark.say(bindings);
//		Mark.say(entity);
//		Z.understand(entity);
//		Z.printInnereseTree(english);
//		Mark.say("end");
	}

}

package basics;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import generator.RoleFrames;
import translator.BasicTranslator;
import translator.Translator;
import utils.Mark;
import utils.Z;

public class T2_TranslateEnglishToInnerese {

	public static void main(String[] args) {
		
//		demoTranslator();
		demoSpecialFrames();
	}
	
	public static void demoTranslator() {

		// To convert sentences into innerese
		List<String> sentences = new ArrayList<>();
		sentences.add("John loves Mary");
		sentences.add("John wants to kiss Mary");
		sentences.add("John thinks that he wants to kiss Mary");
		sentences.add("John loves Mary because Mary is smart");
		sentences.add("John loves Mary because Mary is smart and she is beautiful");
		sentences.add("If John becomes angry at Mary, he will eat chocolates");
		sentences.add("John loves Mary and chocolates");
		sentences.add("John went to see Mary during Christmas at a park near Harvard");
		
		for (String sentence : sentences) {
			Entity translation = BasicTranslator.getTranslator().translate(sentence);
			Mark.say(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\nSentence:", sentence);
			Z.printInnereseTree(translation);
			Mark.say("Simplified print:\n" + Z.getPrettyTree(sentence));
		}
		
		// Just use "Translate," instead of "Internalize"
		//   - translate produces a sequence because result may be two Innerese expressions
		//   - internalize assumes there will be just one Innerese expression
		Mark.say("\n\n===========================================================");
		Mark.say("Translate", Translator.getTranslator().translate("John loves Mary and Mary loves John"));
		Mark.say("Internalize", Translator.getTranslator().internalize("John loves Mary and Mary loves John"));
	}
	
	public static void demoSpecialFrames() {
		
		Translator t = Translator.getTranslator();
		
		// Relation
		Entity x = t.translateToEntity("George is the husband of Mary");
		Mark.say("Entity:", x);
		Mark.say("Entity type:", x.getType());
		
	}

}

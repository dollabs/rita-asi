package zhutianYang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import generator.Generator;
import matchers.StandardMatcher;
import translator.Translator;
import utils.Mark;
import utils.NewTimer;
import utils.PairOfEntities;
import utils.minilisp.LList;

public class TestSTART {
	
	static public NewTimer timer = NewTimer.zTimer;

	public static void main(String[] args) throws IOException {
		Translator t = Translator.getTranslator();
		Generator g = Generator.getGenerator();
		
		List<String> strings = new ArrayList<>();
		strings.add("Jack kicks Mary if I was sad");
		strings.add("She kicks me because she was sad");
		strings.add("Tom said that he would like to join the World Trade Center on Monday");
		strings.add("it's a good idea to tell people the news");
		strings.add("if I have a really long sentence written by my friend, I will not parse it using START in the normal way");
		
		timer.initialize();
		
		for (String string: strings) {
			Mark.say(string);
			Entity entity1 = t.translate(string);
			Mark.say(g.generate(entity1));
//			Mark.say(entity1.toString());
			timer.lapTime(true, "parsed");
		}
		
	}

}

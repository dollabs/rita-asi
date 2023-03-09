package storyProcessor;

import java.util.*;

import utils.*;
import expert.SimpleGenerator;
import frames.entities.Entity;

public class ConceptTranslator {

	/**
	 * Translate concept into English. Drip pan for redundant elements.
	 * 
	 * @param things
	 * @return TODO: Make this more fluent English. 6.863 project.
	 */
	public static String translateConcept(ConceptDescription completion) {
		String answer = "";
		List<Entity> done = new ArrayList<Entity>();
		for (Entity t : completion.getStoryElementsInvolved().getElements()) {
			if (done.contains(t)) {
				continue;
			}
			String s = SimpleGenerator.generate(t);
			// Mark.say(Config.DEBUG, "Translations: " + s);
			answer = answer + Punctuator.addPeriod(s);
			done.add(t);
		}
		int size = answer.length();
		if (size > 0) {
			return Html.h1("Found instance of " + Punctuator.conditionName(completion.getName())) + Html.normal(answer.substring(0, size - 2));
		}
		return answer;

	}

}

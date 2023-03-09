// personality model has empty story body
// count how many rules and concepts

package zhutianYang.School;

import mentalModels.MentalModel;
import utils.Mark;

/*
 * Created on Dec 29, 2017
 * @author phw
 */

public class TestMentalModel {

	public static void main(String[] ignore) throws Exception {
		// The true argument engages debugging statements
		// Constructor reads file defining the rules and concepts associated with the mental model
		/*
		 * // Moved from Western Commonsense1

			// self-serving bias by adding clause xx is American. infer insanity only when culture is American.
			// If xx isn't American and xx kills yy then xx must be insane.
			
			// accommodate situation for westerners. self-serving bias.
			If xx is American and xx becomes angry, then xx becomes violent. 
			If xx becomes violent, then xx may kill yy.
			If yy kills ww then yy must not be sane.
			
			Start story titled "Western person's characteristics".
			
			The end.
		 */
		MentalModel mm = new MentalModel("George", "western.txt", true);
		Mark.say("Done!\n\n\n\n");
		
		// report summary of mental model
		Mark.say("The number of rules in the model is", mm.getStoryProcessor().getRuleMemory().getRuleList().size());
		Mark.say("The number of concepts in the model is", mm.getStoryProcessor().getConcepts().size());
		
		mm = new MentalModel("Amanda", "eastern.txt", true);
		Mark.say("The number of rules in the model is", mm.getStoryProcessor().getRuleMemory().getRuleList().size());
		Mark.say("The number of concepts in the model is", mm.getStoryProcessor().getConcepts().size());
	}
}

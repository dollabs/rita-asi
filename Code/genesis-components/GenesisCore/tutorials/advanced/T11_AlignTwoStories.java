package advanced;

import frames.entities.Sequence;
import matthewFay.Demo;
import matthewFay.StoryAlignment.*;
import matthewFay.Utilities.Pair;
import utils.minilisp.LList;
import utils.*;

public class T11_AlignTwoStories {
	
	// See Matthew Fay's 2012 thesis: https://groups.csail.mit.edu/genesis/papers/Fay%202012.pdf
	
	public static void main(String[] args) {
		
		// Create demo stories.
		
		Sequence seqA = Demo.ApproachStory();
		/* Start Story titled "Approach Story". 
		 * Paul is a person. Jill is a person. 
		 * Paul is far from Jill. Paul approaches Jill. Paul is near Jill.
		 */

		Sequence seqB = Demo.CarryStory();
		/* Start Story titled "Carry Story". 
		 * Matt is a person. Mary is a person. 
		 * The ball is an object. Matt controls the ball. 
		 * Matt carries the ball. Matt approaches Mary. Matt is near Mary. 
		 * The ball is near Mary.
		 */

		Aligner aligner = new Aligner();
		SortableAlignmentList sal = aligner.align(seqA, seqB);

		// Shows which story entries match.
		SequenceAlignment bestAlignment = (SequenceAlignment) sal.get(0);
		LList<PairOfEntities> bestBindings = bestAlignment.bindings;
		Mark.say("\nBest entity bindings");
		for (PairOfEntities p : bestBindings) {
			Mark.say("Entity binding:", p);
		}

		// Shows which story elements match.
		Mark.say("\nBest element bindings");
		bestAlignment.stream().forEachOrdered(e -> Mark.say("Element binding:\n", ((Pair) e).a, "\n", ((Pair) e).b));
		Mark.say("\n>>> Not good match", sal.size(), ":");
		sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

		sal = aligner.align(seqB, seqA);
		Mark.say("\n>>> Reverse,", sal.size(), ":");
		sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

		sal = aligner.align(seqA, seqA);
		Mark.say("\n>>> Self alignment A,", sal.size(), ":");
		sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));

		Mark.say("\n>>> Self alignment B,", sal.size(), ":");
		sal = aligner.align(seqB, seqB);
		sal.stream().forEachOrdered(a -> Mark.say("Score:", a.score));
	}

}

package rules;

import java.util.List;

import constants.Radio;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Relation;
import utils.Mark;

/*
 * Created on Aug 13, 2015
 * @author phw
 */

public class DefaultExplanationTestor implements ExplanationContinuationInterface {

	public DefaultExplanationTestor() {
		// TODO Auto-generated constructor stub
	}

	/***
	 * Indicates true if 
	 */
	@Override
	public boolean test(Entity elementToBeExplained, Relation rule, List<Relation> previousExplanations) {
		// Mark.say("Testing\n", elementToBeExplained, "\n>>> ");
		
		/***
		 * @Priya is currently searching for the best place to put this code.
		 */
		if (!Radio.sch_offButton.isSelected()) {
			Switch.useOnlyOneExplanation.setSelected(true);
		}
			
		return !previousExplanations.isEmpty() && Switch.useOnlyOneExplanation.isSelected();
	}

}

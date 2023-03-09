package rules;

import java.util.*;

import constants.Markers;
import constants.Radio;
import constants.Switch;
import frames.entities.Relation;
import utils.Mark;

/*
 * Created on Aug 3, 2015
 * @author phw
 */

public class RuleSorter implements RuleSorterInterface {

	@Override
	public List<Memory> sort(List<Memory> input) {
		
		if (Radio.sch_nonSchizophrenicButton.isSelected()) {
			//Explanation rules before presumption rules
			Collections.sort(input, Memory.NonSchizophrenicComparator);
		} 
		else if (Radio.sch_hyperpresumptionButton.isSelected()) {
			Collections.sort(input, Memory.HyperpresumptionComparator);
		} 
		else if (Radio.sch_failedInferringWantButton.isSelected()) {
			
			//Remove any rules that infer 'want', 'believe', or 'feel'
			List<Memory> toRemove = new ArrayList<Memory>();
			for (int i=0; i<input.size(); i++) { //Find what to remove
				Memory memory = input.get(i);
				if (memory.rule.asString().contains("want") || memory.rule.asString().contains("believe")|| memory.rule.asString().contains("feel")) {
					toRemove.add(memory);
				}
			}
			input.removeAll(toRemove); //remove
			
			//Explanation rules before presumption rules
			Collections.sort(input, Memory.NonSchizophrenicComparator);
		}

		
		/***
		 *  This Schizo mechanism ("extreme failed source monitoring") was never implemented.
			If it is implemented here, this can be uncommented.
		 */
//		else if (Radio.sch_extremeFailedSourceMonitoringButton.isSelected()) {
//			//int position = ???
//			//String type = Markers.ABDUCTION_RULE;
//			//Entity subject = 
//			//Entity object = 
//			//Relation rule = new Relation(subject, object);
//			//Memory extreme_failed_source_monitoring_memory = new Memory(position, type, rule);
//			//input.add(extreme_failed_source_monitoring_memory);
//			//Explanation rules before presumption rules
//			Collections.sort(input, Memory.NonSchizophrenicComparator);
//		}
		
		else {
			//Default: Do no reordering
		}
		
		
		return input;

	}

}
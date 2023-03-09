package rules;

import java.util.*;

/*
 * Created on oct 10, 2015
 * This class is not currently used by Genesis. Instead of using multiple sorters, Genesis schizophrenic mode uses multiple comparators.
 * @author priyak
 */

public class SchizophrenicRuleSorter implements RuleSorterInterface {

	public SchizophrenicRuleSorter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Memory> sort(List<Memory> input) {
		return input;
	}

}
//
//
//public List<Memory> sort(List<Memory> input) {
//	
//	if (Switch.nonSchizophrenicGenesis.isSelected()) {
//		
//		Collections.sort(input, new ExplanationsFirstComparator());
//	}
//	else {
//		return input;
//	}
//}
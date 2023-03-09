package rules;

import java.util.*;

/*
 * Created on oct 10, 2015
 * This class is not currently used by Genesis. Instead of using multiple sorters, Genesis schizophrenic mode uses multiple comparators.
 * @author priyak
 */

public class NonSchizophrenicRuleSorter implements RuleSorterInterface {

	public NonSchizophrenicRuleSorter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Memory> sort(List<Memory> input) {
		return input;
	}

}
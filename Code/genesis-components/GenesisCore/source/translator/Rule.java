package translator;

import java.util.*;



/*
 * Mostly replace when moved to new non-matching parser
 */

public class Rule {

	private String name;

	public Rule() {
	}

	public Rule(String name) {
		this.name = name;
	}
	
	public Rule(BasicRule basicRule) {
		this.addRunnable(basicRule);
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}


	BasicRule basicRule;

	public void addRunnable(BasicRule rule) {
		this.name = rule.getClass().getSimpleName();
		this.basicRule = rule;
	}

	public BasicRule getRunnable() {
		return basicRule;
	}

}

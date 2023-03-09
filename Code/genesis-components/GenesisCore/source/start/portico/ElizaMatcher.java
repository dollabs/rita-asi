package start.portico;

import java.util.*;
import java.util.regex.*;

import utils.Mark;

/*
 * Created on Feb 24, 2008 @author phw
 */

public class ElizaMatcher {

	private HashMap<String, String> bindings;

	private static ElizaMatcher elizaMatcher;

	public static ElizaMatcher getElizaMatcher() {
		if (elizaMatcher == null) {
			elizaMatcher = new ElizaMatcher();
		}
		return elizaMatcher;
	}

	private ElizaMatcher() {
	}

	public boolean match(String p, String d) {
		return match(Listifier.listify(p), Listifier.listify(d));
	}

	public boolean match(List<String> p, List<String> d) {
		getBindings().clear();
		return performMatch(p, d);
	}

	public HashMap<String, String> getBindings() {
		if (bindings == null) {
			bindings = new HashMap<String, String>();
		}
		return bindings;
	}

	public boolean findMatch(List<String> pattern, List<String> datum) {
		getBindings().clear();
		boolean result = performMatch(pattern, datum);
		// System.out.println("Bindings: " + getBindings());
		return result;
	}

	private void addBinding(String variable, String value) {
		Object result = getBindings().get(variable);
		String newResult;
		if (result == null) {
			newResult = value;
		}
		else {
			newResult = result + " " + value;
		}
		// System.out.println("New binding is " + newResult);
		getBindings().put(variable, newResult);
	}

	private void clearBinding(String variable) {
		getBindings().remove(variable);
	}

	public boolean performMatch(List<String> pattern, List<String> datum) {
		// Mark.say("Matching " + pattern + " | " + datum);
		if (pattern.isEmpty() && datum.isEmpty()) {
			return true;
		}
		else if (pattern.isEmpty()) {
			return false;
		}
		String p = pattern.get(0);

		String nextP = null;

		if (pattern.size() > 1) {
			nextP = pattern.get(1);
		}

		char pChar = p.charAt(0);

		if (datum.isEmpty()) {
			if (pattern.size() == 1 && (p.equals("*") || pChar == '*')) {
				return true;
			}
			return false;
		}
		String d = datum.get(0);
		// char dChar = d.charAt(0);
		// try {
		// Script.anInteger = Integer.valueOf(d);
		// return true;
		// }
		// catch (NumberFormatException e) {
		// }
		// try {
		// Script.aDouble = Double.valueOf(d);
		// return true;
		// }
		// catch (NumberFormatException e) {
		// }
		if (nextP != null && nextP.equalsIgnoreCase(d)) {
			List<String> restPattern = new ArrayList();
			restPattern.addAll(pattern);
			restPattern.remove(0);
			restPattern.remove(0);
			List<String> restDatum = new ArrayList();
			restDatum.addAll(datum);
			restDatum.remove(0);
			return performMatch(restPattern, restDatum);
		}
		else if (p.equals("*")) {
			List<String> newPattern = new ArrayList();
			newPattern.addAll(pattern);
			newPattern.remove(0);
			List<String> newDatum = new ArrayList();
			newDatum.addAll(datum);
			newDatum.remove(0);
			return performMatch(newPattern, datum) || performMatch(pattern, newDatum);
		}
		else if (pChar == '*') {
			List<String> newPattern = new ArrayList();
			newPattern.addAll(pattern);
			newPattern.remove(0);
			List<String> newDatum = new ArrayList();
			newDatum.addAll(datum);
			newDatum.remove(0);
			addBinding(p, d);
			boolean result = performMatch(newPattern, datum) || performMatch(pattern, newDatum);
			if (!result) {
				clearBinding(p);
			}
			return result;
		}
		else if (p.equals("?")) {
			List<String> newPattern = new ArrayList();
			newPattern.addAll(pattern);
			newPattern.remove(0);
			List<String> newDatum = new ArrayList();
			newDatum.addAll(datum);
			newDatum.remove(0);
			return performMatch(newPattern, newDatum);
		}
		else if (pChar == '?') {
			List<String> newPattern = new ArrayList();
			newPattern.addAll(pattern);
			newPattern.remove(0);
			List<String> newDatum = new ArrayList();
			newDatum.addAll(datum);
			newDatum.remove(0);
			addBinding(p, d);
			boolean result = performMatch(newPattern, newDatum);
			if (!result) {
				clearBinding(p);
			}
			return result;
		}
		else if (p.equalsIgnoreCase(d)) {
			List<String> restPattern = new ArrayList();
			restPattern.addAll(pattern);
			restPattern.remove(0);
			List<String> restDatum = new ArrayList();
			restDatum.addAll(datum);
			restDatum.remove(0);
			return performMatch(restPattern, restDatum);
		}
		return false;
	}

	public static void main(String[] ignore) {

		// List<String> pattern = Listifier.listify("I am *y about *x");
		// List<String> sentence = Listifier.listify("I am worried about the debate");
		List<String> pattern = Listifier.listify("Transfer knowledge from ?s to ?t");
		List<String> sentence = Listifier.listify("Transfer knowledge from \"horseshit\" to \"new situation\".");

		pattern = Listifier.listify("Transfer knowledge from *s to *t.");
		sentence = Listifier.listify("Transfer knowledge from l m n to x y z.");

		ElizaMatcher elizaMatcher = new ElizaMatcher();
		System.out.println("Result: " + elizaMatcher.performMatch(pattern, sentence));
		System.out.println("Bindings: " + elizaMatcher.getBindings());

	}

}

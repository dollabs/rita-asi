package utils;

import java.util.*;

import utils.Mark;

/*
 * Created on May 12, 2010
 * @author phw
 */

public class Punctuator {

	public static String conditionName(String name) {
		if (name.trim().isEmpty()) {
			return "";
		}
		StringBuffer buffer = new StringBuffer(name);
		int index;
		while ((index = buffer.indexOf("_")) >= 0) {
			buffer.replace(index, index + 1, " ");
		}
		while ((index = buffer.indexOf("\"")) >= 0) {
			buffer.replace(index, index + 1, "");
		}
		buffer.replace(0, 1, buffer.substring(0, 1).toUpperCase());
		return buffer.toString();
	}

	public static String removeQuotes(String name) {
		if (name.trim().isEmpty()) {
			return "";
		}
		StringBuffer buffer = new StringBuffer(name);
		int index;
		while ((index = buffer.indexOf("\"")) >= 0) {
			buffer.replace(index, index + 1, "");
		}
		return buffer.toString();
	}

	public static String punctuateAnd(List<String> elements) {
		return punctuateSequence(elements, "and") + ".";
	}

	public static String punctuateOr(List<String> elements) {
		return punctuateSequence(elements, "or");
	}

	public static String punctuateSequence(List<String> elements, String insertion) {
		String result = "";
		int size = elements.size();
		if (size == 1) {
			result += elements.get(0);
		}
		else {
			for (int i = 0; i < size; ++i) {
				result += elements.get(i);
				if (i == size - 1) {
				}
				else if (i == size - 2) {
					if (size == 2) {
						result += " " + insertion + " ";
					}
					else {
						result += ", " + insertion + " ";
					}
				}
				else {
					result += ", ";
				}
			}
		}
		return result;
	}

	public static String addPeriod(Object o) {
		String s = o.toString().trim();
		if (s.isEmpty()) {
			return "";
		}
		char last = s.charAt(s.length() - 1);
		if (">".indexOf(last) >= 0) {
			return s;
		}
		else if (":".indexOf(last) >= 0) {
			return s.trim() + " ";
		}
		else if (".?!".indexOf(last) >= 0) {
			return s + "  ";
		}
		return s + ".  ";
	}

	public static String addSpace(Object o) {
		String s = o.toString().trim();
		if (s.isEmpty()) {
			return "";
		}
		char last = s.charAt(s.length() - 1);
		if (">".indexOf(last) >= 0) {
			return s;
		}
		else if (":".indexOf(last) >= 0) {
			return s.trim() + " ";
		}
		else if (".?!".indexOf(last) >= 0) {
			return s + "  ";
		}
		// return s + ".  ";
		return s + "  ";
	}

	public static String removePeriod(String s) {
		s = s.trim();
		char last = s.charAt(s.length() - 1);
		if (".?!".indexOf(last) >= 0) {
			return s.substring(0, s.length() - 1).trim();
		}
		return s;
	}

	public static void main(String[] ignore) {
		Mark.say(conditionName("Tragedy_of_Macbeth's"));
	}

}

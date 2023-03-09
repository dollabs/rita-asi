package utils;

import java.util.*;

import utils.Mark;

/*
 * Created on May 18, 2010
 * @author phw
 */

public class Html {

	public static String biggest = "style=\"font-family: times; font-size: 40px\"";

	public static String bigger = "style=\"font-family: times; font-size: 30px\"";

	public static String normal = "style=\"font-family: times; font-size: 20px\"";

	public static double multiplier = 1.0;

	public static double ratio = 1.2;

	public static double base = 25;

	public static String surround(String tag, String c, String s) {
		return "<" + tag + " " + c + ">" + s + "</" + tag + ">";
	}

	public static String surround(String tag, String s) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

	public static String size5(String s) {
		return "<font size=\"+5\">" + s + "</font>";
	}

	public static String size4(String s) {
		return "<font size=\"+4\">" + s + "</font>";
	}

	public static String size3(String s) {
		return "<font size=\"+3\">" + s + "</font>";
	}

	public static String size2(String s) {
		return "<font size=\"+2\">" + s + "</font>";
	}

	public static String h1(String s) {
		return surround("h1", biggest, s);
	}

	public static String h2(String s) {
		return surround("h2", bigger, s);
	}

	private static String atSize(double size, String s) {
		double pixels = multiplier * base * size;
		// Mark.say("Size is", pixels);
		String result = "<font style=\"font-family: times; font-size: ";
		result += pixels;
		result += "px\"> ";
		result += s;
		result += "</font>";
		return result;
	}

	public static String normal(String s) {
		return atSize(1, s);
	}

	public static String large(String s) {
		return atSize(Math.pow(ratio, 1), s);
	}

	public static String Large(String s) {
		return atSize(Math.pow(ratio, 2), s);
	}

	public static String LARGE(String s) {
		return atSize(Math.pow(ratio, 3), s);
	}

	public static String huge(String s) {
		return atSize(Math.pow(ratio, 4), s);
	}

	public static String Huge(String s) {
		return atSize(Math.pow(ratio, 5), s);
	}

	public static String small(String s) {
		return atSize(Math.pow(ratio, -1), s);
	}

	public static String footnotesize(String s) {
		return atSize(Math.pow(ratio, -2), s);
	}

	public static String scriptsize(String s) {
		return atSize(Math.pow(ratio, -3), s);
	}

	public static String tiny(String s) {
		return atSize(Math.pow(ratio, -4), s);
	}

	public static String p(String s) {
		return "\n<p>" + s;
	}

	public static String br(String s) {
		return "<br/>" + s;
	}

	public static String line(String s) {
		return s + "<br/>";
	}

	public static String bold(String s) {
		return surround("b", s);
	}

	public static String html(String s) {
		return surround("html", s);
	}

	public static String ital(String s) {
		return surround("i", s);
	}

	public static String center(String s) {
		return surround("center", s);
	}

	public static String convertLf(String s) {
		return s.replaceAll("\n", "<br/>");
	}

	public static String remove_(String s) {
		StringBuffer b = new StringBuffer(s);
		while (true) {
			int index = b.indexOf("_");
			if (index >= 0) {
				b.replace(index, index + 1, " ");
				continue;
			}
			break;
		}
		return b.toString();
	}

	public static String strip(String s) {
		StringBuffer b = new StringBuffer(s);
		while (true) {
			int index1 = b.indexOf("<");
			if (index1 >= 0) {
				int index2 = b.indexOf(">", index1);
				if (index2 >= 0) {
					b.replace(index1, index2 + 1, " ");
					continue;
				}
				break;
			}
			break;
		}
		return b.toString();
	}

	public static String coloredText(String color, String s) {
		return "<font color=\"" + color + "\">" + s + "</font>";
	}

	public static String red(String s) {
		return "<font color=\"red\">" + s + "</font>";
	}

	public static String gray(String s) {
		return "<font color=\"gray\">" + s + "</font>";
	}

	public static String green(String s) {
		return "<font color=\"green\">" + s + "</font>";
	}

	public static String blue(String s) {
		return "<font color=\"blue\">" + s + "</font>";
	}

	public static String strike(String s) {
		return "<strike>" + s + "</strike>";
	}

	public static String capitalize(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static String list(String description) {
		return Html.surround("ul", description);
	}

	public static String bullet(String description) {
		return Html.surround("li", description);
	}

	public static String number(int level, String description) {
		return Html.surround("p", level + description);
	}

	public static String twoDigits(String s) {
		if (s.length() == 0) {
			return "00";
		}
		else if (s.length() == 1) {
			return "0" + s;
		}
		return s;
	}

	public static String table(String... strings) {
		return table(Arrays.asList(strings));
	}

	public static String table(List<String> strings) {
		String result = "<table>";
		result += tableHeading(strings);
		result += "</table>";
		return result;
	}

	public static String tableWithPadding(int padding, String... strings) {
		return tableWithPadding(padding, Arrays.asList(strings));
	}

	public static String tableWithPadding(int padding, List<String> strings) {
		String result = "<table cellpadding=\"" + padding + "\">";
		result += tableHeading(strings);
		result += "</table>";
		return result;
	}

	public static String tableHeading(String... strings) {
		return tableHeading(Arrays.asList(strings));
	}

	public static String tableHeading(List<String> strings) {
		String result = "<tr>";
		for (String string : strings) {
			result += "<th>" + string + "</th>";
		}
		result += "</tr>";
		return result;
	}

	public static String tableAddRow(String table, String... elements) {
		return tableAddRow(table, Arrays.asList(elements));
	}

	public static String tableAddRow(String table, List<String> elements) {
		String suffix = "</table>";
		String start = table.trim();
		if (start.endsWith(suffix)) {
			start = start.substring(0, start.length() - suffix.length());
			return start + tableRow(elements) + suffix;
		}
		Mark.err("Adding to string that is not a table in Html.tableAddRow");
		return null;
	}

	public static String tableRow(String... strings) {
		return tableRow(Arrays.asList(strings));
	}

	public static String tableRow(List<String> strings) {
		String result = "<tr>";
		for (String string : strings) {
			result += "<td>" + string + "</td>";
		}
		result += "</tr>";
		return result;
	}

	public static String size(int size, String stuff) {
		return "<font size=" + size + ">" + stuff + "</font>";
	}
}

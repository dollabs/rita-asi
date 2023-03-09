package utils;

/*
 * Created on Dec 29, 2016
 * @author phw
 */

public class Comments {

	public static String dike(String input) {
		StringBuffer b = new StringBuffer(input);
		// First, get rid of /* */, taking care that there may be nesting
		while (b.indexOf("/*") >= 0) {
			int nesting = 0;
			int left = b.indexOf("/*");
			int right = b.indexOf("*/");
			if (right < left) {
				Mark.err("Bug in /* */ nesting!");
			}
			int start = left + 1;
			while (true) {
				// Mark.say("b is", b);
				int nextLeft = b.indexOf("/*", start);
				int nextRight = b.indexOf("*/", start);
				if (nextLeft > 0 && nextLeft < nextRight) {
					// Nested!
					++nesting;
					start = nextLeft + 1;
					continue;
				}
				else if (nesting > 0) {
					--nesting;
					start = nextRight + 1;
					continue;

				}
				if (nesting == 0) {
					b = b.delete(left, nextRight + 2);
					// Mark.say(left, nextRight + 2, b);
					break;
				}

			}
		}
		// Ok, at this point /* */ are gone, so time for //
		while (b.indexOf("//") >= 0) {
			int start = b.indexOf("//");
			int end = b.indexOf("\n", start);
			if (end < 0) {
				end = b.length();
			}
			if (start > 0 && b.charAt(start - 1) == '\n') {
				b = b.delete(start, end + 1);
			}
			else {
				b = b.delete(start, end);
			}
		}

		return b.toString();
	}

	public static void main(String[] ignore) {
		Mark.say("Hello world");
		Mark.say("Result:", Comments.dike("This is a nice /*little*/ \n// screwy\n hansome test."));

		Mark.say("Hello mars");
	}

}

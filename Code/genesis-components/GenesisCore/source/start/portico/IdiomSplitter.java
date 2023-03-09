package start.portico;

import java.util.*;
import java.util.regex.Pattern;

import connections.*;

/*
 * Created on Mar 14, 2009 Dead code
 * @author phw
 */

public class IdiomSplitter extends AbstractWiredBox {

	public final static String LEFT = "left";

	public final static String RIGHT = "right";

	public final static String COMBINATOR = "combinator";

	public final static String NONE = "none";

	public final static String CAUSE = "cause";

	public final static String BEFORE = "before";

	public final static String WHILE = "while";

	public final static String AFTER = "after";

	public IdiomSplitter() {
		super("Idiom splitter");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public synchronized void process(Object s) {
		// If not a string, ignore it
		if (!(s instanceof String)) {
			return;
		}
		System.out.println("Working on " + s);
		analyze(((String) s).trim());
	}

	/*
	 * Look for idioms and places to divide sentence
	 */
	private void analyze(String words) {
		String splitter = " because ";
		int index = words.indexOf(splitter);
		if (index > 0) {
			transmit(words, index, splitter, CAUSE);
			return;
		}
		splitter = " before ";
		index = words.indexOf(splitter);
		if (index > 0) {
			transmit(words, index, splitter, BEFORE);
			return;
		}
		splitter = " while ";
		index = words.indexOf(splitter);
		if (index > 0) {
			transmit(words, index, splitter, WHILE);
			return;
		}
		splitter = " after ";
		index = words.indexOf(splitter);
		if (index > 0) {
			transmit(words, index, splitter, AFTER);
			return;
		}
		Connections.getPorts(this).transmit(LEFT, words);
		Connections.getPorts(this).transmit(COMBINATOR, NONE);
	}

	private void transmit(String words, int index, String splitter, String combinator) {
		Connections.getPorts(this).transmit(LEFT, words.substring(0, index).trim());
		Connections.getPorts(this).transmit(RIGHT, words.substring(index + splitter.length(), words.length()).trim());
		Connections.getPorts(this).transmit(COMBINATOR, combinator);
	}

	/*
	 * Split string of words into words
	 */
	// private List<String> split(String s) {
	// String[] words = Pattern.compile(" ").split(s);
	// List<String> list = Arrays.asList(words);
	// ArrayList<String> result = new ArrayList<String>();
	// for (String word : list) {
	// if (!word.isEmpty()) {
	// result.add(word);
	// }
	// }
	// return result;
	// }
	public static void main(String[] ignore) {
		// System.out.println(new IdiomSplitter().split("This is a  because        test"));
		new IdiomSplitter().process("This is a  because        test");
	}

}

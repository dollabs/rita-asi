package generator;

import java.util.HashMap;

/*
 * Created on Feb 11, 2011
 * @author phw
 */

public class RoleFrameGrandParent {

	public static String ENTITY = "entity";

	protected static int index = 0;

	protected static HashMap<String, String> indexMap = new HashMap<String, String>();

	protected static HashMap<Integer, RoleFrameGrandParent> roleFrameGrandParents = new HashMap<Integer, RoleFrameGrandParent>();

	protected Object head;

	private String translation = "";

	private static HashMap<String, String> markers = new HashMap<String, String>();

	public RoleFrameGrandParent() {
	}

	public RoleFrameGrandParent(String source, RoleFrameGrandParent... entities) {
		// initialize();
		translation = translate(source, entities);
	}

	// private void initialize() {
	// if (markers.size() != 0) {
	// return;
	// }
	//
	// // Basic
	//
	// markers.put(":subject", "subject");
	//
	// // Decorations
	// markers.put(":definite", "definite");
	//
	// markers.put(":the", "definite");
	//
	// markers.put(":indefinite", "indefinite");
	//
	// markers.put(":a", "indefinite");
	//
	// markers.put(":an", "indefinite");
	// }

	private String translate(String source, RoleFrameGrandParent[] entities) {
		String result = "";
		String[] elements = source.split("\\s+");

		// First, go through and replace all # variables with their heads and
		// put their triple forms on the result string
		for (int i = 0; i < elements.length; ++i) {
			String element = elements[i];
			if (element.charAt(0) == '#') {
				index = Integer.parseInt(element.substring(1));
				elements[i] = extractHead(entities[index - 1]);
				result += extractGuts(entities[index - 1]);
			}
			else {
			}
		}
		head = getIndexedWord(elements[elements.length - 1]);
		for (int i = 0; i < elements.length - 1; ++i) {
			String element = elements[i];
			// Now, see if element is a key word
			if (element.charAt(0) == ':') {
				if (element.equals(":definite") || element.equals(":the")) {
					result += makeProperty(head, "has_det", "definite");
				}
				else if (element.equals(":indefinite") || element.equals(":a") || element.equals(":an")) {
					result += makeProperty(head, "has_det", "indefinite");
				}
			}
			else {
				result += makeProperty(head, "has_property", elements[i]);
			}
		}
		return result;
	}

	public String toString() {

		return translation;
	}

	protected String decorate(Object s, Object r, Object o) {
		return "[" + s + " " + r + " " + o + "]";
	}

	protected String addTriple(Object s, Object r, Object o) {
		String result = extractGuts(s);
		result += decorate(s, r, o);
		return result;
	}

	protected String makeProperty(Object s, Object r, Object o) {
		String result = extractGuts(s);
		result += decorate(extractHead(s), r, o);
		return result;
	}

	protected String makeTriple(Object s, Object r, Object o) {
		String result = extractGuts(s);
		result += extractGuts(r);
		result += extractGuts(o);
		result += decorate(extractHead(s), extractHead(r), extractHead(o));
		return result;
	}

	public String extractHead(Object x) {
		if (x == null) {
			return null;
		}
		else if (x instanceof RoleFrameGrandParent) {
			return ((RoleFrameGrandParent) x).getHead();
		}
		return getIndexedWord((String) x);
	}

	protected String extractGuts(Object x) {
		String result = "";
		if (x == null) {
			return result;
		}
		else if (x instanceof String && ((String) x).charAt(0) == '#') {
			String element = (String) x;
			int index = Integer.parseInt(element.substring(1));
			return roleFrameGrandParents.get(index).toString();
		}
		else if (x instanceof RoleFrameGrandParent) {
			result += ((RoleFrameGrandParent) x).toString();
		}
		else if (x instanceof RoleFrameParent) {
			result += ((RoleFrameParent) x).toString();
		}
		return result;
	}

	protected String getIndexedWord(Object object) {
		return getIndexedWord(object, true);
	}

	protected String getIndexedWord(Object object, boolean useNewIndex) {
		if (object == null) {
			return null;
		}
		String key = object.toString();
		String value = indexMap.get(key);
		String result = key;
		if (value == null || useNewIndex) {
			// This hack added to deal with translations from Genesis which have
			// index numbers
			int location = key.lastIndexOf('-');
			if (location > 0) {
				StringBuffer buffer = new StringBuffer(key);
				String suffix = buffer.substring(location + 1);
				try {
					Integer.parseInt(suffix);
					buffer.replace(location, location + 1, "+");
					result = buffer.toString();
				}
				catch (NumberFormatException e) {
					// If the last bit is not a number, then it must be a
					// hyphenated word, so leave alone.
				}
			}
			if (result.indexOf('+') < 0) {
				// If not, add one
				result += '+' + Integer.toString(index++);
				indexMap.put(key, result);
			}
			// Mark.say("Get indexed word", object, indexedRelation);
			return result;
		}
		else {
			// Mark.say("Get indexed word", object, value);
			return value;
		}
	}

	public String getHead() {
		if (head == null) {
			this.toString();
		}
		// Mark.say("Getting relation", head);
		return getIndexedWord(head);
	}

	public void setHead(Object head) {
		this.head = head;
	}

}

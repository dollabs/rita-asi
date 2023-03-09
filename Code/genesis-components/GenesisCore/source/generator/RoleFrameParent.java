package generator;

import java.util.HashMap;

import connections.DefaultSettings;
import utils.Mark;

/*
 * Created on Feb 8, 2011
 * @author phw
 */

public class RoleFrameParent extends RoleFrameGrandParent {

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	private String translation = "";

	private static HashMap<String, String> markers = new HashMap<String, String>();

	public RoleFrameParent() {
	}

	public RoleFrameParent(String source, RoleFrameGrandParent... entities) {
		initialize();
		translation = translate(source, entities);
	}

	public String toString() {
		return translation;
	}

	private void record(RoleFrameGrandParent[] inputs) {
		if (inputs == null) {
			return;
		}
		for (int i = 0; i < inputs.length; ++i) {
			roleFrameGrandParents.put(i + 1, inputs[i]);
		}

	}

	private void initialize() {
		if (markers.size() != 0) {
			return;
		}

		// Basic

		markers.put(":subject", "subject");
		markers.put(":object", "object");
		markers.put(":action", "verb");
		markers.put(":verb", "verb");

		// Decorations
		markers.put(":present", "present");
		markers.put(":past", "past");
		markers.put(":future", "future");
		markers.put(":passive", "passive");
		markers.put(":progressive", "progressive");
		markers.put(":ing", "progressive");
		markers.put(":negative", "not");
		markers.put(":not", "not");

		// Roles

		markers.put(":instrument", "with");
		markers.put(":coagent", "with");
		markers.put(":with", "with");
		markers.put(":conveyance", "by");
		markers.put(":by", "by");
		markers.put(":source", "from");
		markers.put(":from", "from");
		markers.put(":out_OF", "out_of");
		markers.put(":direction", "toward");
		markers.put(":toward", "toward");
		markers.put(":via", "via");
		markers.put(":destination", "to");
		markers.put(":to", "to");
		markers.put(":in", "in");
		markers.put(":during", "during");
		markers.put(":while", "while");
		markers.put(":when", "when");

		// Connectors

		markers.put(":before", "before");
		markers.put(":after", "after");
		markers.put(":while", "while");

	}

	public String translate(String source, RoleFrameGrandParent[] entities) {
		String result = "";
		Object subject = null;
		head = null;
		Object object = null;
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
		for (int i = 0; i < elements.length; ++i) {
			String element = elements[i];
			// Now, see if element is a key word
			if (element.charAt(0) == ':') {
				if (element.equals(":subject")) {
					subject = elements[++i];
				}
				else if (element.equals(":verb") || element.equals(":action")) {
					head = elements[++i];
				}
				else if (element.equals(":object")) {
					object = elements[++i];
				}
				// Miscellaneous Roles
				else if (element.equals(":with") || element.equals(":instrument") || element.equals(":coagent")) {
					result += makeTriple(head, markers.get(element), elements[++i]);
				}
				else if (element.equals(":in")) {
					result += makeTriple(head, "in", elements[++i]);
				}
				// Trajectory roles
				else if (element.equals(":source") || element.equals(":from") || element.equals(":out_of") || element.equals(":via")
				        || element.equals(":direction") || element.equals(":toward") || element.equals(":destination") || element.equals(":to")) {
					result += makeTriple(head, markers.get(element), elements[++i]);
				}
				// Time roles
				else if (element.equals(":during") || element.equals(":in")) {
					Mark.say("Head is", head);
					result += makeTriple(head, markers.get(element), elements[++i]);
				}
				else if (element.equals(":before") || element.equals(":after") || element.equals(":while")) {
					head = markers.get(element);
					object = elements[++i];
					result += makeProperty(markers.get(element), "is_clausal", "Yes");
				}
				// Decorations
				else if (element.equals(":present")) {
					result += makeTriple(head, "has_tense", "present");
				}
				else if (element.equals(":past")) {
					result += makeTriple(head, "has_tense", "past");
				}
				else if (element.equals(":future")) {
					result += makeTriple(head, "has_tense", "present");
					result += makeTriple(head, "has_modal", "will");
				}
				else if (element.equals(":passive")) {
					result += makeTriple(head, "has_voice", "passive");
				}
				else if (element.equals(":progressive") || element.equals(":ing")) {
					result += makeTriple(head, "is_progressive", "Yes");
				}
				else if (element.equals(":negative") || element.equals(":not")) {
					result += makeTriple(head, "has_polarity", "not");
				}

			}
			// Defaults
			else if (subject == null) {
				subject = element;
			}
			else if (head == null) {
				setRelation(element);
			}
			else if (object == null) {
				object = element;
			}
		}
		result = makeTriple(subject, head, object) + result;
		return result;
	}

	public void setRelation(Object verb) {
		this.head = verb;
	}

	

}

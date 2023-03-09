package start;

import translator.Translator;
import utils.Mark;

/*
 * Created on Jan 27, 2011
 * @author phw
 */

public class StartFoundation extends StartSoapConnection {
	
	private static Boolean debug;

	protected boolean showWebInteraction = false;

	private static StartFoundation startFoundation = null;

	// Legacy version
	// public static String urlString = "http://start.csail.mit.edu/askstart.cgi";

	public static String urlString = "http://start.csail.mit.edu/api.php";

	public StartFoundation() {
		setUrlString(urlString);
	}

	public static StartFoundation getStartFoundation() {
		if (startFoundation == null) {
			startFoundation = new StartFoundation();
		}
		return startFoundation;
	}

	public String processParseRequest(String request) {
		boolean debug = false;
		try {

			Mark.say(debug, "Before process probe");
			StringBuffer buffer = processProbe(request);
			Mark.say(debug, "After process probe");
			
			// Mark.say("Result: |", buffer, "|");
			if (buffer == null || buffer.length() == 0) {
				// Mark.err("A No Start result in processParseRequest for " + request);
				return null;
			}
			int startIndex = buffer.indexOf("<PRE>");
			int endIndex = buffer.indexOf("</PRE>");
			String result = "";
			if (startIndex < 0 || endIndex < 0) {
				// Mark.err("Unable to parse in processParseRequest", request);
				// Mark.err("Result is", buffer.toString());
			}
			else {
				result = buffer.substring(startIndex + 5, endIndex).trim();
			}

			if (result == null || result.trim().isEmpty()) {
				// Mark.err("B No Start result in processParseRequest for " + request);
				return null;
			}

			Mark.say(debug, "Request results\n>>> ", request, "\n>>>  ", result);
			return result.toLowerCase();
		}
		catch (Exception e) {
			Mark.say("Error thrown inside StartFoundation.processParseRequest");
			// e.printStackTrace();
		}
		return "";
	}

	public String processGeneratorRequest(String request) {
		boolean debug = false;
		Mark.say(debug, "Request:", request);
		String result = processProbe(request).toString();
		if (result == null || result.length() == 0) {
			return null;
		}
		Mark.say(debug, "Result", result);
		return result;
	}

	public static void main(String[] ignore) throws Exception {
		Mark.say("Starting");
		Mark.say("The START parse:", Start.getStart().parse("A bird flew flew."));
		Mark.say("The Genesis Innerese:", Translator.getTranslator().translate("A bird flew flew"));
	}
}

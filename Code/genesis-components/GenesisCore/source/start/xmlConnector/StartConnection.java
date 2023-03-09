package start.xmlConnector;

import java.io.*;
import java.net.*;

import start.StartFoundation;
import translator.BasicTranslator;
import utils.Mark;

/**
 * This package is used to connect to start's XML output with the goal of upgrading how Genesis currently communicates
 * with Start. Advantages over the old method include better handling of Start names and id tags. This class uses the
 * Start.java class to make a connection to Start and make a parse request in the XML format
 * 
 * @author Matthew July 6, 2013
 */

public class StartConnection {
	private static final String DEFAULT_SERVER = "genesis";

	private static final String EXPERIMENTAL_SERVER = "e-genesis";

	private static final String PARSE = "parse&dg=no";

	private static final String STORY_MODE = "use-kb&dg=no";

	private String mode = STORY_MODE;

	public StartConnection useParseMode() {
		mode = PARSE;
		return this;
	}

	public StartConnection useStoryMode() {
		mode = STORY_MODE;
		return this;
	}

	private String server = EXPERIMENTAL_SERVER;

	public StartConnection useDefaultServer() {
		server = DEFAULT_SERVER;
		return this;
	}

	public StartConnection useExperimentalServer() {
		server = EXPERIMENTAL_SERVER;
		return this;
	}

	// Legacy version
	// public String urlString = "http://start.csail.mit.edu/askstart.cgi";

	// public String urlString = "http://start.csail.mit.edu/api.php";

	/**
	 * Sends a sentence to Start for Parsing, recieves the output from start in XML format
	 * 
	 * @param sentence
	 * @return
	 */
	public String parse(String sentence) {
		// Encode sentence to URL format String
		try {
			sentence = URLEncoder.encode(sentence, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String query = "query=" + sentence;

		// Use STORY_MODE if you don't want to flush start ids
		// Use PARSE if you want to flush
		String params = "&pa=" + mode + "&action=compute-lf&te=XML&server=" + server;

		String request = query + params;

		Mark.say("Request is", request);

		StringBuffer buffer = new StringBuffer();
		try {

			URL url = new URL(StartFoundation.urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(request);
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				Mark.say(decodedString);
				buffer.append(decodedString + "\n");
			}
			in.close();

		}
		catch (MalformedURLException e) {
			Mark.err("Evidently bad start url");
		}
		catch (IOException e) {
			Mark.err("Evidently not connected to web or START is down");
		}
		catch (Exception e) {
			Mark.err("Evidently unable to process '" + request + "'");
		}
		return buffer.toString();
	}

	public static void main(String[] args) throws Exception {
		// String sentence = "John likes Mary.";
		// StartConnection sxq = new StartConnection();
		// String response = sxq.useExperimentalServer().useStoryMode().parse(sentence);
		// Mark.say(response);

		Mark.say(BasicTranslator.getTranslator().translate("John loves Mary"));
	}
}

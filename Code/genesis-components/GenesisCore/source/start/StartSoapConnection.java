package start;

import java.io.*;
import java.net.*;

import utils.*;

/*
 * Created on Apr 2, 2009
 * @author phw
 */

public class StartSoapConnection {

	String urlString;

	protected void setUrlString(String urlString) {
		this.urlString = urlString;
	}

	protected StringBuffer processProbe(String probe) {
		boolean debug = false;
		if (urlString == null) {
			System.err.println("No url string in SoapConnection.processProbe");
		}
		StringBuffer buffer = new StringBuffer();

		NewTimer.connectionTimer.reset();
		try {
			Mark.say(debug, "A1: Processing via web", probe);
			Mark.say(debug, "A2: url", urlString);
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);

			Mark.say(debug, "B");
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(probe);
			out.close();
			Mark.say(debug, "C");

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			Mark.say(debug, "D:", in);

			String decodedString;
			while ((decodedString = in.readLine()) != null) {
				Mark.say(debug, "decodedString", decodedString);
				buffer.append(decodedString + "\n");
			}
			in.close();

			Mark.say(debug, "E: Returned from Start");
			Mark.say(debug, "F: String is:", buffer.toString());
		}
		catch (MalformedURLException e) {
			Mark.err("Evidently bad url");
		}
		catch (IOException e) {
			// e.printStackTrace();
			Mark.err("Evidently not connected to web or START is down");
		}
		catch (Exception e) {
			Mark.err("Evidently unable to process '" + probe + "'");
		}
		NewTimer.connectionTimer.report(true, "Time taken");
		return buffer;
	}

	protected void processProbeWithoutReturn(String probe) {
		boolean debug = false;
		if (urlString == null) {
			System.err.println("No url string in SoapConnection.processProbe");
		}
		try {
			Mark.say(debug, "Processing via web", probe);
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(probe);
			out.close();
		}
		catch (MalformedURLException e) {
			Mark.err("Evidently bad url");
		}
		catch (IOException e) {
			e.printStackTrace();
			Mark.err("Evidently not connected to web or START is down");
		}
		catch (Exception e) {
			Mark.err("Evidently unable to process '" + probe + "'");
		}
	}

}
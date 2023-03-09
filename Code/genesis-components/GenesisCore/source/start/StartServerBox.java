package start;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import utils.Mark;
import zhutianYang.TestSTART;
import connections.*;
import constants.GenesisConstants;

public class StartServerBox extends AbstractWiredBox {

	private boolean debug = false;

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	private static StartServerBox startServerBox;

	@Override
	public String getName() {
		return "StartServerBox: a demonstration of net wire capability";
	}

	private StartServerBox() {
		super("Start server box");
	}

	public String remoteParse(String text, String mode, String url, String uuid) {
		String header = "query=";
		String trailer = "&pa=" + mode + "&action=compute-lf&server=" + url;
		String encodedString = "";
		try {
			encodedString = URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String probe = header + encodedString + trailer + "&uuid=" + uuid;
		String response = StartFoundation.getStartFoundation().processParseRequest(probe);
		return response;
	}

	public Object remoteGenerate(String text, String uuid) {
		// Mark.say("Received", text);

		String encodedString = null;

		try {
			encodedString = URLEncoder.encode(text, "UTF-8");
		}
		catch (UnsupportedEncodingException e1) {
			Mark.say("Unable to encode in PhraseFactory.generate");
			return null;
		}

		String header = "server=genesis&te=formated-text&de=n&action=generate&query=";
		
		// added 190826 for running our own START server
//		header = "machine=ashmore&" + header;

		String request = header + encodedString + "&uuid=" + uuid;

		String response = StartFoundation.getStartFoundation().processGeneratorRequest(header + encodedString);
		if (response != null) {
			return response.trim();
		}
		return null;
	}

	public static StartServerBox getStartServerBox() {
		if (startServerBox == null) {
			startServerBox = new StartServerBox();
			try {
				Connections.publish(startServerBox, GenesisConstants.server);
				if(Start.DEBUG_SPEED) {
					TestSTART.timer.initialize();
					TestSTART.timer.lapTime(true,"Created Start server");
				}
				
			}
			catch (Exception e) {
				Mark.err("Failed to create Start server");
				// e.printStackTrace();
			}
		}
		return startServerBox;
	}

	public static StartServerBox getStartServerBox(String name) {
		if (startServerBox == null) {
			startServerBox = new StartServerBox();
			try {
				Connections.publish(startServerBox, name);
				Mark.say("Created Start server");

			}
			catch (Exception e) {
				Mark.err("Failed to create Start server");
				// e.printStackTrace();
			}
		}
		return startServerBox;
	}

	public static void main(String[] ignore) throws Exception {
		StartServerBox.getStartServerBox("Start server test");
		Thread.sleep(5000);
	}
}

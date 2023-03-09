package gui;

import java.io.File;
import java.net.*;

import utils.*;
import connections.*;

/*
 * Created on Dec 29, 2009
 * @author phw
 */

public class MovieViewerExternal extends AbstractWiredBox {

	public MovieViewerExternal() {
		super("Movie viewer external");
		Connections.getPorts(this).addSignalProcessor("processInput");
	}

	public void processInput(Object o) {
		if (o instanceof String) {
			String s = (String) o;
			Mark.say("Processing movie externally, internal method abandoned and check box shut off");
			ProcessExternally.processFileExternally(s);
		}
		else if (o instanceof URL) {
			URL url = (URL) o;
			try {
				ProcessExternally.processFileExternally(new File(url.toURI()).getPath());
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		else if (o == null) {
		}
		else {
			System.err.println("ImagePanel.setImage got a " + o.getClass());
		}
	}

	public static void main(String[] ignore) {
		new MovieViewerExternal().processInput("c:/phw/java/gauntlet/memories/visualmemory/videos/Give.mpg");
	}

}

package standards;

import java.io.File;
import java.net.*;
import java.util.ArrayList;

import utils.*;

/**
 * Modified 15 Jun 2013 Idea now is to keep track of subdirectories, as need when running in webstart mode. Created on
 * Nov 1, 2007
 * 
 * @author phw
 */

public class StandardsAnchor extends Anchor {

	private static StandardsAnchor standardsAnchor;

	public static StandardsAnchor getStoryAnchor() {
		if (standardsAnchor == null) {
			standardsAnchor = new StandardsAnchor();
		}
		return standardsAnchor;
	}
	
	public String getFile(String resource) {
		try {
	        return getContent(resource).toURI().getPath();
        }
        catch (URISyntaxException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }return null;
	}

	
	public URL getContent(String resource) {
		URL url = this.getClass().getResource(resource);
		return url;
	}

	public static void main(String[] main) throws Exception {
		String command = StandardsAnchor.getStoryAnchor().getFile("Shakespeare Macbeth.pdf");
		Mark.say("Command:", command);
		// command = "\"c:/phw/javagit/genesis/bin/standards/shakespeare macbeth.pdf\"";
		command = "\"" + command.substring(1) + "\"";
		WindowsConnection.run(command);

	}

}

package stories;

import java.net.URL;
import java.util.ArrayList;

import utils.*;

/**
 * Modified 15 Jun 2013 Idea now is to keep track of subdirectories, as need when running in webstart mode. Created on
 * Nov 1, 2007
 * 
 * @author phw
 */

public class StoryAnchor extends Anchor {

	private static StoryAnchor storyAnchor;

	public static StoryAnchor getStoryAnchor() {
		if (storyAnchor == null) {
			storyAnchor = new StoryAnchor();
		}
		return storyAnchor;
	}

	ArrayList<String> subdirectories;
	
	private StoryAnchor() {}

	private ArrayList<String> getSubdirectories() {
		if (subdirectories == null) {
			subdirectories = new ArrayList<String>();
			// subdirectories.add("conflicts");
			// subdirectories.add("Cyberwar");
			// subdirectories.add("debugging");
			// subdirectories.add("Dune");
			// subdirectories.add("dxh");
			// subdirectories.add("giulianoStories");
			// subdirectories.add("hibaStories");
			// subdirectories.add("Icarus");
			// subdirectories.add("Imagination");
			// subdirectories.add("katherineStories");
			// subdirectories.add("Knowledge");
			// subdirectories.add("Law");
			// subdirectories.add("marinaMorozova");
			// subdirectories.add("marinaMorozova/1.Supplication");
			// subdirectories.add("marinaMorozova/2.Deliverance");
			// subdirectories.add("marinaMorozova/3.Crime_pursued_by_Vengence");
			// subdirectories.add("matthewFay");
			// subdirectories.add("matthewFay/generationCollab");
			// subdirectories.add("methods");
			// subdirectories.add("Personalities");
			// subdirectories.add("Shakespeare");
			// // No subdirectory listings
			// // subdirectories.add("silaStories");
			// // No subdirectory listings
			// // subdirectories.add("susanStories");
			// subdirectories.add("virginaStories");
		}
		return subdirectories;
	}

	public String getContent(String resource) {
		URL url = this.getClass().getResource(resource);
		String divider = System.getProperty("file.separator");
		if (url == null) {
			for (String s : getSubdirectories()) {
				url = this.getClass().getResource(divider + s + divider + resource);
				if (url != null) {
					break;
				}
			}
		}
		if (url == null) {
			Mark.err("Unable to find", resource);
		}
        try {
        	return TextIO.readStringFromURL(url);
        }
        catch (Exception e) {
        }
		return null;
	}

	public static void main(String[] main) throws Exception {
		Mark.say(StoryAnchor.getStoryAnchor().getContent("macbeh1.txt"));
	}

}

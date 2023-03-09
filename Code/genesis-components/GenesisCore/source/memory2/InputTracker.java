package memory2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;

/**
 * Since there is a race condition between the sentences and frames lists, you 
 * cannot trust that the two will be paired up correctly.
 * 
 * At this point, the only live code is the topFrame Set stuff.
 * 
 * @author sglidden
 *
 */
public class InputTracker {
	
	static private List<String> sentences =  new ArrayList<String>();
	static private List<Entity> frames = new ArrayList<Entity>();
	static private Set<Entity> topFrames = new HashSet<Entity>();
	
//	static private InputTracker sentenceFrame = null;
//	
//	static public InputTracker getSentenceFrame() {
//		if (sentenceFrame == null) {
//			sentenceFrame = new InputTracker();
//		}
//		return sentenceFrame;
//	}
	
	static synchronized public void addSentence(String english) {
//		System.out.println("SENTENCE: "+english);
		sentences.add(english);
	}
	
	static synchronized public void addFrame(Entity frame) {
//		System.out.println("FRAME: "+frame.toString(true));
//		System.out.println("FRAME T: "+frame.entityP());
//		System.out.println("FRAME D: "+frame.functionP());
//		System.out.println("FRAME S: "+frame.sequenceP());
//		System.out.println("FRAME R: "+frame.relationP());
		if (frame.getElements().size() > 0) {
			frames.add(frame.getElements().get(0));
		}
		else {
			frames.add(frame);
		}
		if (frames.size() > sentences.size()) {
			System.err.println("[InputTracker] See sam: WRONG NUMBER OF FRAMES!!!!!!!!!!!!!!!!");
		}
	}

	static synchronized public Entity getFrame(String english) {
		int index = sentences.indexOf(english);
		if (index == -1 || frames.size() <= index) {
			return null;
		}
		return frames.get(index);
	}
	
	static synchronized public String getSentence(Entity frame) {
//		System.out.println("LOOKING FOR: "+frame);
//		System.out.println("FRAMES: "+frames);
//		System.out.println("SENTENCES: "+sentences);
		int index = frames.indexOf(frame);
		if (index == -1 || sentences.size() <= index) {
			return null;
		}
		return sentences.get(index);
	}
	
	static synchronized public void addTopLevelFrame(Entity frame) {
//		System.out.print("Adding top level frame: "+frame);
//		if (frame.getElements().size() > 0) {
//			topFrames.add(frame.getElements().get(0));
//		}
//		else {
//			topFrames.add(frame);
//		}
	}
	
	static synchronized public boolean containsTopLevelFrame(Entity frame) {
//		return topFrames.contains(frame);
		return false;
	}
}

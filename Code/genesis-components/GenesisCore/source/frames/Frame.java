package frames;
import gui.NewFrameViewer;
import connections.WiredViewer;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
/*
 * Stub for future incorporation of reflection mechanism @author phw
 */
public abstract class Frame {
	/**
	 * @return thing representation of the frame;
	 */
	public abstract Entity getThing();
	/**
	 * The label found on the thread of each frame instance.
	 */
	public static final String FRAMETYPE = (String) RecognizedRepresentations.CAUSE_THING;
	
	/**
	 * @return a graphical viewer for this sort of Frame
	 */
	public WiredViewer getThingViewer() {
		return new NewFrameViewer();
	}
}

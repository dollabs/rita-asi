package frames;
import frames.entities.Entity;
import utils.EntityUtils;
public class FrameFactory {
	/**
	 * Translates from a given thing object to its corresponding frame, if any.
	 * 
	 * @param frameThing :
	 *            a thing representation of a frame.
	 * @return corresponding frame
	 */
	public static Frame translate(Entity frameThing) {
		String type = FrameFactory.getFrameType(frameThing);
		if (type.equals(ADRLFrame.FRAMETYPE)) {
			return new ADRLFrame(frameThing);
		}
		if (type.equals(BlockFrame.FRAMETYPE)) {
			return new BlockFrame(frameThing);
		}
		if (type.equals(Frame.FRAMETYPE)) {
			return new CAFrame(frameThing);
		}
		if (type.equals(CDFrame.FRAMETYPE)) {
			return new CDFrame(frameThing);
		}
		if (type.equals(ForceFrame.FRAMETYPE)) {
			return new ForceFrame(frameThing);
		}
		if (type.equals(GeometryFrame.FRAMETYPE)) {
			return new GeometryFrame(frameThing);
		}
		if (type.equals(PathElementFrame.FRAMETYPE)) {
			return new PathElementFrame(frameThing);
		}
		if (type.equals(PlaceFrame.FRAMETYPE)) {
			return new PlaceFrame(frameThing);
		}
		if (type.equals(TimeFrame.FRAMETYPE)) {
			return new TimeFrame(frameThing);
		}
		if (type.equals(TrajectoryFrame.FRAMETYPE)) {
			return new TrajectoryFrame(frameThing);
		}
		if (type.equals(TransitionFrame.FRAMETYPE)) {
			return new TransitionFrame(frameThing);
		}
		if (type.equals(EventFrame.FRAMETYPE)) {
			return new EventFrame(frameThing);
		}
		if (type.equals(QuestionFrame.FRAMETYPE)) {
			return new QuestionFrame(frameThing);
		}
		else {
			return null;
		}
	}
	/**
	 * Extracts a thing representation from a given frame.
	 * 
	 * @param frame :
	 *            given frame
	 * @return : clone of the thing representation of frame.
	 */
	public static Entity translate(Frame frame) {
		return (Entity) frame.getThing().clone();
	}
	public static String getFrameType(Entity frameThing) {
		// Thread t = frameThing.getPrimedThread();
		// if (t.get(0).equalsIgnoreCase("thing")) {
		// return t.get(1);
		// } else {
		// return t.get(0);
		// }
		return (String) EntityUtils.getRepType(frameThing);
	}
	/**
	 * Returns the type of the frame.
	 * 
	 * @param frame
	 *            Frame
	 * @return String of FRAMETYPE
	 */
	public static String getFrameType(Frame frame) {
		return FrameFactory.getFrameType(frame.getThing());
	}
}

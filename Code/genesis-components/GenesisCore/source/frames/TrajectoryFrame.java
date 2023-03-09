package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import gui.TrajectoryViewer;
public class TrajectoryFrame extends Frame {
	public static final String	FRAMETYPE	= (String) RecognizedRepresentations.TRAJECTORY_THING;
	private Entity			thing;
	/**
	 * Constructor takes in a Trajectory in Thing form
	 * 
	 * @param t
	 *            Thing
	 */
	public TrajectoryFrame(Entity t) {
		// make sure t is in a valid form
		// just use Adam's test for now
		if (t.isA(TrajectoryFrame.FRAMETYPE)) {
			this.thing = t;
		}
	}
	public TrajectoryFrame(TrajectoryFrame f) {
		this((Entity) f.getThing().clone());
	}
	@Override
	public Entity getThing() {
		return this.thing;
	}
	//@Override
	//public WiredPanel getThingViewer() {
	//	return new TrajectoryViewer();
	//}
	// TODO: need to know how Thing is structured internally
	// public Thing getMover() {
	//		
	// }
	//	
	// public Thing getFrom() {
	//		
	// }
	//
	// public Thing getTo() {
	//		
	// }
	//	
	// public Thing getVia() {
	//		
	// }
}

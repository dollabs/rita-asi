package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Relation;
import utils.StringUtils;
/**
 * Class for creating Allen time relations between two events.
 * @author blamothe
 */
public class TimeFrame extends Frame {
	public static String [] times = {"before", "equal", "meets", "overlaps", "during", "starts", "finishes"};
	public static String FRAMETYPE = (String) RecognizedRepresentations.TIME_REPRESENTATION;
	public static Relation createTimeRelation(String type, Entity event1, Entity event2) {
		if (!StringUtils.testType(type, times)) {
			System.err.println("Sorry, argument " + type + " provided to createTimeRelation is not a valid time relation.");
			return null;
		}
		Relation result = new Relation(FRAMETYPE, event1, event2);
		result.addType(type);
		return result;
	}
	
	public static Entity getEvent1(Relation timeRelation) {
		if (timeRelation.isA(FRAMETYPE)) {
			return timeRelation.getSubject();
		}
		System.err.println("Sorry, " + timeRelation + " is not a valid time relation.");
		return null;
	}
	
	public static Entity getEvent2(Relation timeRelation) {
		if (timeRelation.isA(FRAMETYPE)) {
			return timeRelation.getObject();
		}
		System.err.println("Sorry, " + timeRelation + " is not a valid time relation.");
		return null;
	}
	
	public static String getTimeType(Relation timeRelation) {
		if (timeRelation.isA(FRAMETYPE)) {
			return timeRelation.getType();
		}
		System.err.println("Sorry, " + timeRelation + " is not a valid time relation.");
		return "";
	}
	
	/**
	 * Thing representation of time frames.
	 */
	private Relation timeRelation;
	
	public TimeFrame(Entity t) {
		super();
		if (t.isA(TimeFrame.FRAMETYPE)) {
			this.timeRelation = (Relation) t;
		}
	}
	
	public Entity getThing() {
		return timeRelation;
	}
}

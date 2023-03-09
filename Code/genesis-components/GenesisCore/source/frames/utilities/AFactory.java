package frames.utilities;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import utils.StringUtils;
public class AFactory {
	
	public static String [] times = {"Before", "Equal", "Meets", "Overlaps", "During", "Starts", "Finishes"};
	public static Relation createTimeRelation(String type, Entity subject, Entity object) {
		if (!StringUtils.testType(type, times)) {
			System.err.println("Sorry, argument " + type + " provided to createTimeRelation is not a valid time relation.");
			return null;
		}
		Relation result = new Relation("Time Relation", subject, object);
		result.addType(type);
		return result;
	}
	
	public static Sequence createActionSequence (String type, Vector<Entity> args) {
		Sequence result = new Sequence("Action Sequence");
		result.addType(type);
		result.setElements(args);
		return result;
	}
}

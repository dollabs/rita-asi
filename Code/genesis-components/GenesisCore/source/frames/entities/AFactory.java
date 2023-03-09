package frames.entities;

import java.util.Vector;

import utils.StringUtils;

public class AFactory {
	
	public static String [] times = {"before", "meets", "overlaps", "during",  "starts", "finishes", "equal"};

	public static Relation createTimeRelation(String type, Entity subject, Entity object) {
		if (!StringUtils.testType(type, times)) {
			System.err.println("Sorry, argument " + type + " provided to createTimeRelation is not a valid time relation.");
			return null;
		}
		Relation result = new Relation("time", subject, object);
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

package frames.utilities;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import utils.StringUtils;
/**
 * Utility class for Rieger's Commonsense Algorithmic Knowledge Representation.
 * @author blamothe
 */
public class CAFactory {
	public static String graphType = "commonsense algorithm";
	public static String eventType = "ca event";
	public static String relationType = "ca relation";
	public static final String [] causalTypes = {"causes", "enables"};
	public static final String [] causalSubtypes = {"one-shot", "continuous", "gated", "coupled"};
	public static final String [] eventTypes = {"action", "state", "statechange", "tendency", "want"};
	
	public static Function makeNewEvent(String type, Entity event) {
		if (!StringUtils.testType(type, eventTypes)) {
			System.err.println("Sorry, " + type + " is not a recognized commonsense event type.");
			return null;
		}
		Function result = new Function(eventType, event);
		result.addType(type);
		return result;
	}
	public static Relation makeNewLink (String type, String subtype, Entity subject, Entity object) {
     System.err.println("Type: " + type);
     System.err.println("Subject: " + subject + ", " + subject.isA("action"));
     System.err.println("Object: " + object + ", " + object.isA("statechange"));
		
		if ((subtype != "one-shot") && (subtype != "continuous") && (subtype != "")) {
			System.err.println("Invalid subtype to CAFactory.makeNewLink.");
			return null;
		}
		if (type == "causes") {
			if (subject.isA("action") || subject.isA("tendency")) {
				if (object.isA("state") || (object.isA("statechange") && (subtype == "continuous"))) {
					return makeLink(type, subtype, subject, object);
				}
			} else {
				System.err.println("Sorry, the subject of a causal relation must be an action or tendency, and the state must be a state or statechange.");
				return null;
			}
		}
		
		if (type == "enables") {
			if (subject.isA("state") && (object.isA("action") || object.isA("tendency"))) {
				return makeLink(type, subtype, subject, object);
			} else {
				System.err.println("Sorry, the subject of an enablement relation must be a state, and the object must be an action or tendency.");
				return null;
			}
		}
		
		if (type == "coupled") {
			if ((subject.isA("state") || subject.isA("statechange")) && (object.isA("state") || object.isA("statechange"))) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, all components of a coupled relation must either be a state or statechange.");
				return null;
			}
		}
		
		if (type == "byproduct") {
			if (subject.isA("action") && (object.isA("state") || object.isA("statechange"))) {
				return makeLink(type, subtype, subject, object);
			} else {
				System.err.println("Error, the subject of a byproduct relation must be an action, and the object must be a state or statechange.");
				return null;
			}
		}
		
		if (type == "antagonize") {
			if ((subject.isA("state") || subject.isA("statechange")) && (object.isA("state") || object.isA("statechange"))) {
				return makeLink(type, subtype, subject, object);
			} else {
				System.err.println("Error, all components of an antagonize relation must either be states or statechanges.");
			}
		}
		
		if (type == "goal couple") {
			if ((subject.isA("want") || subject.isA("state")) && object.isA("state")) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, the subject of a goal couple must be a want, and the object must be a state.");
				return null;
			}
		}
		
		if (type == "disenables") {
			if ((subject.isA("action") || subject.isA("tendency")) && (object.isA("state") || object.isA("statechange"))) {
				return makeLink(type, subtype, subject, object);
			} else {
				System.err.println("Error, the subject of a disenables relation must be an action or tendency, and the object must be a state or statechange.");
				return null;
			}
		}
		
		if (type == "induces") {
			if ((subject.isA("state") || subject.isA("statechange")) && object.isA("want")) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, the subject of a induces relation must be a state or statechange, and the object must be a want.");
				return null;
			}
		}
		
		if (type == "optimizes") {
			if (subject.isA("state") && object.isA("action")) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, the subject of an optimizes relation must be a state, and the object must be an action.");
				return null;
			}
		}
		
		if (type == "gates") {
			if (subject.isA("state") && object.isA(relationType)) {
				object.addType("gated");
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, the subject of a gates relation must be a state, and the object must be a valid ca relation.");
			}
		}
		
		if (type == "concurrent") {
			if (subject.isA("action") && object.isA(relationType)) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, the subject of a concurrent relation must be an action, and the object must be a valid ca relation.");
			}
		}
		
		if (type == "compound") {
			if (subject.isA("state") && object.isA("state")) {
				return makeLink(type, subject, object);
			} else {
				System.err.println("Error, a compound link can only be between states.");
			}
		}
			System.err.println("Sorry, " + type + " is not a know ca event type.");
			return null; 
	}
	
	private static Relation makeLink (String type, String subtype, Entity subject, Entity object) {
		Relation result = new Relation(relationType, subject, object);
		result.addType(type);
		result.addType(subtype);
		return result;
	}
	
	private static Relation makeLink (String type, Entity subject, Entity object) {
		Relation result = new Relation(relationType, subject, object);
		result.addType(type);
		return result;
	}
	
	
}

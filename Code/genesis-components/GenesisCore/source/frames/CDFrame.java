package frames;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import utils.StringUtils;
public class CDFrame extends Frame{
	public static String FRAMETYPE = "conceptualization";
	public static String [] actions = {"atrans", "ptrans", "propel", "move", "grasp", "ingest", "expel", "mtrans", "mbuild", "speak", "attend"};
	
	public static Sequence makeConceptualization (Entity actor, String action, Entity object, Function direction, Entity instrument) {
		if (!StringUtils.testType(action, actions)) {
			System.err.println("Error, " + action + " is not a Conceptual Dependency Theory primitive act.");
			return null;
		}
		Sequence result = new Sequence(FRAMETYPE);
		result.addType(action);
		Vector<Entity> elts = new Vector<Entity>();
		elts.insertElementAt(actor, 0);
		elts.insertElementAt(object, 1);
		elts.insertElementAt(direction, 2);
		elts.insertElementAt(instrument, 3);
		result.setElements(elts);
		return result;
	}
	
	public static Entity getActor(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to getActor must be a conceptualization.");
			return null;
		}
		return conceptualization.getElement(0);
	}
	
	public static Entity getObject(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error: Argument to getObject must be a conceptualization.");
			return null;
		}
		return conceptualization.getElement(1);
	}
	
	public static Function getDirection(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error: Argument to getDirection must be a conceptualization.");
			return null;
		}
		return (Function) conceptualization.getElement(2);
	}
	
	public static String getAction(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error: Argument to getAction must be a conceptualization.");
			return "";
		}
		return conceptualization.getType();
	}
	
	public static Entity getInstrument(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error: Argument to getInstrument must be a conceptualization.");
			return null;
		}
		return (Entity) conceptualization.getElement(3);
	}
	
	public static void setActor (Sequence conceptualization, Entity actor) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to setActor must be a conceptualization.");
			return;
		}
		conceptualization.setElementAt(actor, 0);
		return;
	}
	
	public static void setAction (Sequence conceptualization, String action) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to setAction must be a conceptualization.");
			return;
		}
		conceptualization.removeType(conceptualization.getType());
		conceptualization.addType(action);
		return;
	}
	
	public static void setObject (Sequence conceptualization, Entity object) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to setObject must be a conceptualization.");
			return;
		}
		conceptualization.setElementAt(object, 1);
		return;
	}
	
	public static void setDirection (Sequence conceptualization, Function direction) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to setDirection must be a conceptualization.");
			return;
		}
		conceptualization.setElementAt(direction, 2);
		return;
	}
	
	public static void setInstrument (Sequence conceptualization, Entity instrument) {
		if (!conceptualization.isA(FRAMETYPE)) {
			System.err.println("Error:  Argument to setInstrument must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(instrument, 3);
			return;
		}
		System.err.println("Sorry, can't set instrument in a stative conceptualization");
		return;
	}
	private Sequence conceptualization;
	
	public CDFrame (Entity t) {
		if (t.isA(FRAMETYPE)) {
			this.conceptualization = (Sequence) t;
		}
	}
	public Entity getThing() {
		return conceptualization;
	}
	
	public String toString() {
		if (conceptualization != null) {
			return conceptualization.toString();
		}
		return "";
	}
}

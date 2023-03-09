package frames.utilities;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Sequence;
import utils.StringUtils;
/**
 * Utility class for creating and using Shank conceptualizations in
 * Conceptual Dependency Theory.
 * @author blamothe
 *
 */
public class SFactory {
	public static String FRAMELABEL = "conceptualization";
	public static String [] actions = {"atrans", "ptrans", "propel", "move", "grasp", "ingest", "expel", "mtrans", "mbuild", "speak", "attend"};
	public static Sequence makeActiveConceptualization (Entity actor, Entity action, Entity object, Entity direction, Entity instrument) {
		if (!StringUtils.testType(action.getType(), actions)) {
			System.err.println("Sorry, the thing " + action + " is not a Conceptual Dependency Theory primitive act.");
			return null;
		}
		Sequence result = new Sequence(FRAMELABEL);
		result.addType("active");
		Vector<Entity> elts = new Vector<Entity>();
		elts.insertElementAt(actor, 0);
		elts.insertElementAt(action, 1);
		elts.insertElementAt(object, 2);
		elts.insertElementAt(direction, 3);
		elts.insertElementAt(instrument, 4);
		result.setElements(elts);
		return result;
	}
	
	public static Sequence makeStativeConceptualization (Entity sObject, Entity state, Entity value) {
		Sequence result = new Sequence(FRAMELABEL);
		result.addType("stative");
		Vector<Entity> elts = new Vector<Entity>();
		elts.insertElementAt(sObject, 0);
		elts.insertElementAt(state, 1);
		elts.insertElementAt(value, 2);
		result.setElements(elts);
		return result;
	}
	
	public static Entity getActor(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getActor must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("active")) {
			return (Entity) conceptualization.getElements().get(0);
		}
		System.err.println("Sorry, can't get actor from a stative conceptualization");
		return null;
	}
	
	public static Entity getAction(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getAction must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("active")) {
			return (Entity) conceptualization.getElements().get(1);
		}
		System.err.println("Sorry, can't get action from a stative conceptualization");
		return null;
	}
	
	public static Entity getObject(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getObject must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("active")) {
			return (Entity) conceptualization.getElements().get(2);
		}
		System.err.println("Sorry, can't get object from an active conceptualization");
		return null;
	}
	
	public static Entity getDirection(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getDirection must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("active")) {
			return (Entity) conceptualization.getElements().get(3);
		}
		System.err.println("Sorry, can't get direction from a stative conceptualization");
		return null;
	}
	
	public static Entity getInstrument(Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getInstrument must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("active")) {
			return (Entity) conceptualization.getElements().get(5);
		}
		System.err.println("Sorry, can't get instrument from a stative conceptualization");
		return null;
	}
	
	public static Entity getSObject (Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getSObject must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("stative")) {
			return (Entity) conceptualization.getElements().get(0);
		}
		System.err.println("Sorry, can't get sObject from an active conceptualization");
		return null;
	}
	
	public static Entity getState (Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getState must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("stative")) {
			return (Entity) conceptualization.getElements().get(1);
		}
		System.err.println("Sorry, can't get state from an active conceptualization");
		return null;
	}
	
	public static Entity getValue (Sequence conceptualization) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to getValue must be a conceptualization.");
			return null;
		}
		if (conceptualization.isA("stative")) {
			return (Entity) conceptualization.getElements().get(2);
		}
		System.err.println("Sorry, can't get value from an active conceptualization");
		return null;
	}
	
	public static void setActor (Sequence conceptualization, Entity actor) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setActor must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(actor, 0);
			return;
		}
		System.err.println("Sorry, can't set actor in a stative conceptualization");
		return;
	}
	
	public static void setAction (Sequence conceptualization, Entity action) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setAction must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(action, 1);
			return;
		}
		System.err.println("Sorry, can't set action in a stative conceptualization");
	}
	
	public static void setObject (Sequence conceptualization, Entity object) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setObject must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(object, 2);
			return;
		}
		System.err.println("Sorry, can't set object in a stative conceptualization");
		return;
	}
	
	public static void setDirection (Sequence conceptualization, Entity direction) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setDirection must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(direction, 3);
			return;
		}
		System.err.println("Sorry, can't set direction in a stative conceptualization");
		return;
	}
	
	public static void setInstrument (Sequence conceptualization, Entity instrument) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setInstrument must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("active")) {
			conceptualization.setElementAt(instrument, 4);
			return;
		}
		System.err.println("Sorry, can't set instrument in a stative conceptualization");
		return;
	}
	
	public static void setSObject (Sequence conceptualization, Entity sObject) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setSObject must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("stative")) {
			conceptualization.setElementAt(sObject, 0);
			return;
		}
		System.err.println("Sorry, can't set sObject in an active conceptualization");
		return;
	}
	
	public static void setState (Sequence conceptualization, Entity state) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setState must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("stative")) {
			conceptualization.setElementAt(state, 1);
			return;
		}
		System.err.println("Sorry, can't set state in an active conceptualization");
		return;
	}
	
	public static void setValue (Sequence conceptualization, Entity value) {
		if (!conceptualization.isA(FRAMELABEL)) {
			System.err.println("Error:  Argument to setValue must be a conceptualization.");
			return;
		}
		if (conceptualization.isA("stative")) {
			conceptualization.setElementAt(value, 2);
			return;
		}
		System.err.println("Sorry, can't set value in an active conceptualization");
		return;
	}
}

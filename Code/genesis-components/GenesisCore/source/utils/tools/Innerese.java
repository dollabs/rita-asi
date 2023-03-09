package utils.tools;

import utils.Mark;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/**
 * Function of this class mostly moved to innerese package class Super pi day
 * <p>
 * Created on Jul 25, 2013
 * 
 * @author phw
 */

public class Innerese {

	/*
	 * Used for Co57 play by play
	 */
	public static Relation makeEvent(String subject, String verb, Object object, Object indirectObject) {
		Entity subjectThing = new Entity(subject);
		Entity objectThing = null;
		Entity indirectObjectThing = null;
		if (object instanceof Entity) {
			objectThing = (Entity) object;
		}
		else if (object instanceof String) {
			objectThing = new Entity((String) object);
		}
		subjectThing.addFeature(Markers.INDEFINITE);
		if (objectThing != null) {
			objectThing.addFeature(Markers.INDEFINITE);
		}

		if (indirectObject instanceof Entity) {
			indirectObjectThing = (Entity) indirectObject;
		}
		else if (indirectObject instanceof String) {
			indirectObjectThing = new Entity((String) indirectObject);
		}

		Sequence roles = new Sequence(Markers.ROLE_MARKER);

		if (verb.equalsIgnoreCase("putdown")) {
			verb = "put_down";
		}
		else if (verb.equalsIgnoreCase("pickup") || verb.equalsIgnoreCase("pickup2")) {
			verb = "pick_up";
		}
		if (object != null && objectThing != null) {
			if (objectThing.isA(Markers.SOMETHING)) {
				objectThing.addFeature(Markers.NONE);
			}
			roles.addElement(new Function(Markers.OBJECT_MARKER, objectThing));
		}
		if (indirectObject != null && indirectObjectThing != null) {
			if (indirectObjectThing.isA(Markers.SOMETHING)) {
				indirectObjectThing.addFeature(Markers.NONE);
			}
			// Depends on verb
			if ("replace".equalsIgnoreCase(verb)) {
				roles.addElement(new Function(Markers.WITH_MARKER, indirectObjectThing));
			}
			else if ("give".equalsIgnoreCase(verb)) {
				roles.addElement(new Function(Markers.TO_MARKER, indirectObjectThing));
			}
			else if ("take".equalsIgnoreCase(verb)) {
				roles.addElement(new Function(Markers.FROM_MARKER, indirectObjectThing));
			}
			else {
				roles.addElement(new Function(Markers.TO_MARKER, indirectObjectThing));
			}

		}
		// Mark.say("Event description:", roles.asString());
		Relation event = new Relation(verb, subjectThing, roles);
		return event;
	}

	public static void main(String[] ignore) throws Throwable {

	}

}

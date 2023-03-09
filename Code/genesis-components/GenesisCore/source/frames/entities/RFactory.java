package frames.entities;

import constants.Markers;

/**
 * Conventience class.  Sugar for creating role frames.
 * Created on Jan 29, 2013
 * @author phw
 */

public class RFactory {

	public static Relation makeRoleFrameRelation(Object a, String relation, Object o) {
		Entity agent;
		Entity object;
		if (a instanceof String) {
			agent = new Entity((String) a);
		}
		else {
			agent = (Entity) a;
		}

		if (o instanceof String) {
			object = new Entity((String) o);
		}
		else if (o instanceof Entity) {
			object = (Entity) o;
		}
		else {
			object = null;
		}
		Sequence roles = new Sequence(Markers.ROLE_MARKER);

		Relation result = new Relation(relation, agent, roles);

		if (object != null) {
			roles.addElement(new Function(Markers.OBJECT_MARKER, object));
		}
		return result;
	}
	
	public static Relation addRoleFrameTo(Object o, Relation relation) {
		return addRoleFrameRole(Markers.TO_MARKER, o, relation);
	}
	
	public static Relation addRoleFrameFrom(Object o, Relation relation) {
		return addRoleFrameRole(Markers.FROM_MARKER, o, relation);
	}
	
	public static Relation addRoleFrameRole (String marker, Object o, Relation relation) {
		Entity object;
		if (o instanceof String) {
			object = new Entity((String) o);
		}
		else if (o instanceof Entity) {
			object = (Entity) o;
		}
		else {
			object = null;
		}
		
		Entity roles = relation.getObject();
		
		if (roles.sequenceP() && object != null) {
			roles.addElement(new Function(marker, object));
		}
		return relation;
	}


}

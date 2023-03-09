package expert;

import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Aug 3, 2010
 * @author phw
 */

public class Recognizers {

	public static Entity theThing(Object o) {
		if (o instanceof Entity) {
			return (Entity) o;
		}
		return null;
	}

	public static Entity theSubject(Object o) {
		if (o instanceof Entity) {
			return ((Entity) o).getSubject();
		}
		return null;
	}

	public static Entity theObject(Object o) {
		if (o instanceof Entity) {
			return ((Entity) o).getObject();
		}
		return null;
	}

	public static boolean action(Object o) {
		if (o instanceof Entity) {
			Entity t = (Entity) o;
			if (t.isA(Markers.ACTION_WORD)) {
				return true;
			}
		}
		return false;
	}

	public static boolean agent(Object o) {
		if (o instanceof Entity) {
			Entity t = (Entity) o;
			if (t.relationP(Markers.AGENT_MARKER) && t.getSubject().entityP(Markers.ENTITY_WORD) && t.getObject().isA(Markers.ACTION_WORD)) {
				return true;
			}
		}
		return false;
	}

}

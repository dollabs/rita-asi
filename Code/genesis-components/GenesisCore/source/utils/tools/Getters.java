package utils.tools;

import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Jul 27, 2012
 * @author phw
 */

public class Getters {

	/*
	 * Replaces object of action
	 */
	public static void replaceObject(Entity x, Entity element) {
		Entity object = getSlot(Markers.OBJECT_MARKER, x);
		object.setSubject(element);
	}

	/*
	 * Get's specified slot from an action
	 */
	public static Entity getSlot(String marker, Entity t) {
		if (t.relationP() && t.getObject().sequenceP()) {
			for (Entity role : t.getObject().getElements()) {
				if (role.functionP(marker)) {
					return role;
				}
			}
		}
		return null;
	}

}

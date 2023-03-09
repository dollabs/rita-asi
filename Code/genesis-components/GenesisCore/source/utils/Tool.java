package utils;


import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Nov 3, 2011
 * @author phw
 */

public class Tool {

	public static Entity extractObject(Entity t) {
		if (t.relationP(Markers.ACTION_MARKER)) {
			return extractObject(t.getObject());
		}
		else if (t.sequenceP(Markers.ROLE_MARKER)) {
			for (Entity x : t.getElements()) {
				if (x.isA(Markers.OBJECT_MARKER)) {
					return x.getSubject();
				}
			}
		}
		return null;
	}
	
	public static Entity extractPath(Entity r) {
		Entity path = extractObject(r);
		if (path.sequenceP(Markers.PATH_MARKER)) {
			return path;
		}
		return null;
	}
}

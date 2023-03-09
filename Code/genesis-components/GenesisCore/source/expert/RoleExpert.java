package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class RoleExpert extends AbstractWiredBox {

	public RoleExpert() {
		super("Role expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP(Markers.ACTION_MARKER)) {
			Relation r = (Relation) t;
			// Connections.getPorts(this).transmit(Markers.NEXT, r.getSubject());
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		// Want to pass on in any case to see what kind of role frame it is
		Connections.getPorts(this).transmit(Markers.NEXT, t);
	}
}

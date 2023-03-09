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

public class CoercionExpert extends AbstractWiredBox {

	public CoercionExpert() {
		super("Coercion expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.COERCE_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			if (!r.getSubject().entityP()) {
				Connections.getPorts(this).transmit(Markers.NEXT, r.getSubject());
			}
			Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
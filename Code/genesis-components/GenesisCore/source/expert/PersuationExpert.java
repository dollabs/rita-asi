package expert;

import utils.*;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class PersuationExpert extends AbstractWiredBox {

	public PersuationExpert() {
		super("Persuation expert");
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP(Markers.PERSUATION_MARKER) || t.relationP(Markers.ASK_MARKER)) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);

			Entity goal = Tool.extractObject(r);
			if (goal != null) {
				Connections.getPorts(this).transmit(Markers.NEXT, goal);
			}
			else {
				Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
			}

		}
		else {
			// Mark.say("Transmitting from persuation expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
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

public class BeliefExpert extends AbstractWiredBox {

	public BeliefExpert() {
		super("Belief expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isA(Markers.BELIEF_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
		}
		else {
			// Mark.say("Transmitting from belief expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
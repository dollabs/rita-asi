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

public class IntentionExpert extends AbstractWiredBox {

	public IntentionExpert() {
		super("Persuation expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		// Mark.say("In intention expert:", t.asString());
		if (t.relationP(Markers.INTENTION_MARKER)) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);

			Entity goal = Tool.extractObject(r);
			if (goal != null) {
				// Mark.say("Intention expert forwarding", goal);
				Connections.getPorts(this).transmit(Markers.NEXT, goal);
			}
			else {
				Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
			}

		}
		else {
			// Mark.say("Transmitting from intention expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
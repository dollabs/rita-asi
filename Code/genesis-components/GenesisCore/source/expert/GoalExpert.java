package expert;

import translator.BasicTranslator;
import utils.*;

import connections.*;
import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Entity;
import frames.entities.Relation;

public class GoalExpert extends AbstractWiredBox {

	public GoalExpert() {
		super("Goal expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.GOAL_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			// Mark.say("Transmitting!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			Entity goal = Tool.extractObject(r);
			if (goal != null) {
				Connections.getPorts(this).transmit(Markers.NEXT, goal);
			}
			else if (!r.getObject().entityP()) {
				transmitIfNotThing(this, Markers.LOOP, r.getObject());
			}
		}
		else {
			// Mark.say("Transmitting from goal expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}

	private void transmitIfNotThing(WiredBox box, String port, Entity t) {
		if (!t.entityP()) {
			Connections.getPorts(box).transmit(port, t);
		}
	}

}
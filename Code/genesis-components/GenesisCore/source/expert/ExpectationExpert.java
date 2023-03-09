package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class ExpectationExpert extends AbstractWiredBox {

	public ExpectationExpert() {
		super("Expectation expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.hasProperty(Markers.MODAL, Markers.WILL_WORD)) {
			// Mark.say("Got it!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			Function r = (Function) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			// Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
		}
		else if (t.functionP(Markers.EXPECTATION)) {
			// Mark.say("Got it!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			Function r = (Function) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			// Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
		}
		else {
			// Mark.say("Transmitting from prediction expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
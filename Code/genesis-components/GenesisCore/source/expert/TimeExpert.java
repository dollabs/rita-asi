package expert;

import utils.Mark;
import connections.*;
import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class TimeExpert extends AbstractWiredBox {

	public TimeExpert() {
		super("Time expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.TIME_TYPE) && t.relationP()) {
			Connections.getPorts(this).transmit(Markers.VIEWER, t);
			Connections.getPorts(this).transmit(Markers.LOOP, t.getSubject());
			Connections.getPorts(this).transmit(Markers.LOOP, t.getObject());
		}
		else if (t.functionP(Markers.MILESTONE)) {
			Connections.getPorts(this).transmit(Markers.VIEWER, t);
		}
		else {
			// Mark.say("Transmitting from time expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
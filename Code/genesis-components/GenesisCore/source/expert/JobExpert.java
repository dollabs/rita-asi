package expert;

import utils.Mark;

import connections.*;
import constants.*;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 23, 2009
 * @author phw
 */

public class JobExpert extends AbstractWiredBox {

	public JobExpert() {
		super("Position expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering JobExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.JOB_TYPE_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}

package expert;

import utils.Mark;

import connections.*;
import constants.*;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class ThreadExpert extends AbstractWiredBox {

	public ThreadExpert() {
		super("Thread expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering ThreadExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.CLASSIFICATION_MARKER) && t instanceof Relation) {
			Connections.getPorts(this).transmit(Markers.VIEWER, t);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
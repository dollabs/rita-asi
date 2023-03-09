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

public class PossessionExpert extends AbstractWiredBox {

	public PossessionExpert() {
		super("Possession expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering PosessionExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP() && t.isAPrimed(Markers.HAVE)) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);

		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
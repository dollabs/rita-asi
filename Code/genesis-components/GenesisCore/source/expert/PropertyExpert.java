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

public class PropertyExpert extends AbstractWiredBox {

	public PropertyExpert() {
		super("Property expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering PropertyExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP() && t.isAPrimed(Markers.PROPERTY_TYPE)) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
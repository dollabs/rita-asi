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

public class SocialExpert extends AbstractWiredBox {

	public SocialExpert() {
		super("Social expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Mark.say(GenesisConstants.FLOW, "Entering SocialExpert");
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.SOCIAL_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 23, 2009
 * @author phw
 */

public class MoodExpert extends AbstractWiredBox {

	public MoodExpert() {
		super("Mood expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.MENTAL_STATE_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}

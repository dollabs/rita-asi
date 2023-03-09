package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class PathExpert extends AbstractWiredBox {

	public PathExpert() {
		super("Path expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.ROLE_MARKER)) {
			Sequence s = (Sequence) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, s);
			for (Entity element : s.getElements()) {
				Connections.getPorts(this).transmit(Markers.PATH, element);
			}
		}
	}
}
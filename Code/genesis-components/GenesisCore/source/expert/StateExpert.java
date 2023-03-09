package expert;


import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class StateExpert extends AbstractWiredBox {

	public StateExpert() {
		super("State expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.STATE_TYPE) && t.relationP()) {
			Relation r = (Relation) t;
			Entity o = r.getObject();
			if (o.isAPrimed(Markers.LOCATION_TYPE) && o.sequenceP()) {
				Sequence s = (Sequence) o;
				for (Entity e : s.getElements()) {
					Connections.getPorts(this).transmit(Markers.DIRECT, e);
				}
			}
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
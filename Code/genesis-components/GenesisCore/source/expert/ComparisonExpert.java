package expert;


import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 23, 2009
 * @author phw
 */

public class ComparisonExpert extends AbstractWiredBox {

	public ComparisonExpert() {
		super("Comparison expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.COMPARISON_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
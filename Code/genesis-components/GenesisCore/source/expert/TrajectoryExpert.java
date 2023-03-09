package expert;

import translator.NewRuleSet;
import utils.*;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class TrajectoryExpert extends AbstractWiredBox {

	public TrajectoryExpert() {
		super("Trajectory expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP() && t.isA(NewRuleSet.trajectoryWords) && t.isNotA(Markers.JOB_TYPE_MARKER)) {
			Relation r = (Relation) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			Connections.getPorts(this).transmit(Markers.PATH, r.getObject());
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
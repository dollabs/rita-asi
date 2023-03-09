package expert;

import utils.Mark;
import connections.*;
import constants.*;
import frames.entities.Entity;
import generator.RoleFrames;

/*
 * Created on Mar 23, 2009
 * @author phw
 */

public class PartExpert extends AbstractWiredBox {

	public PartExpert() {
		super("Part expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering PartExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;

		if (t.relationP(Markers.HAVE)) {
			Entity o = RoleFrames.getObject(t);
			if (o != null && (o.isA("body-part") || o.isA("body-covering"))) {
				Connections.getPorts(this).transmit(Markers.VIEWER, t);
			}
			else {
				Connections.getPorts(this).transmit(Markers.NEXT, t);
			}
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, t);

		}
	}
}
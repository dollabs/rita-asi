package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class DescribeExpert extends AbstractWiredBox {

	public final static String PROCESS = "process";

	public DescribeExpert() {
		super("Describe expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.DESCRIBE_MARKER) && t instanceof Function) {
			Function d = (Function) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, d);
			Connections.getPorts(this).transmit(PROCESS, d.getSubject());
		}
		else {
			// Mark.say("Transmitting from describe expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
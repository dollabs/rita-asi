package expert;

import utils.Mark;

import connections.*;
import constants.Markers;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class AgentExpert extends AbstractWiredBox {

	public AgentExpert() {
		super("Agent expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (Recognizers.agent(object)) {
			Connections.getPorts(this).transmit(Markers.VIEWER, object);
			Connections.getPorts(this).transmit(Markers.LOOP, Recognizers.theObject(object));

		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, object);
		}

	}
}
package frames;

import connections.*;
import frames.entities.Entity;

public class BensGauntletComponent extends AbstractWiredBox {

	public BensGauntletComponent() {
		super("Ben's gauntlet component");
		Connections.getPorts(this).addSignalProcessor("dispatch");
	}

	public void dispatch(Object o) {
		if (o instanceof Entity) {
			Entity t = (Entity) o;
			if (t.isA(ForceFrame.FRAMETYPE)) {
				Connections.getPorts(this).transmit("force", t);
			}
			else if (t.isA(GeometryFrame.FRAMETYPE)) {
				Connections.getPorts(this).transmit("geometry", t);
			}
			else if (t.isA(BlockFrame.FRAMETYPE)) {
				Connections.getPorts(this).transmit("block", t);
			}
			else if (t.isA(TimeFrame.FRAMETYPE)) {
				Connections.getPorts(this).transmit("time", t);
			}
		}
	}
}

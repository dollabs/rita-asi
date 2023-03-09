package expert;

import translator.NewRuleSet;
import utils.Mark;

import connections.*;
import constants.*;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class PlaceExpert extends AbstractWiredBox {

	public PlaceExpert() {
		super("Place expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entering PlaceExpert");
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.functionP() && t.isAPrimed(NewRuleSet.placePrepositions)) {
			Function d = (Function) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, d);
		}
	}
}
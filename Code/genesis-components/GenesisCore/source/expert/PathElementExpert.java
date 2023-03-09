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

public class PathElementExpert extends AbstractWiredBox {

	public PathElementExpert() {
		super("Path element expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		Mark.say(GenesisConstants.FLOW, "Entered path element expert", object);
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.functionP()) {
			Function d = (Function) t;
			if (d.isA(NewRuleSet.pathPrepositions)) {
				Connections.getPorts(this).transmit(Markers.VIEWER, d);
				if (d.getSubject().functionP() && d.getSubject().isAPrimed(NewRuleSet.placePrepositions)) {
					Connections.getPorts(this).transmit(Markers.PATH, d.getSubject());
				}
				else {
					// No longer an error
					// Mark.err("No place indicate with respect to", t);
				}
			}
		}
	}
}
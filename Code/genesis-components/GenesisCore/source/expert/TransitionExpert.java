package expert;

import translator.NewRuleSet;
import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class TransitionExpert extends AbstractWiredBox {

	public TransitionExpert() {
		super("Transition expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.relationP() && t.isAPrimed(NewRuleSet.transitionWords)) {
			Function d = (Function) t;
			Connections.getPorts(this).transmit(Markers.VIEWER, t);
			// Don't send downstream if it is just a thing
			if (!d.getSubject().entityP()) {
				Connections.getPorts(this).transmit(Markers.NEXT, d.getSubject());
			}
		}
		else {

			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}

	}
}
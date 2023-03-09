package mentalModels;


import connections.*;
import frames.entities.Entity;
import frames.entities.Sequence;
import utils.Mark;

/*
 * Used to avoid obsolete IdiomHandler and Anaphora boxes
 */

public class ShortCut extends AbstractWiredBox {
	public ShortCut() {
		super("Short cut");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object o) {
		if (o instanceof Sequence) {
			for (Entity t : ((Sequence) o).getElements()) {
				// Mark.say("Shortcut transmitting", t);
				Connections.getPorts(this).transmit(t);
			}
		}
	}
	}
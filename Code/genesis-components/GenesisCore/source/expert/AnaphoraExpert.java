package expert;

import matchers.Substitutor;
import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

/*
 * Finds anaphoric references in things, derivatives, relations, and sequences
 * @Deprecated
 */
public class AnaphoraExpert extends AbstractWiredBox {

	public AnaphoraExpert() {
		super("Anaphora expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	/*
	 * On second thought, no need to do anything by way of intersentence dereference because START handles that now. But
	 * do need to look for things to send to picture viewer.
	 */
	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		// Mark.say("ANAPHORA 1: " + ((Thing) object).asString());
		// Thing t = Substitutor.dereference((Thing) object);
		Entity t = (Entity) object;
		// Mark.say("Anaphora expert forwarding", t);
		Connections.getPorts(this).transmit(Markers.NEXT, t);
		findObjectsForViewer(t);
		// Mark.say("ANAPHORA 2: " + t.asString());

		// Sam's hack to restore "feature" threads after the Substitutor
		// processes the thing...Sam's hack did not work because the
		// getDescendants method not gauranteed to
		// return items in canonical order. PHW change dereference to handle
		// feature threads
		// t = restoreFeatureThreads((Thing)object, t);
		// Mark.say("ANAPHORA 3: "+t);

		// if (t.isAPrimed(Markers.SEMANTIC_INTERPRETATION) && t.sequenceP()) {
		// // Concepts benefit heavily from receiving the entire package at
		// // once.
		// // added @cdluna, July 23, 2009
		// // Connections.getPorts(this).transmit(Markers.CONCEPT, t);
		// for (Thing element : ((Sequence) t).getElements()) {
		// Connections.getPorts(this).transmit(Markers.NEXT, element);
		// }
		// }
	}

	private void findObjectsForViewer(Entity t) {
		try {
			if (t.entityP()) {
				Connections.getPorts(this).transmit(Markers.VIEWER, t);
			}
			else if (t.functionP()) {
				findObjectsForViewer(t.getSubject());
			}
			else if (t.relationP()) {
				findObjectsForViewer(t.getSubject());
				findObjectsForViewer(t.getObject());
			}
			else if (t.sequenceP()) {
				for (Entity e : t.getElements()) {
					findObjectsForViewer(e);
				}
			}
		}
		catch (Exception e) {
			Mark.err("Harmless view exception");
		}
	}
}

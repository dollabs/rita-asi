/**
 * 
 */
package genesis;

import java.util.*;

import connections.*;
import frames.entities.Entity;

/**
 * Processes 'derived' things, which are a convenient way for Understand to represent thread changes. Also removes
 * undigested 'parse-link' things. For example, Deriver would convert (d "derived trajectory" (r "action ... flew" ...))
 * to (r "action trajectory ... flew" ...).
 * 
 * @author harold
 */
public class UnderstandProcessor extends AbstractWiredBox {
	public static final String DERIVED = "derived";

	UnderstandProcessor() {
		super("Understand processor");
		Connections.getPorts(this).addSignalProcessor("setInput");
	}

	public void setInput(Object o) {
		if (o instanceof Entity) Connections.getPorts(this).transmit(derive((Entity) o));
	}

	public Entity derive(Entity t) {

		if (t.functionP()) {

			if (t.isA("derived")) {
				deriveTypes(t, t.getSubject());
				return derive(t.getSubject());
			}

			t.setSubject(derive(t.getSubject()));
		}

		else if (t.relationP()) {
			t.setSubject(derive(t.getSubject()));
			t.setObject(derive(t.getObject()));
		}

		else if (t.sequenceP()) {

			if (t.isA("derived")) { // gets rid of all but first element
				deriveTypes(t, t.getElement(0));
				return derive(t.getElement(0));
			}

			ListIterator<Entity> kids = t.getElements().listIterator();
			while (kids.hasNext()) {
				Entity kid = kids.next();
				if (kid.isA("parse-link"))
					kids.remove();
				else
					kids.set(derive(kid));
			}
		}

		return t;
	}

	/**
	 * insert everything after DERIVED in derived's thread after the first type in t's thread
	 * 
	 * @param reference
	 * @param derived
	 * @param t
	 */
	public static void deriveTypes(Entity derived, Entity t) {
		deriveTypesAfterReference(t.getPrimedThread().get(0), derived, t);
	}

	/**
	 * insert everything after DERIVED in derived's thread after reference in t's thread
	 * 
	 * @param reference
	 * @param derived
	 * @param t
	 */
	public static void deriveTypesAfterReference(String reference, Entity derived, Entity t) {
		Vector<String> target = t.getPrimedThread();
		for (int i = 0; i < target.size(); i++) { // find reference
			if (reference.equals(target.get(i))) {
				Vector<String> source = derived.getPrimedThread();
				for (int j = 0; j < source.size(); j++) { // find DERIVED
					if (source.get(i).equals(DERIVED)) {
						target.addAll(i + 1, source.subList(j + 1, source.size()));
						return;
					}
				}
			}
		}
	}
}

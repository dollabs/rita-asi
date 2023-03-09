package utils;

import frames.entities.Entity;

/*
 * Created on May 2, 2009
 * @author phw
 */

public class PairOfEntities {
	private Entity pattern;

	private Entity datum;

	public PairOfEntities(Entity pattern, Entity datum) {
		super();
		// System.out.println("Binding " + pattern + " to " + datum);
		this.pattern = pattern;
		this.datum = datum;
	}

	public Entity getDatum() {
		return datum;
	}

	public Entity getPattern() {
		return pattern;
	}

	public boolean equals(Object o) {
		if (o instanceof PairOfEntities) {
			PairOfEntities thatPair = (PairOfEntities) o;
			if (getDatum() == thatPair.getDatum() && getPattern() == thatPair.getPattern()) {
				// Mark.say(true, this, "==", o);
				return true;
			}
		}
		// Mark.say(true, this, "!=", o);
		return false;
	}

	public PairOfEntities reverse() {
		return new PairOfEntities(datum, pattern);
	}

	public String toString() {
		// return "<" + pattern.asStringWithNames() + ", " + datum.asStringWithNames() + ">";
		// return "<" + pattern.getName() + ", " + datum.getName() + ">";
		return "<" + (pattern == null ? "null" : pattern.getName()) + ", " + (datum == null ? "null" : datum.getName()) + ">";
	}
}

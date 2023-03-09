package utils;

/*
 * Created on Jul 17, 2010
 * @author phw
 */

/*
 * Created on May 2, 2009
 * @author phw
 */

public class Pair<F, S> {
	public F first;

	public S second;
	
	public Pair(F first) {
		this (first, null);
	}

	public Pair(F first, S second) {
		super();
		// System.out.println("Binding " + pattern + " to " + datum);
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}

	public boolean equals(Object o) {
		if (o instanceof PairOfEntities) {
			PairOfEntities thatPair = (PairOfEntities) o;
			if (getFirst() == thatPair.getDatum() && getSecond() == thatPair.getPattern()) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return "<" + first.toString() + ", " + second.toString() + ">";
	}
}

package jakeBarnwell.concept;

import java.util.HashMap;

public class Direction {

	public static final Direction SUBJECT = new Direction(-1);
	public static final Direction OBJECT = new Direction(-2);
	
	private static final HashMap<Integer, Direction> elements = new HashMap<>();

	private int ind;
	
	private Direction(int i) {
		ind = i;
	}
	
	public static Direction ELEMENT(int i) {
		assert i >= 0;
		
		if(!elements.containsKey(i)) {
			Direction d = new Direction(i);
			elements.put(i, d);
			return d;
		} else {
			return elements.get(i);
		}
	}
	
	public int getIndex() {
		return ind;
	}
	
	public boolean isElements() {
		return this.ind >= 0;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof Direction)) {
			return false;
		}
		
		Direction o = (Direction)other;
		return this.ind == o.ind;
	}
	
	@Override
	public int hashCode() {
		// The minimum index is -2 so just add some number >= 3 to ensure non-zero.
		return this.ind + 2129;
	}
	
	@Override
	public String toString() {
		if(this == Direction.SUBJECT) {
			return "SUBJ";
		}
		if(this == Direction.OBJECT) {
			return "OBJ";
		}
		return "ELE" + ind;
	}

}

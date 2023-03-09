package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.List;

public class NodeLocation {
	
	// Used to keep track of where we are when "following along" the directions
	private int nextIndex = 0;
	
	// The list of directions TODO make private
	public List<Direction> directions = new ArrayList<>();
	
	public NodeLocation() {
		;
	}
	
	/**
	 * Takes this direction and returns the new (copied) object.
	 * Doesn't modify the original object.
	 * @param d
	 * @return
	 */
	public NodeLocation take(Direction d) {
		NodeLocation copy = this.copy();
		copy.directions.add(d);
		return copy;
	}
	
	/**
	 * Appends another node location at the end of this one, returning
	 * the (copied) object. Doesn't modify the original object.
	 * @param other
	 * @return
	 */
	public NodeLocation append(NodeLocation other) {
		NodeLocation copy = this.copy();
		for(Direction d : other.directions) {
			copy.directions.add(d);
		}
		return this;
	}
	
	/**
	 * Readies this object to be iterated over, i.e. as directions
	 * into a tree.
	 */
	public void ready() {
		nextIndex = 0;
	}
	
	public Direction next() {
		if(nextIndex < directions.size()) {
			nextIndex++;
			return directions.get(nextIndex - 1);
		} else {
			nextIndex = 0;
			return null;
		}
	}
	
	public boolean isEmpty() {
		return directions.size() == 0;
	}
		
	/**
	 * Makes a deep copy of this object.
	 * @return
	 */
	public NodeLocation copy() {
		NodeLocation copy = new NodeLocation();
		for(Direction d : this.directions) {
			copy.directions.add(d);
		}
		return copy;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for(int i = 0; i < directions.size(); i++) {
			hash += (i + 769) * directions.get(i).hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object oth) {
		if(oth == null || !(oth instanceof NodeLocation)) {
			return false;
		}
		
		NodeLocation o = (NodeLocation)oth;
		if(directions.size() != o.directions.size()) {
			return false;
		}
	
		for(int i = 0; i < directions.size(); i++) {
			if(directions.get(i) != o.directions.get(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("<");
		if(directions.size() > 0) {
			for(Direction d : directions) {
				sb.append(d.toString());
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(">");
		return sb.toString();
	}
}

package memory2.datatypes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;
import memory2.M2;
import utils.EntityUtils;

/**
 * Stores a flattened view of a cluster of Things.
 * 
 * A chain represents a "concept" -- a cluster of positive
 * and negative examples (Things).
 * 
 * TODO: find out how hashCode is inherited from ArrayList
 * TODO: make this thread-safe! (that's a lot of work... is it necessary?)
 * 
 * @author Sam Glidden
 *
 */
public class Chain extends ArrayList<DoubleBundle>{
	private static final long serialVersionUID = 2853206012463972242L;
	
//	private List<Thing> posInputs = new ArrayList<Thing>();
//	private List<Thing> negInputs = new ArrayList<Thing>();
	
	private List<Entity> inputs = new ArrayList<Entity>();
	
	// public so that we can modify this directly
//	public Set<Chain> neighbors = new HashSet<Chain>();
	
	public Chain(Entity t) {
		for (Entity el : flattenThing(t)) {
//			System.out.println("current el: "+el);
			DoubleBundle db2 = new DoubleBundle();
			// choose the correct thread
			Thread newThread = null;
			if (el.getThread("thing") != null) {
				newThread = new Thread(el.getThread("thing"));
			}
			else {
				newThread = new Thread(el.getPrimedThread());
			}

			Thread ft = el.getThread("feature");
			if (ft!=null && ft.contains("not")) {
				db2.addNeg(newThread);
			}
			else {
				db2.addPos(newThread);
			}			
			this.add(db2);
		}
//		posInputs.add(t);
		inputs.add(t);
	}
	
//	public void updatePosHistory(Thing t) {
//		posInputs.add(t);
//	}
//	
//	public List<Thing> getPosInputList() {
//		return new ArrayList<Thing>(posInputs);
//	}
//	
//	public void updateNegHistory(Thing t) {
//		negInputs.remove(t);
//		negInputs.add(t);
//	}
//	
//	public List<Thing> getNegInputList() {
//		return new ArrayList<Thing>(negInputs);
//	}
	
	public void updateHistory(Entity t) {
		inputs.add(t);
	}
	
	public List<Entity> getInputList() {
		return new ArrayList<Entity>(inputs);
	}

	// I think this is a depth-first flatten
	public static List<Entity> flattenThing(Entity thing) {
		if (thing == null) {return null;}
		List<Entity> list = new ArrayList<Entity>();
		list.add(thing);
		if (thing instanceof Sequence) {
			Sequence sequence = (Sequence) thing;
			Vector<Entity> v = sequence.getElements();
			for (int i = 0; i < v.size(); ++i) {
				list.addAll(flattenThing(v.elementAt(i)));
			}
		}
		else if (thing instanceof Relation) {
			Relation relation = (Relation) thing;
			list.addAll(flattenThing(relation.getSubject()));
			list.addAll(flattenThing(relation.getObject()));
		}
		else if (thing instanceof Function) {
			Function derivative = (Function) thing;
			list.addAll(flattenThing(derivative.getSubject()));
		}
		return list;
	}
	
	public int getWeight() {
//		return posInputs.size() + negInputs.size();
		return inputs.size();
	}
	
//	/**
//	 * @return Set of neighboring chains. A chain is a neighbor
//	 * if it is a Chain-Distance of 2 away.
//	 */
//	public Set<Chain> getNeighborCopy() {
//		return new HashSet<Chain>(neighbors);
//	}
	
	/**
	 * Returns a copy of the Chain. Things and threads are not cloned,
	 * but everything else is.
	 */
	@Override
	public Chain clone() {
		Chain clone = new Chain(this.inputs.get(0));
		for (int i = 0; i<this.size(); i++) {
			clone.set(i, this.get(i).clone());
		}
		clone.inputs = new ArrayList<Entity>(this.inputs);
//		clone.neighbors = new HashSet<Chain>(this.neighbors);
		return clone;
	}
	
	/**
	 * @param c Chain
	 * @param max int
	 * @return returns the chain-distance between two chains. The chain-distance
	 * is defined as the total number of DoubleBundles that are found in one chain
	 * but not the other.
	 * 
	 * Dist = 0: chains overlap explicitly
	 * Dist = 1: one chain is a subset of the other
	 * Dist = 2+: chains conflict directly or fail to overlap in at least one doublebundle.
	 */
//	public int getChainDistance(Chain c) {
//		int dist = 0;
//		if (c.getRepType() != this.getRepType()) {
//			dist =  c.size() + this.size();
//		}
//		else {
//			dist = distHelper(c, 0, 0);
//		}
////		System.out.println("Dist (" +this+ " " +c+") = "+dist);
//		return dist;
//	}
	
//	private int distHelper(Chain c, int i, int j) {
////		System.out.println("Getting distance, i, j = "+i+", "+j);
//		if (i == this.size()) {
//			return c.size()-j;
//		}
//		else if (j == c.size()) {
//			return this.size()-i;
//		}
//		int dist = this.get(i).getDistance(c.get(j));
////		System.out.println("  distance btw: "+this.get(i).getNegSingle() +" " + c.get(j).getNegSingle()+" is "+dist);
//		if (dist==0) {
//			return distHelper(c, i+1, j+1);
//		}
//		else {
//			return dist + Math.min(distHelper(c, i, j+1), distHelper(c, i+1, j));
//		}
//	}
	
	public boolean overlaps(Chain c2) {
		if (this.size() != c2.size()) {
			return false;
		}
		for (int i=0; i<this.size(); i++) {
			if (this.get(i).getDistance(c2.get(i)) != 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param c Chain
	 * @return returns a new Chain, which is the union of this Chain and the
	 * input Chain.
	 */
	public Chain mergeChain(Chain c) {
		// note: merges the two chains without first removing them from the system;
		// the caller better do removeChain if appropriate first.
		M2.m2assert(this.size()==c.size(), "LLMerger.mergeChains assertion failed: Chain are different sizes!");
		Chain result = this.clone();
		// merge DoubleBundles
		for (int i=0; i<this.size(); i++) {
			DoubleBundle c2db = c.get(i);
			DoubleBundle resultdb = result.get(i);
			for (Thread t : c2db.getPosSet()) {
				resultdb.addPos(t);
			}
			for (Thread t : c2db.getNegSet()) {
				resultdb.addNeg(t);
			}
		}
		// merge thing cluster
//		for (Thing t: c.getPosInputList()) {
//			result.updatePosHistory(t);
//		}
//		for (Thing t: c.getNegInputList()) {
//			result.updateNegHistory(t);
//		}
		for (Entity t: c.getInputList()) {
			result.updateHistory(t);
		}
		
		// merge neighbor list
//		result.neighbors.addAll(c.getNeighborCopy());
		
		return result;
	}
	
	/**
	 * @return String rep type of this Chain
	 */
	public String getRepType() {
		return (String) EntityUtils.getRepType(this.getInputList().get(0));
	}
}
	
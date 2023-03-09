package memory2.datatypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Thread;
import memory2.lattice.FasterLLConcept;
import memory2.LLCode;


/**
 * A link in a Chain. Has a set for positive examples, and a set
 * for negative examples.
 * 
 * Depends pretty heavily on LL* code. 
 * 
 * @author Sam Glidden
 *
 */
public class DoubleBundle {
	private Set<Thread> posSet = new HashSet<Thread>();
	private Set<Thread> negSet = new HashSet<Thread>();
	
	// does the LL* stuff -- ask Mike Klein
	private FasterLLConcept<String> llcon;
	
	@Override
	public String toString() {
		String s = "";
		for (Thread t: posSet) {
			s+="/"+t.lastElement();
		}
		for (Thread t: negSet) {
			s+="/!"+t.lastElement();
		}
		s=s.substring(1, s.length());
		return s;
	}
	
	/**
	 * @return only Thread in positive Set.
	 * Useful when we are making a chain for a new Thing
	 */
	public Thread getPosSingle() {
		if (posSet.size()==1) {
			List<Thread> tempList = new ArrayList<Thread>(posSet);
			return tempList.get(0);
		}
		return null;
	}
	
	public Thread getNegSingle() {
		if (negSet.size()==1) {
			List<Thread> tempList = new ArrayList<Thread>(negSet);
			return tempList.get(0);
		}
		return null;
	}
	
	// TODO: potential optimization: more fine-grained update of llcon below
	
	/**
	 * @param t Thread adds as a positive example
	 */
	public void addPos(Thread t) {
		// add thread and regenerate LLConcept
		posSet.add(t);
		llcon = LLCode.getLLConcept(posSet, negSet);
	}
	
	/**
	 * @param t Thread -- adds as a negative example
	 */
	public void addNeg(Thread t) {
		// add thread and regenerate LLConcept
		negSet.add(t);
		llcon = LLCode.getLLConcept(posSet, negSet);
	}
	
	/**
	 * @param instanceCounter String
	 * @return 	 
	 * * 0 if hard miss,
	 * 1 if soft miss,
	 * 2 if match
	 */
	public int matches(Thread t) {
//		if (this.negSet.isEmpty()) {
//			System.out.println("********* LL HACK! **************");
//			if (LLCode.LLContains(llcon, t)) {
//				return 2;
//			}
//			return 1;
//		}
//		else {
			if (this.negSet.contains(t)) {
				return 0;
			}
			if (LLCode.LLSearch(llcon, t)) {
				return 2;
			}
			return 1;
//		}
	}
	
	public boolean containsPos(Thread t) {
		return posSet.contains(t);
	}
	
	public boolean containsNeg(Thread t) {
		return negSet.contains(t);
	}
	
	public Set<Thread> getPosSet() {
		return new HashSet<Thread>(posSet);
	}
	
	public Set<Thread> getNegSet() {
		return new HashSet<Thread>(negSet);
	}
	
//	public Thread getNegSingleton() {
//		if (negSet.size()==1) {
//			List<Thread> tempList = new ArrayList<Thread>(negSet);
//			return tempList.get(0);
//		}
//		return null;
//	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((negSet == null) ? 0 : negSet.hashCode());
		result = PRIME * result + ((posSet == null) ? 0 : posSet.hashCode());
		return result;
	}
	
	/* 
	 * Equal if the two DoubleBundles have the same negSet and posSet
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DoubleBundle other = (DoubleBundle) obj;
		if (negSet == null) {
			if (other.negSet != null)
				return false;
		} else if (!negSet.equals(other.negSet))
			return false;
		if (posSet == null) {
			if (other.posSet != null)
				return false;
		} else if (!posSet.equals(other.posSet))
			return false;
		return true;
	}

	// Doesn't clone threads.
	@Override
	public DoubleBundle clone() {
		DoubleBundle clone = new DoubleBundle();
		clone.posSet = new HashSet<Thread>(this.posSet);
		clone.negSet = new HashSet<Thread>(this.negSet);
		clone.llcon = LLCode.getLLConcept(posSet, negSet);
		return clone;
	}

	
	/**
	 * @param db2 DoubleBundle
	 * @return distance between these two DoubleBundles
	 * 0 -> explicit overlapping
	 * 1 -> soft miss
	 * 2 -> hard miss! direct conflict
	 */
	public int getDistance(DoubleBundle db2) {
		// pos1 intersection neg2 == 0
		for (Thread other : db2.negSet) {
			if(this.posSet.contains(other))
				return 2;
		}
		// pos2 intersection neg1 == 0
		for (Thread other : db2.posSet) {
			if(this.negSet.contains(other))
				return 2;
		}
		// pos1 intersection pos2 > 0
		for (Thread other: db2.posSet) {
			if (this.posSet.contains(other))
				return 0;
		}
		// neg1 intersection neg2 > 0
		for (Thread other: db2.negSet) {
			if (this.negSet.contains(other))
				return 0;
		}
		return 1;
	}
	
}
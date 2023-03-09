package memory.utilities;
import java.util.List;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;
import memory2.datatypes.Chain;
import memory2.datatypes.DoubleBundle;
import utils.EntityUtils;
/**
 * Provides several different static comparison utilities
 * 
 * @author sglidden
 *
 */
public class SubSetUtils {
	
	/**
	 * Returns true if all the elements (children Things and Threads)
	 * in subThing are present in their respective location in superThing.
	 * Partially complete threads match successfully. <p>
	 * 
	 * At a base case, if subThing and superThing are equal, then this
	 * function returns true. If one then adds elements to superThing 
	 * or removes them from subThing, the function continues to return 
	 * true. However, if subThing contains anything that superThing 
	 * does not, then false is returned.
	 * 
	 * @param subThing Thing
	 * @param superThing Thing
	 * @returns true is subThing is equal to all or a part of superThing,
	 * false otherwise.
	 */
	public static boolean isSubSet(Entity subThing, Entity superThing) {
		if (subThing==null && superThing==null) return true;
		else if (subThing==null || superThing==null) return false;
		if (isSubSet(subThing.getBundle(), superThing.getBundle())) {
			return checkChildren(subThing, superThing);
		}
		return false;
	}
	
	private static boolean checkChildren(Entity subThing, Entity superThing) {
		List<Entity> c1 = EntityUtils.getOrderedChildren(subThing);
		List<Entity> c2 = EntityUtils.getOrderedChildren(superThing);
		for (int i=0; i<c1.size(); i++) {
			if (!isSubSet(c1.get(i).getBundle(), c2.get(i).getBundle())) {
				return false;
			}
			else {
				boolean check = checkChildren(c1.get(i), c2.get(i));
				if (!check) return false;
			}
		}
		return true;
	}
	
	
	/**
	 * @param subBundle Bundle
	 * @param superBundle Bundle
	 * @return true if every Thread in subBundle isSubSet(...) of a Thread
	 * in superBundle; false otherwise.
	 */
	public static boolean isSubSet(Bundle subBundle, Bundle superBundle) {
		if (subBundle==null && superBundle==null) return true;
		else if (subBundle==null || superBundle==null) return false;
		Bundle temp1 = (Bundle) subBundle.clone();
		for (Thread t1 : subBundle) {
			if (!temp1.contains(t1)) continue;
			for (Thread t2 : superBundle) {
				if (isSubSet(t1, t2)) {
					temp1.remove(t1);
				}
			}
		}
		if (temp1.isEmpty()) return true;
		return false;
	}
	/**
	 * @param subThread Thread
	 * @param superThread Thread
	 * @return true if superThread contains all of subThread's elements in
	 * the appropriate order (gaps allowed); false otherwise.
	 */
	public static boolean isSubSet(Thread subThread, Thread superThread) {
		if (subThread==null && superThread==null) return true;
		else if (subThread==null || superThread==null) return false;
		Thread temp = new Thread(subThread);
		Thread temp2 = new Thread(superThread);
		for (String s : subThread) {
			for (String s2: superThread) {
				if (!temp.contains(s) || !temp2.contains(s2)) {
					continue;
				}
				if (s.equals(s2)) {
					temp2.remove(s2);
					temp.remove(s);
				}
				else {
					temp2.remove(s2);
				}
			}
		}
		if (temp.isEmpty()) return true;
		return false;
	}
	
	public static boolean isChainSubSet(Entity subThing, Entity superThing) {
		Chain c1 = new Chain(superThing);
		Chain c2 = new Chain(subThing);
		for (int i=0; i<c2.size(); i++) {
//			System.out.println("Checking: "+c1.get(i)+" "+c2.get(i));
			if (c1.get(i).getDistance(c2.get(i)) > 0) {
				return false;
			}
		}
		return true;
	}
}

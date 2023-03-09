package jakeBarnwell;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import frames.entities.Thread;

public class Tools {
	
	public static boolean safeEquals(Object a, Object b) {
		if(a == null && b == null) {
			return true;
		} else if(a == null || b == null) {
			return false;
		} else {
			return a.equals(b);
		}
	}
	
	public static int safeHashCode(Object a) {
		if(a == null) {
			return 0;
		}
		
		return a.hashCode();
	}
	
	public static <T> int listHashCode(List<T> li) {
		if(li == null) {
			return 0;
		}
		
		int hash = 1;
		for(int i = 0; i < li.size(); i++) {
			T ele = li.get(i);
			hash = 31 * hash * (i + 71) + (ele == null ? 0 : ele.hashCode());
		}
		
		return hash;
	}
	
	/**
	 * The types T and E must be hashable!
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T, E> boolean unorderedEquals(List<T> a, List<E> b) {
		if(a == null && b == null) {
			return true;
		} else if(a == null || b == null) {
			return false;
		}
		
		if(a.size() != b.size()) {
			return false;
		}
		
		return new HashSet<>(a).equals(new HashSet<>(b));
	}
	
	public static <T> HashSet<T> unorderedIntersection(List<T> a, List<T> b) {
		if(a == null || b == null) {
			return new HashSet<T>();
		}
		
		HashSet<T> ha = new HashSet<T>(a);
		ha.retainAll(b);

		return ha;
	}
	
	public static Thread subThread(Thread t, int start, int end) {
		return Thread.constructThread(new Vector<String>(t.subList(start, end)));
	}

}

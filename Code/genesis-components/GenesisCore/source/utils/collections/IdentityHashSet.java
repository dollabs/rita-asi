/*
 * Created on Mar 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package utils.collections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * @author Keith
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IdentityHashSet<T> extends AbstractSet<T> {
	protected IdentityHashMap<T, Object> backing;
	public static final Object DUMMY = new Object();

	public IdentityHashSet() { backing = new IdentityHashMap<T, Object>();	}
	public IdentityHashSet(Collection<? extends T> c) {
		this();
		addAll(c);
	}

	public Iterator<T> iterator() { return backing.keySet().iterator(); }
	public int size() {	return backing.size(); }
	public void clear() { backing.clear();	}
	public boolean isEmpty() { return backing.isEmpty(); }
	public boolean add(T o) { return (backing.put(o, DUMMY) == null); }
	public boolean contains(Object o) {	return backing.containsKey(o); }
	public boolean remove(Object o) { return (backing.remove(o) != null); }
	public int hashCode() {	return backing.hashCode();	}
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof IdentityHashSet)) {
			return false;
		}
		IdentityHashSet other = (IdentityHashSet) arg0;
		return this.backing.equals(other.backing);
	}
	
}

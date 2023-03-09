/* filename: ExclusiveSet.java
 * format: Java v.1.4.2
 * author: M.A. Finlayson
 * date created: Aug 7, 2003
 */
 
package utils.collections;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
 
/**
 * Wraps a Set, adding a restriction on membership to a specified class. 
 * This class is a wrapper for classes which implement the interface Set.
 * It restricts the membership of the supplied set to objects of a class 
 * type specified at creation.  Objects added to the class, either by the
 * add() or addAll() methods, must be cast directly as objects of the 
 * required type; they cannot be passed cast as more general objects than
 * the class restriction.
 * 
 * It adds one new method, getType(), which returns a Class object 
 * corresponding to the class restriction for the set.
 * 
 * <b>Future improvements:</b> Support multiple class types passed by 
 * a collection.
 * 
 * @see java.util.Set
 * 
 * @author M. A. Finlayson
 * @since JDK 1.4
 * @version 2.0
 */
public class ExclusiveSet<T> 
			extends AbstractSet<T> 
			implements Set<T>, Cloneable, Serializable, ExclusiveCollection<T> {
	
	/**  Set for which this class is a wrapper. */
	private final Set<T> set;
	
	/** Returns the set which is wrapped by this ExclusiveCollection Set. */
	protected Set<T> getWrappedSet(){return set;}
	
	/**  Class to which members of the set are restricted. */
	private Class type;
	
	/** Indicates if the set is restricted only to the specific class,
	 * or the class family (i.e., the class and all its descendants).
	 * Default is STRICT. */
	private final int restriction;
	
	/**  Constructs an ExclusiveSet.
	 * @param s Class implementing the interface Set. 
	 * @param t Class type indicating membership restriction of the 
	 * ExclusiveSet. 
	 */
	public ExclusiveSet(Set<T> s, Class t){
		set = s;
		type = t;
		restriction = STRICT;
	}
	
	public ExclusiveSet(Set<T> s, Class t, int r){
		set = s;
		type = t;
		if(r == STRICT || r == FAMILY){
			restriction = r;
		} else {
			throw new RuntimeException("Restriction " + r + " not valid.");
		}
	}

    public boolean testType(Object element){
    	if(restriction == STRICT){
    		return (element.getClass() == getType());
    	} else if (restriction == FAMILY) {
    		return getType().isInstance(element);
    	}
    	return false;
    }

    /**
     * Returns a Class object representing the class restriction on the
     * ExclusiveSet.
     */
    public Class getType(){
    	return type;
    }
    
    public int getRestriction(){
        return restriction;
    }
	
    public boolean equals(Object o){
        return super.equals(o);
    }
	// Overridden basic operations:
	public int size(){return set.size();}
	public boolean isEmpty(){return set.isEmpty();}
	public boolean contains(Object element){return set.contains(element);}
	
	/** Adds the specified element to the set.
	 * If the element does not match the class type restriction on the set,
	 * it throws a RuntimeException.
	 * 
	 * @throws RuntimeException
	 */
	public boolean add(T element){
		if(testType(element)){
			return set.add(element);
		} else {
			throw new RuntimeException(
					"Tried to add object of type \"" + 
					element.getClass().getName() + 
					"\" to a RestrictedMembershipClass " +
					"with type restriction \"" + 
					type.getName() + "\".");
		}
	}
	
	public boolean remove(Object element){return set.remove(element);}
	
	public Iterator<T> iterator(){return set.iterator();}
	
	// Overridden bulk operations:
	public boolean containsAll(Collection c){return set.containsAll(c);}
	
	/**
	 * Adds the elements in the specified collection to the set.
	 * If any element in the collection does not match the class type
	 * restriction for the set, the method throws a RuntimeException.
	 * @throws RuntimeException
	 */
	@Override
	public boolean addAll(Collection<? extends T> c){
		for(Iterator i = c.iterator(); i.hasNext();){
			Object object = i.next();
			if(!testType(object)){
				throw new RuntimeException(
						"Tried to add collection " + 
						"containing object of type \"" + 
						object.getClass().getName() + 
						"\" to a " + 
						"RestrictedMembershipClass " + 
						"with type restriction \"" + 
						type.getName() + "\"");
			}
		}
		return set.addAll(c);
	}
	public boolean removeAll(Collection c){return set.removeAll(c);}
	public boolean retainAll(Collection c){return set.retainAll(c);} 
	public void clear(){set.clear();}                       

	// Overridden array operations:
	public Object[] toArray(){return set.toArray();}
	public <T2> T2[] toArray(T2[] a){return set.toArray(a);}

}

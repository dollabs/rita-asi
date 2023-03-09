package utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;


/** Random utilities for manipulating Collections.
 * @author M.A. Finlayson
 * @since Jan 15, 2005; JDK 1.4.2_06
 */
public class CollectionUtils {
    
    public static <T> Set<T> union(Collection<? extends T> A, Collection<? extends T> B){
        Set<T> result = new HashSet<T>();
        result.addAll(A);
        result.addAll(B);
        return result;
    }
    
    public static <T> Set<T> union(Collection<? extends T> A, Collection<? extends T> B, Collection<? extends T> C){
        Set<T> result = new HashSet<T>();
        result.addAll(A);
        result.addAll(B);
        result.addAll(C);
        return result;
    }
    
    public static <T> Set<T> union(Collection<? extends T> A, Collection<? extends T> B, Collection<? extends T> C, Collection<? extends T> D){
        Set<T> result = new HashSet<T>();
        result.addAll(A);
        result.addAll(B);
        result.addAll(C);
        result.addAll(D);        
        return result;
    }

    
    public static <T> Set<T> intersection(Collection<? extends T> A, Collection<? extends T> B){
        Set<T> result = new HashSet<T>();
        result.addAll(A);
        result.retainAll(B);
        return result;
    }
    
    /** Returns A - B
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public static <T> Collection<T> difference(Collection<? extends T> A, Collection<? extends T> B){
        Set<T> result = new HashSet<T>();
        result.addAll(A);
        Collection<? extends T> intersection = intersection(A, B);
        result.removeAll(intersection);
        return result;
    }
    
    public static <T> boolean isIntersectionEmpty(Collection<? extends T> A, Collection<? extends T> B){
        Object element;
        for(Iterator<? extends T> i = A.iterator(); i.hasNext(); ){
            element = i.next();
            if(B.contains(element)){return false;}
        }
        return true;
    }
    
    public static <T> boolean isIntersectionNonempty(Collection<? extends T> A, Collection<? extends T> B){
        return !isIntersectionEmpty(A, B);
    }
    
    public static String getPrintString(Collection c){
        return "[" + StringUtils.join(c, ", ") + "]";
    }
    
    public static Object get(Collection c, int x) {
        if (c instanceof List) {
            return ((List) c).get(x);
        } else {
            Iterator iC = c.iterator();
            for (int i=0; i < x-1; i++) iC.next();
            return iC.next();
        }
    }
    
    /**
     * Returns the first element of any collection
     * 
     */
    public static <T> T getFirstElement(Collection<T> c){
        Iterator<T> i = c.iterator(); 
        if (i.hasNext()) {
            return i.next();
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * Removes and returns the first element of any collection
     * 
     */
    public static <T> T removeFirstElement(Collection<T> c){
        Iterator<T> i = c.iterator(); 
        if (i.hasNext()) {
            T o = i.next();
            i.remove();
            return o;
        } else {
            throw new NoSuchElementException();
        }
    }    
    
    public static <T> T getFirstElement(List<T> l) {
        if (l.isEmpty()) throw new NoSuchElementException();
        if (l instanceof LinkedList) return ((LinkedList<T>) l).getFirst();
        return l.get(0);
    }
    
    public static <T> T getLastElement(List<T> l) {
        if (l.isEmpty()) throw new NoSuchElementException();
        if (l instanceof LinkedList) return ((LinkedList<T>) l).getLast();
        return l.get(l.size()-1);
    }
    
    protected static class SizeLimitedList<T> implements List<T> {
    	List<T> backing;
    	int sizeLimit;
    	
    	public SizeLimitedList(List<T> backing, int sizeLimit) {
    		this.backing = backing;
    		this.sizeLimit = sizeLimit;
		}

		public void add(int index, T element) {
			if (backing.size() >= sizeLimit) return;
			backing.add(index, element);
		}

		public boolean add(T o) {
			if (backing.size() >= sizeLimit) return false;
			return backing.add(o);
		}

		public boolean addAll(Collection<? extends T> c) {
			return addAll(size(), c);
		}

		public boolean addAll(int index, Collection<? extends T> c) {
			Iterator<? extends T> iC = c.iterator();
			while (iC.hasNext()) {
				T e = iC.next();
				add(index, e);
				index++;
			}
			return true;
		}

		public void clear() {
			backing.clear();
		}

		public boolean contains(Object o) {
			return backing.contains(o);
		}

		public boolean containsAll(Collection c) {
			return backing.containsAll(c);
		}

		public boolean equals(Object o) {
			return backing.equals(o);
		}

		public T get(int index) {
			return backing.get(index);
		}

		public int hashCode() {
			return backing.hashCode();
		}

		public int indexOf(Object o) {
			return backing.indexOf(o);
		}

		public boolean isEmpty() {
			return backing.isEmpty();
		}

		public Iterator<T> iterator() {
			return backing.iterator();
		}

		public int lastIndexOf(Object o) {
			return backing.lastIndexOf(o);
		}

		public ListIterator<T> listIterator() {
			return backing.listIterator();
		}

		public ListIterator<T> listIterator(int index) {
			return backing.listIterator(index);
		}

		public T remove(int index) {
			return backing.remove(index);
		}

		public boolean remove(Object o) {
			return backing.remove(o);
		}

		public boolean removeAll(Collection c) {
			return backing.removeAll(c);
		}

		public boolean retainAll(Collection c) {
			return backing.retainAll(c);
		}

		public T set(int index, T element) {
			return backing.set(index, element);
		}

		public int size() {
			return backing.size();
		}

		public List<T> subList(int fromIndex, int toIndex) {
			return backing.subList(fromIndex, toIndex);
		}

		public Object[] toArray() {
			return backing.toArray();
		}

		public <T2> T2[] toArray(T2[] a) {
			return backing.toArray(a);
		}
    }
    protected static class RandomAccessSizeLimitedList<T> extends SizeLimitedList<T> implements RandomAccess {
		public RandomAccessSizeLimitedList(List<T> backing, int sizeLimit) {
			super(backing, sizeLimit);
		}
    }
    
	public static <T> List<T> sizeLimited(List<T> list, int sizeLimit) {
		if (list instanceof RandomAccess) {
			return new RandomAccessSizeLimitedList<T>(list, sizeLimit);
		} else {
			return new SizeLimitedList<T>(list, sizeLimit);
		}
	}
    

    

}

package memory.soms;
import java.util.List;
import java.util.Set;
import memory.multithreading.Watcher;
public interface Som<E> {
	/**
	 * @return Set of all elements in the memory.
	 */
	public abstract Set<E> getMemory();
	
	/**
	 * Observer Design Pattern:
	 * 
	 * Adds an implementation of class Watcher to the SOM.
	 * Watcher.ping() should be called to notify the rest
	 * of the system that the SOM has changed.
	 * 
	 * @param w Watcher
	 */
	public abstract void add(Watcher w);
	
	
	/**
	 * Inserts a new element into the map.
	 * 
	 * @param e E
	 */
	public abstract void add(E e);
	/**
	 * Returns all neighbors of a element.
	 * 
	 * @param e E to get neighbors of
	 * @return Set of neighboring E
	 */
	public abstract Set<E> neighbors(E e);
	
	/**
	 * Returns the n nearest elements to Element e in the SOM.
	 * 
	 * @param e Element
	 * @param n integer
	 * @return List of nearest elements, sorted by proximity
	 */
	//public abstract List<E> getNearest(E e, int n);
	
	
	/**
	 * Creates a shallow clone of the self-organizing map, were "shallow"
	 * means all structure of the map is cloned, but the contents are not.
	 * 
	 * @return Som
	 */
	public Som<E> clone();
	
	
	
	
	/**
	 * @param e E
	 * @return true if the SOM contains an element zero distance 
	 * away from e
	 */
	public boolean containsEquivalent(E e);
	
	
	/**
	 * @param e1 E
	 * @param e2 E
	 * @return the SOM distance between two elements
	 */
	public double getDistance(E e1, E e2);
}

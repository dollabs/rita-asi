package memory.soms;
/**
 * Provides the self-organizing maps with a distance metric for elements.
 * 
 * @author sglidden
 *
 * @param <T> compatible elements
 */
public interface DistanceMetric<T> {
	
	/**
	 * Returns a distance between two elements.
	 * 
	 * @param e1
	 * @param e2
	 * @return double distance.
	 */
	public double distance(T e1, T e2);
	
}

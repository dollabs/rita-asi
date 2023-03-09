package memory.soms;
import java.util.Set;
/**
 * Provides the self-organizing maps with a way to merge
 * similar elements together.
 * 
 * @author sglidden
 *
 * @param <T>
 */
public interface ElementMerger<T> {
	
	/**
	 * Returns a Set of elements created from the 
	 * fusion of seed and neighbors.
	 * 
	 * @param seed
	 * @param neighbors
	 * @return Set of new elements.
	 */
	public Set<T> merge(T seed, Set<T> neighbors);
}

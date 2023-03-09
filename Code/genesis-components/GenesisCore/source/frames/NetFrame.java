package frames;
import java.util.HashSet;

import utils.graphs.DirectedMultiGraph;
/**
 * A frame for storing relational data between things.<br>
 * Example:<br>
 * Field is Flat<br>
 * Pancake is Flat<br>
 * Tree has Leaves<br>
 * 
 * Note: is-a relations are stored in threads, NOT here.
 * <p>
 * We don't currently have any way to handle qualifiers.
 * We can say that "running is fast", but we can't record
 * that "running is faster than walking".
 * <p>
 * This implementation is parameterized. So, if you want
 * to use it with Things, for Bridge or Gauntlet, you need 
 * to parametize it (specify the "T").
 * 
 * 
 * @author Sam Glidden
 * @modified 14 Nov 2006
 *
 */
public class NetFrame<T> {
	
	// an enumeration of possible relations
	// not really necessary, here for clarity
	private enum Relation {is, has}
	
	
	private DirectedMultiGraph<T, Relation> isNet;
	private DirectedMultiGraph<T, Relation> hasNet;
	
	public NetFrame() {
		isNet = new DirectedMultiGraph<T, Relation>();
		hasNet = new DirectedMultiGraph<T, Relation>();
	}
	
	/**
	 * Adds a simple "has" relation between the subject and 
	 * object: Subject has Object
	 * 
	 * @param subject: T
	 * @param object: T
	 */
	public void addHas(T subject, T object) {
		hasNet.addEdge(subject, object, Relation.has);
	}
	
	/**
	 * Adds an "is" relation. NOTE: this is the "is" of
	 * a charactistic, not of a subcategory -- i.e. this is
	 * not "is-a". Therefore, specifying that the "pancake
	 * is flat" is appropiate, while saying the "dog is 
	 * animal" is NOT.
	 * 
	 * @param subject: T
	 * @param object: T
	 */
	public void addIs(T subject, T object) {
		isNet.addEdge(subject, object, Relation.is);
	}
	
	/**
	 * Gets a HashSet of all objects that the provided
	 * subject "has".
	 * 
	 * @param: subject: T, the "haver"
	 * @return: HashSet<T> of what the haver "has"
	 */
	public HashSet<T> getHas(T subject) {
		return hasNet.getSuccessors(subject);
	}
	
	/**
	 * Returns the charactistics of a subject, that is, 
	 * what the subject "is".
	 * 
	 * @param subject: T
	 * @return: HashSet<T> of what the subject is
	 */
	public HashSet<T> getIs(T subject) {
		return isNet.getSuccessors(subject);
	}
	
	/**
	 * Returns a HashSet of what subjects have an
	 * "is" relation to the provided object.
	 * Example: returns {"pancake", "field"} when asked
	 * what is "flat".
	 * 
	 * @param object: T
	 * @return: HashSet of T that "is" the object
	 */
	public HashSet<T> getWhatIs(T object) {
		return isNet.getPredecessors(object);
	} 
	
	/**
	 * Returns a HashSet of all subjects that
	 * "has" the provided object. Example: returns
	 * {"tree"} when given "leaves".
	 * 
	 * @param object: T
	 * @return HashSet of all T that "has" object
	 */
	public HashSet<T> getWhatHas(T object) {
		return hasNet.getPredecessors(object);
	} 
	
}

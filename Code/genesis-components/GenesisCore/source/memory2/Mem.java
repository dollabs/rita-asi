package memory2;

import java.util.List;
import java.util.Set;

import connections.WiredBox;
import frames.entities.Entity;

/**
 * <p>Provides the abstraction layer to the memory:
 * one set of functions for interaction.
 * 
 * <p>Use M2.getMem() from anywhere in Gauntlet to use the
 * memory.
 * 
 * <p><b>Some examples:</b></p>
 * 
 * <br>	Thing t1, t2, t3;					// some arbitrary things
 * <br>	Mem mem = M2.getMem();				// static getter for the singleton Memory
 * <br>	List<Thing> neighbors = mem.neighbors(t1);		// neighbors are Things close to t1
 * <br>	boolean possible = mem.isPossible(t2);			// true if the memory believes that t2 could happen
 * <br>	Set<Thing> context = mem.getContext(t3);		// context is the set of Things which contain t3
 * 	
 * 
 * @author sglidden
 *
 */

public interface Mem extends WiredBox {

	/**
	 * Inputs t to the memory for storage and processing.
	 * 
	 * @param t Thing
	 */
	public void input(Entity t);
	
	/**
	 * @param t Thing
	 * @return int number times a Thing equivalent to
	 * t has been stored in memory
	 */
	public int frequency(Entity t);
	
	/**
	 * @param t Thing
	 * @return List of Things, sorted by Chain-distance to t, most similar
	 * first.
	 */
	public List<Entity> neighbors(Entity t);

	/**
	 * @param t Thing
	 * @return List of Things, sorted by Chain-distance to t, most similar
	 * first. Only returns Things that are zero distance from t.
	 * (i.e. are in the same cluster in LLMerger)
	 */
	public List<Entity> nearNeighbors(Entity t);
	
	
	/**
	 * @param t Thing
	 * @return the Chain-distance from t to the nearest Thing in the
	 * memory to t. 
	 */
	public int getMissDistance(Entity t);
	
	/**
	 * @param t Thing
	 * @return boolean - true if the Memory contains something with
	 * an Chain-distance of zero from the given t.
	 */
	public boolean isPossible(Entity t);
	
	/**
	 * @param t Thing
	 * @return Set of Things that contain a Thing equivalent to t.
	 * (I ignore the uniquifying numbers, and just look at Threads.)
	 */
	public Set<Entity> getContext(Entity t);
	
	
	/**
	 * @param t Thing where t.thingP is true.
	 * @param repType Object, from RecognizedRepresentations.java
	 * @return The latest Thing in memory that is the matching representation and
	 * contains t. If there's no match, we return null.
	 */
	public Entity getMostRecentRep(Entity t, Object repType);
	
	// tells memory output everything
	public void outputAll();
	
}

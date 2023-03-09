package memory;

import java.util.List;

import frames.entities.Entity;

/**
 * NOT YET IN USE: see Memory.java for now
 * 
 * An interface defining what the rest of Gauntlet expects the Memory system
 * to be like. Basically, outside subcomponents should only access the memory
 * through some implementing class of MemHub.
 * 
 * @author sglidden
 *
 */
/**
 * @author sglidden
 *
 */
public interface MemHub {
	
	/**
	 * @return a reference to the Memory Hub
	 */
	public MemHub getMemHub();
	
	
	/*
	 *  MUTATORS
	 */
	
	/**
	 * Adds a Thing to memory. The memory will take care of storing it
	 * internally however it wants, including placing a copy into the 
	 * self-organizing maps.
	 * 
	 * @param t Thing
	 */
	public void add(Entity t);
	
	
	/**
	 * Instructs the memory to remove all references to T. Any secondary
	 * usages of T (e.g. copies placed into self-organizing maps) are NOT
	 * removed, because they are inherently part of the overall set of 
	 * knowledge now.
	 * 
	 * @param t Thing
	 */
	public void remove(Entity t);
	
	
	/**
	 * Wipes the memory clean. I can't imagine why you'd want to do this.
	 */
	public void reset();
	
	
	/*
	 * ACCESSORS
	 */
	
	/**
	 * Returns a List of elements similar to a given Thing t.
	 * The search Thing t is appended to the beginning of the List.
	 * 
	 * @param t Thing
	 * @return List of Things similar to T, as defined by the self-
	 * organizing maps. They are ordered by distance, least to greatest.
	 */
	public List<Entity> getNeighbors(Entity t);
	
	
	/**
	 * Returns a List of at least the n nearest elements to a given Thing t.
	 * The search Thing t is appended to the beginning of the List.
	 * 
	 * @param t Thing
	 * @param n integer or requested neighbors
	 * @return List of nearest neighbors to t, sorted by proximity
	 */
	public List<Entity> getNearestNeighbors(Entity t, int n);
	
	
	/**
	 * Checks to see if the Memory is storing a Thing t.
	 * 
	 * @param t Thing
	 * @return true or false
	 */
	public boolean contains(Entity t);
	
	
	/**
	 * Returns the frequency with which a Thing is found in the memory.
	 * Returns max(# of times the Thing has been added to memory,
	 * 				# of times the Thing is found in the SOMs)
	 * 
	 * @param t Thing
	 * @return int frequency
	 */
	public int getFrequency(Entity t);
	
	
	/**
	 * Predicts what would reasonably follow the given Thing
	 * t.
	 * 
	 * @param t Thing
	 * @return ordered list of expected predictions
	 */
	public List<Entity> makePredictions(Entity t);
	
	/**
	 * Returns true if the memory expects t1 and t2 to causally or
	 * temporally related in some way.
	 * 
	 * @param t1 Thing
	 * @param t2 Thing
	 * @return true or false
	 */
	public boolean checkCorrespondence(Entity t1, Entity t2);
	
	
	/**
	 * Returns an ordered list of descriptions of representations that
	 * describe Thing t. The list is ordered by frequency of occurrence
	 * in the memory.
	 * 
	 * @param t Thing
	 * @return List of Things that contain t
	 */
	public List<Entity> getDescription(Entity t);
	

}

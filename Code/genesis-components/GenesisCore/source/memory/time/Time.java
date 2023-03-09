package memory.time;
import frames.entities.Entity;
/**
 * This interface is for handling time relationships
 * between elements in the memory's self-organizing maps.
 * 
 * @author sglidden
 *
 */
public interface Time {
	
	/*
	 * Methods for adding relations
	 */
	
	/**
	 * add relation of A ending before B begins
	 */
	public void addBefore(Entity a, Entity b);
	
	/**
	 * add relation of B beginning when A ends
	 */
	public void addMeets(Entity a, Entity b);
	
	/**
	 * add relation of A and B starting at the same time
	 */
	public void addStarts(Entity a, Entity b);
	
	/**
	 * add relation of A and B finishing at the same time
	 */
	public void addFinishes(Entity a, Entity b);
	
	/**
	 * add relation where B starts before A ends
	 */
	public void addOverlaps(Entity a, Entity b);
	
	/**
	 * add relation where A occurs entirely while B is occuring
	 */
	public void addDuring(Entity sub, Entity sup);
	
	/**
	 * add relation where A and B start start and end at equals points in time
	 */
	public void addEquals(Entity a, Entity b);
	
	
	
	/*
	 * Methods for retrieving relations
	 */
	
	/**
	 * Returns a list of the relations between A and B.
	 * 
	 * @param a
	 * @param b
	 * @return TimeRelation
	 */
	public TimeRelation getRelation(Entity a, Entity b);
	
	
}

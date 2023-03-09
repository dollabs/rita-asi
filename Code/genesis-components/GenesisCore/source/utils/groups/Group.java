package utils.groups;
import java.util.AbstractCollection;
import java.util.List;

import frames.entities.Entity;
import frames.entities.Relation;
/**
 * Interface for groups of Things with multiple relations between them.
 * @author blamothe
 */
public interface Group {
	/**
	 * Returns the type of group of which the implementing class is,
	 * such as sequence or graph.
	 */
	public String whatGroup();
	
	/**
	 * Returns the set of Things which belong to this group.
	 */
	public AbstractCollection<Entity> getElts();
	
	/**
	 * Returns the set of relations between elements of the group.
	 */
	public AbstractCollection<Relation> getRels();
	
	/**
	 * Returns all relations of which elt is either subject or object.
	 */
	public AbstractCollection<Relation> getRelationsInvolving(Entity elt);
	
	/**
	 * Adds elt to the group.
	 */
	public boolean addElt(Entity elt);
	
	/**
	 * Adds the specified relation to the group.
	 */
	public boolean addRel(Relation rel);
	
	/**
	 * Reports if elt is a member of the group
	 */
	public boolean in(Entity elt);
	
	/**
	 * Reports if rel is a known relation of members of the group.
	 */
	public boolean in(Relation rel);
	/**
	 * Removes elt, and all relations involving elt, from the group.
	 */
	public boolean removeElt(Entity elt);
	
	/**
	 * Removes the relation rel fromt the group.
	 */
	public boolean removeRel(Relation rel);
	
	/**
	 * Checks if object o is equal to this.
	 */
	public boolean isEqual(Object o);
	
	/**
	 * Returns a list of all components of the group
	 */
	public List<Entity> getAllComponents();
}

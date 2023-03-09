package utils.groups;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;
import frames.entities.Relation;
/**
 * Data structure to hold things that are equivalent in some way.
 * For example, a collection of events that happen at the same time.
 * Maintains a hash set of things.  Relations are assumed to be all
 * the same and relate all elements of the group.  
 * @author blamothe
 */
public class Equivalence extends Entity implements Group {
	public static String EQUIVNAME = "equivalence";
	private HashSet<Entity> elts = new HashSet<Entity>();
	
	public Equivalence() {}
	/**
	 * @param type
	 */
	public Equivalence(String type) {
		super(type);
	}
	public boolean addElt(Entity elt) {
		return elts.add(elt);
	}
	/**
	 * If one component of rel is in the equivalence group
	 * and another one is not, this method adds the other
	 * component, irrespective of the relations types.  Type
	 * checking should be done by whatever method calls addRel.
	 */
	public boolean addRel(Relation rel) {
		if (in(rel.getSubject())) {
			return addElt(rel.getObject());
		}
		if (in(rel.getObject())) {
			return addElt(rel.getSubject());
		}
		return false;
	}
	public HashSet<Entity> getElts() {
		return elts;
	}
	public HashSet<Relation> getRelationsInvolving(Entity elt) {
		return getRels();
	}
	public HashSet<Relation> getRels() {
		return null;
	}
	public boolean in(Entity elt) {
		return elts.contains(elt);
	}
	/**
	 * Returns true if both the subject and object of rel are
	 * elements of the equivalence, irrespective of rel's type.
	 */
	public boolean in(Relation rel) {
		return (in(rel.getSubject()) && in(rel.getObject()));
	}
	public boolean removeElt(Entity elt) {
		return elts.remove(elt);
	}
	/**
	 * Removes the object of rel from the equivalence group.
	 */
	public boolean removeRel(Relation rel) {
		return (removeElt(rel.getObject()));
	}
	public String whatGroup() {
		return EQUIVNAME;
	}
	
	public boolean addAll(Set<Entity> elts) {
		return elts.addAll(elts);
	}
	
	public Equivalence merge(Equivalence e) {
		Equivalence result = new Equivalence();
		result.addAll(this.getElts());
		result.addAll(e.getElts());
		return result;
	}
	
	public List<Entity> getAllComponents() {
		List<Entity> result = super.getAllComponents();
		result.addAll(elts);
		return result;
	}
	
	public boolean isEqual (Object o) {
		if (o instanceof Equivalence) {
			Equivalence e = (Equivalence) o;
			HashSet<Entity> eElts = e.getElts();
			HashSet<Entity> thisElts = this.getElts();
			for (Entity t : eElts) {
				Entity equivThing = Graph.equalHelper(t, thisElts);
				if (equivThing == null) {
					return false;
				}
				thisElts.remove(equivThing);
			}
			if (!thisElts.isEmpty()) {
				return false;
			}
			return super.isEqual(o);
		}
		return false;
	}
}	

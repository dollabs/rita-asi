package utils.groups;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Relation;
/**
 * Graph data structure which implements the Group interface.  Maintains a
 * hash set of Things (elts), and a hash set of relations between those things
 * (rels).
 * @author blamothe
 */
public class Graph extends Entity implements Group {
	public static String GRAPHNAME = "graph";
	private HashSet<Entity> elts = new HashSet<Entity>();
	private HashSet<Relation> rels = new HashSet<Relation>();
	public Graph(){
		super();
	}
	
	public Graph(String type) {
		super(type);
	}
	
	public boolean addElt(Entity elt) {
		saveState();
		elts.add(elt);
		fireNotification();
		return true;
	}
	
	public boolean addRel(Relation rel) {
		saveState();
		//rel only gets added if both the subject and object are already elements of the group.
		if (this.in(rel.getSubject()) && this.in(rel.getObject())) {
			rels.add(rel);
			fireNotification();
			return true;
		}
		return false;
	}
	
	public HashSet<Entity> getElts() {
		return elts;
	}
	public HashSet<Relation> getRels() {
		return rels;
	}
	
	/**
	 * Returns a vector including all relations of which elt is subject or
	 * object.
	 */
	public Vector<Relation> getRelationsInvolving(Entity elt) {
		Vector<Relation> relations = new Vector<Relation>();
		if (!elts.contains(elt)) {
			System.err.println("Thing " + elt + " not a node in graph " +  this);
		} else {
			for (Relation r : rels) {
				if (r.getSubject().equals(elt) || r.getObject().equals(elt)){
					relations.add(r);
				}	
			}
		}
		return relations;
	}
	public boolean in(Entity elt) {
		return elts.contains(elt);
	}
	public boolean in(Relation rel) {
		return elts.contains(rel);
	}
	public boolean removeElt(Entity elt) {
		saveState();
        boolean b = elts.remove(elt);
        if (b) {
        	rels.removeAll(getRelationsInvolving(elt));
            fireNotification();
        }
        return b;
	}
	public boolean removeRel(Relation rel) {
		saveState();
		boolean b = rels.remove(rel);
		if (b) {
			fireNotification();
		}
		return b;
	}
	public String whatGroup() {
		return GRAPHNAME;
	}
	public Object clone() {
		Graph graph = new Graph();
		Bundle bundle = (Bundle) (getBundle().clone());
        graph.setBundle(bundle);
        HashSet<Entity> s = getElts();
		for (Entity t : s) {
			graph.addElt(t);
		}
		HashSet<Relation> t = getRels();
		for (Relation r : t) {
			graph.addRel(r);
		}
		Vector<Entity> m = getModifiers();
		for (Entity u : m) {
			graph.addModifier(u);
		}
		return graph;
	}
	public List<Entity> getAllComponents() {
		List<Entity> result = super.getAllComponents();
		result.addAll(getElts());
		result.addAll(getRels());
		return result;
	}
	
	/**
	 * Checks if the supplied object is equal to this.  Compares
	 * the element and relation hash sets by looping through each
	 * element in the hash set of the graph being compared, and, for
	 * each element in the hash set being compared, equalHelper finds
	 * the element in this's hash set that is equal to it, and removes
	 * it.  If equal helper ever can't find an equal element, or if
	 * this's hash set is not empty after the end of the loop, returns
	 * false.  The same loop is the executed on the relations.
	 */
	public boolean isEqual(Object o) {
		if (o instanceof Graph) {
			Graph g = (Graph) o;
			HashSet<Entity> gElts = g.getElts();
			HashSet<Entity> thisElts = this.getElts();
			for (Entity t : gElts) {
				Entity equivThing = equalHelper(t, thisElts);
				if (equivThing == null) {
					return false;
				}
				thisElts.remove(equivThing);
			}
			if (!thisElts.isEmpty()) {
				return false;
			}
			
			HashSet<Relation> gRels = g.getRels();
			HashSet<Relation> thisRels = this.getRels();
			for (Relation r : gRels) {
				Relation equivRel = equalHelper(r, thisRels);
				if (equivRel == null) {
					return false;
				}
				thisRels.remove(equivRel);
			}
			if (thisRels.isEmpty()) {
				return super.isEqual(g);
			}
		}
		return false;
	}
	
	protected static Entity equalHelper(Entity x, HashSet<Entity> things) {
		for (Entity t : things) {
			if (x.isEqual(t)) {
				return t;
			}
		}
		return null;
	}
	
	protected static Relation equalHelper(Relation x, HashSet<Relation> rels) {
		for (Relation r : rels) {
			if (x.isEqual(r)) {
				return r;
			}
		}
		return null;
	}
}

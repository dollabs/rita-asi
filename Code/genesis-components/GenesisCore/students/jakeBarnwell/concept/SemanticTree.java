package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A tree-like structure for things in Genesis that have semantic
 * meaning and are tree-like (that is, built recursively) in nature.
 * @author jb16
 *
 */
public abstract class SemanticTree {
	
	/** Faux hash function that encodes uniqueness by location in the tree */
	public static final java.util.function.Function<SemanticTree, Integer> UNIQUE_BY_LOCATION =
			(SemanticTree et) -> et.getLocation().hashCode(); // TODO private?

	/**
	 *  The computed parent pointer of this node. Only guaranteed
	 *  to be accurate immediately after calling
	 *  {@link #recomputeParentPointers(boolean)}.
	 */
	protected transient SemanticTree computedParent;
	
	/**
	 *  The computed location of this node in the tree. Only guaranteed
	 *  to be accurate immediately after calling
	 *  {@link #recomputeLocation}.
	 */
	private transient NodeLocation computedLocation = null;
	
	/** Cached hash code for this object */
	public transient Integer hashCode = null;

	/**
	 * Follows directions down this tree and returns the node 
	 * pointed to by the directions.
	 * @param directions
	 * @throws RuntimeException if at some point the directions point to a null node
	 * @return
	 */
	public SemanticTree follow(NodeLocation directions) {
		if (directions == null) {
			return this;
		}

		SemanticTree theNode = this;
		directions.ready();
		Direction d;
		while(true) {
			d = directions.next();
			if(d == null) {
				return theNode;
			}
			if(d == Direction.SUBJECT) {
				theNode = theNode.getSubject();
			} else if(d == Direction.OBJECT) {
				theNode = theNode.getObject();
			} else if(d.isElements()) {
				theNode = theNode.getElement(d.getIndex());
			} else {
				throw new RuntimeException("Illegal directions.");
			}
		}
	}
	
	/**
	 * Gets the {@link EntityType} that this tree represents
	 * @return
	 */
	public EntityType getNodeType() {
		return EntityType.of(this);
	}
	
	/**
	 * Returns the subject of this node
	 * @return
	 */
	public abstract SemanticTree getSubject();
	
	/** 
	 * Returns the object of this node
	 * @return
	 */
	public abstract SemanticTree getObject();
	
	/**
	 * Returns true if this node is marked by a semantic <code>not</code> marker
	 * @return
	 */
	public abstract boolean getNot();
	
	/**
	 * Fetches the label associated with the node. The label is typically the
	 * terminal class of the thread associated with the node.
	 * @return
	 */
	public abstract String getLabel();
	
	/**
	 * Returns the (i)th element if this node represents a <code>sequence</code>,
	 * otherwise <code>null</code>.
	 * <br><br>
	 * Equivalent to calling {@link #getElements()}<code>.get(i)</code>
	 * @param i
	 * @return
	 */
	public abstract SemanticTree getElement(int i);
	
	/**
	 * Returns the sequence of elements associated with this node, if this
	 * node represents a <code>sequence</code>; otherwise, returns <code>
	 * null</code>.
	 * @return
	 */
	public abstract List<SemanticTree> getElements();
	
	public NodeLocation getLocation() {
		if(computedLocation == null) {
			recomputeLocation();
		}
		
		return computedLocation;
	}
	
	/**
	 * Gets all nodes in this subtree of some given type.
	 * @param type
	 * @return
	 */
	public AssocSet<SemanticTree> getNodesOfType(EntityType type) {
		AssocSet<SemanticTree> nodes = new AssocSet<>(UNIQUE_BY_LOCATION);
		if(this.getNodeType() == type) {
			nodes.add(this);
		}
		
		// Recurse down
		SemanticTree subject = getSubject(), object = getObject();
		if(subject != null) {
			nodes.addAll(subject.getNodesOfType(type));
		}
		if(object != null) {
			nodes.addAll(object.getNodesOfType(type));
		}
		List<SemanticTree> elements = getElements();
		if(elements != null && elements.size() > 0) {
			elements.forEach(ele -> nodes.addAll(ele.getNodesOfType(type)));
		}
		return nodes;
	}
	
	/**
	 * Gets all children of this node, regardless of what type of
	 * child. The children of this node include the {@link #subject},
	 * {@link #object}, and each of the {@link #elements}.
	 * May return an empty set, but never null.
	 * @return
	 */
	public AssocSet<SemanticTree> getChildren() {
		AssocSet<SemanticTree> children = new AssocSet<>(UNIQUE_BY_LOCATION);
		if(getSubject() != null) {
			children.add(getSubject());
		}
		if(getObject() != null) {
			children.add(getObject());
		}
		if(getElements() != null && getElements().size() > 0) {
			children.addAll(getElements());
		}
		return children;
	}
	
	/**
	 * Gets all unique descendants (children, grandchildren, ...)
	 * of this node. Here, 'unique' means that each descendant must
	 * have a different associated {@link NodeLocation}.
	 * <br>
	 * Does not include this node itself.
	 * @return
	 */
	public AssocSet<SemanticTree> getDescendants() {
		this.recomputeParentPointers(true);
		
		AssocSet<SemanticTree> descendants = new AssocSet<SemanticTree>(UNIQUE_BY_LOCATION);
		ArrayList<SemanticTree> agenda = new ArrayList<>();
		
		// In the style of a BFS
		agenda.add(this);
		SemanticTree now;
		while(!agenda.isEmpty()) {		
			now = agenda.remove(0);
			for(SemanticTree child : now.getChildren()) {
				if(!descendants.contains(child)) {
					agenda.add(child);
					descendants.add(child);
				}
			}
		}
		
		return descendants;
	}
	
	/**
	 * Performs a deep copy of the entire list of {@link #elements} 
	 * and returns it.
	 * @return
	 */
	public List<SemanticTree> copyElements() {
		if(getElements() == null) {
			return null;
		}
		
		List<SemanticTree> copies = getElements()
				.stream()
				.map(ele -> ele.copy())
				.collect(Collectors.toList());
		copies.forEach(copy -> copy.recomputeParentPointers(true));
		return copies;
	}

	/**
	 * Performs a deep copy of this object and returns it.
	 * @return
	 */
	protected abstract SemanticTree copy();

	/**
	 * Assigns the {@link #computedParent} attribute to the children of this node
	 * dynamically.
	 * @param deep if true, recursively assigns parent attributes to ALL
	 * 			descendants, not just the direct children.
	 */
	protected void recomputeParentPointers(boolean deep) {
		SemanticTree s = getSubject(), o = getObject();
		List<SemanticTree> elts = getElements();
		if(s != null) {
			s.computedParent = this;
			if(deep) s.recomputeParentPointers(deep);
		}
		if(o != null) {
			o.computedParent = this;
			if(deep) o.recomputeParentPointers(deep);
		}
		if(elts != null && elts.size() > 0) {
			elts.forEach(ele -> ele.computedParent = this);
			if(deep) elts.forEach(ele -> ele.recomputeParentPointers(deep));
		}
	}

	/** 
	 * Updates the {@link NodeLocation} object describing this node's
	 * location in the tree: {@link #computedLocation}.
	 * @requires The {@link #computedParent} fields are all set correctly.
	 * @return The directions for this node. Possibly empty, but not null.
	 */
	private void recomputeLocation() {
		if(computedParent == null) {
			this.computedLocation = new NodeLocation();
			return;
		}
		
		// Gets the directions from root to the parent
		SemanticTree parent = this.computedParent;
		NodeLocation directionsToParent = parent.getLocation();
		
		// Gets the direction from parent to this
		NodeLocation directions;
		if(parent.getSubject() == this) {
			directions = directionsToParent.take(Direction.SUBJECT);
		} else if(parent.getObject() == this) {
			directions = directionsToParent.take(Direction.OBJECT);
		} else if(parent.getElements() != null && parent.getElements().contains(this)) {
			directions = directionsToParent.take(Direction.ELEMENT(parent.getElements().indexOf(this)));
		} else {
			throw new RuntimeException("Must ensure that parent fields are set correctly before calling this method!");
		}
		
		this.computedLocation = directions;
	}
	

}

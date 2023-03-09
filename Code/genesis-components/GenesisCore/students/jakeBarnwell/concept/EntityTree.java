package jakeBarnwell.concept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import constants.Markers;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import jakeBarnwell.Tools;

/**
 * An immutable tree representing any single entity without
 * having to deal with polymorphism.
 * @author jb16
 *
 */
public class EntityTree extends SemanticTree {
	
	/** Subject and object, if applicable; otherwise, null */
	private final EntityTree subject, object;
	
	/** Elements (if this node is a sequence), otherwise null */
	private final List<EntityTree> elements;
	
	/** True if this node is marked by a semantic `not` */
	private final boolean not;

	/** The bundle of threads associated with this object */
	private final Bundle bundle;
	
	/**
	 * Public-facing constructor. Creates a node of this type given 
	 * an {@link Entity} and a pointer to the parent of this node.
	 * @param e the {@link Entity} to base this node off of
	 * @param p the parent {@link EntityTree} of this node
	 */
	public EntityTree(Entity e, EntityTree p) {
		computedParent = p;
		
		// Set final fields
		bundle = e.getBundle();
		subject = e instanceof Function ? new EntityTree(e.getSubject(), this) : null;
		object = e instanceof Relation ? new EntityTree(e.getObject(), this) : null;
		elements = e instanceof Sequence ? 
				e.getElements()
				.stream()
				.map(ent -> new EntityTree(ent, this))
				.collect(Collectors.<EntityTree>toList()) :
				null;
		not = e.hasFeature(Markers.NOT);
	}
	
	private EntityTree(EntityTree s, EntityTree o, Bundle b, List<EntityTree> l, boolean n) {
		bundle = b;
		subject = s;
		object = o;
		elements = l;
		not = n;
	}
	
	/**
	 * Gets directions to all possible groups of nodes in this subtree
	 * that are lexically the same. May return an empty set.
	 * A group of two (or more) nodes must satisfy two conditions in order 
	 * to be considered the 'same.'
	 * <br><b>(1)</b>
	 * Both nodes must be "valid" nodes to even be considered;
	 * such validity is determined by the first argument to this method.
	 * <br><b>(2)</b>
	 * Both nodes must have equal "sameness hashes"; such hashes
	 * are computed by the second argument to this method.
	 * @param samenessValidity A function mapping {@link EntityTree}-->
	 * {@link Boolean} which returns true if a particular {@link EntityTree}
	 * can even be considered to be the 'same' as another. For example,
	 * you may only be interested in nodes that represent {@link Entity}s
	 * or {@link Function}s, but not {@link Relation}s or {@link Sequence}s.
	 * @param samenessHash A function mapping {@link EntityTree}-->
	 * {@link Integer} which acts as a hash function; this hash function
	 * should hash two objects to the same value iff they should be considered
	 * the 'same.'
	 * @return A set of {@link Group} of {@link NodeLocation}s. Each individual
	 * {@link Group} represents a multitude of nodes all of which should be 
	 * considered the same as each other.
	 * 
	 */
	public Set<Group<NodeLocation>> getSame(
			java.util.function.Function<EntityTree, Boolean> samenessValidity,
			java.util.function.Function<EntityTree, Integer> samenessHash) {
		Set<Group<NodeLocation>> sameGroupSet = new HashSet<>();
		
		// Gets all nodes of this subtree (including this object) that are
		//  applicable to be 'same'
		AssocSet<SemanticTree> subtree = this.getDescendants();
		subtree.add(this);
		List<EntityTree> applicableNodes = subtree
				.stream()
				.map(d -> (EntityTree)d)
				.filter(d -> samenessValidity.apply(d))
				.collect(Collectors.<EntityTree>toList());
		
		// TODO is this relevant to the Constraints? Can be generalized?
		// We'll apply the sameness hash to the descendants and compare them
		//  to see if any are repeats
		List<Integer> samenessHashCodes = applicableNodes
				.stream()
				.map(d -> samenessHash.apply(d))
				.collect(Collectors.toList());
		
		// Compare sizes to see if there are any repeats
		if(samenessHashCodes.size() == new HashSet<>(samenessHashCodes).size()) {
			return sameGroupSet;
		}
		
		// By now we know there are repeats.
		
		// Maps samenessHash --> EntityTreesWithThatSamenessHash
		HashMap<Integer, Group<EntityTree>> treesWithThatHash = new HashMap<>();
		for(EntityTree d : applicableNodes) {
			int hash = samenessHash.apply(d);
			Group<EntityTree> entityTrees = treesWithThatHash.getOrDefault(hash, new Group<>(UNIQUE_BY_LOCATION));
			entityTrees.add(d);
			treesWithThatHash.put(hash, entityTrees);
		}
		
		// Store only entries whose sameness hash codes are shared amongst multiple objects
		Map<Integer, Group<EntityTree>> sharedHashes = treesWithThatHash.entrySet()
				.stream()
				.filter(e -> e.getValue().size() > 1)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		// Finally we add each entry appropriately to our set and return
		this.recomputeParentPointers(true);
		for(Group<EntityTree> sameNodesGroup : sharedHashes.values()) {
			Group<NodeLocation> nodeDirectionsGroup = new Group<>(
					sameNodesGroup
					.stream()
					.map(EntityTree::getLocation)
					.collect(Collectors.<NodeLocation>toSet()));
			sameGroupSet.add(nodeDirectionsGroup);
		}
		
		return sameGroupSet;
	}
	
	@Override
	protected EntityTree copy() {
		return this.copyExcept();
	}
	
	/**
	 * Creates a copy of this node but with the given attributes
	 * instead of the current ones. Arguments should be of the form
	 * {@code [EntityAttribute, value, EntityAttribute, value, ...]}
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private EntityTree copyExcept(Object... args) {
		// Get references for things which will be copied:
		EntityTree s = subject, o = object;
		Bundle b = bundle;
		boolean n = not;
		List<EntityTree> ele = elements;
		for(int i = 0; i < args.length; i += 2) {
			EntityAttribute attr = (EntityAttribute)args[i];
			Object val = args[i + 1];
			switch(attr) {
				case BUNDLE: b = (Bundle)val; break;
				case SUBJECT: s = (EntityTree)val; break;
				case OBJECT: o = (EntityTree)val; break;
				case SEQUENCE: ele = (List<EntityTree>)val; break;
				case NOT: n = (boolean)val; break;
				default: throw new RuntimeException("Inappropriate attribute type given: " + attr);
			}
		}
		
		// Actually copy the objects:
		s = s == null ? s : s.copy();
		o = o == null ? o : o.copy();
		ele = ele == null ? ele :
				ele
				.stream()
				.map(EntityTree::copy)
				.collect(Collectors.<EntityTree>toList());
		b = b == null ? b : (Bundle)((Bundle)b).clone();
		
		EntityTree copy = new EntityTree(s, o, b, ele, n);
		
		// Re-assign parent pointers to be consistent within the copy
		copy.recomputeParentPointers(true);
		
		return copy;
	}
	
	/**
	 * Turns this object into a proper {@link Entity} that most
	 * closely represents this object.
	 * @return
	 */
	public Entity toEntity() {
		EntityType type = this.getNodeType();
		Entity entity;
		if(type == EntityType.ENTITY) {
			entity = new Entity(bundle);
		} else if(type == EntityType.FUNCTION) {
			entity = new Function(bundle, subject.toEntity());
		} else if(type == EntityType.RELATION) {
			entity = new Relation(bundle, subject.toEntity(), object.toEntity());
		} else {
			entity = new Sequence(bundle);
			Vector<Entity> eles = new Vector<>(elements.stream()
					.map(EntityTree::toEntity)
					.collect(Collectors.<Entity>toList()));
			((Sequence)entity).setElements(eles);
		}
		
		if(not) {
			entity.addFeature(Markers.NOT);
		}
		
		return entity;
	}
	
	public Bundle getBundle() {
		return bundle;
	}

	@Override @SuppressWarnings("unchecked")
	public List<SemanticTree> getElements() {
		return (List<SemanticTree>)(Object)elements;
	}
	
	@Override
	public SemanticTree getElement(int i) {
		return elements.get(i);
	}
	
	@Override
	public SemanticTree getSubject() {
		return subject;
	}
	
	@Override
	public SemanticTree getObject() {
		return object;
	}
	
	@Override
	public boolean getNot() {
		return not;
	}
	
	@Override
	public String getLabel() {
		return this.getBundle().getPrimedThread().getType();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof EntityTree)) {
			return false;
		}
		
		EntityTree o = (EntityTree)other;
		return Tools.safeEquals(bundle, o.bundle) &&
				Tools.safeEquals(subject, o.subject) &&
				Tools.safeEquals(object, o.object) &&
				Tools.unorderedEquals(elements, o.elements) &&
				not == o.not;
	}
	
	@Override
	public int hashCode() {
		if(hashCode != null) {
			return hashCode;
		}
		
		hashCode = (3 * Tools.safeHashCode(bundle)) +
				(521 * Tools.safeHashCode(subject)) +
				(1171 * Tools.safeHashCode(object)) +
				(3793 * Boolean.hashCode(not)) +
				(elements == null ? 0 : (127 * Tools.safeHashCode(new HashSet<>(elements))));
		
		return hashCode;
	}
	
	@Override
	public String toString() {
		String attributes = (subject != null ? "subj," : "")
			+ (object != null ? "obj," : "")
			+ (elements != null ? String.format("seq[%d]", elements.size()) : "");
		return String.format("%s(%s%s#%s@%d/%s)", 
				this.getClass().getSimpleName(),
				not ? "not " : "",
				bundle.size() > 0 ? bundle.getPrimedThread().get(bundle.getPrimedThread().size() - 1) : "?",
				hashCode() % 1000,
				System.identityHashCode(this) % 1000,
				attributes);
	}


}
package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import jakeBarnwell.Tools;

/**
 * An immutable tree representing a single concept.
 * @author jb16
 *
 */
public class ConceptTree extends SemanticTree {
	
	/** Subject and object, if applicable; otherwise, null */
	private final ConceptTree subject, object;
	
	/** Elements (if this node is a sequence), otherwise null */
	private final List<ConceptTree> elements;
	
	/** True if this node is marked by a semantic `not` */
	private final boolean not;
	
	/** The permissible classes of the threads; always non-null, but possibly empty */
	private final Spool spool;
	
	/**
	 * The navigation route taken starting at the root of this subtree
	 * and ending or going through this node, with the intent of finding
	 * some goal node (either this node, or a descendant of this node).
	 * After applying {@link #copyDirectionsToGoalFromChild(Direction)}, any node in the path 
	 * from the root to the transformed-upon node (inclusive) will have
	 * the same value for this field.
	 */
	private transient NodeLocation directionsToGoal;
	
	/**
	 * Stores various properties that are relevant for this tree, for example,
	 * details about how this tree was obtained by transforming a previous
	 * tree.
	 */
	private Properties transformationProperties = Properties.create();
	
	/**
	 * Stores information about what types of values have existence 
	 * quantifiers attached to them, such as "must exist" or "optionally
	 * exists."
	 */
	public transient Map<Quantifier, List<Which>> existenceQuantifiers = new HashMap<>();
	
	/**
	 * Public-facing constructor. Creates a node of this type given 
	 * an {@link Entity} and a pointer to the parent of this node.
	 * @param e the {@link Entity} to base this node off of
	 * @param p the parent {@link ConceptTree} of this node
	 */
	public ConceptTree(Entity e, ConceptTree p) {
		computedParent = p;
		
		// Set final fields
		spool = Spool.build(e.getBundle().getPrimedThread(), Charge.POSITIVE);
		subject = e instanceof Function ? new ConceptTree(e.getSubject(), this) : null;
		object = e instanceof Relation ? new ConceptTree(e.getObject(), this) : null;
		elements = e instanceof Sequence ? 
				e.getElements()
				.stream()
				.map(ent -> new ConceptTree(ent, this))
				.collect(Collectors.<ConceptTree>toList()) :
				null;
		not = e.hasFeature(Markers.NOT);
	}
	
	/**
	 * Private constructor. Constructs a {@link ConceptTree} based on 
	 * the properties of a similar {@link EntityTree}.
	 * @param et
	 * @param p
	 */
	private ConceptTree(EntityTree et, ConceptTree p) {
		computedParent = p;
		
		// Set final fields
		spool = Spool.build(et.getBundle().getPrimedThread(), Charge.POSITIVE);
		EntityType type = et.getNodeType();
		subject = type == EntityType.FUNCTION || type == EntityType.RELATION ?
				new ConceptTree((EntityTree)et.getSubject(), this) :
				null;
		object = type == EntityType.RELATION ?
				new ConceptTree((EntityTree)et.getObject(), this) :
				null;
		elements = type == EntityType.SEQUENCE ?
				et.getElements()
				.stream()
				.map(e -> new ConceptTree((EntityTree)e, this))
				.collect(Collectors.<ConceptTree>toList()) :
				null;
		not = et.getNot();
	}
	
	private ConceptTree(ConceptTree s, ConceptTree o, List<ConceptTree> l, Spool sp, boolean n) {
		spool = sp;
		subject = s;
		object = o;
		elements = l;
		not = n;
	}
	
	/**
	 * Does a search to find all "nearby" trees in a tree-space; only
	 * trees within a certain transformation-cost distance will be
	 * returned. The search uses a specified goal or beacon tree 
	 * as a guide to help generate relevant neighbors.
	 * @param maxCostPermitted
	 * @return
	 */
	public PriorityQueue<TransformationPath> getTransformationPaths(EntityTree goal, double maxCostPermitted) {
		// PQ so that we can get the best one at the end of the day
		PriorityQueue<TransformationPath> results
			= new PriorityQueue<>(16, Comparator.comparing(path -> path.getCost()));
		
		// This is going to basically be a branch & bound search with extended set
		
		Set<ConceptTree> visited = new HashSet<>();
		PriorityQueue<TransformationPath> agenda 
			= new PriorityQueue<>(16, Comparator.comparing(path -> path.getCost()));
		agenda.add(new TransformationPath(this));
		
		// Later on I will loop through all neighbors by operator. In the spirit 
		//  of the B&B search, I need to loop through the operators from least
		//  to greatest cost, because if we add a state to the /visited/ list that
		//  came via a more expensive operator, we've screwed ourselves.
		List<EntityOperator> opsOrdered = Arrays.asList(EntityOperator.values());
		opsOrdered.sort(Comparator.comparing(EntityOperator::getCost));
		
		TransformationPath currPath, nextPath;
		ConceptTree currentTree;
		// There are several types of operators, so for each operator type 
		//  we'll store all possible neighbors that we can attain by applying
		//  that operator
		Map<EntityOperator, Set<ConceptTree>> neighbors;
		// TODO do some sort of pre-check (before even fetching all neighbors) to see 
		//  if it's worth it. This will definitely improve the speed of my alg, I think.
		while(!agenda.isEmpty()) {
			currPath = agenda.poll();
			// If the *best* partial path costs higher than permitted, we terminate.
			if(currPath.getCost() > maxCostPermitted) {
				return results;
			}
			currentTree = currPath.latestTree();
			// Gets all neighbors of the current tree
			neighbors = currentTree.getAllNeighbors(goal, currPath.getNumMergeExpand());
			// Loop through all states attained from each operator
			for(EntityOperator op : opsOrdered) {
				if(op == null) continue;
				for(ConceptTree ct : neighbors.get(op)) {
					// Check if we haven't yet visited this neighbor, and if it's within range
					if(!visited.contains(ct) && currPath.getCost() + op.getCost() <= maxCostPermitted) {
						visited.add(ct);
						nextPath = currPath.copy().register(op, ct.directionsToGoal.copy(), ct);
						if(matches(ct, goal)) {
							results.add(nextPath);
						} else {
							agenda.add(nextPath);
						}
					}
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Gets neighbors of this object in {@link EntityTree}-space that are biased
	 * towards some guiding (beacon) {@link EntityTree}.
	 * @param guide
	 * @param numMergeExpand
	 * @return
	 */
	private Map<EntityOperator, Set<ConceptTree>> getAllNeighbors(EntityTree guide, int numMergeExpand) {
		HashMap<EntityOperator, Set<ConceptTree>> allNeighbors = new HashMap<>();
		for(EntityOperator t : EntityOperator.values()) {
			allNeighbors.put(t, neighbors(t, guide, new NodeLocation(), numMergeExpand));
		}
		
		return allNeighbors;
	}
	
	/**
	 * Gets neighbors of a certain type of this object in {@link EntityTree}-space
	 * that are biased towards some guiding {@link EntityTree}.
	 * @param op the type of single operation to apply to yield neighbors
	 * @param g the guiding {@link EntityTree}
	 * @param loc current location of the node in the tree; not null
	 * @param numMergeExpand how many times a sequence merge or expand has been called
	 * @return the set of resultant {@link ConceptTree}s after applying the operator 
	 */
	private Set<ConceptTree> neighbors(EntityOperator op, EntityTree g, NodeLocation loc, int numMergeExpand) {
		// Sets the tree path declared by the caller, which is necessary
		//  because we recursively call neighbors on all nodes in the tree
		//  to make sure we cover all our bases
		if(loc == null) {
			throw new RuntimeException("Incoming node address must not be null!");
		}
		HashSet<ConceptTree> neighbors = new HashSet<>();
		switch(op) {
			case MERGE_SEQ:
				if(numMergeExpand < 1) {
					neighbors.addAll(_neighbors_merge_seq(g));
				}
				break;
			case EXPAND_SEQ: 
				if(numMergeExpand < 1) {
					neighbors.addAll(_neighbors_expand_seq(g));
				}
				break;
			case CHANGE_SPOOL: neighbors.addAll(_neighbors_change_spool(g)); break;
			case SWAP_SRO: neighbors.addAll(_neighbors_swap_sro(g)); break;
			case SWAP_SGO: neighbors.addAll(_neighbors_swap_sgo(g)); break;
			case CHANGE_ELE: neighbors.addAll(_neighbors_change_ele(g)); break;
			case ADD_ELE: neighbors.addAll(_neighbors_add_ele(g)); break;
			case REMOVE_ELE: neighbors.addAll(_neighbors_remove_ele(g)); break;
			case NOT: neighbors.addAll(_neighbors_not(g)); break;
			default: throw new RuntimeException("Not a valid transformation type: " + op.toString());
		}
		// Assigns the node locations to all neighbors found
		neighbors.forEach(n -> n.directionsToGoal = loc);

		if(subject != null) {
			Set<ConceptTree> subjNeighbors = subject
					.neighbors(op, g, loc.take(Direction.SUBJECT), numMergeExpand)
					.stream()
					.map(n -> this.copyExcept(ConceptAttribute.SUBJECT, n))
					.collect(Collectors.<ConceptTree>toSet());
			subjNeighbors.forEach(n -> n.copyDirectionsToGoalFromChild(Direction.SUBJECT));
			neighbors.addAll(subjNeighbors);
		}
		
		if(object != null) {
			Set<ConceptTree> theNeighbors = object.neighbors(op, g, loc.take(Direction.OBJECT), numMergeExpand);
			Set<ConceptTree> objNeighbors = object
					.neighbors(op, g, loc.take(Direction.OBJECT), numMergeExpand)
					.stream()
					.map(n -> this.copyExcept(ConceptAttribute.OBJECT, n))
					.collect(Collectors.<ConceptTree>toSet());
			objNeighbors.forEach(n -> n.copyDirectionsToGoalFromChild(Direction.OBJECT));
			neighbors.addAll(objNeighbors);
		}
		
		Set<ConceptTree> eleNeighbors;
		ConceptTree eleNeighbor;
		if(elements != null) {
			for(int i = 0; i < elements.size(); i++) {
				ConceptTree ele = (ConceptTree)(elements.get(i));
				eleNeighbors = ele.neighbors(op, g, loc.take(Direction.ELEMENT(i)), numMergeExpand);
				List<SemanticTree> newElements = copyElements();
				for(ConceptTree childNeighbor : eleNeighbors) {
					newElements.remove(i);
					newElements.add(i, childNeighbor);
					eleNeighbor = copyExcept(ConceptAttribute.SEQUENCE, newElements);
					eleNeighbor.copyDirectionsToGoalFromChild(Direction.ELEMENT(i));
					neighbors.add(eleNeighbor);
				}
			}
		}
		
		// Make sure all parent pointers are assigned
		neighbors.forEach(n -> n.recomputeParentPointers(true));
		
		// TODO i believe this logic is flawed 
		// If this was a merge or expansion, increment the counter
		if(op == EntityOperator.MERGE_SEQ || op == EntityOperator.EXPAND_SEQ) {
			numMergeExpand++;
		}
		
		return neighbors;
	}
	
	private Set<ConceptTree> _neighbors_not(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		possibles.add(this.copyExcept(ConceptAttribute.NOT, !this.not));
		return possibles;
	}
	
	private Set<ConceptTree> _neighbors_merge_seq(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		if(elements != null && elements.size() == 1) {
			ConceptTree mergeSeq = elements.get(0).copy();
			possibles.add(mergeSeq);
		}
		return possibles;
	}
	
	private Set<ConceptTree> _neighbors_expand_seq(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();

		if(elements == null) {
			List<ConceptTree> newSeq = new ArrayList<>();
			newSeq.add(this.copy());
			ConceptTree expand = new ConceptTree(null, null, newSeq, Spool.build(""), false);
			possibles.add(expand);
		}
		return possibles;
	}
	
	private Set<ConceptTree> _neighbors_change_spool(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		
		// Only Entities, Functions, and Relations can change bundles; not Sequences.
		EntityType type = this.getNodeType();
		if(type != EntityType.SEQUENCE) {
			// You can only change bundles to a node that is like you (e.g. Function -> Function)
			Set<EntityTree> permissibleToNodes = o.getNodesOfType(type)
					.stream()
					.map(n -> (EntityTree)n)
					.collect(Collectors.<EntityTree>toSet());
			for(EntityTree et : permissibleToNodes) {
				// Don't try to change to a spool that permits the potential bundle
				if(!this.spool.permits(et.getBundle().getPrimedThread())) {
					possibles.add(copyExcept(ConceptAttribute.SPOOL, Spool.build(et.getBundle().getPrimedThread(), Charge.POSITIVE)));
				}
			}
		}
		return possibles;
	}
	
	/**
	 * Swap the subject with the relation object
	 * @param o
	 * @return
	 */
	private Set<ConceptTree> _neighbors_swap_sro(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		if(!(subject == null && object == null)) {
			ConceptTree swapSubjObj = copyExcept(
					ConceptAttribute.SUBJECT, object,
					ConceptAttribute.OBJECT, subject);
			possibles.add(swapSubjObj);
		}
		return possibles;
	}
	
	/**
	 * Swap the subject with the grammatical object
	 * @param o
	 * @return
	 */
	private Set<ConceptTree> _neighbors_swap_sgo(EntityTree o) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		
		 // Just a quick escape route to prune off some time
		if(object == null || object.elements == null || object.elements.size() == 0 
				|| object.elements.get(0).subject == null) {
			return possibles;
		}
		
		// Otherwise...
		try {
			ConceptTree relationObject = object;
			List<ConceptTree> roSequence = relationObject.elements;
			// Make sure it's the "object" we grab; may not be the first element of the sequence
			ConceptTree roFunction = roSequence
					.stream()
					.filter(e -> e.spool.embodies("object"))
					.findFirst()
					.orElseThrow(RuntimeException::new);
			ConceptTree grammaticalObject = roFunction.subject;
			// Rebuild the nodes all the way up
			ConceptTree newRoFunction = roFunction.copyExcept(ConceptAttribute.SUBJECT, this.subject);
			List<SemanticTree> newRoSequence = relationObject.copyElements();
			newRoSequence.remove(0);
			newRoSequence.add(0, newRoFunction);
			ConceptTree newRo = relationObject.copyExcept(ConceptAttribute.SEQUENCE, newRoSequence);
			ConceptTree swapSgo = copyExcept(
					ConceptAttribute.SUBJECT, grammaticalObject,
					ConceptAttribute.OBJECT, newRo);
			possibles.add(swapSgo);
		} catch(Exception e) {
			// Simply means we can't do the SGO swap, as there's no valid GO
			// TODO try-catch is very slow; improve speed by doing it properly?
		}
		return possibles;
	}
	
	private Set<ConceptTree> _neighbors_change_ele(EntityTree o) {
		return __neighbors_mutate_ele(o, true, true);
	}
	
	private Set<ConceptTree> _neighbors_add_ele(EntityTree o) {
		return __neighbors_mutate_ele(o, true, false);
	}
	
	private Set<ConceptTree> _neighbors_remove_ele(EntityTree o) {
		return __neighbors_mutate_ele(o, false, true);
	}
	
	private Set<ConceptTree> __neighbors_mutate_ele(EntityTree o, boolean add, boolean remove) {
		HashSet<ConceptTree> possibles = new HashSet<>();
		
		if(elements == null) {
			return possibles;
		}
		
		// Get all sequence objects from the other tree as possible guides; furthermore,
		//  enforce that the potential guide sequence must have the same name, or 
		//  that this bundle is empty (e.g. for manually-created bundles).
		Set<EntityTree> possibleGuideSeqs = o.getNodesOfType(EntityType.SEQUENCE)
				.stream()
				.map(gSeq -> (EntityTree)gSeq)
				.filter(gSeq -> this.spool.isEmpty() || 
						this.spool.embodies(gSeq.getBundle().getPrimedThread()))
				.collect(Collectors.<EntityTree>toSet());
		
		// For each of guide sequences, make a neighbor with respect to that sequence
		for(EntityTree gSeq : possibleGuideSeqs) {
			@SuppressWarnings("unchecked")
			List<EntityTree> othElements = (List<EntityTree>)(Object)gSeq.getElements();
			// Determines which trees correspond to each other (if any) in the 2 sequences
			final Set<Pair<ConceptTree, EntityTree>> matchedPairs = matchingPairs(elements, othElements);
			// Determines if this guide sequence qualifies to be considered
			boolean qualifiesP = false;
			if(add && remove) {
				// Sequences must be same size, but NOT all be the same
				if(elements.size() == othElements.size()) {
					qualifiesP = matchedPairs.size() != elements.size();
				}
			} else if(add) { 
				qualifiesP = elements.size() < othElements.size();
			} else {
				qualifiesP = elements.size() > othElements.size();
			}
			
			if(!qualifiesP) {
				continue;
			}
			
			// Set up record of which CTs and ETs were matched, for easy reference
			Set<ConceptTree> matchedConceptTrees = new HashSet<>();
			Set<EntityTree> matchedEntityTrees = new HashSet<>();
			for(Pair<ConceptTree, EntityTree> matchedPair : matchedPairs) {
				matchedConceptTrees.add(matchedPair.left);
				matchedEntityTrees.add(matchedPair.right);
			}

			// Find all node in each elements list that aren't shared with the other
			Set<ConceptTree> uniqConceptTrees = elements
					.stream()
					.filter(n -> !matchedConceptTrees.contains(n))
					.collect(Collectors.<ConceptTree>toSet());
			Set<EntityTree> uniqEntityTrees = othElements
					.stream()
					.filter(n -> !matchedEntityTrees.contains(n))
					.collect(Collectors.<EntityTree>toSet());
			
			// Create the new possible sequence elements for this neighbor
			List<SemanticTree> newElements;
			if(remove && add) {
				int idxToChange;
				// Changes (i.e. removes + adds) an item in the sequence
				for(ConceptTree uniqCT : uniqConceptTrees) {
					for(EntityTree uniqET : uniqEntityTrees) {
						newElements = copyElements();
						idxToChange = newElements.indexOf(uniqCT);
						newElements.remove(idxToChange);
						newElements.add(idxToChange, new ConceptTree(uniqET, null));
						
						ConceptTree possible = copyExcept(ConceptAttribute.SEQUENCE, newElements);
						ConceptTree added = possible.elements.get(idxToChange);
						possible.transformationProperties = Properties.create("ELE_ADDED", added, "ELE_REMOVED", uniqCT);
						
						possibles.add(possible);
					}
				}
			} else if(add) {
				// Adds an item (from the ET guide sequence) to the CT sequence
				for(EntityTree uniqET : uniqEntityTrees) {
					newElements = copyElements();
					newElements.add(new ConceptTree(uniqET, null));
					ConceptTree possible = copyExcept(ConceptAttribute.SEQUENCE, newElements);
					ConceptTree added = possible.elements.get(possible.elements.size() - 1);
					possible.transformationProperties = Properties.create("ELE_ADDED", added);
					
					possibles.add(possible);
				}
				
			} else {
				// Removes an item from the CT sequence
				for(ConceptTree uniqCT : uniqConceptTrees) {
					newElements = copyElements();
					newElements.remove(uniqCT);
					ConceptTree possible = copyExcept(ConceptAttribute.SEQUENCE, newElements);
					possible.transformationProperties = Properties.create("ELE_REMOVED", uniqCT);
					
					possibles.add(possible);
				}
			}
		}
		
		return possibles;
	}
	
	/**
	 * Lifts the {@link #directionsToGoal} field from the child in a given 
	 * direction to this node.
	 */
	private void copyDirectionsToGoalFromChild(Direction d) {
		if(d == Direction.SUBJECT) {
			directionsToGoal = subject.directionsToGoal;
		} else if(d == Direction.OBJECT) {
			directionsToGoal = object.directionsToGoal;
		} else {
			directionsToGoal = elements.get(d.getIndex()).directionsToGoal;
		}
	}
	
	/**
	 * Performs a deep copy of the subtree rooted at this node.
	 */
	@Override
	protected ConceptTree copy() {
		return this.copyExcept();
	}
	
	/**
	 * Creates a copy of this node but with the given attributes
	 * instead of the current ones. Arguments should be of the form
	 * {@code [ConceptAttribute, value, ConceptAttribute, value, ...]}
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ConceptTree copyExcept(Object... args) {
		// Get references for things which will be copied:
		ConceptTree s = subject, o = object;
		Spool sp = spool;
		boolean n = not;
		List<ConceptTree> ele = elements;
		for(int i = 0; i < args.length; i += 2) {
			ConceptAttribute attr = (ConceptAttribute)args[i];
			Object val = args[i + 1];
			switch(attr) {
				case SPOOL: sp = (Spool)val; break;
				case SUBJECT: s = (ConceptTree)val; break;
				case OBJECT: o = (ConceptTree)val; break;
				case SEQUENCE: ele = (List<ConceptTree>)val; break;
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
				.map(ConceptTree::copy)
				.collect(Collectors.<ConceptTree>toList());
		sp = sp == null ? sp : sp.copy();
		
		ConceptTree copy = new ConceptTree(s, o, ele, sp, n);
		
		// Re-assign parent pointers to be consistent within the copy
		copy.recomputeParentPointers(true);
		// Copy over node directions as well; necessary while doing searches
		copy.directionsToGoal = this.directionsToGoal;
		
		// Copy transformation properties as well; these are only accessed when
		//  necessary, which means these may or may not be superfluous except
		//  when accessed directly.
		copy.transformationProperties = this.transformationProperties.copy();
		
		return copy;
	}
	
	/**
	 * Finds which elements from two lists match (via the {@link #matches(ConceptTree, EntityTree)}
	 * function), and returns them as a set of pairs. 
	 * Assumes 1-to-1 correspondence.
	 * @param cts
	 * @param ets
	 * @return
	 */
	private static Set<Pair<ConceptTree, EntityTree>> matchingPairs(List<ConceptTree> cts, List<EntityTree> ets) {
		HashSet<Pair<ConceptTree, EntityTree>> matches = new HashSet<>();
		
		if(ets == null || cts == null || ets.size() == 0 || cts.size() == 0) {
			return matches;
		}

		for(ConceptTree ct : cts) {
			for(EntityTree et : ets) {
				if(matches(ct, et)) {
					matches.add(new Pair<>(ct, et));
					break;
				}
			}
		}
		return matches;
	}
	
	/**
	 * Checks if these two trees match in the context of the lattice-
	 * learning system.
	 * This method is static because it makes it much easier to check
	 *  against possibly-null trees.
	 * @param ct
	 * @param et
	 * @return
	 */
	public static boolean matches(ConceptTree ct, EntityTree et) {
		// Check nullity
		if(ct == null && et == null) {
			return true;
		} else if(ct == null || et == null) {
			return false;
		}
		
		// Check if the spool matches with the entity's thread
		if(!ct.spool.permits(et.getBundle().getPrimedThread())) {
			return false;
		}
		
		// Check that the 'not' matches
		if(ct.not != et.getNot()) {
			return false;
		}
		
		// Check that subject and object match
		if(!matches(ct.subject, (EntityTree)et.getSubject()) || !matches(ct.object, (EntityTree)et.getObject())) {
			return false;
		}
		
		// Check that elements match
		// TODO assuming for now that null list means it's not a Sequence meaning definite mismatch.
		// 		This logic may not actually be correct given that we allow optional eles now. Look into this.
		List<SemanticTree> treeElements = et.getElements();
		if(ct.elements == null && treeElements == null) {
			return true;
		} else if(ct.elements == null || treeElements == null) {
			return false;
		}
		
		// We may possibly allow different-sized lists due to existential quantifiers, 
		//  so don't bother with size checks yet.
		
		// Check that each ele of the CT is valid with the ET and vice versa. Simple algorithm:
		// Strike all ele's that qualify as optional from consideration (from both the CT
		//  and ET). Then, make sure the remaining eles all match 1-to-1.
		List<ConceptTree> nonOptionalElesCT = new ArrayList<>(ct.elements)
				.stream()
				.filter(ele -> !ct.elementOptionalP(ele.getLabel()))
				.collect(Collectors.<ConceptTree>toList());
		List<SemanticTree> nonOptionalElesET = new ArrayList<>(treeElements)
				.stream()
				.filter(ele -> !ct.elementOptionalP(ele.getLabel()))
				.collect(Collectors.<SemanticTree>toList());
		if(nonOptionalElesCT.size() != nonOptionalElesET.size()) {
			return false;
		}
		for(int i = 0; i < nonOptionalElesCT.size(); i++) {
			if(!matches(nonOptionalElesCT.get(i), (EntityTree)nonOptionalElesET.get(i))) {
				return false;
			}
		}
		
		return true;		
	}
	
	private boolean elementOptionalP(String label) {
		List<Which> quantifiers = this.existenceQuantifiers.get(Quantifier.OPTIONAL_ELEMENT);
		if(quantifiers == null) {
			return false;
		}
		for(Which whichOnes : quantifiers) {
			if(whichOnes.permits(label)) {
				return true;
			}
		}
		return false;
	}
	
	public Spool getSpool() {
		return spool;
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
	/**
	 * Gets the label of one of the terminal spools associated with this node.
	 */
	public String getLabel() {
		return this.getSpool().getATerminal().thread.getType();
	}
	
	public Properties getProperties() {
		return transformationProperties;
	}

	@Override
	public String toString() {
		String attributes = (subject != null ? "subj," : "")
			+ (object != null ? "obj," : "")
			+ (elements != null ? String.format("seq[%d]", elements.size()) : "");
		return String.format("%s(%s%s~%s(%dT)#%s@%d/%s)", 
				this.getClass().getSimpleName(),
				not ? "not " : "",
				spool.isEmpty() ? "?" : spool.getRoot().thread.getType(),
				spool.isEmpty() ? "?" : spool.getATerminal().thread.getType(),
				spool.getTerminals().size(),
				hashCode() % 1000,
				System.identityHashCode(this) % 1000,
				attributes);
	}

	@Override
	public int hashCode() {
		return (3 * Tools.safeHashCode(spool)) +
				(521 * Tools.safeHashCode(subject)) +
				(1171 * Tools.safeHashCode(object)) +
				(5081 * Boolean.hashCode(not)) +
				(elements == null ? 0 : (127 * Tools.safeHashCode(new HashSet<>(elements))));
	}

	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof ConceptTree)) {
			return false;
		}
		
		ConceptTree o = (ConceptTree)other;
		return Tools.safeEquals(spool, o.spool) &&
				Tools.safeEquals(subject, o.subject) &&
				Tools.safeEquals(object, o.object) &&
				Tools.unorderedEquals(elements, o.elements) &&
				not == o.not;
	}

}
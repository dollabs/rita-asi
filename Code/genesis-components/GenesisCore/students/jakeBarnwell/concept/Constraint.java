package jakeBarnwell.concept;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A type of constraint amongst a group of items.
 * @author jb16
 *
 */
public enum Constraint {
	/**
	 * Implores that all items must be distinct. If there are fewer
	 * than 2 items, vacuously true. 
	 */
	ALL_DIFFERENT((items, hashes)
			-> items.size() < 2 || items.size() == hashes.size()),
	/**
	 * Implores that all items must be the same. If there are no 
	 * items, vacuously true.
	 */
	ALL_SAME((items, hashes) 
			-> hashes.size() <= 1),
	/**
	 * Implores that not all of the items are the same. If there
	 * are fewer than 2 items, definitively false.
	 */
	NOT_ALL_SAME((items, hashes)
			-> hashes.size() > 1);
	
	/**
	 * Function that hashes {@link EntityTree}s in a way that guarantees
	 * that two such {@link EntityTree}s will have the same hash function
	 * if they should be considered to be the 'same.'
	 */
	public static final Function<EntityTree, Integer> SAMENESS_HASH = 
			(EntityTree et) -> 3 * et.getBundle().hashCode() + 7 * Boolean.hashCode(et.getNot());

	/**
	 * Function that takes an {@link EntityTree} and returns true if
	 * it makes sense to test that node for 'sameness.'
	 */
	public static final Function<EntityTree, Boolean> SAMENESS_APPLICABLE = 
			(EntityTree et) -> et.getNodeType() == EntityType.ENTITY;
	
	/**
	 * The predicate function associated with this enum; it is used to 
	 * determine if a group of nodes satisfy the predicate.
	 */
	private final BiFunction<Group<EntityTree>, Group<Integer>, Boolean> predicateFn;
	
	private Constraint(BiFunction<Group<EntityTree>, Group<Integer>, Boolean> predicateFn) {
		this.predicateFn = predicateFn;
	}
	
	/**
	 * Checks if this constraint is valid for a group of nodes keyed by
	 * a group of locations on some root tree. In other words, checks
	 * if the aforementioned group of nodes satisfies the predicate.
	 * @param et
	 * @param locations
	 * @return
	 */
	public boolean implore(EntityTree et, Group<NodeLocation> locations) {
		// Some directions may point to nodes that don't exist in the entity tree.
		//  If that's the case, just ignore those nodes in the constraint check.
		Group<EntityTree> nodes = new Group<>(EntityTree.UNIQUE_BY_LOCATION);
		for(NodeLocation loc : locations) {
			try {
				nodes.add((EntityTree)et.follow(loc));
			} catch(RuntimeException badDirections) {
				;
			}
		}
		
		Group<Integer> samenessHashes = new Group<>(
				nodes.stream()
				.map(n -> Constraint.SAMENESS_HASH.apply(n))
				.collect(Collectors.toSet()));
		
		return predicateFn.apply(nodes, samenessHashes);
	}
}
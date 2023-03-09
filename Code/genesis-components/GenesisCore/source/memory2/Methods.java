package memory2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;
import memory2.datatypes.Chain;
import memory2.datatypes.ImmutableEntity;

/**
 * Static methods
 * 
 * @author Sam Glidden
 *
 */
public class Methods {

	/**
	 * Converts a list of Ithings into a list of Things
	 */
	public static List<Entity> convertFromIthingList(List<ImmutableEntity> l) {
		List<Entity> result = new ArrayList<Entity>();
		for (ImmutableEntity item: l) {
			result.add(item.getThing());
		}
		return result;
	}
	
	public static Set<Entity> convertFromIthingSet(Set<ImmutableEntity> l) {
		Set<Entity> result = new HashSet<Entity>();
		for (ImmutableEntity item: l) {
			result.add(item.getThing());
		}
		return result;
	}
	
	/**
	 * Returns true if parent contains a Thing equivalent to t.
	 */
	public static boolean containsThing(Entity parent, Entity t) {
		ImmutableEntity ithing = new ImmutableEntity(t);
		List<Entity> subThings = Chain.flattenThing(parent);
		for (Entity sub : subThings) {
			ImmutableEntity isub = new ImmutableEntity(sub);
			if (isub.equals(ithing)) return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true if parent contains an ImmutableEntity equal to t.
	 */
	public static boolean containsIthing(ImmutableEntity parent, ImmutableEntity t) {
		List<Entity> subThings = Chain.flattenThing(parent.getThing());
		for (Entity sub : subThings) {
			ImmutableEntity isub = new ImmutableEntity(sub);
//			System.out.println("ISUB: "+isub);
//			System.out.println("T: "+t);
			if (isub.equals(t)) {
				return true;
			}
		}
		return false;
	}
	

	
}

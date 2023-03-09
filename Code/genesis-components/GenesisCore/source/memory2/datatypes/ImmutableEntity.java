package memory2.datatypes;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Bundle;
import frames.entities.Entity;

/**
 * Quick and dirty immutable Entity. The primary motivation of this
 * is to remove the uniquifiers from regular Things -- I want a
 * good equals() and hashCode(). The class is then made immutable
 * so that it is friendly with HashSets and the like.
 * 
 * @author sglidden
 *
 */
public final class ImmutableEntity {
	
	private final Entity t;
	// this is for the sake of performance
	private final List<Bundle> flat;
	
	public ImmutableEntity(Entity t) {
		this.t = t;
		List<Entity> flatThing = Chain.flattenThing(this.t);
		flat = new ArrayList<Bundle>(flatThing.size());
		for (Entity el : flatThing) {
			flat.add(el.getBundle());
		}
	}
	
	public Entity getThing() {
//		System.out.println(t);
//		System.out.println(t.deepClone());
		return t.deepClone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flat == null) ? 0 : flat.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ImmutableEntity other = (ImmutableEntity) obj;
		if (flat == null) {
			if (other.flat != null)
				return false;
		} else if (!flat.equals(other.flat))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImmutableEntity: "+flat.toString();
	}

}

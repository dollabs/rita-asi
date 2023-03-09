package memory.soms.mergers;
import java.util.Set;

import frames.entities.Entity;
import memory.soms.ElementMerger;
//Don't do any merging.
public class NullMerger implements ElementMerger<Entity> {
	public Set<Entity> merge(Entity seed, Set<Entity> neighbors) {
		return neighbors;
	}
}

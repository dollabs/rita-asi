package memory.soms.metrics;
import frames.entities.Entity;
import memory.soms.DistanceMetric;
import memory.utilities.Distances;
/**
 * Compares two Things.
 * 
 * @author sglidden
 *
 */
public class EntityMetric implements DistanceMetric<Entity> {
	public double distance(Entity e1, Entity e2) {
		return Distances.distance(e1, e2);
	}
}

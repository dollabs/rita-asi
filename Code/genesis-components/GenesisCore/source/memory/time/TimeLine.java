package memory.time;
import java.util.HashMap;
import java.util.Map;

import frames.entities.Entity;
import utils.graphs.DirectedMultiGraph;
/**
 * Implements a time relation module, so we can keep track
 * of time relationships between Frames.
 * 
 * Timeline is threadsafe.
 * 
 * @author sglidden
 *
 */
public class TimeLine implements Time {
	
	private static TimeLine timeline;
	
	public static TimeLine getTimeLine() {
		if (timeline==null) {
			timeline = new TimeLine();
		}
		return timeline;
	}
	
	// graph holds all the relations
	// nodes are Frames, edges are TimeRelations
	private DirectedMultiGraph<Entity, TimeRelation> graph = new DirectedMultiGraph<Entity, TimeRelation>();
	
	
	public synchronized void addBefore(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.before);
	}
	
	public synchronized void addMeets(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.meets);
	}
	public synchronized void addStarts(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.starts);
		graph.addEdge(b, a, TimeRelation.starts);
	}
	public synchronized void addFinishes(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.finishes);
		graph.addEdge(b, a, TimeRelation.finishes);
	}
	public synchronized void addOverlaps(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.overlaps);
	}
	public synchronized void addDuring(Entity sub, Entity sup) {
		graph.addEdge(sub, sup, TimeRelation.during);
	}
	public synchronized void addEquals(Entity a, Entity b) {
		graph.addEdge(a, b, TimeRelation.equals);
		graph.addEdge(b, a, TimeRelation.equals);
	}
	
	public synchronized TimeRelation getRelation(Entity a, Entity b) {
		// ideally, there should only be one time relation
		// between two frames. So we should just return it.
		for (TimeRelation t : graph.getEdgesBetween(a, b)) {
			return t;
		}
		return null;
	}
	
	private Map<Entity, Long> timestamps = new HashMap<Entity, Long>();
	
	/**
	 * Returns the timestamp for when the Thing t was experienced
	 * 
	 * @param t
	 * @return
	 */
	public long getTimestamp(Entity t) {
		if (timestamps.get(t)==null) return 0;
		return timestamps.get(t);
	}
	
	public void timestamp(Entity t, long time) {
		timestamps.put(t, time);
	}
//	public synchronized Set<TimeFrame> getContext(Thing f) {
//		Set<TimeFrame> frames = new HashSet<TimeFrame>();
//		for (Frame a : graph.getSuccessors(f)) {
//			// TODO: construct TimeFrame from f, a, relation
//			getRelation(f, a);
//		}
//		for (Frame b : graph.getPredecessors(f)) {
//			// TODO: construct TimeFrame from b, f, relation
//			getRelation(b, f);
//		}
//		return frames;
//	}
}

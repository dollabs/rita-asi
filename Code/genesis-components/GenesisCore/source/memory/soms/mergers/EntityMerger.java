package memory.soms.mergers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;
import memory.distancemetrics.Operations;
import memory.distancemetrics.Point;
import memory.distancemetrics.ThreadWithSimilarityDistance;
import memory.soms.ElementMerger;
/**
 * Thing Merger for self-organizing maps. Only handles the top
 * level of a Thing.
 * 
 * @author sglidden
 *
 */
public class EntityMerger implements ElementMerger<Entity> {
	
	
	public Set<Entity> merge(Entity seed, Set<Entity> neighbors) {
		Set<Entity> results = new HashSet<Entity>();
		
		for (Entity targ : neighbors) {
			results.add(merge(seed, targ));
		}
		return results;	
	}
	/**
	 * Returns a Thing which is a version of targ made
	 * more like seed. The Threads on targ's Bundles are
	 * pruned to be more similar to seed's Threads.
	 * 
	 * @param seed Thing
	 * @param targ Thing to push towards seed
	 * @return a new Thing to replace targ
	 */
	public static Entity merge(Entity seed, Entity targ) {
		Entity newThing = new Entity();
		Bundle newBundle = new Bundle();
		newThing.setBundle(newBundle);
		// get optimal pairing of threads in the two bundles
		// uses Adam the Red's crazy code
		List<Point<Thread>> seedList = getPointList(seed.getBundle());
		List<Point<Thread>> targList = getPointList(targ.getBundle());
		Map<Point<Thread>, Point<Thread>> bestMatches = Operations.hungarian(seedList, targList);
		// keys of the map are the shorter list; need to determine which that was
		if (bestMatches.keySet().containsAll(seedList)) {
			// seedList is keys: prune values
			for (Point<Thread> t: bestMatches.keySet()) {
				Thread mergedThread = pruneThread(t.getWrapped(), bestMatches.get(t).getWrapped());
				targList.remove(bestMatches.get(t));
				newBundle.add(mergedThread);
			}
			// add remaining threads
			for (Point<Thread> remaining : targList) {
				newBundle.add(remaining.getWrapped());
			}
		}
		else {
			// else targList is keys: prune keys
			for (Point<Thread> t: bestMatches.keySet()) {
				Thread mergedThread = pruneThread(bestMatches.get(t).getWrapped(), t.getWrapped());
				newBundle.add(mergedThread);
			}
		}
		
		return newThing;
	}
	private static List<Point<Thread>> getPointList(Bundle a) {
		List<Point<Thread>> aa = new ArrayList<Point<Thread>>(a.size());
		for(Thread t:a){
			aa.add(new ThreadWithSimilarityDistance(t));
		}
		return aa;
	}
	
	
	
	private static Thread pruneThread(Thread base, Thread prune) {
		if (prune.isEmpty()) return prune;
		if (base.equals(prune)) {
			return new Thread(prune);
		}
		if (base.contains(prune.get(prune.size()-1))) {
			return new Thread(prune);
		}
		Thread pruned = new Thread(prune);
		pruned.remove(prune.get(prune.size()-1));
		return pruned;
	}
}

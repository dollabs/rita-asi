package memory.soms.mergers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;
import lexicons.WorkingVocabulary;
import memory.distancemetrics.Operations;
import memory.distancemetrics.Point;
import memory.distancemetrics.ThreadWithSimilarityDistance;
import memory.soms.ElementMerger;
import memory.utilities.Distances;
/**
 * Merges two Entities of any type, deeply merging components. Component 
 * threads are only changed if Gauntlet has experienced a shared
 * supertype on each thread (where experienced equals 
 * WorkingVocabulary.getWorkingVocabulary().contains("hawk")).
 * 
 * @author sglidden
 *
 */
public class ConditionalMerger implements ElementMerger<Entity> {
	public Set<Entity> merge(Entity seed, Set<Entity> neighbors) {
		Set<Entity> newThings = new HashSet<Entity>();
		for (Entity n : neighbors) {
			Entity newThing = merge(seed, n);
			newThings.add(newThing);
		}
		
		return newThings;
	}
	
	/**
	 * Deeply merges n towards seed.
	 * 
	 * @param seed Thing
	 * @param n Thing
	 * @return newThing which is n made more like seed.
	 */
	public static Entity merge(Entity seed, Entity n) {
		Entity newThing = n.deepClone();
		
		newThing.setBundle(condMerge(seed, n).getBundle());
		// recursively merge the children
		mergeChildren(seed, newThing);
		
		return newThing;
	}
	
	private static void mergeChildren(Entity seed, Entity n) {
		if (n.getChildren().isEmpty()) return;
		Map<Entity, Entity> pairing = getOptimalPairing(n.getChildren(), seed.getChildren());
//		System.out.println("seed children: "+seed.getChildren());
//		System.out.println("n children: "+n.getChildren());
//		System.out.println("pairing: "+pairing);
		for (Entity matchingChild : pairing.keySet()) {
			Entity seedChild = pairing.get(matchingChild);
			if (seedChild!=null) {
				// update the child's bundle
//				System.out.println("seedChild: "+seedChild);
//				System.out.println("matchingChild: "+matchingChild);
//				System.out.println("merge result: "+EntityMerger.merge(seedChild, matchingChild));
				matchingChild.setBundle(condMerge(seedChild, matchingChild).getBundle());
				// merge that child's children
				mergeChildren(seedChild, matchingChild);
			}
		}
	}
	
	
	// returns a map from Things in set 1 to Things in set to match the closest things
	private static Map<Entity, Entity> getOptimalPairing(Set<Entity> a, Set<Entity> b) {
		Set<Entity> firstSet = new HashSet<Entity>(a);
		Set<Entity> secondSet = new HashSet<Entity>(b);
		
		Map<Entity, Entity> mapping = new HashMap<Entity, Entity>();
		
		while (!firstSet.isEmpty() && !secondSet.isEmpty()) {
			Entity[] match = getBestMatch(firstSet, secondSet);
			mapping.put(match[0], match[1]);
			firstSet.remove(match[0]);
			secondSet.remove(match[1]);
		}
		
		if (!firstSet.isEmpty()) {
			for (Entity leftover : firstSet) {
				mapping.put(leftover, null);
			}
		}
		
		return mapping;
	}
	
	// finds the best pair between all the Things in Set a and Set b
	private static Entity[] getBestMatch(Set<Entity> a, Set<Entity> b) {
		Entity bestT1 = null;
		Entity bestT2 = null;
		double bestScore = 2;
		for (Entity t1 : a) {
			for (Entity t2 : b) {
				double score = Distances.distance(t1, t2);
				if (score < bestScore) {
					bestT1 = t1;
					bestT2 = t2;
					bestScore = score;
				}
			}
		}
		Entity[] results = new Entity[2];
		results[0] = bestT1;
		results[1] = bestT2;
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
	public static Entity condMerge(Entity seed, Entity targ) {
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
		// check if we should actually merge the thread;
		// i.e. if a known supertype is present to merge towards
		Thread pruned = new Thread(prune);
//		System.out.println("SOMS!  prior to for loop");
		for (int i=prune.size()-2; i>2; i--) {	// HACK ALERT: skip top levels of threads, so we never merge to "thing" or likewise!
			// walk up prune, looking for a good merge point
			String testword = prune.get(i);
//			System.out.println("SOMS!  testword is: "+testword);
			if (base.contains(testword) && WorkingVocabulary.getWorkingVocabulary().contains(testword)) {
				// found a target!
				pruned.remove(i+1, pruned.size());
//				System.out.println("SOMS!  pruning thread");
				break;
			}
		}
		
//		System.out.println("SOMS! returning");
		return pruned;
	}
	
	
}

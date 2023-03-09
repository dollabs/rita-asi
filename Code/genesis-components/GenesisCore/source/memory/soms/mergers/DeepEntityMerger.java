package memory.soms.mergers;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import frames.entities.Entity;
import memory.soms.ElementMerger;
import memory.utilities.Distances;
/**
 * Merges two Things of any type, deeply merging components. Should
 * work with Derivatives, Relations, Sequences, etc to any depth.
 * 
 * @author sglidden
 *
 */
public class DeepEntityMerger implements ElementMerger<Entity> {
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
		
		newThing.setBundle(EntityMerger.merge(seed, n).getBundle());
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
				matchingChild.setBundle(EntityMerger.merge(seedChild, matchingChild).getBundle());
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
}

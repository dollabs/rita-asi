package memory.utilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;
import memory.distancemetrics.Operations;
import utils.EntityUtils;
public class Distances {
	
	/**
	 * Computes the distance between two Things, of
	 * arbitrary complexity (Derivatives, Sequences, whatever).
	 * 
	 * Matches children by structure, computes their distance, and
	 * averages for a total result. The average is computed by
	 * weighted averages taken at each level.
	 * 
	 * @param t1 Thing
	 * @param t2 Thing
	 * @returns 1 as max distance, 0 as no distance
	 */
	public static double distance(Entity t1, Entity t2) {
		if (t1.getChildren().isEmpty() && t2.getChildren().isEmpty()) {
			return distance(t1.getBundle(), t2.getBundle());
		}
		double score = ((double) 1/3)*getChildrenAverage(EntityUtils.getOrderedChildren(t1), EntityUtils.getOrderedChildren(t2))
		+ ((double) 2/3)*distance(t1.getBundle(), t2.getBundle());
//		System.out.println("bundle score: "+distance(t1.getBundle(), t2.getBundle()));
//		System.out.println("Returning score: "+score);
		return score;
	}
	private static double getChildrenAverage(List<Entity> t1Level, List<Entity> t2Level) {
		// optimally match Things within level
		List<Entity> temp1 = new ArrayList<Entity>(t1Level);
		List<Entity> temp2 = new ArrayList<Entity>(t2Level);
		// NOTE: there's a lot of room for optimization here;
		// NeedlemanWunsch recursively calculates the distances between things,
		// which is then recalculated later in this method.
		Map<Entity, Entity> pairing = NeedlemanWunsch.pair(temp1, temp2);
		
		double sum = 0;
		int count = 0;
		// get distances of paired elements
		for(Entity p1: pairing.keySet()) {
			count++;
			sum += distance(p1, pairing.get(p1));
		}
		// unpaired Things contribute a distance of 1
		temp1.removeAll(pairing.keySet());
		for (Entity t : temp1) {
			sum+=1;
			count++;
		}
		temp2.removeAll(pairing.values());
		for (Entity t : temp2) {
			sum+=1;
			count++;
		}
		double avg = (double) sum/count;
//		System.out.println("Returning avg for children: "+avg);
		return avg;
	}
	
	
	/**
	 * Returns a distance between two bundles, optimally
	 * pairing each thread and taking their average distance.
	 * 
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static double distance(Bundle b1, Bundle b2) {
//		System.out.println("bundle1: "+b1);
//		System.out.println("bundle2: "+b2);
		if(b1.size()==0 || b2.size()==0)return 1.;
		List<ThreadPoint> b1Threads = new ArrayList<ThreadPoint>(b1.size());
		List<ThreadPoint> b2Threads = new ArrayList<ThreadPoint>(b2.size());
		for(Thread t : b1){
			b1Threads.add(new ThreadPoint(t));
		}
		for(Thread t : b2){
			b2Threads.add(new ThreadPoint(t));
		}
		Map<ThreadPoint, ThreadPoint> optimalPairing = Operations.hungarian(b1Threads, b2Threads);
		
		double score = 0;
		for (ThreadPoint p : optimalPairing.keySet()) {
			score += p.getDistanceTo(optimalPairing.get(p));
		}
		
		// add unmatched elements
		double unmatched = Math.max(b1Threads.size(), b2Threads.size()) - optimalPairing.keySet().size();
		score += unmatched;
		return (double) score/Math.max(b1Threads.size(), b2Threads.size());
	}
	
	/**
	 * Computes an edit distance between two threads. Basically,
	 * it's the number of changes needed to make one Thread into
	 * the other (a change can be an insertion, deletion, or 
	 * reordering) divided by the number of elements in the 
	 * longest thread.
	 * 
	 * @param t1 Thread
	 * @param t2 Thread
	 * @return double distance between 0 and 1.
	 */
	public static double distance(Thread t1, Thread t2) {
		if (t1==null && t2==null) return 0;
		else if (t1==null || t2==null) return 1;
		if (t1.isEmpty() && t2.isEmpty()) return 0;
		else if (t1.isEmpty() || t2.isEmpty()) return 1;
		Thread larger;
		Thread smaller;
		boolean t1Larger = t1.size() > t2.size();
		if (t1Larger) {
			larger = new Thread(t1);
			smaller = new Thread(t2);
		}
		else {
			larger = new Thread(t2);
			smaller = new Thread(t1);
		}
		Thread overlap = new Thread();
		for (String s : larger) {
			if (smaller.contains(s)) {
				overlap.add(s);
			}
		}
		// find the version of overlap that is a subset
		// of larger
		double ans = shortenTillSubSet(overlap, larger);
//		System.out.println("Thread compare "+ ans);
		return ans;
	}
	
	private static double shortenTillSubSet(Thread overlap, Thread larger) {
		if (SubSetUtils.isSubSet(overlap, larger)) {
			return ((double) (larger.size()-overlap.size())/larger.size());
		}
		Set<Double> scores = new HashSet<Double>();
		for (String s: overlap) {
			Thread temp = new Thread(overlap);
			temp.remove(s);
			double score = shortenTillSubSet(temp, larger);
			scores.add(score);
		}
		return Collections.min(scores);
	}
}

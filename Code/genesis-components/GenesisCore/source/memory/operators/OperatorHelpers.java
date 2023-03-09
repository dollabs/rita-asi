package memory.operators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import frames.entities.Entity;
import frames.entities.Sequence;
import frames.entities.Thread;
/** A bunch of functions that make operations on Things, Derivatives, Sequences possible.
 * @author Sam Glidden
 *
 */
public class OperatorHelpers {
	
	/**
	 * @param thread
	 * @param things
	 * @return Number of things in things thread does NOT occur
	 */
	public static int numberLacking(Thread thread, ArrayList<Entity> things) {
		int count = 0;
		for (Entity thing : things) {
			if (!(thing.getBundle().contains(thread))) {
				count++;
			}
		}
		return count;
	}
	
	protected static int getNumberCrossing(ArrayList<Integer> match, ArrayList<ArrayList<Integer>> lines) {
		int counter=0;
		int i = match.get(0);
		int j = match.get(1);
		for (int a=0; a<lines.size(); a++) {
			if (i<lines.get(a).get(0) && j>lines.get(a).get(1)) {
				counter++;
			}
			if (i>lines.get(a).get(0) && j<lines.get(a).get(1)) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @return: list of matching (i.e. similar) Things in the Sequence.
	 * If the sequences are: [A B C D] and [B E C], the return will be
	 * [[1, 0], [2, 2]]. Note that the Things do not have to be identical, 
	 * merely similar, as defined by a Operators.compare() call.
	 */
	protected static ArrayList<ArrayList<Integer>> getLinks(Sequence s1, Sequence s2) {
		ArrayList<ArrayList<Integer>> matches = new ArrayList<ArrayList<Integer>>();
		for (int i=0; i<s1.getElements().size(); i++) {
//			System.out.println(s1.getElement(i)+" "+s2.getElement(getIndexBestMatch(s1.getElement(i), s2).get(0)));
			for (int k: getIndexBestMatch(s1.getElement(i), s2)) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(i);
				temp.add(k);
				matches.add(temp);
			}
		}
		
		System.out.println("Raw matches: "+matches);
		
		while (true) {
			ArrayList<Integer> worst = new ArrayList<Integer>();
			worst.add(0);		// num crossing
			worst.add(-1);		// index
			for (int i=0; i<matches.size(); i++) {
				int num = getNumberCrossing(matches.get(i), matches);
				if (worst.get(0) < num) {
					worst.set(0, num);
					worst.set(1, i);
				}
			}
			if (worst.get(1) == -1) {break;}
			int removeInt = worst.get(1);
			matches.remove(removeInt);
		}
		System.out.println("Cross-free matches: "+matches);
		// make sure no element has multiple matches
		for (int i=0; i<matches.size(); i++) {
			for (int j=0; j<matches.size(); j++) {
				if (j != i) {
					if (matches.get(i).get(0) == matches.get(j).get(0) || matches.get(i).get(1) == matches.get(j).get(1))
						matches.remove(j);
				}
			}
		}
		System.out.println("Final matches: "+matches);
		return matches;
	}
	
	protected static ArrayList<Integer> getIndexBestMatch(Entity t, Sequence s) {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		ArrayList<Double> scores = new ArrayList<Double>();
		for (Entity targ : s.getElements()) {
			scores.add(Operators.compare(t, targ));
		}
		double minVal = Collections.min(scores);
		// LOOK: this is where we decide how similar Things in Sequences need to be to be considered matches.
		if (minVal < .5) {
			while (true) {
				int occurance = scores.lastIndexOf(minVal);
				if (occurance != -1) {
					ints.add(occurance);
					scores.subList(occurance, scores.size()).clear();
				}
				else {break;}
			}	
		}
		return ints;
	}
	/**
	 * @param targ
	 * @param things
	 * @return Things that are almost identical to targ; that is, Things that
	 * differ only by the final element(s) on there threads.
	 * 
	 * Slow only if you don't properly restrict the input ArrayList of things.
	 */
	public static ArrayList<Entity> getNearMisses(Entity targ, ArrayList<Entity> things) {
		HashSet<Entity> results = new HashSet<Entity>();
		for (Thread t : targ.getBundle()) {
			for (Entity miss : things) {
				for (Thread m : miss.getBundle()) {
					if (t.getSupertype().equals(m.getSupertype())) {
						results.add(miss);
					}
				}
			}
 		}
		return new ArrayList<Entity>(results);
	}
	
	/**Used for creating more generic sequences: takes in a 2d list of things, and
	 * returns the length of the shortest sub-list, with the exception of 'except'.
	 * 
	 * @param list
	 * @param except
	 * @return length of shortest sub-list in 2d list
	 */
	protected static int getShortestList(ArrayList<ArrayList<Entity>> list, int except) {
		int l = Integer.MAX_VALUE;
		for (int i=0; i<list.size(); i++) {
			if (i == except)
				continue;
			if (list.get(i).size() < l)
				l=list.get(i).size();
		}
		return l;
	}
	
	
	
}

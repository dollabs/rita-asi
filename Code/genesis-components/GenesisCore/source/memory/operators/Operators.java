package memory.operators;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import frames.entities.Thread;
import frames.memories.BasicMemory;
/**
 * @author Sam Glidden
 * @modified 2007.5.31
 * 
 * A bunch of static operators, useful for Things and Threads
 *
 *	DEPRECATED: There are now better ways to do everything in here.
 *	See utilities/Distances and soms/mergers and soms/metrics.
 */
@Deprecated
public class Operators {
	
	/**
	 * @param t1 Thread
	 * @param t2 Thread 
	 * @return double: 0 if equal, 1 if completely different.
	 * 
	 * Compares two threads. 
	 * The value returned is the number of differing elements
	 * between the two Threads divided by the total number of
	 * elements in both Threads.
	 * 
	 */
	
//	public static double compare(Thread t1, Thread t2) {
//		ArrayList<Integer> links = new ArrayList<Integer>();
//		for (int i = t1.size()-1; i>-1; --i) {
//			for (int j=t2.size()-1; j>-1; --j) {
//				if (t1.get(i).equals(t2.get(j))) {
//					links.add((t1.size()-1-i)+(t2.size()-1-j));
//				}
//			}
//		}
//		if (links.size() > 0) { 
//			int steps = Collections.min(links); 
//			return (double) steps/(t1.size()+t2.size());
//		}
//		else { return 1; }
//	}
	
	public static double compare(Thread t1, Thread t2) {
		if (t1==null && t2==null) {return 0.;}
		if (t1==null || t2==null) {return 1.;}
		List<String> a = new ArrayList<String>(t1);
		List<String> b = new ArrayList<String>(t2);
		
		int i = 0;
		while (i<a.size()) {
			int j = 0;
			boolean advance = true;
			while (j<b.size()) {
				if (a.get(i).equals(b.get(j))) {
					a.remove(i);
					b.remove(j);
					advance=false;
					break;
				}
				else {j++;}
			}
			if (advance) {i++;}
		}
		
		int unmatched = a.size() + b.size();
		return (double) unmatched/(t1.size()+t2.size());
	}
	
	
	
	/**
	 * Compares two bundles.
	 * 
	 * @param b1
	 * @param b2
	 * @return double
	 */
	public static double compare(Bundle bundle1, Bundle bundle2) {
		
		Set<Thread> b1 = new HashSet<Thread>(bundle1);
		Set<Thread> b2 = new HashSet<Thread>(bundle2);
		
		Set<Double> scores = new HashSet<Double>();
		while (!b1.isEmpty() && !b2.isEmpty()) {
			List<Thread> best = getBestMatch(b1, b2);
			scores.add(compare(best.get(0), best.get(1)));
			b1.remove(best.get(0));
			b2.remove(best.get(1));
		}
 
		
		// adjust scores for uneven bundle sizes
		for (int i=0; i<Math.max(b1.size(), b2.size()); i++) {
			scores.add(1.);
		}
		// average scores: is this the best way to do this?
		double total = 0;
		for (double d : scores) {
			total = total + d;
		}
//		System.err.println("Comparision complete: " + total/scores.size());
		return total/scores.size();
	}
	
	
	private static List<Thread> getBestMatch(Set<Thread> b1, Set<Thread> b2) {
		Thread bestT1 = null;
		Thread bestT2 = null;
		double bestScore = 2;
		for (Thread t1 : b1) {
			for (Thread t2 : b2) {
				double score = compare(t1, t2);
				if (score < bestScore) {
					bestT1 = t1;
					bestT2 = t2;
					bestScore = score;
				}
			}
		}
		List<Thread> results = new ArrayList<Thread>();
		results.add(bestT1);
		results.add(bestT2);
		return results;
	}
	
	/**
	 * Compares two Things. This comparison is done by comparing the Things' 
	 * top level bundle. Sub-Things are ignored. The threads in the two bundles
	 * are matched optimally, and the average distance between the optimally
	 * paired Threads is returned.
	 * 
	 * @param thing1 Thing
	 * @param thing2 Thing
	 * @return double range from 0 to 1, 0 meaning identical, 1 meaning completely
	 * different.
	 */
	public static double compare(Entity thing1, Entity thing2) {
		if (thing1==null && thing2==null) {
			return 0;
		}
		if (thing1==null || thing2==null) {
			return 1;
		}
		return compare(thing1.getBundle(), thing2.getBundle());
	}
	
	
	/**
	 * @param d1 Derivative.
	 * @param d2 Derivative.
	 * @return double.
	 * 
	 * Dirivatives must have the same surface threads to be equal at all. If they do, 
	 * then the comparison drops down the the subject level. If they don't,
	 * then they are considered completely different in the SOM implementation.
	 */
	public static double compare(Function d1, Function d2) {
		if (Operators.compare((Entity) d1, (Entity) d2) == 0) {
			if (d1 == null && d2 == null) {
				return 0;
			}
			if (d1 == null || d2 == null)
				return 1;
			return Operators.compare(d1.getSubject(), d2.getSubject());
		}
		return 1;
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @return a comparison of the sequences: 0 is equal, 1 is completely different
	 */
	public static double compare(Sequence s1, Sequence s2) {
		// find matches of similar Things in the Sequences
		ArrayList<ArrayList<Integer>> matches = OperatorHelpers.getLinks(s1, s2);
		// now, using matches as optimal links, compare the things
		double matchQual = 0;
		int totalEls = s1.getElements().size()+s2.getElements().size();
		int unmatched = (s1.getElements().size()-matches.size()) + (s2.getElements().size()-matches.size());
		for (ArrayList<Integer> m : matches) {
			matchQual=+Operators.compare(s1.getElement(m.get(0)), s2.getElement(m.get(1)));
		}
		if (matches.size() > 0) 
			matchQual = matchQual/matches.size();
		System.out.println("matchQual: "+matchQual);
		System.out.println("unmatched: "+unmatched);
		System.out.println("totalEls: "+totalEls);
		return (unmatched+matchQual)/totalEls;
	}
	
	/**
	 * @param prime
	 * @param things
	 * @return using prime as a baseline, tries to compare things to total knowledge in
	 * memory to create a new, generic thing. A new thing will be created only if the
	 * method thinks there are enough specific cases in things to support a generalization.
	 * Otherwise, if the majority of things equal prime, then prime will be returned.
	 */
	public static ArrayList<Entity> generalize(Entity prime, ArrayList<Entity> things) {
		ArrayList<Entity> results = new ArrayList<Entity>();
//		System.out.println("prime: "+prime);
		if (prime == null) {
			return results;
		}
		things = OperatorHelpers.getNearMisses(prime, things);
		for (Thread t: prime.getBundle()) {
			if (Operators.getInstancesInMemory(t.getSupertype()) != 0) {
				if ((double) ((1+OperatorHelpers.numberLacking(t, things))/Operators.getInstancesInMemory(t.getSupertype())) > .5) {
					Entity newThing = new Entity(t.getSupertype());
					BasicMemory.getStaticMemory().extendVia(newThing, "thing");
					results.add(newThing);
				}
			}
		}
//		System.out.println("num in list: "+countInstances(prime, things));
//		System.out.println("size of list: "+things.size());
		if (countInstances(prime, things)*2 > things.size()) {
			if (results.size() == 0) {
				results.add(prime);
			}
		}
		return results;	
	}
		
	protected static int countInstances(Entity t, List<Entity> l) {
		int counter=0;
		for (Entity current : l) {
			if (current.isEqual(t))
				counter++;
		}
		return counter;
	}
	
	protected static int countInstances(Sequence t, List<Sequence> l) {
		int counter=0;
		for (Sequence current : l) {
			if (current.isEqual(t))
				counter++;
		}
		return counter;
	}
	
	/**
	 * Same as generalize for Thing, but for Derivatives. If the Derivatives have different type threads,
	 * no inferences are made. If they are of the same type, then we generalize their subjects.
	 * This means that a generalized derivative could be identical to the provided one.
	 */
	public static ArrayList<Function> generalize(Function prime, ArrayList<Function> derivs) {
		ArrayList<Function> results = new ArrayList<Function>();
		if (prime==null) {
			return results;
		}
		
		// remove any derivatives of a different type
		for (int i=0; i<derivs.size(); i++) {
			if (Operators.compare((Entity) prime, (Entity) derivs.get(i)) == 1) {
				derivs.remove(i);
			}
		}
		ArrayList<Entity> subjects = new ArrayList<Entity>();
		for (Function d : derivs) {
			subjects.add(d.getSubject());
		}
		for (Entity newThing : generalize(prime.getSubject(), subjects)) {
			Function newDeriv = (Function) prime.clone();
			newDeriv.setSubject(newThing);
			results.add(newDeriv);
		}
		return results;
	}
	
	/**Takes in a prime sequence, and a list of other similar sequences. Returns
	 * sequences that more generic (i.e. threads terminate at more generic levels).
	 * If no generalizations are possible, but the majority of seqs equals prime,
	 * then prime is returned in the list.
	 * @param prime
	 * @param seqs
	 * @return: ArrayList of more generic sequences.
	 */
	public static ArrayList<Sequence> generalize(Sequence prime, ArrayList<Sequence> seqs) {
		ArrayList<Sequence> newSeqs = new ArrayList<Sequence>();
		for (Sequence s: seqs) {
			// get the matching elements of the two sequences
			ArrayList<ArrayList<Integer>> links = OperatorHelpers.getLinks(prime, s);
			ArrayList<ArrayList<Entity>> newThings = new ArrayList<ArrayList<Entity>>();
			for (int i=0; i<links.size(); i++) {
				ArrayList<Entity> wrapper = new ArrayList<Entity>();
				wrapper.add(s.getElement(links.get(i).get(1)));
				ArrayList<Entity> emptyList = new ArrayList<Entity>();
				newThings.add(i, emptyList);
				for (Entity newThing : Operators.generalize(prime.getElement(links.get(i).get(0)), wrapper)) {
					newThings.get(i).add(newThing);
				}
			}
			// another really ugly looking structure
			for (int i=0; i<newThings.size(); i++) {
				for (int j=0; j<newThings.get(i).size(); j++) {
					for (int k=0; k<OperatorHelpers.getShortestList(newThings, i); k++) {
						Sequence newSeq = (Sequence) prime.clone();
						newSeq.clearElements();
						for (int l=0; l<newThings.size(); l++) {
							newSeq.getElements().add(l, newThings.get(l).get(k));
						}
						newSeq.getElements().set(i, newThings.get(i).get(j));
						// make sure we figured something out, and that it is isn't redundant
						if (newSeq.getElements().size() > 0) {
							boolean isNew = true;
							for (Sequence seq: newSeqs) {
								if (newSeq.isEqual(seq)) {
									isNew = false;
								}
							}
							if (isNew) {
								newSeqs.add(newSeq);
							}
						}
					}
				}
			}
		}
		if (countInstances(prime, seqs)*2 > newSeqs.size()) {
			newSeqs.add(prime);
		}
		return newSeqs;
	}
	
	
	/**
	 * @param type
	 * @return counts the number of threads in memory with type as their supertype.
	 * Essentially getting the size of the domain of knowledge about type.
	 * 
	 * As written, this is really slow.
	 */
	public static int getInstancesInMemory(String type) {
		int count = 0;
		for (Object o : BasicMemory.getStaticMemory().getThings(type)) {
			Entity thing = (Entity) o;
			for (Thread t : thing.getBundle()) {
				if (type.equals(t.getSupertype())) {
					count++;
				}
			}
		}
		return count;
	}
	
	
	
	// testing testing
	public static void main (String[] ignore) {
		Thread a = new Thread();
		Thread b = new Thread();
		a.add("thing");
		b.add("thing");
		a.add("place");
		b.add("place");
		a.add("property");
		a.add("garage");
		b.add("property");
		b.add("home");
		b.add("house");
		//System.out.println(a);
		//System.out.println(b);
		//System.out.println(Operators.compare(a, b));
		Entity t1 = new Entity();
		Entity t2 = new Entity();
		Entity t3 = new Entity();
		t1.addTypes("thing", "person sam");
		//t1.addTypes("thing", "dog cat");
		t2.addTypes("thing", "person Harrison");
		t2.addTypes("thing", "person Joe");
		BasicMemory.getStaticMemory().store(t1);
		BasicMemory.getStaticMemory().store(t2);
		BasicMemory.getStaticMemory().store(t3);
		//System.out.println(t1);
		//System.out.println(t2);
		//System.out.println(Operators.compare(t1, t2));
		
//		Thing t3 = new Thing();
//		Thing t4 = new Thing();
//		t3.addTypes("thing", "living animal cat");
//		t3.addTypes("place", "artifical garage");
//		t4.addTypes("thing", "living animal dog");
//		t4.addTypes("place", "artifical house home");
//		System.out.println(t3);
//		System.out.println(t4);
//		Operators.moveThing(t3, t4);
//		System.out.println(t3);
//		System.out.println(t4);
		
//		Derivative d1 = new Derivative("burned", t1);
//		Derivative d2 = new Derivative("burned", t2);
		
//		System.out.println(d1);
//		System.out.println(d2);
		
		ArrayList<Entity> frames = new ArrayList<Entity>();
		frames.add(t1);
		frames.add(t2);
		
//		System.out.println(Operators.generalize(t1, frames));
		
		Entity book = new Entity("book");
		Entity shelf = new Entity("shelf");
		Entity table = new Entity("table");
//		Thing timeInterval = new Thing("just now");
		Vector<Entity> fArgs = new Vector<Entity>();
		fArgs.addElement(table);
		fArgs.addElement(table);
		fArgs.addElement(table);
		fArgs.addElement(book);
//		Sequence fall = AFactory.createNewEvent("fall", fArgs);
		Vector<Entity> gArgs = new Vector<Entity>();
		gArgs.addElement(shelf);
		gArgs.addElement(table);
		gArgs.addElement(shelf);
		gArgs.addElement(book);
//		Sequence gall = AFactory.createNewEvent("gall", gArgs);
		Vector<Entity> hArgs = new Vector<Entity>();
		hArgs.addElement(shelf);
		hArgs.addElement(table);
		hArgs.addElement(table);
		hArgs.addElement(book);
//		Sequence hall = AFactory.createNewEvent("hall", hArgs);
//		ArrayList<Sequence> seqs = new ArrayList<Sequence>();
//		seqs.add(gall);
//		seqs.add(hall);
//		ArrayList<Sequence> seq = Operators.generalize(fall, seqs);
//		System.out.println(Operators.generalize(fall, seqs));
	}
}

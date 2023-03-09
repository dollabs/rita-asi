package tomLarson;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;

import connections.AbstractWiredBox;
import connections.Connections;
import expert.SimpleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import frames.entities.Thread;

/**
 * The disambiguator lives between the Transformer and the Demultiplexor. Its
 * job is to determine the best threads for words, given words with multiple
 * possible uses. For example, a "hawk" may be a bird or a militarist. The
 * disambiguator tries to figure out the appropriate use, given a number of
 * factors, including 1. Memory
 * 
 * @author tal
 */

@SuppressWarnings("rawtypes")
public class Disambiguator2 extends AbstractWiredBox {

	private Entity input;

	private JCheckBox useDisambiguator;

	private DisambiguatorMemory disambiguatorMemory;

	public final static String VIEW = "VIEW";

	public final static String VIEWBUNDLE = "VIEWBUNDLE";

	private final static boolean debug = false;

	/**
	 * Constructs a Disambiguator
	 */
	public Disambiguator2() {
		Connections.getPorts(this).addSignalProcessor("process");
		disambiguatorMemory = new DisambiguatorMemory();

	}

	public Disambiguator2(JCheckBox useDisambiguator) {
		this.useDisambiguator = useDisambiguator;
		Connections.getPorts(this).addSignalProcessor("process");
		disambiguatorMemory = new DisambiguatorMemory();
	}

	/**
	 * Process input, in accordance with the WiredBox pattern
	 * 
	 * @param o
	 */
	// The input should come in from the transformer. From this input, we can
	// pick out
	// different frame types.
	public void process(Object o) {
		input = null;
		if (o instanceof Entity) {
			// PHW added following, and switched o to input in tests and calls
			// Bug was that input was never rebound and an else was missing
			input = (Entity) o;
			if (this.useDisambiguator.isSelected()) {
				if (debug) {
					System.out.println("Disambiguator got something");
				}
				recursivelyDisambiguate(input);
				// if (input.relationP()) {
				// disambiguate(((Relation) input).getSubject());
				// disambiguate(((Relation) input).getObject());
				// }
				// // PHW added following
				// else {
				transmit(input);
				// }
			}
			else {
				transmit(input);
			}
		}
		else {
			System.err.println(o.getClass());
		}
	}

	
	private void recursivelyDisambiguate(Entity input) {
		if (input.isA("trajectory") && input.relationP()) {
			disambiguate(input);
		}
		if (input.functionP()) {
			recursivelyDisambiguate(((Function) input).getSubject());
		}
		else if (input.relationP()) {
			recursivelyDisambiguate(((Function) input).getSubject());
			recursivelyDisambiguate(((Function) input).getObject());
		}
		else if (input.sequenceP()) {
			Vector v = ((Sequence) input).getElements();
			for (Object o : v) {
				recursivelyDisambiguate((Entity)o);
			}
		}

	}

	/*
	 * Transmits a message, in accordance with the WiredBox pattern
	 * @param port
	 * @param t The output is transmitted to the Linkviewer, so that correct
	 * threads are shown, and to the demultiplexor.
	 */
	private void transmit(Entity t) {
		Connections.getPorts(this).transmit(t);
	}

	private void transmit(String s, ThreadTree t) {
		Connections.getPorts(this).transmit(s, t);
	}

	/*
	 * Disambiguate the input.
	 */
	private void disambiguate(Entity t) {
		// if (debug) {System.out.println("starting disambiguation");}
		if (t.isA("trajectory")) {
			boolean report = false;
			System.out.println("Found a trajectory to disambiguate");
			Bundle bundle = t.getSubject().getBundle();
			transmit(VIEWBUNDLE, ThreadTree.makeThreadTree(bundle));
			
			Thread thread = t.getSubject().getPrimedThread();
			ThreadTree tree = disambiguatorMemory.getThreadTree(t.getType());
			Type type = tree.getImpactofThread(thread);
			double score = type.getWeight();
			double max = score;
			for (int i = 0; i < bundle.size(); i++) {
				if (tree != null) {
					// System.out.println(bundle.get(i));
					//System.out.println(tree.getImpactofThread(bundle.get(i)));
					type = tree.getImpactofThread(bundle.get(i));
					score = type.getWeight();
					if (score > max) {
						thread = bundle.get(i);
						max = score;
						report = true;
					}
				}
			}
			disambiguatorMemory.addThread(t.getType(), thread);
			Entity newThing = new Entity();
			Bundle newBundle = new Bundle(thread);
			newThing.setBundle(newBundle);
			t.setSubject(newThing);
			if (report) {
				for (Vector x : bundle) {
					System.out.println(x);
				}
				Connections.getPorts(this).transmit(SimpleGenerator.DISAMBIGUATED, newThing);
				}
			// System.out.println(tree);
			//System.out.println(disambiguatorMemory.getThreadTree(t.getType()))
			// ;
		}
		else {
			if (debug) {
				System.out.println("Type is " + t.getType());
			}
		}

		// transmit(t);
		transmit(VIEW, disambiguatorMemory.getThreadTree(t.getType()));
	}

	/*
	 * A helper function. Finds where the best thread is located in the bundle
	 */
	@SuppressWarnings("unused")
	private int indexOfBestThread(Map<Thread, Integer> threadMap, Map<Thread, Integer> posMap) {
		int min = Integer.MAX_VALUE;
		int best = 0;
		boolean difference = false;
		assert (threadMap.keySet() != null);
		Iterator<Integer> vals = threadMap.values().iterator();
		int val = vals.next();
		while (vals.hasNext()) {
			if (vals.next() != val) {
				difference = true;
			}
		}

		for (Thread thread : threadMap.keySet()) {
			if (threadMap.get(thread) < min && !thread.contains("unknownWord")) {
				min = threadMap.get(thread);
				best = posMap.get(thread);
			}
		}
		if (!difference) {
			return -1;
		}
		else {
			return best;
		}
	}

	/*
	 * private Map<Thread,String> setPercentages(Map<Thread, Integer> threadMap,
	 * Map<Thread,Integer> posMap) { //find the total sum double sum = 0; double
	 * percent; Map<Thread, String> percentages = new HashMap<Thread, String>();
	 * for (Thread t : threadMap.keySet()) { if (threadMap.get(t) != 0) {sum +=
	 * 1.0/threadMap.get(t);} } if (sum != 0) { for (Thread t : posMap.keySet())
	 * { if (threadMap.get(t) != 0) {percent = (1.0/threadMap.get(t))/sum;
	 * percentages.put(t, String.valueOf(Math.round(percent100)).concat("%"));}
	 * } } else { for (Thread t : posMap.keySet()) { percentages.put(t,"0%"); }
	 * } return percentages; }
	 */

	/*
	 * Computes the minimum number of moves necessary to get a common ancestor
	 * between t1 to t2. TODO: Example
	 */
	/*
	 * private static int editDistance(Thread t1, Thread t2) { // int t1dist =
	 * 0, t2dist = 0; boolean stop = false; for (int i = t1.size()-1; i >=0 &&
	 * !stop; i--) { t1dist = 0; for (int j = t2.size()-1; j >=0 && !stop; j--)
	 * { if (t1.get(i).equals(t2.get(j))){ stop = true; t1dist+=t1.size()-i-1;
	 * //System.out.println(t1.get(i)); } else {t1dist++;} } } stop = false; for
	 * (int i = t2.size()-1; i >=0 && !stop; i--) { t2dist = 0; for (int j =
	 * t1.size()-1; j >=0 && !stop; j--) { if (t2.get(i).equals(t1.get(j))){
	 * stop = true; t2dist+=t2.size()-i-1; //System.out.println(t2.get(i)); }
	 * else {t2dist++;} } } return Math.min(t1dist, t2dist); }
	 */

	/*
	 * Test the Disambiguator
	 */
	public static void main(String[] args) {
		Thread t1 = new Thread();
		Thread t2 = new Thread();
		t1.add("Thing");
		t1.add("Person");
		t1.add("Tom");
		t1.add("Thing");
		t2.add("Walrus");
		t2.add("Person");
		t2.add("Monkey");
		t2.add("Paul");
		t2.add("George");
		t2.add("Ringo");
		System.out.println(t1);
		System.out.println(t2);
		// System.out.println(editDistance(t1,t2));
	}
}

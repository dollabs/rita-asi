package tomLarson;

import java.util.List;

import javax.swing.JCheckBox;

import memory.utilities.Distances;
import memory2.InputTracker;
import memory2.M2;
import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import expert.SimpleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Thread;

/**
 * The disambiguator lives between the Transformer and the Demultiplexor. Its job is to determine the best threads for
 * words, given words with multiple possible uses. For example, a "hawk" may be a bird or a militarist. The
 * disambiguator tries to figure out the appropriate use. The proof of concept was completed by Tomas Larsen (see
 * Disambiguator2).
 * 
 * @author phw
 */

public class Disambiguator3 extends AbstractWiredBox {

	private Entity input;

	private JCheckBox useDisambiguator;

	private final static boolean debug = false;

	/**
	 * Constructs a Disambiguator
	 */
	public Disambiguator3() {
		super("Disambiguator3");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public Disambiguator3(JCheckBox useDisambiguator) {
		this();
		this.useDisambiguator = useDisambiguator;

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
		// System.out.println("Hello from Disambiguator3");
		input = null;
		if (o instanceof Entity) {
			// PHW added following, and switched o to input in tests and calls
			// Bug was that input was never rebound and an else was missing
			input = (Entity) o;
			InputTracker.addTopLevelFrame(input); // sam's addition
			if (useDisambiguator == null || useDisambiguator.isSelected()) {
				if (debug) {
					System.out.println("Disambiguator3 got " + input.getTypes());
				}
				if (input.sequenceP() && input.isAPrimed(Markers.SEMANTIC_INTERPRETATION)) {
					for (Entity e : input.getElements()) {
						processElement(e);
					}
				}
				transmit(input);
			}
			else {
				transmit(input);
			}
		}
		else {
			System.err.println(o.getClass());
		}
	}

	private void processElement(Entity e) {
		Entity pivot = findPivot(e);
		// System.out.println("Pivot is: " + pivot);
		if (pivot != null) {
			Bundle bundle = pivot.getBundle();
			double closestDistance = -1;
			int closestIndex = 0;
			for (int i = 0; i < bundle.size(); ++i) {
				Thread primedThread = bundle.get(i);
				// System.out.println("Working on prime thread: " +
				// primedThread);
				Bundle primedBundle = new Bundle();
				primedBundle.add(primedThread);
				pivot.setBundle(primedBundle);

				double distance = evaluation(e, primedThread);
				if (closestDistance < 0) {
					closestDistance = distance;
					System.out.println("Candidate: A" + i + ", " + distance);
				}
				else if (distance < closestDistance) {
					closestDistance = distance;
					System.out.println("Candidate B: " + i + ", " + distance);
					closestIndex = i;
				}
				else {
					// System.out.println("Candidate C: " + i + ", " +
					// distance);
				}
			}

			Thread primedThread = bundle.get(closestIndex);
			Bundle primedBundle = new Bundle();
			primedBundle.add(primedThread);
			pivot.setBundle(primedBundle);

			if (closestIndex > 0) {
				// System.out.println("Best thread is " +
				// pivot.getPrimedThread() + " among choices from wordnet " +
				// bundle);
				Connections.getPorts(this).transmit(SimpleGenerator.DISAMBIGUATED, pivot);
			}
		}
	}

	public double evaluation(Entity e, Thread primed) {
		// System.out.println("Thread: " + (Vector)primed);
		// List<Thing> nearbyList = Memory.getMemory().getNeighbors(e); // OLD
		// memory
		List<Entity> nearbyList = M2.getMem().neighbors(e); // NEW memory
		// System.out.println("Processing elements: " + nearbyList.size());
		if (nearbyList.size() > 0) {
			for (Entity t : nearbyList) {
				if (t != e) {
					// System.out.println("Nearest to: " + e + "\nis: " + t);
					// System.out.println("Found nearest neighbor using " +
					// primed + ":\n" + t);
					double d = Distances.distance(t, e);
					System.out.println("Distance of " + t.getName() + " is " + d);
					return d;
				}
			}
		}
		else {
			// System.out.println("Unable to process using " + primed);
		}
		return -1;
	}

	/*
	 * Find living-thing that has multiple threads
	 */
	private Entity findPivot(Entity e) {
		if (e.relationP()) {
			Entity r = findPivot(((Relation) e).getSubject());
			if (r != null) {
				return r;
			}
			else {
				return findPivot(((Relation) e).getObject());
			}
		}
		else if (e.functionP()) {
			return findPivot(((Function) e).getSubject());
		}
		else if (e.entityP()) {
			if (e.getBundle().size() > 1) {
				return e;
			}
		}
		return null;
	}

	private void transmit(Entity t) {
		Connections.getPorts(this).transmit(t);
		if (t.sequenceP()) {
			Sequence s = (Sequence) t;
			for (Entity element : s.getElements()) {
				Connections.getPorts(this).transmit(Markers.CHAIN, element);
			}
		}
	}

	/*
	 * Test the Disambiguator
	 */
	public static void main(String[] args) {
		Thread t1 = new Thread("Thing");
		Thread t2 = new Thread("Thing");
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
		Entity thing = new Entity();
		Bundle bundle = new Bundle(t1);
		thing.setBundle(bundle);
		System.out.println(new Disambiguator3().evaluation(thing, t2));
	}
}
package memory;

import java.util.*;

import javax.swing.SwingUtilities;

import memory.time.TimeLine;
import utils.EntityUtils;
import connections.*;
import constants.RecognizedRepresentations;
import frames.Frame;
import frames.entities.Entity;

/**
 * @author Sam Glidden The Memory. You want to remember something? You use the Memory. The Memory is really just a hub
 *         for many different Memory-related functions. Should be thread-safe.
 */
@Deprecated
public final class Memory extends AbstractWiredBox {

	// turn on debugging outputs
	public static boolean DEBUG = false;

	/**
	 * *********************************************************** PUBLIC METHODS Useful if you don't want to use wires
	 * It's ok to use both wire and no wires concurrently
	 */

	/**
	 * Retrieves the singleton Memory for use
	 * 
	 * @return Memory
	 */
	static public Memory getMemory() {
		if (memory == null) {
			memory = new Memory();
			memory.setName("Memory");
		}
		return memory;
	}

	/**
	 * Resets all the self-organizing maps
	 */
	public void clearSOMs() {
		soms = new SomSpace();
	}

	/**
	 * Adds a Thing, and all it's recognized sub-Things, to the SOMs
	 * 
	 * @param t
	 *            Thing
	 */
	public void addRepTree(final Entity t) {
		// create a new thread to process input
		Thread thread = new Thread() {
			public void run() {
				if (DEBUG) System.out.println("[MEMORY] Received Rep Tree: " + EntityUtils.getRepType(t));
				// need to "demultiplex" tree and add sub reps
				// don't add components of Questions
				if (EntityUtils.getRepType(t).equals(RecognizedRepresentations.QUESTION_THING)) {
					if (DEBUG) System.out.println("[MEMORY] Input is not being stored in the SOMs.");
					return;
				}
				// Problem: sometimes Gauntlet hands the memory every rep from a
				// particular sentence,
				// sometimes only the top one.
				// Solution: pull apart each rep, getting nested reps, and
				// adding them if they are new
				Set<Entity> family = RepProcessor.extractSubReps(t);
				family.add(t);
				for (Entity elt : family) {
					// make sure we haven't already added this element
					if (!things.contains(elt)) {
						try {
							add(elt);
						}
						catch (RuntimeException e) {
							e.printStackTrace();
							System.err.println("Exception in memory system");
						}
					}
				}
				// if
				// (EntityUtils.getRepType(t).equals(RecognizedRepresentations.CAUSE_THING))
				// {
				// if (DEBUG)
				// System.out.println("[MEMORY] Adding meta-frame relation.");
				// family.remove(t);
				// System.out.println("COMPONENTS::::::::::::::");
				// for (Thing elt : family) {
				// // System.out.println("COMPONENTS::::::::::::::");
				// // System.out.println(elt);
				// //
				// System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				// if
				// (RecognizedRepresentations.ALL_THING_REPS.contains(EntityUtils.getRepType(elt)))
				// {
				// System.out.println(elt);
				// }
				// }
				// System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
				// }

			}
		};
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	/**
	 * Adds the Thing to the appropriate SOM
	 * 
	 * @param t
	 *            Thing
	 */
	public void addRep(final Entity t) {
		Thread thread = new Thread() {
			public void run() {
				add(t);
			}
		};
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	/**
	 * Returns the neighbors of a given Thing. In order for this to work, the Thing t must be of a type recognized by
	 * RecognizedRepresentations, or else the Memory doesn't know what SOM to look in.
	 * 
	 * @param t
	 *            Thing
	 */
	public List<Entity> getNeighbors(final Entity t) {
		// don't bother spawning a new thread -- happens right away in
		// soms.getNeighbors()
		List<Entity> n = soms.getNeighbors(t);
		n.add(0, t);
		return n;
	}

	/**
	 * Returns a List of at least the n nearest elements to a given Thing t. The list will be longer than n if there are
	 * elements with tied distances from t. The search Thing t is appended to the beginning of the List.
	 * 
	 * @param t
	 *            Thing
	 * @param n
	 *            integer or requested neighbors
	 * @return List of nearest neighbors to t, sorted by proximity
	 */
	public List<Entity> getNearest(final Entity t, int n) {
		// don't bother spawning a new thread -- happens right away in
		// soms.getNearest()
		List<Entity> nearest = soms.getNearest(t, n);
		nearest.add(0, t);
		return nearest;
	}

	/**
	 * Checks to see if a specific Thing can be found in one or another SOM.
	 * 
	 * @param t
	 *            Thing
	 * @return true or false
	 */
	public boolean containsInSOMs(Entity t) {
		return soms.contains(t);
	}

	/**
	 * Returns the frequency with which a Thing is found in the SOMs.
	 * 
	 * @param t
	 *            Thing
	 * @return int
	 */
	public int getFrequencyInSOMs(Entity t) {
		return soms.getFrequency(t);
	}

	public int[] getFrequencyInSOMs(List<Entity> t) {
		int[] result = new int[t.size()];

		for (int i = 0; i < t.size(); i++) {
			result[i] = soms.getFrequency(t.get(i));
		}
		return result;
	}

	/**
	 * *********************************************************** WIRE METHODS these may be public, but you shouldn't
	 * use them! wire the system up right, and they'll be called appropriately
	 */

	// adds to the SOMs a Thing, plus subthings as specified by recognized
	// represents
	public void wireAddRepTree(Object input) {
		// create a new thread to process input
		if (!(input instanceof Entity)) {
			System.err.println("[MEMORY] Expected Rep Tree, but received: " + input);
			return;
		}
		this.addRepTree((Entity) input);
	}

	// adds a Thing to the SOMs
	public void wireAddThing(final Object input) {
		if (input instanceof Entity) {
			this.addRep((Entity) input);
		}
		else if (input instanceof Frame) {
			if (DEBUG) System.out.println("[MEMORY] Frame transmitted to Memory");
			System.out.println("[MEMORY] doesn't know how to handle frames");
		}
		else {
			if (DEBUG) System.out.println("Unknown object received by memory: " + input.toString());
		}
	}

	public void wireGetNeighbors(Object input) {
		if (input instanceof Entity) {
			List<Entity> n = this.getNeighbors((Entity) input);
			Connections.getPorts(Memory.getMemory()).transmit(PORT_RESPONSE, n);
		}
		else {
			if (DEBUG) System.out.println("[Memory] Received invalid object on PORT_STIMULUS: " + input);
		}
	}

	// TODO: rename below stuff!!!
	/**
	 * Tells the memory to transmit a SOM on output wire.
	 * 
	 * @param k
	 *            KnowledgeRep SOM to output.
	 */
	public void outputSOM(final String frameType) {
		if (DEBUG) System.out.println("[MEMORY] Transmitting SOM of type " + frameType + " out to GUI Components");
		// TODO: make sure this is the right thread to use
		// need to switch back to the GUI thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// System.out.println("Probe: " + frameType + " " +
				// soms.getSom(frameType).getClass());
				Connections.getPorts(Memory.getMemory()).transmit(frameType, soms.getSom(frameType));
			}
		});
	}

	/**
	 * Tells the memory to output a SOM of the given frameType to the
	 * 
	 * @param frameType
	 */
	public void outputSOM(String destination, final String frameType) {
		// System.out.println("Sent Memory to Disambiguator");
		Connections.getPorts(Memory.getMemory()).transmit(destination, soms.getSom(frameType));
	}

	/**
	 * *********************************************************** PRIVATE METHODS and LOCAL VARIABLES
	 */

	public static final String PORT_STIMULUS = "Input stimulus port";

	public static final String PORT_RESPONSE = "Response to stimulus output port";

	public static final String PORT_CONTEXT = "Port for frames temporally near a Frame";

	public static final String PORT_REPTREE = "input a rep tree from an entire sentence";

	public static final String PORT_PREDICTIONS = "output port for predictions";

	static private Memory memory;

	// stores self-organizing maps
	private SomSpace soms = new SomSpace();

	// stores all the things that memory has seen
	private EntitySpace things = new EntitySpace();

	// stores time relations
	private final TimeLine timeline = new TimeLine();;

	// // stores cross-linkages between representations
	// private final XMem xmem = new XMem();

	private Memory() {
		super("Memory");
		Connections.getPorts(this).addSignalProcessor("wireAddThing");
		Connections.getPorts(this).addSignalProcessor(PORT_REPTREE, "wireAddRepTree");

		Connections.getPorts(this).addSignalProcessor(PORT_CONTEXT, "wireGetContext");

		if (Memory.DEBUG) System.out.println("[MEMORY] Debugging print statements are ON! To turn off, set the DEBUG flag in Memory.java to false.");
	}

	/**
	 * Saves a Thing in a self-organizing map.
	 * 
	 * @param t
	 *            Thing
	 */
	private void add(Entity t) {
		// add to EntitySpace
		things.add(t);

		// add to SOMs
		soms.add(t);

		List<Entity> predictions = things.predict(t);
		if (predictions.size() > 0) {
			// System.out.println("Made prediction from : "+t.toString(true));
			// System.out.println("PREDICTION: "+things.predict(t));
			Connections.getPorts(Memory.getMemory()).transmit(PORT_PREDICTIONS, predictions.get(0));
		}

		if (DEBUG) {
			System.out.println("[MEMORY] added " + t.getID() + " to memory");
		}
		// timestamp
		// TODO: timestamp equivalent events how?
		// timeline.timestamp(t, System.currentTimeMillis());
	}

	/**
	 * *********************************************************** DEPRECATED METHODS you should try to use the public
	 * methods above, which have consistent names and (eventually) passing tests. 'Course, if it ain't broken, no need
	 * to fix it...
	 */

	@Deprecated
	// use clearSOMs() instead
	public void clear() {
		soms = new SomSpace();
	}

	@Deprecated
	private void add(Frame f) {
		// if (f instanceof TimeFrame) {
		// timeline.addTimeFrame((TimeFrame) f);
		// }
		// else {
		soms.add(f.getThing());
		// }
		// timeStamp(f);
	}

	@Deprecated
	// use getNeighbors() instead
	public List<Entity> getBestMatches(Object input) {
		if (input instanceof Entity) {
			;
			Entity t = (Entity) input;
			List<Entity> n = soms.getNeighbors(t);
			return n;
		}
		return new ArrayList<Entity>();
	}

	@Deprecated
	// Checks to see if the Thing is contained in its memory
	// use containsInSOMs() instead
	public boolean containsInMemory(Object input) {
		if (input instanceof Entity) {
			return soms.contains((Entity) input);
		}
		return false;
	}

	// // TODO: update this to take argument of Object input for wires
	// public void getContext(Frame f) {
	// Set<TimeFrame> set = timeline.getContext(f);
	// Connections.getPorts(Memory.getMemory()).transmit(PORT_CONTEXT, set);
	// }

	//
	// /**
	// * Timestamps the Frame in the timeline. This is a hack, right now:
	// * when something is added to the system, it is assumed it just
	// * finished happening. Therefore, we add a "finished" relation to
	// * the timeweb.
	// *
	// * @param f: Frame to be timestamped as finishing at the current system
	// time.
	// */
	// private void timeStamp(Frame f) {
	// timeline.addFinishes(f, new TimeInstant(new
	// Date(System.currentTimeMillis())));
	// }
}

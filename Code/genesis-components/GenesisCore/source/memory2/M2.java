package memory2;

import java.util.*;

import memory2.datatypes.Chain;
import memory2.models.World;
import memory2.storage.Raw;
import memory.RepProcessor;
import memory.utilities.Distances;
import utils.EntityUtils;
import utils.Mark;
import connections.*;
import constants.RecognizedRepresentations;
import frames.entities.Entity;

/**
 * Top level class of the memory. You can interact with the memory either through this (use the Mem interface), or
 * through the MemBox class and corresponding wiring. Both M2 and MemBox are singleton objects. Use their getters to
 * construct them.
 * 
 * @author sglidden
 */
public class M2 extends AbstractWiredBox implements Mem {

	public static boolean DEBUG = false;

	private static Mem mem;

	private Raw raw = new Raw();

	private LLMerger merger = new LLMerger();

	private World world = new World();

	public static final String PORT_ENGLISH = "port for english sentence input";

	public static final String PORT_VISION = "port for vision input";

	public static final String PORT_CHAINS = "port for llmerger contents";

	public static final String PORT_PREDICTIONS = "port for outputing predictions";

	public static final String PORT_TALKER = "port for sending thing to prediction generator";

	public static final String PORT_STIMULUS = "port for asking about neighbors";

	public static final String PORT_RESPONSE = "port for responding with neighbors";

	/*
	 * Wire Methods for connection to Gauntlet
	 */
	private M2() {
		super("M2");
		Connections.getPorts(this).addSignalProcessor(PORT_ENGLISH, "wireInputEnglish");
		Connections.getPorts(this).addSignalProcessor(PORT_VISION, "wireInputVision");
		Connections.getPorts(this).addSignalProcessor(PORT_STIMULUS, "wireGetNeighbors");
	}

	public void wireInputEnglish(Object input) {
		// System.out.println("**M2 WIRE INPUT + "+ input);
		if (!(input instanceof Entity)) {
			if (M2.DEBUG) System.out.println("[M2] Expected Thing, but received: " + input);
			return;
		}
		Entity t = (Entity) input;
		// if (!InputTracker.containsTopLevelFrame(t)) {
		// return; // filter out all but top level frames
		// }
		this.input(t);
	}

	public void wireInputVision(Object input) {
		if (!(input instanceof Entity)) {
			if (M2.DEBUG) System.out.println("[M2] Expected Thing, but received: " + input);
			return;
		}
		this.input((Entity) input);
	}

	// sends all Chains from LLMerger out on wire
	private void outputChains(final Set<Chain> chains) {
		Connections.getPorts(getMem()).transmit(PORT_CHAINS, chains);
	}

	private void outputReps(final Map<String, Set<Chain>> chains) {
		for (final String repString : chains.keySet()) {
			Connections.getPorts(getMem()).transmit(repString, new ArrayList<Chain>(chains.get(repString)));
		}
	}

	// sends out the memory's predictions about the future
	private void outputPredictions(final Map<String, List<Entity>> preds) {
		Connections.getPorts(getMem()).transmit(PORT_PREDICTIONS, preds);
		List<Entity> lst = preds.get(World.EXPLICIT);
		if (lst != null && lst.size() > 0) {
			Entity t = lst.get(0);
			Mark.a("Prediction sent to talker " + t.asString());
			Connections.getPorts(getMem()).transmit(PORT_TALKER, t);
			// Connections.getPorts(getMem()).transmit(s.trim());
		}
		else {
			lst = preds.get(World.ANALOGICAL);
			if (lst != null && lst.size() > 0) {
				Entity t = lst.get(0);
				Connections.getPorts(getMem()).transmit(PORT_TALKER, t);
				// Connections.getPorts(getMem()).transmit(s.trim());
			}
		}
	}

	/**
	 * Static accessor for this type of Mem
	 */
	public static Mem getMem() {
		if (mem == null) {
			mem = new M2();
			((M2) mem).setName("M2");
			if (M2.DEBUG) System.out.println("[M2] Debugging print statements are ON! To turn off, set the DEBUG flag in M2.java to false.");
		}
		return mem;
	}

	// general purpose way to stick something in memory
	public void input(Entity t) {
		// System.out.println("**M2 ENGLISH INPUT");
		// System.out.println("INPUT t: " + t);
		// System.out.println("getContext(t): " + getContext(t));
		// if (M2.DEBUG) System.out.println("[M2] Received input.");
		boolean modified = false;
		Set<Entity> family = RepProcessor.extractSubReps(t);
		family.add(t);
		for (Entity elt : family) {
			try {
				if (!RecognizedRepresentations.ALL_THING_REPS.contains(EntityUtils.getRepType(t))) {
					continue;
				}
				if (EntityUtils.getRepType(t) == RecognizedRepresentations.QUESTION_THING) {
					continue;
				}
				inputRep(elt);
				modified = true;
			}
			catch (RuntimeException e) {
				e.printStackTrace();
				System.err.println("[M2] Runtime exception in memory system");
			}
		}
		if (modified) {
			// update GUI
			// if (M2.DEBUG) System.out.println("[M2] updating GUI");
			outputAll();
			outputPredictions(world.predict(t));
		}
	}

	@Override
	public void outputAll() {
		outputChains(merger.getChains());
		outputReps(merger.getRepChains());
	}

	// adds a clean rep to the memory
	public void inputRep(final Entity t) {
		if (M2.DEBUG) System.out.println("[M2] Processing rep: " + EntityUtils.getRepType(t));
		// 1) Store a backup copy
		raw.add(t);
		// 2) LL* thing, near miss only
		// java.lang.Thread thread = new java.lang.Thread() {
		// public void run() {
		merger.add(t);
		// }
		// };
		// thread.start();
		// 3) Add to concrete object engine
		world.add(t);
	}

	public int getMissDistance(final Entity t) {
		return merger.getMissDistance(t);
	}

	public int frequency(Entity t) {
		return raw.frequency(t);
	}

	public static void m2assert(boolean b, String s) {
		if (!b) {
			System.out.println("  !!! [M2] assertion failed: " + s);
		}
	}

	public List<Entity> neighbors(final Entity t) {
		List<Entity> results = merger.getNeighbors(t);
		Collections.sort(results, new Comparator<Entity>() {
			public int compare(Entity e1, Entity e2) {
				double s1 = Distances.distance(t, e1);
				double s2 = Distances.distance(t, e2);
				if (s1 < s2) return -1;
				if (s1 > s2) return 1;
				return 0;
			}
		});
		return results;
	}

	public List<Entity> nearNeighbors(final Entity t) {
		List<Entity> results = merger.getNearNeighbors(t);
		Collections.sort(results, new Comparator<Entity>() {
			public int compare(Entity e1, Entity e2) {
				double s1 = Distances.distance(t, e1);
				double s2 = Distances.distance(t, e2);
				if (s1 < s2) return -1;
				if (s1 > s2) return 1;
				return 0;
			}
		});
		return results;
	}

	public boolean isPossible(Entity t) {
		return merger.isPossible(t);
	}

	public Set<Entity> getContext(Entity t) {
		return raw.getContext(t);
	}

	public Entity getMostRecentRep(Entity t, Object rep) {
		return world.mostRecentRep(t, rep);
	}

	public void wireGetNeighbors(Object input) {
		if (input instanceof Entity) {
			List<Entity> n = this.neighbors((Entity) input);
			Connections.getPorts(this).transmit(PORT_RESPONSE, n);
		}
		else {
			if (DEBUG) System.out.println("[M2] Received invalid object on PORT_STIMULUS: " + input);
		}
	}
}

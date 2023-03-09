package memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import constants.RecognizedRepresentations;
import memory.multithreading.Task;
import memory.multithreading.Watcher;
import memory.multithreading.Worker;
import memory.soms.NewSom;
import memory.soms.Som;
import memory.soms.mergers.ConditionalMerger;
import memory.soms.mergers.DeepEntityMerger;
import memory.soms.metrics.EntityMetric;
import frames.BlockFrame;
import frames.CAFrame;
import frames.ForceFrame;
import frames.FrameFactory;
import frames.GeometryFrame;
import frames.PathElementFrame;
import frames.PlaceFrame;
import frames.TrajectoryFrame;
import frames.TransitionFrame;
import frames.entities.Entity;
/**
 * Stores the self-organizing maps the back the memory system.
 * SomSpace is asynchronous and thread-safe. Each SOM has its
 * own Worker Thread.
 * 
 * @author sglidden
 *
 */
public final class OldSomSpace {
	// the self organizing maps
// private final Som<Thing> geoSom, placeSom, trajSom, tranSom;
	// HashMap of self-organizing maps by type
	// for thread safety, these maps better not be modified after the constructor
	private final Map<String, Som<Entity>> soms = new HashMap<String, Som<Entity>>();
	private final Map<String, Worker> workers = new HashMap<String, Worker>();
	
	private final Map<Integer, Entity> originals = new HashMap<Integer, Entity>();

	public static final String OTHER = "other";

	// constructor initializes the self-organizing maps
	public OldSomSpace() {
		// create a SOM for each frame type
		// NOTE: you may be tempted to consolidate this into a loop. You can
		// only do that if each SOM type uses the same metric, merger and propDist,
		// which may not be desirable in the long run.
		soms.put(BlockFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put(ForceFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put(GeometryFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put((String) RecognizedRepresentations.PATH_THING, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put(PlaceFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put(TrajectoryFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .5));
		soms.put(TransitionFrame.FRAMETYPE, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put((String) RecognizedRepresentations.TIME_REPRESENTATION, 
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		soms.put(OldSomSpace.OTHER,
				new NewSom<Entity>(new EntityMetric(), new ConditionalMerger(), .3));
		//SomSpace.OTHER stores everything that isnt stored by the other SOM's

		// create the workers and watchers
		for (final String type : soms.keySet()) {
			workers.put(type, new Worker());
			soms.get(type).add(new Watcher() {
				public void ping() {
					Memory.getMemory().outputSOM(type);
				}
			});
		}
	}
	public void add(final Entity tRaw) {
		final Entity t = tRaw.deepClone();
		originals.put(t.getID(), tRaw);
		final String type = FrameFactory.getFrameType(t);
		final Som<Entity> som = soms.get(type);
		if (som!=null) {		// don't try to add a frame that doesn't have a SOM
//			new Thread() { public void run() { 
				workers.get(type).put(new Task() { 
					public void execute() { 
						som.add(t);
						if (Memory.DEBUG) System.out.println("[MEMORY] Added "+type+" to SOM");
					}});
//			}}.start();
		}
		else {		//No SOM for Thing. Store in SomSpace.OTHER SOM
			final Som<Entity> otherSom = soms.get(OldSomSpace.OTHER);
//			new Thread() { public void run() { 	
				workers.get(OldSomSpace.OTHER).put(new Task() { 
					public void execute() { 
						otherSom.add(t);
						if (Memory.DEBUG) System.out.println("[MEMORY] Added "+type+" to SOM");
					}});
//			}}.start();
			if (Memory.DEBUG) System.out.println("[MEMORY] No SOM for: "+type);
		}
	}

	//Searches the SomSpace for the presence of Thing.
	public boolean contains(final Entity t){
		String type = FrameFactory.getFrameType(t);
		Som<Entity> s = soms.get(type);
		if (s == null){
			type = OldSomSpace.OTHER;
			s = soms.get(type);
		}
		final Som<Entity> s2 = s;
		final String type2 = type;
		final BlockingQueue<Boolean> q = new LinkedBlockingQueue<Boolean>();
		if (soms.get(type)!=null) {
//			new Thread() { public void run() { 
				workers.get(type2).put(new Task() { 
					public void execute() { 
						if (s2.containsEquivalent(t)) {
							try {q.put(true);} 
							catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
						}
						else {
							try {q.put(false);} 
							catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
						}
					}});
//			}}.start();
			while (true) {
				try {return q.take();} 
				catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
			}
		}
		else {
			return false;
		}
	}

	/**
	 * @param t Thing
	 * @return List of Things that the appropriate SOM
	 * considers to be neighbors of t, SORTED by best to
	 * worst match.
	 */
	public List<Entity> getNeighbors(final Entity t) {
		final String type = FrameFactory.getFrameType(t);

		if (soms.get(type)!=null) {
			final BlockingQueue<List<Entity>> q = new LinkedBlockingQueue<List<Entity>>();
//			new Thread() { public void run() { 
				workers.get(type).put(new Task() { 
					public void execute() { 
						final Som<Entity> s = soms.get(type);
						Set<Entity> neighbors = s.neighbors(t);
						List<Entity> sorted = new ArrayList<Entity>(neighbors);
						Collections.sort(sorted, new Comparator<Entity>() {
							public int compare(Entity e1, Entity e2) {
								double s1 = s.getDistance(t, e1);
								double s2 = s.getDistance(t, e2);
								if (s1 < s2) return -1;
								if (s1 > s2) return 1;
								return 0;
							}
						});
						while (true) {
							try {q.put(sorted); break;} 
							catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
						}
					}});
//			}}.start();
			List<Entity> things = null;
			while (things==null) {
				try {things = q.take();} 
				catch (InterruptedException ie) {System.err.println("[MEMORY] Worker Thread Interrupted");}
			}
			return things;
		}
		else{ //getNeighbors on the "other" SOM
			final BlockingQueue<List<Entity>> q = new LinkedBlockingQueue<List<Entity>>();
//			new Thread() { public void run() { 
				workers.get(OldSomSpace.OTHER).put(new Task() { 
					public void execute() { 
						final Som<Entity> s = soms.get(OldSomSpace.OTHER);
						Set<Entity> neighbors = s.neighbors(t);
						List<Entity> sorted = new ArrayList<Entity>(neighbors);
						Collections.sort(sorted, new Comparator<Entity>() {
							public int compare(Entity e1, Entity e2) {
								double s1 = s.getDistance(t, e1);
								double s2 = s.getDistance(t, e2);
								if (s1 < s2) return -1;
								if (s1 > s2) return 1;
								return 0;
							}
						});
						while (true) {
							try {q.put(sorted); break;} 
							catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
						}
					}});
//			}}.start();
			List<Entity> things = null;
			while (things==null) {
				try {things = q.take();} 
				catch (InterruptedException ie) {System.err.println("[MEMORY] Worker Thread Interrupted");}
			}

			if (Memory.DEBUG) System.out.println("[MEMORY] Received request for neighbors of an unsupported frame type: "+t);

			return things;
		}

	}
	/**
	 * @param k KnowledgeRep that the SOM holds
	 * @return SomInterface of type k
	 */
	public Som<Entity> getSom(final String type) {
		//System.out.println(soms);
		final BlockingQueue<Som<Entity>> q = new LinkedBlockingQueue<Som<Entity>>();
//		new Thread() { public void run() {
			workers.get(type).put(new Task() { 
				public void execute() { 
					try {q.put(soms.get(type).clone());} 
					catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
				}});
//		}}.start();
		while (true) {
			try {return q.take();} 
			catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
		}
	}

	public static void main(String[] args) {
		OldSomSpace soms = new OldSomSpace();
		for (String s : soms.soms.keySet()) {
			System.out.println(s);
		}
	}
	
	public List<Entity> getNearest(final Entity t, final int n) {
		final String type = FrameFactory.getFrameType(t);
		final BlockingQueue<List<Entity>> q = new LinkedBlockingQueue<List<Entity>>();
		if (soms.get(type)!=null) {
//			new Thread() { public void run() { 
				workers.get(type).put(new Task() { 
					public void execute() { 
						final Som<Entity> s = soms.get(type);
						NewSom<Entity> ns = (NewSom<Entity>) s;
						try {q.put(ns.getNearest(t, n));}
						catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
					}});
//			}}.start();
			while (true) {
				try {return q.take();} 
				catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
			}
		}
		return new ArrayList<Entity>();
	}

	// TODO: clean this up!
	public int getFrequency(final Entity t) {
		final String type = FrameFactory.getFrameType(t);
		final BlockingQueue<Integer> q = new LinkedBlockingQueue<Integer>();
		if (soms.get(type)!=null) {
//			new Thread() { public void run() { 
				workers.get(type).put(new Task() { 
					public void execute() { 
						final Som<Entity> s = soms.get(type);
						NewSom<Entity> ns = (NewSom<Entity>) s;
						try {q.put(ns.getWeights().get(t));} 
						catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
					}});
//			}}.start();
			while (true) {
				try {return q.take();} 
				catch (InterruptedException e) {System.err.println("[MEMORY] Worker Thread Interrupted");}
			}
		}
		else {
			return 0;
		}
	}
}

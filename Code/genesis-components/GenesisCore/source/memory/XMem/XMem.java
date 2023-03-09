package memory.XMem;

import java.util.*;

import matchers.Substitutor;
import memory.*;
import memory.utilities.Distances;
import utils.graphs.DirectedGraph;
import frames.Analogy;
import frames.entities.Entity;

/**
 * Designed to bridge the gap between SOMs and different representational
 * structures. Stores Things and linkages between them.
 * 
 * @author sglidden
 * @created 25 Jan 2008
 */
public class XMem {

	final private DirectedGraph<Entity, Integer> graph = new DirectedGraph<Entity, Integer>();

	/**
	 * Stores a set of two related representations. Order matters.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 */
	public synchronized void add(Entity t1, Entity t2) {
		int edge = 1;
		if (graph.contains(t1, t2)) {
			edge = graph.getEdgeBetween(t1, t2) + 1;
		}
		graph.addEdge(t1, t2, edge);
	}

	/**
	 * Removes the linkage between two Things. If neither Thing has any more
	 * associations, then it is also removed.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 */
	public synchronized void remove(Entity t1, Entity t2) {
		graph.removeEdge(t1, t2);
		if (graph.getSuccessors(t1).isEmpty() && graph.getPredecessors(t1).isEmpty()) {
			graph.removeNode(t1);
		}
		if (graph.getSuccessors(t2).isEmpty() && graph.getPredecessors(t2).isEmpty()) {
			graph.removeNode(t2);
		}
	}

	/**
	 * Assesses the likelihood that the two thing are related. Qualitatively, if
	 * t1 is similar to a Thing that is linked to a Thing that is similar to t2,
	 * then True is returned. Otherwise, false. "Similar" is defined by the
	 * memory system's self-organizing maps.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 */
	public synchronized boolean check(Entity t1, Entity t2) {
		// first check for direct association
		if (graph.getSuccessors(t1).contains(t2)) {
			return true;
		}
		// get similar Things to t1 and t2
		Memory m = Memory.getMemory();
		List<Entity> t1Neighbors = m.getNeighbors(t1);
		List<Entity> t2Neighbors = m.getNeighbors(t2);

		// see if there is any association between those lists
		for (Entity start : t1Neighbors) {
			for (Entity end : t2Neighbors) {
				if (graph.getSuccessors(start).contains(end)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns a List of Things that are expected to be associated with the
	 * given Thing. The list is ordered by strength of association.
	 * 
	 * @param t1
	 *            Thing
	 * @return List of Things
	 */
	public synchronized List<Entity> predict(Entity tRaw) {
		final Entity thing = getMatch(tRaw);
		List<Entity> matches = new ArrayList<Entity>();
		// first check for direct associations
		if (graph.contains(thing)) {
			// TODO: wrap hack
			List<Entity> preds = new ArrayList<Entity>(graph.getSuccessors(thing));
			Collections.sort(preds, new Comparator<Entity>() {
				public int compare(Entity t1, Entity t2) {
					if (graph.getEdgeBetween(thing, t1) < graph.getEdgeBetween(thing, t2))
						return -1;
					else if (graph.getEdgeBetween(thing, t1) > graph.getEdgeBetween(thing, t2)) return 1;
					return 0;
				}
			});

			for (Entity pred : preds) {
				matches.add(RepProcessor.wrap(pred));
			}
		}
		else {
			// second, try to make an analogy
			Memory m = Memory.getMemory();

			// get only neighbors that are useful
			// TODO: migrate to SOMs
			// double cutoff = .5;
			double cutoff = 0.05;
			List<Entity> neighbors = new ArrayList<Entity>();
			for (Entity potential : graph.getNodes()) {
				double distance = Distances.distance(potential, thing);
				System.out.println("Distance is " + distance + ", threshold " + cutoff);
				if (distance < cutoff) {
					// System.out.println("Match is with " + potential);
					neighbors.add(potential);
				}
			}
			Collections.sort(neighbors, new Comparator<Entity>() {
				public int compare(Entity t1, Entity t2) {
					if (Distances.distance(t1, thing) < Distances.distance(t2, thing))
						return -1;
					else if (Distances.distance(t1, thing) > Distances.distance(t2, thing)) return 1;
					return 0;
				}
			});

			if (neighbors.size() > 0) {
				final Entity base = neighbors.get(0);
				List<Entity> basies = new ArrayList<Entity>(graph.getSuccessors(base));
				// sort by strength of association
				Collections.sort(basies, new Comparator<Entity>() {
					public int compare(Entity t1, Entity t2) {
						if (graph.getEdgeBetween(base, t1) < graph.getEdgeBetween(base, t2))
							return -1;
						else if (graph.getEdgeBetween(base, t1) > graph.getEdgeBetween(base, t2)) return 1;
						return 0;
					}
				});
				// System.out.println("DOING ANALOGY");
				// System.out.println("BASE: "+base.toString(true));
				// System.out.println("TARGET: "+thing.toString(true));
				Analogy analogy = new Analogy(base, thing);
				// System.out.println("ANALOGY: "+analogy.getThing().toString(true));
				// Connections.getPorts(Memory.getMemory()).transmit(Memory.PORT_PREDICTIONS,
				// analogy.getThing());
				for (Entity basey : basies) {
					// System.out.println("BASEY: "+basey.toString(true));
					// TODO: deal with wrap hack
					// System.out.println("Players, thing:\n" + thing +
					// "\nMatch:\n" + base + "\nConsequence:\n" + basey);
					// This no longer seems to work, so phw made substitution
					// feb 09
					// matches.add(RepProcessor.wrap(analogy.targetify(basey)));
					matches.add(RepProcessor.wrap(Substitutor.substitute(thing, base, basey)));
				}
				// matches.add(0, analogy.getThing());
			}
		}
		return matches;
	}



	// TODO: make this faster! Use immutable things?
	private Entity getMatch(Entity thing) {
		for (Entity t : graph.getNodes()) {
			if (Distances.distance(thing, t) == 0) {
				return t;
			}
		}
		return thing;
	}

	
}

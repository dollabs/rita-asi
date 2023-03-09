package memory2.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import constants.RecognizedRepresentations;
import memory2.datatypes.Chain;
import memory2.datatypes.ImmutableEntity;
import memory.RepProcessor;
import memory.utilities.Distances;
import memory2.M2;
import utils.EntityUtils;
import utils.graphs.DirectedGraph;
import utils.graphs.DirectedMultiGraph;
import frames.Analogy;
import frames.entities.Entity;
import frames.entities.Thread;


/**
 * Keeps a running "model" of the the world. Sort of like Harold's imaginer,
 * except we are using only Things we've seen to maintain information about
 * concrete object in our model. 
 * 
 * This model of the world is the key to making predictions!
 * 
 * @author sglidden
 *
 */
public class World {
	
	public final static String EXPLICIT = "exact causal precedence";
	public final static String CIRCUMSTANTIAL = "subject precedence";
	public final static String HISTORICAL = "historical event precedence";
	public final static String ANALOGICAL = "analogy from known causal relation";

	/*
	 * TO DO
	 * 1. Extract Concrete Objects (COs) from frames
	 * 2. Update information about COs based on new frames
	 * 3. Store a model for how the world is Right Now (i.e. what is the status of each CO)
	 * 4. Provide some useful accessors for that.
	 * 5. Figure out how to deal with history of COs
	 * 
	 * Each CO has a set of frames that talk about it
	 * --> it has the latest frame to hopefully talk about current status
	 * --> A CO can "branch" into two COs if we suddenly see conflicting reports
	 * 
	 * Merge together multiple instances of the same frame types... eg trajectories combine to be 
	 * a super trajectory. Use a directed graph?
	 * 
	 */
	
	/*
	 * Answering questions:
	 * 1. Analyze the relevant CO the question refers to.
	 */
	
	/*
	 * Predictions
	 * - Compare storyline of concrete objects. If two similar Things, one from each
	 * storyline, lead to two other similar things, "learn" that correlation. Then
	 * we can use it to make predictions in the future!
	 * 
	 * 	 use the LLMerger to get Things that are similar... see what happened to 
	 * their corresponding CO; use that to predict what will happen to our CO
	 * 
	 */
	
	// for storying a history of inputs, in order to make predictions
	private Map<Thread, ImmutableEntity> linestarts = new HashMap<Thread, ImmutableEntity>();
	private Map<Thread, ImmutableEntity> lineends = new HashMap<Thread, ImmutableEntity>();
	// stores a graph of all COs -- nodes are an IThing containing the CO, edges are the CO
	private DirectedMultiGraph<ImmutableEntity, Thread> coGraph = new DirectedMultiGraph<ImmutableEntity, Thread>();
	private List<ImmutableEntity> history = new ArrayList<ImmutableEntity>();
	
	// for storing learned causal relations
	private DirectedGraph<ImmutableEntity, Integer> causeGraph = new DirectedGraph<ImmutableEntity, Integer>();

	/**
	 * Adds a new thing to our concrete object model of the world.
	 * @param timer Thing
	 */
	public synchronized void add(Entity in) {
		in = RepProcessor.unwrap(in);
		// extract and link CO's
		Set<Thread> cos = extractCOs(in);
		ImmutableEntity t = new ImmutableEntity(in);
//		if (M2.DEBUG) System.out.println("[M2] adding thing with COs: "+cos.toString());
		for (Thread co : cos) {
			if (linestarts.containsKey(co)) {
				// have a storyline for this CO; update it
				ImmutableEntity node = lineends.get(co);
//				coGraph.addNode(t);
				coGraph.addEdge(node, t, co);
				lineends.put(co, t);
			}
			else {
				// need to create a new storyline
				coGraph.addNode(t);
				linestarts.put(co, t);
				lineends.put(co, t);
			}
		}
		// track history of inputs
		history.add(t);
		
		// now explicitly deal with cause frames
		if (EntityUtils.getRepType(in) == RecognizedRepresentations.CAUSE_THING) {
			processCause(in);
		}
	}


	/*
	 * prediction levels:
	 * 1. explicit knowledge from "cause" frames
	 * 2. looking back in storyline to see if the same Thing occurred, and then
	 * reporting what followed it
	 * 3. using LLMerger... lookup Things that are equivalent in the LLMerger,
	 * get their corresponding CO, and see what happened next to it in that CO's
	 * storyline. Report that as something that might happen to our CO, using 
	 * the analogy code developed by someone else
	 */
	// TODO: test this more thoroughly
	public synchronized Map<String, List<Entity>> predict(Entity input2) {
//		System.out.println("predicting off of: "+input2);
		final Entity input = RepProcessor.unwrap(input2);
		final ImmutableEntity in = new ImmutableEntity(input);
		// first level: explicit from cause frames - EXPLICIT
		List<ImmutableEntity> l1 = null;
		if (causeGraph.contains(in)) {
			l1 = new ArrayList<ImmutableEntity>(causeGraph.getSuccessors(in));
			Collections.sort(l1, new Comparator<ImmutableEntity>() {
				public int compare(ImmutableEntity t1, ImmutableEntity t2) {
					if (causeGraph.getEdgeBetween(in, t1) < causeGraph.getEdgeBetween(in, t2))
						return -1;
					else if (causeGraph.getEdgeBetween(in, t1) > causeGraph.getEdgeBetween(in, t2))
						return 1;
					return 0;
				}});
		}
		
		// second level: exact historical precedence in CO graph - CIRCUMSTANTIAL
		List<ImmutableEntity> l2 = null;
//		System.out.println("coGraph: "+coGraph);
		if (coGraph.contains(in)) {
			// TODO: does it make sense to sort this by CO?
			l2 = new ArrayList<ImmutableEntity>(coGraph.getSuccessors(in));
			if (l2.isEmpty()) l2=null;
		}
		
		// third level: historical precedence in history; no CO match - HISTORICAL
		List<ImmutableEntity> l3 = null;
		if (history.contains(in)) {
			List<ImmutableEntity> copy = new ArrayList<ImmutableEntity>(history);
			l3 = new ArrayList<ImmutableEntity>();
			while(copy.contains(in)) {
				int pos = copy.indexOf(in);
				if (pos!=-1 && pos < copy.size()-1) {
					l3.add(copy.get(pos+1));
				}
				copy.remove(pos);
			}
			// sort so most recent is first
			Collections.reverse(l3);
			if (l3.isEmpty()) l3 = null;
		}

		

		// fourth level: LLMerger-backed analogy - ANALOGY
		List<ImmutableEntity> l4 = new ArrayList<ImmutableEntity>();
		List<Entity> nn = M2.getMem().nearNeighbors(RepProcessor.wrap(input));
//		System.out.println("NN: "+nn);
		Collections.sort(nn, new Comparator<Entity>() {
			public int compare(Entity t1, Entity t2) {
				if (Distances.distance(t1, input) < Distances.distance(t2, input))
					return -1;
				else if (Distances.distance(t1, input) > Distances.distance(t2, input))
					return 1;
				return 0;
			}});
		for (Entity baseThing : nn) {
			final ImmutableEntity base = new ImmutableEntity(RepProcessor.unwrap(baseThing));
			if (causeGraph.contains(base)) {
				List<ImmutableEntity> basies = new ArrayList<ImmutableEntity>(causeGraph.getSuccessors(base));
				// sort by strength of association
				Collections.sort(basies, new Comparator<ImmutableEntity>() {
					public int compare(ImmutableEntity t1, ImmutableEntity t2) {
						if (causeGraph.getEdgeBetween(base, t1) < causeGraph.getEdgeBetween(base, t2))
							return -1;
						else if (causeGraph.getEdgeBetween(base, t1) > causeGraph.getEdgeBetween(base, t2))
							return 1;
						return 0;
					}});
				Analogy analogy = new Analogy(base.getThing(), input);
				for (ImmutableEntity basey : basies) {
					l4.add(new ImmutableEntity(analogy.targetify(basey.getThing())));
				}
			}
		}
		
		// fifth level: analogy made from cause frames
		// get similar elements in the cause graph
//		List<ImmutableEntity> l5 = null;
//		double cutoff = .5;
//		List<ImmutableEntity> neighbors = new ArrayList<ImmutableEntity>();
//		for (ImmutableEntity potential : causeGraph.getNodes()) {
//			if (Distances.distance(potential.getThing(), input) < cutoff) {
//				neighbors.add(potential);
//			}
//		}
//		Collections.sort(neighbors, new Comparator<ImmutableEntity>() {
//			public int compare(ImmutableEntity t1, ImmutableEntity t2) {
//				if (Distances.distance(t1.getThing(), input) < Distances.distance(t2.getThing(), input))
//					return -1;
//				else if (Distances.distance(t1.getThing(), input) > Distances.distance(t2.getThing(), input))
//					return 1;
//				return 0;
//			}});
//
//		if (neighbors.size()>0) {
//			l5 = new ArrayList<ImmutableEntity>();
//			final ImmutableEntity base = neighbors.get(0);
//			List<ImmutableEntity> basies = new ArrayList<ImmutableEntity>(causeGraph.getSuccessors(base));
//			// sort by strength of association
//			Collections.sort(basies, new Comparator<ImmutableEntity>() {
//				public int compare(ImmutableEntity t1, ImmutableEntity t2) {
//					if (causeGraph.getEdgeBetween(base, t1) < causeGraph.getEdgeBetween(base, t2))
//						return -1;
//					else if (causeGraph.getEdgeBetween(base, t1) > causeGraph.getEdgeBetween(base, t2))
//						return 1;
//					return 0;
//				}});
//			Analogy analogy = new Analogy(base.getThing(), input);
//			for (ImmutableEntity basey : basies) {
//				l5.add(new ImmutableEntity(analogy.targetify(basey.getThing())));
//			}
//		}

		Map<String, List<Entity>> results = new HashMap<String, List<Entity>>();
		if (l1 != null) results.put(World.EXPLICIT, convertAndCleanList(l1));
		if (l2 != null) results.put(World.CIRCUMSTANTIAL, convertAndCleanList(l2));
		if (l3 != null) results.put(World.HISTORICAL, convertAndCleanList(l3));
		if (!l4.isEmpty()) results.put(World.ANALOGICAL, convertAndCleanList(l4));
//		if (l5 != null) results.put("l5", convertAndCleanList(l5));
		return results;
	}

	// converts Ithings to Things, first removing redundant Ithings
	private List<Entity> convertAndCleanList(List<ImmutableEntity> lst) {
		List<Entity> result = new ArrayList<Entity>();
		for (int i=0; i<lst.size(); i++) {
			ImmutableEntity item = lst.get(i);
			if (!lst.subList(0, i).contains(item)) {
				result.add(RepProcessor.wrap(item.getThing()));
			}
		}
		return result;
	}
	
	// TODO: use IThings for COs instead of threads
	private Set<Thread> extractCOs(Entity t) {
		// hack: look for keywords on a Thing's primed-thread
		List<Entity> children = Chain.flattenThing(t);
		Set<Thread> results = new HashSet<Thread>();
		for (Entity c : children) {
			if (c.getTypes().contains("entity")) {
				results.add(new Thread(c.getPrimedThread()));
			}
		}
		return results;
	}
	
	private void processCause(Entity cause) {
		ImmutableEntity t1 = new ImmutableEntity(cause.getSubject());
		ImmutableEntity t2 = new ImmutableEntity(cause.getObject());
		if (t1 != null && t2 != null) {
			int edge = 1;
			if (causeGraph.contains(t1, t2)) {
				edge = causeGraph.getEdgeBetween(t1, t2) + 1;
			}
			causeGraph.addEdge(t1, t2, edge);
			if (M2.DEBUG) System.out.println("[M2] Learned from cause frame");
		}
	}
	
	// set rep to RecognizedRepresentations.TRAJECTORY_THING, for example.
	public synchronized Entity mostRecentRep(Entity in, Object rep) {
		if (!in.entityP()) {
			if (M2.DEBUG) System.out.println("[M2] invalid input to mostRecentRep -- just input a plain Thing.");
			return null;
		}
		ImmutableEntity co = new ImmutableEntity(in);
		ImmutableEntity repThing = null;
		ImmutableEntity next = lineends.get(co);
		while (next != null) {
			if (EntityUtils.getRepType(next.getThing()).equals(rep)) {
				repThing = next;
				break;
			}
			for (ImmutableEntity pos : coGraph.getPredecessors(next)) {
				if (coGraph.getEdgesBetween(pos, next).contains(co)) {
					next = pos;
					break;
				}
				else {
					next = null;
				}
			}
		}
		if (repThing == null) {
			return null;
		}
		return repThing.getThing();
	}	
}

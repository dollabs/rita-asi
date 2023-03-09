package memory2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Entity;
import frames.entities.Thread;
import memory2.datatypes.Chain;
import memory2.datatypes.DoubleBundle;


/**
 * I don't even know what this does yet.
 * 
 * @author Sam Glidden
 * 
 * 
 */
public class LLMerger {

	// NOTE: because Chains are mutable, we need to be very careful with using HashSets/maps
	// i.e. remove a Chain from a Hash, mutate it, then add it back

	private Map<String, Set<Chain>> reps = new HashMap<String, Set<Chain>>();
	private Set<Chain> chains = new HashSet<Chain>();

	//	private List<List<Chain>> merges = new LinkedList<List<Chain>>();
//	private List<Chain> modified = new LinkedList<Chain>();


	/**
	 * @return all the Chains in the memory
	 */
	public synchronized Set<Chain> getChains() {
		return new HashSet<Chain>(chains);
	}


	public synchronized Map<String, Set<Chain>> getRepChains() {
		return new HashMap<String, Set<Chain>>(reps);
	}

	/**
	 * @param t Thing
	 * Adds a new Thing to the Memory, trying to merge it into existing Chains
	 */
	public void add(Entity t) {
		Chain c = new Chain(t);
		//
		//		Feature f = getFeature(t);
		//		//		synchronized (this) {
		//		if (f==null) {
		//			addPos(c);
		//		}
		//		if (f==Feature.can || f==Feature.did) {
		//			addPos(c);
		//		}
		//		if (f==Feature.cannot) {
		//			addNeg(c);
		//		}
		addNew(c);
		doMerges();
		//		doNeighborhood();
	}

	private enum Feature {
		can,
		cannot,
		did,
		didnot
	}


	//	private Feature getFeature(Thing t) {
	//		for (Thing child : Chain.flattenThing(t)) {
	//			Thread ft = child.getBundle().getThread("feature");
	//			if (ft!=null) {
	//				if (ft.contains("can")) {
	//					if (ft.contains("not"))
	//						return Feature.cannot;
	//					return Feature.can;
	//				}
	//				if (ft.contains("did")) {
	//					if (ft.contains("not"))
	//						return Feature.didnot;
	//					return Feature.did;
	//				}
	//			}
	//		}
	//		return null;
	//	}

	private synchronized void addNew(Chain newT) {
		// get near misses

		// check for conflicts first
		conflictResolver(newT);

//		System.out.println("ALL CHAINS after conflictReoslver: "+chains);

		// now update near misses
		Map<Chain, Integer> candidates = getMisses(newT, 1);
		//		System.out.println("candidates for near miss updating: "+candidates);
		if (!candidates.isEmpty()) {
			if (M2.DEBUG) System.out.println("[M2] LLMerger updating existing Chain.");
			// update each candidate to include this added example
			boolean isRecorded = false;
			for (Chain cand : candidates.keySet()) {
				removeChain(cand);

				M2.m2assert(candidates.get(cand) < 2, "addNew -- invalid candidate found");
				M2.m2assert(!conflicts(cand, newT), "addNew -- cand conflicting with new chain--is getMisses broken?");

				// try to do merge into
				if (M2.DEBUG) System.out.println("[M2] LLMerger trying mergeInto.");
				Chain bkp = cand.clone();
				//				System.out.println("testing merge of: "+cand+" "+newT);
				mergeInto(cand, newT);
				if (getConflicts(cand).isEmpty()) {
					// no conflict... add it
					//					System.out.println("adding cand chain: "+cand);
					addChain(cand);
					isRecorded = true;
				}
				else {
					// causes a conflict... add unmerged version
					//					System.out.println("adding bkp chain: "+bkp);
					addChain(bkp);
				}
			}
			if (!isRecorded) {
				// we failed to do a single merge... add the chain independently
				addChain(newT);
			}
		}
		else {
			if (M2.DEBUG) System.out.println("[M2] LLMerger creating new Chain.");
			// no candidates made it through
			// add as independent concept
			addChain(newT);
		}

//		System.out.println("ALL CHAINS after addNew: "+chains);

	}

	// adds n to base, no checks
	// n needs to be one-dimensional
	private void mergeInto(Chain base, Chain n) {
		for (int i=0; i<n.size(); i++) {
			Thread pos = n.get(i).getPosSingle();
			Thread neg = n.get(i).getNegSingle();
			if (pos != null) {
				base.get(i).addPos(pos);
			}
			else if (neg != null) {
				base.get(i).addNeg(neg);
			}
		}
		base.updateHistory(n.getInputList().get(0));
	}


	// doesn't add "in" -- you have to do this yourself!
	// returns true if there is a still a conflict somewhere
	private boolean conflictResolver(Chain in) {
		List<Chain> conflicts = getConflicts(in);
		boolean results = false;
		if (!conflicts.isEmpty()) {
			results = true;
		}
		for (Chain con : conflicts) {
			removeChain(con);
			if (M2.DEBUG) System.out.println("[M2] LLMerger resolving conflict.");
			List<Chain> newChains = reconstitute(con, in);
			for (Chain n: newChains) {
				addChain(n);
			}
		}
		return results;
	}

	// sees if old and n conflict explicitly
	private boolean conflicts(Chain old1, Chain n1) {
		//		System.out.println("checking for conflict between: "+n+" "+old);
		Chain longer = old1;
		Chain shorter = n1;
		if (old1.size() < n1.size()) {
			longer = n1;
			shorter = old1;
		}
		boolean hardMiss = false;
		for (int i=0; i<shorter.size(); i++) {
			int dist = shorter.get(i).getDistance(longer.get(i));
			if (dist == 2) {
				if (hardMiss) {
					return false;
				}
				else {
					hardMiss = true;
				}
			}
			else if (dist == 1) {
				return false;
			}
		}
		if (hardMiss) {
			//			System.out.println("adding conflict with: "+old);
			return true;
		}
		return false;
	}

	// sees if old and n conflict explicitly
	private List<Chain> getConflicts(Chain c) {
		List<Chain> results = new ArrayList<Chain>();
		//		System.out.println("getting all conflicts of: "+c);
		for (Chain cand : this.chains) {
			if (conflicts(cand, c)) {
				results.add(cand);
			}
		}
		return results;
	}

	// base implies something that neg explicitly says is impossible.
	// this method splits base up to comply with neg
	private List<Chain> reconstitute(Chain base, Chain neg) {
		//		System.out.println(neg+ " conflicts with " + base);

		List<Chain> results = new ArrayList<Chain>();
		List<Entity> inputThings = base.getInputList();
		inputThings.addAll(neg.getInputList());

		List<Chain> atoms = new ArrayList<Chain>();

		for (Entity in: inputThings) {
			Chain c = new Chain(in);
			atoms.add(c);
		}

		results.add(atoms.get(0).clone());

		int r = 0;
		while(r < results.size()) {
			//			System.out.println("in loop, atoms are " + atoms);
			//			System.out.println("in loop, results are " + results);
			Chain c1 = results.get(r);
			for (Chain c2: atoms) {
				//				System.out.println("c1 is now : "+c1);
				//				System.out.println("c2 is now : "+c2);
				// TODO: maybe we shouldn't care about size?
				if (c1.size() < c2.size()) {
					break;
				}
				Chain c1bkp = c1.clone();
				mergeInto(c1, c2);
				//				System.out.println("new c1: "+c1+" from c2: "+c2);

				for (Chain tester : atoms) {
					//					System.out.println("tester is: "+tester);
					if (conflicts(c1, tester)) {
						//						System.out.println("conflicts!!! "+ c1+ " and "+tester);
						results.set(r, c1bkp);
						boolean newConcept = true;
						for (Chain f : results) {
							//							System.out.println("comparing "+ c2+ " and "+f+" , distance: "+c2.getChainDistance(f));
							if (c2.overlaps(f)) {
								newConcept = false;
								break;
							}
						}
						if (newConcept) {
							results.add(c2.clone());
							//							System.out.println("Added to results: "+c2);
						}
						c1 = c1bkp;
						break;
					}
				}
			}
			r++;
		}

//		System.out.println("final reconstitute results: "+results);

		return results;
	}
	//
	//	// base implies something that neg explicitly says is impossible.
	//	// this method splits base up to comply with neg
	//	private List<Chain> reconstitute3(Chain base, Chain neg) {
	//		//		System.out.println(neg+ " conflicts with " + base);
	//
	//		List<Chain> results = new ArrayList<Chain>();
	//		List<Thing> inputThings = base.getInputList();
	//
	//		List<Chain> negs = new ArrayList<Chain>();
	//		negs.add(neg);
	//		List<Chain> inputs = new ArrayList<Chain>();
	//
	//		for (Thing in: inputThings) {
	//			Chain c = new Chain(in);
	//			for (DoubleBundle db : c) {
	//				if (!db.getNegSet().isEmpty()) {
	//					negs.add(c);
	//					break;
	//				}
	//			}
	//			inputs.add(c);
	//		}
	//
	//		//		System.out.println("negs: "+negs);
	//		//		int j = (int) Math.round(Math.random()*inputs.size());
	//
	//		for (int i=0; i<inputs.size(); i++) {
	//			//			j++;
	//			//			int index = j%inputs.size();
	//			//			System.out.println("index: "+index);
	//			//			System.out.println("Results: "+results);
	//
	//			Chain c = inputs.get(i);
	//			//			System.out.println("chain c: "+c);
	//			Map<Chain, Integer> cands = new HashMap<Chain, Integer>();
	//			for (Chain cc : results) {
	//				cands.put(cc, 0);
	//			}
	//
	//			cands = getMissesHelper(c, cands, 1);
	//			//			System.out.println("cands1: "+cands);
	//
	//			if (!cands.isEmpty()) {
	//				for (Chain r : results) {
	//					if (cands.containsKey(r)) {
	//						M2.m2assert(!conflicts(r, c), "addNew -- problem conflict!");
	//						mergeInto(r, c);
	//					}
	//				}
	//			}
	//			else {
	//				results.add(c);
	//			}
	//			// now update intermediate results to include neg example
	//			cands = new HashMap<Chain, Integer>();
	//			for (Chain cc : results) {
	//				cands.put(cc, 0);
	//			}
	//
	//			for (Chain n : negs) {
	//				//				System.out.println("  neg is: "+n);
	//				cands = getMissesHelper(n, cands, 1);
	//				//				System.out.println("   misses are: "+cands);
	//				if (!cands.isEmpty()) {
	//					for (Chain r : results) {
	//						if (cands.containsKey(r)) {
	//							M2.m2assert(!conflicts(r, n), "addNew -- neg conflicts!");
	//							//							System.out.println("   merging neg in: "+r+" "+n);
	//							mergeInto(r, n);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		//		System.out.println("final reconstitute results: "+results);
	//
	//		return results;
	//	}

	//	
	//	private List<Chain> reconstitute_old(Chain base, Chain neg) {
	//		System.out.println("Conflict is with: "+base);
	//		
	//		List<Chain> results = new ArrayList<Chain>();
	//		
	//		List<Thing> inputs = base.getInputList();
	//
	//		for (int i=0; i<inputs.size(); i++) {
	//			
	//			System.out.println("Results: "+results);
	//			
	//			Chain c = new Chain(inputs.get(i));
	//			System.out.println("chain c: "+c);
	//			Map<Chain, Integer> cands = new HashMap<Chain, Integer>();
	//			for (Chain cc : results) {
	//				cands.put(cc, 0);
	//			}
	//			
	//			cands = getMissesHelper(c, cands, 1);
	//			System.out.println("cands1: "+cands);
	//
	//			if (!cands.isEmpty()) {
	//				for (Chain r : results) {
	//					if (cands.containsKey(r)) {
	//						M2.m2assert(!conflicts(r, c), "addNew -- problem conflict!");
	//						mergeInto(r, c);
	//					}
	//				}
	//			}
	//			else {
	//				results.add(c);
	//			}
	//			// now update intermediate results to include neg example
	//			cands = new HashMap<Chain, Integer>();
	//			for (Chain cc : results) {
	//				cands.put(cc, 0);
	//			}
	//			
	//			cands = getMissesHelper(neg, cands, 1);
	//			if (!cands.isEmpty()) {
	//				for (Chain r : results) {
	//					if (cands.containsKey(r)) {
	//						M2.m2assert(!conflicts(r, neg), "addNew -- neg conflicts!");
	//						mergeInto(r, neg);
	//					}
	//				}
	//			}
	//		}
	//		System.out.println("final reconstitute results: "+results);
	//
	//		return results;
	//	}

	//	private synchronized void addPos(Chain newT) {
	//		// get near misses
	//		Map<Chain, Integer> candidates = getMisses(newT, 1);
	//		//		System.out.println("Adding a positive example");
	//		if (!candidates.isEmpty()) {
	//			if (M2.DEBUG) System.out.println("[M2] LLMerger updating existing Chain.");
	//			// update each candidate to include this added example
	//			for (Chain cand : candidates.keySet()) {
	//				removeChain(cand);
	//				//				System.err.println(cand);
	//				//				System.err.println(candidates);
	//				//				System.err.println(candidates.get(cand));
	//								M2.m2assert(candidates.get(cand) < 2, "addPos");
	//				for(int i=0; i<newT.size(); i++) {
	//					DoubleBundle db = cand.get(i);
	//					Thread singleton = newT.get(i).getPosSingleton();
	//					if (!db.containsPos(singleton)) {
	//						db.addPos(singleton);
	//					}
	//				}
	//				cand.updatePosHistory(newT.getPosInputList().get(0));
	//				addChain(cand);
	//				//				System.out.println("new cand: "+cand.toString());
	//			}
	//		}
	//		else {
	//			if (M2.DEBUG) System.out.println("[M2] LLMerger creating new Chain.");
	//			// no candidates made it through
	//			// add as new input and update indexes
	//			addChain(newT);
	//		}
	//	}

	//	private synchronized void addNeg(Chain newT) {
	//		//		System.out.println("Adding a negative example");
	//		// STEP 1: update all near-misses to include negative example
	//		Map<Chain, Integer> matches = getMisses(newT, 1);
	//		for (Chain c: matches.keySet()) {
	//			if (matches.get(c) == 1) {				// we will fix perfect matches in step 2
	//				removeChain(c);
	//				applyNegExample(c, newT);
	//				addChain(c);
	//			}
	//		}
	//		// STEP 2: get and remove all incorrect matches
	//		matches = getMisses(newT, 0);
	//		//		System.out.println("matches: "+matches);
	//		// reconstruct new chains so they don't match
	//		for (Chain c: matches.keySet()) {
	//			removeChain(c);
	//			//			System.out.println("neg updating chain: "+c);
	//			List<Thing> posList = c.getPosInputList();
	//			List<Thing> negList = c.getNegInputList();
	//			negList.add(newT.getPosInputList().get(0));
	//			
	//			List<Chain> newChains = reconstitute(posList, negList);
	//			
	//			for (Chain newC : newChains) {
	//				addChain(newC);
	//			}
	//			
	////
	////			// base case chain
	////			Chain c1 = new Chain(inputs.get(0));
	////			for (Thing negThing : negList) {
	////				applyNegExample(c1, new Chain(negThing));
	////				c1.updateNegHistory(negThing);
	////			}
	////			applyNegExample(c1, newT);
	////			c1.updateNegHistory(newT.getPosInputList().get(0));
	////			newChains.add(c1);
	////			//			System.out.println("newChains: "+newChains);
	////
	////			// add each additional input
	////			for (int i=1; i<inputs.size(); i++) {
	////				Chain next = new Chain(inputs.get(i));
	////				Map<Chain, Integer> cands = new HashMap<Chain, Integer>();
	////				for (Chain cc : newChains) {
	////					cands.put(cc, 0);
	////				}
	////				cands = getMissesHelper(next, cands, 1);
	////				if (!cands.isEmpty()) {
	////					for (Chain newC : newChains) {
	////						if (cands.containsKey(newC)) {
	////							for(int j=0; j<next.size(); j++) {
	////								DoubleBundle db = newC.get(j);
	////								Thread singleton = next.get(j).getPosSingleton();
	////								if (!db.containsPos(singleton)) {
	////									db.addPos(singleton);
	////								}
	////							}
	////						}
	////					}
	////				}
	////				else {
	////					// need to fork a new chain, since the neg prevents matching
	////					newChains.add(next);
	////					//					System.out.println("newChains: "+newChains);
	////				}
	////				// update all new Chains to incorporate all relevant neg example
	////				for (Chain newC : newChains) {
	////					for (Thing negThing : negList) {
	////						applyNegExample(newC, new Chain(negThing));
	////						newC.updateNegHistory(negThing);
	////					}
	////					applyNegExample(newC, newT);
	////					newC.updateNegHistory(newT.getPosInputList().get(0));
	////				}
	////			}
	//		}
	//
	//		M2.m2assert(getMisses(newT, 0).isEmpty(), "addNeg -- still matching incorrectly");
	//	}
	//	
	//	private List<Chain> reconstitute(List<Thing> posThings, List<Thing> negThings) {
	//		List<Chain> results = new ArrayList<Chain>();
	//		
	//		Chain c1 = new Chain(posThings.get(0));
	//		results.add(c1);
	//		
	//		for (int i=0; i<posThings.size(); i++) {
	//			Chain c2 = new Chain(posThings.get(i));
	//			
	//			Map<Chain, Integer> cands = new HashMap<Chain, Integer>();
	//			for (Chain cc : results) {
	//				cands.put(cc, 0);
	//			}
	//			
	//			cands = getMissesHelper(c2, cands, 1);
	//			if (!cands.isEmpty()) {
	//				for (Chain r : results) {
	//					if (cands.containsKey(r)) {
	//						for(int j=0; j<c2.size(); j++) {
	//							DoubleBundle db = r.get(j);
	//							Thread singleton = c2.get(j).getPosSingleton();
	//							if (!db.containsPos(singleton)) {
	//								db.addPos(singleton);
	//							}
	//						}
	//						for (Thing negThing : negThings) {
	//							applyNegExample(r, new Chain(negThing));
	//							r.updateNegHistory(negThing);
	//						}
	//					}
	//				}
	//
	//			}
	//			else {
	//				for (Thing negThing : negThings) {
	//					applyNegExample(c2, new Chain(negThing));
	//					c2.updateNegHistory(negThing);
	//				}
	//				results.add(c2);
	//			}
	//		}
	//
	//		return results;
	//	}
	//	
	// helper to update local data storage
	private synchronized void removeChain(Chain c) {
//		modified.remove(c);
		M2.m2assert(chains.remove(c), "LLMerger: removeChain() failed to remove bad chain from chains");
		String repString = c.getRepType();
		M2.m2assert(reps.get(repString).remove(c), "LLMerger: removeChain() failed to remove bad chain from reps");
//		// update Chain neighborhoods
//		for (Chain neighbor: c.getNeighborCopy()) {
//			neighbor.neighbors.remove(c);
//		}
//		c.neighbors.clear();
	}

	// helper to update local data storage
	// not safe to mess with other Chains in this function
	private synchronized void addChain(Chain c) {
//		System.out.println("addChain, input is: "+c);
//		modified.add(c);
		if (!getConflicts(c).isEmpty()) {
			System.out.println("**[M2] BIG ERROR! trying to add a conflicting chain: "+c);
			return;
		}
		//		// calculate Chain new neighborhoods first
		//		for (Chain c2 : chains) {
		//			int dist = c.getChainDistance(c2);
		//			if (dist == 2 || dist == 1){
		//				// these are neighbors; update accordingly
		//				c.neighbors.add(c2);
		//				c2.neighbors.add(c);
		//			}
		//			else if (dist == 0) {
		//				// we should merge these two Chains together (but later, when it is safe)
		//				List<Chain> lst = new ArrayList<Chain>(2);
		//				lst.add(c);
		//				lst.add(c2);
		//				merges.add(lst);
		//				System.out.println("added to merge-list: "+lst);
		//			}
		//			else {
		//				// not a neighbors
		//			}
		//		}

		// add to local data storage structures
		chains.add(c);
		String repString = c.getRepType();
		if (reps.containsKey(repString)) {
			reps.get(repString).add(c);
		}
		else {
			Set<Chain> repSet = new HashSet<Chain>();
			repSet.add(c);
			reps.put(repString, repSet);
		}

	}

	//	private void applyNegExample(Chain target, Chain neg) {
	//		// one negative, no double negatives!
	////		boolean negated = false;
	//		for (int i=0; i<neg.size(); i++) {
	//			DoubleBundle db = target.get(i);
	//			Thread singleton = neg.get(i).getPosSingleton();
	//			if (!(db.containsPos(singleton))) {
	//				db.addNeg(singleton);
	////				return;
	//			}
	//		}
	//	}

	private synchronized Map<Chain, Integer> getMisses(Chain chain, int numMisses) {
		// only search same rep type
		Map<Chain, Integer> candidates = new HashMap<Chain, Integer>();
		String repString = chain.getRepType();
		Set<Chain> repMatches = reps.get(repString);
		if (repMatches == null) repMatches = new HashSet<Chain>();
		for(Chain c: repMatches){
			candidates.put(c, 0);
		}
		return getMissesHelper(chain, candidates, numMisses);
	}

	private synchronized Map<Chain, Integer> getMissesHelper(Chain chain, Map<Chain, Integer> cands, int numMisses) {
		Map<Chain, Integer> candidates = new HashMap<Chain, Integer>(cands);
		for(int i=0; i<chain.size(); i++) {
			// see if there are still Chains to search
			if (candidates.isEmpty()) {
				break;
			}
			// yep, see who matches this level
			DoubleBundle db = chain.get(i);
			//			Thread in = db.getPosSingleton();
			//			M2.m2assert(in!=null, "getmisshelper");
			Set<Chain> tempCand = new HashSet<Chain>(candidates.keySet());
			for (Chain c: tempCand) {
				if (c.size() <= i) {
					candidates.remove(c);
					continue;
				}
				int match = matches(db, c.get(i));
				if (match==0) {
					candidates.remove(c);
				}
				if (match==1) {
					int misses = candidates.get(c);
					misses++;
					if (misses > numMisses) {
						candidates.remove(c);
					}
					else {
						candidates.put(c, misses);
					}
				}
			}			
		}
		return candidates;
	}

	/**
	 * @param timer Thread
	 * @param db Doublebundle
	 * @return
	 * 0 if hard miss,
	 * 1 if soft miss,
	 * 2 if match
	 */
//	private int matches(Thread t, DoubleBundle db) {
//		//		if (db.containsPos(t)) {
//		//			return 2;
//		//		}
//		//		if (db.containsNeg(t)) {
//		//			return 0;
//		//		}
//		//		boolean ll = db.matches(t);
//		//		if (ll==true) {
//		//			return 2;
//		//		}
//		//		return 1;
//		return db.matches(t);
//	}

	private int matches(DoubleBundle singleton, DoubleBundle db) {
		Thread pos = singleton.getPosSingle();
		Thread neg = singleton.getNegSingle();
		if (pos != null) {
			return db.matches(pos);
		}
		else if (neg != null) {
			// TODO: check this logic later
			return 2-db.matches(neg);
		}
		return 0; // hopefully we never get here
	}



	/*
	 * 		PUBLIC GETTER METHODS
	 */

	/**
	 * @param t Thing
	 * @return integer number of misses to the closest match in memory
	 */
	public int getMissDistance(Entity t) {
		// todo: this could be faster!
		Chain c = new Chain(t);
		for (int i=0; i<c.size(); i++) {
			Map<Chain, Integer> misses = getMisses(c, i);
			if (!misses.isEmpty()) {
				return i;
			}
		}
		return c.size();
	}


	/**
	 * @param t Thing
	 * @return true if the Thing represents something the memory finds 
	 * consistent with prior input statements, false otherwise
	 */
	public boolean isPossible(Entity t) {
		Chain c = new Chain(t);
		Map<Chain, Integer> matches = getMisses(c, 0);
		if (!matches.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * @param t Thing
	 * @param numMisses max number of misses tolerated
	 * @return a list of matching Things
	 */
	//	public List<Thing> getSortedMisses(Thing t, int numMisses) {
	//		List<Thing> results = new ArrayList<Thing>();
	//		Map<Chain, Integer> misses = getMisses(new Chain(t), numMisses);
	//		Map<Integer, Set<Thing>> temp = new HashMap<Integer, Set<Thing>>();
	//		for (Chain c: misses.keySet()) {
	//			if (temp.containsKey(misses.get(c))) {
	//				temp.get(misses.get(c)).addAll(c.getPosInputList());
	//			}
	//			else {
	//				Set<Thing> set = new HashSet<Thing>();
	//				set.addAll(c.getPosInputList());
	//				temp.put(misses.get(c), set);
	//			}	
	//		}
	//		for (int i=0; i<=numMisses; i++) {
	//			Set<Thing> set = temp.get(i);
	//			if (set!=null) {
	//				results.addAll(set);
	//			}
	//		}
	//		return results;
	//	}

	public List<Entity> getNearNeighbors(Entity t) {
		Map<Chain, Integer> misses = getMisses(new Chain(t), 0);
		List<Entity> results = new ArrayList<Entity>();
		for (Chain c : misses.keySet()) {
			results.addAll(c.getInputList());
		}
		return results;
	}

	public List<Entity> getNeighbors(Entity t) {
		Map<Chain, Integer> misses = getMisses(new Chain(t), 2);
		List<Entity> results = new ArrayList<Entity>();
		for (Chain c : misses.keySet()) {
			results.addAll(c.getInputList());
		}
		return results;
	}



	//	private synchronized void doMerges() {
	//		System.out.println("STARTING DOMERGES!!!");
	//		System.out.println("merges: " +merges);
	//		List<Chain> toAdd = new ArrayList<Chain>();
	//		List<List<Chain>> merged = new ArrayList<List<Chain>>();
	//		while (merges.size() > 0) {
	//			List<List<Chain>> copy = new ArrayList<List<Chain>>(merges);
	//			for (List<Chain> lst : copy) {
	//				System.out.println("Working on merge-pair: "+lst);
	//				Chain c1 = lst.get(0);
	//				Chain c2 = lst.get(1);
	//				merges.remove(lst);
	//				if (!merged.contains(lst)) {
	//					merged.add(lst);
	//					if (c1.getChainDistance(c2) == 0) {
	//						removeChain(c1);
	//						removeChain(c2);
	//						Chain bkp = c1.clone();
	//						Chain newChain = c1.mergeChain(c2);
	//						if (getConflicts(newChain).isEmpty()) {
	//							// no conflict caused by new chain.
	//							System.out.println("doMerges:no conflicts, adding: "+newChain);
	//							addChain(newChain);
	//						}
	//						else {
	//							// merge would cause conflict -- don't do it.
	//							System.out.println("doMerges: conflicts, adding: "+bkp+" "+c2);
	//							toAdd.add(bkp);
	//							toAdd.add(c2);
	//						}
	//					}
	//				}
	//			}
	//		}
	//		for (Chain c: toAdd) {
	//			addChain(c);
	//		}
	//		System.out.println("DONE DOMERGES!!!");
	//	}
	//
	//
	//	private void doNeighborhood() {
	//		while(modified.size() > 0) {
	//			List<Chain> tempList = new ArrayList<Chain>(modified);
	//			for (Chain c1: tempList) {
	//				removeChain(c1);
	//				// calculate Chain new neighborhoods first
	//				for (Chain c2 : chains) {
	//					int dist = 0;//c1.getChainDistance(c2);
	//					if (dist == 2 || dist == 1){
	//						removeChain(c2);
	//						// these are neighbors; update accordingly
	//						c1.neighbors.add(c2);
	//						c2.neighbors.add(c1);
	//						addChain(c2);
	//					}
	//					else if (dist == 0) {
	//						// try to merge these two Chains together
	//						Chain bkp = c1.clone();
	//						Chain newChain = c1.mergeChain(c2);
	//						if (getConflicts(newChain).isEmpty()) {
	//							// no conflict caused by new chain.
	//							System.out.println("doNeighborhood: no conflicts, adding: "+newChain);
	//							removeChain(c2);
	//							addChain(newChain);
	//						}
	////						else {
	////							// merge would cause conflict -- don't do it.
	////							System.out.println("doNeighborhood: conflicts, adding: "+bkp+" "+c2);
	////							addChain(bkp);
	////						}
	//					}
	//					else {
	//						// not a neighbors
	//					}
	//				}
	//				addChain(c1);
	//			}
	//		}
	//	}

	private synchronized void doMerges() {
//		System.out.println("STARTING DOMERGES!!!");
	
		while(!mergeHelper());
		
//		System.out.println("DONE DOMERGES!!!");
	}

	private boolean mergeHelper() {
		List<Chain> copy = new ArrayList<Chain>(chains);
		for (Chain c1: copy) {
			for (Chain c2: copy) {
				if (!c1.equals(c2) && c1.overlaps(c2)) {
					// should do merge
					removeChain(c1);
					removeChain(c2);
//					Chain bkp = c1.clone();
					Chain test = c1.mergeChain(c2);
					if (getConflicts(test).isEmpty()) {
						// no conflict caused by new chain.
//						System.out.println("doMerges:no conflicts, adding: "+test);
						addChain(test);
						// did a merge... we need to restart this process
						return false;
					}
					else {
						// merge would cause conflict -- don't do it.
//						System.out.println("doMerges: conflicts, adding: "+c1+" "+c2);
						addChain(c1);
						addChain(c2);
					}
				}
			}
		}
		return true;
	}



}

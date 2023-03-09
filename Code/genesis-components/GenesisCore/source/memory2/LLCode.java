package memory2;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dictionary.WordNet;
import frames.entities.Thread;
import memory2.lattice.Concept;
import memory2.lattice.FasterLLConcept;
import memory2.lattice.TypeLattice;

public class LLCode {
	/**
	 * @param posSet - positive examples
	 * @param negSet - negative examples
	 * @return LLConcept to describe the full set of examples
	 */
	public static FasterLLConcept<String> getLLConcept(Set<Thread> posSet, Set<Thread> negSet) {

		//Build TypeLattice from threads
		Set<Thread> allThreads = new HashSet<Thread>();
		allThreads.addAll(posSet);
		allThreads.addAll(negSet);
		TypeLattice lattice = new TypeLattice(allThreads);

		//Build a concept 
		FasterLLConcept<String> llcon = new FasterLLConcept<String>(lattice);
		for (Thread t : posSet) {
			llcon.learnPositive(t.getType());
		}
		for (Thread t : negSet) {
			llcon.learnNegative(t.getType());
		}
		
		// hack -- manufacture artificial negative examples if there are none
		if (negSet.isEmpty()) {
			for (Thread t : posSet) {
				llcon.learnNegative(t.getSupertype());
			}
		}

		// return concept
		return llcon;
	}

	/**
	 * Does full LL search on a thread
	 */
	public static boolean LLSearch(Concept<String> c, Thread query) {
		for (String parent : query) {
			if (c.contains(parent)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Only sees if the thread is explicitly contained by the LL concept --
	 * not really doing LL here.
	 */
	public static boolean LLContains(Concept<String> c, Thread query) {
		return c.contains(query.getType());
	}


	//	static public ISet<Thread> getAccessors(bridge.reps.entities.Bundle posBundle, bridge.reps.entities.Bundle negBundle) {
	//		//Convert old threads to new threads
	//		Bundle positive = Bundle.EMPTY;
	//		Bundle negative = Bundle.EMPTY;
	//		
	//		for (bridge.reps.entities.Thread t : posBundle) {
	//			Thread th = Thread.EMPTY.plusAll(t);
	//			positive = positive.plus(th);
	//		}
	//		
	//		for (bridge.reps.entities.Thread t : negBundle) {
	//			Thread th = Thread.EMPTY.plusAll(t);
	//			negative = negative.plus(th);
	//		}
	//		
	//		//Build TypeLattice from threads
	//		TypeLattice lattice = new TypeLattice(positive.plusAll(negative));
	//
	//		//Build a concept 
	//		LLConcept<String> llcon = new LLConcept<String>(lattice);
	//		for (Thread t : positive) {
	//			llcon = llcon.learnPositive(t.getType());
	//		}
	//		for (Thread t : negative) {
	//			llcon = llcon.learnNegative(t.getType());
	//		}
	//
	//		ISet<Thread> results = HashTreeISet.empty();
	//		
	//		for (String s : llcon.maximalElements()) {
	//			// reconstruct my threads
	//			// presumably I only need to look at pos examples
	//			for (bridge.reps.entities.Thread t : posBundle) {
	//				if (t.contains(s)) {
	//					Thread newThread = Thread.EMPTY.plusAll(t.subList(0, t.indexOf(s)+1));
	//					results = results.plus(newThread);
	//				}
	//			}
	//		}
	//		return results;
	//	}



	public static void main(String[] args) {
		WordNet wn = new WordNet();

		frames.entities.Bundle posBundle = new frames.entities.Bundle();
		for (String s : Arrays.asList("fish", "monkey", "cupcake")) {
			posBundle.add(wn.lookup(s).getPrimedThread());
		}

		frames.entities.Bundle negBundle = new frames.entities.Bundle();
		for (String s : Arrays.asList("tree")) {
			negBundle.add(wn.lookup(s).getPrimedThread());
		}

		//		for(Thread t : SamsCode.getAccessors(posBundle, negBundle)) {
		//			Log.debug(t);
		//		}
	}
}

package memory.soms;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import memory.multithreading.Watcher;
import utils.graphs.UniGraph;
public class NewSom<E> implements Som<E> {
	
	private UniGraph<E, Double> graph = new UniGraph<E, Double>();
	
	private DistanceMetric<E> dm;
	private ElementMerger<E> em;
	
	private double propDist = .5;
	
	private Map<E, Integer> elementCount = new HashMap<E, Integer>();
	private Map<E, E> equivalentElements = new HashMap<E, E>();
	
	public NewSom(DistanceMetric<E> distanceMetric, ElementMerger<E> em, double propDist) {
		this.dm = distanceMetric;
		this.em = em;
		this.propDist = propDist;
	}
	public void add(E e) {
		if (e==null) return;
		Set<E> neighbors = neighbors(e);
		// if graph contains e already, increment its count
		boolean newElement = processNew(e, neighbors);
		// else add it and merge neighbors
		if (newElement) {
			elementCount.put(e, 1);
			graph.addNode(e);
			merge(e, neighbors);
		}
		// notify watchers
		for (Watcher w : watchers) {
			w.ping();
		}
	}
	
	private void addWithoutMerge(E e) {
		Set<E> neighbors = findNeighbors(e);
		// if graph contains e already, increment its count
		boolean newElement = processNew(e, neighbors);
		// else add it
		if (newElement) {
			elementCount.put(e, 1);
			graph.addNode(e);
			for (E n : neighbors) {
				graph.addEdge(e, n, dm.distance(e, n));
			}
		}	
	}
	
	// return true is element is new, false otherwise
	private boolean processNew(E e, Set<E> neighbors) {
		boolean newElement = true;
		for (E n : neighbors) {
			if (dm.distance(e, n) == 0) {
				elementCount.put(n, elementCount.get(n)+1);
				newElement = false;
				equivalentElements.put(e, n);
				break;
			}
		}
		return newElement;
	}
	public Set<E> neighbors(E e) {
		if (e==null) return null;
		if (!graph.contains(e)) {
			if (equivalentElements.containsKey(e)) {
				e = equivalentElements.get(e);
			}
			else {
				return findNeighbors(e);
			}
		}
//		System.out.println("Element: "+e);
//		System.out.println("Sucessors: "+graph.getSuccessors(e));
//		System.out.println("FindNeighbors: "+findNeighbors(e));
//		assert(!findNeighbors(e).contains(e));
//		assert(graph.getSuccessors(e).equals(findNeighbors(e)));
		return graph.getSuccessors(e);
	}
	
	/**
	 * Returns the n nearest elements to Element e in the SOM.
	 * Currently runs in Theta(n) time, where n is the size of the 
	 * SOM.
	 * 
	 * @param e Element
	 * @param n integer
	 * @return List of nearest elements, sorted by proximity
	 */
	public List<E> getNearest(E e, int n) {
		List<E> nearest = new ArrayList<E>();
		//UniGraph<E, Double> distances = new UniGraph<E, Double>();
		SortedMap<Double, Set<E>> distances = new TreeMap<Double, Set<E>>();
		
		for (E node : this.getMemory()) {
			double d = this.getDistance(e, node);
			if (distances.containsKey(d)) {
				distances.get(d).add(node);
			}
			else {
				Set<E> tempSet = new HashSet<E>();
				tempSet.add(node);
				distances.put(d, tempSet);
			}
		}
		while (n>0 && !distances.isEmpty()) {
			Set<E> temp = distances.get(distances.firstKey());
			nearest.addAll(temp);
			n -= temp.size();
			distances.remove(distances.firstKey());
		}
		return nearest;
	}
	
	// merges a set of neighboring elements towards a seed element
	private void merge(E seed, Set<E> neighbors) {
		Set<E> newElements = em.merge(seed, neighbors);
		// seed remains untouched
		// consume neighbors
		for (E neighbor : neighbors) {
			decrementOrRemove(neighbor);
		}
		// add new elements, without doing a merge
		for (E newEl : newElements) {
			addWithoutMerge(newEl);
		}
		
	}
	
	// decrements the count of an element,
	// or removes it if it's 1
	private void decrementOrRemove(E element) {
		int count = elementCount.get(element);
		if (count==1) {
			graph.removeNode(element);
			elementCount.remove(element);
		}
		else {
			elementCount.put(element, (count-1));
		}
	}
	
	// do a manual scan to find the neighbors in the graph
	private Set<E> findNeighbors(E element) {
		Set<E> winners = new HashSet<E>();
		if (equivalentElements.get(element)!=null) {
			element = equivalentElements.get(element);
		}
		for (E e : graph.getNodes()) {
			if (e.equals(element)) continue;
			if (dm.distance(element, e) < propDist) {
				winners.add(e);
			}
		}
		assert(!winners.contains(element));
		return winners;
	}
	
	
// if the below code was mad to work, this would be faster
	
//	// manually locates the neighbors of an element
//	private Set<E> findNeighbors(E element) {
//		Set<E> winners = new HashSet<E>();
//		E seed = findSeedNeighbor(element);
//		if (seed!=null) {
//			winners.add(seed);
//			findNeighborsHelper(element, winners, new HashSet<E>());
//			winners.remove(element);
//			return winners;
//		}
//		return new HashSet<E>();
//	}
//	
//	private void findNeighborsHelper(E element, Set<E> winners, Set<E> visited) {
//		Set<E> maybes = new HashSet<E>(winners);
//		maybes.removeAll(visited);
//		for (E winner : maybes) {
//			visited.add(winner);
//			Set<E> cands = new HashSet<E>(graph.getSuccessors(winner));
//			cands.removeAll(visited);
//			if (cands.isEmpty()) return;
//			for (E next : cands) {
//				if (dm.distance(element, next) < propDist) {
//					winners.add(next);
//				}
//				else {visited.add(next);}
//			}
//		}
//		findNeighborsHelper(element, winners, visited);
//	}
//	
//	// manually locates a single neighbor
//	private E findSeedNeighbor(E element) {
//		for (E el : graph.getNodes()) {
//			if (!el.equals(element)) {
//				if (dm.distance(element, el) < propDist) {
//					return el;
//				}
//			}
//		}
//		return null;
//	}
	
	
	public NewSom<E> clone() {
		NewSom<E> clone = new NewSom<E>(this.dm, this.em, this.propDist);
		clone.graph = new UniGraph<E, Double>(this.graph);
		clone.elementCount = new HashMap<E, Integer>(this.elementCount);
		return clone;
	}
	Set<Watcher> watchers = new HashSet<Watcher>();
	public void add(Watcher w) {
		watchers.add(w);
	}
	
	public Set<E> getMemory() {
		return new HashSet<E>(graph.getNodes());
	}
	public double getPropDist() {
		return this.propDist;
	}
	
	public String toString() {
		return graph.toString();
	}
	public boolean containsEquivalent(E e) {
		if (e==null) {
			return false;
		}
		if (graph.contains(e)) {
			return true;
		}
		else if (equivalentElements.containsKey(e)) {
			return true;
		}
		else {
			// not in graph, see if we would have a match
			for (E n : findNeighbors(e)) {
				if (dm.distance(e, n) == 0) {
					return true;
				}
			}
		}
		return false;
	}
	public double getDistance(E e1, E e2) {
		if (e1==null || e2==null) return 1;
		return dm.distance(e1, e2);
	}
	
	public Map<E, Integer> getWeights() {
		return new HashMap<E, Integer>(elementCount);
	}
}

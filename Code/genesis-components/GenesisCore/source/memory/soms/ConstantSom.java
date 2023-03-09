package memory.soms;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import memory.multithreading.Watcher;
import utils.graphs.UniGraph;
/**
 * A SOM thats maintains a constant number of neighbors for each element.
 * i.e. each element has 5 neighbors.
 * 
 * @author sglidden
 *
 * @param <E>
 */
public class ConstantSom<E> implements Som<E> {
	
	private UniGraph<E, Double> graph = new UniGraph<E, Double>();
	
	private DistanceMetric<E> dm;
	private ElementMerger<E> em;
	
	int numNeighbors;
	
	private Map<E, Integer> elementCount = new HashMap<E, Integer>();
	private Map<E, E> equivalentElements = new HashMap<E, E>();
	
	public ConstantSom(DistanceMetric<E> distanceMetric, ElementMerger<E> em, int numNeighbors) {
		this.dm = distanceMetric;
		this.em = em;
		this.numNeighbors = numNeighbors;
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
		Set<E> neighbors = neighbors(e);
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
				elementCount.put(e, elementCount.get(n)+1);
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
		assert (graph.getSuccessors(e).equals(findNeighbors(e)));
		return graph.getSuccessors(e);
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
	
	// manually locates the neighbors of an element
	private Set<E> findNeighbors(E element) {
		// currently a linear scan. maybe we can improve this in the future
		Map<E, Double> scores = new LinkedHashMap<E, Double>();
		List<E> neighbors = new LinkedList<E>();
		for (E e : graph.getNodes()) {
			double score = dm.distance(e, element);
			scores.put(e, score);
			// insert it into the neighbor list
			for (int i=neighbors.size()-1; i>-1; i--) {
				E n = neighbors.get(i);
				if (i==0 && score < scores.get(n)) {
					neighbors.add(0, e);
				}
				if (score < scores.get(n)) {
					continue;
				}
				else {
					neighbors.add(i+1, e);
				}
			}
			// clean up
			if (neighbors.size() > numNeighbors) {
				for (int i=neighbors.size(); i>numNeighbors-1; i--) {
					E dead = neighbors.get(i);
					scores.remove(dead);
					neighbors.remove(i);
				}
			}
		}
		assert neighbors.size() == numNeighbors;
		return new HashSet<E>(neighbors);
	}
	
	public ConstantSom<E> clone() {
		ConstantSom<E> clone = new ConstantSom<E>(this.dm, this.em, this.numNeighbors);
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
}

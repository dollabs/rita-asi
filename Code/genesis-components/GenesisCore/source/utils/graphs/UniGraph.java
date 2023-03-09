package utils.graphs;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/**
 * Graph is a labeled graph. It consists of a set of
 * nodes, and edges which each connect two nodes. The edges are 
 * labeled. There can only one edge
 * between two nodes. An
 * edge can connect a node to itself.
 * 
 * 
 * @author Sam Glidden
 * @modified 7.18.2007
 *
 * @param <N>: A node must be a type or sub-type of N.
 * @param <E>: An edge label must be a type or sub-type of E.
 * 
 */
public class UniGraph<N, E> {
	
	private Map<N, Map<N, E>> map;
	/**
	 * Default constructor. Takes no arguments, and constructs
	 * an Graph containing no nodes or edges.
	 */
	public UniGraph() {
		map = new HashMap<N, Map<N, E>>();
		assert checkRep();
	}
	
	/**
	 * Constructor. Creates a new graph identical
	 * to the provided one. Does not clone contents.
	 * 
	 * @param g
	 */
	public UniGraph(UniGraph<N, E> g) {
		map = new HashMap<N, Map<N, E>>(g.map);
		assert checkRep();
	}
	
	/**
	 * Returns a set of all the nodes that are successors
	 * of the given node. Node Y is a successor of Node X if
	 * there is one or more edges leading from X to Y.
	 * The returned set of nodes link back to the graph,
	 * so changing them changes the graph. Adding and
	 * removing nodes from the returned set does not, however
	 * add of remove them from the graph.
	 * 
	 * @param node: N
	 * @return HashSet<N> containing successor nodes.
	 */
	public HashSet<N> getSuccessors(N node) {
		assert checkRep();
		return new HashSet<N>(map.get(node).keySet());
	}
	/**
	 * Adds a new node to the graph. The node will not have any
	 * edges associated with it.
	 * 
	 * @param node: N
	 * @return true if node did not previously exist and was added,
	 * otherwise false.
	 */
	public boolean addNode(N node) { 
		assert checkRep();
		if (!map.containsKey(node)) {
			map.put(node, new HashMap<N, E>());
			return true;
		}
		return false;
	}
	/**
	 * Adds an edge to the graph, leading from a startNode to endNode, with
	 * the given label.
	 * 
	 * NOTE: If start and/or end node(s) are not in the graph, they will be
	 * added automatically.
	 * 
	 * If an edge already exists between the two nodes, it will be overrwritten.
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 */
	public void addEdge(N startNode, N endNode, E label) {
		assert checkRep();
		if (!contains(startNode)) { addNode(startNode); }
		if (!contains(endNode)) { addNode(endNode); }
		map.get(startNode).put(endNode, label);
		map.get(endNode).put(startNode, label);
	}
	
	/**
	 * Returns true if the Graph constains the given node. Formally, the graph
	 * contains the node if a node A within the graph satifies
	 * (node==null ? A==null : node.equals(A)). 
	 * 
	 * @param node: N
	 * @return: true if the Graph contains the node.
	 */
	public boolean contains(N node) { 
		assert checkRep();
		return map.containsKey(node);
	}
	
	/**
	 * Returns true if the graph contains the described edge. More formally,
	 * the graph contains the edge if the graph has an edge with the same
	 * start and end nodes, and the same label (where same is defined by 
	 * equals()).
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 * @return: true if the graph contains an edge of the described charactistics.
	 */
	public boolean contains(N startNode, N endNode, E label) {
		assert checkRep();
		if (map.containsKey(startNode) && map.containsKey(endNode)) {
			return map.get(startNode).get(endNode).equals(label);
		}
		return false;
	}
	
	/**
	 * Removes the given node from the Graph. All edges that
	 * lead to or from the node are also removed.
	 * 
	 * @param node: N
	 */
	public void removeNode(N node) {
		assert checkRep();
		
		for (N endNode : getSuccessors(node)) {
			map.get(endNode).remove(node);
		}
		map.remove(node);
	}
	
	
	/**
	 * Removes the edge that between two nodes.
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 */
	public void removeEdge(N startNode, N endNode) { 
		assert checkRep();
		if (contains(startNode) && map.get(startNode).containsKey(endNode)) {
			map.get(startNode).remove(endNode);
			map.get(endNode).remove(startNode);
		}
	}
	/**
	 * Returns the label between two nodes. Changes to the returned label
	 * effect the graph.
	 * 
	 * @param startNode N
	 * @param endNode N
	 * @return: label E, or null if none exist
	 */
	public E getEdge(N startNode, N endNode) {
		assert checkRep();
		if (contains(startNode) && map.get(startNode).containsKey(endNode)) {
			return map.get(startNode).get(endNode);
		}
		else return null;
	}
	
	/**
	 * Returns a set of all the nodes in the graph. The set
	 * links back to the graph, so changing a node will
	 * change it in the graph. You cannot, however, add
	 * or remove nodes in this manner.
	 * 
	 * @return: HashSet<N> of all nodes in graph
	 */
	public HashSet<N> getNodes() {
		assert checkRep();
		// defensive copy
		return new HashSet<N>(map.keySet());
	}
	
	
	/**
	 * Returns the size of the graph, which is just the number of nodes in it.
	 * 
	 * @return number of nodes in graph.
	 */
	public int size() {
		assert checkRep();
		return map.keySet().size();
	}
	
	public String toString() {
		String s = "";
		for (N node : getNodes()) {
			s = s + "\n{ " + node + " ";
			for (N suc : getSuccessors(node)) {
				s = s + "\n   ( " + getEdge(node, suc) + " " + suc + " )";
			}
			s = s + "\n} ";
		}
		return s;
	}
	/*
	 * Testing methods:
	 */
	/**
	 * Checks the representation invarients
	 */
	private boolean checkRep() {
		
		for (N n : map.keySet()) {
			for (N suc : map.get(n).keySet()) {
				if (!map.get(suc).containsKey(n)) {
					return false;
				}
				// make sure edges are symmetric
				if (!map.get(n).get(suc).equals(map.get(suc).get(n))) {
					return false;
				}
			}
		}
		
		
		return true;
	}
}

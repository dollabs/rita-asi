package utils.graphs;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
/**
 * Graph is a directed labeled graph. It consists of a set of
 * nodes, and a set of edges which connect nodes. The edges are 
 * directional and labeled. There can be only one edge
 * between two nodes. An
 * edge can connect a node to itself.
 * 
 * @author Sam Glidden
 * @created 1.31.2008
 *
 * @param <N>: A node must be a type or sub-type of N.
 * @param <E>: An edge label must be a type or sub-type of E.
 * 
 */
public class DirectedGraph<N, E> {
	
	private Map<N, Map<N, E>> starts;
	private Map<N, Map<N, E>> ends;
	/**
	 * Default constructor. Takes no arguments, and constructs
	 * an Graph containing no nodes or edges.
	 */
	public DirectedGraph() {
		starts = new HashMap<N, Map<N, E>>();
		ends = new HashMap<N, Map<N, E>>();
		assert checkRep();
	}
	
	/**
	 * Constructor. Creates a new graph identical
	 * to the provided one. Does not clone contents.
	 * 
	 * @param g
	 */
	public DirectedGraph(DirectedGraph<N, E> g) {
		starts = new HashMap<N, Map<N, E>>(g.starts);
		ends = new HashMap<N, Map<N, E>>(g.ends);
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
		if (starts.containsKey(node)) {
			return new HashSet<N>(starts.get(node).keySet());
		}
		return new HashSet<N>(); 
	}
	
	/**
	 * Returns a set of all the nodes that are predecessors
	 * of the given node. Node Y is a predecessor of Node X if
	 * there is one or more edges leading from Y to X.
	 * The returned set of nodes link back to the graph,
	 * so changing them changes the graph. Adding and
	 * removing nodes from the returned set does not, however
	 * add of remove them from the graph.
	 * 
	 * @param node: N
	 * @return HashSet<N> containing predecessor nodes.
	 */
	public HashSet<N> getPredecessors(N node) {
		assert checkRep();
		if (ends.containsKey(node)) {
			return new HashSet<N>(ends.get(node).keySet());
		}
		return new HashSet<N>();
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
		if (!starts.containsKey(node)) {
			starts.put(node, new HashMap<N, E>());
			ends.put(node, new HashMap<N, E>());
			return true;
		}
		return false;
	}
	/**
	 * Adds an edge to the graph, leading from a startNode to endNode, with
	 * the given label.
	 * 
	 * This replaces any previous edge between the two nodes.
	 * 
	 * NOTE: If start and/or end node(s) are not in the graph, they will be
	 * added automatically.
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 */
	public void addEdge(N startNode, N endNode, E label) {
		assert checkRep();
		if (!contains(startNode)) { addNode(startNode); }
		if (!contains(endNode)) { addNode(endNode); }
		starts.get(startNode).put(endNode, label);
		ends.get(endNode).put(startNode, label);
	}

	/**
	 * Returns true if the Graph contains the given node. Formally, the graph
	 * contains the node if a node A within the graph satisfies
	 * (node==null ? A==null : node.equals(A)). 
	 * 
	 * @param node: N
	 * @return: true if the Graph contains the node.
	 */
	public boolean contains(N node) { 
		assert checkRep();
		return starts.containsKey(node);
	}
	
	/**
	 * Returns true if there is an edge between the two nodes
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 * @return: true if the graph contains an edge of the described characteristics.
	 */
	public boolean contains(N startNode, N endNode) {
		assert checkRep();
		if (starts.containsKey(startNode)) {
			return starts.get(startNode).containsKey(endNode);
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
		
		for (N sNode : starts.get(node).keySet()) {
			ends.get(sNode).remove(node);
		}
		for (N eNode : ends.get(node).keySet()) {
			starts.get(eNode).remove(node);
		}
		
		starts.remove(node);
		ends.remove(node);
	}
	
	
	/**
	 * Removes the edge between two given nodes
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 */
	public void removeEdge(N startNode, N endNode) { 
		assert checkRep();
		if (contains(startNode, endNode)) {
			starts.get(startNode).remove(endNode);
			ends.get(endNode).remove(startNode);
		}
	}
	/**
	 * Returns the label of an edge between two nodes, if it exists. 
	 * Otherwise, returns null.
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @return label E
	 */
	public E getEdgeBetween(N startNode, N endNode) {
		assert checkRep();
		if (contains(startNode) && starts.get(startNode).containsKey(endNode)) {
			return starts.get(startNode).get(endNode);
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
		return new HashSet<N>(starts.keySet());
	}
	
	
	/**
	 * Returns the size of the graph, which is just the number of nodes in it.
	 * 
	 * @return number of nodes in graph.
	 */
	public int size() {
		assert checkRep();
		return starts.keySet().size();
	}
	
	public String toString() {
		String s = "";
		for (N node : getNodes()) {
			s = s + "\n{ " + node + " ";
			for (N suc : getSuccessors(node)) {
				s = s + "\n   ( " + getEdgeBetween(node, suc) + " " + suc + " )";
			}
			s = s + "\n} ";
		}
		return s;
	}
	
	/*
	 * Testing methods:
	 */
	/**
	 * Checks the representation invariants
	 */
	private boolean checkRep() {
		boolean check1 = false, check2 = true, check3 = true;
		// make sure nodes are recorded in both maps
		if (starts.keySet().equals(ends.keySet())) {
			check1 = true;
		}
		for (N node1 : starts.keySet()) {
			for (N node2 : starts.get(node1).keySet()) {
				if (!ends.get(node2).containsKey(node1)) {
					check2 = false;
				}
				if (!starts.get(node1).get(node2).equals(ends.get(node2).get(node1))) {
					check3 = false;
				}
			}
		} 
		return (check1 && check2 && check3);
	}
}

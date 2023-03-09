package utils.graphs;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * Graph is a directed labeled multi-graph. It consists of a set of
 * nodes, and a set of edges which each connect two nodes. The edges are 
 * directional and labeled. There can be any number of distinct edges
 * between two nodes, and distinct edges may have the same label. An
 * edge can connect a node to itself.
 * 
 * Performance:
 * 
 * getSuccessors, getPredecessors, addNode, addEdge, contains, removeEdge,
 * getEdgesBetween, getNodes, and size all take constant time O(c).
 * 
 * removeNode takes O(x) time, where x is the number of nodes connected
 * to the node to be removed.
 * 
 * @author Sam Glidden
 * @modified 10.20.2006
 *
 * @param <N>: A node must be a type or sub-type of N.
 * @param <E>: An edge label must be a type or sub-type of E.
 * 
 * @specfield Nodes : set of N
 * @specfield Edges : Labeled path from one Node to one Node.
 * @specfield Size : Number of nodes.
 */
public class DirectedMultiGraph<N, E> {
	
	private Map<N, Map<N, Set<E>>> starts;
	private Map<N, Map<N, Set<E>>> ends;
	/**
	 * Default constructor. Takes no arguments, and constructs
	 * an Graph containing no nodes or edges.
	 */
	public DirectedMultiGraph() {
		starts = new HashMap<N, Map<N, Set<E>>>();
		ends = new HashMap<N, Map<N, Set<E>>>();
		assert checkRep();
	}
	
	/**
	 * Constructor. Creates a new graph identical
	 * to the provided one. Does not clone contents.
	 * 
	 * @param g
	 */
	public DirectedMultiGraph(DirectedMultiGraph<N, E> g) {
		starts = new HashMap<N, Map<N, Set<E>>>(g.starts);
		ends = new HashMap<N, Map<N, Set<E>>>(g.ends);
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
		return new HashSet<N>(starts.get(node).keySet());
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
		return new HashSet<N>(ends.get(node).keySet());
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
			starts.put(node, new HashMap<N, Set<E>>());
			ends.put(node, new HashMap<N, Set<E>>());
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
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 */
	public void addEdge(N startNode, N endNode, E label) {
		assert checkRep();
		if (!contains(startNode)) { addNode(startNode); }
		if (!contains(endNode)) { addNode(endNode); }
		if (starts.get(startNode).containsKey(endNode)) {
			// already have some edges connecting nodes, 
			// just need to append the new one
			starts.get(startNode).get(endNode).add(label);
			ends.get(endNode).get(startNode).add(label);
		}
		else {
			// no edges connecting nodes, we need to create a new entry
			HashSet<E> temp = new HashSet<E>();
			temp.add(label);
			starts.get(startNode).put(endNode, temp);
			ends.get(endNode).put(startNode, temp);
		}
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
		return starts.containsKey(node);
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
		if (starts.containsKey(startNode)) {
			return starts.get(startNode).get(endNode).contains(label);
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
	 * Removes an edge that meets the given description (has the same
	 * start and end nodes, and the same label).
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @param label: E
	 */
	public void removeEdge(N startNode, N endNode, E label) { 
		assert checkRep();
		if (contains(startNode, endNode, label)) {
			starts.get(startNode).get(endNode).remove(label);
			ends.get(endNode).get(startNode).remove(label);
		}
	}
	/**
	 * Returns a set of edge labels of all edges leading from startNode
	 * to endNode. The set points to labels in the graph, so you can
	 * change a label in the set to change a label in the graph. You
	 * cannot add or remove labels in this manner, however.
	 * 
	 * @param startNode: N
	 * @param endNode: N
	 * @return: HashSet of edge labels of edges connecting startNode to endNode.
	 * If one or both nodes do not exist, an empty HashSet is returned.
	 */
	public HashSet<E> getEdgesBetween(N startNode, N endNode) {
		assert checkRep();
		// defensive copy
		if (contains(startNode) && starts.get(startNode).containsKey(endNode)) {
			return new HashSet<E>(starts.get(startNode).get(endNode));
		}
		else return new HashSet<E>();
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
				for (E edge : getEdgesBetween(node, suc)) {
					s = s + "\n   ( " + edge + " " + suc + " )";
				}
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

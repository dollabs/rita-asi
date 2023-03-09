package memory.story;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Vector;

import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import memory.Memory;
import utils.graphs.DirectedGraph;

/**
 * Designed to store Things (or frames) and the relationships between them. This
 * can be used either to store relationships such as time progression or
 * comparisons, stored in a DirectedGraph.
 * 
 * @author ryscheng
 * @created 05 March 2008
 */
public class FrameGraph {
	/**
	 * Stores a graph of Things (only one edge between nodes) Each edge is
	 * assigned a graph-wide unique ID number, describing the order in which it
	 * was added The unique ID can be mapped to a set of labels in edgeRelations,
	 * describing relationships between nodes
	 */
	private DirectedGraph<Entity, Integer> graph;

	private HashMap<Integer, Map<String, Relation>> edgeRelations;

	private int numEdges;

	public FrameGraph() {
		graph = new DirectedGraph<Entity, Integer>();
		edgeRelations = new HashMap<Integer, Map<String, Relation>>();
		numEdges = 0;
	}

	public FrameGraph(Sequence input) {
		this();
		this.addSequence(input);
	}

	/**
	 * Returns the number of edges in the graph
	 * @return int
	 */
	public int getNumEdges() {
		return numEdges;
	}

	/**
	 * Returns the number of nodes in this graph
	 * @return int
	 */
	public int getNumNodes() {
		return graph.size();
	}

	/**
	 * Returns a set of all the nodes in the graph. The set links back to the
	 * graph, so changing a node will change it in the graph. You cannot,
	 * however, add or remove nodes in this manner.
	 * 
	 * @return: HashSet<N> of all nodes in graph
	 */
	public HashSet<Entity> getNodes() {
		return graph.getNodes();
	}
	
	/**
	 * Returns a map, where key = Relationship Type, object = Relation object
	 * containing all relationships defined by edge i
	 * @param i int - determines which edge to pick
	 * @return Map
	 */
	public Map<String,Relation> getEdgeRelations(int i){
		return edgeRelations.get(i);
	}

	/**
	 * Add a sequence of Relations to the Frame Graph. This is not a traditional
	 * sequence that represents chronological order. It is merely used as a list
	 * to store only Relations.
	 * 
	 * @param input
	 *            Sequence
	 */
	public void addSequence(Sequence input) {
		Vector<Entity> elements = input.getElements();

		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i) instanceof Relation) {
				this.add((Relation) elements.get(i));
			}
		}
	}

	/**
	 * Stores a set of two related representations. Order matters. The edge has
	 * no labels
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 */
	public void add(Entity t1, Entity t2) {
		if (!graph.contains(t1, t2)) {
			graph.addEdge(t1, t2, numEdges);
			edgeRelations.put(numEdges, new HashMap<String, Relation>());
			numEdges++;
		}
	}

	/**
	 * Stores a set of two related representations. Order matters. relation is a
	 * String that describes the relationship between t1 and t2. If the
	 * relationship already exists, replace with a new Relation.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @param relationship
	 *            String
	 */
	public void add(Entity t1, Entity t2, String relationship) {
		int edgeNum;
		if (!graph.contains(t1, t2)) {
			edgeNum = numEdges;
			graph.addEdge(t1, t2, edgeNum);
			edgeRelations.put(edgeNum, new HashMap<String, Relation>());
			numEdges++;
		} else {
			edgeNum = graph.getEdgeBetween(t1, t2);
		}

		edgeRelations.get(edgeNum).put(relationship,
				new Relation(relationship, t1, t2));
	}

	/**
	 * Stores a set of two related representations. Order matters. relation is a
	 * Relation that describes the relationship between t1 and t2. If the
	 * relationship already exists, replace with the new Relation.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @param relationship
	 *            Relation
	 */
	public void add(Entity t1, Entity t2, Relation relationship) {
		int edgeNum;
		if (!graph.contains(t1, t2)) {
			edgeNum = numEdges;
			graph.addEdge(t1, t2, edgeNum);
			edgeRelations.put(edgeNum, new HashMap<String, Relation>());
			numEdges++;
		} else {
			edgeNum = graph.getEdgeBetween(t1, t2);
		}

		edgeRelations.get(edgeNum).put(relationship.getType(), relationship);
	}

	/**
	 * Creates an edge in the graph between the subject and object of the
	 * relation, indexed by the type of the relation
	 * 
	 * @param r
	 *            Relation
	 */
	public void add(Relation r) {
		if (r != null) {
			this.add(r.getSubject(), r.getObject(), r);
		}
	}

	/**
	 * Removes all linkages between two Things. If neither Thing has any more
	 * associations, then it is also removed.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 */
	public void remove(Entity t1, Entity t2) {
		Integer edgeNum = graph.getEdgeBetween(t1, t2);
		if (edgeNum != null) {
			edgeRelations.remove(edgeNum);
		}

		graph.removeEdge(t1, t2);
		if (graph.getSuccessors(t1).isEmpty()
				&& graph.getPredecessors(t1).isEmpty()) {
			graph.removeNode(t1);
		}
		if (graph.getSuccessors(t2).isEmpty()
				&& graph.getPredecessors(t2).isEmpty()) {
			graph.removeNode(t2);
		}
	}

	/**
	 * Removes a label on an edge (specified by relation) between two Things.
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @param relation
	 *            String
	 */
	public void remove(Entity t1, Entity t2, String relationship) {
		Integer edgeNum = graph.getEdgeBetween(t1, t2);
		if (edgeNum != null) {
			edgeRelations.get(edgeNum).remove(relationship);
		}
	}

	/**
	 * Removes a label on an edge between the subject and object of the
	 * parameter. The label removed is the parameter's getType() result.
	 * 
	 * @param r
	 *            Relation
	 */
	public void remove(Relation r) {
		if (r != null) {
			this.remove(r.getSubject(), r.getObject(), r.getType());
		}
	}

	/**
	 * Get a Map of relationships between t1 and t2. Order matters. These also
	 * correspond to edge labels in the graph
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @return Map of relationships, relating t1 to t2. Key is type, Value is
	 *         Relation Thing
	 */
	public Map<String, Relation> getLinkRelationships(Entity t1, Entity t2) {
		Integer edgeNum = graph.getEdgeBetween(t1, t2);

		if (edgeNum == null) {
			return null;
		}

		return edgeRelations.get(edgeNum);
	}

	/**
	 * Get the Relation Thing associated with t1 and t2 by relationship
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @param relationship
	 *            String
	 * @return
	 */
	public Relation getLinkRelationship(Entity t1, Entity t2, String relationship) {
		Map<String, Relation> results = this.getLinkRelationships(t1, t2);

		if (results == null) {
			return null;
		}

		return results.get(relationship);
	}

	/**
	 * Checks to see if t1 and t2 are related by the String relationship
	 * 
	 * @param t1
	 *            Thing
	 * @param t2
	 *            Thing
	 * @param relationship
	 *            String
	 * @return
	 */
	public boolean isLinkRelated(Entity t1, Entity t2, String relationship) {
		Integer edgeNum = graph.getEdgeBetween(t1, t2);
		if (edgeNum == null) {
			return false;
		} else {
			return edgeRelations.get(edgeNum).containsKey(relationship);
		}
	}

	/**
	 * Checks to see if the Relation is in the graph
	 * 
	 * @param r Relation
	 * @return
	 */
	public boolean isLinkRelated(Relation r) {
		if (r != null) {
			return false;
		}
		return this.isLinkRelated(r.getSubject(), r.getObject(), r.getType());
	}
	
	/**
	 * Returns all start nodes for a given end node and relationship, across
	 * only one link
	 * 
	 * @param t2
	 *            Thing - end node
	 * @param relationship
	 *            String
	 * @return Set of start nodes that are linked to the end node by
	 *         relationship
	 */
	public Set<Entity> getLinkStartNodes(Entity t2, String relationship) {
		HashSet<Entity> startNodes = graph.getPredecessors(t2);
		Iterator<Entity> nodeIterator = startNodes.iterator();
		Entity currStartNode;

		while (nodeIterator.hasNext()) {
			currStartNode = nodeIterator.next();
			if (!this.isLinkRelated(currStartNode, t2, relationship)) {
				startNodes.remove(currStartNode);
			}
		}
		return startNodes;
	}

	/**
	 * Returns all end nodes for a given start node and relationship, across
	 * only one link
	 * 
	 * @param t2
	 *            Thing - start node
	 * @param relationship
	 *            String
	 * @return Set of end nodes that are linked to the start node by
	 *         relationship
	 */
	public Set<Entity> getLinkEndNodes(Entity t1, String relationship) {
		HashSet<Entity> endNodes = graph.getSuccessors(t1);
		Iterator<Entity> nodeIterator = endNodes.iterator();
		Entity currEndNode;

		while (nodeIterator.hasNext()) {
			currEndNode = nodeIterator.next();
			if (!this.isLinkRelated(t1, currEndNode, relationship)) {
				endNodes.remove(currEndNode);
			}
		}
		return endNodes;
	}

	/**
	 * Return all start nodes for a given end node and relationship, across an
	 * arbitary number of links. If there is a loop, it will properly terminate,
	 * including both end points
	 * 
	 * @param t2
	 *            Thing - end Node
	 * @param relationship
	 *            String
	 * @return Set of start nodes that are related to end node across an
	 *         arbitrary number of relationship links
	 */
	public List<Entity> getStartNodes(Entity t2, String relationship) {
		return this.getStartNodesHelper(t2, relationship,
				new ArrayList<Entity>());
	}

	private List<Entity> getStartNodesHelper(Entity t2, String relationship,
			List<Entity> result) {
		Set<Entity> currStartNodes = this.getLinkStartNodes(t2, relationship);

		if (!currStartNodes.isEmpty()) {
			for (Entity node : currStartNodes) {
				if (!result.contains(node)) {
					result.add(node);
				}
			}
			for (Entity node : currStartNodes) {
				this.getStartNodesHelper(node, relationship, result);
			}
		}
		return result;
	}

	/**
	 * Return all end nodes for a given start node and relationship, across an
	 * arbitary number of links. If there is a loop, it will properly terminate,
	 * including both end points
	 * 
	 * @param t1
	 *            Thing - start Node
	 * @param relationship
	 *            String
	 * @return Set of end nodes that are related to start node across an
	 *         arbitrary number of relationship links
	 */
	public List<Entity> getEndNodes(Entity t1, String relationship) {
		return this.getEndNodesHelper(t1, relationship, new ArrayList<Entity>());
	}

	private List<Entity> getEndNodesHelper(Entity t1, String relationship,
			List<Entity> result) {
		Set<Entity> currEndNodes = this.getLinkEndNodes(t1, relationship);

		if (!currEndNodes.isEmpty()) {
			for (Entity node : currEndNodes) {
				if (!result.contains(node)) {
					result.add(node);
				}
			}
			for (Entity node : currEndNodes) {
				this.getEndNodesHelper(node, relationship, result);
			}
		}
		return result;
	}

	/**
	 * Returns whether there is a directed path (across an arbitrary number of
	 * links), that connects t1 and t2 (transitive property)
	 * 
	 * @param t1 Thing
	 * @param t2 Thing
	 * @param relationship String
	 * @return boolean
	 * 
	 */
	public boolean isRelated(Entity t1, Entity t2, String relationship) {
		return this.getEndNodes(t1, relationship).contains(t2);
	}
	
	/**
	 * @todo This has never been tested. Try it, or fix it, or both
	 * That would be nice
	 * 
	 * @param t1
	 * @param t2
	 * @param relationship
	 * @return
	 */
	public int getShortestPathLength(Entity t1, Entity t2, String relationship) {
		return this.getShortestPathLengthHelper(t1,t2,relationship,0);
	}
	
	private int getShortestPathLengthHelper(Entity t1, Entity t2, String relationship,
											int currLength) {
		Set<Entity> currEndNodes = this.getLinkEndNodes(t1, relationship);
		int pathLength = 0;
		int tempLength;
		currLength++;
		
		if (!currEndNodes.isEmpty()) {
			for (Entity node : currEndNodes) {
				if (node.equals(t2)){
					return currLength;
				}
				else {
					tempLength = this.getShortestPathLengthHelper(node,t2, relationship, currLength);
					if ((tempLength > 0) && (tempLength < pathLength)){
						pathLength = tempLength;
					}
				}
			}
		}
		return pathLength;
	}

	/**
	 * Returns a list of all relationships in this graph
	 * @return List<String>
	 */
	public List<String> getAllRelationships(){
		ArrayList<String> result = new ArrayList<String>();
		Iterator<Map<String, Relation>> edgeValues = edgeRelations.values().iterator();
		Iterator<String> tempSetIter;
		String tempStr;
		
		// Populate result with a list of all possible relationships
		while (edgeValues.hasNext()) {
			tempSetIter = edgeValues.next().keySet().iterator();
			while (tempSetIter.hasNext()) {
				tempStr = tempSetIter.next();
				if (!result.contains(tempStr)) {
					result.add(tempStr);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns a list of all relationships for which there is a directed path
	 * from t1 to t2 (using transitive property)
	 * 
	 * @param t1 Thing
	 * @param t2 Thing
	 * @return List of relationships between t1 and t2
	 */
	public List<String> getRelationships(Entity t1, Entity t2) {
		List<String> result = this.getAllRelationships();
		// Remove the relationship if there is no relation
		for (String relationship : result) {
			if (!this.isRelated(t1, t2, relationship)) {
				result.remove(relationship);
			}
		}
		return result;
	}

	/**
	 * For the sake of saving memory, we can store all of the information in this
	 * FrameGraph in a Sequence. 
	 * This will contain a bundle of Relations (not necessarily chronologically sequential)
	 * that can be fed back into the FrameGraph constructor to form an equivalent FrameGraph
	 * @return Sequence
	 */
	public Sequence toSequence() {
		Sequence result = new Sequence();
		Iterator<Map<String, Relation>> edgeValues = edgeRelations.values()
				.iterator();
		Iterator<Relation> tempValuesIter;

		// Populate result with a list of all possible relationships
		while (edgeValues.hasNext()) {
			tempValuesIter = edgeValues.next().values().iterator();
			while (tempValuesIter.hasNext()) {
				result.addElement(tempValuesIter.next());
			}
		}

		return result;
	}

	public static void main(String args[]) {
		FrameGraph graph = new FrameGraph();
		FrameGraph newGraph;
		List<String> resultListStr;
		List<Entity> resultListThing;
		Set<Entity> resultSetThing;
		Sequence graphSeq;
		Entity t1 = new Entity("Ray");
		Entity t2 = new Entity("Patrick");
		Entity t3 = new Entity("Mike");
		Entity t4 = new Entity("Mark");

		graph.add(t3, t1, "older");
		graph.add(t4, t3, new Relation("older", t4, t3));
		graph.add(new Relation("older", t2, t4));

		System.out.println("Relationships between Patrick and Ray");
		resultListStr = graph.getRelationships(t2, t1);
		for (String tempStr : resultListStr) {
			System.out.println(tempStr);
		}

		System.out.println("Is Patrick Older than Ray?");
		if (graph.isRelated(t2, t1, "older")) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}

		System.out.println("Who is Patrick older than?");
		resultListThing = graph.getEndNodes(t2, "older");
		for (Entity tempThing : resultListThing) {
			System.out.println(tempThing.getType());
		}

		System.out.println("Who is Mark older than?");
		resultListThing = graph.getEndNodes(t4, "older");
		for (Entity tempThing : resultListThing) {
			System.out.println(tempThing.getType());
		}
		
		System.out.println("All relationships in this graph:");
		resultListStr = graph.getAllRelationships();
		for (String tempStr : resultListStr) {
			System.out.println(tempStr);
		}
		

		graphSeq = graph.toSequence();
		newGraph = new FrameGraph(graphSeq);
		// Now test for same graph (remember could have different edge numbers)

	}
}

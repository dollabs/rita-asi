package bryanWilliams.Coherence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import conceptNet.conceptNetModel.ConceptNetJustification;
import constants.Markers;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import frames.entities.Entity;
import frames.entities.Sequence;
import utils.Mark;
import utils.tools.Predicates;

public class CauseGraph {

    private final Set<CauseNode> roots;
    private final Set<CauseNode> nodes;
    private boolean containsCycle;

    public CauseGraph() {
        roots = new HashSet<CauseNode>();
        nodes = new HashSet<CauseNode>();
        containsCycle = false;
    }
    
    /**
     * Creates a CauseGraph by extracting the causal connections from the passed in sequence. A connection is causal
     * if it's a prediction or cause.
     */
    public CauseGraph(Sequence story) {
        this();
        for (Entity causeEntity : getCausalEntities(story.getElements())) {
            Sequence cause = (Sequence) causeEntity.getSubject();
            Set<Entity> causes = new HashSet<Entity>(); 
            cause.stream().forEach(e -> causes.add(e));

            Entity effect = causeEntity.getObject();
            // label connection with ConceptNet justification if present
            Optional<String> label;
            if (causeEntity.hasProperty(Markers.CONCEPTNET_JUSTIFICATION)) {
                @SuppressWarnings("unchecked")
                List<ConceptNetJustification> justification = (List<ConceptNetJustification>) causeEntity.getProperty(Markers.CONCEPTNET_JUSTIFICATION);
                label = Optional.of(ConceptNetJustification.toJustificationString(justification));
            } else {
                label = Optional.empty();
            }
            for (Entity c : causes) {
                this.addEdge(c, effect, label);
            }
        }
    }
    
    /**
     * Extracts the causal entities from the passed in collection. An entity is causal
     * if it's a prediction or cause.
     */
    public static Set<Entity> getCausalEntities(Collection<Entity> entities) {
        return entities.stream()
                .filter(e -> Predicates.isCause(e) || Predicates.isEntail(e) || 
                             Predicates.isInference(e) || Predicates.isMeans(e))
                .collect(Collectors.toSet());
    }
    
    /**
     * Extracts the causal entities from the passed in sequence. An entity is causal
     * if it's a prediction or cause.
     */
    public static Set<Entity> getCausalEntities(Sequence s) {
        return getCausalEntities(s.getElements());
    }
    
    /**
     * Returns the set of nodes in the CauseGraph. The CauseGraph
     * is backed by this set, so be careful with making changes!
     * @return the set of nodes in the CauseGraph
     */
    public Set<CauseNode> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the set of roots (nodes without any parent) in the CauseGraph. 
     * The CauseGraph is backed by this set, so be careful with making changes!
     * @return the set of roots in the CauseGraph
     */
    public Set<CauseNode> getRoots() {
        return roots;
    }
    
    /**
     * Checks if the graph contains a node representing the given entity.
     * @param event - the entity to be checked
     * @return true if the graph contains an Entity equal to the given one, false otherwise
     */
    public boolean containsEntity(Entity event) {
        return nodes.stream().anyMatch(node -> node.getEvent().equals(event));
    }
    
    /**
     * Returns the CauseNode representing the given event in the graph
     * @param event - the entity for which a representative node should be returned
     * @return the CauseNode with an event equal to the given Entity if one exists in
     * the graph; otherwise returns null
     */
    public CauseNode nodeForEntity(Entity event) {
        for (CauseNode node : nodes) {
            if (node.getEvent().equals(event)) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Return the CauseNode representing the given event in the graph, 
     * creating one if necessary
     * @param event - the entity for which a representative node should be returned
     * @return a CauseNode with an event equal to the given Entity. will be "freshly"
     * created if one did not previously exist
     */
    private CauseNode findOrCreateNodeForEntity(Entity event) {
        CauseNode node = nodeForEntity(event);
        if (node == null) {
            node = new CauseNode(event);
            nodes.add(node);
            roots.add(node);
        }
        return node;
    }

    /**
     * Adds a node to the graph representing the given Entity if one does not
     * already exist
     * @param event - the Entity for which a node should be added
     * @return true if the graph did not previously contain a node representing this 
     * event, false otherwise
     */
    public boolean addNode(Entity event) {
        if (containsEntity(event)) {
            return false;
        }
        CauseNode node = new CauseNode(event);
        nodes.add(node);
        roots.add(node);
        return true;
    }
    
    /**
     * @return true iff connecting causeNode and effectNode induces a cycle in this cause graph
     */
    private boolean inducesCycle(CauseNode causeNode, CauseNode effectNode) {
        if (causeNode.equals(effectNode)) {
            return true;
        }
        
        Queue<CauseNode> q = new LinkedList<>();
        q.add(effectNode);
        while (!q.isEmpty()) {
            CauseNode curNode = q.remove();
            if (curNode.equals(causeNode)) {
                return true;
            }
            q.addAll(curNode.getChildren());
        }
        return false;
    }

    /**
     * Adds a directed edge between the nodes for two entities, either of which may
     * not exist in the graph already
     * @param cause - the entity from which the edge departs
     * @param effect - the node at the end of the edge
     * @param label - an optional label between the cause and effect
     */
    public void addEdge(Entity cause, Entity effect, Optional<String> label) {
        CauseNode causeNode = findOrCreateNodeForEntity(cause);
        CauseNode effectNode = findOrCreateNodeForEntity(effect);
        
        if (!containsCycle && inducesCycle(causeNode, effectNode)) {
            containsCycle = true;
            nodes.forEach(node -> node.setInCyclicalGraph(true));
        } else if (containsCycle) {
            causeNode.setInCyclicalGraph(true);
            effectNode.setInCyclicalGraph(true);
        }
        
        if (label.isPresent()) {
            causeNode.addChild(effectNode, label.get());
        } else {
            causeNode.addChild(effectNode);
        }
        effectNode.addParent(causeNode);
        if (roots.contains(effectNode)) {
            roots.remove(effectNode);
        }
    }
    
    public void addEdge(Entity cause, Entity effect) {
        addEdge(cause, effect, Optional.empty());
    }
    
    /**
     * @return the number of nodes in the CauseGraph
     */
    public int numNodes() {
        return nodes.size();
    }
    
    /**
     * @return the number of roots (nodes without any parent) in the CauseGraph
     */
    public int numRoots() {
        return roots.size();
    }
    
    /**
     * @return the length of the longest cause chain in this graph, or 0 if the graph contains cycles
     */
    public int maxChainLength() {
        if (roots.size() == 0) {
            return 0;
        }
        return 1+roots.stream().map(n->n.getHeight()).max(Integer::compare).get();
    }
    
    /**
     * Returns the "chains" (Lists of CauseNodes) of maximum length
     * that occur in the CauseGraph. A set of chains is returned 
     * because there could be multiple chains with a length equal to 
     * the maximum of all possible chains. 
     * The results are not meaningful if the graph contains cycles.
     * @return the set of "chains" (Lists of CauseNodes) of maximum length
     * that occur in the CauseGraph
     */
    // TODO change this - list should not be in set
    public Set<List<CauseNode>> getMaxLengthChains() {
        int maxRootHeight = maxChainLength() - 1;
        Set<CauseNode> rootsWithMaxLength = roots.stream()
                .filter(root -> root.getHeight() == maxRootHeight)
                .collect(Collectors.toSet());
        Set<List<CauseNode>> maxLengthChains = new HashSet<>();
        for (CauseNode root : rootsWithMaxLength) {
            maxLengthChains.addAll(getMaxLengthChains(root));
        }
        return maxLengthChains;
    }
    
    /**
     * Returns the "chains" (Lists of CauseNodes) of maximum length from the 
     * given start node. A set of chains is returned because there could be multiple
     * chains with a length equal to the maximum of all possible chains stemming from
     * the start node.
     * The results are not meaningful if the graph contains cycles.
     * @param start - the start node from which the maximum length chains should be computed
     * @return the set of "chains" (Lists of CauseNodes) of maximum length from the given
     * start node
     */
    // TODO change this - list should not be in set
    public Set<List<CauseNode>> getMaxLengthChains(CauseNode start) {
        Set<List<CauseNode>> maxLengthChains = new HashSet<>();
        if (!start.hasChild()) {
            List<CauseNode> chain = new ArrayList<>();
            chain.add(start);
            maxLengthChains.add(chain);
        } else {
            int startHeight = start.getHeight();
            Set<CauseNode> nextNodesToFollow = start.getChildren().stream()
                    .filter(child -> child.getHeight() == (startHeight - 1))
                    .collect(Collectors.toSet());
            for (CauseNode nextNode : nextNodesToFollow) {
                for (List<CauseNode> nextChain : getMaxLengthChains(nextNode)) {
                    List<CauseNode> curChain = new ArrayList<>();
                    curChain.add(start);
                    curChain.addAll(nextChain);
                    maxLengthChains.add(curChain);
                }
            }
        }
        return maxLengthChains;
    }
    
    /**
     * @return the maximum branching factor (number of children) for a node in this graph
     */
    public int maxBranchingFactor() {
        if (roots.size() == 0) {
            return 0;
        }
        return roots.stream().map(n->n.getMaxBranchingFactor()).max(Integer::compare).get();
    }
    
    /**
     * @return the weakly connected components in the cause graph. Each component
     * is represented as a set of CauseNodes, so the collection of components
     * is a set of sets of CauseNodes.
     */
    // TODO change this - set should not be in set
    public Set<Set<CauseNode>> weaklyConnectedComponents() {
        Set<Set<CauseNode>> components = new HashSet<Set<CauseNode>>();
        
        for (CauseNode root: roots) {
            Set<CauseNode> curComponent = new HashSet<CauseNode>();
            Queue<CauseNode> q = new LinkedList<CauseNode>();
            curComponent.add(root);
            q.add(root);
            while (!q.isEmpty()) {
                CauseNode curNode = q.remove();
                boolean inOtherComponent = false;
                for (Set<CauseNode> component : components) {
                    if (component.contains(curNode)) {
                        curComponent.addAll(component);
                        components.remove(component);
                        inOtherComponent = true;
                        break;
                    }
                }
                
                if (!inOtherComponent && !curComponent.contains(curNode)) {
                    curComponent.add(curNode);
                    q.addAll(curNode.getChildren());
                }
            }
            components.add(curComponent);
        }
        
        return components;
    }
    
    /**
     * Returns the (weakly) connected component to which the given CauseNode belongs
     * @param node - the node for which the containing connected component should be returned
     * @return the connected component to which the given CauseNode belongs
     */
    public Set<CauseNode> componentContainingNode(CauseNode node) {
        Set<Set<CauseNode>> connectedComponents = weaklyConnectedComponents();
        for (Set<CauseNode> component : connectedComponents) {
            if (component.contains(node)) {
                return component;
            }
        }
        // should never happen
        assert false;
        return null;
    }
    
    /**
     * @return the number of weakly connected components in the graph
     */
    public int numWeaklyConnectedComponents() {
        return weaklyConnectedComponents().size();
    }
    
    
    /**
     * @return all CauseNodes downstream of the given node (reachable by following outgoing edges)
     */
    public Set<CauseNode> getDownstreamNodes(CauseNode node) {
        assert nodes.contains(node);
        Set<CauseNode> downstreamNodes = new HashSet<>();
        Queue<CauseNode> q = new LinkedList<>();
        q.add(node);
        while (!q.isEmpty()) {
            CauseNode curNode = q.remove();
            if (!downstreamNodes.contains(curNode)) {
                downstreamNodes.add(curNode);
                q.addAll(curNode.getChildren());
            }
        }
        // should not include the starting node
        downstreamNodes.remove(node);
        return downstreamNodes;
    }
    
    /**
     * "Says" the graph (prints it out using Mark.say).
     */
    public void sayGraph() {
        Queue<CauseNode> q = new LinkedList<CauseNode>();
        Set<CauseNode> alreadySeen = new HashSet<CauseNode>();
        for (CauseNode root : roots) {
            q.add(root);
            Mark.say("New root: "+root.toString());
            while (!q.isEmpty()) {
                CauseNode curNode = q.remove();
                if (alreadySeen.contains(curNode)) {
                    continue;
                }
                alreadySeen.add(curNode);
                Mark.say(curNode.toString() +" -> " + curNode.getChildren().toString());
                q.addAll(curNode.getChildren());
            }
        }
    }

    /**
     * Returns a new compressed graph in which the event of every node  
     * satisfies the filterFunction. The connections from the original graph
     * are preserved in the new graph, even if this requires "jumping" over an arbitrary number
     * of filtered out nodes in the original graph.
     * @param filterFunction - a function which maps Entities to true if they should 
     * be included in the compressed graph and false otherwise
     * @return a new compressed graph in which the event of every node satisfies the filter
     * function, preserving connections from the original graph
     */
    public CauseGraph compressGraph(Function<Entity, Boolean> filterFunction) {
        CauseGraph g = new CauseGraph();
        Queue<CauseNode> q = new LinkedList<CauseNode>();
        Set<CauseNode> alreadySeen = new HashSet<CauseNode>();
        roots.stream().forEach(root -> q.add(root));
        while (!q.isEmpty()) {
            CauseNode curNode = q.remove();

            if (alreadySeen.contains(curNode)) {
                continue;
            }

            Entity curEvent = curNode.getEvent();
            if (filterFunction.apply(curEvent)) {
                // search backwards from curEvent to find closest 
                // parents that weren't filtered out
                g.addNode(curEvent);
                Queue<CauseNode> parentQ = new LinkedList<CauseNode>();
                parentQ.addAll(curNode.getParents());
                Set<CauseNode> parentsAlreadySeen = new HashSet<>();
                while (!parentQ.isEmpty()) {
                    CauseNode curParentNode = parentQ.remove();
                    if (parentsAlreadySeen.contains(curParentNode)) {
                        continue;
                    }
                    parentsAlreadySeen.add(curParentNode);
                    
                    Entity curParentEvent = curParentNode.getEvent();
                    if (filterFunction.apply(curParentEvent)) {
                        g.addEdge(curParentEvent, curEvent);
                    } else {
                        parentQ.addAll(curParentNode.getParents());
                    }
                }
            }
            
            q.addAll(curNode.getChildren());
            alreadySeen.add(curNode);
        }

        return g;
    }
    
    /**
     * @return a DirectedGraph (JUNG library) version of the CauseGraph, preserving all 
     * existing nodes and edges
     */
    public DirectedGraph<CauseNode, String> getVisualGraph() {
        DirectedGraph<CauseNode, String> g = new DirectedSparseGraph<CauseNode, String>();
        for (CauseNode node : nodes) {
            g.addVertex(node);
        }
        int edgeNum = 0;
        for (CauseNode node : nodes) {
            for (CauseNode child : node.getChildren()) {
                // ensure every edge will have a distinct label by adding an arbitrary (but unique) edge number
                String label = edgeNum+"";
                if (node.hasLabelForChild(child)) {
                    label += " "+node.getLabelForChild(child);
                }
                g.addEdge(label, node, child);
                edgeNum++;
            }
        }
        return g;
    }
}

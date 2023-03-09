package bryanWilliams.Learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import bryanWilliams.Util;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Tree;
import frames.entities.Entity;
import matchers.Substitutor;
import matchers.representations.BindingPair;

// CAUTION: Not for use in sets or maps (no .equals() or .hashCode())
public class ReasoningNode implements Iterable<ReasoningNode> {
    private final Entity entity;
    private final ReasoningNode parent;
    private final List<ReasoningNode> children;
    
    // bindings specific to this entity. no relevance to parents or children
    private final Set<BindingPair> localBindings;
    
    // map of every base entity in every node along path to root to a binding pair which
    // binds entity to a unique placeholder. entities that are equivalent (bound together by some
    // localBindings in the path) are mapped to same placeholder
    private final Map<Entity, BindingPair> bindingsToRoot;
    
    // a version of this node's entity with substituted name components such that no placeholder appears
    // more than once on the path from the root to this node
    private final Entity substitutedEntity;
    
    // index in Util's placeholders of first placeholder not in bindingsToRoot, 
    // or Util.numPlaceholders() if all placeholders are used
    private int nextPlaceholderIndex;
    
    public ReasoningNode(Entity entity, ReasoningNode parent) {
        this(entity, parent, new HashSet<>());
    }
    
    public ReasoningNode(Entity entity, ReasoningNode parent, 
            Set<BindingPair> bindings) {
        this(entity, parent, bindings, new ArrayList<>());
    }
    
    public ReasoningNode(Entity fullEntity, ReasoningNode parent, 
            Set<BindingPair> localBindings, List<ReasoningNode> children) {
        this.entity = fullEntity;
        this.localBindings = new HashSet<>(localBindings);
        this.parent = parent;
        this.children = new ArrayList<>(children);
        
        if (isRoot()) {
            nextPlaceholderIndex = 0;
            bindingsToRoot = new HashMap<>();
        } else {
            nextPlaceholderIndex = parent.nextPlaceholderIndex;
            bindingsToRoot = parent.getBindingsToRoot();
        }
        populateBindingsToRoot();
        
        Entity tempSubstitutedEntity = entity;
        for (Entity e : Util.getAllPersonComponents(entity)) {
            Entity standardizedPlaceholder = bindingsToRoot.get(e).getPattern();
            tempSubstitutedEntity = Substitutor.substitute(standardizedPlaceholder, 
                    e, tempSubstitutedEntity);
        }
        substitutedEntity = tempSubstitutedEntity;
    }
    
    /**
     * Initializes bindingsToRoot, a map of every base entity in every node along path to
     * root to a unique placeholder. 
     * Entities that are equivalent (bound together) are mapped to the same placeholder.
     */
    private void populateBindingsToRoot() {
        if (isRoot()) {
            // Root should not have any bindings or placeholders. Just a 
            // simple Entity, like "Macbeth harms Lady Macduff."
            assert localBindings.isEmpty();
            for (Entity e : Util.getAllPersonComponents(entity)) {
                bindingsToRoot.put(e, new BindingPair(e, e, 1.0));
            }
        } else {
            for (Entity e : Util.getAllPersonComponents(entity)) {
                Set<BindingPair> relevantBinding = localBindings.stream()
                        .filter(bp -> bp.getEntities().contains(e))
                        .collect(Collectors.toSet());
                assert relevantBinding.size() <= 1;
                Optional<BindingPair> binding = Optional.empty();
                if (relevantBinding.size() == 1) {
                    binding = Optional.of(relevantBinding.iterator().next());
                }
                findPlaceholder(e, binding);
            }
        }
    }
    
    /**
     * Finds placeholder for entity e. Entity e may already have a placeholder,
     * or may be bound to an entity that has a placeholder. If either is true,
     * bindingsToRoot will update to map e to that placeholder. Otherwise,
     * a new placeholder will be reserved for e, and bindingsToRoot will be
     * updated to map e to its new placeholder.
     * 
     * @param e entity for placeholder to be found
     * @param binding an optional containing the local binding of e if it has one, 
     * otherwise empty
     */
    private void findPlaceholder(Entity e, Optional<BindingPair> binding) {
        if (bindingsToRoot.containsKey(e)) {
            return;
        }
        
        if (binding.isPresent()) {
            for (Entity boundEntity : binding.get().getEntities()) {
                if (bindingsToRoot.containsKey(boundEntity)) {
                    Entity placeholder = bindingsToRoot.get(boundEntity).getPattern();
                    BindingPair newBindingPair = new BindingPair(placeholder, e, 1.0);
                    bindingsToRoot.put(e, newBindingPair);
                    return;
                }
            }
        }
        if (nextPlaceholderIndex >= Util.numPlaceholders()) {
            throw new RuntimeException("Ran out of placeholders while trying to standardize!");
        }
        Entity reservedPlaceholder = Util.getPlaceholder(nextPlaceholderIndex);
        bindingsToRoot.put(e, new BindingPair(reservedPlaceholder, e, 1.0));
        nextPlaceholderIndex++;
    }
    
    /**
     * @return a version of this node's entity with substituted name components such that no placeholder appears
     * more than once on the path from the root to this node
     */
    public Entity getSubstitutedEntity() {
        return substitutedEntity;
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public boolean isRoot() {
        return parent == null;
    }
    
    public ReasoningNode getParent() {
        return parent;
    }
    
    /**
     * @return bindings specific to this entity. no relevance to parents or children
     */
    public Set<BindingPair> getLocalBindings() {
        return new HashSet<>(localBindings);
    }
    
    /**
     * @return map of every base entity in every node along path to root to a unique
     * placeholder. entities that are equivalent (bound together) are mapped
     * to same placeholder
     */
    public Map<Entity, BindingPair> getBindingsToRoot() {
        return new HashMap<>(bindingsToRoot);
    }
    
    public List<ReasoningNode> getChildren() {
        return new ArrayList<>(children);
    }
    
    public void addChild(ReasoningNode child) {
        children.add(child);
    }
    
    public void clearChildren() {
        children.clear();
    }
    
    /**
     * @return number of nodes in the tree with this as root (includes root)
     */
    public int numNodesInTree() {
        return 1 + children.stream().mapToInt(ReasoningNode::numNodesInTree).sum();
    }
    
    /**
     * @return a String representation of the path from the root to this node
     */
    public String chainFromRoot() {
        if (isRoot()) {
            return substitutedEntity.toEnglish();
        }
        return parent.chainFromRoot() + " -> " + substitutedEntity.toEnglish();
    }
    
    /**
     * @return the depth of this node in the tree, where the root has depth 0
     */
    public int getDepth() {
        return isRoot() ? 0 : 1 + parent.getDepth();
    }
    
    /**
     * @return a Tree (JUNG library) version of the tree with this as root, preserving all 
     * existing nodes and edges
     */
    public Tree<ReasoningNode, String> getVisualGraph() {
        DelegateTree<ReasoningNode, String> t = new DelegateTree<>();
        t.addVertex(this);
        Queue<ReasoningNode> nodeQ = new LinkedList<>();
        nodeQ.add(this);
        int edgeCount = 0;
        while (!nodeQ.isEmpty()) {
            ReasoningNode curNode = nodeQ.remove();
            for (ReasoningNode child : curNode.children) {
                t.addChild(edgeCount+"", curNode, child);
                nodeQ.add(child);
                edgeCount++;
            }
        }
        return t;
    }
    
    @Override
    public String toString() {
        if (children.size() == 0) {
            return entity.toEnglish();
        }
        List<String> childrenStrs = children.stream()
                .map(c -> c.toString())
                .collect(Collectors.toList());
        String s = "";
        for (String childrenStr : childrenStrs) {
            for (String childrenChain : childrenStr.split("\n")) {
                s += entity.toEnglish() +" -> "+childrenChain+"\n";
            }
        }
        return s;
    }

    /**
     * Iterates over all the nodes in the tree in a breadth-first fashion.
     */
    @Override
    public Iterator<ReasoningNode> iterator() {
        Queue<ReasoningNode> nodeQ = new LinkedList<ReasoningNode>();
        nodeQ.add(this);
        return new Iterator<ReasoningNode>() {
            @Override
            public boolean hasNext() {
                return !nodeQ.isEmpty();
            }

            @Override
            public ReasoningNode next() {
                ReasoningNode node = nodeQ.remove();
                nodeQ.addAll(node.children);
                return node;
            }
        };
    }
}